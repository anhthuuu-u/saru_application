package saru.com.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import saru.com.models.Blog;
import saru.com.models.BlogCategory;

public class AddEditBlogActivity extends AppCompatActivity {
    private EditText edtTitle, edtContent, edtImageUrl;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel, btnDelete, btnUploadImage;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Blog blog;
    private List<BlogCategory> categories;
    private ArrayAdapter<String> categoryAdapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_blog);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        initializeViews();
        setupPermissionAndImagePicker();
        loadCategories();
        displayBlog();
        setupEvents();
    }

    private void initializeViews() {
        edtTitle = findViewById(R.id.title_input);
        edtContent = findViewById(R.id.content_input);
        edtImageUrl = findViewById(R.id.image_url_input);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.save_button);
        btnCancel = findViewById(R.id.cancel_button);
        btnDelete = findViewById(R.id.delete_button);
        btnUploadImage = findViewById(R.id.upload_image_button);
        categories = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupPermissionAndImagePicker() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openImagePicker();
            } else {
                showToast("Permission denied to access storage");
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                uploadImageToStorage(imageUri);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("blog_images/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                // Kiểm tra dữ liệu hiện tại để đảm bảo không ghi đè nếu không cần thiết
                                if (blog == null || !imageUrl.equals(blog.getImageUrl())) {
                                    edtImageUrl.setText(imageUrl);
                                }
                                showToast("Image uploaded successfully");
                            }))
                    .addOnFailureListener(e -> showToast("Failed to upload image: " + e.getMessage()));
        }
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
            // Đảm bảo cateblogID khớp với dữ liệu hiện tại
            if (!blog.getCateblogID().isEmpty()) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCateblogID().equals(blog.getCateblogID())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void setupEvents() {
        btnUploadImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSave.setOnClickListener(v -> saveBlog());
        btnCancel.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> deleteBlog());
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openImagePicker();
        }
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

    private void deleteBlog() {
        if (blog != null) {
            db.collection("blogs").document(blog.getBlogID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast("Blog deleted");
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> showToast("Failed to delete blog: " + e.getMessage()));
        } else {
            showToast("No blog to delete");
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}