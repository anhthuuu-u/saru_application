package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class CustomerSupportActivity extends AppCompatActivity {

    private LinearLayout chatBox;
    private ImageView btnChat;
    private String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);
        BASE_URL = getString(R.string.api_base_url);
        loadFaqsFromServer();

        // Handle back to ActivityProfile
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

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
