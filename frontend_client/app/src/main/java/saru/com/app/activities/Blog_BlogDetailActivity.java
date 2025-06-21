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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.connectors.RelatedBlogAdapter;
import saru.com.app.models.Blog;

public class Blog_BlogDetailActivity extends AppCompatActivity {

    ImageView imgDetailBlog_Back, imgBlogDetail;
    TextView txtBlogTitle, txtCategoryName, txtBlogContent;
    RecyclerView recyclerRelatedBlogs;
    RelatedBlogAdapter relatedAdapter;
    List<Blog> relatedBlogList;
    Map<String, String> categoryMap;
    FirebaseFirestore db;
    String blogID, cateblogID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_blog_detail);
        db = FirebaseFirestore.getInstance();
        relatedBlogList = new ArrayList<>();
        categoryMap = new HashMap<>();
        addView();
        addEvents();
        loadCategoryMap();
        loadBlogDetail();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addView() {
        imgDetailBlog_Back = findViewById(R.id.imgDetailBlog_Back);
        imgBlogDetail = findViewById(R.id.imgBlogDetail);
        txtBlogTitle = findViewById(R.id.txtBlogTitle);
        txtCategoryName = findViewById(R.id.txtCategoryName);
        txtBlogContent = findViewById(R.id.txtBlogContent);
        recyclerRelatedBlogs = findViewById(R.id.recyclerRelatedBlogs);
        if (recyclerRelatedBlogs != null) {
            recyclerRelatedBlogs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            relatedAdapter = new RelatedBlogAdapter(this, relatedBlogList, categoryMap);
            recyclerRelatedBlogs.setAdapter(relatedAdapter);
            recyclerRelatedBlogs.setNestedScrollingEnabled(false);
            recyclerRelatedBlogs.setVisibility(View.VISIBLE);
            Log.d("Blog_Detail", "RecyclerView initialized with visibility: " + recyclerRelatedBlogs.getVisibility());
        } else {
            Log.e("Blog_Detail", "recyclerRelatedBlogs is null!");
        }
    }

    private void addEvents() {
        imgDetailBlog_Back.setOnClickListener(v -> backToBlogList());
    }

    private void backToBlogList() {
        Intent intent = new Intent(Blog_BlogDetailActivity.this, Blog_ListActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadCategoryMap() {
        db.collection("blogcategories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryMap.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        categoryMap.put(document.getId(), document.getString("name"));
                    }
                    Log.d("Blog_Detail", "Loaded " + categoryMap.size() + " categories");
                    if (cateblogID != null) {
                        txtCategoryName.setText(categoryMap.getOrDefault(cateblogID, "Không rõ danh mục"));
                    }
                })
                .addOnFailureListener(e -> Log.e("Blog_Detail", "Failed to load categories: " + e.getMessage()));
    }

    private void loadBlogDetail() {
        blogID = getIntent().getStringExtra("blogID");
        Log.d("Blog_Detail", "Loading blog with blogID: " + blogID);
        if (blogID != null) {
            db.collection("blogs").document(blogID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Blog blog = documentSnapshot.toObject(Blog.class);
                        if (blog != null) {
                            cateblogID = blog.getCateblogID();
                            Log.d("Blog_Detail", "cateblogID: " + cateblogID);
                            txtBlogTitle.setText(blog.getTitle() != null ? blog.getTitle() : "No Title");
                            txtBlogContent.setText(blog.getContent() != null ? blog.getContent() : "No Content");
                            txtCategoryName.setText(categoryMap.getOrDefault(cateblogID, "Không rõ danh mục"));
                            Glide.with(this)
                                    .load(blog.getImageUrl() != null ? blog.getImageUrl() : R.mipmap.img_taybacvillage)
                                    .placeholder(R.mipmap.img_taybacvillage)
                                    .into(imgBlogDetail);
                            loadRelatedBlogs();
                        } else {
                            Log.e("Blog_Detail", "Blog data is null for blogID: " + blogID);
                            Toast.makeText(this, "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Blog_Detail", "Failed to load blog: " + e.getMessage());
                        Toast.makeText(this, "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Blog_Detail", "blogID is null from Intent!");
            Toast.makeText(this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRelatedBlogs() {
        if (cateblogID == null || cateblogID.isEmpty()) {
            Log.e("Blog_Detail", "cateblogID is null or empty!");
            Toast.makeText(this, "Không thể tải tin liên quan: Danh mục không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Blog_Detail", "Loading related blogs for cateblogID: " + cateblogID);
        db.collection("blogs")
                .whereEqualTo("cateblogID", cateblogID)
                .whereNotEqualTo("blogID", blogID)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    relatedBlogList.clear();
                    Log.d("Blog_Detail", "Query returned " + queryDocumentSnapshots.size() + " documents");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Blog blog = document.toObject(Blog.class);
                        if (blog != null && blog.getBlogID() != null) {
                            relatedBlogList.add(blog);
                            Log.d("Blog_Detail", "Added related blog: " + blog.getBlogID() + ", Title: " + blog.getTitle());
                        }
                    }
                    Log.d("Blog_Detail", "Total related blogs: " + relatedBlogList.size());
                    if (relatedBlogList.isEmpty()) {
                        Log.w("Blog_Detail", "No related blogs found for cateblogID: " + cateblogID);
                        recyclerRelatedBlogs.setVisibility(View.GONE);
                        Toast.makeText(this, "Không có tin liên quan", Toast.LENGTH_SHORT).show();
                    } else {
                        recyclerRelatedBlogs.setVisibility(View.VISIBLE);
                    }
                    if (relatedAdapter != null) {
                        runOnUiThread(() -> {
                            relatedAdapter.notifyDataSetChanged();
                            Log.d("Blog_Detail", "Adapter notified with " + relatedBlogList.size() + " items");
                        });
                    } else {
                        Log.e("Blog_Detail", "relatedAdapter is null!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Blog_Detail", "Failed to load related blogs: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi tải tin liên quan", Toast.LENGTH_SHORT).show();
                    recyclerRelatedBlogs.setVisibility(View.GONE);
                });
    }
}