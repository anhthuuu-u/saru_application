package saru.com.app.models;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CartManager {
    private static final String TAG = "CartManager";
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final List<TextView> badgeViews = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration cartListener;
    private String currentAccountID;

    private CartManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void initialize(Context context, Consumer<Boolean> onDataLoaded) {
        if (auth.getCurrentUser() != null) {
            currentAccountID = auth.getCurrentUser().getUid();
            loadCartItemsFromFirestore(onDataLoaded);
            startListeningToCartChanges();
        } else {
            Log.d(TAG, "No user logged in, clearing cart items");
            cartItems.clear();
            updateAllBadges();
            if (onDataLoaded != null) {
                onDataLoaded.accept(false);
            }
        }
    }

    public void loadCartItemsFromFirestore(Consumer<Boolean> onDataLoaded) {
        if (currentAccountID == null) {
            Log.e(TAG, "No account ID, cannot load cart items");
            if (onDataLoaded != null) {
                onDataLoaded.accept(false);
            }
            return;
        }

        db.collection("carts").document(currentAccountID)
                .collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartItems.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null && item.getProductID() != null) {
                                cartItems.add(item);
                                Log.d(TAG, "Loaded item: " + item.getProductName());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cart item: " + doc.getId(), e);
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                    Log.d(TAG, "Loaded " + cartItems.size() + " items from Firestore");
                    updateAllBadges();
                    startListeningToCartChanges(); // Khởi động lại listener sau khi tải
                    if (onDataLoaded != null) {
                        onDataLoaded.accept(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart items: " + e.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(e);
                    if (onDataLoaded != null) {
                        onDataLoaded.accept(false);
                    }
                });
    }

    public void addBadgeView(TextView badgeView) {
        if (badgeView != null && !badgeViews.contains(badgeView)) {
            badgeViews.add(badgeView);
            updateBadge(badgeView);
            Log.d(TAG, "Added badge view: " + badgeView.getId());
        }
    }

    public void removeBadgeView(TextView badgeView) {
        badgeViews.remove(badgeView);
        Log.d(TAG, "Removed badge view: " + badgeView.getId());
    }

    public void addItem(CartItem item) {
        if (item == null || item.getProductID() == null) {
            Log.e(TAG, "Cannot add null item or item with null productID");
            return;
        }
        int index = findItemIndex(item.getProductID());
        if (index != -1) {
            cartItems.set(index, item);
        } else {
            cartItems.add(item);
        }
        updateAllBadges();
        updateFirestoreItem(item);
        Log.d(TAG, "Added/Updated item: " + item.getProductName());
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            CartItem item = cartItems.get(position);
            stopListening(); // Tạm dừng listener để tránh xung đột
            cartItems.remove(position);
            updateAllBadges();
            if (currentAccountID != null) {
                db.collection("carts").document(currentAccountID).collection("items")
                        .document(item.getProductID())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Deleted item from Firestore: " + item.getProductID());
                            loadCartItemsFromFirestore(null); // Đồng bộ lại danh sách
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting item from Firestore: " + e.getMessage());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            addItem(item); // Phục hồi nếu xóa thất bại
                        });
            }
            Log.d(TAG, "Removed item from local cart: " + item.getProductName() + ", Position: " + position);
        } else {
            Log.e(TAG, "Invalid position for removeItem: " + position);
        }
    }

    public void clear() {
        if (currentAccountID != null) {
            stopListening();
            for (CartItem item : cartItems) {
                db.collection("carts").document(currentAccountID).collection("items")
                        .document(item.getProductID())
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Cleared item from Firestore: " + item.getProductID()))
                        .addOnFailureListener(e -> Log.e(TAG, "Error clearing item from Firestore: " + e.getMessage()));
            }
        }
        cartItems.clear();
        updateAllBadges();
        startListeningToCartChanges();
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

    public int getItemCount() {
        int count = cartItems.size();
        Log.d(TAG, "Current item count: " + count);
        return count;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    private void startListeningToCartChanges() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
        if (currentAccountID != null) {
            cartListener = db.collection("carts").document(currentAccountID)
                    .collection("items")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error listening to cart changes: " + e.getMessage());
                            FirebaseCrashlytics.getInstance().recordException(e);
                            return;
                        }
                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                CartItem item;
                                try {
                                    item = dc.getDocument().toObject(CartItem.class);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error parsing cart item: " + dc.getDocument().getId(), ex);
                                    FirebaseCrashlytics.getInstance().recordException(ex);
                                    continue;
                                }
                                switch (dc.getType()) {
                                    case ADDED:
                                        if (findItemIndex(item.getProductID()) == -1) {
                                            cartItems.add(item);
                                            Log.d(TAG, "Added item via listener: " + item.getProductName());
                                        }
                                        break;
                                    case MODIFIED:
                                        int index = findItemIndex(item.getProductID());
                                        if (index != -1) {
                                            cartItems.set(index, item);
                                            Log.d(TAG, "Modified item via listener: " + item.getProductName());
                                        }
                                        break;
                                    case REMOVED:
                                        index = findItemIndex(item.getProductID());
                                        if (index != -1) {
                                            cartItems.remove(index);
                                            Log.d(TAG, "Removed item via listener: " + item.getProductName());
                                        }
                                        break;
                                }
                            }
                            Log.d(TAG, "Cart updated with " + cartItems.size() + " items via listener");
                            updateAllBadges();
                        }
                    });
        }
    }

    private int findItemIndex(String productId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProductID().equals(productId)) {
                return i;
            }
        }
        return -1;
    }

    public void stopListening() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
            Log.d(TAG, "Stopped Firestore listener");
        }
    }

    public void updateAllBadges() {
        int count = getItemCount();
        for (TextView badge : badgeViews) {
            updateBadge(badge);
        }
        Log.d(TAG, "Updated badges with count: " + count);
    }

    private void updateBadge(TextView badge) {
        if (badge != null) {
            int count = getItemCount();
            badge.setText(String.valueOf(count));
            badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Updated badge: " + badge.getId() + ", Count: " + count);
        }
    }

    public void setUser(String accountID) {
        if (!accountID.equals(currentAccountID)) {
            currentAccountID = accountID;
            loadCartItemsFromFirestore(null);
            startListeningToCartChanges();
        }
    }

    public void clearUser() {
        currentAccountID = null;
        cartItems.clear();
        stopListening();
        updateAllBadges();
    }

    private void updateFirestoreItem(CartItem item) {
        if (currentAccountID != null && item != null && item.getProductID() != null) {
            db.collection("carts").document(currentAccountID).collection("items")
                    .document(item.getProductID())
                    .update("selected", item.isSelected(), "quantity", item.getQuantity())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated selected state and quantity for item: " + item.getProductID()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating item: " + e.getMessage()));
        }
    }
}