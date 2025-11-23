package com.example.accizardlucban;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import com.mapbox.geojson.Point;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for health facility detection and information retrieval
 * Extracted from MapViewActivity to reduce file size
 */
public class HealthFacilityHelper {
    private static final String TAG = "HealthFacilityHelper";
    
    /**
     * Get health facility info from location using reverse geocoding
     */
    public static void getHealthFacilityInfo(Geocoder geocoder, Point point, 
                                           HealthFacilityCallback callback) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                    point.latitude(), point.longitude(), 10
                );
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address bestAddress = findBestHealthFacilityAddress(addresses);
                    
                    if (bestAddress != null) {
                        String facilityName = extractFacilityName(bestAddress);
                        String facilityType = determineHealthFacilityType(bestAddress, facilityName);
                        
                        if (facilityName.equals("Health Facility") || facilityName.matches("^[A-Z0-9+]+$")) {
                            // Plus Code detected, try forward geocoding
                            searchNearbyHealthFacilities(geocoder, point, facilityName, facilityType, callback);
                            return;
                        }
                        
                        callback.onResult(facilityName, facilityType);
                        return;
                    }
                }
                
                callback.onResult("Health Facility", "Health Facility");
            } catch (IOException e) {
                Log.e(TAG, "Error in reverse geocoding: " + e.getMessage());
                callback.onResult("Health Facility", "Health Facility");
            }
        }).start();
    }
    
    private static Address findBestHealthFacilityAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        
        for (Address address : addresses) {
            String featureName = address.getFeatureName() != null ? address.getFeatureName().toLowerCase() : "";
            String thoroughfare = address.getThoroughfare() != null ? address.getThoroughfare().toLowerCase() : "";
            String combined = featureName + " " + thoroughfare;
            
            if (combined.contains("pharmacy") || combined.contains("drugstore") ||
                combined.contains("hospital") || combined.contains("clinic") ||
                combined.contains("doctor") || combined.contains("medical") ||
                combined.contains("health") || combined.contains("dental") ||
                combined.contains("laboratory") || combined.contains("lab")) {
                return address;
            }
        }
        
        return addresses.get(0);
    }
    
    private static String extractFacilityName(Address address) {
        StringBuilder nameBuilder = new StringBuilder();
        
        if (address.getFeatureName() != null && !address.getFeatureName().trim().isEmpty()) {
            String featureName = address.getFeatureName().trim();
            if (!featureName.matches("^[A-Z0-9+]+$") || featureName.length() < 8) {
                nameBuilder.append(featureName);
            }
        }
        
        if (nameBuilder.length() == 0 && address.getThoroughfare() != null && 
            !address.getThoroughfare().trim().isEmpty()) {
            String thoroughfare = address.getThoroughfare().trim();
            if (!thoroughfare.matches("^[A-Z0-9+]+$") || thoroughfare.length() < 8) {
                nameBuilder.append(thoroughfare);
            }
        }
        
        if (nameBuilder.length() == 0 && address.getSubThoroughfare() != null && 
            !address.getSubThoroughfare().trim().isEmpty()) {
            nameBuilder.append(address.getSubThoroughfare().trim());
        }
        
        if (nameBuilder.length() == 0 && address.getLocality() != null && 
            !address.getLocality().trim().isEmpty()) {
            nameBuilder.append(address.getLocality().trim());
        }
        
        return nameBuilder.length() > 0 ? nameBuilder.toString() : "Health Facility";
    }
    
    private static String determineHealthFacilityType(Address address, String facilityName) {
        if (address == null) return "Health Facility";
        
        StringBuilder addressText = new StringBuilder();
        
        if (facilityName != null && !facilityName.trim().isEmpty() && !facilityName.matches("^[A-Z0-9+]+$")) {
            addressText.append(facilityName.toLowerCase()).append(" ");
        }
        if (address.getFeatureName() != null) {
            String featureName = address.getFeatureName().toLowerCase();
            if (!featureName.matches("^[a-z0-9+]+$") || featureName.length() < 8) {
                addressText.append(featureName).append(" ");
            }
        }
        if (address.getThoroughfare() != null) {
            addressText.append(address.getThoroughfare().toLowerCase()).append(" ");
        }
        if (address.getSubThoroughfare() != null) {
            addressText.append(address.getSubThoroughfare().toLowerCase()).append(" ");
        }
        if (address.getLocality() != null) {
            addressText.append(address.getLocality().toLowerCase()).append(" ");
        }
        if (address.getSubLocality() != null) {
            addressText.append(address.getSubLocality().toLowerCase()).append(" ");
        }
        if (address.getAdminArea() != null) {
            addressText.append(address.getAdminArea().toLowerCase()).append(" ");
        }
        
        String combined = addressText.toString();
        Log.d(TAG, "🔍 Analyzing facility type from: " + combined);
        
        // Check in order of specificity - most specific first
        // 1. Pharmacy/Drugstore (very specific)
        if (combined.contains("pharmacy") || combined.contains("drugstore") || 
            combined.contains("apotek") || combined.contains("botika") ||
            combined.contains("mercury") || combined.contains("watsons") ||
            combined.contains("southstar") || combined.contains("generics")) {
            return "Pharmacy";
        }
        
        // 2. Doctor/Physician (specific)
        if (combined.contains("doctor") || combined.contains("physician") ||
            combined.contains("dr.") || combined.contains("dra.") ||
            combined.contains("doktor") || combined.contains("mga doktor") ||
            combined.contains("md") || combined.contains("general practitioner")) {
            return "Doctor's Office";
        }
        
        // 3. Dental (specific)
        if (combined.contains("dental") || combined.contains("dentist") ||
            combined.contains("dentista") || combined.contains("orthodontist")) {
            return "Dental Clinic";
        }
        
        // 4. Hospital (specific)
        if (combined.contains("hospital") || combined.contains("medical center") ||
            combined.contains("health center") || combined.contains("medical centre")) {
            return "Hospital";
        }
        
        // 5. Clinic (general, but check it's not a specific type)
        if (combined.contains("clinic") || combined.contains("health clinic") ||
            combined.contains("medical clinic")) {
            // Make sure it's not a dental or other specific clinic
            if (!combined.contains("dental") && !combined.contains("optical") &&
                !combined.contains("veterinary") && !combined.contains("maternity")) {
                return "Clinic";
            }
        }
        
        // 6. Laboratory (specific, but check it's not part of a hospital)
        if ((combined.contains("laboratory") || combined.contains("lab")) &&
            !combined.contains("hospital") && !combined.contains("medical center")) {
            // Only if it's a standalone lab, not part of a hospital
            if (combined.contains("diagnostic") || combined.contains("testing") ||
                combined.contains("pathology")) {
                return "Laboratory";
            }
        }
        
        // 7. Optical (specific)
        if (combined.contains("optical") || combined.contains("eye") ||
            combined.contains("ophthalmology") || combined.contains("optometrist") ||
            combined.contains("eyewear") || combined.contains("vision")) {
            return "Optical Clinic";
        }
        
        // 8. Veterinary (specific)
        if (combined.contains("veterinary") || combined.contains("vet") ||
            combined.contains("veterinarian") || combined.contains("animal hospital") ||
            combined.contains("pet clinic")) {
            return "Veterinary Clinic";
        }
        
        // 9. Maternity (specific)
        if (combined.contains("maternity") || combined.contains("obstetric") ||
            combined.contains("prenatal") || combined.contains("ob-gyn")) {
            return "Maternity Clinic";
        }
        
        // 10. Mental Health (specific)
        if (combined.contains("mental health") || combined.contains("psychiatry") ||
            combined.contains("psychologist") || combined.contains("psychiatric") ||
            combined.contains("therapy") || combined.contains("counseling")) {
            return "Mental Health Clinic";
        }
        
        // 11. Urgent Care (specific)
        if (combined.contains("urgent care") || combined.contains("emergency room") ||
            (combined.contains("emergency") && !combined.contains("medical emergency"))) {
            return "Urgent Care";
        }
        
        // Default to generic health facility if no specific type found
        return "Health Facility";
    }
    
    private static void searchNearbyHealthFacilities(Geocoder geocoder, Point point,
                                                    String defaultName, String defaultType,
                                                    HealthFacilityCallback callback) {
        new Thread(() -> {
            try {
                String[] terms = {"pharmacy", "hospital", "clinic", "drugstore"};
                String name = defaultName;
                String type = defaultType;
                double minDist = Double.MAX_VALUE;
                
                for (String term : terms) {
                    try {
                        List<Address> results = geocoder.getFromLocationName(
                            term + ", Lucban, Quezon, Philippines", 3);
                        if (results != null) {
                            for (Address addr : results) {
                                double dist = calculateDistance(
                                    point.latitude(), point.longitude(),
                                    addr.getLatitude(), addr.getLongitude());
                                if (dist < 500 && dist < minDist) {
                                    minDist = dist;
                                    if (addr.getFeatureName() != null && 
                                        !addr.getFeatureName().trim().isEmpty()) {
                                        name = addr.getFeatureName().trim();
                                    } else if (addr.getThoroughfare() != null && 
                                               !addr.getThoroughfare().trim().isEmpty()) {
                                        name = addr.getThoroughfare().trim();
                                    }
                                    type = determineHealthFacilityType(addr, name);
                                }
                            }
                        }
                    } catch (IOException e) {
                        // Continue
                    }
                }
                
                callback.onResult(name, type);
            } catch (Exception e) {
                Log.e(TAG, "Error in forward geocoding: " + e.getMessage());
                callback.onResult(defaultName, defaultType);
            }
        }).start();
    }
    
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    
    /**
     * Callback interface for health facility info retrieval
     */
    public interface HealthFacilityCallback {
        void onResult(String facilityName, String facilityType);
    }
}

