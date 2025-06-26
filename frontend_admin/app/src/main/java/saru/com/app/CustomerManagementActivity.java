package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot; // Giữ lại nếu bạn có dùng DocumentSnapshot cho các mục đích khác
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // Giữ lại nếu bạn có cài đặt listener realtime ở đâu đó khác
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
    private Button btnAddCustomer, btnApplyFilter; // Đã loại bỏ btnPreviousPage, btnNextPage
    // private TextView txtPagination; // Đã loại bỏ
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList;
    private FirebaseFirestore db;
    private ListenerRegistration customerListener;
    private static final int REQUEST_CODE_NEW_CUSTOMER = 300;
    private static final int REQUEST_CODE_EDIT_CUSTOMER = 400;

    // Đã loại bỏ các biến liên quan đến phân trang
    // private static final int PAGE_SIZE = 10;
    // private int currentPage = 1;
    // private DocumentSnapshot lastVisible;
    // private long totalCustomers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        setupFilterSpinner();
        loadCustomers(); // Tải tất cả khách hàng ban đầu
    }

    private void initializeViews() {
        rvCustomers = findViewById(R.id.rvCustomers);
        edtSearch = findViewById(R.id.edtSearch);
        spinnerFilterStatus = findViewById(R.id.spinnerFilterStatus);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        // Đã loại bỏ các findViewById cho nút phân trang và textview
        // btnPreviousPage = findViewById(R.id.btnPreviousPage);
        // btnNextPage = findViewById(R.id.btnNextPage);
        // txtPagination = findViewById(R.id.txtPagination);

        btnAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_NEW_CUSTOMER);
        });

        // btnApplyFilter sẽ kích hoạt loadCustomers để tải lại dữ liệu với bộ lọc mới
        btnApplyFilter.setOnClickListener(v -> loadCustomers());

        // Đã loại bỏ click listeners cho nút phân trang
        // btnPreviousPage.setOnClickListener(v -> { ... });
        // btnNextPage.setOnClickListener(v -> { ... });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi tìm kiếm thay đổi, tải lại dữ liệu với bộ lọc mới
                loadCustomers();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterSpinner() {
        // Cập nhật các tùy chọn của spinner để khớp với trường 'sex'
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Male", "Female", "Other"}); // Giả sử các giá trị 'sex' là thế này
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterStatus.setAdapter(statusAdapter);
    }

    private void setupRecyclerView() {
        customerList = new ArrayList<>();
        customerAdapter = new CustomerAdapter(customerList,
                customer -> {
                    Intent intent = new Intent(this, CustomerDetailActivity.class);
                    intent.putExtra("SELECTED_CUSTOMER", customer);
                    startActivityForResult(intent, REQUEST_CODE_EDIT_CUSTOMER);
                },
                customer -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Delete Customer")
                            .setMessage("Are you sure you want to delete " + customer.getCustomerName() + "? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                db.collection("customers").document(customer.getCustomerID()).delete()
                                        .addOnSuccessListener(aVoid -> showToast("Customer deleted"))
                                        .addOnFailureListener(e -> showToast("Failed to delete customer: " + e.getMessage()));
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                },
                customer -> {
                    Intent intent = new Intent(this, MessageActivity.class);
                    intent.putExtra("CUSTOMER_ID", customer.getCustomerID());
                    intent.putExtra("CUSTOMER_NAME", customer.getCustomerName());
                    startActivity(intent);
                });
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(customerAdapter);
    }

    private void loadCustomers() {
        String searchQuery = edtSearch.getText().toString().trim().toLowerCase();
        String selectedStatus = spinnerFilterStatus.getSelectedItem().toString();

        Query query = db.collection("customers");

        // Áp dụng bộ lọc trạng thái (Sex) trực tiếp vào truy vấn Firestore
        if (!selectedStatus.equals("All")) {
            query = query.whereEqualTo("sex", selectedStatus);
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            customerList.clear(); // Xóa danh sách hiện có
            List<Customer> fetchedCustomers = new ArrayList<>();
            if (!querySnapshot.isEmpty()) {
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Customer customer = document.toObject(Customer.class);
                    customer.setCustomerID(document.getId());
                    fetchedCustomers.add(customer);
                }
            }

            // Áp dụng bộ lọc tìm kiếm (tên, điện thoại) trên client sau khi đã tải tất cả khách hàng
            applySearchFilter(fetchedCustomers, searchQuery);

        }).addOnFailureListener(e -> {
            showToast("Failed to load customers: " + e.getMessage());
        });

        // Đã loại bỏ các truy vấn và cập nhật liên quan đến tổng số khách hàng hoặc phân trang
    }

    // Phương thức mới để lọc tìm kiếm trên client
    private void applySearchFilter(List<Customer> fetchedCustomers, String searchQuery) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : fetchedCustomers) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    (customer.getCustomerName() != null && customer.getCustomerName().toLowerCase().contains(searchQuery)) ||
                    (customer.getCustomerPhone() != null && customer.getCustomerPhone().toLowerCase().contains(searchQuery));
            if (matchesSearch) {
                filteredList.add(customer);
            }
        }
        // Cập nhật danh sách hiển thị trên RecyclerView
        customerAdapter.updateList(filteredList);
    }

    // Phương thức applyFilters giờ chỉ có nhiệm vụ gọi loadCustomers để tải lại dữ liệu với bộ lọc mới
    private void applyFilters() {
        loadCustomers(); // Tải lại dữ liệu với các bộ lọc hiện tại
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
        } else if (itemId == R.id.menu_broadcast_advertising) {
            showToast("Broadcast advertising not implemented yet");
        } else if (itemId == R.id.menu_help) {
            showToast("Open help website not implemented yet");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_CODE_NEW_CUSTOMER || requestCode == REQUEST_CODE_EDIT_CUSTOMER)) {
            loadCustomers(); // Tải lại tất cả khách hàng sau khi thêm/sửa
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