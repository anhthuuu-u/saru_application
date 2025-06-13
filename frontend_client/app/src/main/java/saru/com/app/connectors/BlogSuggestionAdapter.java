package saru.com.app.connectors;

import android.content.Context;
import android.content.Intent;
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

public class BlogSuggestionAdapter extends RecyclerView.Adapter<BlogSuggestionAdapter.BlogViewHolder> {
    private final Context context;
    private final List<Blog> blogList;
    private final Map<String, String> categoryMap;

    public BlogSuggestionAdapter(Context context, List<Blog> blogList, Map<String, String> categoryMap) {
        this.context = context;
        this.blogList = blogList;
        this.categoryMap = categoryMap;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog_suggestion, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        holder.txtBlogTitle.setText(blog.getTitle() != null ? blog.getTitle() : "No Title");
        holder.txtBlogContent.setText(blog.getContent() != null && blog.getContent().length() > 100
                ? blog.getContent().substring(0, 100) + "..."
                : (blog.getContent() != null ? blog.getContent() : "No Content"));

        String categoryName = categoryMap.getOrDefault(blog.getCateblogID(), "Không rõ danh mục");
        holder.txtCategoryName.setText(categoryName);

        Glide.with(context)
                .load(blog.getImageUrl() != null ? blog.getImageUrl() : R.mipmap.img_taybacvillage)
                .placeholder(R.mipmap.img_taybacvillage)
                .into(holder.imgBlogImage);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView txtBlogTitle, txtBlogContent, txtCategoryName;
        ImageView imgBlogImage;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBlogTitle = itemView.findViewById(R.id.txtBlogTitle);
            txtBlogContent = itemView.findViewById(R.id.txtBlogContent);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgBlogImage = itemView.findViewById(R.id.imgBlogImage);
        }
    }
}