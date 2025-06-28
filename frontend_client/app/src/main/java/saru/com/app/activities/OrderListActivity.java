package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private List<Order> allOrders = new ArrayList<>();
    private String highlightOrderId;

    // UI elements for tabs
    private TextView tabAll, tabConfirming, tabConfirmed, tabInTransit, tabComplete, tabCanceled;

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

        ImageButton btn_noti = findViewById(R.id.btn_noti);
        btn_noti.setOnClickListener(v -> openNotification());

        String initialStatusID = getIntent().getStringExtra("statusID");
        highlightOrderId = getIntent().getStringExtra("highlightOrderId");

        if (initialStatusID != null) {
            highlightTab(initialStatusID);
            fetchOrdersForUser(() -> filterOrdersByStatus(initialStatusID));
        } else {
            fetchOrdersForUser(null);
        }
    }

    private void openNotification() {
        Intent intent = new Intent(this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }

    private void highlightTab(String statusID) {
        resetAllTabs();
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
            orderList.addAll(allOrders);
        } else {
            String targetStatus = getStatusName(statusID);
            for (Order order : allOrders) {
                if (order.getOrderStatus().equals(targetStatus)) {
                    orderList.add(order);
                }
            }
        }

        updateUI();
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
                        if (callback != null) {
                            callback.onOrdersLoaded();
                        }
                    });
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng.", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onOrdersLoaded();
            }
        }
    }

    private void fetchCustomerOrders(String customerID, OnOrdersLoadedCallback callback) {
        db.collection("orders")
                .whereEqualTo("CustomerID", customerID)
                .get()
                .addOnCompleteListener(orderTask -> {
                    if (orderTask.isSuccessful()) {
                        if (!orderTask.getResult().isEmpty()) {
                            allOrders.clear(); // Clear previous orders
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
                            Log.d("OrderListActivity", "No orders found for CustomerID: " + customerID);
                            Toast.makeText(this, "Không tìm thấy đơn hàng nào.", Toast.LENGTH_SHORT).show();
                            if (callback != null) {
                                callback.onOrdersLoaded();
                            }
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching orders: " + orderTask.getException());
                        Toast.makeText(this, "Lỗi khi tải danh sách đơn hàng.", Toast.LENGTH_SHORT).show();
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
                            totalValue[0] += productPrice * quantity;
                        }
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
                        fetchTotalProductQuantity(orderID, orderStatus != null ? orderStatus : "Unknown", orderDate, onCompleted);
                    } else {
                        Log.e("OrderListActivity", "Error fetching order status: " + statusTask.getException());
                        fetchTotalProductQuantity(orderID, "Unknown", orderDate, onCompleted);
                    }
                });
    }

    private void fetchTotalProductQuantity(String orderID, String orderStatus, String orderDate, Runnable onCompleted) {
        db.collection("orders")
                .document(orderID)
                .get()
                .addOnCompleteListener(orderTask -> {
                    if (orderTask.isSuccessful()) {
                        DocumentSnapshot orderDoc = orderTask.getResult();
                        if (orderDoc != null && orderDoc.exists()) {
                            Long totalProductLong = orderDoc.getLong("totalProduct");
                            Double totalValue = orderDoc.getDouble("totalAmount"); // Assuming totalAmount is stored as the total value
                            int totalProduct = totalProductLong != null ? totalProductLong.intValue() : 0;

                            if (totalProduct > 0 && totalValue != null) {
                                // Use the values directly from the orders document
                                Order order = new Order(orderID, orderDate, orderStatus, totalProduct, totalValue);
                                allOrders.add(order);
                                if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                    orderList.add(0, order);
                                } else {
                                    orderList.add(order);
                                }
                                updateUI();
                                if (onCompleted != null) {
                                    onCompleted.run();
                                }
                            } else {
                                // Fallback to calculate from orderdetails if totalProduct or totalValue is missing
                                db.collection("orderdetails")
                                        .whereEqualTo("OrderID", orderID)
                                        .get()
                                        .addOnCompleteListener(detailsTask -> {
                                            if (detailsTask.isSuccessful()) {
                                                int calculatedTotalQuantity = 0;
                                                final double[] calculatedTotalValue = {0.0};
                                                List<DocumentSnapshot> details = detailsTask.getResult().getDocuments();

                                                if (details.isEmpty()) {
                                                    Order order = new Order(orderID, orderDate, orderStatus, 0, 0.0);
                                                    allOrders.add(order);
                                                    if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                                        orderList.add(0, order);
                                                    } else {
                                                        orderList.add(order);
                                                    }
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

                                                    if (quantityObj != null) {
                                                        calculatedTotalQuantity += (quantityObj instanceof Long) ? ((Long) quantityObj).intValue() : (Integer) quantityObj;
                                                    }

                                                    if (productID != null) {
                                                        int finalTotalQuantity = calculatedTotalQuantity;
                                                        fetchProductPriceAndCalculateTotal(productID, quantityObj, calculatedTotalValue, calculatedValue -> {
                                                            if (pendingProducts.decrementAndGet() == 0) {
                                                                Order order = new Order(orderID, orderDate, orderStatus, finalTotalQuantity, calculatedTotalValue[0]);
                                                                allOrders.add(order);
                                                                if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                                                    orderList.add(0, order);
                                                                } else {
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
                                                            Order order = new Order(orderID, orderDate, orderStatus, calculatedTotalQuantity, calculatedTotalValue[0]);
                                                            allOrders.add(order);
                                                            if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                                                orderList.add(0, order);
                                                            } else {
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
                                                Order order = new Order(orderID, orderDate, orderStatus, 0, 0.0);
                                                allOrders.add(order);
                                                if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                                    orderList.add(0, order);
                                                } else {
                                                    orderList.add(order);
                                                }
                                                updateUI();
                                                if (onCompleted != null) {
                                                    onCompleted.run();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("OrderListActivity", "Order document does not exist for orderID: " + orderID);
                            Order order = new Order(orderID, orderDate, orderStatus, 0, 0.0);
                            allOrders.add(order);
                            if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                                orderList.add(0, order);
                            } else {
                                orderList.add(order);
                            }
                            updateUI();
                            if (onCompleted != null) {
                                onCompleted.run();
                            }
                        }
                    } else {
                        Log.e("OrderListActivity", "Error fetching order: " + orderTask.getException());
                        Order order = new Order(orderID, orderDate, orderStatus, 0, 0.0);
                        allOrders.add(order);
                        if (highlightOrderId != null && highlightOrderId.equals(orderID)) {
                            orderList.add(0, order);
                        } else {
                            orderList.add(order);
                        }
                        updateUI();
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
            } else {
                orderAdapter.notifyDataSetChanged();
            }
        });
    }
}