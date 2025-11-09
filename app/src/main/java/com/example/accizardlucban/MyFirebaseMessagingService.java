package com.example.accizardlucban;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications from Firebase
 * 
 * This service receives notifications in three states:
 * 1. Foreground - App is open and visible
 * 2. Background - App is running but not visible
 * 3. Terminated - App is completely closed
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "MyFCMService";
    
    /**
     * Called when a new FCM token is generated or refreshed
     * This happens on first app install, after app data is cleared, or periodically
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        
        Log.d(TAG, "🔄 FCM token refreshed: " + token);
        
        // Save the new token to Firestore
        FCMTokenManager tokenManager = new FCMTokenManager(this);
        tokenManager.saveFCMTokenToFirestore(token);
    }
    
    /**
     * Called when a message is received
     * This method is called for:
     * - Data-only messages (always)
     * - Notification messages when app is in FOREGROUND
     * 
     * Note: When app is in background/terminated, notification messages are handled
     * automatically by the system and this method only receives the data payload
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "📩 Message received from: " + remoteMessage.getFrom());
        
        // Check if message contains a notification payload
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        
        // ✅ CRITICAL FIX: Check if this is a chat message and suppress if chat is visible
        String notificationType = data.get("type");
        if ("chat_message".equals(notificationType) && ChatActivityTracker.isChatActivityVisible()) {
            Log.d(TAG, "🚫 CRITICAL: Chat notification COMPLETELY SUPPRESSED - User is in chat");
            Log.d(TAG, "🚫 Notification will NOT be shown (not even by system)");
            return; // Exit immediately - don't process anything
        }
        
        if (notification != null) {
            String title = notification.getTitle();
            String body = notification.getBody();
            
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
            
            // Show the notification
            showNotification(title, body, data);
        }
        
        // Check if message contains a data payload
        if (!data.isEmpty()) {
            Log.d(TAG, "Message data payload: " + data.toString());
            handleDataPayload(data);
        }
        
        // If message contains both notification and data, both are processed
    }
    
    /**
     * Display the notification using our custom notification manager
     */
    private void showNotification(String title, String body, Map<String, String> data) {
        try {
            String notificationType = data.get("type");
            Log.d(TAG, "📱 Notification received - Type: " + notificationType);
            
            // ✅ FIXED: Check if this is a chat message and user is already viewing chat
            if ("chat_message".equals(notificationType)) {
                boolean isChatVisible = ChatActivityTracker.isChatActivityVisible();
                Log.d(TAG, "💬 Chat message notification - Chat visible: " + isChatVisible);
                
                if (isChatVisible) {
                    Log.d(TAG, "🚫 SUPPRESSED: User is viewing chat - notification NOT shown");
                    return; // Don't show notification if user is already in chat
                } else {
                    Log.d(TAG, "✅ SHOWING: User is NOT viewing chat - notification will be shown");
                }
            }
            
            AcciZardNotificationManager notificationManager = new AcciZardNotificationManager(this);
            notificationManager.showNotification(title, body, data);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle data-only messages
     * This is where you can process background data without showing a notification
     */
    private void handleDataPayload(Map<String, String> data) {
        String notificationType = data.get("type");
        
        Log.d(TAG, "Processing data payload - Type: " + notificationType);
        
        // You can add custom handling here based on notification type
        // For example:
        // - Update local database
        // - Trigger app-specific actions
        // - Update UI if app is in foreground
        
        switch (notificationType) {
            case "report_update":
                handleReportUpdate(data);
                break;
                
            case "announcement":
                handleAnnouncement(data);
                break;
                
            case "chat_message":
                handleChatMessage(data);
                break;
                
            default:
                Log.d(TAG, "Unknown notification type: " + notificationType);
                break;
        }
    }
    
    /**
     * Handle report update data
     */
    private void handleReportUpdate(Map<String, String> data) {
        String reportId = data.get("reportId");
        String newStatus = data.get("newStatus");
        String oldStatus = data.get("oldStatus");
        
        Log.d(TAG, "Report update: ID=" + reportId + ", Status: " + oldStatus + " → " + newStatus);
        
        // You can add custom logic here, such as:
        // - Update local cache
        // - Send broadcast to update UI if ReportSubmissionActivity is open
        // - Update notification badge
    }
    
    /**
     * Handle announcement data
     */
    private void handleAnnouncement(Map<String, String> data) {
        String announcementId = data.get("announcementId");
        String announcementType = data.get("announcementType");
        String priority = data.get("priority");
        
        Log.d(TAG, "Announcement: ID=" + announcementId + ", Type=" + announcementType + ", Priority=" + priority);
        
        // You can add custom logic here, such as:
        // - Update announcement badge in the UI
        // - Trigger emergency alert sound if high priority
        // - Update local announcement cache
        
        // Update announcement notification badge
        // Note: You already have AnnouncementNotificationManager for this
        // You might want to integrate with it here
    }
    
    /**
     * Handle chat message data
     */
    private void handleChatMessage(Map<String, String> data) {
        String messageId = data.get("messageId");
        String senderId = data.get("senderId");
        String senderName = data.get("senderName");
        
        Log.d(TAG, "Chat message: ID=" + messageId + ", From=" + senderName + " (" + senderId + ")");
        
        // You can add custom logic here, such as:
        // - Update chat badge count
        // - Update local message cache
        // - Send broadcast to ChatActivity if it's open to auto-refresh
    }
    
    /**
     * Called when a message is deleted on the server before delivery
     * This is rare and usually indicates the server had too many messages queued
     */
    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.w(TAG, "⚠️ Some messages were deleted on the server before delivery");
    }
    
    /**
     * Called when an upstream message fails to send
     * (Only relevant if you're sending messages TO FCM, which is rare)
     */
    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, "Message sent successfully: " + msgId);
    }
    
    /**
     * Called when sending a message fails
     */
    @Override
    public void onSendError(String msgId, Exception exception) {
        super.onSendError(msgId, exception);
        Log.e(TAG, "Failed to send message: " + msgId, exception);
    }
}

