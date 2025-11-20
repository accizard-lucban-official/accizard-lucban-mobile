package com.example.accizardlucban;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Manages the display of push notifications
 * Builds and shows notifications with proper styling and actions
 */
public class AcciZardNotificationManager {
    
    private static final String TAG = "AcciZardNotificationMgr";
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public AcciZardNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) 
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    /**
     * Show a notification based on the data received from FCM
     * 
     * @param title Notification title
     * @param body Notification body/message
     * @param data Additional data from FCM
     */
    public void showNotification(String title, String body, Map<String, String> data) {
        try {
            if (title == null || body == null) {
                Log.w(TAG, "Title or body is null, skipping notification");
                return;
            }
            
            // Determine notification type and channel
            String notificationType = data != null ? data.get("type") : null;
            
            // ✅ CRITICAL FIX: Removed suppression check - upstream already handles it
            // If notification reached here, it means MyFirebaseMessagingService already determined it should be shown
            // We trust the upstream logic and always show notifications that reach this point
            if ("chat_message".equals(notificationType)) {
                Log.d(TAG, "💬 Chat message notification reached NotificationManager - will show");
                Log.d(TAG, "💬 Upstream already approved this notification");
            }
            
            String priority = data.get("priority");
            String channelId = NotificationChannelManager.getChannelId(notificationType, priority);
            
            Log.d(TAG, "Showing notification - Type: " + notificationType + ", Channel: " + channelId);
            
            // Create notification intent
            Intent intent = NotificationDeepLinkHandler.createNotificationIntent(context, data);
            
            int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    generateNotificationId(data),
                    intent,
                    pendingIntentFlags
            );
            
            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.appiconpng) // Your app icon
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(getNotificationPriority(channelId))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true) // Dismiss when tapped
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(getVibrationPattern(channelId));
            
            // Add large icon (optional)
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.appiconpng));
            
            // Add notification color (orange theme)
            builder.setColor(0xFFFF5722); // #FF5722 - your app's orange color
            
            // Add specific actions based on notification type
            addNotificationActions(builder, notificationType, data, channelId);
            
            // ✅ CRITICAL: Verify notification channel exists before showing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
                if (channel == null) {
                    Log.e(TAG, "❌ CRITICAL ERROR: Notification channel does not exist: " + channelId);
                    Log.e(TAG, "❌ Creating channel on-the-fly as fallback");
                    // Create channel on-the-fly as emergency fallback
                    NotificationChannelManager channelManager = new NotificationChannelManager(context);
                    channelManager.createAllChannels();
                } else {
                    Log.d(TAG, "✅ Notification channel verified: " + channelId + " (importance: " + channel.getImportance() + ")");
                }
            }
            
            // Show the notification
            int notificationId = generateNotificationId(data);
            try {
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "✅ Notification displayed successfully - ID: " + notificationId);
                Log.d(TAG, "✅ Notification Type: " + notificationType);
                Log.d(TAG, "✅ Notification Title: " + title);
                Log.d(TAG, "✅ Notification Body: " + body);
            } catch (Exception notifyException) {
                Log.e(TAG, "❌ CRITICAL ERROR: Failed to display notification", notifyException);
                Log.e(TAG, "❌ Notification ID: " + notificationId);
                Log.e(TAG, "❌ Channel ID: " + channelId);
                Log.e(TAG, "❌ Error: " + notifyException.getMessage());
                throw notifyException; // Re-throw to be caught by outer try-catch
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR: Error showing notification: " + e.getMessage(), e);
            Log.e(TAG, "❌ Notification Type: " + (data != null ? data.get("type") : "null"));
            Log.e(TAG, "❌ Title: " + title);
            Log.e(TAG, "❌ Body: " + body);
            e.printStackTrace();
        }
    }
    
    /**
     * Get notification priority based on channel
     */
    private int getNotificationPriority(String channelId) {
        switch (channelId) {
            case NotificationChannelManager.CHANNEL_REPORT_UPDATES:
            case NotificationChannelManager.CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS:
            case NotificationChannelManager.CHANNEL_CHAT_MESSAGES:
                return NotificationCompat.PRIORITY_HIGH;
                
            case NotificationChannelManager.CHANNEL_ANNOUNCEMENTS:
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }
    
    /**
     * Get vibration pattern based on channel
     */
    private long[] getVibrationPattern(String channelId) {
        switch (channelId) {
            case NotificationChannelManager.CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS:
                // Emergency pattern: long-short-long-short-long
                return new long[]{0, 500, 200, 500, 200, 500};
                
            case NotificationChannelManager.CHANNEL_REPORT_UPDATES:
                // Report update pattern: short-short
                return new long[]{0, 300, 200, 300};
                
            case NotificationChannelManager.CHANNEL_CHAT_MESSAGES:
                // Chat pattern: single short
                return new long[]{0, 300};
                
            case NotificationChannelManager.CHANNEL_ANNOUNCEMENTS:
            default:
                // Default pattern
                return new long[]{0, 200};
        }
    }
    
    /**
     * Add notification actions based on type
     */
    private void addNotificationActions(NotificationCompat.Builder builder, String notificationType, 
                                       Map<String, String> data, String channelId) {
        // For now, we'll keep it simple with just the tap action
        // You can add more actions here like "View", "Reply", etc.
        
        // Example for future enhancement:
        // if ("chat_message".equals(notificationType)) {
        //     // Add "Reply" action for chat messages
        //     Intent replyIntent = new Intent(context, ChatActivity.class);
        //     PendingIntent replyPendingIntent = PendingIntent.getActivity(...);
        //     builder.addAction(R.drawable.ic_reply, "Reply", replyPendingIntent);
        // }
    }
    
    /**
     * Generate a unique notification ID based on the notification data
     * This ensures each notification has a unique ID
     */
    private int generateNotificationId(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return (int) System.currentTimeMillis();
        }
        
        String notificationType = data.get("type");
        
        if ("report_update".equals(notificationType)) {
            String reportId = data.get("reportId");
            if (reportId != null) {
                return ("report_" + reportId).hashCode();
            }
        } else if ("announcement".equals(notificationType)) {
            String announcementId = data.get("announcementId");
            if (announcementId != null) {
                return ("announcement_" + announcementId).hashCode();
            }
        } else if ("chat_message".equals(notificationType)) {
            String messageId = data.get("messageId");
            if (messageId != null) {
                return ("chat_" + messageId).hashCode();
            }
        }
        
        // Fallback to timestamp-based ID
        return (int) System.currentTimeMillis();
    }
    
    /**
     * Cancel a specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
        Log.d(TAG, "Notification cancelled - ID: " + notificationId);
    }
    
    /**
     * Cancel all notifications from this app
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
        Log.d(TAG, "All notifications cancelled");
    }
    
    /**
     * Download image from URL for notification (optional - for future use)
     * Can be used to show images in notifications
     */
    private Bitmap downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image: " + e.getMessage());
            return null;
        }
    }
}

