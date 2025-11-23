package com.example.accizardlucban;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.content.SharedPreferences;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ValidIdActivity extends AppCompatActivity {

    private static final String TAG = "ValidIdActivity";
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CUSTOM_CAMERA_REQUEST_CODE = 200;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private static final int STORAGE_PERMISSION_CODE = 103;
    
    // Rate limiting constants for email verification
    private static final String PREFS_EMAIL_VERIFICATION = "email_verification_rate_limit";
    private static final String KEY_LAST_VERIFICATION_TIME = "last_verification_time";
    private static final String KEY_VERIFICATION_ATTEMPTS = "verification_attempts";
    private static final String KEY_BLOCKED_UNTIL = "blocked_until";
    private static final long MIN_TIME_BETWEEN_EMAILS = 60000; // 1 minute between emails (in milliseconds)
    private static final int MAX_ATTEMPTS_PER_HOUR = 3; // Maximum 3 attempts per hour
    private static final long HOUR_IN_MILLIS = 3600000; // 1 hour in milliseconds
    private static final long BLOCK_DURATION = 3600000; // Block for 1 hour if rate limit exceeded
    
    // Retry logic constants
    private static final int MAX_RETRY_ATTEMPTS = 3; // Maximum retry attempts
    private static final long INITIAL_RETRY_DELAY = 2000; // Initial delay: 2 seconds
    private static final long MAX_RETRY_DELAY = 30000; // Maximum delay: 30 seconds
    private static final double BACKOFF_MULTIPLIER = 2.0; // Exponential backoff multiplier

    private CardView btnUpload;
    private Button btnNext;
    private ImageButton btnBack;
    private Spinner spinnerValidIdType;
    private TextView spinnerHintOverlay; // Hint overlay TextView
    private ImageView ivValidId;
    private LinearLayout placeholderContainer;
    
    private String firstName, lastName, mobileNumber, email, password, province, cityTown, barangay, streetAddress;
    private Bitmap validIdBitmap;
    private Uri validIdUri;
    private boolean hasValidId = false;
    private String selectedValidIdType = null; // e.g., Passport, Driver's License, etc.
    private boolean hasUserSelectedIdType = false; // Track if user has explicitly selected an ID type
    private boolean isRestoringSelection = false; // Flag to prevent listener from firing during restore
    
    // Profile picture data
    private boolean hasProfilePicture = false;
    private String profileImageUriString;
    private byte[] profileBitmapData;
    private Bitmap profileBitmap;
    
    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valid_id);

        try {
            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            
            initializeViews();
            getIntentData();
            setupSpinner();
            setupClickListeners();
            restoreValidIdData(); // Restore previously saved valid ID (after spinner is set up)
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading Valid ID activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            // Initialize navigation buttons
            btnNext = findViewById(R.id.btn_next);
            btnBack = findViewById(R.id.btn_back);
            spinnerValidIdType = findViewById(R.id.spinnerValidIdType);
            spinnerHintOverlay = findViewById(R.id.spinnerHintOverlay); // Hint overlay
            
            // Ensure hint overlay is visible initially (will be shown/hidden based on selection)
            if (spinnerHintOverlay != null) {
                spinnerHintOverlay.setVisibility(View.VISIBLE);
            }
            
            // Initialize upload button
            btnUpload = findViewById(R.id.btn_upload);
            ivValidId = findViewById(R.id.iv_valid_id);
            
            // Initialize placeholder container
            placeholderContainer = findViewById(R.id.placeholder_container);

            // Enable click on image view to re-upload: open camera directly
            ivValidId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                }
            });
            
            // Enable click on placeholder to open standard phone camera (always)
            placeholderContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showReplaceImageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Replace ID Image")
                .setMessage("Do you want to replace the current ID image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    showUploadOptionsDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUploadOptionsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload ID Image")
                .setItems(new String[]{"Gallery", "Take Photo with ID Guide"}, (dialog, which) -> {
                    if (which == 0) {
                        // Gallery option
                        if (checkStoragePermission()) {
                            openGallery();
            } else {
                            requestStoragePermission();
                        }
                    } else if (which == 1) {
                        // Camera with ID guide option
                        openCustomCamera();
                    }
                })
                .setNegativeButton("Cancel", null)
                    .show();
    }
    
    private void openCustomCamera() {
        try {
            Log.d(TAG, "openCustomCamera: Starting");
            Intent cameraIntent = new Intent(this, CameraCaptureActivity.class);
            startActivityForResult(cameraIntent, CUSTOM_CAMERA_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getIntentData() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                firstName = intent.getStringExtra("firstName");
                lastName = intent.getStringExtra("lastName");
                mobileNumber = intent.getStringExtra("mobileNumber");
                email = intent.getStringExtra("email");
                password = intent.getStringExtra("password");
                province = intent.getStringExtra("province");
                cityTown = intent.getStringExtra("cityTown");
                barangay = intent.getStringExtra("barangay");
                streetAddress = intent.getStringExtra("streetAddress");
                
                // Get profile picture data
                hasProfilePicture = intent.getBooleanExtra("hasProfilePicture", false);
                profileImageUriString = intent.getStringExtra("profileImageUri");
                profileBitmapData = intent.getByteArrayExtra("profileBitmap");
                
                // Convert bitmap data back to Bitmap if available
                if (profileBitmapData != null) {
                    profileBitmap = BitmapFactory.decodeByteArray(profileBitmapData, 0, profileBitmapData.length);
                }

                // Debug: Check if data is received
                if (firstName == null || lastName == null) {
                    Toast.makeText(this, "Warning: Some user data is missing", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting intent data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            // Upload Button - directly opens gallery
            if (btnUpload != null) {
                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Directly open gallery
                        if (checkStoragePermission()) {
                            openGallery();
                        } else {
                            requestStoragePermission();
                        }
                    }
                });
            }

            if (btnNext != null) {
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // Validate valid ID type selection
                            // Check if user has explicitly selected an ID type
                            int selectedPosition = spinnerValidIdType != null ? spinnerValidIdType.getSelectedItemPosition() : -1;
                            if (spinnerValidIdType == null || selectedPosition < 0 || !hasUserSelectedIdType || selectedValidIdType == null) {
                                Toast.makeText(ValidIdActivity.this, "Please select a valid ID type", Toast.LENGTH_SHORT).show();
                                if (spinnerValidIdType != null) {
                                    spinnerValidIdType.requestFocus();
                                }
                                return;
                            }
                            
                            // Validate image upload
                            if (!hasValidId || validIdBitmap == null) {
                                Toast.makeText(ValidIdActivity.this, "Please upload or take a photo of your valid ID", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            // Create user account and save to Firestore
                            createUserAccount();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            saveValidIdData(); // Save current data before going back
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error going back: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            // Spinner selection listener is set up in setupSpinner() method
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkStoragePermission() {
        // For Android 13 and above, use READ_MEDIA_IMAGES
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
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
            e.printStackTrace();
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup spinner for valid ID type selection
     * Uses a TextView overlay to show "Select Valid ID Type" hint when nothing is selected
     * The spinner itself only contains actual ID types (no hint in dropdown)
     */
    private void setupSpinner() {
        try {
            // Create array WITHOUT hint - hint is shown via TextView overlay
            String[] idTypes = new String[]{
                    "National ID (PhilID)",
                    "Driver's License",
                    "Passport",
                    "UMID",
                    "SSS",
                    "PhilHealth",
                    "Voter's ID",
                    "Postal ID",
                    "Student ID",
                    "Others"
            };

            // Use standard adapter (no hint in items)
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                idTypes
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerValidIdType.setAdapter(adapter);
            
            // Initially show hint overlay (no selection) - overlay will cover spinner text
            // The spinner will show first item by default, but overlay covers it
            if (spinnerHintOverlay != null) {
                spinnerHintOverlay.setVisibility(View.VISIBLE);
            }
            
            // Don't set any selection initially - hint overlay will show
            hasUserSelectedIdType = false;
            selectedValidIdType = null;
            Log.d(TAG, "✅ Spinner initialized with hint overlay visible (covering spinner text)");

            // Set selection listener to save when user selects an ID type
            setupSpinnerSelectionListener();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up valid ID spinner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Setup spinner selection listener
     */
    private void setupSpinnerSelectionListener() {
        try {
            // ID types array (without hint)
            String[] idTypes = new String[]{
                    "National ID (PhilID)",
                    "Driver's License",
                    "Passport",
                    "UMID",
                    "SSS",
                    "PhilHealth",
                    "Voter's ID",
                    "Postal ID",
                    "Student ID",
                    "Others"
            };

            spinnerValidIdType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    // Ignore if we're restoring selection (programmatic change)
                    if (isRestoringSelection) {
                        // Still handle overlay visibility during restore
                        if (spinnerHintOverlay != null && position >= 0 && position < idTypes.length) {
                            spinnerHintOverlay.setVisibility(View.GONE);
                        } else if (spinnerHintOverlay != null) {
                            spinnerHintOverlay.setVisibility(View.VISIBLE);
                        }
                        return;
                    }
                    
                    // Valid selection (position >= 0 and within array bounds)
                    if (position >= 0 && position < idTypes.length) {
                        hasUserSelectedIdType = true;
                        selectedValidIdType = idTypes[position];
                        
                        // Hide hint overlay when something is selected
                        if (spinnerHintOverlay != null) {
                            spinnerHintOverlay.setVisibility(View.GONE);
                        }
                        
                        saveValidIdData(); // Save selection immediately
                        Log.d(TAG, "Valid ID type selected: " + selectedValidIdType);
                    } else {
                        selectedValidIdType = null;
                        hasUserSelectedIdType = false;
                        
                        // Show hint overlay when nothing is selected
                        if (spinnerHintOverlay != null) {
                            spinnerHintOverlay.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Ignore if we're restoring selection
                    if (isRestoringSelection) {
                        return;
                    }
                    
                    // Clear selection if nothing is selected
                    selectedValidIdType = null;
                    hasUserSelectedIdType = false;
                    
                    // Show hint overlay
                    if (spinnerHintOverlay != null) {
                        spinnerHintOverlay.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error setting up spinner selection listener: " + e.getMessage(), e);
        }
    }

    private void enableNextButton() {
        if (btnNext != null) {
            btnNext.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Open standard phone camera
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error handling permission result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK && data != null) {
                if (requestCode == CAMERA_REQUEST_CODE) {
                    // Handle standard camera result
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        Log.d(TAG, "Camera image captured, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        
                        // Create a temporary file and save the bitmap
                        validIdUri = saveBitmapToTempFile(bitmap);
                        validIdBitmap = bitmap;
                        
                        // Show the image and hide placeholder
                        ivValidId.setImageBitmap(validIdBitmap);
                        ivValidId.setVisibility(View.VISIBLE);
                        placeholderContainer.setVisibility(View.GONE);
                            
                            hasValidId = true;
                            enableNextButton();
                        saveValidIdData();
                        
                        // Success toast removed per request
                    } else {
                        Log.w(TAG, "Camera bitmap is null");
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == CUSTOM_CAMERA_REQUEST_CODE) {
                    // Handle custom camera result
                    String imagePath = data.getStringExtra("image_path");
                    if (imagePath != null && !imagePath.isEmpty()) {
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                validIdUri = Uri.fromFile(imageFile);
                                validIdBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), validIdUri);
                                
                                if (validIdBitmap != null) {
                                    // Show the image and hide placeholder
                                    ivValidId.setImageBitmap(validIdBitmap);
                                    ivValidId.setVisibility(View.VISIBLE);
                                    placeholderContainer.setVisibility(View.GONE);
                                    
                                    hasValidId = true;
                                    enableNextButton();
                                    saveValidIdData();
                                    
                                    // Success toast removed per request
                                } else {
                                    Log.e(TAG, "Failed to decode bitmap from custom camera");
                                    Toast.makeText(this, "Error loading captured image", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing custom camera image", e);
                            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (requestCode == GALLERY_REQUEST_CODE) {
                    Uri selectedUri = data.getData();
                    if (selectedUri != null) {
                        Log.d(TAG, "Gallery image selected, URI: " + selectedUri.toString());
                        validIdUri = selectedUri;
                        validIdBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                        if (validIdBitmap != null) {
                            // Show the image and hide placeholder
                            ivValidId.setImageBitmap(validIdBitmap);
                            ivValidId.setVisibility(View.VISIBLE);
                            placeholderContainer.setVisibility(View.GONE);
                            
                            hasValidId = true;
                            enableNextButton();
                            saveValidIdData(); // Save data for retention
                            // Success toast removed per request
                        } else {
                            Log.e(TAG, "Failed to decode bitmap from gallery URI");
                            Toast.makeText(this, "Error loading image from gallery", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a bitmap to a temporary file and returns its URI
     */
    private Uri saveBitmapToTempFile(Bitmap bitmap) {
        try {
            // Create a temporary file in the cache directory
            java.io.File tempFile = new java.io.File(getCacheDir(), "valid_id_" + System.currentTimeMillis() + ".jpg");
            
            // Save the bitmap to the file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            // Return the URI of the file
            return Uri.fromFile(tempFile);
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to temp file", e);
            return null;
        }
    }

    private void createUserAccount() {
        saveValidIdData(); // Save current data before creating account
        btnNext.setEnabled(false);
        btnNext.setText("Creating Account...");

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send email verification before completing registration
                                sendEmailVerification(user);
                            }
                        } else {
                            btnNext.setEnabled(true);
                            btnNext.setText("Next");
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ValidIdActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Checks if we can send a verification email based on rate limiting
     * @return null if allowed, error message if blocked
     */
    private String canSendVerificationEmail() {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();
        
        // Check if currently blocked
        long blockedUntil = prefs.getLong(KEY_BLOCKED_UNTIL, 0);
        if (blockedUntil > currentTime) {
            long minutesRemaining = (blockedUntil - currentTime) / 60000;
            return "Too many verification email attempts. Please wait " + minutesRemaining + " minute(s) before trying again.";
        }
        
        // Check time since last email
        long lastVerificationTime = prefs.getLong(KEY_LAST_VERIFICATION_TIME, 0);
        if (lastVerificationTime > 0) {
            long timeSinceLastEmail = currentTime - lastVerificationTime;
            if (timeSinceLastEmail < MIN_TIME_BETWEEN_EMAILS) {
                long secondsRemaining = (MIN_TIME_BETWEEN_EMAILS - timeSinceLastEmail) / 1000;
                return "Please wait " + secondsRemaining + " second(s) before requesting another verification email.";
            }
        }
        
        // Check attempts in the last hour
        long firstAttemptTime = prefs.getLong("first_attempt_time", 0);
        int attempts = prefs.getInt(KEY_VERIFICATION_ATTEMPTS, 0);
        
        if (firstAttemptTime > 0 && (currentTime - firstAttemptTime) < HOUR_IN_MILLIS) {
            // Still within the hour window
            if (attempts >= MAX_ATTEMPTS_PER_HOUR) {
                long timeUntilReset = HOUR_IN_MILLIS - (currentTime - firstAttemptTime);
                long minutesRemaining = timeUntilReset / 60000;
                return "Maximum verification email attempts reached. Please wait " + minutesRemaining + " minute(s) before trying again.";
            }
        } else {
            // Hour window expired, reset attempts
            attempts = 0;
            firstAttemptTime = currentTime;
        }
        
        return null; // Allowed to send
    }
    
    /**
     * Records a verification email attempt
     */
    private void recordVerificationAttempt(boolean success) {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long currentTime = System.currentTimeMillis();
        
        if (success) {
            // Successful send - record time and increment attempts
            editor.putLong(KEY_LAST_VERIFICATION_TIME, currentTime);
            
            long firstAttemptTime = prefs.getLong("first_attempt_time", 0);
            int attempts = prefs.getInt(KEY_VERIFICATION_ATTEMPTS, 0);
            
            if (firstAttemptTime == 0 || (currentTime - firstAttemptTime) >= HOUR_IN_MILLIS) {
                // New hour window
                editor.putLong("first_attempt_time", currentTime);
                editor.putInt(KEY_VERIFICATION_ATTEMPTS, 1);
            } else {
                // Same hour window
                editor.putInt(KEY_VERIFICATION_ATTEMPTS, attempts + 1);
            }
        } else {
            // Failed send - check if it's a rate limit error
            // This will be handled in the error handler
        }
        
        editor.apply();
    }
    
    /**
     * Handles rate limit error from Firebase and blocks future attempts
     */
    private void handleRateLimitError() {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long currentTime = System.currentTimeMillis();
        
        // Block for the specified duration
        editor.putLong(KEY_BLOCKED_UNTIL, currentTime + BLOCK_DURATION);
        editor.putLong("first_attempt_time", currentTime);
        editor.putInt(KEY_VERIFICATION_ATTEMPTS, MAX_ATTEMPTS_PER_HOUR);
        editor.apply();
        
        Log.w(TAG, "Rate limit detected. Blocked until: " + new java.util.Date(currentTime + BLOCK_DURATION));
    }
    
    /**
     * Gets user-friendly error message for Firebase errors
     */
    private String getUserFriendlyErrorMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return "Failed to send verification email. Please try again later.";
        }
        
        String errorMessage = exception.getMessage();
        
        // Check for rate limiting errors
        if (errorMessage.contains("blocked") || 
            errorMessage.contains("unusual activity") || 
            errorMessage.contains("too many requests") ||
            errorMessage.contains("quota exceeded")) {
            
            handleRateLimitError();
            long blockedUntil = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE)
                .getLong(KEY_BLOCKED_UNTIL, 0);
            
            if (blockedUntil > System.currentTimeMillis()) {
                long minutesRemaining = (blockedUntil - System.currentTimeMillis()) / 60000;
                return "Too many verification email requests. Please wait " + minutesRemaining + 
                       " minute(s) before trying again. This helps prevent spam.";
            }
            
            return "Too many verification email requests. Please wait 1 hour before trying again. This helps prevent spam.";
        }
        
        // Check for network errors
        if (errorMessage.contains("network") || errorMessage.contains("timeout")) {
            return "Network error. Please check your internet connection and try again.";
        }
        
        // Check for invalid email
        if (errorMessage.contains("invalid-email")) {
            return "Invalid email address. Please check your email and try again.";
        }
        
        // Generic error
        return "Failed to send verification email. Please try again later.";
    }
    
    /**
     * Checks if an error is retryable (transient errors that might succeed on retry)
     */
    private boolean isRetryableError(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return false;
        }
        
        String errorMessage = exception.getMessage().toLowerCase();
        
        // Retry for transient errors
        if (errorMessage.contains("network") || 
            errorMessage.contains("timeout") ||
            errorMessage.contains("connection") ||
            errorMessage.contains("unavailable") ||
            errorMessage.contains("internal error") ||
            errorMessage.contains("service unavailable")) {
            return true;
        }
        
        // Don't retry for permanent errors
        if (errorMessage.contains("invalid-email") ||
            errorMessage.contains("user-disabled") ||
            errorMessage.contains("user-not-found") ||
            errorMessage.contains("email-already-in-use") ||
            errorMessage.contains("operation-not-allowed")) {
            return false;
        }
        
        // Don't retry for rate limit errors (handled separately)
        if (errorMessage.contains("blocked") || 
            errorMessage.contains("unusual activity") || 
            errorMessage.contains("too many requests")) {
            return false;
        }
        
        // Default: don't retry for unknown errors
        return false;
    }
    
    /**
     * Sends email verification with retry logic for transient errors
     */
    private void sendEmailVerificationWithRetry(final FirebaseUser user, int retryCount) {
        // Check rate limits before sending
        String rateLimitError = canSendVerificationEmail();
        if (rateLimitError != null) {
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            Toast.makeText(ValidIdActivity.this, rateLimitError, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Rate limit check failed: " + rateLimitError);
            return;
        }
        
        // Update button text to show retry attempt if applicable
        if (retryCount > 0) {
            btnNext.setText("Retrying... (" + retryCount + "/" + MAX_RETRY_ATTEMPTS + ")");
            Log.d(TAG, "Retry attempt " + retryCount + " of " + MAX_RETRY_ATTEMPTS);
        } else {
            btnNext.setText("Sending Verification Email...");
        }
        
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ Verification email sent to: " + user.getEmail());
                            recordVerificationAttempt(true);
                            // Continue with registration after email is sent
                            generateCustomUserIdAndContinue(user);
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "Failed to send verification email (attempt " + (retryCount + 1) + ")", exception);
                            
                            // Check if it's a rate limit error
                            if (exception != null && exception.getMessage() != null) {
                                String errorMsg = exception.getMessage();
                                if (errorMsg.contains("blocked") || 
                                    errorMsg.contains("unusual activity") || 
                                    errorMsg.contains("too many requests")) {
                                    recordVerificationAttempt(false);
                                    handleRateLimitError();
                                    btnNext.setEnabled(true);
                                    btnNext.setText("Next");
                                    String userFriendlyMessage = getUserFriendlyErrorMessage(exception);
                                    Toast.makeText(ValidIdActivity.this, userFriendlyMessage, Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            
                            // Check if error is retryable and we haven't exceeded max retries
                            if (isRetryableError(exception) && retryCount < MAX_RETRY_ATTEMPTS) {
                                // Calculate exponential backoff delay
                                long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(BACKOFF_MULTIPLIER, retryCount));
                                delay = Math.min(delay, MAX_RETRY_DELAY); // Cap at max delay
                                
                                Log.d(TAG, "Retryable error detected. Retrying in " + (delay / 1000) + " seconds...");
                                
                                // Schedule retry with exponential backoff
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendEmailVerificationWithRetry(user, retryCount + 1);
                                    }
                                }, delay);
                            } else {
                                // Not retryable or max retries exceeded
                                btnNext.setEnabled(true);
                                btnNext.setText("Next");
                                
                                String userFriendlyMessage = getUserFriendlyErrorMessage(exception);
                                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                                    userFriendlyMessage = "Failed to send verification email after " + (MAX_RETRY_ATTEMPTS + 1) + " attempts. " + userFriendlyMessage;
                                    Log.w(TAG, "Max retry attempts reached");
                                }
                                
                                Toast.makeText(ValidIdActivity.this, userFriendlyMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
    
    /**
     * Sends email verification to the user with rate limiting protection and retry logic
     */
    private void sendEmailVerification(final FirebaseUser user) {
        sendEmailVerificationWithRetry(user, 0);
    }

    // Generate custom userId in the format RID-[auto-incremented value]
    private void generateCustomUserIdAndContinue(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Order by numeric field instead of string to get proper counting
        db.collection("users")
                .orderBy("userIdNumber", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    String newUserId = "RID-1";
                    int newUserIdNumber = 1;
                    
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Get the numeric ID field
                            Object userIdNumberObj = doc.get("userIdNumber");
                            if (userIdNumberObj != null) {
                                try {
                                    if (userIdNumberObj instanceof Long) {
                                        newUserIdNumber = ((Long) userIdNumberObj).intValue() + 1;
                                    } else if (userIdNumberObj instanceof Integer) {
                                        newUserIdNumber = (Integer) userIdNumberObj + 1;
                                    } else {
                                        newUserIdNumber = Integer.parseInt(userIdNumberObj.toString()) + 1;
                                    }
                                    newUserId = "RID-" + newUserIdNumber;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing userIdNumber, using fallback", e);
                                    // Try to get from userId string as fallback
                                    String lastUserId = doc.getString("userId");
                                    if (lastUserId != null && lastUserId.startsWith("RID-")) {
                                        try {
                                            newUserIdNumber = Integer.parseInt(lastUserId.replace("RID-", "")) + 1;
                                            newUserId = "RID-" + newUserIdNumber;
                                        } catch (NumberFormatException ex) {
                                            newUserId = "RID-1";
                                            newUserIdNumber = 1;
                                        }
                                    }
                                }
                            } else {
                                // Fallback: try to parse from userId string
                                String lastUserId = doc.getString("userId");
                                if (lastUserId != null && lastUserId.startsWith("RID-")) {
                                    try {
                                        newUserIdNumber = Integer.parseInt(lastUserId.replace("RID-", "")) + 1;
                                        newUserId = "RID-" + newUserIdNumber;
                                    } catch (NumberFormatException e) {
                                        newUserId = "RID-1";
                                        newUserIdNumber = 1;
                                    }
                                }
                            }
                            break; // Only need the first (highest)
                        }
                    }
                    
                    Log.d(TAG, "Generated new user ID: " + newUserId + " (number: " + newUserIdNumber + ")");
                    // Continue registration with newUserId
                    uploadImagesAndSaveUserData(newUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to generate user ID", e);
                    Toast.makeText(this, "Failed to generate user ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnNext.setEnabled(true);
                    btnNext.setText("Next");
                });
    }

    private void uploadImagesAndSaveUserData(String userId) {
        // Upload profile picture if exists
        if (hasProfilePicture) {
            uploadProfilePicture(userId, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String profilePictureUrl) {
                    // Upload valid ID
                    uploadValidId(userId, profilePictureUrl);
                }
            });
        } else {
            // Upload valid ID only
            uploadValidId(userId, null);
        }
    }

    private void uploadProfilePicture(String userId, OnSuccessListener<String> onProfileSuccess) {
        try {
            // Use firebaseUid for the profile picture path to ensure consistency
            String firebaseUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
            if (firebaseUid.isEmpty()) {
                Log.e(TAG, "Firebase UID not available");
                onProfileSuccess.onSuccess(null);
                return;
            }
            
            StorageReference profileImageRef = storageRef.child("profile_pictures/" + firebaseUid + "/profile.jpg");
            byte[] data = null;

            // Handle both bitmap and URI data
            if (profileBitmap != null) {
                // Use bitmap data directly
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                profileBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                data = baos.toByteArray();
            } else if (profileImageUriString != null) {
                // Use URI data
                Uri profileImageUri = Uri.parse(profileImageUriString);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                data = baos.toByteArray();
            }

            if (data == null) {
                onProfileSuccess.onSuccess(null);
                return;
            }

            Log.d(TAG, "Uploading profile picture to: " + profileImageRef.getPath());

            // Upload the image
            UploadTask uploadTask = profileImageRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Profile picture uploaded successfully");
                    // Get the download URL
                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            Log.d(TAG, "Profile picture download URL: " + downloadUri.toString());
                            onProfileSuccess.onSuccess(downloadUri.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failed to get profile picture download URL", e);
                            onProfileSuccess.onSuccess(null);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Failed to upload profile picture", e);
                    onProfileSuccess.onSuccess(null);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Error processing profile picture", e);
            onProfileSuccess.onSuccess(null);
        }
    }

    private void uploadValidId(String userId, String profilePictureUrl) {
        if (validIdBitmap == null) {
            saveUserDataToFirestore(userId, profilePictureUrl, null);
            return;
        }

        // Upload valid ID image
        uploadSingleValidId(userId, profilePictureUrl);
    }

    private void uploadSingleValidId(String userId, String profilePictureUrl) {
        try {
            // Use firebaseUid for the valid ID path to ensure consistency
            String firebaseUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
            if (firebaseUid.isEmpty()) {
                Log.e(TAG, "Firebase UID not available");
                saveUserDataToFirestore(userId, profilePictureUrl, null);
                return;
            }
            
            // Create unique filename for the image
            String imageFileName = "id.jpg";
            StorageReference validIdRef = storageRef.child("valid_ids/" + firebaseUid + "/" + imageFileName);

            // Get current image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            validIdBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            Log.d(TAG, "Uploading valid ID image to: " + validIdRef.getPath());

            // Upload the image
            UploadTask uploadTask = validIdRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Valid ID image uploaded successfully");
                    // Continue with next image
                    validIdRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            Log.d(TAG, "Valid ID image download URL: " + downloadUri.toString());
                            saveUserDataToFirestore(userId, profilePictureUrl, downloadUri.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failed to get valid ID image download URL", e);
                            saveUserDataToFirestore(userId, profilePictureUrl, null);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Failed to upload valid ID image", e);
                    saveUserDataToFirestore(userId, profilePictureUrl, null);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Error processing valid ID image", e);
            saveUserDataToFirestore(userId, profilePictureUrl, null);
        }
    }

    private void saveUserDataToFirestore(String userId, String profilePictureUrl, String validIdUrl) {
        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        
        // Extract and save the numeric part of userId for proper sorting
        int userIdNumber = 1;
        try {
            if (userId != null && userId.startsWith("RID-")) {
                userIdNumber = Integer.parseInt(userId.replace("RID-", ""));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing userId number, defaulting to 1", e);
        }
        userData.put("userIdNumber", userIdNumber);
        
        userData.put("firebaseUid", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "");
        userData.put("email", email);
        userData.put("fullName", firstName + " " + lastName);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("phoneNumber", mobileNumber);
        
        // Build complete address with street address if provided
        // Format: Street Address (if provided), Barangay, Town, Province
        String fullAddress;
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            fullAddress = streetAddress + ", " + barangay + ", " + cityTown + ", " + province;
        } else {
            fullAddress = barangay + ", " + cityTown + ", " + province;
        }
        userData.put("address", fullAddress);
        userData.put("province", province);
        userData.put("cityTown", cityTown);
        userData.put("barangay", barangay);
        userData.put("streetAddress", streetAddress != null ? streetAddress : "");
        userData.put("profilePictureUrl", profilePictureUrl != null ? profilePictureUrl : "");
        userData.put("validIdUrl", validIdUrl != null ? validIdUrl : "");
        userData.put("validIdType", selectedValidIdType != null ? selectedValidIdType : "");
        userData.put("validIdCount", 1); // Only one image
        String createdDate = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        String createdTime = new java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()).format(new java.util.Date());
        userData.put("createdDate", createdDate);
        userData.put("createdTime", createdTime);
        userData.put("isVerified", false);

        // Merge previously saved personal info from SharedPreferences if available
        try {
            SharedPreferences sp = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
            String birthday = sp.getString("birthday", null);
            String gender = sp.getString("gender", null);
            String civilStatus = sp.getString("civil_status", null);
            String religion = sp.getString("religion", null);
            String bloodType = sp.getString("blood_type", null);
            boolean pwd = sp.getBoolean("pwd", false);
            if (birthday != null) {
                userData.put("birthday", birthday);
                // Calculate and save age from birthday
                int age = calculateAgeFromBirthday(birthday);
                if (age > 0) {
                    userData.put("age", age);
                    Log.d(TAG, "Calculated age: " + age + " from birthday: " + birthday);
                }
            }
            if (gender != null) userData.put("gender", gender);
            if (civilStatus != null) userData.put("civil_status", civilStatus);
            if (religion != null) userData.put("religion", religion);
            if (bloodType != null) userData.put("blood_type", bloodType);
            userData.put("pwd", pwd);
            
            // Save email to SharedPreferences for ProfileActivity access
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("email_address", email);
            editor.putString("first_name", firstName);
            editor.putString("last_name", lastName);
            editor.putString("mobile_number", mobileNumber);
            editor.putString("province", province);
            editor.putString("city", cityTown);
            editor.putString("barangay", barangay);
            editor.putString("street_address", streetAddress != null ? streetAddress : "");
            
            // Save complete mailing address
            // Format: Street Address (if provided), Barangay, Town, Province
            String mailingAddress;
            if (streetAddress != null && !streetAddress.trim().isEmpty()) {
                mailingAddress = streetAddress + ", " + barangay + ", " + cityTown + ", " + province;
            } else {
                mailingAddress = barangay + ", " + cityTown + ", " + province;
            }
            editor.putString("mailing_address", mailingAddress);
            
            editor.apply();
        } catch (Exception ignored) {}

        // ✅ UPDATED: Use Firebase Auth UID as document ID for new users
        // This ensures document ID matches the firebaseUid for easier querying and FCM token management
        String firebaseUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        
        if (firebaseUid != null) {
            android.util.Log.d(TAG, "Creating user document with Firebase Auth UID as document ID: " + firebaseUid);
            
            FirestoreHelper.createUser(firebaseUid, userData,
                new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        android.util.Log.d(TAG, "✅ User data saved successfully with document ID: " + firebaseUid);
                        // Clear all registration data since registration is complete
                        clearRegistrationData();
                        // Navigate to success screen with clean stack
                        Intent intent = new Intent(ValidIdActivity.this, SuccessActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                },
                new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        btnNext.setEnabled(true);
                        btnNext.setText("Next");
                        android.util.Log.w(TAG, "Error saving user data", e);
                        android.widget.Toast.makeText(ValidIdActivity.this,
                                "Error saving user data: " + e.getMessage(),
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                });
        } else {
            // Fallback to auto-generated ID if no Firebase user (shouldn't happen)
            android.util.Log.w(TAG, "No Firebase Auth user found, using auto-generated document ID");
            
            FirestoreHelper.createUserWithAutoId(userData,
                new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                        android.util.Log.d(TAG, "User data saved with auto-generated ID");
                        // Clear all registration data since registration is complete
                        clearRegistrationData();
                        // Navigate to success screen with clean stack
                        Intent intent = new Intent(ValidIdActivity.this, SuccessActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                },
                new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        btnNext.setEnabled(true);
                        btnNext.setText("Next");
                        android.util.Log.w(TAG, "Error saving user data", e);
                        android.widget.Toast.makeText(ValidIdActivity.this,
                                "Error saving user data: " + e.getMessage(),
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    /**
     * Saves valid ID data to SharedPreferences for data retention
     */
    private void saveValidIdData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Save the count of valid ID images
            editor.putInt("valid_id_count", 1); // Only one image
            editor.putBoolean("has_valid_id", hasValidId);
            
            // Save the URI of the valid ID image
            if (validIdUri != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                validIdBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] byteArray = stream.toByteArray();
                    String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
                editor.putString("valid_id_uri", validIdUri.toString());
                editor.putString("valid_id_bitmap", base64Image);
            } else {
                editor.remove("valid_id_uri");
                editor.remove("valid_id_bitmap");
            }
            // Save selected valid ID type
            if (selectedValidIdType != null) {
                editor.putString("valid_id_type", selectedValidIdType);
            } else {
                editor.remove("valid_id_type");
            }
            
            editor.apply();
            Log.d(TAG, "Valid ID data saved to SharedPreferences. Count: " + 1);
        } catch (Exception e) {
            Log.e(TAG, "Error saving valid ID data", e);
        }
    }

    /**
     * Restores valid ID data from SharedPreferences
     */
    private void restoreValidIdData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            boolean hasSavedValidId = prefs.getBoolean("has_valid_id", false);
            String savedUriString = prefs.getString("valid_id_uri", null);
            String savedBitmapBase64 = prefs.getString("valid_id_bitmap", null);
            String savedValidIdType = prefs.getString("valid_id_type", null);
            
            Log.d(TAG, "=== RESTORING VALID ID DATA ===");
            Log.d(TAG, "Has saved valid ID: " + hasSavedValidId);
            Log.d(TAG, "Saved valid ID type: '" + savedValidIdType + "'");
            Log.d(TAG, "Current spinner selection before restore: " + 
                  (spinnerValidIdType != null ? spinnerValidIdType.getSelectedItemPosition() : "null"));
            
            if (hasSavedValidId && savedUriString != null && savedBitmapBase64 != null) {
                // Clear existing data
                validIdBitmap = null;
                validIdUri = null;
                
                Log.d(TAG, "Restoring valid ID image...");
                
                // Restore bitmap
                byte[] byteArray = android.util.Base64.decode(savedBitmapBase64, android.util.Base64.DEFAULT);
                validIdBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                
                // Restore URI
                validIdUri = Uri.parse(savedUriString);
                
                                 if (validIdBitmap != null) {
                    ivValidId.setImageBitmap(validIdBitmap);
                    ivValidId.setVisibility(View.VISIBLE);
                    placeholderContainer.setVisibility(View.GONE);
                    hasValidId = true;
                    enableNextButton();
                    Log.d(TAG, "✅ Valid ID data restored from SharedPreferences. Count: " + 1);
                } else {
                    Log.w(TAG, "Failed to decode bitmap for restored image");
                }
            } else {
                Log.d(TAG, "No saved valid ID data found");
            }

            // Restore selected valid ID type and reflect in UI
            // Only restore if there's a valid saved selection (not the hint text)
            Log.d(TAG, "Checking saved valid ID type for restore: '" + savedValidIdType + "'");
            if (savedValidIdType != null && !savedValidIdType.isEmpty() && 
                !savedValidIdType.equals("Select Valid ID Type") && 
                !savedValidIdType.trim().isEmpty() &&
                spinnerValidIdType != null) {
                
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerValidIdType.getAdapter();
                if (adapter != null) {
                    // Find the position of the saved type
                    int position = -1;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).equals(savedValidIdType)) {
                            position = i;
                            break;
                        }
                    }
                    
                    if (position >= 0) { // Valid position found
                        selectedValidIdType = savedValidIdType;
                        hasUserSelectedIdType = true; // Mark as user-selected since it was saved
                        // Set flag to prevent listener from firing during restore
                        // Make position final for use in inner class
                        final int finalPosition = position;
                        isRestoringSelection = true;
                        spinnerValidIdType.post(new Runnable() {
                            @Override
                            public void run() {
                                spinnerValidIdType.setSelection(finalPosition, false);
                                // Hide hint overlay since we have a selection
                                if (spinnerHintOverlay != null) {
                                    spinnerHintOverlay.setVisibility(View.GONE);
                                }
                                isRestoringSelection = false;
                            }
                        });
                        Log.d(TAG, "Valid ID type restored: " + savedValidIdType + " at position " + position);
                    } else {
                        // If saved type not found, show hint overlay
                        isRestoringSelection = true;
                        spinnerValidIdType.post(new Runnable() {
                            @Override
                            public void run() {
                                // Don't set selection - show hint overlay instead
                                if (spinnerHintOverlay != null) {
                                    spinnerHintOverlay.setVisibility(View.VISIBLE);
                                }
                                isRestoringSelection = false;
                            }
                        });
                        selectedValidIdType = null;
                        hasUserSelectedIdType = false;
                        Log.w(TAG, "Saved valid ID type not found in adapter: " + savedValidIdType + ", showing hint overlay");
                    }
                }
            } else {
                // No saved type, show hint overlay (no selection)
                isRestoringSelection = true;
                if (spinnerValidIdType != null) {
                    spinnerValidIdType.post(new Runnable() {
                        @Override
                        public void run() {
                            // Don't set selection - show hint overlay
                            if (spinnerHintOverlay != null) {
                                spinnerHintOverlay.setVisibility(View.VISIBLE);
                            }
                            isRestoringSelection = false;
                            Log.d(TAG, "✅ Showing hint overlay 'Select Valid ID Type' (no selection)");
                        }
                    });
                } else {
                    isRestoringSelection = false;
                }
                selectedValidIdType = null;
                hasUserSelectedIdType = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring valid ID data", e);
            e.printStackTrace();
        }
    }

    /**
     * Calculates age from birthday string (format: MM/dd/yyyy)
     */
    private int calculateAgeFromBirthday(String birthday) {
        try {
            if (birthday == null || birthday.isEmpty()) {
                return 0;
            }

            // Parse birthday (format: MM/dd/yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(birthday);
            
            if (birthDate == null) {
                Log.w(TAG, "Failed to parse birthday: " + birthday);
                return 0;
            }

            // Get current date
            Calendar today = Calendar.getInstance();
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);

            // Calculate age
            int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            // Check if birthday has occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating age from birthday: " + birthday, e);
            return 0;
        }
    }

    /**
     * Clears all registration data from SharedPreferences when registration is complete
     */
    private void clearRegistrationData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Clear registration activity data
            editor.remove("saved_first_name");
            editor.remove("saved_last_name");
            editor.remove("saved_mobile_number");
            editor.remove("saved_email");
            editor.remove("saved_password");
            editor.remove("saved_terms");
            
            // Clear personal info data
            editor.remove("saved_birthday");
            editor.remove("saved_gender");
            editor.remove("saved_civil_status");
            editor.remove("saved_religion");
            editor.remove("saved_blood_type");
            editor.remove("saved_pwd");
            
            // Clear address data
            editor.remove("saved_province");
            editor.remove("saved_city_town");
            editor.remove("saved_barangay");
            editor.remove("saved_street_address");
            
            // Clear profile picture data
            editor.remove("has_profile_picture");
            editor.remove("profile_picture_base64");
            
            // Clear valid ID data
            editor.remove("has_valid_id");
            editor.remove("valid_id_count");
            editor.remove("valid_id_uri");
            editor.remove("valid_id_bitmap");
            editor.remove("valid_id_type"); // Also clear the saved valid ID type
            
            editor.apply();
            Log.d(TAG, "✅ All registration data cleared from SharedPreferences");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing registration data", e);
        }
    }
    
}