package saru.com.app.connectors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import saru.com.app.R;
import saru.com.app.models.OrderDetail;

public class OrderDetailAdapter extends ArrayAdapter<OrderDetail> {
    private Context context;
    private List<OrderDetail> orderDetailList;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        super(context, R.layout.item_order_product, orderDetailList);
        this.context = context;
        this.orderDetailList = orderDetailList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_order_product, null);
        }

        OrderDetail orderDetail = orderDetailList.get(position);

        // Bind data to the view
        TextView txtProductName = convertView.findViewById(R.id.txtShowProductName);
        TextView txtQuantity = convertView.findViewById(R.id.txtShowQuantity);
        TextView txtPrice = convertView.findViewById(R.id.txtShowPriceAfterDiscount);
        TextView txtOriginPrice = convertView.findViewById(R.id.txtShowOriginPrice);
        TextView txtBrand = convertView.findViewById(R.id.txtShowBrand);
        ImageView imgProductImage = convertView.findViewById(R.id.imgProductImage);

        // Set product details
        txtProductName.setText(orderDetail.getProductName());
        txtQuantity.setText(String.valueOf(orderDetail.getQuantity()));
        txtBrand.setText(orderDetail.getBrand() != null ? orderDetail.getBrand() : "Unknown");

        // Format prices using Vietnamese currency
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Calculate total price (product price * quantity)
        double totalPrice = orderDetail.getPrice() * orderDetail.getQuantity();
        txtPrice.setText(formatter.format(totalPrice));

        // Handle voucher logic
        String voucherID = orderDetail.getVoucherID();
        if (voucherID != null && !voucherID.isEmpty()) {
            // Assume original price is stored in a field or calculated (e.g., price before discount)
            // For simplicity, using the same price as original price (adjust if you have a separate field)
            double originPrice = orderDetail.getPrice(); // Replace with actual original price if available
            txtOriginPrice.setText(formatter.format(originPrice * orderDetail.getQuantity()));
            txtOriginPrice.setVisibility(View.VISIBLE);
        } else {
            txtOriginPrice.setVisibility(View.GONE);
        }

        // Load the product image using Glide
        String imageUrl = orderDetail.getProductImageCover();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(imgProductImage);
        }

        return convertView;
    }
}