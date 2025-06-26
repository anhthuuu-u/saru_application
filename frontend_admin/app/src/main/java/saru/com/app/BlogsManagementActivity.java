package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogs_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        loadBlogs();
        setupEvents();
    }

    private void initializeViews() {
        rvBlogs = findViewById(R.id.rvBlogs);
        btnAddBlog = findViewById(R.id.btnAddBlog);
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
        if (requestCode == REQUEST_CODE_ADD_EDIT_BLOG && resultCode == RESULT_OK) {
            loadBlogs();
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
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
                startActivity(intent);
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