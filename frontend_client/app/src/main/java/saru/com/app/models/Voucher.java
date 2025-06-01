package saru.com.app.models;

import java.io.Serializable;

public class Voucher implements Serializable {
    private String voucherCode;
    private String description;
    private String expiryDate;
    private String voucherID;

    public Voucher() {
    }

    public Voucher(String voucherID,String voucherCode, String description, String expiryDate) {
        this.voucherID = voucherID;
        this.voucherCode = voucherCode;
        this.description = description;
        this.expiryDate = expiryDate;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getVoucherID() {
        return voucherID;
    }

    public void setVoucherID(String voucherID) {
        this.voucherID = voucherID;
    }
}