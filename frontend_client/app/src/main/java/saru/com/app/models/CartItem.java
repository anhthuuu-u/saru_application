package saru.com.app.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Product product;
    private int quantity;
    private boolean selected;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.selected = false;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}