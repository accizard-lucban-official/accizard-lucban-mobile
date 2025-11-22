package com.example.accizardlucban;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final String ALERTS_PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    private static final String KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT = "last_viewed_announcement_count";
    
    private ImageView backButton;
    private TextView signOutButton;
    private TextView residentName;
    private TextView editProfileButton;
    private Switch locationSwitch, notificationSwitch;
    private LinearLayout termsLayout, deleteAccountLayout;
    private TextView createdDateText;
    private ImageView profilePictureImageView;
    private TextView verifiedStatusText;
    private View verifiedStatusDot;
    
    // Your Info section views
    private LinearLayout residentNameInfoLayout;
    private LinearLayout mobileNumberInfoLayout;
    private LinearLayout emailAddressInfoLayout;
    private LinearLayout mailingAddressInfoLayout;
    
    // Invite Friends section
    private LinearLayout inviteFriendsLayout;
    
    // Bottom navigation
    private LinearLayout navHome, navChat, navReport, navMap, navAlerts, navProfile;
    private TextView alertsBadgeProfile;
    private SharedPreferences alertsSharedPreferences;
    
    // Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    
    // ActivityResultLauncher for handling profile updates
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SharedPreferences for badge
        alertsSharedPreferences = getSharedPreferences(ALERTS_PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        setupEditProfileLauncher();
        loadUserData();
        loadProfilePicture();
        loadVerifiedStatus();
        
        // Update notification badge
        updateNotificationBadge();
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
        verifiedStatusText = findViewById(R.id.verified_status_text);
        verifiedStatusDot = findViewById(R.id.verified_status_dot);
        
        // Apply circular clip to profile picture
        if (profilePictureImageView != null) {
            profilePictureImageView.setClipToOutline(true);
            profilePictureImageView.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        }
        
        // Initialize Your Info section views
        residentNameInfoLayout = findViewById(R.id.resident_name_info_layout);
        mobileNumberInfoLayout = findViewById(R.id.mobile_number_info_layout);
        emailAddressInfoLayout = findViewById(R.id.email_address_info_layout);
        mailingAddressInfoLayout = findViewById(R.id.mailing_address_info_layout);
        
        // Initialize Invite Friends section
        inviteFriendsLayout = findViewById(R.id.invite_friends_layout);
        
        // Initialize bottom navigation
        navHome = findViewById(R.id.nav_home);
        navChat = findViewById(R.id.nav_chat);
        navReport = findViewById(R.id.nav_report);
        navMap = findViewById(R.id.nav_map);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        alertsBadgeProfile = findViewById(R.id.alerts_badge_profile);
        
        // Register badge with AnnouncementNotificationManager so it gets updated when alerts are viewed
        if (alertsBadgeProfile != null) {
            AnnouncementNotificationManager.getInstance().registerBadge("ProfileActivity", alertsBadgeProfile);
            Log.d(TAG, "✅ ProfileActivity badge registered with AnnouncementNotificationManager");
        }
        
        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            // Set refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_dark,
                android.R.color.holo_red_light
            );
            
            // Set refresh listener
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshProfileData();
                }
            });
            
            Log.d(TAG, "✅ SwipeRefreshLayout initialized");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackNavigation();
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
                // Pass current user data to EditProfileActivity
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                intent.putExtra("firstName", prefs.getString(KEY_FIRST_NAME, ""));
                intent.putExtra("lastName", prefs.getString(KEY_LAST_NAME, ""));
                intent.putExtra("mobileNumber", prefs.getString("mobile_number", ""));
                intent.putExtra("province", prefs.getString("province", ""));
                intent.putExtra("city", prefs.getString("city", ""));
                editProfileLauncher.launch(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        deleteAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });

        // Invite Friends click listener
        if (inviteFriendsLayout != null) {
            inviteFriendsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareApp();
                }
            });
        }

        // Your Info section click listeners
        if (residentNameInfoLayout != null) {
            residentNameInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to edit profile for name
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }

        if (mobileNumberInfoLayout != null) {
            mobileNumberInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to edit profile for mobile number
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }

        if (emailAddressInfoLayout != null) {
            emailAddressInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to edit profile for email
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }

        if (mailingAddressInfoLayout != null) {
            mailingAddressInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to edit profile for address
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }
    }

    private void setupEditProfileLauncher() {
        editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean profileUpdated = data.getBooleanExtra("profile_updated", false);
                    
                    if (profileUpdated) {
                        Log.d(TAG, "Profile was updated, refreshing user data");
                        // Refresh profile data immediately
                        loadUserData();
                        loadProfilePicture();
                        
                        // Show success message
                        String fullName = data.getStringExtra("full_name");
                        if (fullName != null && !fullName.isEmpty()) {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void setupBottomNavigation() {
        // Home tab
        if (navHome != null) {
            navHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, MainDashboard.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            });
        }

        // Chat tab
        if (navChat != null) {
            navChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            });
        }

        // Report tab
        if (navReport != null) {
            navReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ReportSubmissionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            });
        }

        // Map tab
        if (navMap != null) {
            navMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, MapViewActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            });
        }

        // Alerts tab
        if (navAlerts != null) {
            navAlerts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, AlertsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            });
        }

        // Profile tab (current)
        if (navProfile != null) {
            navProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Already on profile screen
                    Toast.makeText(ProfileActivity.this, "You're already on Profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserData() {
        // Use ProfileDataManager for consistent data access
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        String fullName = profileManager.getFullName();
        
        if (fullName != null && !fullName.isEmpty()) {
            residentName.setText(fullName);
        } else {
            // Fallback to SharedPreferences if ProfileDataManager doesn't have data
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String firstName = prefs.getString(KEY_FIRST_NAME, "");
            String lastName = prefs.getString(KEY_LAST_NAME, "");
            String displayName = (firstName + " " + lastName).trim();
            if (displayName.isEmpty()) {
                displayName = "Resident Name";
            }
            residentName.setText(displayName);
        }
        
        // Load additional user data for Your Info section
        loadUserInfoData();

        // Set default created date to match image
        if (createdDateText != null) {
            createdDateText.setText("Created on June 14, 2025");
            createdDateText.setVisibility(View.VISIBLE);
        }
        
        // Load createdDate from Firestore (optional)
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
                        }
                        // Keep default if no date found
                    }
                    // Keep default if no user found
                })
                .addOnFailureListener(e -> {
                    // Keep default on failure
                });
        }
    }

    private void loadUserInfoData() {
        // ✅ FIXED: Load directly from SharedPreferences first
        loadUserInfoFromSharedPreferences();
        
        // Then try to update from Firestore if available
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
                        
                        // Update SharedPreferences with Firestore data for consistency
                        updateSharedPreferencesFromFirestore(doc);
                        
                        // Reload from SharedPreferences to update UI
                        loadUserInfoFromSharedPreferences();
                        
                        Log.d(TAG, "Synced user info from Firestore");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user info from Firestore", e);
                });
        }
    }
    
    private void loadUserInfoFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load resident name from SharedPreferences
        String firstName = prefs.getString(KEY_FIRST_NAME, "");
        String lastName = prefs.getString(KEY_LAST_NAME, "");
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            fullName = "Resident Name";
        }
        updateInfoText(residentNameInfoLayout, fullName);
        
        // Load mobile number from SharedPreferences
        String mobileNumber = prefs.getString("mobile_number", "");
        if (mobileNumber.isEmpty()) {
            mobileNumber = "Mobile Number";
        }
        updateInfoText(mobileNumberInfoLayout, mobileNumber);
        
        // Load email address from SharedPreferences
        String emailAddress = prefs.getString("email", "");
        if (emailAddress.isEmpty()) {
            emailAddress = prefs.getString("email_address", "Email Address");
        }
        updateInfoText(emailAddressInfoLayout, emailAddress);
        
        // ✅ FIXED: Load mailing address from SharedPreferences with proper construction
        String mailingAddress = prefs.getString("mailing_address", "");
        Log.d(TAG, "📧 Reading mailing_address: '" + mailingAddress + "'");
        
        if (mailingAddress.isEmpty()) {
            mailingAddress = prefs.getString("address", "");
            Log.d(TAG, "📧 Reading address: '" + mailingAddress + "'");
        }
        
        // If still empty, construct from individual components
        if (mailingAddress.isEmpty()) {
            String barangay = prefs.getString("barangay", "");
            String city = prefs.getString("city", "");
            if (city.isEmpty()) {
                city = prefs.getString("cityTown", "");
            }
            String province = prefs.getString("province", "");
            
            Log.d(TAG, "📍 Constructing address - Barangay: '" + barangay + "', City: '" + city + "', Province: '" + province + "'");
            
            // Construct complete address: Barangay, City/Town, Province
            if (!barangay.isEmpty() && !city.isEmpty() && !province.isEmpty()) {
                mailingAddress = barangay + ", " + city + ", " + province;
                Log.d(TAG, "✅ Constructed full address (all 3): " + mailingAddress);
            } else if (!city.isEmpty() && !province.isEmpty()) {
                mailingAddress = city + ", " + province;
                Log.d(TAG, "✅ Constructed address (city+province): " + mailingAddress);
            } else if (!barangay.isEmpty() && !city.isEmpty()) {
                mailingAddress = barangay + ", " + city;
                Log.d(TAG, "✅ Constructed address (barangay+city): " + mailingAddress);
            } else if (!city.isEmpty()) {
                mailingAddress = city;
                Log.d(TAG, "✅ Using city only: " + mailingAddress);
            } else if (!barangay.isEmpty()) {
                mailingAddress = barangay;
                Log.d(TAG, "✅ Using barangay only: " + mailingAddress);
            } else {
                mailingAddress = "Address";
                Log.d(TAG, "⚠️ No address data found, using default");
            }
        } else {
            Log.d(TAG, "✅ Using existing mailing address: " + mailingAddress);
        }
        
        updateInfoText(mailingAddressInfoLayout, mailingAddress);
        Log.d(TAG, "✅ Final mailing address displayed: " + mailingAddress);
    }
    
    private void updateSharedPreferencesFromFirestore(QueryDocumentSnapshot doc) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save user info
        editor.putString(KEY_FIRST_NAME, doc.getString("firstName"));
        editor.putString(KEY_LAST_NAME, doc.getString("lastName"));
        editor.putString("mobile_number", doc.getString("phoneNumber"));
        editor.putString("email", doc.getString("email"));
        editor.putString("email_address", doc.getString("email"));
        
        // Save address components
        String province = doc.getString("province");
        String cityTown = doc.getString("cityTown");
        String barangay = doc.getString("barangay");
        String address = doc.getString("address");
        String location = doc.getString("location");
        
        editor.putString("province", province);
        editor.putString("city", cityTown);
        editor.putString("cityTown", cityTown);
        editor.putString("barangay", barangay);
        
        // Save mailing address with fallback construction
        if (address != null && !address.isEmpty()) {
            editor.putString("mailing_address", address);
            editor.putString("address", address);
        } else if (barangay != null && cityTown != null && province != null) {
            String constructedAddress = barangay + ", " + cityTown + ", " + province;
            editor.putString("mailing_address", constructedAddress);
            editor.putString("address", constructedAddress);
        }
        
        // Save location text for MainDashboard
        if (location != null && !location.isEmpty()) {
            editor.putString("location_text", location);
        } else if (barangay != null && !barangay.isEmpty()) {
            editor.putString("location_text", barangay);
        }
        
        // Commit changes for immediate sync
        editor.commit();
        
        Log.d(TAG, "SharedPreferences synced from Firestore successfully");
    }

    private void updateInfoText(LinearLayout layout, String text) {
        if (layout != null) {
            // Find the TextView in the layout (it should be the second child)
            if (layout.getChildCount() > 1) {
                View child = layout.getChildAt(1);
                if (child instanceof TextView) {
                    ((TextView) child).setText(text);
                }
            }
        }
    }

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performSignOut();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Performs the actual sign out process:
     * 1. Sign out from Firebase Auth
     * 2. Clear saved credentials
     * 3. Clear user data
     * 4. Delete FCM token
     * 5. Navigate to login screen
     */
    private void performSignOut() {
        try {
            // Sign out from Firebase Auth
            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "✅ Signed out from Firebase Auth");
            
            // Clear saved credentials for persistent login
            MainActivity.clearSavedCredentials(this);
            
            // Clear all user data
            clearUserData();
            
            // Delete FCM token from Firestore
            try {
                FCMTokenManager tokenManager = new FCMTokenManager(this);
                tokenManager.deleteFCMTokenFromFirestore();
            } catch (Exception e) {
                Log.w(TAG, "Could not delete FCM token: " + e.getMessage());
            }
            
            Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate to Login Activity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error during sign out: " + e.getMessage(), e);
            Toast.makeText(ProfileActivity.this, "Error signing out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAccountDialog() {
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete_account, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        
        // Initialize views
        EditText etPasswordConfirm = bottomSheetView.findViewById(R.id.et_password_confirm);
        Button btnDeleteAccount = bottomSheetView.findViewById(R.id.btn_delete_account);
        Button btnCancelDelete = bottomSheetView.findViewById(R.id.btn_cancel_delete);
        
        // Set click listeners
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = etPasswordConfirm.getText().toString().trim();
                if (enteredPassword.isEmpty()) {
                    etPasswordConfirm.setError("Please enter your password");
                    etPasswordConfirm.requestFocus();
                    return;
                }
                
                // Verify password and delete account
                verifyPasswordAndDeleteAccount(enteredPassword, bottomSheetDialog);
            }
        });
        
        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        
        // Show bottom sheet
        bottomSheetDialog.show();
    }
    
    private void verifyPasswordAndDeleteAccount(String enteredPassword, BottomSheetDialog bottomSheetDialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state
        Toast.makeText(this, "Verifying password...", Toast.LENGTH_SHORT).show();
        
        // Re-authenticate user with entered password
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.getEmail(), enteredPassword);
        
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password is correct, proceed with account deletion
                        deleteUserAccount(bottomSheetDialog);
                    } else {
                        // Password is incorrect
                        Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void deleteUserAccount(BottomSheetDialog bottomSheetDialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog before final deletion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Final Confirmation");
        builder.setMessage("Are you absolutely sure you want to delete your account?\n\n" +
                "This will permanently delete:\n" +
                "• Your profile and personal information\n" +
                "• All your submitted reports\n" +
                "• All your chat messages\n" +
                "• Your profile picture\n\n" +
                "This action CANNOT be undone!");
        builder.setPositiveButton("Yes, Delete Forever", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close bottom sheet first
                bottomSheetDialog.dismiss();
                
                // Show loading dialog
                android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(ProfileActivity.this);
                progressDialog.setMessage("Deleting your account and all data...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                // Delete all user data in sequence
                deleteAllUserData(user.getUid(), progressDialog, () -> {
                    // Finally delete the Firebase Auth account
                    user.delete()
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                
                                if (task.isSuccessful()) {
                                    // Clear local data
                                    clearUserData();
                                    
                                    // Clear saved credentials for persistent login
                                    MainActivity.clearSavedCredentials(ProfileActivity.this);
                                    
                                    // Show success message
                                    Toast.makeText(ProfileActivity.this, "✅ Account deleted successfully", Toast.LENGTH_LONG).show();
                                    
                                    // Navigate to login screen
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e(TAG, "Failed to delete Firebase Auth account", task.getException());
                                    Toast.makeText(ProfileActivity.this, "❌ Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e(TAG, "Error deleting Firebase Auth account", e);
                                Toast.makeText(ProfileActivity.this, "❌ Error deleting account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        
        // Make the positive button red to emphasize danger
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }
    
    /**
     * ✅ COMPREHENSIVE: Delete ALL user data from Firebase
     * Deletes: Profile, Reports, Chat Messages, Profile Picture, FCM Tokens, Chat Rooms
     */
    private void deleteAllUserData(String userId, android.app.ProgressDialog progressDialog, Runnable onComplete) {
        Log.d(TAG, "🗑️ Starting comprehensive account deletion for user: " + userId);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        
        // Step 1: Delete user profile picture from Storage
        progressDialog.setMessage("Deleting profile picture...");
        deleteProfilePictureFromStorage(userId, storage, () -> {
            Log.d(TAG, "✅ Profile picture deleted (or didn't exist)");
            
            // Step 2: Delete all user's reports
            progressDialog.setMessage("Deleting your reports...");
            deleteUserReports(userId, db, () -> {
                Log.d(TAG, "✅ User reports deleted");
                
                // Step 3: Delete all user's chat messages
                progressDialog.setMessage("Deleting chat messages...");
                deleteUserChatMessages(userId, db, () -> {
                    Log.d(TAG, "✅ Chat messages deleted");
                    
                    // Step 4: Delete user's chat room
                    progressDialog.setMessage("Deleting chat room...");
                    deleteUserChatRoom(userId, db, () -> {
                        Log.d(TAG, "✅ Chat room deleted");
                        
                        // Step 5: Delete user profile from Firestore
                        progressDialog.setMessage("Deleting profile data...");
                        deleteUserProfile(userId, db, () -> {
                            Log.d(TAG, "✅ User profile deleted");
                            
                            // Step 6: Delete FCM tokens
                            progressDialog.setMessage("Cleaning up...");
                            deleteFCMTokens(userId, db, () -> {
                                Log.d(TAG, "✅ FCM tokens deleted");
                                Log.d(TAG, "🎉 All user data deleted successfully!");
                                
                                // All done, proceed to delete auth account
                                onComplete.run();
                            });
                        });
                    });
                });
            });
        });
    }
    
    /**
     * Delete profile picture from Firebase Storage
     */
    private void deleteProfilePictureFromStorage(String userId, FirebaseStorage storage, Runnable onComplete) {
        try {
            StorageReference profileRef = storage.getReference()
                    .child("profile_pictures/" + userId + "/profile.jpg");
            
            profileRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Profile picture deleted from Storage");
                        onComplete.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "⚠️ Profile picture not found or already deleted: " + e.getMessage());
                        // Continue anyway - picture might not exist
                        onComplete.run();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting profile picture", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * Delete all user's submitted reports
     */
    private void deleteUserReports(String userId, FirebaseFirestore db, Runnable onComplete) {
        try {
            db.collection("reports")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No reports found for user");
                            onComplete.run();
                            return;
                        }
                        
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " reports to delete");
                        int totalReports = queryDocumentSnapshots.size();
                        int[] deletedCount = {0};
                        
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("reports").document(doc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedCount[0]++;
                                        Log.d(TAG, "Report deleted: " + doc.getId() + " (" + deletedCount[0] + "/" + totalReports + ")");
                                        
                                        if (deletedCount[0] == totalReports) {
                                            Log.d(TAG, "✅ All reports deleted");
                                            onComplete.run();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting report: " + doc.getId(), e);
                                        deletedCount[0]++;
                                        
                                        if (deletedCount[0] == totalReports) {
                                            onComplete.run(); // Continue anyway
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error querying reports", e);
                        onComplete.run(); // Continue anyway
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserReports", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * Delete all user's chat messages
     */
    private void deleteUserChatMessages(String userId, FirebaseFirestore db, Runnable onComplete) {
        try {
            db.collection("chat_messages")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No chat messages found for user");
                            onComplete.run();
                            return;
                        }
                        
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " chat messages to delete");
                        int totalMessages = queryDocumentSnapshots.size();
                        int[] deletedCount = {0};
                        
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("chat_messages").document(doc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedCount[0]++;
                                        Log.d(TAG, "Chat message deleted: " + doc.getId() + " (" + deletedCount[0] + "/" + totalMessages + ")");
                                        
                                        if (deletedCount[0] == totalMessages) {
                                            Log.d(TAG, "✅ All chat messages deleted");
                                            onComplete.run();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting chat message: " + doc.getId(), e);
                                        deletedCount[0]++;
                                        
                                        if (deletedCount[0] == totalMessages) {
                                            onComplete.run(); // Continue anyway
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error querying chat messages", e);
                        onComplete.run(); // Continue anyway
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserChatMessages", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * Delete user's chat room
     */
    private void deleteUserChatRoom(String userId, FirebaseFirestore db, Runnable onComplete) {
        try {
            db.collection("chats")
                    .document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Chat room deleted");
                        onComplete.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "⚠️ Chat room not found or already deleted: " + e.getMessage());
                        onComplete.run(); // Continue anyway
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting chat room", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * Delete user profile from Firestore
     */
    private void deleteUserProfile(String userId, FirebaseFirestore db, Runnable onComplete) {
        try {
            db.collection("users")
                    .whereEqualTo("firebaseUid", userId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            String docId = doc.getId();
                            
                            db.collection("users").document(docId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "✅ User profile deleted from Firestore");
                                        onComplete.run();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting user profile from Firestore", e);
                                        onComplete.run(); // Continue anyway
                                    });
                        } else {
                            Log.d(TAG, "No user profile found in Firestore");
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error querying user profile", e);
                        onComplete.run(); // Continue anyway
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserProfile", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * Delete FCM tokens
     */
    private void deleteFCMTokens(String userId, FirebaseFirestore db, Runnable onComplete) {
        try {
            db.collection("fcmTokens")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No FCM tokens found for user");
                            onComplete.run();
                            return;
                        }
                        
                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " FCM tokens to delete");
                        int totalTokens = queryDocumentSnapshots.size();
                        int[] deletedCount = {0};
                        
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("fcmTokens").document(doc.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedCount[0]++;
                                        Log.d(TAG, "FCM token deleted: " + doc.getId());
                                        
                                        if (deletedCount[0] == totalTokens) {
                                            Log.d(TAG, "✅ All FCM tokens deleted");
                                            onComplete.run();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting FCM token: " + doc.getId(), e);
                                        deletedCount[0]++;
                                        
                                        if (deletedCount[0] == totalTokens) {
                                            onComplete.run(); // Continue anyway
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error querying FCM tokens", e);
                        onComplete.run(); // Continue anyway
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteFCMTokens", e);
            onComplete.run(); // Continue anyway
        }
    }
    
    /**
     * ✅ COMPREHENSIVE: Clear ALL local user data
     */
    private void clearUserData() {
        try {
            // Clear user profile preferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().clear().apply();
            Log.d(TAG, "✅ User profile preferences cleared");
            
            // Clear alerts preferences
            SharedPreferences alertsPrefs = getSharedPreferences(ALERTS_PREFS_NAME, MODE_PRIVATE);
            alertsPrefs.edit().clear().apply();
            Log.d(TAG, "✅ Alerts preferences cleared");
            
            // Clear any default preferences
            SharedPreferences defaultPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
            defaultPrefs.edit().clear().apply();
            Log.d(TAG, "✅ Default preferences cleared");
            
            // Clear ProfileDataManager cache
            try {
                ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
                // Force clear by reinitializing
                Log.d(TAG, "✅ ProfileDataManager cache cleared");
            } catch (Exception e) {
                Log.w(TAG, "Could not clear ProfileDataManager: " + e.getMessage());
            }
            
            // Clear ProfilePictureCache
            try {
                ProfilePictureCache.getInstance().clearCache();
                Log.d(TAG, "✅ Profile picture cache cleared");
            } catch (Exception e) {
                Log.w(TAG, "Could not clear ProfilePictureCache: " + e.getMessage());
            }
            
            Log.d(TAG, "🎉 All local user data cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user data from local storage", e);
        }
    }

    /**
     * Load verified status from Firestore and update UI
     */
    private void loadVerifiedStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user logged in, cannot load verified status");
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
                    
                    // Get verified field from Firestore
                    Object verifiedObj = doc.get("verified");
                    boolean isVerified = false;
                    
                    // Handle different data types
                    if (verifiedObj instanceof Boolean) {
                        isVerified = (Boolean) verifiedObj;
                    } else if (verifiedObj instanceof String) {
                        isVerified = Boolean.parseBoolean((String) verifiedObj);
                    } else if (verifiedObj != null) {
                        // Try to convert to boolean
                        isVerified = Boolean.parseBoolean(verifiedObj.toString());
                    }
                    
                    Log.d(TAG, "Verified status from Firestore: " + isVerified);
                    
                    // Update UI based on verified status
                    updateVerifiedStatusUI(isVerified);
                } else {
                    Log.w(TAG, "No user document found for firebaseUid: " + user.getUid());
                    // Default to not verified if user document not found
                    updateVerifiedStatusUI(false);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading verified status from Firestore", e);
                // Default to not verified on error
                updateVerifiedStatusUI(false);
            });
    }
    
    /**
     * Update verified status UI based on verification state
     */
    private void updateVerifiedStatusUI(boolean isVerified) {
        if (verifiedStatusText == null || verifiedStatusDot == null) {
            Log.w(TAG, "Verified status views are null, cannot update UI");
            return;
        }
        
        if (isVerified) {
            // Verified account - green text and green dot
            verifiedStatusText.setText("Account Verified");
            verifiedStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            verifiedStatusDot.setBackgroundResource(R.drawable.green_dot);
            verifiedStatusDot.setVisibility(View.VISIBLE);
            Log.d(TAG, "✅ Account is verified - showing green status");
        } else {
            // Not verified account - red text and red dot
            verifiedStatusText.setText("Account Not Verified");
            verifiedStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            verifiedStatusDot.setBackgroundResource(R.drawable.red_dot);
            verifiedStatusDot.setVisibility(View.VISIBLE);
            Log.d(TAG, "⚠️ Account is not verified - showing red status");
        }
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
                            // Use cached loading for instant display
                            ProfilePictureCache.getInstance().loadProfilePicture(profilePictureImageView, profilePictureUrl);
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
            // Use cached loading for instant display
            ProfilePictureCache.getInstance().loadProfilePicture(profilePictureImageView, uri.toString());
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
                final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profilePictureImageView != null) {
                        // The circular clipping is handled by OutlineProvider
                        profilePictureImageView.setImageBitmap(bitmap);
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

    private void handleBackNavigation() {
        try {
            // Check where we came from
            Intent intent = getIntent();
            String fromActivity = intent.getStringExtra("from_activity");
            
            if ("MapViewActivity".equals(fromActivity)) {
                // Go back to MapViewActivity specifically
                Log.d(TAG, "Navigating back to MapViewActivity");
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } else {
                // Default behavior - just finish
                Log.d(TAG, "Default back navigation");
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling back navigation: " + e.getMessage(), e);
            // Fallback to simple finish
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
    
    private void shareApp() {
        try {
            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            
            // Get app name and package name
            String appName = getString(R.string.app_name);
            String packageName = getPackageName();
            
            // Create shareable link with referral code
            String shareUrl = generateReferralLink(packageName);
            
            // Create share message
            String shareMessage = "Join me on " + appName + "! 🏘️\n\n" +
                    "Download the app to stay connected with your community and get important updates from MDRRMO. " +
                    "Perfect for residents of Lucban, Quezon!\n\n" +
                    "Download here: " + shareUrl + "\n\n" +
                    "#Accizard #Lucban #Community #MDRRMO";
            
            // Set share content
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me on " + appName + " - Community App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            
            // Start share chooser
            Intent chooser = Intent.createChooser(shareIntent, "Share " + appName);
            if (chooser.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                Toast.makeText(this, "No sharing app available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing app", e);
            Toast.makeText(this, "Error sharing app", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String generateReferralLink(String packageName) {
        try {
            // Get current user info for referral
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String firstName = prefs.getString(KEY_FIRST_NAME, "");
            String lastName = prefs.getString(KEY_LAST_NAME, "");
            String userName = (firstName + " " + lastName).trim();
            
            // Create referral code (you can customize this)
            String referralCode = "REF" + System.currentTimeMillis() % 10000;
            
            // Store referral code for tracking
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("referral_code", referralCode);
            editor.apply();
            
            // Create referral link (you can use your own domain)
            String baseUrl = "https://play.google.com/store/apps/details?id=" + packageName;
            String referralUrl = baseUrl + "&referrer=" + referralCode;
            
            return referralUrl;
        } catch (Exception e) {
            Log.e(TAG, "Error generating referral link", e);
            // Fallback to basic link
            return "https://play.google.com/store/apps/details?id=" + packageName;
        }
    }
    
    // Method to handle referral tracking (call this from MainActivity when app is opened)
    public static void handleReferralLink(android.content.Context context, android.content.Intent intent) {
        try {
            if (intent != null && intent.getData() != null) {
                String data = intent.getData().toString();
                if (data.contains("referrer=")) {
                    String referralCode = data.substring(data.indexOf("referrer=") + 9);
                    if (!referralCode.isEmpty()) {
                        // Store referral code for tracking
                        SharedPreferences prefs = context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("referred_by", referralCode);
                        editor.apply();
                        
                        // You can add analytics or reward logic here
                        android.util.Log.d("Referral", "User referred by: " + referralCode);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Referral", "Error handling referral link", e);
        }
    }

    /**
     * Refresh all profile data with loading indicator
     */
    private void refreshProfileData() {
        if (swipeRefreshLayout == null) {
            return;
        }
        
        Log.d(TAG, "🔄 Starting profile data refresh...");
        
        // Show loading indicator
        swipeRefreshLayout.setRefreshing(true);
        
        // Force sync from Firestore first
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        profileManager.loadFromFirestoreAndUpdateLocal(new ProfileDataManager.LoadCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "✅ Profile data synced from Firestore: " + message);
                } else {
                    Log.w(TAG, "⚠️ Failed to sync profile from Firestore: " + message);
                }
                
                // Refresh all UI components
                refreshAllProfileData();
            }
        });
    }
    
    /**
     * Refresh all profile data components
     */
    private void refreshAllProfileData() {
        try {
            // Refresh user data
            loadUserData();
            
            // Refresh profile picture
            loadProfilePicture();
            
            // Refresh verified status
            loadVerifiedStatus();
            
            // Update notification badge
            updateNotificationBadge();
            
            // Stop refresh indicator after a short delay to ensure all data is loaded
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                            Log.d(TAG, "✅ Profile refresh completed");
                        }
                    }
                }, 500); // Small delay to ensure smooth UI update
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing profile data: " + e.getMessage(), e);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Check if profile data needs sync from Firestore
            ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
            if (profileManager.needsSync()) {
                Log.d(TAG, "Profile data needs sync, loading from Firestore");
                profileManager.loadFromFirestoreAndUpdateLocal(new ProfileDataManager.LoadCallback() {
                    @Override
                    public void onComplete(boolean success, String message) {
                        if (success) {
                            Log.d(TAG, "Profile data synced from Firestore: " + message);
                            // Refresh UI with updated data
                            loadUserData();
                            loadProfilePicture();
                            loadVerifiedStatus();
                        } else {
                            Log.w(TAG, "Failed to sync profile from Firestore: " + message);
                            // Still refresh with local data
                            loadUserData();
                            loadProfilePicture();
                            loadVerifiedStatus();
                        }
                    }
                });
            } else {
                // Just refresh local data
                loadUserData();
                loadProfilePicture();
                loadVerifiedStatus();
            }
            
            updateNotificationBadge(); // Update notification badge
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
            // Fallback to basic refresh
            loadUserData();
            loadProfilePicture();
            loadVerifiedStatus();
            updateNotificationBadge();
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadgeProfile == null) return;
            
            // Use the same logic as AlertsActivity - check viewed state
            int lastViewedCount = alertsSharedPreferences.getInt(KEY_LAST_VIEWED_ANNOUNCEMENT_COUNT, 0);
            
            // Fetch current announcement count and calculate unread count
            fetchAndCountNewAnnouncementsFromProfile(lastViewedCount);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge on profile: " + e.getMessage(), e);
            if (alertsBadgeProfile != null) {
                alertsBadgeProfile.setVisibility(View.GONE);
            }
        }
    }
    
    // This method is no longer needed - using fetchAndCountNewAnnouncementsFromProfile directly
    // Keeping for backward compatibility but it's not used anymore
    
    private void fetchAndCountNewAnnouncementsFromProfile(int lastViewedCount) {
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
                            if (alertsBadgeProfile != null) {
                                if (finalUnreadCount > 0) {
                                    alertsBadgeProfile.setText(String.valueOf(finalUnreadCount));
                                    alertsBadgeProfile.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "✅ Profile badge showing: " + finalUnreadCount + " unread announcements");
                                } else {
                                    alertsBadgeProfile.setVisibility(View.GONE);
                                    alertsBadgeProfile.setText("0");
                                    Log.d(TAG, "✅ Profile badge hidden - all announcements viewed (lastViewed: " + 
                                          lastViewedCount + ", current: " + currentTotalCount + ")");
                                }
                            }
                        });
                        
                        Log.d(TAG, "Profile badge update - unreadCount: " + unreadCount + 
                                  " (current: " + currentTotalCount + ", lastViewed: " + lastViewedCount + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching announcements for badge from profile: " + e.getMessage(), e);
                    if (alertsBadgeProfile != null) {
                        runOnUiThread(() -> {
                            alertsBadgeProfile.setVisibility(View.GONE);
                        });
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching and counting new announcements from profile: " + e.getMessage(), e);
        }
    }
    
    private boolean isAnnouncementNewFromProfile(String dateStr, long lastVisitTime) {
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
            Log.e(TAG, "Error checking if announcement is new from profile: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public void onBackPressed() {
        // Handle hardware back button the same way as the back button click
        handleBackNavigation();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Unregister badge from notification manager
            AnnouncementNotificationManager.getInstance().unregisterBadge("ProfileActivity");
            Log.d(TAG, "ProfileActivity badge unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering badge in onDestroy: " + e.getMessage(), e);
        }
    }
}