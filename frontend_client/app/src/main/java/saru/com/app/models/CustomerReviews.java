package saru.com.app.models;

import android.media.Image;

public class CustomerReviews {
    private String customerName;
    private String reviewContent;
    private String purchasedProduct;
    private String customerImage;

    public CustomerReviews() {
    }

    public CustomerReviews(String customerName, String reviewContent, String purchasedProduct, String customerImage) {
        this.customerName = customerName;
        this.reviewContent = reviewContent;
        this.purchasedProduct = purchasedProduct;
        this.customerImage = customerImage;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getPurchasedProduct() {
        return purchasedProduct;
    }

    public void setPurchasedProduct(String purchasedProduct) {
        this.purchasedProduct = purchasedProduct;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public void setCustomerImage(String customerImage) {
        this.customerImage = customerImage;
    }
}
