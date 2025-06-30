package saru.com.app.models;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final List<TextView> badgeViews = new ArrayList<>();
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentAccountID;

    public interface OnInitializationCompleteListener {
        void onInitializationComplete(boolean success);
    }

    private CartManager() {
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setUser(String uid) {
        this.currentAccountID = uid;
        Log.d(TAG, "Set user with accountID: " + uid);
    }

    public void initialize(Context context, OnInitializationCompleteListener listener) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (currentAccountID == null && auth.getCurrentUser() != null) {
            currentAccountID = auth.getCurrentUser().getUid();
            Log.d(TAG, "Initialized accountID from FirebaseAuth: " + currentAccountID);
        }
        if (currentAccountID == null) {
            Log.e(TAG, "Cannot initialize, currentAccountID is null");
            listener.onInitializationComplete(false);
            return;
        }
        Log.d(TAG, "Initializing with accountID: " + currentAccountID);
        loadCartItemsFromFirestore(listener);
        startListeningToCartChanges();
    }

    public String getCurrentAccountID() {
        return currentAccountID;
    }

    public void addBadgeView(TextView badgeView) {
        if (!badgeViews.contains(badgeView)) {
            badgeViews.add(badgeView);
            updateBadge(badgeView);
        }
    }

    public void updateAllBadges() {
        for (TextView badgeView : badgeViews) {
            badgeView.post(() -> updateBadge(badgeView));
        }
        Log.d(TAG, "Updated badges with count: " + getItemCount());
    }

    private void updateBadge(TextView badgeView) {
        int count = getItemCount();
        Log.d(TAG, "Updated badge: " + badgeView.getId() + ", Count: " + count);
        if (count > 0) {
            badgeView.setVisibility(View.VISIBLE);
            badgeView.setText(String.valueOf(count));
        } else {
            badgeView.setVisibility(View.GONE);
        }
    }

    public void removeBadgeView(TextView badgeView) {
        badgeViews.remove(badgeView);
        Log.d(TAG, "Removed badge view: " + badgeView.getId());
    }

    public void loadCartItemsFromFirestore(OnInitializationCompleteListener listener) {
        if (currentAccountID == null) {
            Log.e(TAG, "Cannot load cart items, currentAccountID is null");
            listener.onInitializationComplete(false);
            return;
        }
        db.collection("carts").document(currentAccountID).collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartItems.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null && item.getProductID() != null) {
                                item.setAccountID(currentAccountID);
                                Log.d(TAG, "Loaded item: " + item.getProductName() + ", AccountID: " + item.getAccountID());
                                cartItems.add(item);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cart item: " + doc.getId(), e);
                            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    Log.d(TAG, "Cart updated with " + cartItems.size() + " items");
                    updateAllBadges();
                    listener.onInitializationComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart items: " + e.getMessage());
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(context, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    listener.onInitializationComplete(false);
                });
    }

    public void startListeningToCartChanges() {
        if (currentAccountID == null) {
            Log.e(TAG, "Cannot start listening, currentAccountID is null");
            return;
        }
        db.collection("carts").document(currentAccountID).collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed: " + e.getMessage());
                        com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e);
                        return;
                    }
                    if (snapshots != null) {
                        List<CartItem> newItems = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            try {
                                CartItem item = doc.toObject(CartItem.class);
                                if (item != null && item.getProductID() != null) {
                                    item.setAccountID(currentAccountID);
                                    Log.d(TAG, "Snapshot item: " + item.getProductName() + ", ProductID: " + item.getProductID());
                                    newItems.add(item);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parsing cart item: " + doc.getId(), ex);
                                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(ex);
                            }
                        }
                        // Cập nhật cartItems, tránh trùng lặp
                        synchronized (cartItems) {
                            cartItems.clear();
                            for (CartItem newItem : newItems) {
                                boolean exists = false;
                                for (CartItem existingItem : cartItems) {
                                    if (existingItem.getProductID().equals(newItem.getProductID())) {
                                        exists = true;
                                        existingItem.setQuantity(newItem.getQuantity());
                                        existingItem.setSelected(newItem.isSelected());
                                        Log.d(TAG, "Updated existing item: " + newItem.getProductName() + ", Quantity: " + newItem.getQuantity());
                                        break;
                                    }
                                }
                                if (!exists) {
                                    cartItems.add(newItem);
                                    Log.d(TAG, "Added new item from snapshot: " + newItem.getProductName());
                                }
                            }
                        }
                        Log.d(TAG, "Cart updated with " + cartItems.size() + " items via listener");
                        updateAllBadges();
                    }
                });
    }

    private void updateFirestoreItem(CartItem item) {
        if (currentAccountID == null || item == null || item.getProductID() == null) {
            Log.e(TAG, "Invalid parameters for Firestore update");
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(new Exception("Invalid parameters for Firestore update"));
            Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("carts").document(currentAccountID).collection("items")
                .document(item.getProductID())
                .update("selected", item.isSelected(), "quantity", item.getQuantity())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated selected state and quantity for item: " + item.getProductID()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating item: " + e.getMessage());
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    public void setAllSelected(boolean isSelected) {
        for (CartItem item : cartItems) {
            item.setSelected(isSelected);
            updateFirestoreItem(item);
        }
        updateAllBadges();
    }

    public double calculateTotalPrice() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getProductPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            updateAllBadges();
        }
    }

    public void addItem(CartItem item) {
        if (item != null && item.getProductID() != null) {
            synchronized (cartItems) {
                for (CartItem existingItem : cartItems) {
                    if (existingItem.getProductID().equals(item.getProductID())) {
                        Log.d(TAG, "Item already exists in cart: " + item.getProductName() + ", increasing quantity");
                        existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                        updateFirestoreItem(existingItem);
                        updateAllBadges();
                        return;
                    }
                }
                cartItems.add(item);
                updateAllBadges();
                Log.d(TAG, "Added new item to cart: " + item.getProductName());
            }
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getItemCount() {
        int count = cartItems.size();
        Log.d(TAG, "Current item count: " + count);
        return count;
    }
}