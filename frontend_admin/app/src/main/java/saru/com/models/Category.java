package saru.com.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String cateID;
    private String cateName;
    private String cateDescription;

    public Category() {}

    public Category(String cateID, String cateName, String cateDescription) {
        this.cateID = cateID;
        this.cateName = cateName;
        this.cateDescription = cateDescription;
    }

    public String getCateID() { return cateID; }
    public void setCateID(String cateID) { this.cateID = cateID; }
    public String getCateName() { return cateName; }
    public void setCateName(String cateName) { this.cateName = cateName; }
    public String getCateDescription() { return cateDescription; }
    public void setCateDescription(String cateDescription) { this.cateDescription = cateDescription; }

    @Override
    public String toString() {
        return cateName;
    }
}