package saru.com.app;

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

public class TransactionCheckoutActivity extends AppCompatActivity {

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

    // Data variables
    private double totalAmount = 1020000; // 1,020,000đ
    private double discountAmount = 0;
    private double payableAmount = 1020000; // 1,020,000đ
    private String selectedPaymentMethod = "COD"; // Default payment method

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
        // Set customer information
        txtCustomerName.setText(R.string.title_customer_name);
        txtCustomerPhoneNumber.setText(R.string.title_customer_phone_number);
        txtCustomerAddress.setText(R.string.title_customer_address);

        // Set payment amounts
        updatePaymentSummary();
    }

    private void updatePaymentSummary() {
    }

    private void addViews() {
        imgBack = findViewById(R.id.imgBack);
        imgEditInfo = findViewById(R.id.imgEditInfo);
        imgEditInfoBank=findViewById(R.id.imgEditInfoBank);
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

        // The checkout button is at the bottom of the screen, possibly in a bottom bar
        // that isn't included in the ScrollView but is part of the main layout
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        txtTotalPayment = findViewById(R.id.txtTotalPayment);
        txtMerchandiseTotal = findViewById(R.id.txtMerchandiseTotal);

        layoutCODDetails = findViewById(R.id.layoutCODDetails);
        layoutBankDetails = findViewById(R.id.layoutBankDetails);
        layoutEWalletDetails = findViewById(R.id.layoutEWalletDetails);
    }

    private void addEvents() {

        // Edit shipping info button
        imgEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTransactionEditAddressActivity();
            }
        }
//        {
//            // Open shipping information edit activity/dialog
//            Toast.makeText(this, "Chỉnh sửa thông tin vận chuyển", Toast.LENGTH_SHORT).show();
//
//        }
        );
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
            // Kiểm tra xem nút đang ở trạng thái "Apply" hay "Applied"
            if (btnApplyVoucherCode.getText().toString().equals(getString(R.string.title_voucher_apply))) {
                // Nút đang ở trạng thái "Apply", xử lý áp dụng voucher
                String discountCode = edtVoucherCode.getText().toString().trim();
                if (!discountCode.isEmpty()) {
                    applyVoucherCode(discountCode);
                } else {
                    Toast.makeText(this, R.string.title_enter_voucher_code_message, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Nút đang ở trạng thái "Applied"
                // Người dùng có thể muốn áp dụng lại mã hiện tại hoặc đã thay đổi mã
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

        // Checkout button
//        btnPlaceOrder.setOnClickListener(v -> {
//            processCheckout();
//        });
    }

    private void applyVoucherCode(String discountCode) {
        // Check if the voucher code is valid - this would typically involve an API call
        // For this example, we'll simulate a valid voucher code check
        boolean isValidCode = validateVoucherCode(discountCode);

        if (isValidCode) {
            // Apply discount (in real app, you would calculate the actual discount)
            discountAmount = 50000; // Example: 50,000đ discount
            payableAmount = totalAmount - discountAmount;

            // Update payment summary
            updatePaymentSummary();

            // Change button text from "Apply" to "Applied"
            btnApplyVoucherCode.setText(R.string.title_applied);

            // Change colors of button and edit text
            btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
            edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));

            // Keep edit text editable so user can change the code if needed
            edtVoucherCode.setEnabled(true);

            // Store currently applied voucher code
            String currentAppliedCode = discountCode;

            // Show success message
            Toast.makeText(this, R.string.title_voucher_applied_success, Toast.LENGTH_SHORT).show();

            // Change the OnFocusChangeListener for the voucher code edit text to detect changes
            edtVoucherCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        // Check if code was changed
                        String newCode = edtVoucherCode.getText().toString().trim();
                        if (!newCode.equals(currentAppliedCode) && !newCode.isEmpty()) {
                            // Reset button to "Apply" state
                            resetVoucherButtonState();
                        }
                    }
                }
            });

            // Add text change listener to detect when user types in a new code
            edtVoucherCode.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String newCode = s.toString().trim();
                    if (!newCode.equals(currentAppliedCode)) {
                        // Reset button to "Apply" state
                        resetVoucherButtonState();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        } else {
            // Invalid voucher code
            Toast.makeText(this, R.string.title_invalid_voucher_code, Toast.LENGTH_SHORT).show();
        }
    }

    // Simulate voucher code validation
    private boolean validateVoucherCode(String code) {
        // In a real app, this would check against a database or API
        // For example purposes, let's accept any non-empty code
        return !code.isEmpty();
    }

    // Reset the voucher button and edit text to the initial state
    private void resetVoucherButtonState() {
        // Reset button text back to "Apply"
        btnApplyVoucherCode.setText(R.string.title_voucher_apply);

        // Reset colors
        btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.color_golden_yellow));
        edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.color_golden_yellow));

        // Reset discount amount
        discountAmount = 0;
        payableAmount = totalAmount;

        // Update the payment summary
        updatePaymentSummary();
    }
    private void updatePaymentMethodDetails() {
        // Ẩn tất cả các layout chi tiết
        layoutCODDetails.setVisibility(View.GONE);
        layoutBankDetails.setVisibility(View.GONE);
        layoutEWalletDetails.setVisibility(View.GONE);

        // Hiển thị layout chi tiết tương ứng với phương thức thanh toán đã chọn
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
//    private void processCheckout() {
//    }

    public void do_back(View view) {
        AlertDialog.Builder builder=new AlertDialog.Builder(TransactionCheckoutActivity.this);
        Resources res=getResources();
        //Thiết lập tiêu đề
        builder.setTitle(R.string.title_confirm_exit);
        //Nội dung cửa sổ
        builder.setMessage(R.string.title_confirm_exit_message);
        //biểu tượng
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        //thiết lập tương tác YES
        builder.setPositiveButton(R.string.title_confirm_exit_message_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                System.exit(0);
                finish();
            }
        });
        builder.setNegativeButton(R.string.title_confirm_exit_message_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void do_edit_information(View view) {

    }
    public void do_payment(View view) {

    }
    void openTransactionEditAddressActivity() {
        Intent intent = new Intent(TransactionCheckoutActivity.this, TransactionEditAddressActivity.class);
        startActivity(intent);
    }
    void openTransactionEditPaymentMethodActivity(){
        Intent intent=new Intent(TransactionCheckoutActivity.this, TransactionEditPaymentMethodActivity.class);
        startActivity(intent);
    }
    void  openTransactionFaceAuthorizationActivity(){
        Intent intent=new Intent(TransactionCheckoutActivity.this, TransactionFaceAuthorizationActivity.class);
        startActivity(intent);
    }

}