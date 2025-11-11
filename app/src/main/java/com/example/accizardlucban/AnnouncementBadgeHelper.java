package com.example.accizardlucban;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Helper class to easily integrate announcement badges into any activity
 */
public class AnnouncementBadgeHelper {
    
    private static final String TAG = "AnnouncementBadgeHelper";
    private String activityName;
    private TextView badge;
    private Activity activity;
    
    public AnnouncementBadgeHelper(Activity activity, String activityName, TextView badge) {
        this.activity = activity;
        this.activityName = activityName;
        this.badge = badge;
        
        // Register the badge with the global notification manager
        AnnouncementNotificationManager.getInstance().registerBadge(activityName, badge);
        
        Log.d(TAG, "Badge helper initialized for: " + activityName);
    }
    
    /**
     * Call this when the activity is resumed and user can see the badge
     * This will clear the badge since user is now viewing the activity
     */
    public void onActivityResumed() {
        try {
            // Clear badge when user is actively viewing this activity
            AnnouncementNotificationManager.getInstance().clearBadgeForActivity(activityName);
            Log.d(TAG, "Badge cleared for resumed activity: " + activityName);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing badge on resume: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call this when the activity is destroyed to clean up
     */
    public void onActivityDestroyed() {
        try {
            // Unregister badge when activity is destroyed
            AnnouncementNotificationManager.getInstance().unregisterBadge(activityName);
            Log.d(TAG, "Badge helper destroyed for: " + activityName);
        } catch (Exception e) {
            Log.e(TAG, "Error destroying badge helper: " + e.getMessage(), e);
        }
    }
    
    /**
     * Manually show/hide the badge
     */
    public void setBadgeVisibility(boolean visible) {
        try {
            if (badge != null) {
                badge.setVisibility(visible ? View.VISIBLE : View.GONE);
                Log.d(TAG, "Badge visibility set to: " + visible + " for " + activityName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting badge visibility: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the badge TextView
     */
    public TextView getBadge() {
        return badge;
    }
    
    /**
     * Get the activity name
     */
    public String getActivityName() {
        return activityName;
    }
}







































