package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;

public class AddAddressActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText edtCustomerName2, edtCustomerPhone2, edtCustomerAddress2;
    private String customerID;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        initializeFirebase();
        initializeViews();

        customerID = getIntent().getStringExtra("customerID");
        if (customerID == null) {
            Log.e("AddAddressActivity", "CustomerID is null");
            Toast.makeText(this, "Error: CustomerID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if in edit mode
        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            String addressId = getIntent().getStringExtra("addressId");
            String name = getIntent().getStringExtra("name");
            String phone = getIntent().getStringExtra("phone");
            String address = getIntent().getStringExtra("address");

            if (name != null) edtCustomerName2.setText(name);
            if (phone != null) edtCustomerPhone2.setText(phone);
            if (address != null) edtCustomerAddress2.setText(address);
        }

        ImageView backIcon = findViewById(R.id.ic_back_arrow);
        backIcon.setOnClickListener(v -> finish());

        Button btnSaveAddress = findViewById(R.id.btnSaveAddress);
        btnSaveAddress.setOnClickListener(v -> saveAddress(isEdit));
    }

    private void saveAddress(boolean isEdit) {
        String name2 = edtCustomerName2.getText().toString().trim();
        String phone2 = edtCustomerPhone2.getText().toString().trim();
        String address2 = edtCustomerAddress2.getText().toString().trim();

        if (name2.isEmpty() || phone2.isEmpty() || address2.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name2);
        addressData.put("phone", phone2);
        addressData.put("address", address2);
        addressData.put("isDefault", false);
        addressData.put("lastUpdated", com.google.firebase.Timestamp.now()); // Add server timestamp for sync

        if (isEdit) {
            String addressId = getIntent().getStringExtra("addressId");
            if (addressId != null && customerID != null) {
                String documentPath = "customers/" + customerID + "/addresses/" + addressId;
                Log.d("AddAddressActivity", "Attempting to update address at path: " + documentPath);

                // Asynchronous check for document existence
                DocumentReference docRef = db.collection("customers").document(customerID).collection("addresses").document(addressId);
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("AddAddressActivity", "Document exists at path: " + documentPath);

                        // Check authentication
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser == null) {
                            Log.e("AddAddressActivity", "User not authenticated");
                            uiHandler.post(() -> Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        Log.d("AddAddressActivity", "User UID: " + currentUser.getUid() + ", CustomerID: " + customerID);

                        // Perform the update with server sync
                        db.collection("customers").document(customerID)
                                .collection("addresses")
                                .document(addressId)
                                .set(addressData, SetOptions.merge())
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("AddAddressActivity", "Address updated successfully with ID: " + addressId + ", Server time: " + addressData.get("lastUpdated"));
                                    uiHandler.post(() -> {
                                        Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent();
                                        intent.putExtra("addressId", addressId);
                                        intent.putExtra("name", name2);
                                        intent.putExtra("phone", phone2);
                                        intent.putExtra("address", address2);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AddAddressActivity", "Error updating address: " + e.getMessage(), e);
                                    uiHandler.post(() -> Toast.makeText(this, "Failed to update address: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                })
                                .addOnCompleteListener(task -> {
                                    Log.d("AddAddressActivity", "Update operation completed for address ID: " + addressId + ", isSuccessful: " + task.isSuccessful());
                                });
                    } else {
                        Log.e("AddAddressActivity", "Document does not exist at path: " + documentPath);
                        uiHandler.post(() -> Toast.makeText(this, "Error: Address document not found", Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> {
                    Log.e("AddAddressActivity", "Error checking document existence: " + e.getMessage(), e);
                    uiHandler.post(() -> Toast.makeText(this, "Error verifying address: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            } else {
                Log.e("AddAddressActivity", "addressId or customerID is null in edit mode: addressId=" + addressId + ", customerID=" + customerID);
                Toast.makeText(this, "Error: Address ID or Customer ID not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            db.collection("customers").document(customerID)
                    .collection("addresses")
                    .add(addressData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AddAddressActivity", "Address added with ID: " + documentReference.getId());
                        uiHandler.post(() -> {
                            Toast.makeText(this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra("name", name2);
                            intent.putExtra("phone", phone2);
                            intent.putExtra("address", address2);
                            setResult(RESULT_OK, intent);
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AddAddressActivity", "Error adding address: ", e);
                        uiHandler.post(() -> Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show());
                    });
        }
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        db.enableNetwork();
    }

    private void initializeViews() {
        edtCustomerName2 = findViewById(R.id.edtCustomerName2);
        edtCustomerPhone2 = findViewById(R.id.edtCustomerPhone2);
        edtCustomerAddress2 = findViewById(R.id.edtCustomerAddress2);
    }
}