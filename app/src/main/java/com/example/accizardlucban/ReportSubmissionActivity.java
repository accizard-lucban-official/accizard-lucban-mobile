package com.example.accizardlucban;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.VideoView;
import android.widget.MediaController;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import android.net.Uri;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.example.accizardlucban.StorageHelper;
import android.view.animation.AnimationUtils;
import android.content.SharedPreferences;

// Location services imports
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

// Use the Report class from models package
import com.example.accizardlucban.models.Report;

import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileOutputStream;

public class ReportSubmissionActivity extends AppCompatActivity {

    private static final String TAG = "ReportSubmissionActivity";
    private static final String PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    private static final String KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT = "last_viewed_announcement_count";
    private static final String USER_PREFS_NAME = "user_profile_prefs";
    
    // Location permission request code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private Spinner reportTypeSpinner;
    private EditText descriptionEditText;
    private EditText locationEditText; // Legacy field (hidden)
    private EditText coordinatesEditText; // Coordinates field for map picker
    private ImageView pinningButton;
    private CardView uploadImagesButton;
    private Button takePhotoButton;
    private Button submitReportButton;
    private ImageButton profileButton;
    private CheckBox patientCheckbox;
    private ImageView locationInfoIcon;
    private ImageView reportTypeInfoIcon;
    private RecyclerView reportLogRecyclerView;
    private Spinner reportLogTypeFilterSpinner;
    private Spinner reportLogStatusFilterSpinner;
    private RecyclerView imageGalleryRecyclerView;
    private LinearLayout placeholderContainer;
    
    // Tab Components
    private LinearLayout submitReportTab;
    private LinearLayout reportLogTab;
    private ScrollView submitReportContent;
    private ScrollView reportLogContent;
    private View submitReportIndicator;
    private View reportLogIndicator;
    private TextView alertsBadgeReport;
    private TextView chatBadgeReport; // Chat notification badge
    private SharedPreferences sharedPreferences;
    
    // Real-time listener for chat badge
    private com.google.firebase.firestore.ListenerRegistration chatBadgeListener;
    
    // Status count TextViews
    private TextView pendingCountText;
    private TextView ongoingCountText;
    private TextView respondedCountText;
    private TextView unrespondedCountText;
    private TextView redundantCountText;
    private TextView falseReportCountText;
    private TextView totalCountText;
    
    private static final int IMAGE_PICK_REQUEST = 2001;
    private static final int VIDEO_PICK_REQUEST = 2004;
    private static final int VIDEO_RECORD_REQUEST = 2005;
    private static final int CAMERA_REQUEST_CODE = 2002;
    // Using PermissionHelper's request codes
    private static final int CAMERA_PERMISSION_REQUEST_CODE = PermissionHelper.CAMERA_PERMISSION_REQUEST_CODE;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = PermissionHelper.STORAGE_PERMISSION_REQUEST_CODE;
    private Uri selectedImageUri;
    private List<Uri> selectedImageUris = new ArrayList<>(); // Keep for backward compatibility
    private List<MediaItem> selectedMediaItems = new ArrayList<>(); // New: supports both images and videos
    private boolean pendingVideoRecording = false; // Track if video recording is pending permission

    // Bottom Navigation
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private FrameLayout reportTab; // Changed to FrameLayout for circular button design
    private LinearLayout mapTab;
    private LinearLayout alertsTab;

    // Firebase
    private FirebaseAuth mAuth;
    
    // Location services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    
    // Report Log Data
    private ReportLogAdapter reportLogAdapter;
    private List<Report> allReports;
    private List<Report> filteredReports;
    
