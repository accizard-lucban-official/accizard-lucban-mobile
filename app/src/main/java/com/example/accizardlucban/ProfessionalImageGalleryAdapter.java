package com.example.accizardlucban;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfessionalImageGalleryAdapter extends RecyclerView.Adapter<ProfessionalImageGalleryAdapter.ImageGalleryViewHolder> {
    
    private Context context;
    private List<Uri> imageUris;
    private OnImageClickListener onImageClickListener;
    private OnImageRemoveListener onImageRemoveListener;
    
    public interface OnImageClickListener {
        void onImageClick(int position, Uri imageUri);
    }
    
    public interface OnImageRemoveListener {
        void onImageRemove(int position, Uri imageUri);
    }
    
    public ProfessionalImageGalleryAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }
    
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }
    
    public void setOnImageRemoveListener(OnImageRemoveListener listener) {
        this.onImageRemoveListener = listener;
    }
    
    @NonNull
    @Override
    public ImageGalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_professional_image, parent, false);
        return new ImageGalleryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageGalleryViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        
        // Set image with professional styling - handle both URL and local URIs
        if (imageUri.toString().startsWith("http")) {
            // For URL-based images, use ProfilePictureCache for better loading
            ProfilePictureCache.getInstance().loadChatImage(holder.imageView, imageUri.toString());
        } else {
            // For local URIs, use setImageURI
            holder.imageView.setImageURI(imageUri);
        }
        
        // Set click listener for image
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(position, imageUri);
                }
            }
        });
        
        // Set click listener for remove button (hide if no listener set)
        if (onImageRemoveListener != null) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onImageRemoveListener.onImageRemove(position, imageUri);
                }
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }
        
        // Show image count badge if there are multiple images
        if (imageUris.size() > 1) {
            holder.imageCountText.setVisibility(View.VISIBLE);
            holder.imageCountText.setText(String.valueOf(position + 1));
        } else {
            holder.imageCountText.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return imageUris.size();
    }
    
    public void updateImages(List<Uri> newImageUris) {
        int oldSize = this.imageUris.size();
        this.imageUris = newImageUris;
        int newSize = this.imageUris.size();
        
        if (newSize > oldSize) {
            // New items added - notify only for new items for smooth animation
            notifyItemRangeInserted(oldSize, newSize - oldSize);
            // Update existing items if needed
            if (oldSize > 0) {
                notifyItemRangeChanged(0, oldSize);
            }
        } else if (newSize < oldSize) {
            // Items removed
            notifyDataSetChanged();
        } else {
            // Same size, just update
            notifyDataSetChanged();
        }
    }
    
    public void addImage(Uri imageUri) {
        imageUris.add(imageUri);
        notifyItemInserted(imageUris.size() - 1);
    }
    
    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageUris.size());
        }
    }
    
    static class ImageGalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView removeButton;
        TextView imageCountText;
        
        public ImageGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            removeButton = itemView.findViewById(R.id.removeButton);
            imageCountText = itemView.findViewById(R.id.imageCountText);
        }
    }
}





















