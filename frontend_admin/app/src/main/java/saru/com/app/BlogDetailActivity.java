package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
        btnDelete.setOnClickListener(v -> deleteBlog());
    }

    private void deleteBlog() {
        if (blog == null) return;
        db.collection("blogs").document(blog.getBlogID()).delete()
                .addOnSuccessListener(aVoid -> {
                    showToast("Blog deleted");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to delete blog: " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_BLOG && resultCode == RESULT_OK) {
            finish(); // Reload blog list in BlogsManagementActivity
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}