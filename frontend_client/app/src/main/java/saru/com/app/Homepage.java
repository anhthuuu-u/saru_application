package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
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
import saru.com.app.models.CartItem;
import saru.com.app.models.CustomerReviews;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;
import saru.com.app.models.ProductList;
import saru.com.app.models.VoucherList;
import androidx.fragment.app.FragmentManager;

public class Homepage extends BaseActivity {
    private ListCartItems cartItems = new ListCartItems();
    private TextView cartItemCountText;
    private List<Product> products = new ArrayList<>();
    private ProductAdapter searchResultsAdapter;

    RecyclerView recyclerViewSearchResults;
    SearchView searchBar;

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_home; // Ensure this ID exists in your menu_bottom_nav.xml
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        // Setup bottom navigation
        setupBottomNavigation();

        // Xử lý window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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



        // Tìm TextView cho số lượng giỏ hàng (thêm vào layout nếu chưa có)
        cartItemCountText = findViewById(R.id.cart_item_count); // Giả định ID trong layout
        if (cartItemCountText == null) {
            throw new IllegalStateException("TextView with ID 'cart_item_count' not found in activity_homepage.xml");
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

        // Khởi tạo dữ liệu từ ProductList
        ProductList productList = new ProductList();
        products = productList.getProducts();
        Log.d("Homepage", "Products size: " + products.size()); // Debug số lượng sản phẩm

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

        // Xử lý sự kiện nhập liệu (chỉ log để kiểm tra, chưa lọc dữ liệu)
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Homepage", "Query submitted: " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Homepage", "Text changed: " + newText); // Chỉ log để kiểm tra gõ chữ
                return true;
            }
        });

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

        // Update cart count
        updateCartItemCount();
    }


    // Cập nhật số lượng sản phẩm trong giỏ hàng
    private void updateCartItemCount() {
        int count = cartItems.getItemCount();
        if (cartItemCountText != null) {
            cartItemCountText.setText(String.valueOf(count));
            cartItemCountText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    // Phương thức để thêm sản phẩm vào giỏ hàng (gọi từ ProductAdapter)
    public void addToCart(Product product) {
        cartItems.addItem(new CartItem(product, 1));
        updateCartItemCount();
    }
}