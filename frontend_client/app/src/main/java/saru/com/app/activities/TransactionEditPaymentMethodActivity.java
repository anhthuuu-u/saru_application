package saru.com.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import saru.com.app.R;

public class TransactionEditPaymentMethodActivity extends AppCompatActivity {

    private Spinner spnCardType;
    private EditText edtBankName;
    private EditText edtCardNumber;
    private EditText edtCvv;
    private EditText edtExpiryDate;
    private MaterialButton btnFaceAuthorCancel;
    private Button btnFaceAuthorConfirm;
    private ImageView imgBack;

    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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

        // Load existing payment information if available
        loadExistingPaymentInfo();

        // Set click listeners
        imgBack.setOnClickListener(v -> onBackPressed());
        btnFaceAuthorCancel.setOnClickListener(v -> onBackPressed());
        btnFaceAuthorConfirm.setOnClickListener(v -> onConfirmClick());

        // Set up date picker for expiry date
        setupDatePicker();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupDatePicker() {
        // Làm cho EditText không thể edit trực tiếp, chỉ có thể chọn qua DatePicker
        edtExpiryDate.setFocusable(false);
        edtExpiryDate.setClickable(true);

        edtExpiryDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar currentDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Cập nhật EditText với ngày đã chọn
                        edtExpiryDate.setText(dateFormat.format(selectedDate.getTime()));
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Chỉ cho phép chọn ngày từ hôm nay trở đi
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());

        // Giới hạn ngày tối đa (20 năm từ bây giờ)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 20);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    // Thêm method để load thông tin payment hiện tại
    private void loadExistingPaymentInfo() {
        Intent intent = getIntent();
        if (intent != null) {
            String bankName = intent.getStringExtra("bank_name");
            String cardNumber = intent.getStringExtra("card_number");
            String cardType = intent.getStringExtra("card_type");
            String cvv = intent.getStringExtra("cvv");
            String expiryDate = intent.getStringExtra("expiry_date");

            // Set values to form fields if they exist
            if (bankName != null && !bankName.isEmpty()) {
                edtBankName.setText(bankName);
            }
            if (cardNumber != null && !cardNumber.isEmpty()) {
                edtCardNumber.setText(cardNumber);
            }
            if (cvv != null && !cvv.isEmpty()) {
                edtCvv.setText(cvv);
            }
            if (expiryDate != null && !expiryDate.isEmpty()) {
                edtExpiryDate.setText(expiryDate);
                // Parse existing date để set selectedDate
                try {
                    selectedDate.setTime(dateFormat.parse(expiryDate));
                } catch (Exception e) {
                    // Nếu parse lỗi, giữ nguyên selectedDate hiện tại
                }
            }

            // Set spinner selection for card type
            if (cardType != null && !cardType.isEmpty()) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spnCardType.getAdapter();
                int position = adapter.getPosition(cardType);
                if (position >= 0) {
                    spnCardType.setSelection(position);
                }
            }
        }
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

        if (expiryDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_expiry_date), Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Intent để trả về data cho CheckoutActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("bank_name", bankName);
        resultIntent.putExtra("card_number", cardNumber);
        resultIntent.putExtra("card_type", cardType);
        resultIntent.putExtra("cvv", cvv);
        resultIntent.putExtra("expiry_date", expiryDate);

        // Set result và finish activity
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, R.string.message_payment_method_updated, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onConfirmClick(View view) {
        onConfirmClick();
    }

    public void onBackPressed(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        // Có thể thêm logic để hỏi user có muốn lưu thay đổi không
        super.onBackPressed();
    }
}