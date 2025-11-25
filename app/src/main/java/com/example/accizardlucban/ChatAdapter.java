package com.example.accizardlucban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.net.Uri;
import android.media.MediaPlayer;
import android.widget.MediaController;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.util.Log;
import android.graphics.Outline;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ADMIN = 2;
    private static final String TAG = "ChatAdapter";

    private List<ChatMessage> messagesList;

    public ChatAdapter(List<ChatMessage> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ADMIN;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_admin, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messagesList.get(position);
        holder.bind(message);
    }
    
    @Override
    public void onViewRecycled(@NonNull ChatViewHolder holder) {
        super.onViewRecycled(holder);
        // Clean up video resources when view is recycled
        holder.cleanupVideo();
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull ChatViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Re-anchor MediaController when view becomes visible
        holder.reattachMediaController();
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timestampText;
        private ImageView messageImage;
        private LinearLayout imageContainer;
        private ImageView adminProfilePicture;
        private ImageView userProfilePicture;
        private VideoView messageVideo;
        private LinearLayout messageAudioContainer;
        private LinearLayout messageBubble; // The bubble container
        private MediaController mediaController; // Store MediaController reference
        private String currentVideoUrl; // Track current video URL to prevent unnecessary reloads

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            messageImage = itemView.findViewById(R.id.messageImage);
            imageContainer = itemView.findViewById(R.id.imageContainer);
            adminProfilePicture = itemView.findViewById(R.id.adminProfilePicture);
            userProfilePicture = itemView.findViewById(R.id.userProfilePicture);
            messageVideo = itemView.findViewById(R.id.messageVideo);
            messageAudioContainer = itemView.findViewById(R.id.messageAudioContainer);
            
            // Find the message bubble (parent of messageText)
            if (messageText != null && messageText.getParent() instanceof LinearLayout) {
                messageBubble = (LinearLayout) messageText.getParent();
            }
            
            // Initialize currentVideoUrl
            currentVideoUrl = null;
        }
        
        /**
         * Clean up video resources when view is recycled
         */
        public void cleanupVideo() {
            if (messageVideo != null) {
                try {
                    // Stop and release video
                    messageVideo.stopPlayback();
                    messageVideo.setVideoURI(null);
                    
                    // Remove MediaController
                    if (mediaController != null) {
                        mediaController.hide();
                        messageVideo.setMediaController(null);
                        mediaController = null;
                    }
                    
                    // Clear listeners to prevent memory leaks
                    messageVideo.setOnPreparedListener(null);
                    messageVideo.setOnErrorListener(null);
                    messageVideo.setOnCompletionListener(null);
                    
                    currentVideoUrl = null;
                    Log.d(TAG, "Video cleaned up for recycled view");
                } catch (Exception e) {
                    Log.e(TAG, "Error cleaning up video", e);
                }
            }
        }
        
        /**
         * Re-attach MediaController when view becomes visible again
         * This ensures the play button stays with the correct video
         */
        public void reattachMediaController() {
            if (messageVideo != null && mediaController != null && currentVideoUrl != null) {
                try {
                    // Re-anchor MediaController to ensure it stays with this VideoView
                    mediaController.setAnchorView(messageVideo);
                    messageVideo.setMediaController(mediaController);
                    Log.d(TAG, "MediaController re-attached to VideoView for URL: " + currentVideoUrl);
                } catch (Exception e) {
                    Log.e(TAG, "Error re-attaching MediaController", e);
                }
            }
        }

        public void bind(ChatMessage message) {
            // Handle media attachments first
            boolean hasMediaAttachment = message.hasAttachment() && imageContainer != null;
            
            if (hasMediaAttachment) {
                imageContainer.setVisibility(View.VISIBLE);
                
                String attachmentType = message.getAttachmentType();
                String videoUrl = message.getVideoUrl();
                String audioUrl = message.getAudioUrl();
                
                // First hide all media views
                if (messageImage != null) messageImage.setVisibility(View.GONE);
                if (messageVideo != null) messageVideo.setVisibility(View.GONE);
                if (messageAudioContainer != null) messageAudioContainer.setVisibility(View.GONE);
                
                // Show appropriate media based on type
                // Check for video first (by attachmentType or videoUrl)
                if (("video".equals(attachmentType) || (videoUrl != null && !videoUrl.isEmpty())) && messageVideo != null) {
                    // Video attachment
                    messageVideo.setVisibility(View.VISIBLE);
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        // Only setup video if it's a different URL (prevents unnecessary reloads)
                        if (!videoUrl.equals(currentVideoUrl)) {
                            Log.d(TAG, "Displaying video message: " + (message.isUser() ? "USER" : "ADMIN") + ", videoUrl=" + videoUrl);
                            setupVideoPlayer(videoUrl);
                        }
                    } else {
                        Log.w(TAG, "Video attachment type detected but no videoUrl found");
                        cleanupVideo();
                    }
                } else if (("audio".equals(attachmentType) || (audioUrl != null && !audioUrl.isEmpty())) && messageAudioContainer != null) {
                    // Not a video message, clean up any existing video
                    if (currentVideoUrl != null) {
                        cleanupVideo();
                    }
                    // Audio attachment
                    messageAudioContainer.setVisibility(View.VISIBLE);
                    if (audioUrl != null && !audioUrl.isEmpty()) {
                        // Make the audio container clickable to play audio
                        messageAudioContainer.setOnClickListener(v -> playAudio(audioUrl));
                        Log.d(TAG, "Loading chat audio from URL: " + audioUrl);
                    } else {
                        Log.w(TAG, "Audio attachment type detected but no audioUrl found");
                    }
                } else if (messageImage != null) {
                    // Not a video or audio message, clean up any existing video
                    if (currentVideoUrl != null) {
                        cleanupVideo();
                    }
                    // Image attachment (default or legacy)
                    messageImage.setVisibility(View.VISIBLE);
                    
                    // Apply rounded corners to ImageView (after view is measured)
                    messageImage.post(() -> {
                        float radius = 12 * itemView.getContext().getResources().getDisplayMetrics().density;
                        messageImage.setOutlineProvider(new ViewOutlineProvider() {
                            @Override
                            public void getOutline(View view, Outline outline) {
                                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                            }
                        });
                        messageImage.setClipToOutline(true);
                    });
                    
                    if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                        ProfilePictureCache.getInstance().loadChatImage(messageImage, message.getImageUrl());
                        Log.d(TAG, "Loading chat image from URL: " + message.getImageUrl());
                    } else if (message.getImageBitmap() != null) {
                        messageImage.setImageBitmap(message.getImageBitmap());
                        Log.d(TAG, "Loading chat image from Bitmap");
                    } else {
                        messageImage.setImageResource(R.drawable.ic_camera_placeholder);
                        Log.w(TAG, "Message has attachment flag but no media data");
                    }
                }
            } else if (imageContainer != null) {
                imageContainer.setVisibility(View.GONE);
            }
            
            // Handle message text - hide if there's a media attachment or if content is empty
            String content = message.getContent();
            boolean hasTextContent = content != null && !content.trim().isEmpty();
            boolean shouldShowBubble = hasTextContent && !hasMediaAttachment;
            
            if (messageText != null) {
                if (hasMediaAttachment || !hasTextContent) {
                    messageText.setVisibility(View.GONE);
                } else {
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(content);
                }
            }
            
            // Hide the entire message bubble if there's only media attachment
            if (messageBubble != null) {
                if (shouldShowBubble) {
                    messageBubble.setVisibility(View.VISIBLE);
                } else {
                    messageBubble.setVisibility(View.GONE);
                }
            }
            
            if (timestampText != null) {
                timestampText.setText(message.getTimestamp());
            }
            
            // Handle profile pictures
            if (message.isUser()) {
                // User message - show user profile picture
                if (userProfilePicture != null) {
                    userProfilePicture.setVisibility(View.VISIBLE);
                    // Load user profile picture from URL with global caching
                    String profileUrl = message.getProfilePictureUrl();
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        ProfilePictureCache.getInstance().loadProfilePicture(userProfilePicture, profileUrl);
                    } else {
                        // Set default profile picture
                        userProfilePicture.setImageResource(R.drawable.ic_person);
                    }
                }
                if (adminProfilePicture != null) {
                    adminProfilePicture.setVisibility(View.GONE);
                }
            } else {
                // Admin message - show admin profile picture (app icon)
                if (adminProfilePicture != null) {
                    adminProfilePicture.setVisibility(View.VISIBLE);
                    adminProfilePicture.setImageResource(R.drawable.appiconpng);
                }
                if (userProfilePicture != null) {
                    userProfilePicture.setVisibility(View.GONE);
                }
            }
        }
        
        private void setupVideoPlayer(String videoUrl) {
            try {
                // Clean up previous video if exists
                if (currentVideoUrl != null && !currentVideoUrl.equals(videoUrl)) {
                    cleanupVideo();
                }
                
                // If this is the same video URL, don't reload
                if (videoUrl.equals(currentVideoUrl) && mediaController != null) {
                    Log.d(TAG, "Video already set up for URL: " + videoUrl);
                    return;
                }
                
                Uri videoUri = Uri.parse(videoUrl);
                
                // Stop any existing playback first
                if (messageVideo != null) {
                    messageVideo.stopPlayback();
                    messageVideo.setVideoURI(null);
                }
                
                // Remove old MediaController if exists
                if (mediaController != null) {
                    try {
                        mediaController.hide();
                    } catch (Exception e) {
                        Log.w(TAG, "Error hiding old MediaController", e);
                    }
                    if (messageVideo != null) {
                        messageVideo.setMediaController(null);
                    }
                    mediaController = null;
                }
                
                // Apply rounded corners to VideoView (after view is measured)
                if (messageVideo != null) {
                    messageVideo.post(() -> {
                        try {
                            float radius = 12 * itemView.getContext().getResources().getDisplayMetrics().density;
                            messageVideo.setOutlineProvider(new ViewOutlineProvider() {
                                @Override
                                public void getOutline(View view, Outline outline) {
                                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                                }
                            });
                            messageVideo.setClipToOutline(true);
                        } catch (Exception e) {
                            Log.e(TAG, "Error applying rounded corners to VideoView", e);
                        }
                    });
                }
                
                // Set the video URI
                if (messageVideo != null) {
                    messageVideo.setVideoURI(videoUri);
                    
                    // Create new MediaController for this specific VideoView
                    // Use a custom MediaController that's properly anchored
                    mediaController = new MediaController(itemView.getContext()) {
                        @Override
                        public void show(int timeout) {
                            // Ensure MediaController is anchored to the correct VideoView
                            if (messageVideo != null) {
                                setAnchorView(messageVideo);
                            }
                            super.show(timeout);
                        }
                    };
                    
                    // Set anchor view BEFORE setting MediaController
                    mediaController.setAnchorView(messageVideo);
                    messageVideo.setMediaController(mediaController);
                    
                    // Set up video player callbacks
                    messageVideo.setOnPreparedListener(mp -> {
                        Log.d(TAG, "Video prepared successfully for URL: " + videoUrl);
                        // After preparation, seek to 1ms to show first frame
                        try {
                            if (messageVideo != null && messageVideo.isPlaying() == false) {
                                messageVideo.seekTo(1);
                                // Ensure MediaController is still properly attached
                                if (mediaController != null && messageVideo != null) {
                                    mediaController.setAnchorView(messageVideo);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error seeking video", e);
                        }
                    });
                    
                    messageVideo.setOnErrorListener((mp, what, extra) -> {
                        Log.e(TAG, "Video playback error for URL " + videoUrl + ": what=" + what + ", extra=" + extra);
                        return true;
                    });
                    
                    messageVideo.setOnCompletionListener(mp -> {
                        Log.d(TAG, "Video playback completed for URL: " + videoUrl);
                    });
                    
                    // Store current video URL
                    currentVideoUrl = videoUrl;
                    
                    // Request focus to ensure video view is properly displayed
                    messageVideo.requestFocus();
                    
                    Log.d(TAG, "Video player setup complete for URL: " + videoUrl + ", MediaController anchored to VideoView");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up video player: " + e.getMessage(), e);
                currentVideoUrl = null;
                if (mediaController != null) {
                    mediaController = null;
                }
            }
        }
        
        private void playAudio(String audioUrl) {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioUrl);
                
                // Prepare asynchronously to avoid blocking UI
                mediaPlayer.prepareAsync();
                
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    Log.d(TAG, "Audio playback started from URL: " + audioUrl);
                });
                
                // Release MediaPlayer when playback completes
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    Log.d(TAG, "Audio playback completed");
                });
                
                // Handle errors
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                    mp.release();
                    return true;
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error playing audio: " + e.getMessage(), e);
            }
        }
    }
}