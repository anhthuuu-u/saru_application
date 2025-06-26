package saru.com.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;
import saru.com.models.FAQ;

public class AddEditFAQActivity extends AppCompatActivity {
    private EditText edtTitle, edtContent;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private FAQ faq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_faq);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        displayFAQ();
        setupEvents();
    }

    private void initializeViews() {
        edtTitle = findViewById(R.id.edtFaqTitle);
        edtContent = findViewById(R.id.edtFaqContent);
        btnSave = findViewById(R.id.btnSaveFaq);
        btnCancel = findViewById(R.id.btnCancelFaq);
    }

    private void displayFAQ() {
        faq = (FAQ) getIntent().getSerializableExtra("SELECTED_FAQ");
        if (faq != null) {
            edtTitle.setText(faq.getTitle());
            edtContent.setText(faq.getContent());
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveFAQ());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveFAQ() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (faq == null) {
            faq = new FAQ(UUID.randomUUID().toString(), content, title);
        } else {
            faq.setTitle(title);
            faq.setContent(content);
        }

        db.collection("faqs").document(faq.getFaqID()).set(faq)
                .addOnSuccessListener(aVoid -> {
                    showToast("FAQ saved");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save FAQ: " + e.getMessage()));
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}