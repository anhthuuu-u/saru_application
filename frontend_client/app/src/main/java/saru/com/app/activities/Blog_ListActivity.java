package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.connectors.BlogCategoryAdapter;
import saru.com.app.connectors.BlogSuggestionAdapter;
import saru.com.app.models.Blog;
import saru.com.app.models.BlogCategory;

public class Blog_ListActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories, recyclerSuggestions;
    private BlogCategoryAdapter adapter;
    private BlogSuggestionAdapter blogSuggestionAdapter;
    private List<BlogCategory> categoryList = new ArrayList<>();
    private List<Blog> blogList = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private FirebaseFirestore db;
    private ImageView imgBlogList_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_list);
        addView();
        addEvents();
        loadData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addView() {
        db = FirebaseFirestore.getInstance();

        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new BlogCategoryAdapter(this, categoryList); // Truyền Context và danh sách
        recyclerCategories.setAdapter(adapter);

        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        blogSuggestionAdapter = new BlogSuggestionAdapter(this, blogList, categoryMap); // Đảm bảo constructor khớp
        recyclerSuggestions.setAdapter(blogSuggestionAdapter);

        imgBlogList_Back = findViewById(R.id.ic_back_arrow); // Sửa ID cho đúng
    }

    private void loadData() {
        // Load danh mục trước
        db.collection("blogcategories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    categoryMap.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BlogCategory category = doc.toObject(BlogCategory.class);
                        if (category.getCateblogID() != null && category.getName() != null) {
                            categoryList.add(category);
                            categoryMap.put(category.getCateblogID(), category.getName());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    loadBlogs(); // gọi sau khi đã có map
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Lỗi khi tải danh mục", e));
    }

    private void loadBlogs() {
        db.collection("blogs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    blogList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Blog blog = doc.toObject(Blog.class);
                        blogList.add(blog);
                    }
                    blogSuggestionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải blog: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addEvents() {
        imgBlogList_Back.setOnClickListener(v -> startActivity(new Intent(Blog_ListActivity.this, Homepage.class)));
    }
}