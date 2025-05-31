package saru.com.app.models;

import java.util.ArrayList;
import java.util.List;

public class VoucherList {
    private List<Voucher> vouchers;

    public VoucherList() {
        vouchers = new ArrayList<>();
        vouchers.add(new Voucher("GIẢM 200K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott...", "Còn ĐB 58:48"));
        vouchers.add(new Voucher("GIẢM 100K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott...", "Còn ĐB 58:48"));
        vouchers.add(new Voucher("GIẢM 60K", "[SALE NGAY ĐỒI] Trừ trực tiếp cho đơn từ 24 tháng tười, sử dụng Abbott, TPC...", "Còn ĐB 58:48"));
        vouchers.add(new Voucher("FREESHIP 50K", "[SALE NGAY ĐỒI] Áp dụng đơn hàng online", "Ngày 07-05-05"));
        vouchers.add(new Voucher("FREESHIP 50K", "[THỨ 2 FREESHIP] Áp dụng đơn hàng online", "Còn ĐB 58:48"));
        vouchers.add(new Voucher("FREESHIP 20K", "Áp dụng đơn hàng online", "Còn ĐB 58:48"));
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }
}