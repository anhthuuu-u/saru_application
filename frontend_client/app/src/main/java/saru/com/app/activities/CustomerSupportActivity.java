package saru.com.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private ImageView img_open_chat; // Khai báo img_open_chat như field
    private EditText askQuestionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kiểm tra trạng thái đăng nhập
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
        askQuestionInput = findViewById(R.id.ask_question_input); // Gán EditText
    }

    private void addEvents() {
        img_open_chat.setOnClickListener(v -> openLiveChat());

        // Thêm sự kiện cho nút gửi câu hỏi
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
                Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Lỗi khi tải FAQs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Thêm phương thức để gửi câu hỏi
    private void sendQuestion() {
        String questionText = askQuestionInput.getText().toString().trim();
        if (questionText.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_question), Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> question = new HashMap<>();
            question.put("userId", currentUser.getUid());
            question.put("question", questionText);
            question.put("timestamp", System.currentTimeMillis());

            db.collection(getString(R.string.firestore_questions_collection))
                    .add(question)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, getString(R.string.success_question_sent), Toast.LENGTH_SHORT).show();
                        askQuestionInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.error_send_question, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}