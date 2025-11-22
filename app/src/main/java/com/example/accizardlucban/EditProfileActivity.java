package com.example.accizardlucban;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.InputStream;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.accizardlucban.StorageHelper;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;
    private static final int CAMERA_PERMISSION_CODE = 1003;
    private static final int STORAGE_PERMISSION_CODE = 1004;

    private ImageView backButton, profilePicture, editPictureButton;
    private Button saveButton;
    private EditText firstNameEdit, lastNameEdit, mobileNumberEdit,
            emailEdit, provinceEdit, cityEdit, streetAddressEdit;
    private AutoCompleteTextView barangayEdit;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_MOBILE = "mobile_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_CITY = "city";
    private static final String KEY_BARANGAY = "barangay";
    private static final String KEY_STREET_ADDRESS = "street_address";

    private Bitmap newProfileBitmap;
    private Uri newProfileImageUri;
    private boolean hasNewProfilePicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupClickListeners();
        setupBarangayAdapter();
        loadUserData();
        loadProfilePicture();
    }

    private void initViews() {
        backButton = findViewById(R.id.back_button);
        profilePicture = findViewById(R.id.profile_picture);
        editPictureButton = findViewById(R.id.edit_picture_button);
        saveButton = findViewById(R.id.save_button);

        firstNameEdit = findViewById(R.id.first_name_edit);
        lastNameEdit = findViewById(R.id.last_name_edit);
        mobileNumberEdit = findViewById(R.id.mobile_number_edit);
        emailEdit = findViewById(R.id.email_edit);
        provinceEdit = findViewById(R.id.province_edit);
        cityEdit = findViewById(R.id.city_edit);
        barangayEdit = findViewById(R.id.barangay_edit);
        streetAddressEdit = findViewById(R.id.etStreetAddress);
    }


    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        // Setup field watchers for barangay adapter
        provinceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBarangayAdapter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cityEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBarangayAdapter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBarangayAdapter() {
        barangayEdit.setThreshold(1);
        updateBarangayAdapter();
    }

    private void updateBarangayAdapter() {
        String selectedProvince = provinceEdit.getText().toString().trim();
        String cityTown = cityEdit.getText().toString().trim();
        
        if ("Quezon".equalsIgnoreCase(selectedProvince) && "Lucban".equalsIgnoreCase(cityTown)) {
            // Set up Lucban barangays for Autocomplete
            String[] lucbanBarangays = {
                    "Abang", "Aliliw", "Atulinao", "Ayuti (Poblacion)", "Barangay 1 (Poblacion)", "Barangay 2 (Poblacion)", 
                    "Barangay 3 (Poblacion)", "Barangay 4 (Poblacion)", "Barangay 5 (Poblacion)", "Barangay 6 (Poblacion)", 
                    "Barangay 7 (Poblacion)", "Barangay 8 (Poblacion)", "Barangay 9 (Poblacion)", "Barangay 10 (Poblacion)", 
                    "Igang", "Kabatete", "Kakawit", "Kalangay", "Kalyaat", "Kilib", "Kulapi", "Mahabang Parang", 
                    "Malupak", "Manasa", "May-It", "Nagsinamo", "Nalunao", "Palola", "Piis", "Samil", "Tiawe", "Tinamnan"
            };
            InitialLetterAutoCompleteAdapter adapter = new InitialLetterAutoCompleteAdapter(this, lucbanBarangays);
            barangayEdit.setAdapter(adapter);
        } else {
            // Allow free text input for other locations
            barangayEdit.setAdapter(null);
        }
    }

    private SharedPreferences getUserPrefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadUserData() {
        // First try to load from Intent extras (passed from ProfileActivity)
        Intent intent = getIntent();
        if (intent != null) {
            String firstName = intent.getStringExtra("firstName");
            String lastName = intent.getStringExtra("lastName");
            String mobileNumber = intent.getStringExtra("mobileNumber");
            String email = intent.getStringExtra("email");
            String province = intent.getStringExtra("province");
            String city = intent.getStringExtra("city");
            String barangay = intent.getStringExtra("barangay");
            String streetAddress = intent.getStringExtra("streetAddress");
            
            if (firstName != null) firstNameEdit.setText(firstName);
            if (lastName != null) lastNameEdit.setText(lastName);
            if (mobileNumber != null) mobileNumberEdit.setText(mobileNumber);
            if (email != null) emailEdit.setText(email);
            if (province != null) provinceEdit.setText(province);
            if (city != null) cityEdit.setText(city);
            if (barangay != null) barangayEdit.setText(barangay);
            if (streetAddress != null) streetAddressEdit.setText(streetAddress);
        } else {
            // Fallback to SharedPreferences if no Intent data
            SharedPreferences prefs = getUserPrefs();
            firstNameEdit.setText(prefs.getString(KEY_FIRST_NAME, ""));
            lastNameEdit.setText(prefs.getString(KEY_LAST_NAME, ""));
            mobileNumberEdit.setText(prefs.getString(KEY_MOBILE, ""));
            emailEdit.setText(prefs.getString(KEY_EMAIL, ""));
            provinceEdit.setText(prefs.getString(KEY_PROVINCE, ""));
            cityEdit.setText(prefs.getString(KEY_CITY, ""));
            barangayEdit.setText(prefs.getString(KEY_BARANGAY, ""));
            streetAddressEdit.setText(prefs.getString(KEY_STREET_ADDRESS, ""));
        }
        
        // Also try to load from Firestore to get the latest data
        loadUserDataFromFirestore();
    }

    private void loadUserDataFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user signed in, skipping Firestore data load");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .whereEqualTo("firebaseUid", user.getUid())
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    
                    // Load email from Firebase Auth (most reliable)
                    String email = user.getEmail();
                    if (email != null && !email.isEmpty()) {
                        emailEdit.setText(email);
                    } else {
                        // Fallback to Firestore
                        String firestoreEmail = doc.getString("email");
                        if (firestoreEmail != null && !firestoreEmail.isEmpty()) {
                            emailEdit.setText(firestoreEmail);
                        }
                    }
                    
                    // Load other fields from Firestore
                    String firstName = doc.getString("firstName");
                    String lastName = doc.getString("lastName");
                    String mobileNumber = doc.getString("mobileNumber");
                    if (mobileNumber == null) mobileNumber = doc.getString("phoneNumber");
                    String province = doc.getString("province");
                    String city = doc.getString("city");
                    if (city == null) city = doc.getString("cityTown");
                    String barangay = doc.getString("barangay");
                    String streetAddress = doc.getString("streetAddress");
                    
                    // Only update fields that are empty (don't overwrite intent data)
                    if (firstName != null && firstNameEdit.getText().toString().trim().isEmpty()) {
                        firstNameEdit.setText(firstName);
                    }
                    if (lastName != null && lastNameEdit.getText().toString().trim().isEmpty()) {
                        lastNameEdit.setText(lastName);
                    }
                    if (mobileNumber != null && mobileNumberEdit.getText().toString().trim().isEmpty()) {
                        mobileNumberEdit.setText(mobileNumber);
                    }
                    if (province != null && provinceEdit.getText().toString().trim().isEmpty()) {
                        provinceEdit.setText(province);
                    }
                    if (city != null && cityEdit.getText().toString().trim().isEmpty()) {
                        cityEdit.setText(city);
                    }
                    if (barangay != null && barangayEdit.getText().toString().trim().isEmpty()) {
                        barangayEdit.setText(barangay);
                    }
                    if (streetAddress != null && streetAddressEdit.getText().toString().trim().isEmpty()) {
                        streetAddressEdit.setText(streetAddress);
                    }
                    
                    // Update barangay adapter after loading data
                    updateBarangayAdapter();
                    
                    Log.d(TAG, "User data loaded from Firestore");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user data from Firestore", e);
            });
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }
        
        // Get form data
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String mobileNumber = mobileNumberEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String province = provinceEdit.getText().toString().trim();
        String city = cityEdit.getText().toString().trim();
        String barangay = barangayEdit.getText().toString().trim();
        String streetAddress = streetAddressEdit.getText().toString().trim();

        // Save to SharedPreferences immediately for instant local updates
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        profileManager.saveProfileLocally(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
        
        Log.d(TAG, "Profile data saved locally, now syncing to Firestore...");

        // Proceed with profile sync
        proceedWithProfileSync(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
    }

    private void proceedWithProfileSync(String firstName, String lastName, String mobileNumber,
                                       String email, String province, String city, String barangay, String streetAddress) {
        // If there's a new profile picture, upload it first, then sync profile data
        if (hasNewProfilePicture) {
            uploadNewProfilePicture();
        } else {
            // Sync profile data to Firestore
            syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
        }
    }


    private void syncProfileToFirestore(String firstName, String lastName, String mobileNumber,
                                       String email, String province, String city, String barangay, String streetAddress) {
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        
        profileManager.syncToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress, 
            new ProfileDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Profile synced to Firestore successfully");
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Set result to indicate profile was updated
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("profile_updated", true);
                        resultIntent.putExtra("full_name", firstName + " " + lastName);
                        String locationText = city;
                        if (barangay != null && !barangay.isEmpty()) {
                            locationText = city + ", " + barangay;
                        } else {
                            locationText = city + ", " + province;
                        }
                        // Build mailing address with street address if provided
                        String mailingAddress = city + ", " + province;
                        if (streetAddress != null && !streetAddress.isEmpty()) {
                            mailingAddress = streetAddress + ", " + barangay + ", " + city + ", " + province;
                        } else if (barangay != null && !barangay.isEmpty()) {
                            mailingAddress = barangay + ", " + city + ", " + province;
                        }
                        resultIntent.putExtra("location", locationText);
                        setResult(RESULT_OK, resultIntent);
                        
                        finish();
                    });
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to sync profile to Firestore: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, 
                            "Profile updated locally, but failed to sync to server. Changes will sync when online.", 
                            Toast.LENGTH_LONG).show();
                        
                        // Still set result as profile was updated locally
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("profile_updated", true);
                        resultIntent.putExtra("full_name", firstName + " " + lastName);
                        String locationText = city;
                        if (barangay != null && !barangay.isEmpty()) {
                            locationText = city + ", " + barangay;
                        } else {
                            locationText = city + ", " + province;
                        }
                        // Build mailing address with street address if provided
                        String mailingAddress = city + ", " + province;
                        if (streetAddress != null && !streetAddress.isEmpty()) {
                            mailingAddress = streetAddress + ", " + barangay + ", " + city + ", " + province;
                        } else if (barangay != null && !barangay.isEmpty()) {
                            mailingAddress = barangay + ", " + city + ", " + province;
                        }
                        resultIntent.putExtra("location", locationText);
                        setResult(RESULT_OK, resultIntent);
                        
                        finish();
                    });
                }
            });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate first name (required)
        if (TextUtils.isEmpty(firstNameEdit.getText().toString().trim())) {
            firstNameEdit.setError("First name is required");
            isValid = false;
        }

        // Validate last name (required)
        if (TextUtils.isEmpty(lastNameEdit.getText().toString().trim())) {
            lastNameEdit.setError("Last name is required");
            isValid = false;
        }

        // Validate mobile number (required if provided)
        String mobile = mobileNumberEdit.getText().toString().trim();
        if (!mobile.isEmpty() && !mobile.matches("^09\\d{9}$")) {
            mobileNumberEdit.setError("Invalid mobile number format (should be 09XXXXXXXXX)");
            isValid = false;
        }

        // Validate email (required if provided)
        String email = emailEdit.getText().toString().trim();
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.setError("Invalid email format");
            isValid = false;
        }

        // Validate province (required if city is provided)
        String province = provinceEdit.getText().toString().trim();
        String city = cityEdit.getText().toString().trim();
        if (!city.isEmpty() && TextUtils.isEmpty(province)) {
            provinceEdit.setError("Province is required when City/Town is provided");
            isValid = false;
        }

        return isValid;
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (checkCameraPermission()) {
                        openCamera();
                    } else {
                        requestCameraPermission();
                    }
                    break;
                case 1:
                    if (checkStoragePermission()) {
                        openGallery();
                    } else {
                        requestStoragePermission();
                    }
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private boolean checkStoragePermission() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    // Create circular bitmap for display
                    Bitmap circularBitmap = createCircularBitmap(bitmap);
                    profilePicture.setImageBitmap(circularBitmap);
                    newProfileBitmap = bitmap; // Store original bitmap for upload
                    hasNewProfilePicture = true;
                    Toast.makeText(this, "Profile picture captured successfully", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            // Create circular bitmap for display
                            Bitmap circularBitmap = createCircularBitmap(bitmap);
                            profilePicture.setImageBitmap(circularBitmap);
                            newProfileBitmap = bitmap; // Store original bitmap for upload
                            hasNewProfilePicture = true;
                            Toast.makeText(this, "Profile picture selected successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading image from gallery", e);
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void loadProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
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
                            loadImageFromUrl(profilePictureUrl);
                        } else {
                            Log.d(TAG, "No profile picture URL found in Firestore");
                            // Try to check if profile picture exists in Firebase Storage
                            checkProfilePictureInStorage(user.getUid());
                        }
                    } else {
                        Log.d(TAG, "No user document found for firebaseUid: " + user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile picture", e);
                });
        }
    }

    private void checkProfilePictureInStorage(String firebaseUid) {
        // Try to construct the profile picture path and check if it exists
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference profileRef = storage.getReference().child("profile_pictures/" + firebaseUid + "/profile.jpg");
        
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "Found profile picture in Storage: " + uri.toString());
            loadImageFromUrl(uri.toString());
            // Update Firestore with the found URL using ProfileDataManager
            ProfileDataManager profileManager = ProfileDataManager.getInstance(EditProfileActivity.this);
            profileManager.saveProfilePictureUrlLocally(uri.toString());
            profileManager.syncProfilePictureUrlToFirestore(uri.toString(), new ProfileDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Profile picture URL synced to Firestore");
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to sync profile picture URL to Firestore", e);
                }
            });
        }).addOnFailureListener(e -> {
            Log.d(TAG, "No profile picture found in Storage for UID: " + firebaseUid);
        });
    }



    private void loadImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profilePicture != null) {
                        // Create circular bitmap
                        Bitmap circularBitmap = createCircularBitmap(bitmap);
                        profilePicture.setImageBitmap(circularBitmap);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL", e);
            }
        }).start();
    }

    private Bitmap createCircularBitmap(Bitmap bitmap) {
        // Center-crop to square first to avoid distortion
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int squareSize = Math.min(width, height);
        int xOffset = (width - squareSize) / 2;
        int yOffset = (height - squareSize) / 2;

        Bitmap squareCropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

        int targetSize = 300;
        Bitmap scaledSquare = squareSize == targetSize
                ? squareCropped
                : Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

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

    private void uploadNewProfilePicture() {
        if (!hasNewProfilePicture) {
            // No new picture, just sync profile data
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String mobileNumber = mobileNumberEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String province = provinceEdit.getText().toString().trim();
            String city = cityEdit.getText().toString().trim();
            String barangay = barangayEdit.getText().toString().trim();
            String streetAddress = streetAddressEdit.getText().toString().trim();
            syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        if (newProfileBitmap != null) {
            StorageHelper.uploadProfileImage(userId, newProfileBitmap,
                    new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String downloadUrl) {
                            Log.d(TAG, "Profile picture uploaded successfully: " + downloadUrl);
                            
                            // Save profile picture URL locally immediately
                            ProfileDataManager profileManager = ProfileDataManager.getInstance(EditProfileActivity.this);
                            profileManager.saveProfilePictureUrlLocally(downloadUrl);
                            
                            // Sync profile picture URL to Firestore
                            profileManager.syncProfilePictureUrlToFirestore(downloadUrl, 
                                new ProfileDataManager.SyncCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "Profile picture URL synced to Firestore");
                                        // Now sync profile data
                                        String firstName = firstNameEdit.getText().toString().trim();
                                        String lastName = lastNameEdit.getText().toString().trim();
                                        String mobileNumber = mobileNumberEdit.getText().toString().trim();
                                        String email = emailEdit.getText().toString().trim();
                                        String province = provinceEdit.getText().toString().trim();
                                        String city = cityEdit.getText().toString().trim();
                                        String barangay = barangayEdit.getText().toString().trim();
                                        String streetAddress = streetAddressEdit.getText().toString().trim();
                                        syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
                                    }
                                    
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Failed to sync profile picture URL to Firestore", e);
                                        // Still proceed with profile data sync
                                        String firstName = firstNameEdit.getText().toString().trim();
                                        String lastName = lastNameEdit.getText().toString().trim();
                                        String mobileNumber = mobileNumberEdit.getText().toString().trim();
                                        String email = emailEdit.getText().toString().trim();
                                        String province = provinceEdit.getText().toString().trim();
                                        String city = cityEdit.getText().toString().trim();
                                        String barangay = barangayEdit.getText().toString().trim();
                                        String streetAddress = streetAddressEdit.getText().toString().trim();
                                        syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
                                    }
                                });
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error uploading profile picture", e);
                            Toast.makeText(EditProfileActivity.this, "Failed to upload profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            
                            // Still sync profile data even if picture upload failed
                            String firstName = firstNameEdit.getText().toString().trim();
                            String lastName = lastNameEdit.getText().toString().trim();
                            String mobileNumber = mobileNumberEdit.getText().toString().trim();
                            String email = emailEdit.getText().toString().trim();
                            String province = provinceEdit.getText().toString().trim();
                            String city = cityEdit.getText().toString().trim();
                            String barangay = barangayEdit.getText().toString().trim();
                            String streetAddress = streetAddressEdit.getText().toString().trim();
                            syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
                        }
                    });
        } else {
            // No bitmap available, just sync profile data
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String mobileNumber = mobileNumberEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String province = provinceEdit.getText().toString().trim();
            String city = cityEdit.getText().toString().trim();
            String barangay = barangayEdit.getText().toString().trim();
            String streetAddress = streetAddressEdit.getText().toString().trim();
            syncProfileToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, streetAddress);
        }
    }

    // Old methods removed - now using ProfileDataManager for all sync operations
}