package saru.com.models;

import java.io.Serializable;

public class OrdersViewer implements Serializable {
    private String orderID;
    private String orderDate;
    private String customerName;
    private double totalAmount;
    private String orderStatusID;
    private Long paymentStatusID;

    public OrdersViewer() {}

    // Getters and Setters
    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getOrderStatusID() { return orderStatusID; }
    public void setOrderStatusID(String orderStatusID) { this.orderStatusID = orderStatusID; }
    public Long getPaymentStatusID() { return paymentStatusID; }
    public void setPaymentStatusID(Long paymentStatusID) { this.paymentStatusID = paymentStatusID; }
}