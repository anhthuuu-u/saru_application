package saru.com.models;

import java.io.Serializable;

public class BlogCategory implements Serializable {
    private String cateblogID;
    private String imageUrl;
    private String name;

    public BlogCategory() {}

    public BlogCategory(String cateblogID, String imageUrl, String name) {
        this.cateblogID = cateblogID;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public String getCateblogID() { return cateblogID; }
    public void setCateblogID(String cateblogID) { this.cateblogID = cateblogID; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}