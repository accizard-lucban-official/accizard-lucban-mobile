package com.example.accizardlucban;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to update facility pin categories in Firestore
 * This ensures all facility pins have the correct category names matching
 * the Emergency Support Facilities filter options:
 * - Evacuation Centers
 * - Health Facilities
 * - Police Stations
 * - Fire Stations
 * - Government Offices
 */
public class FirestorePinUpdater {
    private static final String TAG = "FirestorePinUpdater";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    // Correct category names matching Emergency Support Facilities
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>();
    
    static {
        // Map old/variations to correct category names
        CATEGORY_MAPPING.put("evacuation center", "Evacuation Centers");
        CATEGORY_MAPPING.put("evacuation centers", "Evacuation Centers");
        CATEGORY_MAPPING.put("evacuation", "Evacuation Centers");
        
        CATEGORY_MAPPING.put("health facility", "Health Facilities");
        CATEGORY_MAPPING.put("health facilities", "Health Facilities");
        CATEGORY_MAPPING.put("health", "Health Facilities");
        CATEGORY_MAPPING.put("hospital", "Health Facilities");
        CATEGORY_MAPPING.put("clinic", "Health Facilities");
        
        CATEGORY_MAPPING.put("police station", "Police Stations");
        CATEGORY_MAPPING.put("police stations", "Police Stations");
        CATEGORY_MAPPING.put("police", "Police Stations");
        
        CATEGORY_MAPPING.put("fire station", "Fire Stations");
        CATEGORY_MAPPING.put("fire stations", "Fire Stations");
        CATEGORY_MAPPING.put("fire", "Fire Stations");
        
        CATEGORY_MAPPING.put("government office", "Government Offices");
        CATEGORY_MAPPING.put("government offices", "Government Offices");
        CATEGORY_MAPPING.put("government", "Government Offices");
    }
    
    /**
     * Update all facility pins in Firestore to use correct type field
     * This updates the "type" field (not category) to match Emergency Support Facilities options
     * This should be called once to fix existing data
     */
    public static void updateAllFacilityPinCategories(
            OnSuccessListener<Integer> successListener,
            OnFailureListener failureListener) {
        
        Log.d(TAG, "Starting facility pin type field update...");
        
        db.collection("pins")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error getting pins", task.getException());
                            if (failureListener != null) {
                                failureListener.onFailure(task.getException());
                            }
                            return;
                        }
                        
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot == null || snapshot.isEmpty()) {
                            Log.d(TAG, "No pins found to update");
                            if (successListener != null) {
                                successListener.onSuccess(0);
                            }
                            return;
                        }
                        
                        WriteBatch batch = db.batch();
                        final int[] updateCount = {0}; // Use array to make it effectively final
                        
                        for (QueryDocumentSnapshot document : snapshot) {
                            // PRIORITY: Check type field first (for facilities)
                            String currentType = document.getString("type");
                            String currentCategory = document.getString("category");
                            
                            // Determine which field to use and update
                            String fieldToCheck = null;
                            String currentValue = null;
                            
                            // For facilities, prioritize type field
                            if (currentType != null && !currentType.trim().isEmpty()) {
                                fieldToCheck = "type";
                                currentValue = currentType.trim();
                            } else if (currentCategory != null && !currentCategory.trim().isEmpty()) {
                                fieldToCheck = "category";
                                currentValue = currentCategory.trim();
                            }
                            
                            if (fieldToCheck == null || currentValue == null) {
                                continue;
                            }
                            
                            // Check if this is a facility
                            boolean isFacility = isFacilityCategory(currentValue);
                            if (!isFacility) {
                                continue; // Skip non-facility pins
                            }
                            
                            String normalizedValue = currentValue.toLowerCase();
                            String correctType = CATEGORY_MAPPING.get(normalizedValue);
                            
                            // Only update if type needs to be corrected
                            if (correctType != null && !currentValue.equals(correctType)) {
                                Log.d(TAG, "Updating pin " + document.getId() + 
                                        ": '" + currentValue + "' -> '" + correctType + "' (field: " + fieldToCheck + ")");
                                
                                // Update the type field with correct category name
                                batch.update(document.getReference(), "type", correctType);
                                // Also update category field for consistency
                                batch.update(document.getReference(), "category", correctType);
                                updateCount[0]++;
                            }
                        }
                        
                        final int finalUpdateCount = updateCount[0]; // Create final variable for inner class
                        
                        if (finalUpdateCount > 0) {
                            batch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Successfully updated " + finalUpdateCount + " facility pins (type field)");
                                            if (successListener != null) {
                                                successListener.onSuccess(finalUpdateCount);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e(TAG, "Error updating pins", e);
                                            if (failureListener != null) {
                                                failureListener.onFailure(e);
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, "No pins needed updating - all types are correct");
                            if (successListener != null) {
                                successListener.onSuccess(0);
                            }
                        }
                    }
                });
    }
    
    /**
     * Get the correct category name for a facility type
     * Use this when creating new facility pins to ensure category matches Emergency Support Facilities
     * 
     * Valid Emergency Support Facilities categories (exact match):
     * - "Evacuation Centers"
     * - "Health Facilities"
     * - "Police Stations"
     * - "Fire Stations"
     * - "Government Offices"
     * 
     * @param facilityType The facility type selected by user (will be normalized to correct name)
     * @return Correct category name matching Emergency Support Facilities options
     */
    public static String getCorrectCategoryName(String facilityType) {
        if (facilityType == null || facilityType.trim().isEmpty()) {
            return null;
        }
        
        // First check if it's already a correct category name (exact match)
        String trimmed = facilityType.trim();
        if (CATEGORY_MAPPING.containsValue(trimmed)) {
            return trimmed; // Already correct, return as-is
        }
        
        // Normalize and check mapping
        String normalized = trimmed.toLowerCase();
        String correctCategory = CATEGORY_MAPPING.get(normalized);
        
        // If exact match found in mapping, return it
        if (correctCategory != null) {
            return correctCategory;
        }
        
        // Check if it contains any facility keywords (partial match)
        for (Map.Entry<String, String> entry : CATEGORY_MAPPING.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // If no mapping found, return original (might be a new category)
        return trimmed;
    }
    
    /**
     * Check if a category is a facility category
     */
    public static boolean isFacilityCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        
        String normalized = category.trim().toLowerCase();
        return CATEGORY_MAPPING.containsKey(normalized) || 
               CATEGORY_MAPPING.containsValue(category);
    }
}

