package saru.com.app.models;

import saru.com.app.connectors.ProductAdapter;

public class CartItem {
    private String productID;
    private String AccountID;
    private long timestamp;
    private String productName;
    private double productPrice;
    private String imageID;
    private int quantity;
    private boolean selected;

    public CartItem(String productID, String accountID, long timestamp, String productName, double productPrice, String imageID, int quantity, boolean selected) {
        this.productID = productID;
        AccountID = accountID;
        this.timestamp = timestamp;
        this.productName = productName;
        this.productPrice = productPrice;
        this.imageID = imageID;
        this.quantity = quantity;
        this.selected = selected;
    }

    public CartItem() {
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return productPrice * quantity;
    }

    public void setSelected(boolean selected) {
        this.selected = selected; // Lưu trạng thái chọn
    }

    public boolean isSelected() {
        return selected; // Trả về trạng thái chọn
    }

}