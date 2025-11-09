package com.example.accizardlucban;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * Manages notification channels for Android O (API 26) and above
 * Creates 4 channels as per web app specification:
 * 1. report_updates - HIGH priority for report status updates
 * 2. high_priority_announcements - HIGH priority for critical announcements
 * 3. announcements - DEFAULT priority for general announcements
 * 4. chat_messages - HIGH priority for chat messages
 */
public class NotificationChannelManager {
    
    private static final String TAG = "NotificationChannelMgr";
    
    // Channel IDs (must match web app Cloud Functions)
    public static final String CHANNEL_REPORT_UPDATES = "report_updates";
    public static final String CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS = "high_priority_announcements";
    public static final String CHANNEL_ANNOUNCEMENTS = "announcements";
    public static final String CHANNEL_CHAT_MESSAGES = "chat_messages";
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public NotificationChannelManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) 
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    /**
     * Create all notification channels
     * Should be called when app starts (in Application class or MainActivity)
     */
    public void createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                createReportUpdatesChannel();
                createHighPriorityAnnouncementsChannel();
                createAnnouncementsChannel();
                createChatMessagesChannel();
                Log.d(TAG, "✅ All notification channels created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels: " + e.getMessage(), e);
            }
        } else {
            Log.d(TAG, "Device API level < 26, notification channels not required");
        }
    }
    
    /**
     * Channel 1: Report Status Updates (HIGH priority)
     * Used when admin updates the status of a user's report
     */
    private void createReportUpdatesChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_REPORT_UPDATES,
                    "Report Status Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Status updates on your submitted reports");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            channel.enableLights(true);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created channel: " + CHANNEL_REPORT_UPDATES);
        }
    }
    
    /**
     * Channel 2: High Priority Announcements (HIGH priority)
     * Used for critical announcements like typhoon warnings, evacuations
     */
    private void createHighPriorityAnnouncementsChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS,
                    "Important Announcements",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("High priority announcements from AcciZard");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300, 200, 300});
            channel.setShowBadge(true);
            channel.enableLights(true);
            
            // Custom sound for emergency announcements (optional)
            // channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/raw/emergency_sound"), 
            //     new AudioAttributes.Builder()
            //         .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            //         .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            //         .build());
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created channel: " + CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS);
        }
    }
    
    /**
     * Channel 3: General Announcements (DEFAULT priority)
     * Used for general announcements like events, updates
     */
    private void createAnnouncementsChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ANNOUNCEMENTS,
                    "Announcements",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("General announcements from AcciZard");
            channel.setShowBadge(true);
            channel.enableLights(true);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created channel: " + CHANNEL_ANNOUNCEMENTS);
        }
    }
    
    /**
     * Channel 4: Chat Messages (HIGH priority)
     * Used for messages from AcciZard Support
     */
    private void createChatMessagesChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_CHAT_MESSAGES,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Messages from AcciZard Support");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.enableLights(true);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created channel: " + CHANNEL_CHAT_MESSAGES);
        }
    }
    
    /**
     * Get the appropriate channel ID based on notification type and priority
     * This matches the logic from the web app Cloud Functions
     */
    public static String getChannelId(String notificationType, String priority) {
        switch (notificationType) {
            case "report_update":
                return CHANNEL_REPORT_UPDATES;
                
            case "announcement":
                if ("high".equalsIgnoreCase(priority)) {
                    return CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS;
                } else {
                    return CHANNEL_ANNOUNCEMENTS;
                }
                
            case "chat_message":
                return CHANNEL_CHAT_MESSAGES;
                
            default:
                Log.w(TAG, "Unknown notification type: " + notificationType + ", using default channel");
                return CHANNEL_ANNOUNCEMENTS;
        }
    }
    
    /**
     * Delete a notification channel (for testing purposes)
     */
    public void deleteChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId);
            Log.d(TAG, "Deleted channel: " + channelId);
        }
    }
    
    /**
     * Delete all notification channels (for testing purposes)
     */
    public void deleteAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deleteChannel(CHANNEL_REPORT_UPDATES);
            deleteChannel(CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS);
            deleteChannel(CHANNEL_ANNOUNCEMENTS);
            deleteChannel(CHANNEL_CHAT_MESSAGES);
            Log.d(TAG, "All channels deleted");
        }
    }
}

