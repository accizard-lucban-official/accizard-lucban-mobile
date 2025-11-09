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
                
                // Update all registered badges
                updateAllBadges(currentCount);
                
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
     */
    private void updateAllBadges(int currentCount) {
        try {
            SharedPreferences prefs = getSharedPreferences();
            int lastCount = prefs.getInt(KEY_LAST_ANNOUNCEMENT_COUNT, 0);
            int newCount = currentCount - lastCount;
            
            Log.d(TAG, "Updating badges: lastCount=" + lastCount + ", currentCount=" + currentCount + ", newCount=" + newCount);
            
            // Update each registered badge
            for (Map.Entry<String, TextView> entry : badgeReferences.entrySet()) {
                String activityName = entry.getKey();
                TextView badge = entry.getValue();
                
                updateBadgeForActivity(activityName, newCount);
            }
            
            // Save the current count as the last count
            prefs.edit()
                .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, currentCount)
                .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, currentCount)
                .apply();
                
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
                Log.d(TAG, "Badge cleared for activity: " + activityName);
            }
            
            // Reset the last count to current count to prevent showing badges again
            SharedPreferences prefs = getSharedPreferences();
            int currentCount = prefs.getInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, 0);
            prefs.edit()
                .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, currentCount)
                .apply();
                
        } catch (Exception e) {
            Log.e(TAG, "Error clearing badge for " + activityName + ": " + e.getMessage(), e);
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
}







