package saru.com.app.models;


import java.util.ArrayList;


public class Rating {
    private String CustomerID; // Changed from userId
    private float rating;
    private String comment;
    private String ratingDate;
    private String orderId;
    private ArrayList<String> imageUrls;


    // Constructor
    public Rating(String CustomerID, float rating, String comment, String ratingDate) {
        this.CustomerID = CustomerID;
        this.rating = rating;
        this.comment = comment;
        this.ratingDate = ratingDate;
    }


    // Getters and Setters
    public String getCustomerID() {
        return CustomerID;
    }


    public void setCustomerID(String CustomerID) {
        this.CustomerID = CustomerID;
    }


    public float getRating() {
        return rating;
    }


    public void setRating(float rating) {
        this.rating = rating;
    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getRatingDate() {
        return ratingDate;
    }


    public void setRatingDate(String ratingDate) {
        this.ratingDate = ratingDate;
    }


    public String getOrderId() {
        return orderId;
    }


    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }


    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
