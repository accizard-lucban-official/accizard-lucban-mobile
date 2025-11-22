package com.example.accizardlucban;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications from Firebase
 * 
 * This service receives notifications in three states:
 * 1. Foreground - App is open and visible
 * 2. Background - App is running but not visible
 * 3. Terminated - App is completely closed
 * 
 * ✅ NOTIFICATION TYPES SUPPORTED:
 * - chat_message: Messages from admin/web to user
 *   - Suppressed only if user is actively viewing ChatActivity
 *   - Always shown when user is on another tab or app is closed
 * 
 * - announcement: New announcements from admin/web
 *   - Always shown (never suppressed)
 *   - Formatted as: "Type: Description [Priority: High/Medium/Low]"
 *   - HTML tags are stripped from content
 * 
 * - report_update: Status updates on user's reports
 *   - Always shown (never suppressed)
 * 
 * ✅ NOTIFICATION FLOW:
 * 1. Web app sends FCM notification (with notification payload OR data-only)
 * 2. onMessageReceived() is called (foreground) or system handles it (background/terminated)
 * 3. For foreground: We check suppression and show notification if needed
 * 4. For background/terminated: System shows notification automatically (if notification payload exists)
 * 5. For data-only messages: We MUST show notification ourselves
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
        
        // Save the new token to Firestore (only if notifications are enabled)
        FCMTokenManager tokenManager = new FCMTokenManager(this);
        tokenManager.refreshFCMToken(token); // This method checks notification preference
    }
    
    /**
     * Called when a message is received
     * This method is called for:
     * - Data-only messages (always, regardless of app state)
     * - Notification messages when app is in FOREGROUND
     * 
     * IMPORTANT: When app is in BACKGROUND/TERMINATED:
     * - If message has notification payload → FCM shows automatically (onMessageReceived NOT called)
     * - If message is data-only → onMessageReceived IS called, we MUST show notification ourselves
     * 
     * When app is in FOREGROUND:
     * - onMessageReceived IS called for both notification and data-only messages
     * - We MUST show notification ourselves (FCM does NOT show automatically in foreground)
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "📩 ========== FCM MESSAGE RECEIVED ==========");
        Log.d(TAG, "📩 Message received from: " + remoteMessage.getFrom());
        Log.d(TAG, "📩 App state: " + (isAppInForeground() ? "FOREGROUND" : "BACKGROUND/TERMINATED"));
        Log.d(TAG, "📩 Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "📩 Message Type: " + remoteMessage.getMessageType());
        
        // ✅ CRITICAL: Check if notifications are enabled in settings FIRST
        if (!NotificationPermissionHelper.areNotificationsEnabledWithLog(this, "MyFirebaseMessagingService")) {
            Log.w(TAG, "🚫 Notifications are DISABLED by user - NOT showing notification");
            Log.w(TAG, "🚫 Message will be ignored (chats, reports, alerts will not be shown)");
            return; // Exit immediately - don't process or show notification
        }
        
        // Check if message contains a notification payload
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        
        Log.d(TAG, "📩 Has notification payload: " + (notification != null));
        Log.d(TAG, "📩 Has data payload: " + (data != null && !data.isEmpty()));
        if (data != null) {
            Log.d(TAG, "📩 Data payload keys: " + data.keySet().toString());
            Log.d(TAG, "📩 Data payload values: " + data.toString());
        }
        
        // Get notification type
        String notificationType = data != null ? data.get("type") : null;
        
        // ✅ CRITICAL FIX: For chat messages, check suppression FIRST
        // RULE: Only suppress if user is actively viewing ChatActivity
        // Otherwise, ALWAYS show notification (regardless of app state or payload type)
        if ("chat_message".equals(notificationType)) {
            boolean isChatVisible = ChatActivityTracker.isChatActivityVisible();
            boolean hasNotificationPayload = (notification != null);
            boolean isForeground = isAppInForeground();
            
            Log.d(TAG, "💬 ========== CHAT MESSAGE NOTIFICATION ==========");
            Log.d(TAG, "💬 Chat visible: " + isChatVisible);
            Log.d(TAG, "💬 App foreground: " + isForeground);
            Log.d(TAG, "💬 Has notification payload: " + hasNotificationPayload);
            Log.d(TAG, "💬 Has data payload: " + (data != null && !data.isEmpty()));
            
            // ✅ CRITICAL RULE: Only suppress if chat is visible
            // If chat is NOT visible, ALWAYS show notification (no exceptions, no conditions)
            if (isChatVisible) {
                Log.d(TAG, "🚫 SUPPRESSING: User is viewing chat - notification will NOT be shown");
                // Still process data payload for badge updates
                if (data != null && !data.isEmpty()) {
                    Log.d(TAG, "Processing data payload for badge update");
                    handleDataPayload(data);
                }
                Log.d(TAG, "💬 ============================================");
                return; // Exit immediately - don't show notification
            } else {
                Log.d(TAG, "✅ ALLOWING notification - Chat is NOT visible");
                Log.d(TAG, "✅ Will show notification (chat not visible)");
                Log.d(TAG, "✅ App state does NOT matter - notification will be shown");
                Log.d(TAG, "💬 ============================================");
                // Continue to show notification below - DO NOT RETURN
            }
        }
        
        // ✅ CRITICAL: Process notification payload OR data-only messages
        // Priority: Show notification first, then process data payload for badge updates
        
        if (notification != null) {
            // ✅ NOTIFICATION PAYLOAD EXISTS - Show notification immediately
            String title = notification.getTitle();
            String body = notification.getBody();
            
            Log.d(TAG, "📢 ========== NOTIFICATION PAYLOAD RECEIVED ==========");
            Log.d(TAG, "📢 Title: " + title);
            Log.d(TAG, "📢 Body: " + body);
            Log.d(TAG, "📢 Type: " + notificationType);
            
            // ✅ FIXED: Strip HTML tags from body immediately if it's an announcement
            if ("announcement".equals(notificationType) && body != null && !body.isEmpty()) {
                body = stripHtmlTags(body);
                Log.d(TAG, "✅ Stripped HTML tags from notification body");
            }
            
            // ✅ CRITICAL: For chat messages with notification payload, ensure we show it
            // If we reached here, it means chat is NOT visible (suppression already handled above)
            if ("chat_message".equals(notificationType)) {
                Log.d(TAG, "✅ Chat message with notification payload - will show notification");
                Log.d(TAG, "✅ Chat is NOT visible - notification WILL be displayed");
                Log.d(TAG, "✅ This is a FOREGROUND notification (FCM doesn't show automatically in foreground)");
            }
            
            Log.d(TAG, "📢 ================================================");
            
            // ✅ CRITICAL: Show the notification (chat is NOT visible, so always show)
            showNotification(title, body, data);
            
            // Process data payload for badge updates (after showing notification)
            if (data != null && !data.isEmpty()) {
                handleDataPayload(data);
            }
            
        } else if (data != null && !data.isEmpty() && notificationType != null) {
            // ✅ DATA-ONLY MESSAGE - We MUST show notification ourselves
            // This happens when web app sends data-only or when app is in background
            Log.d(TAG, "📦 ========== DATA-ONLY MESSAGE RECEIVED ==========");
            Log.d(TAG, "📦 Type: " + notificationType);
            Log.d(TAG, "📦 Data payload: " + data.toString());
            Log.d(TAG, "📦 CRITICAL: Data-only message - we MUST show notification ourselves");
            
            // ✅ HANDLE CHAT MESSAGES (data-only)
            // NOTE: Suppression already handled above, so if we reach here, chat is NOT visible
            if ("chat_message".equals(notificationType)) {
                // ✅ CRITICAL: Double-check chat visibility (safety check)
                boolean isChatVisible = ChatActivityTracker.isChatActivityVisible();
                Log.d(TAG, "💬 Processing data-only chat message");
                Log.d(TAG, "💬 Chat visible (double-check): " + isChatVisible);
                
                if (isChatVisible) {
                    Log.d(TAG, "🚫 Chat became visible - suppressing data-only notification");
                    // Still process data payload for badge updates
                    handleDataPayload(data);
                    Log.d(TAG, "📦 ============================================");
                    return; // Exit early
                }
                
                // Extract title and body from data payload
                String title = data.get("senderName");
                String body = data.get("content");
                
                // Try multiple field names for message content (web app may use different fields)
                if (body == null || body.isEmpty()) {
                    body = data.get("message");
                }
                if (body == null || body.isEmpty()) {
                    body = data.get("text");
                }
                if (body == null || body.isEmpty()) {
                    body = data.get("body");
                }
                
                // Try multiple field names for sender name
                if (title == null || title.isEmpty()) {
                    title = data.get("title");
                }
                if (title == null || title.isEmpty()) {
                    title = "AcciZard Support";
                }
                
                // Default message if still empty
                if (body == null || body.isEmpty()) {
                    body = "You have a new message";
                }
                
                Log.d(TAG, "✅ SHOWING notification for data-only chat message");
                Log.d(TAG, "✅ Title: " + title);
                Log.d(TAG, "✅ Body: " + body);
                Log.d(TAG, "✅ Data keys: " + data.keySet().toString());
                Log.d(TAG, "✅ Chat is NOT visible - notification will be displayed");
                
                // Show notification (chat is NOT visible, so always show)
                showNotification(title, body, data);
                
                // Process data payload for badge updates
                handleDataPayload(data);
                Log.d(TAG, "📦 ============================================");
            }
            // ✅ HANDLE ANNOUNCEMENTS (data-only)
            else if ("announcement".equals(notificationType)) {
                String title = data.get("title");
                String announcementType = data.get("announcementType");
                String description = data.get("description");
                String priority = data.get("priority");
                
                // Build title if not provided
                if (title == null || title.isEmpty()) {
                    if (priority != null && priority.equalsIgnoreCase("high")) {
                        title = "🚨 Important Announcement";
                    } else if (priority != null && priority.equalsIgnoreCase("medium")) {
                        title = "⚠️ Announcement";
                    } else {
                        title = "Announcement";
                    }
                }
                
                // Build body with type, description, and priority
                StringBuilder bodyBuilder = new StringBuilder();
                if (announcementType != null && !announcementType.isEmpty()) {
                    bodyBuilder.append(announcementType);
                }
                if (description != null && !description.isEmpty()) {
                    if (bodyBuilder.length() > 0) {
                        bodyBuilder.append(": ");
                    }
                    bodyBuilder.append(description);
                }
                if (priority != null && !priority.isEmpty()) {
                    String priorityDisplay = priority.substring(0, 1).toUpperCase() + 
                                           (priority.length() > 1 ? priority.substring(1).toLowerCase() : "");
                    if (bodyBuilder.length() > 0) {
                        bodyBuilder.append(" [Priority: ").append(priorityDisplay).append("]");
                    } else {
                        bodyBuilder.append("Priority: ").append(priorityDisplay);
                    }
                }
                
                String body = bodyBuilder.length() > 0 ? bodyBuilder.toString() : "New announcement";
                
                // Strip HTML tags from body
                body = stripHtmlTags(body);
                
                // Truncate if too long
                if (body.length() > 200) {
                    body = body.substring(0, 197) + "...";
                }
                
                Log.d(TAG, "✅ Showing notification for data-only announcement message");
                Log.d(TAG, "✅ Title: " + title);
                Log.d(TAG, "✅ Body: " + body);
                
                // Show notification
                showNotification(title, body, data);
                
                // Process data payload for badge updates
                handleDataPayload(data);
                Log.d(TAG, "📦 ============================================");
            }
            // ✅ HANDLE OTHER NOTIFICATION TYPES (data-only)
            else {
                // For other types (report_update, etc.), try to show notification if we have title/body
                String title = data.get("title");
                String body = data.get("body");
                
                if (title != null && !title.isEmpty() && body != null && !body.isEmpty()) {
                    Log.d(TAG, "✅ Showing notification for data-only " + notificationType + " message");
                    showNotification(title, body, data);
                }
                
                // Always process data payload
                handleDataPayload(data);
                Log.d(TAG, "📦 ============================================");
            }
        } else {
            // No notification payload and no data payload - this shouldn't happen
            Log.w(TAG, "⚠️ No notification payload and no data payload in message");
        }
        
        // ✅ CRITICAL DEBUG: Log final state
        Log.d(TAG, "📊 ========== FINAL NOTIFICATION STATE ==========");
        Log.d(TAG, "📊 Type: " + notificationType);
        Log.d(TAG, "📊 Has notification payload: " + (notification != null));
        Log.d(TAG, "📊 Has data payload: " + (data != null && !data.isEmpty()));
        Log.d(TAG, "📊 Chat visible: " + ChatActivityTracker.isChatActivityVisible());
        Log.d(TAG, "📊 App foreground: " + isAppInForeground());
        
        // ✅ CRITICAL: For chat messages, verify notification was shown or suppressed correctly
        if ("chat_message".equals(notificationType)) {
            boolean isChatVisible = ChatActivityTracker.isChatActivityVisible();
            if (isChatVisible) {
                Log.d(TAG, "📊 ✅ VERIFIED: Chat is visible - notification correctly suppressed");
            } else {
                Log.d(TAG, "📊 ✅ VERIFIED: Chat is NOT visible - notification should be shown");
                // Double-check: If we have data but no notification was shown, log warning
                if (data != null && !data.isEmpty() && notification == null) {
                    Log.w(TAG, "📊 ⚠️ WARNING: Data-only chat message - notification should be shown in data-only handler");
                }
            }
        }
        
        Log.d(TAG, "📊 ==============================================");
    }
    
    /**
     * Display the notification using our custom notification manager
     * This method is ONLY called when notification should be shown (suppression already handled upstream)
     */
    private void showNotification(String title, String body, Map<String, String> data) {
        try {
            if (title == null || title.isEmpty()) {
                Log.e(TAG, "❌ CRITICAL ERROR: Notification title is null or empty - cannot show notification");
                return;
            }
            
            if (body == null || body.isEmpty()) {
                Log.e(TAG, "❌ CRITICAL ERROR: Notification body is null or empty - cannot show notification");
                return;
            }
            
            if (data == null) {
                Log.w(TAG, "Data map is null, creating empty map");
                data = new java.util.HashMap<>();
            }
            
            String notificationType = data.get("type");
            Log.d(TAG, "📱 ========== SHOWING NOTIFICATION ==========");
            Log.d(TAG, "📱 Type: " + notificationType);
            Log.d(TAG, "📱 Title: " + title);
            Log.d(TAG, "📱 Body: " + body);
            Log.d(TAG, "📱 Chat visible: " + ChatActivityTracker.isChatActivityVisible());
            Log.d(TAG, "📱 App foreground: " + isAppInForeground());
            
            // ✅ CRITICAL: Verify chat is NOT visible (safety check)
            if ("chat_message".equals(notificationType)) {
                boolean isChatVisible = ChatActivityTracker.isChatActivityVisible();
                if (isChatVisible) {
                    Log.e(TAG, "❌ CRITICAL ERROR: Chat is visible but notification reached showNotification()!");
                    Log.e(TAG, "❌ This should have been suppressed upstream - NOT showing notification");
                    return; // Don't show if chat is visible (safety check)
                }
                Log.d(TAG, "💬 Chat message notification - will show (chat is NOT visible)");
                Log.d(TAG, "💬 Upstream already approved this notification");
            }
            
            // ✅ CUSTOMIZE: For announcements, format notification to show type, description, and priority
            if ("announcement".equals(notificationType)) {
                String announcementType = data.get("announcementType");
                String description = data.get("description");
                String priority = data.get("priority");
                
                // ✅ CRITICAL FIX: Strip HTML from body first (in case it came from notification payload)
                if (body != null && !body.isEmpty()) {
                    body = stripHtmlTags(body);
                    Log.d(TAG, "✅ Stripped HTML tags from body parameter");
                }
                
                // If description is not in data, try to get it from the body (already stripped)
                if (description == null || description.isEmpty()) {
                    description = body;
                }
                
                // ✅ STRIP HTML TAGS: Remove HTML tags like <p>, </p>, <br>, etc. from description
                if (description != null && !description.isEmpty()) {
                    description = stripHtmlTags(description);
                    Log.d(TAG, "✅ Stripped HTML tags from description");
                }
                
                // Also strip HTML from announcementType if it exists
                if (announcementType != null && !announcementType.isEmpty()) {
                    announcementType = stripHtmlTags(announcementType);
                }
                
                // Format priority display text (capitalize first letter)
                String priorityDisplay = "";
                if (priority != null && !priority.isEmpty()) {
                    priorityDisplay = priority.substring(0, 1).toUpperCase() + 
                                     (priority.length() > 1 ? priority.substring(1).toLowerCase() : "");
                }
                
                // Format notification body: "Type: Description [Priority: High/Medium/Low]"
                StringBuilder bodyBuilder = new StringBuilder();
                
                // Add type if available
                if (announcementType != null && !announcementType.isEmpty()) {
                    bodyBuilder.append(announcementType);
                }
                
                // Add description if available
                if (description != null && !description.isEmpty()) {
                    if (bodyBuilder.length() > 0) {
                        bodyBuilder.append(": ");
                    }
                    bodyBuilder.append(description);
                }
                
                // Add priority if available
                if (priorityDisplay != null && !priorityDisplay.isEmpty()) {
                    if (bodyBuilder.length() > 0) {
                        bodyBuilder.append(" [Priority: ").append(priorityDisplay).append("]");
                    } else {
                        bodyBuilder.append("Priority: ").append(priorityDisplay);
                    }
                }
                
                // If we have content, use it; otherwise use original body
                if (bodyBuilder.length() > 0) {
                    body = bodyBuilder.toString();
                    Log.d(TAG, "✅ Formatted announcement notification - Type: " + announcementType + 
                              ", Description: " + description + ", Priority: " + priorityDisplay);
                } else {
                    Log.d(TAG, "⚠️ No announcement data available, using original body");
                }
                
                // Update title to include priority indicator if high priority
                if (priority != null && priority.equalsIgnoreCase("high")) {
                    if (title == null || title.isEmpty()) {
                        title = "🚨 Important Announcement";
                    } else if (!title.contains("🚨")) {
                        title = "🚨 " + title;
                    }
                    Log.d(TAG, "✅ Added high priority indicator to title");
                } else if (priority != null && priority.equalsIgnoreCase("medium")) {
                    if (title == null || title.isEmpty()) {
                        title = "⚠️ Announcement";
                    } else if (!title.contains("⚠️")) {
                        title = "⚠️ " + title;
                    }
                    Log.d(TAG, "✅ Added medium priority indicator to title");
                } else if (title == null || title.isEmpty()) {
                    title = "Announcement";
                }
                
                // Truncate long descriptions (max 200 chars for notification)
                if (body.length() > 200) {
                    body = body.substring(0, 197) + "...";
                    Log.d(TAG, "✅ Truncated announcement notification body to 200 chars");
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
     * Strip HTML tags from text
     * Removes tags like <p>, </p>, <br>, <div>, etc. and converts HTML entities
     * Preserves text content while removing all HTML formatting
     */
    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        
        try {
            String text = html;
            
            // First, replace <br> and <br/> with spaces (preserve line breaks as spaces)
            text = text.replaceAll("(?i)<br\\s*/?>", " ");
            
            // Replace <p> and </p> tags with spaces
            text = text.replaceAll("(?i)</?p>", " ");
            
            // Replace <div> and </div> tags with spaces
            text = text.replaceAll("(?i)</?div>", " ");
            
            // Replace other common block elements with spaces
            text = text.replaceAll("(?i)</?(span|strong|b|em|i|u|h[1-6]|li|ul|ol|blockquote|pre|code)>", " ");
            
            // Remove all remaining HTML tags using regex
            // This pattern matches any HTML tag: <...>
            text = text.replaceAll("<[^>]+>", "");
            
            // Replace common HTML entities (order matters - do &amp; last)
            text = text.replace("&nbsp;", " ");
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            text = text.replace("&quot;", "\"");
            text = text.replace("&#39;", "'");
            text = text.replace("&apos;", "'");
            text = text.replace("&amp;", "&");
            
            // Replace numeric HTML entities
            text = text.replaceAll("&#\\d+;", " ");
            text = text.replaceAll("&#x[0-9a-fA-F]+;", " ");
            
            // Clean up multiple spaces and newlines (replace with single space)
            text = text.replaceAll("\\s+", " ");
            
            // Remove leading/trailing whitespace
            text = text.trim();
            
            Log.d(TAG, "✅ Stripped HTML - Original: '" + html + "' → Cleaned: '" + text + "'");
            
            return text;
        } catch (Exception e) {
            Log.e(TAG, "Error stripping HTML tags: " + e.getMessage(), e);
            // Return original text if stripping fails
            return html;
        }
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
    
    /**
     * Check if the app is currently in the foreground
     * @return true if app is in foreground, false if in background or terminated
     */
    private boolean isAppInForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return false;
            }
            
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            
            String packageName = getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking app foreground state: " + e.getMessage(), e);
            // On error, assume app is in background to be safe (show notifications)
            return false;
        }
    }
}

