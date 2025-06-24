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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Biến để lưu order ID cần highlight (nếu có)
    private String highlightOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        lvOrder = findViewById(R.id.lvOrder);

        initializeFirebase();
        initializeViews();
        setupTabListeners();

        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Lấy thông tin từ Intent
        String initialStatusID = getIntent().getStringExtra("statusID");
        highlightOrderId = getIntent().getStringExtra("highlightOrderId");

        if (initialStatusID != null) {
            highlightTab(initialStatusID);
            // Load tất cả orders trước, sau đó filter
            fetchOrdersForUser(() -> filterOrdersByStatus(initialStatusID));
        } else {
            fetchOrdersForUser(null);
        }
    }

    private void highlightTab(String statusID) {
        // Reset all tabs
        resetAllTabs();

        // Highlight the selected tab
        switch (statusID) {
            case "0":
                tabConfirming.setBackgroundResource(R.drawable.tab_selected_background);
                break;
            case "1":
                tabConfirmed.setBackgroundResource(R.drawable.tab_selected_background);
                break;
            case "2":
                tabCanceled.setBackgroundResource(R.drawable.tab_selected_background);
                break;
            case "3":
                tabInTransit.setBackgroundResource(R.drawable.tab_selected_background);
                break;
            case "4":
                tabComplete.setBackgroundResource(R.drawable.tab_selected_background);
                break;
            default:
                tabAll.setBackgroundResource(R.drawable.tab_selected_background);
                break;
        }
    }

    private void resetAllTabs() {
        tabAll.setBackgroundResource(0);
        tabConfirming.setBackgroundResource(0);
        tabConfirmed.setBackgroundResource(0);
        tabInTransit.setBackgroundResource(0);
        tabComplete.setBackgroundResource(0);
        tabCanceled.setBackgroundResource(0);
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
        tabAll.setOnClickListener(v -> {
            resetAllTabs();
            tabAll.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus(null);
        });

        tabConfirming.setOnClickListener(v -> {
            resetAllTabs();
            tabConfirming.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus("0");
        });

        tabConfirmed.setOnClickListener(v -> {
            resetAllTabs();
            tabConfirmed.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus("1");
        });

        tabInTransit.setOnClickListener(v -> {
            resetAllTabs();
            tabInTransit.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus("3");
        });

        tabComplete.setOnClickListener(v -> {
            resetAllTabs();
            tabComplete.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus("4");
        });

        tabCanceled.setOnClickListener(v -> {
            resetAllTabs();
            tabCanceled.setBackgroundResource(R.drawable.tab_selected_background);
            filterOrdersByStatus("2");
        });
    }

    private void filterOrdersByStatus(String statusID) {
        orderList.clear();
        if (statusID == null) {
            // Hiển thị tất cả đơn hàng
            orderList.addAll(allOrders);
        } else {
            // Lọc đơn hàng theo trạng thái
            String targetStatus = getStatusName(statusID);
            for (Order order : allOrders) {
                if (order.getOrderStatus().equals(targetStatus)) {
                    orderList.add(order);
                }
            }
        }

        if (orderList.isEmpty() && statusID != null) {
            Toast.makeText(OrderListActivity.this, "Không tìm thấy đơn hàng nào cho trạng thái này.", Toast.LENGTH_SHORT).show();
        }

        runOnUiThread(() -> {
            if (orderAdapter == null) {
                orderAdapter = new OrderAdapter(OrderListActivity.this, orderList);
                lvOrder.setAdapter(orderAdapter);
            }
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

    // Interface callback để biết khi nào load orders xong
    interface OnOrdersLoadedCallback {
        void onOrdersLoaded();
    }

    private void fetchOrdersForUser(OnOrdersLoadedCallback callback) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("accounts")
                    .document(userUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String customerID = task.getResult().getString("CustomerID");
                            if (customerID != null) {
                                fetchCustomerOrders(customerID, callback);
                            } else {
                                Log.e("OrderListActivity", "CustomerID not found for user: " + userUID);
                                Toast.makeText(this, "Không tìm thấy thông tin khách hàng.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("OrderListActivity", "Error fetching user account: " + task.getException());
                            Toast.makeText(this, "Lỗi khi tải thông tin tài khoản.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCustomerOrders(String customerID, OnOrdersLoadedCallback callback) {
        db.collection("orders")
                .whereEqualTo("CustomerID", customerID)
                .get()
                .addOnCompleteListener(orderTask -> {
                    if (orderTask.isSuccessful()) {
                        if (!orderTask.getResult().isEmpty()) {
                            AtomicInteger pendingOrders = new AtomicInteger(orderTask.getResult().size());

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
                                    fetchOrderStatus(orderStatusID, orderID, orderDate, () -> {
                                        if (pendingOrders.decrementAndGet() == 0) {
                                            // Tất cả orders đã load xong
                                            if (callback != null) {
                                                callback.onOrdersLoaded();
                                            }
                                        }
                                    });
                                } else {
                                    if (pendingOrders.decrementAndGet() == 0) {
                                        if (callback != null) {
                                            callback.onOrdersLoaded();
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e("OrderListActivity", "No orders found for CustomerID: " + customerID);
                            Toast.makeText(OrderListActivity.this, "Không tìm thấy đơn hàng nào.", Toast.LENGTH_SHORT).show();
                            if (callback != null) {
                                callback.onOrdersLoaded();
                            }
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching orders: " + orderTask.getException());
                        Toast.makeText(OrderListActivity.this, "Lỗi khi tải danh sách đơn hàng.", Toast.LENGTH_SHORT).show();
                        if (callback != null) {
                            callback.onOrdersLoaded();
                        }
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
                        if (listener != null) {
                            listener.onTotalCalculated(totalValue[0]);
                        }
                    }
                });
    }

    private void fetchOrderStatus(String orderStatusID, String orderID, String orderDate, Runnable onCompleted) {
        db.collection("orderstatuses")
                .document(orderStatusID)
                .get()
                .addOnCompleteListener(statusTask -> {
                    if (statusTask.isSuccessful()) {
                        String orderStatus = statusTask.getResult().getString("Status");
                        Log.d("OrderListActivity", "OrderStatus for OrderID " + orderID + ": " + orderStatus);
                        fetchTotalProductQuantity(orderID, orderStatus, orderDate, onCompleted);
                    } else {
                        Log.e("OrderListActivity", "Error fetching order status: " + statusTask.getException());
                        if (onCompleted != null) {
                            onCompleted.run();
                        }
                    }
                });
    }

    private void fetchTotalProductQuantity(String orderID, String orderStatus, String orderDate, Runnable onCompleted) {
        db.collection("orderdetails")
                .whereEqualTo("OrderID", orderID)
                .get()
                .addOnCompleteListener(detailsTask -> {
                    if (detailsTask.isSuccessful()) {
                        int totalQuantity = 0;
                        final double[] totalValue = {0.0};

                        List<DocumentSnapshot> details = detailsTask.getResult().getDocuments();
                        if (details.isEmpty()) {
                            // Không có chi tiết đơn hàng, tạo order với giá trị mặc định
                            Order order = new Order(orderID, orderDate, orderStatus, 0, 0.0);
                            allOrders.add(order);
                            updateUI();
                            if (onCompleted != null) {
                                onCompleted.run();
                            }
                            return;
                        }

                        AtomicInteger pendingProducts = new AtomicInteger(details.size());

                        for (DocumentSnapshot document : details) {
                            Object quantityObj = document.get("Quantity");
                            String productID = document.getString("ProductID");

                            if (quantityObj instanceof Long) {
                                totalQuantity += ((Long) quantityObj).intValue();
                            } else if (quantityObj instanceof Integer) {
                                totalQuantity += (Integer) quantityObj;
                            }

                            if (productID != null) {
                                int finalTotalQuantity = totalQuantity;
                                fetchProductPriceAndCalculateTotal(productID, quantityObj, totalValue, calculatedValue -> {
                                    if (pendingProducts.decrementAndGet() == 0) {
                                        // Tất cả products đã tính xong
                                        Order order = new Order(orderID, orderDate, orderStatus, finalTotalQuantity, totalValue[0]);
                                        allOrders.add(order);

                                        // Nếu đây là order cần highlight, thêm vào đầu danh sách
                                        if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                            orderList.add(0, order);
                                        } else if (orderList.isEmpty()) {
                                            orderList.add(order);
                                        }

                                        updateUI();
                                        if (onCompleted != null) {
                                            onCompleted.run();
                                        }
                                    }
                                });
                            } else {
                                if (pendingProducts.decrementAndGet() == 0) {
                                    Order order = new Order(orderID, orderDate, orderStatus, totalQuantity, totalValue[0]);
                                    allOrders.add(order);

                                    if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                        orderList.add(0, order);
                                    } else if (orderList.isEmpty()) {
                                        orderList.add(order);
                                    }

                                    updateUI();
                                    if (onCompleted != null) {
                                        onCompleted.run();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching order details: " + detailsTask.getException());
                        if (onCompleted != null) {
                            onCompleted.run();
                        }
                    }
                });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (orderAdapter == null) {
                orderAdapter = new OrderAdapter(OrderListActivity.this, orderList);
                lvOrder.setAdapter(orderAdapter);
            }
            orderAdapter.notifyDataSetChanged();
        });
    }
}