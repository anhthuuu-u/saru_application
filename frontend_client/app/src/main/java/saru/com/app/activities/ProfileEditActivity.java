package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import saru.com.app.R;

public class ProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements (EditText and TextView)
    ImageView imgCustomerAva;
    EditText edtCustomerName, edtCustomerPhone, edtCustomerBirth, edtCustomerMail, edtCustomerPass, edtCustomerJoinDate;
    TextView txtCustomerName, txtCustomerEmail;
    RadioButton ckbMale, ckbFemale;
    TextView txtCustomerFullName, txtCustomerFullAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        try {
            initializeFirebase();
            initializeViews();

            // Ensure mAuth is properly initialized
            if (mAuth == null) {
                mAuth = FirebaseAuth.getInstance();
            }

            // Get the current logged-in user UID
            String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            Log.d("ProfileEditActivity", "User UID: " + userUID);  // Log the UID to check if it's null

            if (userUID != null) {
                // Fetch user data from Firestore based on UID
                fetchUserProfile(userUID);
            } else {
                // Log and display a message if the user is not logged in
                Log.d("ProfileEditActivity", "User not logged in.");
                Toast.makeText(ProfileEditActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();

                // Redirect to LoginActivity
                Intent loginIntent = new Intent(ProfileEditActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();  // Ensure ProfileEditActivity is not kept in the stack
            }

            // Back navigation
            ImageView backIcon = findViewById(R.id.ic_back_arrow);
            backIcon.setOnClickListener(v -> {
                // Navigate back to ProfileActivity
                finish();
            });

            // Reload button to update profile
            ImageView reloadBtn = findViewById(R.id.btn_reload);
            reloadBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileEditActivity.this, ProfileUpdateCardActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error in onCreate: ", e);
            Toast.makeText(ProfileEditActivity.this, "An error occurred, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeFirebase() {
        Log.d("ProfileEditActivity", "Initializing Firebase...");
        try {
            if (mAuth == null) {
                mAuth = FirebaseAuth.getInstance();
            }
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error initializing Firebase: ", e);
        }
    }

    private void initializeViews() {
        imgCustomerAva = findViewById(R.id.imgCustomerAva);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtCustomerPhone = findViewById(R.id.edtCustomerPhone);
        edtCustomerBirth = findViewById(R.id.edtCustomerBirth);
        edtCustomerMail = findViewById(R.id.edtCustomerMail);
        edtCustomerPass = findViewById(R.id.edtCustomerPass);
        edtCustomerJoinDate = findViewById(R.id.edtCustomerJoinDate);
        ckbMale = findViewById(R.id.ckbMale);
        ckbFemale = findViewById(R.id.ckbFemale);
        txtCustomerFullName = findViewById(R.id.txtCustomerFullName);
        txtCustomerFullAddress = findViewById(R.id.txtCustomerFullAddress);


        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
    }

    private void fetchUserProfile(String userUID) {
        Log.d("ProfileEditActivity", "Fetching user profile for UID: " + userUID);

        try {
            // Reference to the 'accounts' collection
            DocumentReference accountRef = db.collection("accounts").document(userUID);

            accountRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String email = task.getResult().getString("CustomerEmail");
                    String password = task.getResult().getString("CustomerPassword");

                    // Set Customer Email and Password to both TextView and EditText
                    txtCustomerEmail.setText(email);
                    edtCustomerMail.setText(email);

                    edtCustomerPass.setText(password);

                    String customerID = task.getResult().getString("CustomerID");
                    if (customerID != null) {
                        Log.d("ProfileEditActivity", "Fetched CustomerID: " + customerID);
                        fetchCustomerData(customerID); // Proceed to fetch customer data
                    } else {
                        Log.e("ProfileEditActivity", "CustomerID is missing.");
                        Toast.makeText(ProfileEditActivity.this, "CustomerID is missing", Toast.LENGTH_SHORT).show();
                    }

                    // Fetch the creation date from Firebase Authentication
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
                        Date date = new Date(creationTimestamp);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String createdDate = sdf.format(date);
                        edtCustomerJoinDate.setText(createdDate); // Set the join date
                    }
                } else {
                    Log.e("ProfileEditActivity", "Error fetching user data: " + task.getException());
                    Toast.makeText(ProfileEditActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("ProfileEditActivity", "Error fetching user profile: ", e);
        }
    }


        private void fetchCustomerData(String customerID){
            Log.d("ProfileEditActivity", "Fetching customer data for CustomerID: " + customerID);

            try {
                // Reference to the 'customers' collection
                DocumentReference customerRef = db.collection("customers").document(customerID);

                customerRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String name = task.getResult().getString("CustomerName");
                        Object phoneObject = task.getResult().get("CustomerPhone"); // Get CustomerPhone as Object
                        String phone = (phoneObject != null) ? phoneObject.toString() : null;  // Convert to String

                        String birth = task.getResult().getString("CustomerBirth");
                        String avatarUrl = task.getResult().getString("CustomerAvatar");
                        String sex = task.getResult().getString("Sex"); // Get Gender (Male/Female)
                        String address = task.getResult().getString("CustomerAdd");

                        Log.d("ProfileEditActivity", "Fetched Customer Name: " + name);
                        Log.d("ProfileEditActivity", "Fetched Customer Phone: " + phone);
                        Log.d("ProfileEditActivity", "Fetched Customer Birth: " + birth);
                        Log.d("ProfileEditActivity", "Fetched Avatar URL: " + avatarUrl);
                        Log.d("ProfileEditActivity", "Fetched Customer Address: " + address);
                        Log.d("ProfileEditActivity", "Fetched Gender: " + sex);

                        // Set the customer details in both the TextView and EditText fields
                        if (name != null) {
                            txtCustomerName.setText(name);
                            edtCustomerName.setText(name);
                            txtCustomerFullName.setText(name);
                        } else {
                            Log.e("ProfileEditActivity", "CustomerName is null");
                        }

                        if (phone != null) {
                            edtCustomerPhone.setText(phone);
                        } else {
                            Log.e("ProfileEditActivity", "CustomerPhone is null");
                        }

                        if (birth != null) {
                            edtCustomerBirth.setText(birth);
                        } else {
                            Log.e("ProfileEditActivity", "CustomerBirth is null");
                        }

                        if (address != null) {
                            txtCustomerFullAddress.setText(address);
                        } else {
                            Log.e("ProfileEditActivity", "CustomerAddress is null");
                        }

                        // Handle checkbox for Gender (Sex)
                        if ("Female".equalsIgnoreCase(sex)) {
                            ckbFemale.setChecked(true);
                            ckbMale.setChecked(false);
                        } else if ("Male".equalsIgnoreCase(sex)) {
                            ckbMale.setChecked(true);
                            ckbFemale.setChecked(false);
                        }


                        // Load the customer's avatar image using Glide
                        if (avatarUrl != null) {
                            Glide.with(ProfileEditActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.mipmap.img_circle) // Placeholder for avatar
                                    .into(imgCustomerAva);
                        }
                    } else {
                        Log.e("ProfileEditActivity", "Error fetching customer data: " + task.getException());
                        Toast.makeText(ProfileEditActivity.this, "Failed to load customer data.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("ProfileEditActivity", "Error fetching customer data: ", e);
            }
        }

    }
