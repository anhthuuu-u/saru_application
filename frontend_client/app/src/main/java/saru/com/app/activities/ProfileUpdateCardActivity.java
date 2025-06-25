package saru.com.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import saru.com.app.R;

public class ProfileUpdateCardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtCusName, txtCusMail;
    private Spinner edtCardType;
    private EditText edtBank, edtCardNum, edtCVV, edtExDate;
    private Button btnSaveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_updatecard);
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.setLoggingEnabled(true);
            db.useEmulator("10.0.2.2", 8080); // For Android emulator
        }

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        txtCusName = findViewById(R.id.txtCusName);
        txtCusMail = findViewById(R.id.txtCusMail);
        edtCardType = findViewById(R.id.edtCardType);
        edtBank = findViewById(R.id.edtBank);
        edtCardNum = findViewById(R.id.edtCardNum);
        edtCVV = findViewById(R.id.edtCVV);
        edtExDate = findViewById(R.id.edtExDate);
        btnSaveInfo = findViewById(R.id.btn_save_info);

        btnSaveInfo.setOnClickListener(v -> savePaymentInfo());

        // Back button
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Fetch user data (name, email)
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            fetchUserData(userUID);
        } else {
            Toast.makeText(ProfileUpdateCardActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePaymentInfo() {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID == null) {
            Log.e("ProfileUpdateCardActivity", "User not authenticated");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;

        }
        Log.d("ProfileUpdateCardActivity", "Authenticated user UID: " + userUID);
        Log.d("ProfileUpdateCardActivity", "Attempting to write to path: paymentofcustomer/" + userUID + "/payment0X");

        // Get data from the input fields
        String cardType = edtCardType.getSelectedItem().toString();
        String bank = edtBank.getText().toString().trim();
        String cardNumber = edtCardNum.getText().toString().trim();
        String cvv = edtCVV.getText().toString().trim();
        String expiryDate = edtExDate.getText().toString().trim();

        if (cardType.isEmpty() || bank.isEmpty() || cardNumber.isEmpty() || cvv.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map for the payment method data
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("PaymentMethodID", "1"); // Giá trị mặc định theo yêu cầu
        paymentMethod.put("PaymentMethod", "Internet Banking"); // Giá trị mặc định theo yêu cầu
        paymentMethod.put("Bank", bank);
        paymentMethod.put("CardNumber", cardNumber);
        paymentMethod.put("CVV", cvv);
        paymentMethod.put("ExpiryDate", expiryDate);

        // Kiểm tra và xác định collection payment0X phù hợp
        determinePaymentCollection(userUID, paymentMethod);
    }

    private void determinePaymentCollection(String userUID, Map<String, Object> paymentMethod) {
        DocumentReference userDocRef = db.collection("paymentofcustomer").document(userUID);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Log.d("ProfileUpdateCardActivity", "Creating parent document for UID: " + userUID);
                userDocRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    Log.d("ProfileUpdateCardActivity", "Parent document created successfully");
                    proceedWithPaymentCollection(userUID, paymentMethod);
                }).addOnFailureListener(e -> {
                    Log.e("ProfileUpdateCardActivity", "Error creating parent document: " + e.getMessage());
                    Toast.makeText(this, "Error initializing payment data", Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.d("ProfileUpdateCardActivity", "Parent document exists for UID: " + userUID);
                proceedWithPaymentCollection(userUID, paymentMethod);
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileUpdateCardActivity", "Error checking document: " + e.getMessage());
            Toast.makeText(this, "Error checking payment methods", Toast.LENGTH_SHORT).show();
        });
    }

    private void proceedWithPaymentCollection(String userUID, Map<String, Object> paymentMethod) {
        final int[] nextPaymentIndex = {1};
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 1; i <= 5; i++) {
            final int currentIndex = i;
            String paymentCollection = "payment0" + String.format("%02d", currentIndex);
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty() && nextPaymentIndex[0] == currentIndex) {
                                    createPaymentCollection(userUID, paymentCollection, paymentMethod);
                                    latch.countDown();
                                    return;
                                } else if (!task.getResult().isEmpty()) {
                                    nextPaymentIndex[0] = currentIndex + 1;
                                }
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    if (nextPaymentIndex[0] <= 5) {
                        String paymentCollection = "payment0" + String.format("%02d", nextPaymentIndex[0]);
                        createPaymentCollection(userUID, paymentCollection, paymentMethod);
                    } else {
                        Toast.makeText(this, "Maximum 5 payment methods allowed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                Log.e("ProfileUpdateCardActivity", "Interrupted while waiting for latch: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error processing payment methods", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    private void createPaymentCollection(String userUID, String paymentCollection, Map<String, Object> paymentMethod) {
        db.collection("paymentofcustomer")
                .document(userUID)
                .collection(paymentCollection)
                .document("PaymentMethodID")
                .set(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileUpdateCardActivity", "Payment method saved successfully to " + paymentCollection);
                    Toast.makeText(ProfileUpdateCardActivity.this, "Payment method saved successfully", Toast.LENGTH_SHORT).show();
                    showCustomToast();
                    finish(); // Quay lại activity trước đó sau khi lưu thành công
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileUpdateCardActivity", "Error saving payment method: " + e.getMessage());
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        Log.e("ProfileUpdateCardActivity", "Check Firestore rules for userUID: " + userUID);
                    }
                    Toast.makeText(ProfileUpdateCardActivity.this, "Error saving payment method: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchUserData(String userUID) {
        // Lấy CustomerID từ bảng "accounts"
        DocumentReference accountRef = FirebaseFirestore.getInstance().collection("accounts").document(userUID);

        accountRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String customerEmail = documentSnapshot.getString("CustomerEmail");

                    Log.d("ProfileUpdateCardActivity", "Fetched CustomerEmail: " + customerEmail);

                    if (txtCusMail != null && customerEmail != null) {
                        txtCusMail.setText(customerEmail);
                    } else {
                        Log.e("ProfileUpdateCardActivity", "CustomerEmail is missing.");
                        Toast.makeText(ProfileUpdateCardActivity.this, "CustomerEmail is missing", Toast.LENGTH_SHORT).show();
                    }

                    String customerID = documentSnapshot.getString("CustomerID");

                    Log.d("ProfileUpdateCardActivity", "Fetched CustomerID: " + customerID);

                    if (customerID != null) {
                        fetchCustomerData(customerID);
                    } else {
                        Log.e("ProfileUpdateCardActivity", "CustomerID is missing.");
                        Toast.makeText(ProfileUpdateCardActivity.this, "CustomerID is missing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ProfileUpdateCardActivity", "No data found in accounts.");
                    Toast.makeText(ProfileUpdateCardActivity.this, "No data found in accounts", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ProfileUpdateCardActivity", "Error fetching user data: " + task.getException());
                Toast.makeText(ProfileUpdateCardActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCustomerData(String customerID) {
        DocumentReference customerRef = FirebaseFirestore.getInstance().collection("customers").document(customerID);

        customerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot customerSnapshot = task.getResult();
                if (customerSnapshot != null && customerSnapshot.exists()) {
                    String customerName = customerSnapshot.getString("CustomerName");

                    Log.d("ProfileUpdateCardActivity", "Fetched CustomerName: " + customerName);

                    if (txtCusName != null && customerName != null) {
                        txtCusName.setText(customerName);
                    } else {
                        Log.e("ProfileUpdateCardActivity", "CustomerName is missing.");
                        Toast.makeText(ProfileUpdateCardActivity.this, "CustomerName is missing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ProfileUpdateCardActivity", "Customer data not found.");
                    Toast.makeText(ProfileUpdateCardActivity.this, "Customer data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ProfileUpdateCardActivity", "Error fetching customer data: " + task.getException());
                Toast.makeText(ProfileUpdateCardActivity.this, "Error fetching customer data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView message = layout.findViewById(R.id.tv_toast_message);
        message.setText(getString(R.string.update_success_message));

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}