package saru.com.app.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String productID;
    private String productName;
    private double productPrice;
    private String productBrand;
    private String stockStatus;
    private String alcoholStrength;
    private String netContent;
    private String wineType;
    private String ingredients;
    private float customerRating;
    private String productDescription;
    private String CateID; // Tham chiếu đến productCategory
    private String productStatusID; // Tham chiếu đến productStatus
    private String brandID; // Tham chiếu đến productBrand
    private String imageID; // Tham chiếu đến picture
    private boolean isOnSale;
    private boolean isBestSelling;
    private long createdAt;
    private String productTaste;// Thêm field productTaste

    private String productCategory;

    // Constructor mặc định cho Firestore
    public Product() {
        this.isOnSale = false;
        this.isBestSelling = false;
    }

    // Constructor đầy đủ
    public Product(String productID, String productName, double productPrice, String productBrand,
                   String stockStatus, String alcoholStrength, String netContent, String wineType,
                   String ingredients, float customerRating, String productDescription,
                   String cateID, String productStatusID, String brandID, String imageID,
                   boolean isOnSale, boolean isBestSelling, long createdAt, String productTaste, String productCategory) {
        this.productID = productID;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productBrand = productBrand;
        this.stockStatus = stockStatus;
        this.alcoholStrength = alcoholStrength;
        this.netContent = netContent;
        this.wineType = wineType;
        this.ingredients = ingredients;
        this.customerRating = customerRating;
        this.productDescription = productDescription;
        this.CateID = CateID;
        this.productStatusID = productStatusID;
        this.brandID = brandID;
        this.imageID = imageID;
        this.isOnSale = isOnSale;
        this.isBestSelling = isBestSelling;
        this.createdAt = createdAt;
        this.productTaste = productTaste;
        this.productCategory = productCategory;
    }

    // Getter và Setter
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

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getAlcoholStrength() {
        return alcoholStrength;
    }

    public void setAlcoholStrength(String alcoholStrength) {
        this.alcoholStrength = alcoholStrength;
    }

    public String getNetContent() {
        return netContent;
    }

    public void setNetContent(String netContent) {
        this.netContent = netContent;
    }

    public String getWineType() {
        return wineType;
    }

    public void setWineType(String wineType) {
        this.wineType = wineType;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public float getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(float customerRating) {
        this.customerRating = customerRating;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getCateID() {
        return CateID;
    }

    public void setCateID(String CateID) {
        this.CateID = CateID;
    }

    public String getProductStatusID() {
        return productStatusID;
    }

    public void setProductStatusID(String productStatusID) {
        this.productStatusID = productStatusID;
    }

    public String getBrandID() {
        return brandID;
    }

    public void setBrandID(String brandID) {
        this.brandID = brandID;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public boolean isOnSale() {
        return isOnSale;
    }

    public void setOnSale(boolean onSale) {
        isOnSale = onSale;
    }

    public boolean isBestSelling() {
        return isBestSelling;
    }

    public void setBestSelling(boolean bestSelling) {
        isBestSelling = bestSelling;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getProductTaste() {
        return productTaste;
    }

    public void setProductTaste(String productTaste) {
        this.productTaste = productTaste;
    }

    public void setCategory(String cateName) {
    }

    public String getCategory() {
        return productCategory;
    }
}