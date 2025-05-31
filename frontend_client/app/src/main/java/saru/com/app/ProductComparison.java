package saru.com.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ProductComparison extends BaseActivity {
    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_compare; // Trả về ID của mục menu tương ứng
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_comparison);

        // Bind views
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        Button btnClearAll = findViewById(R.id.button_clear_all);

        // Handle back button
        btnBack.setOnClickListener(v -> finish());

        // Handle clear all button (placeholder logic)
        btnClearAll.setOnClickListener(v -> {
            // TODO: Xóa tất cả sản phẩm đang so sánh
            // Ví dụ: Xóa dữ liệu trong TableLayout hoặc cập nhật lại UI
        });
        setupBottomNavigation();

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}