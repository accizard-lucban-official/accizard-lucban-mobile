package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.ClickableSpan;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AlertsActivity extends AppCompatActivity {

    private static final String TAG = "AlertsActivity";
    private static final String PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    private static final String KEY_LAST_ANNOUNCEMENT_COUNT = "last_announcement_count";
    private static final String KEY_TOTAL_ANNOUNCEMENT_COUNT = "total_announcement_count";
    
    private Spinner filterSpinner;
    private ImageView profileIcon;
    private LinearLayout navHome, navChat, navReport, navMap, navAlerts;
    private RecyclerView announcementsRecyclerView;
    private AnnouncementAdapter announcementAdapter;
    private List<Announcement> announcementList = new ArrayList<>();
    private List<Announcement> fullAnnouncementList = new ArrayList<>(); // For filtering
    private SwipeRefreshLayout swipeRefreshLayout;
    private String selectedFilter = "All";
    private FirebaseAuth mAuth;
    private TextView alertsBadge;
    private TextView chatBadgeAlerts; // Chat notification badge
    private SharedPreferences sharedPreferences;
    
    // Real-time listener
    private ListenerRegistration announcementListener;
    private boolean isRealtimeListenerActive = false;
    private boolean isInitialLoad = true; // Track if this is the first load

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Start global announcement listener
        AnnouncementNotificationManager.getInstance().startGlobalListener();
        
        initViews();
        
        // Register badge with notification manager and hide since user is viewing alerts
        if (alertsBadge != null) {
            AnnouncementNotificationManager.getInstance().registerBadge("AlertsActivity", alertsBadge);
            alertsBadge.setVisibility(View.GONE);
            AnnouncementNotificationManager.getInstance().clearBadgeForActivity("AlertsActivity");
        }
        
        setupSpinner();
        setupClickListeners();
        setupAnnouncementsRecyclerView();
        setupRealtimeAnnouncementListener();
        
        // Load user profile picture
        loadUserProfilePicture();
    }

    private void initViews() {
        filterSpinner = findViewById(R.id.filter_spinner);
        profileIcon = findViewById(R.id.profile_icon);
        navHome = findViewById(R.id.nav_home);
        navChat = findViewById(R.id.nav_chat);
        navReport = findViewById(R.id.nav_report);
        navMap = findViewById(R.id.nav_map);
        navAlerts = findViewById(R.id.nav_alerts);
        announcementsRecyclerView = findViewById(R.id.announcementsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        alertsBadge = findViewById(R.id.alerts_badge);
        chatBadgeAlerts = findViewById(R.id.chat_badge_alerts); // Initialize chat badge
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Real-time listener will automatically refresh data
                // Just stop the refresh indicator
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    private void setupSpinner() {
        String[] filterOptions = {"All", "Weather Warning", "Flood", "Landslide", "Earthquake", "Road Closure", "Evacuation Order", "Missing Person", "Informational"};
        
        // Create adapter and set to spinner (same as ReportSubmissionActivity)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        // Set initial selection
        filterSpinner.setSelection(0);

        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = filterOptions[position];
                android.util.Log.d("AlertsActivity", "Filter selected: " + selectedFilter);
                filterAnnouncements(selectedFilter);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                android.util.Log.d("AlertsActivity", "Nothing selected in spinner");
            }
        });
    }

    private void filterAnnouncements(String filter) {
        android.util.Log.d("AlertsActivity", "Filtering announcements with: " + filter);
        
        announcementList.clear();
        
        if (filter == null || filter.equals("All")) {
            // Add all announcements (already sorted with newest first)
            announcementList.addAll(fullAnnouncementList);
            android.util.Log.d("AlertsActivity", "Showing all " + fullAnnouncementList.size() + " announcements (newest first)");
        } else {
            // Filter and maintain newest-first order
            int count = 0;
            for (Announcement ann : fullAnnouncementList) {
                if (ann != null && ann.type != null && ann.type.equalsIgnoreCase(filter)) {
                    announcementList.add(ann);
                    count++;
                }
            }
            android.util.Log.d("AlertsActivity", "Found " + count + " announcements matching filter: " + filter + " (newest first)");
        }
        
        announcementAdapter.updateAnnouncements(announcementList);
    }

    private void setupClickListeners() {
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlertsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on home screen
                Intent intent = new Intent(AlertsActivity.this, MainDashboard.class);
                startActivity(intent);
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        navChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to Chat Activity
                Intent intent = new Intent(AlertsActivity.this, ChatActivity.class);
                startActivity(intent);
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        navReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to Report Activity
                Intent intent = new Intent(AlertsActivity.this, ReportSubmissionActivity.class);
                startActivity(intent);
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        navMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to Map Activity
                Intent intent = new Intent(AlertsActivity.this, MapViewActivity.class);
                startActivity(intent);
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        navAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on alerts/announcements screen - do nothing
                android.widget.Toast.makeText(AlertsActivity.this, "You're already on Alerts", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAnnouncementsRecyclerView() {
        announcementAdapter = new AnnouncementAdapter(announcementList);
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementsRecyclerView.setAdapter(announcementAdapter);
        
        // Set click listener to show preview dialog
        announcementAdapter.setOnAnnouncementClickListener(new AnnouncementAdapter.OnAnnouncementClickListener() {
            @Override
            public void onAnnouncementClick(Announcement announcement) {
                showAnnouncementPreview(announcement);
            }
        });
    }

    private void setupRealtimeAnnouncementListener() {
        try {
            Log.d(TAG, "Setting up real-time announcement listener");
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Create query for announcements ordered by createdTime descending
            Query query = db.collection("announcements")
                    .orderBy("createdTime", Query.Direction.DESCENDING);
            
            // Set up real-time listener
            announcementListener = query.addSnapshotListener((snapshots, error) -> {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                
                if (error != null) {
                    Log.e(TAG, "Error in real-time listener: " + error.getMessage());
                    // Load sample data on error
                    loadSampleAnnouncements();
                    filterAnnouncements(selectedFilter);
                    android.widget.Toast.makeText(this, "Connection error. Showing sample data.", android.widget.Toast.LENGTH_SHORT).show();
                    isInitialLoad = false; // Mark initial load as complete
                    return;
                }
                
                if (snapshots == null) {
                    Log.d(TAG, "Snapshots is null");
                    return;
                }
                
                Log.d(TAG, "Real-time listener triggered with " + snapshots.size() + " documents");
                
                // For initial load, rebuild the entire list from snapshot
                if (isInitialLoad) {
                    Log.d(TAG, "Initial load - rebuilding announcement list");
                    fullAnnouncementList.clear();
                    
                    // Add all documents in order (already sorted by Firestore query)
                    for (com.google.firebase.firestore.DocumentSnapshot docSnapshot : snapshots.getDocuments()) {
                        try {
                            String docId = docSnapshot.getId();
                            Log.d(TAG, "=== Processing Firestore Document: " + docId + " ===");
                            
                            // Debug: Print all available fields in the document
                            if (docSnapshot.getData() != null) {
                                Log.d(TAG, "Document fields: " + docSnapshot.getData().keySet().toString());
                                for (String key : docSnapshot.getData().keySet()) {
                                    Object value = docSnapshot.getData().get(key);
                                    Log.d(TAG, "  Field '" + key + "' = '" + value + "' (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                                }
                            } else {
                                Log.w(TAG, "Document data is null!");
                            }
                            
                            // Extract image URL using helper method
                            String imageUrl = extractImageUrlFromDocument(docSnapshot);
                            Log.d(TAG, "✅ Final imageUrl value: '" + imageUrl + "'");
                            
                            // Extract updatedAt field
                            String updatedAt = extractUpdatedAtFromDocument(docSnapshot);
                            Log.d(TAG, "✅ Final updatedAt value: '" + updatedAt + "'");
                            
                            Announcement announcement = new Announcement(
                                    docSnapshot.getString("type"),
                                    docSnapshot.getString("priority"),
                                    docSnapshot.getString("description"),
                                    docSnapshot.getString("date"),
                                    imageUrl,
                                    updatedAt
                            );
                            fullAnnouncementList.add(announcement);
                            Log.d(TAG, "✅ Added announcement: " + announcement.type + 
                                    " (imageUrl: '" + announcement.imageUrl + "')");
                            Log.d(TAG, "=== End Document: " + docId + " ===\n");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing announcement during initial load", e);
                            e.printStackTrace();
                        }
                    }
                    
                    Log.d(TAG, "Initial load complete with " + fullAnnouncementList.size() + " announcements");
                    isInitialLoad = false;
                    
                    // Apply filter after initial load
                    filterAnnouncements(selectedFilter);
                } else {
                    // After initial load, handle incremental changes
                    boolean hasChanges = false;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                handleNewAnnouncement(dc.getDocument(), false);
                                hasChanges = true;
                                break;
                            case MODIFIED:
                                handleModifiedAnnouncement(dc.getDocument());
                                hasChanges = true;
                                break;
                            case REMOVED:
                                handleRemovedAnnouncement(dc.getDocument());
                                hasChanges = true;
                                break;
                        }
                    }
                    
                    // If there were changes, refresh the filtered list
                    if (hasChanges) {
                        filterAnnouncements(selectedFilter);
                        Log.d(TAG, "Real-time update: Refreshed announcement list after changes");
                    }
                }
                
                // If no announcements exist, load sample data
                if (fullAnnouncementList.isEmpty()) {
                    Log.d(TAG, "No announcements found, loading sample data");
                    loadSampleAnnouncements();
                    filterAnnouncements(selectedFilter);
                }
                
                // Update notification badge count for other activities
                updateNotificationCountForOtherActivities();
                
                // Update global notification manager
                AnnouncementNotificationManager.getInstance().updateAnnouncementCount(
                    AlertsActivity.this, fullAnnouncementList.size());
                
                // Hide badge since user is viewing alerts
                clearNotificationBadge();
                
                isRealtimeListenerActive = true;
                Log.d(TAG, "Real-time listener is now active");
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up real-time listener: " + e.getMessage(), e);
            // Fallback to sample data
            loadSampleAnnouncements();
            filterAnnouncements(selectedFilter);
        }
    }
    
    private void handleNewAnnouncement(QueryDocumentSnapshot doc, boolean isInitialLoad) {
        try {
            String docId = doc.getId();
            Log.d(TAG, "New announcement added: " + docId + " (Initial load: " + isInitialLoad + ")");
            
            // Extract image URL using helper method
            String urlValue = extractImageUrlFromDocument(doc);
            
            // Extract updatedAt field
            String updatedAt = extractUpdatedAtFromDocument(doc);
            
            Announcement newAnnouncement = new Announcement(
                    doc.getString("type"),
                    doc.getString("priority"),
                    doc.getString("description"),
                    doc.getString("date"),
                    urlValue,
                    updatedAt
            );
            
            Log.d(TAG, "New announcement imageUrl from Firestore: '" + urlValue + "'");
            
            // Add to the beginning of the list (newest first)
            fullAnnouncementList.add(0, newAnnouncement);
            
            // Only show notification for genuinely new announcements (not during initial load)
            if (!isInitialLoad) {
                showNewAnnouncementNotification(newAnnouncement);
                Log.d(TAG, "Toast notification shown for new announcement: " + newAnnouncement.type);
                
                // Apply filter and scroll to top to show the new announcement
                filterAnnouncements(selectedFilter);
                scrollToTop();
            } else {
                Log.d(TAG, "Skipping toast notification during initial load for: " + newAnnouncement.type);
            }
            
            // Update global notification manager
            AnnouncementNotificationManager.getInstance().updateAnnouncementCount(
                AlertsActivity.this, fullAnnouncementList.size());
            
            Log.d(TAG, "New announcement processed: " + newAnnouncement.type);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling new announcement: " + e.getMessage(), e);
        }
    }
    
    private void handleModifiedAnnouncement(QueryDocumentSnapshot doc) {
        try {
            String docId = doc.getId();
            Log.d(TAG, "Announcement modified: " + docId);
            
            // Extract image URL using helper method
            String urlValue = extractImageUrlFromDocument(doc);
            
            // Extract updatedAt field
            String updatedAt = extractUpdatedAtFromDocument(doc);
            
            Announcement modifiedAnnouncement = new Announcement(
                    doc.getString("type"),
                    doc.getString("priority"),
                    doc.getString("description"),
                    doc.getString("date"),
                    urlValue,
                    updatedAt
            );
            
            // Find and update the announcement in the list
            boolean found = false;
            for (int i = 0; i < fullAnnouncementList.size(); i++) {
                // Since we don't have a unique ID in our model, we'll match by content
                // In a real app, you'd want to add a document ID field to your Announcement model
                Announcement existing = fullAnnouncementList.get(i);
                if (existing.type.equals(modifiedAnnouncement.type) && 
                    existing.message.equals(modifiedAnnouncement.message)) {
                    fullAnnouncementList.set(i, modifiedAnnouncement);
                    found = true;
                    break;
                }
            }
            
            // If not found, add it as a new announcement
            if (!found) {
                fullAnnouncementList.add(0, modifiedAnnouncement);
                Log.d(TAG, "Modified announcement not found in list, added as new: " + modifiedAnnouncement.type);
            } else {
                Log.d(TAG, "Announcement updated: " + modifiedAnnouncement.type);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling modified announcement: " + e.getMessage(), e);
        }
    }
    
    private void handleRemovedAnnouncement(QueryDocumentSnapshot doc) {
        try {
            String docId = doc.getId();
            Log.d(TAG, "Announcement removed: " + docId);
            
            // Remove from list (matching by content since we don't have document ID)
            String removedType = doc.getString("type");
            String removedMessage = doc.getString("description");
            
            fullAnnouncementList.removeIf(ann -> 
                ann.type.equals(removedType) && ann.message.equals(removedMessage));
            
            Log.d(TAG, "Announcement removed from list");
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling removed announcement: " + e.getMessage(), e);
        }
    }
    
    private void showNewAnnouncementNotification(Announcement announcement) {
        try {
            // Show a toast notification for new announcements
            String notificationText = "New " + announcement.type + " announcement";
            if (announcement.priority != null && announcement.priority.equalsIgnoreCase("High")) {
                notificationText += " (HIGH PRIORITY)";
            }
            
            android.widget.Toast.makeText(this, notificationText, android.widget.Toast.LENGTH_LONG).show();
            
            Log.d(TAG, "Notification shown: " + notificationText);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Show announcement preview dialog with full details
     */
    private void showAnnouncementPreview(Announcement announcement) {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            android.view.LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_announcement_preview, null);
            
            // Find views in dialog
            TextView tvPreviewType = dialogView.findViewById(R.id.tvPreviewType);
            TextView tvPreviewPriority = dialogView.findViewById(R.id.tvPreviewPriority);
            TextView tvPreviewDate = dialogView.findViewById(R.id.tvPreviewDate);
            TextView tvPreviewMessage = dialogView.findViewById(R.id.tvPreviewMessage);
            ImageView ivAnnouncementImage = dialogView.findViewById(R.id.ivAnnouncementImage);
            LinearLayout priorityIndicatorBox = dialogView.findViewById(R.id.priorityIndicatorBox);
            Button btnClosePreview = dialogView.findViewById(R.id.btnClosePreview);
            
            // Set announcement data
            if (tvPreviewType != null) {
                tvPreviewType.setText(announcement.type != null ? announcement.type : "Unknown Type");
            }
            
            if (tvPreviewPriority != null) {
                tvPreviewPriority.setText(announcement.priority != null ? announcement.priority : "Medium");
                // Set background color and text color based on priority (matching report status badges)
                int bgRes = R.drawable.medium_priority_bg;
                int textColor = getResources().getColor(android.R.color.holo_orange_dark);
                if (announcement.priority != null) {
                    if ("High".equalsIgnoreCase(announcement.priority)) {
                        bgRes = R.drawable.high_priority_bg;
                        textColor = getResources().getColor(android.R.color.holo_red_dark);
                        // Update priority indicator box color for high priority
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFFFEBEE); // Light red
                        }
                    } else if ("Low".equalsIgnoreCase(announcement.priority)) {
                        bgRes = R.drawable.low_priority_bg;
                        textColor = getResources().getColor(android.R.color.holo_green_dark);
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFF1F8E9); // Light green
                        }
                    } else {
                        // Medium priority
                        textColor = getResources().getColor(android.R.color.holo_orange_dark);
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFFFF3E0); // Light orange
                        }
                    }
                }
                tvPreviewPriority.setBackgroundResource(bgRes);
                tvPreviewPriority.setTextColor(textColor);
            }
            
            if (tvPreviewDate != null) {
                // Use updatedAt if available, otherwise fall back to date field
                String dateTimeText = null;
                if (announcement.updatedAt != null && !announcement.updatedAt.isEmpty()) {
                    dateTimeText = announcement.updatedAt;
                    Log.d(TAG, "Using updatedAt for date/time display: '" + dateTimeText + "'");
                } else if (announcement.date != null && !announcement.date.isEmpty()) {
                    dateTimeText = announcement.date;
                    Log.d(TAG, "Using date field for date/time display: '" + dateTimeText + "'");
                }
                
                tvPreviewDate.setText(dateTimeText != null ? dateTimeText : "No date available");
            }
            
            if (tvPreviewMessage != null) {
                // Apply rich text formatting to message
                CharSequence formattedMessage = formatRichText(announcement.message != null ? announcement.message : "No details available");
                tvPreviewMessage.setText(formattedMessage);
                tvPreviewMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }
            
            // Create dialog first
            android.app.AlertDialog dialog = builder.setView(dialogView)
                    .setCancelable(true)
                    .create();
            
            // Close button
            if (btnClosePreview != null) {
                btnClosePreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            
            // Show dialog first
            dialog.show();
            
            // Load and display announcement image if available (after dialog is shown)
            LinearLayout imageContainer = dialogView.findViewById(R.id.imageContainer);
            final String imagePathOrUrl = announcement.imageUrl; // Store in final variable for use in listener
            
            Log.d(TAG, "=== IMAGE LOADING DEBUG ===");
            Log.d(TAG, "1. Announcement type: " + announcement.type);
            Log.d(TAG, "2. Image path/URL from announcement: '" + imagePathOrUrl + "'");
            Log.d(TAG, "3. ImageView (ivAnnouncementImage) is null: " + (ivAnnouncementImage == null));
            Log.d(TAG, "4. ImageContainer is null: " + (imageContainer == null));
            
            if (ivAnnouncementImage == null) {
                Log.e(TAG, "❌ ERROR: ImageView (ivAnnouncementImage) is NULL! Cannot display image.");
            }
            if (imageContainer == null) {
                Log.e(TAG, "❌ ERROR: ImageContainer is NULL! Cannot display image.");
            }
            
            if (ivAnnouncementImage != null && imageContainer != null) {
                Log.d(TAG, "5. Both ImageView and Container are NOT null - proceeding...");
                
                if (imagePathOrUrl != null && !imagePathOrUrl.isEmpty() && !imagePathOrUrl.trim().isEmpty()) {
                    // Show the image container and image view
                    Log.d(TAG, "6. ✅ Image path/URL is VALID: '" + imagePathOrUrl + "'");
                    Log.d(TAG, "7. Setting image container VISIBLE");
                    imageContainer.setVisibility(View.VISIBLE);
                    ivAnnouncementImage.setVisibility(View.VISIBLE);
                    Log.d(TAG, "8. Image container visibility set. Container visible: " + (imageContainer.getVisibility() == View.VISIBLE));
                    
                    // Set a placeholder while loading
                    Log.d(TAG, "9. Setting placeholder image");
                    ivAnnouncementImage.setImageResource(R.drawable.ic_camera_placeholder);
                    
                    // Use postDelayed to ensure dialog is fully rendered before loading image
                    Log.d(TAG, "10. Scheduling image load with 100ms delay");
                    ivAnnouncementImage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "11. Starting image load process...");
                            // Check if it's a Firebase Storage path or a full URL
                            if (isFirebaseStoragePath(imagePathOrUrl)) {
                                // It's a Storage path - convert to download URL
                                Log.d(TAG, "12. ✅ Detected Firebase Storage path. Converting to download URL: " + imagePathOrUrl);
                                loadImageFromStoragePath(ivAnnouncementImage, imagePathOrUrl, dialog);
                            } else {
                                // It's already a full URL - load directly
                                Log.d(TAG, "12. ✅ Detected full URL. Loading announcement image from URL: " + imagePathOrUrl);
                                loadImageFromUrl(ivAnnouncementImage, imagePathOrUrl);
                            }
                        }
                    }, 100); // Small delay to ensure dialog is rendered
                    
                    // Make image clickable to open fullscreen preview
                    ivAnnouncementImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // For fullscreen, we need the actual URL, so handle both cases
                            if (isFirebaseStoragePath(imagePathOrUrl)) {
                                // Get download URL first, then show fullscreen
                                getDownloadUrlFromStoragePath(imagePathOrUrl, new OnImageUrlReadyListener() {
                                    @Override
                                    public void onUrlReady(String downloadUrl) {
                                        showFullScreenAnnouncementImage(downloadUrl);
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        android.widget.Toast.makeText(AlertsActivity.this, "Unable to load image", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                showFullScreenAnnouncementImage(imagePathOrUrl);
                            }
                        }
                    });
                } else {
                    // Hide image container if no image URL
                    Log.d(TAG, "6. ❌ Image path/URL is NULL or EMPTY. Hiding image container.");
                    Log.d(TAG, "   Value was: '" + imagePathOrUrl + "'");
                    imageContainer.setVisibility(View.GONE);
                    ivAnnouncementImage.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "❌ ERROR: ImageView or imageContainer is null - cannot display image.");
                Log.e(TAG, "   ImageView is null: " + (ivAnnouncementImage == null));
                Log.e(TAG, "   ImageContainer is null: " + (imageContainer == null));
            }
            
            Log.d(TAG, "=== END IMAGE LOADING DEBUG ===");
            
            Log.d(TAG, "Announcement preview shown for: " + announcement.type + " with imagePath/URL: '" + imagePathOrUrl + "'");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing announcement preview", e);
            android.widget.Toast.makeText(this, "Error showing announcement details", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Interface for image URL ready callback
     */
    private interface OnImageUrlReadyListener {
        void onUrlReady(String downloadUrl);
        void onError(String error);
    }
    
    /**
     * Extract image URL from Firestore document
     * Handles both top-level fields and nested media structure (media.0.url)
     */
    private String extractImageUrlFromDocument(com.google.firebase.firestore.DocumentSnapshot doc) {
        String imageUrl = null;
        
        try {
            // First, try top-level fields
            imageUrl = doc.getString("url");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in top-level 'url' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            imageUrl = doc.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in top-level 'imageUrl' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            imageUrl = doc.getString("image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in top-level 'image' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            // If not found at top level, try nested media structure (media.0.url)
            Object mediaObj = doc.get("media");
            if (mediaObj != null) {
                Log.d(TAG, "Found 'media' field, extracting URL from nested structure");
                
                if (mediaObj instanceof Map) {
                    Map<String, Object> mediaMap = (Map<String, Object>) mediaObj;
                    Log.d(TAG, "Media is a Map with keys: " + mediaMap.keySet());
                    
                    // Try to get the first item (could be "0" or first key)
                    Object firstMediaItem = null;
                    if (mediaMap.containsKey("0")) {
                        firstMediaItem = mediaMap.get("0");
                        Log.d(TAG, "Found media item at key '0'");
                    } else if (!mediaMap.isEmpty()) {
                        // Get first entry
                        firstMediaItem = mediaMap.values().iterator().next();
                        Log.d(TAG, "Getting first media item from map");
                    }
                    
                    if (firstMediaItem != null && firstMediaItem instanceof Map) {
                        Map<String, Object> mediaItemMap = (Map<String, Object>) firstMediaItem;
                        Object urlObj = mediaItemMap.get("url");
                        if (urlObj != null) {
                            imageUrl = urlObj.toString();
                            Log.d(TAG, "✅ Found URL in media.0.url: '" + imageUrl + "'");
                            return imageUrl;
                        } else {
                            // Try path field and convert to download URL
                            Object pathObj = mediaItemMap.get("path");
                            if (pathObj != null) {
                                String path = pathObj.toString();
                                Log.d(TAG, "Found path in media.0.path: '" + path + "', will convert to URL");
                                return path; // Store path, will convert later
                            }
                        }
                    }
                } else if (mediaObj instanceof List) {
                    List<Object> mediaList = (List<Object>) mediaObj;
                    Log.d(TAG, "Media is a List with size: " + mediaList.size());
                    
                    if (!mediaList.isEmpty()) {
                        Object firstItem = mediaList.get(0);
                        if (firstItem instanceof Map) {
                            Map<String, Object> mediaItemMap = (Map<String, Object>) firstItem;
                            Object urlObj = mediaItemMap.get("url");
                            if (urlObj != null) {
                                imageUrl = urlObj.toString();
                                Log.d(TAG, "✅ Found URL in media[0].url: '" + imageUrl + "'");
                                return imageUrl;
                            } else {
                                Object pathObj = mediaItemMap.get("path");
                                if (pathObj != null) {
                                    String path = pathObj.toString();
                                    Log.d(TAG, "Found path in media[0].path: '" + path + "', will convert to URL");
                                    return path;
                                }
                            }
                        }
                    }
                }
            }
            
            // Try other field names as fallback
            imageUrl = doc.getString("imagePath");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in 'imagePath' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            imageUrl = doc.getString("image_url");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in 'image_url' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            imageUrl = doc.getString("photo");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in 'photo' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
            imageUrl = doc.getString("photoUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Found imageUrl in 'photoUrl' field: '" + imageUrl + "'");
                return imageUrl;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting image URL from document: " + e.getMessage(), e);
        }
        
        Log.d(TAG, "No imageUrl found in document");
        return null;
    }
    
    /**
     * Extract updatedAt timestamp from Firestore document
     * Handles Firestore Timestamp, Long, String, and Date formats
     */
    private String extractUpdatedAtFromDocument(com.google.firebase.firestore.DocumentSnapshot doc) {
        try {
            Object updatedAtObj = doc.get("updatedAt");
            
            if (updatedAtObj == null) {
                // Try alternative field names
                updatedAtObj = doc.get("updated_at");
                if (updatedAtObj == null) {
                    updatedAtObj = doc.get("createdAt");
                }
                if (updatedAtObj == null) {
                    updatedAtObj = doc.get("created_at");
                }
                if (updatedAtObj == null) {
                    updatedAtObj = doc.get("timestamp");
                }
            }
            
            if (updatedAtObj == null) {
                Log.d(TAG, "No updatedAt field found in document");
                return null;
            }
            
            String formattedDate = null;
            
            // Handle Firestore Timestamp
            if (updatedAtObj instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) updatedAtObj;
                Date date = timestamp.toDate();
                formattedDate = formatDateForDisplay(date);
                Log.d(TAG, "Found Firestore Timestamp, formatted: '" + formattedDate + "'");
                return formattedDate;
            }
            
            // Handle Long timestamp
            if (updatedAtObj instanceof Long) {
                Long timestamp = (Long) updatedAtObj;
                Date date = new Date(timestamp);
                formattedDate = formatDateForDisplay(date);
                Log.d(TAG, "Found Long timestamp, formatted: '" + formattedDate + "'");
                return formattedDate;
            }
            
            // Handle String date (like "November 19, 2025 at 3:18:23 AM UTC+8")
            if (updatedAtObj instanceof String) {
                String dateStr = (String) updatedAtObj;
                // Try to parse the string format from Firestore
                try {
                    // Try multiple date formats
                    SimpleDateFormat[] formats = {
                        new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ssa z", Locale.ENGLISH),
                        new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ssa z", Locale.ENGLISH),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()),
                        new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    };
                    
                    Date parsedDate = null;
                    for (SimpleDateFormat format : formats) {
                        try {
                            parsedDate = format.parse(dateStr);
                            if (parsedDate != null) {
                                break;
                            }
                        } catch (Exception e) {
                            // Try next format
                        }
                    }
                    
                    if (parsedDate != null) {
                        formattedDate = formatDateForDisplay(parsedDate);
                        Log.d(TAG, "Parsed string date, formatted: '" + formattedDate + "'");
                        return formattedDate;
                    } else {
                        // If parsing fails, return the string as-is
                        Log.d(TAG, "Could not parse date string, using as-is: '" + dateStr + "'");
                        return dateStr;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing date string: " + dateStr, e);
                    return dateStr; // Return as-is if parsing fails
                }
            }
            
            // Handle Date object
            if (updatedAtObj instanceof Date) {
                Date date = (Date) updatedAtObj;
                formattedDate = formatDateForDisplay(date);
                Log.d(TAG, "Found Date object, formatted: '" + formattedDate + "'");
                return formattedDate;
            }
            
            // If we can't handle the type, convert to string
            Log.w(TAG, "Unknown updatedAt type: " + (updatedAtObj != null ? updatedAtObj.getClass().getSimpleName() : "null"));
            return updatedAtObj != null ? updatedAtObj.toString() : null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting updatedAt from document: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Format Date object for display in announcement dialog
     */
    private String formatDateForDisplay(Date date) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage(), e);
            return date != null ? date.toString() : "Unknown date";
        }
    }
    
    /**
     * Check if the given string is a Firebase Storage path (not a full URL)
     */
    private boolean isFirebaseStoragePath(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            return false;
        }
        
        // If it starts with http:// or https://, it's a full URL
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return false;
        }
        
        // If it starts with gs://, it's a Storage path
        if (pathOrUrl.startsWith("gs://")) {
            return true;
        }
        
        // If it doesn't contain :// and looks like a path (contains /), it's likely a Storage path
        if (!pathOrUrl.contains("://") && pathOrUrl.contains("/")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Load image from Firebase Storage path
     */
    private void loadImageFromStoragePath(ImageView imageView, String storagePath, android.app.AlertDialog dialog) {
        try {
            Log.d(TAG, "Loading image from Firebase Storage path: " + storagePath);
            
            // Show placeholder while loading
            imageView.setImageResource(R.drawable.ic_camera_placeholder);
            
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef;
            
            // Handle different path formats
            if (storagePath.startsWith("gs://")) {
                // Full gs:// path
                storageRef = storage.getReferenceFromUrl(storagePath);
            } else {
                // Relative path (e.g., "announcements/image1.jpg")
                storageRef = storage.getReference().child(storagePath);
            }
            
            // Get download URL
            storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "✅ SUCCESS: Got download URL from Storage: " + downloadUrl);
                    
                    // Load image using ProfilePictureCache on UI thread
                    runOnUiThread(() -> {
                        try {
                            Log.d(TAG, "Loading image into ImageView using ProfilePictureCache");
                            ProfilePictureCache.getInstance().loadChatImage(imageView, downloadUrl);
                            Log.d(TAG, "✅ Image loading initiated from Storage URL: " + downloadUrl);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error loading image with ProfilePictureCache: " + e.getMessage(), e);
                            // Fallback: Try direct loading
                            loadImageDirectlyFromUrl(imageView, downloadUrl);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ ERROR: Failed to get download URL from Storage path: " + storagePath, e);
                    Log.e(TAG, "Error details: " + e.getMessage());
                    runOnUiThread(() -> {
                        imageView.setImageResource(R.drawable.ic_camera_placeholder);
                        android.widget.Toast.makeText(this, "Unable to load image: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    });
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in loadImageFromStoragePath: " + e.getMessage(), e);
            imageView.setImageResource(R.drawable.ic_camera_placeholder);
        }
    }
    
    /**
     * Get download URL from Firebase Storage path (for fullscreen preview)
     */
    private void getDownloadUrlFromStoragePath(String storagePath, OnImageUrlReadyListener listener) {
        try {
            Log.d(TAG, "Getting download URL from Firebase Storage path: " + storagePath);
            
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef;
            
            // Handle different path formats
            if (storagePath.startsWith("gs://")) {
                // Full gs:// path
                storageRef = storage.getReferenceFromUrl(storagePath);
            } else {
                // Relative path (e.g., "announcements/image1.jpg")
                storageRef = storage.getReference().child(storagePath);
            }
            
            // Get download URL
            storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Got download URL for fullscreen: " + downloadUrl);
                    listener.onUrlReady(downloadUrl);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL from Storage path: " + storagePath, e);
                    listener.onError("Unable to load image: " + e.getMessage());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in getDownloadUrlFromStoragePath: " + e.getMessage(), e);
            listener.onError("Error: " + e.getMessage());
        }
    }
    
    /**
     * Load image from full URL
     */
    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        try {
            Log.d(TAG, "Loading image from URL: " + imageUrl);
            
            // Use post to ensure view is attached before loading
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Loading image into ImageView using ProfilePictureCache for URL: " + imageUrl);
                        ProfilePictureCache.getInstance().loadChatImage(imageView, imageUrl);
                        Log.d(TAG, "✅ Image loading initiated for URL: " + imageUrl);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error loading image with ProfilePictureCache: " + e.getMessage(), e);
                        // Fallback: Try direct loading
                        loadImageDirectlyFromUrl(imageView, imageUrl);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in loadImageFromUrl: " + e.getMessage(), e);
            imageView.setImageResource(R.drawable.ic_camera_placeholder);
        }
    }
    
    /**
     * Fallback method to load image directly from URL (without cache)
     */
    private void loadImageDirectlyFromUrl(ImageView imageView, String imageUrl) {
        try {
            Log.d(TAG, "Loading announcement image directly from URL: " + imageUrl);
            
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    
                    runOnUiThread(() -> {
                        if (bitmap != null && imageView != null) {
                            imageView.setImageBitmap(bitmap);
                            Log.d(TAG, "Announcement image loaded successfully");
                        } else {
                            Log.e(TAG, "Failed to load announcement image - bitmap is null");
                            if (imageView != null) {
                                imageView.setImageResource(R.drawable.ic_camera_placeholder);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading announcement image directly: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        if (imageView != null) {
                            imageView.setImageResource(R.drawable.ic_camera_placeholder);
                        }
                    });
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in loadAnnouncementImageDirectly: " + e.getMessage(), e);
            if (imageView != null) {
                imageView.setImageResource(R.drawable.ic_camera_placeholder);
            }
        }
    }
    
    /**
     * Show fullscreen image preview for announcement image
     */
    private void showFullScreenAnnouncementImage(String imageUrl) {
        try {
            Log.d(TAG, "Showing fullscreen announcement image: " + imageUrl);
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            android.view.LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_fullscreen_image, null);
            
            ImageView fullScreenImageView = dialogView.findViewById(R.id.fullScreenImageView);
            if (fullScreenImageView != null && imageUrl != null && !imageUrl.isEmpty()) {
                // Load image from URL using ProfilePictureCache
                ProfilePictureCache.getInstance().loadChatImage(fullScreenImageView, imageUrl);
                Log.d(TAG, "Loading fullscreen image from URL: " + imageUrl);
            } else {
                Log.w(TAG, "Cannot show fullscreen image - imageUrl is null or empty");
                android.widget.Toast.makeText(this, "Image not available", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create dialog
            android.app.AlertDialog dialog = builder.setView(dialogView)
                    .setCancelable(true)
                    .create();
            
            // Make image clickable to close dialog
            if (fullScreenImageView != null) {
                fullScreenImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            
            dialog.show();
            
            Log.d(TAG, "Fullscreen announcement image dialog shown");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing fullscreen announcement image", e);
            android.widget.Toast.makeText(this, "Error showing image", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateNotificationCountForOtherActivities() {
        try {
            // Save the current total count for other activities to check
            int currentCount = fullAnnouncementList.size();
            sharedPreferences.edit()
                .putInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, currentCount)
                .apply();
            
            Log.d(TAG, "Notification count updated for other activities: " + currentCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification count: " + e.getMessage(), e);
        }
    }
    
    private void loadSampleAnnouncements() {
        fullAnnouncementList.clear();
        
        // Add sample announcements for testing
        fullAnnouncementList.add(new Announcement("Weather Warning", "High", "Heavy rainfall expected in the next 24 hours. Please stay indoors.", "Today"));
        fullAnnouncementList.add(new Announcement("Flood", "High", "Flood warning issued for low-lying areas. Evacuation may be necessary.", "Today"));
        fullAnnouncementList.add(new Announcement("Road Closure", "Medium", "Main Street closed due to construction work. Use alternative routes.", "Yesterday"));
        fullAnnouncementList.add(new Announcement("Earthquake", "High", "Earthquake detected. Please follow safety protocols.", "2 days ago"));
        fullAnnouncementList.add(new Announcement("Informational", "Low", "Community meeting scheduled for next week.", "3 days ago"));
        fullAnnouncementList.add(new Announcement("Landslide", "High", "Landslide risk in mountainous areas. Avoid travel if possible.", "4 days ago"));
        fullAnnouncementList.add(new Announcement("Missing Person", "High", "Search ongoing for missing elderly person. Contact authorities if seen.", "5 days ago"));
        fullAnnouncementList.add(new Announcement("Evacuation Order", "High", "Immediate evacuation ordered for Zone A residents.", "1 week ago"));
        
        android.util.Log.d("AlertsActivity", "Loaded " + fullAnnouncementList.size() + " sample announcements");
    }

    // Announcement model
    public static class Announcement {
        public String type, priority, message, date, imageUrl, updatedAt;
        public Announcement(String type, String priority, String message, String date) {
            this.type = type != null ? type : "Announcement";
            this.priority = priority != null ? priority : "Medium";
            this.message = message != null ? message : "";
            this.date = date != null ? date : "";
            this.imageUrl = null;
            this.updatedAt = null;
        }
        
        public Announcement(String type, String priority, String message, String date, String imageUrl) {
            this.type = type != null ? type : "Announcement";
            this.priority = priority != null ? priority : "Medium";
            this.message = message != null ? message : "";
            this.date = date != null ? date : "";
            this.imageUrl = imageUrl;
            this.updatedAt = null;
        }
        
        public Announcement(String type, String priority, String message, String date, String imageUrl, String updatedAt) {
            this.type = type != null ? type : "Announcement";
            this.priority = priority != null ? priority : "Medium";
            this.message = message != null ? message : "";
            this.date = date != null ? date : "";
            this.imageUrl = imageUrl;
            this.updatedAt = updatedAt;
        }
    }

    // RecyclerView Adapter
    public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
        private List<Announcement> announcements;
        private OnAnnouncementClickListener onAnnouncementClickListener;
        
        public interface OnAnnouncementClickListener {
            void onAnnouncementClick(Announcement announcement);
        }
        
        public AnnouncementAdapter(List<Announcement> announcements) {
            this.announcements = announcements != null ? announcements : new ArrayList<>();
        }
        
        public void setOnAnnouncementClickListener(OnAnnouncementClickListener listener) {
            this.onAnnouncementClickListener = listener;
        }
        
        @Override
        public AnnouncementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_announcement, parent, false);
            return new AnnouncementViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(AnnouncementViewHolder holder, int position) {
            try {
                Announcement ann = announcements.get(position);
                if (ann != null) {
                    // Set text safely with null checks
                    holder.tvType.setText(ann.type != null ? ann.type : "Unknown Type");
                    holder.tvPriority.setText(ann.priority != null ? ann.priority : "Medium");
                    // Apply rich text formatting to message
                    CharSequence formattedMessage = formatRichText(ann.message != null ? ann.message : "No message");
                    holder.tvMessage.setText(formattedMessage);
                    holder.tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.tvDate.setText(ann.date != null ? ann.date : "No date");
                    
                    // Set background color and text color based on priority (matching report status badges)
                    int bgRes = R.drawable.medium_priority_bg;
                    int textColor = getResources().getColor(android.R.color.holo_orange_dark);
                    if (ann.priority != null) {
                        if ("High".equalsIgnoreCase(ann.priority)) {
                            bgRes = R.drawable.high_priority_bg;
                            textColor = getResources().getColor(android.R.color.holo_red_dark);
                        } else if ("Low".equalsIgnoreCase(ann.priority)) {
                            bgRes = R.drawable.low_priority_bg;
                            textColor = getResources().getColor(android.R.color.holo_green_dark);
                        } else {
                            // Medium priority
                            textColor = getResources().getColor(android.R.color.holo_orange_dark);
                        }
                    }
                    holder.tvPriority.setBackgroundResource(bgRes);
                    holder.tvPriority.setTextColor(textColor);
                    
                    // Set click listener on item view
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onAnnouncementClickListener != null) {
                                onAnnouncementClickListener.onAnnouncementClick(ann);
                            }
                        }
                    });
                    
                    // Make item view clickable
                    holder.itemView.setClickable(true);
                    holder.itemView.setFocusable(true);
                } else {
                    // Handle null announcement
                    holder.tvType.setText("Error");
                    holder.tvPriority.setText("Error");
                    holder.tvMessage.setText("Error loading announcement");
                    holder.tvDate.setText("Error");
                }
            } catch (Exception e) {
                android.util.Log.e("AnnouncementAdapter", "Error binding announcement at position " + position, e);
                holder.tvType.setText("Error");
                holder.tvPriority.setText("Error");
                holder.tvMessage.setText("Error loading announcement");
                holder.tvDate.setText("Error");
            }
        }
        
        @Override
        public int getItemCount() { 
            return announcements != null ? announcements.size() : 0; 
        }
        
        // Method to update the list
        public void updateAnnouncements(List<Announcement> newAnnouncements) {
            this.announcements = newAnnouncements != null ? newAnnouncements : new ArrayList<>();
            notifyDataSetChanged();
        }
        
        class AnnouncementViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvPriority, tvMessage, tvDate;
            
            AnnouncementViewHolder(View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvAnnouncementType);
                tvPriority = itemView.findViewById(R.id.tvAnnouncementPriority);
                tvMessage = itemView.findViewById(R.id.tvAnnouncementMessage);
                tvDate = itemView.findViewById(R.id.tvAnnouncementDate);
            }
        }
    }
    
    private void loadUserProfilePicture() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && profileIcon != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                    .whereEqualTo("firebaseUid", user.getUid())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            String profilePictureUrl = doc.getString("profilePictureUrl");
                            Log.d(TAG, "Found profile picture URL: " + profilePictureUrl);
                            
                            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                // Use cached loading for instant display
                                ProfilePictureCache.getInstance().loadProfilePicture(profileIcon, profilePictureUrl);
                            } else {
                                Log.d(TAG, "No profile picture URL found in Firestore");
                                // Try to check if profile picture exists in Firebase Storage
                                checkProfilePictureInStorage(user.getUid());
                            }
                        } else {
                            Log.d(TAG, "No user document found for firebaseUid: " + user.getUid());
                            setDefaultProfileIcon();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading profile picture", e);
                        setDefaultProfileIcon();
                    });
            } else {
                setDefaultProfileIcon();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfilePicture: " + e.getMessage(), e);
            setDefaultProfileIcon();
        }
    }

    private void checkProfilePictureInStorage(String firebaseUid) {
        try {
            // Try to construct the profile picture path and check if it exists
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference profileRef = storage.getReference().child("profile_pictures/" + firebaseUid + "/profile.jpg");
            
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Found profile picture in Storage: " + uri.toString());
                // Use cached loading for instant display
                ProfilePictureCache.getInstance().loadProfilePicture(profileIcon, uri.toString());
                // Update Firestore with the found URL
                updateProfilePictureUrlInFirestore(uri.toString());
            }).addOnFailureListener(e -> {
                Log.d(TAG, "No profile picture found in Storage for UID: " + firebaseUid);
                // Set default profile icon if no picture found
                setDefaultProfileIcon();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking profile picture in storage: " + e.getMessage(), e);
            setDefaultProfileIcon();
        }
    }

    private void updateProfilePictureUrlInFirestore(String profilePictureUrl) {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("firebaseUid", user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String docId = doc.getId();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profilePictureUrl", profilePictureUrl);
                        
                        db.collection("users").document(docId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Profile picture URL updated in Firestore");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating profile picture URL in Firestore", e);
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying user document for update", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile picture URL in Firestore: " + e.getMessage(), e);
        }
    }

    private void loadProfileImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profileIcon != null) {
                        // Create circular bitmap
                        android.graphics.Bitmap circularBitmap = createCircularProfileBitmap(bitmap);
                        profileIcon.setImageBitmap(circularBitmap);
                        Log.d(TAG, "Profile picture loaded successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL", e);
                runOnUiThread(() -> setDefaultProfileIcon());
            }
        }).start();
    }

    private android.graphics.Bitmap createCircularProfileBitmap(android.graphics.Bitmap bitmap) {
        try {
            // Center-crop to square first to avoid distortion
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int squareSize = Math.min(width, height);
            int xOffset = (width - squareSize) / 2;
            int yOffset = (height - squareSize) / 2;

            android.graphics.Bitmap squareCropped = android.graphics.Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

            int targetSize = 150; // Size for profile icon
            android.graphics.Bitmap scaledSquare = squareSize == targetSize
                    ? squareCropped
                    : android.graphics.Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

            android.graphics.Bitmap circularBitmap = android.graphics.Bitmap.createBitmap(targetSize, targetSize, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(circularBitmap);

            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            android.graphics.Path path = new android.graphics.Path();
            path.addCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, android.graphics.Path.Direction.CW);
            canvas.clipPath(path);
            canvas.drawBitmap(scaledSquare, 0, 0, paint);

            if (scaledSquare != squareCropped) {
                scaledSquare.recycle();
            }
            squareCropped.recycle();

            return circularBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error creating circular bitmap: " + e.getMessage(), e);
            return bitmap; // Return original bitmap if circular conversion fails
        }
    }

    private void setDefaultProfileIcon() {
        try {
            if (profileIcon != null) {
                profileIcon.setImageResource(R.drawable.ic_person);
                Log.d(TAG, "Default profile icon set");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting default profile icon: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Refresh profile picture when returning to this activity
            loadUserProfilePicture();
            
            // Hide alerts badge since user is actively viewing alerts
            clearNotificationBadge();
            
            // ✅ NEW: Update chat badge (show unread chat messages)
            updateChatBadge();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        try {
            // Save last visit time when leaving the activity
            saveLastVisitTime();
            
            // Don't remove the listener here - keep it active for real-time updates
            // The listener will continue to work in the background
            Log.d(TAG, "Activity paused - real-time listener remains active");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update chat notification badge
     * ✅ Shows unread chat messages count on the chat tab
     */
    private void updateChatBadge() {
        try {
            if (chatBadgeAlerts == null) {
                Log.w(TAG, "Chat badge view is null");
                return;
            }
            
            // Use ChatBadgeManager to update the badge
            ChatBadgeManager.getInstance().updateChatBadge(this, chatBadgeAlerts);
            
            Log.d(TAG, "Chat badge updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating chat badge: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Unregister badge from notification manager
            AnnouncementNotificationManager.getInstance().unregisterBadge("AlertsActivity");
            
            // Remove the real-time listener when activity is destroyed
            if (announcementListener != null) {
                announcementListener.remove();
                announcementListener = null;
                isRealtimeListenerActive = false;
                Log.d(TAG, "Real-time listener removed in onDestroy");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadge == null) return;
            
            int newAnnouncementCount = countNewAnnouncements();
            
            if (newAnnouncementCount > 0) {
                alertsBadge.setText(String.valueOf(newAnnouncementCount));
                alertsBadge.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing badge with count: " + newAnnouncementCount);
            } else {
                alertsBadge.setVisibility(View.GONE);
                Log.d(TAG, "Hiding badge - no new announcements");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge: " + e.getMessage(), e);
        }
    }
    
    // Method to update badge from other activities
    public void updateBadgeFromRealTime(int totalAnnouncementCount) {
        try {
            runOnUiThread(() -> {
                int lastCount = sharedPreferences.getInt(KEY_TOTAL_ANNOUNCEMENT_COUNT, 0);
                int newCount = totalAnnouncementCount - lastCount;
                
                if (newCount > 0 && alertsBadge != null) {
                    alertsBadge.setText(String.valueOf(newCount));
                    alertsBadge.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Badge updated from real-time: " + newCount + " new announcements");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge from real-time: " + e.getMessage(), e);
        }
    }
    
    private void clearNotificationBadge() {
        try {
            if (alertsBadge != null) {
                alertsBadge.setVisibility(View.GONE);
                Log.d(TAG, "Badge cleared - user visited alerts screen");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing notification badge: " + e.getMessage(), e);
        }
    }
    
    private int countNewAnnouncements() {
        try {
            long lastVisitTime = sharedPreferences.getLong(KEY_LAST_VISIT_TIME, 0);
            int newCount = 0;
            
            // If this is the first visit, don't show any badges
            if (lastVisitTime == 0) {
                return 0;
            }
            
            // Count announcements that are newer than last visit
            for (Announcement announcement : fullAnnouncementList) {
                if (isAnnouncementNew(announcement, lastVisitTime)) {
                    newCount++;
                }
            }
            
            Log.d(TAG, "Found " + newCount + " new announcements since last visit");
            return newCount;
        } catch (Exception e) {
            Log.e(TAG, "Error counting new announcements: " + e.getMessage(), e);
            return 0;
        }
    }
    
    private boolean isAnnouncementNew(Announcement announcement, long lastVisitTime) {
        try {
            // Parse announcement date
            String dateStr = announcement.date;
            if (dateStr == null || dateStr.isEmpty()) {
                return false;
            }
            
            long announcementTime = parseAnnouncementDate(dateStr);
            if (announcementTime == 0) {
                return false;
            }
            
            // Announcement is new if it's created after last visit
            return announcementTime > lastVisitTime;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if announcement is new: " + e.getMessage(), e);
            return false;
        }
    }
    
    private long parseAnnouncementDate(String dateStr) {
        try {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
            // Handle different date formats
            if (dateStr.toLowerCase().contains("today")) {
                return currentDate.getTime();
            } else if (dateStr.toLowerCase().contains("yesterday")) {
                return currentDate.getTime() - TimeUnit.DAYS.toMillis(1);
            } else if (dateStr.toLowerCase().contains("days ago")) {
                String[] parts = dateStr.split(" ");
                if (parts.length > 0) {
                    try {
                        int days = Integer.parseInt(parts[0]);
                        return currentDate.getTime() - TimeUnit.DAYS.toMillis(days);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            } else if (dateStr.toLowerCase().contains("week ago")) {
                return currentDate.getTime() - TimeUnit.DAYS.toMillis(7);
            } else {
                // Try to parse as standard date format
                try {
                    Date parsedDate = sdf.parse(dateStr);
                    return parsedDate != null ? parsedDate.getTime() : 0;
                } catch (Exception e) {
                    // If parsing fails, assume it's new
                    return currentDate.getTime();
                }
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing announcement date: " + e.getMessage(), e);
            return 0;
        }
    }
    
    private void saveLastVisitTime() {
        try {
            long currentTime = System.currentTimeMillis();
            sharedPreferences.edit()
                .putLong(KEY_LAST_VISIT_TIME, currentTime)
                .putInt(KEY_LAST_ANNOUNCEMENT_COUNT, fullAnnouncementList.size())
                .apply();
            Log.d(TAG, "Last visit time saved: " + currentTime);
        } catch (Exception e) {
            Log.e(TAG, "Error saving last visit time: " + e.getMessage(), e);
        }
    }
    
    private void scrollToTop() {
        try {
            if (announcementsRecyclerView != null) {
                // Smoothly scroll to the top to show the new announcement
                announcementsRecyclerView.smoothScrollToPosition(0);
                Log.d(TAG, "Scrolled to top to show new announcement");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scrolling to top: " + e.getMessage(), e);
        }
    }
    
    // Static method to update badge from other activities
    public static void updateBadgeFromOtherActivity(android.content.Context context, int newAnnouncementCount) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
            long lastVisitTime = prefs.getLong(KEY_LAST_VISIT_TIME, 0);
            
            if (lastVisitTime == 0) {
                // First time, don't show badge
                return;
            }
            
            // You can implement logic here to update badge from other activities
            // For now, we'll rely on the badge being updated when AlertsActivity is created
            Log.d(TAG, "Badge update requested from other activity: " + newAnnouncementCount);
        } catch (Exception e) {
            Log.e(TAG, "Error updating badge from other activity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Format rich text with support for bold, italic, underline, lists, and links
     * Supports:
     * - **bold** or <b>bold</b>
     * - *italic* or <i>italic</i>
     * - __underline__ or <u>underline</u>
     * - Numbered lists (1. item)
     * - Bulleted lists (- item or * item)
     * - Links [text](url) or <a href="url">text</a>
     */
    private CharSequence formatRichText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        try {
            // First, format lists
            String processedText = formatLists(text);
            
            // Convert markdown to HTML for easier processing
            processedText = convertMarkdownToHtml(processedText);
            
            // Use Html.fromHtml to handle HTML tags
            CharSequence htmlFormatted = Html.fromHtml(processedText, Html.FROM_HTML_MODE_LEGACY);
            SpannableString spannable = new SpannableString(htmlFormatted);
            
            // Auto-linkify URLs and email addresses
            Linkify.addLinks(spannable, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
            
            return spannable;
            
        } catch (Exception e) {
            Log.e(TAG, "Error formatting rich text: " + e.getMessage(), e);
            return text; // Return original text on error
        }
    }
    
    /**
     * Convert markdown-style formatting to HTML
     */
    private String convertMarkdownToHtml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // Handle markdown links: [text](url) -> <a href="url">text</a>
            text = text.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>");
            
            // Handle bold: **text** -> <b>text</b> (but preserve if already HTML)
            text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
            
            // Handle underline: __text__ -> <u>text</u> (but not if it's bold)
            text = text.replaceAll("(?<!<b>)(?<!</b>)(?<!<u>)__(?![*])([^_]+)__(?!</u>)", "<u>$1</u>");
            
            // Handle italic: *text* -> <i>text</i> (but not **text** which is bold)
            text = text.replaceAll("(?<!\\*)\\*(?!\\*)([^*]+)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");
            
            return text;
        } catch (Exception e) {
            Log.e(TAG, "Error converting markdown to HTML: " + e.getMessage(), e);
            return text;
        }
    }
    
    /**
     * Format numbered and bulleted lists
     */
    private String formatLists(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            StringBuilder result = new StringBuilder();
            String[] lines = text.split("\n");
            boolean inList = false;
            
            for (String line : lines) {
                String trimmedLine = line.trim();
                
                // Numbered list: 1. item, 2. item, etc.
                if (Pattern.matches("^\\d+\\.\\s+.+", trimmedLine)) {
                    if (!inList) {
                        result.append("\n");
                        inList = true;
                    }
                    result.append("• ").append(trimmedLine.replaceFirst("^\\d+\\.\\s+", "")).append("\n");
                }
                // Bulleted list: - item or * item (but not markdown bold/italic)
                else if (Pattern.matches("^[-]\\s+.+", trimmedLine) || 
                        (Pattern.matches("^\\*\\s+.+", trimmedLine) && !trimmedLine.startsWith("**"))) {
                    if (!inList) {
                        result.append("\n");
                        inList = true;
                    }
                    result.append("• ").append(trimmedLine.replaceFirst("^[-*]\\s+", "")).append("\n");
                }
                else {
                    if (inList && !trimmedLine.isEmpty()) {
                        result.append("\n");
                        inList = false;
                    }
                    result.append(line).append("\n");
                }
            }
            
            return result.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "Error formatting lists: " + e.getMessage(), e);
            return text;
        }
    }
}
