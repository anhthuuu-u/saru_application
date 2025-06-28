package saru.com.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import saru.com.app.R;

public class ProfileUpdateCardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtCusName, txtCusMail;
    private Spinner edtCardType;
    private EditText edtBank, edtCardNum, edtCVV, edtExDate;
    private Button btnSaveInfo;
    private String selectedPaymentMethodID; // To store the selected payment method ID

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

        // Restrict edtCVV to 3 digits
        edtCVV.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3), new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && !source.toString().matches("[0-9]*")) {
                    return "";
                }
                return null;
            }
        }});

        // Restrict and format edtExDate to MM/YY with mandatory "/"
        edtExDate.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String newText = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                        String cleanText = newText.replace("/", "");

                        // Allow only digits and "/" at the correct position
                        if (source != null && !source.toString().matches("[0-9/]*")) {
                            return "";
                        }

                        // Prevent multiple slashes
                        if (source.toString().equals("/") && newText.indexOf("/") != newText.lastIndexOf("/")) {
                            return "";
                        }

                        // Allow up to 4 digits (MMYY)
                        if (cleanText.length() > 4) {
                            return "";
                        }

                        // Automatically add "/" after MM (e.g., after typing "06")
                        if (cleanText.length() == 2 && dstart == 2 && !newText.contains("/")) {
                            return source + "/";
                        }

                        // Prevent adding "/" manually unless at position 2
                        if (source.toString().equals("/") && dstart != 2) {
                            return "";
                        }

                        return null;
                    }
                },
                new InputFilter.LengthFilter(5) // Limit to 5 characters (MM/YY)
        });

        // Check if this is an edit operation
        if (getIntent().hasExtra("paymentMethodID")) {
            selectedPaymentMethodID = getIntent().getStringExtra("paymentMethodID");
            fetchExistingPaymentMethod(selectedPaymentMethodID);
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
        db.collection("paymentofcustomer")
                .document(userUID)
                .collection("paymentMethods")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String paymentMethodID = document.getId();
                            String cardType = document.getString("CardType");
                            String bank = document.getString("Bank");
                            TextView paymentView = findViewById(getResources().getIdentifier("txt_payment_" + paymentMethodID, "id", getPackageName()));
                            if (paymentView != null) {
                                paymentView.setText(cardType + " - " + bank);
                                paymentView.setOnLongClickListener(v -> {
                                    showContextMenu(paymentMethodID);
                                    return true;
                                });
                            }
                        }
                    }
                });
    }

    private void showContextMenu(String paymentMethodID) {
        String[] options = {"Delete", "Edit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Delete
                        showDeleteConfirmation(paymentMethodID);
                    } else if (which == 1) { // Edit
                        Intent intent = new Intent(ProfileUpdateCardActivity.this, ProfileUpdateCardActivity.class);
                        intent.putExtra("paymentMethodID", paymentMethodID);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    private void showDeleteConfirmation(String paymentMethodID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deletePaymentMethod(paymentMethodID);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);
        builder.create().show();
    }

    private void deletePaymentMethod(String paymentMethodID) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection("paymentMethods")
                    .document(paymentMethodID)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ProfileUpdateCardActivity", "Payment method deleted successfully");
                        Toast.makeText(ProfileUpdateCardActivity.this, "Payment method deleted", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(getIntent());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileUpdateCardActivity", "Error deleting payment method: " + e.getMessage());
                        Toast.makeText(ProfileUpdateCardActivity.this, "Error deleting payment method", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fetchExistingPaymentMethod(String paymentMethodID) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection("paymentMethods")
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
                            if (edtExDate != null && expiryDate != null) {
                                // Format expiryDate from MM/YYYY to MM/YY for display
                                String[] parts = expiryDate.split("/");
                                if (parts.length == 3) {
                                    String mm = parts[1];
                                    String yy = parts[2].substring(2); // Last 2 digits of year
                                    edtExDate.setText(mm + "/" + yy);
                                }
                            }
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

        // Get data from the input fields
        String cardType = edtCardType.getSelectedItem() != null ? edtCardType.getSelectedItem().toString() : "";
        String bank = edtBank.getText().toString().trim();
        String cardNumber = edtCardNum.getText().toString().trim();
        String cvv = edtCVV.getText().toString().trim();
        String expiryDateInput = edtExDate.getText().toString().trim();

        if (cardType.isEmpty() || bank.isEmpty() || cardNumber.isEmpty() || cvv.isEmpty() || expiryDateInput.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate expiry date format MM/YY with mandatory "/"
        if (!expiryDateInput.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            Toast.makeText(this, "Expiry date must be in MM/YY format (e.g., 06/25)", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] expiryParts = expiryDateInput.split("/");
        int expiryMonth = Integer.parseInt(expiryParts[0]);
        int expiryYear = Integer.parseInt("20" + expiryParts[1]); // Convert to full year (e.g., 25 -> 2025)

        if (expiryMonth < 1 || expiryMonth > 12) {
            Toast.makeText(this, "Month must be between 01 and 12", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate that the expiry date is not in the past
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date()); // Current date: June 28, 2025
        int currentMonth = currentDate.get(Calendar.MONTH) + 1; // 0-based, so +1
        int currentYear = currentDate.get(Calendar.YEAR);

        if (expiryYear < currentYear || (expiryYear == currentYear && expiryMonth < currentMonth)) {
            Toast.makeText(this, "Expiry date must be current or future date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format expiryDate to MM/YYYY for storage
        String expiryDate = String.format("%02d/%d", expiryMonth, expiryYear);

        // Create a map for the payment method data
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("PaymentMethodID", "1"); // Internet Banking
        paymentMethod.put("PaymentMethod", "Internet Banking");
        paymentMethod.put("Bank", bank);
        paymentMethod.put("CardNumber", cardNumber);
        paymentMethod.put("CVV", cvv);
        paymentMethod.put("ExpiryDate", expiryDate);
        paymentMethod.put("CardType", cardType);

        if (selectedPaymentMethodID != null) {
            // Update existing payment method
            updatePaymentMethod(selectedPaymentMethodID, paymentMethod);
        } else {
            // Create new payment method
            createPaymentMethod(userUID, paymentMethod);
        }
    }

    private void updatePaymentMethod(String paymentMethodID, Map<String, Object> paymentMethod) {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID != null) {
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection("paymentMethods")
                    .document(paymentMethodID)
                    .set(paymentMethod)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ProfileUpdateCardActivity", "Payment method updated successfully");
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

    private String generateUniquePaymentId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100; // Generate a random number between 100 and 999
        return timestamp + randomNum; // Example: 20250628165009123 at 06:50 PM +07, June 28, 2025
    }

    private void createPaymentMethod(String userUID, Map<String, Object> paymentMethod) {
        String paymentMethodID = generateUniquePaymentId(); // Generate unique ID

        db.collection("paymentofcustomer")
                .document(userUID)
                .collection("paymentMethods")
                .document(paymentMethodID)
                .set(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileUpdateCardActivity", "Payment method saved successfully with ID: " + paymentMethodID);
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