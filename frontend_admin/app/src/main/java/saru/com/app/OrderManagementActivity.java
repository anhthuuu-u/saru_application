package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import saru.com.models.Orders;

public class OrderManagementActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private Button btnAddOrder;
    private EditText edtSearch;
    private OrderAdapter orderAdapter;
    private FirebaseFirestore db;
    private List<Orders> orderList = new ArrayList<>();

    private static final int ADD_ORDER_REQUEST = 1;
    private static final int EDIT_ORDER_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng layout có thanh tìm kiếm
        setContentView(R.layout.activity_order_management);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        setupEvents();

        loadOrders();
    }

    private void initializeViews() {
        rvOrders = findViewById(R.id.rvOrders);
        btnAddOrder = findViewById(R.id.btnAddOrder);
        edtSearch = findViewById(R.id.edtSearch);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orderList, order -> {
            Intent intent = new Intent(OrderManagementActivity.this, OrderDetailActivity.class);
            intent.putExtra("SELECTED_ORDER", order);
            startActivityForResult(intent, EDIT_ORDER_REQUEST);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupEvents() {
        btnAddOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderManagementActivity.this, AddEditOrderActivity.class);
            startActivityForResult(intent, ADD_ORDER_REQUEST);
        });

        // Logic tìm kiếm có thể được thêm vào edtSearch ở đây
    }

    private void loadOrders() {
        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sắp xếp đơn hàng mới nhất lên đầu
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Orders order = document.toObject(Orders.class);
                        orderList.add(order);
                    }
                    orderAdapter.notifyDataSetChanged();
                    if (orderList.isEmpty()) {
                        Toast.makeText(this, "No orders found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderManagement", "Error loading orders", e);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Tải lại danh sách khi có thay đổi từ màn hình thêm/sửa
        if (resultCode == RESULT_OK && (requestCode == ADD_ORDER_REQUEST || requestCode == EDIT_ORDER_REQUEST)) {
            loadOrders();
        }
    }

    // Inner Adapter class for Orders
    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<Orders> orders;
        private OnOrderClickListener clickListener;

        interface OnOrderClickListener {
            void onClick(Orders order);
        }

        OrderAdapter(List<Orders> orders, OnOrderClickListener clickListener) {
            this.orders = orders;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Sử dụng item layout cho danh sách đơn hàng
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordersviewer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Orders order = orders.get(position);
            holder.bind(order, clickListener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtOrderID, txtOrderDate, txtCustomerName, txtOrderStatus, txtTotalAmount;

            ViewHolder(View itemView) {
                super(itemView);
                txtOrderID = itemView.findViewById(R.id.txtOrderID);
                txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
                txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
                txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
                txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            }

            public void bind(final Orders order, final OnOrderClickListener listener) {
                txtOrderID.setText("ID: " + order.getOrderID());
                txtOrderDate.setText(order.getOrderDate());
                txtCustomerName.setText(order.getCustomerName());
                txtOrderStatus.setText("Status ID: " + order.getOrderStatusID());
                txtTotalAmount.setText(String.format("%,.0f VNĐ", order.getTotalAmount()));
                itemView.setOnClickListener(v -> listener.onClick(order));
            }
        }
    }
}