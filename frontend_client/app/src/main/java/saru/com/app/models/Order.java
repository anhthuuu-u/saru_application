package saru.com.app.models;

public class Order {
    private String orderID;
    private String orderDate;
    private String orderStatus;
    private int totalProduct;
    private double totalAmount;


    public Order(String orderID, String orderDate, String orderStatus) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
    }

    public Order(String orderID, String orderDate) {
        this.orderID = orderID;
        this.orderDate = orderDate;
    }

    public Order() {
    }

    public Order(String orderID, String orderDate, String orderStatus, int totalProduct, double totalAmount) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalProduct = totalProduct;
        this.totalAmount = totalAmount;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getTotalProduct() {
        return totalProduct;
    }

    public void setTotalProduct(int totalProduct) {
        this.totalProduct = totalProduct;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }


}
