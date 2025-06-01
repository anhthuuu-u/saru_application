package saru.com.app.connectors;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;
import saru.com.app.R;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private ListCartItems listCartItems;
    private Context context;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onItemChanged();
    }

    public CartAdapter(Context context, ListCartItems listCartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.listCartItems = listCartItems;
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
        CartItem cartItem = listCartItems.getCartItems().get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");

        // Log để debug
        Log.d("CartAdapter", "Binding item: " + cartItem.getName() +
                ", Position: " + position +
                ", Quantity: " + cartItem.getQuantity() +
                ", Selected: " + cartItem.isSelected() +
                ", Price: " + cartItem.getPrice());

        // Bind dữ liệu
        holder.productName.setText(cartItem.getName());
        holder.productPrice.setText(context.getString(R.string.product_cart_unit_price_label) + " " +
                formatter.format(cartItem.getPrice()) +
                context.getString(R.string.product_cart_currency));
        holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                formatter.format(cartItem.getTotalPrice()) +
                context.getString(R.string.product_cart_currency));
        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.itemCheckbox.setChecked(cartItem.isSelected());

        // Nút giảm số lượng
        holder.minusButton.setText(context.getString(R.string.minus_button_text));
        holder.minusButton.setOnClickListener(v -> {
            int qty = cartItem.getQuantity();
            if (qty > 1) {
                listCartItems.updateQuantity(position, qty - 1);
                holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                        formatter.format(cartItem.getTotalPrice()) +
                        context.getString(R.string.product_cart_currency));
                notifyListener();
                Log.d("CartAdapter", "Decreased quantity for " + cartItem.getName() +
                        " to " + cartItem.getQuantity() + ", New total: " + cartItem.getTotalPrice());
            }
        });

        // Nút tăng số lượng
        holder.plusButton.setText(context.getString(R.string.plus_button_text));
        holder.plusButton.setOnClickListener(v -> {
            int qty = cartItem.getQuantity();
            listCartItems.updateQuantity(position, qty + 1);
            holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
            holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                    formatter.format(cartItem.getTotalPrice()) +
                    context.getString(R.string.product_cart_currency));
            notifyListener();
            Log.d("CartAdapter", "Increased quantity for " + cartItem.getName() +
                    " to " + cartItem.getQuantity() + ", New total: " + cartItem.getTotalPrice());
        });

        // Checkbox chọn mục
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            notifyListener();
            Log.d("CartAdapter", "Checkbox changed for " + cartItem.getName() +
                    ", Selected: " + isChecked);
        });

        // Nút xóa mục
        holder.deleteButton.setOnClickListener(v -> {
            Log.d("CartAdapter", "Deleting item: " + cartItem.getName() +
                    ", Position: " + position);
            listCartItems.removeItem(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listCartItems.getItemCount());
            notifyListener();
        });
    }

    @Override
    public int getItemCount() {
        int size = listCartItems != null ? listCartItems.getItemCount() : 0;
        Log.d("CartAdapter", "Item count: " + size);
        return size;
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onItemChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheckbox;
        TextView productName;
        TextView productPrice;
        TextView productQuantity;
        TextView productTotal;
        Button minusButton;
        Button plusButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_cart_quantity);
            productTotal = itemView.findViewById(R.id.temporary_total);
            minusButton = itemView.findViewById(R.id.minus_button);
            plusButton = itemView.findViewById(R.id.plus_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}