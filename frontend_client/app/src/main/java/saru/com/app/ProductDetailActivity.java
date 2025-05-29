package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import saru.com.app.connectors.ProductAdapter;
import saru.com.app.models.Product;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.graphics.Rect;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tắt EdgeToEdge để kiểm tra, sau đó sẽ xử lý thủ công
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        // Bind views
        TextView productName = findViewById(R.id.product_name);
        TextView productBrand = findViewById(R.id.product_brand);
        RatingBar productRating = findViewById(R.id.product_rating);
        TextView stockStatus = findViewById(R.id.stock_status);
        TextView productPrice = findViewById(R.id.product_price);
        TextView alcoholStrength = findViewById(R.id.alcohol_strength);
        TextView netContent = findViewById(R.id.net_content);
        TextView wineType = findViewById(R.id.wine_type);
        TextView ingredients = findViewById(R.id.ingredients);
        TextView productDescription = findViewById(R.id.product_description);
        ImageButton btnBack = findViewById(R.id.btn_back_arrow);
        Button addToCartButton = findViewById(R.id.add_to_cart_button);
        Button compareButton = findViewById(R.id.compare_button);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_products);

        // Get product data from Intent
        Intent intent = getIntent();
        Product product = (Product) intent.getSerializableExtra("product");

        // Bind product data
        if (product != null) {
            productName.setText(product.getProductName());
            productBrand.setText(product.getProductBrand());
            productRating.setRating(product.getCustomerRating());
            stockStatus.setText(product.getStockStatus());
            productPrice.setText(product.getProductPrice());
            alcoholStrength.setText(product.getAlcoholStrength());
            netContent.setText(product.getNetContent());
            wineType.setText(product.getWineType());
            ingredients.setText(product.getIngredients());
            productDescription.setText(product.getProductDescription());
        }

        // Set up RecyclerView for Related Products (2 items per row, horizontal scroll)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new ProductAdapter());

        // Thêm ItemDecoration để tạo khoảng trống giữa các item
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Handle back button
        btnBack.setOnClickListener(v -> finish());

        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent2 = new Intent(ProductDetailActivity.this, ProductCart.class);
                startActivity(intent2);
            });
        }

        // Handle Add to Cart button (placeholder logic)
        addToCartButton.setOnClickListener(v -> {
            // Add to cart logic here
        });

        // Handle Compare button (placeholder logic)
        compareButton.setOnClickListener(v -> {
            // Compare logic here
        });

        // Xử lý insets để đảm bảo thanh công cụ hiển thị
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

                if (layoutManager.getOrientation() == GridLayoutManager.HORIZONTAL) {
                    if (position < spanCount) {
                        outRect.top = 0; // Không thêm khoảng cách ở hàng đầu tiên
                    }
                    if (position % spanCount == 0) {
                        outRect.left = 0; // Không thêm khoảng cách ở cột đầu tiên bên trái
                    }
                    if (position % spanCount == spanCount - 1) {
                        outRect.right = 0; // Không thêm khoảng cách ở cột cuối cùng bên phải
                    }
                } else {
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
}