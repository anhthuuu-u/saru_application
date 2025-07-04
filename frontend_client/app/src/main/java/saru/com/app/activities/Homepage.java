package saru.com.app.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import saru.com.app.R;
import saru.com.app.connectors.CustomerReviewAdapter;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.connectors.VoucherAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.CartManager;
import saru.com.app.models.CustomerReviewList;
import saru.com.app.models.Product;
import saru.com.app.models.VoucherList;

public class Homepage extends BaseActivity implements ProductAdapter.OnAddToCartListener {
    private static final String TAG = "Homepage";
    private TextView cartItemCountText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView btn_noti;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewSearchResults;
    private SearchView searchBar;
    private ProductAdapter searchResultsAdapter;
    private RecyclerView recyclerViewSuperSales, recyclerViewBestseller, recyclerViewForYou, recyclerViewCustomerReviews;
    private ConstraintLayout ForYouSection, SuperSaleSection, BestSellerSection;
    private LinearLayout CustomerReviewSection, Banner, Categories;
    private TextView txtViewAllSuperSales;
    private TextView txtViewAllBestSeller;
    private TextView txtForYou;
    private TextView txtViewAllForYou;
    private TextView txtSuperSales;
    private TextView txtBestSeller;
    private ImageView imgCustomerAvatar;
    private TextView txtCustomerName;
    private final Set<String> pendingCartOperations = new HashSet<>();

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

