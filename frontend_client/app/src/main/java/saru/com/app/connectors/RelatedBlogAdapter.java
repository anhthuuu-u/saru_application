package saru.com.app.connectors;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

import saru.com.app.R;
import saru.com.app.activities.Blog_BlogDetailActivity;
import saru.com.app.models.Blog;

public class RelatedBlogAdapter extends RecyclerView.Adapter<RelatedBlogAdapter.RelatedViewHolder> {
    private Context context;
    private List<Blog> relatedBlogList;
    private Map<String, String> categoryMap;

    public RelatedBlogAdapter(Context context, List<Blog> relatedBlogList, Map<String, String> categoryMap) {
        this.context = context;
        this.relatedBlogList = relatedBlogList;
        this.categoryMap = categoryMap;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_blog, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Blog blog = relatedBlogList.get(position);
        Log.d("Blog_Adapter", "Binding blog at position " + position + ": " + blog.getTitle());
        holder.txtRelatedTitle.setText(blog.getTitle() != null ? blog.getTitle() : "No Title");
        holder.txtRelatedCategory.setText(categoryMap.getOrDefault(blog.getCateblogID(), "Không rõ danh mục"));
        Glide.with(context)
                .load(blog.getImageUrl() != null ? blog.getImageUrl() : R.mipmap.img_taybacvillage)
                .placeholder(R.mipmap.img_taybacvillage)
                .error(R.mipmap.img_taybacvillage) // Thêm để xử lý lỗi tải hình
                .into(holder.imgRelatedArticle);
        Log.d("Blog_Adapter", "Image URL: " + blog.getImageUrl());

        holder.itemView.setOnClickListener(v -> {
            Log.d("Blog_Adapter", "Clicked blog: " + blog.getBlogID());
            Intent intent = new Intent(context, Blog_BlogDetailActivity.class);
            intent.putExtra("blogID", blog.getBlogID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return relatedBlogList.size();
    }

    static class RelatedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRelatedArticle;
        TextView txtRelatedTitle, txtRelatedCategory;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRelatedArticle = itemView.findViewById(R.id.imgRelatedArticle);
            txtRelatedTitle = itemView.findViewById(R.id.txtRelatedTitle);
            txtRelatedCategory = itemView.findViewById(R.id.txtRelatedCategory);
        }
    }
}
