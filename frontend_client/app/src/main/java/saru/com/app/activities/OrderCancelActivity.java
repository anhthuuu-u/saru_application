package saru.com.app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import saru.com.app.R;
import saru.com.app.connectors.OrderDetailAdapter;
import saru.com.app.models.OrderDetail;

public class OrderCancelActivity extends AppCompatActivity {

    private ImageView icBackArrow;
    private Button submitButton;
    private ListView lvOrderDetail;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView txtShowOrderCode, txtShowOrderDate, txtShowStatus, txtAmountRefunded;
    private String customerID;
    private String orderId;
    private EditText otherReasonInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cancel);

        // Initialize Firebase and variables
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        orderDetailList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(this, orderDetailList);

        // Initialize views
        icBackArrow = findViewById(R.id.ic_back_arrow);
        submitButton = findViewById(R.id.cancel_submit_button);
        lvOrderDetail = findViewById(R.id.lvOrderDetail);
        txtShowOrderCode = findViewById(R.id.txtShowOrderCode);
        txtShowOrderDate = findViewById(R.id.txtShowOrderDate);
        txtShowStatus = findViewById(R.id.txtShowStatus);
        txtAmountRefunded = findViewById(R.id.txtAmountRefunded);
        lvOrderDetail.setAdapter(orderDetailAdapter);

        // Get orderId from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            showCustomToast("Invalid order ID. Please try again.");
            finish();
            return;
        }

        // Fetch order details
        fetchOrderDetails(orderId);

        // Set click listeners
        icBackArrow.setOnClickListener(v -> finish());

        // Xử lý khi bấm nút Gửi yêu cầu hủy đơn
        submitButton.setOnClickListener(v -> {
            RadioGroup reasonGroup = findViewById(R.id.reason_group);
            int selectedId = reasonGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                showCustomToast("Please select a reason for cancellation.");
                return;
            }
            String reason = "";
            if (selectedId == R.id.reason_no_need) {
                reason = getString(R.string.cancel_reason_no_need);
            } else if (selectedId == R.id.reason_change_address) {
                reason = getString(R.string.cancel_reason_change_address);
            } else if (selectedId == R.id.reason_not_meet_expectation) {
                reason = getString(R.string.cancel_reason_not_meet_expectation);
            } else if (selectedId == R.id.reason_enter_voucher) {
                reason = getString(R.string.cancel_reason_enter_voucher);
            } else if (selectedId == R.id.reason_edit_items) {
                reason = getString(R.string.cancel_reason_edit_items);
                if (reason.isEmpty()) {
                    showCustomToast("Please specify other reason.");
                    return;
                }
            }
            saveCancelRequest(orderId, customerID, reason);
        });
    }

    private void fetchOrderDetails(String orderId) {
        db.collection("orders").document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String orderDate = document.getString("OrderDate");
                            Object orderStatusIDObj = document.get("OrderStatusID");
                            String orderStatusID = orderStatusIDObj != null ? orderStatusIDObj.toString() : null;
                            customerID = document.getString("CustomerID");
                            Double totalAmount = document.getDouble("totalAmount");

                            if (customerID == null || customerID.isEmpty()) {
                                showCustomToast("Customer ID not found for this order.");
                                customerID = "Unknown";
                            }

                            txtShowOrderCode.setText(orderId != null ? orderId : "Unknown");
                            txtShowOrderDate.setText(orderDate != null ? orderDate : "Unknown");

                            if (totalAmount != null) {
                                updateAmountRefunded(totalAmount);
                            } else {
                                txtAmountRefunded.setText("0.00 VND");
                                showCustomToast("Total amount not found for this order.");
                            }

                            if (orderStatusID != null) {
                                db.collection("orderstatuses").document(orderStatusID)
                                        .get()
                                        .addOnCompleteListener(statusTask -> {
                                            if (statusTask.isSuccessful()) {
                                                DocumentSnapshot statusDoc = statusTask.getResult();
                                                if (statusDoc != null && statusDoc.exists()) {
                                                    String orderStatus = statusDoc.getString("Status");
                                                    txtShowStatus.setText(orderStatus != null ? orderStatus : "Unknown");
                                                } else {
                                                    txtShowStatus.setText("Unknown");
                                                }
                                            } else {
                                                txtShowStatus.setText("Unknown");
                                                showCustomToast("Failed to load order status.");
                                            }
                                            fetchOrderProducts(orderId);
                                        });
                            } else {
                                txtShowStatus.setText("Unknown");
                                fetchOrderProducts(orderId);
                            }
                        } else {
                            showCustomToast("Order not found.");
                            txtShowOrderCode.setText("Unknown");
                            txtShowOrderDate.setText("Unknown");
                            txtShowStatus.setText("Unknown");
                            txtAmountRefunded.setText("0.00 VND");
                        }
                    } else {
                        showCustomToast("Failed to load order details: " + task.getException().getMessage());
                        txtShowOrderCode.setText("Unknown");
                        txtShowOrderDate.setText("Unknown");
                        txtShowStatus.setText("Unknown");
                        txtAmountRefunded.setText("0.00 VND");
                    }
                });
    }

    private void fetchOrderProducts(String orderId) {
        db.collection("orderdetails")
                .whereEqualTo("OrderID", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderDetailList.clear();
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String productID = document.getString("ProductID");
                                Long quantityLong = document.getLong("Quantity");
                                int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                if (productID != null) {
                                    db.collection("products").document(productID)
                                            .get()
                                            .addOnCompleteListener(productTask -> {
                                                if (productTask.isSuccessful()) {
                                                    DocumentSnapshot productDoc = productTask.getResult();
                                                    if (productDoc != null && productDoc.exists()) {
                                                        String productName = productDoc.getString("productName");
                                                        Double productPrice = productDoc.getDouble("productPrice");
                                                        String imageID = productDoc.getString("imageID");
                                                        String brandID = productDoc.getString("brandID");

                                                        if (productName != null && productPrice != null && imageID != null && brandID != null) {
                                                            db.collection("image").document(imageID)
                                                                    .get()
                                                                    .addOnCompleteListener(imageTask -> {
                                                                        if (imageTask.isSuccessful()) {
                                                                            DocumentSnapshot imageDoc = imageTask.getResult();
                                                                            String productImageCover = imageDoc != null ? imageDoc.getString("ProductImageCover") : null;

                                                                            db.collection("productBrand").document(brandID)
                                                                                    .get()
                                                                                    .addOnCompleteListener(brandTask -> {
                                                                                        if (brandTask.isSuccessful()) {
                                                                                            DocumentSnapshot brandDoc = brandTask.getResult();
                                                                                            String brandName = brandDoc != null ? brandDoc.getString("brandName") : "Unknown";
                                                                                            if (brandName != null) {
                                                                                                OrderDetail orderDetail = new OrderDetail(productID, productName, quantity, productPrice, brandName);
                                                                                                orderDetail.setProductImageCover(productImageCover);
                                                                                                orderDetailList.add(orderDetail);
                                                                                                orderDetailAdapter.notifyDataSetChanged();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    private void updateAmountRefunded(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String formattedAmount = decimalFormat.format(amount) + " VND";
        txtAmountRefunded.setText(formattedAmount);
    }

    private void saveCancelRequest(String orderId, String customerID, String reason) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting cancel request...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("orderId", orderId);
        cancelRequest.put("customerID", customerID);
        cancelRequest.put("reason", reason);
        cancelRequest.put("timestamp", System.currentTimeMillis());
        cancelRequest.put("status", "pending");

        // Lấy totalAmount từ orders
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double totalAmount = documentSnapshot.getDouble("totalAmount");
                    if (totalAmount != null) {
                        cancelRequest.put("amountRefunded", totalAmount);

                        // Thêm document vào collection cancelRequests với ID duy nhất
                        String cancelRequestId = UUID.randomUUID().toString();
                        db.collection("cancelRequests")
                                .document(cancelRequestId)
                                .set(cancelRequest)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    showCustomToast("Cancel request submitted successfully.");
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    showCustomToast("Failed to submit cancel request: " + e.getMessage());
                                });
                    } else {
                        progressDialog.dismiss();
                        showCustomToast("Total amount not found for this order.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showCustomToast("Failed to fetch order details: " + e.getMessage());
                });
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.tv_toast_message);
        if (toastText != null) {
            toastText.setText(message);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}