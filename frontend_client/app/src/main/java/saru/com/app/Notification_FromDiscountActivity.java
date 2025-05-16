package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Notification_FromDiscountActivity extends AppCompatActivity {
    TextView txtNotification_system;
    TextView txtNotification_order;
    TextView txtNotification_discount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_from_discount);
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
    }

    void switchSystemTab()
    {
        Intent intent=new Intent(Notification_FromDiscountActivity.this, Notification_FromSettingActivity.class);
        startActivity(intent);
    }
    void switchOrderTab()
    {
        Intent intent=new Intent(Notification_FromDiscountActivity.this, Notification_FromOrderActivity.class);
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
        txtNotification_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchOrderTab();
            }
        });
}
}