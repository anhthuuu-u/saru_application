package saru.com.app.models;

public class BlogCategory {
    private String cateblogID;
    private String name;
    private String imageUrl;

    public BlogCategory() {
        // Bắt buộc phải có constructor rỗng cho Firestore
    }

    public String getCateblogID() {
        return cateblogID;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}