package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class ProductComparisonItems extends Product {
    private String productName;
    private String productBrand;
    private String alcoholStrength;
    private String netContent;
    private String wineType;
    private String ingredients;
    private String productTaste;
    private double productPrice;
    private String imageResId;

    public ProductComparisonItems(String productName, String productBrand, String alcoholStrength, String netContent,
                                  String wineType, String ingredients, String productTaste,
                                  double productPrice, String imageResId) {
        this.productName = productName;
        this.productBrand = productBrand;
        this.alcoholStrength = alcoholStrength;
        this.netContent = netContent;
        this.wineType = wineType;
        this.ingredients = ingredients;
        this.productTaste = productTaste;
        this.productPrice = productPrice;
        this.imageResId = imageResId;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getProductBrand() { return productBrand; }
    public String getAlcoholStrength() { return alcoholStrength; }
    public String getNetContent() { return netContent; }
    public String getWineType() { return wineType; }
    public String getIngredients() { return ingredients; }
    public String getProductTaste() { return productTaste; }
    public double getProductPrice() { return productPrice; }
    public String getImageResId() { return imageResId; }

    // Danh sách sản phẩm so sánh
    private static List<ProductComparisonItems> comparisonItems = new ArrayList<>();

    public static void addItem(ProductComparisonItems item) {
        if (item != null && comparisonItems.size() < 3) { // Giới hạn 3 sản phẩm
            comparisonItems.add(item);
        }
    }

    public static void removeItem(int index) {
        if (index >= 0 && index < comparisonItems.size()) {
            comparisonItems.remove(index);
        }
    }

    public static void clear() {
        comparisonItems.clear();
    }

    public static List<ProductComparisonItems> getComparisonItems() {
        return new ArrayList<>(comparisonItems);
    }

    public static int getItemCount() {
        return comparisonItems.size();
    }
}