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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;
    private static final int CAMERA_PERMISSION_CODE = 1003;
    private static final int STORAGE_PERMISSION_CODE = 1004;

    private ImageView backButton, profilePicture, editPictureButton;
    private Button saveButton;
    private EditText firstNameEdit, lastNameEdit, mobileNumberEdit,
            provinceEdit, cityEdit, passwordEdit;
    private Spinner barangaySpinner;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_MOBILE = "mobile_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_CITY = "city";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BARANGAY = "barangay";

    private Bitmap newProfileBitmap;
    private Uri newProfileImageUri;
    private boolean hasNewProfilePicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupSpinner();
        setupClickListeners();
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
        provinceEdit = findViewById(R.id.province_edit);
        cityEdit = findViewById(R.id.city_edit);
        passwordEdit = findViewById(R.id.password_edit);
        barangaySpinner = findViewById(R.id.barangay_spinner);
    }

    private void setupSpinner() {
        String[] barangays = {
                "Select Barangay",
                "Barangay 1 (Poblacion)",
                "Barangay 2 (Poblacion)",
                "Barangay 3 (Poblacion)",
                "Barangay 4 (Poblacion)",
                "Barangay 5 (Poblacion)",
                "Barangay 6 (Poblacion)",
                "Barangay 7 (Poblacion)",
                "Barangay 8 (Poblacion)",
                "Anlilising",
                "Ayaas",
                "Bukid",
                "Silangan",
                "Kanlurang"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, barangays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        barangaySpinner.setAdapter(adapter);
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
    }

    private SharedPreferences getUserPrefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadUserData() {
        SharedPreferences prefs = getUserPrefs();
        firstNameEdit.setText(prefs.getString(KEY_FIRST_NAME, ""));
        lastNameEdit.setText(prefs.getString(KEY_LAST_NAME, ""));
        mobileNumberEdit.setText(prefs.getString(KEY_MOBILE, ""));
        provinceEdit.setText(prefs.getString(KEY_PROVINCE, ""));
        cityEdit.setText(prefs.getString(KEY_CITY, ""));
        // Don't set password for security
        // Set barangay spinner selection
        String barangay = prefs.getString(KEY_BARANGAY, "Select Barangay");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) barangaySpinner.getAdapter();
        if (adapter != null && barangay != null) {
            int position = adapter.getPosition(barangay);
            if (position >= 0) {
                barangaySpinner.setSelection(position);
            }
        }
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }
        
        // Save to SharedPreferences first
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String mobileNumber = mobileNumberEdit.getText().toString().trim();
        String province = provinceEdit.getText().toString().trim();
        String city = cityEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String barangay = barangaySpinner.getSelectedItem().toString();

        SharedPreferences.Editor editor = getUserPrefs().edit();
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_MOBILE, mobileNumber);
        editor.putString(KEY_PROVINCE, province);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_BARANGAY, barangay);
        editor.apply();

        // If there's a new profile picture, upload it first
        if (hasNewProfilePicture) {
            uploadNewProfilePicture();
        } else {
            // Update Firestore profile data
            updateFirestoreProfile(firstName, lastName, mobileNumber, province, city, barangay, password);
        }
    }

    private void updateFirestoreProfile(String firstName, String lastName, String mobileNumber, 
                                      String province, String city, String barangay, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final boolean[] needToUpdate = {false};
        
        // Update Firebase Auth password if changed
        if (!password.isEmpty()) {
            user.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor pwEditor = getUserPrefs().edit();
                        pwEditor.putString(KEY_PASSWORD, password);
                        pwEditor.apply();
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to update password in Firebase: " + errorMsg, Toast.LENGTH_LONG).show();
                        if (errorMsg != null && errorMsg.toLowerCase().contains("recent login")) {
                            Toast.makeText(this, "Please re-authenticate and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            needToUpdate[0] = true;
        }

        // Update Firestore user profile
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String firebaseUid = user.getUid();
        db.collection("users")
            .whereEqualTo("firebaseUid", firebaseUid)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String docId = doc.getId();
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("firstName", firstName);
                    userProfile.put("lastName", lastName);
                    userProfile.put("fullName", firstName + " " + lastName);
                    userProfile.put("mobileNumber", mobileNumber);
                    userProfile.put("province", province);
                    userProfile.put("city", city);
                    userProfile.put("barangay", barangay);
                    
                    db.collection("users").document(docId)
                        .update(userProfile)
                        .addOnSuccessListener(aVoid -> {
                            if (needToUpdate[0]) {
                                Toast.makeText(this, "Profile and Firebase Auth updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update profile on server: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    Toast.makeText(this, "User profile not found in database.", Toast.LENGTH_LONG).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to query user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate first name
        if (TextUtils.isEmpty(firstNameEdit.getText().toString().trim())) {
            firstNameEdit.setError("First name is required");
            isValid = false;
        }

        // Validate last name
        if (TextUtils.isEmpty(lastNameEdit.getText().toString().trim())) {
            lastNameEdit.setError("Last name is required");
            isValid = false;
        }

        // Validate mobile number
        String mobile = mobileNumberEdit.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            mobileNumberEdit.setError("Mobile number is required");
            isValid = false;
        } else if (!mobile.matches("^09\\d{9}$")) {
            mobileNumberEdit.setError("Invalid mobile number format");
            isValid = false;
        }

        // Email validation removed

        // Validate province
        if (TextUtils.isEmpty(provinceEdit.getText().toString().trim())) {
            provinceEdit.setError("Province is required");
            isValid = false;
        }

        // Validate city
        if (TextUtils.isEmpty(cityEdit.getText().toString().trim())) {
            cityEdit.setError("City/Town is required");
            isValid = false;
        }

        // Validate barangay selection
        if (barangaySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a barangay", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate password if provided
        String password = passwordEdit.getText().toString().trim();
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            passwordEdit.setError("Password must be at least 6 characters");
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
            // Update Firestore with the found URL
            updateProfilePictureUrlInFirestore(uri.toString());
        }).addOnFailureListener(e -> {
            Log.d(TAG, "No profile picture found in Storage for UID: " + firebaseUid);
        });
    }



    private void loadImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
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
        // Resize bitmap to consistent dimensions (e.g., 300x300 pixels)
        int targetSize = 300;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
        
        // Create circular bitmap
        Bitmap circularBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(circularBitmap);
        
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        
        // Create circular clipping path
        android.graphics.Path path = new android.graphics.Path();
        path.addCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, android.graphics.Path.Direction.CW);
        canvas.clipPath(path);
        
        // Draw the resized bitmap (will be clipped to circle)
        canvas.drawBitmap(resizedBitmap, 0, 0, paint);
        
        // Recycle the resized bitmap to free memory
        resizedBitmap.recycle();
        
        return circularBitmap;
    }

    private void uploadNewProfilePicture() {
        if (!hasNewProfilePicture) {
            saveProfileToFirestore();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        if (newProfileBitmap != null) {
            StorageHelper.uploadProfilePicture(userId, newProfileBitmap,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Log.d(TAG, "Profile picture uploaded successfully: " + downloadUrl);
                        updateProfilePictureUrlInFirestore(downloadUrl);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error uploading profile picture", e);
                        Toast.makeText(EditProfileActivity.this, "Failed to upload profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveProfileToFirestore();
                    }
                });
        }
    }

    private void updateProfilePictureUrlInFirestore(String profilePictureUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                            saveProfileToFirestore();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating profile picture URL in Firestore", e);
                            saveProfileToFirestore();
                        });
                } else {
                    saveProfileToFirestore();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error querying user document", e);
                saveProfileToFirestore();
            });
    }

    private void saveProfileToFirestore() {
        // This method will be called after profile picture upload
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String mobileNumber = mobileNumberEdit.getText().toString().trim();
        String province = provinceEdit.getText().toString().trim();
        String city = cityEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String barangay = barangaySpinner.getSelectedItem().toString();
        
        updateFirestoreProfile(firstName, lastName, mobileNumber, province, city, barangay, password);
    }
}