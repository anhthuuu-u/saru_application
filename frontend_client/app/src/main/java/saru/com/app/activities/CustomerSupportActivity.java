package saru.com.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;

public class CustomerSupportActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView img_open_chat;
    private EditText askQuestionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check login status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(CustomerSupportActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_customer_support);
        addView();
        addEvents();
        loadFaqsFromFirestore();
    }

    private void addView() {
        img_open_chat = findViewById(R.id.img_open_chat);
        askQuestionInput = findViewById(R.id.ask_question_input);
    }

    private void addEvents() {
        img_open_chat.setOnClickListener(v -> openLiveChat());

        // Add event for sending question
        findViewById(R.id.btn_send_question).setOnClickListener(v -> sendQuestion());

        setupSocialMediaLinks();
    }

    private void setupSocialMediaLinks() {
        // Facebook
        ImageView facebookIcon = findViewById(R.id.ic_fb);
        facebookIcon.setOnClickListener(v -> {
            String facebookUrl = "https://www.facebook.com/landoflala.store";
            openSocialMedia("com.facebook.katana", facebookUrl, "fb://page/saruapp");
        });

        // Instagram
        ImageView instagramIcon = findViewById(R.id.ic_IG);
        instagramIcon.setOnClickListener(v -> {
            String instagramUsername = "saruapp";
            openSocialMedia("com.instagram.android", "https://www.instagram.com/minh.la_lala/" + instagramUsername,
                    "instagram://user?username=" + instagramUsername);
        });

        // Gmail
        ImageView gmailIcon = findViewById(R.id.ic_gmail);
        gmailIcon.setOnClickListener(v -> {
            String email = "support@saruapp.com";
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                showCustomToast("Không tìm thấy ứng dụng email");
            }
        });

        // Telegram
        ImageView telegramIcon = findViewById(R.id.ic_telegram);
        telegramIcon.setOnClickListener(v -> {
            String telegramUrl = "https://web.telegram.org/a/";
            openSocialMedia("org.telegram.messenger", telegramUrl, telegramUrl);
        });
    }

    private void openSocialMedia(String packageName, String fallbackUrl, String deepLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.setPackage(packageName);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                startActivity(browserIntent);
            } catch (Exception ex) {
                showCustomToast("Không thể mở liên kết");
            }
        }
    }

    void openLiveChat() {
        Intent intent = new Intent(CustomerSupportActivity.this, LiveChatActivity.class);
        startActivity(intent);
    }

    private void loadFaqsFromFirestore() {
        LinearLayout faqContainer = findViewById(R.id.faq_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        faqContainer.removeAllViews();

        db.collection("faqs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String content = document.getString("content");

                        View faqView = inflater.inflate(R.layout.item_faq, faqContainer, false);
                        TextView question = faqView.findViewById(R.id.faq_question);
                        TextView answer = faqView.findViewById(R.id.faq_answer);
                        ImageView toggleIcon = faqView.findViewById(R.id.faq_toggle_icon);
                        LinearLayout questionContainer = faqView.findViewById(R.id.faq_question_container);

                        question.setText(title);
                        answer.setText(content);

                        questionContainer.setOnClickListener(v -> {
                            boolean visible = answer.getVisibility() == View.VISIBLE;
                            answer.setVisibility(visible ? View.GONE : View.VISIBLE);
                            toggleIcon.setImageResource(visible ? R.mipmap.ic_down_arrow : R.mipmap.ic_up_arrow);
                        });

                        faqContainer.addView(faqView);
                    }
                })
                .addOnFailureListener(e -> {
                    showCustomToast("Lỗi khi tải FAQs: " + e.getMessage());
                });
    }

    private void sendQuestion() {
        String questionText = askQuestionInput.getText().toString().trim();
        if (questionText.isEmpty()) {
            showCustomToast(getString(R.string.error_empty_question));
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            db.collection("accounts")
                    .whereEqualTo("CustomerEmail", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            String customerID = document.getString("CustomerID");
                            if (customerID != null && !customerID.isEmpty()) {
                                Map<String, Object> question = new HashMap<>();
                                question.put("customerID", customerID);
                                question.put("sector", "CustomerSupport");
                                question.put("question", questionText);
                                question.put("timestamp", System.currentTimeMillis());

                                db.collection(getString(R.string.firestore_questions_collection))
                                        .add(question)
                                        .addOnSuccessListener(documentReference -> {
                                            showCustomToast(getString(R.string.success_question_sent));
                                            askQuestionInput.setText("");
                                        })
                                        .addOnFailureListener(e -> {
                                            showCustomToast(getString(R.string.error_send_question, e.getMessage()));
                                        });
                            } else {
                                showCustomToast("Không tìm thấy CustomerID");
                            }
                        } else {
                            showCustomToast("Không tìm thấy thông tin tài khoản");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showCustomToast("Lỗi khi lấy CustomerID: " + e.getMessage());
                    });
        }
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
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}