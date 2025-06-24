package saru.com.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.connectors.CartAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;

public class ProductCart extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private ListCartItems listCartItems;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartLayout;
    private TextView txtDeleteAll;
    private LinearLayout bottomSectionLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_cart);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các view
        listCartItems = new ListCartItems();
        recyclerView = findViewById(R.id.cart_recycler_view);
        emptyCartLayout = findViewById(R.id.empty_cart_layout);
        txtDeleteAll = findViewById(R.id.txtProductCart_DeleteAll);
        bottomSectionLayout = findViewById(R.id.bottom_section_layout);

        // Khởi tạo RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, listCartItems, this);
        recyclerView.setAdapter(cartAdapter);

        // Kiểm tra đăng nhập
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Tải dữ liệu từ Firestore
        loadCartFromFirestore();

        // Cập nhật giao diện dựa trên trạng thái giỏ hàng
        updateCartVisibility();

        // Sự kiện nút Back
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện checkbox chọn tất cả
        CheckBox selectAllCheckbox = findViewById(R.id.select_all_checkbox);
        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listCartItems.setAllSelected(isChecked);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        // Sự kiện nút thanh toán
        Button paymentButton = findViewById(R.id.payment_button);
        paymentButton.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : listCartItems.getCartItems()) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_items_selected), Toast.LENGTH_SHORT).show();
                return;
            }
            double total = listCartItems.calculateTotalPrice();
            Toast.makeText(this, getString(R.string.product_cart_payment_toast_label) + " " +
                    String.format("%.0f", total) + getString(R.string.product_cart_currency), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductCart.this, TransactionCheckoutActivity.class);
            intent.putParcelableArrayListExtra("selectedItems", new ArrayList<>(selectedItems));
            startActivity(intent);
        });

        // Sự kiện xóa tất cả
        txtDeleteAll.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : listCartItems.getCartItems()) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_items_selected), Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_delete_all_title))
                    .setMessage(getString(R.string.dialog_delete_all_message, selectedItems.size()))
                    .setPositiveButton(getString(R.string.dialog_confirm_delete), (dialog, which) -> {
                        String accountID = auth.getCurrentUser().getUid();
                        for (CartItem item : selectedItems) {
                            int position = listCartItems.getCartItems().indexOf(item);
                            if (position != -1) {
                                db.collection("carts").document(accountID).collection("items")
                                        .document(item.getProductID())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Log.d("ProductCart", "Deleted item: " + item.getProductName()))
                                        .addOnFailureListener(e -> {
                                            Log.e("ProductCart", "Error deleting item: " + e.getMessage());
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        });
                                listCartItems.removeItem(position);
                                cartAdapter.notifyItemRemoved(position);
                                cartAdapter.notifyItemRangeChanged(position, listCartItems.getItemCount());
                            }
                        }
                        updateTotalPrice();
                        updateCartVisibility();
                        Toast.makeText(this, getString(R.string.delete_all_noti), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.dialog_cancel), null)
                    .show();
        });

        // Sự kiện nút quay lại trang sản phẩm
        Button backToProductsButton = findViewById(R.id.back_to_products_button);
        backToProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCart.this, Products.class);
            startActivity(intent);
            finish();
        });

        // Sự kiện nút mua sắm ngay
        Button shopNowButton = findViewById(R.id.shop_now_button);
        shopNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCart.this, Products.class);
            startActivity(intent);
            finish();
        });

        updateTotalPrice();
    }

    private void loadCartFromFirestore() {
        String accountID = auth.getCurrentUser().getUid();
        db.collection("carts").document(accountID).collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listCartItems.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String productID = doc.getString("productID");
                        if (productID == null) {
                            Log.e("ProductCart", "Null ProductID in Firestore document: " + doc.getId());
                            FirebaseCrashlytics.getInstance().recordException(new Exception("Null ProductID in Firestore: " + doc.getId()));
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
                                doc.getBoolean("selected") != null ? doc.getBoolean("selected") : false
                        );
                        listCartItems.addItem(item);
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateCartVisibility();
                    updateTotalPrice();
                    Log.d("ProductCart", "Loaded " + listCartItems.getItemCount() + " items from Firestore");
                });
    }

    private void updateTotalPrice() {
        double total = listCartItems.calculateTotalPrice();
        TextView totalAmountText = findViewById(R.id.total_amount_text);
        totalAmountText.setText(getString(R.string.product_cart_total_amount_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency));
    }

    private void updateCartVisibility() {
        int itemCount = listCartItems.getItemCount();
        Log.d("ProductCart", "Cart items count: " + itemCount);
        if (itemCount == 0) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            txtDeleteAll.setVisibility(View.GONE);
            bottomSectionLayout.setVisibility(View.GONE);
            emptyCartLayout.requestLayout();
            Log.d("ProductCart", "Showing empty cart layout");
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            txtDeleteAll.setVisibility(View.VISIBLE);
            bottomSectionLayout.setVisibility(View.VISIBLE);
            recyclerView.requestLayout();
            Log.d("ProductCart", "Showing cart with items");
        }
    }

    @Override
    public void onItemChanged() {
        updateTotalPrice();
        updateCartVisibility();
    }
}