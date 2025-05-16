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
                // Open Google Maps for address selection
                openGoogleMap();
            }
        });
    }

    public void onBackPressed(View view) {
        // Handle back button click
        finish();
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
        // This would be implemented based on your app's requirements
        Toast.makeText(TransactionEditAddressActivity.this,
                "Opening Google Maps...", Toast.LENGTH_SHORT).show();

        // Intent to open map activity
        // For example:
        // Intent mapIntent = new Intent(this, MapSelectionActivity.class);
        // startActivityForResult(mapIntent, MAP_REQUEST_CODE);
    }

    private void loadAddressData() {
        // Check if we're editing an existing address
        Intent intent = getIntent();
        if (intent.hasExtra("address_name")) {
            edtCustomerName.setText(intent.getStringExtra(String.valueOf(R.string.title_customer_name)));
            edtPhoneNumber.setText(intent.getStringExtra(String.valueOf(R.string.title_customer_phone_number)));
            edtCustomerAddress.setText(intent.getStringExtra(String.valueOf(R.string.title_customer_address)));
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (edtCustomerName.getText().toString().trim().isEmpty()) {
            edtCustomerName.setError("Tên không được để trống");
            isValid = false;
        }

        String phone = edtPhoneNumber.getText().toString().trim();
        if (phone.isEmpty()) {
            edtPhoneNumber.setError("Số điện thoại không được để trống");
            isValid = false;
        } else if (!phone.matches("\\d{10,11}")) {
            edtPhoneNumber.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }

        if (edtCustomerAddress.getText().toString().trim().isEmpty()) {
            edtCustomerAddress.setError("Địa chỉ không được để trống");
            isValid = false;
        }

        return isValid;
    }

    private void saveAddressAndReturn() {
        String name = edtCustomerName.getText().toString().trim();
        String phone = edtPhoneNumber.getText().toString().trim();
        String address = edtCustomerAddress.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("address_name", name);
        resultIntent.putExtra("address_phone", phone);
        resultIntent.putExtra("address_full", address);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show();

        // Return to previous activity
        finish();
    }
}