package saru.com.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import saru.com.app.R;

public class LiveChatActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_CODE = 1001;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1002;

    private LinearLayout chatMessagesContainer;
    private EditText inputMessage;
    private ImageView btnSendMessage;
    private ScrollView scrollViewMessages;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri photoUri;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_live_chat);

        initializeFirebase();
        initializeViews();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }
        userUID = mAuth.getCurrentUser().getUid();
        setupMessageListener();

        // Add long-click on header to simulate admin message (for testing)
        TextView headerTitle = findViewById( R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setOnLongClickListener(v -> {
                simulateAdminMessage();
                return true;
            });
        }
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void initializeViews() {
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(LiveChatActivity.this, CustomerSupportActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatMessagesContainer = findViewById(R.id.chat_messages_container);
        inputMessage = findViewById(R.id.input_message);
        btnSendMessage = findViewById(R.id.img_send);
        scrollViewMessages = findViewById(R.id.scroll_view_messages);

        btnSendMessage.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, R.string.error_enter_message, Toast.LENGTH_SHORT).show();
            } else {
                sendTextMessage(message);
                inputMessage.setText("");
                hideKeyboard();
            }
        });

        ImageView imgAdd = findViewById(R.id.img_add);
        imgAdd.setOnClickListener(v -> showImagePickerDialog());
    }

    private void setupMessageListener() {
        db.collection("chats")
                .document(userUID)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, R.string.load_messages_error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Clear UI only on initial load or significant changes
                    if (snapshots.getMetadata().isFromCache() && snapshots.getDocumentChanges().size() == snapshots.size()) {
                        chatMessagesContainer.removeAllViews();
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String senderType = dc.getDocument().getString("senderType");
                            String contentType = dc.getDocument().getString("contentType");
                            String content = dc.getDocument().getString("content");
                            Date timestamp = dc.getDocument().getDate("timestamp");

                            String formattedTimestamp = formatTimestamp(timestamp);

                            if ("text".equals(contentType)) {
                                addTextMessageToChat(content, formattedTimestamp, "customer".equals(senderType));
                            } else if ("image".equals(contentType)) {
                                addImageToChat(Uri.parse(content), formattedTimestamp, "customer".equals(senderType));
                            }
                        }
                    }
                });
    }

    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.message_timestamp_format), Locale.getDefault());
        return sdf.format(timestamp);
    }

    private void sendTextMessage(String message) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderType", "customer");
        messageData.put("senderID", userUID);
        messageData.put("contentType", "text");
        messageData.put("content", message);
        messageData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("chats")
                .document(userUID)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, R.string.message_sent_success, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.message_send_error, Toast.LENGTH_SHORT).show();
                });
    }

    private void sendImageMessage(Uri imageUri) {
        StorageReference storageRef = storage.getReference()
                .child("chat_images/" + userUID + "/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("senderType", "customer");
                    messageData.put("senderID", userUID);
                    messageData.put("contentType", "image");
                    messageData.put("content", uri.toString());
                    messageData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("chats")
                            .document(userUID)
                            .collection("messages")
                            .add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, R.string.image_upload_success, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, R.string.image_upload_error, Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.image_upload_error, Toast.LENGTH_SHORT).show();
                });
    }

    private void simulateAdminMessage() {
        // For testing: Simulate an admin message
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderType", "admin");
        messageData.put("senderID", "admin123"); // Example admin ID
        messageData.put("contentType", "text");
        messageData.put("content", "Hello, how can we assist you today?");
        messageData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("chats")
                .document(userUID)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, R.string.message_sent_success, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.message_send_error, Toast.LENGTH_SHORT).show();
                });
    }

    private void showImagePickerDialog() {
        String[] options = new String[]{
                getString(R.string.option_take_photo),
                getString(R.string.option_choose_gallery)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_choose_image))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, R.string.error_create_file, Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                sendImageMessage(imageUri);
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                if (photoUri != null) {
                    sendImageMessage(photoUri);
                }
            }
        }
    }

    private void addTextMessageToChat(String message, String timestamp, boolean isCustomer) {
        LinearLayout messageWrapper = new LinearLayout(this);
        messageWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        messageWrapper.setOrientation(LinearLayout.VERTICAL);
        messageWrapper.setGravity(isCustomer ? Gravity.END : Gravity.START);
        messageWrapper.setPadding(8, 8, 8, 8);

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(14);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(24, 16, 24, 16);
        textView.setBackgroundResource(isCustomer ? R.drawable.bubble_right : R.drawable.bubble_left);
        textView.setMaxWidth(convertDpToPx(250));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(textParams);

        TextView timestampView = new TextView(this);
        timestampView.setText(timestamp);
        timestampView.setTextSize(12);
        timestampView.setTextColor(Color.GRAY);
        timestampView.setPadding(24, 4, 24, 4);

        LinearLayout.LayoutParams timestampParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        timestampParams.gravity = isCustomer ? Gravity.END : Gravity.START;
        timestampView.setLayoutParams(timestampParams);

        messageWrapper.addView(textView);
        messageWrapper.addView(timestampView);
        chatMessagesContainer.addView(messageWrapper);

        scrollViewMessages.post(() -> scrollViewMessages.fullScroll(View.FOCUS_DOWN));
    }

    private void addImageToChat(Uri imageUri, String timestamp, boolean isCustomer) {
        LinearLayout messageWrapper = new LinearLayout(this);
        messageWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        messageWrapper.setOrientation(LinearLayout.VERTICAL);
        messageWrapper.setGravity(isCustomer ? Gravity.END : Gravity.START);
        messageWrapper.setPadding(8, 8, 8, 8);

        ImageView imageView = new ImageView(this);
        int maxWidthPx = convertDpToPx(250);
        int maxHeightPx = convertDpToPx(250);

        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(maxWidthPx);
        imageView.setMaxHeight(maxHeightPx);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageURI(imageUri);
        imageView.setBackgroundResource(isCustomer ? R.drawable.bubble_right : R.drawable.bubble_left);
        imageView.setPadding(8, 8, 8, 8);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(imageParams);

        TextView timestampView = new TextView(this);
        timestampView.setText(timestamp);
        timestampView.setTextSize(12);
        timestampView.setTextColor(Color.GRAY);
        timestampView.setPadding(24, 4, 24, 4);

        LinearLayout.LayoutParams timestampParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        timestampParams.gravity = isCustomer ? Gravity.END : Gravity.START;
        timestampView.setLayoutParams(timestampParams);

        messageWrapper.addView(imageView);
        messageWrapper.addView(timestampView);
        chatMessagesContainer.addView(messageWrapper);

        scrollViewMessages.post(() -> scrollViewMessages.fullScroll(View.FOCUS_DOWN));
    }

    private int convertDpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}