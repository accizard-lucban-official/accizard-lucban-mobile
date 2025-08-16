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
    
    // Create report with auto-incremented reportId (RID-X)
    public static void createReportWithAutoId(final Map<String, Object> reportData,
                                              final OnSuccessListener<DocumentReference> successListener,
                                              final OnFailureListener failureListener) {
        // Query for the highest reportId
        getInstance().collection(COLLECTION_REPORTS)
            .orderBy("reportId", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                int nextId = 1;
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
                            nextId = lastNum + 1;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                String newReportId = "RID-" + nextId;
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
    
    // Helper method to create report data map
    public static Map<String, Object> createReportData(String userId, String title, 
                                                      String description, String location,
                                                      String priority, String category) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("title", title);
        reportData.put("description", description);
        reportData.put("location", location);
        reportData.put("priority", priority);
        reportData.put("category", category);
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
} 