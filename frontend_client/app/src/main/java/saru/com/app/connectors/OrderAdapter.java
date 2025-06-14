package saru.com.app.connectors;

import android.annotation.SuppressLint;
import android.content.Context;
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
        Button btnOrderStatus=convertView.findViewById(R.id.btnOrderStatus);
        TextView txtTotalProduct = convertView.findViewById(R.id.txtTotalProduct);
        TextView txtTotalValue = convertView.findViewById(R.id.txtTotalValue);

        Log.d("OrderAdapter", "Total Value for OrderID " + order.getOrderID() + ": " + order.gettotalValue());

        // Kiểm tra nếu txtTotalValue là null
        if (txtTotalValue == null) {
            Log.e("OrderAdapter", "txtTotalValue is null in getView()");
        }
        else {
            Log.d("OrderAdapter", "txtTotalValue is not null");

        }

        // Cập nhật giá trị cho txtTotalValue
        txtTotalValue.setText(String.format("%.0f", order.gettotalValue()));



        txtOrderID.setText(order.getOrderID());
        txtOrderDate.setText(order.getOrderDate());
        btnOrderStatus.setText(order.getOrderStatus());
        txtTotalProduct.setText(String.valueOf(order.getTotalProduct()));


        return convertView;
    }
}
