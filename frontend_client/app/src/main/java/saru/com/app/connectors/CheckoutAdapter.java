package saru.com.app.connectors;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;
import saru.com.app.models.CartItem;
import saru.com.app.models.image;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {
    private List<CartItem> selectedItems;
    private Context context;
    private FirebaseFirestore db;
    private final int placeholderResId = R.mipmap.img_saru_cup;
    private final int errorResId = R.drawable.ic_ver_fail;

    public CheckoutAdapter(Context context, List<CartItem> selectedItems) {
        this.context = context;
        this.selectedItems = selectedItems != null ? selectedItems : new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_for_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = selectedItems.get(position);
        if (item == null) {
            Log.e("CheckoutAdapter", "CartItem is null at position: " + position);
            return;
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        // Bind data
        holder.productName.setText(item.getProductName() != null ? item.getProductName() : "Unknown");
        holder.productQuantity.setText(String.valueOf(item.getQuantity()));
        holder.productTotal.setText(formatter.format(item.getTotalPrice()) + context.getString(R.string.product_cart_currency));

        // Load image
        if (item.getImageID() != null && !item.getImageID().isEmpty()) {
            db.collection("image").document(item.getImageID()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            image img = documentSnapshot.toObject(image.class);
                            if (img != null && img.getProductImageCover() != null && !img.getProductImageCover().isEmpty()) {
                                Glide.with(context)
                                        .load(img.getProductImageCover())
                                        .placeholder(placeholderResId)
                                        .error(errorResId)
                                        .into(holder.productImage);
                            } else {
                                Glide.with(context).load(errorResId).into(holder.productImage);
                            }
                        } else {
                            Glide.with(context).load(errorResId).into(holder.productImage);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CheckoutAdapter", "Error loading image: " + e.getMessage());
                        Glide.with(context).load(errorResId).into(holder.productImage);
                    });
        } else {
            Glide.with(context).load(errorResId).into(holder.productImage);
        }
    }

    @Override
    public int getItemCount() {
        return selectedItems.size();
    }

    public void updateData(List<CartItem> newItems) {
        this.selectedItems = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productQuantity;
        TextView productTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_cart_quantity);
            productTotal = itemView.findViewById(R.id.temporary_total);
        }
    }
}