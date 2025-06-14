package saru.com.app.activities;

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

import java.util.ArrayList;
import java.util.List;

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
                                fetchCustomerOrders(customerID);
                            }
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

                                if (orderID != null && orderDate != null && orderStatusID != null) {
                                    // Truy vấn thêm trạng thái đơn hàng từ collection orderstatuses
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

    private void fetchOrderStatus(String orderStatusID, String orderID, String orderDate) {
        db.collection("orderstatuses")
                .document(orderStatusID)
                .get()
                .addOnCompleteListener(statusTask -> {
                    if (statusTask.isSuccessful()) {
                        String orderStatus = statusTask.getResult().getString("Status");

                        Order order = new Order(orderID, orderDate, orderStatus);
                        orderList.add(order);

                        // Cập nhật dữ liệu vào ListView
                        orderAdapter = new OrderAdapter(OrderListActivity.this, orderList);
                        lvOrder.setAdapter(orderAdapter);
                    }
                });
    }

}
