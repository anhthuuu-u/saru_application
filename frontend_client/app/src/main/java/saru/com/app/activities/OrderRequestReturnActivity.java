package saru.com.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import saru.com.app.R;
import saru.com.app.connectors.OrderDetailAdapter;
import saru.com.app.models.OrderDetail;

public class OrderRequestReturnActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int MAX_IMAGE_COUNT = 3;
    private static final int STORAGE_PERMISSION_CODE = 1;

    private ImageView icBackArrow;
    private Button submitReturnButton;
    private ListView lvOrderDetail;
    private TextView txtShowOrderCode, txtShowOrderDate, txtShowStatus, txtAmountRefunded;
    private TextView txtShowName, txtShowPhoneNumber, txtShowAddress;
    private LinearLayout layoutSelectedImages;
    private RadioGroup requestTypeGroup, reasonGroup;
    private FirebaseFirestore db;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailList;
    private ArrayList<Uri> imageUriList;
    private String customerID;
    private String orderId;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_returnrequest);

        // Initialize Firebase and variables
        db = FirebaseFirestore.getInstance();
        orderDetailList = new ArrayList<>();
        imageUriList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(this, orderDetailList);

        // Initialize views
        icBackArrow = findViewById(R.id.ic_back_arrow);
        submitReturnButton = findViewById(R.id.return_submit_button);
        lvOrderDetail = findViewById(R.id.lvOrderDetail);
        txtShowOrderCode = findViewById(R.id.txtShowOrderCode);
        txtShowOrderDate = findViewById(R.id.txtShowOrderDate);
        txtShowStatus = findViewById(R.id.txtShowStatus);
        txtAmountRefunded = findViewById(R.id.txtAmountRefunded);
        txtShowName = findViewById(R.id.txtShowName);
        txtShowPhoneNumber = findViewById(R.id.txtShowPhoneNumber);
        txtShowAddress = findViewById(R.id.txtShowAddress);
        layoutSelectedImages = findViewById(R.id.layout_selected_images);
        requestTypeGroup = findViewById(R.id.request_type_group);
        reasonGroup = findViewById(R.id.reason_group);
        lvOrderDetail.setAdapter(orderDetailAdapter);
        submitReturnButton.setEnabled(false); // Disable until data is loaded

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

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

        layoutSelectedImages.setOnClickListener(v -> {
            if (imageUriList.size() < MAX_IMAGE_COUNT) {
                pickImageFromGallery();
            } else {
                showCustomToast(getString(R.string.max_images_error));
            }
        });

        submitReturnButton.setOnClickListener(v -> {
            if (!isDataLoaded) {
                showCustomToast("Please wait for order data to load.");
                return;
            }

            int selectedRequestTypeId = requestTypeGroup.getCheckedRadioButtonId();
            if (selectedRequestTypeId == -1) {
                showCustomToast("Please select a request type.");
                return;
            }
            String requestType = selectedRequestTypeId == R.id.radio_refunded ?
                    getString(R.string.return_request_exchange) :
                    getString(R.string.return_request_returned);

            int selectedReasonId = reasonGroup.getCheckedRadioButtonId();
            if (selectedReasonId == -1) {
                showCustomToast("Please select a reason for return.");
                return;
            }
            String reason;
            if (selectedReasonId == R.id.reason_wrong_item) {
                reason = getString(R.string.return_reason_wrong_item);
            } else if (selectedReasonId == R.id.reason_not_as_expected) {
                reason = getString(R.string.return_reason_not_as_expected);
            } else if (selectedReasonId == R.id.reason_not_meet_request) {
                reason = getString(R.string.return_reason_not_meet_request);
            } else if (selectedReasonId == R.id.reason_damaged_product) {
                reason = getString(R.string.return_reason_damaged_product);
            } else if (selectedReasonId == R.id.reason_wrong_item_sent) {
                reason = getString(R.string.return_reason_wrong_item_sent);
            } else if (selectedReasonId == R.id.reason_suspected_counterfeit) {
                reason = getString(R.string.return_reason_suspected_counterfeit);
            } else if (selectedReasonId == R.id.reason_no_longer_needed) {
                reason = getString(R.string.return_reason_no_longer_needed);
            } else {
                reason = "Unknown";
            }

            saveReturnRequest(orderId, customerID, requestType, reason);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                showCustomToast("Storage permission is required to select images.");
            }
        }
    }

    private void fetchOrderDetails(String orderId) {
        try {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
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

                                    // Fetch customer information from "customers"
                                    if (customerID != null) {
                                        db.collection("customers").document(customerID)
                                                .get()
                                                .addOnCompleteListener(customerTask -> {
                                                    try {
                                                        if (customerTask.isSuccessful()) {
                                                            DocumentSnapshot customerDoc = customerTask.getResult();
                                                            if (customerDoc != null && customerDoc.exists()) {
                                                                Object customerNameObj = customerDoc.get("CustomerName");
                                                                Object customerPhoneObj = customerDoc.get("CustomerPhone");
                                                                Object customerAddressObj = customerDoc.get("CustomerAdd");

                                                                String customerName = customerNameObj != null ? customerNameObj.toString() : null;
                                                                String customerPhone = customerPhoneObj != null ? customerPhoneObj.toString() : null;
                                                                String customerAddress = customerAddressObj != null ? customerAddressObj.toString() : null;

                                                                txtShowName.setText(customerName != null ? customerName : "Unknown");
                                                                txtShowPhoneNumber.setText(customerPhone != null ? customerPhone : "Unknown");
                                                                txtShowAddress.setText(customerAddress != null ? customerAddress : "Unknown");
                                                            } else {
                                                                txtShowName.setText("Unknown");
                                                                txtShowPhoneNumber.setText("Unknown");
                                                                txtShowAddress.setText("Unknown");
                                                                showCustomToast("Customer not found for ID: " + customerID);
                                                            }
                                                        } else {
                                                            txtShowName.setText("Unknown");
                                                            txtShowPhoneNumber.setText("Unknown");
                                                            txtShowAddress.setText("Unknown");
                                                            showCustomToast("Failed to load customer details: " + customerTask.getException().getMessage());
                                                        }
                                                    } catch (Exception e) {
                                                        txtShowName.setText("Unknown");
                                                        txtShowPhoneNumber.setText("Unknown");
                                                        txtShowAddress.setText("Unknown");
                                                        showCustomToast("Error loading customer: " + e.getMessage());
                                                    }
                                                });
                                    } else {
                                        txtShowName.setText("Unknown");
                                        txtShowPhoneNumber.setText("Unknown");
                                        txtShowAddress.setText("Unknown");
                                        showCustomToast("Customer ID is missing");
                                    }

                                    // Fetch order status from "orderstatuses"
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
                                                            showCustomToast("Order status not found for ID: " + orderStatusID);
                                                        }
                                                    } else {
                                                        txtShowStatus.setText("Unknown");
                                                        showCustomToast("Failed to load order status: " + statusTask.getException().getMessage());
                                                    }
                                                    fetchOrderProducts(orderId);
                                                    submitReturnButton.setEnabled(true);
                                                    isDataLoaded = true;
                                                });
                                    } else {
                                        txtShowStatus.setText("Unknown");
                                        showCustomToast("Order status ID is missing");
                                        fetchOrderProducts(orderId);
                                        submitReturnButton.setEnabled(true);
                                        isDataLoaded = true;
                                    }
                                } else {
                                    showCustomToast("Order not found.");
                                    txtShowOrderCode.setText("Unknown");
                                    txtShowOrderDate.setText("Unknown");
                                    txtShowStatus.setText("Unknown");
                                    txtAmountRefunded.setText("0.00 VND");
                                    txtShowName.setText("Unknown");
                                    txtShowPhoneNumber.setText("Unknown");
                                    txtShowAddress.setText("Unknown");
                                    submitReturnButton.setEnabled(true);
                                    isDataLoaded = true;
                                }
                            } else {
                                showCustomToast("Failed to load order details: " + task.getException().getMessage());
                                txtShowOrderCode.setText("Unknown");
                                txtShowOrderDate.setText("Unknown");
                                txtShowStatus.setText("Unknown");
                                txtAmountRefunded.setText("0.00 VND");
                                txtShowName.setText("Unknown");
                                txtShowPhoneNumber.setText("Unknown");
                                txtShowAddress.setText("Unknown");
                                submitReturnButton.setEnabled(true);
                                isDataLoaded = true;
                            }
                        } catch (Exception e) {
                            showCustomToast("Error loading order details: " + e.getMessage());
                            submitReturnButton.setEnabled(true);
                            isDataLoaded = true;
                        }
                    });
        } catch (Exception e) {
            showCustomToast("Error initiating order load: " + e.getMessage());
            submitReturnButton.setEnabled(true);
            isDataLoaded = true;
        }
    }

    private void fetchOrderProducts(String orderId) {
        try {
            db.collection("orderdetails")
                    .whereEqualTo("OrderID", orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                orderDetailList.clear();
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String productID = document.getString("ProductID");
                                        Long quantityLong = document.getLong("Quantity");
                                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                        // Skip invalid ProductIDs (e.g., promo1)
                                        if (productID == null || productID.startsWith("promo")) {
                                            continue; // Skip promotional items
                                        }

                                        db.collection("products").document(productID)
                                                .get()
                                                .addOnCompleteListener(productTask -> {
                                                    try {
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
                                                                                try {
                                                                                    if (imageTask.isSuccessful()) {
                                                                                        DocumentSnapshot imageDoc = imageTask.getResult();
                                                                                        String productImageCover = imageDoc != null ? imageDoc.getString("ProductImageCover") : null;

                                                                                        db.collection("productBrand").document(brandID)
                                                                                                .get()
                                                                                                .addOnCompleteListener(brandTask -> {
                                                                                                    try {
                                                                                                        if (brandTask.isSuccessful()) {
                                                                                                            DocumentSnapshot brandDoc = brandTask.getResult();
                                                                                                            String brandName = brandDoc != null ? brandDoc.getString("brandName") : "Unknown";
                                                                                                            OrderDetail orderDetail = new OrderDetail(productID, productName, quantity, productPrice, brandName);
                                                                                                            orderDetail.setProductImageCover(productImageCover);
                                                                                                            orderDetailList.add(orderDetail);
                                                                                                            orderDetailAdapter.notifyDataSetChanged();
                                                                                                        } else {
                                                                                                            showCustomToast("Error fetching brand details: " + brandTask.getException().getMessage());
                                                                                                        }
                                                                                                    } catch (
                                                                                                            Exception e) {
                                                                                                        showCustomToast("Error fetching brand details: " + e.getMessage());
                                                                                                    }
                                                                                                });
                                                                                    } else {
                                                                                        showCustomToast("Error fetching product image: " + imageTask.getException().getMessage());
                                                                                    }
                                                                                } catch (
                                                                                        Exception e) {
                                                                                    showCustomToast("Error fetching image details: " + e.getMessage());
                                                                                }
                                                                            });
                                                                } else {
                                                                    showCustomToast("Incomplete product data for product: " + productID);
                                                                }
                                                            } else {
                                                                showCustomToast("Product not found: " + productID);
                                                            }
                                                        } else {
                                                            showCustomToast("Error fetching product details: " + productTask.getException().getMessage());
                                                        }
                                                    } catch (Exception e) {
                                                        showCustomToast("Error fetching product details: " + e.getMessage());
                                                    }
                                                });
                                    }
                                } else {
                                    showCustomToast("No products found for this order.");
                                }
                            } else {
                                showCustomToast("Failed to load order products: " + task.getException().getMessage());
                            }
                        } catch (Exception e) {
                            showCustomToast("Error loading order products: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            showCustomToast("Error initiating product load: " + e.getMessage());
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null && !imageUriList.contains(imageUri)) {
                imageUriList.add(imageUri);
                displaySelectedImages();
            } else {
                showCustomToast("Invalid image selected.");
            }
        }
    }

    private void displaySelectedImages() {
        layoutSelectedImages.removeAllViews();
        if (imageUriList.isEmpty()) {
            // Restore placeholder with camera and video icons
            layoutSelectedImages.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            ImageView cameraIcon = new ImageView(this);
            cameraIcon.setImageResource(android.R.drawable.ic_menu_camera);
            cameraIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            cameraIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ImageView videoIcon = new ImageView(this);
            videoIcon.setImageResource(android.R.drawable.ic_media_play);
            videoIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            videoIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutSelectedImages.addView(cameraIcon);
            layoutSelectedImages.addView(videoIcon);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cameraIcon.getLayoutParams();
            params.setMargins(0, 0, 16, 0); // Margin between icons
            cameraIcon.setLayoutParams(params);
        } else {
            // Remove background and display selected images
            layoutSelectedImages.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            for (Uri imageUri : imageUriList) {
                View imageItemView = getLayoutInflater().inflate(R.layout.item_image_remove, null);
                ImageView imageView = imageItemView.findViewById(R.id.image_view);
                ImageView removeImageView = imageItemView.findViewById(R.id.remove_image_view);

                if (imageView != null && removeImageView != null) {
                    imageView.setImageURI(imageUri);
                    // Long press listener for image deletion confirmation
                    imageView.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(OrderRequestReturnActivity.this)
                                .setTitle("Delete Image")
                                .setMessage("Are you sure you want to delete this image?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    imageUriList.remove(imageUri);
                                    displaySelectedImages();
                                    showCustomToast("The image has been deleted.");
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    });
                    removeImageView.setOnClickListener(v -> {
                        imageUriList.remove(imageUri);
                        displaySelectedImages();
                    });
                    layoutSelectedImages.addView(imageItemView);
                } else {
                    showCustomToast("Error loading image preview.");
                }
            }
        }
    }

    private void updateAmountRefunded(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String formattedAmount = decimalFormat.format(amount) + " VND";
        txtAmountRefunded.setText(formattedAmount);
    }

    private void saveReturnRequest(String orderId, String customerID, String requestType, String reason) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting request...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("customerID", customerID);
        request.put("requestType", requestType);
        request.put("reason", reason);
        request.put("timestamp", System.currentTimeMillis());
        request.put("status", "pending");

        ArrayList<String> imageBase64List = new ArrayList<>();
        try {
            for (Uri imageUri : imageUriList) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                imageBase64List.add(base64Image);
            }
        } catch (IOException e) {
            progressDialog.dismiss();
            showCustomToast("Error processing images: " + e.getMessage());
            return;
        }

        request.put("images", imageBase64List);

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Double totalAmount = documentSnapshot.getDouble("totalAmount");
                    if (totalAmount != null) {
                        request.put("amountRefunded", totalAmount);

                        String requestId = UUID.randomUUID().toString();
                        String collectionName = requestType.equals(getString(R.string.return_request_exchange)) ?
                                "exchangeRequests" : "returnRequests";
                        db.collection(collectionName)
                                .document(requestId)
                                .set(request)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    showCustomToast(getString(R.string.return_submit_success));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    showCustomToast("Failed to submit request: " + e.getMessage());
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