    // Media Gallery Data (supports both images and videos)
    private MediaGalleryAdapter mediaGalleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_submission);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize SharedPreferences for badge
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize location services
        initializeLocationServices();

        // Initialize UI components
        initializeViews();

        // Setup spinner
        setupReportTypeSpinner();

        // Setup RecyclerView
        setupReportLogRecyclerView();

        // Setup filter spinners
        setupReportLogFilterSpinners();
        
        // Setup professional image gallery
        setupProfessionalImageGallery();

        // Setup click listeners
        setupClickListeners();
        
        // Setup tab functionality
        setupTabFunctionality();
        
        // Load user profile information
        loadUserProfileInformation();
        
        // Load user profile picture
        loadUserProfilePicture();
        
        // Load user reports from Firestore
        loadUserReportsFromFirestore();
    }

    private void initializeViews() {
        // Form components
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText); // Legacy field (hidden)
        coordinatesEditText = findViewById(R.id.coordinatesEditText); // Coordinates field for map picker
        pinningButton = findViewById(R.id.pinningButton);
        // getCurrentLocationButton removed - functionality moved to bottom sheet
        uploadImagesButton = findViewById(R.id.uploadImagesButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        submitReportButton = findViewById(R.id.submitReportButton);
        profileButton = findViewById(R.id.profile);
        patientCheckbox = findViewById(R.id.patientCheckbox);
        locationInfoIcon = findViewById(R.id.locationInfoIcon);
        reportTypeInfoIcon = findViewById(R.id.reportTypeInfoIcon);
        reportLogRecyclerView = findViewById(R.id.reportLogRecyclerView);
        reportLogTypeFilterSpinner = findViewById(R.id.reportLogTypeFilterSpinner);
        reportLogStatusFilterSpinner = findViewById(R.id.reportLogStatusFilterSpinner);
        imageGalleryRecyclerView = findViewById(R.id.imageGalleryRecyclerView);
        placeholderContainer = findViewById(R.id.placeholder_container);
        
        // Tab components
        submitReportTab = findViewById(R.id.submitReportTab);
        reportLogTab = findViewById(R.id.reportLogTab);
        submitReportContent = findViewById(R.id.submitReportContent);
        reportLogContent = findViewById(R.id.reportLogContent);
        submitReportIndicator = findViewById(R.id.submitReportIndicator);
        reportLogIndicator = findViewById(R.id.reportLogIndicator);
        
        // Status count TextViews
        pendingCountText = findViewById(R.id.pendingCountText);
        ongoingCountText = findViewById(R.id.ongoingCountText);
        respondedCountText = findViewById(R.id.respondedCountText);
        unrespondedCountText = findViewById(R.id.unrespondedCountText);
        redundantCountText = findViewById(R.id.redundantCountText);
        falseReportCountText = findViewById(R.id.falseReportCountText);
        totalCountText = findViewById(R.id.totalCountText);

        // Bottom navigation
        homeTab = findViewById(R.id.homeTab);
        chatTab = findViewById(R.id.chatTab);
        reportTab = findViewById(R.id.reportTab);
        mapTab = findViewById(R.id.mapTab);
        alertsTab = findViewById(R.id.alertsTab);
        alertsBadgeReport = findViewById(R.id.alerts_badge_report);
        chatBadgeReport = findViewById(R.id.chat_badge_report);
        
        // Register badge with AnnouncementNotificationManager so it gets updated when alerts are viewed
        if (alertsBadgeReport != null) {
            AnnouncementNotificationManager.getInstance().registerBadge("ReportSubmissionActivity", alertsBadgeReport);
            Log.d(TAG, "✅ ReportSubmissionActivity badge registered with AnnouncementNotificationManager");
        }
        
        // Initialize chat badge as hidden and setup real-time listener
        if (chatBadgeReport != null) {
            chatBadgeReport.setVisibility(View.GONE);
            chatBadgeReport.setText("0");
            Log.d(TAG, "✅ Chat badge initialized in ReportSubmissionActivity");
            
            // Setup real-time listener for chat badge updates
            setupChatBadgeListener();
            
            // ✅ NEW: Update chat badge immediately (like alerts badge)
            ChatBadgeManager.getInstance().updateChatBadge(this, chatBadgeReport);
            Log.d(TAG, "✅ Chat badge updated immediately in ReportSubmissionActivity");
        }
        
        // Setup required field labels with red asterisks
        setupRequiredFieldLabels();
    }
    
    /**
     * Setup required field labels with red asterisks
     * Only Report Type and Location Information are required fields
     */
    private void setupRequiredFieldLabels() {
        try {
            // Report Type label (required)
            TextView reportTypeLabel = findViewById(R.id.reportTypeLabel);
            if (reportTypeLabel != null) {
                setRequiredLabelText(reportTypeLabel, "Report Type");
            }
            
            // Description label (optional - no asterisk)
            TextView descriptionLabel = findViewById(R.id.descriptionLabel);
            if (descriptionLabel != null) {
                descriptionLabel.setText("Description");
            }
            
            // Location Information label (required)
            TextView locationInformationLabel = findViewById(R.id.locationInformationLabel);
            if (locationInformationLabel != null) {
                setRequiredLabelText(locationInformationLabel, "Location Information");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up required field labels: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set TextView text with a red asterisk at the end
     * @param textView The TextView to update
     * @param baseText The base text without asterisk
     */
    private void setRequiredLabelText(TextView textView, String baseText) {
        try {
            String fullText = baseText + " *";
            SpannableString spannableString = new SpannableString(fullText);
            
            // Get red color
            int redColor = getResources().getColor(android.R.color.holo_red_dark, getTheme());
            
            // Find the asterisk position and color it red
            int asteriskStart = fullText.indexOf("*");
            int asteriskEnd = asteriskStart + 1;
            
            if (asteriskStart >= 0) {
                spannableString.setSpan(
                    new ForegroundColorSpan(redColor),
                    asteriskStart,
                    asteriskEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            textView.setText(spannableString);
        } catch (Exception e) {
            Log.e(TAG, "Error setting required label text: " + e.getMessage(), e);
            // Fallback to plain text with asterisk
            textView.setText(baseText + " *");
        }
    }

    private void setupReportTypeSpinner() {
        // Create array of report types with emojis (matching MapViewActivity filter panel)
        String[] reportTypes = {
                "Select Report Type",
                "🚗 Road Crash",
                "🏥 Medical Emergency",
                "🌋 Volcanic Activity",
                "🏚️ Earthquake",
                "🔴 Armed Conflict",
                "🔥 Fire",
                "🌊 Flooding",
                "⛰️ Landslide",
                "⚠️ Civil Disturbance",
                "🦠 Infectious Disease",
                "🏗️ Poor Infrastructure",
                "🚧 Obstructions",
                "⚡ Electrical Hazard",
                "🌿 Environmental Hazard",
                "🐾 Animal Concern",
                "➕ Others"
        };

        // Create custom adapter with reduced text size
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                reportTypes
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextSize(14);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextSize(14);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);
    }
    
    /**
     * Helper method to remove emoji from report type string
     * This ensures the stored value doesn't include emojis
     */
    private String getReportTypeWithoutEmoji(String reportTypeWithEmoji) {
        if (reportTypeWithEmoji == null || reportTypeWithEmoji.isEmpty()) {
            return reportTypeWithEmoji;
        }
        
        // Remove emoji characters using regex pattern that matches emojis
        // This pattern matches most emoji characters including variations
        String withoutEmoji = reportTypeWithEmoji.replaceAll("[\uD83C-\uDBFF\uDC00-\uDFFF]+", "").trim();
        
        // Fallback: if regex didn't work, try removing common emoji patterns
        if (withoutEmoji.equals(reportTypeWithEmoji)) {
            // Remove specific emojis we know we're using
            withoutEmoji = reportTypeWithEmoji
                .replace("🚗", "")
                .replace("🏥", "")
                .replace("🌋", "")
                .replace("🏚️", "")
                .replace("🔴", "")
                .replace("🔥", "")
                .replace("🌊", "")
                .replace("⛰️", "")
                .replace("⚠️", "")
                .replace("🦠", "")
                .replace("🏗️", "")
                .replace("🚧", "")
                .replace("⚡", "")
                .replace("🌿", "")
                .replace("🐾", "")
                .replace("➕", "")
                .trim();
        }
        
        return withoutEmoji;
    }

    private void setupReportLogRecyclerView() {
        // Setup RecyclerView with LinearLayoutManager
        reportLogRecyclerView.setLayoutManager(new NonScrollableLinearLayoutManager(this));
        reportLogRecyclerView.setNestedScrollingEnabled(false);

        // Add divider decoration between cards (like the image design)
        reportLogRecyclerView.addItemDecoration(new ReportLogAdapter.ReportItemDivider());

        // Initialize adapter
        allReports = new ArrayList<>();
        filteredReports = new ArrayList<>();
        reportLogAdapter = new ReportLogAdapter(this, filteredReports);
        
        // Set click listener (only for viewing attachments, not for report details)
        reportLogAdapter.setOnReportClickListener(new ReportLogAdapter.OnReportClickListener() {
            @Override
            public void onViewAttachmentsClick(Report report) {
                // Show attachments dialog if images or videos exist
                boolean hasImages = report.getImageUrls() != null && !report.getImageUrls().isEmpty();
                boolean hasVideos = report.getVideoUrls() != null && !report.getVideoUrls().isEmpty();
                if (hasImages || hasVideos) {
                    showReportAttachmentsDialog(report);
                } else {
                    Toast.makeText(ReportSubmissionActivity.this, 
                        "No attachments available for this report", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        reportLogRecyclerView.setAdapter(reportLogAdapter);
    }
    
    
    private void setupProfessionalImageGallery() {
        // Setup 4-column grid layout with media matching container height
        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager = 
            new androidx.recyclerview.widget.GridLayoutManager(this, 4);
        imageGalleryRecyclerView.setLayoutManager(gridLayoutManager);
        
        // Initialize media adapter with MediaItems (supports both images and videos)
        mediaGalleryAdapter = new MediaGalleryAdapter(this, selectedMediaItems);
        
        // Set click listeners
        mediaGalleryAdapter.setOnMediaClickListener(new MediaGalleryAdapter.OnMediaClickListener() {
            @Override
            public void onMediaClick(int position, MediaItem mediaItem) {
                if (mediaItem.isImage()) {
                    showImageInDialog(mediaItem.getUri());
                } else if (mediaItem.isVideo()) {
                    showVideoInDialog(mediaItem.getUri());
                }
            }
        });
        
        mediaGalleryAdapter.setOnMediaRemoveListener(new MediaGalleryAdapter.OnMediaRemoveListener() {
            @Override
            public void onMediaRemove(int position, MediaItem mediaItem) {
                removeMediaFromGallery(position);
            }
        });
        
        imageGalleryRecyclerView.setAdapter(mediaGalleryAdapter);
        
        // Setup placeholder click listener to show capture menu (take photo or record video)
        if (placeholderContainer != null) {
            placeholderContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMediaCaptureMenu(v);
                }
            });
        }
    }
    
    private void loadUserProfileInformation() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Get user information from SharedPreferences
        String firstName = prefs.getString("first_name", "");
        String lastName = prefs.getString("last_name", "");
        String mobileNumber = prefs.getString("mobile_number", "");
        
        // Reporter information fields removed - no longer needed
        // User data is automatically associated with their Firebase account
        Log.d(TAG, "loadUserProfileInformation() - Reporter fields removed, using Firebase Auth");
    }
    
    private void fetchUserDataFromFirestore() {
        // No longer needed - reporter fields removed
        // User is automatically identified by their Firebase Auth UID
        Log.d(TAG, "fetchUserDataFromFirestore() - Reporter fields removed");
    }

    private void setupReportLogFilterSpinners() {
        // Setup Type Filter Spinner
        String[] typeOptions = {
                "All Types",
                "Road Crash",
                "Medical Emergency",
                "Volcanic Activity",
                "Earthquake",
                "Armed Conflict",
                "Fire",
                "Flooding",
                "Landslide",
                "Civil Disturbance",
                "Infectious Disease",
                "Poor Infrastructure",
                "Obstructions",
                "Electrical Hazard",
                "Environmental Hazard",
                "Animal Concern",
                "Others"
        };
        
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                typeOptions
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportLogTypeFilterSpinner.setAdapter(typeAdapter);
        reportLogTypeFilterSpinner.setSelection(0);
        
        // Setup Status Filter Spinner
        String[] statusOptions = {
                "All Status",
                "Pending",
                "Ongoing",
                "Responded",
                "Not Responded",
                "Redundant",
                "False Report"
        };
        
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportLogStatusFilterSpinner.setAdapter(statusAdapter);
        reportLogStatusFilterSpinner.setSelection(0);
        
        // Set listeners
        reportLogTypeFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                applyFilters();
            }
        });
        
        reportLogStatusFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                applyFilters();
            }
        });
        
        // Apply initial filter
        applyFilters();
    }
    
    private void applyFilters() {
        String selectedType = reportLogTypeFilterSpinner.getSelectedItem().toString();
        String selectedStatus = reportLogStatusFilterSpinner.getSelectedItem().toString();
        
        filteredReports.clear();
        
        boolean filterByType = !selectedType.equals("All Types");
        boolean filterByStatus = !selectedStatus.equals("All Status");
        
        if (!filterByType && !filterByStatus) {
            // No filters - show all
            filteredReports.addAll(allReports);
        } else {
            for (Report report : allReports) {
                boolean matchesType = !filterByType || 
                    (report.getCategory() != null && report.getCategory().equals(selectedType));
                boolean matchesStatus = !filterByStatus || 
                    (report.getStatus() != null && report.getStatus().equalsIgnoreCase(selectedStatus));
                
                if (matchesType && matchesStatus) {
                    filteredReports.add(report);
                }
            }
        }
        
        reportLogAdapter.notifyDataSetChanged();
        Log.d(TAG, "Filtered reports: " + filteredReports.size() + " reports (Type: " + selectedType + ", Status: " + selectedStatus + ")");
    }

    private void setupClickListeners() {
        // Profile button click
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfile();
            }
        });
        
        // Location info icon click
        locationInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationInfoDialog();
            }
        });

        if (reportTypeInfoIcon != null) {
            reportTypeInfoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReportTypeGuideDialog();
                }
            });
        }
        
        // Tab click listeners
        submitReportTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSubmitReportTab();
            }
        });
        
        reportLogTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToReportLogTab();
            }
        });

        // Pinning button click - show location options bottom sheet
        pinningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationOptionsBottomSheet();
            }
        });

        // Take photo button click
        if (takePhotoButton != null) {
            takePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkCameraPermissionAndTakePhoto();
                }
            });
        }
        
        // Upload images/videos button click - show selection menu (select image or select video)
        uploadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMediaSelectMenu(v);
            }
        });

        // Submit report button click
        submitReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReport();
            }
        });

        // Bottom navigation click listeners
        homeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        chatTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChat();
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        reportTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on report screen - do nothing
            }
        });

        mapTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMap();
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        alertsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAlerts();
                // Fade transition animation
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * Show report type guide dialog with detailed descriptions.
     */
    private void showReportTypeGuideDialog() {
        try {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(LayoutInflater.from(this).inflate(R.layout.dialog_report_type_guide, null))
                    .create();

            dialog.setOnShowListener(d -> {
                Button closeButton = dialog.findViewById(R.id.btnCloseReportTypeGuide);
                if (closeButton != null) {
                    closeButton.setOnClickListener(v -> dialog.dismiss());
                }
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing report type guide dialog", e);
            Toast.makeText(this, "Unable to open report type guide", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show location information modal dialog
     */
    private void showLocationInfoDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_location_info, null);
            
            // Find views in dialog
            TextView tvLocationInfoMessage = dialogView.findViewById(R.id.tvLocationInfoMessage);
            Button btnCloseLocationInfo = dialogView.findViewById(R.id.btnCloseLocationInfo);
            
            // Message is already set in XML layout
            
            // Create dialog
            AlertDialog dialog = builder.setView(dialogView)
                    .setCancelable(true)
                    .create();
            
            // Close button
            if (btnCloseLocationInfo != null) {
                btnCloseLocationInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing location info dialog", e);
            // Fallback to Toast if dialog fails
            Toast.makeText(this, 
                "The Lucban MDRRMO only responds to geotagged reports within Lucban, but you may submit reports that are outside of the vicinity.",
                Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show location options bottom sheet with two options:
     * 1. Pin Location on Map
     * 2. Get Current Location
     */
    private void showLocationOptionsBottomSheet() {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_location_options, null);
            bottomSheetDialog.setContentView(bottomSheetView);
            
            // Find option views
            LinearLayout optionPinLocation = bottomSheetView.findViewById(R.id.optionPinLocation);
            LinearLayout optionGetCurrentLocation = bottomSheetView.findViewById(R.id.optionGetCurrentLocation);
            
            // Pin Location on Map option
            if (optionPinLocation != null) {
                optionPinLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        // If current location is already obtained, open map in view-only mode
                        // Otherwise, open map for selection
                        if (isLocationSelected && selectedLatitude != 0.0 && selectedLongitude != 0.0) {
                            Log.d(TAG, "Opening map to VIEW current location pin");
                            openMapPickerViewOnly();
                        } else {
                            Log.d(TAG, "Opening map to SELECT new location");
                            openMapPicker();
                        }
                    }
                });
            }
            
            // Get Current Location option
            if (optionGetCurrentLocation != null) {
                optionGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        Log.d(TAG, "Get Current Location option clicked");
                        getCurrentLocation();
                    }
                });
            }
            
            bottomSheetDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing location options bottom sheet: " + e.getMessage(), e);
            Toast.makeText(this, "Error showing location options", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigation methods
    private void navigateToHome() {
        Intent intent = new Intent(this, MainDashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish(); // Optional: remove this activity from stack
    }

    private void navigateToChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToMap() {
        Intent intent = new Intent(this, MapViewActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToAlerts() {
        Intent intent = new Intent(this, AlertsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // Functional methods
    // Open a map picker activity to let the user pick a location
    private static final int MAP_PICKER_REQUEST_CODE = 1001;
    private String selectedLocationName = "";
    private Double selectedLongitude = 0.0;
    private Double selectedLatitude = 0.0;
    private boolean isLocationSelected = false;
    
    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        
        // If we have a previously selected location (from current location or map), pass it to MapPickerActivity
        if (isLocationSelected && selectedLongitude != 0.0 && selectedLatitude != 0.0) {
            intent.putExtra("selectedLongitude", selectedLongitude);
            intent.putExtra("selectedLatitude", selectedLatitude);
            intent.putExtra("selectedLocationName", selectedLocationName);
            
            Log.d(TAG, "Opening map picker with existing location:");
            Log.d(TAG, "   Name: " + selectedLocationName);
            Log.d(TAG, "   Lat: " + selectedLatitude + ", Lon: " + selectedLongitude);
        } else {
            Log.d(TAG, "Opening map picker without pre-selected location");
        }
        
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
    }
    
    /**
     * Opens map in view-only mode to show the pinned current location
     * User can see the pin but map interaction is limited to viewing
     */
    private void openMapPickerViewOnly() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        
        // Pass the current location to show on map
        intent.putExtra("selectedLongitude", selectedLongitude);
        intent.putExtra("selectedLatitude", selectedLatitude);
        intent.putExtra("selectedLocationName", selectedLocationName);
        intent.putExtra("viewOnlyMode", true); // Enable view-only mode
        
        Log.d(TAG, "Opening map in VIEW-ONLY mode to show current location:");
        Log.d(TAG, "   Location: " + selectedLocationName);
        Log.d(TAG, "   Coordinates: " + selectedLatitude + ", " + selectedLongitude);
        Log.d(TAG, "   Mode: View Only (no search, just show pin)");
        
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
    }

    private void checkCameraPermissionAndTakePhoto() {
        pendingVideoRecording = false; // Set flag to indicate photo is pending
        PermissionHelper.requestCameraPermission(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                openCamera();
            }
            
            @Override
            public void onPermissionDenied() {
                Toast.makeText(ReportSubmissionActivity.this, 
                        "Camera permission is required to take photos", 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Check camera permission and record video
     */
    private void checkCameraPermissionAndRecordVideo() {
        pendingVideoRecording = true; // Set flag to indicate video recording is pending
        PermissionHelper.requestCameraPermission(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                openVideoRecorder();
            }
            
            @Override
            public void onPermissionDenied() {
                pendingVideoRecording = false; // Reset flag
                Toast.makeText(ReportSubmissionActivity.this, 
                        "Camera permission is required to record videos", 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Open video recorder
     */
    private void openVideoRecorder() {
        try {
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (videoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(videoIntent, VIDEO_RECORD_REQUEST);
            } else {
                Toast.makeText(this, "Video recorder not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening video recorder: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening video recorder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show popup menu for capturing media (Take Photo or Record Video)
     * Used when placeholder is clicked
     */
    private void showMediaCaptureMenu(View anchor) {
        try {
            PopupMenu popupMenu = new PopupMenu(this, anchor);
            
            // Inflate menu from resource
            popupMenu.getMenuInflater().inflate(R.menu.media_capture_menu, popupMenu.getMenu());
            
            // Ensure menu has items (fallback if resource not found)
            if (popupMenu.getMenu().size() == 0) {
                popupMenu.getMenu().add(0, 1, 0, "Take Photo");
                popupMenu.getMenu().add(0, 2, 0, "Record Video");
            }
            
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    int itemId = item.getItemId();
                    String title = item.getTitle().toString();
                    
                    // Check by resource ID first, then by numeric ID, then by title
                    if (itemId == R.id.menu_take_photo || itemId == 1 || title.contains("Take Photo")) {
                        checkCameraPermissionAndTakePhoto();
                        return true;
                    } else if (itemId == R.id.menu_record_video || itemId == 2 || title.contains("Record Video")) {
                        checkCameraPermissionAndRecordVideo();
                        return true;
                    }
                    return false;
                }
            });
            
            popupMenu.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing media capture menu: " + e.getMessage(), e);
            // Fallback: just take photo
            checkCameraPermissionAndTakePhoto();
        }
    }
    
    /**
     * Show popup menu for selecting media from gallery (Select Image or Select Video)
     * Used when upload button is clicked
     */
    private void showMediaSelectMenu(View anchor) {
        try {
            PopupMenu popupMenu = new PopupMenu(this, anchor);
            
            // Inflate menu from resource
            popupMenu.getMenuInflater().inflate(R.menu.media_select_menu, popupMenu.getMenu());
            
            // Ensure menu has items (fallback if resource not found)
            if (popupMenu.getMenu().size() == 0) {
                popupMenu.getMenu().add(0, 1, 0, "Select Image");
                popupMenu.getMenu().add(0, 2, 0, "Select Video");
            }
            
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    int itemId = item.getItemId();
                    String title = item.getTitle().toString();
                    
                    // Check by resource ID first, then by numeric ID, then by title
                    if (itemId == R.id.menu_select_image || itemId == 1 || title.contains("Select Image")) {
                        pickImageFromGallery();
                        return true;
                    } else if (itemId == R.id.menu_select_video || itemId == 2 || title.contains("Select Video")) {
                        pickVideoFromGallery();
                        return true;
                    }
                    return false;
                }
            });
            
            popupMenu.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing media select menu: " + e.getMessage(), e);
            // Fallback: just pick image
            pickImageFromGallery();
        }
    }
    
    private void pickImageFromGallery() {
        // Request storage permission first if needed
        PermissionHelper.requestStoragePermission(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Pictures"), IMAGE_PICK_REQUEST);
            }
            
            @Override
            public void onPermissionDenied() {
                Toast.makeText(ReportSubmissionActivity.this, 
                        "Storage permission is required to select images", 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Pick video from gallery
     * Uses ACTION_OPEN_DOCUMENT for better permission handling on Android 5.0+
     */
    private void pickVideoFromGallery() {
        // Request storage permission first if needed
        PermissionHelper.requestStoragePermission(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Intent intent;
                
                // Use ACTION_OPEN_DOCUMENT for Android 5.0+ (better for persistent permissions)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("video/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    // These flags are important for persistent access
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
                    // Fallback to ACTION_GET_CONTENT for older Android versions
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                
                startActivityForResult(Intent.createChooser(intent, "Select Videos"), VIDEO_PICK_REQUEST);
            }
            
            @Override
            public void onPermissionDenied() {
                Toast.makeText(ReportSubmissionActivity.this, 
                        "Storage permission is required to select videos", 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Take persistent URI permission for content URIs
     * This is required to access the file later when uploading
     */
    private void takePersistableUriPermission(Uri uri) {
        try {
            if (uri != null && android.content.ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    flags |= Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
                }
                
                try {
                    getContentResolver().takePersistableUriPermission(uri, flags);
                    Log.d(TAG, "✅ Persistent URI permission granted for: " + uri.toString());
                } catch (SecurityException e) {
                    // Some URIs don't support persistable permissions, but we can still use them
                    Log.w(TAG, "URI doesn't support persistable permissions (this is OK): " + uri.toString());
                    // Grant regular read permission instead
                    grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } else {
                Log.d(TAG, "URI is not a content URI, no permission needed: " + (uri != null ? uri.toString() : "null"));
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to take persistent URI permission: " + e.getMessage(), e);
            // Try to grant regular permission as fallback
            try {
                if (uri != null) {
                    grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.d(TAG, "Granted regular read permission as fallback");
                }
            } catch (Exception ex) {
                Log.e(TAG, "Failed to grant fallback permission: " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error taking persistent URI permission: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String pickedLocation = data.getStringExtra("pickedLocation");
            String locationName = data.getStringExtra("locationName");
            Double longitude = data.getDoubleExtra("longitude", 0.0);
            Double latitude = data.getDoubleExtra("latitude", 0.0);
            
            if (pickedLocation != null) {
                // Store the selected location coordinates
                selectedLongitude = longitude;
                selectedLatitude = latitude;
                isLocationSelected = true;
                
                // Store coordinates text for logging and submission
                String coordinatesText = String.format("%.6f, %.6f", latitude, longitude);
                
                // Use provided location name if available, otherwise get it via geocoding
                if (locationName != null && !locationName.trim().isEmpty()) {
                    selectedLocationName = locationName;
                    // Update coordinates field with location name only (no coordinates)
                    coordinatesEditText.setText(selectedLocationName);
                    coordinatesEditText.setEnabled(true);
                    
                    // Update legacy field for backward compatibility
                    String legacyDisplayText = selectedLocationName + " (" + coordinatesText + ")";
                    locationEditText.setText(legacyDisplayText);
                    
                    // Make coordinates EditText read-only and add click listener
                    makeCoordinatesEditTextReadOnly();
                    
                    Toast.makeText(this, "Location pinned: " + selectedLocationName, Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "✅ Pinned location received:");
                    Log.d(TAG, "   Location Name: " + selectedLocationName);
                    Log.d(TAG, "   Latitude: " + latitude);
                    Log.d(TAG, "   Longitude: " + longitude);
                    Log.d(TAG, "   Coordinates: " + coordinatesText);
                } else {
                    // Location name not provided, show placeholder and get full address via geocoding
                    coordinatesEditText.setText("Getting location name...");
                    coordinatesEditText.setEnabled(true);
                    
                    // Get location name using reverse geocoding
                    getLocationNameFromCoordinates(latitude, longitude);
                    
                    // Update legacy field for backward compatibility
                    String displayText = "Selected Location (" + coordinatesText + ")";
                    locationEditText.setText(displayText);
                    
                    // Make coordinates EditText read-only and add click listener
                    makeCoordinatesEditTextReadOnly();
                    
                    Toast.makeText(this, "Location pinned, getting location name...", Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "✅ Pinned location received (geocoding for name):");
                    Log.d(TAG, "   Latitude: " + latitude);
                    Log.d(TAG, "   Longitude: " + longitude);
                    Log.d(TAG, "   Coordinates: " + coordinatesText);
                }
            }
        }
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();
            
            Log.d(TAG, "Processing image selection result");
            
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                Log.d(TAG, "Multiple images selected: " + count);
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                    Log.d(TAG, "Added image " + (i + 1) + ": " + imageUri.toString());
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
                Log.d(TAG, "Single image selected: " + imageUri.toString());
            } else {
                Log.d(TAG, "No images found in result");
            }
            
            Log.d(TAG, "Total images stored: " + selectedImageUris.size());
            
            // Also add to MediaItem list
            for (Uri imageUri : selectedImageUris) {
                selectedMediaItems.add(new MediaItem(imageUri, MediaItem.TYPE_IMAGE));
            }
            
            // Update professional image gallery
            updateProfessionalImageGallery();
        }
        
        // Handle video selection
        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "Processing video selection result");
            
            // Ensure the returned Intent has the necessary flags
            if (data.getFlags() == 0) {
                data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    data.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }
            }
            
            List<Uri> selectedVideoUris = new ArrayList<>();
            
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                Log.d(TAG, "Multiple videos selected: " + count);
                for (int i = 0; i < count; i++) {
                    Uri videoUri = data.getClipData().getItemAt(i).getUri();
                    // Grant read permission immediately
                    grantUriPermission(getPackageName(), videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    // Take persistent URI permission for content URIs
                    takePersistableUriPermission(videoUri);
                    selectedVideoUris.add(videoUri);
                    selectedMediaItems.add(new MediaItem(videoUri, MediaItem.TYPE_VIDEO));
                    Log.d(TAG, "Added video " + (i + 1) + ": " + videoUri.toString());
                }
            } else if (data.getData() != null) {
                Uri videoUri = data.getData();
                // Grant read permission immediately
                grantUriPermission(getPackageName(), videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Take persistent URI permission for content URIs
                takePersistableUriPermission(videoUri);
                selectedVideoUris.add(videoUri);
                selectedMediaItems.add(new MediaItem(videoUri, MediaItem.TYPE_VIDEO));
                Log.d(TAG, "Single video selected: " + videoUri.toString());
            } else {
                Log.d(TAG, "No videos found in result");
            }
            
            Log.d(TAG, "Total videos stored: " + selectedVideoUris.size());
            Log.d(TAG, "Total media items: " + selectedMediaItems.size());
            
            // Update professional image gallery (it will handle videos too)
            updateProfessionalImageGallery();
        }
        
        // Handle video recording result
        if (requestCode == VIDEO_RECORD_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri videoUri = data.getData();
                if (videoUri != null) {
                    // Grant read permission
                    grantUriPermission(getPackageName(), videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    // Take persistent URI permission for content URIs
                    takePersistableUriPermission(videoUri);
                    
                    selectedMediaItems.add(new MediaItem(videoUri, MediaItem.TYPE_VIDEO));
                    Log.d(TAG, "Recorded video added: " + videoUri.toString());
                    
                    // Update professional image gallery
                    updateProfessionalImageGallery();
                } else {
                    Log.e(TAG, "Video URI is null");
                    Toast.makeText(this, "Error: Video URI is null", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing recorded video: " + e.getMessage(), e);
                Toast.makeText(this, "Error processing video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        
        // Handle camera result
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("data")) {
                    // Image captured as Bitmap
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    
                    // Convert bitmap to URI and save to temporary file
                    Uri imageUri = saveBitmapToTempFile(imageBitmap);
                    
                    if (imageUri != null) {
                        selectedImageUris.add(imageUri);
                        selectedMediaItems.add(new MediaItem(imageUri, MediaItem.TYPE_IMAGE));
                        Log.d(TAG, "Camera image added: " + imageUri.toString());
                        
                        // Update professional image gallery
                        updateProfessionalImageGallery();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing camera image: " + e.getMessage(), e);
                Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Save bitmap to a temporary file and return URI
     */
    private Uri saveBitmapToTempFile(Bitmap bitmap) {
        try {
            // Create a temporary file in cache directory
            File tempFile = File.createTempFile("camera_image_", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            
            // Compress bitmap to JPEG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            // Return URI
            return Uri.fromFile(tempFile);
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to file: " + e.getMessage(), e);
            return null;
        }
    }

    
    private void updateProfessionalImageGallery() {
        // Combine MediaItems and legacy selectedImageUris
        List<MediaItem> allMediaItems = new ArrayList<>(selectedMediaItems);
        
        // Add legacy selectedImageUris as MediaItems if not already present
        for (Uri uri : selectedImageUris) {
            boolean exists = false;
            for (MediaItem item : allMediaItems) {
                if (item.getUri().equals(uri)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allMediaItems.add(new MediaItem(uri, MediaItem.TYPE_IMAGE));
            }
        }
        
        if (allMediaItems.isEmpty()) {
            // Show placeholder, hide gallery
            if (placeholderContainer != null) {
                placeholderContainer.setVisibility(View.VISIBLE);
            }
            imageGalleryRecyclerView.setVisibility(View.GONE);
        } else {
            // Hide placeholder, show gallery
            if (placeholderContainer != null) {
                placeholderContainer.setVisibility(View.GONE);
            }
            imageGalleryRecyclerView.setVisibility(View.VISIBLE);
            imageGalleryRecyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_scale));
            
            // Update adapter with all media items
            if (mediaGalleryAdapter != null) {
                mediaGalleryAdapter.updateMedia(allMediaItems);
            }
        }
    }
    
    
    private void removeMediaFromGallery(int position) {
        // Combine MediaItems and legacy selectedImageUris to find the correct position
        List<MediaItem> allMediaItems = new ArrayList<>(selectedMediaItems);
        
        // Add legacy selectedImageUris as MediaItems if not already present
        for (Uri uri : selectedImageUris) {
            boolean exists = false;
            for (MediaItem item : allMediaItems) {
                if (item.getUri().equals(uri)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allMediaItems.add(new MediaItem(uri, MediaItem.TYPE_IMAGE));
            }
        }
        
        if (position >= 0 && position < allMediaItems.size()) {
            MediaItem itemToRemove = allMediaItems.get(position);
            Uri uriToRemove = itemToRemove.getUri();
            
            // Remove from MediaItems
            selectedMediaItems.removeIf(item -> item.getUri().equals(uriToRemove));
            
            // Remove from legacy selectedImageUris
            selectedImageUris.remove(uriToRemove);
            
            // Update adapter
            if (mediaGalleryAdapter != null) {
                mediaGalleryAdapter.removeMedia(position);
            }
            
            // Update gallery visibility
            updateProfessionalImageGallery();
        }
    }
    

    private void initializeLocationServices() {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    handleLocationUpdate(location);
                    
                    // Stop location updates after getting the location
                    stopLocationUpdates();
                }
            }
        };
    }
    
    private void getCurrentLocation() {
        // First check if user has enabled location access in ProfileActivity settings
        if (!LocationPermissionHelper.isLocationAccessEnabledWithLog(this, "ReportSubmissionActivity")) {
            Toast.makeText(this, "Location access is disabled in settings. Please enable it in Profile settings to use this feature.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Check if location permissions are granted
        if (!checkLocationPermissions()) {
            requestLocationPermissions();
            return;
        }
        
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Show loading message
        Toast.makeText(this, "Getting your current location...", Toast.LENGTH_SHORT).show();
        
        // Get current location
        getCurrentLocationWithCallback();
    }
    
    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    
    private void getCurrentLocationWithCallback() {
        try {
            // Check if user has enabled location access in settings
            if (!LocationPermissionHelper.isLocationAccessEnabledWithLog(this, "ReportSubmissionActivity")) {
                return;
            }
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Create location request
                LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdateDelayMillis(10000)
                        .build();
                
                // Request location updates
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                
                // Also try to get last known location as fallback
                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            // Check if location is recent (within 5 minutes)
                            if (System.currentTimeMillis() - location.getTime() < 5 * 60 * 1000) {
                                handleLocationUpdate(location);
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while getting location", e);
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleLocationUpdate(Location location) {
        if (location != null) {
            // Update location data
            selectedLatitude = location.getLatitude();
            selectedLongitude = location.getLongitude();
            isLocationSelected = true;
            
            // Store coordinates text for logging and submission
            String coordinatesText = String.format("%.6f, %.6f", selectedLatitude, selectedLongitude);
            
            // Show placeholder while geocoding is in progress
            if (coordinatesEditText != null) {
                coordinatesEditText.setText("Getting location name...");
                coordinatesEditText.setEnabled(true);
            }
            
            // Get location name using reverse geocoding (will update the display when complete)
            getLocationNameFromCoordinates(selectedLatitude, selectedLongitude);
            
            // Update legacy field for backward compatibility
            String displayText = "Current Location (" + coordinatesText + ")";
            locationEditText.setText(displayText);
            
            // Make coordinates EditText clickable to view/change on map
            makeCoordinatesEditTextReadOnly();
            
            Log.d(TAG, "✅ Current location obtained:");
            Log.d(TAG, "   Latitude: " + selectedLatitude);
            Log.d(TAG, "   Longitude: " + selectedLongitude);
            Log.d(TAG, "   Coordinates: " + coordinatesText);
            Log.d(TAG, "💡 User can now click pin button to verify location on map");
        } else {
            Toast.makeText(this, "Unable to get current location. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Location is null in handleLocationUpdate");
        }
    }
    
    /**
     * Get location name from coordinates using reverse geocoding
     * Builds full address and displays it with coordinates
     */
    private void getLocationNameFromCoordinates(double latitude, double longitude) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                
                // Build full address string
                StringBuilder fullAddress = new StringBuilder();
                
                // Add feature name (street name)
                if (address.getFeatureName() != null && !address.getFeatureName().trim().isEmpty()) {
                    fullAddress.append(address.getFeatureName().trim());
                }
                
                // Add thoroughfare (street)
                if (address.getThoroughfare() != null && !address.getThoroughfare().trim().isEmpty()) {
                    if (fullAddress.length() > 0) {
                        fullAddress.append(" ");
                    }
                    fullAddress.append(address.getThoroughfare().trim());
                }
                
                // Add sub-locality (barangay)
                if (address.getSubLocality() != null && !address.getSubLocality().trim().isEmpty()) {
                    if (fullAddress.length() > 0) {
                        fullAddress.append(", ");
                    }
                    fullAddress.append(address.getSubLocality().trim());
                }
                
                // Add locality (city/town)
                if (address.getLocality() != null && !address.getLocality().trim().isEmpty()) {
                    if (fullAddress.length() > 0) {
                        fullAddress.append(", ");
                    }
                    fullAddress.append(address.getLocality().trim());
                }
                
                // Add admin area (province)
                if (address.getAdminArea() != null && !address.getAdminArea().trim().isEmpty()) {
                    if (fullAddress.length() > 0) {
                        fullAddress.append(", ");
                    }
                    fullAddress.append(address.getAdminArea().trim());
                }
                
                // Add country
                if (address.getCountryName() != null && !address.getCountryName().trim().isEmpty()) {
                    if (fullAddress.length() > 0) {
                        fullAddress.append(", ");
                    }
                    fullAddress.append(address.getCountryName().trim());
                }
                
                // If no address components found, use sublocality or locality as fallback
                if (fullAddress.length() == 0) {
                    if (address.getSubLocality() != null) {
                        fullAddress.append(address.getSubLocality());
                    } else if (address.getLocality() != null) {
                        fullAddress.append(address.getLocality());
                    } else {
                        fullAddress.append("Current Location");
                    }
                }
                
                selectedLocationName = fullAddress.toString();
                Log.d(TAG, "✅ Full address from geocoding: " + selectedLocationName);
                
                // Update UI with full address only (no coordinates)
                runOnUiThread(() -> {
                    if (coordinatesEditText != null) {
                        coordinatesEditText.setText(selectedLocationName);
                        coordinatesEditText.setEnabled(true);
                    }
                });
                
            } else {
                selectedLocationName = "Current Location";
                Log.w(TAG, "⚠️ Geocoder returned no addresses, using default name");
                
                // Update UI with default name only (no coordinates)
                runOnUiThread(() -> {
                    if (coordinatesEditText != null) {
                        coordinatesEditText.setText(selectedLocationName);
                        coordinatesEditText.setEnabled(true);
                    }
                });
            }
        } catch (Exception e) {
            selectedLocationName = "Current Location";
            Log.e(TAG, "Error getting location name from coordinates: " + e.getMessage(), e);
            
            // Update UI with default name only (no coordinates) on error
            runOnUiThread(() -> {
                if (coordinatesEditText != null) {
                    coordinatesEditText.setText(selectedLocationName);
                    coordinatesEditText.setEnabled(true);
                }
            });
        }
    }
    
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void uploadImages() {
        // TODO: Implement image upload functionality
        // You can use Intent to pick images from gallery or camera
        Toast.makeText(this, "Image upload functionality will be implemented", Toast.LENGTH_SHORT).show();

        // Example intent to pick image from gallery:
        /*
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
        */
    }

    private void submitReport() {
        if (validateForm()) {
            // Get current user
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Please sign in to submit a report", Toast.LENGTH_LONG).show();
                return;
            }

            // Show confirmation dialog instead of submitting directly
            showReportConfirmationDialog();
        }
    }
    
    /**
     * Show confirmation dialog before submitting the report
     */
    private void showReportConfirmationDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_report_confirmation, null);
            
            // Get form data
            String reportType = reportTypeSpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            String locationName = isLocationSelected ? selectedLocationName : "Location not specified";
            String coordinates = "";
            if (isLocationSelected && selectedLatitude != 0.0 && selectedLongitude != 0.0) {
                coordinates = String.format("%.6f, %.6f", selectedLatitude, selectedLongitude);
            }
            
            // Populate dialog fields
            TextView tvConfirmReportType = dialogView.findViewById(R.id.tvConfirmReportType);
            TextView tvConfirmDescription = dialogView.findViewById(R.id.tvConfirmDescription);
            TextView tvConfirmLocationName = dialogView.findViewById(R.id.tvConfirmLocationName);
            TextView tvConfirmCoordinates = dialogView.findViewById(R.id.tvConfirmCoordinates);
            RecyclerView rvConfirmImagePreviews = dialogView.findViewById(R.id.rvConfirmImagePreviews);
            Button btnCancelReport = dialogView.findViewById(R.id.btnCancelReport);
            Button btnConfirmSubmit = dialogView.findViewById(R.id.btnConfirmSubmit);
            
            // Set report type
            if (tvConfirmReportType != null) {
                tvConfirmReportType.setText(reportType);
            }
            
            // Set description
            if (tvConfirmDescription != null) {
                tvConfirmDescription.setText(description.isEmpty() ? "No description provided" : description);
            }
            
            // Set location
            if (tvConfirmLocationName != null) {
                tvConfirmLocationName.setText(locationName);
            }
            
            if (tvConfirmCoordinates != null) {
                if (!coordinates.isEmpty()) {
                    tvConfirmCoordinates.setText(coordinates);
                } else {
                    tvConfirmCoordinates.setText("Coordinates not available");
                    tvConfirmCoordinates.setTextColor(0xFF999999);
                }
            }
            
            // Create dialog
            AlertDialog dialog = builder.setView(dialogView)
                    .setCancelable(true)
                    .create();
            
            // Cancel button
            if (btnCancelReport != null) {
                btnCancelReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            
            // Confirm button
            if (btnConfirmSubmit != null) {
                btnConfirmSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        // Proceed with actual submission
                        actuallySubmitReport();
                    }
                });
            }
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing confirmation dialog", e);
            // Fallback to direct submission if dialog fails
            actuallySubmitReport();
        }
    }
    
    /**
     * Actually submit the report to Firestore (called after confirmation)
     */
    private void actuallySubmitReport() {
        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to submit a report", Toast.LENGTH_LONG).show();
            return;
        }

        // Show loading state
        submitReportButton.setEnabled(false);
        submitReportButton.setText("Submitting...");

        // Get form data
        String reportTypeWithEmoji = reportTypeSpinner.getSelectedItem().toString();
        // Strip emoji for storage in Firestore
        String reportType = getReportTypeWithoutEmoji(reportTypeWithEmoji);
        String description = descriptionEditText.getText().toString().trim();
        
        // Use selected location name from map picker, or default
        String location = isLocationSelected ? selectedLocationName : "Location not specified";
        // Append coordinates if available (coordinates are stored in selectedLatitude/selectedLongitude)
        if (isLocationSelected && selectedLatitude != 0.0 && selectedLongitude != 0.0) {
            String coordinatesText = String.format("%.6f, %.6f", selectedLatitude, selectedLongitude);
            location = location + " (" + coordinatesText + ")";
        }
        
        Log.d(TAG, "Submitting report with location: " + location);

        // Create report data (reporter info removed - using Firebase Auth UID)
        Map<String, Object> reportData = createReportDataWithReporterInfo(
            currentUser.getUid(),
            reportType,
            description,
            location
        );

        // Upload images and videos first if any, then submit report
        List<Uri> imageUris = new ArrayList<>();
        List<Uri> videoUris = new ArrayList<>();
        
        for (MediaItem mediaItem : selectedMediaItems) {
            if (mediaItem.isImage()) {
                imageUris.add(mediaItem.getUri());
            } else if (mediaItem.isVideo()) {
                videoUris.add(mediaItem.getUri());
            }
        }
        
        // Also add from legacy selectedImageUris for backward compatibility
        for (Uri uri : selectedImageUris) {
            if (!imageUris.contains(uri)) {
                imageUris.add(uri);
            }
        }
        
        if (!imageUris.isEmpty() || !videoUris.isEmpty()) {
            uploadReportMediaAndSubmit(reportData, imageUris, videoUris);
        } else {
            // Submit report without media
            submitReportToFirestore(reportData);
        }
    }

    /**
     * Upload both images and videos, then submit report
     */
    private void uploadReportMediaAndSubmit(Map<String, Object> reportData, List<Uri> imageUris, List<Uri> videoUris) {
        // Generate a temporary report ID for organizing media
        final String tempReportId = "temp_" + System.currentTimeMillis();
        
        // Keep image and video URLs separate
        final List<String> imageUrlsList = new ArrayList<>();
        final List<String> videoUrlsList = new ArrayList<>();
        final int[] uploadCount = {0};
        final int totalMedia = imageUris.size() + videoUris.size();
        
        if (totalMedia == 0) {
            submitReportToFirestore(reportData);
            return;
        }
        
        submitReportButton.setEnabled(false);
        submitReportButton.setText("Submitting...");
        
        // Upload images first
        if (!imageUris.isEmpty()) {
            StorageHelper.uploadReportImages(tempReportId, imageUris,
                new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> imageUrls) {
                        imageUrlsList.addAll(imageUrls);
                        uploadCount[0] += imageUrls.size();
                        Log.d(TAG, "✅ Images uploaded: " + imageUrls.size() + " image(s)");
                        
                        // Upload videos after images
                        if (!videoUris.isEmpty()) {
                            Log.d(TAG, "📹 Uploading " + videoUris.size() + " video(s) after images...");
                            StorageHelper.uploadReportVideos(ReportSubmissionActivity.this, tempReportId, videoUris,
                                new OnSuccessListener<List<String>>() {
                                    @Override
                                    public void onSuccess(List<String> videoUrls) {
                                        Log.d(TAG, "✅ Videos uploaded successfully: " + videoUrls.size() + " video(s)");
                                        videoUrlsList.addAll(videoUrls);
                                        uploadCount[0] += videoUrls.size();
                                        
                                        // All media uploaded
                                        if (uploadCount[0] == totalMedia) {
                                            Log.d(TAG, "✅ All media uploaded (" + uploadCount[0] + "/" + totalMedia + "), submitting report...");
                                            Log.d(TAG, "   - Image URLs: " + imageUrlsList.size());
                                            Log.d(TAG, "   - Video URLs: " + videoUrlsList.size());
                                            
                                            // Build compatibility media list (images + videos)
                                            List<String> combinedMediaUrls = new ArrayList<>(imageUrlsList);
                                            combinedMediaUrls.addAll(videoUrlsList);
                                            
                                            // Store pure image list for mobile rendering
                                            reportData.put("photoUrls", new ArrayList<>(imageUrlsList));
                                            
                                            // For backward compatibility, store combined media under imageUrls
                                            reportData.put("imageUrls", combinedMediaUrls);
                                            
                                            // Store videos separately for apps that support them
                                            reportData.put("videoUrls", new ArrayList<>(videoUrlsList));
                                            
                                            // Update videoCount to match actual uploaded videos
                                            reportData.put("videoCount", videoUrlsList.size());
                                            submitReportToFirestore(reportData);
                                        } else {
                                            Log.w(TAG, "⚠️ Upload count mismatch: " + uploadCount[0] + "/" + totalMedia);
                                            // Still submit with what we have
                                            List<String> combinedMediaUrls = new ArrayList<>(imageUrlsList);
                                            combinedMediaUrls.addAll(videoUrlsList);
                                            reportData.put("photoUrls", new ArrayList<>(imageUrlsList));
                                            reportData.put("imageUrls", combinedMediaUrls);
                                            reportData.put("videoUrls", new ArrayList<>(videoUrlsList));
                                            reportData.put("videoCount", videoUrlsList.size());
                                            submitReportToFirestore(reportData);
                                        }
                                    }
                                },
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "❌ Error uploading videos: " + e.getMessage(), e);
                                        // Only show toast for critical errors that prevent submission
                                        // Check if it's a permission error that might be recoverable
                                        String errorMsg = e.getMessage();
                                        if (errorMsg != null && errorMsg.contains("Permission denied")) {
                                            Toast.makeText(ReportSubmissionActivity.this, 
                                                "Video permission error. Please try selecting the video again.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(ReportSubmissionActivity.this, 
                                                "Error uploading videos: " + errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                        submitReportButton.setEnabled(true);
                                        submitReportButton.setText("Submit Report");
                                    }
                                });
                        } else {
                            // No videos, just submit with images
                            Log.d(TAG, "✅ Images uploaded successfully, submitting report with " + imageUrlsList.size() + " image(s)...");
                            List<String> combinedMediaUrls = new ArrayList<>(imageUrlsList);
                            reportData.put("photoUrls", new ArrayList<>(imageUrlsList));
                            reportData.put("imageUrls", combinedMediaUrls);
                            // Ensure videoUrls is empty list
                            reportData.put("videoUrls", new ArrayList<String>());
                            reportData.put("videoCount", 0);
                            submitReportToFirestore(reportData);
                        }
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error uploading images", e);
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Error uploading images: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                });
        } else {
            // Only videos, no images
            Log.d(TAG, "📹 Starting upload of " + videoUris.size() + " video(s) only (no images)");
            Log.d(TAG, "📹 Video URIs: " + videoUris.toString());
            
            // Show progress to user
            submitReportButton.setText("Submitting...");
            
            StorageHelper.uploadReportVideos(this, tempReportId, videoUris,
                new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> videoUrls) {
                        Log.d(TAG, "✅ Video upload success callback received with " + videoUrls.size() + " video URL(s)");
                        if (videoUrls == null || videoUrls.isEmpty()) {
                            Log.e(TAG, "❌ Video URLs list is null or empty!");
                            Toast.makeText(ReportSubmissionActivity.this, 
                                "Video upload completed but no URLs received. Please try again.", Toast.LENGTH_LONG).show();
                            submitReportButton.setEnabled(true);
                            submitReportButton.setText("Submit Report");
                            return;
                        }
                        
                        // Ensure imageUrls/photoUrls are set (even if empty) for consistency
                        if (!reportData.containsKey("imageUrls")) {
                            reportData.put("imageUrls", new ArrayList<String>());
                        }
                        if (!reportData.containsKey("photoUrls")) {
                            reportData.put("photoUrls", new ArrayList<String>());
                        }
                        
                        // Build compatibility list: since there are no images, combined = videos
                        List<String> combinedMediaUrls = new ArrayList<>(videoUrls);
                        reportData.put("photoUrls", new ArrayList<String>()); // No images captured
                        reportData.put("imageUrls", combinedMediaUrls);
                        reportData.put("videoUrls", new ArrayList<>(videoUrls));
                        // Update videoCount to match actual uploaded videos
                        reportData.put("videoCount", videoUrls.size());
                        Log.d(TAG, "✅ All videos uploaded successfully, submitting report with " + videoUrls.size() + " video(s)");
                        Log.d(TAG, "   - Video URLs: " + videoUrls.toString());
                        submitReportToFirestore(reportData);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Error uploading videos: " + e.getMessage(), e);
                        Log.e(TAG, "❌ Error class: " + e.getClass().getName());
                        if (e.getCause() != null) {
                            Log.e(TAG, "❌ Error cause: " + e.getCause().getMessage());
                        }
                        
                        // Only show toast for critical errors that prevent submission
                        String errorMsg = e.getMessage();
                        if (errorMsg == null) {
                            errorMsg = "Unknown error";
                        }
                        
                        // Check error type and show appropriate message
                        if (errorMsg.contains("Permission denied") || errorMsg.contains("permission")) {
                            Toast.makeText(ReportSubmissionActivity.this, 
                                "Video permission error. Please try selecting the video again.", Toast.LENGTH_LONG).show();
                        } else if (errorMsg.contains("Unable to access") || errorMsg.contains("Cannot access")) {
                            Toast.makeText(ReportSubmissionActivity.this, 
                                "Cannot access video file. Please try selecting the video again.", Toast.LENGTH_LONG).show();
                        } else if (errorMsg.contains("network") || errorMsg.contains("Network")) {
                            Toast.makeText(ReportSubmissionActivity.this, 
                                "Network error uploading videos. Please check your connection and try again.", Toast.LENGTH_LONG).show();
                        } else {
                            // Generic error message
                            Log.e(TAG, "❌ Video upload failed with error: " + errorMsg);
                            Toast.makeText(ReportSubmissionActivity.this, 
                                "Error uploading videos: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                });
        }
    }
    
    /**
     * @deprecated Use uploadReportMediaAndSubmit instead
     */
    @Deprecated
    private void uploadReportImagesAndSubmit(Map<String, Object> reportData) {
        // Generate a temporary report ID for organizing images
        final String tempReportId = "temp_" + System.currentTimeMillis();
        
        StorageHelper.uploadReportImages(tempReportId, selectedImageUris,
                new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> imageUrls) {
                        // Add image URLs to report data
                        reportData.put("imageUrls", imageUrls);
                        reportData.put("imageCount", imageUrls.size());
                        
                        // Submit report with images
                        submitReportToFirestore(reportData);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error uploading images", e);
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Error uploading images: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                });
    }

    private void submitReportToFirestore(Map<String, Object> reportData) {
        // Ensure imageUrls and videoUrls are always present (even if empty)
        if (!reportData.containsKey("imageUrls")) {
            reportData.put("imageUrls", new ArrayList<String>());
        }
        if (!reportData.containsKey("videoUrls")) {
            reportData.put("videoUrls", new ArrayList<String>());
        }
        // Ensure videoCount is present and matches videoUrls size
        if (!reportData.containsKey("videoCount")) {
            Object videoUrlsObj = reportData.get("videoUrls");
            int videoCount = 0;
            if (videoUrlsObj instanceof List) {
                videoCount = ((List<?>) videoUrlsObj).size();
            }
            reportData.put("videoCount", videoCount);
        }
        
        // Ensure videoUrls is a proper List<String> (not ArrayList or other type)
        Object videoUrlsObj = reportData.get("videoUrls");
        if (videoUrlsObj != null) {
            if (videoUrlsObj instanceof List) {
                // Convert to ArrayList<String> to ensure proper serialization
                List<String> videoUrlsList = new ArrayList<>();
                for (Object item : (List<?>) videoUrlsObj) {
                    if (item != null) {
                        videoUrlsList.add(item.toString());
                    }
                }
                reportData.put("videoUrls", videoUrlsList);
            }
        }
        
        // Ensure imageUrls is also a proper List<String>
        Object imageUrlsObj = reportData.get("imageUrls");
        if (imageUrlsObj != null) {
            if (imageUrlsObj instanceof List) {
                List<String> imageUrlsList = new ArrayList<>();
                for (Object item : (List<?>) imageUrlsObj) {
                    if (item != null) {
                        imageUrlsList.add(item.toString());
                    }
                }
                reportData.put("imageUrls", imageUrlsList);
            }
        }
        
        // Ensure photoUrls (pure images) is a List<String>
        Object photoUrlsObj = reportData.get("photoUrls");
        if (photoUrlsObj != null) {
            if (photoUrlsObj instanceof List) {
                List<String> photoUrlsList = new ArrayList<>();
                for (Object item : (List<?>) photoUrlsObj) {
                    if (item != null) {
                        photoUrlsList.add(item.toString());
                    }
                }
                reportData.put("photoUrls", photoUrlsList);
            }
        } else {
            reportData.put("photoUrls", new ArrayList<String>());
        }
        
        // Log report data for debugging
        Log.d(TAG, "📤 Submitting report to Firestore:");
        Log.d(TAG, "   - Has imageUrls: " + (reportData.containsKey("imageUrls") ? "Yes (" + ((List<?>) reportData.get("imageUrls")).size() + " items)" : "No"));
        Log.d(TAG, "   - Has videoUrls: " + (reportData.containsKey("videoUrls") ? "Yes (" + ((List<?>) reportData.get("videoUrls")).size() + " items)" : "No"));
        
        // Log actual video URLs for debugging
        if (reportData.containsKey("videoUrls")) {
            List<?> videoUrls = (List<?>) reportData.get("videoUrls");
            if (videoUrls != null && !videoUrls.isEmpty()) {
                Log.d(TAG, "   - Video URLs count: " + videoUrls.size());
                for (int i = 0; i < videoUrls.size(); i++) {
                    Log.d(TAG, "   - Video URL[" + i + "]: " + videoUrls.get(i).toString());
                }
            } else {
                Log.d(TAG, "   - Video URLs: Empty list");
            }
        }
        
        // Log all keys in reportData to verify structure
        Log.d(TAG, "   - Report data keys: " + reportData.keySet().toString());
        
        // Final verification: Ensure videoUrls is definitely a List and not null
        Object finalVideoUrlsCheck = reportData.get("videoUrls");
        if (finalVideoUrlsCheck == null) {
            Log.e(TAG, "❌ CRITICAL: videoUrls is NULL before saving to Firestore!");
            reportData.put("videoUrls", new ArrayList<String>());
        } else if (!(finalVideoUrlsCheck instanceof List)) {
            Log.e(TAG, "❌ CRITICAL: videoUrls is not a List before saving! Type: " + finalVideoUrlsCheck.getClass().getName());
            // Convert to List
            List<String> convertedList = new ArrayList<>();
            convertedList.add(finalVideoUrlsCheck.toString());
            reportData.put("videoUrls", convertedList);
        } else {
            List<?> finalList = (List<?>) finalVideoUrlsCheck;
            Log.d(TAG, "✅ FINAL CHECK: videoUrls is a List with " + finalList.size() + " item(s)");
            if (!finalList.isEmpty()) {
                Log.d(TAG, "✅ FINAL CHECK: First video URL: " + finalList.get(0).toString());
            }
        }
        
        FirestoreHelper.createReportWithAutoId(reportData,
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "✅ Report submitted successfully with ID: " + documentReference.getId());
                        
                        // Verify the saved document contains videoUrls
                        documentReference.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Object savedVideoUrls = documentSnapshot.get("videoUrls");
                                if (savedVideoUrls != null) {
                                    if (savedVideoUrls instanceof List) {
                                        List<?> savedList = (List<?>) savedVideoUrls;
                                        Log.d(TAG, "✅ VERIFIED: videoUrls saved to Firestore with " + savedList.size() + " video(s)");
                                        for (int i = 0; i < savedList.size(); i++) {
                                            Log.d(TAG, "   - Saved Video URL[" + i + "]: " + savedList.get(i).toString());
                                        }
                                    } else {
                                        Log.w(TAG, "⚠️ videoUrls saved but not as List: " + savedVideoUrls.getClass().getSimpleName());
                                    }
                                } else {
                                    Log.e(TAG, "❌ ERROR: videoUrls NOT found in saved document!");
                                }
                                
                                // Log all fields in the saved document
                                Log.d(TAG, "📋 Saved document fields: " + documentSnapshot.getData().keySet().toString());
                            }
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error verifying saved document: " + e.getMessage(), e);
                        });
                        
                        // If report has images, reorganize them with the actual report ID
                        if (reportData.containsKey("imageUrls")) {
                            List<String> imageUrls = (List<String>) reportData.get("imageUrls");
                            if (imageUrls != null && !imageUrls.isEmpty()) {
                                reorganizeImagesWithReportId(documentReference.getId(), imageUrls);
                            }
                        }
                        
                        // Web app Cloud Functions will automatically detect new report and send notifications to web admins
                        Log.d(TAG, "✅ Report submitted - web app Cloud Functions will handle notifications");
                        
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Clear form first (before switching tabs)
                        clearForm();
                        
                        // Reload reports from Firestore to show the new report in Report Log
                        loadUserReportsFromFirestore();
                        
                        // Switch to Report Log tab to show the new report
                        switchToReportLogTab();
                        
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Error submitting report to Firestore: " + e.getMessage(), e);
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Error submitting report: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                });
    }

    private void reorganizeImagesWithReportId(String reportId, List<String> imageUrls) {
        // This method can be implemented to move images from temp folder to actual report folder
        // For now, we'll just log the reorganization
        Log.d(TAG, "Images uploaded for report " + reportId + ": " + imageUrls.size() + " images");
    }

    private Map<String, Object> createReportDataWithReporterInfo(String userId, String reportType, 
                                                              String description, String location) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("reportType", reportType);
        reportData.put("title", reportType); // Add title field
        reportData.put("category", reportType); // Add category field
        reportData.put("description", description);
        
        // Auto-fill reporter info from registration (SharedPreferences)
        String userBarangay = "";
        try {
            SharedPreferences userPrefs = getSharedPreferences(USER_PREFS_NAME, MODE_PRIVATE);
            String firstName = userPrefs.getString("first_name", "");
            String lastName = userPrefs.getString("last_name", "");
            String mobile = userPrefs.getString("mobile_number", "");

            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                reportData.put("reporterName", fullName);
            }
            if (mobile != null && !mobile.trim().isEmpty()) {
                reportData.put("reporterMobile", mobile.trim());
            }
            
            // ✅ CRITICAL: Get user's barangay from profile FIRST (needed for patient info)
            // Priority 1: Direct barangay field
            userBarangay = userPrefs.getString("barangay", "");
            
            // Priority 2: Extract from location_text (format: "City, Barangay")
            if ((userBarangay == null || userBarangay.trim().isEmpty()) && userPrefs.contains("location_text")) {
                String locationText = userPrefs.getString("location_text", "");
                if (locationText != null && !locationText.trim().isEmpty() && locationText.contains(",")) {
                    String[] parts = locationText.split(",");
                    if (parts.length >= 2) {
                        userBarangay = parts[parts.length - 1].trim(); // Last part is usually barangay
                    }
                }
            }
            
            // Priority 3: Try selected_barangay field
            if ((userBarangay == null || userBarangay.trim().isEmpty())) {
                String selectedBarangay = userPrefs.getString("selected_barangay", "");
                if (selectedBarangay != null && !selectedBarangay.trim().isEmpty() && 
                    !"Other".equalsIgnoreCase(selectedBarangay) &&
                    !"Choose a barangay".equalsIgnoreCase(selectedBarangay)) {
                    userBarangay = selectedBarangay.trim();
                }
            }
            
            // Priority 4: Try barangay_other field
            if ((userBarangay == null || userBarangay.trim().isEmpty())) {
                String barangayOther = userPrefs.getString("barangay_other", "");
                if (barangayOther != null && !barangayOther.trim().isEmpty()) {
                    userBarangay = barangayOther.trim();
                }
            }
            
            if (userBarangay != null && !userBarangay.trim().isEmpty()) {
                Log.d(TAG, "✅ User barangay retrieved: " + userBarangay);
            } else {
                Log.w(TAG, "⚠️ No barangay found in user profile");
            }
            
            // ✅ NEW: If "I am the Patient" checkbox is checked, save all patient information
            // Use field names that match admin side expectations: Name, Contact Number, Address, Religion, Birthday, Blood Type
            if (patientCheckbox != null && patientCheckbox.isChecked()) {
                Map<String, Object> patientInformation = new HashMap<>();
                
                // Get all user data from SharedPreferences
                String email = userPrefs.getString("email", "");
                String birthday = userPrefs.getString("birthday", "");
                String gender = userPrefs.getString("gender", "");
                String civilStatus = userPrefs.getString("civil_status", "");
                String religion = userPrefs.getString("religion", "");
                String bloodType = userPrefs.getString("blood_type", "");
                boolean isPWD = userPrefs.getBoolean("pwd", false);
                String province = userPrefs.getString("province", "");
                String city = userPrefs.getString("city", "");
                String cityTown = userPrefs.getString("cityTown", "");
                String mailingAddress = userPrefs.getString("mailing_address", "");
                String streetAddress = userPrefs.getString("street_address", "");
                
                // Build full address string for admin display
                StringBuilder fullAddressBuilder = new StringBuilder();
                if (streetAddress != null && !streetAddress.trim().isEmpty()) {
                    fullAddressBuilder.append(streetAddress.trim());
                }
                if (userBarangay != null && !userBarangay.trim().isEmpty()) {
                    if (fullAddressBuilder.length() > 0) {
                        fullAddressBuilder.append(", ");
                    }
                    fullAddressBuilder.append(userBarangay.trim());
                }
                String finalCityTown = (cityTown != null && !cityTown.trim().isEmpty()) ? cityTown : city;
                if (finalCityTown != null && !finalCityTown.trim().isEmpty()) {
                    if (fullAddressBuilder.length() > 0) {
                        fullAddressBuilder.append(", ");
                    }
                    fullAddressBuilder.append(finalCityTown.trim());
                }
                if (province != null && !province.trim().isEmpty()) {
                    if (fullAddressBuilder.length() > 0) {
                        fullAddressBuilder.append(", ");
                    }
                    fullAddressBuilder.append(province.trim());
                }
                String fullAddress = fullAddressBuilder.toString().trim();
                
                // Save patient information with field names matching admin expectations
                // Always save all fields (even if empty) so admin can display them properly
                patientInformation.put("name", fullName.isEmpty() ? "" : fullName);
                patientInformation.put("firstName", firstName != null ? firstName : "");
                patientInformation.put("lastName", lastName != null ? lastName : "");
                patientInformation.put("contactNumber", mobile != null && !mobile.trim().isEmpty() ? mobile.trim() : "");
                patientInformation.put("email", email != null ? email : "");
                patientInformation.put("address", fullAddress.isEmpty() ? "" : fullAddress);
                patientInformation.put("religion", religion != null ? religion : "");
                patientInformation.put("birthday", birthday != null ? birthday : "");
                patientInformation.put("bloodType", bloodType != null ? bloodType : "");
                patientInformation.put("gender", gender != null ? gender : "");
                patientInformation.put("civilStatus", civilStatus != null ? civilStatus : "");
                patientInformation.put("isPWD", isPWD);
                
                // Also save address components separately for flexibility
                patientInformation.put("province", province != null ? province : "");
                patientInformation.put("cityTown", finalCityTown != null ? finalCityTown : "");
                patientInformation.put("barangay", userBarangay != null ? userBarangay : "");
                patientInformation.put("streetAddress", streetAddress != null ? streetAddress : "");
                patientInformation.put("mailingAddress", mailingAddress != null ? mailingAddress : "");
                
                // Store patient information in report
                reportData.put("patientInformation", patientInformation);
                reportData.put("isPatient", true);
                
                // Comprehensive logging
                Log.d(TAG, "✅ Patient information saved to report (checkbox checked)");
                Log.d(TAG, "   Patient Name: " + fullName);
                Log.d(TAG, "   Contact Number: " + mobile);
                Log.d(TAG, "   Address: " + fullAddress);
                Log.d(TAG, "   Religion: " + religion);
                Log.d(TAG, "   Birthday: " + birthday);
                Log.d(TAG, "   Blood Type: " + bloodType);
                Log.d(TAG, "   Gender: " + gender);
                Log.d(TAG, "   Civil Status: " + civilStatus);
                Log.d(TAG, "   Is PWD: " + isPWD);
                Log.d(TAG, "   Full patientInformation map: " + patientInformation.toString());
            } else {
                reportData.put("isPatient", false);
                Log.d(TAG, "ℹ️ Patient checkbox not checked - patient information not saved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user info: " + e.getMessage(), e);
        }
        
        // Store location information from map picker
        if (isLocationSelected && selectedLongitude != 0.0 && selectedLatitude != 0.0) {
            // Store location data from map picker
            String coordinatesText = String.format("%.6f, %.6f", selectedLatitude, selectedLongitude);
            reportData.put("locationName", selectedLocationName);
            reportData.put("latitude", selectedLatitude);
            reportData.put("longitude", selectedLongitude);
            reportData.put("coordinates", coordinatesText);
            reportData.put("location", selectedLocationName + " (" + coordinatesText + ")");
            
            // ✅ CRITICAL: Store barangay field for report matching
            // Try to extract barangay from locationName first, fallback to user's barangay
            String reportBarangay = extractBarangayFromLocationName(selectedLocationName);
            if (reportBarangay == null || reportBarangay.trim().isEmpty()) {
                reportBarangay = userBarangay;
            }
            
            if (reportBarangay != null && !reportBarangay.trim().isEmpty()) {
                reportData.put("barangay", reportBarangay.trim());
                Log.d(TAG, "✅ Barangay stored in report: " + reportBarangay);
            } else {
                Log.w(TAG, "⚠️ No barangay available to store in report");
            }
            
            Log.d(TAG, "✅ Using map picker location data:");
            Log.d(TAG, "   Name: " + selectedLocationName);
            Log.d(TAG, "   Barangay: " + (reportBarangay != null ? reportBarangay : "Not found"));
            Log.d(TAG, "   Lat: " + selectedLatitude + ", Lon: " + selectedLongitude);
        } else {
            // No location selected from map (should not happen due to validation)
            Log.w(TAG, "⚠️ No location selected from map picker");
            reportData.put("locationName", "Location not specified");
            reportData.put("latitude", null);
            reportData.put("longitude", null);
            reportData.put("coordinates", "");
            reportData.put("location", location);
            
            // Still try to store user's barangay even if location not selected
            if (userBarangay != null && !userBarangay.trim().isEmpty()) {
                reportData.put("barangay", userBarangay.trim());
                Log.d(TAG, "✅ Barangay stored in report (no location): " + userBarangay);
            }
        }
        
        // Reporter information auto-filled above; user also identified by userId (Firebase Auth UID)
        reportData.put("timestamp", System.currentTimeMillis());
        reportData.put("status", "Pending");
        reportData.put("priority", "medium"); // Default priority
        // Count total media items (images + videos) for imageCount field
        int videoCount = 0;
        for (MediaItem item : selectedMediaItems) {
            if (item.isVideo()) {
                videoCount++;
            }
        }
        reportData.put("imageCount", selectedImageUris.size() + videoCount);
        reportData.put("videoCount", videoCount); // Add videoCount field for web app compatibility
        reportData.put("photoUrls", new ArrayList<>()); // Pure image list for mobile rendering
        reportData.put("imageUrls", new ArrayList<>()); // Compatibility field (web uses this)
        reportData.put("videoUrls", new ArrayList<>()); // Will be updated after upload
        
        // Get current date and time
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        java.util.Date currentDate = new java.util.Date();
        
        reportData.put("createdDate", dateFormat.format(currentDate));
        reportData.put("createdTime", timeFormat.format(currentDate));
        
        return reportData;
    }
    
    /**
     * Extract barangay name from location name string
     * Handles different formats like "Street, Barangay, City" or "Barangay, City"
     * @param locationName The location name string from map picker
     * @return Extracted barangay name or null if not found
     */
    private String extractBarangayFromLocationName(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return null;
        }
        
        try {
            String cleaned = locationName.trim();
            
            // Common barangay patterns in location names
            // Pattern 1: "Street, Barangay Name, City" or "Barangay Name, City"
            if (cleaned.contains(",")) {
                String[] parts = cleaned.split(",");
                
                // Try to find barangay in the parts
                for (String part : parts) {
                    String trimmed = part.trim();
                    
                    // Check if this part looks like a barangay
                    // Barangays often have "Brgy", "Barangay", or are standalone names
                    if (trimmed.toLowerCase().contains("brgy") || 
                        trimmed.toLowerCase().contains("barangay") ||
                        trimmed.matches("^[A-Za-z\\s]+$")) { // Simple name without numbers/special chars
                        
                        // Remove common prefixes
                        String barangay = trimmed.replaceAll("(?i)^(brgy\\.?|barangay|brg\\.?)\\s*", "");
                        barangay = barangay.trim();
                        
                        // Skip if it's too short or looks like a city name
                        if (barangay.length() >= 3 && 
                            !barangay.equalsIgnoreCase("Lucban") &&
                            !barangay.equalsIgnoreCase("Quezon") &&
                            !barangay.equalsIgnoreCase("Philippines")) {
                            Log.d(TAG, "✅ Extracted barangay from location name: " + barangay);
                            return barangay;
                        }
                    }
                }
            }
            
            // Pattern 2: Check if the whole string is a barangay name
            String normalized = cleaned.replaceAll("(?i)^(brgy\\.?|barangay|brg\\.?)\\s*", "");
            if (normalized.length() >= 3 && 
                !normalized.equalsIgnoreCase("Lucban") &&
                !normalized.equalsIgnoreCase("Quezon")) {
                return normalized.trim();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting barangay from location name: " + e.getMessage(), e);
        }
        
        return null;
    }

    private boolean validateForm() {
        // Check if report type is selected
        if (reportTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Reporter fields removed - user identified by Firebase Auth
        // No validation needed for reporter name and mobile
        
        // Description is optional - no validation required
        
        // Check if location is selected from map (coordinates must be pinned)
        if (!isLocationSelected || selectedLongitude == 0.0 || selectedLatitude == 0.0) {
            Toast.makeText(this, "Please select a location on the map using the pin button", Toast.LENGTH_LONG).show();
            coordinatesEditText.setError("Location must be selected on map");
            return false;
        }
        
        // Check if location name is available (location name is displayed in the field)
        if (selectedLocationName == null || selectedLocationName.trim().isEmpty()) {
            Toast.makeText(this, "Location error. Please select location again.", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "✅ Form validation passed - Location: " + selectedLocationName + 
                   " (" + selectedLatitude + ", " + selectedLongitude + ")");
        return true;
    }

    private void clearForm() {
        try {
            // Clear report type
            reportTypeSpinner.setSelection(0);
            
            // Clear description
            descriptionEditText.setText("");
            descriptionEditText.clearFocus();
            
            // Clear location/coordinates
            coordinatesEditText.setText("");
            coordinatesEditText.clearFocus();
            locationEditText.setText(""); // Legacy field
            
            // Clear all media items (images and videos)
            selectedImageUris.clear();
            selectedMediaItems.clear();
            updateProfessionalImageGallery();
            
            // Reset location selection state
            resetLocationSelection();
            
            // Reset patient checkbox
            if (patientCheckbox != null) {
                patientCheckbox.setChecked(false);
            }
            
            // Note: Do NOT switch tabs here - let the caller decide which tab to show
            // After submission, we switch to Report Log tab
            // When user manually switches back to Submit Report tab, form will already be clear
            
            Log.d(TAG, "✅ Form cleared successfully - ready for new report submission");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing form: " + e.getMessage(), e);
        }
    }
    
    /**
     * Make coordinates field read-only and add click listener to reopen MapPickerActivity
     */
    private void makeCoordinatesEditTextReadOnly() {
        if (coordinatesEditText != null) {
            // Keep coordinates EditText enabled but make it clickable to change location
            coordinatesEditText.setFocusable(false);
            coordinatesEditText.setFocusableInTouchMode(false);
            coordinatesEditText.setClickable(true);
            
            // Add click listener to reopen MapPickerActivity
            coordinatesEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Coordinates field clicked - reopening map picker");
                    openMapPicker();
                }
            });
            
            Log.d(TAG, "✅ Coordinates field set to read-only with click-to-change");
        }
    }
    
    /**
     * Reset location selection state and make coordinates field editable again
     */
    private void resetLocationSelection() {
        isLocationSelected = false;
        selectedLocationName = "";
        selectedLongitude = 0.0;
        selectedLatitude = 0.0;
        
        if (coordinatesEditText != null) {
            // Reset coordinates EditText to initial state
            coordinatesEditText.setFocusable(false);
            coordinatesEditText.setFocusableInTouchMode(false);
            coordinatesEditText.setClickable(false);
            coordinatesEditText.setOnClickListener(null);
            coordinatesEditText.setEnabled(false);
            coordinatesEditText.setHint("Select location on map to get coordinates");
        }
        
        Log.d(TAG, "✅ Location selection reset");
    }

    private void showImageInDialog(Uri imageUri) {
        if (selectedMediaItems.isEmpty() && selectedImageUris.isEmpty()) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
        
        // Setup horizontal RecyclerView in dialog
        RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialogImagesRecyclerView);
        LinearLayoutManager dialogLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dialogRecyclerView.setLayoutManager(dialogLayoutManager);
        
        // Combine MediaItems for dialog adapter
        List<MediaItem> dialogMediaItems = new ArrayList<>(selectedMediaItems);
        // Add legacy selectedImageUris as MediaItems if not already present
        for (Uri uri : selectedImageUris) {
            boolean exists = false;
            for (MediaItem item : dialogMediaItems) {
                if (item.getUri().equals(uri)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                dialogMediaItems.add(new MediaItem(uri, MediaItem.TYPE_IMAGE));
            }
        }
        
        MediaGalleryAdapter dialogAdapter = new MediaGalleryAdapter(this, dialogMediaItems);
        dialogAdapter.setOnMediaRemoveListener(null); // Disable remove in dialog
        dialogAdapter.setOnMediaClickListener(new MediaGalleryAdapter.OnMediaClickListener() {
            @Override
            public void onMediaClick(int position, MediaItem mediaItem) {
                if (mediaItem.isImage()) {
                    // Show full screen image view
                    showFullScreenImage(mediaItem.getUri());
                } else if (mediaItem.isVideo()) {
                    // Show video player
                    showVideoInDialog(mediaItem.getUri());
                }
            }
        });
        
        dialogRecyclerView.setAdapter(dialogAdapter);
        
        // Fallback vertical layout (hidden)
        LinearLayout imagesContainer = dialogView.findViewById(R.id.imagesContainer);
        imagesContainer.setVisibility(View.GONE);
        
        builder.setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void showFullScreenImage(Uri imageUri) {
        try {
            Log.d(TAG, "Showing full screen image: " + imageUri.toString());
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_fullscreen_image, null);
            
            ImageView fullScreenImageView = dialogView.findViewById(R.id.fullScreenImageView);
            if (fullScreenImageView != null) {
                // For URL-based images, use ProfilePictureCache for better loading
                if (imageUri.toString().startsWith("http")) {
                    Log.d(TAG, "Loading image from URL: " + imageUri.toString());
                    ProfilePictureCache.getInstance().loadChatImage(fullScreenImageView, imageUri.toString());
                } else {
                    // For local URIs, use setImageURI
                    fullScreenImageView.setImageURI(imageUri);
                }
                
                fullScreenImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                fullScreenImageView.setAdjustViewBounds(true);
                
                // Add click to close functionality
                fullScreenImageView.setOnClickListener(v -> {
                    // Close dialog when image is clicked
                    if (builder != null) {
                        // Find the dialog and dismiss it
                        // This is a bit tricky, so we'll just log for now
                        Log.d(TAG, "Full screen image clicked - should close dialog");
                    }
                });
            }
            
            AlertDialog dialog = builder.setView(dialogView)
                    .setCancelable(true)
                    .create();
            
            // Make image clickable to close dialog
            if (fullScreenImageView != null) {
                fullScreenImageView.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            }
            
            dialog.show();
                    
            Log.d(TAG, "Full screen image dialog shown");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error showing full screen image", e);
            Toast.makeText(this, "Error loading full screen image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show video in a dialog with playback controls
     */
    private void showVideoInDialog(Uri videoUri) {
        try {
            Log.d(TAG, "Showing video in dialog: " + videoUri.toString());
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_fullscreen_video, null);
            
            VideoView videoView = dialogView.findViewById(R.id.fullScreenVideoView);
            if (videoView != null) {
                // Set up video player
                try {
                    // Clear previous video URI to avoid issues
                    videoView.setVideoURI(null);
                    
                    // Set the video URI
                    videoView.setVideoURI(videoUri);
                    
                    // Create and set MediaController for video controls
                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    
                    // Set up video player callbacks
                    videoView.setOnPreparedListener(mp -> {
                        Log.d(TAG, "Video prepared successfully");
                        // After preparation, seek to 1ms to show first frame
                        try {
                            videoView.seekTo(1);
                        } catch (Exception e) {
                            Log.e(TAG, "Error seeking video", e);
                        }
                    });
                    
                    videoView.setOnErrorListener((mp, what, extra) -> {
                        Log.e(TAG, "Video playback error: what=" + what + ", extra=" + extra);
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Error playing video", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    
                    videoView.setOnCompletionListener(mp -> {
                        Log.d(TAG, "Video playback completed");
                    });
                    
                    // Request focus to ensure video view is properly displayed
                    videoView.requestFocus();
                    
                    Log.d(TAG, "Video player setup complete for URI: " + videoUri.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up video player: " + e.getMessage(), e);
                    Toast.makeText(this, "Error setting up video player: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            AlertDialog dialog = builder.setView(dialogView)
                    .setPositiveButton("Close", null)
                    .create();
            
            // Stop video playback when dialog is dismissed
            dialog.setOnDismissListener(dialogInterface -> {
                if (videoView != null) {
                    videoView.stopPlayback();
                    videoView.setVideoURI(null);
                }
            });
            
            dialog.show();
                    
            Log.d(TAG, "Video dialog shown successfully");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error showing video dialog", e);
            Toast.makeText(this, "Error loading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Tab functionality methods
    private void setupTabFunctionality() {
        // Start with Submit Report tab active
        switchToSubmitReportTab();
    }
    
    private void switchToSubmitReportTab() {
        // Update tab text colors - find TextViews by their position in the tab layout
        TextView submitTabText = (TextView) submitReportTab.getChildAt(0);
        TextView reportLogTabText = (TextView) reportLogTab.getChildAt(0);
        
        if (submitTabText != null) {
            submitTabText.setTextColor(0xFFFF5722); // Orange color
        }
        if (reportLogTabText != null) {
            reportLogTabText.setTextColor(0xFF666666); // Dark gray color
        }
        
        // Show/hide content
        submitReportContent.setVisibility(View.VISIBLE);
        reportLogContent.setVisibility(View.GONE);
        
        // Update tab indicators
        updateTabIndicators(true, false);
        
        // Note: Form is already cleared after submission, so when user switches back
        // to this tab, the form will be ready for a new submission
    }
    
    private void switchToReportLogTab() {
        // Update tab text colors - find TextViews by their position in the tab layout
        TextView submitTabText = (TextView) submitReportTab.getChildAt(0);
        TextView reportLogTabText = (TextView) reportLogTab.getChildAt(0);
        
        if (submitTabText != null) {
            submitTabText.setTextColor(0xFF666666); // Dark gray color
        }
        if (reportLogTabText != null) {
            reportLogTabText.setTextColor(0xFFFF5722); // Orange color
        }
        
        // Show/hide content
        submitReportContent.setVisibility(View.GONE);
        reportLogContent.setVisibility(View.VISIBLE);
        
        // Update tab indicators
        updateTabIndicators(false, true);
        
        // Note: loadUserReportsFromFirestore() already sets up a real-time listener
        // that will automatically update the UI when reports change, so no manual refresh needed
    }
    
    private void updateTabIndicators(boolean submitReportActive, boolean reportLogActive) {
        // Update tab indicator colors using the specific indicator views
        if (submitReportIndicator != null) {
            submitReportIndicator.setBackgroundColor(submitReportActive ? 
                0xFFFF5722 : // Orange color
                0x00000000); // Transparent
        }
        
        if (reportLogIndicator != null) {
            reportLogIndicator.setBackgroundColor(reportLogActive ? 
                0xFFFF5722 : // Orange color
                0x00000000); // Transparent
        }
    }

    // Report filtering and data management methods
    private void loadUserReportsFromFirestore() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user, cannot load reports");
                return;
            }
            
            Log.d(TAG, "Loading reports for current user: " + currentUser.getUid());
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Load ONLY the current user's reports (filtered by userId)
            // This ensures each user only sees their own submitted reports
            // Order by timestamp descending to show newest reports first
            // Note: Firestore requires an index for queries with orderBy, but we're sorting in memory instead
            db.collection("reports")
                    .whereEqualTo("userId", currentUser.getUid())
                    .addSnapshotListener((queryDocumentSnapshots, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Error loading reports from Firestore", error);
                            Toast.makeText(this, "Error loading reports: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (queryDocumentSnapshots != null) {
                            int previousCount = allReports.size();
                            allReports.clear();
                            
                            Log.d(TAG, "📊 Processing " + queryDocumentSnapshots.size() + " report documents from Firestore");
                            
                            int successfullyParsed = 0;
                            int failedParsing = 0;
                            
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                try {
                                    Report report = convertDocumentToReport(doc);
                                    if (report != null) {
                                        allReports.add(report);
                                        successfullyParsed++;
                                    } else {
                                        failedParsing++;
                                        Log.w(TAG, "⚠️ Report conversion returned null for document: " + doc.getId());
                                    }
                                } catch (Exception e) {
                                    failedParsing++;
                                    Log.e(TAG, "❌ Error parsing report document " + doc.getId() + ": " + e.getMessage(), e);
                                }
                            }
                            
                            // Sort reports by timestamp in memory (newest first)
                            try {
                                allReports.sort((r1, r2) -> {
                                    try {
                                        return Long.compare(r2.getTimestamp(), r1.getTimestamp());
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error comparing timestamps", e);
                                        return 0;
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error sorting reports", e);
                            }
                            
                            Log.d(TAG, "📊 Report loading summary:");
                            Log.d(TAG, "   - Total documents: " + queryDocumentSnapshots.size());
                            Log.d(TAG, "   - Successfully parsed: " + successfullyParsed);
                            Log.d(TAG, "   - Failed parsing: " + failedParsing);
                            Log.d(TAG, "   - Final allReports count: " + allReports.size());
                            
                            // Update filtered reports
                            filteredReports.clear();
                            filteredReports.addAll(allReports);
                            
                            Log.d(TAG, "📋 Filtered reports count: " + filteredReports.size());
                            
                            // Notify adapter of data change on UI thread
                            // Note: Firestore listeners already run on main thread, but using runOnUiThread for safety
                            runOnUiThread(() -> {
                                try {
                                    if (reportLogAdapter != null) {
                                        reportLogAdapter.notifyDataSetChanged();
                                        
                                        // Force RecyclerView to remeasure and redraw
                                        if (reportLogRecyclerView != null) {
                                            reportLogRecyclerView.requestLayout();
                                            reportLogRecyclerView.invalidate();
                                        }
                                    }
                                    
                                    // Update status summary
                                    updateStatusSummary();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating UI after loading reports", e);
                                }
                            });
                            
                            // Show message if no reports found
                            if (allReports.isEmpty()) {
                                Log.d(TAG, "⚠️ No reports found for current user - Report Log will be empty");
                            } else {
                                Log.d(TAG, "✅ Successfully loaded and displayed " + allReports.size() + " reports in Report Log");
                            }
                        } else {
                            Log.w(TAG, "⚠️ queryDocumentSnapshots is null");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up reports listener", e);
        }
    }
    
    private Report convertDocumentToReport(QueryDocumentSnapshot doc) {
        try {
            Report report = new Report();
            report.setReportId(doc.getId());
            report.setUserId(doc.getString("userId"));
            report.setTitle(doc.getString("title"));
            report.setDescription(doc.getString("description"));
            report.setLocation(doc.getString("location"));
            report.setLocationName(doc.getString("locationName"));
            report.setCoordinates(doc.getString("coordinates"));
            report.setReporterName(doc.getString("reporterName"));
            report.setReporterMobile(doc.getString("reporterMobile"));
            report.setCategory(doc.getString("category"));
            report.setStatus(doc.getString("status"));
            report.setPriority(doc.getString("priority"));
            
            // Handle latitude and longitude
            Object latObj = doc.get("latitude");
            Object lonObj = doc.get("longitude");
            if (latObj instanceof Double) {
                report.setLatitude((Double) latObj);
            }
            if (lonObj instanceof Double) {
                report.setLongitude((Double) lonObj);
            }
            
            // Handle timestamp
            Object timestampObj = doc.get("timestamp");
            if (timestampObj instanceof Long) {
                report.setTimestamp((Long) timestampObj);
            } else if (timestampObj != null) {
                report.setTimestamp(Long.parseLong(timestampObj.toString()));
            }
            
            // Handle video URLs
            Object videoUrlsObj = doc.get("videoUrls");
            if (videoUrlsObj instanceof List) {
                report.setVideoUrls((List<String>) videoUrlsObj);
                Log.d(TAG, "✅ Video URLs loaded from Firestore: " + ((List<?>) videoUrlsObj).size() + " video(s)");
            }
            
            // Handle photo URLs (pure images)
            List<String> resolvedImageUrls = null;
            Object photoUrlsObj = doc.get("photoUrls");
            if (photoUrlsObj instanceof List) {
                resolvedImageUrls = new ArrayList<>((List<String>) photoUrlsObj);
                Log.d(TAG, "✅ Photo URLs (pure images) loaded: " + resolvedImageUrls.size());
            }

            // Fallback to imageUrls if photoUrls not available
            if (resolvedImageUrls == null) {
                Object imageUrlsObj = doc.get("imageUrls");
                if (imageUrlsObj instanceof List) {
                    resolvedImageUrls = new ArrayList<>((List<String>) imageUrlsObj);
                    Log.d(TAG, "✅ Fallback imageUrls loaded: " + resolvedImageUrls.size());
                }
            }

            // Remove any video URLs from the image list (in case of compatibility duplication)
            if (resolvedImageUrls != null && videoUrlsObj instanceof List) {
                resolvedImageUrls.removeAll((List<String>) videoUrlsObj);
            }

            if (resolvedImageUrls != null) {
                report.setImageUrls(resolvedImageUrls);
            }
            
            // Handle image count
            Object imageCountObj = doc.get("imageCount");
            if (imageCountObj instanceof Long) {
                report.setImageCount(((Long) imageCountObj).intValue());
            } else if (imageCountObj instanceof Integer) {
                report.setImageCount((Integer) imageCountObj);
            }
            
            return report;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to report", e);
            return null;
        }
    }

    
    private void updateStatusSummary() {
        try {
            // Count reports by status
            int pendingCount = 0;
            int ongoingCount = 0;
            int respondedCount = 0;
            int notRespondedCount = 0;
            int redundantCount = 0;
            int falseReportCount = 0;
            int totalCount = allReports.size();
            
            for (Report report : allReports) {
                String status = report.getStatus();
                if (status != null) {
                    switch (status.toLowerCase()) {
                        case "pending":
                            pendingCount++;
                            break;
                        case "ongoing":
                            ongoingCount++;
                            break;
                        case "responded":
                            respondedCount++;
                            break;
                        case "not responded":
                            notRespondedCount++;
                            break;
                        case "redundant":
                            redundantCount++;
                            break;
                        case "false report":
                            falseReportCount++;
                            break;
                    }
                }
            }
            
            Log.d(TAG, "Status Summary - Total: " + totalCount + 
                      ", Pending: " + pendingCount + 
                      ", Ongoing: " + ongoingCount + 
                      ", Responded: " + respondedCount + 
                      ", Not Responded: " + notRespondedCount + 
                      ", Redundant: " + redundantCount +
                      ", False Report: " + falseReportCount);
            
            // Update the status count TextViews in the Report Log UI
            updateStatusCountTextViews(pendingCount, ongoingCount, respondedCount, notRespondedCount, redundantCount, falseReportCount, totalCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating status summary", e);
        }
    }
    
    private void updateStatusCountTextViews(int pending, int ongoing, int responded, int notResponded, int redundant, int falseReport, int total) {
        try {
            // Update each status count TextView with real-time counts
            if (pendingCountText != null) {
                pendingCountText.setText(String.valueOf(pending));
            }
            
            if (ongoingCountText != null) {
                ongoingCountText.setText(String.valueOf(ongoing));
            }
            
            if (respondedCountText != null) {
                respondedCountText.setText(String.valueOf(responded));
            }
            
            if (unrespondedCountText != null) {
                unrespondedCountText.setText(String.valueOf(notResponded));
            }
            
            if (redundantCountText != null) {
                redundantCountText.setText(String.valueOf(redundant));
            }
            
            if (falseReportCountText != null) {
                falseReportCountText.setText(String.valueOf(falseReport));
            }
            
            if (totalCountText != null) {
                totalCountText.setText(String.valueOf(total));
            }
            
            Log.d(TAG, "Successfully updated all status count TextViews");
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating status count TextViews", e);
        }
    }

    /**
     * Custom LinearLayoutManager that disables internal vertical scrolling so the RecyclerView
     * expands to show all items inside the parent ScrollView.
     */
    private static class NonScrollableLinearLayoutManager extends LinearLayoutManager {
        NonScrollableLinearLayoutManager(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
        
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                // Sometimes RecyclerView throws this when data changes rapidly
                // Log and continue - the layout will be corrected on next pass
                android.util.Log.e("NonScrollableLinearLayoutManager", "IndexOutOfBoundsException in onLayoutChildren: " + e.getMessage());
            } catch (Exception e) {
                // Catch any other exceptions to prevent crashes
                android.util.Log.e("NonScrollableLinearLayoutManager", "Exception in onLayoutChildren: " + e.getMessage(), e);
            }
        }
    }
    

    private void showReportDetailsDialog(Report report) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_report_details, null);
            
            // Find views in dialog
            TextView tvReportId = dialogView.findViewById(R.id.tvReportId);
            TextView tvReportType = dialogView.findViewById(R.id.tvReportType);
            TextView tvReporterName = dialogView.findViewById(R.id.tvReporterName);
            TextView tvReporterMobile = dialogView.findViewById(R.id.tvReporterMobile);
            TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
            TextView tvCoordinates = dialogView.findViewById(R.id.tvCoordinates);
            TextView tvDescription = dialogView.findViewById(R.id.tvDescription);
            TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
            TextView tvDateTime = dialogView.findViewById(R.id.tvDateTime);
            TextView tvAttachmentCount = dialogView.findViewById(R.id.tvAttachmentCount);
            Button btnViewAttachments = dialogView.findViewById(R.id.btnViewAttachments);
            Button btnClose = dialogView.findViewById(R.id.btnClose);
            
            // Set report data
            if (tvReportId != null) tvReportId.setText("Report ID: " + (report.getReportId() != null ? report.getReportId() : "N/A"));
            if (tvReportType != null) tvReportType.setText(report.getCategory() != null ? report.getCategory() : "N/A");
            if (tvReporterName != null) tvReporterName.setText(report.getReporterName() != null ? report.getReporterName() : "N/A");
            if (tvReporterMobile != null) tvReporterMobile.setText(report.getReporterMobile() != null ? report.getReporterMobile() : "N/A");
            if (tvLocation != null) tvLocation.setText(report.getLocationName() != null ? report.getLocationName() : report.getLocation());
            if (tvCoordinates != null) tvCoordinates.setText(report.getCoordinates() != null ? report.getCoordinates() : "N/A");
            if (tvDescription != null) tvDescription.setText(report.getDescription() != null ? report.getDescription() : "N/A");
            if (tvStatus != null) tvStatus.setText(report.getStatus() != null ? report.getStatus() : "Pending");
            
            // Format date and time
            if (tvDateTime != null) {
                long timestamp = report.getTimestamp();
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                String formattedDateTime = dateFormat.format(new java.util.Date(timestamp));
                tvDateTime.setText(formattedDateTime);
            }
            
            // Show attachment count (include both images and videos)
            int imageCount = report.getImageUrls() != null ? report.getImageUrls().size() : 0;
            int videoCount = report.getVideoUrls() != null ? report.getVideoUrls().size() : 0;
            int attachmentCount = imageCount + videoCount;
            if (tvAttachmentCount != null) {
                tvAttachmentCount.setText(attachmentCount + " attachment(s)");
            }
            
            // Setup view attachments button
            if (btnViewAttachments != null) {
                if (attachmentCount > 0) {
                    btnViewAttachments.setVisibility(View.VISIBLE);
                    btnViewAttachments.setOnClickListener(v -> {
                        showReportAttachmentsDialog(report);
                    });
                } else {
                    btnViewAttachments.setVisibility(View.GONE);
                }
            }
            
            AlertDialog dialog = builder.setView(dialogView).create();
            
            // Setup close button
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing report details dialog", e);
            Toast.makeText(this, "Error showing report details", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showReportAttachmentsDialog(Report report) {
        try {
            // Check for both images and videos
            boolean hasImages = report.getImageUrls() != null && !report.getImageUrls().isEmpty();
            boolean hasVideos = report.getVideoUrls() != null && !report.getVideoUrls().isEmpty();
            
            if (!hasImages && !hasVideos) {
                Toast.makeText(this, "No attachments available for this report", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "Showing attachments dialog for report: " + report.getReportId());
            if (hasImages) {
                Log.d(TAG, "Image URLs: " + report.getImageUrls().toString());
            }
            if (hasVideos) {
                Log.d(TAG, "Video URLs: " + report.getVideoUrls().toString());
            }
            
            // Convert String URLs to MediaItem list (supports both images and videos)
            List<MediaItem> mediaItems = new ArrayList<>();
            
            // Add images
            if (hasImages) {
                for (String urlString : report.getImageUrls()) {
                    if (urlString != null && !urlString.trim().isEmpty()) {
                        try {
                            Uri uri = Uri.parse(urlString);
                            mediaItems.add(new MediaItem(uri, MediaItem.TYPE_IMAGE));
                            Log.d(TAG, "Added image URI: " + uri.toString());
                        } catch (Exception e) {
                            Log.e(TAG, "Invalid image URL: " + urlString, e);
                        }
                    }
                }
            }
            
            // Add videos
            if (hasVideos) {
                for (String urlString : report.getVideoUrls()) {
                    if (urlString != null && !urlString.trim().isEmpty()) {
                        try {
                            Uri uri = Uri.parse(urlString);
                            mediaItems.add(new MediaItem(uri, MediaItem.TYPE_VIDEO));
                            Log.d(TAG, "Added video URI: " + uri.toString());
                        } catch (Exception e) {
                            Log.e(TAG, "Invalid video URL: " + urlString, e);
                        }
                    }
                }
            }
            
            if (mediaItems.isEmpty()) {
                Toast.makeText(this, "No valid attachments found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
            
            // Set title and attachment count
            TextView tvAttachmentsTitle = dialogView.findViewById(R.id.tvAttachmentsTitle);
            TextView tvAttachmentCount = dialogView.findViewById(R.id.tvAttachmentCount);
            if (tvAttachmentsTitle != null) {
                tvAttachmentsTitle.setText("Report Attachments");
            }
            if (tvAttachmentCount != null) {
                int count = mediaItems.size();
                tvAttachmentCount.setText(count + (count == 1 ? " attachment" : " attachments"));
            }
            
            // Setup horizontal RecyclerView in dialog
            RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialogImagesRecyclerView);
            if (dialogRecyclerView != null) {
                LinearLayoutManager dialogLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                dialogRecyclerView.setLayoutManager(dialogLayoutManager);
                
                // Use MediaGalleryAdapter to support both images and videos
                MediaGalleryAdapter dialogAdapter = new MediaGalleryAdapter(this, mediaItems);
                dialogAdapter.setOnMediaRemoveListener(null); // Disable remove in dialog
                dialogAdapter.setOnMediaClickListener(new MediaGalleryAdapter.OnMediaClickListener() {
                    @Override
                    public void onMediaClick(int position, MediaItem mediaItem) {
                        if (mediaItem.isImage()) {
                            // Show full screen image view
                            showFullScreenImage(mediaItem.getUri());
                        } else if (mediaItem.isVideo()) {
                            // Show video player
                            showVideoInDialog(mediaItem.getUri());
                        }
                    }
                });
                
                dialogRecyclerView.setAdapter(dialogAdapter);
                Log.d(TAG, "Dialog RecyclerView setup complete with " + mediaItems.size() + " media items (images + videos)");
            } else {
                Log.e(TAG, "Dialog RecyclerView not found in layout");
            }
            
            // Fallback vertical layout (hidden)
            LinearLayout imagesContainer = dialogView.findViewById(R.id.imagesContainer);
            if (imagesContainer != null) {
                imagesContainer.setVisibility(View.GONE);
            }
            
            AlertDialog dialog = builder.setView(dialogView)
                    .create();
            
            // Set rounded corners for dialog window
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            dialog.show();
                    
            Log.d(TAG, "Attachments dialog shown successfully");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error showing attachments dialog", e);
            Toast.makeText(this, "Error showing attachments: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showImageAttachmentsDialog(Report report) {
        // Legacy method - redirect to new method
        showReportAttachmentsDialog(report);
    }
    
    private void loadSampleReportsForDemo() {
        try {
            Log.d(TAG, "Loading sample reports for demonstration with different statuses");
            
            // Create sample reports with proper timestamps and different statuses
            java.util.Date now = new java.util.Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            
            // Report 1 - 2 hours ago - PENDING
            cal.setTime(now);
            cal.add(java.util.Calendar.HOUR, -2);
            long timestamp1 = cal.getTimeInMillis();
            Report report1 = new Report();
            report1.setReportId("demo_001");
            report1.setUserId("demo_user1");
            report1.setTitle("Road Crash");
            report1.setCategory("Road Crash");
            report1.setDescription("Vehicle collision on Main Street near the intersection");
            report1.setLocationName("Brgy. Tinamnan");
            report1.setCoordinates("14.123456, 121.567890");
            report1.setReporterName("Juan Dela Cruz");
            report1.setReporterMobile("09123456789");
            report1.setStatus("Pending");
            report1.setTimestamp(timestamp1);
            report1.setImageCount(2);
            
            List<String> sampleImageUrls1 = new ArrayList<>();
            sampleImageUrls1.add("https://picsum.photos/400/300?random=1");
            sampleImageUrls1.add("https://picsum.photos/400/300?random=2");
            report1.setImageUrls(sampleImageUrls1);
            allReports.add(report1);
            
            // Report 2 - 4 hours ago - ONGOING
            cal.setTime(now);
            cal.add(java.util.Calendar.HOUR, -4);
            long timestamp2 = cal.getTimeInMillis();
            Report report2 = new Report();
            report2.setReportId("demo_002");
            report2.setUserId("demo_user2");
            report2.setTitle("Flooding");
            report2.setCategory("Flooding");
            report2.setDescription("Heavy flooding in residential area due to continuous rain");
            report2.setLocationName("Brgy. Tinamnan");
            report2.setCoordinates("14.123789, 121.568123");
            report2.setReporterName("Maria Santos");
            report2.setReporterMobile("09234567890");
            report2.setStatus("Ongoing");
            report2.setTimestamp(timestamp2);
            report2.setImageCount(1);
            
            List<String> sampleImageUrls2 = new ArrayList<>();
            sampleImageUrls2.add("https://picsum.photos/400/300?random=3");
            report2.setImageUrls(sampleImageUrls2);
            allReports.add(report2);
            
            // Report 3 - 1 day ago - RESPONDED
            cal.setTime(now);
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
            long timestamp3 = cal.getTimeInMillis();
            Report report3 = new Report();
            report3.setReportId("demo_003");
            report3.setUserId("demo_user3");
            report3.setTitle("Medical Emergency");
            report3.setCategory("Medical Emergency");
            report3.setDescription("Person collapsed on street, ambulance called");
            report3.setLocationName("Brgy. Tinamnan");
            report3.setCoordinates("14.124000, 121.569000");
            report3.setReporterName("Pedro Garcia");
            report3.setReporterMobile("09345678901");
            report3.setStatus("Responded");
            report3.setTimestamp(timestamp3);
            report3.setImageCount(0);
            report3.setImageUrls(new ArrayList<>());
            allReports.add(report3);
            
            // Report 4 - 2 days ago - NOT RESPONDED
            cal.setTime(now);
            cal.add(java.util.Calendar.DAY_OF_MONTH, -2);
            long timestamp4 = cal.getTimeInMillis();
            Report report4 = new Report();
            report4.setReportId("demo_004");
            report4.setUserId("demo_user4");
            report4.setTitle("Landslide");
            report4.setCategory("Landslide");
            report4.setDescription("Mudslide blocking main road, no response yet");
            report4.setLocationName("Brgy. Tinamnan");
            report4.setCoordinates("14.125000, 121.570000");
            report4.setReporterName("Ana Rodriguez");
            report4.setReporterMobile("09456789012");
            report4.setStatus("Not Responded");
            report4.setTimestamp(timestamp4);
            report4.setImageCount(1);
            
            List<String> sampleImageUrls4 = new ArrayList<>();
            sampleImageUrls4.add("https://picsum.photos/400/300?random=4");
            report4.setImageUrls(sampleImageUrls4);
            allReports.add(report4);
            
            // Report 5 - 3 days ago - REDUNDANT
            cal.setTime(now);
            cal.add(java.util.Calendar.DAY_OF_MONTH, -3);
            long timestamp5 = cal.getTimeInMillis();
            Report report5 = new Report();
            report5.setReportId("demo_005");
            report5.setUserId("demo_user5");
            report5.setTitle("Road Crash");
            report5.setCategory("Road Crash");
            report5.setDescription("Duplicate report of the same incident");
            report5.setLocationName("Brgy. Tinamnan");
            report5.setCoordinates("14.126000, 121.571000");
            report5.setReporterName("Carlos Lopez");
            report5.setReporterMobile("09567890123");
            report5.setStatus("Redundant");
            report5.setTimestamp(timestamp5);
            report5.setImageCount(0);
            report5.setImageUrls(new ArrayList<>());
            allReports.add(report5);
            
            // Sort by timestamp (newest first)
            allReports.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
            
            // Update filtered reports
            filteredReports.clear();
            filteredReports.addAll(allReports);
            reportLogAdapter.notifyDataSetChanged();
            
            // Update status summary
            updateStatusSummary();
            
            Log.d(TAG, "Loaded " + allReports.size() + " sample reports for demonstration with different statuses");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading sample reports", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, but check if location access is enabled in settings
                if (!LocationPermissionHelper.isLocationAccessEnabledWithLog(this, "ReportSubmissionActivity")) {
                    Toast.makeText(this, "Location access is disabled in settings. Please enable it in Profile settings.", Toast.LENGTH_LONG).show();
                    return;
                }
                // Permission granted and location access enabled, try to get location again
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to get your current location", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, check if video recording or photo
                if (pendingVideoRecording) {
                    openVideoRecorder();
                    pendingVideoRecording = false; // Reset flag
                } else {
                    openCamera();
                }
            } else {
                // Permission denied
                String message = pendingVideoRecording ? 
                    "Camera permission is required to record videos" : 
                    "Camera permission is required to take photos";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                pendingVideoRecording = false; // Reset flag
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            // Storage permission result - handled by PermissionHelper callbacks
            // Check if any permission was granted (supports "allow while in use" on Android 14+)
            boolean permissionGranted = false;
            if (grantResults.length > 0) {
                // On Android 14+, check if full permissions OR "allow while in use" permission was granted
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Check if full permissions granted
                    boolean hasFullPermission = grantResults.length >= 2 && 
                        grantResults[0] == PackageManager.PERMISSION_GRANTED && 
                        grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    
                    // Check if "allow while in use" permission granted (READ_MEDIA_VISUAL_USER_SELECTED)
                    boolean hasPartialPermission = grantResults.length >= 3 && 
                        grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    
                    permissionGranted = hasFullPermission || hasPartialPermission;
                } else {
                    // Android 13 and below - check first permission
                    permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                }
            }
            
            if (permissionGranted) {
                // Permission granted (full or "allow while in use") - user can now select media
                Log.d(TAG, "Storage permission granted (full or allow while in use)");
            } else {
                Toast.makeText(this, "Storage permission is required to select photos and videos", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void loadUserProfilePicture() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && profileButton != null) {
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
                                ProfilePictureCache.getInstance().loadProfilePicture(profileButton, profilePictureUrl);
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
                ProfilePictureCache.getInstance().loadProfilePicture(profileButton, uri.toString());
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
                    if (bitmap != null && profileButton != null) {
                        // Create circular bitmap
                        android.graphics.Bitmap circularBitmap = createCircularProfileBitmap(bitmap);
                        profileButton.setImageBitmap(circularBitmap);
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

            int targetSize = 150; // Size for profile button
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
            if (profileButton != null) {
                profileButton.setImageResource(R.drawable.ic_person);
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
            // Refresh user profile information
            loadUserProfileInformation();
            // Update notification badge (for alerts)
            updateNotificationBadge();
            // ✅ NEW: Update chat badge
            updateChatBadge();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadgeReport == null) return;
            
            // Use the same logic as AlertsActivity - check viewed state
            int lastViewedCount = sharedPreferences.getInt(KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT, 0);
            
            // Fetch current announcement count and calculate unread count
            fetchAndCountNewAnnouncementsFromReport(lastViewedCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge on report: " + e.getMessage(), e);
            if (alertsBadgeReport != null) {
                alertsBadgeReport.setVisibility(View.GONE);
            }
        }
    }
    
    // This method is no longer needed - using fetchAndCountNewAnnouncementsFromReport directly
    // Keeping for backward compatibility but it's not used anymore
    
    private void fetchAndCountNewAnnouncementsFromReport(int lastViewedCount) {
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
                            if (alertsBadgeReport != null) {
                                if (finalUnreadCount > 0) {
                                    alertsBadgeReport.setText(String.valueOf(finalUnreadCount));
                                    alertsBadgeReport.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "✅ Report badge showing: " + finalUnreadCount + " unread announcements");
                                } else {
                                    alertsBadgeReport.setVisibility(View.GONE);
                                    alertsBadgeReport.setText("0");
                                    Log.d(TAG, "✅ Report badge hidden - all announcements viewed (lastViewed: " + 
                                          lastViewedCount + ", current: " + currentTotalCount + ")");
                                }
                            }
                        });
                        
                        Log.d(TAG, "Report badge update - unreadCount: " + unreadCount + 
                                  " (current: " + currentTotalCount + ", lastViewed: " + lastViewedCount + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching announcements for badge from report: " + e.getMessage(), e);
                    if (alertsBadgeReport != null) {
                        runOnUiThread(() -> {
                            alertsBadgeReport.setVisibility(View.GONE);
                        });
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching and counting new announcements from report: " + e.getMessage(), e);
        }
    }
    
    private boolean isAnnouncementNewFromReport(String dateStr, long lastVisitTime) {
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
            Log.e(TAG, "Error checking if announcement is new from report: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * ✅ NEW: Setup real-time listener for chat badge updates
     * This will automatically update the badge when new messages arrive
     */
    private void setupChatBadgeListener() {
        try {
            if (chatBadgeReport == null) {
                Log.w(TAG, "Chat badge view is null, cannot setup listener");
                return;
            }
            
            // Remove existing listener if any
            if (chatBadgeListener != null) {
                chatBadgeListener.remove();
                chatBadgeListener = null;
            }
            
            // Setup real-time listener using ChatBadgeManager
            chatBadgeListener = ChatBadgeManager.getInstance().setupRealtimeBadgeListener(
                this, chatBadgeReport);
            
            if (chatBadgeListener != null) {
                Log.d(TAG, "✅ Real-time chat badge listener setup successfully in ReportSubmissionActivity");
            } else {
                Log.w(TAG, "⚠️ Failed to setup real-time chat badge listener in ReportSubmissionActivity");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up chat badge listener: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update chat notification badge
     * ✅ NEW: Shows unread chat messages count on the chat tab
     */
    private void updateChatBadge() {
        try {
            if (chatBadgeReport == null) {
                Log.w(TAG, "Chat badge view is null");
                return;
            }
            
            // Use ChatBadgeManager to update the badge
            ChatBadgeManager.getInstance().updateChatBadge(this, chatBadgeReport);
            
            Log.d(TAG, "Chat badge updated successfully in ReportSubmissionActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error updating chat badge: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        try {
            // Unregister badge from notification manager
            AnnouncementNotificationManager.getInstance().unregisterBadge("ReportSubmissionActivity");
            Log.d(TAG, "ReportSubmissionActivity badge unregistered");
            
            // ✅ NEW: Remove chat badge listener
            if (chatBadgeListener != null) {
                chatBadgeListener.remove();
                chatBadgeListener = null;
                Log.d(TAG, "✅ Chat badge listener removed in ReportSubmissionActivity");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering badge in onDestroy: " + e.getMessage(), e);
        }
        
        // Stop location updates to prevent memory leaks
        stopLocationUpdates();
    }
    
    @Override
    public void onBackPressed() {
        // Handle back button press
        super.onBackPressed();
        // Optional: Navigate to specific activity instead of default back behavior
        // navigateToHome();
    }
}