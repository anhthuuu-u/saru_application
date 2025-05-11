package saru.com.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TransactionCheckoutActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private ImageButton btnEditInfo;
    private TextView txtCustomerName;
    private TextView txtCustomerPhoneNumber;
    private TextView txtCustomerAddress;
    private EditText edtVoucherCode;
    private Button btnApplyVoucherCode;
    private RadioButton radCOD;
    private RadioButton radBank;
    private RadioButton radEWallet;
    private LinearLayout layoutCOD;
    private LinearLayout layoutBank;
    private LinearLayout layoutEWallet;
    private Button btnPlaceOrder;
    private TextView txtTotalPayment;
    private TextView txtMerchandiseTotal;

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
        setupListeners();

        // Set initial data
        setupInitialData();
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
        btnBack = findViewById(R.id.btnBack);
        btnEditInfo = findViewById(R.id.btnEditInfo);
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
    }

    private void setupListeners() {
        // Back button listener
        btnBack.setOnClickListener(v -> onBackPressed());

        // Edit shipping info button
        btnEditInfo.setOnClickListener(v -> {
            // Open shipping information edit activity/dialog
            Toast.makeText(this, "Chỉnh sửa thông tin vận chuyển", Toast.LENGTH_SHORT).show();
        });

        // Apply discount code button
        btnApplyVoucherCode.setOnClickListener(v -> {
            String discountCode = edtVoucherCode.getText().toString().trim();
            if (!discountCode.isEmpty()) {
                applyVoucherCode(discountCode);
            } else {
                Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
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
        });

        radBank.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radEWallet.setChecked(false);
            selectedPaymentMethod = "BANK";
        });

        radEWallet.setOnClickListener(v -> {
            radCOD.setChecked(false);
            radBank.setChecked(false);
            selectedPaymentMethod = "EWALLET";
        });

        // Checkout button
        btnPlaceOrder.setOnClickListener(v -> {
            processCheckout();
        });
    }

    private void applyVoucherCode(String discountCode) {
    }

    private void processCheckout() {
    }

    public void do_back(View view) {
    }
    public void do_edit_information (View view)
    {

    }
}