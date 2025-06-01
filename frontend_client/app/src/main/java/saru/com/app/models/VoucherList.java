package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class VoucherList {
    private List<Voucher> vouchers;

    public VoucherList() {
        vouchers = new ArrayList<>();
        vouchers.add(new Voucher("SARU1","GIẢM 200K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott...", "10-06-2025"));
        vouchers.add(new Voucher("SARU2","GIẢM 100K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott...", "22-06-2025"));
        vouchers.add(new Voucher("SARU3","GIẢM 60K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott, TPC...", "01-07-2025"));
        vouchers.add(new Voucher("SARU4","FREESHIP 50K", "[SALE NGAY ĐỒI] Áp dụng đơn hàng online", "07-08-2025"));
        vouchers.add(new Voucher("SARU5","FREESHIP 50K", "[THỨ 2 FREESHIP] Áp dụng đơn hàng online", "08-09-2025"));
        vouchers.add(new Voucher("SARU6","FREESHIP 20K", "Áp dụng đơn hàng online", "22-12-2025"));
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }
}