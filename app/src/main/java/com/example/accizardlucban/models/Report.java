package com.example.accizardlucban.models;

import java.util.HashMap;
import java.util.Map;

public class Report {
    private String reportId;
    private String userId;
    private String title;
    private String description;
    private String location;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String coordinates;
    private String reporterName;
    private String reporterMobile;
    private String priority; // low, medium, high, emergency
    private String category; // fire, flood, earthquake, landslide, volcanic, health, police, other
    private String status; // pending, ongoing, responded, resolved, not responded, redundant
    private String imageUrl; // Legacy single image URL
    private java.util.List<String> imageUrls; // Multiple image URLs
    private java.util.List<String> videoUrls; // Multiple video URLs
    private int imageCount;
    private long timestamp;
    private String adminResponse;
    private long responseTimestamp;

    // Default constructor required for Firestore
    public Report() {}

    public Report(String userId, String title, String description, String location, 
                  String priority, String category) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.priority = priority;
        this.category = category;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public long getResponseTimestamp() { return responseTimestamp; }
    public void setResponseTimestamp(long responseTimestamp) { this.responseTimestamp = responseTimestamp; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterMobile() { return reporterMobile; }
    public void setReporterMobile(String reporterMobile) { this.reporterMobile = reporterMobile; }

    public java.util.List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(java.util.List<String> imageUrls) { this.imageUrls = imageUrls; }

    public int getImageCount() { return imageCount; }
    public void setImageCount(int imageCount) { this.imageCount = imageCount; }

    public java.util.List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(java.util.List<String> videoUrls) { this.videoUrls = videoUrls; }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("title", title);
        map.put("description", description);
        map.put("location", location);
        map.put("locationName", locationName != null ? locationName : "");
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("coordinates", coordinates != null ? coordinates : "");
        map.put("reporterName", reporterName != null ? reporterName : "");
        map.put("reporterMobile", reporterMobile != null ? reporterMobile : "");
        map.put("priority", priority);
        map.put("category", category);
        map.put("status", status);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("imageUrls", imageUrls != null ? imageUrls : new java.util.ArrayList<>());
        map.put("videoUrls", videoUrls != null ? videoUrls : new java.util.ArrayList<>());
        map.put("imageCount", imageCount);
        map.put("timestamp", timestamp);
        map.put("adminResponse", adminResponse != null ? adminResponse : "");
        map.put("responseTimestamp", responseTimestamp);
        return map;
    }

    // Priority constants
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";
    public static final String PRIORITY_EMERGENCY = "emergency";

    // Category constants
    public static final String CATEGORY_FIRE = "fire";
    public static final String CATEGORY_FLOOD = "flood";
    public static final String CATEGORY_EARTHQUAKE = "earthquake";
    public static final String CATEGORY_LANDSLIDE = "landslide";
    public static final String CATEGORY_VOLCANIC = "volcanic";
    public static final String CATEGORY_HEALTH = "health";
    public static final String CATEGORY_POLICE = "police";
    public static final String CATEGORY_OTHER = "other";

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ONGOING = "Ongoing";
    public static final String STATUS_RESPONDED = "Responded";
    public static final String STATUS_NOT_RESPONDED = "Not Responded";
    public static final String STATUS_REDUNDANT = "Redundant";
    public static final String STATUS_RESOLVED = "Resolved";
} 