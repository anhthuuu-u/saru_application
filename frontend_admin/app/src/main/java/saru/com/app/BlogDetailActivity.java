package saru.com.app;

import android.content.DialogInterface; // Import for AlertDialog
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog; // Import for AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import saru.com.models.Blog;


public class BlogDetailActivity extends AppCompatActivity {
    private TextView txtTitle, txtContent;
    private ImageView imgBlog;
    private Button btnEdit, btnDelete;
    private FirebaseFirestore db;
    private Blog blog;
    private static final int REQUEST_CODE_EDIT_BLOG = 801;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);
        db = FirebaseFirestore.getInstance();
        setupToolbar(); // Thiết lập Toolbar
        initializeViews();
        displayBlog();
        setupEvents();
    }

    private void initializeViews() {
        txtTitle = findViewById(R.id.txtBlogTitle);
        txtContent = findViewById(R.id.txtBlogContent);
        imgBlog = findViewById(R.id.imgBlog);
        btnEdit = findViewById(R.id.btnEditBlog);
        btnDelete = findViewById(R.id.btnDeleteBlog);
    }

    // Phương thức thiết lập Toolbar
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_detail); // ID của toolbar trong activity_blog_detail.xml
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setTitle(R.string.title_blog_detail); // Đặt tiêu đề
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Xử lý khi nút quay lại được nhấn
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayBlog() {
        blog = (Blog) getIntent().getSerializableExtra("SELECTED_BLOG");
        if (blog != null) {
            txtTitle.setText(blog.getTitle());
            txtContent.setText(blog.getContent());
            Glide.with(this).load(blog.getImageUrl()).into(imgBlog);
        }
    }

    private void setupEvents() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditBlogActivity.class);
            intent.putExtra("SELECTED_BLOG", blog);
            startActivityForResult(intent, REQUEST_CODE_EDIT_BLOG);
        });
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog()); // Gọi dialog xác nhận
    }

    // Phương thức hiển thị dialog xác nhận xóa blog
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, blog.getTitle()))
                .setPositiveButton(R.string.confirm_delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBlog(); // Gọi phương thức xóa khi người dùng xác nhận
                    }
                })
                .setNegativeButton(R.string.confirm_delete_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Phương thức xóa blog khỏi Firestore
    private void deleteBlog() {
        if (blog == null) {
            showToast("No blog to delete.");
            return;
        }

        db.collection("blogs").document(blog.getBlogID()).delete()
                .addOnSuccessListener(aVoid -> {
                    showToast("Blog deleted successfully!");
                    setResult(RESULT_OK); // Đặt kết quả thành OK để BlogsManagementActivity biết
                    finish(); // Đóng BlogDetailActivity
                })
                .addOnFailureListener(e -> showToast("Failed to delete blog: " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_BLOG && resultCode == RESULT_OK) {
            // Nếu chỉnh sửa thành công, cũng cần thông báo cho Activity gốc cập nhật
            setResult(RESULT_OK); // Đặt kết quả OK
            finish(); // Đóng BlogDetailActivity để BlogManagementActivity tải lại
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
