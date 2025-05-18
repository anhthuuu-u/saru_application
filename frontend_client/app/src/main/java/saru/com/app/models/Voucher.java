package saru.com.app.models;

import java.io.Serializable;

public class Voucher implements Serializable {
    private String voucherCode;
    private String description;
    private String expiryDate;

    public Voucher(String voucherCode, String description, String expiryDate) {
        this.voucherCode = voucherCode;
        this.description = description;
        this.expiryDate = expiryDate;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public String getDescription() {
        return description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}