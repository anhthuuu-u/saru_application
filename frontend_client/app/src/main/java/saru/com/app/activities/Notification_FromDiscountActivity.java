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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import saru.com.app.R;
import saru.com.app.connectors.NotificationAdapter;
import saru.com.app.models.Notification;

public class Notification_FromDiscountActivity extends AppCompatActivity {
    TextView txtNotification_system;
    TextView txtNotification_order;
    TextView txtNotification_discount;
    ImageView imgNotification_Back;
    ListView lvNotification;
    CheckBox chkNotification_SelectAll;
    NotificationAdapter adapter;
    FirebaseFirestore db;
    ArrayList<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_from_discount);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();
        addViews();
        fetchNotifications();
        addEvents();
    }

    private void addViews() {
        txtNotification_order = findViewById(R.id.txtNotification_order);
        txtNotification_discount = findViewById(R.id.txtNotification_discount);
        txtNotification_system = findViewById(R.id.txtNotification_system);
        imgNotification_Back = findViewById(R.id.imgNotification_Back);
        lvNotification = findViewById(R.id.lvNotification);
        chkNotification_SelectAll = findViewById(R.id.chkNotification_SelectAll);
        TextView emptyView = new TextView(this);
        emptyView.setText("No notifications available");
        emptyView.setTextSize(16);
        emptyView.setGravity(android.view.Gravity.CENTER);
        lvNotification.setEmptyView(emptyView);
        adapter = new NotificationAdapter(this, R.layout.item_notification);
        adapter.setOnSelectionChangedListener(this::updateSelectAllCheckbox);
        lvNotification.setAdapter(adapter);
    }

    private void fetchNotifications() {
        Log.d("Firestore", "Starting fetchNotifications");
        db.collection("notifications")
                .whereEqualTo("notiID", "NT_03")
                .orderBy("notiTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        adapter.clear();
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No discount notifications available", Toast.LENGTH_SHORT).show();
                            Log.d("Firestore", "No documents found");
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = new Notification();
                                notification.setNotiTitle(document.getString("notiTitle") != null ? document.getString("notiTitle") : "");
                                notification.setAccountID(document.getString("accountID") != null ? document.getString("accountID") : "");
                                notification.setNotiID(document.getString("notiID") != null ? document.getString("notiID") : "");
                                notification.setNoti_content(document.getString("noti_content") != null ? document.getString("noti_content") : "No content");
                                notification.setNotiTime(document.getTimestamp("notiTime"));
                                notification.setId(document.getId());
                                notificationList.add(notification);
                            }
                            adapter.addAll(notificationList);
                            adapter.notifyDataSetChanged();
                            Log.d("Firestore", "Notifications loaded: " + notificationList.size());
                            Log.d("Adapter", "Adapter item count: " + adapter.getCount());
                            Log.d("ListView", "ListView item count: " + lvNotification.getCount());
                        }
                        updateSelectAllCheckbox();
                    } else {
                        Toast.makeText(this, "Error fetching notifications: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error: ", task.getException());
                    }
                });
    }

    private void deleteNotifications() {
        ArrayList<Notification> selectedNotifications = adapter.getSelectedNotifications();
        if (selectedNotifications.isEmpty()) {
            Toast.makeText(this, "No notifications selected", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Notification notification : selectedNotifications) {
            db.collection("notifications").document(notification.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        notificationList.remove(notification);
                        adapter.remove(notification);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                        updateSelectAllCheckbox();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        adapter.selectAll(false); // Clear selections after deletion
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

    void switchSystemTab() {
        Intent intent = new Intent(this, Notification_FromSettingActivity.class);
        startActivity(intent);
    }

    void switchOrderTab() {
        Intent intent = new Intent(this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }

    void switchProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void addEvents() {
        txtNotification_system.setOnClickListener(v -> switchSystemTab());
        txtNotification_order.setOnClickListener(v -> switchOrderTab());
        imgNotification_Back.setOnClickListener(v -> switchProfileActivity());
        chkNotification_SelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });
    }
}