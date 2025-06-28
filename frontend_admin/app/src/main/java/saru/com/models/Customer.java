package saru.com.models;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Customer implements Serializable {
    private String customerID;
    private String customerName;
    private String customerPhone; // Changed to String to match Firestore data
    private String customerAvatar;
    private String customerBirth;
    private String sex;
    private boolean receiveEmail;
    private String memberID;
    private List<Address> addresses;

    public Customer() {
        addresses = new ArrayList<>();
    }

    public Customer(String customerID, String customerName, String customerPhone, String customerAvatar,
                    String customerBirth, String sex, boolean receiveEmail, String memberID,
                    List<Address> addresses) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAvatar = customerAvatar;
        this.customerBirth = customerBirth;
        this.sex = sex;
        this.receiveEmail = receiveEmail;
        this.memberID = memberID;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
    }

    @PropertyName("CustomerID")
    public String getCustomerID() {
        return customerID;
    }

    @PropertyName("CustomerID")
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    @PropertyName("CustomerName")
    public String getCustomerName() {
        return customerName;
    }

    @PropertyName("CustomerName")
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @PropertyName("CustomerPhone")
    public String getCustomerPhone() {
        return customerPhone;
    }

    @PropertyName("CustomerPhone")
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @PropertyName("CustomerAvatar")
    public String getCustomerAvatar() {
        return customerAvatar;
    }

    @PropertyName("CustomerAvatar")
    public void setCustomerAvatar(String customerAvatar) {
        this.customerAvatar = customerAvatar;
    }

    @PropertyName("CustomerBirth")
    public String getCustomerBirth() {
        return customerBirth;
    }

    @PropertyName("CustomerBirth")
    public void setCustomerBirth(String customerBirth) {
        this.customerBirth = customerBirth;
    }

    @PropertyName("Sex")
    public String getSex() {
        return sex;
    }

    @PropertyName("Sex")
    public void setSex(String sex) {
        this.sex = sex;
    }

    @PropertyName("ReceiveEmail")
    public boolean isReceiveEmail() {
        return receiveEmail;
    }

    @PropertyName("ReceiveEmail")
    public void setReceiveEmail(boolean receiveEmail) {
        this.receiveEmail = receiveEmail;
    }

    @PropertyName("MemberID")
    public String getMemberID() {
        return memberID;
    }

    @PropertyName("MemberID")
    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    @PropertyName("Addresses")
    public List<Address> getAddresses() {
        return addresses;
    }

    @PropertyName("Addresses")
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses != null ? addresses : new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return "ID: " + customerID + " - " + customerName + "\n" + customerPhone;
    }

    public static class Address implements Serializable {
        private String addressID;
        private String recipientName;
        private String phone;
        private String address;

        // Added no-argument constructor for Firestore deserialization
        public Address() {
        }

        public Address(String addressID, String recipientName, String phone, String address) {
            this.addressID = addressID;
            this.recipientName = recipientName;
            this.phone = phone;
            this.address = address;
        }

        @PropertyName("AddressID")
        public String getAddressID() {
            return addressID;
        }

        @PropertyName("AddressID")
        public void setAddressID(String addressID) {
            this.addressID = addressID;
        }

        @PropertyName("RecipientName")
        public String getRecipientName() {
            return recipientName;
        }

        @PropertyName("RecipientName")
        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }

        @PropertyName("Phone")
        public String getPhone() {
            return phone;
        }

        @PropertyName("Phone")
        public void setPhone(String phone) {
            this.phone = phone;
        }

        @PropertyName("Address")
        public String getAddress() {
            return address;
        }

        @PropertyName("Address")
        public void setAddress(String address) {
            this.address = address;
        }
    }
}