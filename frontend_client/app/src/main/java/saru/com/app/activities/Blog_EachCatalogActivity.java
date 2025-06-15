package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.connectors.BlogEachCatalogAdapter;
import saru.com.app.models.Blog;
import saru.com.app.models.BlogCategory;

public class Blog_EachCatalogActivity extends AppCompatActivity {

    ImageView imgEachCatalog_Back;
    TextView txtCatalogName;
    RecyclerView recyclerViewBlogs;
    BlogEachCatalogAdapter blogAdapter;
    List<Blog> blogList;
    FirebaseFirestore db;
    String categoryImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_each_catalog);
        db = FirebaseFirestore.getInstance();
        blogList = new ArrayList<>();
        addView();
        addEvents();
        loadCategoryData();
        loadBlogs();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addView() {
        imgEachCatalog_Back = findViewById(R.id.imgBlogList_Back);
        txtCatalogName = findViewById(R.id.textView); // Liên kết với TextView trong layout
        recyclerViewBlogs = findViewById(R.id.recyclerViewBlogs);
        if (recyclerViewBlogs != null) {
            recyclerViewBlogs.setLayoutManager(new LinearLayoutManager(this));
            blogAdapter = new BlogEachCatalogAdapter(this, blogList, categoryImageUrl);
            recyclerViewBlogs.setAdapter(blogAdapter);
        } else {
            Log.e("Blog_EachCatalog", "RecyclerView not found in layout!");
        }
    }

    private void addEvents() {
        imgEachCatalog_Back.setOnClickListener(v -> backToBlogList());
    }

    private void backToBlogList() {
        Intent intent = new Intent(Blog_EachCatalogActivity.this, Blog_ListActivity.class);
        startActivity(intent);
    }

    private void loadCategoryData() {
        String cateblogID = getIntent().getStringExtra("cateblogID");
        Log.d("Blog_EachCatalog", "Received cateblogID: " + cateblogID);
        if (cateblogID != null) {
            db.collection("blogcategories").document(cateblogID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        BlogCategory category = documentSnapshot.toObject(BlogCategory.class);
                        if (category != null) {
                            txtCatalogName.setText(category.getName() != null ? category.getName() : "Unknown Category"); // Đặt tiêu đề vào TextView
                            categoryImageUrl = category.getImageUrl();
                            if (blogAdapter != null) {
                                blogAdapter = new BlogEachCatalogAdapter(this, blogList, categoryImageUrl);
                                recyclerViewBlogs.setAdapter(blogAdapter);
                            }
                        } else {
                            Log.e("Blog_EachCatalog", "Category data is null for cateblogID: " + cateblogID);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Blog_EachCatalog", "Failed to load category data: " + e.getMessage());
                        Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Blog_EachCatalog", "cateblogID is null from Intent!");
        }
    }

    private void loadBlogs() {
        String cateblogID = getIntent().getStringExtra("cateblogID");
        Log.d("Blog_EachCatalog", "Loading blogs for cateblogID: " + cateblogID);
        if (cateblogID != null) {
            db.collection("blogs")
                    .whereEqualTo("cateblogID", cateblogID)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        blogList.clear();
                        Log.d("Blog_EachCatalog", "Found " + queryDocumentSnapshots.size() + " blogs");
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Blog blog = document.toObject(Blog.class);
                            blogList.add(blog);
                        }
                        if (blogAdapter != null) {
                            blogAdapter = new BlogEachCatalogAdapter(this, blogList, categoryImageUrl);
                            recyclerViewBlogs.setAdapter(blogAdapter);
                            blogAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("Blog_EachCatalog", "blogAdapter is null!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Blog_EachCatalog", "Failed to load blogs: " + e.getMessage());
                        Toast.makeText(this, "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Blog_EachCatalog", "cateblogID is null when loading blogs!");
        }
    }
}