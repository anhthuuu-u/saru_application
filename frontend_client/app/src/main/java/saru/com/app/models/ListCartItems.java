package saru.com.app.models;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

public class ListCartItems {
    private List<CartItem> cartItems;

    public ListCartItems() {
        this.cartItems = new ArrayList<>();
    }

    public void addItem(CartItem item) {
        if (item == null || item.getProductID() == null || item.getAccountID() == null) {
            Log.e("ListCartItems", "Cannot add null CartItem or item with null ProductID/AccountID");
            FirebaseCrashlytics.getInstance().recordException(new Exception("Null CartItem or ProductID/AccountID in addItem"));
            return;
        }

        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductID() != null && existingItem.getProductID().equals(item.getProductID())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                Log.d("ListCartItems", "Updated quantity for " + item.getProductName() + " to " + existingItem.getQuantity());
                return;
            }
        }
        cartItems.add(item);
        Log.d("ListCartItems", "Added new item: " + item.getProductName());
    }

    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            CartItem item = cartItems.remove(index);
            Log.d("ListCartItems", "Removed item: " + item.getProductName());
        }
    }

    public void updateQuantity(int index, int newQuantity) {
        if (index >= 0 && index < cartItems.size() && newQuantity >= 1) {
            CartItem item = cartItems.get(index);
            item.setQuantity(newQuantity);
            Log.d("ListCartItems", "Updated quantity for " + item.getProductName() + " to " + newQuantity);
        }
    }

    public void setAllSelected(boolean selected) {
        for (CartItem item : cartItems) {
            item.setSelected(selected);
        }
        Log.d("ListCartItems", "Set all items selected: " + selected);
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public double calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public void clear() {
        cartItems.clear();
        Log.d("ListCartItems", "Cleared all items");
    }
}