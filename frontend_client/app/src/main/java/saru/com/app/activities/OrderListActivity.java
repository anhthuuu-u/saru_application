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

    // UI elements
    TextView txtOrderID, txtOrderDate, txtTotalProduct, txtTotalValue;
    Button  btnOrderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        lvOrder= findViewById(R.id.lvOrder);

        initializeFirebase();
        initializeViews();

        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        TextView orderDetailsLink = findViewById(R.id.tv_order_details);
//        orderDetailsLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(OrderListActivity.this, OrderDetailActivity.class);
//                startActivity(intent);
//            }
//        });

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
        btnOrderStatus=findViewById(R.id.btnOrderStatus);
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
                                // Truy vấn và hiển thị đơn hàng khi dữ liệu đã sẵn sàng
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

                                // Lấy OrderStatusID và kiểm tra kiểu dữ liệu
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

                                // Truy vấn trạng thái đơn hàng từ collection orderstatuses
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

                        // Gọi callback khi hoàn tất
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

                        // Log order status for debugging
                        Log.d("OrderListActivity", "OrderStatus for OrderID " + orderID + ": " + orderStatus);

                        // Truy vấn và tính tổng số lượng sản phẩm từ collection orderdetails
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
                                        totalValue[0] = calculatedValue; // Cập nhật tổng giá trị
                                    }
                                });
                            }
                        }

                        // Đợi một chút để đảm bảo tất cả callback hoàn tất (có thể cần cải thiện bằng CountDownLatch)
                        int finalTotalQuantity = totalQuantity;
                        new android.os.Handler().postDelayed(() -> {
                            Order order = new Order(orderID, orderDate, orderStatus, finalTotalQuantity, totalValue[0]);
                            orderList.add(order);

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
                        }, 500); // Độ trễ 500ms, có thể cần điều chỉnh
                    }
                });
    }


}
