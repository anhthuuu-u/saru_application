package saru.com.app.connectors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import saru.com.app.models.Voucher;
import java.util.List;
import saru.com.app.R;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_item, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.voucherTitle.setText(voucher.getVoucherCode());

        // Cắt description thành khoảng 50 ký tự
        String description = voucher.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 47) + "...";
        }
        holder.voucherDescription.setText(description);

        holder.voucherExpiry.setText("Sắp hết hạn: " + voucher.getExpiryDate());

        // Xử lý nút "Lưu"
        holder.voucherSaveButton.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Save voucher sucessfully!!", Toast.LENGTH_SHORT).show();
            // Thêm logic lưu voucher vào danh sách hoặc database nếu cần
        });
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ImageView voucherIcon;
        TextView voucherTitle;
        TextView voucherDescription;
        TextView voucherExpiry;
        Button voucherSaveButton;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            voucherIcon = itemView.findViewById(R.id.voucher_icon);
            voucherTitle = itemView.findViewById(R.id.voucher_title);
            voucherDescription = itemView.findViewById(R.id.voucher_description);
            voucherExpiry = itemView.findViewById(R.id.voucher_expiry);
            voucherSaveButton = itemView.findViewById(R.id.voucher_save_button);
        }
    }
}