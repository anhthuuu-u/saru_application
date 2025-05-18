package saru.com.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TransactionEditPaymentMethodActivity extends AppCompatActivity {

    private Spinner spnCardType;
    private EditText edtBankName;
    private EditText edtCardNumber;
    private EditText edtCvv;
    private EditText edtExpiryDate;
    private MaterialButton btnFaceAuthorCancel;
    private Button btnFaceAuthorConfirm;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_edit_payment_method);

        // Initialize views
        spnCardType = findViewById(R.id.spnCardType);
        edtBankName = findViewById(R.id.edtBankName);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtCvv = findViewById(R.id.edtCvv);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        btnFaceAuthorCancel = findViewById(R.id.btnFaceAuthorCancel);
        btnFaceAuthorConfirm = findViewById(R.id.btnFaceAuthorConfirm);
        imgBack = findViewById(R.id.imgBack);

        // Setup spinner with card types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.card_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCardType.setAdapter(adapter);

        // Set click listeners
        imgBack.setOnClickListener(v -> onBackPressed());
        btnFaceAuthorCancel.setOnClickListener(v -> onBackPressed());
        btnFaceAuthorConfirm.setOnClickListener(v -> onConfirmClick());
    }

    public void onConfirmClick() {
        // Validate input fields
        String bankName = edtBankName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().trim();
        String cvv = edtCvv.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String cardType = spnCardType.getSelectedItem().toString();

        if (bankName.isEmpty()) {
            edtBankName.setError(getString(R.string.error_bank_name_empty));
            return;
        }

        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            edtCardNumber.setError(getString(R.string.error_invalid_card_number));
            return;
        }

        if (cvv.isEmpty() || cvv.length() < 3) {
            edtCvv.setError(getString(R.string.error_invalid_cvv));
            return;
        }

        if (expiryDate.isEmpty() || !isValidExpiryDate(expiryDate)) {
            edtExpiryDate.setError(getString(R.string.error_invalid_expiry_date));
            return;
        }

        // Process payment method update
        // TODO: Implement actual payment method update logic
        Toast.makeText(this, R.string.message_payment_method_updated, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isValidExpiryDate(String expiryDate) {
        // Basic expiry date validation (MM/YY format)
        String[] parts = expiryDate.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            // Add more sophisticated date validation if needed
            return month >= 1 && month <= 12 && year >= 25;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onConfirmClick(View view) {
    }
}