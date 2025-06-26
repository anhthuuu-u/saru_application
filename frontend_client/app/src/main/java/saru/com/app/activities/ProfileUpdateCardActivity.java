package saru.com.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.concurrent.atomic.AtomicBoolean;

import saru.com.app.R;

public class ProfileUpdateCardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtCusName, txtCusMail;
    private Spinner edtCardType;
    private EditText edtBank, edtCardNum, edtCVV, edtExDate;
    private Button btnSaveInfo;
    private String selectedPaymentCollection; // To store the selected payment method collection
    private String selectedPaymentMethodID;   // To store the selected payment method ID

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

        // Check if this is an edit operation
        if (getIntent().hasExtra("paymentCollection") && getIntent().hasExtra("paymentMethodID")) {
            selectedPaymentCollection = getIntent().getStringExtra("paymentCollection");
            selectedPaymentMethodID = getIntent().getStringExtra("paymentMethodID");
            fetchExistingPaymentMethod(selectedPaymentCollection, selectedPaymentMethodID);
        }

        btnSaveInfo.setOnClickListener(v -> savePaymentInfo());

        // Back button
        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Fetch user data (name, email)
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            fetchUserData(userUID);
            fetchPaymentMethods(userUID); // Fetch existing payment methods for long-click
        } else {
            Toast.makeText(ProfileUpdateCardActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPaymentMethods(String userUID) {
        for (int i = 1; i <= 5; i++) {
            String paymentCollection = "payment" + String.format("%02d", i);
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                TextView paymentView = findViewById(getResources().getIdentifier("txt_payment_" + paymentCollection, "id", getPackageName()));
                                if (paymentView != null) {
                                    paymentView.setText(document.getString("CardType") + " - " + document.getString("Bank"));
                                    final String finalPaymentCollection = paymentCollection;
                                    final String paymentMethodID = document.getId();
                                    paymentView.setOnLongClickListener(v -> {
                                        showContextMenu(finalPaymentCollection, paymentMethodID);
                                        return true;
                                    });
                                }
                            }
                        }
                    });
        }
    }

    private void showContextMenu(String paymentCollection, String paymentMethodID) {
        String[] options = {"Delete", "Edit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Delete
                        showDeleteConfirmation(paymentCollection, paymentMethodID);
                    } else if (which == 1) { // Edit
                        Intent intent = new Intent(ProfileUpdateCardActivity.this, ProfileUpdateCardActivity.class);
                        intent.putExtra("paymentCollection", paymentCollection);
                        intent.putExtra("paymentMethodID", paymentMethodID);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    private void showDeleteConfirmation(String paymentCollection, String paymentMethodID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deletePaymentMethod(paymentCollection, paymentMethodID);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);
        builder.create().show();
    }

    private void deletePaymentMethod(String paymentCollection, String paymentMethodID) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .document(paymentMethodID)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ProfileUpdateCardActivity", "Payment method deleted successfully from " + paymentCollection);
                        Toast.makeText(ProfileUpdateCardActivity.this, "Payment method deleted", Toast.LENGTH_SHORT).show();
                        // Refresh the activity or update UI
                        finish();
                        startActivity(getIntent());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileUpdateCardActivity", "Error deleting payment method: " + e.getMessage());
                        Toast.makeText(ProfileUpdateCardActivity.this, "Error deleting payment method", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fetchExistingPaymentMethod(String paymentCollection, String paymentMethodID) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .document(paymentMethodID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String cardType = documentSnapshot.getString("CardType");
                            String bank = documentSnapshot.getString("Bank");
                            String cardNumber = documentSnapshot.getString("CardNumber");
                            String cvv = documentSnapshot.getString("CVV");
                            String expiryDate = documentSnapshot.getString("ExpiryDate");

                            // Set values to UI elements
                            if (edtCardType != null && cardType != null) {
                                // Assuming edtCardType is a Spinner, you may need to set the selection
                                Log.d("ProfileUpdateCardActivity", "Setting cardType: " + cardType);
                            }
                            if (edtBank != null) edtBank.setText(bank != null ? bank : "");
                            if (edtCardNum != null) edtCardNum.setText(cardNumber != null ? cardNumber : "");
                            if (edtCVV != null) edtCVV.setText(cvv != null ? cvv : "");
                            if (edtExDate != null) edtExDate.setText(expiryDate != null ? expiryDate : "");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileUpdateCardActivity", "Error fetching payment method: " + e.getMessage());
                        Toast.makeText(ProfileUpdateCardActivity.this, "Error loading payment method", Toast.LENGTH_SHORT).show();
                    });
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
        String cardType = edtCardType.getSelectedItem() != null ? edtCardType.getSelectedItem().toString() : "";
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
        paymentMethod.put("PaymentMethodID", "1"); // Internet Banking
        paymentMethod.put("PaymentMethod", "Internet Banking");
        paymentMethod.put("Bank", bank);
        paymentMethod.put("CardNumber", cardNumber);
        paymentMethod.put("CVV", cvv);
        paymentMethod.put("ExpiryDate", expiryDate);
        paymentMethod.put("CardType", cardType);

        if (selectedPaymentCollection != null && selectedPaymentMethodID != null) {
            // Update existing payment method
            updatePaymentMethod(selectedPaymentCollection, selectedPaymentMethodID, paymentMethod);
        } else {
            // Create new payment method
            determinePaymentCollection(userUID, paymentMethod);
        }
    }

    private void updatePaymentMethod(String paymentCollection, String paymentMethodID, Map<String, Object> paymentMethod) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .document(paymentMethodID)
                    .set(paymentMethod)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ProfileUpdateCardActivity", "Payment method updated successfully in " + paymentCollection);
                        Toast.makeText(ProfileUpdateCardActivity.this, "Payment method updated successfully", Toast.LENGTH_SHORT).show();
                        showCustomToast();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileUpdateCardActivity", "Error updating payment method: " + e.getMessage());
                        Toast.makeText(ProfileUpdateCardActivity.this, "Error updating payment method", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void determinePaymentCollection(String userUID, Map<String, Object> paymentMethod) {
        final int[] nextPaymentIndex = {1}; // Start from payment01, will adjust based on existing
        final AtomicBoolean isCollectionAssigned = new AtomicBoolean(false); // Flag to prevent multiple writes
        final boolean[] occupied = new boolean[6]; // Track occupied collections (1 to 5)
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 1; i <= 5; i++) {
            final int currentIndex = i;
            String paymentCollection = "payment" + String.format("%02d", currentIndex);

            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && !isCollectionAssigned.get()) {
                                if (!task.getResult().isEmpty()) {
                                    occupied[currentIndex] = true; // Mark as occupied
                                } else if (task.getResult().isEmpty() && currentIndex > nextPaymentIndex[0]) {
                                    // Update nextPaymentIndex to the first empty slot
                                    if (nextPaymentIndex[0] < currentIndex) {
                                        nextPaymentIndex[0] = currentIndex;
                                    }
                                }
                            }
                        } finally {
                            latch.countDown();
                            // After all checks, assign to the next available slot
                            if (latch.getCount() == 0 && !isCollectionAssigned.get()) {
                                for (int j = 1; j <= 5; j++) {
                                    if (!occupied[j] && !isCollectionAssigned.get()) {
                                        isCollectionAssigned.set(true);
                                        createPaymentCollection(userUID, "payment" + String.format("%02d", j), paymentMethod);
                                        return;
                                    }
                                }
                            }
                        }
                    });
        }

        new Thread(() -> {
            try {
                latch.await();

                runOnUiThread(() -> {
                    if (!isCollectionAssigned.get() && nextPaymentIndex[0] <= 5) {
                        String paymentCollection = "payment" + String.format("%02d", nextPaymentIndex[0]);
                        // Ensure we find the next available slot
                        for (int i = nextPaymentIndex[0]; i <= 5; i++) {
                            if (!occupied[i]) {
                                createPaymentCollection(userUID, "payment" + String.format("%02d", i), paymentMethod);
                                return;
                            }
                        }
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
        String paymentMethodID = String.valueOf(System.currentTimeMillis()); // Unique ID based on timestamp

        db.collection("paymentofcustomer")
                .document(userUID)
                .collection(paymentCollection)
                .document(paymentMethodID)
                .set(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileUpdateCardActivity", "Payment method saved successfully to " + paymentCollection);
                    Toast.makeText(ProfileUpdateCardActivity.this, "Payment method saved successfully", Toast.LENGTH_SHORT).show();
                    showCustomToast();
                    finish();
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