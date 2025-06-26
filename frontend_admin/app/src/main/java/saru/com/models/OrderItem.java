package saru.com.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class OrderItem implements Serializable {
    @PropertyName("productName")
    private String productName;
    @PropertyName("productPrice")
    private double productPrice;
    @PropertyName("quantity")
    private int quantity;
    @PropertyName("totalPrice")
    private double totalPrice;

    public OrderItem() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
