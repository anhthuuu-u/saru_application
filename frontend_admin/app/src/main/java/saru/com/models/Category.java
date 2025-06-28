package saru.com.models;

import java.io.Serializable;

// Lớp Category đại diện cho một danh mục sản phẩm.
public class Category implements Serializable {
    private String CateID; // Giữ nguyên tên để khớp với Firestore
    private String CateName; // Giữ nguyên tên để khớp với Firestore
    // Không có trường CateDescription vì bạn đã xác nhận không có trong Firestore

    // Constructor mặc định cần thiết cho Firebase Firestore
    public Category() {}

    // Constructor với các tham số để tạo đối tượng Category từ dữ liệu Firestore
    // Hoặc để tạo đối tượng "All Categories"
    public Category(String CateID, String CateName) {
        this.CateID = CateID;
        this.CateName = CateName;
    }

    // Getters
    public String getCateID() { return CateID; }
    public String getCateName() { return CateName; }

    // Setters (quan trọng cho Firebase tự động set ID của document)
    public void setCateID(String CateID) { this.CateID = CateID; }
    public void setCateName(String CateName) { this.CateName = CateName; }

    @Override
    public String toString() {
        return CateName;
    }
}
