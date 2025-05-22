package saru.com.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerSupportActivity extends AppCompatActivity {

    private LinearLayout chatBox;
    private ImageView btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        // Handle back to ActivityProfile
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup FAQs
        setupFaqItem(
                R.id.faq_question_container_1,
                R.id.faq_answer_1,
                R.id.faq_toggle_icon_1,
                R.id.faq_divider_1
        );

        // Chat button toggle
        btnChat = findViewById(R.id.btn_open_chat);
        chatBox = findViewById(R.id.chat_box);

        btnChat.setOnClickListener(v -> {
            if (chatBox.getVisibility() == View.VISIBLE) {
                chatBox.setVisibility(View.GONE);
            } else {
                chatBox.setVisibility(View.VISIBLE);
            }
        });

        // Setup social media links
        setupSocialMediaLinks();
    }

    private void setupSocialMediaLinks() {
        // Facebook
        ImageView facebookIcon = findViewById(R.id.ic_fb);
        facebookIcon.setOnClickListener(v -> {
            String facebookUrl = "https://www.facebook.com/landoflala.store"; // Thay bằng URL trang Facebook của bạn
            openSocialMedia("com.facebook.katana", facebookUrl, "fb://page/saruapp"); // Thay "saruapp" bằng ID trang
        });

        // Instagram
        ImageView instagramIcon = findViewById(R.id.ic_IG);
        instagramIcon.setOnClickListener(v -> {
            String instagramUsername = "saruapp"; // Thay bằng tên người dùng Instagram của bạn
            openSocialMedia("com.instagram.android", "https://www.instagram.com/minh.la_lala/" + instagramUsername,
                    "instagram://user?username=" + instagramUsername);
        });

        // Gmail
        ImageView gmailIcon = findViewById(R.id.ic_gmail);
        gmailIcon.setOnClickListener(v -> {
            String email = "support@saruapp.com"; // Thay bằng email của bạn
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
            String telegramUrl = "https://web.telegram.org/a/"; // Thay bằng link Telegram của bạn
            openSocialMedia("org.telegram.messenger", telegramUrl, telegramUrl);
        });
    }

    private void openSocialMedia(String packageName, String fallbackUrl, String deepLink) {
        try {
            // Thử mở ứng dụng bằng deep link
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.setPackage(packageName);
            startActivity(intent);
        } catch (Exception e) {
            // Nếu không có ứng dụng, mở trình duyệt
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupFaqItem(int containerId, int answerId, int iconId, int dividerId) {
        LinearLayout container = findViewById(containerId);
        TextView answer = findViewById(answerId);
        ImageView icon = findViewById(iconId);
        View divider = findViewById(dividerId);

        container.setOnClickListener(v -> {
            boolean isVisible = answer.getVisibility() == View.VISIBLE;
            answer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            divider.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            icon.setImageResource(isVisible ? R.mipmap.ic_down_arrow : R.mipmap.ic_up_arrow);
        });


    }
}
