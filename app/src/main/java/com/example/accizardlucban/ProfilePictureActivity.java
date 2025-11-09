package com.example.accizardlucban;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final String TAG = "ProfilePictureActivity";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private static final int STORAGE_PERMISSION_CODE = 103;

    private CircleImageView ivProfilePicture;
    private CardView btnUploadFromGallery;
    private Button btnNext;
    private ImageButton btnBack;
    private CardView profilePicturePlaceholder;
    private LinearLayout placeholderContent;
    private String firstName, lastName, mobileNumber, email, password, province, cityTown, barangay, streetAddress;
    private Uri profileImageUri;
    private boolean hasProfilePicture = false;
    private Bitmap profileBitmap;
    private String uploadedImageUrl;
    
    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        try {
            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            
            initializeViews();
            getIntentData();
            restoreProfilePictureData(); // Restore previously saved profile picture
            setupClickListeners();
            
            // Enable Next button if profile picture exists
            if (hasProfilePicture && btnNext != null) {
                btnNext.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile picture activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            ivProfilePicture = findViewById(R.id.ivProfilePicture);
            btnUploadFromGallery = findViewById(R.id.btnUploadFromGallery);
            btnNext = findViewById(R.id.btnNext);
            btnBack = findViewById(R.id.btnBack);
            profilePicturePlaceholder = findViewById(R.id.profilePicturePlaceholder);
            placeholderContent = findViewById(R.id.placeholderContent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getIntentData() {
        try {
            Intent intent = getIntent();
            firstName = intent.getStringExtra("firstName");
            lastName = intent.getStringExtra("lastName");
            mobileNumber = intent.getStringExtra("mobileNumber");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            province = intent.getStringExtra("province");
            cityTown = intent.getStringExtra("cityTown");
            barangay = intent.getStringExtra("barangay");
            streetAddress = intent.getStringExtra("streetAddress");

            // Debug: Check if data is received
            if (firstName == null || lastName == null) {
                Toast.makeText(this, "Warning: Some user data is missing", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting intent data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Placeholder area click listener - shows dialog to choose between Camera and Gallery
        if (profilePicturePlaceholder != null) {
            profilePicturePlaceholder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Show dialog to choose between Camera and Gallery
                        showImageSourceDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProfilePictureActivity.this, "Error accessing image sources", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // PlaceholderContent click listener - directly opens camera
        if (placeholderContent != null) {
            placeholderContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Directly open camera
                        if (checkCameraPermission()) {
                            openCamera();
                        } else {
                            requestCameraPermission();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProfilePictureActivity.this, "Error accessing camera", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Upload from Gallery button - directly opens gallery
        if (btnUploadFromGallery != null) {
            btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Directly open gallery
                        if (checkStoragePermission()) {
                            openGallery();
                        } else {
                            requestStoragePermission();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProfilePictureActivity.this, "Error accessing gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Next button
        if (btnNext != null) {
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Block proceeding if no profile picture has been selected
                        if (!hasProfilePicture) {
                            Toast.makeText(ProfilePictureActivity.this, "Please upload a profile picture to continue", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Proceed to Valid ID verification
                        proceedToValidId();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProfilePictureActivity.this, "Error proceeding to next step", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveProfilePictureData(); // Save current data before going back
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProfilePictureActivity.this, "Error going back", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
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
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error handling permission result", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
            
            if (resultCode == RESULT_OK) {
                if (requestCode == CAMERA_REQUEST_CODE) {
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        if (bitmap != null && ivProfilePicture != null) {
                            Log.d(TAG, "Camera bitmap received, size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                            
                            // Create circular bitmap and display directly
                            Bitmap circularBitmap = createCircularBitmap(bitmap);
                            showProfilePicture(circularBitmap);
                            
                            // Store bitmap for upload
                            profileBitmap = circularBitmap;
                            hasProfilePicture = true;
                            uploadedImageUrl = null;
                            uploadProfilePictureToFirebase(circularBitmap, true);
                            saveProfilePictureData();
                            
                            if (btnNext != null) {
                                btnNext.setEnabled(true);
                            }
                        } else {
                            Log.w(TAG, "Camera bitmap is null");
                            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (requestCode == GALLERY_REQUEST_CODE) {
                    if (data != null) {
                        profileImageUri = data.getData();
                        Log.d(TAG, "Gallery URI received: " + profileImageUri);
                        
                        if (profileImageUri != null && ivProfilePicture != null) {
                            try {
                                // Load the bitmap from URI
                                InputStream inputStream = getContentResolver().openInputStream(profileImageUri);
                                Bitmap galleryBitmap = BitmapFactory.decodeStream(inputStream);
                                
                                if (galleryBitmap != null) {
                                    Log.d(TAG, "Gallery bitmap loaded successfully, size: " + 
                                          galleryBitmap.getWidth() + "x" + galleryBitmap.getHeight());
                                    
                                    // Create circular bitmap and display directly
                                    Bitmap circularBitmap = createCircularBitmap(galleryBitmap);
                                    showProfilePicture(circularBitmap);
                                    
                                    // Store bitmap for upload
                                    profileBitmap = circularBitmap;
                                    hasProfilePicture = true;
                                    uploadedImageUrl = null;
                                    uploadProfilePictureToFirebase(circularBitmap, true);
                                    saveProfilePictureData();
                                    
                                    if (btnNext != null) {
                                        btnNext.setEnabled(true);
                                    }
                                } else {
                                    Log.e(TAG, "Failed to decode bitmap from gallery URI");
                                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing gallery image", e);
                                Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Gallery URI is null or ivProfilePicture is null");
                        }
                    } else {
                        Log.w(TAG, "Gallery data is null");
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Activity result cancelled for requestCode: " + requestCode);
            } else {
                Log.w(TAG, "Activity result failed with code: " + resultCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
            e.printStackTrace();
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows dialog to choose between Camera and Gallery
     */
    private void showImageSourceDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");
        
        String[] options = {"Take Photo", "Choose from Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Take Photo option
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            } else if (which == 1) {
                // Choose from Gallery option
                if (checkStoragePermission()) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Shows the profile picture and hides the placeholder content
     */
    private void showProfilePicture(Bitmap bitmap) {
        try {
            if (ivProfilePicture != null) {
                // Set the image to CircleImageView (automatically makes it circular)
                ivProfilePicture.setImageBitmap(bitmap);
                ivProfilePicture.setVisibility(View.VISIBLE);
                
                // Hide the dotted border background by hiding the entire placeholder container
                if (profilePicturePlaceholder != null) {
                    profilePicturePlaceholder.setVisibility(View.GONE);
                }
                
                // Make the image clickable to take another photo
                ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // Directly open camera to take another photo
                            if (checkCameraPermission()) {
                                openCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ProfilePictureActivity.this, "Error accessing camera", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                Log.d(TAG, "Profile picture displayed successfully as full circle and made clickable");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing profile picture", e);
        }
    }

    /**
     * Saves profile picture data to SharedPreferences for data retention
     */
    private void saveProfilePictureData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Log.d(TAG, "Saving profile picture data. Has picture: " + hasProfilePicture + ", Bitmap null: " + (profileBitmap == null));
            
            if (hasProfilePicture && profileBitmap != null) {
                // Save bitmap as byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                profileBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] byteArray = stream.toByteArray();
                
                // Convert to Base64 string for storage
                String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
                editor.putString("profile_picture_base64", base64Image);
                editor.putBoolean("has_profile_picture", true);
                
                Log.d(TAG, "✅ Profile picture data saved to SharedPreferences. Base64 length: " + base64Image.length());
            } else {
                editor.putBoolean("has_profile_picture", false);
                editor.remove("profile_picture_base64");
                Log.d(TAG, "Profile picture flags cleared from SharedPreferences");
            }
            
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile picture data", e);
            e.printStackTrace();
        }
    }

    /**
     * Restores profile picture data from SharedPreferences
     */
    private void restoreProfilePictureData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            boolean hasSavedProfilePicture = prefs.getBoolean("has_profile_picture", false);
            
            Log.d(TAG, "Attempting to restore profile picture data. Has saved: " + hasSavedProfilePicture);
            
            if (hasSavedProfilePicture) {
                String base64Image = prefs.getString("profile_picture_base64", null);
                if (base64Image != null && !base64Image.isEmpty()) {
                    Log.d(TAG, "Base64 image data found, decoding...");
                    // Convert Base64 back to bitmap
                    byte[] byteArray = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                    profileBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    
                    if (profileBitmap != null) {
                        hasProfilePicture = true;
                        Log.d(TAG, "Bitmap decoded successfully, size: " + profileBitmap.getWidth() + "x" + profileBitmap.getHeight());
                        showProfilePicture(profileBitmap);
                        Log.d(TAG, "✅ Profile picture data restored from SharedPreferences");
                    } else {
                        Log.w(TAG, "Failed to decode bitmap from Base64");
                    }
                } else {
                    Log.w(TAG, "Base64 image data is null or empty");
                }
            } else {
                Log.d(TAG, "No saved profile picture found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring profile picture data", e);
            e.printStackTrace();
        }
    }

    // Cropping functionality removed - images are now displayed directly in circular form

    private void proceedToValidId() {
        try {
            saveProfilePictureData(); // Save current data before proceeding
            
            Intent intent = new Intent(ProfilePictureActivity.this, ValidIdActivity.class);

            // Pass all the data to ValidIdActivity
            intent.putExtra("firstName", firstName != null ? firstName : "");
            intent.putExtra("lastName", lastName != null ? lastName : "");
            intent.putExtra("mobileNumber", mobileNumber != null ? mobileNumber : "");
            intent.putExtra("email", email != null ? email : "");
            intent.putExtra("password", password != null ? password : "");
            intent.putExtra("province", province != null ? province : "");
            intent.putExtra("cityTown", cityTown != null ? cityTown : "");
            intent.putExtra("barangay", barangay != null ? barangay : "");
            intent.putExtra("streetAddress", streetAddress != null ? streetAddress : "");
            
            // Pass profile picture data if exists
            if (hasProfilePicture) {
                intent.putExtra("hasProfilePicture", true);
                if (profileBitmap != null) {
                    // Convert bitmap to byte array for passing
                    java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
                    profileBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("profileBitmap", byteArray);
                } else if (profileImageUri != null) {
                    intent.putExtra("profileImageUri", profileImageUri.toString());
                }
            } else {
                intent.putExtra("hasProfilePicture", false);
            }

            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error proceeding to Valid ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadProfilePictureToFirebase(Object imageData, boolean isFromCamera) {
        // During registration, we don't have a Firebase UID yet, so we'll store the image data
        // and upload it later when the user account is created
        if (isFromCamera && imageData instanceof Bitmap) {
            profileBitmap = (Bitmap) imageData;
            uploadedImageUrl = null; // Will be set after account creation
        } else if (!isFromCamera && imageData instanceof Uri) {
            profileImageUri = (Uri) imageData;
            uploadedImageUrl = null; // Will be set after account creation
        }
    }

    private Bitmap createCircularBitmap(Bitmap bitmap) {
        // Center-crop the bitmap to a square first to avoid stretching
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int squareSize = Math.min(width, height);
        int xOffset = (width - squareSize) / 2;
        int yOffset = (height - squareSize) / 2;

        Bitmap squareCropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

        // Scale to a consistent size (keeps aspect since it's square)
        int targetSize = 400; // Increased size for better quality
        Bitmap scaledSquare = squareSize == targetSize
                ? squareCropped
                : Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

        // Recycle the intermediate bitmap if different
        if (scaledSquare != squareCropped) {
            squareCropped.recycle();
        }

        return scaledSquare;
    }
}