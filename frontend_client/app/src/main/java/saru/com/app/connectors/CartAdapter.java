package saru.com.app.connectors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.models.CartItem;
import saru.com.app.models.ListCartItems;
import saru.com.app.models.image;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private ListCartItems listCartItems;
    private Context context;
    private OnCartItemChangeListener listener;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private final int placeholderResId = R.mipmap.img_saru_cup;
    private final int errorResId = R.drawable.ic_ver_fail;

    public interface OnCartItemChangeListener {
        void onItemChanged();
    }

    public CartAdapter(Context context, ListCartItems listCartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.listCartItems = listCartItems;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_in_cart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = listCartItems.getCartItems().get(position);
        if (cartItem == null) {
            Log.e("CartAdapter", "CartItem is null at position: " + position);
            FirebaseCrashlytics.getInstance().recordException(new Exception("Null CartItem at position: " + position));
            return;
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        Log.d("CartAdapter", "Binding item: " + cartItem.getProductName() +
                ", Position: " + position +
                ", Quantity: " + cartItem.getQuantity() +
                ", Selected: " + cartItem.isSelected() +
                ", Price: " + cartItem.getProductPrice() +
                ", ImageID: " + cartItem.getImageID());

        // Tải hình ảnh
        loadImage(holder, cartItem);

        // Cập nhật UI
        holder.productName.setText(cartItem.getProductName() != null ? cartItem.getProductName() : "Unknown");
        holder.productPrice.setText(context.getString(R.string.product_cart_unit_price_label) + " " +
                formatter.format(cartItem.getProductPrice()) +
                context.getString(R.string.product_cart_currency));
        holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                formatter.format(cartItem.getTotalPrice()) +
                context.getString(R.string.product_cart_currency));
        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.itemCheckbox.setChecked(cartItem.isSelected());

        // Nút giảm số lượng
        holder.minusButton.setText(context.getString(R.string.minus_button_text));
        holder.minusButton.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                listCartItems.updateQuantity(position, cartItem.getQuantity() - 1);
                checkPlayServices(() -> updateFirestore(cartItem));
                holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                        formatter.format(cartItem.getTotalPrice()) +
                        context.getString(R.string.product_cart_currency));
                notifyListener();
                Log.d("CartAdapter", "Decreased quantity for " + cartItem.getProductName() +
                        " to " + cartItem.getQuantity() + ", New total: " + cartItem.getTotalPrice());
            }
        });

        // Nút tăng số lượng (bỏ kiểm tra tồn kho)
        holder.plusButton.setText(context.getString(R.string.plus_button_text));
        holder.plusButton.setOnClickListener(v -> {
            listCartItems.updateQuantity(position, cartItem.getQuantity() + 1);
            checkPlayServices(() -> updateFirestore(cartItem));
            holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
            holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                    formatter.format(cartItem.getTotalPrice()) +
                    context.getString(R.string.product_cart_currency));
            notifyListener();
            Log.d("CartAdapter", "Increased quantity for " + cartItem.getProductName() +
                    " to " + cartItem.getQuantity() + ", New total: " + cartItem.getTotalPrice());
        });

        // Checkbox chọn sản phẩm
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            checkPlayServices(() -> updateFirestore(cartItem));
            notifyListener();
            Log.d("CartAdapter", "Checkbox changed for " + cartItem.getProductName() +
                    ", Selected: " + isChecked);
        });

        // Nút xóa
        holder.deleteButton.setOnClickListener(v -> {
            int positionToRemove = holder.getAdapterPosition();
            if (positionToRemove != RecyclerView.NO_POSITION) {
                CartItem itemToDelete = listCartItems.getCartItems().get(positionToRemove);
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.dialog_delete_single_title))
                        .setMessage(context.getString(R.string.dialog_delete_single_message, itemToDelete.getProductName()))
                        .setPositiveButton(context.getString(R.string.dialog_confirm_delete), (dialog, which) -> {
                            Log.d("CartAdapter", "Deleting item: " + itemToDelete.getProductName() +
                                    ", Position: " + positionToRemove);
                            if (auth.getCurrentUser() == null) {
                                Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String accountID = auth.getCurrentUser().getUid();
                            db.collection("carts").document(accountID).collection("items")
                                    .document(itemToDelete.getProductID()) // Cập nhật để dùng productID
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("CartAdapter", "Deleted item from Firestore: " + itemToDelete.getProductName());
                                        listCartItems.removeItem(positionToRemove);
                                        notifyItemRemoved(positionToRemove);
                                        notifyItemRangeChanged(positionToRemove, listCartItems.getItemCount());
                                        notifyListener();
                                        Toast.makeText(context, context.getString(R.string.delete_item_noti, itemToDelete.getProductName()), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("CartAdapter", "Error deleting item: " + e.getMessage());
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                        .show();
            }
        });
    }

    private void loadImage(ViewHolder holder, CartItem cartItem) {
        if (cartItem.getImageID() == null || cartItem.getImageID().isEmpty()) {
            Log.w("CartAdapter", "imageID is null or empty for product: " + cartItem.getProductID());
            Glide.with(context)
                    .load(errorResId)
                    .into(holder.cartProductImage);
            return;
        }

        db.collection("image").document(cartItem.getImageID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        image img = documentSnapshot.toObject(image.class);
                        if (img != null && img.getProductImageCover() != null && !img.getProductImageCover().isEmpty()) {
                            Glide.with(context)
                                    .load(img.getProductImageCover())
                                    .placeholder(placeholderResId)
                                    .error(errorResId)
                                    .into(holder.cartProductImage);
                        } else {
                            Glide.with(context)
                                    .load(errorResId)
                                    .into(holder.cartProductImage);
                        }
                    } else {
                        Glide.with(context)
                                .load(errorResId)
                                .into(holder.cartProductImage);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartAdapter", "Error loading image: " + e.getMessage());
                    Glide.with(context)
                            .load(errorResId)
                            .into(holder.cartProductImage);
                });
    }

    private void updateFirestore(CartItem cartItem) {
        if (cartItem == null || cartItem.getProductID() == null || cartItem.getAccountID() == null) {
            Log.e("CartAdapter", "Invalid CartItem or ProductID/AccountID is null");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Invalid CartItem or ProductID/AccountID"));
            Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", cartItem.getQuantity());
        updates.put("selected", cartItem.isSelected());

        FirebaseFirestore.getInstance()
                .collection("carts")
                .document(cartItem.getAccountID())
                .collection("items")
                .document(cartItem.getProductID())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CartAdapter", "Firestore updated for product: " + cartItem.getProductName());
                })
                .addOnFailureListener(e -> {
                    Log.e("CartAdapter", "Error updating Firestore: " + e.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
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

    private void checkPlayServices(Runnable firebaseAction) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            firebaseAction.run();
        } else {
            Log.e("CartAdapter", "Google Play Services not available");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Google Play Services not available"));
            Toast.makeText(context, "Google Play Services không khả dụng", Toast.LENGTH_SHORT).show();
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
        ImageView cartProductImage;

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
            cartProductImage = itemView.findViewById(R.id.product_image); // Thống nhất ID
        }
    }
}