package saru.com.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import saru.com.app.models.CartItem;
import saru.com.app.models.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onItemChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_in_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        // Bind data to views
        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("Đơn giá: " + product.getProductPrice());
        double total = parsePrice(product.getProductPrice()) * cartItem.getQuantity();
        holder.temporaryTotal.setText("Tổng: " + String.format("%.0f", total) + "đ");
        holder.itemCheckbox.setChecked(cartItem.isSelected());
        holder.quantityText.setText(String.valueOf(cartItem.getQuantity()));

        // Quantity selector listeners
        holder.minusButton.setOnClickListener(v -> {
            int qty = cartItem.getQuantity();
            if (qty > 1) {
                cartItem.setQuantity(qty - 1);
                holder.quantityText.setText(String.valueOf(cartItem.getQuantity()));
                double newTotal = parsePrice(product.getProductPrice()) * cartItem.getQuantity();
                holder.temporaryTotal.setText("Tổng: " + String.format("%.0f", newTotal) + "đ");
                if (listener != null) {
                    listener.onItemChanged();
                }
            }
        });

        holder.plusButton.setOnClickListener(v -> {
            int qty = cartItem.getQuantity();
            cartItem.setQuantity(qty + 1);
            holder.quantityText.setText(String.valueOf(cartItem.getQuantity()));
            double newTotal = parsePrice(product.getProductPrice()) * cartItem.getQuantity();
            holder.temporaryTotal.setText("Tổng: " + String.format("%.0f", newTotal) + "đ");
            if (listener != null) {
                listener.onItemChanged();
            }
        });

        // Checkbox listener
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            if (listener != null) {
                listener.onItemChanged();
            }
        });

        // Delete button listener
        holder.deleteButton.setOnClickListener(v -> {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            if (listener != null) {
                listener.onItemChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private double parsePrice(String price) {
        String cleanedPrice = price.replace("đ", "").replace(".", "").trim();
        try {
            return Double.parseDouble(cleanedPrice);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheckbox;
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        LinearLayout quantityLayout;
        TextView temporaryTotal;
        ImageButton deleteButton;
        TextView quantityText;
        Button minusButton;
        Button plusButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityLayout = itemView.findViewById(R.id.quantity_layout);
            temporaryTotal = itemView.findViewById(R.id.temporary_total);
            deleteButton = itemView.findViewById(R.id.delete_button);
            quantityText = itemView.findViewById(R.id.quantity_text);
            minusButton = itemView.findViewById(R.id.minus_button);
            plusButton = itemView.findViewById(R.id.plus_button);
        }
    }
}