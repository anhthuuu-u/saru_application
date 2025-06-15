package saru.com.app.models;

public class productCategory {
    private String CateDescription;
    private String CateID;
    private String CateName;

    public productCategory() {
    }

    public productCategory(String cateDescription, String cateID, String cateName) {
        CateDescription = cateDescription;
        CateID = cateID;
        CateName = cateName;
    }

    public String getCateDescription() {
        return CateDescription;
    }

    public void setCateDescription(String cateDescription) {
        CateDescription = cateDescription;
    }

    public String getCateID() {
        return CateID;
    }

    public void setCateID(String cateID) {
        CateID = cateID;
    }

    public String getCateName() {
        return CateName;
    }

    public void setCateName(String cateName) {
        CateName = cateName;
    }
}
