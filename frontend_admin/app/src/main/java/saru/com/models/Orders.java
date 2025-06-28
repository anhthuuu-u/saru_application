package saru.com.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Orders implements Serializable {
    private String orderID;
    private String customerID;
    private String orderDate;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String orderStatusID;
    private String paymentMethod;
    private int totalProduct;
    private double totalAmount;
    private long timestamp;
    private List<Object> items; // Adjust type based on your data (e.g., List<OrderDetails>)

    public Orders() {
        // Default constructor required for Firestore
    }

    @PropertyName("OrderID")
    public String getOrderID() { return orderID; }

    @PropertyName("OrderID")
    public void setOrderID(String orderID) { this.orderID = orderID; }

    @PropertyName("CustomerID")
    public String getCustomerID() { return customerID; }

    @PropertyName("CustomerID")
    public void setCustomerID(String customerID) { this.customerID = customerID; }

    @PropertyName("OrderDate")
    public String getOrderDate() { return orderDate; }

    @PropertyName("OrderDate")
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    @PropertyName("OrderStatusID")
    public String getOrderStatusID() { return orderStatusID; }

    @PropertyName("OrderStatusID")
    public void setOrderStatusID(String orderStatusID) { this.orderStatusID = orderStatusID; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getTotalProduct() { return totalProduct; }
    public void setTotalProduct(int totalProduct) { this.totalProduct = totalProduct; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @PropertyName("items")
    public List<Object> getItems() { return items; }

    @PropertyName("items")
    public void setItems(List<Object> items) { this.items = items; }
}