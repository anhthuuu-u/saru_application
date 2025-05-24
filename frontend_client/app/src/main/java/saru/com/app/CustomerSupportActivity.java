package saru.com.app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerSupportActivity extends AppCompatActivity {

    ImageView img_open_chat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);
        addView();
        addEvents();

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

}
