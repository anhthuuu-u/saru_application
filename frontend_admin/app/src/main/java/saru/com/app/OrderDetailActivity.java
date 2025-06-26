package saru.com.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import saru.com.models.Customer;
import saru.com.models.OrderItem;
import saru.com.models.Orders;

public class OrderDetailActivity extends AppCompatActivity {
    private EditText edtCustomerID, edtCustomerName, edtCustomerPhone, edtCustomerBirth;
    private Spinner spinnerSex;
    private CheckBox chkReceiveEmail;
    private Button btnSave, btnRemove, btnCancel, btnAddAddress;
    private RecyclerView rvAddresses, rvOrders;
    private AddressAdapter addressAdapter;
    private OrderAdapter orderAdapter;
    private FirebaseFirestore db;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupSpinners();
        setupRecyclerViews();
        displayInfo();
        setupEvents();
    }

    private void initializeViews() {
        edtCustomerID = findViewById(R.id.edt_customer_id);
        edtCustomerName = findViewById(R.id.edt_customer_name);
        edtCustomerPhone = findViewById(R.id.edt_customer_phone);
        edtCustomerBirth = findViewById(R.id.edt_customer_birth);
        spinnerSex = findViewById(R.id.spinner_sex);
        chkReceiveEmail = findViewById(R.id.chk_receive_email);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        rvAddresses = findViewById(R.id.rvAddresses);
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupSpinners() {
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"", "Male", "Female", "Other"});
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);
    }

    private void setupRecyclerViews() {
        addressAdapter = new AddressAdapter(new ArrayList<>(), position -> showEditAddressDialog(position),
                position -> {
                    String addressID = addressAdapter.getAddress(position).getAddressID();
                    db.collection("customers").document(customer.getCustomerID())
                            .collection("addresses").document(addressID).delete()
                            .addOnSuccessListener(aVoid -> {
                                addressAdapter.removeAddress(position);
                                addressAdapter.notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete address: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(addressAdapter);

        orderAdapter = new OrderAdapter(new ArrayList<>());
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void displayInfo() {
        Intent intent = getIntent();
        customer = (Customer) intent.getSerializableExtra("SELECTED_CUSTOMER");
        if (customer != null) {
            edtCustomerID.setText(customer.getCustomerID());
            edtCustomerID.setEnabled(false);
            edtCustomerName.setText(customer.getCustomerName());
            edtCustomerPhone.setText(customer.getCustomerPhone());
            edtCustomerBirth.setText(customer.getCustomerBirth());
            spinnerSex.setSelection(customer.getSex() != null ? getSexPosition(customer.getSex()) : 0);
            chkReceiveEmail.setChecked(customer.isReceiveEmail());
            loadAddresses();
            loadOrders();
        } else {
            edtCustomerID.setEnabled(true);
        }
    }

    private void loadAddresses() {
        db.collection("customers").document(customer.getCustomerID()).collection("addresses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Customer.Address> addresses = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Customer.Address address = document.toObject(Customer.Address.class);
                        address.setAddressID(document.getId());
                        addresses.add(address);
                    }
                    addressAdapter.updateAddresses(addresses);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("customerID", customer.getCustomerID())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<OrderItem> orderItems = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        Orders order = document.toObject(Orders.class);
                        if (order != null && order.getItems() != null) {
                            for (Map<String, Object> item : order.getItems()) {
                                try {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setProductName((String) item.get("productName"));
                                    orderItem.setProductPrice(item.get("productPrice") instanceof Number ?
                                            ((Number) item.get("productPrice")).doubleValue() : 0.0);
                                    orderItem.setQuantity(item.get("quantity") instanceof Number ?
                                            ((Number) item.get("quantity")).intValue() : 0);
                                    orderItem.setTotalPrice(item.get("totalPrice") instanceof Number ?
                                            ((Number) item.get("totalPrice")).doubleValue() : 0.0);
                                    orderItems.add(orderItem);
                                } catch (Exception e) {
                                    Log.e("OrderDetailActivity", "Error parsing order item: " + item, e);
                                }
                            }
                        }
                    }
                    orderAdapter.updateOrders(orderItems);
                    if (orderItems.isEmpty()) {
                        Toast.makeText(this, "No order items found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load order items: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int getSexPosition(String sex) {
        if (sex == null || sex.isEmpty()) return 0;
        switch (sex) {
            case "Male": return 1;
            case "Female": return 2;
            case "Other": return 3;
            default: return 0;
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveCustomer());
        btnRemove.setOnClickListener(v -> removeCustomer());
        btnCancel.setOnClickListener(v -> finish());
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void showAddAddressDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        EditText edtRecipientName = dialogView.findViewById(R.id.edtRecipientName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);
        EditText edtAddress = dialogView.findViewById(R.id.edtAddress);

        new AlertDialog.Builder(this)
                .setTitle("Add New Address")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String recipientName = edtRecipientName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String address = edtAddress.getText().toString().trim();
                    if (!recipientName.isEmpty() && !phone.isEmpty() && !address.isEmpty()) {
                        Customer.Address newAddress = new Customer.Address(null, recipientName, phone, address);
                        db.collection("customers").document(customer.getCustomerID())
                                .collection("addresses").add(newAddress)
                                .addOnSuccessListener(documentReference -> {
                                    newAddress.setAddressID(documentReference.getId());
                                    addressAdapter.addAddress(newAddress);
                                    addressAdapter.notifyItemInserted(addressAdapter.getItemCount() - 1);
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add address: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Please fill all address fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditAddressDialog(int position) {
        Customer.Address address = addressAdapter.getAddress(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        EditText edtRecipientName = dialogView.findViewById(R.id.edtRecipientName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);
        EditText edtAddress = dialogView.findViewById(R.id.edtAddress);

        edtRecipientName.setText(address.getRecipientName());
        edtPhone.setText(address.getPhone());
        edtAddress.setText(address.getAddress());

        new AlertDialog.Builder(this)
                .setTitle("Edit Address")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String recipientName = edtRecipientName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String addressStr = edtAddress.getText().toString().trim();
                    if (!recipientName.isEmpty() && !phone.isEmpty() && !addressStr.isEmpty()) {
                        Customer.Address updatedAddress = new Customer.Address(address.getAddressID(), recipientName, phone, addressStr);
                        db.collection("customers").document(customer.getCustomerID())
                                .collection("addresses").document(address.getAddressID())
                                .set(updatedAddress)
                                .addOnSuccessListener(aVoid -> {
                                    addressAdapter.updateAddress(position, updatedAddress);
                                    addressAdapter.notifyItemChanged(position);
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update address: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Please fill all address fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCustomer() {
        String customerID = edtCustomerID.getText().toString().trim();
        String name = edtCustomerName.getText().toString().trim();
        String phone = edtCustomerPhone.getText().toString().trim();
        String birth = edtCustomerBirth.getText().toString().trim();
        String sex = spinnerSex.getSelectedItem().toString();
        boolean receiveEmail = chkReceiveEmail.isChecked();

        if (customerID.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Customer newCustomer = new Customer(customerID, name, phone, "", birth, sex, receiveEmail, "", new ArrayList<>());
        db.collection("customers").document(customerID).set(newCustomer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, customer != null ? "Customer updated" : "Customer added", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save customer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeCustomer() {
        if (customer == null) {
            Toast.makeText(this, "No customer selected to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("customers").document(customer.getCustomerID()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Customer deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete customer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
        private List<Customer.Address> addresses;
        private OnAddressClickListener editListener;
        private OnAddressClickListener deleteListener;

        interface OnAddressClickListener {
            void onClick(int position);
        }

        AddressAdapter(List<Customer.Address> addresses, OnAddressClickListener editListener,
                       OnAddressClickListener deleteListener) {
            this.addresses = addresses;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Customer.Address address = addresses.get(position);
            holder.txtRecipientName.setText(address.getRecipientName() != null ? address.getRecipientName() : "N/A");
            holder.txtPhone.setText(address.getPhone() != null ? address.getPhone() : "N/A");
            holder.txtAddress.setText(address.getAddress() != null ? address.getAddress() : "N/A");
            holder.btnEditAddress.setOnClickListener(v -> editListener.onClick(position));
            holder.btnDeleteAddress.setOnClickListener(v -> deleteListener.onClick(position));
        }

        @Override
        public int getItemCount() {
            return addresses.size();
        }

        void updateAddresses(List<Customer.Address> newAddresses) {
            addresses.clear();
            addresses.addAll(newAddresses);
            notifyDataSetChanged();
        }

        void addAddress(Customer.Address address) {
            addresses.add(address);
        }

        void updateAddress(int position, Customer.Address address) {
            addresses.set(position, address);
        }

        void removeAddress(int position) {
            addresses.remove(position);
        }

        Customer.Address getAddress(int position) {
            return addresses.get(position);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtRecipientName, txtPhone, txtAddress;
            Button btnEditAddress, btnDeleteAddress;

            ViewHolder(View itemView) {
                super(itemView);
                txtRecipientName = itemView.findViewById(R.id.txtRecipientName);
                txtPhone = itemView.findViewById(R.id.txtPhone);
                txtAddress = itemView.findViewById(R.id.txtAddress);
                btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
                btnDeleteAddress = itemView.findViewById(R.id.btnDeleteAddress);
            }
        }
    }

    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<OrderItem> orderItems;

        OrderAdapter(List<OrderItem> orderItems) {
            this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderItem item = orderItems.get(position);
            holder.txtProductName.setText(item.getProductName() != null ? item.getProductName() : "N/A");
            holder.txtProductPrice.setText(String.format("Price: %,.0f VNĐ", item.getProductPrice()));
            holder.txtItemQuantity.setText(String.format("Qty: %d", item.getQuantity()));
            holder.txtTotalAmount.setText(String.format("Total: %,.0f VNĐ", item.getTotalPrice()));
        }

        @Override
        public int getItemCount() {
            return orderItems.size();
        }

        void updateOrders(List<OrderItem> newOrderItems) {
            orderItems.clear();
            orderItems.addAll(newOrderItems != null ? newOrderItems : new ArrayList<>());
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtProductName, txtProductPrice, txtItemQuantity, txtTotalAmount;

            ViewHolder(View itemView) {
                super(itemView);
                txtProductName = itemView.findViewById(R.id.txtProductName);
                txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
                txtItemQuantity = itemView.findViewById(R.id.txtItemQuantity);
                txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            }
        }
    }
}
