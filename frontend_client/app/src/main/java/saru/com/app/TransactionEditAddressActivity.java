package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TransactionEditAddressActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleTextView;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private ConstraintLayout googleMapButton;
    private Button cancelButton;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_edit_address);

        initializeViews();
        setupListeners();
        loadAddressData();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleTextView = findViewById(R.id.title_text_view);
        nameEditText = findViewById(R.id.name_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        addressEditText = findViewById(R.id.address_edit_text);
        googleMapButton = findViewById(R.id.google_map_button);
        cancelButton = findViewById(R.id.cancel_button);
        confirmButton = findViewById(R.id.confirm_button);
    }

    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        googleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Google Maps for address selection
                // This would be implemented based on your app's requirements
                Toast.makeText(TransactionEditAddressActivity.this,
                        "Opening Google Maps...", Toast.LENGTH_SHORT).show();
                // Intent to open map activity would go here
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveAddressAndReturn();
                }
            }
        });
    }

    private void loadAddressData() {
        // Check if we're editing an existing address
        Intent intent = getIntent();
        if (intent.hasExtra("address_name")) {
            nameEditText.setText(intent.getStringExtra("address_name"));
            phoneEditText.setText(intent.getStringExtra("address_phone"));
            addressEditText.setText(intent.getStringExtra("address_full"));
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Tên không được để trống");
            isValid = false;
        }

        String phone = phoneEditText.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneEditText.setError("Số điện thoại không được để trống");
            isValid = false;
        } else if (!phone.matches("\\d{10,11}")) {
            phoneEditText.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }

        if (addressEditText.getText().toString().trim().isEmpty()) {
            addressEditText.setError("Địa chỉ không được để trống");
            isValid = false;
        }

        return isValid;
    }

    private void saveAddressAndReturn() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("address_name", name);
        resultIntent.putExtra("address_phone", phone);
        resultIntent.putExtra("address_full", address);

        setResult(RESULT_OK, resultIntent);

        // Return to TransactionCheckoutActivity
        finish();
    }
}