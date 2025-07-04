package saru.com.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import saru.com.app.R;
import saru.com.models.Orders;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Orders> orderList;
    private OnOrderClickListener editListener;

    public interface OnOrderClickListener {
        void onClick(Orders order);
    }

    public OrderAdapter(List<Orders> orderList, OnOrderClickListener editListener) {
        this.orderList = orderList;
        this.editListener = editListener;
    }

    public void updateList(List<Orders> newList) {
        orderList.clear();
        orderList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders order = orderList.get(position);
        holder.txtOrderID.setText(order.getOrderID() != null ? order.getOrderID() : "");
        holder.txtOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "");
        holder.txtCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "");
        holder.txtTotalAmount.setText(String.format("%,.0f VNĐ", order.getTotalAmount()));
        holder.txtOrderStatus.setText(order.getOrderStatusID() != null ? order.getOrderStatusID() : "");
        holder.btnEdit.setOnClickListener(v -> editListener.onClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderID, txtOrderDate, txtCustomerName, txtTotalAmount, txtOrderStatus;
        Button btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            txtOrderID = itemView.findViewById(R.id.txtOrderID);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtCustomerName =itemView.findViewById(R.id.txtCustomerName);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}