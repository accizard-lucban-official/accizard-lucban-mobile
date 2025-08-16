package com.example.accizardlucban;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private ImageView backButton;
    private TextView signOutButton;
    private TextView residentName;
    private Button editProfileButton;
    private Switch locationSwitch, notificationSwitch;
    private LinearLayout termsLayout, deleteAccountLayout;
    private TextView createdDateText;
    private ImageView profilePictureImageView;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupClickListeners();
        loadUserData();
        loadProfilePicture();
    }

    private void initViews() {
        backButton = findViewById(R.id.back_button);
        signOutButton = findViewById(R.id.sign_out_button);
        residentName = findViewById(R.id.resident_name);
        editProfileButton = findViewById(R.id.edit_profile_button);
        locationSwitch = findViewById(R.id.location_switch);
        notificationSwitch = findViewById(R.id.notification_switch);
        termsLayout = findViewById(R.id.terms_layout);
        deleteAccountLayout = findViewById(R.id.delete_account_layout);
        createdDateText = findViewById(R.id.created_date_text);
        profilePictureImageView = findViewById(R.id.profile_picture);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle location permission toggle
            if (isChecked) {
                // Request location permission if needed
                Toast.makeText(ProfileActivity.this, "Location access enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Location access disabled", Toast.LENGTH_SHORT).show();
            }
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notification settings
            if (isChecked) {
                Toast.makeText(ProfileActivity.this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        termsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Terms and Conditions Activity
                Intent intent = new Intent(ProfileActivity.this, TermAndConditionsActivity.class);
                startActivity(intent);
            }
        });

        deleteAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String firstName = prefs.getString(KEY_FIRST_NAME, "");
        String lastName = prefs.getString(KEY_LAST_NAME, "");
        String displayName = (firstName + " " + lastName).trim();
        if (displayName.isEmpty()) {
            displayName = "Your Name";
        }
        residentName.setText(displayName);

        // Load createdDate from Firestore
        String firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (firebaseUid != null && createdDateText != null) {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.QueryDocumentSnapshot doc = (com.google.firebase.firestore.QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String createdDate = doc.getString("createdDate");
                        if (createdDate != null && !createdDate.isEmpty()) {
                            // Convert MM/dd/yyyy to 'Month dd, yyyy'
                            try {
                                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
                                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault());
                                java.util.Date date = inputFormat.parse(createdDate);
                                String formatted = outputFormat.format(date);
                                createdDateText.setText("Created on " + formatted);
                                createdDateText.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                createdDateText.setText("Created on " + createdDate);
                                createdDateText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            createdDateText.setVisibility(View.GONE);
                        }
                    } else {
                        createdDateText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    createdDateText.setVisibility(View.GONE);
                });
        }
    }

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: Clear user session and navigate to login
                Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                // Navigate to Login Activity or Main Activity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: Implement account deletion
                Toast.makeText(ProfileActivity.this, "Account deletion requested", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && profilePictureImageView != null) {
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
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating profile picture URL in Firestore", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error querying user document for update", e);
            });
    }

    private void loadImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profilePictureImageView != null) {
                        // Create circular bitmap
                        Bitmap circularBitmap = createCircularBitmap(bitmap);
                        profilePictureImageView.setImageBitmap(circularBitmap);
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

    @Override
    protected void onResume() {
        super.onResume();
        loadProfilePicture(); // Refresh profile picture when returning from EditProfileActivity
    }
}