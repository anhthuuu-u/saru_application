package saru.com.models;

import java.io.Serializable;

public class Brand implements Serializable {
    private String brandID;
    private String brandName;

    public Brand() {}

    public Brand(String brandID, String brandName) {
        this.brandID = brandID;
        this.brandName = brandName;
    }

    public String getBrandID() { return brandID; }
    public void setBrandID(String brandID) { this.brandID = brandID; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    @Override
    public String toString() {
        return brandName;
    }
}