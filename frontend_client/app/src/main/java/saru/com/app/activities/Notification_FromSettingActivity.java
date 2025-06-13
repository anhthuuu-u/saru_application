package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class Notification_FromSettingActivity extends AppCompatActivity {
    ListView lvNotification;
    NotificationAdapter adapter;
    TextView txtNotification_system;
    TextView txtNotification_order;
    TextView txtNotification_discount;
    ImageView imgNotification_Back;
    FirebaseFirestore db;
    ArrayList<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_from_setting);
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
        imgNotification_Back = findViewById(R.id.imgNotification_Back);
        txtNotification_system = findViewById(R.id.txtNotification_system);
        lvNotification = findViewById(R.id.lvNotification);
        adapter = new NotificationAdapter(this, R.layout.item_notification);
        lvNotification.setAdapter(adapter);
    }

    private void fetchNotifications() {
        Log.d("Firestore", "Starting fetchNotifications");
        db.collection("notifications")
                .whereEqualTo("notiID", "NT_01")
                .orderBy("notiTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        adapter.clear();
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No system notifications available", Toast.LENGTH_SHORT).show();
                            Log.d("Firestore", "No documents found");
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = new Notification();
                                notification.setNotiTitle(document.getString("notiTitle") != null ? document.getString("notiTitle") : "");
                                notification.setAccountID(document.getString("accountID") != null ? document.getString("accountID") : "");
                                notification.setNotiID(document.getString("notiID") != null ? document.getString("notiID") : "");
                                notification.setNoti_content(document.getString("noti_content") != null ? document.getString("noti_content") : "No content");
                                notification.setNotiTime(document.getTimestamp("notiTime")); // Sử dụng Timestamp trực tiếp
                                notification.setId(document.getId());
                                notificationList.add(notification);
                            }
                            adapter.addAll(notificationList);
                            adapter.notifyDataSetChanged();
                            Log.d("Firestore", "Notifications loaded: " + notificationList.size());
                            Log.d("Adapter", "Adapter item count: " + adapter.getCount());
                            Log.d("ListView", "ListView item count: " + lvNotification.getCount());
                        }
                    } else {
                        Toast.makeText(this, "Error fetching notifications: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error: ", task.getException());
                    }
                });
    }

    void switchOrderTab() {
        Intent intent = new Intent(Notification_FromSettingActivity.this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }

    void switchDiscountTab() {
        Intent intent = new Intent(Notification_FromSettingActivity.this, Notification_FromDiscountActivity.class);
        startActivity(intent);
    }

    void switchProfileActivity() {
        Intent intent = new Intent(Notification_FromSettingActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void addEvents() {
        txtNotification_order.setOnClickListener(v -> switchOrderTab());
        txtNotification_discount.setOnClickListener(v -> switchDiscountTab());
        imgNotification_Back.setOnClickListener(v -> switchProfileActivity());
    }
}