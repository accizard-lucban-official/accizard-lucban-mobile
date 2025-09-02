package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageView sendButton, backButton, profileButton;
    private LinearLayout homeTab, chatTab, reportTab, mapTab, alertsTab;
    private TextView statusText;
    private ImageButton addActionButton;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messagesList;

    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;
    private static final int CAMERA_PERMISSION_CODE = 203;
    private static final int STORAGE_PERMISSION_CODE = 204;
    private Uri photoUri;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ChatActivity onCreate started");

        try {
            setContentView(R.layout.activity_chat);
            mAuth = FirebaseAuth.getInstance();
            initializeViews();
            setupRecyclerView();
            setupClickListeners();
            loadInitialMessages();
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
            profileButton = findViewById(R.id.profileButton);
            addActionButton = findViewById(R.id.addActionButton);

            // Bottom navigation
            homeTab = findViewById(R.id.homeTab);
            chatTab = findViewById(R.id.chatTab);
            reportTab = findViewById(R.id.reportTab);
            mapTab = findViewById(R.id.mapTab);
            alertsTab = findViewById(R.id.alertsTab);

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
            chatAdapter = new ChatAdapter(messagesList);

            if (messagesRecyclerView != null) {
                messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                messagesRecyclerView.setAdapter(chatAdapter);
                Log.d(TAG, "RecyclerView setup completed");
            } else {
                Log.e(TAG, "messagesRecyclerView is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            Toast.makeText(this, "Error setting up chat list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                });
            }

            // Send button
            if (sendButton != null) {
                sendButton.setOnClickListener(v -> {
                    Log.d(TAG, "Send button clicked");
                    sendMessage();
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

    private void setupBottomNavigationListeners() {
        // Home tab
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                Log.d(TAG, "Home tab clicked");
                try {
                    Intent intent = new Intent(ChatActivity.this, MainDashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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

        // Report tab - Main implementation for navigation to ReportSubmissionActivity
        if (reportTab != null) {
            reportTab.setOnClickListener(v -> {
                Log.d(TAG, "Report tab clicked - navigating to ReportSubmissionActivity");
                try {
                    Intent intent = new Intent(ChatActivity.this, ReportSubmissionActivity.class);
                    intent.putExtra("from_activity", "ChatActivity");

                    Log.d(TAG, "Starting ReportSubmissionActivity");
                    startActivity(intent);

                    // Add smooth transition animation
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                    Log.d(TAG, "Successfully navigated to ReportSubmissionActivity");

                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ReportSubmissionActivity", e);
                    Toast.makeText(ChatActivity.this, "Error opening report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "reportTab is null - check your layout file for R.id.reportTab");
        }

        // Map tab
        if (mapTab != null) {
            mapTab.setOnClickListener(v -> {
                Log.d(TAG, "Map tab clicked");
                try {
                    Intent intent = new Intent(ChatActivity.this, MapViewActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
                    Intent intent = new Intent(ChatActivity.this, AlertsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to alerts", e);
                    Toast.makeText(ChatActivity.this, "Alerts feature coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Profile tab
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Log.d(TAG, "Profile tab clicked");
                try {
                    Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to profile", e);
                    Toast.makeText(ChatActivity.this, "Profile feature coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadInitialMessages() {
        try {
            Log.d(TAG, "Loading initial messages");
            addMessage("Hello! How can we assist you today?", false, "Today • 11:54 AM");
            addMessage("Welcome to ACCiZard Lucban Chat Support!", false, getCurrentTime());
            Log.d(TAG, "Initial messages loaded");
        } catch (Exception e) {
            Log.e(TAG, "Error loading initial messages", e);
            Toast.makeText(this, "Error loading initial messages", Toast.LENGTH_SHORT).show();
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
                addMessage(message, true, getCurrentTime());
                messageInput.setText("");

                // Simulate response after a delay
                if (messagesRecyclerView != null) {
                    messagesRecyclerView.postDelayed(() -> {
                        addMessage("Thank you for your message. We'll get back to you shortly.", false, getCurrentTime());
                    }, 1000);
                }
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessage(String content, boolean isUser, String timestamp) {
        try {
            if (messagesList == null || chatAdapter == null) {
                Log.e(TAG, "messagesList or chatAdapter is null");
                return;
            }

            ChatMessage message = new ChatMessage(content, isUser, timestamp);
            messagesList.add(message);
            chatAdapter.notifyItemInserted(messagesList.size() - 1);

            if (messagesRecyclerView != null) {
                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
            }
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

            ChatMessage message = new ChatMessage(content, isUser, timestamp, imageBitmap);
            messagesList.add(message);
            chatAdapter.notifyItemInserted(messagesList.size() - 1);

            if (messagesRecyclerView != null) {
                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
            }
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
                        Toast.makeText(ChatActivity.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
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
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Take a Photo");
        popup.getMenu().add(0, 2, 1, "Open Gallery");
        popup.getMenu().add(0, 3, 2, "Reference Report");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                    return true;
                case 2:
                    if (checkStoragePermission()) {
                        openGallery();
                    } else {
                        requestStoragePermission();
                    }
                    return true;
                case 3:
                    showReferenceReportDialog();
                    return true;
            }
            return false;
        });
        popup.show();
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

    private void showReferenceReportDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reference Report");
        final EditText input = new EditText(this);
        input.setHint("Enter report reference (e.g., #REP-001)");
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String ref = input.getText().toString().trim();
            if (!ref.isEmpty()) {
                addMessage("📋 Reference: " + ref, true, getCurrentTime());
                // Simulate admin response
                messagesRecyclerView.postDelayed(() -> {
                    addMessage("I've noted your report reference " + ref + ". Let me check the details for you.", false, getCurrentTime());
                }, 1500);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
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
                    addImageMessage("", true, getCurrentTime(), photoBitmap);
                    uploadImageToFirebase(photoBitmap, true);
                    // Simulate admin response
                    messagesRecyclerView.postDelayed(() -> {
                        addMessage("Great! I can see the photo you took. How can I help you with this?", false, getCurrentTime());
                    }, 1500);
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Handle gallery image
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap galleryBitmap = BitmapFactory.decodeStream(inputStream);
                        if (galleryBitmap != null) {
                            addImageMessage("", true, getCurrentTime(), galleryBitmap);
                            uploadImageToFirebase(galleryBitmap, false);
                            // Simulate admin response
                            messagesRecyclerView.postDelayed(() -> {
                                addMessage("I can see the image you selected. What would you like me to help you with?", false, getCurrentTime());
                            }, 1500);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading image from gallery", e);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called");
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ChatActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ChatActivity onPause");
    }
}