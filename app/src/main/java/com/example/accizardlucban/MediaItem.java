package com.example.accizardlucban;

import android.net.Uri;

/**
 * Represents a media item (image or video) for report submissions
 */
public class MediaItem {
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    
    private Uri uri;
    private String type; // "image" or "video"
    
    public MediaItem(Uri uri, String type) {
        this.uri = uri;
        this.type = type != null ? type : TYPE_IMAGE;
    }
    
    public Uri getUri() {
        return uri;
    }
    
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isImage() {
        return TYPE_IMAGE.equals(type);
    }
    
    public boolean isVideo() {
        return TYPE_VIDEO.equals(type);
    }
}




