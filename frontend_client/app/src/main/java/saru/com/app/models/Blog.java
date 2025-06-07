package saru.com.app.models;

public class Blog {
    private String blogID;
    private String title;
    private String content;
    private String imageUrl;
    private String cateblogID;

    public Blog() {
        // Firestore requires empty constructor
    }

    public String getBlogID() { return blogID; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getCateblogID() { return cateblogID; }
}
