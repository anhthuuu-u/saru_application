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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saru.com.app.R;

public class AddEWalletActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerEWalletType;
    private EditText editTextPhoneNumber;
    private Button btnSaveEWallet;
    private String paymentCollection;
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
            paymentCollection = intent.getStringExtra("paymentCollection");
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

        if (isEditMode && paymentCollection != null && paymentMethodID != null) {
            // Update existing E-Wallet
            db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(paymentCollection)
                    .document(paymentMethodID)
                    .set(paymentMethod)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AddEWallet", "E-Wallet payment method updated successfully in " + paymentCollection);
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
            findNextAvailablePaymentIndex(userUID, nextIndex -> {
                if (nextIndex > 0 && nextIndex <= 5) {
                    String paymentCollectionName = "payment0" + nextIndex;
                    db.collection("paymentofcustomer")
                            .document(userUID)
                            .collection(paymentCollectionName)
                            .document(String.valueOf(System.currentTimeMillis())) // Unique ID
                            .set(paymentMethod)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AddEWallet", "E-Wallet payment method added successfully to " + paymentCollectionName);
                                Intent intent = new Intent();
                                intent.putExtra("ewalletType", eWalletType);
                                intent.putExtra("phoneNumber", phoneNumber);
                                setResult(RESULT_OK, intent);
                                Toast.makeText(AddEWalletActivity.this, R.string.ewallet_added_success, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AddEWallet", "Error adding E-Wallet payment method: " + e.getMessage());
                                Toast.makeText(AddEWalletActivity.this, R.string.ewallet_add_error, Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, R.string.max_payment_methods_reached, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void findNextAvailablePaymentIndex(String userUID, OnIndexFoundListener listener) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Create tasks for checking each payment collection
        for (int i = 1; i <= 5; i++) {
            String collectionName = "payment0" + i;
            Task<QuerySnapshot> task = db.collection("paymentofcustomer")
                    .document(userUID)
                    .collection(collectionName)
                    .get();
            tasks.add(task);
        }

        // Wait for all tasks to complete
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(task -> {
                    int[] occupied = new int[6]; // 1 to 5
                    for (int i = 0; i < tasks.size(); i++) {
                        Task<QuerySnapshot> queryTask = tasks.get(i);
                        if (queryTask.isSuccessful() && !queryTask.getResult().isEmpty()) {
                            occupied[i + 1] = 1; // Mark as occupied
                        }
                    }

                    // Find the next available index
                    for (int i = 1; i <= 5; i++) {
                        if (occupied[i] == 0) {
                            listener.onIndexFound(i);
                            return;
                        }
                    }
                    listener.onIndexFound(-1); // No available slot
                })
                .addOnFailureListener(e -> {
                    Log.e("AddEWallet", "Error checking payment collections: " + e.getMessage());
                    listener.onIndexFound(-1); // Indicate failure
                });
    }

    // Callback interface to handle the async result
    interface OnIndexFoundListener {
        void onIndexFound(int index);
    }
}