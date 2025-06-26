package saru.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.Voucher;

public class PromotionsManagementActivity extends AppCompatActivity {
    private RecyclerView rvVouchers;
    private Button btnAddVoucher;
    private FirebaseFirestore db;
    private List<Voucher> vouchers;
    private VoucherAdapter adapter;
    private static final int REQUEST_CODE_ADD_EDIT_VOUCHER = 900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions_management);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        loadVouchers();
        setupEvents();
    }

    private void initializeViews() {
        rvVouchers = findViewById(R.id.rvVouchers);
        btnAddVoucher = findViewById(R.id.btnAddVoucher);
    }

    private void setupRecyclerView() {
        vouchers = new ArrayList<>();
        adapter = new VoucherAdapter(vouchers);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        db.collection("vouchers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vouchers.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Voucher voucher = document.toObject(Voucher.class);
                        vouchers.add(voucher);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load vouchers: " + e.getMessage()));
    }

    private void setupEvents() {
        btnAddVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditVoucherActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_VOUCHER);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT_VOUCHER && resultCode == RESULT_OK) {
            loadVouchers();
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {
        private List<Voucher> voucherList;

        VoucherAdapter(List<Voucher> voucherList) {
            this.voucherList = voucherList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Voucher voucher = voucherList.get(position);
            holder.txtDescription.setText(voucher.getDescription());
            holder.txtCode.setText("Code: " + voucher.getVoucherCode());
            holder.txtExpiry.setText("Expires: " + voucher.getExpiryDate());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PromotionsManagementActivity.this, AddEditVoucherActivity.class);
                intent.putExtra("SELECTED_VOUCHER", voucher);
                startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_VOUCHER);
            });
        }

        @Override
        public int getItemCount() {
            return voucherList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDescription, txtCode, txtExpiry;

            ViewHolder(View itemView) {
                super(itemView);
                txtDescription = itemView.findViewById(R.id.txtVoucherDesc);
                txtCode = itemView.findViewById(R.id.txtVoucherCode);
                txtExpiry = itemView.findViewById(R.id.txtVoucherExpiry);
            }
        }
    }
}