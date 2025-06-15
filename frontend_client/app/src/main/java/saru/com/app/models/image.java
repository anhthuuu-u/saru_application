package saru.com.app.models;

public class image {
    private String imageID;
    private String ProductImageCover;
    private String ProductImageSub1;
    private String ProductImageSub2;

    public image() {
    }

    public image(String imageID, String productImageCover, String productImageSub1, String productImageSub2) {
        this.imageID = imageID;
        ProductImageCover = productImageCover;
        ProductImageSub1 = productImageSub1;
        ProductImageSub2 = productImageSub2;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getProductImageCover() {
        return ProductImageCover;
    }

    public void setProductImageCover(String productImageCover) {
        ProductImageCover = productImageCover;
    }

    public String getProductImageSub1() {
        return ProductImageSub1;
    }

    public void setProductImageSub1(String productImageSub1) {
        ProductImageSub1 = productImageSub1;
    }

    public String getProductImageSub2() {
        return ProductImageSub2;
    }

    public void setProductImageSub2(String productImageSub2) {
        ProductImageSub2 = productImageSub2;
    }
}
