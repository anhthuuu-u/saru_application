package saru.com.app;

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

import java.util.ArrayList;
import java.util.List;

import saru.com.app.connectors.CartAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;

public class ProductCart extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private ListCartItems listCartItems;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartLayout;
    private TextView txtDeleteAll;
    private LinearLayout bottomSectionLayout;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_cart);

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

        // Thêm dữ liệu mẫu
        List<CartItem> sampleItems = getSampleCartItems();
        Log.d("ProductCart", "Sample items size: " + (sampleItems != null ? sampleItems.size() : "null"));
        if (sampleItems != null) {
            for (CartItem item : sampleItems) {
                listCartItems.addItem(item);
            }
        } else {
            Log.e("ProductCart", "getSampleCartItems() returned null");
        }
        Log.d("ProductCart", "Cart items size after adding: " + listCartItems.getCartItems().size());

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
            double total = listCartItems.calculateTotalPrice();
            Toast.makeText(this, getString(R.string.product_cart_payment_toast_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductCart.this, TransactionCheckoutActivity.class);
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
                        for (CartItem item : selectedItems) {
                            int position = listCartItems.getCartItems().indexOf(item);
                            if (position != -1) {
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

        // Sự kiện nút quay lại trang sản phẩm từ bottom_section_layout
        Button backToProductsButton = findViewById(R.id.back_to_products_button);
        backToProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCart.this, Products.class);
            startActivity(intent);
            finish();
        });

        // Sự kiện nút mua sắm ngay từ empty_cart_layout
        Button shopNowButton = findViewById(R.id.shop_now_button);
        shopNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCart.this, Products.class);
            startActivity(intent);
            finish();
        });

        updateTotalPrice();
    }

    private List<CartItem> getSampleCartItems() {
        List<CartItem> sampleItems = new ArrayList<>();
        try {
            sampleItems.add(new CartItem("Peach Wine", 200000, 1));
            sampleItems.add(new CartItem("Rice Wine", 150000, 2));
            sampleItems.add(new CartItem("Apple Wine", 180000, 1));
            sampleItems.add(new CartItem("Apricot Wine", 220000, 1));
            sampleItems.add(new CartItem("Nang Mo Wine", 220000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));
            sampleItems.add(new CartItem("Hoang Su Phi Blood Plum Wine", 392000, 1));
            Log.d("ProductCart", "Added 8 sample items to getSampleCartItems");
        } catch (Exception e) {
            Log.e("ProductCart", "Error adding sample items: " + e.getMessage());
        }
        return sampleItems;
    }

    private void updateTotalPrice() {
        double total = listCartItems.calculateTotalPrice();
        TextView totalAmountText = findViewById(R.id.total_amount_text);
        totalAmountText.setText(getString(R.string.product_cart_total_amount_label) + " " + String.format("%.0f", total) + getString(R.string.product_cart_currency));
    }

    private void updateCartVisibility() {
        int itemCount = listCartItems.getCartItems().size();
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