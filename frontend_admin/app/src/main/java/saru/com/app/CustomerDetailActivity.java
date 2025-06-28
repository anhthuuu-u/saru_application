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
import saru.com.models.Customer;
import saru.com.models.Orders;

public class CustomerDetailActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_customer_detail);
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
        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);
    }

    private void setupRecyclerViews() {
        addressAdapter = new AddressAdapter(new ArrayList<>(), this::showEditAddressDialog,
                position -> {
                    String addressID = addressAdapter.getAddress(position).getAddressID();
                    db.collection("customers").document(customer.getCustomerID())
                            .collection("addresses").document(addressID).delete()
                            .addOnSuccessListener(aVoid -> {
                                addressAdapter.removeAddress(position);
                                showToast("Address deleted.");
                            })
                            .addOnFailureListener(e -> showToast("Error deleting address: " + e.getMessage()));
                });
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(addressAdapter);

        orderAdapter = new OrderAdapter(new ArrayList<>(), selectedOrder -> {
            Intent intent = new Intent(CustomerDetailActivity.this, OrderDetailActivity.class);
            db.collection("orders").document(selectedOrder.getOrderID()).get()
                    .addOnSuccessListener(doc -> {
                        Orders fullOrder = doc.toObject(Orders.class);
                        if (fullOrder != null) {
                            intent.putExtra("SELECTED_ORDER", fullOrder);
                            startActivity(intent);
                        } else {
                            showToast("Could not load order details.");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
        });
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

    private int getSexPosition(String sex) {
        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        for (int i = 0; i < sexOptions.length; i++) {
            if (sexOptions[i].equalsIgnoreCase(sex)) {
                return i;
            }
        }
        return 0;
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveCustomer());
        btnRemove.setOnClickListener(v -> removeCustomer());
        btnCancel.setOnClickListener(v -> finish());
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void saveCustomer() {
        String customerID = edtCustomerID.getText().toString();
        String customerName = edtCustomerName.getText().toString();
        String customerPhone = edtCustomerPhone.getText().toString();
        String customerBirth = edtCustomerBirth.getText().toString();
        String sex = spinnerSex.getSelectedItem().toString();
        boolean receiveEmail = chkReceiveEmail.isChecked();

        if (customerID.isEmpty() || customerName.isEmpty() || customerPhone.isEmpty()) {
            showToast("Please fill in all required fields.");
            return;
        }

        Customer newCustomer = new Customer();
        newCustomer.setCustomerID(customer != null ? customer.getCustomerID() : customerID);
        newCustomer.setCustomerName(customerName);
        newCustomer.setCustomerPhone(customerPhone);
        newCustomer.setCustomerBirth(customerBirth);
        newCustomer.setSex(sex);
        newCustomer.setReceiveEmail(receiveEmail);
        newCustomer.setMemberID("member01");

        db.collection("customers").document(newCustomer.getCustomerID())
                .set(newCustomer)
                .addOnSuccessListener(aVoid -> {
                    showToast("Customer saved successfully!");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error saving customer: " + e.getMessage()));
    }

    private void removeCustomer() {
        if (customer != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this customer?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("customers").document(customer.getCustomerID())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Customer deleted successfully!");
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> showToast("Error deleting customer: " + e.getMessage()));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            showToast("Cannot remove a non-existent customer.");
        }
    }

    private void showAddAddressDialog() { /* Omitted for brevity, assumed correct */ }
    private void showEditAddressDialog(int position) { /* Omitted for brevity, assumed correct */ }

    private void loadAddresses() {
        db.collection("customers").document(customer.getCustomerID()).collection("addresses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Customer.Address> addresses = querySnapshot.toObjects(Customer.Address.class);
                    addressAdapter.updateAddresses(addresses);
                })
                .addOnFailureListener(e -> Log.e("CustomerDetail", "Error loading addresses", e));
    }

    private void loadOrders() {
        db.collection("orders").whereEqualTo("customerID", customer.getCustomerID())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Orders> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Orders order = doc.toObject(Orders.class);
                        if (order != null) {
                            Orders viewer = new Orders();
                            viewer.setOrderID(order.getOrderID());
                            viewer.setOrderDate(order.getOrderDate());
                            viewer.setTotalAmount(order.getTotalAmount());
                            viewer.setOrderStatusID(order.getOrderStatusID());
                            orders.add(viewer);
                        }
                    }
                    orderAdapter.updateOrders(orders);
                })
                .addOnFailureListener(e -> Log.e("CustomerDetail", "Error loading orders", e));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
        private List<Customer.Address> addressList;
        private OnItemClickListener editListener;
        private OnItemClickListener deleteListener;

        public interface OnItemClickListener { void onClick(int position); }

        public AddressAdapter(List<Customer.Address> list, OnItemClickListener edit, OnItemClickListener delete) {
            this.addressList = list;
            this.editListener = edit;
            this.deleteListener = delete;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Customer.Address address = addressList.get(position);
            holder.txtAddress.setText(address.getAddress());
            holder.btnEditAddress.setOnClickListener(v -> editListener.onClick(position));
            holder.btnDeleteAddress.setOnClickListener(v -> deleteListener.onClick(position));
        }

        @Override
        public int getItemCount() { return addressList.size(); }

        public Customer.Address getAddress(int pos) { return addressList.get(pos); }

        public void removeAddress(int pos) {
            addressList.remove(pos);
            notifyItemRemoved(pos);
        }

        public void updateAddresses(List<Customer.Address> newAddresses) {
            this.addressList = newAddresses;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtAddress;
            Button btnEditAddress, btnDeleteAddress;

            ViewHolder(View itemView) {
                super(itemView);
                txtAddress = itemView.findViewById(R.id.txtAddress);
                btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
                btnDeleteAddress = itemView.findViewById(R.id.btnDeleteAddress);
            }
        }
    }

    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<Orders> orderList;
        private OnOrderClickListener clickListener;

        public interface OnOrderClickListener { void onClick(Orders order); }

        public OrderAdapter(List<Orders> orderList, OnOrderClickListener listener) {
            this.orderList = orderList;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordersviewer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Orders item = orderList.get(position);
            holder.txtOrderID.setText(item.getOrderID());
            holder.txtOrderDate.setText(item.getOrderDate());
            holder.txtTotalAmount.setText(String.format("%,.0f VNĐ", item.getTotalAmount()));
            holder.txtOrderStatus.setText(item.getOrderStatusID());
            holder.itemView.setOnClickListener(v -> clickListener.onClick(item));
        }

        @Override
        public int getItemCount() { return orderList.size(); }

        void updateOrders(List<Orders> newOrders) {
            orderList.clear();
            orderList.addAll(newOrders);
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtOrderID, txtOrderDate, txtTotalAmount, txtOrderStatus;

            ViewHolder(View itemView) {
                super(itemView);
                txtOrderID = itemView.findViewById(R.id.txtOrderID);
                txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
                txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
                txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            }
        }
    }
}