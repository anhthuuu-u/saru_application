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
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.models.Product;
import saru.com.app.models.productBrand;
import saru.com.app.models.productCategory;

public class Products extends BaseActivity {
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;
    private SearchView searchBar;
    private List<productCategory> categories;
    private List<productBrand> brands;
    private List<String> volumes;
    private List<String> wineTypes;

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.menu_product;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);

        db = FirebaseFirestore.getInstance();
        categories = new ArrayList<>();
        brands = new ArrayList<>();
        volumes = new ArrayList<>();
        wineTypes = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter();
        recyclerView.setAdapter(productAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        loadProducts(null);
        loadFilterData();

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

        setupBottomNavigation();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadProducts(Query query) {
        if (query == null) {
            query = db.collection("products");
        }
        query.get().addOnSuccessListener(querySnapshot -> {
            List<Product> products = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot) {
                Product product = doc.toObject(Product.class);
                if (product != null) {
                    String cateID = product.getCateID();
                    if (cateID != null) {
                        db.collection("productCategory").document(cateID).get()
                                .addOnSuccessListener(categoryDoc -> {
                                    productCategory category = categoryDoc.toObject(productCategory.class);
                                    if (category != null) {
                                        product.setCategory(category.getCateName()); // Giả định Product có phương thức setCategoryName
                                    } else {
                                        product.setCategory(getString(R.string.no_category_available));
                                    }
                                    products.add(product);
                                    productAdapter.updateData(products);
                                    Log.d("Products", "Product loaded with category: " + product.getCategory());
                                })
                                .addOnFailureListener(e -> {
                                    product.setCategory(getString(R.string.error_loading_category));
                                    products.add(product);
                                    productAdapter.updateData(products);
                                    Log.e("Products", "Error loading category for cateID: " + cateID, e);
                                });
                    } else {
                        product.setCategory(getString(R.string.no_category_id));
                        products.add(product);
                        productAdapter.updateData(products);
                        Log.w("Products", "No cateID for product: " + product.getProductID());
                    }
                }
            }
            productAdapter.updateData(products); // Cập nhật lần cuối
            Log.d("Products", "Products loaded: " + products.size());
        }).addOnFailureListener(e -> Log.e("Products", "Error loading products: " + e.getMessage()));
    }

    private void loadFilterData() {
        // Load categories
        db.collection("productCategory").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        categories.add(doc.toObject(productCategory.class));
                    }
                    Log.d("Products", "Categories loaded: " + categories.size());
                })
                .addOnFailureListener(e -> Log.e("Products", "Error loading categories: " + e.getMessage()));

        // Load brands
        db.collection("productBrand").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        brands.add(doc.toObject(productBrand.class));
                    }
                    Log.d("Products", "Brands loaded: " + brands.size());
                })
                .addOnFailureListener(e -> Log.e("Products", "Error loading brands: " + e.getMessage()));

        // Load volumes
        db.collection("products").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String volume = doc.getString("netContent");
                        if (volume != null && !volumes.contains(volume)) {
                            volumes.add(volume);
                        }
                    }
                    Log.d("Products", "Volumes loaded: " + volumes.size());
                })
                .addOnFailureListener(e -> Log.e("Products", "Error loading volumes: " + e.getMessage()));

        // Load wineTypes
        db.collection("products").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String wineType = doc.getString("wineType");
                        if (wineType != null && !wineTypes.contains(wineType)) {
                            wineTypes.add(wineType);
                        }
                    }
                    Log.d("Products", "WineTypes loaded: " + wineTypes.size());
                })
                .addOnFailureListener(e -> Log.e("Products", "Error loading wineTypes: " + e.getMessage()));
    }

    private void showFilterDialog() {
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

        dropdownCategory.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownCategory);
            for (productCategory category : categories) {
                popupMenu.getMenu().add(category.getCateName());
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                textCategory.setText(item.getTitle());
                // Tìm cateID tương ứng
                String cateID = categories.stream()
                        .filter(c -> c.getCateName().equals(item.getTitle()))
                        .findFirst()
                        .map(productCategory::getCateID)
                        .orElse(null);
                Log.d("Products", "Selected category: " + item.getTitle() + ", cateID: " + cateID);
                return true;
            });
            popupMenu.show();
        });

        dropdownSortBy.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Products.this, dropdownSortBy);
            popupMenu.getMenu().add(getString(R.string.sort_low_to_high));
            popupMenu.getMenu().add(getString(R.string.sort_high_to_low));
            popupMenu.setOnMenuItemClickListener(item -> {
                textSortBy.setText(item.getTitle());
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

            if (!selectedCategory.equals(getString(R.string.filter_product_dialog_category))) {
                String cateID = categories.stream()
                        .filter(c -> c.getCateName().equals(selectedCategory))
                        .findFirst()
                        .map(productCategory::getCateID)
                        .orElse(null);
                if (cateID != null) {
                    query = query.whereEqualTo(getString(R.string.field_cateID), cateID);
                    Log.d("Products", "Filtering by cateID: " + cateID);
                } else {
                    Log.w("Products", "No cateID found for category: " + selectedCategory);
                }
            }

            if (!selectedBrand.equals(getString(R.string.title_filter_product_dialog_brand)) &&
                    !selectedBrand.equals(getString(R.string.filter_all))) {
                String brandID = brands.stream()
                        .filter(b -> b.getBrandName().equals(selectedBrand))
                        .findFirst()
                        .map(productBrand::getBrandID)
                        .orElse(null);
                if (brandID != null) {
                    query = query.whereEqualTo(getString(R.string.field_brandID), brandID);
                }
            }

            if (!selectedVolume.equals(getString(R.string.title_filter_product_dialog_volume)) &&
                    !selectedVolume.equals(getString(R.string.filter_all))) {
                query = query.whereEqualTo(getString(R.string.field_netContent), selectedVolume);
            }

            if (!selectedWineType.equals(getString(R.string.title_filter_product_dialog_wine_type)) &&
                    !selectedWineType.equals(getString(R.string.filter_all))) {
                query = query.whereEqualTo(getString(R.string.field_wineType), selectedWineType);
            }

            if (!priceMin.isEmpty()) {
                try {
                    double min = Double.parseDouble(priceMin);
                    query = query.whereGreaterThanOrEqualTo(getString(R.string.field_productPrice), min);
                } catch (NumberFormatException e) {
                    Log.e("Products", "Invalid priceMin: " + priceMin);
                }
            }

            if (!priceMax.isEmpty()) {
                try {
                    double max = Double.parseDouble(priceMax);
                    query = query.whereLessThanOrEqualTo(getString(R.string.field_productPrice), max);
                } catch (NumberFormatException e) {
                    Log.e("Products", "Invalid priceMax: " + priceMax);
                }
            }

            if (isBestSelling) {
                query = query.whereEqualTo(getString(R.string.field_isBestSelling), true);
            }

            if (isOnSale) {
                query = query.whereEqualTo(getString(R.string.field_isOnSale), true);
            }

            if (selectedSortBy.equals(getString(R.string.sort_low_to_high))) {
                query = query.orderBy(getString(R.string.field_productPrice), Query.Direction.ASCENDING);
            } else if (selectedSortBy.equals(getString(R.string.sort_high_to_low))) {
                query = query.orderBy(getString(R.string.field_productPrice), Query.Direction.DESCENDING);
            }

            loadProducts(query);
            filterDialog.dismiss();
        });

        textResetFilter.setOnClickListener(v -> {
            textCategory.setText(getString(R.string.filter_product_dialog_category));
            textSortBy.setText(getString(R.string.title_product_dialog_sort));
            textBrand.setText(getString(R.string.title_filter_product_dialog_brand));
            textVolume.setText(getString(R.string.title_filter_product_dialog_volume));
            textWineType.setText(getString(R.string.title_filter_product_dialog_wine_type));
            editPriceMin.setText("");
            editPriceMax.setText("");
            checkboxBestSelling.setChecked(false);
            checkboxOnSale.setChecked(false);
        });

        filterDialog.setCanceledOnTouchOutside(true);
        filterDialog.show();
    }

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