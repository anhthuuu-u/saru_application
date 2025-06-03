package saru.com.app.activities;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import saru.com.app.R;

public class LiveChatActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_CODE = 1001;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1002;

    private LinearLayout chatMessagesContainer;
    private EditText inputMessage;
    private ImageView btnSendMessage;
    private ScrollView scrollViewMessages;

    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_live_chat);

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
                Toast.makeText(this, getString(R.string.error_enter_message), Toast.LENGTH_SHORT).show();
            } else {
                addMessageToChat(message);
                inputMessage.setText("");
                hideKeyboard();
            }
        });


        ImageView imgAdd = findViewById(R.id.img_add);
        imgAdd.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        String[] options = new String[] {
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
                ex.printStackTrace();
                Toast.makeText(this, getString(R.string.error_create_file), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.error_camera_unavailable), Toast.LENGTH_SHORT).show();
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
                addImageToChat(imageUri);
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                if (photoUri != null) {
                    addImageToChat(photoUri);
                }
            }
        }
    }

    private void addMessageToChat(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(14);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(24, 16, 24, 16);
        textView.setBackgroundResource(R.drawable.bubble_left);
        textView.setMaxWidth(convertDpToPx(250));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 8, 0, 8);
        textView.setLayoutParams(textParams);

        LinearLayout messageWrapper = new LinearLayout(this);
        messageWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        messageWrapper.setOrientation(LinearLayout.HORIZONTAL);
        messageWrapper.setGravity(Gravity.END);

        messageWrapper.addView(textView);
        chatMessagesContainer.addView(messageWrapper);

        scrollViewMessages.post(() -> scrollViewMessages.fullScroll(View.FOCUS_DOWN));
    }

    private void addImageToChat(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        int maxWidthPx = convertDpToPx(250);
        int maxHeightPx = convertDpToPx(250);

        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(maxWidthPx);
        imageView.setMaxHeight(maxHeightPx);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageURI(imageUri);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        imageView.setLayoutParams(params);

        LinearLayout messageWrapper = new LinearLayout(this);
        messageWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        messageWrapper.setOrientation(LinearLayout.HORIZONTAL);
        messageWrapper.setGravity(Gravity.END);

        messageWrapper.addView(imageView);
        chatMessagesContainer.addView(messageWrapper);

        scrollViewMessages.post(() -> scrollViewMessages.fullScroll(View.FOCUS_DOWN));
    }

    private int convertDpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
