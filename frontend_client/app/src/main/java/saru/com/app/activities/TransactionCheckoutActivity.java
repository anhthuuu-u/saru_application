package saru.com.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.connectors.CheckoutAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.Voucher;

public class TransactionCheckoutActivity extends AppCompatActivity {
    private static final int EDIT_ADDRESS_REQUEST = 1001;
    private static final int EDIT_PAYMENT_METHOD_REQUEST = 1002;
    private static final double SHIPPING_FEE = 20000.0;

    // UI Components
    private ImageView imgBack, imgVoucher, imgEditInfo, imgEditInfoBank;
    private TextView txtCustomerName, txtCustomerPhoneNumber, txtCustomerAddress;
    private EditText edtVoucherCode;
    private Button btnApplyVoucherCode, btnPlaceOrder;
    private RadioButton radCOD, radBank, radEWallet;
    private LinearLayout layoutCOD, layoutBank, layoutEWallet;
    private LinearLayout layoutCODDetails, layoutBankDetails, layoutEWalletDetails;
    private TextView txtBankName, txtCardNumber, txtCardType;
    private TextView txtMerchandiseTotal, txtTotalPrice, txtTotalItemQuantity;
    private RecyclerView recyclerProductCheckout;
    private CheckoutAdapter checkoutAdapter;

