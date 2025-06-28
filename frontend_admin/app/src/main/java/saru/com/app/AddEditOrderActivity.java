package saru.com.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.UUID;
import saru.com.models.Customer;
import saru.com.models.OrderDetails;
import saru.com.models.Orders;
import saru.com.models.Product;
import saru.com.app.R;

public class AddEditOrderActivity extends AppCompatActivity {
    private EditText edtOrderID, edtOrderDate, edtCustomerName, edtCustomerPhone, edtCustomerAddress;
    private Spinner spinnerCustomer, spinnerOrderStatus;
    private Button btnSave, btnCancel;
    private RecyclerView rvProducts;
    private ProductSelectionAdapter productAdapter;
    private FirebaseFirestore db;
    private List<Customer> customers;
    private List<String> customerNames;
    private List<String> orderStatuses;
    private List<Product> products;
    private List<OrderDetails> selectedOrderDetails;
    private Orders existingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_order);
        db = FirebaseFirestore.getInstance();
        customers = new ArrayList<>();
        customerNames = new ArrayList<>();
        orderStatuses = new ArrayList<>();
        products = new ArrayList<>();
        selectedOrderDetails = new ArrayList<>();
        initializeViews();
        loadCustomers();
        loadOrderStatuses();
        loadProducts();
        checkForExistingOrder();
        setupEvents();
    }

    private void initializeViews() {
        edtOrderID = findViewById(R.id.edtOrderID);
        edtOrderDate = findViewById(R.id.edtOrderDate);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtCustomerPhone = findViewById(R.id.edtCustomerPhone);
        edtCustomerAddress = findViewById(R.id.edtCustomerAddress);
        spinnerCustomer = findViewById(R.id.spinnerCustomer);
        spinnerOrderStatus = findViewById(R.id.spinnerOrderStatus);
        rvProducts = findViewById(R.id.rvProducts);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup RecyclerView for product selection
        productAdapter = new ProductSelectionAdapter(products, selectedOrderDetails);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void checkForExistingOrder() {
        existingOrder = (Orders) getIntent().getSerializableExtra("SELECTED_ORDER");
        if (existingOrder != null) {
            edtOrderID.setText(existingOrder.getOrderID());
            edtOrderID.setEnabled(false);
            edtOrderDate.setText(existingOrder.getOrderDate());
            edtCustomerName.setText(existingOrder.getCustomerName());
            edtCustomerPhone.setText(existingOrder.getCustomerPhone());
            edtCustomerAddress.setText(existingOrder.getCustomerAddress());
            // Set spinner selections
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getCustomerID().equals(existingOrder.getCustomerID())) {
                    spinnerCustomer.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < orderStatuses.size(); i++) {
                if (orderStatuses.get(i).equals(existingOrder.getOrderStatusID())) {
                    spinnerOrderStatus.setSelection(i);
                    break;
                }
            }
            // Load existing order details
            loadExistingOrderDetails();
        } else {
            edtOrderID.setText(UUID.randomUUID().toString());
        }
    }

    private void loadCustomers() {
        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, customerNames);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomer.setAdapter(customerAdapter);

        db.collection("customers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    customers.clear();
                    customerNames.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Customer customer = document.toObject(Customer.class);
                        customer.setCustomerID(document.getId());
                        customers.add(customer);
                        customerNames.add(customer.getCustomerName());
                    }
                    customerAdapter.notifyDataSetChanged();
                    if (existingOrder != null) {
                        for (int i = 0; i < customers.size(); i++) {
                            if (customers.get(i).getCustomerID().equals(existingOrder.getCustomerID())) {
                                spinnerCustomer.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load customers: " + e.getMessage()));
    }

    private void loadOrderStatuses() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orderStatuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderStatus.setAdapter(statusAdapter);

        db.collection("orderstatuses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderStatuses.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        orderStatuses.add(document.getString("Status"));
                    }
                    statusAdapter.notifyDataSetChanged();
                    if (existingOrder != null) {
                        for (int i = 0; i < orderStatuses.size(); i++) {
                            if (orderStatuses.get(i).equals(existingOrder.getOrderStatusID())) {
                                spinnerOrderStatus.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load statuses: " + e.getMessage()));
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    products.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setProductID(document.getId());
                        products.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load products: " + e.getMessage()));
    }

    private void loadExistingOrderDetails() {
        db.collection("orderdetails").whereEqualTo("OrderID", existingOrder.getOrderID()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    selectedOrderDetails.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        OrderDetails detail = document.toObject(OrderDetails.class);
                        detail.setId(document.getId());
                        selectedOrderDetails.add(detail);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load order details: " + e.getMessage()));
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveOrder());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveOrder() {
        String orderID = edtOrderID.getText().toString().trim();
        String orderDate = edtOrderDate.getText().toString().trim();
        String customerName = edtCustomerName.getText().toString().trim();
        String customerPhone = edtCustomerPhone.getText().toString().trim();
        String customerAddress = edtCustomerAddress.getText().toString().trim();
        int customerPosition = spinnerCustomer.getSelectedItemPosition();
        int statusPosition = spinnerOrderStatus.getSelectedItemPosition();

        if (orderID.isEmpty() || orderDate.isEmpty() || customerName.isEmpty() || customerPosition < 0 || statusPosition < 0) {
            showToast("Please fill all required fields");
            return;
        }

        if (selectedOrderDetails.isEmpty()) {
            showToast("Please select at least one product");
            return;
        }

        Orders order = new Orders();
        order.setOrderID(orderID);
        order.setCustomerID(customers.get(customerPosition).getCustomerID());
        order.setOrderDate(orderDate);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        order.setOrderStatusID(orderStatuses.get(statusPosition));
        order.setPaymentMethod("EWALLET");
        order.setTimestamp(System.currentTimeMillis());

        // Validate and calculate totalProduct and totalAmount
        List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();
        for (OrderDetails detail : selectedOrderDetails) {
            if (detail.getProductID() != null && !detail.getProductID().isEmpty()) {
                productTasks.add(db.collection("products").document(detail.getProductID()).get());
            } else {
                showToast("Invalid product ID in order details");
                return;
            }
        }

        Tasks.whenAllSuccess(productTasks).addOnSuccessListener(results -> {
            int totalProduct = 0;
            double totalAmount = 0.0;
            List<Task<Void>> detailTasks = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                DocumentSnapshot productDoc = (DocumentSnapshot) results.get(i);
                OrderDetails detail = selectedOrderDetails.get(i);
                detail.setOrderID(orderID);
                if (detail.getId() == null) {
                    detail.setId(UUID.randomUUID().toString());
                }
                Product product = productDoc.toObject(Product.class);
                if (product != null) {
                    totalProduct += detail.getQuantity();
                    totalAmount += product.getProductPrice() * detail.getQuantity();
                    detailTasks.add(db.collection("orderdetails").document(detail.getId()).set(detail));
                } else {
                    showToast("Product not found for ID: " + detail.getProductID());
                    return;
                }
            }

            order.setTotalProduct(totalProduct);
            order.setTotalAmount(totalAmount);

            // Save order details first, then save order
            Tasks.whenAll(detailTasks).addOnSuccessListener(aVoid -> {
                db.collection("orders").document(orderID).set(order)
                        .addOnSuccessListener(aVoid2 -> {
                            showToast(existingOrder != null ? "Order updated successfully" : "Order added successfully");
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Failed to save order: " + e.getMessage()));
            }).addOnFailureListener(e -> showToast("Failed to save order details: " + e.getMessage()));
        }).addOnFailureListener(e -> showToast("Failed to load product prices: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Inner Adapter for Product Selection
    private static class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {
        private List<Product> products;
        private List<OrderDetails> selectedOrderDetails;

        ProductSelectionAdapter(List<Product> products, List<OrderDetails> selectedOrderDetails) {
            this.products = products;
            this.selectedOrderDetails = selectedOrderDetails;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = products.get(position);
            holder.txtProductName.setText(product.getProductName());
            holder.txtProductPrice.setText(String.format("%,.0f VNĐ", product.getProductPrice()));

            // Check if product is already selected
            OrderDetails existingDetail = selectedOrderDetails.stream().filter(detail -> detail.getProductID() != null && detail.getProductID().equals(product.getProductID())).findFirst().orElse(null);
            holder.edtQuantity.setText(existingDetail != null ? String.valueOf(existingDetail.getQuantity()) : "0");

            holder.edtQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String quantityStr = holder.edtQuantity.getText().toString();
                    int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
                    if (quantity > 0) {
                        if (existingDetail == null) {
                            OrderDetails detail = new OrderDetails();
                            detail.setProductID(product.getProductID());
                            detail.setQuantity(quantity);
                            selectedOrderDetails.add(detail);
                        } else {
                            existingDetail.setQuantity(quantity);
                        }
                    } else if (existingDetail != null) {
                        selectedOrderDetails.remove(existingDetail);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtProductName, txtProductPrice;
            EditText edtQuantity;

            ViewHolder(View itemView) {
                super(itemView);
                txtProductName = itemView.findViewById(R.id.txtProductName);
                txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
                edtQuantity = itemView.findViewById(R.id.edtQuantity);
            }
        }
    }
}