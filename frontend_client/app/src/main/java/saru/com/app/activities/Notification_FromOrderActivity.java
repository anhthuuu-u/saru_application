package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import saru.com.app.R;
import saru.com.app.connectors.NotificationAdapter;
import saru.com.app.models.Notification;

public class Notification_FromOrderActivity extends AppCompatActivity {
    private static final String TAG = "NotificationOrderActivity";
    private TextView txtNotification_system, txtNotification_order, txtNotification_discount;
    private ImageView imgNotification_Back;
    private ListView lvNotification;
    private CheckBox chkNotification_SelectAll;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<Notification> notificationList;
    private String accountID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_from_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();
        addViews();
        fetchAccountIDAndNotifications();
        addEvents();
    }

    private void addViews() {
        txtNotification_order = findViewById(R.id.txtNotification_order);
        txtNotification_system = findViewById(R.id.txtNotification_system);
        txtNotification_discount = findViewById(R.id.txtNotification_discount);
        imgNotification_Back = findViewById(R.id.imgNotification_Back);
        lvNotification = findViewById(R.id.lvNotification);
        chkNotification_SelectAll = findViewById(R.id.chkNotification_SelectAll);

        TextView emptyView = new TextView(this);
        emptyView.setText("No order notifications available");
        emptyView.setTextSize(16);
        emptyView.setGravity(android.view.Gravity.CENTER);
        lvNotification.setEmptyView(emptyView);

        adapter = new NotificationAdapter(this, R.layout.item_notification);
        adapter.setOnSelectionChangedListener(this::updateSelectAllCheckbox);
        lvNotification.setAdapter(adapter);
    }

    private void fetchAccountIDAndNotifications() {
        Log.d(TAG, "Starting fetchAccountIDAndNotifications");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity");
            Toast.makeText(this, "Please log in to view notifications", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        String userUID = currentUser.getUid();
        Log.d(TAG, "Current user UID: " + userUID);

        // Fetch accountID from accounts collection
        db.collection("accounts").document(userUID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        accountID = documentSnapshot.getString("AccountID");
                        if (accountID == null || accountID.isEmpty()) {
                            Log.e(TAG, "AccountID is null or empty for user: " + userUID);
                            Toast.makeText(this, "Error: Account ID not found", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d(TAG, "Fetched AccountID: " + accountID);
                        fetchNotifications(accountID);
                    } else {
                        Log.e(TAG, "Account document not found for user: " + userUID);
                        Toast.makeText(this, "Error: Account not found. Please contact support.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching account document: ", e);
                    Toast.makeText(this, "Error fetching account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchNotifications(String accountID) {
        Log.d(TAG, "Fetching notifications for AccountID: " + accountID);

        db.collection("notifications")
                .whereEqualTo("notiID", "NT_02")
                .whereEqualTo("accountID", accountID)
                .orderBy("notiTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        adapter.clear();

                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No order notifications found for AccountID: " + accountID);
                            // Don't show toast here, let empty view handle it
                        } else {
                            int validNotificationCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Validate that this notification actually belongs to the current user
                                    String notificationAccountID = document.getString("accountID");
                                    if (notificationAccountID != null && notificationAccountID.equals(accountID)) {
                                        Notification notification = new Notification();
                                        notification.setNotiTitle(document.getString("notiTitle") != null ?
                                                document.getString("notiTitle") : "Order Notification");
                                        notification.setAccountID(notificationAccountID);
                                        notification.setNotiID(document.getString("notiID") != null ?
                                                document.getString("notiID") : "NT_02");
                                        notification.setNoti_content(document.getString("noti_content") != null ?
                                                document.getString("noti_content") : "No content available");
                                        notification.setNotiTime(document.getTimestamp("notiTime"));
                                        notification.setId(document.getId());

                                        notificationList.add(notification);
                                        validNotificationCount++;
                                    } else {
                                        Log.w(TAG, "Skipping notification with mismatched accountID: " +
                                                notificationAccountID + " vs expected: " + accountID);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing notification document: " + document.getId(), e);
                                }
                            }

                            if (validNotificationCount > 0) {
                                adapter.addAll(notificationList);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "Valid order notifications loaded: " + validNotificationCount +
                                        " for AccountID: " + accountID);
                            } else {
                                Log.w(TAG, "No valid order notifications found after filtering for AccountID: " + accountID);
                            }
                        }
                        updateSelectAllCheckbox();
                    } else {
                        Log.e(TAG, "Error fetching order notifications: ", task.getException());
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Error fetching notifications: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteSelectedNotifications() {
        ArrayList<Notification> selectedNotifications = adapter.getSelectedNotifications();
        if (selectedNotifications.isEmpty()) {
            Toast.makeText(this, "No notifications selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Deleting " + selectedNotifications.size() + " selected notifications");

        // Create a copy to avoid concurrent modification
        ArrayList<Notification> notificationsToDelete = new ArrayList<>(selectedNotifications);

        for (Notification notification : notificationsToDelete) {
            // Double-check ownership before deletion
            if (accountID != null && accountID.equals(notification.getAccountID())) {
                db.collection("notifications").document(notification.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            notificationList.remove(notification);
                            adapter.remove(notification);
                            selectedNotifications.remove(notification);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Notification deleted successfully: " + notification.getId());
                            updateSelectAllCheckbox();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting notification: " + notification.getId(), e);
                            Toast.makeText(this, "Error deleting notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.w(TAG, "Attempted to delete notification not owned by current user: " + notification.getId());
                Toast.makeText(this, "Cannot delete notification: Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        // Clear selections after attempting deletion
        adapter.selectAll(false);
    }

    private void updateSelectAllCheckbox() {
        int total = adapter.getCount();
        int selected = adapter.getSelectedNotifications().size();
        chkNotification_SelectAll.setOnCheckedChangeListener(null); // Prevent recursive calls
        chkNotification_SelectAll.setChecked(total > 0 && selected == total);
        chkNotification_SelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });
    }

    private void switchSystemTab() {
        Intent intent = new Intent(this, Notification_FromSettingActivity.class);
        startActivity(intent);
        finish(); // Close current activity to prevent memory leaks
    }

    private void switchDiscountTab() {
        Intent intent = new Intent(this, Notification_FromDiscountActivity.class);
        startActivity(intent);
        finish(); // Close current activity to prevent memory leaks
    }

    private void switchHomepage() {
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
        finish(); // Close current activity to prevent memory leaks
    }

    private void addEvents() {
        txtNotification_system.setOnClickListener(v -> switchSystemTab());
        txtNotification_discount.setOnClickListener(v -> switchDiscountTab());
        imgNotification_Back.setOnClickListener(v -> switchHomepage());
        chkNotification_SelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });

        // Add long click listener for bulk delete
        lvNotification.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteSelectedNotifications();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notifications when returning to this activity
        if (accountID != null) {
            fetchNotifications(accountID);
        } else {
            fetchAccountIDAndNotifications();
        }
    }
}