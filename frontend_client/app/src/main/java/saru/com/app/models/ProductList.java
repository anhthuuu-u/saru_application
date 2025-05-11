package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class ProductList {
    private List<Product> products;

    public ProductList() {
        products = new ArrayList<>();
        products.add(new Product(
                "Hoang Su Phi Blood Plum Wine",
                "392,000 VND",
                "Mam Distillery",
                "In Stock",
                "19% ABV",
                "530ml",
                "Infused Wine",
                "Hoang Su Phi Blood Plum, Rice Wine",
                4.8f,
                "Hoang Su Phi District, located in Ha Giang Province, is renowned for its rare and precious ancient plum variety - the Blood Plum. Unlike the Tam Hoa or Hau plums from Son La, the Blood Plum has a distinctive flavor with a delightful aroma and a slight bitterness. The name \"Blood Plum\" was given by locals because, when cut, the fruit reveals a deep red color, with a firm sweetness and mild tartness, complemented by an alluring reddish-purple hue."
        ));
        products.add(new Product(
                "Apple Wine",
                "180,000 VND",
                "Nature Spirits",
                "In Stock",
                "15% ABV",
                "500ml",
                "Fruit Wine",
                "Fresh Apples, Rice Wine",
                4.5f,
                "A refreshing apple wine made from the finest fresh apples, offering a sweet and crisp taste, perfect for a relaxing evening."
        ));
        products.add(new Product(
                "Rice Wine",
                "250,000 VND",
                "Traditional Brews",
                "Out of Stock",
                "20% ABV",
                "600ml",
                "Rice Wine",
                "Premium Rice, Yeast",
                4.7f,
                "A traditional rice wine crafted from premium rice, delivering a robust and authentic flavor that embodies the essence of Vietnamese brewing traditions."
        ));
        products.add(new Product(
                "Apricot Wine",
                "220,000 VND",
                "Mountain Essence",
                "In Stock",
                "17% ABV",
                "550ml",
                "Infused Wine",
                "Apricots, Rice Wine",
                4.6f,
                "An infused apricot wine with a delightful balance of sweetness and tartness, sourced from the mountains of Vietnam."
        ));
    }

    public List<Product> getProducts() {
        return products;
    }
}