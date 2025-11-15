package com.example.accizardlucban;

import java.util.Date;
import java.util.List;

public class Pin {
    private String id;
    private String category; // "accident", "fire", etc.
    private Date createdAt;
    private String createdBy; // User ID
    private String createdByName; // User display name
    private double latitude;
    private String locationName; // Full address
    private double longitude;
    private String reportId; // Can be null
    private List<String> searchTerms; // For search functionality
    
    // Empty constructor required for Firestore
    public Pin() {
    }
    
    // Constructor with essential fields
    public Pin(String id, String category, double latitude, double longitude, String locationName) {
        this.id = id;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public List<String> getSearchTerms() {
        return searchTerms;
    }
    
    public void setSearchTerms(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }
    
    // Helper methods
    public String getDisplayTitle() {
        if (locationName != null && !locationName.isEmpty()) {
            // Extract just the location name without full address
            String[] parts = locationName.split(",");
            return parts[0].trim();
        }
        return category != null ? category : "Unknown Location";
    }
    
    public String getFullAddress() {
        return locationName != null ? locationName : "No address available";
    }
}







































