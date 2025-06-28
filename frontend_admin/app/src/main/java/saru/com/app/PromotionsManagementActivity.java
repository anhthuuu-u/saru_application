package saru.com.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.Voucher; // Import model Voucher

public class PromotionsManagementActivity extends AppCompatActivity {
    private RecyclerView rvVouchers;
    private Button btnAddVoucher;
    private FirebaseFirestore db;
    private List<Voucher> vouchers;
    private VoucherAdapter adapter;
    private static final int REQUEST_CODE_ADD_EDIT_VOUCHER = 900; // Request code cho Add/Edit Voucher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions_management);
        db = FirebaseFirestore.getInstance();
        setupToolbar(); // Thiết lập Toolbar
        initializeViews();
        setupRecyclerView();
        loadVouchers(); // Tải danh sách Vouchers
        setupEvents();
    }

    private void initializeViews() {
        rvVouchers = findViewById(R.id.rvVouchers);
        btnAddVoucher = findViewById(R.id.btnAddVoucher);
    }

    // Thiết lập Toolbar cho Activity
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_promotions_management);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setTitle(R.string.title_promotions_management); // Đặt tiêu đề
        }
    }

    // Xử lý sự kiện khi nút trên Toolbar được chọn (ví dụ: nút quay lại)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Xử lý khi nút quay lại được nhấn
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        vouchers = new ArrayList<>();
        adapter = new VoucherAdapter(vouchers);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);
    }

    // Tải danh sách Vouchers từ Firebase Firestore
    private void loadVouchers() {
        db.collection("vouchers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vouchers.clear(); // Xóa dữ liệu cũ
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Voucher voucher = document.toObject(Voucher.class);
                        vouchers.add(voucher);
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                })
                .addOnFailureListener(e -> showToast("Failed to load vouchers: " + e.getMessage()));
    }

    private void setupEvents() {
        btnAddVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditVoucherActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_VOUCHER); // Mở màn hình Add/Edit Voucher để thêm mới
        });
    }

    // Xử lý kết quả trả về từ AddEditVoucherActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT_VOUCHER && resultCode == RESULT_OK) {
            loadVouchers(); // Tải lại danh sách Vouchers khi thêm/sửa thành công
        }
    }

    // Hiển thị Toast message
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    // Phương thức hiển thị dialog xác nhận xóa Voucher
    private void showDeleteConfirmationDialog(final Voucher voucherToDelete) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title) // Tiêu đề popup, dùng chung
                .setMessage(getString(R.string.confirm_delete_message, voucherToDelete.getDescription())) // Message, dùng chung
                .setPositiveButton(R.string.confirm_delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteVoucher(voucherToDelete); // Gọi phương thức xóa khi người dùng xác nhận
                    }
                })
                .setNegativeButton(R.string.confirm_delete_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Phương thức xóa Voucher khỏi Firestore và cập nhật UI
    private void deleteVoucher(Voucher voucher) {
        if (voucher != null && voucher.getVoucherID() != null) {
            db.collection("vouchers").document(voucher.getVoucherID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast("Voucher deleted successfully!");
                        loadVouchers(); // Tải lại danh sách Vouchers để cập nhật UI
                    })
                    .addOnFailureListener(e -> showToast("Failed to delete Voucher: " + e.getMessage()));
        } else {
            showToast("Cannot delete a null Voucher or Voucher with no ID.");
        }
    }

    // Adapter cho RecyclerView để hiển thị danh sách Vouchers
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
            holder.txtCode.setText(getString(R.string.voucher_code_prefix, voucher.getVoucherCode())); // Cần định nghĩa @string/voucher_code_prefix
            holder.txtExpiry.setText(getString(R.string.voucher_expiry_prefix, voucher.getExpiryDate())); // Cần định nghĩa @string/voucher_expiry_prefix
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PromotionsManagementActivity.this, AddEditVoucherActivity.class);
                intent.putExtra("SELECTED_VOUCHER", voucher);
                startActivityForResult(intent, REQUEST_CODE_ADD_EDIT_VOUCHER); // Mở màn hình Add/Edit Voucher để sửa
            });

            // Thêm LongClickListener để xóa Voucher
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(voucher); // Hiển thị popup xác nhận khi giữ lâu
                return true; // Trả về true để tiêu thụ sự kiện long click
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
