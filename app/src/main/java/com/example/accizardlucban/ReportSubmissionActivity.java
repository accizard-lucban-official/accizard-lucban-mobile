package com.example.accizardlucban;

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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import android.net.Uri;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.example.accizardlucban.StorageHelper;

public class ReportSubmissionActivity extends AppCompatActivity {

    private static final String TAG = "ReportSubmissionActivity";

    // UI Components
    private Spinner reportTypeSpinner;
    private EditText descriptionEditText;
    private EditText locationEditText;
    private ImageView locationButton;
    private ImageView pinningButton;
    private Button uploadImagesButton;
    private Button submitReportButton;
    private ImageButton profileButton;
    private RecyclerView reportLogRecyclerView;
    
    // Tab Components
    private LinearLayout submitReportTab;
    private LinearLayout reportLogTab;
    private LinearLayout submitReportContent;
    private ScrollView reportLogContent;
    private View submitReportIndicator;
    private View reportLogIndicator;
    
    private static final int IMAGE_PICK_REQUEST = 2001;
    private Uri selectedImageUri;
    private ImageView imagePreview;
    private LinearLayout attachmentsContainer;
    private List<Uri> selectedImageUris = new ArrayList<>();

    // Bottom Navigation
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private LinearLayout reportTab;
    private LinearLayout mapTab;
    private LinearLayout alertsTab;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_submission);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeViews();

        // Setup spinner
        setupReportTypeSpinner();

        // Setup RecyclerView
        setupReportLogRecyclerView();

        // Setup click listeners
        setupClickListeners();
        
        // Setup tab functionality
        setupTabFunctionality();
    }

    private void initializeViews() {
        // Form components
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        locationButton = findViewById(R.id.locationButton);
        pinningButton = findViewById(R.id.pinningButton);
        uploadImagesButton = findViewById(R.id.uploadImagesButton);
        submitReportButton = findViewById(R.id.submitReportButton);
        profileButton = findViewById(R.id.profile);
        reportLogRecyclerView = findViewById(R.id.reportLogRecyclerView);
        imagePreview = findViewById(R.id.imagePreview);
        attachmentsContainer = findViewById(R.id.attachmentsContainer);
        
        // Tab components
        submitReportTab = findViewById(R.id.submitReportTab);
        reportLogTab = findViewById(R.id.reportLogTab);
        submitReportContent = findViewById(R.id.submitReportContent);
        reportLogContent = findViewById(R.id.reportLogContent);
        submitReportIndicator = findViewById(R.id.submitReportIndicator);
        reportLogIndicator = findViewById(R.id.reportLogIndicator);

        // Bottom navigation
        homeTab = findViewById(R.id.homeTab);
        chatTab = findViewById(R.id.chatTab);
        reportTab = findViewById(R.id.reportTab);
        mapTab = findViewById(R.id.mapTab);
        alertsTab = findViewById(R.id.alertsTab);
    }

    private void setupReportTypeSpinner() {
        // Create array of report types
        String[] reportTypes = {
                "Select Report Type",
                "Road Crash",
                "Medical Emergency",
                "Flooding",
                "Volcanic Activity",
                "Landslide",
                "Earthquake",
                "Civil Disturbance",
                "Armed Conflict",
                "Infectious Disease"
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
        reportLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportLogRecyclerView.setNestedScrollingEnabled(false);

        // You can add your RecyclerView adapter here if you want to load dynamic data
        // ReportLogAdapter adapter = new ReportLogAdapter(reportList);
        // reportLogRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Profile button click
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfile();
            }
        });
        
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

        // Location button click
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        // Pinning button click
        pinningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapPicker();
            }
        });

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
                // Optional: Add transition animation
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        chatTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToChat();
                // Optional: Add transition animation
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
                // Optional: Add transition animation
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        alertsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAlerts();
                // Optional: Add transition animation
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    // Navigation methods
    private void navigateToHome() {
        Intent intent = new Intent(this, MainDashboard.class);
        startActivity(intent);
        finish(); // Optional: remove this activity from stack
    }

    private void navigateToChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void navigateToMap() {
        Intent intent = new Intent(this, MapViewActivity.class);
        startActivity(intent);
    }

    private void navigateToAlerts() {
        Intent intent = new Intent(this, AlertsActivity.class);
        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Functional methods
    // Open a map picker activity to let the user pick a location
    private static final int MAP_PICKER_REQUEST_CODE = 1001;
    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
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
            if (pickedLocation != null) {
                locationEditText.setText(pickedLocation);
            }
        }
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();
            attachmentsContainer.removeAllViews();
            
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
            
            // Add only one attachment link for all images
            if (!selectedImageUris.isEmpty()) {
                addSingleAttachmentLink();
            }
        }
    }

    private void addSingleAttachmentLink() {
        TextView link = new TextView(this);
        String text = selectedImageUris.size() == 1 ? "See Attachment" : "See Attachments (" + selectedImageUris.size() + " images)";
        link.setText(text);
        link.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        link.setTextSize(16);
        link.setPadding(0, 8, 0, 8);
        link.setBackgroundResource(android.R.drawable.list_selector_background);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageInDialog(null);
            }
        });
        attachmentsContainer.addView(link);
        Log.d(TAG, "Added single attachment link for " + selectedImageUris.size() + " images");
    }

    private void getCurrentLocation() {
        // TODO: Implement location retrieval using GPS or network
        // For now, show a toast
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();

        // Example: Set dummy location
        locationEditText.setText("Current Location: Lat 14.1234, Lng 121.5678");
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

            // Show loading state
            submitReportButton.setEnabled(false);
            submitReportButton.setText("Submitting...");

            // Get form data
            String reportType = reportTypeSpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();

            // Create report data (without category and priority)
            Map<String, Object> reportData = FirestoreHelper.createReportData(
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
    }

    private void uploadReportImagesAndSubmit(Map<String, Object> reportData) {
        // Generate a temporary report ID for organizing images
        String tempReportId = "temp_" + System.currentTimeMillis();
        
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

    private boolean validateForm() {
        // Check if report type is selected
        if (reportTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if description is provided
        String description = descriptionEditText.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            descriptionEditText.requestFocus();
            return false;
        }

        // Check if location is provided
        String location = locationEditText.getText().toString().trim();
        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            locationEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        reportTypeSpinner.setSelection(0);
        descriptionEditText.setText("");
        locationEditText.setText("");
        descriptionEditText.clearFocus();
        locationEditText.clearFocus();
    }

    private void showImageInDialog(Uri imageUri) {
        if (selectedImageUris.isEmpty()) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
        LinearLayout imagesContainer = dialogView.findViewById(R.id.imagesContainer);
        
        // Clear any existing images
        imagesContainer.removeAllViews();
        
        // Debug: Log how many images we have
        Log.d(TAG, "Showing " + selectedImageUris.size() + " images in dialog");
        
        // Add all selected images to the dialog
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            Log.d(TAG, "Adding image " + (i + 1) + ": " + uri.toString());
            
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
            imageView.setImageURI(uri);
            imageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            
            imagesContainer.addView(imageView);
        }
        
        builder.setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
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

    @Override
    public void onBackPressed() {
        // Handle back button press
        super.onBackPressed();
        // Optional: Navigate to specific activity instead of default back behavior
        // navigateToHome();
    }
}