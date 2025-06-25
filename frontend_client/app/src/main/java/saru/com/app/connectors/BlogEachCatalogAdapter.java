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

import saru.com.app.R;
import saru.com.app.activities.Blog_BlogDetailActivity;
import saru.com.app.models.Blog;

public class BlogEachCatalogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BLOG = 1;

    private final Context context;
    private final List<Blog> blogList;
    private final String categoryImageUrl;

    public BlogEachCatalogAdapter(Context context, List<Blog> blogList, String categoryImageUrl) {
        this.context = context;
        this.blogList = blogList;
        this.categoryImageUrl = categoryImageUrl;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_blog_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_blog_each_catalog, parent, false);
            return new BlogViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            Glide.with(context)
                    .load(categoryImageUrl != null ? categoryImageUrl : R.mipmap.img_taybacvillage)
                    .placeholder(R.mipmap.img_taybacvillage)
                    .into(headerHolder.imgCategoryHeader);
        } else if (holder instanceof BlogViewHolder) {
            BlogViewHolder blogHolder = (BlogViewHolder) holder;
            int blogPosition = position - 1;
            if (blogPosition >= 0 && blogPosition < blogList.size()) {
                Blog blog = blogList.get(blogPosition);
                blogHolder.txtBlogTitle.setText(blog.getTitle() != null ? blog.getTitle() : "No Title");
                String contentPreview = blog.getContent() != null && blog.getContent().length() > 100
                        ? blog.getContent().substring(0, 100) + "..."
                        : (blog.getContent() != null ? blog.getContent() : "No Content");
                blogHolder.txtBlogContent.setText(contentPreview);
                Glide.with(context)
                        .load(blog.getImageUrl() != null ? blog.getImageUrl() : R.mipmap.img_taybacvillage)
                        .placeholder(R.mipmap.img_taybacvillage)
                        .into(blogHolder.imgBlog);

                blogHolder.itemView.setOnClickListener(v -> {
                    if (blog.getBlogID() != null) {
                        Intent intent = new Intent(context, Blog_BlogDetailActivity.class);
                        intent.putExtra("blogID", blog.getBlogID());
                        context.startActivity(intent);
                    }
                });

                blogHolder.txtSeemore.setOnClickListener(v -> {
                    if (blog.getBlogID() != null) {
                        Intent intent = new Intent(context, Blog_BlogDetailActivity.class);
                        intent.putExtra("blogID", blog.getBlogID());
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return blogList.size() + 1; // Thêm 1 cho header
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_BLOG;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryHeader = itemView.findViewById(R.id.imgCategoryHeader);
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBlog;
        TextView txtBlogTitle, txtBlogContent, txtSeemore;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlog = itemView.findViewById(R.id.imageView7);
            txtBlogTitle = itemView.findViewById(R.id.txtAboutus_StoreLocation);
            txtBlogContent = itemView.findViewById(R.id.textView9);
            txtSeemore = itemView.findViewById(R.id.textView10);
        }
    }
}