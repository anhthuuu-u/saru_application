package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private boolean isSettingsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Edit Profile
        ImageView editProfileIcon = findViewById(R.id.img_profile);
        editProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });

        // View All Orders
        TextView viewAllOrders = findViewById(R.id.tv_view_all_orders);
        viewAllOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderListActivity.class);
            startActivity(intent);
        });

        // Customer Support Center
        TextView customerSupportCenter = findViewById(R.id.tv_customer_support_center);
        customerSupportCenter.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CustomerSupportActivity.class);
            startActivity(intent);
        });

        // Expand Settings Section
        LinearLayout settingsSection = findViewById(R.id.settings_section);
        LinearLayout expandableSettings = findViewById(R.id.settings_expandable_section);

        settingsSection.setOnClickListener(v -> {
            isSettingsExpanded = !isSettingsExpanded;
            expandableSettings.setVisibility(isSettingsExpanded ? View.VISIBLE : View.GONE);
        });

        Switch notificationSwitch = findViewById(R.id.switch_notifications);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        });

        // Language Setting click
        TextView tvLanguageSetting = findViewById(R.id.tv_language_setting);
        tvLanguageSetting.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LanguageSettingActivity.class);
            startActivity(intent);
        });
    }
}