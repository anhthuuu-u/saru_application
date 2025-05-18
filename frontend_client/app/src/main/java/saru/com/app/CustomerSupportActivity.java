package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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