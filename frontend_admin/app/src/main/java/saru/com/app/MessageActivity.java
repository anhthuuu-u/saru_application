// File: saru/com/app/MessageActivity.java
package saru.com.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import saru.com.adapters.MessageAdapter;
import saru.com.models.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private EditText edtMessage;
    private Button btnSend;
    private Toolbar toolbar;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseFirestore db;
    private ListenerRegistration messageListener;
    private String customerId;
    private String customerName;
    private static final String SENDER_ADMIN = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        db = FirebaseFirestore.getInstance();
        customerId = getIntent().getStringExtra("CUSTOMER_ID");
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");

        if (customerId == null) {
            showToast("Thiếu ID khách hàng");
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadMessages();
        setupSendButton();
    }

    private void initializeViews() {
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        toolbar = findViewById(R.id.toolbar_message);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_message);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(customerName != null ? customerName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, SENDER_ADMIN);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Hiển thị tin nhắn mới nhất ở dưới cùng
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        messageListener = db.collection("messages")
                .whereEqualTo("customerID", customerId) // Lọc tin nhắn theo customerId
                .orderBy("timestamp", Query.Direction.ASCENDING) // Sắp xếp theo thời gian
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        showToast("Lỗi tải tin nhắn: " + error.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    message.setMessageID(dc.getDocument().getId());
                                    messageList.add(message);
                                    break;
                                case REMOVED:
                                    // Xử lý xóa tin nhắn nếu cần
                                    break;
                                case MODIFIED:
                                    // Xử lý sửa tin nhắn nếu cần
                                    break;
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1); // Cuộn mượt mà đến tin nhắn mới
                        }
                    }
                });
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (content.isEmpty()) {
                showToast("Vui lòng nhập nội dung tin nhắn");
                return;
            }
            sendMessage(content);
        });
    }

    private void sendMessage(String content) {
        Message message = new Message();
        message.setCustomerID(customerId);
        message.setMessageContent(content);
        message.setTimestamp(Timestamp.now());
        message.setSender(SENDER_ADMIN);

        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    edtMessage.setText(""); // Xóa nội dung sau khi gửi
                    // Không cần cuộn vì SnapshotListener sẽ tự động cập nhật
                })
                .addOnFailureListener(e -> showToast("Lỗi gửi tin nhắn: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove(); // Ngừng lắng nghe để tránh rò rỉ bộ nhớ
        }
    }
}