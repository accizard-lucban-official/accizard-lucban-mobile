package com.example.accizardlucban;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * ProfileDataManager handles all profile data synchronization between
 * SharedPreferences (local) and Firestore (cloud)
 */
public class ProfileDataManager {
    
    private static final String TAG = "ProfileDataManager";
    private static final String PREFS_NAME = "user_profile_prefs";
    
    // SharedPreferences keys
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_MOBILE_NUMBER = "mobile_number";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_PROVINCE = "province";
    public static final String KEY_CITY = "city";
    public static final String KEY_BARANGAY = "barangay";
    public static final String KEY_STREET_ADDRESS = "street_address";
    public static final String KEY_LOCATION_TEXT = "location_text";
    public static final String KEY_MAILING_ADDRESS = "mailing_address";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROFILE_PICTURE_URL = "profile_picture_url";
    public static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    
    private static ProfileDataManager instance;
    private Context context;
    private SharedPreferences prefs;
    private FirebaseFirestore db;
    
    private ProfileDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized ProfileDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileDataManager(context);
        }
        return instance;
    }
    
    /**
     * Save profile data to SharedPreferences (immediate local update)
     * Now supports email, barangay, and street address fields
     */
    public void saveProfileLocally(String firstName, String lastName, String mobileNumber, 
                                  String email, String province, String city, String barangay, String streetAddress) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            
            // Save individual fields
            editor.putString(KEY_FIRST_NAME, firstName);
            editor.putString(KEY_LAST_NAME, lastName);
            editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
            editor.putString(KEY_PHONE_NUMBER, mobileNumber); // Also save as phone_number for compatibility
            if (email != null && !email.isEmpty()) {
                editor.putString(KEY_EMAIL, email);
            }
            editor.putString(KEY_PROVINCE, province);
            editor.putString(KEY_CITY, city);
            if (barangay != null && !barangay.isEmpty()) {
                editor.putString(KEY_BARANGAY, barangay);
            }
            if (streetAddress != null && !streetAddress.isEmpty()) {
                editor.putString(KEY_STREET_ADDRESS, streetAddress);
            }
            
            // Save computed fields
            String fullName = (firstName + " " + lastName).trim();
            editor.putString(KEY_FULL_NAME, fullName);
            
            // Build mailing address with street address if provided
            String mailingAddress;
            if (streetAddress != null && !streetAddress.isEmpty()) {
                mailingAddress = streetAddress + ", " + barangay + ", " + city + ", " + province;
            } else if (barangay != null && !barangay.isEmpty()) {
                mailingAddress = barangay + ", " + city + ", " + province;
            } else {
                mailingAddress = city + ", " + province;
            }
            editor.putString(KEY_MAILING_ADDRESS, mailingAddress);
            
            // Update location_text with barangay if available
            if (barangay != null && !barangay.isEmpty()) {
                String fullLocation = city + ", " + barangay;
                editor.putString(KEY_LOCATION_TEXT, fullLocation);
                Log.d(TAG, "Updated location_text with barangay: " + fullLocation);
            } else if (city != null && !city.isEmpty() && province != null && !province.isEmpty()) {
                // Fallback to city, province if no barangay
                editor.putString(KEY_LOCATION_TEXT, city + ", " + province);
            }
            
            // Update sync timestamp
            editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis());
            
            editor.apply();
            
            Log.d(TAG, "Profile data saved locally: " + fullName + " from " + mailingAddress);
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile data locally: " + e.getMessage(), e);
        }
    }
    
    /**
     * Overloaded method for backward compatibility (without email, barangay, and street address)
     */
    public void saveProfileLocally(String firstName, String lastName, String mobileNumber, 
                                  String email, String province, String city, String barangay) {
        saveProfileLocally(firstName, lastName, mobileNumber, email, province, city, barangay, "");
    }
    
    /**
     * Overloaded method for backward compatibility (without email and barangay)
     */
    public void saveProfileLocally(String firstName, String lastName, String mobileNumber, 
                                  String province, String city) {
        saveProfileLocally(firstName, lastName, mobileNumber, "", province, city, "", "");
    }
    
    /**
     * Save profile picture URL locally
     */
    public void saveProfilePictureUrlLocally(String profilePictureUrl) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_PROFILE_PICTURE_URL, profilePictureUrl);
            editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis());
            editor.apply();
            
            Log.d(TAG, "Profile picture URL saved locally: " + profilePictureUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile picture URL locally: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get full name from SharedPreferences
     */
    public String getFullName() {
        try {
            String firstName = prefs.getString(KEY_FIRST_NAME, "");
            String lastName = prefs.getString(KEY_LAST_NAME, "");
            
            StringBuilder sb = new StringBuilder();
            if (firstName != null && !firstName.trim().isEmpty()) {
                sb.append(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(lastName.trim());
            }
            
            String fullName = sb.toString();
            Log.d(TAG, "Retrieved full name: " + fullName);
            return fullName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting full name: " + e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Get location text for MainDashboard display
     */
    public String getLocationText() {
        try {
            // Priority 1: location_text (most specific)
            String locationText = prefs.getString(KEY_LOCATION_TEXT, "");
            if (locationText != null && !locationText.isEmpty()) {
                return locationText;
            }
            
            // Priority 2: mailing_address
            String mailingAddress = prefs.getString(KEY_MAILING_ADDRESS, "");
            if (mailingAddress != null && !mailingAddress.isEmpty()) {
                return mailingAddress;
            }
            
            // Priority 3: city + province
            String city = prefs.getString(KEY_CITY, "");
            String province = prefs.getString(KEY_PROVINCE, "");
            if (city != null && !city.isEmpty() && province != null && !province.isEmpty()) {
                return city + ", " + province;
            }
            
            // Priority 4: barangay (fallback)
            String barangay = prefs.getString(KEY_BARANGAY, "");
            if (barangay != null && !barangay.isEmpty()) {
                return barangay;
            }
            
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting location text: " + e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Get profile picture URL
     */
    public String getProfilePictureUrl() {
        return prefs.getString(KEY_PROFILE_PICTURE_URL, "");
    }
    
    /**
     * Get mobile number
     */
    public String getMobileNumber() {
        String mobile = prefs.getString(KEY_MOBILE_NUMBER, "");
        if (mobile.isEmpty()) {
            mobile = prefs.getString(KEY_PHONE_NUMBER, "");
        }
        return mobile;
    }
    
    /**
     * Get province
     */
    public String getProvince() {
        return prefs.getString(KEY_PROVINCE, "");
    }
    
    /**
     * Get city
     */
    public String getCity() {
        return prefs.getString(KEY_CITY, "");
    }
    
    /**
     * Sync profile data to Firestore
     * Now supports email, barangay, and street address fields
     */
    public void syncToFirestore(String firstName, String lastName, String mobileNumber,
                               String email, String province, String city, String barangay, String streetAddress, SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user signed in, skipping Firestore sync");
            if (callback != null) callback.onSuccess();
            return;
        }
        
        try {
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
                        userProfile.put("fullName", (firstName + " " + lastName).trim());
                        userProfile.put("mobileNumber", mobileNumber);
                        userProfile.put("phoneNumber", mobileNumber); // Also save as phoneNumber for compatibility
                        
                        // Update email if provided (and also update Firebase Auth email)
                        if (email != null && !email.isEmpty()) {
                            userProfile.put("email", email);
                            // Update Firebase Auth email
                            user.updateEmail(email)
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Failed to update Firebase Auth email: " + e.getMessage());
                                });
                        }
                        
                        userProfile.put("province", province);
                        userProfile.put("city", city);
                        userProfile.put("cityTown", city); // Also save as cityTown for compatibility
                        
                        // Update barangay if provided
                        if (barangay != null && !barangay.isEmpty()) {
                            userProfile.put("barangay", barangay);
                        }
                        
                        // Update street address if provided
                        if (streetAddress != null && !streetAddress.isEmpty()) {
                            userProfile.put("streetAddress", streetAddress);
                        }
                        
                        // Build mailing address with street address if provided
                        String mailingAddress;
                        if (streetAddress != null && !streetAddress.isEmpty()) {
                            mailingAddress = streetAddress + ", " + barangay + ", " + city + ", " + province;
                        } else if (barangay != null && !barangay.isEmpty()) {
                            mailingAddress = barangay + ", " + city + ", " + province;
                        } else {
                            mailingAddress = city + ", " + province;
                        }
                        userProfile.put("mailing_address", mailingAddress);
                        userProfile.put("address", mailingAddress); // Also save as address for compatibility
                        
                        // Update location display format
                        if (barangay != null && !barangay.isEmpty()) {
                            userProfile.put("location_text", city + ", " + barangay);
                        } else {
                            userProfile.put("location_text", city + ", " + province);
                        }
                        
                        userProfile.put("lastUpdated", System.currentTimeMillis());
                        
                        db.collection("users").document(docId)
                            .update(userProfile)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Profile data synced to Firestore successfully");
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating profile in Firestore: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            });
                    } else {
                        Log.w(TAG, "User document not found in Firestore");
                        if (callback != null) callback.onFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying user document: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error syncing profile to Firestore: " + e.getMessage(), e);
            if (callback != null) callback.onFailure(e);
        }
    }
    
    /**
     * Overloaded method for backward compatibility (without email, barangay, and street address)
     */
    public void syncToFirestore(String firstName, String lastName, String mobileNumber,
                               String email, String province, String city, String barangay, SyncCallback callback) {
        syncToFirestore(firstName, lastName, mobileNumber, email, province, city, barangay, "", callback);
    }
    
    /**
     * Overloaded method for backward compatibility (without email and barangay)
     */
    public void syncToFirestore(String firstName, String lastName, String mobileNumber,
                               String province, String city, SyncCallback callback) {
        syncToFirestore(firstName, lastName, mobileNumber, "", province, city, "", "", callback);
    }
    
    /**
     * Sync profile picture URL to Firestore
     */
    public void syncProfilePictureUrlToFirestore(String profilePictureUrl, SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user signed in, skipping profile picture sync");
            if (callback != null) callback.onSuccess();
            return;
        }
        
        try {
            String firebaseUid = user.getUid();
            db.collection("users")
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String docId = doc.getId();
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profilePictureUrl", profilePictureUrl);
                        updates.put("lastUpdated", System.currentTimeMillis());
                        
                        db.collection("users").document(docId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Profile picture URL synced to Firestore successfully");
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating profile picture URL in Firestore: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            });
                    } else {
                        Log.w(TAG, "User document not found in Firestore");
                        if (callback != null) callback.onFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying user document for profile picture sync: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error syncing profile picture URL to Firestore: " + e.getMessage(), e);
            if (callback != null) callback.onFailure(e);
        }
    }
    
    /**
     * Load profile data from Firestore and update local SharedPreferences
     */
    public void loadFromFirestoreAndUpdateLocal(LoadCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user signed in, skipping Firestore load");
            if (callback != null) callback.onComplete(false, "No user signed in");
            return;
        }
        
        try {
            String firebaseUid = user.getUid();
            db.collection("users")
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        
                        // Extract data from Firestore
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String mobileNumber = doc.getString("mobileNumber");
                        if (mobileNumber == null) mobileNumber = doc.getString("phoneNumber");
                        String province = doc.getString("province");
                        String city = doc.getString("city");
                        String profilePictureUrl = doc.getString("profilePictureUrl");
                        
                        // Update local SharedPreferences
                        if (firstName != null && lastName != null) {
                            saveProfileLocally(firstName, lastName, mobileNumber != null ? mobileNumber : "", 
                                             province != null ? province : "", city != null ? city : "");
                        }
                        
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            saveProfilePictureUrlLocally(profilePictureUrl);
                        }
                        
                        Log.d(TAG, "Profile data loaded from Firestore and updated locally");
                        if (callback != null) callback.onComplete(true, "Profile data loaded successfully");
                    } else {
                        Log.w(TAG, "User document not found in Firestore");
                        if (callback != null) callback.onComplete(false, "User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading profile from Firestore: " + e.getMessage(), e);
                    if (callback != null) callback.onComplete(false, e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile from Firestore: " + e.getMessage(), e);
            if (callback != null) callback.onComplete(false, e.getMessage());
        }
    }
    
    /**
     * Check if profile data needs sync (based on timestamp)
     */
    public boolean needsSync() {
        long lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long syncInterval = 24 * 60 * 60 * 1000; // 24 hours
        
        return (currentTime - lastSyncTime) > syncInterval;
    }
    
    /**
     * Clear all profile data (for logout)
     */
    public void clearProfileData() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Log.d(TAG, "Profile data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing profile data: " + e.getMessage(), e);
        }
    }
    
    // Callback interfaces
    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    public interface LoadCallback {
        void onComplete(boolean success, String message);
    }
}
