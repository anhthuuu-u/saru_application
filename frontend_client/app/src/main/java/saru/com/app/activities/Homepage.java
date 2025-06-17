package saru.com.app.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import saru.com.app.R;
import saru.com.app.connectors.CustomerReviewAdapter;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.connectors.VoucherAdapter;
import saru.com.app.models.CustomerReviewList;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;
import saru.com.app.models.VoucherList;

public class Homepage extends BaseActivity {
    private ListCartItems cartItems = new ListCartItems();
    private TextView cartItemCountText;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    private DrawerLayout drawerLayout;

    RecyclerView recyclerViewSearchResults;
    SearchView searchBar;

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            throw new IllegalStateException("DrawerLayout not found");
        }


        ImageButton btnFilter = findViewById(R.id.btn_filter);
        if (btnFilter == null) {
            throw new IllegalStateException("ImageButton btn_filter not found");
        }
        btnFilter.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        findViewById(R.id.main).setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        setupMenuNavigation();
        setupBottomNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerViewSuperSales = findViewById(R.id.recycler_view_super_sales);
        RecyclerView recyclerViewBestseller = findViewById(R.id.recycler_view_best_seller);
        RecyclerView recyclerViewForYou = findViewById(R.id.recycler_view_for_you);
        if (recyclerViewSuperSales == null || recyclerViewBestseller == null || recyclerViewForYou == null) {
            throw new IllegalStateException("RecyclerView not found");
        }

        cartItemCountText = findViewById(R.id.cart_item_count);
        if (cartItemCountText == null) {
            throw new IllegalStateException("TextView cart_item_count not found");
        }

        searchBar = findViewById(R.id.search_bar);
        if (searchBar == null) {
            throw new IllegalStateException("SearchView search_bar not found");
        }
        Log.d("Homepage", "SearchView found");

        searchBar.setIconifiedByDefault(false);
        searchBar.setFocusable(false);
        searchBar.setFocusableInTouchMode(false);
        searchBar.setOnClickListener(v -> {
            searchBar.setFocusable(true);
            searchBar.setFocusableInTouchMode(true);
            searchBar.requestFocus();
            Log.d("Homepage", "SearchView focused");
        });

        ProductAdapter superSalesAdapter = new ProductAdapter();
        ProductAdapter bestsellerAdapter = new ProductAdapter();
        VoucherAdapter voucherAdapter = new VoucherAdapter(new ArrayList<>());

        LinearLayoutManager superSalesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSuperSales.setLayoutManager(superSalesLayoutManager);
        recyclerViewSuperSales.setAdapter(superSalesAdapter);
        int superSalesSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewSuperSales.addItemDecoration(new ItemSpacingDecoration(superSalesSpacing));

        LinearLayoutManager bestsellerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewBestseller.setLayoutManager(bestsellerLayoutManager);
        recyclerViewBestseller.setAdapter(bestsellerAdapter);
        int bestsellerSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewBestseller.addItemDecoration(new ItemSpacingDecoration(bestsellerSpacing));

        LinearLayoutManager forYouLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewForYou.setLayoutManager(forYouLayoutManager);
        recyclerViewForYou.setAdapter(voucherAdapter);

        loadSuperSales(superSalesAdapter);
        loadBestsellers(bestsellerAdapter);

        VoucherList voucherList = new VoucherList();
        voucherList.getVouchers().observe(this, vouchers -> {
            voucherAdapter.updateVouchers(vouchers);
            voucherAdapter.notifyDataSetChanged();
        });

        CustomerReviewList customerReviewList = new CustomerReviewList();
        CustomerReviewAdapter customerReviewAdapter = new CustomerReviewAdapter(new ArrayList<>());
        LinearLayoutManager customerReviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerViewCustomerReviews = findViewById(R.id.recycler_customer_reviews);
        if (recyclerViewCustomerReviews == null) {
            throw new IllegalStateException("RecyclerView customer_reviews not found");
        }
        recyclerViewCustomerReviews.setLayoutManager(customerReviewsLayoutManager);
        recyclerViewCustomerReviews.setAdapter(customerReviewAdapter);
        customerReviewList.getReviews().observe(this, reviews -> {
            customerReviewAdapter.updateReviews(reviews);
            customerReviewAdapter.notifyDataSetChanged();
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Homepage", "Query submitted: " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Homepage", "Text changed: " + newText);
                return true;
            }
        });

        TextView textViewAllSuperSales = findViewById(R.id.txtViewAllSuperSales);
        if (textViewAllSuperSales != null) {
            textViewAllSuperSales.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Products.class)));
        }

        TextView textViewAllBestseller = findViewById(R.id.txtViewAllBestSeller);
        if (textViewAllBestseller != null) {
            textViewAllBestseller.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Products.class)));
        }

        TextView textViewAllForYou = findViewById(R.id.txtViewAllForYou);
        if (textViewAllForYou != null) {
            textViewAllForYou.setOnClickListener(v -> startActivity(new Intent(Homepage.this, VouchersManagement.class)));
        }

        ImageView icCart = findViewById(R.id.btn_cart);
        if (icCart != null) {
            icCart.setOnClickListener(v -> startActivity(new Intent(Homepage.this, ProductCart.class)));
        }

        updateCartItemCount();
    }

    private void loadSuperSales(ProductAdapter adapter) {
        db.collection("products")
                .whereEqualTo("isOnSale", true)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        products.add(doc.toObject(Product.class));
                    }
                    adapter.updateData(products);
                    Log.d("Homepage", "Super Sales loaded: " + products.size());
                })
                .addOnFailureListener(e -> Log.e("Homepage", "Error loading Super Sales: " + e.getMessage()));
    }

    private void loadBestsellers(ProductAdapter adapter) {
        db.collection("products")
                .whereEqualTo("isBestSelling", true)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        products.add(doc.toObject(Product.class));
                    }
                    adapter.updateData(products);
                    Log.d("Homepage", "Bestsellers loaded: " + products.size());
                })
                .addOnFailureListener(e -> Log.e("Homepage", "Error loading Bestsellers: " + e.getMessage()));
    }

    private void setupMenuNavigation() {
        LinearLayout homepageItem = findViewById(R.id.menu_item_homepage);
        LinearLayout productItem = findViewById(R.id.menu_item_product);
        LinearLayout cartItem = findViewById(R.id.menu_item_cart);
        LinearLayout blogItem = findViewById(R.id.menu_item_blog);
        LinearLayout faqsItem = findViewById(R.id.menu_item_faqs);
        LinearLayout aboutUsItem = findViewById(R.id.menu_item_about_us);
        LinearLayout notiItem = findViewById(R.id.menu_item_notification);

        if (homepageItem == null || productItem == null || cartItem == null ||
                blogItem == null || faqsItem == null || aboutUsItem == null || notiItem == null) {
            Log.e("Homepage", "Menu items not found");
            return;
        }

        homepageItem.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        productItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, Products.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        cartItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, ProductCart.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        blogItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, Blog_ListActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        faqsItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, CustomerSupportActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        aboutUsItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, Aboutus_SARUActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        notiItem.setOnClickListener(v -> {
            startActivity(new Intent(Homepage.this, Notification_FromOrderActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void updateCartItemCount() {
        int count = cartItems.getItemCount();
        if (cartItemCountText != null) {
            cartItemCountText.setText(String.valueOf(count));
            cartItemCountText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private static class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;

            outRect.left = spacing;
            outRect.right = spacing;

            if (position == 0) {
                outRect.left = 0;
            }
            if (position == itemCount - 1) {
                outRect.right = 0;
            }
        }
    }
}