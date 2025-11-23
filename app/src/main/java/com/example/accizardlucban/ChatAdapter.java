package com.example.accizardlucban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        }

        public void bind(ChatMessage message) {
            if (messageText != null) {
                messageText.setText(message.getContent());
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
                    adminProfilePicture.setImageResource(R.drawable.appiconsquareandroid);
                }
                if (userProfilePicture != null) {
                    userProfilePicture.setVisibility(View.GONE);
                }
            }
            
            // Handle media attachments
            if (message.hasAttachment() && imageContainer != null) {
                imageContainer.setVisibility(View.VISIBLE);
                
                String attachmentType = message.getAttachmentType();
                
                // First hide all media views
                if (messageImage != null) messageImage.setVisibility(View.GONE);
                if (messageVideo != null) messageVideo.setVisibility(View.GONE);
                if (messageAudioContainer != null) messageAudioContainer.setVisibility(View.GONE);
                
                // Show appropriate media based on type
                if ("video".equals(attachmentType) && messageVideo != null) {
                    // Video attachment
                    messageVideo.setVisibility(View.VISIBLE);
                    String videoUrl = message.getVideoUrl();
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        setupVideoPlayer(videoUrl);
                    }
                } else if ("audio".equals(attachmentType) && messageAudioContainer != null) {
                    // Audio attachment
                    messageAudioContainer.setVisibility(View.VISIBLE);
                    String audioUrl = message.getAudioUrl();
                    if (audioUrl != null && !audioUrl.isEmpty()) {
                        // Make the audio container clickable to play audio
                        messageAudioContainer.setOnClickListener(v -> playAudio(audioUrl));
                        Log.d(TAG, "Loading chat audio from URL: " + audioUrl);
                    }
                } else if (messageImage != null) {
                    // Image attachment (default or legacy)
                    messageImage.setVisibility(View.VISIBLE);
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
        }
        
        private void setupVideoPlayer(String videoUrl) {
            try {
                Uri videoUri = Uri.parse(videoUrl);
                
                // Clear previous video URI to avoid issues with recycling
                messageVideo.setVideoURI(null);
                
                // Set the video URI first
                messageVideo.setVideoURI(videoUri);
                
                // Create and set MediaController for video controls
                MediaController mediaController = new MediaController(itemView.getContext());
                mediaController.setAnchorView(messageVideo);
                messageVideo.setMediaController(mediaController);
                
                // Set up video player callbacks
                messageVideo.setOnPreparedListener(mp -> {
                    Log.d(TAG, "Video prepared successfully");
                    // After preparation, seek to 1ms to show first frame
                    try {
                        messageVideo.seekTo(1);
                    } catch (Exception e) {
                        Log.e(TAG, "Error seeking video", e);
                    }
                });
                
                messageVideo.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Video playback error: what=" + what + ", extra=" + extra);
                    return true;
                });
                
                messageVideo.setOnCompletionListener(mp -> {
                    Log.d(TAG, "Video playback completed");
                });
                
                // Request focus to ensure video view is properly displayed
                messageVideo.requestFocus();
                
                Log.d(TAG, "Video player setup complete for URL: " + videoUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up video player: " + e.getMessage(), e);
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