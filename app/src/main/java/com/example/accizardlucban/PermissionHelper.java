package com.example.accizardlucban;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Centralized helper class for managing all app permissions.
 * Ensures permissions are requested when users first encounter features that need them.
 */
public class PermissionHelper {
    
    private static final String TAG = "PermissionHelper";
    private static final String PREFS_NAME = "permission_prefs";
    
    // Permission request preference keys
    private static final String KEY_CAMERA_PERMISSION_REQUESTED = "camera_permission_requested";
    private static final String KEY_LOCATION_PERMISSION_REQUESTED = "location_permission_requested";
    private static final String KEY_STORAGE_PERMISSION_REQUESTED = "storage_permission_requested";
    private static final String KEY_MICROPHONE_PERMISSION_REQUESTED = "microphone_permission_requested";
    private static final String KEY_PHONE_PERMISSION_REQUESTED = "phone_permission_requested";
    private static final String KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested";
    
    // Permission request codes
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1003;
    public static final int MICROPHONE_PERMISSION_REQUEST_CODE = 1004;
    public static final int PHONE_PERMISSION_REQUEST_CODE = 1005;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1006;
    
    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if storage permission is granted (handles Android version differences)
     * Supports "Allow while in use" permission on Android 14+ (API 34+)
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34+) - Check for full permissions OR "allow while in use" permission
            boolean hasFullPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) 
                    == PackageManager.PERMISSION_GRANTED;
            
            // Also check for "allow while in use" permission (READ_MEDIA_VISUAL_USER_SELECTED)
            boolean hasPartialPermission = ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
            
            return hasFullPermission || hasPartialPermission;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33)
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Check if microphone permission is granted
     */
    public static boolean hasMicrophonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if phone permission is granted
     */
    public static boolean hasPhonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Not required for Android 12 and below
    }
    
    /**
     * Request camera permission if not granted.
     * Shows rationale dialog if needed.
     */
    public static void requestCameraPermission(Activity activity, PermissionCallback callback) {
        if (hasCameraPermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_CAMERA_PERMISSION_REQUESTED, false);
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) 
                || !alreadyRequested) {
            // Show rationale or request directly
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_CAMERA_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission denied permanently
            Toast.makeText(activity, 
                    "Camera permission is required to take photos and record videos. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Request location permission if not granted.
     */
    public static void requestLocationPermission(Activity activity, PermissionCallback callback) {
        if (hasLocationPermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_LOCATION_PERMISSION_REQUESTED, false);
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
                || !alreadyRequested) {
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_LOCATION_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(activity,
                    "Location permission is required to show your location on the map. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Request storage permission if not granted (handles Android version differences).
     * On Android 14+, also requests READ_MEDIA_VISUAL_USER_SELECTED to support "Allow while in use" option.
     */
    public static void requestStoragePermission(Activity activity, PermissionCallback callback) {
        if (hasStoragePermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_STORAGE_PERMISSION_REQUESTED, false);
        
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34+) - Request full permissions and "allow while in use" option
            permissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33)
            permissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            // Android 12 and below
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        
        boolean shouldShowRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                shouldShowRationale = true;
                break;
            }
        }
        
        if (shouldShowRationale || !alreadyRequested) {
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_STORAGE_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(activity,
                    "Storage permission is required to select photos and videos. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Request microphone permission if not granted.
     */
    public static void requestMicrophonePermission(Activity activity, PermissionCallback callback) {
        if (hasMicrophonePermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_MICROPHONE_PERMISSION_REQUESTED, false);
        
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ needs both RECORD_AUDIO and READ_MEDIA_AUDIO
            permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        }
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO) 
                || !alreadyRequested) {
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_MICROPHONE_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity, permissions, MICROPHONE_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(activity,
                    "Microphone permission is required to record audio. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Request phone permission if not granted.
     */
    public static void requestPhonePermission(Activity activity, PermissionCallback callback) {
        if (hasPhonePermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_PHONE_PERMISSION_REQUESTED, false);
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE) 
                || !alreadyRequested) {
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_PHONE_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PHONE_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(activity,
                    "Phone permission is required to make emergency calls. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Request notification permission if not granted (Android 13+).
     */
    public static void requestNotificationPermission(Activity activity, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Not required for Android 12 and below
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        if (hasNotificationPermission(activity)) {
            if (callback != null) callback.onPermissionGranted();
            return;
        }
        
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyRequested = prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false);
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) 
                || !alreadyRequested) {
            if (!alreadyRequested) {
                prefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true).apply();
            }
            
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(activity,
                    "Notification permission is required to receive alerts and updates. Please enable it in Settings.",
                    Toast.LENGTH_LONG).show();
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Handle permission result and call appropriate callback.
     * For storage permissions on Android 14+, also accepts "allow while in use" permission.
     */
    public static void handlePermissionResult(Activity activity, int requestCode, 
            @NonNull String[] permissions, @NonNull int[] grantResults, PermissionCallback callback) {
        boolean permissionGranted = false;
        
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            // Special handling for storage permissions to support "allow while in use" on Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Check if full permissions granted
                boolean hasFullPermission = grantResults.length >= 2 && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED && 
                    grantResults[1] == PackageManager.PERMISSION_GRANTED;
                
                // Check if "allow while in use" permission granted (READ_MEDIA_VISUAL_USER_SELECTED)
                boolean hasPartialPermission = grantResults.length >= 3 && 
                    grantResults[2] == PackageManager.PERMISSION_GRANTED;
                
                permissionGranted = hasFullPermission || hasPartialPermission;
                Log.d(TAG, "Storage permission result - Full: " + hasFullPermission + ", Partial: " + hasPartialPermission);
            } else {
                // Android 13 and below - check first permission
                permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        } else if (grantResults.length > 0) {
            // For other permissions, check first result
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        
        if (permissionGranted) {
            Log.d(TAG, "Permission granted for request code: " + requestCode);
            if (callback != null) callback.onPermissionGranted();
        } else {
            Log.d(TAG, "Permission denied for request code: " + requestCode);
            if (callback != null) callback.onPermissionDenied();
        }
    }
    
    /**
     * Callback interface for permission requests.
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
    
    /**
     * Simple callback that does nothing (for cases where we don't need callbacks).
     */
    public static class EmptyCallback implements PermissionCallback {
        @Override
        public void onPermissionGranted() {}
        @Override
        public void onPermissionDenied() {}
    }
}


