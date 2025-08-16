package com.example.accizardlucban;

import android.graphics.Bitmap;

public class ChatMessage {
    private String content;
    private boolean isUser;
    private String timestamp;
    private Bitmap imageBitmap;
    private String imageUrl;

    public ChatMessage(String content, boolean isUser, String timestamp) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = null;
        this.imageUrl = null;
    }

    public ChatMessage(String content, boolean isUser, String timestamp, Bitmap imageBitmap) {
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.imageBitmap = imageBitmap;
        this.imageUrl = null;
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
}