package saru.com.app;

import android.widget.ImageView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.connectors.CustomerReviewAdapter;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.connectors.VoucherAdapter;
import saru.com.app.models.CustomerReviews;
import saru.com.app.models.VoucherList;

public class Homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        // Tìm RecyclerView cho Super Sales
        RecyclerView recyclerViewSuperSales = findViewById(R.id.recycler_view_super_sales);
        if (recyclerViewSuperSales == null) {
            throw new IllegalStateException("RecyclerView with ID 'recycler_view_super_sales' not found in activity_homepage.xml");
        }

        // Tìm RecyclerView cho Bestseller Section
        RecyclerView recyclerViewBestseller = findViewById(R.id.recycler_view_best_seller);
        if (recyclerViewBestseller == null) {
            throw new IllegalStateException("RecyclerView with ID 'recycler_view_bestseller' not found in activity_homepage.xml");
        }

        // Tìm RecyclerView cho For You Section
        RecyclerView recyclerViewForYou = findViewById(R.id.recycler_view_for_you);
        if (recyclerViewForYou == null) {
            throw new IllegalStateException("RecyclerView with ID 'recycler_view_for_you' not found in activity_homepage.xml");
        }

        // Khởi tạo ProductAdapter cho Super Sales
        ProductAdapter superSalesAdapter = new ProductAdapter();

        // Khởi tạo ProductAdapter cho Bestseller Section
        ProductAdapter bestsellerAdapter = new ProductAdapter();

        // Khởi tạo VoucherAdapter cho For You Section
        VoucherList voucherList = new VoucherList();
        VoucherAdapter voucherAdapter = new VoucherAdapter(voucherList.getVouchers());

        // Thiết lập LinearLayoutManager với hướng ngang cho Super Sales
        LinearLayoutManager superSalesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSuperSales.setLayoutManager(superSalesLayoutManager);
        recyclerViewSuperSales.setAdapter(superSalesAdapter);

        // Thiết lập LinearLayoutManager với hướng ngang cho Bestseller Section
        LinearLayoutManager bestsellerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewBestseller.setLayoutManager(bestsellerLayoutManager);
        recyclerViewBestseller.setAdapter(bestsellerAdapter);

        // Thiết lập LinearLayoutManager với hướng ngang cho For You Section
        LinearLayoutManager forYouLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewForYou.setLayoutManager(forYouLayoutManager);
        recyclerViewForYou.setAdapter(voucherAdapter);

        // Tìm RecyclerView cho Customer Reviews
        RecyclerView recyclerViewCustomerReviews = findViewById(R.id.recycler_customer_reviews);
        if (recyclerViewCustomerReviews == null) {
            throw new IllegalStateException("RecyclerView with ID 'recycler_view_customer_reviews' not found in activity_homepage.xml");
        }

        // Khởi tạo danh sách CustomerReviews (ví dụ: dữ liệu tĩnh hoặc từ nguồn dữ liệu)
                List<CustomerReviews> customerReviewsList = new ArrayList<>();
        // Thêm dữ liệu mẫu (thay bằng dữ liệu thực tế từ API hoặc cơ sở dữ liệu)
                customerReviewsList.add(new CustomerReviews("John Doe", "Great product!", "Laptop", R.mipmap.ic_account));
                customerReviewsList.add(new CustomerReviews("Jane Smith", "Very satisfied!", "Phone", R.mipmap.ic_account));

        // Khởi tạo CustomerReviewAdapter
                CustomerReviewAdapter customerReviewAdapter = new CustomerReviewAdapter(customerReviewsList);

        // Thiết lập LinearLayoutManager (có thể là ngang hoặc dọc tùy thiết kế)
                LinearLayoutManager customerReviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                recyclerViewCustomerReviews.setLayoutManager(customerReviewsLayoutManager);
                recyclerViewCustomerReviews.setAdapter(customerReviewAdapter);

        // Xử lý nút "View All" của Super Sales
        TextView textViewAllSuperSales = findViewById(R.id.txtViewAllSuperSales);
        if (textViewAllSuperSales != null) {
            textViewAllSuperSales.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, Products.class);
                startActivity(intent);
            });
        }

        // Xử lý nút "View All" của Best Seller
        TextView textViewAllBestseller = findViewById(R.id.txtViewAllBestSeller);
        if (textViewAllBestseller != null) {
            textViewAllBestseller.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, Products.class);
                startActivity(intent);
            });
        }

        // Xử lý nút "View All" của For You
        TextView textViewAllForYou = findViewById(R.id.txtViewAllForYou);
        if (textViewAllForYou != null) {
            textViewAllForYou.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, VouchersManagement.class);
                startActivity(intent);
            });
        }

        // Xử lý icon ic_cart
        ImageView icCart = findViewById(R.id.btn_cart);
        if (icCart != null) {
            icCart.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, ProductCart.class);
                startActivity(intent);
            });
        }

        // Xử lý icon ic_back
        ImageView icBack = findViewById(R.id.btn_back_arrow);
        if (icBack != null) {
            icBack.setOnClickListener(v -> {
                // Quay lại Homepage (nếu đã ở Homepage thì không cần làm gì)
                // Hoặc có thể gọi finish() nếu muốn thoát activity hiện tại
                finish(); // Hoặc để trống nếu không cần hành động
            });
        }

        // Xử lý window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}