package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * ChatBadgeManager - Centralized manager for chat notification badges across all activities
 * 
 * This class handles:
 * - Counting ALL admin chat messages (not just unread) like alerts badge
 * - Tracking last viewed message count using SharedPreferences
 * - Updating badge UI across different activities
 * - Real-time badge updates
 * - Badge clearing when user opens chat
 */
public class ChatBadgeManager {
    
    private static final String TAG = "ChatBadgeManager";
    private static final String PREFS_NAME = "ChatBadgePrefs";
    private static final String KEY_LAST_VIEWED_MESSAGE_COUNT = "last_viewed_message_count";
    private static final String KEY_TOTAL_MESSAGE_COUNT = "total_message_count";
    
    private static ChatBadgeManager instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    // Singleton pattern
    private ChatBadgeManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    
    public static synchronized ChatBadgeManager getInstance() {
        if (instance == null) {
            instance = new ChatBadgeManager();
        }
        return instance;
    }
    
    /**
     * Update chat badge for a given TextView
     * ✅ NEW: Counts ALL admin messages (not just unread) like alerts badge
     * Shows badge count = current total admin messages - last viewed count
     * 
     * @param context The activity context
     * @param chatBadgeView The TextView to update with badge count
     */
    public void updateChatBadge(Context context, TextView chatBadgeView) {
        Log.d(TAG, "=== updateChatBadge() called ===");
        
        if (chatBadgeView == null) {
            Log.e(TAG, "❌ Chat badge view is NULL! Check findViewById() in your activity");
            return;
        }
        
        Log.d(TAG, "✅ Chat badge view is NOT null");
        
        // ✅ CRITICAL: Don't show badge if user is currently viewing chat
        if (ChatActivityTracker.isChatActivityVisible()) {
            chatBadgeView.setVisibility(View.GONE);
            Log.d(TAG, "🔵 User is viewing chat - hiding badge");
            return;
        }
        
        Log.d(TAG, "✅ User is NOT viewing chat - can show badge");
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ No authenticated user, cannot update chat badge");
            chatBadgeView.setVisibility(View.GONE);
            return;
        }
        
        String userId = currentUser.getUid();
        
        Log.d(TAG, "✅ Current user ID: " + userId);
        Log.d(TAG, "🔍 Querying Firestore for ALL admin messages...");
        Log.d(TAG, "🔍 Query: userId==" + userId + " AND isUser==false");
        
        // ✅ FIXED: Query Firestore for ALL messages for this user, then filter for admin messages
        // Web app messages might not have "isUser" field, so we filter client-side
        // Similar to how alerts badge counts all announcements
        db.collection("chat_messages")
            .whereEqualTo("userId", userId)  // User's chat room (all messages)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Filter for admin messages (messages NOT from current user)
                int currentTotalCount = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    // Check if message is from admin (not from current user)
                    String senderId = doc.getString("senderId");
                    Boolean isUser = doc.getBoolean("isUser");
                    
                    // Web app format: check senderId != userId (admin message)
                    // Mobile app format: check isUser == false (admin message)
                    boolean isAdminMessage = false;
                    if (senderId != null && !senderId.equals(userId)) {
                        // Message sender is not the current user = admin message
                        isAdminMessage = true;
                        Log.d(TAG, "✅ Admin message found by senderId: " + senderId);
                    } else if (isUser != null && !isUser) {
                        // Mobile app format: isUser == false = admin message
                        isAdminMessage = true;
                        Log.d(TAG, "✅ Admin message found by isUser: false");
                    }
                    
