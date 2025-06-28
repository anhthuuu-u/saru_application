package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.adapters.CustomerAdapter;
import saru.com.models.Customer;

public class CustomerManagementActivity extends AppCompatActivity {
    private RecyclerView rvCustomers;
    private EditText edtSearch;
    private Spinner spinnerFilterStatus;
    private Button btnAddCustomer, btnApplyFilter;
    private CustomerAdapter customerAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration customerListener;
    private static final int REQUEST_CODE_NEW_CUSTOMER = 300;
    private static final int REQUEST_CODE_EDIT_CUSTOMER = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        setupFilterSpinner();
        loadCustomers();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Use the adapter's filter method
                if (customerAdapter != null) {
                    customerAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_NEW_CUSTOMER);
        });

        btnApplyFilter.setOnClickListener(v -> loadCustomers());
    }

    private void initializeViews() {
        rvCustomers = findViewById(R.id.rvCustomers);
        edtSearch = findViewById(R.id.edtSearch);
        spinnerFilterStatus = findViewById(R.id.spinnerFilterStatus);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
    }

    private void setupRecyclerView() {
        customerAdapter = new CustomerAdapter(new ArrayList<>(),
                customer -> { // editListener
                    Intent intent = new Intent(this, CustomerDetailActivity.class);
                    intent.putExtra("SELECTED_CUSTOMER", customer);
                    startActivityForResult(intent, REQUEST_CODE_EDIT_CUSTOMER);
                },
                customer -> { // deleteListener
                    db.collection("customers").document(customer.getCustomerID())
                            .delete()
                            .addOnSuccessListener(aVoid -> showToast("Customer deleted successfully!"))
                            .addOnFailureListener(e -> showToast("Error deleting customer: " + e.getMessage()));
                },
                customer -> { // messageListener
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra("CUSTOMER_ID", customer.getCustomerID());
                    intent.putExtra("CUSTOMER_NAME", customer.getCustomerName());
                    startActivity(intent);
                });
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(customerAdapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.customer_status_filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterStatus.setAdapter(adapter);
    }

    private void loadCustomers() {
        Query query = db.collection("customers");
        String selectedStatus = spinnerFilterStatus.getSelectedItem().toString();

        // Example filter logic (requires a 'status' field in your Customer model)
        if (!selectedStatus.equals("All")) {
            // query = query.whereEqualTo("status", selectedStatus);
        }

        if (customerListener != null) {
            customerListener.remove();
        }
        customerListener = query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.w("CustomerManagement", "Listen failed.", e);
                return;
            }

            if (querySnapshot != null) {
                List<Customer> updatedCustomers = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Customer customer = document.toObject(Customer.class);
                    updatedCustomers.add(customer);
                }
                customerAdapter.updateList(updatedCustomers);
                // After updating, re-apply the current search term
                edtSearch.setText(edtSearch.getText().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_new_customer) {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_NEW_CUSTOMER);
            return true;
        } else if (itemId == R.id.menu_broadcast_advertising) {
            showToast("Broadcast advertising not implemented yet");
            return true;
        } else if (itemId == R.id.menu_help) {
            showToast("Open help website not implemented yet");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_CODE_NEW_CUSTOMER || requestCode == REQUEST_CODE_EDIT_CUSTOMER)) {
            // The real-time listener will handle the update automatically.
            showToast("Operation successful!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customerListener != null) {
            customerListener.remove();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}