package saru.com.app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.graphics.Rect;

public class Products extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ProductAdapter());

        // Thêm ItemDecoration để tạo khoảng trống giữa các item
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Nút Filter
        ImageButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFilterDialog() {
        Dialog filterDialog = new Dialog(this, android.R.style.Theme_NoTitleBar);
        filterDialog.setContentView(R.layout.filter_dialog);

        // Điều chỉnh kích thước dialog (chiếm 80% chiều rộng màn hình)
        Window window = filterDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setGravity(android.view.Gravity.LEFT); // Đặt dialog ở bên trái
        }

        // Áp dụng animation
        filterDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        // Khởi tạo các thành phần trong dialog
        Spinner spinnerCategory = filterDialog.findViewById(R.id.spinner_category);
        Spinner spinnerSortBy = filterDialog.findViewById(R.id.spinner_sort_by);
        Spinner spinnerBrand = filterDialog.findViewById(R.id.spinner_brand);
        Spinner spinnerVolume = filterDialog.findViewById(R.id.spinner_volume);
        Spinner spinnerWineType = filterDialog.findViewById(R.id.spinner_wine_type);
        EditText editPriceMin = filterDialog.findViewById(R.id.edit_price_min);
        EditText editPriceMax = filterDialog.findViewById(R.id.edit_price_max);
        CheckBox checkboxBestSelling = filterDialog.findViewById(R.id.checkbox_best_selling);
        CheckBox checkboxOnSale = filterDialog.findViewById(R.id.checkbox_on_sale);
        Button buttonApplyFilter = filterDialog.findViewById(R.id.button_apply_filter);
        TextView textResetFilter = filterDialog.findViewById(R.id.text_reset_filter);

        // Populate các Spinner
        String[] categories = {"Tay Bac Wine", "Set wine gift", "Wine accessories"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] sortOptions = {"Low to High", "High to Low"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(sortAdapter);

        String[] brands = {"All", "Mầm Distillery", "Sauchua Spirit Sapa", "CamTa", "Rượu Zuji", "Việt Moutains", "Công Ty Rượu Vodka Cá Sấu"};
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brands);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrand.setAdapter(brandAdapter);

        String[] volumes = {"All", "500ml", "350ml", "250ml"};
        ArrayAdapter<String> volumeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, volumes);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVolume.setAdapter(volumeAdapter);

        String[] wineTypes = {"Infused Wine", "Distilled Wine", "Fermented Wine"};
        ArrayAdapter<String> wineTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wineTypes);
        wineTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWineType.setAdapter(wineTypeAdapter);

        // Xử lý nút Apply Filter
        buttonApplyFilter.setOnClickListener(v -> {
            // Lấy giá trị từ các thành phần
            String selectedCategory = spinnerCategory.getSelectedItem().toString();
            String selectedSortBy = spinnerSortBy.getSelectedItem().toString();
            String selectedBrand = spinnerBrand.getSelectedItem().toString();
            String selectedVolume = spinnerVolume.getSelectedItem().toString();
            String selectedWineType = spinnerWineType.getSelectedItem().toString();
            String priceMin = editPriceMin.getText().toString();
            String priceMax = editPriceMax.getText().toString();
            boolean isBestSelling = checkboxBestSelling.isChecked();
            boolean isOnSale = checkboxOnSale.isChecked();

            // TODO: Áp dụng logic lọc sản phẩm tại đây
            // Ví dụ: Cập nhật adapter của RecyclerView dựa trên các giá trị đã chọn

            filterDialog.dismiss();
        });

        // Xử lý nút Reset Filter
        textResetFilter.setOnClickListener(v -> {
            // Đặt lại các trường về giá trị mặc định
            spinnerCategory.setSelection(0); // Chọn mục đầu tiên
            spinnerSortBy.setSelection(0);
            spinnerBrand.setSelection(0);
            spinnerVolume.setSelection(0);
            spinnerWineType.setSelection(0);
            editPriceMin.setText("");
            editPriceMax.setText("");
            checkboxBestSelling.setChecked(false);
            checkboxOnSale.setChecked(false);

            // TODO: Đặt lại danh sách sản phẩm về trạng thái ban đầu
            // Ví dụ: Cập nhật adapter của RecyclerView để hiển thị tất cả sản phẩm

            filterDialog.dismiss();
        });

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

            // Điều chỉnh cho GridLayoutManager để không thêm khoảng cách dư thừa ở cạnh ngoài
            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
                int position = parent.getChildAdapterPosition(view);
                int spanCount = layoutManager.getSpanCount();

                if (position % spanCount == 0) {
                    outRect.left = 0; // Không thêm khoảng cách ở cột đầu tiên bên trái
                }
                if (position % spanCount == spanCount - 1) {
                    outRect.right = 0; // Không thêm khoảng cách ở cột cuối cùng bên phải
                }
                if (position < spanCount) {
                    outRect.top = 0; // Không thêm khoảng cách ở hàng đầu tiên
                }
            }
        }
    }
}