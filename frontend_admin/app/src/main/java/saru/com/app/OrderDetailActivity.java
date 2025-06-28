package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.OrderDetailItem;
import saru.com.models.OrderDetails;
import saru.com.models.Orders;
import saru.com.models.Product;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView txtOrderID, txtOrderDate, txtOrderStatus, txtCustomerName, txtCustomerPhone, txtCustomerAddress, txtTotalAmount;
    private Spinner spinnerOrderStatus;
    private Button btnSave, btnDelete, btnCancel;
    private RecyclerView rvOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private FirebaseFirestore db;
    private Orders currentOrder;
    private ArrayList<String> orderStatusNames = new ArrayList<>();
    private ArrayList<String> orderStatusIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        initializeViews();

        currentOrder = (Orders) getIntent().getSerializableExtra("SELECTED_ORDER");
        if (currentOrder == null) {
            Toast.makeText(this, "Error: Order not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        displayOrderInfo();
        loadOrderStatuses();
        loadOrderItems();
        setupEvents();
    }

    private void initializeViews() {
        txtOrderID = findViewById(R.id.txtOrderID);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        spinnerOrderStatus = findViewById(R.id.spinnerOrderStatus);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void displayOrderInfo() {
        txtOrderID.setText(currentOrder.getOrderID() != null ? currentOrder.getOrderID() : "N/A");
        txtOrderDate.setText("Ngày đặt: " + (currentOrder.getOrderDate() != null ? currentOrder.getOrderDate() : "N/A"));
        txtCustomerName.setText("Khách hàng: " + (currentOrder.getCustomerName() != null ? currentOrder.getCustomerName() : "N/A"));
        txtCustomerPhone.setText("SĐT: " + (currentOrder.getCustomerPhone() != null ? currentOrder.getCustomerPhone() : "N/A"));
        txtCustomerAddress.setText("Địa chỉ: " + (currentOrder.getCustomerAddress() != null ? currentOrder.getCustomerAddress() : "N/A"));
        txtTotalAmount.setText(String.format("Tổng cộng: %,.0f VNĐ", currentOrder.getTotalAmount()));
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(new ArrayList<>(), new ArrayList<>(), currentOrder);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveOrderStatus());
        btnCancel.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> deleteOrder());
    }

    private void saveOrderStatus() {
        int selectedPosition = spinnerOrderStatus.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= orderStatusIDs.size()) {
            Toast.makeText(this, "Please select a valid status.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatusID = orderStatusIDs.get(selectedPosition);
        db.collection("orders").document(currentOrder.getOrderID())
                .update("orderStatus", newStatusID)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrderDetailActivity.this, "Order status updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(OrderDetailActivity.this, "Failed to update status.", Toast.LENGTH_SHORT).show());
    }

    private void deleteOrder() {
        db.collection("orderdetails").whereEqualTo("OrderID", currentOrder.getOrderID()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        deleteTasks.add(db.collection("orderdetails").document(doc.getId()).delete());
                    }
                    Tasks.whenAll(deleteTasks).addOnSuccessListener(aVoid -> {
                        db.collection("orders").document(currentOrder.getOrderID()).delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(OrderDetailActivity.this, "Order deleted successfully!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(OrderDetailActivity.this, "Failed to delete order.", Toast.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> Toast.makeText(OrderDetailActivity.this, "Failed to delete order details.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(OrderDetailActivity.this, "Failed to load order details for deletion.", Toast.LENGTH_SHORT).show());
    }

    private void loadOrderStatuses() {
        db.collection("orderstatuses").get().addOnSuccessListener(queryDocumentSnapshots -> {
            orderStatusNames.clear();
            orderStatusIDs.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                orderStatusNames.add(doc.getString("Status"));
                orderStatusIDs.add(doc.getString("OrderStatusID"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, orderStatusNames);
            spinnerOrderStatus.setAdapter(adapter);

            int currentStatusIndex = orderStatusIDs.indexOf(currentOrder.getOrderStatusID());
            if (currentStatusIndex != -1) {
                spinnerOrderStatus.setSelection(currentStatusIndex);
                txtOrderStatus.setText("Trạng thái: " + orderStatusNames.get(currentStatusIndex));
            }
        });
    }

    private void loadOrderItems() {
        db.collection("orderdetails").whereEqualTo("OrderID", currentOrder.getOrderID()).get()
                .addOnSuccessListener(orderDetailsSnapshot -> {
                    if (orderDetailsSnapshot.isEmpty()) {
                        Toast.makeText(this, "This order has no items.", Toast.LENGTH_SHORT).show();
                        orderItemAdapter.updateItems(new ArrayList<>(), new ArrayList<>());
                        return;
                    }

                    List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();
                    List<OrderDetails> orderDetailsList = new ArrayList<>();
                    for (QueryDocumentSnapshot detailDoc : orderDetailsSnapshot) {
                        OrderDetails detail = detailDoc.toObject(OrderDetails.class);
                        detail.setId(detailDoc.getId());
                        if (detail.getProductID() != null && !detail.getProductID().isEmpty()) {
                            orderDetailsList.add(detail);
                            productTasks.add(db.collection("products").document(detail.getProductID()).get());
                        } else {
                            Log.w("OrderDetailActivity", "Skipping order detail with null or empty productID: " + detailDoc.getId());
                        }
                    }

                    if (productTasks.isEmpty()) {
                        Toast.makeText(this, "No valid order details found.", Toast.LENGTH_SHORT).show();
                        orderItemAdapter.updateItems(new ArrayList<>(), new ArrayList<>());
                        return;
                    }

                    Tasks.whenAllSuccess(productTasks).addOnSuccessListener(results -> {
                        List<OrderDetailItem> finalItems = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            DocumentSnapshot productDoc = (DocumentSnapshot) results.get(i);
                            OrderDetails detail = orderDetailsList.get(i);
                            Product product = productDoc.toObject(Product.class);
                            if (product != null) {
                                product.setProductID(productDoc.getId());
                                finalItems.add(new OrderDetailItem(product, detail.getQuantity()));
                            } else {
                                Log.w("OrderDetailActivity", "Product not found for ID: " + detail.getProductID());
                            }
                        }
                        orderItemAdapter.updateItems(finalItems, orderDetailsList);
                    }).addOnFailureListener(e -> {
                        Log.e("OrderDetailActivity", "Error loading products: " + e.getMessage());
                        Toast.makeText(this, "Failed to load order items.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderDetailActivity", "Error loading order details: " + e.getMessage());
                    Toast.makeText(this, "Failed to load order details.", Toast.LENGTH_SHORT).show();
                });
    }

    private static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
        private List<OrderDetailItem> items;
        private List<OrderDetails> orderDetails;
        private Orders currentOrder;

        OrderItemAdapter(List<OrderDetailItem> items, List<OrderDetails> orderDetails, Orders currentOrder) {
            this.items = items;
            this.orderDetails = orderDetails;
            this.currentOrder = currentOrder;
        }

        public void updateItems(List<OrderDetailItem> newItems, List<OrderDetails> newOrderDetails) {
            this.items = newItems;
            this.orderDetails = newOrderDetails;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position), orderDetails.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtProductName, txtProductPrice, txtItemQuantity, txtTotalAmount;
            Button btnEdit;

            ViewHolder(View itemView) {
                super(itemView);
                txtProductName = itemView.findViewById(R.id.txtProductName);
                txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
                txtItemQuantity = itemView.findViewById(R.id.txtItemQuantity);
                txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
                btnEdit = itemView.findViewById(R.id.btnEdit);
            }

            public void bind(final OrderDetailItem item, final OrderDetails orderDetail) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();

                txtProductName.setText(product.getProductName());
                txtProductPrice.setText(String.format("Giá: %,.0f VNĐ", product.getProductPrice()));
                txtItemQuantity.setText("SL: " + quantity);
                txtTotalAmount.setText(String.format("Tổng: %,.0f VNĐ", product.getProductPrice() * quantity));

                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), AddEditOrderActivity.class);
                    intent.putExtra("SELECTED_ORDER", currentOrder);
                    itemView.getContext().startActivity(intent);
                });
            }
        }
    }
}