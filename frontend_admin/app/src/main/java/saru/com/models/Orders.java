package saru.com.models;


import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Orders implements Serializable {
    private String orderID;
    private String customerID;
    private String orderDate;
    private String orderStatusID;
    private double totalAmount;
    private List<Map<String, Object>> items;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String paymentMethod;
    private Long timestamp;
    private int totalProduct;
    private Long paymentStatusID;

    public Orders() {
        items = new ArrayList<>();
    }

    public Orders(String orderID, String customerID, String orderDate, String orderStatusID, double totalAmount,
                  List<Map<String, Object>> items, String customerName, String customerPhone, String customerAddress,
                  String paymentMethod, Long timestamp, int totalProduct, Long paymentStatusID) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.orderDate = orderDate;
        this.orderStatusID = orderStatusID;
        this.totalAmount = totalAmount;
        this.items = items != null ? items : new ArrayList<>();
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.totalProduct = totalProduct;
        this.paymentStatusID = paymentStatusID;
    }

    @PropertyName("orderID")
    public String getOrderID() { return orderID; }
    @PropertyName("orderID")
    public void setOrderID(String orderID) { this.orderID = orderID; }

    @PropertyName("customerID")
    public String getCustomerID() { return customerID; }
    @PropertyName("customerID")
    public void setCustomerID(String customerID) { this.customerID = customerID; }

    @PropertyName("orderDate")
    public String getOrderDate() { return orderDate; }
    @PropertyName("orderDate")
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    @PropertyName("orderStatusID")
    public String getOrderStatusID() { return orderStatusID; }
    @PropertyName("orderStatusID")
    public void setOrderStatusID(String orderStatusID) { this.orderStatusID = orderStatusID; }

    @PropertyName("totalAmount")
    public double getTotalAmount() { return totalAmount; }
    @PropertyName("totalAmount")
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    @PropertyName("items")
    public List<Map<String, Object>> getItems() { return items; }
    @PropertyName("items")
    public void setItems(List<Map<String, Object>> items) { this.items = items != null ? items : new ArrayList<>(); }

    @PropertyName("customerName")
    public String getCustomerName() { return customerName; }
    @PropertyName("customerName")
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @PropertyName("customerPhone")
    public String getCustomerPhone() { return customerPhone; }
    @PropertyName("customerPhone")
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    @PropertyName("customerAddress")
    public String getCustomerAddress() { return customerAddress; }
    @PropertyName("customerAddress")
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    @PropertyName("paymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    @PropertyName("paymentMethod")
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @PropertyName("timestamp")
    public Long getTimestamp() { return timestamp; }
    @PropertyName("timestamp")
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @PropertyName("totalProduct")
    public int getTotalProduct() { return totalProduct; }
    @PropertyName("totalProduct")
    public void setTotalProduct(int totalProduct) { this.totalProduct = totalProduct; }

    @PropertyName("paymentStatusID")
    public Long getPaymentStatusID() { return paymentStatusID; }
    @PropertyName("paymentStatusID")
    public void setPaymentStatusID(Long paymentStatusID) { this.paymentStatusID = paymentStatusID; }
}
