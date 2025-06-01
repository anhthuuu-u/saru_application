package saru.com.app.models;

public class CartItem {
    private String name;
    private double price;
    private int quantity;
    private boolean selected;

    public CartItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.selected = false;

    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public void setSelected(boolean selected) {
        this.selected = selected; // Lưu trạng thái chọn
    }

    public boolean isSelected() {
        return selected; // Trả về trạng thái chọn
    }

}