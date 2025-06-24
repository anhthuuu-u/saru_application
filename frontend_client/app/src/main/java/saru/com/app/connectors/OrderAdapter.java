package saru.com.app.connectors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import saru.com.app.R;
import saru.com.app.activities.OrderDetailActivity;
import saru.com.app.models.Order;

public class OrderAdapter extends ArrayAdapter<Order> {
    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        super(context, R.layout.order_item, orderList);
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.order_item, parent, false);
        }

        Order order = orderList.get(position);

        TextView txtOrderID = convertView.findViewById(R.id.txtOrderID);
        TextView txtOrderDate = convertView.findViewById(R.id.txtOrderDate);
        Button btnOrderStatus = convertView.findViewById(R.id.btnOrderStatus);
        TextView txtTotalProduct = convertView.findViewById(R.id.txtTotalProduct);
        TextView txtTotalValue = convertView.findViewById(R.id.txtTotalValue);
        TextView tvOrderDetails = convertView.findViewById(R.id.tv_order_details);

        txtOrderID.setText(order.getOrderID() != null ? order.getOrderID() : "Unknown");
        txtOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "Unknown");
        btnOrderStatus.setText(order.getOrderStatus() != null ? order.getOrderStatus() : "Unknown");
        txtTotalProduct.setText(String.valueOf(order.getTotalProduct()));

        DecimalFormat formatter = new DecimalFormat("#,###");
        txtTotalValue.setText(formatter.format(order.gettotalValue()) + " VNĐ");

        tvOrderDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderID());
            context.startActivity(intent);
        });

        return convertView;
    }
}