package saru.com.app.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import saru.com.app.R;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.models.CartItem;
import saru.com.app.models.CartManager;
import saru.com.app.models.Product;
import saru.com.app.models.productBrand;
import saru.com.app.models.productCategory;

public class Products extends BaseActivity implements ProductAdapter.OnAddToCartListener {
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SearchView searchBar;
    private List<productCategory> categories;
    private List<productBrand> brands;
    private List<String> volumes;
    private List<String> wineTypes;
    private TextView cartItemCountText;
    private AtomicBoolean isFilterDataLoaded = new AtomicBoolean(false);
    private Map<String, String> categoryCache = new HashMap<>();

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_product;
    }

    private void initializeData() {
        try {
            checkPlayServices(() -> {
                loadFilterData(null);
                loadProducts(null);
            });
        } catch (SecurityException e) {
            Log.e("Products", "SecurityException in initializeData: " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, getString(R.string.google_play_services_unavailable), Toast.LENGTH_SHORT).show();
            // Tiếp tục tải dữ liệu mà không phụ thuộc Google Play Services
            loadFilterData(null);
            loadProducts(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cartItemCountText = findViewById(R.id.cart_item_count);
        if (cartItemCountText == null) {
            throw new IllegalStateException("TextView cart_item_count not found");
        }

        categories = new ArrayList<>();
        brands = new ArrayList<>();
        volumes = new ArrayList<>();
        wineTypes = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, this);
        recyclerView.setAdapter(productAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Khởi tạo CartManager với callback
        CartManager.getInstance().initialize(this, success -> {
            runOnUiThread(() -> {
                if (success && cartItemCountText != null) {
                    CartManager.getInstance().addBadgeView(cartItemCountText);
                    CartManager.getInstance().updateAllBadges();
                    Log.d("Products", "Cart data loaded, badge updated");
                } else {
                    Log.e("Products", "Failed to load cart data");
                    Toast.makeText(this, getString(R.string.error_loading_cart), Toast.LENGTH_SHORT).show();
                }
            });
        });

        initializeData();

        ImageButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        TextView txtFilter = findViewById(R.id.txtFilter);
        txtFilter.setOnClickListener(v -> showFilterDialog());

        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> startActivity(new Intent(Products.this, ProductCart.class)));
        }

        ImageButton btnBackArrow = findViewById(R.id.btn_back_arrow);
        if (btnBackArrow != null) {
            btnBackArrow.setOnClickListener(v -> {
                startActivity(new Intent(Products.this, Homepage.class));
                finish();
            });
        }
        ImageButton btn_noti = findViewById(R.id.btn_noti);
        btn_noti.setOnClickListener(v -> openNotification());

        searchBar = findViewById(R.id.search_bar);
        if (searchBar == null) {
            throw new IllegalStateException("SearchView search_bar not found");
        }
        Log.d("Products", "SearchView found");

        searchBar.setIconifiedByDefault(false);
        searchBar.setFocusable(false);
        searchBar.setFocusableInTouchMode(false);
        searchBar.setOnClickListener(v -> {
            searchBar.setFocusable(true);
            searchBar.setFocusableInTouchMode(true);
            searchBar.requestFocus();
            Log.d("Products", "SearchView focused");
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Products", "Query submitted: " + query);
                searchProducts(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Products", "Text changed: " + newText);
                searchProducts(newText.trim());
                return true;
            }
        });

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
        if (auth.getCurrentUser() != null) {
            CartManager.getInstance().setUser(auth.getCurrentUser().getUid());
            CartManager.getInstance().initialize(this, success -> {
                runOnUiThread(() -> {
                    if (success && cartItemCountText != null) {
                        CartManager.getInstance().addBadgeView(cartItemCountText);
                        CartManager.getInstance().updateAllBadges();
                        Log.d("Products", "Cart data reloaded in onStart, badge updated");
                    } else {
                        Log.e("Products", "Failed to reload cart data in onStart");
                        Toast.makeText(this, getString(R.string.error_loading_cart), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cartItemCountText != null) {
            CartManager.getInstance().removeBadgeView(cartItemCountText);
        }
    }

    private void openNotification() {
        Intent intent = new Intent(this, Notification_FromOrderActivity.class);
        startActivity(intent);
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            loadProducts(null);
            Log.d("Products", "Query empty, loading default products");
            return;
        }

        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null && product.getProductName() != null &&
                                product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                            String cateID = product.getCateID();
                            if (cateID != null && categoryCache.containsKey(cateID)) {
                                product.setCategory(categoryCache.get(cateID));
                                products.add(product);
                            } else if (cateID != null) {
                                db.collection("productCategory").document(cateID).get()
                                        .addOnSuccessListener(categoryDoc -> {
                                            productCategory category = categoryDoc.toObject(productCategory.class);
                                            String categoryName = category != null ? category.getCateName() : getString(R.string.no_category_available);
                                            product.setCategory(categoryName);
                                            categoryCache.put(cateID, categoryName);
                                            products.add(product);
                                            productAdapter.updateData(new ArrayList<>(products));
                                        })
                                        .addOnFailureListener(e -> {
                                            product.setCategory(getString(R.string.error_loading_category));
                                            categoryCache.put(cateID, getString(R.string.error_loading_category));
                                            products.add(product);
                                            productAdapter.updateData(new ArrayList<>(products));
                                            Log.e("Products", "Error loading category for cateID: " + cateID, e);
                                        });
                            } else {
                                product.setCategory(getString(R.string.no_category_id));
                                products.add(product);
                            }
                        }
                    }
                    productAdapter.updateData(new ArrayList<>(products));
                    Log.d("Products", "Search results loaded: " + products.size() + " for query: " + query);
                    if (products.isEmpty()) {
                        Toast.makeText(Products.this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Products", "Error searching products: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(Products.this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAddToCart(Product product) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (product == null || product.getProductID() == null) {
            Log.e("Products", "Cannot add null Product or Product with null ProductID");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Null Product or ProductID in onAddToCart"));
            Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
            return;
        }

        String accountID = auth.getCurrentUser().getUid();
        onAddToCartInternal(product, accountID); // Thực hiện trực tiếp mà không cần checkPlayServices
    }

    private void checkPlayServices(Runnable firebaseAction) {
        try {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
            if (resultCode == ConnectionResult.SUCCESS) {
                Log.d("Products", "Google Play Services available");
                firebaseAction.run();
            } else {
                Log.e("Products", "Google Play Services not available, resultCode: " + resultCode);
                FirebaseCrashlytics.getInstance().recordException(new Exception("Google Play Services not available, resultCode: " + resultCode));
                Toast.makeText(this, getString(R.string.google_play_services_unavailable), Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Log.e("Products", "SecurityException in checkPlayServices: " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, getString(R.string.google_play_services_unavailable), Toast.LENGTH_SHORT).show();
            firebaseAction.run(); // Tiếp tục thực hiện hành động Firestore
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

        db.collection("carts").document(accountID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection("carts").document(accountID).set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> Log.d("Products", "Created cart document for: " + accountID))
                                .addOnFailureListener(e -> {
                                    Log.e("Products", "Error creating cart document: ", e);
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                });
                    }

                    db.collection("carts").document(accountID).collection("items").document(product.getProductID())
                            .get()
                            .addOnSuccessListener(itemSnapshot -> {
                                if (itemSnapshot.exists() && itemSnapshot.getLong("quantity") != null) {
                                    Long currentQuantity = itemSnapshot.getLong("quantity");
                                    cartItemMap.put("quantity", currentQuantity + 1);
                                    cartItem.setQuantity(currentQuantity.intValue() + 1);
                                    Log.d("Products", "Item exists, updated quantity to: " + (currentQuantity + 1));
                                } else {
                                    cartItemMap.put("quantity", 1);
                                    cartItem.setQuantity(1);
                                    Log.d("Products", "Item does not exist, setting quantity to 1");
                                }
                                db.collection("carts").document(accountID).collection("items").document(product.getProductID())
                                        .set(cartItemMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Products", "Added/Updated to cart: " + product.getProductName() + ", Quantity: " + cartItem.getQuantity());
                                            Toast.makeText(this, getString(R.string.added_to_cart, product.getProductName()), Toast.LENGTH_SHORT).show();
                                            CartManager.getInstance().updateAllBadges();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Products", "Error adding to cart: ", e);
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                            Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Products", "Error checking cart item: ", e);
                                FirebaseCrashlytics.getInstance().recordException(e);
                                Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Products", "Error checking cart document: ", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(this, getString(R.string.error_adding_to_cart), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProducts(Query query) {
        if (query == null) {
            query = db.collection("products");
        }
        query.get().addOnSuccessListener(querySnapshot -> {
            List<Product> products = new ArrayList<>();
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                productAdapter.updateData(products);
                Toast.makeText(this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
                Log.d("Products", "No products found");
                return;
            }

            for (DocumentSnapshot doc : documents) {
                Product product = doc.toObject(Product.class);
                if (product != null) {
                    String cateID = product.getCateID();
                    if (cateID != null) {
                        if (categoryCache.containsKey(cateID)) {
                            product.setCategory(categoryCache.get(cateID));
                            products.add(product);
                        } else {
                            db.collection("productCategory").document(cateID).get()
                                    .addOnSuccessListener(categoryDoc -> {
                                        productCategory category = categoryDoc.toObject(productCategory.class);
                                        String categoryName = category != null ? category.getCateName() : getString(R.string.no_category_available);
                                        product.setCategory(categoryName);
                                        categoryCache.put(cateID, categoryName);
                                        products.add(product);
                                        productAdapter.updateData(new ArrayList<>(products));
                                    })
                                    .addOnFailureListener(e -> {
                                        product.setCategory(getString(R.string.error_loading_category));
                                        categoryCache.put(cateID, getString(R.string.error_loading_category));
                                        products.add(product);
                                        productAdapter.updateData(new ArrayList<>(products));
                                        Log.e("Products", "Error loading category for cateID: " + cateID, e);
                                    });
                        }
                    } else {
                        product.setCategory(getString(R.string.no_category_id));
                        products.add(product);
                    }
                }
            }
            productAdapter.updateData(new ArrayList<>(products));
            Log.d("Products", "Products loaded: " + products.size());
        }).addOnFailureListener(e -> {
            Log.e("Products", "Error loading products: ", e);
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFilterData(Runnable callback) {
        categories.clear();
        brands.clear();
        volumes.clear();
        wineTypes.clear();

        db.collection("productCategory").get().addOnSuccessListener(categorySnapshot -> {
            for (DocumentSnapshot doc : categorySnapshot.getDocuments()) {
                try {
                    productCategory category = doc.toObject(productCategory.class);
                    if (category != null) {
                        Log.d("Products", "Raw category data: " + doc.getData());
                        String cateName = doc.getString("CateName");
                        String cateID = doc.getString("CateID");
                        if (cateName != null || cateID != null) {
                            if (category.getCateName() == null) category.setCateName(cateName);
                            if (category.getCateID() == null) category.setCateID(cateID);
                            categories.add(category);
                            Log.d("Products", "Loaded category: ID=" + category.getCateID() + ", Name=" + category.getCateName());
                        } else {
                            Log.w("Products", "Invalid category data (null CateName and CateID): " + doc.getData());
                        }
                    } else {
                        Log.e("Products", "Failed to map category: " + doc.getData());
                        String cateName = doc.getString("CateName");
                        String cateID = doc.getString("CateID");
                        if (cateName != null && cateID != null) {
                            category = new productCategory(cateID, cateName);
                            categories.add(category);
                            Log.d("Products", "Manually loaded category: ID=" + cateID + ", Name=" + cateName);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Products", "Error mapping category document: " + doc.getId(), e);
                }
            }
            Log.d("Products", "Total categories loaded: " + categories.size());
        }).addOnFailureListener(e -> {
            Log.e("Products", "Error loading categories: ", e);
            Toast.makeText(this, getString(R.string.error_loading_filter_data), Toast.LENGTH_SHORT).show();
        });

        com.google.android.gms.tasks.Tasks.whenAllSuccess(
                db.collection("productBrand").get(),
                db.collection("products").get()
        ).addOnSuccessListener(results -> {
            com.google.firebase.firestore.QuerySnapshot brandSnapshot = (com.google.firebase.firestore.QuerySnapshot) results.get(0);
            for (DocumentSnapshot doc : brandSnapshot.getDocuments()) {
                productBrand brand = doc.toObject(productBrand.class);
                if (brand != null && brand.getBrandID() != null && brand.getBrandName() != null) {
                    brands.add(brand);
                    Log.d("Products", "Loaded brand: ID=" + brand.getBrandID() + ", Name=" + brand.getBrandName());
                }
            }
            Log.d("Products", "Total brands loaded: " + brands.size());

            Set<String> volumeSet = new HashSet<>();
            Set<String> wineTypeSet = new HashSet<>();
            com.google.firebase.firestore.QuerySnapshot productSnapshot = (com.google.firebase.firestore.QuerySnapshot) results.get(1);
            for (DocumentSnapshot doc : productSnapshot.getDocuments()) {
                String volume = doc.getString("netContent");
                if (volume != null && !volume.isEmpty() && !volumeSet.contains(volume)) {
                    volumeSet.add(volume);
                    volumes.add(volume);
                }
                String wineType = doc.getString("wineType");
                if (wineType != null && !wineType.isEmpty() && !wineTypeSet.contains(wineType)) {
                    wineTypeSet.add(wineType);
                    wineTypes.add(wineType);
                }
            }
            Log.d("Products", "Total volumes loaded: " + volumes.size());
            Log.d("Products", "Total wineTypes loaded: " + wineTypes.size());

            if (!categories.isEmpty()) {
                isFilterDataLoaded.set(true);
                if (callback != null) {
                    callback.run();
                }
            } else {
                Log.e("Products", "No categories loaded. Check Firestore or connection.");
                Toast.makeText(this, getString(R.string.error_loading_filter_data), Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Products", "Error loading filter data: ", e);
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, getString(R.string.error_loading_filter_data), Toast.LENGTH_SHORT).show();
            isFilterDataLoaded.set(false);
        });
    }

    private void showFilterDialog() {
        if (!isFilterDataLoaded.get()) {
            Toast.makeText(this, getString(R.string.filter_data_not_ready), Toast.LENGTH_SHORT).show();
            Log.d("Products", "Filter data not loaded, reloading...");
            loadFilterData(() -> showFilterDialog());
            return;
        }

        Dialog filterDialog = new Dialog(this, android.R.style.Theme_NoTitleBar);
        filterDialog.setContentView(R.layout.filter_dialog);

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

        String defaultCategory = getString(R.string.filter_product_dialog_category);
        String defaultSortBy = getString(R.string.title_product_dialog_sort);
        String defaultBrand = getString(R.string.title_filter_product_dialog_brand);
        String defaultVolume = getString(R.string.title_filter_product_dialog_volume);
        String defaultWineType = getString(R.string.title_filter_product_dialog_wine_type);

        dropdownCategory.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownCategory);
            popupMenu.getMenu().add(getString(R.string.filter_all));
            if (categories != null && !categories.isEmpty()) {
                for (productCategory category : categories) {
                    if (category != null && category.getCateName() != null) {
                        popupMenu.getMenu().add(category.getCateName());
                        Log.d("Products", "Added to dropdown: " + category.getCateName() + " (ID: " + category.getCateID() + ")");
                    }
                }
            } else {
                Log.e("Products", "Categories list is empty or null. Check loadFilterData()");
                popupMenu.getMenu().add(getString(R.string.no_categories_available));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                textCategory.setText(item.getTitle());
                Log.d("Products", "Selected category: " + item.getTitle());
                return true;
            });
            popupMenu.show();
        });

        dropdownSortBy.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownSortBy);
            popupMenu.getMenu().add(getString(R.string.filter_all));
            popupMenu.getMenu().add(getString(R.string.sort_low_to_high));
            popupMenu.getMenu().add(getString(R.string.sort_high_to_low));
            popupMenu.setOnMenuItemClickListener(item -> {
                textSortBy.setText(item.getTitle());
                Log.d("Products", "Selected sort: " + item.getTitle());
                return true;
            });
            popupMenu.show();
        });

        dropdownBrand.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownBrand);
            popupMenu.getMenu().add(getString(R.string.filter_all));
            for (productBrand brand : brands) {
                popupMenu.getMenu().add(brand.getBrandName());
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                textBrand.setText(item.getTitle());
                Log.d("Products", "Selected brand: " + item.getTitle());
                return true;
            });
            popupMenu.show();
        });

        dropdownVolume.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownVolume);
            popupMenu.getMenu().add(getString(R.string.filter_all));
            for (String volume : volumes) {
                popupMenu.getMenu().add(volume);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                textVolume.setText(item.getTitle());
                Log.d("Products", "Selected volume: " + item.getTitle());
                return true;
            });
            popupMenu.show();
        });

        dropdownWineType.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownWineType);
            popupMenu.getMenu().add(getString(R.string.filter_all));
            for (String wineType : wineTypes) {
                popupMenu.getMenu().add(wineType);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                textWineType.setText(item.getTitle());
                Log.d("Products", "Selected wine type: " + item.getTitle());
                return true;
            });
            popupMenu.show();
        });

        buttonApplyFilter.setOnClickListener(v -> {
            Query query = db.collection("products");
            String selectedCategory = textCategory.getText().toString().trim();
            String selectedSortBy = textSortBy.getText().toString().trim();
            String selectedBrand = textBrand.getText().toString().trim();
            String selectedVolume = textVolume.getText().toString().trim();
            String selectedWineType = textWineType.getText().toString().trim();
            String priceMin = editPriceMin.getText().toString().trim();
            String priceMax = editPriceMax.getText().toString().trim();
            boolean isBestSelling = checkboxBestSelling.isChecked();
            boolean isOnSale = checkboxOnSale.isChecked();
            boolean hasFilter = false;

            try {
                if (!selectedCategory.equals(defaultCategory) && !selectedCategory.equals(getString(R.string.filter_all))) {
                    Log.d("Products", "Attempting to filter by category: " + selectedCategory);
                    String cateID = categories.stream()
                            .filter(c -> c != null && c.getCateName() != null && c.getCateName().equals(selectedCategory))
                            .findFirst()
                            .map(productCategory::getCateID)
                            .orElse(null);
                    if (cateID != null) {
                        query = query.whereEqualTo("CateID", cateID);
                        hasFilter = true;
                        Log.d("Products", "Filtering by CateID: " + cateID);
                    } else {
                        Log.w("Products", "No CateID found for category: " + selectedCategory + ". Available categories: " +
                                (categories != null ? categories.stream().filter(c -> c != null && c.getCateName() != null)
                                        .map(c -> c.getCateName()).collect(Collectors.joining(", ")) : "null"));
                        Toast.makeText(this, getString(R.string.invalid_category), Toast.LENGTH_SHORT).show();
                    }
                }

                if (!selectedBrand.equals(defaultBrand) && !selectedBrand.equals(getString(R.string.filter_all))) {
                    String brandID = brands.stream()
                            .filter(b -> b != null && b.getBrandName() != null && b.getBrandName().equals(selectedBrand))
                            .findFirst()
                            .map(productBrand::getBrandID)
                            .orElse(null);
                    if (brandID != null) {
                        query = query.whereEqualTo("brandID", brandID);
                        hasFilter = true;
                        Log.d("Products", "Filtering by brandID: " + brandID);
                    } else {
                        Log.w("Products", "No brandID found for brand: " + selectedBrand);
                        Toast.makeText(this, getString(R.string.invalid_brand), Toast.LENGTH_SHORT).show();
                    }
                }

                if (!selectedVolume.equals(defaultVolume) && !selectedVolume.equals(getString(R.string.filter_all))) {
                    query = query.whereEqualTo("netContent", selectedVolume);
                    hasFilter = true;
                    Log.d("Products", "Filtering by netContent: " + selectedVolume);
                }

                if (!selectedWineType.equals(defaultWineType) && !selectedWineType.equals(getString(R.string.filter_all))) {
                    query = query.whereEqualTo("wineType", selectedWineType);
                    hasFilter = true;
                    Log.d("Products", "Filtering by wineType: " + selectedWineType);
                }

                if (!priceMin.isEmpty()) {
                    try {
                        double min = Double.parseDouble(priceMin);
                        if (min >= 0) {
                            query = query.whereGreaterThanOrEqualTo("productPrice", min);
                            hasFilter = true;
                            Log.d("Products", "Filtering by productPrice min: " + min);
                        } else {
                            Toast.makeText(this, getString(R.string.invalid_price_min), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Log.e("Products", "Invalid priceMin: " + priceMin, e);
                        Toast.makeText(this, getString(R.string.invalid_price_min), Toast.LENGTH_SHORT).show();
                    }
                }

                if (!priceMax.isEmpty()) {
                    try {
                        double max = Double.parseDouble(priceMax);
                        if (max >= 0 && (!priceMin.isEmpty() ? max >= Double.parseDouble(priceMin) : true)) {
                            query = query.whereLessThanOrEqualTo("productPrice", max);
                            hasFilter = true;
                            Log.d("Products", "Filtering by productPrice max: " + max);
                        } else {
                            Toast.makeText(this, getString(R.string.invalid_price_max), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Log.e("Products", "Invalid priceMax: " + priceMax, e);
                        Toast.makeText(this, getString(R.string.invalid_price_max), Toast.LENGTH_SHORT).show();
                    }
                }

                if (isBestSelling) {
                    query = query.whereEqualTo("isBestSelling", true);
                    hasFilter = true;
                    Log.d("Products", "Filtering by isBestSelling: true");
                }

                if (isOnSale) {
                    query = query.whereEqualTo("isOnSale", true);
                    hasFilter = true;
                    Log.d("Products", "Filtering by isOnSale: true");
                }

                if (!selectedSortBy.equals(defaultSortBy) && !selectedSortBy.equals(getString(R.string.filter_all))) {
                    if (selectedSortBy.equals(getString(R.string.sort_low_to_high))) {
                        query = query.orderBy("productPrice", Query.Direction.ASCENDING);
                        hasFilter = true;
                        Log.d("Products", "Sorting by productPrice: low to high");
                    } else if (selectedSortBy.equals(getString(R.string.sort_high_to_low))) {
                        query = query.orderBy("productPrice", Query.Direction.DESCENDING);
                        hasFilter = true;
                        Log.d("Products", "Sorting by productPrice: high to low");
                    }
                }

                if (!hasFilter) {
                    Toast.makeText(this, getString(R.string.no_filter_applied), Toast.LENGTH_SHORT).show();
                    loadProducts(null);
                } else {
                    loadProducts(query);
                }
            } catch (Exception e) {
                Log.e("Products", "Error applying filter: ", e);
                FirebaseCrashlytics.getInstance().recordException(e);
                Toast.makeText(this, getString(R.string.error_applying_filter), Toast.LENGTH_SHORT).show();
            }
            filterDialog.dismiss();
        });

        textResetFilter.setOnClickListener(v -> {
            textCategory.setText(defaultCategory);
            textSortBy.setText(defaultSortBy);
            textBrand.setText(defaultBrand);
            textVolume.setText(defaultVolume);
            textWineType.setText(defaultWineType);
            editPriceMin.setText("");
            editPriceMax.setText("");
            checkboxBestSelling.setChecked(false);
            checkboxOnSale.setChecked(false);
            Log.d("Products", "Filter reset");
            loadProducts(null);
        });

        filterDialog.setCanceledOnTouchOutside(true);
        filterDialog.show();
    }

    static class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
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