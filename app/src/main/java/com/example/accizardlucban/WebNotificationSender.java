package com.example.accizardlucban;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to send push notifications from mobile app to web admin dashboard
 * 
 * This class triggers notifications by writing to Firestore collections that
 * Cloud Functions monitor. The Cloud Functions then send FCM notifications to web admins.
 * 
 * Supported notification types:
 * - new_report: When a user submits a new report
 * - chat_message: When a user sends a chat message to admin
 */
public class WebNotificationSender {
    
    private static final String TAG = "WebNotificationSender";
    private static final String COLLECTION_WEB_NOTIFICATIONS = "web_notifications";
    private static final String COLLECTION_FCM_TOKENS = "fcmTokens";
    
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    
    public WebNotificationSender(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Send notification to web admins when a new report is submitted
     * 
     * @param reportId The ID of the submitted report
     * @param reportType The type of report (e.g., "Fire", "Medical Emergency")
     * @param reporterName Name of the user who submitted the report
     * @param location Location of the incident
     * @param description Description of the report
     */
    public void notifyNewReport(String reportId, String reportType, String reporterName, 
                                String location, String description) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot send web notification");
            return;
        }
        
        Log.d(TAG, "Sending new report notification to web admins");
        
        // Create notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "new_report");
        notificationData.put("reportId", reportId);
        notificationData.put("reportType", reportType != null ? reportType : "Unknown");
        notificationData.put("reporterName", reporterName != null ? reporterName : "Unknown User");
        notificationData.put("location", location != null ? location : "Unknown Location");
        notificationData.put("description", description != null ? description : "");
        notificationData.put("userId", currentUser.getUid());
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("status", "pending"); // Cloud Function will process this
        
        // Write to Firestore - Cloud Function will pick this up and send FCM notification
        firestore.collection(COLLECTION_WEB_NOTIFICATIONS)
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Web notification trigger created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to create web notification trigger: " + e.getMessage(), e);
                });
    }
    
    /**
     * Send notification to web admins when a user sends a chat message
     * 
     * @param messageId The ID of the chat message
     * @param messageContent The content of the message
     * @param senderName Name of the user who sent the message
     * @param userId The ID of the user who sent the message
     */
    public void notifyChatMessage(String messageId, String messageContent, String senderName, String userId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot send web notification");
            return;
        }
        
        Log.d(TAG, "Sending chat message notification to web admins");
        
        // Create notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "chat_message");
        notificationData.put("messageId", messageId);
        notificationData.put("messageContent", messageContent != null ? messageContent : "");
        notificationData.put("senderName", senderName != null ? senderName : "Unknown User");
        notificationData.put("userId", userId);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("status", "pending"); // Cloud Function will process this
        
        // Write to Firestore - Cloud Function will pick this up and send FCM notification
        firestore.collection(COLLECTION_WEB_NOTIFICATIONS)
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Web notification trigger created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to create web notification trigger: " + e.getMessage(), e);
                });
    }
    
    /**
     * Get count of active web admin FCM tokens
     * This is useful for debugging/verification
     */
    public void getWebAdminTokenCount(OnCompleteListener<QuerySnapshot> listener) {
        firestore.collection(COLLECTION_FCM_TOKENS)
                .whereEqualTo("platform", "web")
                .get()
                .addOnCompleteListener(listener);
    }
}

