package saru.com.app;

import android.os.Bundle;
import android.view.MenuItem; // Import MenuItem
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;
import saru.com.models.Voucher; // Import model Voucher

public class AddEditVoucherActivity extends AppCompatActivity {
    private EditText edtDescription, edtCode, edtExpiryDate; // Đổi tên để rõ ràng hơn
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private Voucher voucher; // Đối tượng Voucher được chỉnh sửa hoặc thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_voucher);
        db = FirebaseFirestore.getInstance();
        setupToolbar(); // Thiết lập Toolbar
        initializeViews();
        displayVoucher(); // Hiển thị dữ liệu Voucher nếu đang chỉnh sửa
        setupEvents();
    }

    private void initializeViews() {
        edtDescription = findViewById(R.id.textDescription);
        edtCode = findViewById(R.id.textField);
        edtExpiryDate = findViewById(R.id.endDate); // Đổi tên biến để khớp với XML
        btnSave = findViewById(R.id.btnSaveVoucher);
        btnCancel = findViewById(R.id.btnCancelVoucher);
    }

    // Thiết lập Toolbar cho Activity
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_add_edit_voucher);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            // Đặt tiêu đề dựa trên việc đang thêm mới hay chỉnh sửa
            if (getIntent().getSerializableExtra("SELECTED_VOUCHER") != null) {
                getSupportActionBar().setTitle(R.string.title_edit_voucher); // Cần định nghĩa @string/title_edit_voucher
            } else {
                getSupportActionBar().setTitle(R.string.title_add_voucher); // Cần định nghĩa @string/title_add_voucher
            }
        }
    }

    // Xử lý sự kiện khi nút trên Toolbar được chọn (ví dụ: nút quay lại)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Xử lý khi nút quay lại được nhấn
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Hiển thị dữ liệu Voucher nếu được truyền từ Activity trước
    private void displayVoucher() {
        voucher = (Voucher) getIntent().getSerializableExtra("SELECTED_VOUCHER");
        if (voucher != null) {
            edtDescription.setText(voucher.getDescription());
            edtCode.setText(voucher.getVoucherCode());
            edtExpiryDate.setText(voucher.getExpiryDate());
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveVoucher()); // Xử lý sự kiện nút Save
        btnCancel.setOnClickListener(v -> finish()); // Đóng Activity khi nhấn Cancel
    }

    // Lưu hoặc cập nhật Voucher lên Firebase Firestore
    private void saveVoucher() {
        String description = edtDescription.getText().toString().trim();
        String code = edtCode.getText().toString().trim();
        String expiry = edtExpiryDate.getText().toString().trim();

        // Kiểm tra xem các trường có trống không
        if (description.isEmpty() || code.isEmpty() || expiry.isEmpty()) {
            showToast("Please fill all fields.");
            return;
        }

        // Nếu voucher là null, tạo mới; ngược lại, cập nhật Voucher hiện có
        if (voucher == null) {
            voucher = new Voucher(UUID.randomUUID().toString(), description, expiry, code); // Tạo ID mới cho Voucher
        } else {
            voucher.setDescription(description);
            voucher.setVoucherCode(code);
            voucher.setExpiryDate(expiry);
        }

        // Lưu Voucher vào Firestore
        db.collection("vouchers").document(voucher.getVoucherID()).set(voucher)
                .addOnSuccessListener(aVoid -> {
                    showToast("Voucher saved successfully!");
                    setResult(RESULT_OK); // Đặt kết quả OK để Activity gọi biết
                    finish(); // Đóng Activity
                })
                .addOnFailureListener(e -> showToast("Failed to save Voucher: " + e.getMessage()));
    }

    // Hiển thị Toast message
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
