package saru.com.app.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.connectors.CustomerReviewAdapter;
import saru.com.app.connectors.ProductAdapter;
import saru.com.app.connectors.ProductImageAdapter;
import saru.com.app.models.CustomerReviewList;
import saru.com.app.models.image;
import saru.com.app.models.Product;
import saru.com.app.models.ProductComparisonItems;
import saru.com.app.models.productBrand;
import saru.com.app.models.productStatus;

public class ProductDetailActivity extends AppCompatActivity {

    private LinearLayout productDetailsContainer;
    private RecyclerView recyclerCustomerReviews, recyclerViewProducts, recyclerViewImages;
    private ViewPager2 viewPagerImages;
    private TextView showProductDetails, showCustomerReviews;
    private CustomerReviewAdapter customerReviewAdapter;
    private CustomerReviewList customerReviewList;
    private ProductImageAdapter imageAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();

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
        recyclerViewImages = findViewById(R.id.recycler_view_images);
        viewPagerImages = findViewById(R.id.view_pager_images);
        productDetailsContainer = findViewById(R.id.product_details_container);
        recyclerCustomerReviews = findViewById(R.id.recycler_customer_reviews);
        showProductDetails = findViewById(R.id.show_product_details);
        showCustomerReviews = findViewById(R.id.show_customer_reviews);

        if (recyclerViewProducts == null || recyclerViewImages == null || viewPagerImages == null ||
                recyclerCustomerReviews == null || productDetailsContainer == null ||
                showProductDetails == null || showCustomerReviews == null) {
            Log.e("ProductDetailActivity", "View not found");
            return;
        }

        Intent intent = getIntent();
        Product product = (Product) intent.getSerializableExtra("product");

        if (product != null) {
            loadProductDetails(product, productName, productBrand, productRating, stockStatus, productPrice,
                    alcoholStrength, netContent, wineType, ingredients, productDescription);
            loadImages(product.getImageID());
            loadRelatedProducts(product.getCateID());
        }

        // Setup ViewPager2
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter();
        viewPagerImages.setAdapter(pagerAdapter);

