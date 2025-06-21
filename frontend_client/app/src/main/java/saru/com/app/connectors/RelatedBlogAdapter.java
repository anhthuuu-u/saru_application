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

public class RelatedBlogAdapter extends RecyclerView.Adapter<RelatedBlogAdapter.BlogViewHolder> {

    private Context context;
    private List<Blog> blogList;
    private Map<String, String> categoryMap;

    public RelatedBlogAdapter(Context context, List<Blog> blogList, Map<String, String> categoryMap) {
        this.context = context;
        this.blogList = blogList;
        this.categoryMap = categoryMap;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        holder.txtTitle.setText(blog.getTitle() != null ? blog.getTitle() : "No Title");
        holder.txtCategory.setText(categoryMap.getOrDefault(blog.getCateblogID(), "Không rõ danh mục"));
        Glide.with(context)
                .load(blog.getImageUrl() != null ? blog.getImageUrl() : R.mipmap.img_taybacvillage)
                .placeholder(R.mipmap.img_taybacvillage)
                .into(holder.imgBlog);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Blog_BlogDetailActivity.class);
            intent.putExtra("blogID", blog.getBlogID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBlog;
        TextView txtTitle, txtCategory;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlog = itemView.findViewById(R.id.imgRelatedBlog);
            txtTitle = itemView.findViewById(R.id.txtRelatedBlogTitle);
            txtCategory = itemView.findViewById(R.id.txtRelatedBlogCategory);
        }
    }
}