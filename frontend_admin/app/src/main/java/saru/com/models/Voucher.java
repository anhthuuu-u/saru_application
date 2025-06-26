package saru.com.models;

import java.io.Serializable;

// Lớp Voucher đại diện cho một mã khuyến mãi, triển khai Serializable để truyền giữa các Activity.
public class Voucher implements Serializable {
    private String voucherID;
    private String description;
    private String expiryDate; // Định dạng "DD/MM/YYYY" hoặc kiểu dữ liệu phù hợp hơn nếu cần xử lý ngày tháng
    private String voucherCode;

    // Constructor mặc định cho Firebase Firestore
    public Voucher() {
        // Cần có một constructor công khai không đối số cho Firebase deserialization
    }

    // Constructor với các tham số để tạo đối tượng Voucher
    public Voucher(String voucherID, String description, String expiryDate, String voucherCode) {
        this.voucherID = voucherID;
        this.description = description;
        this.expiryDate = expiryDate;
        this.voucherCode = voucherCode;
    }

    // Getters
    public String getVoucherID() {
        return voucherID;
    }

    public String getDescription() {
        return description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    // Setters
    public void setVoucherID(String voucherID) {
        this.voucherID = voucherID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}
