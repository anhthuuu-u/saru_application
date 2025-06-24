package saru.com.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.models.Product;
import saru.com.app.models.productCategory;

public class SuccessfulPaymentActivity extends AppCompatActivity implements ProductAdapter.OnAddToCartListener {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;
    private TextView txtProductName1;
    private Button btnAddToCart1;

    // Biến để lưu order ID vừa tạo (nếu có)
    private String currentOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_payment);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        addViews();

        // Set up events
        addEvents();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load products
        loadProducts();

        // Lấy order ID từ Intent (nếu có)
        currentOrderId = getIntent().getStringExtra("orderId");
        if (currentOrderId != null) {
            loadOrderDetails(currentOrderId);
        }
    }

    private void loadOrderDetails(String orderId) {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> order = documentSnapshot.getData();
                        Log.d("SuccessfulPayment", "Order details: " + order);
                        // Có thể hiển thị thông tin order lên UI nếu cần
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SuccessfulPayment", "Error loading order: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void addViews() {
        recyclerView = findViewById(R.id.recycler_view_products);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2-column grid
        productAdapter = new ProductAdapter(this, this);
        recyclerView.setAdapter(productAdapter);

        // Add spacing between items (optional, for better UI)
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new Products.ItemSpacingDecoration(spacingInPixels));
    }

    private void addEvents() {
        // Optional: Add event for btnAddToCart1 if used
        if (btnAddToCart1 != null) {
            btnAddToCart1.setOnClickListener(v -> do_add_to_cart(v));
        }
    }

    private void loadProducts() {
        // Fetch products from Firestore
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            String cateID = product.getCateID();
                            if (cateID != null) {
                                // Fetch category name
                                db.collection("productCategory").document(cateID).get()
                                        .addOnSuccessListener(categoryDoc -> {
                                            productCategory category = categoryDoc.toObject(productCategory.class);
                                            if (category != null) {
                                                product.setCategory(category.getCateName());
                                            } else {
                                                product.setCategory(getString(R.string.no_category_available));
                                            }
                                            products.add(product);
                                            productAdapter.updateData(products);
                                            Log.d("SuccessfulPayment", "Product loaded: " + product.getProductName());
                                        })
                                        .addOnFailureListener(e -> {
                                            product.setCategory(getString(R.string.error_loading_category));
                                            products.add(product);
                                            productAdapter.updateData(products);
                                            Log.e("SuccessfulPayment", "Error loading category for cateID: " + cateID, e);
                                        });
                            } else {
                                product.setCategory(getString(R.string.no_category_id));
                                products.add(product);
                                productAdapter.updateData(products);
                                Log.w("SuccessfulPayment", "No cateID for product: " + product.getProductID());
                            }
                        }
                    }
                    productAdapter.updateData(products);
                    if (products.isEmpty()) {
                        Toast.makeText(this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SuccessfulPayment", "Error loading products: " + e.getMessage());
                    Toast.makeText(this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAddToCart(Product product) {
        // Handle add-to-cart action
        String productName = product.getProductName();
        String message = getString(R.string.title_the_add_to_cart) + productName + getString(R.string.title_add_to_cart_message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Optional: Add logic to save cart item to Firestore, similar to Products.java
    }

    public void onBackPressed(View view) {
        Intent intent = new Intent(this, ProductCart.class);
        startActivity(intent);
    }

    /**
     * Method được gọi khi bấm nút "See Order Detail"
     * Sẽ hiển thị danh sách đơn hàng với trạng thái "Delivery successful" (completed orders)
     */
    public void do_view_order_detail(View view) {
        Intent intent = new Intent(this, OrderListActivity.class);
        // Truyền statusID = "4" để hiển thị các đơn hàng đã hoàn thành
        intent.putExtra("statusID", "4"); // "4" = Delivery successful (completed)

        // Nếu có order ID hiện tại, có thể truyền thêm để highlight
        if (currentOrderId != null) {
            intent.putExtra("highlightOrderId", currentOrderId);
        }

        startActivity(intent);
    }

    public void do_cart(View view) {
        Intent intent = new Intent(this, ProductCart.class);
        startActivity(intent);
    }

    public void do_view_product_detail(View view) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        startActivity(intent);
    }

    public void do_add_to_cart(View view) {
        String productName = getString(R.string.title_trans_product_name);
        String message = getString(R.string.title_the_add_to_cart) + productName + getString(R.string.title_add_to_cart_message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void do_compare_product(View view) {
        Intent intent = new Intent(this, ProductComparison.class);
        startActivity(intent);
    }
}