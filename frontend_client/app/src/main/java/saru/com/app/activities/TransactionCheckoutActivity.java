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

import saru.com.app.R;

public class TransactionCheckoutActivity extends AppCompatActivity {

    // Constants for activity result
    private static final int EDIT_ADDRESS_REQUEST = 1001;
    private static final int EDIT_PAYMENT_METHOD_REQUEST = 1002; // Thêm constant mới

    // UI Components
    ImageView imgBack;
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
    TextView txtTotalPayment;
    TextView txtMerchandiseTotal;
    LinearLayout layoutBankDetails;
    LinearLayout layoutCODDetails;
    LinearLayout layoutEWalletDetails;

    // Thêm các TextView để hiển thị thông tin payment
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

    // Payment information variables - Thêm biến lưu thông tin payment
    private String bankName = "";
    private String cardNumber = "";
    private String cardType = "";
    private String cvv = "";
    private String expiryDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_checkout);

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
        updatePaymentInfo(); // Thêm method cập nhật thông tin payment
    }

    private void updateCustomerInfo() {
        txtCustomerName.setText(customerName);
        txtCustomerPhoneNumber.setText(customerPhone);
        txtCustomerAddress.setText(customerAddress);
    }

    private void updatePaymentSummary() {
        // Update payment summary display
        // Implementation depends on your UI layout
    }

    // Thêm method cập nhật thông tin payment
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
        txtCustomerName = findViewById(R.id.txtCustomerName);
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

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        txtTotalPayment = findViewById(R.id.txtTotalPayment);
        txtMerchandiseTotal = findViewById(R.id.txtMerchandiseTotal);

        layoutCODDetails = findViewById(R.id.layoutCODDetails);
        layoutBankDetails = findViewById(R.id.layoutBankDetails);
        layoutEWalletDetails = findViewById(R.id.layoutEWalletDetails);

        // Thêm initialization cho các TextView payment info
        txtBankName = findViewById(R.id.txtBankName);
        txtCardNumber = findViewById(R.id.txtCardNumber);
        txtCardType = findViewById(R.id.txtCardType);
    }

    private void addEvents() {
        // Edit shipping info button
        imgEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionEditAddressActivity();
            }
        });

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionFaceAuthorizationActivity();
            }
        });

        imgEditInfoBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionEditPaymentMethodActivity();
            }
        });

        // Apply discount code button
        btnApplyVoucherCode.setOnClickListener(v -> {
            if (btnApplyVoucherCode.getText().toString().equals(getString(R.string.title_voucher_apply))) {
                String discountCode = edtVoucherCode.getText().toString().trim();
                if (!discountCode.isEmpty()) {
                    applyVoucherCode(discountCode);
                } else {
                    Toast.makeText(this, R.string.title_enter_voucher_code_message, Toast.LENGTH_SHORT).show();
                }
            } else {
                String currentCode = edtVoucherCode.getText().toString().trim();
                if (!currentCode.isEmpty()) {
                    applyVoucherCode(currentCode);
                } else {
                    Toast.makeText(this, R.string.title_enter_voucher_code_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Payment method selection
        layoutCOD.setOnClickListener(v -> {
            radCOD.setChecked(true);
            radBank.setChecked(false);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "COD";
        });

        layoutBank.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(true);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "BANK";
        });

        layoutEWallet.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(false);
            radEWallet.setChecked(true);
            selectedPaymentMethod = "EWALLET";
        });

        // Radio button listeners
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

    // Handle result from both EditAddress and EditPaymentMethod activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_ADDRESS_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get updated address information
            customerName = data.getStringExtra("address_name");
            customerPhone = data.getStringExtra("address_phone");
            customerAddress = data.getStringExtra("address_full");

            // Update UI with new information
            updateCustomerInfo();

            Toast.makeText(this, "Thông tin địa chỉ đã được cập nhật", Toast.LENGTH_SHORT).show();
        }
        // Thêm xử lý cho payment method result
        else if (requestCode == EDIT_PAYMENT_METHOD_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get updated payment information
            bankName = data.getStringExtra("bank_name");
            cardNumber = data.getStringExtra("card_number");
            cardType = data.getStringExtra("card_type");
            cvv = data.getStringExtra("cvv");
            expiryDate = data.getStringExtra("expiry_date");

            // Update UI with new payment information
            updatePaymentInfo();

            Toast.makeText(this, "Thông tin thanh toán đã được cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyVoucherCode(String discountCode) {
        boolean isValidCode = validateVoucherCode(discountCode);

        if (isValidCode) {
            discountAmount = 50000;
            payableAmount = totalAmount - discountAmount;

            updatePaymentSummary();

            btnApplyVoucherCode.setText(R.string.title_applied);
            btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
            edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
            edtVoucherCode.setEnabled(true);

            String currentAppliedCode = discountCode;

            Toast.makeText(this, R.string.title_voucher_applied_success, Toast.LENGTH_SHORT).show();

            edtVoucherCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String newCode = edtVoucherCode.getText().toString().trim();
                        if (!newCode.equals(currentAppliedCode) && !newCode.isEmpty()) {
                            resetVoucherButtonState();
                        }
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
    }

    private boolean validateVoucherCode(String code) {
        return !code.isEmpty();
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

    public void do_edit_information(View view) {

    }

    public void do_payment(View view) {

    }

    void openTransactionEditAddressActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionEditAddressActivity.class);

        // Pass current address information to edit activity
        intent.putExtra("address_name", customerName);
        intent.putExtra("address_phone", customerPhone);
        intent.putExtra("address_full", customerAddress);

        startActivityForResult(intent, EDIT_ADDRESS_REQUEST);
    }

    // Cập nhật method để sử dụng startActivityForResult
    void openTransactionEditPaymentMethodActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionEditPaymentMethodActivity.class);

        // Pass current payment information to edit activity
        intent.putExtra("bank_name", bankName);
        intent.putExtra("card_number", cardNumber);
        intent.putExtra("card_type", cardType);
        intent.putExtra("cvv", cvv);
        intent.putExtra("expiry_date", expiryDate);

        startActivityForResult(intent, EDIT_PAYMENT_METHOD_REQUEST);
    }

    void openTransactionFaceAuthorizationActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionFaceAuthorizationActivity.class);
        startActivity(intent);
    }
}