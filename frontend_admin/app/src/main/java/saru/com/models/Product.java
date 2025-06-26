package saru.com.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String productID;
    private String productName;
    private String CateID;
    private String brandID;
    private double customerRating;
    private String imageID;
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

    public Product() {}

    public Product(String productID, String productName, String CateID, String brandID, double customerRating,
                   String imageID, String ingredients, boolean isBestSelling, boolean isOnSale, String netContent,
                   String productDescription, double productPrice, String productStatusID, String wineType,
                   String alcoholStrength, String productTaste) {
        this.productID = productID;
        this.productName = productName;
        this.CateID = CateID;
        this.brandID = brandID;
        this.customerRating = customerRating;
        this.imageID = imageID;
        this.ingredients = ingredients;
        this.isBestSelling = isBestSelling;
        this.isOnSale = isOnSale;
        this.netContent = netContent;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.productStatusID = productStatusID;
        this.wineType = wineType;
        this.alcoholStrength = alcoholStrength;
        this.productTaste = productTaste;
    }

    // Getters and Setters
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCateID() { return CateID; }
    public void setCateID(String CateID) { this.CateID = CateID; }
    public String getBrandID() { return brandID; }
    public void setBrandID(String brandID) { this.brandID = brandID; }
    public double getCustomerRating() { return customerRating; }
    public void setCustomerRating(double customerRating) { this.customerRating = customerRating; }
    public String getImageID() { return imageID; }
    public void setImageID(String imageID) { this.imageID = imageID; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public boolean isBestSelling() { return isBestSelling; }
    public void setBestSelling(boolean bestSelling) { isBestSelling = bestSelling; }
    public boolean isOnSale() { return isOnSale; }
    public void setOnSale(boolean onSale) { isOnSale = onSale; }
    public String getNetContent() { return netContent; }
    public void setNetContent(String netContent) { this.netContent = netContent; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public String getProductStatusID() { return productStatusID; }
    public void setProductStatusID(String productStatusID) { this.productStatusID = productStatusID; }
    public String getWineType() { return wineType; }
    public void setWineType(String wineType) { this.wineType = wineType; }
    public String getAlcoholStrength() { return alcoholStrength; }
    public void setAlcoholStrength(String alcoholStrength) { this.alcoholStrength = alcoholStrength; }
    public String getProductTaste() { return productTaste; }
    public void setProductTaste(String productTaste) { this.productTaste = productTaste; }
}