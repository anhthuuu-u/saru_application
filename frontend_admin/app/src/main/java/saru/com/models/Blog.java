package saru.com.models;

import java.io.Serializable;

public class Blog implements Serializable {
    private String blogID;
    private String cateblogID;
    private String content;
    private String imageUrl;
    private String title;

    public Blog() {}

    public Blog(String blogID, String cateblogID, String content, String imageUrl, String title) {
        this.blogID = blogID;
        this.cateblogID = cateblogID;
        this.content = content;
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public String getBlogID() { return blogID; }
    public void setBlogID(String blogID) { this.blogID = blogID; }
    public String getCateblogID() { return cateblogID; }
    public void setCateblogID(String cateblogID) { this.cateblogID = cateblogID; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}