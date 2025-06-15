package saru.com.app.models;

public class Order {
    private String orderID;
    private String orderDate;
    private String orderStatus;
    private int totalProduct;
    private double totalValue;


    public Order(String orderDate, String orderID, String orderStatus, int totalProduct) {
        this.orderDate = orderDate;
        this.orderID = orderID;
        this.orderStatus = orderStatus;
        this.totalProduct = totalProduct;
    }

    public Order(String orderID, String orderDate,String orderStatus) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.orderStatus=orderStatus;
    }

    public Order() {
    }

    public Order(String orderID, String orderDate, String orderStatus, int totalProduct, double totalValue) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalProduct = totalProduct;
        this.totalValue = totalValue;
    }

    public Order(String orderID, String orderDate, String orderStatus, String ordertotalQuantity) {
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

    public double gettotalValue() {
        return totalValue;
    }

    public void settotalValue(double totalValue) {

        this.totalValue = totalValue;
    }


}
