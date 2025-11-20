package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentChange;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages real-time announcement notifications across all activities
 * This ensures that notification badges are updated even when users are on different tabs
 */
public class AnnouncementNotificationManager {
    
    private static final String TAG = "AnnouncementNotificationManager";
    private static final String PREFS_NAME = "AnnouncementNotificationPrefs";
    private static final String KEY_LAST_ANNOUNCEMENT_COUNT = "last_announcement_count";
    private static final String KEY_TOTAL_ANNOUNCEMENT_COUNT = "total_announcement_count";
    
    private static AnnouncementNotificationManager instance;
    private static ListenerRegistration globalListener;
    private static boolean isListenerActive = false;
    
    // Map to store badge references from different activities
    private static Map<String, TextView> badgeReferences = new HashMap<>();
    
    private AnnouncementNotificationManager() {
        // Private constructor for singleton
    }
    
    public static synchronized AnnouncementNotificationManager getInstance() {
        if (instance == null) {
            instance = new AnnouncementNotificationManager();
        }
        return instance;
    }
    
    /**
     * Start the global real-time listener for announcements
     */
    public void startGlobalListener() {
        try {
            if (isListenerActive && globalListener != null) {
                Log.d(TAG, "Global listener already active");
                return;
            }
            
            Log.d(TAG, "Starting global announcement listener");
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collection("announcements")
                    .orderBy("createdTime", Query.Direction.DESCENDING);
            
            globalListener = query.addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error in global listener: " + error.getMessage());
                    return;
                }
                
                if (snapshots == null) {
                    Log.d(TAG, "Global listener snapshots is null");
                    return;
                }
                
                int currentCount = snapshots.size();
                Log.d(TAG, "Global listener: Current announcement count = " + currentCount);
                
                // Don't automatically update badges here - let AlertsActivity handle it
                // based on viewed state. Just log the change.
                // AlertsActivity will calculate unread count and update badges accordingly
                
            });
            
            isListenerActive = true;
            Log.d(TAG, "Global listener started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting global listener: " + e.getMessage(), e);
        }
    }
    
    /**
     * Stop the global real-time listener
     */
    public void stopGlobalListener() {
        try {
            if (globalListener != null) {
                globalListener.remove();
                globalListener = null;
                isListenerActive = false;
                Log.d(TAG, "Global listener stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping global listener: " + e.getMessage(), e);
        }
    }
    
    /**
     * Register a badge TextView for updates
     */
    public void registerBadge(String activityName, TextView badge) {
        try {
            if (badge != null) {
                badgeReferences.put(activityName, badge);
                Log.d(TAG, "Badge registered for activity: " + activityName);
                
                // Update immediately with current count
                updateBadgeForActivity(activityName, getCurrentAnnouncementCount());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering badge: " + e.getMessage(), e);
        }
    }
    
    /**
     * Unregister a badge TextView
     */
    public void unregisterBadge(String activityName) {
        try {
            badgeReferences.remove(activityName);
            Log.d(TAG, "Badge unregistered for activity: " + activityName);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering badge: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update all registered badges with the current announcement count
     * Note: This is called by the global listener and should respect the "viewed" state
     */
    private void updateAllBadges(int currentCount) {
        try {
            // This method is called by the global listener
            // We need context to get SharedPreferences, so we'll skip automatic updates here
            // The AlertsActivity will handle badge updates based on viewed state
            Log.d(TAG, "Global listener detected count change: " + currentCount + 
                      " (badge updates handled by AlertsActivity based on viewed state)");
            
            // Just update the total count, but don't update badges automatically
            // AlertsActivity will calculate unread count and update badges accordingly
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating all badges: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update badge for a specific activity
     */
    private void updateBadgeForActivity(String activityName, int newCount) {
        try {
            TextView badge = badgeReferences.get(activityName);
            if (badge == null) return;
            
            if (newCount > 0) {
                badge.setText(String.valueOf(newCount));
                badge.setVisibility(View.VISIBLE);
                Log.d(TAG, "Badge updated for " + activityName + ": " + newCount + " new announcements");
            } else {
                badge.setVisibility(View.GONE);
                Log.d(TAG, "Badge hidden for " + activityName + ": no new announcements");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge for " + activityName + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear badge for a specific activity (when user visits that activity)
     */
    public void clearBadgeForActivity(String activityName) {
        try {
            TextView badge = badgeReferences.get(activityName);
            if (badge != null) {
                badge.setVisibility(View.GONE);
                badge.setText("0");
                Log.d(TAG, "Badge cleared for activity: " + activityName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing badge for " + activityName + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear all registered badges and reset notification state
     * Called when user views all announcements
     */
    public void clearAllBadges(Context context) {
        try {
            // Clear all badge views
            for (Map.Entry<String, TextView> entry : badgeReferences.entrySet()) {
                TextView badge = entry.getValue();
                if (badge != null) {
                    badge.setVisibility(View.GONE);
                    badge.setText("0");
                }
            }
            
            // Update SharedPreferences to mark all announcements as viewed
            SharedPreferences prefs = getSharedPreferences(context);
            if (prefs != null) {
                int currentTotalCount = prefs.getInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, 0);
                prefs.edit()
                    .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, currentTotalCount)
                    .apply();
                Log.d(TAG, "All badges cleared. Updated lastCount to: " + currentTotalCount);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all badges: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update notification state when announcements are viewed
     * This resets the badge calculation base
     */
    public void markAnnouncementsAsViewed(Context context, int currentTotalCount) {
        try {
            Log.d(TAG, "✅✅✅ markAnnouncementsAsViewed called with count: " + currentTotalCount);
            
            SharedPreferences notificationPrefs = getSharedPreferences(context);
            SharedPreferences alertsPrefs = context.getSharedPreferences("AlertsActivityPrefs", Context.MODE_PRIVATE);
            
            if (notificationPrefs != null) {
                notificationPrefs.edit()
                    .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, currentTotalCount)
                    .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, currentTotalCount)
                    .commit(); // Use commit for immediate write
                
                Log.d(TAG, "✅✅✅ Notification prefs updated - lastCount=" + currentTotalCount + 
                          ", totalCount=" + currentTotalCount);
            }
            
            // Also sync with AlertsActivity's SharedPreferences
            if (alertsPrefs != null) {
                alertsPrefs.edit()
                    .putInt("last_viewed_announcement_count", currentTotalCount)
                    .putLong("last_viewed_announcement_time", System.currentTimeMillis())
                    .commit(); // Use commit for immediate write
                
                Log.d(TAG, "✅✅✅ AlertsActivity prefs synced - lastViewedCount: " + currentTotalCount);
            }
            
            // Clear all badges since all are viewed
            clearAllBadges(context);
            
        } catch (Exception e) {
            Log.e(TAG, "Error marking announcements as viewed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get current announcement count from preferences
     */
    private int getCurrentAnnouncementCount() {
        try {
            SharedPreferences prefs = getSharedPreferences();
            return prefs.getInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current announcement count: " + e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Get SharedPreferences (requires context)
     */
    private SharedPreferences getSharedPreferences() {
        // This is a limitation - we need context to get SharedPreferences
        // In a real app, you might want to pass context or use Application context
        return null; // This will be handled by the calling activity
    }
    
    /**
     * Get SharedPreferences with context
     */
    public SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Update announcement count with context
     */
    public void updateAnnouncementCount(Context context, int count) {
        try {
            SharedPreferences prefs = getSharedPreferences(context);
            int lastCount = prefs.getInt(KEY_LAST_ANNOUNCEMENT_COUNT, 0);
            int newCount = count - lastCount;
            
            Log.d(TAG, "Updating announcement count: " + count + ", new: " + newCount);
            
            // Update all badges
            for (Map.Entry<String, TextView> entry : badgeReferences.entrySet()) {
                String activityName = entry.getKey();
                TextView badge = entry.getValue();
                
                if (newCount > 0) {
                    badge.setText(String.valueOf(newCount));
                    badge.setVisibility(View.VISIBLE);
                } else {
                    badge.setVisibility(View.GONE);
                }
            }
            
            // Save counts
            prefs.edit()
                .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, count)
                .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, count)
                .apply();
                
        } catch (Exception e) {
            Log.e(TAG, "Error updating announcement count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if listener is active
     */
    public boolean isListenerActive() {
        return isListenerActive;
    }
    
    /**
     * Get all registered badge references
     */
    public Map<String, TextView> getBadgeReferences() {
        return badgeReferences;
    }
    
    /**
     * Update badges with unread count directly
     * Used when we know the exact unread count
     */
    public void updateBadgesWithUnreadCount(Context context, int unreadCount) {
        try {
            Log.d(TAG, "✅✅✅ updateBadgesWithUnreadCount called with unreadCount: " + unreadCount);
            
            // Update all registered badges
            for (Map.Entry<String, TextView> entry : badgeReferences.entrySet()) {
                TextView badge = entry.getValue();
                if (badge != null) {
                    if (unreadCount > 0) {
                        badge.setText(String.valueOf(unreadCount));
                        badge.setVisibility(View.VISIBLE);
                        Log.d(TAG, "✅ Badge updated: showing " + unreadCount + " unread announcements");
                    } else {
                        badge.setVisibility(View.GONE);
                        badge.setText("0");
                        Log.d(TAG, "✅✅✅ Badge HIDDEN: all announcements viewed (unreadCount=0)");
                    }
                }
            }
            
            // Also sync with AlertsActivity's SharedPreferences
            // This ensures both preference files are in sync
            SharedPreferences alertsPrefs = context.getSharedPreferences("AlertsActivityPrefs", Context.MODE_PRIVATE);
            SharedPreferences notificationPrefs = getSharedPreferences(context);
            
            if (notificationPrefs != null) {
                int currentTotalCount = notificationPrefs.getInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, 0);
                
                if (unreadCount == 0) {
                    // All viewed - update last count to current total in BOTH preference files
                    notificationPrefs.edit()
                        .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, currentTotalCount)
                        .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, currentTotalCount)
                        .commit(); // Use commit for immediate write
                    
                    // Also update AlertsActivity's preferences
                    if (alertsPrefs != null) {
                        alertsPrefs.edit()
                            .putInt("last_viewed_announcement_count", currentTotalCount)
                            .putLong("last_viewed_announcement_time", System.currentTimeMillis())
                            .commit();
                        Log.d(TAG, "✅✅✅ Synced AlertsActivity preferences - lastViewedCount: " + currentTotalCount);
                    }
                    
                    Log.d(TAG, "✅✅✅ All announcements viewed - updated lastCount and totalCount to: " + currentTotalCount);
                } else {
                    // There are unread announcements - update total count but keep last count
                    notificationPrefs.edit()
                        .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, currentTotalCount)
                        .apply();
                    Log.d(TAG, "Unread announcements exist - updated totalCount to: " + currentTotalCount);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating badges with unread count: " + e.getMessage(), e);
        }
    }
}







