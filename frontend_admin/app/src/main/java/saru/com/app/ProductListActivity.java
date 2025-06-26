package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
import saru.com.app.R;

public class ProductListActivity extends AppCompatActivity {
    private EditText edtSearch;
    private Button btnAddProduct;
    private Spinner spinnerCategory;
    private TextView txtCategoryDescription;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Category> categories;
    private Map<String, String> brandMap; // Maps brandID to brandName
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        setupSpinner();
        loadBrands();
        loadProducts();
    }

    private void initializeViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        txtCategoryDescription = findViewById(R.id.txtCategoryDescription);
        rvProducts = findViewById(R.id.rvProducts);

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(this, AddEditProductActivity.class)));

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

    private void setupSpinner() {
        categories = new ArrayList<>();
        Category allCategory = new Category("All", "All Categories", "Show all products");
        categories.add(allCategory);

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        db.collection("productCategory").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setCateID(document.getId());
                        categories.add(category);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load categories: " + e.getMessage()));

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = categories.get(position);
                filterProductsByCategory(selectedCategory.getCateID());
                txtCategoryDescription.setText(selectedCategory.getCateDescription());
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadBrands() {
        brandMap = new HashMap<>();
        db.collection("productBrand").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Brand brand = document.toObject(Brand.class);
                        brand.setBrandID(document.getId());
                        brandMap.put(brand.getBrandID(), brand.getBrandName());
                    }
                    productAdapter.setBrandMap(brandMap); // Update adapter with brand map
                    loadProducts(); // Reload products to reflect brand names
                })
                .addOnFailureListener(e -> showToast("Failed to load brands: " + e.getMessage()));
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList,
                product -> {
                    Intent intent = new Intent(this, AddEditProductActivity.class);
                    intent.putExtra("PRODUCT", product);
                    startActivity(intent);
                },
                product -> {
                    db.collection("products").document(product.getProductID()).delete()
                            .addOnSuccessListener(aVoid -> loadProducts())
                            .addOnFailureListener(e -> showToast("Failed to delete product"));
                });
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setProductID(document.getId());
                            productList.add(product);
                        }
                        productAdapter.updateList(productList);
                    } else {
                        showToast("Failed to load products: " + task.getException().getMessage());
                    }
                });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getProductName() != null && product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    private void filterProductsByCategory(String cateID) {
        List<Product> filteredList = new ArrayList<>();
        if (cateID.equals("All")) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getCateID() != null && product.getCateID().equals(cateID)) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.updateList(filteredList);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}