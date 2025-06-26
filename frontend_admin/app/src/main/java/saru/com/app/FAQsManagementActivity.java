package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.FAQ;

public class FAQsManagementActivity extends AppCompatActivity {
    private RecyclerView rvFAQs;
    private Button btnAddFAQ;
    private FirebaseFirestore db;
    private List<FAQ> faqs;
    private FAQAdapter adapter;
    private static final int REQUEST_CODE_ADD_FAQ = 1100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        loadFAQs();
        setupEvents();
    }

    private void initializeViews() {
        rvFAQs = findViewById(R.id.rvFaqs);
        btnAddFAQ = findViewById(R.id.btnAddFaq);
    }

    private void setupRecyclerView() {
        faqs = new ArrayList<>();
        adapter = new FAQAdapter(faqs);
        rvFAQs.setLayoutManager(new LinearLayoutManager(this));
        rvFAQs.setAdapter(adapter);
    }

    private void loadFAQs() {
        db.collection("faqs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    faqs.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FAQ faq = document.toObject(FAQ.class);
                        faqs.add(faq);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load FAQs: " + e.getMessage()));
    }

    private void setupEvents() {
        btnAddFAQ.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditFAQActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_FAQ);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_FAQ && resultCode == RESULT_OK) {
            loadFAQs();
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder> {
        private List<FAQ> faqList;

        FAQAdapter(List<FAQ> faqList) {
            this.faqList = faqList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FAQ faq = faqList.get(position);
            holder.txtTitle.setText(faq.getTitle());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(FAQsManagementActivity.this, AddEditFAQActivity.class);
                intent.putExtra("SELECTED_FAQ", faq);
                startActivityForResult(intent, REQUEST_CODE_ADD_FAQ);
            });
        }

        @Override
        public int getItemCount() {
            return faqList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;

            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtFaqTitle);
            }
        }
    }
}