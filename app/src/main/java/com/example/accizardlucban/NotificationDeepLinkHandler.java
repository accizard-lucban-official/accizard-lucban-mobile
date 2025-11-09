package com.example.accizardlucban;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;

/**
 * Handles deep linking and navigation when user taps on a push notification
 * Routes to the appropriate screen based on notification type and data
 */
public class NotificationDeepLinkHandler {
    
    private static final String TAG = "NotificationDeepLink";
    
    /**
     * Handle notification tap and navigate to appropriate screen
     * 
     * @param context Application context
     * @param notificationData Data from the notification
     */
    public static void handleNotificationTap(Context context, Map<String, String> notificationData) {
        if (notificationData == null || notificationData.isEmpty()) {
            Log.w(TAG, "Notification data is null or empty");
            openDashboard(context);
            return;
        }
        
        String notificationType = notificationData.get("type");
        Log.d(TAG, "📱 Notification tapped - Type: " + notificationType);
        Log.d(TAG, "Data: " + notificationData.toString());
        
        if (notificationType == null) {
            Log.w(TAG, "Notification type is null, opening dashboard");
            openDashboard(context);
            return;
        }
        
        switch (notificationType) {
            case "report_update":
                handleReportUpdateTap(context, notificationData);
                break;
                
            case "announcement":
                handleAnnouncementTap(context, notificationData);
                break;
                
            case "chat_message":
                handleChatMessageTap(context, notificationData);
                break;
                
            default:
                Log.w(TAG, "Unknown notification type: " + notificationType);
                openDashboard(context);
                break;
        }
    }
    
    /**
     * Handle tap on report status update notification
     * Navigate to ReportSubmissionActivity (My Reports screen)
     */
    private static void handleReportUpdateTap(Context context, Map<String, String> data) {
        try {
            String reportId = data.get("reportId");
            String reportNumber = data.get("reportNumber");
            String newStatus = data.get("newStatus");
            
            Log.d(TAG, "Opening My Reports screen for report: " + reportNumber + " (ID: " + reportId + ")");
            Log.d(TAG, "New status: " + newStatus);
            
            // Navigate to ReportSubmissionActivity
            // Note: Based on your app structure, ReportSubmissionActivity shows user's reports
            Intent intent = new Intent(context, ReportSubmissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Pass data to highlight the updated report (optional)
            if (reportId != null) {
                intent.putExtra("highlightReportId", reportId);
                intent.putExtra("highlightReport", true);
            }
            
            context.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling report update tap: " + e.getMessage(), e);
            openDashboard(context);
        }
    }
    
    /**
     * Handle tap on announcement notification
     * Navigate to AlertsActivity (Announcements screen)
     */
    private static void handleAnnouncementTap(Context context, Map<String, String> data) {
        try {
            String announcementId = data.get("announcementId");
            String announcementType = data.get("announcementType");
            String priority = data.get("priority");
            
            Log.d(TAG, "Opening Announcements screen for announcement: " + announcementId);
            Log.d(TAG, "Type: " + announcementType + ", Priority: " + priority);
            
            // Navigate to AlertsActivity (Announcements/Alerts screen)
            Intent intent = new Intent(context, AlertsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Pass data to highlight the announcement (optional)
            if (announcementId != null) {
                intent.putExtra("highlightAnnouncementId", announcementId);
                intent.putExtra("highlightAnnouncement", true);
            }
            
            context.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling announcement tap: " + e.getMessage(), e);
            openDashboard(context);
        }
    }
    
    /**
     * Handle tap on chat message notification
     * Navigate to ChatActivity
     */
    private static void handleChatMessageTap(Context context, Map<String, String> data) {
        try {
            String messageId = data.get("messageId");
            String senderId = data.get("senderId");
            String senderName = data.get("senderName");
            
            Log.d(TAG, "Opening Chat screen for message: " + messageId);
            Log.d(TAG, "From: " + senderName + " (ID: " + senderId + ")");
            
            // Navigate to ChatActivity
            Intent intent = new Intent(context, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Pass data to scroll to the message (optional)
            if (messageId != null) {
                intent.putExtra("highlightMessageId", messageId);
                intent.putExtra("scrollToMessage", true);
            }
            
            context.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling chat message tap: " + e.getMessage(), e);
            openDashboard(context);
        }
    }
    
    /**
     * Open the main dashboard as fallback
     */
    private static void openDashboard(Context context) {
        try {
            Log.d(TAG, "Opening Dashboard screen");
            Intent intent = new Intent(context, MainDashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening dashboard: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a pending intent for a notification
     * This is used when building the notification
     */
    public static Intent createNotificationIntent(Context context, Map<String, String> notificationData) {
        if (notificationData == null || notificationData.isEmpty()) {
            return new Intent(context, MainDashboard.class);
        }
        
        String notificationType = notificationData.get("type");
        
        if (notificationType == null) {
            return new Intent(context, MainDashboard.class);
        }
        
        Intent intent;
        
        switch (notificationType) {
            case "report_update":
                intent = new Intent(context, ReportSubmissionActivity.class);
                String reportId = notificationData.get("reportId");
                if (reportId != null) {
                    intent.putExtra("highlightReportId", reportId);
                    intent.putExtra("highlightReport", true);
                }
                break;
                
            case "announcement":
                intent = new Intent(context, AlertsActivity.class);
                String announcementId = notificationData.get("announcementId");
                if (announcementId != null) {
                    intent.putExtra("highlightAnnouncementId", announcementId);
                    intent.putExtra("highlightAnnouncement", true);
                }
                break;
                
            case "chat_message":
                intent = new Intent(context, ChatActivity.class);
                String messageId = notificationData.get("messageId");
                if (messageId != null) {
                    intent.putExtra("highlightMessageId", messageId);
                    intent.putExtra("scrollToMessage", true);
                }
                break;
                
            default:
                intent = new Intent(context, MainDashboard.class);
                break;
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}

