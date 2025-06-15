package saru.com.app.connectors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import saru.com.app.R;
import saru.com.app.activities.Blog_EachCatalogActivity;
import saru.com.app.models.BlogCategory;

public class BlogCategoryAdapter extends RecyclerView.Adapter<BlogCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<BlogCategory> categoryList;

    public BlogCategoryAdapter(Context context, List<BlogCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        BlogCategory category = categoryList.get(position);
        holder.txtCategoryName.setText(category.getName());

        Glide.with(context)
                .load(category.getImageUrl())
                .placeholder(R.mipmap.img_taybacvillage)
                .into(holder.imgCategory);

        holder.cardView.setOnClickListener(v -> {
            if (category.getCateblogID() != null) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    Intent intent = new Intent(context, Blog_EachCatalogActivity.class);
                    intent.putExtra("cateblogID", category.getCateblogID());
                    context.startActivity(intent);
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }).start();
            } else {
                android.util.Log.e("BlogCategoryAdapter", "cateblogID is null for category at position: " + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView txtCategoryName;
        CardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            cardView = itemView.findViewById(R.id.card_view); // Thêm CardView để áp dụng hiệu ứng
        }
    }
}