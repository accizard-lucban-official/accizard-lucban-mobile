package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Helper class to manage notification permission preference
 * This checks if the user has enabled notifications in ProfileActivity settings
 */
public class NotificationPermissionHelper {
    
    private static final String TAG = "NotificationPermissionHelper";
    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    
    /**
     * Check if notifications are enabled by the user in settings
     * @param context The application context
     * @return true if notifications are enabled, false otherwise
     */
    public static boolean areNotificationsEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // Default to enabled for backward compatibility
            Log.d(TAG, "Notifications enabled: " + enabled);
            return enabled;
        } catch (Exception e) {
            Log.e(TAG, "Error checking notification preference", e);
            // Default to enabled if there's an error
            return true;
        }
    }
    
    /**
     * Check if notifications are enabled and log a warning if disabled
     * @param context The application context
     * @param serviceName Name of the service checking (for logging)
     * @return true if notifications are enabled, false otherwise
     */
    public static boolean areNotificationsEnabledWithLog(Context context, String serviceName) {
        boolean enabled = areNotificationsEnabled(context);
        if (!enabled) {
            Log.w(TAG, "Notifications are DISABLED by user in ProfileActivity settings. " + 
                      serviceName + " should not show notifications.");
        }
        return enabled;
    }
}