                    if (isAdminMessage) {
                        currentTotalCount++;
                    }
                }
                
                Log.d(TAG, "📥 Firestore query completed successfully");
                Log.d(TAG, "📊 Total documents queried: " + queryDocumentSnapshots.size());
                Log.d(TAG, "📊 Total admin messages in database: " + currentTotalCount);
                
                // Get last viewed count from SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                final int lastViewedCountInitial = prefs.getInt(KEY_LAST_VIEWED_MESSAGE_COUNT, 0);
                
                Log.d(TAG, "📊 Last viewed message count: " + lastViewedCountInitial);
                
                // Calculate badge count: new messages since last view
                int badgeCountCalc = currentTotalCount - lastViewedCountInitial;
                
                // Ensure badge count is never negative
                final int badgeCount;
                final int lastViewedCount;
                if (badgeCountCalc < 0) {
                    badgeCount = 0;
                    // Update last viewed count if it's higher than current total
                    prefs.edit().putInt(KEY_LAST_VIEWED_MESSAGE_COUNT, currentTotalCount).apply();
                    lastViewedCount = currentTotalCount;
                } else {
                    badgeCount = badgeCountCalc;
                    lastViewedCount = lastViewedCountInitial;
                }
                
                // Save current total count
                prefs.edit().putInt(KEY_TOTAL_MESSAGE_COUNT, currentTotalCount).apply();
                
                Log.d(TAG, "📊 Badge count (new messages): " + badgeCount);
                Log.d(TAG, "📊 Calculation: " + currentTotalCount + " - " + lastViewedCount + " = " + badgeCount);
                
                // ✅ FIXED: Update badge on UI thread (exactly like alerts badge)
                // Make badgeCount final for lambda
                final int finalBadgeCount = badgeCount;
                
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (chatBadgeView != null) {
                            if (finalBadgeCount > 0) {
                                // Show badge with count (exactly like alerts badge)
                                chatBadgeView.setText(String.valueOf(finalBadgeCount));
                                chatBadgeView.setVisibility(View.VISIBLE);
                                Log.d(TAG, "✅✅✅ Chat badge SHOWN with count: " + finalBadgeCount + " ✅✅✅");
                                Log.d(TAG, "✅ Badge visibility: VISIBLE");
                            } else {
                                // Hide badge
                                chatBadgeView.setVisibility(View.GONE);
                                chatBadgeView.setText("0");
                                Log.d(TAG, "⚪ No new messages - badge HIDDEN");
                            }
                        }
                    });
                } else {
                    // Fallback if not an Activity
                    if (chatBadgeView != null) {
                        if (finalBadgeCount > 0) {
                            chatBadgeView.setText(String.valueOf(finalBadgeCount));
                            chatBadgeView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "✅ Chat badge SHOWN with count: " + finalBadgeCount);
                        } else {
                            chatBadgeView.setVisibility(View.GONE);
                            chatBadgeView.setText("0");
                            Log.d(TAG, "⚪ No new messages - badge HIDDEN");
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ ERROR fetching admin messages from Firestore");
                Log.e(TAG, "❌ Error message: " + e.getMessage(), e);
                chatBadgeView.setVisibility(View.GONE);
                
                // Show user-friendly message
                if (context != null) {
                    android.widget.Toast.makeText(context, 
                        "Error loading chat notifications: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        
        Log.d(TAG, "=== updateChatBadge() query sent ===");
    }
    
    /**
     * Count unread messages for current user
     * Returns the count via callback
     * 
     * @param callback Callback to receive the count
     */
    public void countUnreadMessages(UnreadCountCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot count unread messages");
            callback.onCountReceived(0);
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Query Firestore for unread messages
        db.collection("chat_messages")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isUser", false)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                Log.d(TAG, "Counted " + count + " unread messages");
                callback.onCountReceived(count);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error counting unread messages: " + e.getMessage(), e);
                callback.onCountReceived(0);
            });
    }
    
    /**
     * ✅ NEW: Mark all messages as viewed and clear badge count
     * Called when user opens ChatActivity - similar to how alerts badge works
     * This updates SharedPreferences to mark all current messages as viewed
     * 
     * @param context The activity context
     */
    public void markMessagesAsViewed(Context context) {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user, cannot mark messages as viewed");
                return;
            }
            
            String userId = currentUser.getUid();
            
            Log.d(TAG, "✅ Marking all messages as viewed for user: " + userId);
            
            // Get current total count of admin messages
            // ✅ FIXED: Query all messages and filter for admin messages (web app compatibility)
            // Web app messages might not have "isUser" field, so we filter client-side
            db.collection("chat_messages")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Filter for admin messages (messages NOT from current user)
                    int currentTotalCount = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Check if message is from admin (not from current user)
                        String senderId = doc.getString("senderId");
                        Boolean isUser = doc.getBoolean("isUser");
                        
                        // Web app format: check senderId != userId (admin message)
                        // Mobile app format: check isUser == false (admin message)
                        boolean isAdminMessage = false;
                        if (senderId != null && !senderId.equals(userId)) {
                            // Message sender is not the current user = admin message
                            isAdminMessage = true;
                        } else if (isUser != null && !isUser) {
                            // Mobile app format: isUser == false = admin message
                            isAdminMessage = true;
                        }
                        
                        if (isAdminMessage) {
                            currentTotalCount++;
                        }
                    }
                    
                    Log.d(TAG, "📊 Total documents queried: " + queryDocumentSnapshots.size());
                    
                    Log.d(TAG, "📊 Current total admin messages: " + currentTotalCount);
                    
                    // Update SharedPreferences: mark all current messages as viewed
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                        .putInt(KEY_LAST_VIEWED_MESSAGE_COUNT, currentTotalCount)
                        .putInt(KEY_TOTAL_MESSAGE_COUNT, currentTotalCount)
                        .commit(); // Use commit for immediate write
                    
                    Log.d(TAG, "✅✅✅ All messages marked as viewed - lastViewedCount updated to: " + currentTotalCount);
                    Log.d(TAG, "✅✅✅ Badge will now show 0 (no new messages)");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting current message count: " + e.getMessage(), e);
                });
            
        } catch (Exception e) {
            Log.e(TAG, "Error marking messages as viewed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear all chat badges (call when user opens chat)
     */
    public void clearBadges() {
        Log.d(TAG, "Clearing all chat badges");
        // This will be handled by updateChatBadge() checking ChatActivityTracker
    }
    
    /**
     * Mark all messages as read for current user
     * This should be called when user opens ChatActivity
     */
    public void markAllMessagesAsRead() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot mark messages as read");
            return;
        }
        
        String userId = currentUser.getUid();
        
        Log.d(TAG, "Marking all messages as read for user: " + userId);
        
        // Find all unread messages from admin
        db.collection("chat_messages")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isUser", false)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    // Mark each message as read
                    db.collection("chat_messages")
                        .document(doc.getId())
                        .update("isRead", true)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Marked message as read: " + doc.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Error marking message as read: " + e.getMessage(), e);
                        });
                    count++;
                }
                
                Log.d(TAG, "Marked " + count + " messages as read");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error marking messages as read: " + e.getMessage(), e);
            });
    }
    
    /**
     * ✅ UPDATED: Setup real-time listener for ALL admin messages
     * This will automatically update the badge when new messages arrive
     * Now counts ALL admin messages (not just unread) like alerts badge
     * 
     * @param context The activity context
     * @param chatBadgeView The TextView to update
     * @return ListenerRegistration that should be removed in onDestroy()
     */
    public com.google.firebase.firestore.ListenerRegistration setupRealtimeBadgeListener(
            Context context, TextView chatBadgeView) {
        
        if (chatBadgeView == null) {
            Log.w(TAG, "Chat badge view is null, cannot setup listener");
            return null;
        }
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user, cannot setup listener");
            return null;
        }
        
        String userId = currentUser.getUid();
        
        Log.d(TAG, "Setting up real-time badge listener for user: " + userId);
        
        // ✅ FIXED: Setup real-time listener for ALL messages, then filter for admin messages
        // Web app messages might not have "isUser" field, so we filter client-side
        return db.collection("chat_messages")
            .whereEqualTo("userId", userId)  // All messages for this user
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error in real-time listener: " + error.getMessage(), error);
                    return;
                }
                
                if (snapshots != null) {
                    // Filter for admin messages (messages NOT from current user)
                    int currentTotalCount = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        // Check if message is from admin (not from current user)
                        String senderId = doc.getString("senderId");
                        Boolean isUser = doc.getBoolean("isUser");
                        
                        // Web app format: check senderId != userId (admin message)
                        // Mobile app format: check isUser == false (admin message)
                        boolean isAdminMessage = false;
                        if (senderId != null && !senderId.equals(userId)) {
                            // Message sender is not the current user = admin message
                            isAdminMessage = true;
                        } else if (isUser != null && !isUser) {
                            // Mobile app format: isUser == false = admin message
                            isAdminMessage = true;
                        }
                        
                        if (isAdminMessage) {
                            currentTotalCount++;
                        }
                    }
                    
                    Log.d(TAG, "📡 Real-time badge update: " + snapshots.size() + " total messages, " + currentTotalCount + " admin messages");
                    
                    // ✅ CRITICAL: Don't show badge if user is viewing chat
                    if (ChatActivityTracker.isChatActivityVisible()) {
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                chatBadgeView.setVisibility(View.GONE);
                            });
                        } else {
                            chatBadgeView.setVisibility(View.GONE);
                        }
                        Log.d(TAG, "🔵 User is viewing chat - hiding badge (real-time)");
                        return;
                    }
                    
                    // Get last viewed count from SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    final int lastViewedCount = prefs.getInt(KEY_LAST_VIEWED_MESSAGE_COUNT, 0);
                    
                    // Calculate badge count: new messages since last view
                    final int badgeCount = Math.max(0, currentTotalCount - lastViewedCount);
                    
                    // Save current total count
                    prefs.edit().putInt(KEY_TOTAL_MESSAGE_COUNT, currentTotalCount).apply();
                    
                    Log.d(TAG, "📡 Real-time badge count: " + badgeCount + " (total: " + currentTotalCount + ", last viewed: " + lastViewedCount + ")");
                    
                    // Make badgeCount final for lambda (exactly like alerts badge)
                    final int finalBadgeCount = badgeCount;
                    
                    // Update badge on UI thread (exactly like alerts badge)
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (chatBadgeView != null) {
                                if (finalBadgeCount > 0) {
                                    chatBadgeView.setText(String.valueOf(finalBadgeCount));
                                    chatBadgeView.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "✅✅✅ Real-time badge SHOWN with count: " + finalBadgeCount + " ✅✅✅");
                                } else {
                                    chatBadgeView.setVisibility(View.GONE);
                                    chatBadgeView.setText("0");
                                    Log.d(TAG, "⚪ Real-time badge HIDDEN - no new messages");
                                }
                            }
                        });
                    } else {
                        // Fallback if not an Activity
                        if (chatBadgeView != null) {
                            if (finalBadgeCount > 0) {
                                chatBadgeView.setText(String.valueOf(finalBadgeCount));
                                chatBadgeView.setVisibility(View.VISIBLE);
                            } else {
                                chatBadgeView.setVisibility(View.GONE);
                                chatBadgeView.setText("0");
                            }
                        }
                    }
                }
            });
    }
    
    /**
     * Enhanced query that shows ALL messages and why they're counted or not
     * This helps debug why badge isn't showing even when notifications arrive
     * 
     * @param context The activity context
     * @param chatBadgeView The TextView to update
     */
    public void updateChatBadgeFlexible(Context context, TextView chatBadgeView) {
        Log.d(TAG, "");
        Log.d(TAG, "========================================");
        Log.d(TAG, "=== ENHANCED BADGE QUERY (DEBUG) ===");
        Log.d(TAG, "========================================");
        
        if (chatBadgeView == null) {
            Log.e(TAG, "❌ Badge view is NULL");
            return;
        }
        
        if (ChatActivityTracker.isChatActivityVisible()) {
            chatBadgeView.setVisibility(View.GONE);
            Log.d(TAG, "🔵 User is viewing chat - badge hidden");
            return;
        }
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ No authenticated user");
            chatBadgeView.setVisibility(View.GONE);
            return;
        }
        
        String userId = currentUser.getUid();
        Log.d(TAG, "✅ Current user ID: " + userId);
        Log.d(TAG, "");
        Log.d(TAG, "🔍 Querying ALL messages for this user...");
        
        // Query ALL messages for this user (no filters)
        db.collection("chat_messages")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalMessages = queryDocumentSnapshots.size();
                int unreadFromAdmin = 0;
                
                Log.d(TAG, "");
                Log.d(TAG, "📊 TOTAL messages in database: " + totalMessages);
                Log.d(TAG, "");
                
                if (totalMessages == 0) {
                    Log.d(TAG, "⚠️ NO MESSAGES FOUND!");
                    Log.d(TAG, "⚠️ This means:");
                    Log.d(TAG, "   - Messages might have different userId");
                    Log.d(TAG, "   - Or no messages exist yet");
                    Log.d(TAG, "");
                }
                
                // Examine each message in detail
                int messageNum = 0;
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    messageNum++;
                    
                    // Get all fields
                    String messageId = doc.getId();
                    String content = doc.getString("content");
                    String message = doc.getString("message"); // Web app might use "message" instead
                    Boolean isUser = doc.getBoolean("isUser");
                    Boolean isRead = doc.getBoolean("isRead");
                    String docUserId = doc.getString("userId");
                    String senderId = doc.getString("senderId");
                    Object timestamp = doc.get("timestamp");
                    
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    Log.d(TAG, "📝 Message #" + messageNum + " of " + totalMessages);
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    Log.d(TAG, "Message ID: " + messageId);
                    Log.d(TAG, "Content: " + (content != null ? content : message));
                    Log.d(TAG, "");
                    
                    // Check userId
                    Log.d(TAG, "Field: userId");
                    Log.d(TAG, "  Value: " + docUserId);
                    if (docUserId != null && docUserId.equals(userId)) {
                        Log.d(TAG, "  ✅ MATCHES current user");
                    } else {
                        Log.d(TAG, "  ❌ WRONG! Should be: " + userId);
                    }
                    Log.d(TAG, "");
                    
                    // Check isUser field
                    Log.d(TAG, "Field: isUser");
                    Log.d(TAG, "  Value: " + isUser);
                    if (isUser == null) {
                        Log.d(TAG, "  ❌ MISSING! Field doesn't exist");
                        Log.d(TAG, "  💡 Web app must add: isUser: false");
                    } else if (isUser) {
                        Log.d(TAG, "  ℹ️  TRUE = message from user (not admin)");
                        Log.d(TAG, "  ℹ️  Not counted for badge");
                    } else {
                        Log.d(TAG, "  ✅ FALSE = message from admin");
                        Log.d(TAG, "  ✅ This is good for badge!");
                    }
                    Log.d(TAG, "");
                    
                    // Check isRead field
                    Log.d(TAG, "Field: isRead");
                    Log.d(TAG, "  Value: " + isRead);
                    if (isRead == null) {
                        Log.d(TAG, "  ❌ MISSING! Field doesn't exist");
                        Log.d(TAG, "  💡 Web app must add: isRead: false");
                    } else if (isRead) {
                        Log.d(TAG, "  ℹ️  TRUE = already read");
                        Log.d(TAG, "  ℹ️  Not counted for badge");
                    } else {
                        Log.d(TAG, "  ✅ FALSE = unread message");
                        Log.d(TAG, "  ✅ This is good for badge!");
                    }
                    Log.d(TAG, "");
                    
                    // Check senderId
                    Log.d(TAG, "Field: senderId");
                    Log.d(TAG, "  Value: " + senderId);
                    Log.d(TAG, "");
                    
                    // Check timestamp
                    Log.d(TAG, "Field: timestamp");
                    Log.d(TAG, "  Value: " + timestamp);
                    Log.d(TAG, "  Type: " + (timestamp != null ? timestamp.getClass().getSimpleName() : "null"));
                    Log.d(TAG, "");
                    
                    // Final decision for this message
                    boolean countThis = false;
                    if (isUser != null && !isUser && isRead != null && !isRead) {
                        countThis = true;
                        unreadFromAdmin++;
                        Log.d(TAG, "✅ ✅ ✅ THIS MESSAGE COUNTS FOR BADGE! ✅ ✅ ✅");
                    } else {
                        Log.d(TAG, "❌ This message DOES NOT count because:");
                        if (isUser == null) {
                            Log.d(TAG, "   • isUser field is missing");
                        } else if (isUser) {
                            Log.d(TAG, "   • isUser = true (message from user, not admin)");
                        }
                        if (isRead == null) {
                            Log.d(TAG, "   • isRead field is missing");
                        } else if (isRead) {
                            Log.d(TAG, "   • isRead = true (already read)");
                        }
                    }
                    Log.d(TAG, "");
                }
                
                Log.d(TAG, "");
                Log.d(TAG, "========================================");
                Log.d(TAG, "=== FINAL RESULTS ===");
                Log.d(TAG, "========================================");
                Log.d(TAG, "📊 Total messages in database: " + totalMessages);
                Log.d(TAG, "📊 Unread messages from admin: " + unreadFromAdmin);
                Log.d(TAG, "");
                
                if (unreadFromAdmin > 0) {
                    chatBadgeView.setText(String.valueOf(unreadFromAdmin));
                    chatBadgeView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "✅ ✅ ✅ BADGE SHOWN WITH COUNT: " + unreadFromAdmin + " ✅ ✅ ✅");
                } else {
                    chatBadgeView.setVisibility(View.GONE);
                    if (totalMessages == 0) {
                        Log.d(TAG, "⚪ Badge hidden - no messages found");
                        Log.d(TAG, "💡 Check if messages have correct userId field");
                    } else {
                        Log.d(TAG, "⚪ Badge hidden - messages found but none are unread from admin");
                        Log.d(TAG, "💡 Check message fields above to see why they don't count");
                    }
                }
                
                Log.d(TAG, "========================================");
                Log.d(TAG, "");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "");
                Log.e(TAG, "❌ ❌ ❌ ERROR QUERYING FIRESTORE ❌ ❌ ❌");
                Log.e(TAG, "Error: " + e.getMessage(), e);
                Log.e(TAG, "");
                chatBadgeView.setVisibility(View.GONE);
                
                if (context != null) {
                    android.widget.Toast.makeText(context,
                        "Error loading chat badge: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
                }
            });
    }
    
    /**
     * Callback interface for unread count
     */
    public interface UnreadCountCallback {
        void onCountReceived(int count);
    }
}

