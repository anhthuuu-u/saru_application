package saru.com.adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.Target;
import saru.com.app.R;
import saru.com.models.Product;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;
    private HashMap<String, String> categoryMap;
    private HashMap<String, String> brandMap;
    private final OnProductClickListener editListener;
    private final OnProductClickListener deleteListener;

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener editListener, OnProductClickListener deleteListener) {
        this.productList = productList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.categoryMap = new HashMap<>();
        this.brandMap = new HashMap<>();
    }

    public void setCategoryMap(HashMap<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public void setBrandMap(HashMap<String, String> brandMap) {
        this.brandMap = brandMap;
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.txtProductName.setText(product.getProductName());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.txtProductPrice.setText(formatter.format(product.getProductPrice()));
        String categoryName = categoryMap.getOrDefault(product.getCateID(), "N/A");
        holder.txtProductCategory.setText(String.format("Category: %s", categoryName));
        String brandName = brandMap.getOrDefault(product.getBrandID(), "N/A");
        holder.txtProductBrand.setText(String.format("Brand: %s", brandName));

        String imageUrl = product.getProductImageCover();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ProductAdapter", "Loading image for product " + product.getProductName() + ": " + imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imgProduct);
        } else {
            Log.w("ProductAdapter", "No cover image for product: " + product.getProductName());
            holder.imgProduct.setImageResource(R.drawable.ic_placeholder);
        }

        holder.btnEdit.setOnClickListener(v -> editListener.onClick(product));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtProductCategory, txtProductBrand;
        ImageView imgProduct;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductCategory = itemView.findViewById(R.id.txtProductCategory);
            txtProductBrand = itemView.findViewById(R.id.txtProductBrand);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}