package com.example.accizardlucban;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.PopupMenu;
import android.widget.ImageButton;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.io.File;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.widget.TextView.OnEditorActionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;
import android.os.Build;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    private static final String KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT = "last_viewed_announcement_count";
    private static final String KEY_ADMIN_DELETION_DETECTED = "admin_deletion_detected";
    private static final String KEY_ADMIN_DELETION_TIMESTAMP = "admin_deletion_timestamp";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageView sendButton, backButton, callButton;
    private LinearLayout homeTab, chatTab, mapTab, alertsTab;
    private FrameLayout reportTab; // Changed to FrameLayout for circular button design
    private TextView statusText;
    private ImageButton addActionButton;
    private LinearLayout inputContainer;
    private TextView alertsBadgeChat;
    private SharedPreferences sharedPreferences;
    
    private static final int CALL_PERMISSION_REQUEST_CODE = 101;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messagesList;
    private java.util.Set<String> loadedMessageIds; // Track loaded message IDs to prevent duplicates
    private int previousMessageCount = 0; // Track previous message count to detect bulk deletion
    private long lastDeletionCheckTime = 0; // Track when we last checked for bulk deletion

    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;
    private static final int VIDEO_REQUEST_CODE = 203;
    private static final int AUDIO_REQUEST_CODE = 204;
    private static final int CAMERA_PERMISSION_CODE = 205;
    private static final int STORAGE_PERMISSION_CODE = 206;
    private static final int AUDIO_PERMISSION_CODE = 207;
    private Uri photoUri;
    private FirebaseAuth mAuth;
    
    // Firestore for chat messages
    private FirebaseFirestore db;
    private String chatRoomId; // Will be set to user's UID for private chats
    private com.google.firebase.firestore.ListenerRegistration messageListener;
    
    // User profile picture URL
    private String userProfilePictureUrl = null;
    
    // User phone number
    private String userPhoneNumber = null;
    
    // Keyboard handling
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private boolean isKeyboardVisible = false;
    private int initialHeight = 0;
    private Handler keyboardHandler = new Handler(Looper.getMainLooper());
    private Runnable keyboardRunnable;
    
    // Chat badge count
    private static final int CHAT_BADGE_NOTIFICATION_ID = 999;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ChatActivity onCreate started");

        try {
            setContentView(R.layout.activity_chat);
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is authenticated
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Please sign in to access chat", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Initialize Firestore
            db = FirebaseFirestore.getInstance();
            
            // Set chatRoomId to current user's UID for private chat
            chatRoomId = currentUser.getUid();
            Log.d(TAG, "Chat room ID set to: " + chatRoomId);
            
            // Initialize notification manager for chat badge
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Initialize SharedPreferences for badge
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            
            // Keep screen on for better chat experience
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            
            initializeViews();
            setupRecyclerView();
            setupClickListeners();
            setupKeyboardListener();
            setupInputHandling();
            
            // Initialize chat room metadata
            initializeChatRoomMetadata();
            
            // Load messages from Firestore
            loadMessagesFromFirestore();
            
            // Setup real-time message listener
            setupRealtimeMessageListener();
            
            // Load user's profile picture URL for messages (not for the call button)
            loadUserProfilePictureUrl();
            
            // Load user's phone number from Firestore
            loadUserPhoneNumber();
            
            // ✅ CRITICAL FIX: Mark chat as visible IMMEDIATELY in onCreate
            ChatActivityTracker.setChatActivityVisible(true);
            Log.d(TAG, "🔵 CRITICAL: Chat marked as VISIBLE in onCreate - notifications SUPPRESSED");
            
            // ✅ CRITICAL FIX: Clear chat badge IMMEDIATELY when activity is created
            clearChatBadge();
            
            // Update notification badge for alerts (NOT chat)
            updateNotificationBadge();
            
            // Handle notification extras (if opened from notification)
            handleNotificationExtras();
            
            Log.d(TAG, "ChatActivity onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in ChatActivity onCreate", e);
            Toast.makeText(this, "Error loading chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            Log.d(TAG, "Initializing views");

            messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
            messageInput = findViewById(R.id.messageInput);
            sendButton = findViewById(R.id.sendButton);
            backButton = findViewById(R.id.backButton);
            statusText = findViewById(R.id.statusText);
            callButton = findViewById(R.id.profileButton); // This is the call button in chat
            addActionButton = findViewById(R.id.addActionButton);
            inputContainer = findViewById(R.id.inputContainer);

            // Bottom navigation
            homeTab = findViewById(R.id.homeTab);
            chatTab = findViewById(R.id.chatTab);
            reportTab = findViewById(R.id.reportTab);
            mapTab = findViewById(R.id.mapTab);
            alertsTab = findViewById(R.id.alertsTab);
            alertsBadgeChat = findViewById(R.id.alerts_badge_chat);
            
            // Register badge with AnnouncementNotificationManager so it gets updated when alerts are viewed
            if (alertsBadgeChat != null) {
                AnnouncementNotificationManager.getInstance().registerBadge("ChatActivity", alertsBadgeChat);
                Log.d(TAG, "✅ ChatActivity badge registered with AnnouncementNotificationManager");
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing chat views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        try {
            Log.d(TAG, "Setting up RecyclerView");

            messagesList = new ArrayList<>();
            loadedMessageIds = new java.util.HashSet<>(); // Initialize message ID tracker
            chatAdapter = new ChatAdapter(messagesList);

            if (messagesRecyclerView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setStackFromEnd(true); // Stack from end to show latest messages at bottom
                layoutManager.setReverseLayout(false); // Keep normal order (old to new)
                messagesRecyclerView.setLayoutManager(layoutManager);
                messagesRecyclerView.setAdapter(chatAdapter);
                
                // Allow scrolling from the very top to show logo background
                messagesRecyclerView.setClipToPadding(false);
                
                Log.d(TAG, "RecyclerView setup completed with stackFromEnd=true");
            } else {
                Log.e(TAG, "messagesRecyclerView is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            Toast.makeText(this, "Error setting up chat list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInputHandling() {
        try {
            // Set up input method action
            messageInput.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                        sendMessage();
                        return true;
                    }
                    return false;
                }
            });

            // Focus on input when activity starts
            messageInput.requestFocus();
            
            Log.d(TAG, "Input handling setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up input handling", e);
        }
    }

    private void setupKeyboardListener() {
        try {
            Log.d(TAG, "Setting up keyboard listener");
            
            final View rootView = findViewById(android.R.id.content);
            
            keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int currentHeight = rootView.getHeight();
                    
                    if (initialHeight == 0) {
                        initialHeight = currentHeight;
                        return;
                    }
                    
                    int heightDifference = initialHeight - currentHeight;
                    int threshold = (int) (200 * getResources().getDisplayMetrics().density);
                    
                    boolean wasKeyboardVisible = isKeyboardVisible;
                    isKeyboardVisible = heightDifference > threshold;
                    
                    if (isKeyboardVisible && !wasKeyboardVisible) {
                        Log.d(TAG, "Keyboard became visible - using adjustPan mode");
                        onKeyboardShown();
                    } else if (!isKeyboardVisible && wasKeyboardVisible) {
                        Log.d(TAG, "Keyboard became hidden");
                        onKeyboardHidden();
                    }
                }
            };
            
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
            Log.d(TAG, "Keyboard listener setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up keyboard listener", e);
        }
    }

    private void onKeyboardShown() {
        try {
            // Cancel any pending keyboard runnable
            if (keyboardRunnable != null) {
                keyboardHandler.removeCallbacks(keyboardRunnable);
            }
            
            // Input container stays fixed at bottom - keyboard will adjust window automatically
            // Do NOT modify elevation, margins, or bringToFront - keep it fixed
            
            // Scroll to bottom with a slight delay to ensure layout is complete
            keyboardRunnable = new Runnable() {
                @Override
                public void run() {
                    scrollToBottomSmooth();
                }
            };
            keyboardHandler.postDelayed(keyboardRunnable, 200);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling keyboard shown", e);
        }
    }

    private void onKeyboardHidden() {
        try {
            // Input container stays fixed - no modifications needed
            Log.d(TAG, "Keyboard hidden");
        } catch (Exception e) {
            Log.e(TAG, "Error handling keyboard hidden", e);
        }
    }

    private void scrollToBottomSmooth() {
        try {
            if (messagesRecyclerView != null && messagesList != null && !messagesList.isEmpty()) {
                final int lastPosition = messagesList.size() - 1;
                messagesRecyclerView.post(() -> {
                    try {
                        messagesRecyclerView.smoothScrollToPosition(lastPosition);
                        Log.d(TAG, "Smooth scrolled to position: " + lastPosition);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in smooth scroll", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scrolling to bottom smooth", e);
        }
    }

    private void scrollToBottom() {
        try {
            if (messagesRecyclerView != null && messagesList != null && !messagesList.isEmpty()) {
                final int lastPosition = messagesList.size() - 1;
                messagesRecyclerView.post(() -> {
                    try {
                        messagesRecyclerView.scrollToPosition(lastPosition);
                        Log.d(TAG, "Scrolled to position: " + lastPosition);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in scroll", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scrolling to bottom", e);
        }
    }

    private void setupClickListeners() {
        try {
            Log.d(TAG, "Setting up click listeners");

            // Add Action Button (shows popup menu)
            if (addActionButton != null) {
                addActionButton.setOnClickListener(v -> showActionPopupMenu(v));
            }

            // Back button
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    Log.d(TAG, "Back button clicked");
                    hideKeyboard();
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                });
            }

            // Send button
            if (sendButton != null) {
                sendButton.setOnClickListener(v -> {
                    Log.d(TAG, "Send button clicked");
                    sendMessage();
                });
            }
            
            // Call button
            if (callButton != null) {
                callButton.setOnClickListener(v -> {
                    Log.d(TAG, "Call button clicked");
                    makeEmergencyCall();
                });
            }

            // Bottom navigation tabs
            setupBottomNavigationListeners();

            Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding keyboard", e);
        }
    }

    private void setupBottomNavigationListeners() {
        // Home tab
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                Log.d(TAG, "Home tab clicked");
                try {
                    hideKeyboard();
                    Intent intent = new Intent(ChatActivity.this, MainDashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to home", e);
                    Toast.makeText(ChatActivity.this, "Error navigating to home: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Chat tab
        if (chatTab != null) {
            chatTab.setOnClickListener(v -> {
                Log.d(TAG, "Chat tab clicked");
                Toast.makeText(ChatActivity.this, "Already on Chat", Toast.LENGTH_SHORT).show();
            });
        }

        // Report tab
        if (reportTab != null) {
            reportTab.setOnClickListener(v -> {
                Log.d(TAG, "Report tab clicked - navigating to ReportSubmissionActivity");
                try {
                    hideKeyboard();
                    Intent intent = new Intent(ChatActivity.this, ReportSubmissionActivity.class);
                    intent.putExtra("from_activity", "ChatActivity");
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    Log.d(TAG, "Successfully navigated to ReportSubmissionActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ReportSubmissionActivity", e);
                    Toast.makeText(ChatActivity.this, "Error opening report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Map tab
        if (mapTab != null) {
            mapTab.setOnClickListener(v -> {
                Log.d(TAG, "Map tab clicked");
                try {
                    hideKeyboard();
                    Intent intent = new Intent(ChatActivity.this, MapViewActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to map", e);
                    Toast.makeText(ChatActivity.this, "Map feature coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Alerts tab
        if (alertsTab != null) {
            alertsTab.setOnClickListener(v -> {
                Log.d(TAG, "Alerts tab clicked");
                try {
                    hideKeyboard();
                    Intent intent = new Intent(ChatActivity.this, AlertsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to alerts", e);
                    Toast.makeText(ChatActivity.this, "Alerts feature coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void sendMessage() {
        try {
            if (messageInput == null) {
                Log.e(TAG, "messageInput is null");
                return;
            }

            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                Log.d(TAG, "Sending message: " + message);
                
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(this, "Please sign in to send messages", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Create message data for Firestore
                String senderName = getSenderName();
                long timestamp = System.currentTimeMillis();
                
                java.util.Map<String, Object> messageData = new java.util.HashMap<>();
                messageData.put("userId", currentUser.getUid()); // User ID for filtering
                messageData.put("userPhoneNumber", userPhoneNumber); // User's phone number for reference
                messageData.put("content", message);
                messageData.put("senderId", currentUser.getUid());
                messageData.put("senderName", senderName);
                messageData.put("timestamp", timestamp);
                messageData.put("isUser", true);
                messageData.put("imageUrl", null);
                messageData.put("profilePictureUrl", userProfilePictureUrl); // Save user's profile picture URL
                messageData.put("isRead", true); // ✅ NEW: User messages are always read by default
                
                // Clear input immediately for better UX
                messageInput.setText("");
                
                // Save message to Firestore (flat structure - all messages in one collection)
                db.collection("chat_messages")
                    .add(messageData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Message sent successfully with ID: " + documentReference.getId());
                        // Update chat room metadata with last message info
                        updateChatRoomLastMessage(message, System.currentTimeMillis());
                        // Message will appear via realtime listener
                        scrollToBottomSmooth();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error sending message", e);
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        // Re-add message text if sending failed
                        messageInput.setText(message);
                    });
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getSenderName() {
        try {
            SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
            String firstName = prefs.getString("first_name", "");
            String lastName = prefs.getString("last_name", "");
            
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            } else if (!firstName.isEmpty()) {
                return firstName;
            } else if (!lastName.isEmpty()) {
                return lastName;
            } else {
                return "User";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting sender name", e);
            return "User";
        }
    }

    private void addMessage(String content, boolean isUser, String timestamp) {
        try {
            if (messagesList == null || chatAdapter == null) {
                Log.e(TAG, "messagesList or chatAdapter is null");
                return;
            }

            ChatMessage message = new ChatMessage(content, isUser, timestamp, userProfilePictureUrl);
            messagesList.add(message);
            final int insertedPosition = messagesList.size() - 1;
            chatAdapter.notifyItemInserted(insertedPosition);

            // Always scroll to bottom after adding message
            messagesRecyclerView.postDelayed(() -> {
                try {
                    messagesRecyclerView.scrollToPosition(insertedPosition);
                    Log.d(TAG, "Scrolled to newly added message at position: " + insertedPosition);
                } catch (Exception e) {
                    Log.e(TAG, "Error scrolling to new message", e);
                }
            }, 100);
        } catch (Exception e) {
            Log.e(TAG, "Error adding message", e);
            Toast.makeText(this, "Error adding message", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImageMessage(String content, boolean isUser, String timestamp, Bitmap imageBitmap) {
        try {
            if (messagesList == null || chatAdapter == null) {
                Log.e(TAG, "messagesList or chatAdapter is null");
                return;
            }

            ChatMessage message = new ChatMessage(content, isUser, timestamp, imageBitmap, userProfilePictureUrl);
            messagesList.add(message);
            final int insertedPosition = messagesList.size() - 1;
            chatAdapter.notifyItemInserted(insertedPosition);

            // Always scroll to bottom after adding image message
            messagesRecyclerView.postDelayed(() -> {
                try {
                    messagesRecyclerView.scrollToPosition(insertedPosition);
                    Log.d(TAG, "Scrolled to newly added image message at position: " + insertedPosition);
                } catch (Exception e) {
                    Log.e(TAG, "Error scrolling to image message", e);
                }
            }, 100);
        } catch (Exception e) {
            Log.e(TAG, "Error adding image message", e);
            Toast.makeText(this, "Error adding image message", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(Bitmap imageBitmap, boolean isFromCamera) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not signed in");
            Toast.makeText(this, "Please sign in to upload images", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String source = isFromCamera ? "camera" : "gallery";

        StorageHelper.uploadChatImage(userId, imageBitmap,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Log.d(TAG, "Image uploaded successfully to Firebase Storage: " + downloadUrl);
                        
                        // Save image message to Firestore
                        String senderName = getSenderName();
                        long timestamp = System.currentTimeMillis();
                        
                        java.util.Map<String, Object> messageData = new java.util.HashMap<>();
                        messageData.put("userId", userId); // User ID for filtering
                        messageData.put("userPhoneNumber", userPhoneNumber); // User's phone number for reference
                        messageData.put("content", "Sent an image");
                        messageData.put("senderId", userId);
                        messageData.put("senderName", senderName);
                        messageData.put("timestamp", timestamp);
                        messageData.put("isUser", true);
                        messageData.put("imageUrl", downloadUrl);
                        messageData.put("profilePictureUrl", userProfilePictureUrl); // Save user's profile picture URL
                        messageData.put("isRead", true); // ✅ NEW: User messages are always read by default
                        
                        // Flat structure - all messages in one collection
                        db.collection("chat_messages")
                            .add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Image message saved to Firestore with ID: " + documentReference.getId());
                                // Update chat room metadata with last message info
                                updateChatRoomLastMessage("📷 Sent an image", System.currentTimeMillis());
                                Toast.makeText(ChatActivity.this, "Image sent", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving image message to Firestore", e);
                                Toast.makeText(ChatActivity.this, "Failed to save image message", Toast.LENGTH_SHORT).show();
                            });
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error uploading image to Firebase Storage", e);
                        Toast.makeText(ChatActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadVideoToFirebase(Uri videoUri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not signed in");
            Toast.makeText(this, "Please sign in to upload videos", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        
        Toast.makeText(this, "Uploading video...", Toast.LENGTH_SHORT).show();
        
        StorageHelper.uploadChatVideo(userId, videoUri,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Log.d(TAG, "Video uploaded successfully to Firebase Storage: " + downloadUrl);
                        
                        // Save video message to Firestore
                        String senderName = getSenderName();
                        long timestamp = System.currentTimeMillis();
                        
                        java.util.Map<String, Object> messageData = new java.util.HashMap<>();
                        messageData.put("userId", userId);
                        messageData.put("userPhoneNumber", userPhoneNumber);
                        messageData.put("content", "Sent a video");
                        messageData.put("senderId", userId);
                        messageData.put("senderName", senderName);
                        messageData.put("timestamp", timestamp);
                        messageData.put("isUser", true);
                        messageData.put("videoUrl", downloadUrl);
                        messageData.put("attachmentType", "video");
                        messageData.put("profilePictureUrl", userProfilePictureUrl);
                        messageData.put("isRead", true);
                        
                        db.collection("chat_messages")
                            .add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Video message saved to Firestore with ID: " + documentReference.getId());
                                updateChatRoomLastMessage("🎥 Sent a video", System.currentTimeMillis());
                                Toast.makeText(ChatActivity.this, "Video sent", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving video message to Firestore", e);
                                Toast.makeText(ChatActivity.this, "Failed to save video message", Toast.LENGTH_SHORT).show();
                            });
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error uploading video to Firebase Storage", e);
                        Toast.makeText(ChatActivity.this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAudioToFirebase(Uri audioUri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not signed in");
            Toast.makeText(this, "Please sign in to upload audio", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        
        Toast.makeText(this, "Uploading audio...", Toast.LENGTH_SHORT).show();
        
        StorageHelper.uploadChatAudio(userId, audioUri,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Log.d(TAG, "Audio uploaded successfully to Firebase Storage: " + downloadUrl);
                        
                        // Save audio message to Firestore
                        String senderName = getSenderName();
                        long timestamp = System.currentTimeMillis();
                        
                        java.util.Map<String, Object> messageData = new java.util.HashMap<>();
                        messageData.put("userId", userId);
                        messageData.put("userPhoneNumber", userPhoneNumber);
                        messageData.put("content", "Sent an audio");
                        messageData.put("senderId", userId);
                        messageData.put("senderName", senderName);
                        messageData.put("timestamp", timestamp);
                        messageData.put("isUser", true);
                        messageData.put("audioUrl", downloadUrl);
                        messageData.put("attachmentType", "audio");
                        messageData.put("profilePictureUrl", userProfilePictureUrl);
                        messageData.put("isRead", true);
                        
                        db.collection("chat_messages")
                            .add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Audio message saved to Firestore with ID: " + documentReference.getId());
                                updateChatRoomLastMessage("🎤 Sent an audio", System.currentTimeMillis());
                                Toast.makeText(ChatActivity.this, "Audio sent", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving audio message to Firestore", e);
                                Toast.makeText(ChatActivity.this, "Failed to save audio message", Toast.LENGTH_SHORT).show();
                            });
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error uploading audio to Firebase Storage", e);
                        Toast.makeText(ChatActivity.this, "Failed to upload audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getCurrentTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("Today • h:mm a", Locale.getDefault());
            return sdf.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error getting current time", e);
            return "Now";
        }
    }

    private void showActionPopupMenu(View anchor) {
        try {
            // Inflate custom popup layout
            View popupView = LayoutInflater.from(this).inflate(R.layout.chat_action_popup_menu, null);
            
            // Create AlertDialog with custom view
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(popupView);
            
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Setup click listeners for each menu item
            LinearLayout menuTakePhoto = popupView.findViewById(R.id.menuTakePhoto);
            LinearLayout menuOpenGallery = popupView.findViewById(R.id.menuOpenGallery);
            LinearLayout menuRecordVideo = popupView.findViewById(R.id.menuRecordVideo);
            LinearLayout menuRecordAudio = popupView.findViewById(R.id.menuRecordAudio);
            
            if (menuTakePhoto != null) {
                menuTakePhoto.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                });
            }
            
            if (menuOpenGallery != null) {
                menuOpenGallery.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (checkStoragePermission()) {
                        openGallery();
                    } else {
                        requestStoragePermission();
                    }
                });
            }
            
            if (menuRecordVideo != null) {
                menuRecordVideo.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (checkCameraPermission()) {
                        openVideoRecorder();
                    } else {
                        requestCameraPermission();
                    }
                });
            }
            
            if (menuRecordAudio != null) {
                menuRecordAudio.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (checkAudioPermission()) {
                        openAudioRecorder();
                    } else {
                        requestAudioPermission();
                    }
                });
            }
            
            // Position dialog above the anchor button
            dialog.show();
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
                // Get anchor button position on screen
                int[] location = new int[2];
                anchor.getLocationOnScreen(location);
                
                // Position the popup above the button
                layoutParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
                layoutParams.x = location[0] + anchor.getWidth() / 2 - 100; // Center horizontally
                layoutParams.y = location[1] - (int)(280 * getResources().getDisplayMetrics().density); // Above the button
                window.setAttributes(layoutParams);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing action popup menu", e);
            Toast.makeText(this, "Error showing menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAudioPermission() {
        // RECORD_AUDIO is required for audio recording on all versions
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        // For Android 13+ (API 33+), we need both RECORD_AUDIO and READ_MEDIA_AUDIO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO};
            ActivityCompat.requestPermissions(this, permissions, AUDIO_PERMISSION_CODE);
        } else {
            // For older Android versions, only RECORD_AUDIO is needed
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
        }
    }

    private void openVideoRecorder() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(videoIntent, VIDEO_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Video recorder not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAudioRecorder() {
        Intent audioIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (audioIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(audioIntent, AUDIO_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Audio recorder not available", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Handle camera photo
                Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
                if (photoBitmap != null) {
                    // Only upload to Firebase - realtime listener will add it to UI (prevents duplicates)
                    uploadImageToFirebase(photoBitmap, true);
                    Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Handle gallery image
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap galleryBitmap = BitmapFactory.decodeStream(inputStream);
                        if (galleryBitmap != null) {
                            // Only upload to Firebase - realtime listener will add it to UI (prevents duplicates)
                            uploadImageToFirebase(galleryBitmap, false);
                            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading image from gallery", e);
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == VIDEO_REQUEST_CODE) {
                // Handle video recording
                Uri videoUri = data.getData();
                if (videoUri != null) {
                    uploadVideoToFirebase(videoUri);
                    Toast.makeText(this, "Uploading video...", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == AUDIO_REQUEST_CODE) {
                // Handle audio recording
                Uri audioUri = data.getData();
                if (audioUri != null) {
                    uploadAudioToFirebase(audioUri);
                    Toast.makeText(this, "Uploading audio...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called");
        hideKeyboard();
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ChatActivity onStart");
        
        // ✅ CRITICAL FIX: Mark chat as visible in onStart as well
        ChatActivityTracker.setChatActivityVisible(true);
        Log.d(TAG, "🔵 CRITICAL: Chat marked as VISIBLE in onStart - notifications SUPPRESSED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ChatActivity onResume");
        
        // ✅ CRITICAL FIX: Mark chat as visible FIRST to prevent notifications
        ChatActivityTracker.setChatActivityVisible(true);
        Log.d(TAG, "🔵 Chat is now VISIBLE - notifications will be suppressed");
        
        // ✅ NEW: Check if admin deletion was detected while chat was closed
        checkAndShowAdminDeletionToast();
        
        // ✅ CRITICAL FIX: Clear chat badge IMMEDIATELY when user opens chat
        clearChatBadge();
        
        // ✅ FIXED: Mark all messages as read FIRST (before updating badge)
        markMessagesAsRead();
        
        // Update notification badge for alerts (NOT chat)
        updateNotificationBadge();
        
        // Scroll to bottom to show latest messages
        scrollToBottomWithDelay();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ChatActivity onPause");
        
        // ✅ FIXED: Mark chat as not visible to allow notifications
        ChatActivityTracker.setChatActivityVisible(false);
        Log.d(TAG, "🔴 Chat is now NOT VISIBLE - notifications will be shown");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ChatActivity onStop");
        
        // ✅ CRITICAL FIX: Ensure chat is marked as not visible in onStop
        ChatActivityTracker.setChatActivityVisible(false);
        Log.d(TAG, "🔴 CRITICAL: Chat marked as NOT VISIBLE in onStop - notifications ALLOWED");
    }
    
    private void scrollToBottomWithDelay() {
        // Use a small delay to ensure RecyclerView is fully laid out
        if (messagesRecyclerView != null && messagesList != null && !messagesList.isEmpty()) {
            messagesRecyclerView.postDelayed(() -> {
                try {
                    int lastPosition = messagesList.size() - 1;
                    if (lastPosition >= 0) {
                        messagesRecyclerView.scrollToPosition(lastPosition);
                        Log.d(TAG, "Scrolled to last message at position: " + lastPosition);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error scrolling to bottom on resume", e);
                }
            }, 100); // 100ms delay for layout completion
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ChatActivity onDestroy");
        
        try {
            // Unregister badge from notification manager
            AnnouncementNotificationManager.getInstance().unregisterBadge("ChatActivity");
            Log.d(TAG, "ChatActivity badge unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering badge in onDestroy: " + e.getMessage(), e);
        }
        
        // Remove Firestore listener to prevent memory leaks
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
            Log.d(TAG, "Firestore message listener removed");
        }
        
        // Remove keyboard listener to prevent memory leaks
        if (keyboardLayoutListener != null) {
            try {
                final View rootView = findViewById(android.R.id.content);
                if (rootView != null) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing keyboard listener", e);
            }
        }
        
        // Remove any pending keyboard runnables
        if (keyboardRunnable != null) {
            keyboardHandler.removeCallbacks(keyboardRunnable);
        }
    }
    
    private void initializeChatRoomMetadata() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;
            
            Log.d(TAG, "Initializing chat room metadata");
            
            // Get user information
            String userName = getSenderName();
            String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "No email";
            
            // Create/update chat room metadata
            java.util.Map<String, Object> chatRoomData = new java.util.HashMap<>();
            chatRoomData.put("userId", currentUser.getUid());
            chatRoomData.put("userName", userName);
            chatRoomData.put("userEmail", userEmail);
            chatRoomData.put("userPhoneNumber", userPhoneNumber); // Include phone number in chat room metadata
            chatRoomData.put("lastAccessTime", System.currentTimeMillis());
            chatRoomData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            
            // Set chat room metadata (merge to avoid overwriting existing data)
            db.collection("chats")
                .document(chatRoomId)
                .set(chatRoomData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat room metadata initialized successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error initializing chat room metadata", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeChatRoomMetadata", e);
        }
    }
    
    private void updateChatRoomLastMessage(String messageContent, long timestamp) {
        try {
            // Update chat room with last message information
            java.util.Map<String, Object> updateData = new java.util.HashMap<>();
            updateData.put("lastMessage", messageContent);
            updateData.put("lastMessageTime", timestamp);
            updateData.put("lastMessageSenderName", getSenderName());
            
            db.collection("chats")
                .document(chatRoomId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat room last message updated");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating chat room last message", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateChatRoomLastMessage", e);
        }
    }
    
    private void loadMessagesFromFirestore() {
        try {
            Log.d(TAG, "Loading messages from Firestore (flat structure)");
            Log.d(TAG, "🔍 Searching for messages with userId: " + chatRoomId);
            
            // Flat structure: Filter by userId only (no orderBy to avoid index requirement)
            db.collection("chat_messages")
                .whereEqualTo("userId", chatRoomId) // Filter to get only this user's messages
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "📥 Query returned " + queryDocumentSnapshots.size() + " documents");
                    
                    if (messagesList != null) {
                        messagesList.clear();
                    }
                    if (loadedMessageIds != null) {
                        loadedMessageIds.clear(); // Clear message ID tracker
                    }
                    
                    // Temporary list to hold messages with timestamps
                    java.util.List<MessageWithTimestamp> tempMessages = new java.util.ArrayList<>();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String messageId = doc.getId();
                            Log.d(TAG, "📄 Processing document: " + messageId);
                            Log.d(TAG, "📄 Document data: " + doc.getData().toString());
                            
                            ChatMessage message = convertDocumentToMessage(doc);
                            
                            // ✅ FIXED: Handle Firestore Timestamp objects in initial load
                            Long timestamp = null;
                            Object timestampObj = doc.get("timestamp");
                            if (timestampObj instanceof com.google.firebase.Timestamp) {
                                timestamp = ((com.google.firebase.Timestamp) timestampObj).toDate().getTime();
                                Log.d(TAG, "Parsed Firestore Timestamp in load: " + timestamp);
                            } else if (timestampObj instanceof Long) {
                                timestamp = (Long) timestampObj;
                                Log.d(TAG, "Using Long timestamp in load: " + timestamp);
                            } else if (timestampObj instanceof String) {
                                String timestampStr = (String) timestampObj;
                                try {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ssa z", java.util.Locale.ENGLISH);
                                    java.util.Date date = sdf.parse(timestampStr);
                                    timestamp = date.getTime();
                                    Log.d(TAG, "Parsed string timestamp in load: " + timestamp);
                                } catch (Exception parseException) {
                                    Log.w(TAG, "Could not parse timestamp string: " + timestampStr + ", using current time");
                                    timestamp = System.currentTimeMillis();
                                }
                            } else {
                                Log.w(TAG, "Unknown timestamp type in load: " + (timestampObj != null ? timestampObj.getClass().getSimpleName() : "null"));
                                timestamp = System.currentTimeMillis();
                            }
                            
                            if (message != null && timestamp != null) {
                                tempMessages.add(new MessageWithTimestamp(message, timestamp));
                                loadedMessageIds.add(messageId); // Track this message ID
                                Log.d(TAG, "✅ Successfully converted message: " + message.getContent());
                            } else {
                                Log.w(TAG, "❌ Failed to convert message or timestamp is null");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to message", e);
                        }
                    }
                    
                    // Sort messages by timestamp in memory (no index needed!)
                    java.util.Collections.sort(tempMessages, (m1, m2) -> 
                        Long.compare(m1.timestamp, m2.timestamp));
                    
                    // Add sorted messages to the list
                    for (MessageWithTimestamp msg : tempMessages) {
                        messagesList.add(msg.message);
                    }
                    
                    if (chatAdapter != null) {
                        chatAdapter.notifyDataSetChanged();
                    }
                    
                    // Scroll to bottom after loading with delay to ensure layout is complete
                    messagesRecyclerView.postDelayed(() -> {
                        try {
                            int lastPosition = messagesList.size() - 1;
                            if (lastPosition >= 0) {
                                messagesRecyclerView.scrollToPosition(lastPosition);
                                Log.d(TAG, "Scrolled to last message after loading: position " + lastPosition);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error scrolling to bottom after load", e);
                        }
                    }, 200); // 200ms delay to ensure layout is complete
                    
                    // ✅ FIXED: Don't update chat badge when loading - user is viewing chat
                    // Badge will be updated when user leaves ChatActivity
                    
                    Log.d(TAG, "Loaded " + messagesList.size() + " messages from Firestore");
                    // Initialize previous message count for deletion detection
                    previousMessageCount = messagesList.size();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading messages from Firestore: " + e.getMessage(), e);
                    // Don't show toast if it's just an empty chat
                    if (e.getMessage() != null && !e.getMessage().contains("FAILED_PRECONDITION")) {
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadMessagesFromFirestore", e);
        }
    }
    
    // Helper class to sort messages by timestamp
    private static class MessageWithTimestamp {
        ChatMessage message;
        long timestamp;
        
        MessageWithTimestamp(ChatMessage message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    private void setupRealtimeMessageListener() {
        try {
            Log.d(TAG, "Setting up realtime message listener (flat structure)");
            Log.d(TAG, "🔍 Listening for messages with userId: " + chatRoomId);
            
            // Flat structure: Listen to messages filtered by userId only (no orderBy to avoid index)
            messageListener = db.collection("chat_messages")
                .whereEqualTo("userId", chatRoomId) // Filter to get only this user's messages
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error.getMessage(), error);
                        return;
                    }
                    
                    if (snapshots != null) {
                        Log.d(TAG, "📡 Realtime listener received " + snapshots.getDocumentChanges().size() + " changes");
                        
                        // Track removed messages count
                        int removedCount = 0;
                        int addedCount = 0;
                        
                        for (com.google.firebase.firestore.DocumentChange dc : snapshots.getDocumentChanges()) {
                            Log.d(TAG, "📡 Document change type: " + dc.getType() + ", doc: " + dc.getDocument().getId());
                            Log.d(TAG, "📡 Document data: " + dc.getDocument().getData().toString());
                            
                            // Count changes
                            if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                removedCount++;
                            } else if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                addedCount++;
                            }
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    try {
                                        String messageId = dc.getDocument().getId();
                                        
                                        // Check if we already have this message (prevent duplicates)
                                        if (!isMessageAlreadyInList(messageId)) {
                                            Log.d(TAG, "🆕 Processing new message: " + messageId);
                                            ChatMessage newMessage = convertDocumentToMessage(dc.getDocument());
                                            
                                            // ✅ FIXED: Handle Firestore Timestamp objects in real-time listener
                                            Long timestamp = null;
                                            Object timestampObj = dc.getDocument().get("timestamp");
                                            if (timestampObj instanceof com.google.firebase.Timestamp) {
                                                timestamp = ((com.google.firebase.Timestamp) timestampObj).toDate().getTime();
                                                Log.d(TAG, "Parsed Firestore Timestamp in real-time: " + timestamp);
                                            } else if (timestampObj instanceof Long) {
                                                timestamp = (Long) timestampObj;
                                                Log.d(TAG, "Using Long timestamp in real-time: " + timestamp);
                                            } else if (timestampObj instanceof String) {
                                                String timestampStr = (String) timestampObj;
                                                try {
                                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ssa z", java.util.Locale.ENGLISH);
                                                    java.util.Date date = sdf.parse(timestampStr);
                                                    timestamp = date.getTime();
                                                    Log.d(TAG, "Parsed string timestamp in real-time: " + timestamp);
                                                } catch (Exception parseException) {
                                                    Log.w(TAG, "Could not parse timestamp string: " + timestampStr + ", using current time");
                                                    timestamp = System.currentTimeMillis();
                                                }
                                            } else {
                                                Log.w(TAG, "Unknown timestamp type in real-time: " + (timestampObj != null ? timestampObj.getClass().getSimpleName() : "null"));
                                                timestamp = System.currentTimeMillis();
                                            }
                                            
                                            if (newMessage != null && timestamp != null) {
                                                // ✅ FIXED: If message is from admin and user is viewing chat, mark as read immediately
                                                if (!newMessage.isUser() && ChatActivityTracker.isChatActivityVisible()) {
                                                    newMessage.setRead(true);
                                                    // Update in Firestore
                                                    db.collection("chat_messages")
                                                        .document(messageId)
                                                        .update("isRead", true)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "✅ Auto-marked new admin message as read: " + messageId);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "❌ Error auto-marking message as read: " + e.getMessage(), e);
                                                        });
                                                    Log.d(TAG, "✅ New admin message auto-marked as read (user is viewing chat)");
                                                }
                                                
                                                // Add message to the end (messages should arrive in order)
                                                messagesList.add(newMessage);
                                                loadedMessageIds.add(messageId); // Track this message ID
                                                chatAdapter.notifyItemInserted(messagesList.size() - 1);
                                                
                                                Log.d(TAG, "✅ New message added via realtime listener: " + newMessage.getContent());
                                                
                                                // ✅ FIXED: Don't update badge if user is viewing chat (handled by updateChatNotificationBadge)
                                                updateChatNotificationBadge();
                                                
                                                // Scroll to bottom for new messages
                                                messagesRecyclerView.postDelayed(() -> {
                                                    try {
                                                        int lastPosition = messagesList.size() - 1;
                                                        if (lastPosition >= 0) {
                                                            messagesRecyclerView.smoothScrollToPosition(lastPosition);
                                                            Log.d(TAG, "Scrolled to new message at position: " + lastPosition);
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Error scrolling to new message", e);
                                                    }
                                                }, 100);
                                            } else {
                                                Log.w(TAG, "❌ Failed to process real-time message: " + messageId);
                                            }
                                        } else {
                                            Log.d(TAG, "Message already in list, skipping: " + messageId);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error adding new message", e);
                                    }
                                    break;
                                case MODIFIED:
                                    // Handle message modifications if needed
                                    Log.d(TAG, "Message modified: " + dc.getDocument().getId());
                                    break;
                                case REMOVED:
                                    // Handle message removal
                                    String removedMessageId = dc.getDocument().getId();
                                    Log.d(TAG, "Message removed: " + removedMessageId);
                                    
                                    // Remove from local list
                                    for (int i = messagesList.size() - 1; i >= 0; i--) {
                                        if (messagesList.get(i).getMessageId() != null && 
                                            messagesList.get(i).getMessageId().equals(removedMessageId)) {
                                            messagesList.remove(i);
                                            chatAdapter.notifyItemRemoved(i);
                                            loadedMessageIds.remove(removedMessageId);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                        
                        // Check for conversation deletion (bulk removal or all messages gone)
                        int currentMessageCount = snapshots.size();
                        long currentTime = System.currentTimeMillis();
                        
                        // Detect bulk deletion: multiple removals at once or all messages suddenly gone
                        if (removedCount > 0) {
                            // If we had messages before and now we have significantly fewer or none, it's likely admin deletion
                            if (previousMessageCount > 0 && currentMessageCount < previousMessageCount) {
                                // Check if this looks like a conversation deletion (more than 1 message removed or all gone)
                                if (removedCount > 1 || currentMessageCount == 0) {
                                    // Check if deletion was done by admin (users can only delete their own messages one at a time)
                                    checkAndNotifyAdminDeletion(removedCount, currentMessageCount);
                                }
                            }
                        }
                        
                        previousMessageCount = currentMessageCount;
                        lastDeletionCheckTime = currentTime;
                    }
                });
            
            Log.d(TAG, "Realtime message listener setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up realtime message listener", e);
        }
    }
    
    /**
     * Checks if conversation deletion was done by admin and shows toast notification
     * Users can only delete their own messages one at a time, so bulk deletion indicates admin action
     */
    private void checkAndNotifyAdminDeletion(int removedCount, int remainingMessageCount) {
        try {
            // Users can only delete their own messages one at a time
            // If multiple messages are deleted at once or all messages are gone, it's likely admin deletion
            if (removedCount > 1 || remainingMessageCount == 0) {
                // Verify current user is not an admin (to avoid false positives)
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Check if current user is admin
                    db.collection("admins").document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(adminDoc -> {
                            // If document doesn't exist, user is not an admin
                            if (!adminDoc.exists()) {
                                // Also check superAdmin collection
                                db.collection("superAdmin").document(currentUser.getUid())
                                    .get()
                                    .addOnSuccessListener(superAdminDoc -> {
                                        if (!superAdminDoc.exists()) {
                                            // User is not an admin, so deletion must be by admin
                                            showAdminDeletionToast();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error checking superAdmin status", e);
                                        // On error, assume it's admin deletion if bulk removal
                                        if (removedCount > 1 || remainingMessageCount == 0) {
                                            showAdminDeletionToast();
                                        }
                                    });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error checking admin status", e);
                            // On error, assume it's admin deletion if bulk removal
                            if (removedCount > 1 || remainingMessageCount == 0) {
                                showAdminDeletionToast();
                            }
                        });
                } else {
                    // No current user, but bulk deletion still suggests admin action
                    if (removedCount > 1 || remainingMessageCount == 0) {
                        showAdminDeletionToast();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin deletion", e);
        }
    }
    
    /**
     * Shows toast notification that admin deleted the conversation
     * Always saves deletion flag to SharedPreferences so user sees it when they open chat
     */
    private void showAdminDeletionToast() {
        // Save admin deletion flag to SharedPreferences (regardless of chat visibility)
        // This ensures user sees the toast when they open chat later
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ADMIN_DELETION_DETECTED, true);
        editor.putLong(KEY_ADMIN_DELETION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "Admin deletion flag saved to SharedPreferences");
        
        // If chat is currently visible, show toast immediately
        if (ChatActivityTracker.isChatActivityVisible()) {
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this, 
                    "Your conversation has been deleted by an administrator.", 
                    Toast.LENGTH_LONG).show();
                Log.d(TAG, "Admin deletion detected - showing toast notification (chat is visible)");
            });
        } else {
            Log.d(TAG, "Admin deletion detected but chat is not visible - flag saved, toast will show when chat opens");
        }
    }
    
    /**
     * Checks if admin deletion was detected and shows toast when user opens chat
     * Called in onResume() to show toast even if deletion happened while chat was closed
     */
    private void checkAndShowAdminDeletionToast() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean deletionDetected = prefs.getBoolean(KEY_ADMIN_DELETION_DETECTED, false);
            
            if (deletionDetected) {
                // Get deletion timestamp
                long deletionTimestamp = prefs.getLong(KEY_ADMIN_DELETION_TIMESTAMP, 0);
                long currentTime = System.currentTimeMillis();
                
                // Show toast regardless of when deletion happened (minutes/hours/days ago)
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, 
                        "Your conversation has been deleted by an administrator.", 
                        Toast.LENGTH_LONG).show();
                    
                    // Calculate time since deletion for logging
                    long timeSinceDeletion = currentTime - deletionTimestamp;
                    long hoursSince = timeSinceDeletion / (1000 * 60 * 60);
                    long minutesSince = (timeSinceDeletion / (1000 * 60)) % 60;
                    
                    if (hoursSince > 0) {
                        Log.d(TAG, "Admin deletion toast shown - deletion happened " + hoursSince + " hour(s) and " + minutesSince + " minute(s) ago");
                    } else {
                        Log.d(TAG, "Admin deletion toast shown - deletion happened " + minutesSince + " minute(s) ago");
                    }
                });
                
                // Clear the flag after showing toast (so it doesn't show again)
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(KEY_ADMIN_DELETION_DETECTED);
                editor.remove(KEY_ADMIN_DELETION_TIMESTAMP);
                editor.apply();
                Log.d(TAG, "Admin deletion flag cleared after showing toast");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin deletion toast", e);
        }
    }
    
    private ChatMessage convertDocumentToMessage(QueryDocumentSnapshot doc) {
        try {
            // ✅ FIXED: Handle web app's "message" field as primary (web app format)
            String content = doc.getString("message"); // Web app uses "message"
            if (content == null) {
                content = doc.getString("content"); // Fallback for mobile app format
            }
            
            // ✅ FIXED: Handle Firestore Timestamp objects from web app
            Long timestamp = null;
            Object timestampObj = doc.get("timestamp");
            if (timestampObj instanceof com.google.firebase.Timestamp) {
                timestamp = ((com.google.firebase.Timestamp) timestampObj).toDate().getTime();
                Log.d(TAG, "Parsed Firestore Timestamp: " + timestamp);
            } else if (timestampObj instanceof Long) {
                timestamp = (Long) timestampObj;
                Log.d(TAG, "Using Long timestamp: " + timestamp);
            } else if (timestampObj instanceof String) {
                // Parse string timestamp (legacy format)
                String timestampStr = (String) timestampObj;
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ssa z", java.util.Locale.ENGLISH);
                    java.util.Date date = sdf.parse(timestampStr);
                    timestamp = date.getTime();
                    Log.d(TAG, "Parsed string timestamp: " + timestamp);
                } catch (Exception parseException) {
                    Log.w(TAG, "Could not parse timestamp string: " + timestampStr + ", using current time");
                    timestamp = System.currentTimeMillis();
                }
            } else {
                Log.w(TAG, "Unknown timestamp type: " + (timestampObj != null ? timestampObj.getClass().getSimpleName() : "null"));
                timestamp = System.currentTimeMillis();
            }
            
            // ✅ FIXED: Determine if message is from current user
            String senderId = doc.getString("senderId");
            String userId = doc.getString("userId");
            String currentUserId = mAuth.getCurrentUser().getUid();
            
            boolean isUser = (senderId != null && senderId.equals(currentUserId));
            
            // ✅ FIXED: Handle multiple attachment types from web app
            String attachmentUrl = null;
            String attachmentType = null;
            
            // Check for different attachment types (web app format)
            if (doc.getString("fileUrl") != null) {
                attachmentUrl = doc.getString("fileUrl");
                attachmentType = "file";
            } else if (doc.getString("imageUrl") != null) {
                attachmentUrl = doc.getString("imageUrl");
                attachmentType = "image";
            } else if (doc.getString("audioUrl") != null) {
                attachmentUrl = doc.getString("audioUrl");
                attachmentType = "audio";
            } else if (doc.getString("videoUrl") != null) {
                attachmentUrl = doc.getString("videoUrl");
                attachmentType = "video";
            }
            
            // Fallback to mobile app format
            if (attachmentUrl == null && doc.getString("imageUrl") != null) {
                attachmentUrl = doc.getString("imageUrl");
                attachmentType = "image";
            }
            
            String profilePictureUrl = doc.getString("profilePictureUrl");
            String senderName = doc.getString("senderName");
            
            // Get attachment metadata if available
            String fileName = doc.getString("fileName");
            Long fileSize = doc.getLong("fileSize");
            String fileType = doc.getString("fileType");
            
            if (content == null) content = "";
            if (timestamp == null) timestamp = System.currentTimeMillis();
            
            String timestampStr = formatTimestamp(timestamp);
            
            // Create message with profile picture URL
            ChatMessage message = new ChatMessage(content, isUser, timestampStr, profilePictureUrl);
            
            // Set attachment URL based on type
            if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
                if ("video".equals(attachmentType)) {
                    message.setVideoUrl(attachmentUrl);
                    message.setImageUrl(attachmentUrl); // Also set for display compatibility
                } else if ("audio".equals(attachmentType)) {
                    message.setAudioUrl(attachmentUrl);
                    message.setImageUrl(attachmentUrl); // Also set for display compatibility
                } else {
                    // For images and files, use imageUrl
                    message.setImageUrl(attachmentUrl);
                }
            }
            
            // ✅ ADDED: Set attachment metadata for web app messages
            if (attachmentType != null) {
                message.setAttachmentType(attachmentType);
            }
            if (fileName != null) {
                message.setFileName(fileName);
            }
            if (fileSize != null) {
                message.setFileSize(fileSize);
            }
            if (fileType != null) {
                message.setFileType(fileType);
            }
            
            // ✅ NEW: Set message ID and read status
            message.setMessageId(doc.getId());
            message.setSenderId(senderId);
            
            // Get read status from Firestore (default to false for admin messages, true for user messages)
            Boolean isReadObj = doc.getBoolean("isRead");
            if (isReadObj != null) {
                message.setRead(isReadObj);
            } else {
                // If message is from user, mark as read. If from admin, mark as unread.
                message.setRead(isUser);
            }
            
            // Log for debugging with comprehensive info
            Log.d(TAG, "Converted message: content='" + content + "', isUser=" + isUser + 
                      ", timestamp=" + timestamp + ", senderName='" + senderName + 
                      "', attachmentType='" + attachmentType + "', attachmentUrl='" + attachmentUrl + 
                      "', fileName='" + fileName + "', fileSize=" + fileSize + 
                      ", fileType='" + fileType + "'");
            
            return message;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to message", e);
            return null;
        }
    }
    
    private boolean isMessageAlreadyInList(String messageId) {
        // Check if this message ID has already been loaded
        return loadedMessageIds != null && loadedMessageIds.contains(messageId);
    }
    
    private String formatTimestamp(long timestamp) {
        try {
            Date messageDate = new Date(timestamp);
            Date currentDate = new Date();
            
            long diffInMillis = currentDate.getTime() - messageDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            
            if (diffInDays == 0) {
                return "Today • " + timeFormat.format(messageDate);
            } else if (diffInDays == 1) {
                return "Yesterday • " + timeFormat.format(messageDate);
            } else if (diffInDays < 7) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE • h:mm a", Locale.getDefault());
                return dayFormat.format(messageDate);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd • h:mm a", Locale.getDefault());
                return dateFormat.format(messageDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting timestamp", e);
            return "Now";
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadgeChat == null) return;
            
            // Use the same logic as AlertsActivity - check viewed state
            int lastViewedCount = sharedPreferences.getInt(KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT, 0);
            
            // Fetch current announcement count and calculate unread count
            fetchAndCountNewAnnouncementsFromChat(lastViewedCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge on chat: " + e.getMessage(), e);
            if (alertsBadgeChat != null) {
                alertsBadgeChat.setVisibility(View.GONE);
            }
        }
    }
    
    // This method is no longer needed - using fetchAndCountNewAnnouncementsFromChat directly
    // Keeping for backward compatibility but it's not used anymore
    
    private void fetchAndCountNewAnnouncementsFromChat(int lastViewedCount) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("announcements")
                .orderBy("createdTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int currentTotalCount = task.getResult().size();
                        
                        // Calculate unread count: current count - last viewed count
                        int unreadCount = currentTotalCount - lastViewedCount;
                        
                        // Ensure unread count is never negative
                        if (unreadCount < 0) {
                            unreadCount = 0;
                        }
                        
                        // Make unreadCount final for use in lambda
                        final int finalUnreadCount = unreadCount;
                        
                        // Update badge on UI thread
                        runOnUiThread(() -> {
                            if (alertsBadgeChat != null) {
                                if (finalUnreadCount > 0) {
                                    alertsBadgeChat.setText(String.valueOf(finalUnreadCount));
                                    alertsBadgeChat.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "✅ Chat badge showing: " + finalUnreadCount + " unread announcements");
                                } else {
                                    alertsBadgeChat.setVisibility(View.GONE);
                                    alertsBadgeChat.setText("0");
                                    Log.d(TAG, "✅ Chat badge hidden - all announcements viewed (lastViewed: " + 
                                          lastViewedCount + ", current: " + currentTotalCount + ")");
                                }
                            }
                        });
                        
                        Log.d(TAG, "Chat badge update - unreadCount: " + unreadCount + 
                                  " (current: " + currentTotalCount + ", lastViewed: " + lastViewedCount + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching announcements for badge from chat: " + e.getMessage(), e);
                    if (alertsBadgeChat != null) {
                        runOnUiThread(() -> {
                            alertsBadgeChat.setVisibility(View.GONE);
                        });
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching and counting new announcements from chat: " + e.getMessage(), e);
        }
    }
    
    private boolean isAnnouncementNewFromChat(String dateStr, long lastVisitTime) {
        try {
            Date currentDate = new Date();
            long announcementTime = 0;
            
            // Handle different date formats (same logic as AlertsActivity)
            if (dateStr.toLowerCase().contains("today")) {
                announcementTime = currentDate.getTime();
            } else if (dateStr.toLowerCase().contains("yesterday")) {
                announcementTime = currentDate.getTime() - 86400000; // 1 day in milliseconds
            } else if (dateStr.toLowerCase().contains("days ago")) {
                String[] parts = dateStr.split(" ");
                if (parts.length > 0) {
                    try {
                        int days = Integer.parseInt(parts[0]);
                        announcementTime = currentDate.getTime() - (days * 86400000);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            } else if (dateStr.toLowerCase().contains("week ago")) {
                announcementTime = currentDate.getTime() - (7 * 86400000);
            } else {
                // If parsing fails, assume it's new
                announcementTime = currentDate.getTime();
            }
            
            return announcementTime > lastVisitTime;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if announcement is new from chat: " + e.getMessage(), e);
            return false;
        }
    }
    
    private void loadUserProfilePictureUrl() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Log.w(TAG, "No user logged in, cannot load profile picture");
                return;
            }

            Log.d(TAG, "Loading user profile picture URL from Firestore for messages");
            
            db.collection("users")
                    .whereEqualTo("firebaseUid", user.getUid())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            userProfilePictureUrl = doc.getString("profilePictureUrl");
                            
                            if (userProfilePictureUrl != null && !userProfilePictureUrl.isEmpty()) {
                                Log.d(TAG, "User profile picture URL loaded for messages: " + userProfilePictureUrl);
                                
                                // Pre-cache for instant message display
                                ProfilePictureCache.getInstance().precacheProfilePicture(userProfilePictureUrl);
                            } else {
                                Log.d(TAG, "No profile picture URL found for user");
                            }
                        } else {
                            Log.w(TAG, "No user document found in Firestore");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user profile picture URL from Firestore", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfilePictureUrl", e);
        }
    }
    
    private void loadUserPhoneNumber() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Log.w(TAG, "No user logged in, cannot load phone number");
                return;
            }

            Log.d(TAG, "Loading user phone number from Firestore");
            
            // Try to load from SharedPreferences first (faster)
            SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
            String cachedPhoneNumber = prefs.getString("phone_number", null);
            
            if (cachedPhoneNumber != null && !cachedPhoneNumber.isEmpty()) {
                userPhoneNumber = cachedPhoneNumber;
                Log.d(TAG, "User phone number loaded from cache: " + userPhoneNumber);
                
                // Update chat room metadata with phone number
                updateChatRoomWithPhoneNumber();
                return;
            }
            
            // If not in cache, load from Firestore
            db.collection("users")
                    .whereEqualTo("firebaseUid", user.getUid())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            userPhoneNumber = doc.getString("phoneNumber");
                            
                            if (userPhoneNumber == null || userPhoneNumber.isEmpty()) {
                                // Try alternative field names
                                userPhoneNumber = doc.getString("phone");
                                if (userPhoneNumber == null || userPhoneNumber.isEmpty()) {
                                    userPhoneNumber = doc.getString("mobileNumber");
                                }
                                if (userPhoneNumber == null || userPhoneNumber.isEmpty()) {
                                    userPhoneNumber = doc.getString("contactNumber");
                                }
                            }
                            
                            if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
                                Log.d(TAG, "User phone number loaded from Firestore: " + userPhoneNumber);
                                
                                // Cache it for future use
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("phone_number", userPhoneNumber);
                                editor.apply();
                                
                                // Update chat room metadata with phone number
                                updateChatRoomWithPhoneNumber();
                            } else {
                                Log.d(TAG, "No phone number found for user in Firestore");
                            }
                        } else {
                            Log.w(TAG, "No user document found in Firestore");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user phone number from Firestore", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserPhoneNumber", e);
        }
    }
    
    private void updateChatRoomWithPhoneNumber() {
        try {
            if (userPhoneNumber == null || chatRoomId == null) return;
            
            // Update chat room metadata with phone number
            java.util.Map<String, Object> updateData = new java.util.HashMap<>();
            updateData.put("userPhoneNumber", userPhoneNumber);
            
            db.collection("chats")
                .document(chatRoomId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat room updated with phone number");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating chat room with phone number", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateChatRoomWithPhoneNumber", e);
        }
    }
    
    private void makeEmergencyCall() {
        try {
            String ldrrmoNumber = "tel:042-555-0101"; // LDRRMO emergency number
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, make the call
                makeCall(ldrrmoNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error making emergency call: " + e.getMessage(), e);
            Toast.makeText(this, "Error making emergency call", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void makeCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(phoneNumber));
            startActivity(callIntent);
            Toast.makeText(this, "Calling Lucban LDRRMO...", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            // If permission denied or other security issue, show dial pad
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse(phoneNumber));
            startActivity(dialIntent);
            Toast.makeText(this, "Opening dial pad for LDRRMO", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error making call", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        try {
            if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("tel:042-555-0101");
                } else {
                    // Permission denied, show dial pad instead
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:042-555-0101"));
                    startActivity(dialIntent);
                    Toast.makeText(this, "Permission denied. Opening dial pad instead.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == STORAGE_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == AUDIO_PERMISSION_CODE) {
                // Check if at least RECORD_AUDIO permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAudioRecorder();
                } else {
                    Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage(), e);
            Toast.makeText(this, "Error handling permission", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle notification extras when opened from push notification
     */
    private void handleNotificationExtras() {
        try {
            Intent intent = getIntent();
            if (intent.getBooleanExtra("scrollToMessage", false)) {
                String messageId = intent.getStringExtra("highlightMessageId");
                if (messageId != null) {
                    Log.d(TAG, "Opening chat from notification for message: " + messageId);
                    // Scroll to the specific message after messages are loaded
                    scrollToMessageAfterLoad(messageId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification extras: " + e.getMessage(), e);
        }
    }
    
    /**
     * Scroll to a specific message after messages are loaded
     */
    private void scrollToMessageAfterLoad(String messageId) {
        try {
            // Wait for messages to load, then scroll to the specific message
            messagesRecyclerView.postDelayed(() -> {
                try {
                    if (messagesList != null && !messagesList.isEmpty()) {
                        // Find the message with the given ID
                        for (int i = 0; i < messagesList.size(); i++) {
                            ChatMessage message = messagesList.get(i);
                            // Note: You might need to add a messageId field to ChatMessage class
                            // For now, we'll scroll to the bottom (latest message)
                            if (i == messagesList.size() - 1) {
                                messagesRecyclerView.smoothScrollToPosition(i);
                                Log.d(TAG, "Scrolled to message at position: " + i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error scrolling to message: " + e.getMessage(), e);
                }
            }, 1000); // Wait 1 second for messages to load
        } catch (Exception e) {
            Log.e(TAG, "Error in scrollToMessageAfterLoad: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update chat notification badge count
     * ✅ FIXED: Only update badge when NOT in ChatActivity
     */
    private void updateChatNotificationBadge() {
        try {
            // ✅ CRITICAL FIX: Don't show badge notification when user is viewing chat
            if (ChatActivityTracker.isChatActivityVisible()) {
                Log.d(TAG, "User is viewing chat - NOT showing badge notification");
                // Clear any existing badge notification
                if (notificationManager != null) {
                    notificationManager.cancel(CHAT_BADGE_NOTIFICATION_ID);
                }
                return;
            }
            
            if (notificationManager == null) {
                Log.w(TAG, "Notification manager is null, cannot update badge");
                return;
            }
            
            // Count unread messages (messages not from current user)
            int unreadCount = countUnreadMessages();
            
            Log.d(TAG, "Chat badge count: " + unreadCount);
            
            if (unreadCount > 0) {
                // Show badge with count
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_CHAT_MESSAGES)
                    .setSmallIcon(R.drawable.accizard_logo_svg)
                    .setContentTitle("AcciZard Chat")
                    .setContentText(unreadCount + " unread message" + (unreadCount > 1 ? "s" : ""))
                    .setNumber(unreadCount)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET); // Hide from lock screen
                
                notificationManager.notify(CHAT_BADGE_NOTIFICATION_ID, builder.build());
                Log.d(TAG, "Chat badge shown with count: " + unreadCount);
            } else {
                // Clear badge
                notificationManager.cancel(CHAT_BADGE_NOTIFICATION_ID);
                Log.d(TAG, "Chat badge cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating chat badge: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count unread messages (messages not from current user)
     */
    private int countUnreadMessages() {
        try {
            if (messagesList == null || messagesList.isEmpty()) {
                return 0;
            }
            
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                return 0;
            }
            
            String currentUserId = currentUser.getUid();
            int unreadCount = 0;
            
            // ✅ FIXED: Count messages that are from admin AND not read yet
            for (ChatMessage message : messagesList) {
                if (!message.isUser() && !message.isRead()) { // Message is not from user AND not read
                    unreadCount++;
                    Log.d(TAG, "Unread message found: " + message.getMessageId());
                }
            }
            
            Log.d(TAG, "Total unread messages: " + unreadCount);
            return unreadCount;
        } catch (Exception e) {
            Log.e(TAG, "Error counting unread messages: " + e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Clear chat badge when user opens chat
     */
    private void clearChatBadge() {
        try {
            if (notificationManager != null) {
                notificationManager.cancel(CHAT_BADGE_NOTIFICATION_ID);
                Log.d(TAG, "Chat badge cleared when opening chat");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing chat badge: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark all unread messages as read in Firestore
     * ✅ FIXED: Properly mark messages as read and clear notifications
     */
    private void markMessagesAsRead() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null || messagesList == null || messagesList.isEmpty()) {
                Log.d(TAG, "No messages to mark as read");
                return;
            }
            
            String currentUserId = currentUser.getUid();
            int markedCount = 0;
            java.util.List<String> messagesToMark = new java.util.ArrayList<>();
            
            // Find all unread messages from admin
            for (ChatMessage message : messagesList) {
                if (!message.isUser() && !message.isRead() && message.getMessageId() != null) {
                    messagesToMark.add(message.getMessageId());
                    // Update local message object immediately
                    message.setRead(true);
                    markedCount++;
                }
            }
            
            if (markedCount > 0) {
                Log.d(TAG, "Marking " + markedCount + " messages as read in Firestore");
                
                // Mark all messages as read in Firestore in batch
                for (String messageId : messagesToMark) {
                    db.collection("chat_messages")
                        .document(messageId)
                        .update("isRead", true)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Marked message as read: " + messageId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Error marking message as read: " + messageId + " - " + e.getMessage(), e);
                        });
                }
                
                // ✅ FIXED: Clear badge immediately after marking as read
                clearChatBadge();
                
                // Notify adapter if needed
                if (chatAdapter != null) {
                    chatAdapter.notifyDataSetChanged();
                }
                
                Log.d(TAG, "✅ Successfully marked " + markedCount + " messages as read locally");
            } else {
                Log.d(TAG, "No unread messages to mark");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking messages as read: " + e.getMessage(), e);
        }
    }
}