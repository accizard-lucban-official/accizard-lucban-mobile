package com.example.accizardlucban;

import android.util.Log;

/**
 * Tracks whether the ChatActivity is currently visible to the user
 * Used to prevent showing push notifications when user is already viewing the chat
 */
public class ChatActivityTracker {
    
    private static final String TAG = "ChatActivityTracker";
    private static boolean isChatActivityVisible = false;
    
    /**
     * Reset the tracker - call this when app starts to ensure clean state
     */
    public static void reset() {
        boolean previousState = isChatActivityVisible;
        isChatActivityVisible = false;
        if (previousState) {
            Log.d(TAG, "🔄 RESET: Chat visibility reset to false");
        }
    }
    
    /**
     * Call this when ChatActivity becomes visible (onResume)
     */
    public static void setChatActivityVisible(boolean visible) {
        boolean previousState = isChatActivityVisible;
        isChatActivityVisible = visible;
        
        if (previousState != visible) {
            if (visible) {
                Log.d(TAG, "🔵 ========================================");
                Log.d(TAG, "🔵 CHAT IS NOW VISIBLE");
                Log.d(TAG, "🔵 Push notifications will be SUPPRESSED");
                Log.d(TAG, "🔵 ========================================");
            } else {
                Log.d(TAG, "🔴 ========================================");
                Log.d(TAG, "🔴 CHAT IS NOW NOT VISIBLE");
                Log.d(TAG, "🔴 Push notifications will be SHOWN");
                Log.d(TAG, "🔴 ========================================");
            }
        }
    }
    
    /**
     * Check if ChatActivity is currently visible
     * @return true if user is currently viewing the chat
     */
    public static boolean isChatActivityVisible() {
        Log.d(TAG, "📊 Checking chat visibility: " + isChatActivityVisible);
        return isChatActivityVisible;
    }
}

