package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import saru.com.app.R;

public class Notification_FromOrderActivity extends AppCompatActivity {
    TextView txtNotification_system;
    TextView txtNotification_order;
    TextView txtNotification_discount;
    ImageView imgNotification_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_from_order);
        addView();
        addEvents();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void addView()
    {
        txtNotification_order=findViewById(R.id.txtNotification_order);
        txtNotification_discount=findViewById(R.id.txtNotification_discount);
        txtNotification_system=findViewById(R.id.txtNotification_system);
        imgNotification_Back=findViewById(R.id.imgNotification_Back);


    }

    void switchSystemTab()
    {
        Intent intent=new Intent(Notification_FromOrderActivity.this, Notification_FromSettingActivity.class);
        startActivity(intent);
    }
    void switchDiscountTab()
    {
        Intent intent=new Intent(Notification_FromOrderActivity.this, Notification_FromDiscountActivity.class);
        startActivity(intent);
    }
    void switchProfileActivity()
    {
        Intent intent=new Intent(Notification_FromOrderActivity.this, ProfileActivity.class);
        startActivity(intent);
    }


    private void addEvents()
    {
        txtNotification_system.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSystemTab();
            }
        });
        txtNotification_discount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDiscountTab();
            }
        });
        imgNotification_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchProfileActivity();
            }
        });
    }
}