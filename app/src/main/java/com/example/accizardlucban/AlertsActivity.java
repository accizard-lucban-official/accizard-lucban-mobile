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
                            Announcement announcement = new Announcement(
                                    docSnapshot.getString("type"),
                                    docSnapshot.getString("priority"),
                                    docSnapshot.getString("description"),
                                    docSnapshot.getString("date")
                            );
                            fullAnnouncementList.add(announcement);
                            Log.d(TAG, "Added announcement during initial load: " + announcement.type);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing announcement during initial load", e);
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
            
            Announcement newAnnouncement = new Announcement(
                    doc.getString("type"),
                    doc.getString("priority"),
                    doc.getString("description"),
                    doc.getString("date")
            );
            
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
            
            Announcement modifiedAnnouncement = new Announcement(
                    doc.getString("type"),
                    doc.getString("priority"),
                    doc.getString("description"),
                    doc.getString("date")
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
            LinearLayout priorityIndicatorBox = dialogView.findViewById(R.id.priorityIndicatorBox);
            Button btnClosePreview = dialogView.findViewById(R.id.btnClosePreview);
            
            // Set announcement data
            if (tvPreviewType != null) {
                tvPreviewType.setText(announcement.type != null ? announcement.type : "Unknown Type");
            }
            
            if (tvPreviewPriority != null) {
                tvPreviewPriority.setText(announcement.priority != null ? announcement.priority : "Medium");
                // Set background color based on priority
                int bgRes = R.drawable.medium_priority_bg;
                if (announcement.priority != null) {
                    if ("High".equalsIgnoreCase(announcement.priority)) {
                        bgRes = R.drawable.high_priority_bg;
                        // Update priority indicator box color for high priority
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFFFEBEE); // Light red
                        }
                    } else if ("Low".equalsIgnoreCase(announcement.priority)) {
                        bgRes = R.drawable.low_priority_bg;
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFF1F8E9); // Light green
                        }
                    } else {
                        if (priorityIndicatorBox != null) {
                            priorityIndicatorBox.setBackgroundColor(0xFFFFF3E0); // Light orange
                        }
                    }
                }
                tvPreviewPriority.setBackgroundResource(bgRes);
            }
            
            if (tvPreviewDate != null) {
                tvPreviewDate.setText(announcement.date != null ? announcement.date : "No date available");
            }
            
            if (tvPreviewMessage != null) {
                tvPreviewMessage.setText(announcement.message != null ? announcement.message : "No details available");
            }
            
            // Create dialog
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
            
            dialog.show();
            
            Log.d(TAG, "Announcement preview shown for: " + announcement.type);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing announcement preview", e);
            android.widget.Toast.makeText(this, "Error showing announcement details", android.widget.Toast.LENGTH_SHORT).show();
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
        public String type, priority, message, date;
        public Announcement(String type, String priority, String message, String date) {
            this.type = type != null ? type : "Announcement";
            this.priority = priority != null ? priority : "Medium";
            this.message = message != null ? message : "";
            this.date = date != null ? date : "";
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
                    holder.tvMessage.setText(ann.message != null ? ann.message : "No message");
                    holder.tvDate.setText(ann.date != null ? ann.date : "No date");
                    
                    // Set background color based on priority
                    int bgRes = R.drawable.medium_priority_bg;
                    if (ann.priority != null) {
                        if ("High".equalsIgnoreCase(ann.priority)) {
                            bgRes = R.drawable.high_priority_bg;
                        } else if ("Low".equalsIgnoreCase(ann.priority)) {
                            bgRes = R.drawable.low_priority_bg;
                        }
                    }
                    holder.tvPriority.setBackgroundResource(bgRes);
                    
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
}
