package saru.com.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import saru.com.app.R;
import saru.com.app.models.Rating;

public class OrderReviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int MAX_IMAGE_COUNT = 3;

    private ImageView icBackArrow;
    private Button submitButton;
    private Button addPhotoButton;
    private EditText reviewInput;
    private RatingBar ratingBar;
    private LinearLayout layoutSelectedImages;
    private FirebaseFirestore db;
    private ArrayList<Uri> imageUriList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_review);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        imageUriList = new ArrayList<>();

        // Initialize views
        icBackArrow = findViewById(R.id.ic_back_arrow);
        submitButton = findViewById(R.id.review_submit_button);
        addPhotoButton = findViewById(R.id.btn_add_photo);
        reviewInput = findViewById(R.id.review_input);
        ratingBar = findViewById(R.id.rating_bar);
        layoutSelectedImages = findViewById(R.id.layout_selected_images);

        // Get orderId from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            showCustomToast("Invalid order ID. Please try again.");
            finish();
            return;
        }

        // Back button click listener
        icBackArrow.setOnClickListener(v -> finish());

        // Add photo button click listener
        addPhotoButton.setOnClickListener(v -> {
            if (imageUriList.size() < MAX_IMAGE_COUNT) {
                pickImageFromGallery();
            } else {
                showCustomToast(getString(R.string.max_images_error));
            }
        });

        // Submit button click listener
        submitButton.setOnClickListener(v -> {
            String userId = getCurrentUserId();
            float rating = ratingBar.getRating();
            String comment = reviewInput.getText().toString().trim();
            saveReview(orderId, userId, rating, comment);
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null && !imageUriList.contains(imageUri)) {
                imageUriList.add(imageUri);
                displaySelectedImages();
            }
        }
    }

    private void displaySelectedImages() {
        layoutSelectedImages.removeAllViews();
        for (Uri imageUri : imageUriList) {
            View imageItemView = getLayoutInflater().inflate(R.layout.item_image_remove, null);
            ImageView imageView = imageItemView.findViewById(R.id.image_view);
            ImageView removeImageView = imageItemView.findViewById(R.id.remove_image_view);

            imageView.setImageURI(imageUri);
            removeImageView.setOnClickListener(v -> {
                imageUriList.remove(imageUri);
                displaySelectedImages();
            });

            layoutSelectedImages.addView(imageItemView);
        }
    }

    private void saveReview(String orderId, String userId, float rating, String comment) {
        // Validation
        if (comment.isEmpty()) {
            showCustomToast(getString(R.string.review_empty_error));
            return;
        }
        if (rating == 0) {
            showCustomToast(getString(R.string.rating_empty_error));
            return;
        }
        if (userId == null || userId.isEmpty()) {
            showCustomToast("Please sign in to submit a review.");
            return;
        }
        if (orderId == null || orderId.isEmpty()) {
            showCustomToast("Invalid order ID.");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting review...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Create Rating object
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String ratingDate = dateFormat.format(new Date());
        Rating review = new Rating(userId, rating, comment, ratingDate);
        review.setOrderId(orderId);

        // Convert images to Base64
        ArrayList<String> imageBase64List = new ArrayList<>();
        try {
            for (Uri imageUri : imageUriList) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // Compress bitmap to reduce size
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress to 50% quality
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                imageBase64List.add(base64Image);
            }
        } catch (IOException e) {
            progressDialog.dismiss();
            showCustomToast(getString(R.string.image_upload_error));
            return;
        }

        // Check document size (approximate)
        long estimatedSize = comment.length() + ratingDate.length();
        estimatedSize += (userId != null ? userId.length() : 0);
        estimatedSize += (orderId != null ? orderId.length() : 0);
        for (String base64 : imageBase64List) {
            estimatedSize += base64.length();
        }
        if (estimatedSize > 900000) { // Firestore document size limit is ~1MB
            progressDialog.dismiss();
            showCustomToast("Review data is too large. Please reduce image size or content.");
            return;
        }

        review.setImageUrls(imageBase64List);

        // Save to Firestore
        db.collection("reviews")
                .document(UUID.randomUUID().toString())
                .set(review)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showCustomToast(getString(R.string.review_submit_success));
                    // Không gọi finish() để giữ người dùng ở trang
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showCustomToast(getString(R.string.review_submit_error));
                });
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.tv_toast_message);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }
}