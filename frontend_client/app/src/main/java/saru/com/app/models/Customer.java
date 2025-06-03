package saru.com.app.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

public class Customer implements Serializable {
    private String _id;
    private String AccountID;
    private String CustomerID;
    private String CustomerEmail;
    private String createdAt;
    private String name;

    public Customer() {}

    public Customer(String _id, String AccountID, String CustomerID, String name, String CustomerEmail, String createdAt) {
        this._id = _id;
        this.AccountID = AccountID;
        this.CustomerID = CustomerID;
        this.name = name;
        this.CustomerEmail = CustomerEmail;
        this.createdAt = createdAt;
    }

    public Customer(String name, String email) {
        this._id = UUID.randomUUID().toString();
        this.AccountID = "acc_" + UUID.randomUUID().toString().substring(0, 8);
        this.CustomerID = "cus_" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.CustomerEmail = email;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new java.util.Date());
    }

    // Getters and Setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getAccountID() { return AccountID; }
    public void setAccountID(String AccountID) { this.AccountID = AccountID; }
    public String getCustomerID() { return CustomerID; }
    public void setCustomerID(String CustomerID) { this.CustomerID = CustomerID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCustomerEmail() { return CustomerEmail; }
    public void setCustomerEmail(String CustomerEmail) { this.CustomerEmail = CustomerEmail; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getEmail() { return CustomerEmail; }
    public void setEmail(String email) { this.CustomerEmail = email; }

    // Temporary fallback for migration
    public String getCustomerPassword() { return null; }
    public void setCustomerPassword(String password) {}
}