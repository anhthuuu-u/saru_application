package saru.com.app.connectors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.models.OrderDetail;

public class OrderDetailRecyclerAdapter extends RecyclerView.Adapter<OrderDetailRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<OrderDetail> orderDetailList;

    public OrderDetailRecyclerAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);

        // Bind data to views
        holder.txtProductName.setText(orderDetail.getProductName());
        holder.txtQuantity.setText(String.valueOf(orderDetail.getQuantity()));
        holder.txtBrand.setText(orderDetail.getBrand());

        // Format total price (product price * quantity)
        double totalPrice = orderDetail.getPrice() * orderDetail.getQuantity();
        holder.txtPrice.setText(String.format(Locale.getDefault(), "%,.0f", totalPrice));

        // Check if voucherID is present
        String voucherID = orderDetail.getVoucherID();
        if (voucherID != null && !voucherID.isEmpty()) {
            double originPrice = orderDetail.getPrice();
            holder.txtOriginPrice.setText(String.format(Locale.getDefault(), "%,.0f", originPrice));
            holder.txtOriginPrice.setVisibility(View.VISIBLE);
        } else {
            holder.txtOriginPrice.setVisibility(View.GONE);
        }

        // Load product image using Glide
        String imageUrl = orderDetail.getProductImageCover();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.imgProductImage);
        }
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtQuantity, txtPrice, txtOriginPrice, txtBrand;
        ImageView imgProductImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtShowProductName);
            txtQuantity = itemView.findViewById(R.id.txtShowQuantity);
            txtPrice = itemView.findViewById(R.id.txtShowPriceAfterDiscount);
            txtOriginPrice = itemView.findViewById(R.id.txtShowOriginPrice);
            txtBrand = itemView.findViewById(R.id.txtShowBrand);
            imgProductImage = itemView.findViewById(R.id.imgProductImage);
        }
    }
}