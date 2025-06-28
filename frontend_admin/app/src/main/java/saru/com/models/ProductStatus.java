package saru.com.models;

import java.io.Serializable;

public class ProductStatus implements Serializable {
    private String productStatusID;
    private String productStatus;
    private String productStatusColor;

    public ProductStatus() {}

    public String getProductStatusID() {
        return productStatusID;
    }

    public void setProductStatusID(String productStatusID) {
        this.productStatusID = productStatusID;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getProductStatusColor() {
        return productStatusColor;
    }

    public void setProductStatusColor(String productStatusColor) {
        this.productStatusColor = productStatusColor;
    }

    @Override
    public String toString() {
        return productStatus;
    }
}