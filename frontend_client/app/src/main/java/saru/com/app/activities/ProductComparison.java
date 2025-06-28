package saru.com.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.List;

import saru.com.app.R;
import saru.com.app.models.image;
import saru.com.app.models.ProductComparisonItems;

public class ProductComparison extends BaseActivity {
    private TableLayout comparisonTable;
    private ScrollView comparisonScrollView;
    private LinearLayout emptyComparisonLayout;
    private MaterialButton buttonClearAll;
    private ImageButton[] deleteButtons;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private FirebaseFirestore db;

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_compare;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_comparison);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind views
        comparisonTable = findViewById(R.id.comparison_table);
        comparisonScrollView = findViewById(R.id.comparison_scroll_view);
        emptyComparisonLayout = findViewById(R.id.empty_comparison_layout);
        buttonClearAll = findViewById(R.id.button_clear_all);
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        MaterialButton shopNowButton = findViewById(R.id.shop_now_button);

        // Initialize delete buttons
        deleteButtons = new ImageButton[] {
                findViewById(R.id.delete_product_1),
                findViewById(R.id.delete_product_2),
                findViewById(R.id.delete_product_3)
        };

        // Kiểm tra ánh xạ views
        if (comparisonTable == null || comparisonScrollView == null || emptyComparisonLayout == null ||
                buttonClearAll == null || btnBack == null || shopNowButton == null) {
            Log.e("ProductComparison", "One or more views not found");
            Toast.makeText(this, "Lỗi giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cập nhật giao diện
        updateComparisonTable();
        updateVisibility();

        // Handle back button
        btnBack.setOnClickListener(v -> finish());

        // Handle shop now button
        shopNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductComparison.this, Products.class);
            startActivity(intent);
            finish();
        });

        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent = new Intent(ProductComparison.this, ProductCart.class);
                startActivity(intent);
            });
        }

        // Handle clear all button
        buttonClearAll.setOnClickListener(v -> {
            List<ProductComparisonItems> items = ProductComparisonItems.getComparisonItems();
            if (items.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_items_selected), Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_delete_all_comparison_title))
                    .setMessage(getString(R.string.dialog_delete_all_comparison_message, items.size()))
                    .setPositiveButton(getString(R.string.dialog_confirm_delete), (dialog, which) -> {
                        ProductComparisonItems.clear();
                        updateComparisonTable();
                        updateVisibility();
                        Toast.makeText(this, getString(R.string.delete_comparison_noti), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.dialog_cancel), null)
                    .show();
        });

        // Handle individual delete buttons
        for (int i = 0; i < deleteButtons.length; i++) {
            final int index = i;
            deleteButtons[i].setOnClickListener(v -> {
                List<ProductComparisonItems> items = ProductComparisonItems.getComparisonItems();
                if (index < items.size()) {
                    ProductComparisonItems item = items.get(index);
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.dialog_delete_single_title))
                            .setMessage(getString(R.string.dialog_delete_single_comparison_message, item.getProductName()))
                            .setPositiveButton(getString(R.string.dialog_confirm_delete), (dialog, which) -> {
                                ProductComparisonItems.removeItem(index);
                                updateComparisonTable();
                                updateVisibility();
                                Toast.makeText(this, getString(R.string.delete_comparison_noti), Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(getString(R.string.dialog_cancel), null)
                            .show();
                }
            });
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();
    }

    private void updateComparisonTable() {
        List<ProductComparisonItems> items = ProductComparisonItems.getComparisonItems();

        // Lấy các hàng
        TableRow imageRow = (TableRow) comparisonTable.getChildAt(0);
        TableRow nameRow = (TableRow) comparisonTable.getChildAt(1);
        TableRow brandRow = (TableRow) comparisonTable.getChildAt(2);
        TableRow alcoholRow = (TableRow) comparisonTable.getChildAt(3);
        TableRow volumeRow = (TableRow) comparisonTable.getChildAt(4);
        TableRow wineTypeRow = (TableRow) comparisonTable.getChildAt(5);
        TableRow ingredientsRow = (TableRow) comparisonTable.getChildAt(6);
        TableRow tasteRow = (TableRow) comparisonTable.getChildAt(7);
        TableRow priceRow = (TableRow) comparisonTable.getChildAt(8);
        TableRow deleteRow = (TableRow) comparisonTable.getChildAt(9);

        // Kiểm tra ánh xạ các hàng
        if (imageRow == null || nameRow == null || brandRow == null || alcoholRow == null ||
                volumeRow == null || wineTypeRow == null || ingredientsRow == null ||
                tasteRow == null || priceRow == null || deleteRow == null) {
            Log.e("ProductComparison", "One or more TableRows not found");
            Toast.makeText(this, "Lỗi giao diện bảng so sánh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật từng cột
        for (int i = 1; i <= 3; i++) {
            ImageView imageView = (ImageView) imageRow.getChildAt(i);
            TextView nameText = (TextView) nameRow.getChildAt(i);
            TextView brandText = (TextView) brandRow.getChildAt(i);
            TextView alcoholText = (TextView) alcoholRow.getChildAt(i);
            TextView volumeText = (TextView) volumeRow.getChildAt(i);
            TextView wineTypeText = (TextView) wineTypeRow.getChildAt(i);
            TextView ingredientsText = (TextView) ingredientsRow.getChildAt(i);
            TextView tasteText = (TextView) tasteRow.getChildAt(i);
            TextView priceText = (TextView) priceRow.getChildAt(i);
            ImageButton deleteButton = (ImageButton) deleteRow.getChildAt(i);

            if (i - 1 < items.size()) {
                ProductComparisonItems item = items.get(i - 1);
                // Load image using Glide
                String imageUrl = item.getImageResId();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.img_saru_cup)
                            .error(R.drawable.ic_ver_fail)
                            .into(imageView);
                } else {
                    Log.e("ProductComparison", "No image URL for product: " + item.getProductName());
                    imageView.setImageResource(R.drawable.ic_ver_fail);
                }

                // Hiển thị và log dữ liệu
                nameText.setText(item.getProductName() != null ? item.getProductName() : "N/A");
                String brand = item.getProductBrand();
                Log.d("ProductComparison", "Product: " + item.getProductName() + ", Brand: " + brand);
                brandText.setText(brand != null && !brand.isEmpty() ? brand : "N/A");
                alcoholText.setText(item.getAlcoholStrength() != null ? item.getAlcoholStrength() : "N/A");
                volumeText.setText(item.getNetContent() != null ? item.getNetContent() : "N/A");
                wineTypeText.setText(item.getWineType() != null ? item.getWineType() : "N/A");
                ingredientsText.setText(item.getIngredients() != null ? item.getIngredients() : "N/A");
                tasteText.setText(item.getProductTaste() != null ? item.getProductTaste() : "N/A");
                priceText.setText(item.getProductPrice() > 0 ? formatter.format(item.getProductPrice()) + getString(R.string.product_cart_currency) : "N/A");
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                imageView.setImageDrawable(null);
                nameText.setText("");
                brandText.setText("");
                alcoholText.setText("");
                volumeText.setText("");
                wineTypeText.setText("");
                ingredientsText.setText("");
                tasteText.setText("");
                priceText.setText("");
                deleteButton.setVisibility(View.GONE);
            }
        }
    }

    private void updateVisibility() {
        int itemCount = ProductComparisonItems.getItemCount();
        Log.d("ProductComparison", "Item count: " + itemCount);
        if (itemCount == 0) {
            emptyComparisonLayout.setVisibility(View.VISIBLE);
            comparisonScrollView.setVisibility(View.GONE);
            buttonClearAll.setVisibility(View.GONE);
            emptyComparisonLayout.requestLayout();
            Log.d("ProductComparison", "Showing empty comparison layout");
        } else {
            emptyComparisonLayout.setVisibility(View.GONE);
            comparisonScrollView.setVisibility(View.VISIBLE);
            buttonClearAll.setVisibility(View.VISIBLE);
            comparisonScrollView.requestLayout();
            Log.d("ProductComparison", "Showing comparison table");
        }
    }
}