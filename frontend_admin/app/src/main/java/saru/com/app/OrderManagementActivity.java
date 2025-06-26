package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import saru.com.models.OrdersViewer;
import java.util.ArrayList;
import java.util.List;

public class OrderManagementActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private FirebaseFirestore db;
    private static final int REQUEST_CODE_EDIT_ORDER = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        loadOrders();
    }

    private void initializeViews() {
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(new ArrayList<>(), order -> {
            Intent intent = new Intent(this, AddEditOrderActivity.class);
            intent.putExtra("SELECTED_ORDER", order);
            startActivityForResult(intent, REQUEST_CODE_EDIT_ORDER);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        db.collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<OrdersViewer> orders = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        try {
                            OrdersViewer order = new OrdersViewer();
                            order.setOrderID(document.getString("orderID") != null ? document.getString("orderID") : document.getId());
                            order.setOrderDate(document.getString("orderDate"));
                            order.setCustomerName(document.getString("customerName"));
                            order.setTotalAmount(document.getDouble("totalAmount") != null ? document.getDouble("totalAmount") : 0.0);
                            order.setOrderStatusID(document.getString("orderStatusID") != null ? document.getString("orderStatusID") : document.getString("orderStatus"));
                            order.setPaymentStatusID(document.getLong("paymentStatusID"));
                            orders.add(order);
                        } catch (Exception e) {
                            Log.e("OrderManagementActivity", "Error converting document: " + document.getId(), e);
                        }
                    }
                    orderAdapter.updateOrders(orders);
                    if (orders.isEmpty()) {
                        Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_ORDER && resultCode == RESULT_OK) {
            loadOrders();
            Toast.makeText(this, "Order updated", Toast.LENGTH_SHORT).show();
        }
    }

    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<OrdersViewer> orders;
        private OnOrderClickListener clickListener;

        interface OnOrderClickListener {
            void onClick(OrdersViewer order);
        }

        OrderAdapter(List<OrdersViewer> orders, OnOrderClickListener clickListener) {
            this.orders = orders != null ? orders : new ArrayList<>();
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordersviewer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrdersViewer order = orders.get(position);
            if (holder.txtOrderID != null) {
                holder.txtOrderID.setText(order.getOrderID() != null ? order.getOrderID() : "N/A");
            } else {
                Log.e("OrderAdapter", "txtOrderID is null at position " + position);
            }
            if (holder.txtOrderDate != null) {
                holder.txtOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "N/A");
            } else {
                Log.e("OrderAdapter", "txtOrderDate is null at position " + position);
            }
            if (holder.txtCustomerName != null) {
                holder.txtCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "N/A");
            } else {
                Log.e("OrderAdapter", "txtCustomerName is null at position " + position);
            }
            if (holder.txtOrderStatus != null) {
                holder.txtOrderStatus.setText(getOrderStatus(order.getOrderStatusID()));
            } else {
                Log.e("OrderAdapter", "txtOrderStatus is null at position " + position);
            }
            if (holder.txtTotalAmount != null) {
                holder.txtTotalAmount.setText(String.format("%,.0f VNĐ", order.getTotalAmount()));
            } else {
                Log.e("OrderAdapter", "txtTotalAmount is null at position " + position);
            }
            holder.itemView.setOnClickListener(v -> clickListener.onClick(order));
        }

        private String getOrderStatus(String statusID) {
            if (statusID == null) return "Unknown";
            switch (statusID) {
                case "0": return "Pending confirmation";
                case "1": return "Confirmed";
                case "2": return "Shipped";
                case "3": return "Delivered";
                case "4": return "Cancelled";
                default: return statusID;
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        void updateOrders(List<OrdersViewer> newOrders) {
            orders.clear();
            orders.addAll(newOrders != null ? newOrders : new ArrayList<>());
            notifyDataSetChanged();
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
                if (txtOrderID == null || txtOrderDate == null || txtCustomerName == null ||
                        txtOrderStatus == null || txtTotalAmount == null) {
                    Log.e("OrderAdapter", "One or more TextViews are null in item_ordersviewer.xml");
                }
            }
        }
    }
}
