package saru.com.app.models;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class VoucherList {
    private final MutableLiveData<List<Voucher>> vouchersLiveData;
    private final FirebaseFirestore db;

    public VoucherList() {
        vouchersLiveData = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadVouchersFromFirestore();
    }

    private void loadVouchersFromFirestore() {
        db.collection("vouchers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Voucher> vouchers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Voucher voucher = new Voucher(
                                    document.getString("voucherID"),
                                    document.getString("voucherCode"),
                                    document.getString("description"),
                                    document.getString("expiryDate")
                            );
                            vouchers.add(voucher);
                        }
                        vouchersLiveData.setValue(vouchers);
                        Log.d("VoucherList", "Loaded " + vouchers.size() + " vouchers from Firestore");
                    } else {
                        Log.e("VoucherList", "Error loading vouchers: ", task.getException());
                        vouchersLiveData.setValue(new ArrayList<>());
                    }
                });
    }

    public MutableLiveData<List<Voucher>> getVouchers() {
        return vouchersLiveData;
    }
}