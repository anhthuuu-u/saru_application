package saru.com.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import saru.com.app.R;
import saru.com.app.connectors.OrderDetailAdapter;
import saru.com.app.models.OrderDetail;
import saru.com.app.models.Rating;

public class OrderReviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int MAX_IMAGE_COUNT = 3;
    private static final int STORAGE_PERMISSION_CODE = 1;

    private ImageView icBackArrow;
    private Button submitButton;
    private EditText reviewInput;
    private RatingBar ratingBar;
    private LinearLayout layoutSelectedImages;
    private TextView txtShowOrderCode, txtShowOrderDate, txtShowStatus;
    private ListView lvOrderDetail;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailList;
    private FirebaseFirestore db;
    private ArrayList<Uri> imageUriList;
    private String customerID;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_review);

        // Initialize Firebase and variables
        db = FirebaseFirestore.getInstance();
        imageUriList = new ArrayList<>();
        orderDetailList = new ArrayList<>();

        // Initialize views
        icBackArrow = findViewById(R.id.ic_back_arrow);
        submitButton = findViewById(R.id.review_submit_button);
        reviewInput = findViewById(R.id.review_input);
        ratingBar = findViewById(R.id.rating_bar);
        layoutSelectedImages = findViewById(R.id.layout_selected_images);
        txtShowOrderCode = findViewById(R.id.txtShowOrderCode);
        txtShowOrderDate = findViewById(R.id.txtShowOrderDate);
        txtShowStatus = findViewById(R.id.txtShowStatus);
        lvOrderDetail = findViewById(R.id.lvOrderDetail);

        // Set up ListView adapter
        orderDetailAdapter = new OrderDetailAdapter(this, orderDetailList);
        lvOrderDetail.setAdapter(orderDetailAdapter);
        submitButton.setEnabled(false); // Disable until data is loaded

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        // Get orderId from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            showCustomToast("Invalid order ID. Please try again.");
            finish();
            return;
        }

        // Fetch order details
        fetchOrderDetails(orderId);

        // Set click listeners
        icBackArrow.setOnClickListener(v -> finish());
        layoutSelectedImages.setOnClickListener(v -> {
            if (imageUriList.size() < MAX_IMAGE_COUNT) {
                pickImageFromGallery();
            } else {
                showCustomToast(getString(R.string.max_images_error));
            }
        });

        submitButton.setOnClickListener(v -> {
            if (isDataLoaded) {
                float rating = ratingBar.getRating();
                String comment = reviewInput.getText().toString().trim();
                saveReview(orderId, customerID, rating, comment);
            } else {
                showCustomToast("Please wait for order data to load.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                showCustomToast("Storage permission is required to select images.");
            }
        }
    }

    private void fetchOrderDetails(String orderId) {
        try {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    String orderDate = document.getString("OrderDate");
                                    Object orderStatusIDObj = document.get("OrderStatusID");
                                    String orderStatusID = orderStatusIDObj != null ? orderStatusIDObj.toString() : null;
                                    customerID = document.getString("CustomerID");

                                    if (customerID == null || customerID.isEmpty()) {
                                        showCustomToast("Customer ID not found for this order.");
                                        customerID = "Unknown"; // Fallback value
                                    }

                                    txtShowOrderCode.setText(orderId != null ? orderId : "Unknown");
                                    txtShowOrderDate.setText(orderDate != null ? orderDate : "Unknown");

                                    if (orderStatusID != null) {
                                        db.collection("orderstatuses").document(orderStatusID)
                                                .get()
                                                .addOnCompleteListener(statusTask -> {
                                                    if (statusTask.isSuccessful()) {
                                                        DocumentSnapshot statusDoc = statusTask.getResult();
                                                        if (statusDoc != null && statusDoc.exists()) {
                                                            String orderStatus = statusDoc.getString("Status");
                                                            txtShowStatus.setText(orderStatus != null ? orderStatus : "Unknown");
                                                        } else {
                                                            txtShowStatus.setText("Unknown");
                                                        }
                                                    } else {
                                                        txtShowStatus.setText("Unknown");
                                                        showCustomToast("Failed to load order status.");
                                                    }
                                                    fetchOrderProducts(orderId);
                                                    submitButton.setEnabled(true);
                                                    isDataLoaded = true; // Mark data as loaded
                                                });
                                    } else {
                                        txtShowStatus.setText("Unknown");
                                        fetchOrderProducts(orderId);
                                        submitButton.setEnabled(true);
                                        isDataLoaded = true; // Mark data as loaded
                                    }
                                } else {
                                    showCustomToast("Order not found.");
                                    txtShowOrderCode.setText("Unknown");
                                    txtShowOrderDate.setText("Unknown");
                                    txtShowStatus.setText("Unknown");
                                    submitButton.setEnabled(true);
                                    isDataLoaded = true; // Allow manual retry
                                }
                            } else {
                                showCustomToast("Failed to load order details: " + task.getException().getMessage());
                                txtShowOrderCode.setText("Unknown");
                                txtShowOrderDate.setText("Unknown");
                                txtShowStatus.setText("Unknown");
                                submitButton.setEnabled(true);
                                isDataLoaded = true; // Allow manual retry
                            }
                        } catch (Exception e) {
                            showCustomToast("Error loading order details: " + e.getMessage());
                            submitButton.setEnabled(true);
                            isDataLoaded = true; // Allow manual retry
                        }
                    });
        } catch (Exception e) {
            showCustomToast("Error initiating order load: " + e.getMessage());
            submitButton.setEnabled(true);
            isDataLoaded = true; // Allow manual retry
        }
    }

    private void fetchOrderProducts(String orderId) {
        try {
            db.collection("orderdetails")
                    .whereEqualTo("OrderID", orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                orderDetailList.clear();
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String productID = document.getString("ProductID");
                                        Long quantityLong = document.getLong("Quantity");
                                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                        if (productID != null) {
                                            db.collection("products").document(productID)
                                                    .get()
                                                    .addOnCompleteListener(productTask -> {
                                                        try {
                                                            if (productTask.isSuccessful()) {
                                                                DocumentSnapshot productDoc = productTask.getResult();
                                                                if (productDoc != null && productDoc.exists()) {
                                                                    String productName = productDoc.getString("productName");
                                                                    Double productPrice = productDoc.getDouble("productPrice");
                                                                    String imageID = productDoc.getString("imageID");
                                                                    String brandID = productDoc.getString("brandID");

                                                                    if (imageID != null) {
                                                                        db.collection("image").document(imageID)
                                                                                .get()
                                                                                .addOnCompleteListener(imageTask -> {
                                                                                    try {
                                                                                        if (imageTask.isSuccessful()) {
                                                                                            DocumentSnapshot imageDoc = imageTask.getResult();
                                                                                            String productImageCover = imageDoc != null ? imageDoc.getString("ProductImageCover") : null;

                                                                                            if (brandID != null) {
                                                                                                db.collection("productBrand").document(brandID)
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(brandTask -> {
                                                                                                            try {
                                                                                                                if (brandTask.isSuccessful()) {
                                                                                                                    DocumentSnapshot brandDoc = brandTask.getResult();
                                                                                                                    String brandName = brandDoc != null ? brandDoc.getString("brandName") : "Unknown";
                                                                                                                    OrderDetail orderDetail = new OrderDetail(productID, productName, quantity, productPrice != null ? productPrice : 0.0, brandName);
                                                                                                                    orderDetail.setProductImageCover(productImageCover);
                                                                                                                    orderDetailList.add(orderDetail);
                                                                                                                    orderDetailAdapter.notifyDataSetChanged();
                                                                                                                } else {
                                                                                                                    showCustomToast("Error fetching brand details: " + brandTask.getException().getMessage());
                                                                                                                }
                                                                                                            } catch (Exception e) {
                                                                                                                showCustomToast("Error fetching brand details: " + e.getMessage());
                                                                                                            }
                                                                                                        });
                                                                                            } else {
                                                                                                showCustomToast("Brand ID not found for product: " + productID);
                                                                                            }
                                                                                        } else {
                                                                                            showCustomToast("Error fetching product image: " + imageTask.getException().getMessage());
                                                                                        }
                                                                                    } catch (Exception e) {
                                                                                        showCustomToast("Error fetching image details: " + e.getMessage());
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        showCustomToast("Image ID not found for product: " + productID);
                                                                    }
                                                                } else {
                                                                    showCustomToast("Product not found: " + productID);
                                                                }
                                                            } else {
                                                                showCustomToast("Error fetching product details: " + productTask.getException().getMessage());
                                                            }
                                                        } catch (Exception e) {
                                                            showCustomToast("Error fetching product details: " + e.getMessage());
                                                        }
                                                    });
                                        } else {
                                            showCustomToast("Product ID not found in order details.");
                                        }
                                    }
                                } else {
                                    showCustomToast("No products found for this order.");
                                }
                            } else {
                                showCustomToast("Failed to load order products: " + task.getException().getMessage());
                            }
                        } catch (Exception e) {
                            showCustomToast("Error loading order products: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            showCustomToast("Error initiating product load: " + e.getMessage());
        }
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
            } else {
                showCustomToast("Invalid image selected.");
            }
        }
    }

    private void displaySelectedImages() {
        layoutSelectedImages.removeAllViews();
        if (imageUriList.isEmpty()) {
            // Restore placeholder with camera and video icons
            layoutSelectedImages.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            ImageView cameraIcon = new ImageView(this);
            cameraIcon.setImageResource(android.R.drawable.ic_menu_camera);
            cameraIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            cameraIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ImageView videoIcon = new ImageView(this);
            videoIcon.setImageResource(android.R.drawable.ic_media_play);
            videoIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            videoIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutSelectedImages.addView(cameraIcon);
            layoutSelectedImages.addView(videoIcon);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cameraIcon.getLayoutParams();
            params.setMargins(0, 0, 16, 0); // Margin between icons
            cameraIcon.setLayoutParams(params);
        } else {
            // Remove background and display selected images
            layoutSelectedImages.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            for (Uri imageUri : imageUriList) {
                View imageItemView = getLayoutInflater().inflate(R.layout.item_image_remove, null);
                ImageView imageView = imageItemView.findViewById(R.id.image_view);
                ImageView removeImageView = imageItemView.findViewById(R.id.remove_image_view);

                if (imageView != null && removeImageView != null) {
                    imageView.setImageURI(imageUri);
                    // Long press listener for image deletion confirmation
                    imageView.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(OrderReviewActivity.this)
                                .setTitle("Delete Image")
                                .setMessage("Are you sure you want to delete this image?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    imageUriList.remove(imageUri);
                                    displaySelectedImages();
                                    showCustomToast("The image has been deleted.");
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    });
                    removeImageView.setOnClickListener(v -> {
                        imageUriList.remove(imageUri);
                        displaySelectedImages();
                    });
                    layoutSelectedImages.addView(imageItemView);
                } else {
                    showCustomToast("Error loading image preview.");
                }
            }
        }
    }

    private void saveReview(String orderId, String customerID, float rating, String comment) {
        if (comment.isEmpty()) {
            showCustomToast(getString(R.string.review_empty_error));
            return;
        }
        if (rating == 0) {
            showCustomToast(getString(R.string.rating_empty_error));
            return;
        }
        if (customerID == null || customerID.isEmpty()) {
            showCustomToast("Customer ID not found. Please try again.");
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String ratingDate = dateFormat.format(new Date());
        Rating review = new Rating(customerID, rating, comment, ratingDate);
        review.setOrderId(orderId);

        ArrayList<String> imageBase64List = new ArrayList<>();
        try {
            for (Uri imageUri : imageUriList) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); // Reduced quality to 30
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                imageBase64List.add(base64Image);
            }
        } catch (IOException e) {
            progressDialog.dismiss();
            showCustomToast(getString(R.string.image_upload_error) + ": " + e.getMessage());
            return;
        }

        long estimatedSize = comment.length() + ratingDate.length();
        estimatedSize += (customerID != null ? customerID.length() : 0);
        estimatedSize += (orderId != null ? orderId.length() : 0);
        for (String base64 : imageBase64List) {
            estimatedSize += base64.length();
        }
        if (estimatedSize > 900000) {
            progressDialog.dismiss();
            showCustomToast("Review data is too large. Please reduce image size or content.");
            return;
        }

        review.setImageUrls(imageBase64List);

        db.collection("reviews")
                .document(UUID.randomUUID().toString())
                .set(review)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showCustomToast(getString(R.string.review_submit_success));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showCustomToast(getString(R.string.review_submit_error) + ": " + e.getMessage());
                });
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.tv_toast_message);
        if (toastText != null) {
            toastText.setText(message);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}