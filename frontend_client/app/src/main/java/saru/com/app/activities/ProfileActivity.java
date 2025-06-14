package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import saru.com.app.R;

public class ProfileActivity extends BaseActivity {
    ImageView img_aboutus;
    ImageView nexttoaboutus,imgforAboutSaru,img_backtoaboutSaru,img_directNotifipage,img_directtoNotification;
    TextView aboutus_page,txt_backtoaboutSaru,txt_directtonotificationpage;


    ImageView imgCustomerAva;
    TextView txtCustomerName, txtCustomerEmail;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

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

        // Get the current logged-in user
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d("UserUID", "User UID: " + userUID);  // Log the UID to check if it's null

        if (userUID != null) {
            // Fetch user data from Firestore based on UID
            fetchUserProfile(userUID);
        } else {
            // Log and display a message if the user is not logged in
            Log.d(TAG, "User not logged in.");
            Toast.makeText(ProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

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

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void fetchUserProfile(String userUID) {
        // Reference to the 'accounts' collection
        DocumentReference accountRef = db.collection("accounts").document(userUID);

        accountRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get user data from Firestore
                String email = task.getResult().getString("CustomerEmail");

                // Display email in the profile
                txtCustomerEmail.setText(email);

                // Fetch CustomerID from the accounts collection
                String customerID = task.getResult().getString("CustomerID");
                if (customerID != null) {
                    Log.d(TAG, "Fetched CustomerID: " + customerID);
                    // Fetch customer data using CustomerID
                    fetchCustomerData(customerID);
                } else {
                    Log.e(TAG, "CustomerID is null");
                    Toast.makeText(ProfileActivity.this, "CustomerID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCustomerData(String customerID) {
        // Log the customer data fetch operation
        Log.d(TAG, "Fetching customer data for CustomerID: " + customerID);

        // Reference to the 'customers' collection
        DocumentReference customerRef = db.collection("customers").document(customerID);

        customerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get customer data from Firestore
                String name = task.getResult().getString("CustomerName");
                String avatarUrl = task.getResult().getString("CustomerAvatar");

                // Log customer name and avatar URL to verify they're fetched correctly
                Log.d(TAG, "Fetched Customer Name: " + name);
                Log.d(TAG, "Fetched Avatar URL: " + avatarUrl);

                if (name != null && avatarUrl != null) {
                    // Set the customer name
                    txtCustomerName.setText(name);

                    // Load the customer's avatar image using Glide
                    Glide.with(ProfileActivity.this)
                            .load(avatarUrl)
                            .placeholder(R.mipmap.img_circle)
                            .into(imgCustomerAva);
                } else {
                    Log.e(TAG, "Customer Name or Avatar URL is missing in Firestore.");
                    Toast.makeText(ProfileActivity.this, "Missing customer data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load customer data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addView() {
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        imgCustomerAva = findViewById(R.id.imgCustomerAva);
        txtCustomerName = findViewById(R.id.edtCustomerName);
        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);

        img_aboutus = findViewById(R.id.nexttoaboutus);
        nexttoaboutus = findViewById(R.id.nexttoaboutus);
        aboutus_page = findViewById(R.id.aboutus_page);

        imgforAboutSaru = findViewById(R.id.imgforAboutSaru);
        img_backtoaboutSaru = findViewById(R.id.img_backtoaboutSaru);
        txt_backtoaboutSaru = findViewById(R.id.txt_backtoaboutSaru);

        txt_directtonotificationpage = findViewById(R.id.txt_directtonotificationpage);
        img_directNotifipage = findViewById(R.id.img_directNotifipage);
        img_directtoNotification = findViewById(R.id.img_directtoNotification);
    }

    private void addEvents() {
        //chuyển hướng qua about us
        img_aboutus.setOnClickListener(v -> openAboutUs_SaruWine());
        nexttoaboutus.setOnClickListener(v -> openAboutUs_SaruWine());
        aboutus_page.setOnClickListener(v -> openAboutUs_SaruWine());

        //chuyển qua trang about Saru
        imgforAboutSaru.setOnClickListener(v -> openAboutUs_StoreLocation());
        img_backtoaboutSaru.setOnClickListener(v -> openAboutUs_StoreLocation());
        txt_backtoaboutSaru.setOnClickListener(v -> openAboutUs_StoreLocation());

        //chuyển hướng qua trang notification
        txt_directtonotificationpage.setOnClickListener(v -> openNotification_Page());
        img_directNotifipage.setOnClickListener(v -> openNotification_Page());
        img_directtoNotification.setOnClickListener(v -> openNotification_Page());
    }

    void openAboutUs_SaruWine() {
        Intent intent = new Intent(ProfileActivity.this, Aboutus_SARUActivity.class);
        startActivity(intent);
    }

    void openAboutUs_StoreLocation() {
        Intent intent = new Intent(ProfileActivity.this, Aboutus_locationActivity.class);
        startActivity(intent);
    }

    void openNotification_Page() {
        Intent intent = new Intent(ProfileActivity.this, Notification_FromSettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login screen if the user is not authenticated
            Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();  // Ensure ProfileActivity is not kept in the stack
        }
    }
}
