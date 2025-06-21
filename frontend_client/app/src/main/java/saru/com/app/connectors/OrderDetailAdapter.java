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
        TextView txtOriginPrice = convertView.findViewById(R.id.txtShowOriginPrice); // Price before discount (Origin Price)
        TextView txtBrand = convertView.findViewById(R.id.txtShowBrand);
        ImageView imgProductImage = convertView.findViewById(R.id.imgProductImage);

        txtProductName.setText(orderDetail.getProductName());
        txtQuantity.setText(String.valueOf(orderDetail.getQuantity()));
//        txtPrice.setText(String.format(Locale.getDefault(), "%.0f", orderDetail.getPrice()));
        txtBrand.setText(orderDetail.getBrand());


        // Check if voucherID is present
        String voucherID = orderDetail.getVoucherID(); // Assuming getVoucherID() method exists
        if (voucherID != null && !voucherID.isEmpty()) {
            // If voucherID exists, show the origin price
            double originPrice = orderDetail.getPrice(); // Original price of the product
            txtOriginPrice.setText(String.format(Locale.getDefault(), "%,.0f", originPrice));
            txtOriginPrice.setVisibility(View.VISIBLE); // Ensure the price is visible
        } else {
            // If no voucherID, hide the origin price TextView
            txtOriginPrice.setVisibility(View.GONE); // Hide the origin price TextView
        }

        // Calculate total price (product price * quantity)
        double totalPrice = orderDetail.getPrice() * orderDetail.getQuantity();
        txtPrice.setText(String.format(Locale.getDefault(), "%,.0f", totalPrice)); // Format as currency or normal number
        // Load the product image using Glide
        String imageUrl = orderDetail.getProductImageCover();
        if (imageUrl != null) {
            Glide.with(context)
                    .load(imageUrl)  // Use the image URL from Firestore
                    .into(imgProductImage);  // Load into the ImageView
        }
        return convertView;
    }
}

