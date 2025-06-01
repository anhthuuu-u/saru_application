package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class ProductComparisonItems {
    private String name;
    private String brand;
    private String alcohol;
    private String volume;
    private String wineType;
    private String ingredients;
    private String taste;
    private double price;
    private int imageResId;

    public ProductComparisonItems(String name, String brand, String alcohol, String volume,
                                  String wineType, String ingredients, String taste,
                                  double price, int imageResId) {
        this.name = name;
        this.brand = brand;
        this.alcohol = alcohol;
        this.volume = volume;
        this.wineType = wineType;
        this.ingredients = ingredients;
        this.taste = taste;
        this.price = price;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getAlcohol() { return alcohol; }
    public String getVolume() { return volume; }
    public String getWineType() { return wineType; }
    public String getIngredients() { return ingredients; }
    public String getTaste() { return taste; }
    public double getPrice() { return price; }
    public int getImageResId() { return imageResId; }

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