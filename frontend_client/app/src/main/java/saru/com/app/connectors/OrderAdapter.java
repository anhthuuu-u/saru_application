package saru.com.app.connectors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.order_item, null);
        }

        Order order = orderList.get(position);

        TextView txtOrderID = convertView.findViewById(R.id.txtOrderID);
        TextView txtOrderDate = convertView.findViewById(R.id.txtOrderDate);
        Button btnOrderStatus = convertView.findViewById(R.id.btnOrderStatus);
        TextView txtTotalProduct = convertView.findViewById(R.id.txtTotalProduct);
        TextView txtTotalValue = convertView.findViewById(R.id.txtTotalValue);
        TextView tvOrderDetails = convertView.findViewById(R.id.tv_order_details);

        // Set the order details and total value
        txtOrderID.setText(order.getOrderID());
        txtOrderDate.setText(order.getOrderDate());
        btnOrderStatus.setText(order.getOrderStatus());
        txtTotalProduct.setText(String.valueOf(order.getTotalProduct()));
        txtTotalValue.setText(String.format("%.0f", order.gettotalValue()));

        // Set onClick listener for order details
        tvOrderDetails.setOnClickListener(v -> {
            // Create an Intent to open the OrderDetailActivity
            Intent intent = new Intent(context, OrderDetailActivity.class);
            // Pass the order ID to the next activity to fetch the details
            intent.putExtra("ORDER_ID", order.getOrderID());
            context.startActivity(intent);
        });

        return convertView;
    }
}