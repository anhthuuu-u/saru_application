package saru.com.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.connectors.OrderAdapter;
import saru.com.app.models.Order;

public class OrderListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListView lvOrder;
    OrderAdapter orderAdapter;
    List<Order> orderList = new ArrayList<>();
    List<Order> allOrders = new ArrayList<>(); // Lưu tất cả đơn hàng để lọc

    // UI elements
    TextView txtOrderID, txtOrderDate, txtTotalProduct, txtTotalValue;
    Button btnOrderStatus;
    TextView tabAll, tabConfirming, tabConfirmed, tabInTransit, tabComplete, tabCanceled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        lvOrder = findViewById(R.id.lvOrder);

        initializeFirebase();
        initializeViews();
        setupTabListeners();

        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Fetch and display orders
        fetchOrdersForUser();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        txtOrderID = findViewById(R.id.txtOrderID);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtTotalProduct = findViewById(R.id.txtTotalProduct);
        txtTotalValue = findViewById(R.id.txtTotalValue);
        if (txtTotalValue == null) {
            Log.e("OrderListActivity", "txtTotalValue is null");
        }
        btnOrderStatus = findViewById(R.id.btnOrderStatus);
        // Initialize tabs
        tabAll = findViewById(R.id.tab_all);
        tabConfirming = findViewById(R.id.tab_confirming);
        tabConfirmed = findViewById(R.id.tab_confirmed);
        tabInTransit = findViewById(R.id.tab_intransit);
        tabComplete = findViewById(R.id.tab_complete);
        tabCanceled = findViewById(R.id.tab_canceled);
    }

    private void setupTabListeners() {
        tabAll.setOnClickListener(v -> filterOrdersByStatus(null)); // null để hiển thị tất cả
        tabConfirming.setOnClickListener(v -> filterOrdersByStatus("0")); // Pending confirmation
        tabConfirmed.setOnClickListener(v -> filterOrdersByStatus("1")); // Confirmed
        tabInTransit.setOnClickListener(v -> filterOrdersByStatus("3")); // In transit
        tabComplete.setOnClickListener(v -> filterOrdersByStatus("4")); // Delivery successful
        tabCanceled.setOnClickListener(v -> filterOrdersByStatus("2")); // Canceled
    }

    private void filterOrdersByStatus(String statusID) {
        orderList.clear();
        if (statusID == null) {
            // Hiển thị tất cả đơn hàng
            orderList.addAll(allOrders);
        } else {
            // Lọc đơn hàng theo trạng thái
            for (Order order : allOrders) {
                if (order.getOrderStatus().equals(getStatusName(statusID))) {
                    orderList.add(order);
                }
            }
        }

        if (orderList.isEmpty() && statusID != null) {
            Toast.makeText(OrderListActivity.this, "No orders found for this status.", Toast.LENGTH_SHORT).show();
        }

        runOnUiThread(() -> {
            orderAdapter.notifyDataSetChanged();
        });
    }

    private String getStatusName(String statusID) {
        switch (statusID) {
            case "0": return "Pending confirmation";
            case "1": return "Confirmed";
            case "2": return "Canceled";
            case "3": return "In transit";
            case "4": return "Delivery successful";
            default: return "";
        }
    }

    private void fetchOrdersForUser() {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("accounts")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String customerID = task.getResult().getString("CustomerID");
                            if (customerID != null) {
                                fetchCustomerOrders(customerID);
                            }
                        } else {
                            Log.e("OrderListActivity", "Error fetching user account: " + task.getException());
                        }
                    });
        }
    }

    private void fetchCustomerOrders(String customerID) {
        db.collection("orders")
                .whereEqualTo("CustomerID", customerID)
                .get()
                .addOnCompleteListener(orderTask -> {
                    if (orderTask.isSuccessful()) {
                        if (!orderTask.getResult().isEmpty()) {
                            for (DocumentSnapshot document : orderTask.getResult()) {
                                String orderID = document.getString("OrderID");
                                String orderDate = document.getString("OrderDate");

                                Object orderStatusIDObj = document.get("OrderStatusID");
                                String orderStatusID = null;

                                if (orderStatusIDObj instanceof String) {
                                    orderStatusID = (String) orderStatusIDObj;
                                } else if (orderStatusIDObj instanceof Long) {
                                    orderStatusID = String.valueOf((Long) orderStatusIDObj);
                                } else if (orderStatusIDObj instanceof Integer) {
                                    orderStatusID = String.valueOf((Integer) orderStatusIDObj);
                                } else {
                                    Log.e("OrderListActivity", "Unknown data type for OrderStatusID");
                                }

                                if (orderStatusID != null) {
                                    fetchOrderStatus(orderStatusID, orderID, orderDate);
                                }
                            }
                        } else {
                            Log.e("OrderListActivity", "No orders found for CustomerID: " + customerID);
                            Toast.makeText(OrderListActivity.this, "No orders found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching orders: " + orderTask.getException());
                        Toast.makeText(OrderListActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    interface OnTotalCalculatedListener {
        void onTotalCalculated(double totalValue);
    }

    private void fetchProductPriceAndCalculateTotal(String productID, Object quantityObj, final double[] totalValue, OnTotalCalculatedListener listener) {
        db.collection("products")
                .document(productID)
                .get()
                .addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful()) {
                        Double productPrice = productTask.getResult().getDouble("productPrice");

                        if (productPrice != null && quantityObj != null) {
                            int quantity = (quantityObj instanceof Long) ? ((Long) quantityObj).intValue() : (Integer) quantityObj;
                            double value = productPrice * quantity;
                            totalValue[0] += value;
                        }

                        Log.d("OrderListActivity", "ProductID: " + productID + " Price: " + productPrice + " Quantity: " + quantityObj);
                        Log.d("OrderListActivity", "Total Value so far: " + totalValue[0]);

                        if (listener != null) {
                            listener.onTotalCalculated(totalValue[0]);
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching product price: " + productTask.getException());
                    }
                });
    }

    private void fetchOrderStatus(String orderStatusID, String orderID, String orderDate) {
        db.collection("orderstatuses")
                .document(orderStatusID)
                .get()
                .addOnCompleteListener(statusTask -> {
                    if (statusTask.isSuccessful()) {
                        String orderStatus = statusTask.getResult().getString("Status");

                        Log.d("OrderListActivity", "OrderStatus for OrderID " + orderID + ": " + orderStatus);

                        fetchTotalProductQuantity(orderID, orderStatus, orderDate);
                    }
                });
    }

    private void fetchTotalProductQuantity(String orderID, String orderStatus, String orderDate) {
        db.collection("orderdetails")
                .whereEqualTo("OrderID", orderID)
                .get()
                .addOnCompleteListener(detailsTask -> {
                    if (detailsTask.isSuccessful()) {
                        int totalQuantity = 0;
                        final double[] totalValue = {0.0};

                        for (DocumentSnapshot document : detailsTask.getResult()) {
                            Object quantityObj = document.get("Quantity");
                            String productID = document.getString("ProductID");

                            if (quantityObj instanceof Long) {
                                totalQuantity += ((Long) quantityObj).intValue();
                            } else if (quantityObj instanceof Integer) {
                                totalQuantity += (Integer) quantityObj;
                            }

                            if (productID != null) {
                                fetchProductPriceAndCalculateTotal(productID, quantityObj, totalValue, new OnTotalCalculatedListener() {
                                    @Override
                                    public void onTotalCalculated(double calculatedValue) {
                                        totalValue[0] = calculatedValue;
                                    }
                                });
                            }
                        }

                        int finalTotalQuantity = totalQuantity;
                        new android.os.Handler().postDelayed(() -> {
                            Order order = new Order(orderID, orderDate, orderStatus, finalTotalQuantity, totalValue[0]);
                            allOrders.add(order); // Lưu vào allOrders
                            orderList.add(order); // Thêm vào orderList để hiển thị ban đầu

                            runOnUiThread(() -> {
                                orderAdapter = new OrderAdapter(OrderListActivity.this, orderList);
                                lvOrder.setAdapter(orderAdapter);
                                orderAdapter.notifyDataSetChanged();

                                if (txtTotalValue != null) {
                                    txtTotalValue.setText(String.format("%.0f", totalValue[0]));
                                } else {
                                    Log.e("OrderListActivity", "txtTotalValue is null when trying to set text");
                                }
                            });
                        }, 500);
                    }
                });
    }
}