        btn_noti = findViewById(R.id.btn_noti);
        drawerLayout = findViewById(R.id.drawer_layout);
        searchBar = findViewById(R.id.search_bar);
        cartItemCountText = findViewById(R.id.cart_item_count);
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);
        recyclerViewSuperSales = findViewById(R.id.recycler_view_super_sales);
        recyclerViewBestseller = findViewById(R.id.recycler_view_best_seller);
        recyclerViewForYou = findViewById(R.id.recycler_view_for_you);
        recyclerViewCustomerReviews = findViewById(R.id.recycler_customer_reviews);
        ForYouSection = findViewById(R.id.ForYouSection);
        SuperSaleSection = findViewById(R.id.SuperSaleSection);
        BestSellerSection = findViewById(R.id.BestSellerSection);
        CustomerReviewSection = findViewById(R.id.CustomerReviewSection);
        Banner = findViewById(R.id.Banner);
        Categories = findViewById(R.id.Categories);
        txtViewAllSuperSales = findViewById(R.id.txtViewAllSuperSales);
        txtViewAllBestSeller = findViewById(R.id.txtViewAllBestSeller);
        txtForYou = findViewById(R.id.txtForYou);
        txtViewAllForYou = findViewById(R.id.txtViewAllForYou);
        txtSuperSales = findViewById(R.id.txtSuperSalesTitle);
        txtBestSeller = findViewById(R.id.txtBestSellerTitle);

        View headerView = findViewById(R.id.home_menu);
        imgCustomerAvatar = headerView.findViewById(R.id.imgCustomerAvatar);
        txtCustomerName = headerView.findViewById(R.id.txtCustomerName);

        CartManager.getInstance().initialize(this, success -> {
            runOnUiThread(() -> {
                if (success && cartItemCountText != null) {
                    CartManager.getInstance().addBadgeView(cartItemCountText);
                    CartManager.getInstance().updateAllBadges();
                    Log.d(TAG, "Cart data loaded, badge updated");
                } else {
                    Log.e(TAG, "Failed to load cart data");
                }
            });
        });

        btn_noti.setOnClickListener(v -> openNotification());

        ImageButton btnFilter = findViewById(R.id.btn_filter);
        if (btnFilter == null) {
            Log.e(TAG, "ImageButton btn_filter not found");
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

        ImageView icCart = findViewById(R.id.btn_cart);
        if (icCart != null) {
            icCart.setOnClickListener(v -> startActivity(new Intent(Homepage.this, ProductCart.class)));
        }

        searchBar.setIconifiedByDefault(false);
        searchBar.setFocusable(false);
        searchBar.setFocusableInTouchMode(false);
        searchBar.setOnClickListener(v -> {
            searchBar.setFocusable(true);
            searchBar.setFocusableInTouchMode(true);
            searchBar.requestFocus();
            Log.d(TAG, "SearchView focused");
        });

        searchResultsAdapter = new ProductAdapter(this, this);
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewSearchResults.setAdapter(searchResultsAdapter);
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewSearchResults.addItemDecoration(new GridSpacingItemDecoration(spacing));
        recyclerViewSearchResults.setVisibility(View.GONE);

        ProductAdapter superSalesAdapter = new ProductAdapter(this, this);
        ProductAdapter bestsellerAdapter = new ProductAdapter(this, this);
        VoucherAdapter voucherAdapter = new VoucherAdapter(new ArrayList<>());

        LinearLayoutManager superSalesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSuperSales.setLayoutManager(superSalesLayoutManager);
        recyclerViewSuperSales.setAdapter(superSalesAdapter);
        recyclerViewSuperSales.addItemDecoration(new ItemSpacingDecoration(spacing));

        LinearLayoutManager bestsellerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewBestseller.setLayoutManager(bestsellerLayoutManager);
        recyclerViewBestseller.setAdapter(bestsellerAdapter);
        recyclerViewBestseller.addItemDecoration(new ItemSpacingDecoration(spacing));

        LinearLayoutManager forYouLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewForYou.setLayoutManager(forYouLayoutManager);
        recyclerViewForYou.setAdapter(voucherAdapter);
        recyclerViewForYou.addItemDecoration(new ItemSpacingDecoration(spacing));

        CustomerReviewAdapter customerReviewAdapter = new CustomerReviewAdapter(new ArrayList<>());
        LinearLayoutManager customerReviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCustomerReviews.setLayoutManager(customerReviewsLayoutManager);
        recyclerViewCustomerReviews.setAdapter(customerReviewAdapter);
        recyclerViewCustomerReviews.addItemDecoration(new ItemSpacingDecoration(spacing));

        loadSuperSales(superSalesAdapter);
        loadBestsellers(bestsellerAdapter);

        VoucherList voucherList = new VoucherList();
        voucherList.getVouchers().observe(this, vouchers -> {
            voucherAdapter.updateVouchers(vouchers);
            voucherAdapter.notifyDataSetChanged();
        });

        CustomerReviewList customerReviewList = new CustomerReviewList();
        customerReviewList.getReviews().observe(this, reviews -> {
            customerReviewAdapter.updateReviews(reviews);
            customerReviewAdapter.notifyDataSetChanged();
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query submitted: " + query);
                searchProducts(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Text changed: " + newText);
                searchProducts(newText.trim());
                return true;
            }
        });

        if (txtViewAllSuperSales != null) {
            txtViewAllSuperSales.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Products.class)));
        }
        if (txtViewAllBestSeller != null) {
            txtViewAllBestSeller.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Products.class)));
        }
        if (txtViewAllForYou != null) {
            txtViewAllForYou.setOnClickListener(v -> startActivity(new Intent(Homepage.this, VouchersManagement.class)));
        }

        setupMenuNavigation();
        setupBottomNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, redirecting to LoginActivity");
            Intent intent = new Intent(Homepage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            CartManager.getInstance().setUser(currentUser.getUid());
            CartManager.getInstance().initialize(this, success -> {
                runOnUiThread(() -> {
                    if (success && cartItemCountText != null) {
                        CartManager.getInstance().addBadgeView(cartItemCountText);
                        CartManager.getInstance().updateAllBadges();
                        Log.d(TAG, "Cart data reloaded in onStart, badge updated");
                    } else {
                        Log.e(TAG, "Failed to reload cart data in onStart");
                    }
                });
            });
            updateUserInfo(currentUser);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cartItemCountText != null) {
            CartManager.getInstance().removeBadgeView(cartItemCountText);
        }
        pendingCartOperations.clear();
    }

    private void updateUserInfo(FirebaseUser user) {
        db.collection("accounts").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String customerEmail = documentSnapshot.getString("CustomerEmail");
                        String displayName = "User";
                        if (customerEmail != null && customerEmail.contains("@")) {
                            displayName = customerEmail.split("@")[0];
                        }
                        txtCustomerName.setText(displayName);

                        String photoUrl = documentSnapshot.getString("url");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.menu_customer_img)
                                    .error(R.drawable.menu_customer_img)
                                    .into(imgCustomerAvatar);
                        } else {
                            imgCustomerAvatar.setImageResource(R.drawable.menu_customer_img);
                        }
                    } else {
                        txtCustomerName.setText("User");
                        imgCustomerAvatar.setImageResource(R.drawable.menu_customer_img);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user info: " + e.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    txtCustomerName.setText("User");
                    imgCustomerAvatar.setImageResource(R.drawable.menu_customer_img);
                });
    }

    private void openNotification() {
        Intent intent = new Intent(this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to use cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (product == null || product.getProductID() == null) {
            Log.e(TAG, "Cannot add null Product or Product with null ProductID");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Null Product or ProductID in onAddToCart"));
            Toast.makeText(this, "Error when adding to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        String accountID = mAuth.getCurrentUser().getUid();
        String productID = product.getProductID();
        synchronized (pendingCartOperations) {
            if (pendingCartOperations.contains(productID)) {
                Log.d(TAG, "Add to cart operation for product " + productID + " is already in progress, ignoring");
                return;
            }
            pendingCartOperations.add(productID);
        }
        checkPlayServices(() -> onAddToCartInternal(product, accountID));
    }

    private void checkPlayServices(Runnable firebaseAction) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            firebaseAction.run();
        } else {
            Log.e(TAG, "Google Play Services not available, resultCode: " + resultCode);
            FirebaseCrashlytics.getInstance().recordException(new Exception("Google Play Services not available, resultCode: " + resultCode));
            Toast.makeText(this, "Google Play Services is currently unavailable", Toast.LENGTH_SHORT).show();
            firebaseAction.run();
        }
    }

    private void onAddToCartInternal(Product product, String accountID) {
        CartItem cartItem = new CartItem(
                product.getProductID(),
                accountID,
                System.currentTimeMillis(),
                product.getProductName() != null ? product.getProductName() : "Unknown",
                product.getProductPrice(),
                product.getImageID() != null ? product.getImageID() : "",
                1,
                false
        );

        Map<String, Object> cartItemMap = new HashMap<>();
        cartItemMap.put("productID", cartItem.getProductID());
        cartItemMap.put("AccountID", cartItem.getAccountID());
        cartItemMap.put("timestamp", cartItem.getTimestamp());
        cartItemMap.put("productName", cartItem.getProductName());
        cartItemMap.put("productPrice", cartItem.getProductPrice());
        cartItemMap.put("imageID", cartItem.getImageID());
        cartItemMap.put("quantity", cartItem.getQuantity());
        cartItemMap.put("selected", cartItem.isSelected());

        if (cartItemCountText != null) {
            int currentCount = CartManager.getInstance().getItemCount();
            boolean itemExists = CartManager.getInstance().getCartItems().stream()
                    .anyMatch(item -> item.getProductID().equals(product.getProductID()));
            if (!itemExists) {
                cartItemCountText.setText(String.valueOf(currentCount + 1));
                cartItemCountText.setVisibility(View.VISIBLE);
                Log.d(TAG, "Temporary badge update: " + (currentCount + 1));
            }
        }

        long startTime = System.nanoTime();
        db.collection("carts").document(accountID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection("carts").document(accountID).set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Created cart document for: " + accountID))
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating cart document: ", e);
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                    synchronized (pendingCartOperations) {
                                        pendingCartOperations.remove(product.getProductID());
                                    }
                                    if (cartItemCountText != null) {
                                        cartItemCountText.setText(String.valueOf(CartManager.getInstance().getItemCount()));
                                        cartItemCountText.setVisibility(CartManager.getInstance().getItemCount() > 0 ? View.VISIBLE : View.GONE);
                                    }
                                });
                    }

                    db.collection("carts").document(accountID).collection("items").document(product.getProductID())
                            .get()
                            .addOnSuccessListener(itemSnapshot -> {
                                Long currentQuantity = itemSnapshot.getLong("quantity");
                                if (itemSnapshot.exists() && currentQuantity != null && currentQuantity > 0) {
                                    cartItemMap.put("quantity", currentQuantity + 1);
                                    cartItem.setQuantity(currentQuantity.intValue() + 1);
                                    Log.d(TAG, "Item exists with valid quantity " + currentQuantity + ", updated to: " + (currentQuantity + 1));
                                } else {
                                    cartItemMap.put("quantity", 1);
                                    cartItem.setQuantity(1);
                                    if (itemSnapshot.exists()) {
                                        Log.d(TAG, "Item exists but has invalid quantity (" + currentQuantity + "), resetting to 1");
                                        db.collection("carts").document(accountID).collection("items").document(product.getProductID())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted invalid item document: " + product.getProductID()))
                                                .addOnFailureListener(e -> Log.e(TAG, "Error deleting invalid item document: ", e));
                                    } else {
                                        Log.d(TAG, "Item does not exist, setting quantity to 1");
                                    }
                                }
                                db.collection("carts").document(accountID).collection("items").document(product.getProductID())
                                        .set(cartItemMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Added/Updated to cart: " + product.getProductName() + ", Quantity: " + cartItem.getQuantity() + ", Time: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
                                            Toast.makeText(this, getString(R.string.added_to_cart, product.getProductName()), Toast.LENGTH_SHORT).show();
                                            CartManager.getInstance().updateAllBadges();
                                            synchronized (pendingCartOperations) {
                                                pendingCartOperations.remove(product.getProductID());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error adding to cart: ", e);
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                            Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                                            if (cartItemCountText != null) {
                                                cartItemCountText.setText(String.valueOf(CartManager.getInstance().getItemCount()));
                                                cartItemCountText.setVisibility(CartManager.getInstance().getItemCount() > 0 ? View.VISIBLE : View.GONE);
                                            }
                                            synchronized (pendingCartOperations) {
                                                pendingCartOperations.remove(product.getProductID());
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking cart item: ", e);
                                FirebaseCrashlytics.getInstance().recordException(e);
                                Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                                if (cartItemCountText != null) {
                                    cartItemCountText.setText(String.valueOf(CartManager.getInstance().getItemCount()));
                                    cartItemCountText.setVisibility(CartManager.getInstance().getItemCount() > 0 ? View.VISIBLE : View.GONE);
                                }
                                synchronized (pendingCartOperations) {
                                    pendingCartOperations.remove(product.getProductID());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking cart document: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                    if (cartItemCountText != null) {
                        cartItemCountText.setText(String.valueOf(CartManager.getInstance().getItemCount()));
                        cartItemCountText.setVisibility(CartManager.getInstance().getItemCount() > 0 ? View.VISIBLE : View.GONE);
                    }
                    synchronized (pendingCartOperations) {
                        pendingCartOperations.remove(product.getProductID());
                    }
                });
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            recyclerViewSearchResults.setVisibility(View.GONE);
            if (recyclerViewSuperSales != null) recyclerViewSuperSales.setVisibility(View.VISIBLE);
            if (recyclerViewBestseller != null) recyclerViewBestseller.setVisibility(View.VISIBLE);
            if (recyclerViewForYou != null) recyclerViewForYou.setVisibility(View.VISIBLE);
            if (recyclerViewCustomerReviews != null) recyclerViewCustomerReviews.setVisibility(View.VISIBLE);
            if (txtSuperSales != null) txtSuperSales.setVisibility(View.VISIBLE);
            if (txtViewAllSuperSales != null) txtViewAllSuperSales.setVisibility(View.VISIBLE);
            if (txtBestSeller != null) txtBestSeller.setVisibility(View.VISIBLE);
            if (txtViewAllBestSeller != null) txtViewAllBestSeller.setVisibility(View.VISIBLE);
            if (txtForYou != null) txtForYou.setVisibility(View.VISIBLE);
            if (txtViewAllForYou != null) txtViewAllForYou.setVisibility(View.VISIBLE);
            if (ForYouSection != null) ForYouSection.setVisibility(View.VISIBLE);
            if (SuperSaleSection != null) SuperSaleSection.setVisibility(View.VISIBLE);
            if (BestSellerSection != null) BestSellerSection.setVisibility(View.VISIBLE);
            if (CustomerReviewSection != null) CustomerReviewSection.setVisibility(View.VISIBLE);
            if (Banner != null) Banner.setVisibility(View.VISIBLE);
            if (Categories != null) Categories.setVisibility(View.VISIBLE);
            searchResultsAdapter.updateData(new ArrayList<>());
            return;
        }

        if (recyclerViewSuperSales != null) recyclerViewSuperSales.setVisibility(View.GONE);
        if (recyclerViewBestseller != null) recyclerViewBestseller.setVisibility(View.GONE);
        if (recyclerViewForYou != null) recyclerViewForYou.setVisibility(View.GONE);
        if (recyclerViewCustomerReviews != null) recyclerViewCustomerReviews.setVisibility(View.GONE);
        if (txtSuperSales != null) txtSuperSales.setVisibility(View.GONE);
        if (txtViewAllSuperSales != null) txtViewAllSuperSales.setVisibility(View.GONE);
        if (txtBestSeller != null) txtBestSeller.setVisibility(View.GONE);
        if (txtViewAllBestSeller != null) txtViewAllBestSeller.setVisibility(View.GONE);
        if (txtForYou != null) txtForYou.setVisibility(View.GONE);
        if (txtViewAllForYou != null) txtViewAllForYou.setVisibility(View.GONE);
        if (ForYouSection != null) ForYouSection.setVisibility(View.GONE);
        if (SuperSaleSection != null) SuperSaleSection.setVisibility(View.GONE);
        if (BestSellerSection != null) BestSellerSection.setVisibility(View.GONE);
        if (CustomerReviewSection != null) CustomerReviewSection.setVisibility(View.GONE);
        if (Banner != null) Banner.setVisibility(View.GONE);
        if (Categories != null) Categories.setVisibility(View.GONE);

        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null && product.getProductName() != null &&
                                product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                            products.add(product);
                        }
                    }
                    searchResultsAdapter.updateData(products);
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Search results loaded: " + products.size() + " for query: " + query);
                    if (products.isEmpty()) {
                        Toast.makeText(Homepage.this, "No products found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching products: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(Homepage.this, "Error occurred while searching for products", Toast.LENGTH_SHORT).show();
                    recyclerViewSearchResults.setVisibility(View.GONE);
                });
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
                    Log.d(TAG, "Super Sales loaded: " + products.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Super Sales: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                });
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
                    Log.d(TAG, "Bestsellers loaded: " + products.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Bestsellers: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                });
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
            Log.e(TAG, "Menu items not found");
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

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public GridSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int spanCount = 2;

            outRect.top = spacing;
            outRect.bottom = spacing;

            if (position % spanCount == 0) {
                outRect.left = 0;
                outRect.right = spacing;
            } else {
                outRect.left = spacing;
                outRect.right = 0;
            }
        }
    }
}