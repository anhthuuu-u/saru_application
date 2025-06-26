package saru.com.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import saru.com.models.Blog;
import saru.com.models.BlogCategory;

public class AddEditBlogActivity extends AppCompatActivity {
    private EditText edtTitle, edtContent, edtImageUrl;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private Blog blog;
    private List<BlogCategory> categories;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_blog);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadCategories();
        displayBlog();
        setupEvents();
    }

    private void initializeViews() {
        edtTitle = findViewById(R.id.edtBlogTitle);
        edtContent = findViewById(R.id.edtBlogContent);
        edtImageUrl = findViewById(R.id.edtBlogImageUrl);
        spinnerCategory = findViewById(R.id.spinnerBlogCategory);
        btnSave = findViewById(R.id.btnSaveBlog);
        btnCancel = findViewById(R.id.btnCancelBlog);
        categories = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        db.collection("blogcategories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categories.clear();
                    List<String> categoryNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BlogCategory category = document.toObject(BlogCategory.class);
                        categories.add(category);
                        categoryNames.add(category.getName());
                    }
                    categoryAdapter.clear();
                    categoryAdapter.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                    if (blog != null) {
                        for (int i = 0; i < categories.size(); i++) {
                            if (categories.get(i).getCateblogID().equals(blog.getCateblogID())) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load categories: " + e.getMessage()));
    }

    private void displayBlog() {
        blog = (Blog) getIntent().getSerializableExtra("SELECTED_BLOG");
        if (blog != null) {
            edtTitle.setText(blog.getTitle());
            edtContent.setText(blog.getContent());
            edtImageUrl.setText(blog.getImageUrl());
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveBlog());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveBlog() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();
        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        if (title.isEmpty() || content.isEmpty() || imageUrl.isEmpty() || categoryPosition < 0) {
            showToast("Please fill all fields");
            return;
        }

        String cateblogID = categories.get(categoryPosition).getCateblogID();
        if (blog == null) {
            blog = new Blog(UUID.randomUUID().toString(), cateblogID, content, imageUrl, title);
        } else {
            blog.setTitle(title);
            blog.setContent(content);
            blog.setImageUrl(imageUrl);
            blog.setCateblogID(cateblogID);
        }

        db.collection("blogs").document(blog.getBlogID()).set(blog)
                .addOnSuccessListener(aVoid -> {
                    showToast("Blog saved");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save blog: " + e.getMessage()));
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}