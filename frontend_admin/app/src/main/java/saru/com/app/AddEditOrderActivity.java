        package saru.com.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import saru.com.models.Customer;
import saru.com.models.Orders;
import saru.com.app.R;

public class AddEditOrderActivity extends AppCompatActivity {
    private EditText edtOrderID, edtOrderDate, edtCustomerName, edtCustomerPhone, edtCustomerAddress;
    private Spinner spinnerCustomer, spinnerOrderStatus, spinnerPaymentStatus;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private List<Customer> customers;
    private List<String> customerNames;
    private List<String> orderStatuses;
    private List<String> paymentStatuses;
    private Orders existingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_order);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadCustomers();
        loadOrderStatuses();
        loadPaymentStatuses();
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
        spinnerPaymentStatus = findViewById(R.id.spinnerPaymentStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
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
            for (int i = 0; i < paymentStatuses.size(); i++) {
                if (paymentStatuses.get(i).equals(String.valueOf(existingOrder.getPaymentStatusID()))) {
                    spinnerPaymentStatus.setSelection(i);
                    break;
                }
            }
        } else {
            edtOrderID.setText(UUID.randomUUID().toString());
        }
    }

    private void loadCustomers() {
        customers = new ArrayList<>();
        customerNames = new ArrayList<>();
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
        orderStatuses = new ArrayList<>();
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

    private void loadPaymentStatuses() {
        paymentStatuses = new ArrayList<>();
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentStatuses);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentStatus.setAdapter(paymentAdapter);

        db.collection("paymentstatuses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paymentStatuses.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        paymentStatuses.add(document.getString("PaymentStatus"));
                    }
                    paymentAdapter.notifyDataSetChanged();
                    if (existingOrder != null) {
                        for (int i = 0; i < paymentStatuses.size(); i++) {
                            if (paymentStatuses.get(i).equals(String.valueOf(existingOrder.getPaymentStatusID()))) {
                                spinnerPaymentStatus.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load payment statuses: " + e.getMessage()));
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
        int paymentPosition = spinnerPaymentStatus.getSelectedItemPosition();

        if (orderID.isEmpty() || orderDate.isEmpty() || customerName.isEmpty() || customerPosition < 0 ||
                statusPosition < 0 || paymentPosition < 0) {
            showToast("Please fill all required fields");
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
        order.setPaymentStatusID((long) paymentPosition);
        order.setPaymentMethod("");
        order.setTimestamp(System.currentTimeMillis());
        order.setTotalProduct(0);
        order.setTotalAmount(0.0);
        order.setItems(new ArrayList<>());

        db.collection("orders").document(orderID).set(order)
                .addOnSuccessListener(aVoid -> {
                    showToast(existingOrder != null ? "Order updated successfully" : "Order added successfully");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save order: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
