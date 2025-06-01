package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends BaseActivity {
    ImageView img_aboutus;
    ImageView nexttoaboutus,imgforAboutSaru,img_backtoaboutSaru,img_directNotifipage,img_directtoNotification;
    TextView aboutus_page,txt_backtoaboutSaru,txt_directtonotificationpage;
    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_account;
    }

    private boolean isSettingsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        addView();
        addEvents();

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();

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
            // TODO: Handle notification toggle
        });

        // Language Setting click
        TextView tvLanguageSetting = findViewById(R.id.tv_language_setting);
        tvLanguageSetting.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LanguageSettingActivity.class);
            startActivity(intent);
        });
    }
    private void addView() {
        img_aboutus=findViewById(R.id.nexttoaboutus);
        nexttoaboutus=findViewById(R.id.nexttoaboutus);
        aboutus_page=findViewById(R.id.aboutus_page);

        imgforAboutSaru=findViewById(R.id.imgforAboutSaru);
        img_backtoaboutSaru=findViewById(R.id.img_backtoaboutSaru);
        txt_backtoaboutSaru=findViewById(R.id.txt_backtoaboutSaru);

        txt_directtonotificationpage=findViewById(R.id.txt_directtonotificationpage);
        img_directNotifipage=findViewById(R.id.img_directNotifipage);
        img_directtoNotification=findViewById(R.id.img_directtoNotification);
    }
    private void addEvents() {

        //chuyển hướng qua about us
        img_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_SaruWine();
            }
        });
        nexttoaboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_SaruWine();
            }
        });
        aboutus_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_SaruWine();
            }
        });

        //chuyển qua trang about Saru
        imgforAboutSaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_StoreLocation();
            }
        });
        img_backtoaboutSaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_StoreLocation();
            }
        });
        txt_backtoaboutSaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAboutUs_StoreLocation();
            }
        });

        //chuyển hướng qua trang notification

        txt_directtonotificationpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotification_Page();
            }
        });
        img_directNotifipage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotification_Page();
            }
        });
        img_directtoNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotification_Page();
            }
        });
    }

    void openAboutUs_SaruWine()
    {
        Intent intent=new Intent(ProfileActivity.this, Aboutus_SARUActivity.class);
        startActivity(intent);
    }
    void openAboutUs_StoreLocation()
    {
        Intent intent=new Intent(ProfileActivity.this, Aboutus_locationActivity.class);
        startActivity(intent);
    }

    void openNotification_Page()
    {
        Intent intent=new Intent(ProfileActivity.this, Notification_FromSettingActivity.class);
        startActivity(intent);
    }
}