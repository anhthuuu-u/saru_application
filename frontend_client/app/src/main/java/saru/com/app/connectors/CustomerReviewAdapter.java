package saru.com.app.connectors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import saru.com.app.R;
import saru.com.app.models.CustomerReviews;

public class CustomerReviewAdapter extends RecyclerView.Adapter<CustomerReviewAdapter.CustomerReviewViewHolder> {
    private List<CustomerReviews> reviewsList;

    public CustomerReviewAdapter(List<CustomerReviews> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public CustomerReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_review_item, parent, false);
        return new CustomerReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerReviewViewHolder holder, int position) {
        CustomerReviews review = reviewsList.get(position);
        holder.txtCustomerName.setText(review.getCustomerName());
        holder.txtReviewContent.setText(review.getReviewContent());
        holder.txtPurchasedProduct.setText("Purchased: " + review.getPurchasedProduct());

        // Tải ảnh từ URL bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(review.getCustomerImage())
                .placeholder(R.mipmap.img_saru_cup) // Thêm placeholder nếu có
                .error(R.drawable.ic_account) // Thêm ảnh lỗi nếu có
                .into(holder.imgCustomer);
    }

    @Override
    public int getItemCount() {
        return reviewsList != null ? reviewsList.size() : 0;
    }

    public void updateReviews(List<CustomerReviews> newReviews) {
        this.reviewsList = newReviews;
        notifyDataSetChanged();
    }

    public static class CustomerReviewViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgCustomer;
        public TextView txtCustomerName;
        public TextView txtReviewContent;
        public TextView txtPurchasedProduct;

        public CustomerReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCustomer = itemView.findViewById(R.id.imgCustomer);
            txtCustomerName = itemView.findViewById(R.id.edtCustomerName);
            txtReviewContent = itemView.findViewById(R.id.txtReviewContent);
            txtPurchasedProduct = itemView.findViewById(R.id.txtPurchasedProduct);
        }
    }
}