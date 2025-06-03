package saru.com.app.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.view.LayoutInflater;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import androidx.appcompat.app.AppCompatActivity;

import saru.com.app.R;

public class CustomerSupportActivity extends AppCompatActivity {


    ImageView img_open_chat;
    private String BASE_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);
        addView();
        addEvents();
        BASE_URL = getString(R.string.api_base_url);
        loadFaqsFromServer();

        // Handle back to ActivityProfile
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void addView() {
        img_open_chat=findViewById(R.id.img_open_chat);
    }


    private void addEvents() {
        img_open_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openLiveChat();
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



    void openLiveChat()
    {
        Intent intent=new Intent(CustomerSupportActivity.this, LiveChatActivity.class);
        startActivity(intent);
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

    private void loadFaqsFromServer() {
        String url = BASE_URL + "/faqs";
        RequestQueue queue = Volley.newRequestQueue(this);


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                this::showFaqs,
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, getString(R.string.faq_load_error), Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    private void showFaqs(JSONArray faqs) {
        LinearLayout faqContainer = findViewById(R.id.faq_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        faqContainer.removeAllViews();

        for (int i = 0; i < faqs.length(); i++) {
            try {
                JSONObject item = faqs.getJSONObject(i);
                String title = item.getString("FaqTitle");
                String content = item.getString("FaqContent");

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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
