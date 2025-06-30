package saru.com.app.connectors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.models.CartItem;
import saru.com.app.models.CartManager;
import saru.com.app.models.image;
import saru.com.app.activities.LoginActivity;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private static final String TAG = "CartAdapter";
    private final Context context;
    private final CartManager cartManager;
    private final OnCartItemChangeListener listener;
    private final FirebaseFirestore db;
    private final int placeholderResId = R.mipmap.img_saru_cup;
    private final int errorResId = R.drawable.ic_ver_fail;

    public interface OnCartItemChangeListener {
        void onItemChanged();
    }

    public CartAdapter(Context context, CartManager cartManager, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartManager = cartManager;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
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
        if (cartManager == null || cartManager.getCartItems() == null || position >= cartManager.getCartItems().size()) {
            Log.e(TAG, "Invalid cartManager or cartItems at position: " + position);
            FirebaseCrashlytics.getInstance().recordException(new Exception("Invalid cartManager or cartItems at position: " + position));
            Toast.makeText(context, "Lỗi hiển thị sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        CartItem cartItem = cartManager.getCartItems().get(position);
        if (cartItem == null) {
            Log.e(TAG, "CartItem is null at position: " + position);
            FirebaseCrashlytics.getInstance().recordException(new Exception("Null CartItem at position: " + position));
            Toast.makeText(context, "Lỗi hiển thị sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        Log.d(TAG, "Binding item: " + cartItem.getProductName() +
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
                formatter.format(cartItem.getProductPrice() * cartItem.getQuantity()) +
                context.getString(R.string.product_cart_currency));
        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));

        // Cập nhật checkbox mà không kích hoạt listener
        holder.itemCheckbox.setOnCheckedChangeListener(null);
        holder.itemCheckbox.setChecked(cartItem.isSelected());
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            updateFirestoreItem(cartItem);
            // Sử dụng post để tránh gọi notify trong lúc layout
            holder.itemView.post(() -> notifyListener());
            Log.d(TAG, "Checkbox changed for " + cartItem.getProductName() +
                    ", Selected: " + isChecked);
        });

        // Nút giảm số lượng
        holder.minusButton.setText(context.getString(R.string.minus_button_text));
        holder.minusButton.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                updateFirestoreItem(cartItem);
                holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                        formatter.format(cartItem.getProductPrice() * cartItem.getQuantity()) +
                        context.getString(R.string.product_cart_currency));
                holder.itemView.post(() -> notifyListener());
                Log.d(TAG, "Decreased quantity for " + cartItem.getProductName() +
                        " to " + cartItem.getQuantity() + ", New total: " + (cartItem.getProductPrice() * cartItem.getQuantity()));
            }
        });

        // Nút tăng số lượng
        holder.plusButton.setText(context.getString(R.string.plus_button_text));
        holder.plusButton.setOnClickListener(v -> {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            updateFirestoreItem(cartItem);
            holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
            holder.productTotal.setText(context.getString(R.string.product_cart_total_label) + " " +
                    formatter.format(cartItem.getProductPrice() * cartItem.getQuantity()) +
                    context.getString(R.string.product_cart_currency));
            holder.itemView.post(() -> notifyListener());
            Log.d(TAG, "Increased quantity for " + cartItem.getProductName() +
                    " to " + cartItem.getQuantity() + ", New total: " + (cartItem.getProductPrice() * cartItem.getQuantity()));
        });

        // Nút xóa
        holder.deleteButton.setOnClickListener(v -> {
            int positionToRemove = holder.getAdapterPosition();
            if (positionToRemove != RecyclerView.NO_POSITION) {
                CartItem itemToDelete = cartManager.getCartItems().get(positionToRemove);
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.dialog_delete_single_title))
                        .setMessage(context.getString(R.string.dialog_delete_single_message, itemToDelete.getProductName()))
                        .setPositiveButton(context.getString(R.string.dialog_confirm_delete), (dialog, which) -> {
                            Log.d(TAG, "Initiating delete for item: " + itemToDelete.getProductName() +
                                    ", Position: " + positionToRemove);
                            String accountID = cartManager.getCurrentAccountID();
                            if (accountID == null) {
                                Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, LoginActivity.class);
                                context.startActivity(intent);
                                if (context instanceof AppCompatActivity) {
                                    ((AppCompatActivity) context).finish();
                                }
                                return;
                            }
                            // Xóa item khỏi danh sách cục bộ trước
                            if (positionToRemove < cartManager.getCartItems().size()) {
                                cartManager.removeItem(positionToRemove);
                                holder.itemView.post(() -> {
                                    notifyItemRemoved(positionToRemove);
                                    notifyItemRangeChanged(positionToRemove, cartManager.getItemCount());
                                    notifyListener();
                                    Log.d(TAG, "UI updated locally after deletion, new item count: " + cartManager.getItemCount());
                                });
                            }
                            // Xóa trên Firestore
                            db.collection("carts").document(accountID).collection("items")
                                    .document(itemToDelete.getProductID())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Successfully deleted item from Firestore: " + itemToDelete.getProductName());
                                        cartManager.loadCartItemsFromFirestore(success -> {
                                            holder.itemView.post(() -> {
                                                if (success) {
                                                    notifyDataSetChanged();
                                                    notifyListener();
                                                    CartManager.getInstance().updateAllBadges();
                                                    Log.d(TAG, "UI updated after Firestore sync, new item count: " + cartManager.getItemCount());
                                                } else {
                                                    Log.e(TAG, "Failed to sync cart items after deletion");
                                                    notifyDataSetChanged();
                                                    notifyListener();
                                                }
                                                Toast.makeText(context, context.getString(R.string.delete_item_noti, itemToDelete.getProductName()), Toast.LENGTH_SHORT).show();
                                            });
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting item from Firestore: " + e.getMessage());
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                        cartManager.addItem(itemToDelete);
                                        holder.itemView.post(() -> {
                                            notifyDataSetChanged();
                                            notifyListener();
                                        });
                                    });
                        })
                        .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                        .show();
            }
        });
    }

    private void loadImage(ViewHolder holder, CartItem cartItem) {
        if (cartItem.getImageID() == null || cartItem.getImageID().isEmpty()) {
            Log.w(TAG, "imageID is null or empty for product: " + cartItem.getProductID());
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
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    Glide.with(context)
                            .load(errorResId)
                            .into(holder.cartProductImage);
                });
    }

    private void updateFirestoreItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getProductID() == null) {
            Log.e(TAG, "Invalid CartItem or ProductID is null");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Invalid CartItem or ProductID"));
            Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String accountID = cartManager.getCurrentAccountID();
        if (accountID == null) {
            Log.e(TAG, "AccountID is null, user may not be logged in");
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).finish();
            }
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", cartItem.getQuantity());
        updates.put("selected", cartItem.isSelected());

        Log.d(TAG, "Updating Firestore with accountID: " + accountID + ", productID: " + cartItem.getProductID());
        db.collection("carts")
                .document(accountID)
                .collection("items")
                .document(cartItem.getProductID())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore updated for product: " + cartItem.getProductName());
                    CartManager.getInstance().updateAllBadges();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating Firestore: " + e.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        int size = cartManager != null ? cartManager.getItemCount() : 0;
        Log.d(TAG, "Item count: " + size);
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
            cartProductImage = itemView.findViewById(R.id.product_image);
        }
    }
}