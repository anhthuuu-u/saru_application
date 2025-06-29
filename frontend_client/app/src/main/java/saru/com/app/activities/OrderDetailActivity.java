package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.connectors.OrderDetailAdapter;
import saru.com.app.models.OrderDetail;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView txtShowOrderCode, txtShowOrderDate, txtShowStatus, txtShowName, txtShowPhoneNumber, txtShowAddress;
    private ListView lvOrderDetail;
    private Button btnCancelOrder, btnWriteReview, btnRequestReturn;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailList = new ArrayList<>();
    private FirebaseFirestore db;
    Button btn_write_review,btn_request_return,btn_cancel_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_orderdetail);

            // Initialize views
            lvOrderDetail = findViewById(R.id.lvOrderDetail);
            txtShowOrderCode = findViewById(R.id.txtShowOrderCode);
            txtShowOrderDate = findViewById(R.id.txtShowOrderDate);
            txtShowStatus = findViewById(R.id.txtShowStatus);
            txtShowName = findViewById(R.id.txtShowName);
            txtShowPhoneNumber = findViewById(R.id.txtShowPhoneNumber);
            txtShowAddress = findViewById(R.id.txtShowAddress);

            btn_write_review =findViewById(R.id.btn_write_review);
            btn_request_return = findViewById(R.id.btn_request_return);
            btn_cancel_order = findViewById(R.id.btn_cancel_order);

            btnCancelOrder = findViewById(R.id.btn_cancel_order);
            btnWriteReview = findViewById(R.id.btn_write_review);
            btnRequestReturn = findViewById(R.id.btn_request_return);



           addEvent();

                    // Set up back button functionality
                    ImageView backIcon = findViewById(R.id.ic_back_arrow);
            if (backIcon != null) {
                backIcon.setOnClickListener(v -> {
                    Log.d("OrderDetailActivity", "Back button clicked");
                    onBackPressed();
                });
            } else {
                Log.e("OrderDetailActivity", "Back icon not found in layout");
            }

            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            // Set adapter for ListView
            orderDetailAdapter = new OrderDetailAdapter(OrderDetailActivity.this, orderDetailList);
            lvOrderDetail.setAdapter(orderDetailAdapter);

            // Get the order ID from Intent
            String orderID = getIntent().getStringExtra("ORDER_ID");
            if (orderID != null) {
                Log.d("OrderDetailActivity", "Received OrderID: " + orderID);
                fetchOrderDetails(orderID);
            } else {
                Log.e("OrderDetailActivity", "OrderID is null");
                Toast.makeText(this, "Invalid order ID", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to initialize activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void addEvent() {
        btn_write_review.setOnClickListener(v -> openReview());
        btn_request_return.setOnClickListener(v -> openRequest());
        btn_cancel_order.setOnClickListener(v -> openCancel());

    }

    void openReview() {
        Intent intent = new Intent(OrderDetailActivity.this, OrderReviewActivity.class);
        startActivity(intent);
    }

    void openRequest() {
        Intent intent = new Intent(OrderDetailActivity.this, OrderRequestReturnActivity.class);
        startActivity(intent);
    }

    void openCancel() {
        Intent intent = new Intent(OrderDetailActivity.this, OrderCancelActivity.class);
        startActivity(intent);
    }
    private void fetchOrderDetails(String orderID) {
        try {
            // Fetch the order from the "orders" collection
            db.collection("orders").document(orderID)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    // Log entire document data for debugging
                                    Log.d("OrderDetailActivity", "Order document data: " + document.getData());

                                    String orderDate = document.getString("OrderDate");
                                    // Use get() to handle any type for OrderStatusID
                                    Object orderStatusIDObj = document.get("OrderStatusID");
                                    String orderStatusID = orderStatusIDObj != null ? orderStatusIDObj.toString() : null;
                                    String customerID = document.getString("CustomerID");

                                    // Log fetched data
                                    Log.d("OrderDetailActivity", "OrderDate: " + orderDate);
                                    Log.d("OrderDetailActivity", "OrderStatusID: " + orderStatusID);
                                    Log.d("OrderDetailActivity", "CustomerID: " + customerID);

                                    // Set order information
                                    txtShowOrderCode.setText(orderID != null ? orderID : "Unknown");
                                    txtShowOrderDate.setText(orderDate != null ? orderDate : "Unknown");

                                    // Update button visibility and clickability based on orderStatusID
                                    updateButtonVisibility(orderID, orderStatusID);

                                    // Fetch order status from "orderstatuses"
                                    if (orderStatusID != null) {
                                        db.collection("orderstatuses").document(orderStatusID)
                                                .get()
                                                .addOnCompleteListener(statusTask -> {
                                                    try {
                                                        if (statusTask.isSuccessful()) {
                                                            DocumentSnapshot statusDoc = statusTask.getResult();
                                                            if (statusDoc != null && statusDoc.exists()) {
                                                                String orderStatus = statusDoc.getString("Status");
                                                                txtShowStatus.setText(orderStatus != null ? orderStatus : "Unknown");
                                                                Log.d("OrderDetailActivity", "OrderStatus: " + orderStatus);
                                                            } else {
                                                                txtShowStatus.setText("Unknown");
                                                                Log.e("OrderDetailActivity", "Order status document does not exist for OrderStatusID: " + orderStatusID);
                                                                Toast.makeText(OrderDetailActivity.this, "Order status not found for ID: " + orderStatusID, Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            txtShowStatus.setText("Unknown");
                                                            Log.e("OrderDetailActivity", "Error fetching order status: " + statusTask.getException());
                                                            Toast.makeText(OrderDetailActivity.this, "Failed to load order status: " + statusTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("OrderDetailActivity", "Exception in fetching order status: " + e.getMessage(), e);
                                                        txtShowStatus.setText("Unknown");
                                                        Toast.makeText(OrderDetailActivity.this, "Error loading status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        txtShowStatus.setText("Unknown");
                                        Log.e("OrderDetailActivity", "OrderStatusID is null for orderID: " + orderID);
                                        Toast.makeText(OrderDetailActivity.this, "Order status ID is missing", Toast.LENGTH_LONG).show();
                                    }

                                    // Fetch customer information from "orders"
                                    if (orderID != null) { // Make sure you have orderID available, not customerID
                                        db.collection("orders").document(orderID)
                                                .get()
                                                .addOnCompleteListener(orderTask -> {
                                                    try {
                                                        if (orderTask.isSuccessful()) {
                                                            DocumentSnapshot orderDoc = orderTask.getResult();
                                                            if (orderDoc != null && orderDoc.exists()) {
                                                                // Log order document data
                                                                Log.d("OrderDetailActivity", "Order document data: " + orderDoc.getData());

                                                                // Use get() to handle any type for CustomerName, CustomerPhone, CustomerAdd from the order document
                                                                Object customerNameObj = orderDoc.get("customerName"); // Assuming field names in "orders" are "customerName"
                                                                Object customerPhoneObj = orderDoc.get("customerPhone"); // Assuming field names in "orders" are "customerPhone"
                                                                Object customerAddressObj = orderDoc.get("customerAddress"); // Assuming field names in "orders" are "customerAddress"

                                                                String customerName = customerNameObj != null ? customerNameObj.toString() : null;
                                                                String customerPhone = customerPhoneObj != null ? customerPhoneObj.toString() : null;
                                                                String customerAddress = customerAddressObj != null ? customerAddressObj.toString() : null;

                                                                // Log customer data
                                                                Log.d("OrderDetailActivity", "CustomerName: " + customerName);
                                                                Log.d("OrderDetailActivity", "CustomerPhone: " + customerPhone);
                                                                Log.d("OrderDetailActivity", "CustomerAddress: " + customerAddress);

                                                                // Set customer information
                                                                txtShowName.setText(customerName != null ? customerName : "Unknown");
                                                                txtShowPhoneNumber.setText(customerPhone != null ? customerPhone : "Unknown");
                                                                txtShowAddress.setText(customerAddress != null ? customerAddress : "Unknown");
                                                            } else {
                                                                txtShowName.setText("Unknown");
                                                                txtShowPhoneNumber.setText("Unknown");
                                                                txtShowAddress.setText("Unknown");
                                                                Log.e("OrderDetailActivity", "Order document does not exist for OrderID: " + orderID);
                                                                Toast.makeText(OrderDetailActivity.this, "Order not found for ID: " + orderID, Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            txtShowName.setText("Unknown");
                                                            txtShowPhoneNumber.setText("Unknown");
                                                            txtShowAddress.setText("Unknown");
                                                            Log.e("OrderDetailActivity", "Error fetching order details: " + orderTask.getException());
                                                            Toast.makeText(OrderDetailActivity.this, "Failed to load order details: " + orderTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("OrderDetailActivity", "Exception in fetching order details: " + e.getMessage(), e);
                                                        txtShowName.setText("Unknown");
                                                        txtShowPhoneNumber.setText("Unknown");
                                                        txtShowAddress.setText("Unknown");
                                                        Toast.makeText(OrderDetailActivity.this, "Error loading order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        txtShowName.setText("Unknown");
                                        txtShowPhoneNumber.setText("Unknown");
                                        txtShowAddress.setText("Unknown");
                                        Log.e("OrderDetailActivity", "OrderID is null."); // Changed from CustomerID to OrderID
                                        Toast.makeText(OrderDetailActivity.this, "Order ID is missing", Toast.LENGTH_LONG).show();
                                    }

                                    // Fetch order products
                                    fetchOrderProducts(orderID);
                                } else {
                                    Log.e("OrderDetailActivity", "Order document does not exist for orderID: " + orderID);
                                    Toast.makeText(OrderDetailActivity.this, "Order not found for ID: " + orderID, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.e("OrderDetailActivity", "Error fetching order: " + task.getException());
                                Toast.makeText(OrderDetailActivity.this, "Failed to load order: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("OrderDetailActivity", "Exception in fetchOrderDetails: " + e.getMessage(), e);
                            Toast.makeText(OrderDetailActivity.this, "Error loading order details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Error initiating fetchOrderDetails: " + e.getMessage(), e);
            Toast.makeText(OrderDetailActivity.this, "Error initiating order load: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateButtonVisibility(String orderID, String orderStatusID) {
        try {
            // Convert orderStatusID to integer for comparison
            int statusID = orderStatusID != null ? Integer.parseInt(orderStatusID) : -1;

            // Show and enable Cancel button for status 0 or 1
            if (statusID == 0 || statusID == 1) {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setEnabled(true);
                btnWriteReview.setVisibility(View.GONE);
                btnWriteReview.setEnabled(false);
                btnRequestReturn.setVisibility(View.GONE);
                btnRequestReturn.setEnabled(false);
            }
            // Show and enable Review and Return buttons for status 4
            else if (statusID == 4) {
                btnCancelOrder.setVisibility(View.GONE);
                btnCancelOrder.setEnabled(false);
                btnWriteReview.setVisibility(View.VISIBLE);
                btnWriteReview.setEnabled(true);
                btnRequestReturn.setVisibility(View.VISIBLE);
                btnRequestReturn.setEnabled(true);
            }
            // Hide and disable all buttons for other statuses
            else {
                btnCancelOrder.setVisibility(View.GONE);
                btnCancelOrder.setEnabled(false);
                btnWriteReview.setVisibility(View.GONE);
                btnWriteReview.setEnabled(false);
                btnRequestReturn.setVisibility(View.GONE);
                btnRequestReturn.setEnabled(false);
            }

            // Set click listeners for buttons
            btnCancelOrder.setOnClickListener(v -> {
                Intent intent = new Intent(OrderDetailActivity.this, OrderCancelActivity.class);
                intent.putExtra("ORDER_ID", orderID);
                startActivity(intent);
            });

            btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(OrderDetailActivity.this, OrderReviewActivity.class);
                intent.putExtra("ORDER_ID", orderID);
                startActivity(intent);
            });

            btnRequestReturn.setOnClickListener(v -> {
                Intent intent = new Intent(OrderDetailActivity.this, OrderRequestReturnActivity.class);
                intent.putExtra("ORDER_ID", orderID);
                startActivity(intent);
            });

        } catch (NumberFormatException e) {
            Log.e("OrderDetailActivity", "Invalid OrderStatusID format: " + orderStatusID, e);
            Toast.makeText(OrderDetailActivity.this, "Invalid order status ID", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchOrderProducts(String orderID) {
        try {
            // Fetch order details (products) from "orderdetails"
            db.collection("orderdetails")
                    .whereEqualTo("OrderID", orderID)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                // Clear the list before adding new items
                                orderDetailList.clear();

                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    int totalItems = task.getResult().size();
                                    int[] completedItems = {0}; // Counter for completed items

                                    for (DocumentSnapshot document : task.getResult()) {
                                        String productID = document.getString("ProductID");
                                        Long quantityLong = document.getLong("Quantity");
                                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                        // Log order detail data
                                        Log.d("OrderDetailActivity", "ProductID: " + productID + ", Quantity: " + quantity);

                                        // Fetch product details from the "products" collection
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

                                                                // Log product data
                                                                Log.d("OrderDetailActivity", "ProductName: " + productName + ", Price: " + productPrice);

                                                                // Fetch image details from "image" collection
                                                                db.collection("image").document(imageID)
                                                                        .get()
                                                                        .addOnCompleteListener(imageTask -> {
                                                                            try {
                                                                                if (imageTask.isSuccessful()) {
                                                                                    DocumentSnapshot imageDoc = imageTask.getResult();
                                                                                    String productImageCover = imageDoc != null ? imageDoc.getString("ProductImageCover") : null;
                                                                                    Log.d("OrderDetailActivity", "ProductImageCover: " + productImageCover);

                                                                                    // Fetch brand details from "productBrand" collection
                                                                                    db.collection("productBrand").document(brandID)
                                                                                            .get()
                                                                                            .addOnCompleteListener(brandTask -> {
                                                                                                try {
                                                                                                    if (brandTask.isSuccessful()) {
                                                                                                        DocumentSnapshot brandDoc = brandTask.getResult();
                                                                                                        String brandName = brandDoc != null ? brandDoc.getString("brandName") : "Unknown";
                                                                                                        Log.d("OrderDetailActivity", "BrandName: " + brandName);

                                                                                                        // Create a new OrderDetail object
                                                                                                        OrderDetail orderDetail = new OrderDetail(productID, productName, quantity, productPrice != null ? productPrice : 0.0, brandName);
                                                                                                        orderDetail.setProductImageCover(productImageCover);

                                                                                                        // Add to list and update adapter
                                                                                                        orderDetailList.add(orderDetail);
                                                                                                        orderDetailAdapter.notifyDataSetChanged();

                                                                                                        // Increment completed items
                                                                                                        completedItems[0]++;
                                                                                                        // Check if all items are processed
                                                                                                        if (completedItems[0] == totalItems) {
                                                                                                            // All items are processed, set ListView height
                                                                                                            runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("OrderDetailActivity", "Error fetching brand details: " + brandTask.getException());
                                                                                                        completedItems[0]++;
                                                                                                        if (completedItems[0] == totalItems) {
                                                                                                            runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                                                        }
                                                                                                    }
                                                                                                } catch (Exception e) {
                                                                                                    Log.e("OrderDetailActivity", "Exception in fetching brand details: " + e.getMessage(), e);
                                                                                                    completedItems[0]++;
                                                                                                    if (completedItems[0] == totalItems) {
                                                                                                        runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    Log.e("OrderDetailActivity", "Error fetching product image: " + imageTask.getException());
                                                                                    completedItems[0]++;
                                                                                    if (completedItems[0] == totalItems) {
                                                                                        runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                                    }
                                                                                }
                                                                            } catch (Exception e) {
                                                                                Log.e("OrderDetailActivity", "Exception in fetching image details: " + e.getMessage(), e);
                                                                                completedItems[0]++;
                                                                                if (completedItems[0] == totalItems) {
                                                                                    runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                                }
                                                                            }
                                                                        });
                                                            } else {
                                                                Log.e("OrderDetailActivity", "Product document does not exist for ProductID: " + productID);
                                                                completedItems[0]++;
                                                                if (completedItems[0] == totalItems) {
                                                                    runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                                }
                                                            }
                                                        } else {
                                                            Log.e("OrderDetailActivity", "Error fetching product details: " + productTask.getException());
                                                            completedItems[0]++;
                                                            if (completedItems[0] == totalItems) {
                                                                runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("OrderDetailActivity", "Exception in fetching product details: " + e.getMessage(), e);
                                                        completedItems[0]++;
                                                        if (completedItems[0] == totalItems) {
                                                            runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(OrderDetailActivity.this, "No products found for this order.", Toast.LENGTH_LONG).show();
                                    Log.d("OrderDetailActivity", "No order details found for OrderID: " + orderID);
                                    // Set height even if no items are found to ensure ListView is updated
                                    runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                                }
                            } else {
                                Log.e("OrderDetailActivity", "Error fetching order details: " + task.getException());
                                Toast.makeText(OrderDetailActivity.this, "Failed to load order products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                            }
                        } catch (Exception e) {
                            Log.e("OrderDetailActivity", "Exception in fetchOrderProducts: " + e.getMessage(), e);
                            Toast.makeText(OrderDetailActivity.this, "Error loading order products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
                        }
                    });
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Error initiating fetchOrderProducts: " + e.getMessage(), e);
            Toast.makeText(OrderDetailActivity.this, "Error initiating product load: " + e.getMessage(), Toast.LENGTH_LONG).show();
            runOnUiThread(() -> setListViewHeightBasedOnItems(lvOrderDetail));
        }
    }

    private void setListViewHeightBasedOnItems(ListView listView) {
        OrderDetailAdapter adapter = (OrderDetailAdapter) listView.getAdapter();
        if (adapter == null || adapter.getCount() == 0) {
            // Set a minimum height or make ListView invisible if no items
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = 0; // Or set a minimum height if desired
            listView.setLayoutParams(params);
            listView.requestLayout();
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}