package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MediaGalleryAdapter extends RecyclerView.Adapter<MediaGalleryAdapter.MediaGalleryViewHolder> {
    
    private static final String TAG = "MediaGalleryAdapter";
    private Context context;
    private List<MediaItem> mediaItems;
    private OnMediaClickListener onMediaClickListener;
    private OnMediaRemoveListener onMediaRemoveListener;
    
    public interface OnMediaClickListener {
        void onMediaClick(int position, MediaItem mediaItem);
    }
    
    public interface OnMediaRemoveListener {
        void onMediaRemove(int position, MediaItem mediaItem);
    }
    
    public MediaGalleryAdapter(Context context, List<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
    }
    
    public void setOnMediaClickListener(OnMediaClickListener listener) {
        this.onMediaClickListener = listener;
    }
    
    public void setOnMediaRemoveListener(OnMediaRemoveListener listener) {
        this.onMediaRemoveListener = listener;
    }
    
    @NonNull
    @Override
    public MediaGalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_professional_media, parent, false);
        return new MediaGalleryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MediaGalleryViewHolder holder, int position) {
        // Ensure VideoView is hidden (we only show thumbnails)
        if (holder.videoView != null) {
            holder.videoView.setVisibility(View.GONE);
            holder.videoView.setVideoURI(null);
        }
        
        if (position < 0 || position >= mediaItems.size()) {
            Log.e(TAG, "Invalid position in onBindViewHolder: " + position);
            return;
        }
        
        MediaItem mediaItem = mediaItems.get(position);
        if (mediaItem == null) {
            Log.e(TAG, "MediaItem is null at position: " + position);
            return;
        }
        
        Uri mediaUri = mediaItem.getUri();
        if (mediaUri == null) {
            Log.e(TAG, "MediaItem URI is null at position: " + position);
            return;
        }
        
        if (mediaItem.isImage()) {
            // Handle image
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.videoPlayIcon.setVisibility(View.GONE);
            holder.videoDurationText.setVisibility(View.GONE);
            
            // Ensure VideoView is hidden for images
            holder.videoView.setVisibility(View.GONE);
            
            // Set image with professional styling - handle both URL and local URIs
            try {
                String uriString = mediaUri.toString();
                if (uriString != null && uriString.startsWith("http")) {
                    // For URL-based images, use ProfilePictureCache for better loading
                    ProfilePictureCache.getInstance().loadChatImage(holder.imageView, uriString);
                } else {
                    // For local URIs, use setImageURI
                    holder.imageView.setImageURI(mediaUri);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                try {
                    holder.imageView.setImageResource(R.drawable.ic_video);
                } catch (Exception ex) {
                    Log.e(TAG, "Error setting fallback image: " + ex.getMessage());
                }
            }
        } else if (mediaItem.isVideo()) {
            // Handle video - show thumbnail only
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE); // Always hide VideoView
            holder.videoPlayIcon.setVisibility(View.VISIBLE);
            
            // Generate and show video thumbnail in ImageView
            setVideoThumbnail(holder, mediaUri);
            
            // Get and display video duration if available
            try {
                String duration = getVideoDuration(mediaUri);
                if (duration != null && !duration.isEmpty()) {
                    holder.videoDurationText.setVisibility(View.VISIBLE);
                    holder.videoDurationText.setText(duration);
                } else {
                    holder.videoDurationText.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting video duration: " + e.getMessage());
                holder.videoDurationText.setVisibility(View.GONE);
            }
        }
        
        // Set click listener for media
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMediaClickListener != null) {
                    onMediaClickListener.onMediaClick(position, mediaItem);
                }
            }
        });
        
        // Set click listener for remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMediaRemoveListener != null) {
                    onMediaRemoveListener.onMediaRemove(position, mediaItem);
                }
            }
        });
        
        // Show media count badge if there are multiple items
        if (mediaItems.size() > 1) {
            holder.mediaCountText.setVisibility(View.VISIBLE);
            holder.mediaCountText.setText(String.valueOf(position + 1));
        } else {
            holder.mediaCountText.setVisibility(View.GONE);
        }
    }
    
    /**
     * Generate and set video thumbnail
     */
    private void setVideoThumbnail(MediaGalleryViewHolder holder, Uri videoUri) {
        try {
            if (videoUri == null) {
                holder.imageView.setImageResource(R.drawable.ic_video);
                return;
            }
            
            Bitmap thumbnail = null;
            MediaMetadataRetriever retriever = null;
            
            try {
                // For local videos, use MediaMetadataRetriever (most reliable method)
                String uriString = videoUri.toString();
                if (uriString.startsWith("content://") || uriString.startsWith("file://") || uriString.startsWith("/")) {
                    retriever = new MediaMetadataRetriever();
                    
                    // Try different methods to set data source based on URI type
                    if (uriString.startsWith("content://")) {
                        retriever.setDataSource(context, videoUri);
                    } else if (uriString.startsWith("file://")) {
                        String path = videoUri.getPath();
                        if (path != null) {
                            retriever.setDataSource(path);
                        }
                    } else if (uriString.startsWith("/")) {
                        // Direct file path
                        retriever.setDataSource(uriString);
                    } else {
                        retriever.setDataSource(context, videoUri);
                    }
                    
                    thumbnail = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Illegal argument for MediaMetadataRetriever: " + e.getMessage());
            } catch (RuntimeException e) {
                Log.e(TAG, "Runtime exception with MediaMetadataRetriever: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Error getting video thumbnail with MediaMetadataRetriever: " + e.getMessage());
            } finally {
                if (retriever != null) {
                    try {
                        retriever.release();
                    } catch (Exception e) {
                        Log.e(TAG, "Error releasing MediaMetadataRetriever: " + e.getMessage());
                    }
                }
            }
            
            // Set thumbnail if available
            if (thumbnail != null) {
                holder.imageView.setImageBitmap(thumbnail);
            } else {
                // Fallback: Use a placeholder or default image
                try {
                    holder.imageView.setImageResource(R.drawable.ic_video);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting placeholder image: " + e.getMessage());
                }
                Log.w(TAG, "Could not generate video thumbnail, using placeholder");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting video thumbnail: " + e.getMessage(), e);
            try {
                holder.imageView.setImageResource(R.drawable.ic_video);
            } catch (Exception ex) {
                Log.e(TAG, "Error setting fallback image: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Get video duration in MM:SS format
     */
    private String getVideoDuration(Uri videoUri) {
        if (videoUri == null) {
            return null;
        }
        
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            
            if (durationStr != null && !durationStr.isEmpty()) {
                try {
                    long duration = Long.parseLong(durationStr);
                    return formatDuration(duration);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing video duration: " + durationStr);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument for MediaMetadataRetriever (duration): " + e.getMessage());
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception with MediaMetadataRetriever (duration): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error getting video duration: " + e.getMessage());
        } finally {
            if (retriever != null) {
                try {
                    retriever.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing MediaMetadataRetriever (duration): " + e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Format duration in milliseconds to MM:SS format
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("0:%02d", seconds);
        }
    }
    
    @Override
    public int getItemCount() {
        return mediaItems.size();
    }
    
    @Override
    public void onViewRecycled(@NonNull MediaGalleryViewHolder holder) {
        super.onViewRecycled(holder);
        // Clean up video view when ViewHolder is recycled
        if (holder.videoView != null) {
            holder.videoView.setVideoURI(null);
        }
    }
    
    public void updateMedia(List<MediaItem> newMediaItems) {
        int oldSize = this.mediaItems.size();
        this.mediaItems = newMediaItems;
        int newSize = this.mediaItems.size();
        
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
    
    public void addMedia(MediaItem mediaItem) {
        mediaItems.add(mediaItem);
        notifyItemInserted(mediaItems.size() - 1);
    }
    
    public void removeMedia(int position) {
        if (position >= 0 && position < mediaItems.size()) {
            mediaItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mediaItems.size());
        }
    }
    
    
    static class MediaGalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;
        ImageView removeButton;
        TextView mediaCountText;
        ImageView videoPlayIcon;
        TextView videoDurationText;
        
        public MediaGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            removeButton = itemView.findViewById(R.id.removeButton);
            mediaCountText = itemView.findViewById(R.id.imageCountText);
            videoPlayIcon = itemView.findViewById(R.id.videoPlayIcon);
            videoDurationText = itemView.findViewById(R.id.videoDurationText);
        }
    }
}

