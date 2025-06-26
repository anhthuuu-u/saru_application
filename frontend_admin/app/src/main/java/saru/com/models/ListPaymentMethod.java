package saru.com.models;

import java.util.ArrayList;

public class ListPaymentMethod {
    ArrayList<PaymentMethod> paymentMethods;
    public ListPaymentMethod()
    {
        paymentMethods=new ArrayList<>();
    }

    public ArrayList<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(ArrayList<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
    public void gen_payments_method()
    {
        paymentMethods.add(new PaymentMethod(1,"Banking Account","Chuyển khoản ngân hàng"));
        paymentMethods.add(new PaymentMethod(2,"MOMO","Thanh toán ví MOMO"));
        paymentMethods.add(new PaymentMethod(3,"CASH","Thanh toán tiền mặt"));
        paymentMethods.add(new PaymentMethod(4,"COD","Nhận hàng rồi thanh toán"));
    }
}
