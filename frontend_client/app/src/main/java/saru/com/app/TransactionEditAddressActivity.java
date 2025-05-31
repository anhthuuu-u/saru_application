package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TransactionEditAddressActivity extends AppCompatActivity {

    private ImageView imgBack;
    private TextView titleTextView;
    private EditText edtCustomerName;
    private EditText edtPhoneNumber;
    private EditText edtCustomerAddress;
    private LinearLayout googleMapButton;
    private Button btnTransactionEditAddressCancel;
    private Button btnTransactionEditAddressConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_edit_address);

        addViews();
        setupListeners();
        loadAddressData();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {
        imgBack = findViewById(R.id.imgBack);
        titleTextView = findViewById(R.id.txtEditAddress);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtCustomerAddress = findViewById(R.id.edtCustomerAddress);
        googleMapButton = findViewById(R.id.google_map_button);
        btnTransactionEditAddressCancel = findViewById(R.id.btnTransactionEditAddressCancel);
        btnTransactionEditAddressConfirm = findViewById(R.id.btnTransactionEditAddressConfirm);
    }

    private void setupListeners() {
        googleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMap();
            }
        });

        // Back button listener
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Cancel button listener
        btnTransactionEditAddressCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });

        // Confirm button listener
        btnTransactionEditAddressConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick(v);
            }
        });
    }

    public void onCancelClick(View view) {
        // Handle cancel button click
        finish();
    }

    public void onConfirmClick(View view) {
        // Handle confirm button click
        if (validateInputs()) {
            saveAddressAndReturn();
        }
    }

    private void openGoogleMap() {
        // Open Google Maps for address selection
        Toast.makeText(TransactionEditAddressActivity.this,
                "Opening Google Maps...", Toast.LENGTH_SHORT).show();

        // Intent to open map activity
        // For example:
        // Intent mapIntent = new Intent(this, MapSelectionActivity.class);
        // startActivityForResult(mapIntent, MAP_REQUEST_CODE);
    }

    private void loadAddressData() {
        // Get address data from intent (passed from CheckoutActivity)
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("address_name");
            String phone = intent.getStringExtra("address_phone");
            String address = intent.getStringExtra("address_full");

            // Set the data to EditText fields if they exist
            if (name != null && !name.isEmpty()) {
                edtCustomerName.setText(name);
            }
            if (phone != null && !phone.isEmpty()) {
                edtPhoneNumber.setText(phone);
            }
            if (address != null && !address.isEmpty()) {
                edtCustomerAddress.setText(address);
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate customer name
        String name = edtCustomerName.getText().toString().trim();
        if (name.isEmpty()) {
            edtCustomerName.setError("Tên không được để trống");
            edtCustomerName.requestFocus();
            isValid = false;
        } else {
            edtCustomerName.setError(null);
        }

        // Validate phone number
        String phone = edtPhoneNumber.getText().toString().trim();
        if (phone.isEmpty()) {
            edtPhoneNumber.setError("Số điện thoại không được để trống");
            if (isValid) edtPhoneNumber.requestFocus();
            isValid = false;
        } else if (!phone.matches("\\d{10,11}")) {
            edtPhoneNumber.setError("Số điện thoại không hợp lệ (10-11 chữ số)");
            if (isValid) edtPhoneNumber.requestFocus();
            isValid = false;
        } else {
            edtPhoneNumber.setError(null);
        }

        // Validate address
        String address = edtCustomerAddress.getText().toString().trim();
        if (address.isEmpty()) {
            edtCustomerAddress.setError("Địa chỉ không được để trống");
            if (isValid) edtCustomerAddress.requestFocus();
            isValid = false;
        } else {
            edtCustomerAddress.setError(null);
        }

        return isValid;
    }

    private void saveAddressAndReturn() {
        String name = edtCustomerName.getText().toString().trim();
        String phone = edtPhoneNumber.getText().toString().trim();
        String address = edtCustomerAddress.getText().toString().trim();

        // Create intent to return data to CheckoutActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("address_name", name);
        resultIntent.putExtra("address_phone", phone);
        resultIntent.putExtra("address_full", address);

        // Set result and finish activity
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Đã lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();

        // Return to checkout activity
        finish();
    }
}