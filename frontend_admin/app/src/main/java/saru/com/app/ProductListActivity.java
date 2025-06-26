package saru.com.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import saru.com.adapters.ProductAdapter;
import saru.com.models.Brand;
import saru.com.models.Category;
import saru.com.models.Product;

public class ProductListActivity extends AppCompatActivity {
    private EditText edtSearch;
    private Button btnAddProduct;
    private Spinner spinnerCategory;
    private TextView txtCategoryDescription;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Category> categories;
    private List<Brand> brands;
    private Map<String, String> categoryMap;
    private Map<String, String> brandMap;
    private FirebaseFirestore db;

    private static final int REQUEST_CODE_ADD_EDIT_PRODUCT = 700;
    private static final String TAG = "ProductListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        loadCategoriesAndBrands();
        setupEvents();
    }

    private void initializeViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        txtCategoryDescription = findViewById(R.id.txtCategoryDescription);
        rvProducts = findViewById(R.id.rvProducts);
        txtCategoryDescription.setText("");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_product_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_product_management);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList,
                this::editProduct,
                this::showDeleteConfirmationDialog);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void loadCategoriesAndBrands() {
        categoryMap = new HashMap<>();
        categories = new ArrayList<>();
        categories.add(new Category("All", "All Categories"));
        categoryMap.put("All", "All Categories");

        db.collection("productCategory").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Category category = document.toObject(Category.class);
                            category.setCateID(document.getId());
                            categories.add(category);
                            categoryMap.put(category.getCateID(), category.getCateName());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing category document: " + document.getId() + ", Error: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Categories loaded: " + categories.size());
                    setupCategorySpinner();
                    loadBrands();
                })
                .addOnFailureListener(e -> showToast("Failed to load categories: " + e.getMessage()));
    }

    private void loadBrands() {
        brands = new ArrayList<>();
        brandMap = new HashMap<>();
        db.collection("productBrand").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Brand brand = document.toObject(Brand.class);
                            brand.setBrandID(document.getId());
                            brands.add(brand);
                            brandMap.put(brand.getBrandID(), brand.getBrandName());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing brand document: " + document.getId() + ", Error: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Brands loaded: " + brands.size());
                    productAdapter.setBrandMap((HashMap<String, String>) brandMap);
                    productAdapter.setCategoryMap((HashMap<String, String>) categoryMap);
                    loadProducts();
                })
                .addOnFailureListener(e -> showToast("Failed to load brands: " + e.getMessage()));
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getCateName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategoryName = parent.getItemAtPosition(position).toString();
                String selectedCategoryId = "All";
                for (Category category : categories) {
                    if (category.getCateName().equals(selectedCategoryName)) {
                        selectedCategoryId = category.getCateID();
                        break;
                    }
                }
                txtCategoryDescription.setText("");
                filterProductsByCategory(selectedCategoryId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtCategoryDescription.setText("");
                filterProductsByCategory("All");
            }
        });
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    List<Product> tempProductList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Product product = document.toObject(Product.class);
                            product.setProductID(document.getId());
                            tempProductList.add(product);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing product document: " + document.getId() + ", Error: " + e.getMessage());
                        }
                    }
                    productList.addAll(tempProductList);
                    productAdapter.updateList(productList);
                    fetchImageUrlsForProducts();
                    Log.d(TAG, "Initial products loaded: " + productList.size());
                })
                .addOnFailureListener(e -> showToast("Failed to load products: " + e.getMessage()));
    }

    private void fetchImageUrlsForProducts() {
        if (productList.isEmpty()) {
            return;
        }
        for (int i = 0; i < productList.size(); i++) {
            final int index = i;
            Product product = productList.get(index);
            if (product.getImageID() != null && !product.getImageID().isEmpty()) {
                db.collection("image").document(product.getImageID()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String imageUrl = documentSnapshot.getString("ProductImageCover");
                                product.setProductImageCover(imageUrl);
                                productAdapter.notifyItemChanged(index);
                                Log.d(TAG, "Image loaded for " + product.getProductName());
                            } else {
                                Log.w(TAG, "Image document not found for imageID: " + product.getImageID());
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch image for product: " + product.getProductName(), e));
            }
        }
    }

    private void setupEvents() {
        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_PRODUCT);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String currentCategoryId = categories.get(spinnerCategory.getSelectedItemPosition()).getCateID();
        for (Product product : productList) {
            boolean matchesSearch = (product.getProductName() != null && product.getProductName().toLowerCase().contains(query.toLowerCase()));
            boolean matchesCategory = (currentCategoryId.equals("All") || (product.getCateID() != null && product.getCateID().equals(currentCategoryId)));
            if (matchesSearch && matchesCategory) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    private void filterProductsByCategory(String cateID) {
        List<Product> filteredList = new ArrayList<>();
        String currentSearchQuery = edtSearch.getText().toString().trim();
        if (cateID.equals("All")) {
            for (Product product : productList) {
                if (product.getProductName() != null && product.getProductName().toLowerCase().contains(currentSearchQuery.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        } else {
            for (Product product : productList) {
                if (product.getCateID() != null && product.getCateID().equals(cateID) &&
                        product.getProductName() != null && product.getProductName().toLowerCase().contains(currentSearchQuery.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.updateList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT_PRODUCT && resultCode == RESULT_OK) {
            loadCategoriesAndBrands();
        }
    }

    private void editProduct(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("SELECTED_PRODUCT", product);
        startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_PRODUCT);
    }

    private void showDeleteConfirmationDialog(final Product productToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, productToDelete.getProductName()))
                .setPositiveButton(R.string.confirm_delete_yes, (dialog, which) -> deleteProduct(productToDelete))
                .setNegativeButton(R.string.confirm_delete_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct(Product product) {
        if (product != null && product.getProductID() != null) {
            db.collection("products").document(product.getProductID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast("Product deleted successfully!");
                        loadCategoriesAndBrands();
                    })
                    .addOnFailureListener(e -> showToast("Failed to delete product: " + e.getMessage()));
        } else {
            showToast("Cannot delete a null product or product with no ID.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}