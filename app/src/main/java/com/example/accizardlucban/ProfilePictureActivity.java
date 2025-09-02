package com.example.accizardlucban;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    private ImageView ivProfilePicture;
    private Button btnTakePhoto, btnUploadFromGallery, btnNext, btnBack;
    private String firstName, lastName, mobileNumber, email, password, province, cityTown, barangay;
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
            // Disable Next until a profile picture is provided
            if (btnNext != null) {
                btnNext.setEnabled(false);
            }
            getIntentData();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile picture activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            ivProfilePicture = findViewById(R.id.ivProfilePicture);
            btnTakePhoto = findViewById(R.id.btnTakePhoto);
            btnUploadFromGallery = findViewById(R.id.btnUploadFromGallery);
            btnNext = findViewById(R.id.btnNext);
            btnBack = findViewById(R.id.btnBack);
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
        if (btnTakePhoto != null) {
            btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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

        if (btnUploadFromGallery != null) {
            btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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

        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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
            if (resultCode == RESULT_OK && data != null) {
                if (requestCode == CAMERA_REQUEST_CODE) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null && ivProfilePicture != null) {
                        // Create circular bitmap for display
                        Bitmap circularBitmap = createCircularBitmap(bitmap);
                        ivProfilePicture.setImageBitmap(circularBitmap);
                        profileBitmap = bitmap; // Store original bitmap for upload
                        hasProfilePicture = true;
                        uploadedImageUrl = null; // Clear previous URL
                        uploadProfilePictureToFirebase(bitmap, true);
                        if (btnNext != null) {
                            btnNext.setEnabled(true);
                        }
                        Toast.makeText(this, "Profile picture captured successfully", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == GALLERY_REQUEST_CODE) {
                    profileImageUri = data.getData();
                    if (profileImageUri != null && ivProfilePicture != null) {
                        try {
                            // Load bitmap from URI and create circular version
                            InputStream inputStream = getContentResolver().openInputStream(profileImageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                Bitmap circularBitmap = createCircularBitmap(bitmap);
                                ivProfilePicture.setImageBitmap(circularBitmap);
                                hasProfilePicture = true;
                                uploadedImageUrl = null; // Clear previous URL
                                uploadProfilePictureToFirebase(profileImageUri, false);
                                if (btnNext != null) {
                                    btnNext.setEnabled(true);
                                }
                                Toast.makeText(this, "Profile picture selected successfully", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing gallery image", e);
                            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void proceedToValidId() {
        try {
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
            Toast.makeText(this, "Proceeding to Valid ID verification", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(ProfilePictureActivity.this, "Profile picture captured successfully", Toast.LENGTH_SHORT).show();
        } else if (!isFromCamera && imageData instanceof Uri) {
            profileImageUri = (Uri) imageData;
            uploadedImageUrl = null; // Will be set after account creation
            Toast.makeText(ProfilePictureActivity.this, "Profile picture selected successfully", Toast.LENGTH_SHORT).show();
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

        // Optionally scale to a consistent size (keeps aspect since it's square)
        int targetSize = 300;
        Bitmap scaledSquare = squareSize == targetSize
                ? squareCropped
                : Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

        // Create circular bitmap mask
        Bitmap circularBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
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
    }
}