        // Setup RecyclerView for thumbnail images
        imageAdapter = new ProductImageAdapter();
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imageAdapter);
        int spacingInPixels2 = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewImages.addItemDecoration(new ItemSpacingDecoration(spacingInPixels2));

        // Sync ViewPager2 with RecyclerView
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                imageAdapter.setSelectedPosition(position);
            }
        });

        // Trong onCreate của ProductDetailActivity.java
        recyclerViewProducts = findViewById(R.id.recycler_view_products);
        if (recyclerViewProducts == null) {
            Log.e("ProductDetailActivity", "recycler_view_products not found in layout");
            return;
        }

        // Setup related products
        ProductAdapter relatedProductsAdapter = new ProductAdapter();
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewProducts.setAdapter(relatedProductsAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewProducts.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Setup customer reviews
        recyclerCustomerReviews.setLayoutManager(new LinearLayoutManager(this));
        customerReviewList = new CustomerReviewList();
        customerReviewAdapter = new CustomerReviewAdapter(new ArrayList<>());
        recyclerCustomerReviews.setAdapter(customerReviewAdapter);

        customerReviewList.getReviews().observe(this, reviews -> {
            customerReviewAdapter.updateReviews(reviews);
            customerReviewAdapter.notifyDataSetChanged();
            adjustRecyclerViewHeight();
        });

        adjustRecyclerViewHeight();
        recyclerCustomerReviews.addItemDecoration(new CenterItemDecoration());
        showProductDetails();

        showProductDetails.setOnClickListener(v -> showProductDetails());
        showCustomerReviews.setOnClickListener(v -> showCustomerReviews());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnCart = findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> startActivity(new Intent(ProductDetailActivity.this, ProductCart.class)));
        }

        if (addToCartButton != null) {
            addToCartButton.setOnClickListener(v -> showSuccessDialog(getString(R.string.dialog_add_to_cart_success)));
        }

        if (compareButton != null) {
            compareButton.setOnClickListener(v -> {
                if (product != null && product.getImageID() != null) {
                    db.collection("image").document(product.getImageID()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                image image = documentSnapshot.toObject(image.class);
                                String imageUrl = (image != null && image.getProductImageCover() != null)
                                        ? image.getProductImageCover() : "";
                                if (product.getBrandID() != null) {
                                    db.collection("productBrand").document(product.getBrandID()).get()
                                            .addOnSuccessListener(brandSnapshot -> {
                                                productBrand brand = brandSnapshot.toObject(productBrand.class);
                                                String brandName = (brand != null) ? brand.getBrandName() : "";
                                                if (product.getProductStatusID() != null) {
                                                    db.collection("productStatus").document(product.getProductStatusID()).get()
                                                            .addOnSuccessListener(statusSnapshot -> {
                                                                productStatus status = statusSnapshot.toObject(productStatus.class);
                                                                String statusText = (status != null) ? status.getProductStatus() : "";
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        brandName,
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        imageUrl
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("ProductDetailActivity", "Error loading status for comparison: " + e.getMessage());
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        brandName,
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        imageUrl
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            });
                                                } else {
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            brandName,
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            imageUrl
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProductDetailActivity", "Error loading brand for comparison: " + e.getMessage());
                                                if (product.getProductStatusID() != null) {
                                                    db.collection("productStatus").document(product.getProductStatusID()).get()
                                                            .addOnSuccessListener(statusSnapshot -> {
                                                                productStatus status = statusSnapshot.toObject(productStatus.class);
                                                                String statusText = (status != null) ? status.getProductStatus() : "";
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        "",
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        imageUrl
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            })
                                                            .addOnFailureListener(e2 -> {
                                                                Log.e("ProductDetailActivity", "Error loading status for comparison: " + e2.getMessage());
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        "",
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        imageUrl
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            });
                                                } else {
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            imageUrl
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                }
                                            });
                                } else {
                                    if (product.getProductStatusID() != null) {
                                        db.collection("productStatus").document(product.getProductStatusID()).get()
                                                .addOnSuccessListener(statusSnapshot -> {
                                                    productStatus status = statusSnapshot.toObject(productStatus.class);
                                                    String statusText = (status != null) ? status.getProductStatus() : "";
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            imageUrl
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("ProductDetailActivity", "Error loading status for comparison: " + e.getMessage());
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            imageUrl
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                });
                                    } else {
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                "",
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                imageUrl
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ProductDetailActivity", "Error loading image for comparison: " + product.getImageID(), e);
                                if (product.getBrandID() != null) {
                                    db.collection("productBrand").document(product.getBrandID()).get()
                                            .addOnSuccessListener(brandSnapshot -> {
                                                productBrand brand = brandSnapshot.toObject(productBrand.class);
                                                String brandName = (brand != null) ? brand.getBrandName() : "";
                                                if (product.getProductStatusID() != null) {
                                                    db.collection("productStatus").document(product.getProductStatusID()).get()
                                                            .addOnSuccessListener(statusSnapshot -> {
                                                                productStatus status = statusSnapshot.toObject(productStatus.class);
                                                                String statusText = (status != null) ? status.getProductStatus() : "";
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        brandName,
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        ""
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            })
                                                            .addOnFailureListener(e2 -> {
                                                                Log.e("ProductDetailActivity", "Error loading status for comparison: " + e2.getMessage());
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        brandName,
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        ""
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            });
                                                } else {
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            brandName,
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                }
                                            })
                                            .addOnFailureListener(e2 -> {
                                                Log.e("ProductDetailActivity", "Error loading brand for comparison: " + e2.getMessage());
                                                if (product.getProductStatusID() != null) {
                                                    db.collection("productStatus").document(product.getProductStatusID()).get()
                                                            .addOnSuccessListener(statusSnapshot -> {
                                                                productStatus status = statusSnapshot.toObject(productStatus.class);
                                                                String statusText = (status != null) ? status.getProductStatus() : "";
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        "",
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        ""
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            })
                                                            .addOnFailureListener(e3 -> {
                                                                Log.e("ProductDetailActivity", "Error loading status for comparison: " + e3.getMessage());
                                                                ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                                        product.getProductName(),
                                                                        "",
                                                                        product.getAlcoholStrength(),
                                                                        product.getNetContent(),
                                                                        product.getWineType(),
                                                                        product.getIngredients(),
                                                                        product.getProductTaste(),
                                                                        product.getProductPrice(),
                                                                        ""
                                                                );
                                                                ProductComparisonItems.addItem(comparisonItem);
                                                                showSuccessDialog(getString(R.string.dialog_compare_success));
                                                            });
                                                } else {
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                }
                                            });
                                } else {
                                    if (product.getProductStatusID() != null) {
                                        db.collection("productStatus").document(product.getProductStatusID()).get()
                                                .addOnSuccessListener(statusSnapshot -> {
                                                    productStatus status = statusSnapshot.toObject(productStatus.class);
                                                    String statusText = (status != null) ? status.getProductStatus() : "";
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                })
                                                .addOnFailureListener(e2 -> {
                                                    Log.e("ProductDetailActivity", "Error loading status for comparison: " + e2.getMessage());
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                });
                                    } else {
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                "",
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                ""
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    }
                                }
                            });
                } else {
                    Log.e("ProductDetailActivity", "imageID is null for comparison");
                    if (product != null && product.getBrandID() != null) {
                        db.collection("productBrand").document(product.getBrandID()).get()
                                .addOnSuccessListener(brandSnapshot -> {
                                    productBrand brand = brandSnapshot.toObject(productBrand.class);
                                    String brandName = (brand != null) ? brand.getBrandName() : "";
                                    if (product.getProductStatusID() != null) {
                                        db.collection("productStatus").document(product.getProductStatusID()).get()
                                                .addOnSuccessListener(statusSnapshot -> {
                                                    productStatus status = statusSnapshot.toObject(productStatus.class);
                                                    String statusText = (status != null) ? status.getProductStatus() : "";
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            brandName,
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("ProductDetailActivity", "Error loading status for comparison: " + e.getMessage());
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            brandName,
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                });
                                    } else {
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                brandName,
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                ""
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProductDetailActivity", "Error loading brand for comparison: " + e.getMessage());
                                    if (product.getProductStatusID() != null) {
                                        db.collection("productStatus").document(product.getProductStatusID()).get()
                                                .addOnSuccessListener(statusSnapshot -> {
                                                    productStatus status = statusSnapshot.toObject(productStatus.class);
                                                    String statusText = (status != null) ? status.getProductStatus() : "";
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                })
                                                .addOnFailureListener(e2 -> {
                                                    Log.e("ProductDetailActivity", "Error loading status for comparison: " + e2.getMessage());
                                                    ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                            product.getProductName(),
                                                            "",
                                                            product.getAlcoholStrength(),
                                                            product.getNetContent(),
                                                            product.getWineType(),
                                                            product.getIngredients(),
                                                            product.getProductTaste(),
                                                            product.getProductPrice(),
                                                            ""
                                                    );
                                                    ProductComparisonItems.addItem(comparisonItem);
                                                    showSuccessDialog(getString(R.string.dialog_compare_success));
                                                });
                                    } else {
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                "",
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                ""
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    }
                                });
                    } else {
                        if (product != null && product.getProductStatusID() != null) {
                            db.collection("productStatus").document(product.getProductStatusID()).get()
                                    .addOnSuccessListener(statusSnapshot -> {
                                        productStatus status = statusSnapshot.toObject(productStatus.class);
                                        String statusText = (status != null) ? status.getProductStatus() : "";
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                "",
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                ""
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ProductDetailActivity", "Error loading status for comparison: " + e.getMessage());
                                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                                product.getProductName(),
                                                "",
                                                product.getAlcoholStrength(),
                                                product.getNetContent(),
                                                product.getWineType(),
                                                product.getIngredients(),
                                                product.getProductTaste(),
                                                product.getProductPrice(),
                                                ""
                                        );
                                        ProductComparisonItems.addItem(comparisonItem);
                                        showSuccessDialog(getString(R.string.dialog_compare_success));
                                    });
                        } else {
                            ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                    product.getProductName(),
                                    "",
                                    product.getAlcoholStrength(),
                                    product.getNetContent(),
                                    product.getWineType(),
                                    product.getIngredients(),
                                    product.getProductTaste(),
                                    product.getProductPrice(),
                                    ""
                            );
                            ProductComparisonItems.addItem(comparisonItem);
                            showSuccessDialog(getString(R.string.dialog_compare_success));
                        }
                    }
                }
            });
        }

        View contentView = findViewById(android.R.id.content);
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void loadProductDetails(Product product, TextView productName, TextView productBrand,
                                    RatingBar productRating, TextView stockStatus, TextView productPrice,
                                    TextView alcoholStrength, TextView netContent, TextView wineType,
                                    TextView ingredients, TextView productDescription) {
        // Load tên sản phẩm
        if (productName != null) productName.setText(product.getProductName());

        // Load giá
        if (productPrice != null) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            productPrice.setText(formatter.format(product.getProductPrice()) + " " +
                    getString(R.string.currency_vnd));
        }

        // Load rating
        if (productRating != null) productRating.setRating(product.getCustomerRating());

        // Load alcohol strength
        if (alcoholStrength != null) alcoholStrength.setText(product.getAlcoholStrength());

        // Load net content
        if (netContent != null) netContent.setText(product.getNetContent());

        // Load wine type
        if (wineType != null) wineType.setText(product.getWineType());

        // Load ingredients
        if (ingredients != null) ingredients.setText(product.getIngredients());

        // Load description
        if (productDescription != null) productDescription.setText(product.getProductDescription());

        // Load brand từ brandID
        if (productBrand != null && product.getBrandID() != null) {
            db.collection("productBrand").document(product.getBrandID()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        productBrand brand = documentSnapshot.toObject(saru.com.app.models.productBrand.class);
                        if (brand != null) {
                            productBrand.setText(brand.getBrandName());
                            Log.d("ProductDetailActivity", "Brand loaded for brandID: " + product.getBrandID());
                        } else {
                            productBrand.setText(getString(R.string.no_brand_available));
                            Log.w("ProductDetailActivity", "No brand data for brandID: " + product.getBrandID());
                        }
                    })
                    .addOnFailureListener(e -> {
                        productBrand.setText(getString(R.string.error_loading_brand));
                        Log.e("ProductDetailActivity", "Error loading brand: " + e.getMessage());
                    });
        } else if (productBrand != null) {
            productBrand.setText(getString(R.string.no_brand_id));
            Log.w("ProductDetailActivity", "brandID is null for product: " + product.getProductID());
        }

        // Load productStatus từ productStatusID
        if (stockStatus != null && product.getProductStatusID() != null) {
            db.collection("productStatus").document(product.getProductStatusID()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        productStatus status = documentSnapshot.toObject(saru.com.app.models.productStatus.class);
                        if (status != null) {
                            stockStatus.setText(status.getProductStatus());
                            // Đặt màu chữ dựa trên productStatusColor
                            try {
                                stockStatus.setTextColor(android.graphics.Color.parseColor(status.getProductStatusColor()));
                            } catch (IllegalArgumentException e) {
                                stockStatus.setTextColor(getResources().getColor(android.R.color.black)); // Fallback màu mặc định
                                Log.w("ProductDetailActivity", "Invalid color format: " + status.getProductStatusColor());
                            }
                            Log.d("ProductDetailActivity", "Status loaded for productStatusID: " + product.getProductStatusID());
                        } else {
                            stockStatus.setText(getString(R.string.no_status_available));
                            stockStatus.setTextColor(getResources().getColor(android.R.color.black)); // Fallback màu mặc định
                            Log.w("ProductDetailActivity", "No status data for productStatusID: " + product.getProductStatusID());
                        }
                    })
                    .addOnFailureListener(e -> {
                        stockStatus.setText(getString(R.string.error_loading_status));
                        stockStatus.setTextColor(getResources().getColor(android.R.color.black)); // Fallback màu mặc định
                        Log.e("ProductDetailActivity", "Error loading status: " + e.getMessage());
                    });
        } else if (stockStatus != null) {
            stockStatus.setText(getString(R.string.no_status_id));
            stockStatus.setTextColor(getResources().getColor(android.R.color.black)); // Fallback màu mặc định
            Log.w("ProductDetailActivity", "productStatusID is null for product: " + product.getProductID());
        }
    }

    private void loadImages(String imageID) {
        if (imageID == null || imageID.isEmpty()) {
            Log.e("ProductDetailActivity", "imageID is null or empty");
            updateImageAdapters(new ArrayList<>());
            return;
        }
        db.collection("image").document(imageID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> urls = new ArrayList<>();
                    image image = documentSnapshot.toObject(image.class);
                    if (image != null) {
                        if (image.getProductImageCover() != null && !image.getProductImageCover().isEmpty()) {
                            urls.add(image.getProductImageCover());
                            Log.d("ProductDetailActivity", "Loaded productImageCover: " + image.getProductImageCover());
                        }
                        if (image.getProductImageSub1() != null && !image.getProductImageSub1().isEmpty()) {
                            urls.add(image.getProductImageSub1());
                            Log.d("ProductDetailActivity", "Loaded productImageSub1: " + image.getProductImageSub1());
                        }
                        if (image.getProductImageSub2() != null && !image.getProductImageSub2().isEmpty()) {
                            urls.add(image.getProductImageSub2());
                            Log.d("ProductDetailActivity", "Loaded productImageSub2: " + image.getProductImageSub2());
                        }
                    } else {
                        Log.w("ProductDetailActivity", "No image data for imageID: " + imageID);
                    }
                    updateImageAdapters(urls);
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductDetailActivity", "Error loading image for imageID: " + imageID, e);
                    updateImageAdapters(new ArrayList<>());
                });
    }

    private void updateImageAdapters(List<String> urls) {
        ImagePagerAdapter pagerAdapter = (ImagePagerAdapter) viewPagerImages.getAdapter();
        if (pagerAdapter != null) {
            pagerAdapter.updateImages(urls);
        }
        if (imageAdapter != null) {
            imageAdapter.updateImages(urls);
        }
    }

    private void loadRelatedProducts(String cateID) {
        if (cateID == null || cateID.isEmpty()) {
            Log.w("ProductDetailActivity", "CateID is null or empty, cannot load related products");
            ProductAdapter adapter = (ProductAdapter) recyclerViewProducts.getAdapter();
            if (adapter != null) {
                adapter.updateData(new ArrayList<>());
                adapter.notifyDataSetChanged();
                TextView noRelatedProductsText = findViewById(R.id.no_related_products_text);
                if (noRelatedProductsText != null) {
                    noRelatedProductsText.setVisibility(View.VISIBLE);
                    noRelatedProductsText.setText(R.string.no_related_products);
                }
            }
            return;
        }

        Log.d("ProductDetailActivity", "Loading related products for CateID: " + cateID);
        db.collection("products")
                .whereEqualTo("CateID", cateID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    String currentProductID = getIntent().getSerializableExtra("product") != null
                            ? ((Product) getIntent().getSerializableExtra("product")).getProductID() : null;

                    if (querySnapshot.isEmpty()) {
                        Log.w("ProductDetailActivity", "No documents found for CateID: " + cateID);
                        TextView noRelatedProductsText = findViewById(R.id.no_related_products_text);
                        if (noRelatedProductsText != null) {
                            noRelatedProductsText.setVisibility(View.VISIBLE);
                            noRelatedProductsText.setText(R.string.no_related_products);
                        }
                    } else {
                        for (DocumentSnapshot doc : querySnapshot) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                if (currentProductID == null || !product.getProductID().equals(currentProductID)) {
                                    products.add(product);
                                    Log.d("ProductDetailActivity", "Loaded related product: " + product.getProductName() +
                                            ", ID: " + product.getProductID() + ", CateID: " + product.getCateID());
                                } else {
                                    Log.d("ProductDetailActivity", "Skipping current product: " + product.getProductName());
                                }
                            } else {
                                Log.w("ProductDetailActivity", "Failed to parse product document: " + doc.getId());
                            }
                        }
                    }

                    ProductAdapter adapter = (ProductAdapter) recyclerViewProducts.getAdapter();
                    if (adapter != null) {
                        adapter.updateData(products);
                        adapter.notifyDataSetChanged();
                        Log.d("ProductDetailActivity", "Related products loaded: " + products.size());
                        TextView noRelatedProductsText = findViewById(R.id.no_related_products_text);
                        if (noRelatedProductsText != null) {
                            noRelatedProductsText.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                            noRelatedProductsText.setText(R.string.no_related_products);
                        }
                    } else {
                        Log.e("ProductDetailActivity", "ProductAdapter is null for recyclerViewProducts");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductDetailActivity", "Error loading related products for CateID: " + cateID, e);
                    ProductAdapter adapter = (ProductAdapter) recyclerViewProducts.getAdapter();
                    if (adapter != null) {
                        adapter.updateData(new ArrayList<>());
                        adapter.notifyDataSetChanged();
                        TextView noRelatedProductsText = findViewById(R.id.no_related_products_text);
                        if (noRelatedProductsText != null) {
                            noRelatedProductsText.setVisibility(View.VISIBLE);
                            noRelatedProductsText.setText(R.string.error_loading_related_products);
                        }
                    }
                });
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_success_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_ok_button), (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void showProductDetails() {
        productDetailsContainer.setVisibility(View.VISIBLE);
        recyclerCustomerReviews.setVisibility(View.GONE);
        showProductDetails.setTextColor(getResources().getColor(R.color.color_golden_yellow));
        showCustomerReviews.setTextColor(getResources().getColor(R.color.color_medium_gray));
    }

    private void showCustomerReviews() {
        productDetailsContainer.setVisibility(View.GONE);
        recyclerCustomerReviews.setVisibility(View.VISIBLE);
        showCustomerReviews.setTextColor(getResources().getColor(R.color.color_golden_yellow));
        showProductDetails.setTextColor(getResources().getColor(R.color.color_medium_gray));
    }

    private void adjustRecyclerViewHeight() {
        if (customerReviewAdapter == null || recyclerCustomerReviews == null) return;

        int itemCount = customerReviewAdapter.getItemCount();
        int maxVisibleItems = 3;

        View sampleView = customerReviewAdapter.createViewHolder(recyclerCustomerReviews, 0).itemView;
        sampleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int itemHeight = sampleView.getMeasuredHeight();

        int totalHeight;
        if (itemCount <= maxVisibleItems) {
            totalHeight = itemHeight * itemCount;
            recyclerCustomerReviews.setNestedScrollingEnabled(false);
        } else {
            totalHeight = itemHeight * maxVisibleItems;
            recyclerCustomerReviews.setNestedScrollingEnabled(true);
        }

        ViewGroup.LayoutParams params = recyclerCustomerReviews.getLayoutParams();
        params.height = totalHeight;
        recyclerCustomerReviews.setLayoutParams(params);
    }

    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<String> imageUrls = new ArrayList<>();

        public void updateImages(List<String> newImageUrls) {
            this.imageUrls = newImageUrls != null ? new ArrayList<>(newImageUrls) : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.img_saru_cup)
                    .error(R.drawable.ic_ver_fail)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

    private static class CenterItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
            if (itemCount == 0) return;

            int parentWidth = parent.getWidth();
            int itemWidth = view.getLayoutParams().width;
            int totalItemsWidth = itemWidth * itemCount;
            int remainingSpace = parentWidth - totalItemsWidth;

            if (remainingSpace > 0) {
                int offset = remainingSpace / (2 * itemCount);
                outRect.left = offset;
                outRect.right = offset;
            }
        }
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