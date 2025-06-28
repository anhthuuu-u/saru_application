package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saru.com.adapters.MessageAdapter;
import saru.com.models.Message;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";

    private FirebaseFirestore db;
    private RecyclerView rvMessages;
    private EditText edtMessage;
    private Button btnSend;
    private Toolbar toolbar;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // --- CÁC THAY ĐỔI QUAN TRỌNG ---
    // Biến để lưu thông tin của khách hàng được chọn
    private String customerID;
    private String customerName;

    // ID của người quản trị đang gửi tin nhắn.
    // Bạn có thể lấy ID này từ Firebase Auth nếu admin có đăng nhập.
    private String adminID = "admin_user_01"; // Ví dụ ID của admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Lấy dữ liệu customerID và customerName được truyền từ màn hình trước
        Intent intent = getIntent();
        customerID = intent.getStringExtra("CUSTOMER_ID");
        customerName = intent.getStringExtra("CUSTOMER_NAME");

        // Nếu không có customerID, không thể tiếp tục, đóng Activity
        if (customerID == null || customerID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin khách hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Khởi tạo views
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);
        toolbar = findViewById(R.id.toolbar_message);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        // Hiển thị tên khách hàng trên Toolbar
        getSupportActionBar().setTitle(customerName != null ? customerName : "Trò chuyện");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
        toolbar.setNavigationOnClickListener(v -> finish());

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải tin nhắn của khách hàng này
        loadMessages();

        // Thiết lập sự kiện cho nút gửi
        btnSend.setOnClickListener(v -> {
            String messageText = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(messageText);
                edtMessage.setText("");
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        // Truyền vào adminID để adapter biết tin nhắn nào là "gửi đi" (từ admin)
        messageAdapter = new MessageAdapter(messageList, adminID);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        // Sử dụng customerID động để lấy đúng cuộc trò chuyện
        db.collection("chats")
                .document(customerID) // <--- SỬA Ở ĐÂY
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        messageList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage(String messageText) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderID", adminID); // Người gửi là admin
        messageData.put("content", messageText);
        messageData.put("timestamp", Timestamp.now());
        messageData.put("senderType", "admin"); // Phân loại người gửi
        messageData.put("contentType", "text");

        // Sử dụng customerID động để gửi tin nhắn vào đúng cuộc trò chuyện
        db.collection("chats")
                .document(customerID) // <--- SỬA Ở ĐÂY
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error sending message", e);
                });
    }
}