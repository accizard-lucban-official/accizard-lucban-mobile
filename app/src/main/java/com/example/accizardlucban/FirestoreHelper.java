package com.example.accizardlucban;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Firebase Firestore operations
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static FirebaseFirestore db;
    
    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_ALERTS = "alerts";
    public static final String COLLECTION_CHAT_MESSAGES = "chat_messages";
    public static final String COLLECTION_FACILITIES = "facilities";
    
    // Initialize Firestore
    public static FirebaseFirestore getInstance() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
    
    // User operations
    public static void createUser(String userId, Map<String, Object> userData, 
                                 OnSuccessListener<Void> successListener,
                                 OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    public static void getUser(String userId, 
                              OnCompleteListener<DocumentSnapshot> completeListener) {
        getInstance().collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    public static void updateUser(String userId, Map<String, Object> updates,
                                 OnSuccessListener<Void> successListener,
                                 OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    // Create user with Firestore auto-generated document ID
    public static void createUserWithAutoId(Map<String, Object> userData,
                                            OnSuccessListener<DocumentReference> successListener,
                                            OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_USERS)
            .add(userData)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }
    
    // Report operations
    public static void createReport(Map<String, Object> reportData,
                                   OnSuccessListener<DocumentReference> successListener,
                                   OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_REPORTS)
                .add(reportData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    public static void getReports(OnCompleteListener<QuerySnapshot> completeListener) {
        getInstance().collection(COLLECTION_REPORTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    public static void getUserReports(String userId, 
                                     OnCompleteListener<QuerySnapshot> completeListener) {
        getInstance().collection(COLLECTION_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    public static void updateReport(String reportId, Map<String, Object> updates,
                                   OnSuccessListener<Void> successListener,
                                   OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_REPORTS)
                .document(reportId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    // Create report with auto-incremented reportId (RID-X) using atomic counter
    public static void createReportWithAutoId(final Map<String, Object> reportData,
                                              final OnSuccessListener<DocumentReference> successListener,
                                              final OnFailureListener failureListener) {
        // Use a counter document to ensure atomic increment
        final DocumentReference counterRef = getInstance().collection("counters").document("reportCounter");
        
        getInstance().runTransaction(transaction -> {
            DocumentSnapshot counterSnapshot = transaction.get(counterRef);
            
            // Calculate nextId in a single assignment
            final long nextId = counterSnapshot.exists() ? 
                (counterSnapshot.getLong("count") + 1) : 1;
            
            // Update the counter
            Map<String, Object> counterData = new HashMap<>();
            counterData.put("count", nextId);
            counterData.put("lastUpdated", System.currentTimeMillis());
            transaction.set(counterRef, counterData);
            
            // Set the reportId in the report data
            String newReportId = "RID-" + nextId;
            reportData.put("reportId", newReportId);
            
            return null; // Transaction doesn't need to return anything
        }).addOnSuccessListener(aVoid -> {
            // Transaction successful, now create the report
            createReport(reportData, successListener, failureListener);
        }).addOnFailureListener(e -> {
            // If transaction fails, try fallback method
            android.util.Log.w(TAG, "Transaction failed, trying fallback method", e);
            createReportWithAutoIdFallback(reportData, successListener, failureListener);
        });
    }
    
    // Fallback method for report ID generation (less reliable but works as backup)
    private static void createReportWithAutoIdFallback(final Map<String, Object> reportData,
                                                       final OnSuccessListener<DocumentReference> successListener,
                                                       final OnFailureListener failureListener) {
        // Query for the highest reportId as fallback
        getInstance().collection(COLLECTION_REPORTS)
            .orderBy("reportId", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                // Calculate nextId in a single assignment using helper method
                final int nextId = calculateNextIdFromTask(task);
                
                // Add timestamp to make it more unique in case of race condition
                String timestamp = String.valueOf(System.currentTimeMillis());
                String newReportId = "RID-" + nextId + "-" + timestamp.substring(timestamp.length() - 4);
                reportData.put("reportId", newReportId);
                
                // Now create the report
                createReport(reportData, successListener, failureListener);
            })
            .addOnFailureListener(failureListener);
    }
    
    // Alert operations
    public static void createAlert(Map<String, Object> alertData,
                                  OnSuccessListener<DocumentReference> successListener,
                                  OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_ALERTS)
                .add(alertData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    public static void getAlerts(OnCompleteListener<QuerySnapshot> completeListener) {
        getInstance().collection(COLLECTION_ALERTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    // Chat operations
    public static void sendMessage(Map<String, Object> messageData,
                                  OnSuccessListener<DocumentReference> successListener,
                                  OnFailureListener failureListener) {
        getInstance().collection(COLLECTION_CHAT_MESSAGES)
                .add(messageData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    public static void getMessages(OnCompleteListener<QuerySnapshot> completeListener) {
        getInstance().collection(COLLECTION_CHAT_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    // Facility operations
    public static void getFacilities(OnCompleteListener<QuerySnapshot> completeListener) {
        getInstance().collection(COLLECTION_FACILITIES)
                .get()
                .addOnCompleteListener(completeListener);
    }
    
    /**
     * Create a facility pin in Firestore with the correct type field
     * Type field must match Emergency Support Facilities options exactly:
     * - "Evacuation Centers"
     * - "Health Facilities"
     * - "Police Stations"
     * - "Fire Stations"
     * - "Government Offices"
     * 
     * @param pinData Map containing pin data (type field will be normalized to correct name)
     * @param successListener Success callback
     * @param failureListener Failure callback
     */
    public static void createFacilityPin(Map<String, Object> pinData,
                                        OnSuccessListener<com.google.firebase.firestore.DocumentReference> successListener,
                                        OnFailureListener failureListener) {
        // PRIORITY: Normalize type field to match Emergency Support Facilities options
        if (pinData.containsKey("type")) {
            String type = (String) pinData.get("type");
            String normalizedType = FirestorePinUpdater.getCorrectCategoryName(type);
            if (normalizedType != null) {
                pinData.put("type", normalizedType);
                // Also set category field for consistency
                pinData.put("category", normalizedType);
            }
        } else if (pinData.containsKey("category")) {
            // If type is not set, use category and set both fields
            String category = (String) pinData.get("category");
            String normalizedType = FirestorePinUpdater.getCorrectCategoryName(category);
            if (normalizedType != null) {
                pinData.put("type", normalizedType);
                pinData.put("category", normalizedType);
            }
        }
        
        getInstance().collection("pins")
                .add(pinData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    /**
     * Helper method to create facility pin data map with correct type field
     * Use this when creating facility pins to ensure type matches Emergency Support Facilities
     * 
     * @param facilityType Must be one of: "Evacuation Centers", "Health Facilities", 
     *                    "Police Stations", "Fire Stations", "Government Offices"
     *                    This is what the user clicks in Emergency Support Facilities
     * @param locationName Full address of the facility
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param description Optional description
     * @return Map with pin data ready to save to Firestore (type field will match user selection)
     */
    public static Map<String, Object> createFacilityPinData(String facilityType,
                                                            String locationName,
                                                            double latitude,
                                                            double longitude,
                                                            String description) {
        Map<String, Object> pinData = new HashMap<>();
        
        // Normalize type to match Emergency Support Facilities options
        // This ensures the type field matches exactly what the user clicked
        String normalizedType = FirestorePinUpdater.getCorrectCategoryName(facilityType);
        String finalType = normalizedType != null ? normalizedType : facilityType;
        
        // PRIORITY: Set type field (this is what the app reads for facilities)
        pinData.put("type", finalType);
        // Also set category field for consistency
        pinData.put("category", finalType);
        
        pinData.put("locationName", locationName);
        pinData.put("latitude", latitude);
        pinData.put("longitude", longitude);
        pinData.put("createdAt", com.google.firebase.Timestamp.now());
        
        if (description != null && !description.trim().isEmpty()) {
            pinData.put("description", description);
        }
        
        return pinData;
    }
    
    // Batch operations
    public static WriteBatch getBatch() {
        return getInstance().batch();
    }
    
    // Helper method to create user data map
    public static Map<String, Object> createUserData(String email, String fullName, 
                                                    String phoneNumber, String address) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("phoneNumber", phoneNumber);
        userData.put("address", address);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("isVerified", false);
        return userData;
    }
    
    // Helper method to create report data map (without category and priority)
    public static Map<String, Object> createReportData(String userId, String title, 
                                                      String description, String location) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("title", title);
        reportData.put("description", description);
        reportData.put("location", location);
        reportData.put("status", "pending");
        reportData.put("timestamp", System.currentTimeMillis());
        return reportData;
    }
    
    // Helper method to create alert data map
    public static Map<String, Object> createAlertData(String title, String message, 
                                                     String location, String severity) {
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("title", title);
        alertData.put("message", message);
        alertData.put("location", location);
        alertData.put("severity", severity);
        alertData.put("timestamp", System.currentTimeMillis());
        alertData.put("isActive", true);
        return alertData;
    }
    
    // Helper method to create message data map
    public static Map<String, Object> createMessageData(String userId, String message, 
                                                       boolean isAdmin) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("userId", userId);
        messageData.put("message", message);
        messageData.put("isAdmin", isAdmin);
        messageData.put("timestamp", System.currentTimeMillis());
        return messageData;
    }
    
    // Initialize the report counter (call this once during app setup)
    public static void initializeReportCounter(OnSuccessListener<Void> successListener,
                                              OnFailureListener failureListener) {
        DocumentReference counterRef = getInstance().collection("counters").document("reportCounter");
        
        counterRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (!snapshot.exists()) {
                    // Counter doesn't exist, create it
                    Map<String, Object> counterData = new HashMap<>();
                    counterData.put("count", 0L);
                    counterData.put("lastUpdated", System.currentTimeMillis());
                    counterData.put("createdAt", System.currentTimeMillis());
                    
                    counterRef.set(counterData)
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener);
                } else {
                    // Counter already exists
                    if (successListener != null) {
                        successListener.onSuccess(null);
                    }
                }
            } else {
                if (failureListener != null) {
                    failureListener.onFailure(task.getException());
                }
            }
        });
    }
    
    // Get current report counter value
    public static void getReportCounterValue(OnCompleteListener<DocumentSnapshot> completeListener) {
        DocumentReference counterRef = getInstance().collection("counters").document("reportCounter");
        counterRef.get().addOnCompleteListener(completeListener);
    }
    
    // Helper method to calculate next ID from task result
    private static int calculateNextIdFromTask(com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
            String lastReportId = null;
            try {
                Object ridObj = task.getResult().getDocuments().get(0).get("reportId");
                if (ridObj != null) {
                    lastReportId = ridObj.toString();
                }
            } catch (Exception ignored) {}
            
            if (lastReportId != null && lastReportId.startsWith("RID-")) {
                try {
                    int lastNum = Integer.parseInt(lastReportId.replace("RID-", ""));
                    return lastNum + 1;
                } catch (NumberFormatException ignored) {
                    return 1;
                }
            }
        }
        return 1;
    }
} 