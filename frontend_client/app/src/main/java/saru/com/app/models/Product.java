package saru.com.app.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String productName;
    private String productPrice;
    private String productBrand;
    private String stockStatus;
    private String alcoholStrength;
    private String netContent;
    private String wineType;
    private String ingredients;
    private float customerRating;
    private String productDescription;

    public Product(String productName, String productPrice, String productBrand, String stockStatus,
                   String alcoholStrength, String netContent, String wineType, String ingredients,
                   float customerRating, String productDescription) {
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
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public String getAlcoholStrength() {
        return alcoholStrength;
    }

    public String getNetContent() {
        return netContent;
    }

    public String getWineType() {
        return wineType;
    }

    public String getIngredients() {
        return ingredients;
    }

    public float getCustomerRating() {
        return customerRating;
    }

    public String getProductDescription() {
        return productDescription;
    }
}