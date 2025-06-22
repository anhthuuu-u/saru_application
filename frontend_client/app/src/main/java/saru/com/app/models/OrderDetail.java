package saru.com.app.models;

public class OrderDetail {
    private String productID;
    private String productName;
    private int quantity;
    private double price;
    private String productImageCover;
    private String brand;
    private String voucherID;


    public OrderDetail() {
    }

    public OrderDetail(String productID, String productName, int quantity, double price) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderDetail(String productID, String productName, int quantity, double price, String brand) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.brand = brand;
    }

    public String getVoucherID() {
        return voucherID;
    }

    public void setVoucherID(String voucherID) {
        this.voucherID = voucherID;
    }

    public String getProductImageCover() {
        return productImageCover;
    }

    public void setProductImageCover(String productImageCover) {
        this.productImageCover = productImageCover;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}