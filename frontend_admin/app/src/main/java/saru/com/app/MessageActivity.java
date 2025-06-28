package saru.com.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // IMPORTANT: This should be the ID of the currently logged-in user.
    // Replace this placeholder with your actual user ID logic, e.g., from Firebase Auth.
    private String currentUserID = "iis6IltEW3T92eheyKb066XCx182"; // Example User ID from your screenshot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        db = FirebaseFirestore.getInstance();

        // Initialize views with correct IDs from XML
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserID);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // New messages appear at the bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        // Load messages from Firestore
        loadMessages();

        btnSend.setOnClickListener(v -> {
            String messageText = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(messageText);
                edtMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        db.collection("chats")
                .document(currentUserID)
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
                        rvMessages.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
                    }
                });
    }

    private void sendMessage(String messageText) {
        Map<String, Object> messageData = new HashMap<>();
        // These fields must match your Message.java model and Firestore document structure
        messageData.put("senderID", currentUserID);
        messageData.put("content", messageText);
        messageData.put("timestamp", Timestamp.now());
        // Optional fields from your screenshot, you can add them if needed
        messageData.put("senderType", "customer"); // or "admin" depending on who is sending
        messageData.put("contentType", "text");


        db.collection("chats")
                .document(currentUserID)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error sending message", e);
                });
    }
}