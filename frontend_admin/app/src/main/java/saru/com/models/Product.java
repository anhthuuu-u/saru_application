package saru.com.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String productID;
    private String productName;
    private String cateID;
    private String brandID;
    private double customerRating;
    private String ingredients;
    private boolean isBestSelling;
    private boolean isOnSale;
    private String netContent;
    private String productDescription;
    private double productPrice;
    private String productStatusID;
    private String wineType;
    private String alcoholStrength;
    private String productTaste;
    private String imageID;
    private String productImageCover;
    private String productImageSub1;
    private String productImageSub2;

    public Product() {
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCateID() {
        return cateID;
    }

    public void setCateID(String cateID) {
        this.cateID = cateID;
    }

    public String getBrandID() {
        return brandID;
    }

    public void setBrandID(String brandID) {
        this.brandID = brandID;
    }

    public double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(double customerRating) {
        this.customerRating = customerRating;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public boolean isBestSelling() {
        return isBestSelling;
    }

    public void setBestSelling(boolean bestSelling) {
        this.isBestSelling = bestSelling;
    }

    public boolean isOnSale() {
        return isOnSale;
    }

    public void setOnSale(boolean onSale) {
        this.isOnSale = onSale;
    }

    public String getNetContent() {
        return netContent;
    }

    public void setNetContent(String netContent) {
        this.netContent = netContent;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductStatusID() {
        return productStatusID;
    }

    public void setProductStatusID(String productStatusID) {
        this.productStatusID = productStatusID;
    }

    public String getWineType() {
        return wineType;
    }

    public void setWineType(String wineType) {
        this.wineType = wineType;
    }

    public String getAlcoholStrength() {
        return alcoholStrength;
    }

    public void setAlcoholStrength(String alcoholStrength) {
        this.alcoholStrength = alcoholStrength;
    }

    public String getProductTaste() {
        return productTaste;
    }

    public void setProductTaste(String productTaste) {
        this.productTaste = productTaste;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getProductImageCover() {
        return productImageCover;
    }

    public void setProductImageCover(String productImageCover) {
        this.productImageCover = productImageCover;
    }

    public String getProductImageSub1() {
        return productImageSub1;
    }

    public void setProductImageSub1(String productImageSub1) {
        this.productImageSub1 = productImageSub1;
    }

    public String getProductImageSub2() {
        return productImageSub2;
    }

    public void setProductImageSub2(String productImageSub2) {
        this.productImageSub2 = productImageSub2;
    }
}