    // Data variables
    private double totalAmount = 0.0;
    private double discountAmount = 0.0;
    private double payableAmount = 0.0;
    private String selectedPaymentMethod = "COD";
    private String customerName = "";
    private String customerPhone = "";
    private String customerAddress = "";
    private String bankName = "";
    private String cardNumber = "";
    private String cardType = "";
    private String cvv = "";
    private String expiryDate = "";
    private List<CartItem> selectedItems = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_checkout);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        addViews();

        // Get selected items from Intent
        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }

        // Set up RecyclerView with initial data
        checkoutAdapter.updateData(selectedItems);
        updateSummary();

        // Set up listeners
        addEvents();

        // Load selected cart items from Firestore to sync
        loadSelectedCartItems();

        // Set window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {
        imgBack = findViewById(R.id.imgBack);
        imgEditInfo = findViewById(R.id.imgEditInfo);
        imgEditInfoBank = findViewById(R.id.imgEditInfoBank);
        imgVoucher = findViewById(R.id.imgVoucher);
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
        layoutCODDetails = findViewById(R.id.layoutCODDetails);
        layoutBankDetails = findViewById(R.id.layoutBankDetails);
        layoutEWalletDetails = findViewById(R.id.layoutEWalletDetails);
        txtBankName = findViewById(R.id.txtBankName);
        txtCardNumber = findViewById(R.id.txtCardNumber);
        txtCardType = findViewById(R.id.txtCardType);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        txtMerchandiseTotal = findViewById(R.id.txtMerchandiseTotal);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtTotalItemQuantity = findViewById(R.id.txtTotalItemQuantity);
        recyclerProductCheckout = findViewById(R.id.recyclerProductCheckout);

        // Initialize RecyclerView
        recyclerProductCheckout.setLayoutManager(new LinearLayoutManager(this));
        checkoutAdapter = new CheckoutAdapter(this, selectedItems);
        recyclerProductCheckout.setAdapter(checkoutAdapter);
    }

    private void addEvents() {
        imgEditInfo.setOnClickListener(v -> openTransactionEditAddressActivity());
        imgEditInfoBank.setOnClickListener(v -> openTransactionEditPaymentMethodActivity());
        imgVoucher.setOnClickListener(v -> openVouchersManagementActivity());
        btnPlaceOrder.setOnClickListener(v -> openSuccessfulPaymentActivity());

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

    private void loadSelectedCartItems() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String accountID = auth.getCurrentUser().getUid();
        db.collection("carts").document(accountID).collection("items")
                .whereEqualTo("selected", true)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("TransactionCheckout", "Error loading selected items: " + error.getMessage());
                        Toast.makeText(this, "Lỗi khi tải sản phẩm giao dịch", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedItems.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String productID = doc.getString("productID");
                        if (productID == null) {
                            Log.e("TransactionCheckout", "Null ProductID in Firestore document: " + doc.getId());
                            continue;
                        }
                        CartItem item = new CartItem(
                                productID,
                                accountID,
                                doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0L,
                                doc.getString("productName") != null ? doc.getString("productName") : "Unknown",
                                doc.getDouble("productPrice") != null ? doc.getDouble("productPrice") : 0.0,
                                doc.getString("imageID") != null ? doc.getString("imageID") : "",
                                doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1,
                                true
                        );
                        selectedItems.add(item);
                    }
                    checkoutAdapter.updateData(selectedItems);
                    updateSummary();
                    Log.d("TransactionCheckout", "Loaded " + selectedItems.size() + " selected items");
                });
    }

    private void updateSummary() {
        totalAmount = 0.0;
        for (CartItem item : selectedItems) {
            totalAmount += item.getTotalPrice();
        }
        payableAmount = totalAmount + SHIPPING_FEE - discountAmount;

        DecimalFormat formatter = new DecimalFormat("#,###");
        txtMerchandiseTotal.setText(formatter.format(totalAmount + SHIPPING_FEE) + getString(R.string.product_cart_currency));
        txtTotalPrice.setText(formatter.format(payableAmount) + getString(R.string.product_cart_currency));
        txtTotalItemQuantity.setText(String.valueOf(selectedItems.size()));
    }

    private void updateCustomerInfo() {
        txtCustomerName.setText(customerName);
        txtCustomerPhoneNumber.setText(customerPhone);
        txtCustomerAddress.setText(customerAddress);
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

    void openSuccessfulPaymentActivity() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        String accountID = auth.getCurrentUser().getUid();
        WriteBatch batch = db.batch();

        // Calculate total product quantity
        long totalProduct = selectedItems.stream().mapToLong(CartItem::getQuantity).sum();

        // Format order date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        // Fetch CustomerID from accounts collection
        db.collection("accounts").document(accountID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String customerID = documentSnapshot.getString("CustomerID");
                    if (customerID == null) {
                        Toast.makeText(this, "Không tìm thấy CustomerID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Prepare order data
                    Map<String, Object> order = new HashMap<>();
                    String orderId = db.collection("orders").document().getId();
                    order.put("OrderID", orderId);
                    order.put("CustomerID", customerID); // Use CustomerID
                    order.put("items", selectedItems);
                    order.put("totalAmount", payableAmount);
                    order.put("totalProduct", totalProduct);
                    order.put("paymentMethod", selectedPaymentMethod);
                    order.put("customerName", customerName);
                    order.put("customerPhone", customerPhone);
                    order.put("customerAddress", customerAddress);
                    order.put("OrderDate", orderDate);
                    order.put("OrderStatusID", "0"); // Pending confirmation
                    order.put("timestamp", System.currentTimeMillis());

                    // Add order to 'orders' collection
                    batch.set(db.collection("orders").document(orderId), order);

                    // Remove selected items from cart
                    for (CartItem item : selectedItems) {
                        batch.delete(db.collection("carts").document(accountID)
                                .collection("items").document(item.getProductID()));
                    }

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("TransactionCheckout", "Order placed successfully: " + orderId);
                                Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, SuccessfulPaymentActivity.class);
                                intent.putExtra("orderId", orderId);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("TransactionCheckout", "Error placing order: " + e.getMessage());
                                Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionCheckout", "Error fetching CustomerID: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi lấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyVoucherCode(String discountCode) {
        validateVoucherCode(discountCode, (isValid, voucher) -> {
            if (isValid && voucher != null) {
                discountAmount = 50000; // Adjust based on Firestore voucher data
                payableAmount = totalAmount + SHIPPING_FEE - discountAmount;
                updateSummary();
                btnApplyVoucherCode.setText(R.string.title_applied);
                btnApplyVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
                edtVoucherCode.setBackgroundTintList(getResources().getColorStateList(R.color.voucher_applied_color));
                Toast.makeText(this, voucher.getDescription(), Toast.LENGTH_SHORT).show();

                String currentAppliedCode = discountCode;
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
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Voucher voucher = task.getResult().getDocuments().get(0).toObject(Voucher.class);
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
        payableAmount = totalAmount + SHIPPING_FEE;
        updateSummary();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Resources res = getResources();
        builder.setTitle(R.string.title_confirm_exit);
        builder.setMessage(R.string.title_confirm_exit_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.title_confirm_exit_message_yes, (dialog, which) -> finish());
        builder.setNegativeButton(R.string.title_confirm_exit_message_no, (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    void openTransactionEditAddressActivity() {
        Intent intent = new Intent(this, TransactionEditAddressActivity.class);
        intent.putExtra("address_name", customerName);
        intent.putExtra("address_phone", customerPhone);
        intent.putExtra("address_full", customerAddress);
        startActivityForResult(intent, EDIT_ADDRESS_REQUEST);
    }

    void openTransactionEditPaymentMethodActivity() {
        Intent intent = new Intent(this, TransactionEditPaymentMethodActivity.class);
        intent.putExtra("bank_name", bankName);
        intent.putExtra("card_number", cardNumber);
        intent.putExtra("card_type", cardType);
        intent.putExtra("cvv", cvv);
        intent.putExtra("expiry_date", expiryDate);
        startActivityForResult(intent, EDIT_PAYMENT_METHOD_REQUEST);
    }

    void openVouchersManagementActivity() {
        Intent intent = new Intent(this, VouchersManagement.class);
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

    private interface VoucherValidationCallback {
        void onValidationResult(boolean isValid, Voucher voucher);
    }
}