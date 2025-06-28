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

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.connectors.CartAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.CartManager;
import saru.com.app.models.ListCartItems;
public class ProductCart extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private static final String TAG = "ProductCart";
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartLayout;
    private TextView txtDeleteAll;
    private LinearLayout bottomSectionLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView cartItemCountText;
    private ListCartItems listCartItems;

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

        ImageButton btn_noti = findViewById(R.id.btn_noti);
        btn_noti.setOnClickListener(v -> openNotification());


        // Khởi tạo các view
        recyclerView = findViewById(R.id.cart_recycler_view);
        emptyCartLayout = findViewById(R.id.empty_cart_layout);
        txtDeleteAll = findViewById(R.id.txtProductCart_DeleteAll);
        bottomSectionLayout = findViewById(R.id.bottom_section_layout);
        cartItemCountText = findViewById(R.id.cart_item_count);

        // Đăng ký badge
        if (cartItemCountText != null) {
            CartManager.getInstance().addBadgeView(cartItemCountText);
            Log.d(TAG, "Added badge view in onCreate");
        }

        // Khởi tạo RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, CartManager.getInstance(), this);
        recyclerView.setAdapter(cartAdapter);

        // Kiểm tra đăng nhập
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Khởi tạo CartManager và tải dữ liệu
        CartManager.getInstance().initialize(this, success -> {
            runOnUiThread(() -> {
                if (success) {
                    cartAdapter.notifyDataSetChanged();
                    updateCartVisibility();
                    updateTotalPrice();
                    Log.d(TAG, "Cart data loaded, updated UI in onCreate");
                } else {
                    Log.e(TAG, "Failed to load cart data");
                    Toast.makeText(this, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Sự kiện nút Back
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện checkbox chọn tất cả
        CheckBox selectAllCheckbox = findViewById(R.id.select_all_checkbox);
        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CartManager.getInstance().setAllSelected(isChecked);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
            updateCartVisibility();
            Log.d(TAG, "Select all checkbox changed: " + isChecked);
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
            for (CartItem item : CartManager.getInstance().getCartItems()) {
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
                        List<String> deletedProductIds = new ArrayList<>();
                        for (CartItem item : selectedItems) {

                            db.collection("carts").document(accountID).collection("items")
                                    .document(item.getProductID())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Successfully deleted item from Firestore: " + item.getProductName());
                                        deletedProductIds.add(item.getProductID());
                                        // Chỉ cập nhật giao diện sau khi xóa tất cả
                                        if (deletedProductIds.size() == selectedItems.size()) {
                                            recyclerView.post(() -> {
                                                CartManager.getInstance().loadCartItemsFromFirestore(success -> {
                                                    if (success) {
                                                        cartAdapter.notifyDataSetChanged();
                                                        updateTotalPrice();
                                                        updateCartVisibility();
                                                        Toast.makeText(this, getString(R.string.delete_all_noti), Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "UI updated after delete all, new item count: " + CartManager.getInstance().getItemCount());
                                                    } else {
                                                        Log.e(TAG, "Failed to sync cart items after delete all");
                                                        cartAdapter.notifyDataSetChanged();
                                                        updateTotalPrice();
                                                        updateCartVisibility();
                                                    }
                                                });
                                            });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting item from Firestore: " + item.getProductName() + ", Error: " + e.getMessage());
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                    });

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

        // Xử lý WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private int findItemIndex(String productId) {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProductID().equals(productId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            CartManager.getInstance().setUser(auth.getCurrentUser().getUid());
            CartManager.getInstance().initialize(this, success -> {
                runOnUiThread(() -> {
                    if (success) {
                        cartAdapter.notifyDataSetChanged();
                        updateCartVisibility();
                        updateTotalPrice();
                        Log.d(TAG, "Cart data reloaded in onStart, item count: " + CartManager.getInstance().getItemCount());
                    } else {
                        Log.e(TAG, "Failed to reload cart data in onStart");
                        Toast.makeText(this, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    }

                    cartAdapter.notifyDataSetChanged();
                    updateCartVisibility();
                    updateTotalPrice();
                    Log.d("ProductCart", "Loaded " + listCartItems.getItemCount() + " items from Firestore");

                });
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cartItemCountText != null) {
            CartManager.getInstance().removeBadgeView(cartItemCountText);
            Log.d(TAG, "Removed badge view in onStop");
        }
    }
    private void openNotification() {
        Intent intent = new Intent(this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }
    private void updateTotalPrice() {
        double total = CartManager.getInstance().calculateTotalPrice();
        TextView totalAmountText = findViewById(R.id.total_amount_text);
        totalAmountText.setText(getString(R.string.product_cart_total_amount_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency));
        Log.d(TAG, "Updated total price: " + total);
    }

    private void updateCartVisibility() {
        int itemCount = CartManager.getInstance().getItemCount();
        Log.d(TAG, "Updating cart visibility, item count: " + itemCount);
        if (itemCount == 0) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            txtDeleteAll.setVisibility(View.GONE);
            bottomSectionLayout.setVisibility(View.GONE);
            emptyCartLayout.requestLayout();
            Log.d(TAG, "Showing empty cart layout");
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            txtDeleteAll.setVisibility(View.VISIBLE);
            bottomSectionLayout.setVisibility(View.VISIBLE);
            recyclerView.requestLayout();
            Log.d(TAG, "Showing cart with items");
        }
    }

    @Override
    public void onItemChanged() {
        runOnUiThread(() -> {
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
            updateCartVisibility();
            Log.d(TAG, "onItemChanged triggered, UI updated");
        });
    }
}