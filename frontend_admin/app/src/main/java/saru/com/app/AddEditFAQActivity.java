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
import saru.com.models.FAQ; // Import model FAQ

public class AddEditFAQActivity extends AppCompatActivity {
    private EditText edtTitle, edtContent;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private FAQ faq; // Đối tượng FAQ được chỉnh sửa hoặc thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_faq);
        db = FirebaseFirestore.getInstance();
        setupToolbar(); // Thiết lập Toolbar
        initializeViews();
        displayFAQ(); // Hiển thị dữ liệu FAQ nếu đang chỉnh sửa
        setupEvents();
    }

    private void initializeViews() {
        edtTitle = findViewById(R.id.edtFaqTitle);
        edtContent = findViewById(R.id.edtFaqContent);
        btnSave = findViewById(R.id.btnSaveFaq);
        btnCancel = findViewById(R.id.btnCancelFaq);
    }

    // Thiết lập Toolbar cho Activity
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_add_edit_faq);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            // Đặt tiêu đề dựa trên việc đang thêm mới hay chỉnh sửa
            if (getIntent().getSerializableExtra("SELECTED_FAQ") != null) {
                getSupportActionBar().setTitle(R.string.title_edit_faq); // Cần định nghĩa @string/title_edit_faq
            } else {
                getSupportActionBar().setTitle(R.string.title_add_faq); // Cần định nghĩa @string/title_add_faq
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

    // Hiển thị dữ liệu FAQ nếu được truyền từ Activity trước
    private void displayFAQ() {
        faq = (FAQ) getIntent().getSerializableExtra("SELECTED_FAQ");
        if (faq != null) {
            edtTitle.setText(faq.getTitle());
            edtContent.setText(faq.getContent());
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveFAQ()); // Xử lý sự kiện nút Save
        btnCancel.setOnClickListener(v -> finish()); // Đóng Activity khi nhấn Cancel
    }

    // Lưu hoặc cập nhật FAQ lên Firebase Firestore
    private void saveFAQ() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        // Kiểm tra xem các trường có trống không
        if (title.isEmpty() || content.isEmpty()) {
            showToast("Please fill all fields.");
            return;
        }

        // Nếu faq là null, tạo mới; ngược lại, cập nhật FAQ hiện có
        if (faq == null) {
            faq = new FAQ(UUID.randomUUID().toString(), title, content); // Tạo ID mới cho FAQ
        } else {
            faq.setTitle(title);
            faq.setContent(content);
        }

        // Lưu FAQ vào Firestore
        db.collection("faqs").document(faq.getFaqID()).set(faq)
                .addOnSuccessListener(aVoid -> {
                    showToast("FAQ saved successfully!");
                    setResult(RESULT_OK); // Đặt kết quả OK để Activity gọi biết
                    finish(); // Đóng Activity
                })
                .addOnFailureListener(e -> showToast("Failed to save FAQ: " + e.getMessage()));
    }

    // Hiển thị Toast message
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
