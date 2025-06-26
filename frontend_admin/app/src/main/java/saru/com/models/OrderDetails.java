package saru.com.models;

import java.io.Serializable;

public class OrderDetails implements Serializable {
    private String id;
    private String orderID;
    private String productID;
    private int quantity;
    private double price;
    private double discount;
    private double vat;
    private double totalValue;

    public OrderDetails() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getVAT() { return vat; }
    public void setVAT(double vat) { this.vat = vat; }
    public double getTotalValue() {
        totalValue = (quantity * price - (discount / 100.0 * quantity * price)) * (1 + vat / 100.0);
        return totalValue;
    }
}