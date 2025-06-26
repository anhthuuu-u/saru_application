package saru.com.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.FAQ; // Import model FAQ

public class FAQsManagementActivity extends AppCompatActivity {
    private RecyclerView rvFAQs;
    private Button btnAddFaq;
    private FirebaseFirestore db;
    private List<FAQ> faqs;
    private FAQAdapter adapter;
    private static final int REQUEST_CODE_ADD_EDIT_FAQ = 1100; // Request code cho Add/Edit FAQ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs_management);
        db = FirebaseFirestore.getInstance();
        setupToolbar(); // Thiết lập Toolbar
        initializeViews();
        setupRecyclerView();
        loadFAQs(); // Tải danh sách FAQs
        setupEvents();
    }

    private void initializeViews() {
        rvFAQs = findViewById(R.id.rvFaqs);
        btnAddFaq = findViewById(R.id.btnAddFaq);
    }

    // Thiết lập Toolbar cho Activity
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_faq_management);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setTitle(R.string.title_faqs_management); // Đặt tiêu đề
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

    private void setupRecyclerView() {
        faqs = new ArrayList<>();
        adapter = new FAQAdapter(faqs);
        rvFAQs.setLayoutManager(new LinearLayoutManager(this));
        rvFAQs.setAdapter(adapter);
    }

    // Tải danh sách FAQs từ Firebase Firestore
    private void loadFAQs() {
        db.collection("faqs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    faqs.clear(); // Xóa dữ liệu cũ
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FAQ faq = document.toObject(FAQ.class);
                        faqs.add(faq);
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                })
                .addOnFailureListener(e -> showToast("Failed to load FAQs: " + e.getMessage()));
    }

    private void setupEvents() {
        btnAddFaq.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditFAQActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_FAQ); // Mở màn hình Add/Edit FAQ để thêm mới
        });
    }

    // Xử lý kết quả trả về từ AddEditFAQActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT_FAQ && resultCode == RESULT_OK) {
            loadFAQs(); // Tải lại danh sách FAQs khi thêm/sửa thành công
        }
    }

    // Hiển thị Toast message
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    // Phương thức hiển thị dialog xác nhận xóa FAQ
    private void showDeleteConfirmationDialog(final FAQ faqToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title) // Tiêu đề popup, dùng chung với Blog
                .setMessage(getString(R.string.confirm_delete_message, faqToDelete.getTitle())) // Message, dùng chung với Blog
                .setPositiveButton(R.string.confirm_delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFAQ(faqToDelete); // Gọi phương thức xóa khi người dùng xác nhận
                    }
                })
                .setNegativeButton(R.string.confirm_delete_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Phương thức xóa FAQ khỏi Firestore và cập nhật UI
    private void deleteFAQ(FAQ faq) {
        if (faq != null && faq.getFaqID() != null) {
            db.collection("faqs").document(faq.getFaqID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast("FAQ deleted successfully!");
                        loadFAQs(); // Tải lại danh sách FAQs để cập nhật UI
                    })
                    .addOnFailureListener(e -> showToast("Failed to delete FAQ: " + e.getMessage()));
        } else {
            showToast("Cannot delete a null FAQ or FAQ with no ID.");
        }
    }

    // Adapter cho RecyclerView để hiển thị danh sách FAQs
    private class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder> {
        private List<FAQ> faqList;

        FAQAdapter(List<FAQ> faqList) {
            this.faqList = faqList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FAQ faq = faqList.get(position);
            holder.txtTitle.setText(faq.getTitle());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(FAQsManagementActivity.this, AddEditFAQActivity.class);
                intent.putExtra("SELECTED_FAQ", faq);
                startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_FAQ); // Mở màn hình Add/Edit FAQ để sửa
            });

            // Thêm LongClickListener để xóa FAQ
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(faq); // Hiển thị popup xác nhận khi giữ lâu
                return true; // Trả về true để tiêu thụ sự kiện long click
            });
        }

        @Override
        public int getItemCount() {
            return faqList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;

            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtFaqTitle);
            }
        }
    }
}
