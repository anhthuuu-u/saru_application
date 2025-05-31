package saru.com.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.graphics.Rect;

import saru.com.app.connectors.ProductAdapter;

public class Products extends BaseActivity {
    private ProductAdapter productAdapter;
    // Lưu trữ adapter để sử dụng lại
    SearchView searchBar;

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_product;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(); // Khởi tạo adapter
        recyclerView.setAdapter(productAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        ImageButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        TextView txtFilter = findViewById(R.id.txtFilter);
        txtFilter.setOnClickListener(v -> showFilterDialog());

        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent = new Intent(Products.this, ProductCart.class);
                startActivity(intent);
            });
        }

        ImageButton btnBackArrow = findViewById(R.id.btn_back_arrow);
        if (btnBackArrow != null) {
            btnBackArrow.setOnClickListener(v -> {
                Intent intent = new Intent(Products.this, Homepage.class);
                startActivity(intent);
                finish();
            });
        }
        // Tìm SearchView
        searchBar = findViewById(R.id.search_bar);
        if (searchBar == null) {
            throw new IllegalStateException("SearchView with ID 'search_bar' not found in activity_homepage.xml");
        }
        Log.d("Homepage", "SearchView found");

        // Cấu hình SearchView để không tự động focus khi mở activity
        searchBar.setIconifiedByDefault(false); // Giữ SearchView luôn mở
        searchBar.setFocusable(false); // Không cho phép focus tự động
        searchBar.setFocusableInTouchMode(false); // Không focus khi chạm nếu không có sự kiện

        // Xử lý sự kiện khi nhấn vào SearchView
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setFocusable(true); // Cho phép focus khi nhấn
                searchBar.setFocusableInTouchMode(true); // Cho phép focus khi chạm
                searchBar.requestFocus(); // Focus chuột khi nhấn
                Log.d("Homepage", "SearchView focused on click");
            }
        });

        setupBottomNavigation();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFilterDialog() {
        Dialog filterDialog = new Dialog(this, android.R.style.Theme_NoTitleBar);
        filterDialog.setContentView(R.layout.filter_dialog);

        // Điều chỉnh kích thước dialog
        Window window = filterDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.75);
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setGravity(android.view.Gravity.LEFT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        filterDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        // Khởi tạo các thành phần trong dialog
        LinearLayout dropdownCategory = filterDialog.findViewById(R.id.dropdown_category);
        TextView textCategory = filterDialog.findViewById(R.id.text_category);
        LinearLayout dropdownSortBy = filterDialog.findViewById(R.id.dropdown_sort_by);
        TextView textSortBy = filterDialog.findViewById(R.id.text_sort_by);
        LinearLayout dropdownBrand = filterDialog.findViewById(R.id.dropdown_brand);
        TextView textBrand = filterDialog.findViewById(R.id.text_brand);
        LinearLayout dropdownVolume = filterDialog.findViewById(R.id.dropdown_volume);
        TextView textVolume = filterDialog.findViewById(R.id.text_volume);
        LinearLayout dropdownWineType = filterDialog.findViewById(R.id.dropdown_wine_type);
        TextView textWineType = filterDialog.findViewById(R.id.text_wine_type);
        EditText editPriceMin = filterDialog.findViewById(R.id.edit_price_min);
        EditText editPriceMax = filterDialog.findViewById(R.id.edit_price_max);
        CheckBox checkboxBestSelling = filterDialog.findViewById(R.id.checkbox_best_selling);
        CheckBox checkboxOnSale = filterDialog.findViewById(R.id.checkbox_on_sale);
        Button buttonApplyFilter = filterDialog.findViewById(R.id.button_apply_filter);
        TextView textResetFilter = filterDialog.findViewById(R.id.text_reset_filter);

        // Danh sách tùy chọn
        String[] categories = {"Tay Bac Wine", "Set wine gift", "Wine accessories"};
        String[] sortOptions = {"Low to High", "High to Low"};
        String[] brands = {"All", "Mầm Distillery", "Sauchua Spirit Sapa", "CamTa", "Rượu Zuji", "Việt Moutains", "Công Ty Rượu Vodka Cá Sấu"};
        String[] volumes = {"All", "500ml", "350ml", "250ml"};
        String[] wineTypes = {"Infused Wine", "Distilled Wine", "Fermented Wine"};

        // Xử lý dropdown Category
        dropdownCategory.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownCategory);
            Menu menu = popupMenu.getMenu();
            for (String category : categories) {
                menu.add(category);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                textCategory.setText(item.getTitle());
                return true;
            });

            popupMenu.getMenuInflater().inflate(R.menu.dummy_menu, menu);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                View customView = getLayoutInflater().inflate(R.layout.popup_menu_item, null);
                TextView textView = customView.findViewById(R.id.popup_menu_item_text);
                textView.setText(item.getTitle());
                item.setActionView(customView);
            }

            popupMenu.show();
        });

        // Xử lý dropdown Sort By
        dropdownSortBy.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownSortBy);
            Menu menu = popupMenu.getMenu();
            for (String sortOption : sortOptions) {
                menu.add(sortOption);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                textSortBy.setText(item.getTitle());
                return true;
            });

            popupMenu.getMenuInflater().inflate(R.menu.dummy_menu, menu);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                View customView = getLayoutInflater().inflate(R.layout.popup_menu_item, null);
                TextView textView = customView.findViewById(R.id.popup_menu_item_text);
                textView.setText(item.getTitle());
                item.setActionView(customView);
            }

            popupMenu.show();
        });

        // Xử lý dropdown Brand
        dropdownBrand.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownBrand);
            Menu menu = popupMenu.getMenu();
            for (String brand : brands) {
                menu.add(brand);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                textBrand.setText(item.getTitle());
                return true;
            });

            popupMenu.getMenuInflater().inflate(R.menu.dummy_menu, menu);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                View customView = getLayoutInflater().inflate(R.layout.popup_menu_item, null);
                TextView textView = customView.findViewById(R.id.popup_menu_item_text);
                textView.setText(item.getTitle());
                item.setActionView(customView);
            }

            popupMenu.show();
        });

        // Xử lý dropdown Volume
        dropdownVolume.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownVolume);
            Menu menu = popupMenu.getMenu();
            for (String volume : volumes) {
                menu.add(volume);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                textVolume.setText(item.getTitle());
                return true;
            });

            popupMenu.getMenuInflater().inflate(R.menu.dummy_menu, menu);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                View customView = getLayoutInflater().inflate(R.layout.popup_menu_item, null);
                TextView textView = customView.findViewById(R.id.popup_menu_item_text);
                textView.setText(item.getTitle());
                item.setActionView(customView);
            }

            popupMenu.show();
        });

        // Xử lý dropdown Wine Type
        dropdownWineType.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownWineType);
            Menu menu = popupMenu.getMenu();
            for (String wineType : wineTypes) {
                menu.add(wineType);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                textWineType.setText(item.getTitle());
                return true;
            });

            popupMenu.getMenuInflater().inflate(R.menu.dummy_menu, menu);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                View customView = getLayoutInflater().inflate(R.layout.popup_menu_item, null);
                TextView textView = customView.findViewById(R.id.popup_menu_item_text);
                textView.setText(item.getTitle());
                item.setActionView(customView);
            }

            popupMenu.show();
        });

        // Xử lý nút Apply Filter
        buttonApplyFilter.setOnClickListener(v -> {
            // Lấy giá trị từ các thành phần
            String selectedCategory = textCategory.getText().toString().trim();
            String selectedSortBy = textSortBy.getText().toString().trim();
            String selectedBrand = textBrand.getText().toString().trim();
            String selectedVolume = textVolume.getText().toString().trim();
            String selectedWineType = textWineType.getText().toString().trim();
            String priceMin = editPriceMin.getText().toString().trim();
            String priceMax = editPriceMax.getText().toString().trim();
            boolean isBestSelling = checkboxBestSelling.isChecked();
            boolean isOnSale = checkboxOnSale.isChecked();

            // Chuẩn bị các tham số lọc
            String filterCategory = selectedCategory.isEmpty() ? null : selectedCategory;
            String filterSortBy = selectedSortBy.isEmpty() ? null : selectedSortBy;
            String filterBrand = selectedBrand.isEmpty() ? null : selectedBrand;
            String filterVolume = selectedVolume.isEmpty() ? null : selectedVolume;
            String filterWineType = selectedWineType.isEmpty() ? null : selectedWineType;
            Double filterPriceMin = null;
            try {
                filterPriceMin = priceMin.isEmpty() ? null : Double.parseDouble(priceMin);
            } catch (NumberFormatException e) {
                filterPriceMin = null; // Bỏ qua nếu không hợp lệ
            }
            Double filterPriceMax = null;
            try {
                filterPriceMax = priceMax.isEmpty() ? null : Double.parseDouble(priceMax);
            } catch (NumberFormatException e) {
                filterPriceMax = null; // Bỏ qua nếu không hợp lệ
            }

            // Áp dụng logic lọc sản phẩm
            productAdapter.filterProducts(
                    filterCategory,
                    filterSortBy,
                    filterBrand,
                    filterVolume,
                    filterWineType,
                    filterPriceMin,
                    filterPriceMax,
                    isBestSelling,
                    isOnSale
            );

            // Đóng dialog sau khi áp dụng lọc
            filterDialog.dismiss();
        });

        // Xử lý nút Reset Filter
        textResetFilter.setOnClickListener(v -> {
            // Đặt lại các trường về trạng thái ban đầu
            textCategory.setText(getString(R.string.filter_product_dialog_category)); // Hiển thị lại "Category"
            textSortBy.setText(getString(R.string.title_product_dialog_sort)); // Hiển thị lại "Sort By"
            textBrand.setText(getString(R.string.title_filter_product_dialog_brand)); // Hiển thị lại "Brand"
            textVolume.setText(getString(R.string.title_filter_product_dialog_volume)); // Hiển thị lại "Volume"
            textWineType.setText(getString(R.string.title_filter_product_dialog_wine_type)); // Hiển thị lại "Wine Type"
            editPriceMin.setText(""); // Reset giá trị min
            editPriceMax.setText(""); // Reset giá trị max
            checkboxBestSelling.setChecked(false); // Reset checkbox
            checkboxOnSale.setChecked(false); // Reset checkbox
            // Không đóng dialog, giữ mở để người dùng tiếp tục chỉnh sửa
        });

        // Thêm logic đóng dialog khi nhấn ra ngoài
        filterDialog.setCanceledOnTouchOutside(true);

        // Hiển thị dialog
        filterDialog.show();
    }

    // Custom ItemDecoration để thêm khoảng trống
    private static class ItemSpacingDecoration extends ItemDecoration {
        private final int spacing;

        public ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.top = spacing;
            outRect.bottom = spacing;

            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
                int position = parent.getChildAdapterPosition(view);
                int spanCount = layoutManager.getSpanCount();

                if (position % spanCount == 0) {
                    outRect.left = 0;
                }
                if (position % spanCount == spanCount - 1) {
                    outRect.right = 0;
                }
                if (position < spanCount) {
                    outRect.top = 0;
                }
            }
        }
    }
}