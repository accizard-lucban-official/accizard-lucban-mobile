package com.example.accizardlucban;

import android.Manifest;
import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import android.content.SharedPreferences;
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ValidIdActivity extends AppCompatActivity {

    private static final String TAG = "ValidIdActivity";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    private static final int STORAGE_PERMISSION_CODE = 103;

    private ImageView ivValidId;
    private Button btnTakePhoto, btnUploadFromGallery, btnNext;
    private ImageButton btnBack;
    private TextView tvValidIdList;
    private TextView tvSelectedIdsLabel;
    private android.widget.HorizontalScrollView hsThumbnails;
    private android.widget.LinearLayout thumbnailsContainer;
    private String firstName, lastName, mobileNumber, email, password, province, cityTown, barangay;
    private java.util.List<Uri> validIdUris;
    private java.util.List<Bitmap> validIdBitmaps;
    private boolean hasValidId = false;
    
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
            
            // Initialize lists for multiple images
            validIdUris = new java.util.ArrayList<>();
            validIdBitmaps = new java.util.ArrayList<>();
            
            initializeViews();
            getIntentData();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading Valid ID activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            // Initialize all views
            ivValidId = findViewById(R.id.iv_valid_id);
            btnUploadFromGallery = findViewById(R.id.btn_upload_gallery);
            btnNext = findViewById(R.id.btn_next);
            btnBack = findViewById(R.id.btn_back);
            tvValidIdList = findViewById(R.id.tv_valid_ids_list);

            // Check if views are found
            if (btnTakePhoto == null) {
            }
            if (btnUploadFromGallery == null) {
                Toast.makeText(this, "Error: Upload Gallery button not found", Toast.LENGTH_SHORT).show();
            }
            if (btnNext == null) {
                Toast.makeText(this, "Error: Next button not found", Toast.LENGTH_SHORT).show();
            }
            if (btnBack == null) {
                Toast.makeText(this, "Error: Back button not found", Toast.LENGTH_SHORT).show();
            }
            if (tvValidIdList == null) {
                Toast.makeText(this, "Error: Valid ID list text not found", Toast.LENGTH_SHORT).show();
            }
            if (ivValidId == null) {
                Toast.makeText(this, "Error: Image view not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            if (btnTakePhoto != null) {
                btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Toast.makeText(ValidIdActivity.this, "Take Photo button clicked", Toast.LENGTH_SHORT).show();
                            if (checkCameraPermission()) {
                                openCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error accessing camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (btnUploadFromGallery != null) {
                btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Toast.makeText(ValidIdActivity.this, "Upload Gallery button clicked", Toast.LENGTH_SHORT).show();
                            if (checkStoragePermission()) {
                                openGalleryMulti();
                            } else {
                                requestStoragePermission();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error accessing gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (btnNext != null) {
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (!hasValidId) {
                                Toast.makeText(ValidIdActivity.this, "Please upload a valid ID to continue", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ValidIdActivity.this, "Back button clicked", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error going back: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (tvValidIdList != null) {
                tvValidIdList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            showAllUploadedImages();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ValidIdActivity.this, "Error showing valid ID list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void openGalleryMulti() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Valid ID(s)"), GALLERY_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showValidIdList() {
        try {
            String validIds = "Valid IDs accepted:\n\n" +
                    "• Driver's License\n" +
                    "• Passport\n" +
                    "• National ID (PhilID)\n" +
                    "• SSS ID\n" +
                    "• PhilHealth ID\n" +
                    "• TIN ID\n" +
                    "• Voter's ID\n" +
                    "• Senior Citizen ID\n" +
                    "• PWD ID\n" +
                    "• Postal ID\n" +
                    "• Barangay ID\n" +
                    "• School ID (with signature)\n" +
                    "• Company ID (with signature)";

            new AlertDialog.Builder(this)
                    .setTitle("Valid IDs")
                    .setMessage(validIds)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing valid ID list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void enableNextButton() {
        if (btnNext != null) {
            btnNext.setEnabled(true);
            // You can also change the button appearance here if needed
        }
    }

    private void updateImageCounter() {
        if (tvValidIdList != null) {
            tvValidIdList.setText("Valid IDs (" + validIdBitmaps.size() + " uploaded) - Tap to view all");
        }
    }

    private void showAllUploadedImages() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        
        // Container
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setPadding(50, 50, 50, 50);

        // Header
        android.widget.TextView header = new android.widget.TextView(this);
        header.setText("Upload your Valid ID");
        header.setTextSize(20);
        header.setTextColor(getResources().getColor(android.R.color.black));
        header.setPadding(0, 0, 0, 16);
        root.addView(header);

        // Helper text
        android.widget.TextView helper = new android.widget.TextView(this);
        helper.setText("Make sure your ID is clear, readable, and not cropped. Avoid glare and ensure all corners are visible.");
        helper.setTextSize(14);
        helper.setTextColor(getResources().getColor(android.R.color.darker_gray));
        helper.setPadding(0, 0, 0, 20);
        root.addView(helper);

        // Large preview or placeholder
        android.widget.ImageView largePreview = new android.widget.ImageView(this);
        largePreview.setAdjustViewBounds(true);
        largePreview.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        largePreview.setBackgroundColor(0xFFEFEFEF);
        android.widget.LinearLayout.LayoutParams previewParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        previewParams.setMargins(0, 0, 0, 16);
        largePreview.setLayoutParams(previewParams);
        
        if (!validIdBitmaps.isEmpty()) {
            largePreview.setImageBitmap(validIdBitmaps.get(validIdBitmaps.size() - 1));
        } else {
            largePreview.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        root.addView(largePreview);

        // Thumbnail grid section
        if (!validIdBitmaps.isEmpty()) {
            android.widget.TextView thumbTitle = new android.widget.TextView(this);
            thumbTitle.setText("Attached IDs (" + validIdBitmaps.size() + ")");
            thumbTitle.setTextSize(16);
            thumbTitle.setTextColor(getResources().getColor(android.R.color.black));
            thumbTitle.setPadding(0, 0, 0, 8);
            root.addView(thumbTitle);

            android.widget.GridLayout grid = new android.widget.GridLayout(this);
            grid.setColumnCount(3);
            grid.setRowCount((int) Math.ceil(validIdBitmaps.size() / 3.0));
            android.widget.LinearLayout.LayoutParams gridParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            gridParams.setMargins(0, 0, 0, 16);
            grid.setLayoutParams(gridParams);

            int margin = (int) (8 * getResources().getDisplayMetrics().density);
            int thumbSize = (int) (96 * getResources().getDisplayMetrics().density);

            for (int i = 0; i < validIdBitmaps.size(); i++) {
                final int index = i;

                android.widget.FrameLayout frame = new android.widget.FrameLayout(this);
                android.widget.GridLayout.LayoutParams lp = new android.widget.GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = thumbSize;
                lp.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
                lp.setMargins(i % 3 == 0 ? 0 : margin, margin, 0, margin);
                frame.setLayoutParams(lp);

                android.widget.ImageView thumb = new android.widget.ImageView(this);
                thumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                thumb.setAdjustViewBounds(true);
                android.widget.FrameLayout.LayoutParams tParams = new android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                );
                thumb.setLayoutParams(tParams);
                thumb.setImageBitmap(validIdBitmaps.get(i));
                frame.addView(thumb);

                android.widget.ImageView close = new android.widget.ImageView(this);
                close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                android.widget.FrameLayout.LayoutParams cParams = new android.widget.FrameLayout.LayoutParams(
                        (int) (24 * getResources().getDisplayMetrics().density),
                        (int) (24 * getResources().getDisplayMetrics().density)
                );
                cParams.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
                close.setLayoutParams(cParams);
                close.setPadding(margin / 2, margin / 2, margin / 2, margin / 2);
                close.setOnClickListener(v -> {
                    validIdBitmaps.remove(index);
                    validIdUris.remove(index);
                    if (validIdBitmaps.isEmpty()) {
                        hasValidId = false;
                        ivValidId.setVisibility(View.GONE);
                        btnNext.setEnabled(false);
                    } else {
                        ivValidId.setImageBitmap(validIdBitmaps.get(validIdBitmaps.size() - 1));
                    }
                    updateImageCounter();
                    refreshThumbnails();
                    Toast.makeText(ValidIdActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                    showAllUploadedImages();
                });
                frame.addView(close);

                frame.setOnClickListener(v -> {
                    largePreview.setImageBitmap(validIdBitmaps.get(index));
                });

                grid.addView(frame);
            }
            root.addView(grid);
        }

        // Accepted IDs section
        android.widget.TextView validIdsTitle = new android.widget.TextView(this);
        validIdsTitle.setText("Accepted Valid ID Types");
        validIdsTitle.setTextSize(16);
        validIdsTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        validIdsTitle.setPadding(0, 8, 0, 8);
        root.addView(validIdsTitle);

        String validIds = "• Driver's License\n" +
                "• Passport\n" +
                "• National ID (PhilID)\n" +
                "• SSS ID\n" +
                "• PhilHealth ID\n" +
                "• TIN ID\n" +
                "• Voter's ID\n" +
                "• Senior Citizen ID\n" +
                "• PWD ID\n" +
                "• Postal ID\n" +
                "• Barangay ID\n" +
                "• School ID (with signature)\n" +
                "• Company ID (with signature)";

        android.widget.TextView validIdsText = new android.widget.TextView(this);
        validIdsText.setText(validIds);
        validIdsText.setTextSize(14);
        validIdsText.setTextColor(getResources().getColor(android.R.color.black));
        validIdsText.setPadding(0, 0, 0, 20);
        root.addView(validIdsText);

        // Actions row
        android.widget.LinearLayout actions = new android.widget.LinearLayout(this);
        actions.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        actions.setPadding(0, 8, 0, 0);

        android.widget.Button addMore = new android.widget.Button(this);
        addMore.setText("Add from Gallery");
        addMore.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGalleryMulti();
            } else {
                requestStoragePermission();
            }
        });
        actions.addView(addMore);

        android.view.View spacer = new android.view.View(this);
        android.widget.LinearLayout.LayoutParams spacerParams = new android.widget.LinearLayout.LayoutParams(0, 0, 1f);
        spacer.setLayoutParams(spacerParams);
        actions.addView(spacer);

        android.widget.Button close = new android.widget.Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> {});
        actions.addView(close);

        root.addView(actions);

        scrollView.addView(root);
        builder.setView(scrollView);
        builder.setCancelable(true);
        builder.setPositiveButton(null, null);
        builder.show();
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
                    openGalleryMulti();
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
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        // Add to lists
                        validIdBitmaps.add(bitmap);
                        validIdUris.add(null); // Camera images don't have URIs
                        
                        // Display the latest image without stretching
                        ivValidId.setAdjustViewBounds(true);
                        ivValidId.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        ivValidId.setImageBitmap(bitmap);
                        ivValidId.setVisibility(View.VISIBLE);
                        
                        hasValidId = true;
                        enableNextButton();
                        updateImageCounter();
                        Toast.makeText(this, "Valid ID captured successfully (Total: " + validIdBitmaps.size() + ")", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == GALLERY_REQUEST_CODE) {
                    try {
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                addImageFromUri(uri);
                            }
                        } else if (data.getData() != null) {
                            Uri uri = data.getData();
                            addImageFromUri(uri);
                        }
                        if (!validIdBitmaps.isEmpty()) {
                            Bitmap last = validIdBitmaps.get(validIdBitmaps.size() - 1);
                            ivValidId.setAdjustViewBounds(true);
                            ivValidId.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            ivValidId.setImageBitmap(last);
                            ivValidId.setVisibility(View.VISIBLE);
                            hasValidId = true;
                            enableNextButton();
                            updateImageCounter();
                            refreshThumbnails();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading images from gallery", e);
                        Toast.makeText(this, "Error loading images", Toast.LENGTH_SHORT).show();
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

    private void addImageFromUri(Uri selectedUri) {
        if (selectedUri == null) return;
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                validIdUris.add(selectedUri);
                validIdBitmaps.add(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from gallery", e);
        }
    }

    private void refreshThumbnails() {
        if (thumbnailsContainer == null) return;
        thumbnailsContainer.removeAllViews();
        if (validIdBitmaps.isEmpty()) {
            if (hsThumbnails != null) hsThumbnails.setVisibility(View.GONE);
            if (tvSelectedIdsLabel != null) tvSelectedIdsLabel.setVisibility(View.GONE);
            return;
        }
        if (hsThumbnails != null) hsThumbnails.setVisibility(View.VISIBLE);
        if (tvSelectedIdsLabel != null) tvSelectedIdsLabel.setVisibility(View.VISIBLE);

        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < validIdBitmaps.size(); i++) {
            final int index = i;
            android.widget.FrameLayout card = new android.widget.FrameLayout(this);
            android.widget.LinearLayout.LayoutParams cardParams = new android.widget.LinearLayout.LayoutParams(
                    (int) (96 * getResources().getDisplayMetrics().density),
                    (int) (96 * getResources().getDisplayMetrics().density)
            );
            cardParams.setMargins(i == 0 ? 0 : margin, 0, 0, 0);
            card.setLayoutParams(cardParams);
            card.setForeground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

            ImageView thumb = new ImageView(this);
            thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            thumb.setAdjustViewBounds(true);
            thumb.setImageBitmap(validIdBitmaps.get(i));

            android.widget.FrameLayout.LayoutParams thumbParams = new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            );
            thumb.setLayoutParams(thumbParams);
            card.addView(thumb);

            ImageView close = new ImageView(this);
            close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            android.widget.FrameLayout.LayoutParams closeParams = new android.widget.FrameLayout.LayoutParams(
                    (int) (24 * getResources().getDisplayMetrics().density),
                    (int) (24 * getResources().getDisplayMetrics().density)
            );
            closeParams.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
            close.setLayoutParams(closeParams);
            close.setPadding(margin / 2, margin / 2, margin / 2, margin / 2);
            close.setBackgroundResource(android.R.color.transparent);
            close.setOnClickListener(v -> {
                validIdBitmaps.remove(index);
                validIdUris.remove(index);
                if (validIdBitmaps.isEmpty()) {
                    hasValidId = false;
                    ivValidId.setVisibility(View.GONE);
                    btnNext.setEnabled(false);
                } else {
                    ivValidId.setImageBitmap(validIdBitmaps.get(validIdBitmaps.size() - 1));
                }
                updateImageCounter();
                refreshThumbnails();
            });
            card.addView(close);

            card.setOnClickListener(v -> {
                // Set main preview to this image
                ivValidId.setAdjustViewBounds(true);
                ivValidId.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivValidId.setImageBitmap(validIdBitmaps.get(index));
                ivValidId.setVisibility(View.VISIBLE);
            });

            thumbnailsContainer.addView(card);
        }
    }

    private void createUserAccount() {
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
                                // Generate custom userId (RID-[auto-incremented])
                                generateCustomUserIdAndContinue(user);
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

    // Generate custom userId in the format RID-[auto-incremented value]
    private void generateCustomUserIdAndContinue(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("userId", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    String newUserId = "RID-1";
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String lastUserId = doc.getString("userId");
                            if (lastUserId != null && lastUserId.startsWith("RID-")) {
                                try {
                                    int lastNum = Integer.parseInt(lastUserId.replace("RID-", ""));
                                    newUserId = "RID-" + (lastNum + 1);
                                } catch (NumberFormatException e) {
                                    // fallback to RID-1 if parsing fails
                                    newUserId = "RID-1";
                                }
                            }
                            break; // Only need the first (highest)
                        }
                    }
                    // Continue registration with newUserId
                    uploadImagesAndSaveUserData(newUserId);
                })
                .addOnFailureListener(e -> {
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
        if (validIdBitmaps.isEmpty()) {
            saveUserDataToFirestore(userId, profilePictureUrl, null);
            return;
        }

        // Upload multiple valid ID images
        uploadMultipleValidIds(userId, profilePictureUrl, 0);
    }

    private void uploadMultipleValidIds(String userId, String profilePictureUrl, int currentIndex) {
        if (currentIndex >= validIdBitmaps.size()) {
            // All images uploaded, save user data
            saveUserDataToFirestore(userId, profilePictureUrl, "Multiple images uploaded");
            return;
        }

        try {
            // Use firebaseUid for the valid ID path to ensure consistency
            String firebaseUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
            if (firebaseUid.isEmpty()) {
                Log.e(TAG, "Firebase UID not available");
                saveUserDataToFirestore(userId, profilePictureUrl, null);
                return;
            }
            
            // Create unique filename for each image
            String imageFileName = "id_" + (currentIndex + 1) + ".jpg";
            StorageReference validIdRef = storageRef.child("valid_ids/" + firebaseUid + "/" + imageFileName);

            // Get current image
            Bitmap bitmap = validIdBitmaps.get(currentIndex);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            Log.d(TAG, "Uploading valid ID image " + (currentIndex + 1) + " of " + validIdBitmaps.size());

            // Upload the image
            UploadTask uploadTask = validIdRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Valid ID image " + (currentIndex + 1) + " uploaded successfully");
                    // Continue with next image
                    uploadMultipleValidIds(userId, profilePictureUrl, currentIndex + 1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Failed to upload valid ID image " + (currentIndex + 1), e);
                    // Continue with next image even if this one fails
                    uploadMultipleValidIds(userId, profilePictureUrl, currentIndex + 1);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Error processing valid ID image " + (currentIndex + 1), e);
            // Continue with next image
            uploadMultipleValidIds(userId, profilePictureUrl, currentIndex + 1);
        }
    }

    private void saveUserDataToFirestore(String userId, String profilePictureUrl, String validIdUrl) {
        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("firebaseUid", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "");
        userData.put("email", email);
        userData.put("fullName", firstName + " " + lastName);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("phoneNumber", mobileNumber);
        userData.put("address", province + ", " + cityTown + ", " + barangay);
        userData.put("province", province);
        userData.put("cityTown", cityTown);
        userData.put("barangay", barangay);
        userData.put("profilePictureUrl", profilePictureUrl != null ? profilePictureUrl : "");
        userData.put("validIdUrl", validIdUrl != null ? validIdUrl : "");
        userData.put("validIdCount", validIdBitmaps.size());
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
            if (birthday != null) userData.put("birthday", birthday);
            if (gender != null) userData.put("gender", gender);
            if (civilStatus != null) userData.put("civil_status", civilStatus);
            if (religion != null) userData.put("religion", religion);
            if (bloodType != null) userData.put("blood_type", bloodType);
            userData.put("pwd", pwd);
        } catch (Exception ignored) {}

        FirestoreHelper.createUserWithAutoId(userData,
            new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
                @Override
                public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                    android.util.Log.d(TAG, "User data saved successfully");
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