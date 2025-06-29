package saru.com.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String productID;
    private String accountID;
    private long timestamp;
    private String productName;
    private double productPrice;
    private String imageID;
    private int quantity;
    private boolean selected;

    public CartItem(String productID, String accountID, long timestamp, String productName, double productPrice, String imageID, int quantity, boolean selected) {
        this.productID = productID;
        accountID = accountID;
        this.timestamp = timestamp;
        this.productName = productName;
        this.productPrice = productPrice;
        this.imageID = imageID;
        this.quantity = quantity;
        this.selected = selected;
    }

    public CartItem() {
    }

    // Constructor từ Parcel
    protected CartItem(Parcel in) {
        productID = in.readString();
        accountID = in.readString();
        timestamp = in.readLong();
        productName = in.readString();
        productPrice = in.readDouble();
        imageID = in.readString();
        quantity = in.readInt();
        selected = in.readByte() != 0; // boolean được đọc từ byte
    }

    // Triển khai Parcelable
    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productID);
        dest.writeString(accountID);
        dest.writeLong(timestamp);
        dest.writeString(productName);
        dest.writeDouble(productPrice);
        dest.writeString(imageID);
        dest.writeInt(quantity);
        dest.writeByte((byte) (selected ? 1 : 0)); // boolean được ghi dưới dạng byte
    }

    @Override
    public int describeContents() {
        return 0; // Không có nội dung đặc biệt
    }

    // Các getter và setter
    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        accountID = accountID;
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
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}