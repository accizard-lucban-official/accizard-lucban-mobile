package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MediaGalleryAdapter extends RecyclerView.Adapter<MediaGalleryAdapter.MediaGalleryViewHolder> {
    
    private static final String TAG = "MediaGalleryAdapter";
    private static final String VIDEO_DEBUG_TAG = "ReportMediaVideo";
    private static final long VIDEO_PREPARE_TIMEOUT_MS = 0000L;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
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
            stopPlaybackWithCleanup(holder);
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
            if (holder.videoLoadingSpinner != null) {
                holder.videoLoadingSpinner.setVisibility(View.GONE);
            }
            
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
            if (holder.videoLoadingSpinner != null) {
                holder.videoLoadingSpinner.setVisibility(View.GONE);
            }
            holder.videoPlayIcon.setOnClickListener(v -> {
                logVideoDebug("Play icon tapped. Adapter position=" + holder.getAdapterPosition() + ", uri=" + mediaUri);
                playVideoInline(holder, mediaUri);
            });
            
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
        
        // Set click listener for media (only for images, not videos)
        // Videos should only be playable via the play button
        if (mediaItem.isImage()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onMediaClickListener != null) {
                        onMediaClickListener.onMediaClick(position, mediaItem);
                    }
                }
            });
        } else if (mediaItem.isVideo()) {
            // Remove click listener for videos - only play button should be clickable
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }
        
        // Set click listener for remove button (hide if no listener is set)
        if (onMediaRemoveListener != null) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMediaRemoveListener.onMediaRemove(position, mediaItem);
                }
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }
        
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

    /**
     * Play video inline with MediaController support.
     */
    private void playVideoInline(MediaGalleryViewHolder holder, Uri mediaUri) {
        if (holder == null || mediaUri == null) {
            return;
        }
        
        try {
            logVideoDebug("Start inline playback. position=" + holder.getAdapterPosition() + ", uri=" + mediaUri);
            holder.currentVideoUri = mediaUri;
            holder.videoPlayIcon.setVisibility(View.GONE);
            holder.videoDurationText.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.INVISIBLE); // show after prepared
            if (holder.videoLoadingSpinner != null) {
                holder.videoLoadingSpinner.setVisibility(View.VISIBLE);
            }
            schedulePreparationTimeout(holder, mediaUri);
            
            holder.videoView.setOnPreparedListener(mp -> {
                try {
                    holder.videoView.seekTo(1);
                } catch (Exception e) {
                    Log.e(TAG, "Error seeking inline video", e);
                    logVideoDebug("Seek error: " + e.getMessage());
                }
                cancelPreparationTimeout(holder);
                logVideoDebug("Video prepared. size=" + mp.getVideoWidth() + "x" + mp.getVideoHeight());
            if (holder.videoLoadingSpinner != null) {
                    holder.videoLoadingSpinner.setVisibility(View.GONE);
                }
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.mediaController.show();
                holder.videoView.start();
                logVideoDebug("Video playback started.");
            });
            
            holder.videoView.setOnCompletionListener(mp -> {
                logVideoDebug("Video playback completed. uri=" + holder.currentVideoUri);
                stopPlaybackWithCleanup(holder);
            });
            
            holder.videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Inline video error: what=" + what + ", extra=" + extra);
                logVideoDebug("Video error what=" + what + ", extra=" + extra);
                cancelPreparationTimeout(holder);
                stopPlaybackWithCleanup(holder);
                Toast.makeText(context, "Unable to preview video. Opening externally.", Toast.LENGTH_SHORT).show();
                openVideoExternally(mediaUri);
                return true;
            });

            holder.videoView.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    logVideoDebug("Buffering start for uri=" + holder.currentVideoUri);
                    if (holder.videoLoadingSpinner != null) {
                        holder.videoLoadingSpinner.setVisibility(View.VISIBLE);
                    }
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END || what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    logVideoDebug("Buffering end/render start for uri=" + holder.currentVideoUri);
                    if (holder.videoLoadingSpinner != null) {
                        holder.videoLoadingSpinner.setVisibility(View.GONE);
                    }
                }
                return false;
            });

            if (holder.mediaController == null) {
                holder.mediaController = new MediaController(context);
                holder.mediaController.setAnchorView(holder.videoView);
            }
            
            holder.videoView.setVideoURI(mediaUri);
            holder.videoView.setMediaController(holder.mediaController);
            holder.videoView.requestFocus();
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing inline video: " + e.getMessage(), e);
            logVideoDebug("Exception while starting playback: " + e.getMessage());
            stopPlaybackWithCleanup(holder);
        }
    }
    
    private void schedulePreparationTimeout(MediaGalleryViewHolder holder, Uri uri) {
        cancelPreparationTimeout(holder);
        holder.preparationTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                logVideoDebug("Video prepare timeout for uri=" + uri);
                if (holder.videoLoadingSpinner != null) {
                    holder.videoLoadingSpinner.setVisibility(View.GONE);
                }
                openVideoExternally(uri);
                stopPlaybackWithCleanup(holder);
                holder.preparationTimeoutRunnable = null;
            }
        };
        MAIN_HANDLER.postDelayed(holder.preparationTimeoutRunnable, VIDEO_PREPARE_TIMEOUT_MS);
    }

    private void cancelPreparationTimeout(MediaGalleryViewHolder holder) {
        if (holder.preparationTimeoutRunnable != null) {
            MAIN_HANDLER.removeCallbacks(holder.preparationTimeoutRunnable);
            holder.preparationTimeoutRunnable = null;
        }
    }

    private void openVideoExternally(Uri uri) {
        if (uri == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            logVideoDebug("Opened video externally for uri=" + uri);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No external player available: " + e.getMessage());
            Toast.makeText(context, "No video player available to open this file.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error opening external video player: " + e.getMessage());
        }
    }
    
    @Override
    public int getItemCount() {
        return mediaItems.size();
    }
    
    @Override
    public void onViewRecycled(@NonNull MediaGalleryViewHolder holder) {
        super.onViewRecycled(holder);
        stopPlaybackWithCleanup(holder);
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
    
    private void stopPlaybackWithCleanup(MediaGalleryViewHolder holder) {
        cancelPreparationTimeout(holder);
        holder.stopVideoPlayback();
    }
    
    static class MediaGalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;
        ImageView removeButton;
        TextView mediaCountText;
        ImageView videoPlayIcon;
        TextView videoDurationText;
        ProgressBar videoLoadingSpinner;
        MediaController mediaController;
        Uri currentVideoUri;
        Runnable preparationTimeoutRunnable;
        
        public MediaGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            removeButton = itemView.findViewById(R.id.removeButton);
            mediaCountText = itemView.findViewById(R.id.imageCountText);
            videoPlayIcon = itemView.findViewById(R.id.videoPlayIcon);
            videoDurationText = itemView.findViewById(R.id.videoDurationText);
            videoLoadingSpinner = itemView.findViewById(R.id.videoLoadingSpinner);
        }
        
        void stopVideoPlayback() {
            if (videoView != null) {
                try {
                    videoView.stopPlayback();
                } catch (Exception ignored) {}
                if (currentVideoUri != null) {
                    logVideoDebug("Stopping playback for uri=" + currentVideoUri);
                }
                videoView.setVideoURI(null);
                videoView.setVisibility(View.GONE);
            }
            if (mediaController != null) {
                mediaController.hide();
            }
            if (imageView != null) {
                imageView.setVisibility(View.VISIBLE);
            }
            if (videoPlayIcon != null) {
                videoPlayIcon.setVisibility(View.VISIBLE);
            }
            if (videoDurationText != null) {
                CharSequence durationText = videoDurationText.getText();
                if (durationText != null && durationText.length() > 0) {
                    videoDurationText.setVisibility(View.VISIBLE);
                } else {
                    videoDurationText.setVisibility(View.GONE);
                }
            }
            if (videoLoadingSpinner != null) {
                videoLoadingSpinner.setVisibility(View.GONE);
            }
        }
    }
    
    private static void logVideoDebug(String message) {
        Log.d(VIDEO_DEBUG_TAG, message);
    }
}

