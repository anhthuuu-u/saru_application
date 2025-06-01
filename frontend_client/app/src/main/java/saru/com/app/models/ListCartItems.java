package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class ListCartItems {
    private List<CartItem> cartItems;

    public ListCartItems() {
        this.cartItems = new ArrayList<>();
    }

    // Thêm một CartItem
    public void addItem(CartItem item) {
        if (item == null) {
            return;
        }
        // Kiểm tra nếu sản phẩm đã tồn tại, tăng quantity
        for (CartItem existingItem : cartItems) {
            if (existingItem.getName().equals(item.getName())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartItems.add(item);
    }

    // Xóa một CartItem theo index
    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
        }
    }

    // Cập nhật quantity của một CartItem
    public void updateQuantity(int index, int newQuantity) {
        if (index >= 0 && index < cartItems.size() && newQuantity >= 1) {
            cartItems.get(index).setQuantity(newQuantity);
        }
    }

    // Chọn tất cả hoặc bỏ chọn tất cả
    public void setAllSelected(boolean selected) {
        for (CartItem item : cartItems) {
            item.setSelected(selected);
        }
    }

    // Lấy danh sách CartItem
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    // Tính tổng giá của các mục được chọn
    public double calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    // Lấy số lượng mục trong giỏ hàng
    public int getItemCount() {
        return cartItems.size();
    }

    // Xóa toàn bộ giỏ hàng
    public void clear() {
        cartItems.clear();
    }
}