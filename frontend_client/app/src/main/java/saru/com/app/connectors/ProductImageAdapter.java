package saru.com.app.connectors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import saru.com.app.R;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private List<String> imageUrls = new ArrayList<>();
    private int selectedPosition = 0;

    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls != null ? new ArrayList<>(newImageUrls) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.mipmap.img_saru_cup)
                .error(R.drawable.ic_ver_fail)
                .into(holder.imageView);

        // Highlight selected image
        if (position == selectedPosition) {
            holder.imageView.setAlpha(1f);
            holder.imageView.setScaleX(1.1f);
            holder.imageView.setScaleY(1.1f);
        } else {
            holder.imageView.setAlpha(0.6f);
            holder.imageView.setScaleX(1f);
            holder.imageView.setScaleY(1f);
        }

        // Handle click to select image
        holder.imageView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_product);
        }
    }
}