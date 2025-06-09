package saru.com.app.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import saru.com.app.R;
import saru.com.app.connectors.CustomerReviewAdapter;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.models.CustomerReviewList;
import saru.com.app.models.CustomerReviews;
import saru.com.app.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private LinearLayout productDetailsContainer;
    private RecyclerView recyclerCustomerReviews, recyclerViewProducts;
    private TextView showProductDetails, showCustomerReviews;
    private CustomerReviewAdapter customerReviewAdapter;
    private CustomerReviewList customerReviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        recyclerViewProducts = findViewById(R.id.recycler_view_products);
        productDetailsContainer = findViewById(R.id.product_details_container);
        recyclerCustomerReviews = findViewById(R.id.recycler_customer_reviews);
        showProductDetails = findViewById(R.id.show_product_details);
        showCustomerReviews = findViewById(R.id.show_customer_reviews);

        // Kiểm tra null cho các view quan trọng
        if (recyclerViewProducts == null) {
            Log.e("ProductDetailActivity", "recycler_view_products not found in layout!");
            return;
        }
        if (recyclerCustomerReviews == null) {
            Log.e("ProductDetailActivity", "recycler_customer_reviews not found in layout!");
            return;
        }
        if (productDetailsContainer == null) {
            Log.e("ProductDetailActivity", "product_details_container not found in layout!");
            return;
        }
        if (showProductDetails == null) {
            Log.e("ProductDetailActivity", "show_product_details not found in layout!");
            return;
        }
        if (showCustomerReviews == null) {
            Log.e("ProductDetailActivity", "show_customer_reviews not found in layout!");
            return;
        }

        // Get product data from Intent
        Intent intent = getIntent();
        Product product = (Product) intent.getSerializableExtra("product");

        // Bind product data
        if (product != null) {
            if (productName != null) productName.setText(product.getProductName());
            if (productBrand != null) productBrand.setText(product.getProductBrand());
            if (productRating != null) productRating.setRating(product.getCustomerRating());
            if (stockStatus != null) stockStatus.setText(product.getStockStatus());
            if (productPrice != null) productPrice.setText(product.getProductPrice());
            if (alcoholStrength != null) alcoholStrength.setText(product.getAlcoholStrength());
            if (netContent != null) netContent.setText(product.getNetContent());
            if (wineType != null) wineType.setText(product.getWineType());
            if (ingredients != null) ingredients.setText(product.getIngredients());
            if (productDescription != null) productDescription.setText(product.getProductDescription());
        }

        // Set up RecyclerView for Related Products (Horizontal scroll)
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        recyclerViewProducts.setAdapter(new ProductAdapter());
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewProducts.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Set up RecyclerView for Customer Reviews
        recyclerCustomerReviews.setLayoutManager(new LinearLayoutManager(this));
        customerReviewList = new CustomerReviewList(); // Khởi tạo CustomerReviewList
        customerReviewAdapter = new CustomerReviewAdapter(new ArrayList<>());
        recyclerCustomerReviews.setAdapter(customerReviewAdapter);

        // Quan sát LiveData để tự động cập nhật UI cho Customer Reviews
        customerReviewList.getReviews().observe(this, new Observer<List<CustomerReviews>>() {
            @Override
            public void onChanged(List<CustomerReviews> reviews) {
                customerReviewAdapter.updateReviews(reviews);
                customerReviewAdapter.notifyDataSetChanged();
                adjustRecyclerViewHeight(); // Điều chỉnh chiều cao sau khi cập nhật dữ liệu
            }
        });

        // Điều chỉnh chiều cao RecyclerView dựa trên số lượng item
        adjustRecyclerViewHeight();

        // Căn giữa các item trong RecyclerView
        recyclerCustomerReviews.addItemDecoration(new CenterItemDecoration());

        // Show Product Details by default
        showProductDetails();

        // Handle Product Details button click
        showProductDetails.setOnClickListener(v -> showProductDetails());

        // Handle Customer Reviews button click
        showCustomerReviews.setOnClickListener(v -> showCustomerReviews());

        // Handle back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Handle Cart button
        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent2 = new Intent(ProductDetailActivity.this, ProductCart.class);
                startActivity(intent2);
            });
        }

        // Handle Add to Cart button
        if (addToCartButton != null) {
            addToCartButton.setOnClickListener(v -> {
                showSuccessDialog(getString(R.string.dialog_add_to_cart_success));
            });
        }

        // Handle Compare button
        if (compareButton != null) {
            compareButton.setOnClickListener(v -> {
                showSuccessDialog(getString(R.string.dialog_compare_success));
            });
        }

        // Handle window insets
        View contentView = findViewById(android.R.id.content);
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    // Hiển thị popup thông báo thành công
    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_success_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_ok_button), (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // Hiển thị chi tiết sản phẩm và ẩn đánh giá khách hàng
    private void showProductDetails() {
        productDetailsContainer.setVisibility(View.VISIBLE);
        recyclerCustomerReviews.setVisibility(View.GONE);
        showProductDetails.setTextColor(getResources().getColor(R.color.color_golden_yellow));
        showCustomerReviews.setTextColor(getResources().getColor(R.color.color_medium_gray));
    }

    // Hiển thị đánh giá khách hàng và ẩn chi tiết sản phẩm
    private void showCustomerReviews() {
        productDetailsContainer.setVisibility(View.GONE);
        recyclerCustomerReviews.setVisibility(View.VISIBLE);
        showCustomerReviews.setTextColor(getResources().getColor(R.color.color_golden_yellow));
        showProductDetails.setTextColor(getResources().getColor(R.color.color_medium_gray));
    }

    // Điều chỉnh chiều cao RecyclerView dựa trên số lượng item
    private void adjustRecyclerViewHeight() {
        if (customerReviewAdapter == null || recyclerCustomerReviews == null) return;

        int itemCount = customerReviewAdapter.getItemCount();
        int maxVisibleItems = 3; // Số item tối đa hiển thị mà không cần cuộn

        // Tạo một view mẫu để đo chiều cao của mỗi item
        View sampleView = customerReviewAdapter.createViewHolder(recyclerCustomerReviews, 0).itemView;
        sampleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int itemHeight = sampleView.getMeasuredHeight();

        // Tính chiều cao của RecyclerView
        int totalHeight;
        if (itemCount <= maxVisibleItems) {
            totalHeight = itemHeight * itemCount; // Hiển thị hết tất cả item
            recyclerCustomerReviews.setNestedScrollingEnabled(false); // Tắt cuộn nếu không cần
        } else {
            totalHeight = itemHeight * maxVisibleItems; // Hiển thị tối đa 3 item, còn lại cuộn
            recyclerCustomerReviews.setNestedScrollingEnabled(true); // Bật cuộn
        }

        // Đặt chiều cao cho RecyclerView
        ViewGroup.LayoutParams params = recyclerCustomerReviews.getLayoutParams();
        params.height = totalHeight;
        recyclerCustomerReviews.setLayoutParams(params);
    }

    // ItemDecoration để căn giữa các item
    private static class CenterItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
            if (itemCount == 0) return;

            // Tính toán khoảng cách để căn giữa
            int parentWidth = parent.getWidth();
            int itemWidth = view.getLayoutParams().width; // Chiều rộng của mỗi item
            int totalItemsWidth = itemWidth * itemCount;
            int remainingSpace = parentWidth - totalItemsWidth;

            if (remainingSpace > 0) {
                // Căn giữa bằng cách thêm padding đều hai bên
                int offset = remainingSpace / (2 * itemCount);
                outRect.left = offset;
                outRect.right = offset;
            }
        }
    }

    // ItemDecoration để thêm khoảng trống cho Related Products
    private static class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
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

            int position = parent.getChildAdapterPosition(view);
            if (position == 0) {
                outRect.left = spacing;
            }
            if (position == getItemCount(parent) - 1) {
                outRect.right = spacing;
            }
        }

        private int getItemCount(RecyclerView parent) {
            return parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
        }
    }
}