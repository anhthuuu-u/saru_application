package saru.com.app.connectors;

import android.content.Intent;
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

import saru.com.app.Homepage;
import saru.com.app.ProductDetailActivity;
import saru.com.app.R;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.Product;
import saru.com.app.models.ProductList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final ProductList productList;

    public ProductAdapter() {
        productList = new ProductList();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.getProducts().get(position);
        holder.textProductName.setText(product.getProductName());
        holder.textProductPrice.setText(product.getProductPrice());
        holder.ratingBar.setRating(product.getCustomerRating());

        // Handle click event to navigate to ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            holder.itemView.getContext().startActivity(intent);
        });
        // Handle btnAddToCart click
        holder.btnAddToCart.setOnClickListener(v -> {
            Product p= productList.getProducts().get(holder.getAdapterPosition());
            // Gửi sự kiện đến Homepage (cần context là Homepage)
            if (v.getContext() instanceof Homepage) {
                ((Homepage) v.getContext()).addToCart(p);
            }
            Toast.makeText(holder.itemView.getContext(), "Add " + p.getProductName() + " to cart sucessfully!", Toast.LENGTH_SHORT).show();
        });

        // Handle btnComparison click
        holder.btnComparison.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Compare " + product.getProductName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.getProducts().size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName;
        RatingBar ratingBar;
        TextView textProductPrice;
        Button btnAddToCart;
        ImageButton btnComparison;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProductName = itemView.findViewById(R.id.text_product_name);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnComparison = itemView.findViewById(R.id.btnComparison);
        }
    }
}