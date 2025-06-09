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
import saru.com.app.R;
import saru.com.app.models.Voucher;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    public void updateVouchers(List<Voucher> newVouchers) {
        this.voucherList = newVouchers;
        notifyDataSetChanged();
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
        holder.voucherID.setText(voucher.getVoucherID());

        String description = voucher.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 47) + "...";
        }
        holder.voucherDescription.setText(description);

        holder.voucherExpiry.setText("Expired Date: " + voucher.getExpiryDate());

        holder.voucherSaveButton.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Save voucher successfully!!", Toast.LENGTH_SHORT).show();
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
        TextView voucherID;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            voucherIcon = itemView.findViewById(R.id.voucher_icon);
            voucherTitle = itemView.findViewById(R.id.voucher_title);
            voucherDescription = itemView.findViewById(R.id.voucher_description);
            voucherExpiry = itemView.findViewById(R.id.voucher_expiry);
            voucherSaveButton = itemView.findViewById(R.id.voucher_save_button);
            voucherID = itemView.findViewById(R.id.txtVoucherID);
        }
    }
}