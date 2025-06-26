package saru.com.models;

import java.io.Serializable;

public class Voucher implements Serializable {
    private String voucherID;
    private String description;
    private String expiryDate;
    private String voucherCode;

    public Voucher() {}

    public Voucher(String voucherID, String description, String expiryDate, String voucherCode) {
        this.voucherID = voucherID;
        this.description = description;
        this.expiryDate = expiryDate;
        this.voucherCode = voucherCode;
    }

    public String getVoucherID() { return voucherID; }
    public void setVoucherID(String voucherID) { this.voucherID = voucherID; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}