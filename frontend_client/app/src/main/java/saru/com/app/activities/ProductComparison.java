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

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.List;

import saru.com.app.R;
import saru.com.app.models.ProductComparisonItems;

public class ProductComparison extends BaseActivity {
    private TableLayout comparisonTable;
    private ScrollView comparisonScrollView;
    private LinearLayout emptyComparisonLayout;
    private MaterialButton buttonClearAll;
    private ImageButton[] deleteButtons;
    private DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_compare;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_comparison);

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

        // Thêm dữ liệu mẫu
        initializeSampleData();

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
                            .setMessage(getString(R.string.dialog_delete_single_comparison_message, item.getName()))
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

    private void initializeSampleData() {
        // Xóa dữ liệu cũ để tránh trùng lặp
        ProductComparisonItems.clear();
        // Thêm dữ liệu mẫu
        ProductComparisonItems.addItem(new ProductComparisonItems(
                "Peach Wine", "Brand A", "12%", "750ml", "Fruit Wine",
                "Peach, Sugar", "Sweet", 200000, R.mipmap.img_wine));
        ProductComparisonItems.addItem(new ProductComparisonItems(
                "Rice Wine", "Brand B", "15%", "500ml", "Grain Wine",
                "Rice, Yeast", "Strong", 150000, R.mipmap.img_wine));
        ProductComparisonItems.addItem(new ProductComparisonItems(
                "Apple Wine", "Brand C", "10%", "700ml", "Fruit Wine",
                "Apple, Honey", "Fruity", 180000, R.mipmap.img_wine));
        Log.d("ProductComparison", "Added sample data: " + ProductComparisonItems.getItemCount() + " items");
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
                imageView.setImageResource(item.getImageResId());
                nameText.setText(item.getName());
                brandText.setText(item.getBrand());
                alcoholText.setText(item.getAlcohol());
                volumeText.setText(item.getVolume());
                wineTypeText.setText(item.getWineType());
                ingredientsText.setText(item.getIngredients());
                tasteText.setText(item.getTaste());
                priceText.setText(formatter.format(item.getPrice()) + getString(R.string.product_cart_currency));
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