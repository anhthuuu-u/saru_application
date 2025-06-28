package saru.com.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class OrderDetails implements Serializable {
    private String id;
    private String orderID;
    private String productID;
    private int quantity;

    public OrderDetails() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("OrderID")
    public String getOrderID() { return orderID; }

    @PropertyName("OrderID")
    public void setOrderID(String orderID) { this.orderID = orderID; }

    @PropertyName("ProductID")
    public String getProductID() { return productID; }

    @PropertyName("ProductID")
    public void setProductID(String productID) { this.productID = productID; }

    @PropertyName("Quantity")
    public int getQuantity() { return quantity; }

    @PropertyName("Quantity")
    public void setQuantity(int quantity) { this.quantity = quantity; }
}