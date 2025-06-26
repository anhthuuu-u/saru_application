package saru.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import saru.com.app.R;
import saru.com.models.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;
    private Map<String, String> brandMap;
    private OnProductClickListener editListener;
    private OnProductClickListener deleteListener;

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener editListener, OnProductClickListener deleteListener) {
        this.productList = productList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.brandMap = new HashMap<>();
    }

    public void setBrandMap(Map<String, String> brandMap) {
        this.brandMap = brandMap;
        notifyDataSetChanged();
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
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
        holder.txtProductPrice.setText(String.format("Price: %.2f", product.getProductPrice()));
        String typeText = product.getWineType() != null ? product.getWineType() : "Accessory";
        if (product.getAlcoholStrength() != null && !product.getAlcoholStrength().isEmpty()) {
            typeText += " (" + product.getAlcoholStrength() + ")";
        }
        holder.txtProductType.setText(typeText);
        String brandName = brandMap.getOrDefault(product.getBrandID(), product.getBrandID());
        holder.txtBrandName.setText("Brand: " + brandName);
        if (product.getImageID() != null && !product.getImageID().isEmpty()) {
            Picasso.get().load(product.getImageID()).placeholder(R.drawable.ic_placeholder).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_placeholder);
        }
        holder.btnEdit.setOnClickListener(v -> editListener.onClick(product));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductPrice, txtProductType, txtBrandName;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductType = itemView.findViewById(R.id.txtProductType);
            txtBrandName = itemView.findViewById(R.id.txtBrandName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}