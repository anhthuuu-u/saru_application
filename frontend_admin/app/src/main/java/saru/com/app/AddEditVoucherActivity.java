package saru.com.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;
import saru.com.models.Voucher;

public class AddEditVoucherActivity extends AppCompatActivity {
    private EditText edtDescription, edtCode, expiryDate;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private Voucher voucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_voucher);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        displayVoucher();
        setupEvents();
    }

    private void initializeViews() {
        edtDescription = findViewById(R.id.textDescription);
        edtCode = findViewById(R.id.textField);
        expiryDate = findViewById(R.id.endDate);
        btnSave = findViewById(R.id.btnSaveVoucher);
        btnCancel = findViewById(R.id.btnCancelVoucher);
    }

    private void displayVoucher() {
        voucher = (Voucher) getIntent().getSerializableExtra("SELECTED_VOUCHER");
        if (voucher != null) {
            edtDescription.setText(voucher.getDescription());
            edtCode.setText(voucher.getVoucherCode());
            expiryDate.setText(voucher.getExpiryDate());
        }
    }

    private void setupEvents() {
        btnSave.setOnClickListener(v -> saveVoucher());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveVoucher() {
        String description = edtDescription.getText().toString().trim();
        String code = edtCode.getText().toString().trim();
        String expiry = expiryDate.getText().toString().trim();
        if (description.isEmpty() || code.isEmpty() || expiry.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (voucher == null) {
            voucher = new Voucher(UUID.randomUUID().toString(), description, expiry, code);
        } else {
            voucher.setDescription(description);
            voucher.setVoucherCode(code);
            voucher.setExpiryDate(expiry);
        }

        db.collection("vouchers").document(voucher.getVoucherID()).set(voucher)
                .addOnSuccessListener(aVoid -> {
                    showToast("Voucher saved");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save voucher: " + e.getMessage()));
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}