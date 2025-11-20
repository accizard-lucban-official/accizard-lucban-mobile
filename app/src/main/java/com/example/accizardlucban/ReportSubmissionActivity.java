package com.example.accizardlucban;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    private Button getCurrentLocationButton;
    private Button uploadImagesButton;
    private Button takePhotoButton;
    private Button submitReportButton;
    private ImageButton profileButton;
    private ImageView locationInfoIcon;
    private ImageView reportTypeInfoIcon;
    private RecyclerView reportLogRecyclerView;
    private ChipGroup reportLogFilterChips;
    private RecyclerView imageGalleryRecyclerView;
    private Button addMoreImagesButton;
    
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
    
    // Status count TextViews
    private TextView pendingCountText;
    private TextView ongoingCountText;
    private TextView respondedCountText;
    private TextView unrespondedCountText;
    private TextView redundantCountText;
    private TextView totalCountText;
    
    private static final int IMAGE_PICK_REQUEST = 2001;
    private static final int CAMERA_REQUEST_CODE = 2002;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2003;
    private Uri selectedImageUri;
    private List<Uri> selectedImageUris = new ArrayList<>();

    // Bottom Navigation
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private LinearLayout reportTab;
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
    
    // Image Gallery Data
    private ProfessionalImageGalleryAdapter imageGalleryAdapter;

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

        // Setup filter chips
        setupReportLogFilterChips();
        
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
        getCurrentLocationButton = findViewById(R.id.getCurrentLocationButton);
        uploadImagesButton = findViewById(R.id.uploadImagesButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        submitReportButton = findViewById(R.id.submitReportButton);
        profileButton = findViewById(R.id.profile);
        locationInfoIcon = findViewById(R.id.locationInfoIcon);
        reportTypeInfoIcon = findViewById(R.id.reportTypeInfoIcon);
        reportLogRecyclerView = findViewById(R.id.reportLogRecyclerView);
        reportLogFilterChips = findViewById(R.id.reportLogFilterChips);
        imageGalleryRecyclerView = findViewById(R.id.imageGalleryRecyclerView);
        addMoreImagesButton = findViewById(R.id.addMoreImagesButton);
        
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
    }

    private void setupReportTypeSpinner() {
        // Create array of report types
        String[] reportTypes = {
                "Select Report Type",
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

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reportTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);
    }

    private void setupReportLogRecyclerView() {
        // Setup RecyclerView with LinearLayoutManager
        reportLogRecyclerView.setLayoutManager(new NonScrollableLinearLayoutManager(this));
        reportLogRecyclerView.setNestedScrollingEnabled(false);

        // Initialize adapter
        allReports = new ArrayList<>();
        filteredReports = new ArrayList<>();
        reportLogAdapter = new ReportLogAdapter(this, filteredReports);
        
        // Set click listeners
        reportLogAdapter.setOnReportClickListener(new ReportLogAdapter.OnReportClickListener() {
            @Override
            public void onReportClick(Report report) {
                // Show full report details dialog
                showReportDetailsDialog(report);
            }

            @Override
            public void onViewAttachmentsClick(Report report) {
                // Show attachments dialog
                if (report.getImageUrls() != null && !report.getImageUrls().isEmpty()) {
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
        // Setup 4-column grid layout with images matching container height
        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager = 
            new androidx.recyclerview.widget.GridLayoutManager(this, 4);
        imageGalleryRecyclerView.setLayoutManager(gridLayoutManager);
        
        // Initialize professional adapter
        imageGalleryAdapter = new ProfessionalImageGalleryAdapter(this, selectedImageUris);
        
        // Set click listeners
        imageGalleryAdapter.setOnImageClickListener(new ProfessionalImageGalleryAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position, Uri imageUri) {
                showImageInDialog(imageUri);
            }
        });
        
        imageGalleryAdapter.setOnImageRemoveListener(new ProfessionalImageGalleryAdapter.OnImageRemoveListener() {
            @Override
            public void onImageRemove(int position, Uri imageUri) {
                removeImageFromGallery(position);
            }
        });
        
        imageGalleryRecyclerView.setAdapter(imageGalleryAdapter);
        
        // Setup add more images button
        addMoreImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
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

    private void setupReportLogFilterChips() {
        if (reportLogFilterChips == null) return;

        reportLogFilterChips.setSingleSelection(true);
        reportLogFilterChips.setSelectionRequired(true);
        reportLogFilterChips.removeAllViews();

        String[][] filterOptions = {
                {"All Types", "All Reports"},
                {"Pending", "Pending"},
                {"Ongoing", "Ongoing"},
                {"Responded", "Responded"},
                {"Unresponded", "Not Responded"},
                {"Redundant", "Redundant"},
                {"Road Crash", "Road Crash"},
                {"Medical Emergency", "Medical Emergency"},
                {"Volcanic Activity", "Volcanic Activity"},
                {"Earthquake", "Earthquake"},
                {"Armed Conflict", "Armed Conflict"},
                {"Fire", "Fire"},
                {"Flooding", "Flooding"},
                {"Landslide", "Landslide"},
                {"Civil Disturbance", "Civil Disturbance"},
                {"Infectious Disease", "Infectious Disease"},
                {"Poor Infrastructure", "Poor Infrastructure"},
                {"Obstructions", "Obstructions"},
                {"Electrical Hazard", "Electrical Hazard"},
                {"Environmental Hazard", "Environmental Hazard"},
                {"Animal Concern", "Animal Concern"},
                {"Others", "Others"}
        };

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < filterOptions.length; i++) {
            String displayText = filterOptions[i][0];
            String filterValue = filterOptions[i][1];

            Chip chip = (Chip) inflater.inflate(R.layout.item_report_filter_chip, reportLogFilterChips, false);
            chip.setText(displayText);
            chip.setTag(filterValue);

            if (i == 0) {
                chip.setChecked(true);
            }

            reportLogFilterChips.addView(chip);
        }

        // Ensure initial filter applied
        filterReports((String) reportLogFilterChips.getChildAt(0).getTag());

        reportLogFilterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    filterReports((String) selectedChip.getTag());
                }
            }
        });
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
                Toast.makeText(ReportSubmissionActivity.this, 
                    "We only respond to reports within Lucban. Please select a location within Lucban.",
                    Toast.LENGTH_LONG).show();
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

        // Pinning button click
        pinningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // Get Current Location button click
        getCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Get Current Location button clicked");
                getCurrentLocation();
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
        
        // Upload images button click
        uploadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
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
                // Already on report screen - show toast or refresh
                Toast.makeText(ReportSubmissionActivity.this, "You're already on the Report screen", Toast.LENGTH_SHORT).show();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
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
    
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), IMAGE_PICK_REQUEST);
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
                    // Update coordinates field with location name (not coordinates)
                    coordinatesEditText.setText(selectedLocationName);
                    
                    // Update legacy field for backward compatibility
                    String displayText = selectedLocationName + " (" + coordinatesText + ")";
                    locationEditText.setText(displayText);
                    
                    // Make coordinates EditText read-only and add click listener
                    makeCoordinatesEditTextReadOnly();
                    
                    Toast.makeText(this, "Location pinned: " + selectedLocationName, Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "✅ Pinned location received:");
                    Log.d(TAG, "   Location Name: " + selectedLocationName);
                    Log.d(TAG, "   Latitude: " + latitude);
                    Log.d(TAG, "   Longitude: " + longitude);
                    Log.d(TAG, "   Coordinates: " + coordinatesText);
                } else {
                    // Location name not provided, show placeholder and get it via geocoding
                    coordinatesEditText.setText("Getting location name...");
                    coordinatesEditText.setEnabled(false);
                    
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
            
            // Update professional image gallery
            updateProfessionalImageGallery();
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
        if (selectedImageUris.isEmpty()) {
            // Show upload buttons container with animation, hide gallery
            if (uploadImagesButton != null && takePhotoButton != null) {
                LinearLayout mediaContainer = findViewById(R.id.mediaAttachmentsContainer);
                if (mediaContainer != null) {
                    mediaContainer.setVisibility(View.VISIBLE);
                }
            }
            
            imageGalleryRecyclerView.setVisibility(View.GONE);
            addMoreImagesButton.setVisibility(View.GONE);
        } else {
            // Hide upload buttons container, show gallery with animation
            LinearLayout mediaContainer = findViewById(R.id.mediaAttachmentsContainer);
            if (mediaContainer != null) {
                mediaContainer.setVisibility(View.GONE);
            }
            
            imageGalleryRecyclerView.setVisibility(View.VISIBLE);
            imageGalleryRecyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_scale));
            
            addMoreImagesButton.setVisibility(View.VISIBLE);
            addMoreImagesButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_scale));
            
            imageGalleryAdapter.updateImages(selectedImageUris);
        }
    }
    
    
    private void removeImageFromGallery(int position) {
        if (position >= 0 && position < selectedImageUris.size()) {
            selectedImageUris.remove(position);
            imageGalleryAdapter.removeImage(position);
            
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
            coordinatesEditText.setText("Getting location name...");
            coordinatesEditText.setEnabled(false);
            
            // Get location name using reverse geocoding (will update the display when complete)
            getLocationNameFromCoordinates(selectedLatitude, selectedLongitude);
            
            // Update legacy field for backward compatibility
            String displayText = "Current Location (" + coordinatesText + ")";
            locationEditText.setText(displayText);
            
            // Make coordinates EditText clickable to view/change on map
            makeCoordinatesEditTextReadOnly();
            
            // Show success message
            Toast.makeText(this, "✅ Current location obtained! Click pin button to view on map.", Toast.LENGTH_LONG).show();
            
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
     */
    private void getLocationNameFromCoordinates(double latitude, double longitude) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                
                // Try to get the most specific location name
                String locationName = null;
                
                // Priority 1: Sublocality (Barangay in Philippines)
                if (address.getSubLocality() != null) {
                    locationName = address.getSubLocality();
                }
                // Priority 2: Locality (City/Town)
                else if (address.getLocality() != null) {
                    locationName = address.getLocality();
                }
                // Priority 3: SubAdminArea
                else if (address.getSubAdminArea() != null) {
                    locationName = address.getSubAdminArea();
                }
                // Priority 4: AdminArea (Province)
                else if (address.getAdminArea() != null) {
                    locationName = address.getAdminArea();
                }
                // Fallback
                else {
                    locationName = "Current Location";
                }
                
                selectedLocationName = locationName;
                Log.d(TAG, "✅ Location name from geocoding: " + selectedLocationName);
                
                // Update UI with the location name in the EditText
                runOnUiThread(() -> {
                    // Update coordinates field with location name (not coordinates)
                    if (coordinatesEditText != null) {
                        coordinatesEditText.setText(selectedLocationName);
                    }
                    Toast.makeText(this, "Location: " + selectedLocationName, Toast.LENGTH_SHORT).show();
                });
                
            } else {
                selectedLocationName = "Current Location";
                Log.w(TAG, "⚠️ Geocoder returned no addresses, using default name");
                
                // Update UI with default name
                runOnUiThread(() -> {
                    if (coordinatesEditText != null) {
                        coordinatesEditText.setText(selectedLocationName);
                    }
                });
            }
        } catch (Exception e) {
            selectedLocationName = "Current Location";
            Log.e(TAG, "Error getting location name from coordinates: " + e.getMessage(), e);
            
            // Update UI with default name on error
            runOnUiThread(() -> {
                if (coordinatesEditText != null) {
                    coordinatesEditText.setText(selectedLocationName);
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
            TextView tvConfirmAttachments = dialogView.findViewById(R.id.tvConfirmAttachments);
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
            
            // Set attachments
            if (tvConfirmAttachments != null && rvConfirmImagePreviews != null) {
                int attachmentCount = selectedImageUris.size();
                if (attachmentCount > 0) {
                    tvConfirmAttachments.setText(attachmentCount + " image(s) attached");
                    
                    // Show image previews in horizontal RecyclerView
                    rvConfirmImagePreviews.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                    rvConfirmImagePreviews.setLayoutManager(layoutManager);
                    
                    // Use the same adapter for preview
                    ProfessionalImageGalleryAdapter previewAdapter = new ProfessionalImageGalleryAdapter(this, selectedImageUris);
                    previewAdapter.setOnImageRemoveListener(null); // Disable remove in preview
                    previewAdapter.setOnImageClickListener(new ProfessionalImageGalleryAdapter.OnImageClickListener() {
                        @Override
                        public void onImageClick(int position, Uri imageUri) {
                            // Show full screen image when clicked
                            showFullScreenImage(imageUri);
                        }
                    });
                    rvConfirmImagePreviews.setAdapter(previewAdapter);
                } else {
                    tvConfirmAttachments.setText("No attachments");
                    rvConfirmImagePreviews.setVisibility(View.GONE);
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
        String reportType = reportTypeSpinner.getSelectedItem().toString();
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

        // Upload images first if any, then submit report
        if (!selectedImageUris.isEmpty()) {
            uploadReportImagesAndSubmit(reportData);
        } else {
            // Submit report without images
            submitReportToFirestore(reportData);
        }
    }

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
        FirestoreHelper.createReportWithAutoId(reportData,
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Report submitted successfully with ID: " + documentReference.getId());
                        
                        // If report has images, reorganize them with the actual report ID
                        if (reportData.containsKey("imageUrls")) {
                            reorganizeImagesWithReportId(documentReference.getId(), 
                                    (List<String>) reportData.get("imageUrls"));
                        }
                        
                        Toast.makeText(ReportSubmissionActivity.this, 
                            "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Reload reports from Firestore to show the new report in Report Log
                        loadUserReportsFromFirestore();
                        
                        // Switch to Report Log tab to show the new report
                        switchToReportLogTab();
                        
                        clearForm();
                        submitReportButton.setEnabled(true);
                        submitReportButton.setText("Submit Report");
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error submitting report", e);
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
            
            // ✅ CRITICAL: Get user's barangay from profile for report matching
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
        reportData.put("imageCount", selectedImageUris.size());
        reportData.put("imageUrls", new ArrayList<>()); // Will be updated after upload
        
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
        
        // Check if description is provided
        String description = descriptionEditText.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            descriptionEditText.requestFocus();
            return false;
        }

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
        reportTypeSpinner.setSelection(0);
        descriptionEditText.setText("");
        coordinatesEditText.setText("");
        locationEditText.setText(""); // Legacy field
        descriptionEditText.clearFocus();
        coordinatesEditText.clearFocus();
        
        // Clear images
        selectedImageUris.clear();
        updateProfessionalImageGallery();
        
        // Reset location selection state
        resetLocationSelection();
        
        Log.d(TAG, "Form cleared successfully");
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
        if (selectedImageUris.isEmpty()) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
        
        // Setup horizontal RecyclerView in dialog
        RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialogImagesRecyclerView);
        LinearLayoutManager dialogLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dialogRecyclerView.setLayoutManager(dialogLayoutManager);
        
        // Create adapter for dialog
        ProfessionalImageGalleryAdapter dialogAdapter = new ProfessionalImageGalleryAdapter(this, selectedImageUris);
        dialogAdapter.setOnImageClickListener(new ProfessionalImageGalleryAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position, Uri clickedImageUri) {
                // Show full screen image view
                showFullScreenImage(clickedImageUri);
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
            
            builder.setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
                    
            Log.d(TAG, "Full screen image dialog shown");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error showing full screen image", e);
            Toast.makeText(this, "Error loading full screen image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            db.collection("reports")
                    .whereEqualTo("userId", currentUser.getUid())
                    .addSnapshotListener((queryDocumentSnapshots, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Error loading reports from Firestore", error);
                            Toast.makeText(this, "Error loading reports: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (queryDocumentSnapshots != null) {
                            allReports.clear();
                            
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                try {
                                    Report report = convertDocumentToReport(doc);
                                    if (report != null) {
                                        allReports.add(report);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing report document", e);
                                }
                            }
                            
                            // Sort reports by timestamp in memory (newest first)
                            allReports.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                            
                            Log.d(TAG, "Loaded " + allReports.size() + " reports from Firestore for current user");
                            
                            // Update filtered reports
                            filteredReports.clear();
                            filteredReports.addAll(allReports);
                            reportLogAdapter.notifyDataSetChanged();
                            
                            // Update status summary
                            updateStatusSummary();
                            
                            // Show message if no reports found
                            if (allReports.isEmpty()) {
                                Log.d(TAG, "No reports found for current user - Report Log will be empty");
                            }
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
            
            // Handle image URLs
            Object imageUrlsObj = doc.get("imageUrls");
            if (imageUrlsObj instanceof List) {
                report.setImageUrls((List<String>) imageUrlsObj);
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

    private void filterReports(String filter) {
        filteredReports.clear();
        
        if (filter.equals("All Reports")) {
            filteredReports.addAll(allReports);
        } else {
            for (Report report : allReports) {
                // Check status matches
                if (filter.equals(report.getStatus())) {
                    filteredReports.add(report);
                }
                // Check category matches (for report type filters)
                else if (filter.equals(report.getCategory())) {
                    filteredReports.add(report);
                }
            }
        }
        
        reportLogAdapter.notifyDataSetChanged();
        Log.d(TAG, "Filtered reports: " + filteredReports.size() + " reports match filter: " + filter);
    }
    
    private void updateStatusSummary() {
        try {
            // Count reports by status
            int pendingCount = 0;
            int ongoingCount = 0;
            int respondedCount = 0;
            int notRespondedCount = 0;
            int redundantCount = 0;
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
                    }
                }
            }
            
            Log.d(TAG, "Status Summary - Total: " + totalCount + 
                      ", Pending: " + pendingCount + 
                      ", Ongoing: " + ongoingCount + 
                      ", Responded: " + respondedCount + 
                      ", Not Responded: " + notRespondedCount + 
                      ", Redundant: " + redundantCount);
            
            // Update the status count TextViews in the Report Log UI
            updateStatusCountTextViews(pendingCount, ongoingCount, respondedCount, notRespondedCount, redundantCount, totalCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating status summary", e);
        }
    }
    
    private void updateStatusCountTextViews(int pending, int ongoing, int responded, int notResponded, int redundant, int total) {
        try {
            // Update each status count TextView with real-time counts
            if (pendingCountText != null) {
                pendingCountText.setText(String.valueOf(pending));
                Log.d(TAG, "Updated Pending count: " + pending);
            }
            
            if (ongoingCountText != null) {
                ongoingCountText.setText(String.valueOf(ongoing));
                Log.d(TAG, "Updated Ongoing count: " + ongoing);
            }
            
            if (respondedCountText != null) {
                respondedCountText.setText(String.valueOf(responded));
                Log.d(TAG, "Updated Responded count: " + responded);
            }
            
            if (unrespondedCountText != null) {
                unrespondedCountText.setText(String.valueOf(notResponded));
                Log.d(TAG, "Updated Not Responded count: " + notResponded);
            }
            
            if (redundantCountText != null) {
                redundantCountText.setText(String.valueOf(redundant));
                Log.d(TAG, "Updated Redundant count: " + redundant);
            }
            
            if (totalCountText != null) {
                totalCountText.setText(String.valueOf(total));
                Log.d(TAG, "Updated Total count: " + total);
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
            
            // Show attachment count
            int attachmentCount = report.getImageCount();
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
            if (report.getImageUrls() == null || report.getImageUrls().isEmpty()) {
                Toast.makeText(this, "No attachments available for this report", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "Showing attachments dialog for report: " + report.getReportId());
            Log.d(TAG, "Image URLs: " + report.getImageUrls().toString());
            
            // Convert String URLs to Uri list
            List<Uri> imageUris = new ArrayList<>();
            for (String urlString : report.getImageUrls()) {
                if (urlString != null && !urlString.trim().isEmpty()) {
                    try {
                        Uri uri = Uri.parse(urlString);
                        imageUris.add(uri);
                        Log.d(TAG, "Added image URI: " + uri.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Invalid image URL: " + urlString, e);
                    }
                }
            }
            
            if (imageUris.isEmpty()) {
                Toast.makeText(this, "No valid image attachments found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
            
            // Setup horizontal RecyclerView in dialog
            RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialogImagesRecyclerView);
            if (dialogRecyclerView != null) {
                LinearLayoutManager dialogLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                dialogRecyclerView.setLayoutManager(dialogLayoutManager);
                
                // Create adapter for dialog
                ProfessionalImageGalleryAdapter dialogAdapter = new ProfessionalImageGalleryAdapter(this, imageUris);
                dialogAdapter.setOnImageClickListener(new ProfessionalImageGalleryAdapter.OnImageClickListener() {
                    @Override
                    public void onImageClick(int position, Uri clickedImageUri) {
                        Log.d(TAG, "Image clicked: " + clickedImageUri.toString());
                        // Show full screen image view
                        showFullScreenImage(clickedImageUri);
                    }
                });
                
                dialogRecyclerView.setAdapter(dialogAdapter);
                Log.d(TAG, "Dialog RecyclerView setup complete with " + imageUris.size() + " images");
            } else {
                Log.e(TAG, "Dialog RecyclerView not found in layout");
            }
            
            // Fallback vertical layout (hidden)
            LinearLayout imagesContainer = dialogView.findViewById(R.id.imagesContainer);
            if (imagesContainer != null) {
                imagesContainer.setVisibility(View.GONE);
            }
            
            builder.setView(dialogView)
                    .setTitle("Report Attachments (" + imageUris.size() + ")")
                    .setPositiveButton("Close", null)
                    .show();
                    
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
                // Permission granted, try to get location again
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to get your current location", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, open camera
                openCamera();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
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
     * Update chat notification badge
     * ✅ NEW: Shows unread chat messages count on the chat tab
     * ✅ ENHANCED: Now uses detailed debug query to find issues
     */
    private void updateChatBadge() {
        try {
            Log.d(TAG, "=== updateChatBadge() in ReportSubmissionActivity ===");
            
            if (chatBadgeReport == null) {
                Log.e(TAG, "❌ chatBadgeReport is NULL!");
                Log.e(TAG, "❌ This means findViewById(R.id.chat_badge_report) returned null");
                Log.e(TAG, "❌ Check if chat_badge_report exists in activity_report_submission.xml");
                return;
            }
            
            Log.d(TAG, "✅ chatBadgeReport view is NOT null");
            Log.d(TAG, "✅ Badge view ID: " + chatBadgeReport.getId());
            Log.d(TAG, "✅ Current badge visibility: " + 
                (chatBadgeReport.getVisibility() == View.VISIBLE ? "VISIBLE" : 
                 chatBadgeReport.getVisibility() == View.GONE ? "GONE" : "INVISIBLE"));
            
            // ✅ ENHANCED: Use flexible query that shows ALL messages and why they count or not
            Log.d(TAG, "🔍 Calling ChatBadgeManager.updateChatBadgeFlexible()...");
            Log.d(TAG, "💡 This will show EVERY message and why it's counted or not!");
            ChatBadgeManager.getInstance().updateChatBadgeFlexible(this, chatBadgeReport);
            
            Log.d(TAG, "✅ ChatBadgeManager.updateChatBadgeFlexible() call completed");
            Log.d(TAG, "📋 Check Logcat for detailed message analysis!");
            Log.d(TAG, "=== End updateChatBadge() ===");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR in updateChatBadge(): " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        try {
            // Unregister badge from notification manager
            AnnouncementNotificationManager.getInstance().unregisterBadge("ReportSubmissionActivity");
            Log.d(TAG, "ReportSubmissionActivity badge unregistered");
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