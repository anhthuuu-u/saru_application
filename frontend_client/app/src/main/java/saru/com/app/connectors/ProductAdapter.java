package saru.com.app.connectors;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.activities.ProductDetailActivity;
import saru.com.app.models.image;
import saru.com.app.models.Product;
import saru.com.app.models.ProductComparisonItems;
import saru.com.app.models.productCategory;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private FirebaseFirestore db;
    private final int placeholderResId;
    private final int errorResId;

    public ProductAdapter() {
        products = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        placeholderResId = R.mipmap.img_saru_cup;
        errorResId = R.drawable.ic_ver_fail;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        // Load ảnh
        loadImage(holder, product);

        // Loại bỏ load category nếu không cần (tuỳ bạn)
        loadCategory(holder, product);

        // Load các field khác
        holder.textProductName.setText(product.getProductName());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.textProductPrice.setText(formatter.format(product.getProductPrice()) + " " +
                holder.itemView.getContext().getString(R.string.currency_vnd));
        holder.ratingBar.setRating(product.getCustomerRating());

        // Navigate to ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            holder.itemView.getContext().startActivity(intent);
        });

        // Add to cart
        holder.btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(),
                    holder.itemView.getContext().getString(R.string.dialog_add_to_cart_success),
                    Toast.LENGTH_SHORT).show();
        });

        // Compare
        holder.btnComparison.setOnClickListener(v -> {
            loadImageForComparison(holder, product);
        });
    }

    private void loadImage(ProductViewHolder holder, Product product) {
        if (product.getImageID() == null || product.getImageID().isEmpty()) {
            Log.w("ProductAdapter", "imageID is null or empty for product: " + product.getProductID());
            Glide.with(holder.itemView.getContext())
                    .load(errorResId)
                    .into(holder.imageProduct);
            return;
        }

        Log.d("ProductAdapter", "Attempting to load image for imageID: " + product.getImageID());
        db.collection("image").document(product.getImageID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        image image = documentSnapshot.toObject(image.class);
                        if (image != null && image.getProductImageCover() != null && !image.getProductImageCover().isEmpty()) {
                            Log.d("ProductAdapter", "Image loaded for imageID: " + product.getImageID() +
                                    ", URL: " + image.getProductImageCover());
                            Glide.with(holder.itemView.getContext())
                                    .load(image.getProductImageCover())
                                    .placeholder(placeholderResId)
                                    .error(errorResId)
                                    .into(holder.imageProduct);
                        } else {
                            Log.w("ProductAdapter", "No valid productImageCover for imageID: " + product.getImageID());
                            Glide.with(holder.itemView.getContext())
                                    .load(errorResId)
                                    .into(holder.imageProduct);
                        }
                    } else {
                        Log.w("ProductAdapter", "Image document does not exist for imageID: " + product.getImageID());
                        Glide.with(holder.itemView.getContext())
                                .load(errorResId)
                                .into(holder.imageProduct);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductAdapter", "Error loading image for imageID: " + product.getImageID(), e);
                    Glide.with(holder.itemView.getContext())
                            .load(errorResId)
                            .into(holder.imageProduct);
                });
    }

    private void loadCategory(ProductViewHolder holder, Product product) {
        if (product.getCateID() == null || product.getCateID().isEmpty()) {
            Log.e("ProductAdapter", "cateID is null or empty for product: " + product.getProductID());
            holder.textProductCategory.setText(
                    holder.itemView.getContext().getString(R.string.no_category_id));
            return;
        }
        db.collection("productCategory").document(product.getCateID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    productCategory category = documentSnapshot.toObject(productCategory.class);
                    if (category != null && category.getCateName() != null) {
                        holder.textProductCategory.setText(category.getCateName());
                        product.setCategory(category.getCateName()); // Cập nhật vào model Product
                        Log.d("ProductAdapter", "Category loaded: " + category.getCateName() + " for cateID: " + product.getCateID());
                    } else {
                        Log.w("ProductAdapter", "No category data for cateID: " + product.getCateID());
                        holder.textProductCategory.setText(
                                holder.itemView.getContext().getString(R.string.no_category_available));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductAdapter", "Error loading category for cateID: " + product.getCateID(), e);
                    holder.textProductCategory.setText(
                            holder.itemView.getContext().getString(R.string.error_loading_category));
                });
    }

    private void loadImageForComparison(ProductViewHolder holder, Product product) {
        if (product.getImageID() != null) {
            db.collection("image").document(product.getImageID()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        image image = documentSnapshot.toObject(image.class);
                        String imageUrl = (image != null && image.getProductImageCover() != null)
                                ? image.getProductImageCover() : "";
                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                product.getProductName(),
                                "", // Không dùng productBrand nữa
                                product.getAlcoholStrength(),
                                product.getNetContent(),
                                product.getWineType(),
                                product.getIngredients(),
                                product.getProductTaste(),
                                product.getProductPrice(),
                                imageUrl
                        );
                        ProductComparisonItems.addItem(comparisonItem);
                        Toast.makeText(holder.itemView.getContext(),
                                holder.itemView.getContext().getString(R.string.dialog_compare_success),
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProductAdapter", "Error loading image for comparison: " + product.getImageID(), e);
                        ProductComparisonItems comparisonItem = new ProductComparisonItems(
                                product.getProductName(),
                                "", // Không dùng productBrand nữa
                                product.getAlcoholStrength(),
                                product.getNetContent(),
                                product.getWineType(),
                                product.getIngredients(),
                                product.getProductTaste(),
                                product.getProductPrice(),
                                ""
                        );
                        ProductComparisonItems.addItem(comparisonItem);
                        Toast.makeText(holder.itemView.getContext(),
                                holder.itemView.getContext().getString(R.string.dialog_compare_success),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("ProductAdapter", "imageID is null for comparison: " + product.getProductID());
            ProductComparisonItems comparisonItem = new ProductComparisonItems(
                    product.getProductName(),
                    "", // Không dùng productBrand nữa
                    product.getAlcoholStrength(),
                    product.getNetContent(),
                    product.getWineType(),
                    product.getIngredients(),
                    product.getProductTaste(),
                    product.getProductPrice(),
                    ""
            );
            ProductComparisonItems.addItem(comparisonItem);
            Toast.makeText(holder.itemView.getContext(),
                    holder.itemView.getContext().getString(R.string.dialog_compare_success),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts != null ? new ArrayList<>(newProducts) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName;
        RatingBar ratingBar;
        TextView textProductPrice;
        TextView textProductCategory; // Giữ category nếu cần
        Button btnAddToCart;
        ImageButton btnComparison;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProductName = itemView.findViewById(R.id.text_product_name);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
            textProductCategory = itemView.findViewById(R.id.txtProductCategory); // Giữ nếu cần
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnComparison = itemView.findViewById(R.id.btnComparison);
        }
    }
}