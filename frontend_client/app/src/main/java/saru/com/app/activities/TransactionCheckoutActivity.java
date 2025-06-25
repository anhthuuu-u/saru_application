package saru.com.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import saru.com.app.R;
import saru.com.app.models.Voucher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionCheckoutActivity extends AppCompatActivity {

    // Constants for activity result
    private static final int EDIT_ADDRESS_REQUEST = 1001;
    private static final int EDIT_PAYMENT_METHOD_REQUEST = 1002;

    // UI Components
    ImageView imgBack;
    ImageView imgVoucher;
    ImageView imgEditInfo;
    ImageView imgEditInfoBank;
    TextView txtCustomerName;
    TextView txtCustomerPhoneNumber;
    TextView txtCustomerAddress;
    EditText edtVoucherCode;
    Button btnApplyVoucherCode;
    RadioButton radCOD;
    RadioButton radBank;
    RadioButton radEWallet;
    LinearLayout layoutCOD;
    LinearLayout layoutBank;
    LinearLayout layoutEWallet;
    Button btnPlaceOrder;
    TextView txtTotalPrice;
    TextView txtMerchandiseTotal;
    LinearLayout layoutBankDetails;
    LinearLayout layoutCODDetails;
    LinearLayout layoutEWalletDetails;

    // TextView for payment info
    TextView txtBankName;
    TextView txtCardNumber;
    TextView txtCardType;

    // Data variables
    private double totalAmount = 1020000;
    private double discountAmount = 0;
    private double payableAmount = 1020000;
    private String selectedPaymentMethod = "COD";

    // Address information variables
    private String customerName = "";
    private String customerPhone = "";
    private String customerAddress = "";

    // Payment information variables
    private String bankName = "";
    private String cardNumber = "";
    private String cardType = "";
    private String cvv = "";
    private String expiryDate = "";

    // Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_checkout);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        addViews();

        // Set up listeners
        addEvents();

        // Set initial data
        setupInitialData();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupInitialData() {
        customerName = getString(R.string.title_customer_name);
        customerPhone = getString(R.string.title_customer_phone_number);
        customerAddress = getString(R.string.title_customer_address);

        updateCustomerInfo();
        updatePaymentSummary();
        updatePaymentInfo();
    }

    private void updateCustomerInfo() {
        txtCustomerName.setText(customerName);
        txtCustomerPhoneNumber.setText(customerPhone);
        txtCustomerAddress.setText(customerAddress);
    }

    private void updatePaymentSummary() {
        txtMerchandiseTotal.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", totalAmount));
        txtTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", payableAmount));
    }

    private void updatePaymentInfo() {
        if (txtBankName != null) {
            txtBankName.setText(bankName.isEmpty() ? "Chưa có thông tin" : bankName);
        }
        if (txtCardNumber != null) {
            String maskedCardNumber = cardNumber.isEmpty() ? "Chưa có thông tin" :
                    "**** **** **** " + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
            txtCardNumber.setText(maskedCardNumber);
        }
        if (txtCardType != null) {
            txtCardType.setText(cardType.isEmpty() ? "Chưa có thông tin" : cardType);
        }
    }

    private void addViews() {
        imgBack = findViewById(R.id.imgBack);
        imgEditInfo = findViewById(R.id.imgEditInfo);
        imgEditInfoBank = findViewById(R.id.imgEditInfoBank);
        txtCustomerName = findViewById(R.id.edtCustomerName);
        txtCustomerPhoneNumber = findViewById(R.id.txtCustomerPhoneNumber);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        btnApplyVoucherCode = findViewById(R.id.btnApplyVoucherCode);
        radCOD = findViewById(R.id.radCOD);
        radBank = findViewById(R.id.radBank);
        radEWallet = findViewById(R.id.radEWallet);
        layoutCOD = findViewById(R.id.layoutCOD);
        layoutBank = findViewById(R.id.layoutBank);
        layoutEWallet = findViewById(R.id.layoutEWallet);

        imgVoucher = findViewById(R.id.imgVoucher);

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtMerchandiseTotal = findViewById(R.id.txtMerchandiseTotal);

        layoutCODDetails = findViewById(R.id.layoutCODDetails);
        layoutBankDetails = findViewById(R.id.layoutBankDetails);
        layoutEWalletDetails = findViewById(R.id.layoutEWalletDetails);

        txtBankName = findViewById(R.id.txtBankName);
        txtCardNumber = findViewById(R.id.txtCardNumber);
        txtCardType = findViewById(R.id.txtCardType);
    }

    private void addEvents() {
        imgEditInfo.setOnClickListener(v -> openTransactionEditAddressActivity());
        btnPlaceOrder.setOnClickListener(v -> openSuccessfulPaymentActivity());
        imgEditInfoBank.setOnClickListener(v -> openTransactionEditPaymentMethodActivity());
        imgVoucher.setOnClickListener(v -> openVouchersManagementActivity());

        btnApplyVoucherCode.setOnClickListener(v -> {
            String discountCode = edtVoucherCode.getText().toString().trim();
            if (!discountCode.isEmpty()) {
                applyVoucherCode(discountCode);
            } else {
                Toast.makeText(this, R.string.title_enter_voucher_code_message, Toast.LENGTH_SHORT).show();
            }
        });

        layoutCOD.setOnClickListener(v -> {
            radCOD.setChecked(true);
            radBank.setChecked(false);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "COD";
            updatePaymentMethodDetails();
        });

        layoutBank.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(true);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "BANK";
            updatePaymentMethodDetails();
        });

        layoutEWallet.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(false);
            radEWallet.setChecked(true);
            selectedPaymentMethod = "EWALLET";
            updatePaymentMethodDetails();
        });

        radCOD.setOnClickListener(v -> {
            radBank.setChecked(false);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "COD";
            updatePaymentMethodDetails();
        });

        radBank.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "BANK";
            updatePaymentMethodDetails();
        });

        radEWallet.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(false);
            selectedPaymentMethod = "EWALLET";
            updatePaymentMethodDetails();
        });
    }

    private void openVouchersManagementActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, VouchersManagement.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_ADDRESS_REQUEST && resultCode == RESULT_OK && data != null) {
            customerName = data.getStringExtra("address_name");
            customerPhone = data.getStringExtra("address_phone");
            customerAddress = data.getStringExtra("address_full");
            updateCustomerInfo();
            Toast.makeText(this, "Thông tin địa chỉ đã được cập nhật", Toast.LENGTH_SHORT).show();
        } else if (requestCode == EDIT_PAYMENT_METHOD_REQUEST && resultCode == RESULT_OK && data != null) {
            bankName = data.getStringExtra("bank_name");
            cardNumber = data.getStringExtra("card_number");
            cardType = data.getStringExtra("card_type");
            cvv = data.getStringExtra("cvv");
            expiryDate = data.getStringExtra("expiry_date");
            updatePaymentInfo();
            Toast.makeText(this, "Thông tin thanh toán đã được cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyVoucherCode(String discountCode) {
        validateVoucherCode(discountCode, (isValid, voucher) -> {
            if (isValid && voucher != null) {
                discountAmount = 50000; // Adjust discount based on Firestore data if needed
                payableAmount = totalAmount - discountAmount;
                updatePaymentSummary();
                btnApplyVoucherCode.setText(R.string.title_applied);
                btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
                edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
                edtVoucherCode.setEnabled(true);

                String currentAppliedCode = discountCode;
                // Display voucher description in Toast
                Toast.makeText(this, voucher.getDescription(), Toast.LENGTH_SHORT).show();

                edtVoucherCode.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        String newCode = edtVoucherCode.getText().toString().trim();
                        if (!newCode.equals(currentAppliedCode) && !newCode.isEmpty()) {
                            resetVoucherButtonState();
                        }
                    }
                });

                edtVoucherCode.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String newCode = s.toString().trim();
                        if (!newCode.equals(currentAppliedCode)) {
                            resetVoucherButtonState();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });
            } else {
                Toast.makeText(this, R.string.title_invalid_voucher_code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateVoucherCode(String code, VoucherValidationCallback callback) {
        if (code.isEmpty()) {
            callback.onValidationResult(false, null);
            return;
        }

        db.collection("vouchers")
                .whereEqualTo("voucherCode", code)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Voucher exists, check expiry date
                            Voucher voucher = querySnapshot.getDocuments().get(0).toObject(Voucher.class);
                            if (voucher != null && isVoucherValid(voucher)) {
                                callback.onValidationResult(true, voucher);
                            } else {
                                callback.onValidationResult(false, null);
                            }
                        } else {
                            callback.onValidationResult(false, null);
                        }
                    } else {
                        Toast.makeText(this, "Error checking voucher: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onValidationResult(false, null);
                    }
                });
    }

    private boolean isVoucherValid(Voucher voucher) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expiryDate = sdf.parse(voucher.getExpiryDate());
            Date currentDate = new Date();
            return expiryDate != null && !expiryDate.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void resetVoucherButtonState() {
        btnApplyVoucherCode.setText(R.string.title_voucher_apply);
        btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.color_golden_yellow));
        edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.color_golden_yellow));
        discountAmount = 0;
        payableAmount = totalAmount;
        updatePaymentSummary();
    }

    private void updatePaymentMethodDetails() {
        layoutCODDetails.setVisibility(View.GONE);
        layoutBankDetails.setVisibility(View.GONE);
        layoutEWalletDetails.setVisibility(View.GONE);

        switch (selectedPaymentMethod) {
            case "COD":
                layoutCODDetails.setVisibility(View.VISIBLE);
                break;
            case "BANK":
                layoutBankDetails.setVisibility(View.VISIBLE);
                break;
            case "EWALLET":
                layoutEWalletDetails.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void do_back(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionCheckoutActivity.this);
        Resources res = getResources();
        builder.setTitle(R.string.title_confirm_exit);
        builder.setMessage(R.string.title_confirm_exit_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.title_confirm_exit_message_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.title_confirm_exit_message_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    public void do_edit_information(View view) {}

    public void do_payment(View view) {}

    void openTransactionEditAddressActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionEditAddressActivity.class);
        intent.putExtra("address_name", customerName);
        intent.putExtra("address_phone", customerPhone);
        intent.putExtra("address_full", customerAddress);
        startActivityForResult(intent, EDIT_ADDRESS_REQUEST);
    }

    void openTransactionEditPaymentMethodActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionEditPaymentMethodActivity.class);
        intent.putExtra("bank_name", bankName);
        intent.putExtra("card_number", cardNumber);
        intent.putExtra("card_type", cardType);
        intent.putExtra("cvv", cvv);
        intent.putExtra("expiry_date", expiryDate);
        startActivityForResult(intent, EDIT_PAYMENT_METHOD_REQUEST);
    }

    void openSuccessfulPaymentActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, SuccessfulPaymentActivity.class);
        startActivity(intent);
    }

    // Callback interface for async Firestore validation
    private interface VoucherValidationCallback {
        void onValidationResult(boolean isValid, Voucher voucher);
    }
}