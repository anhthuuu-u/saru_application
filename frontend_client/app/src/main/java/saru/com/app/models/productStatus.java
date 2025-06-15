package saru.com.app.models;

public class productStatus {
    private String productStatusID;
    private String productStatus;
    private String productStatusColor;

    public productStatus() {
    }

    public productStatus(String productStatusID, String productStatus, String productStatusColor) {
        this.productStatusID = productStatusID;
        this.productStatus = productStatus;
        this.productStatusColor = productStatusColor;
    }

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
}
