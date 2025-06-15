package saru.com.app.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Rating implements Serializable {
    private String userId;
    private float rating;
    private String comment;
    private ArrayList<String> imageUrls; // Lưu chuỗi Base64
    private String ratingDate;
    private String orderId;

    public Rating() {
    }

    public Rating(String userId, float rating, String comment, String ratingDate) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.ratingDate = ratingDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
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
}