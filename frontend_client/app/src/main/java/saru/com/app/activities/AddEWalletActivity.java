package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import saru.com.app.R;

public class AddEWalletActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerEWalletType;
    private EditText editTextPhoneNumber;
    private Button btnSaveEWallet;
    private String paymentMethodID;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ewallet);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spinnerEWalletType = findViewById(R.id.spinner_ewallet_type);
        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        btnSaveEWallet = findViewById(R.id.btn_save_ewallet);

        // Set up Spinner with E-Wallet options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ewallet_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEWalletType.setAdapter(adapter);

        // Check for edit mode
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEdit", false);
        if (isEditMode) {
            paymentMethodID = intent.getStringExtra("paymentMethodID");
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String eWalletType = intent.getStringExtra("eWalletType");

            if (phoneNumber != null) {
                editTextPhoneNumber.setText(phoneNumber);
            }
            if (eWalletType != null) {
                ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>) spinnerEWalletType.getAdapter();
                for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                    if (spinnerAdapter.getItem(i).toString().equals(eWalletType)) {
                        spinnerEWalletType.setSelection(i);
                        break;
                    }
                }
            }
            btnSaveEWallet.setText(R.string.update_ewallet);
        }

        ImageView backArrow = findViewById(R.id.ic_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        btnSaveEWallet.setOnClickListener(v -> saveEWallet());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveEWallet() {
        String userUID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userUID == null) {
            Toast.makeText(this, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        String eWalletType = spinnerEWalletType.getSelectedItem().toString();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("PaymentMethodID", "2"); // E-Wallet
        paymentMethod.put("PaymentMethod", "E-Wallet");
        paymentMethod.put("PhoneNumber", phoneNumber);
        paymentMethod.put("EWalletType", eWalletType);

        if (isEditMode && paymentMethodID != null) {
            // Update existing E-Wallet
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection("paymentMethods")
                    .document(paymentMethodID)
                    .set(paymentMethod)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AddEWallet", "E-Wallet payment method updated successfully");
                        Intent intent = new Intent();
                        intent.putExtra("ewalletType", eWalletType);
                        intent.putExtra("phoneNumber", phoneNumber);
                        setResult(RESULT_OK, intent);
                        Toast.makeText(AddEWalletActivity.this, R.string.ewallet_updated_success, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AddEWallet", "Error updating E-Wallet payment method: " + e.getMessage());
                        Toast.makeText(AddEWalletActivity.this, R.string.ewallet_update_error, Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add new E-Wallet
            createPaymentMethod(userUID, paymentMethod);
        }
    }

    private String generateUniquePaymentId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100; // Generate a random number between 100 and 999
        return timestamp + randomNum; // Example: 20250628181309123
    }

    private void createPaymentMethod(String userUID, Map<String, Object> paymentMethod) {
        String paymentMethodID = generateUniquePaymentId(); // Generate unique ID

        db.collection("paymentofcustomer")
                .document(userUID)
                .collection("paymentMethods")
                .document(paymentMethodID)
                .set(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AddEWallet", "E-Wallet payment method added successfully with ID: " + paymentMethodID);
                    Intent intent = new Intent();
                    intent.putExtra("ewalletType", paymentMethod.get("EWalletType").toString());
                    intent.putExtra("phoneNumber", paymentMethod.get("PhoneNumber").toString());
                    setResult(RESULT_OK, intent);
                    Toast.makeText(AddEWalletActivity.this, R.string.ewallet_added_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddEWallet", "Error adding E-Wallet payment method: " + e.getMessage());
                    Toast.makeText(AddEWalletActivity.this, R.string.ewallet_add_error, Toast.LENGTH_SHORT).show();
                });
    }
}