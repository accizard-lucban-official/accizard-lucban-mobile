package com.example.accizardlucban;

import android.graphics.Bitmap;

public class ChatMessage {
    private String content;
    private boolean isUser;
    private String timestamp;
    private Bitmap imageBitmap;
    private String imageUrl;
    private String profilePictureUrl;
    
    // ✅ ADDED: Support for different attachment types from web app
    private String attachmentType; // "image", "file", "audio", "video"
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String videoUrl;
    private String audioUrl;
    
    // ✅ NEW: Track message ID and read status
    private String messageId;
    private boolean isRead;
    private String senderId;

    public ChatMessage(String content, boolean isUser, String timestamp) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = null;
        this.imageUrl = null;
        this.profilePictureUrl = null;
    }

    public ChatMessage(String content, boolean isUser, String timestamp, Bitmap imageBitmap) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = imageBitmap;
        this.imageUrl = null;
        this.profilePictureUrl = null;
    }
    
    public ChatMessage(String content, boolean isUser, String timestamp, String profilePictureUrl) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = null;
        this.imageUrl = null;
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public ChatMessage(String content, boolean isUser, String timestamp, Bitmap imageBitmap, String profilePictureUrl) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = imageBitmap;
        this.imageUrl = null;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasImage() {
        return imageBitmap != null || (imageUrl != null && !imageUrl.isEmpty());
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    // ✅ ADDED: Getters and setters for attachment metadata
    public String getAttachmentType() {
        return attachmentType;
    }
    
    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    // ✅ ADDED: Helper method to check if message has any attachment
    public boolean hasAttachment() {
        return hasImage() || 
               (attachmentType != null && !attachmentType.isEmpty()) ||
               (fileName != null && !fileName.isEmpty());
    }
    
    // ✅ ADDED: Helper method to get formatted file size
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    // ✅ NEW: Getters and setters for message ID and read status
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}