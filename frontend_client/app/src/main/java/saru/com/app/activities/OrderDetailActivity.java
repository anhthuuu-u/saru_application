package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailList = new ArrayList<>();
    private FirebaseFirestore db;

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

    private void fetchOrderDetails(String orderID) {
        try {
            db.collection("orders").document(orderID)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Log.d("OrderDetailActivity", "Order document data: " + document.getData());

                                    String orderDate = document.getString("OrderDate");
                                    Object orderStatusIDObj = document.get("OrderStatusID");
                                    String orderStatusID = orderStatusIDObj != null ? orderStatusIDObj.toString() : null;
                                    String customerID = document.getString("CustomerID");

                                    Log.d("OrderDetailActivity", "OrderDate: " + orderDate);
                                    Log.d("OrderDetailActivity", "OrderStatusID: " + orderStatusID);
                                    Log.d("OrderDetailActivity", "CustomerID: " + customerID);

                                    txtShowOrderCode.setText(orderID != null ? orderID : "Unknown");
                                    txtShowOrderDate.setText(orderDate != null ? orderDate : "Unknown");

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

                                    if (customerID != null) {
                                        db.collection("customers").document(customerID)
                                                .get()
                                                .addOnCompleteListener(customerTask -> {
                                                    try {
                                                        if (customerTask.isSuccessful()) {
                                                            DocumentSnapshot customerDoc = customerTask.getResult();
                                                            if (customerDoc != null && customerDoc.exists()) {
                                                                Log.d("OrderDetailActivity", "Customer document data: " + customerDoc.getData());

                                                                Object customerNameObj = customerDoc.get("CustomerName");
                                                                Object customerPhoneObj = customerDoc.get("CustomerPhone");
                                                                Object customerAddressObj = customerDoc.get("CustomerAdd");

                                                                String customerName = customerNameObj != null ? customerNameObj.toString() : null;
                                                                String customerPhone = customerPhoneObj != null ? customerPhoneObj.toString() : null;
                                                                String customerAddress = customerAddressObj != null ? customerAddressObj.toString() : null;

                                                                Log.d("OrderDetailActivity", "CustomerName: " + customerName);
                                                                Log.d("OrderDetailActivity", "CustomerPhone: " + customerPhone);
                                                                Log.d("OrderDetailActivity", "CustomerAddress: " + customerAddress);

                                                                txtShowName.setText(customerName != null ? customerName : "Unknown");
                                                                txtShowPhoneNumber.setText(customerPhone != null ? customerPhone : "Unknown");
                                                                txtShowAddress.setText(customerAddress != null ? customerAddress : "Unknown");
                                                            } else {
                                                                txtShowName.setText("Unknown");
                                                                txtShowPhoneNumber.setText("Unknown");
                                                                txtShowAddress.setText("Unknown");
                                                                Log.e("OrderDetailActivity", "Customer document does not exist for CustomerID: " + customerID);
                                                                Toast.makeText(OrderDetailActivity.this, "Customer not found for ID: " + customerID, Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            txtShowName.setText("Unknown");
                                                            txtShowPhoneNumber.setText("Unknown");
                                                            txtShowAddress.setText("Unknown");
                                                            Log.e("OrderDetailActivity", "Error fetching customer details: " + customerTask.getException());
                                                            Toast.makeText(OrderDetailActivity.this, "Failed to load customer details: " + customerTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("OrderDetailActivity", "Exception in fetching customer details: " + e.getMessage(), e);
                                                        txtShowName.setText("Unknown");
                                                        txtShowPhoneNumber.setText("Unknown");
                                                        txtShowAddress.setText("Unknown");
                                                        Toast.makeText(OrderDetailActivity.this, "Error loading customer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        txtShowName.setText("Unknown");
                                        txtShowPhoneNumber.setText("Unknown");
                                        txtShowAddress.setText("Unknown");
                                        Log.e("OrderDetailActivity", "CustomerID is null for orderID: " + orderID);
                                        Toast.makeText(OrderDetailActivity.this, "Customer ID is missing", Toast.LENGTH_LONG).show();
                                    }

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

    private void fetchOrderProducts(String orderID) {
        try {
            db.collection("orderdetails")
                    .whereEqualTo("OrderID", orderID)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                orderDetailList.clear();

                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String productID = document.getString("ProductID");
                                        Long quantityLong = document.getLong("Quantity");
                                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                        Log.d("OrderDetailActivity", "ProductID: " + productID + ", Quantity: " + quantity);

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

                                                                Log.d("OrderDetailActivity", "ProductName: " + productName + ", Price: " + productPrice);

                                                                db.collection("image").document(imageID)
                                                                        .get()
                                                                        .addOnCompleteListener(imageTask -> {
                                                                            try {
                                                                                if (imageTask.isSuccessful()) {
                                                                                    DocumentSnapshot imageDoc = imageTask.getResult();
                                                                                    String productImageCover = imageDoc != null ? imageDoc.getString("ProductImageCover") : null;
                                                                                    Log.d("OrderDetailActivity", "ProductImageCover: " + productImageCover);

                                                                                    db.collection("productBrand").document(brandID)
                                                                                            .get()
                                                                                            .addOnCompleteListener(brandTask -> {
                                                                                                try {
                                                                                                    if (brandTask.isSuccessful()) {
                                                                                                        DocumentSnapshot brandDoc = brandTask.getResult();
                                                                                                        String brandName = brandDoc != null ? brandDoc.getString("brandName") : "Unknown";
                                                                                                        Log.d("OrderDetailActivity", "BrandName: " + brandName);

                                                                                                        OrderDetail orderDetail = new OrderDetail(productID, productName, quantity, productPrice != null ? productPrice : 0.0, brandName);
                                                                                                        orderDetail.setProductImageCover(productImageCover);

                                                                                                        orderDetailList.add(orderDetail);
                                                                                                        orderDetailAdapter.notifyDataSetChanged();

                                                                                                        // Set ListView height dynamically after data is added
                                                                                                        if (orderDetailList.size() == task.getResult().size()) {
                                                                                                            setListViewHeightBasedOnItems(lvOrderDetail);
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("OrderDetailActivity", "Error fetching brand details: " + brandTask.getException());
                                                                                                    }
                                                                                                } catch (Exception e) {
                                                                                                    Log.e("OrderDetailActivity", "Exception in fetching brand details: " + e.getMessage(), e);
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    Log.e("OrderDetailActivity", "Error fetching product image: " + imageTask.getException());
                                                                                }
                                                                            } catch (Exception e) {
                                                                                Log.e("OrderDetailActivity", "Exception in fetching image details: " + e.getMessage(), e);
                                                                            }
                                                                        });
                                                            } else {
                                                                Log.e("OrderDetailActivity", "Product document does not exist for ProductID: " + productID);
                                                            }
                                                        } else {
                                                            Log.e("OrderDetailActivity", "Error fetching product details: " + productTask.getException());
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("OrderDetailActivity", "Exception in fetching product details: " + e.getMessage(), e);
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(OrderDetailActivity.this, "No products found for this order.", Toast.LENGTH_LONG).show();
                                    Log.d("OrderDetailActivity", "No order details found for OrderID: " + orderID);
                                }
                            } else {
                                Log.e("OrderDetailActivity", "Error fetching order details: " + task.getException());
                                Toast.makeText(OrderDetailActivity.this, "Failed to load order products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("OrderDetailActivity", "Exception in fetchOrderProducts: " + e.getMessage(), e);
                            Toast.makeText(OrderDetailActivity.this, "Error loading order products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Error initiating fetchOrderProducts: " + e.getMessage(), e);
            Toast.makeText(OrderDetailActivity.this, "Error initiating product load: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setListViewHeightBasedOnItems(ListView listView) {
        OrderDetailAdapter adapter = (OrderDetailAdapter) listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}