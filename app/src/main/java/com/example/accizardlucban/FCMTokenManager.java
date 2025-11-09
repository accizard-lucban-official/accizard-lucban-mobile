package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages FCM (Firebase Cloud Messaging) tokens
 * Handles token generation, storage, and synchronization with Firestore
 */
public class FCMTokenManager {
    
    private static final String TAG = "FCMTokenManager";
    private static final String PREFS_NAME = "FCMTokenPrefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    
    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final SharedPreferences prefs;
    
    public FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Initialize FCM token - get token and save to Firestore
     * Call this when user logs in or app starts with authenticated user
     */
    public void initializeFCMToken() {
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, skipping FCM token initialization");
            return;
        }
        
        Log.d(TAG, "Initializing FCM token for user: " + currentUser.getUid());
        
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to get FCM token", task.getException());
                            return;
                        }
                        
                        // Get the FCM token
                        String token = task.getResult();
                        if (token != null) {
                            Log.d(TAG, "✅ FCM token obtained: " + token);
                            saveFCMTokenToFirestore(token);
                        } else {
                            Log.e(TAG, "FCM token is null");
                        }
                    }
                });
    }
    
    /**
     * Save FCM token to Firestore
     * Stores in users/{userId}/fcmToken field
     * Uses query to find user document by firebaseUid field
     */
    public void saveFCMTokenToFirestore(String token) {
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot save FCM token to Firestore");
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Save to local SharedPreferences first
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token saved to SharedPreferences");
        
        // Save to Firestore
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("lastUpdated", System.currentTimeMillis());
        
        Log.d(TAG, "Attempting to save FCM token for Firebase Auth UID: " + userId);
        
        // ✅ UPDATED: Try direct document ID approach first (for new users created after this update)
        // Then fall back to query by firebaseUid field (for existing users)
        firestore.collection("users")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ FCM token saved to Firestore using document ID: " + userId);
                    Log.d(TAG, "Token: " + token);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Document ID approach failed, trying query by firebaseUid field (for existing users)");
                    
                    // Fallback: Query by firebaseUid field for existing users
                    firestore.collection("users")
                            .whereEqualTo("firebaseUid", userId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    // Found user document by firebaseUid field (existing user)
                                    String documentId = querySnapshot.getDocuments().get(0).getId();
                                    Log.d(TAG, "Found existing user document with ID: " + documentId);
                                    
                                    // Update the found document
                                    firestore.collection("users")
                                            .document(documentId)
                                            .update(tokenData)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d(TAG, "✅ FCM token saved to Firestore for existing user document: " + documentId);
                                                Log.d(TAG, "Token: " + token);
                                            })
                                            .addOnFailureListener(e2 -> {
                                                Log.e(TAG, "Failed to update FCM token for existing user: " + e2.getMessage(), e2);
                                            });
                                } else {
                                    // User document not found at all - this shouldn't happen
                                    Log.e(TAG, "User document not found in Firestore. User may need to complete registration.");
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Failed to query users collection: " + e2.getMessage(), e2);
                            });
                });
    }
    
    /**
     * Get the current FCM token from SharedPreferences
     */
    public String getCurrentToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
    
    /**
     * Delete FCM token from Firestore
     * Call this when user logs out
     */
    public void deleteFCMTokenFromFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot delete FCM token");
            return;
        }
        
        String userId = currentUser.getUid();
        
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", null);
        
        firestore.collection("users")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ FCM token removed from Firestore for user: " + userId);
                    
                    // Clear from SharedPreferences
                    prefs.edit().remove(KEY_FCM_TOKEN).apply();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete FCM token from Firestore: " + e.getMessage(), e);
                });
    }
    
    /**
     * Refresh FCM token
     * Call this when token is refreshed by Firebase
     */
    public void refreshFCMToken(String newToken) {
        Log.d(TAG, "FCM token refreshed: " + newToken);
        saveFCMTokenToFirestore(newToken);
    }
    
    /**
     * Check if FCM token exists in SharedPreferences
     */
    public boolean hasToken() {
        return getCurrentToken() != null;
    }
}

