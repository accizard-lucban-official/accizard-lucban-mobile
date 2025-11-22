package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Helper class to manage location access preference
 * This checks if the user has enabled location access in ProfileActivity settings
 */
public class LocationPermissionHelper {
    
    private static final String TAG = "LocationPermissionHelper";
    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    
    /**
     * Check if location access is enabled by the user in settings
     * @param context The application context
     * @return true if location access is enabled, false otherwise
     */
    public static boolean isLocationAccessEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_LOCATION_ENABLED, true); // Default to enabled for backward compatibility
            Log.d(TAG, "Location access enabled: " + enabled);
            return enabled;
        } catch (Exception e) {
            Log.e(TAG, "Error checking location access preference", e);
            // Default to enabled if there's an error
            return true;
        }
    }
    
    /**
     * Check if location access is enabled and log a warning if disabled
     * @param context The application context
     * @param activityName Name of the activity checking (for logging)
     * @return true if location access is enabled, false otherwise
     */
    public static boolean isLocationAccessEnabledWithLog(Context context, String activityName) {
        boolean enabled = isLocationAccessEnabled(context);
        if (!enabled) {
            Log.w(TAG, "Location access is DISABLED by user in ProfileActivity settings. " + 
                      activityName + " should not access location.");
        }
        return enabled;
    }
}

