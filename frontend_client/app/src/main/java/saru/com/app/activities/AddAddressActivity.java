package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;

public class AddAddressActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText edtCustomerName2, edtCustomerPhone2, edtCustomerAddress2;
    private String customerID;

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

        ImageView backIcon = findViewById(R.id.ic_back_arrow);
        backIcon.setOnClickListener(v -> finish());

        Button btnSaveAddress = findViewById(R.id.btnSaveAddress);
        btnSaveAddress.setOnClickListener(v -> saveNewAddress());
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        edtCustomerName2 = findViewById(R.id.edtCustomerName2);
        edtCustomerPhone2 = findViewById(R.id.edtCustomerPhone2);
        edtCustomerAddress2 = findViewById(R.id.edtCustomerAddress2);
    }

    private void saveNewAddress() {
        String name2 = edtCustomerName2.getText().toString().trim();
        String phone2 = edtCustomerPhone2.getText().toString().trim();
        String address2 = edtCustomerAddress2.getText().toString().trim();

        if (name2.isEmpty() || phone2.isEmpty() || address2.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một document mới trong subcollection 'addresses'
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name2);
        addressData.put("phone", phone2);
        addressData.put("address", address2);

        db.collection("customers").document(customerID)
                .collection("addresses")
                .add(addressData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AddAddressActivity", "Address added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Address saved successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra("name", name2);
                    intent.putExtra("phone", phone2);
                    intent.putExtra("address", address2);
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddAddressActivity", "Error adding address: ", e);
                    Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show();
                });
    }
}