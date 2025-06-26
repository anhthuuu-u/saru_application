package saru.com.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.Blog;

public class BlogsManagementActivity extends AppCompatActivity {
    private RecyclerView rvBlogs;
    private Button btnAddBlog;
    private FirebaseFirestore db;
    private List<Blog> blogs;
    private BlogAdapter adapter;
    private static final int REQUEST_CODE_ADD_EDIT_BLOG = 800;
    private static final int REQUEST_CODE_BLOG_DETAIL = 802; // Request code mới cho BlogDetailActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogs_management);
        db = FirebaseFirestore.getInstance();
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        loadBlogs();
        setupEvents();
    }

    private void initializeViews() {
        rvBlogs = findViewById(R.id.rvBlogs);
        btnAddBlog = findViewById(R.id.btnAddBlog);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_management);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_blogs_management);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        blogs = new ArrayList<>();
        adapter = new BlogAdapter(blogs);
        rvBlogs.setLayoutManager(new LinearLayoutManager(this));
        rvBlogs.setAdapter(adapter);
    }

    private void loadBlogs() {
        db.collection("blogs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    blogs.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Blog blog = document.toObject(Blog.class);
                        blogs.add(blog);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load blogs: " + e.getMessage()));
    }

    private void setupEvents() {
        btnAddBlog.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditBlogActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_BLOG);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Kiểm tra nếu kết quả đến từ AddEditBlogActivity HOẶC BlogDetailActivity
        if ((requestCode == REQUEST_CODE_ADD_EDIT_BLOG || requestCode == REQUEST_CODE_BLOG_DETAIL)
                && resultCode == RESULT_OK) {
            loadBlogs(); // Tải lại danh sách blog khi có thay đổi (thêm/sửa/xóa)
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    // Phương thức hiển thị dialog xác nhận xóa blog (khi long click trên danh sách)
    private void showDeleteConfirmationDialog(final Blog blogToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, blogToDelete.getTitle()))
                .setPositiveButton(R.string.confirm_delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBlogFromManagement(blogToDelete); // Gọi phương thức xóa trực tiếp
                    }
                })
                .setNegativeButton(R.string.confirm_delete_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Phương thức xóa blog trực tiếp từ màn hình quản lý (khi long click)
    private void deleteBlogFromManagement(Blog blog) {
        if (blog != null && blog.getBlogID() != null) {
            db.collection("blogs").document(blog.getBlogID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast("Blog deleted successfully!");
                        loadBlogs(); // Tải lại danh sách ngay lập tức sau khi xóa
                    })
                    .addOnFailureListener(e -> showToast("Failed to delete blog: " + e.getMessage()));
        } else {
            showToast("Cannot delete a null blog or blog with no ID.");
        }
    }

    private class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {
        private List<Blog> blogList;

        BlogAdapter(List<Blog> blogList) {
            this.blogList = blogList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Blog blog = blogList.get(position);
            holder.txtTitle.setText(blog.getTitle());
            Glide.with(holder.itemView.getContext()).load(blog.getImageUrl()).into(holder.imgBlog);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(BlogsManagementActivity.this, BlogDetailActivity.class);
                intent.putExtra("SELECTED_BLOG", blog);
                startActivityForResult(intent, REQUEST_CODE_BLOG_DETAIL); // Quan trọng: dùngForResult
            });

            holder.itemView.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(blog); // Dùng dialog xác nhận cho long click
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return blogList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;
            ImageView imgBlog;

            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtBlogTitle);
                imgBlog = itemView.findViewById(R.id.imgBlog);
            }
        }
    }
}
