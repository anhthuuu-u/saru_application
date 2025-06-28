package saru.com.app.models;

import com.google.firebase.firestore.PropertyName;

public class productCategory {
    private String CateID;
    private String CateName;

    public productCategory() {
    }

    public productCategory(String CateID, String CateName) {
        this.CateID = CateID; // Sửa gán
        this.CateName = CateName; // Sửa gán
    }

    public String getCateID() {
        return CateID;
    }

    public void setCateID(String CateID) {
        this.CateID = CateID;
    }

    public String getCateName() {
        return CateName;
    }

    public void setCateName(String CateName) {
        this.CateName = CateName;
    }
}