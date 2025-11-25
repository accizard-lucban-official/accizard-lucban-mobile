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
import android.widget.TextView;
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
    private static final int VALID_ID_CAMERA_REQUEST_CODE = 1005;
    private static final int VALID_ID_GALLERY_REQUEST_CODE = 1006;

    private ImageView backButton, profilePicture, editPictureButton;
    private ImageView validIdImage;
    private Button saveButton;
    private TextView uploadValidIdText;
    private EditText firstNameEdit, lastNameEdit, mobileNumberEdit,
            provinceEdit, cityEdit, streetAddressEdit, birthdayEdit;
    private AutoCompleteTextView barangayEdit;
    private Spinner genderSpinner, civilStatusSpinner, religionSpinner, bloodTypeSpinner, pwdStatusSpinner, validIdTypeSpinner;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_MOBILE = "mobile_number";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_CITY = "city";
    private static final String KEY_BARANGAY = "barangay";
    private static final String KEY_STREET_ADDRESS = "street_address";

    private Bitmap newProfileBitmap;
    private Uri newProfileImageUri;
    private boolean hasNewProfilePicture = false;
    
    private Bitmap newValidIdBitmap;
    private Uri newValidIdImageUri;
    private boolean hasNewValidId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupClickListeners();
        setupBarangayAdapter();
        loadUserData();
        loadProfilePicture();
        loadValidId();
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
        barangayEdit = findViewById(R.id.barangay_edit);
        streetAddressEdit = findViewById(R.id.etStreetAddress);
        birthdayEdit = findViewById(R.id.birthday_edit);
        genderSpinner = findViewById(R.id.gender_spinner);
        civilStatusSpinner = findViewById(R.id.civil_status_spinner);
        religionSpinner = findViewById(R.id.religion_spinner);
        bloodTypeSpinner = findViewById(R.id.blood_type_spinner);
        pwdStatusSpinner = findViewById(R.id.pwd_status_spinner);
        
        // Valid ID views
        validIdImage = findViewById(R.id.valid_id_image);
        uploadValidIdText = findViewById(R.id.upload_valid_id_text);
        validIdTypeSpinner = findViewById(R.id.valid_id_type_spinner);
        
        // Setup spinners
        setupGenderSpinner();
        setupCivilStatusSpinner();
        setupReligionSpinner();
        setupBloodTypeSpinner();
        setupPWDStatusSpinner();
        setupValidIdTypeSpinner();
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

        uploadValidIdText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showValidIdImagePickerDialog();
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

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genders
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void setupCivilStatusSpinner() {
        String[] civilStatuses = {"Select Civil Status", "Single", "Married", "Divorced", "Widowed", "Separated", "Annulled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                civilStatuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        civilStatusSpinner.setAdapter(adapter);
    }

    private void setupReligionSpinner() {
        String[] religions = {"Select Religion", "Roman Catholic", "Protestant", "Islam", "Iglesia ni Cristo", "Baptist", "Methodist", "Seventh-day Adventist", "Jehovah's Witnesses", "Buddhism", "Hinduism", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                religions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        religionSpinner.setAdapter(adapter);
    }

    private void setupBloodTypeSpinner() {
        String[] bloodTypes = {"Select Blood Type", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(adapter);
    }

    private void setupPWDStatusSpinner() {
        String[] pwdStatuses = {"Select PWD Status", "PWD", "Not PWD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                pwdStatuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pwdStatusSpinner.setAdapter(adapter);
    }

    private void setupValidIdTypeSpinner() {
        String[] idTypes = {"Select Valid ID Type", "National ID (PhilID)", "Driver's License", 
                "Passport", "UMID", "SSS", "PhilHealth", "Voter's ID", "Postal ID", "Student ID", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                idTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        validIdTypeSpinner.setAdapter(adapter);
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
            String province = intent.getStringExtra("province");
            String city = intent.getStringExtra("city");
            String barangay = intent.getStringExtra("barangay");
            String streetAddress = intent.getStringExtra("streetAddress");
            
            if (firstName != null) firstNameEdit.setText(firstName);
            if (lastName != null) lastNameEdit.setText(lastName);
            if (mobileNumber != null) mobileNumberEdit.setText(mobileNumber);
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
            provinceEdit.setText(prefs.getString(KEY_PROVINCE, ""));
            cityEdit.setText(prefs.getString(KEY_CITY, ""));
            barangayEdit.setText(prefs.getString(KEY_BARANGAY, ""));
            streetAddressEdit.setText(prefs.getString(KEY_STREET_ADDRESS, ""));
            
            // Load new fields
            birthdayEdit.setText(prefs.getString("birthday", ""));
            
            // Set spinner selections
            setSpinnerSelection(genderSpinner, prefs.getString("gender", ""));
            setSpinnerSelection(civilStatusSpinner, prefs.getString("civil_status", ""));
            setSpinnerSelection(religionSpinner, prefs.getString("religion", ""));
            setSpinnerSelection(bloodTypeSpinner, prefs.getString("blood_type", ""));
            setSpinnerSelection(pwdStatusSpinner, prefs.getString("pwd_status", ""));
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
                    
                    // Load fields from Firestore
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
                    
                    // Load new fields from Firestore
                    String birthday = doc.getString("birthday");
                    String gender = doc.getString("gender");
                    String civilStatus = doc.getString("civil_status");
                    if (civilStatus == null || civilStatus.isEmpty()) {
                        civilStatus = doc.getString("civilStatus");
                    }
                    String religion = doc.getString("religion");
                    
                    // ✅ FIXED: Fetch blood_type from Firestore (field name is blood_type with underscore)
                    String bloodType = doc.getString("blood_type");
                    if (bloodType == null || bloodType.isEmpty()) {
                        // Fallback to camelCase if underscore version doesn't exist
                        bloodType = doc.getString("bloodType");
                    }
                    
                    String pwdStatus = doc.getString("pwdStatus");
                    if (pwdStatus == null || pwdStatus.isEmpty()) {
                        pwdStatus = doc.getString("pwd_status");
                    }
                    
                    String validIdType = doc.getString("validIdType");
                    
                    // Only update fields that are empty (don't overwrite existing data)
                    if (birthday != null && birthdayEdit.getText().toString().trim().isEmpty()) {
                        birthdayEdit.setText(birthday);
                    }
                    if (gender != null && genderSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(genderSpinner, gender);
                    }
                    if (civilStatus != null && civilStatusSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(civilStatusSpinner, civilStatus);
                    }
                    if (religion != null && religionSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(religionSpinner, religion);
                    }
                    if (bloodType != null && bloodTypeSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(bloodTypeSpinner, bloodType);
                    }
                    if (pwdStatus != null && pwdStatusSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(pwdStatusSpinner, pwdStatus);
                    }
                    if (validIdType != null && !validIdType.isEmpty() && validIdTypeSpinner.getSelectedItemPosition() == 0) {
                        setSpinnerSelection(validIdTypeSpinner, validIdType);
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

    /**
     * Set spinner selection by value
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null || value.isEmpty()) {
            return;
        }
        
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i).toString();
                if (value.equalsIgnoreCase(item)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }
        
        // Get form data
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String mobileNumber = mobileNumberEdit.getText().toString().trim();
        String province = provinceEdit.getText().toString().trim();
        String city = cityEdit.getText().toString().trim();
        String barangay = barangayEdit.getText().toString().trim();
        String streetAddress = streetAddressEdit.getText().toString().trim();
        String birthday = birthdayEdit.getText().toString().trim();
        String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
        String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
        String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
        String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
        String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
        String validIdType = validIdTypeSpinner.getSelectedItemPosition() > 0 ? validIdTypeSpinner.getSelectedItem().toString() : "";

        // Check mobile number uniqueness if it's being changed
        if (!mobileNumber.isEmpty()) {
            checkMobileNumberUniquenessForEdit(mobileNumber, () -> {
                // Mobile number is unique or same as current, proceed with save
                proceedWithSaveProfile(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                        birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
            }, () -> {
                // Mobile number already in use
                mobileNumberEdit.setError("This mobile number is already registered");
                mobileNumberEdit.requestFocus();
                Toast.makeText(EditProfileActivity.this, "This mobile number is already registered. Please use a different number.", Toast.LENGTH_LONG).show();
            });
        } else {
            proceedWithSaveProfile(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                    birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
        }
    }
    
    private void proceedWithSaveProfile(String firstName, String lastName, String mobileNumber,
                                       String province, String city, String barangay, String streetAddress,
                                       String birthday, String gender, String civilStatus, String religion,
                                       String bloodType, String pwdStatus, String validIdType) {
        // Save to SharedPreferences immediately for instant local updates
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        profileManager.saveProfileLocally(firstName, lastName, mobileNumber, null, province, city, barangay, streetAddress);
        
        // Save additional fields to SharedPreferences
        SharedPreferences prefs = getUserPrefs();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("birthday", birthday);
        editor.putString("gender", gender);
        editor.putString("civil_status", civilStatus);
        editor.putString("civilStatus", civilStatus);
        editor.putString("religion", religion);
        editor.putString("blood_type", bloodType);
        editor.putString("bloodType", bloodType);
        editor.putString("pwd_status", pwdStatus);
        editor.putString("pwdStatus", pwdStatus);
        editor.putBoolean("is_pwd", "PWD".equalsIgnoreCase(pwdStatus));
        editor.apply();
        
        Log.d(TAG, "Profile data saved locally, including blood_type: " + bloodType);
        Log.d(TAG, "Profile data saved locally, now syncing to Firestore...");

        // Proceed with profile sync
        proceedWithProfileSync(firstName, lastName, mobileNumber, province, city, barangay, streetAddress, 
                birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
        
        // Upload Valid ID if there's a new one
        if (hasNewValidId && newValidIdBitmap != null) {
            uploadValidId(validIdType);
        }
    }
    
    private void checkMobileNumberUniquenessForEdit(String mobileNumber, Runnable onUnique, Runnable onDuplicate) {
        // Normalize mobile number
        String normalizedMobile = normalizeMobileNumber(mobileNumber);
        
        // Get current user's mobile number to allow keeping the same number
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            onUnique.run();
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .whereEqualTo("firebaseUid", user.getUid())
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                String currentMobileNumber = null;
                String currentPhoneNumber = null;
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    currentMobileNumber = doc.getString("mobileNumber");
                    currentPhoneNumber = doc.getString("phoneNumber");
                }
                
                // Normalize current numbers for comparison
                String normalizedCurrentMobile = currentMobileNumber != null ? normalizeMobileNumber(currentMobileNumber) : null;
                String normalizedCurrentPhone = currentPhoneNumber != null ? normalizeMobileNumber(currentPhoneNumber) : null;
                
                // If the new number is the same as current, allow it
                if (normalizedMobile.equals(normalizedCurrentMobile) || normalizedMobile.equals(normalizedCurrentPhone)) {
                    onUnique.run();
                    return;
                }
                
                // Check if mobile number is already in use by another user
                db.collection("users")
                    .whereEqualTo("mobileNumber", normalizedMobile)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots2 -> {
                        if (!queryDocumentSnapshots2.isEmpty()) {
                            // Check if it's the same user
                            QueryDocumentSnapshot doc2 = (QueryDocumentSnapshot) queryDocumentSnapshots2.getDocuments().get(0);
                            String docUid = doc2.getString("firebaseUid");
                            if (docUid != null && docUid.equals(user.getUid())) {
                                onUnique.run();
                            } else {
                                onDuplicate.run();
                            }
                        } else {
                            // Also check phoneNumber field
                            db.collection("users")
                                .whereEqualTo("phoneNumber", normalizedMobile)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                    if (!queryDocumentSnapshots3.isEmpty()) {
                                        QueryDocumentSnapshot doc3 = (QueryDocumentSnapshot) queryDocumentSnapshots3.getDocuments().get(0);
                                        String docUid3 = doc3.getString("firebaseUid");
                                        if (docUid3 != null && docUid3.equals(user.getUid())) {
                                            onUnique.run();
                                        } else {
                                            onDuplicate.run();
                                        }
                                    } else {
                                        onUnique.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking phoneNumber uniqueness", e);
                                    // On error, allow update to proceed
                                    onUnique.run();
                                });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking mobileNumber uniqueness", e);
                        // On error, allow update to proceed
                        onUnique.run();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting current user data", e);
                // On error, allow update to proceed
                onUnique.run();
            });
    }
    
    private String normalizeMobileNumber(String mobileNumber) {
        // Remove spaces, dashes, and normalize format
        String normalized = mobileNumber.replaceAll("[\\s-]", "");
        
        // Convert to standard format (09XXXXXXXXX)
        if (normalized.startsWith("+63")) {
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("63")) {
            normalized = "0" + normalized.substring(2);
        } else if (!normalized.startsWith("0")) {
            normalized = "0" + normalized;
        }
        
        return normalized;
    }

    private void proceedWithProfileSync(String firstName, String lastName, String mobileNumber,
                                       String province, String city, String barangay, String streetAddress,
                                       String birthday, String gender, String civilStatus, String religion, 
                                       String bloodType, String pwdStatus, String validIdType) {
        // If there's a new profile picture, upload it first, then sync profile data
        if (hasNewProfilePicture) {
            uploadNewProfilePicture(validIdType);
        } else {
            // Sync profile data to Firestore
            syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                    birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
        }
    }


    private void syncProfileToFirestore(String firstName, String lastName, String mobileNumber,
                                       String province, String city, String barangay, String streetAddress,
                                       String birthday, String gender, String civilStatus, String religion,
                                       String bloodType, String pwdStatus, String validIdType) {
        ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
        
        profileManager.syncToFirestore(firstName, lastName, mobileNumber, null, province, city, barangay, streetAddress,
            new ProfileDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    // After basic profile sync, sync additional fields
                    syncAdditionalFieldsToFirestore(birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
                    
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
                    // Still try to sync additional fields
                    syncAdditionalFieldsToFirestore(birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
                    
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

    /**
     * Sync additional profile fields (birthday, gender, civil_status, religion, blood_type, pwd_status, validIdType) to Firestore
     */
    private void syncAdditionalFieldsToFirestore(String birthday, String gender, String civilStatus, 
                                                 String religion, String bloodType, String pwdStatus, String validIdType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user signed in, skipping additional fields sync");
            return;
        }

        try {
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
                        
                        Map<String, Object> updates = new HashMap<>();
                        
                        // Add additional fields if they have values
                        if (birthday != null && !birthday.trim().isEmpty()) {
                            updates.put("birthday", birthday.trim());
                        }
                        if (gender != null && !gender.trim().isEmpty() && !gender.equals("Select Gender")) {
                            updates.put("gender", gender.trim());
                        }
                        if (civilStatus != null && !civilStatus.trim().isEmpty() && !civilStatus.equals("Select Civil Status")) {
                            updates.put("civil_status", civilStatus.trim());
                            updates.put("civilStatus", civilStatus.trim()); // Also save as camelCase for compatibility
                        }
                        if (religion != null && !religion.trim().isEmpty()) {
                            updates.put("religion", religion.trim());
                        }
                        if (bloodType != null && !bloodType.trim().isEmpty() && !bloodType.equals("Select Blood Type")) {
                            // ✅ FIXED: Save blood_type with underscore as primary field in Firestore
                            String trimmedBloodType = bloodType.trim();
                            updates.put("blood_type", trimmedBloodType);
                            updates.put("bloodType", trimmedBloodType); // Also save as camelCase for compatibility
                            Log.d(TAG, "Syncing blood type to Firestore: " + trimmedBloodType);
                        }
                        if (pwdStatus != null && !pwdStatus.trim().isEmpty() && !pwdStatus.equals("Select PWD Status")) {
                            updates.put("pwdStatus", pwdStatus.trim());
                            updates.put("pwd_status", pwdStatus.trim()); // Also save with underscore for compatibility
                            updates.put("isPWD", "PWD".equalsIgnoreCase(pwdStatus.trim()));
                        }
                        if (validIdType != null && !validIdType.trim().isEmpty() && !validIdType.equals("Select Valid ID Type")) {
                            updates.put("validIdType", validIdType.trim());
                            Log.d(TAG, "Syncing valid ID type to Firestore: " + validIdType.trim());
                        }
                        
                        if (!updates.isEmpty()) {
                            updates.put("lastUpdated", System.currentTimeMillis());
                            
                            Log.d(TAG, "Syncing additional fields to Firestore: " + updates.toString());
                            
                            db.collection("users").document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Additional profile fields synced to Firestore successfully, including blood_type: " + 
                                        (updates.containsKey("blood_type") ? updates.get("blood_type") : "not included"));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error syncing additional fields to Firestore", e);
                                });
                        } else {
                            Log.d(TAG, "No additional fields to sync (all fields are empty or default)");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying user document for additional fields sync", e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in syncAdditionalFieldsToFirestore", e);
        }
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
        
        // Mobile number uniqueness will be checked asynchronously before saving

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

    private void showValidIdImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Valid ID");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (checkCameraPermission()) {
                        openValidIdCamera();
                    } else {
                        requestCameraPermission();
                    }
                    break;
                case 1:
                    if (checkStoragePermission()) {
                        openValidIdGallery();
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

    private void openValidIdCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, VALID_ID_CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openValidIdGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, VALID_ID_GALLERY_REQUEST_CODE);
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
            } else if (requestCode == VALID_ID_CAMERA_REQUEST_CODE) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    validIdImage.setImageBitmap(bitmap);
                    validIdImage.setVisibility(View.VISIBLE);
                    newValidIdBitmap = bitmap;
                    hasNewValidId = true;
                    Toast.makeText(this, "Valid ID captured successfully", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == VALID_ID_GALLERY_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            validIdImage.setImageBitmap(bitmap);
                            validIdImage.setVisibility(View.VISIBLE);
                            newValidIdBitmap = bitmap;
                            hasNewValidId = true;
                            Toast.makeText(this, "Valid ID selected successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading Valid ID image from gallery", e);
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

    private void uploadNewProfilePicture(String validIdType) {
        if (!hasNewProfilePicture) {
            // No new picture, just sync profile data
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String mobileNumber = mobileNumberEdit.getText().toString().trim();
            String province = provinceEdit.getText().toString().trim();
            String city = cityEdit.getText().toString().trim();
            String barangay = barangayEdit.getText().toString().trim();
            String streetAddress = streetAddressEdit.getText().toString().trim();
            String birthday = birthdayEdit.getText().toString().trim();
            String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
            String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
            String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
            String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
            String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
            syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                    birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
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
                                        String province = provinceEdit.getText().toString().trim();
                                        String city = cityEdit.getText().toString().trim();
                                        String barangay = barangayEdit.getText().toString().trim();
                                        String streetAddress = streetAddressEdit.getText().toString().trim();
                                        String birthday = birthdayEdit.getText().toString().trim();
                                        String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
                                        String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
                                        String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
                                        String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
                                        String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
                                        // Use the validIdType parameter from the outer method
                                        syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                                                birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
                                    }
                                    
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "Failed to sync profile picture URL to Firestore", e);
                                        // Still proceed with profile data sync
                                        String firstName = firstNameEdit.getText().toString().trim();
                                        String lastName = lastNameEdit.getText().toString().trim();
                                        String mobileNumber = mobileNumberEdit.getText().toString().trim();
                                        String province = provinceEdit.getText().toString().trim();
                                        String city = cityEdit.getText().toString().trim();
                                        String barangay = barangayEdit.getText().toString().trim();
                                        String streetAddress = streetAddressEdit.getText().toString().trim();
                                        String birthday = birthdayEdit.getText().toString().trim();
                                        String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
                                        String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
                                        String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
                                        String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
                                        String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
                                        // Use the validIdType parameter from the outer method
                                        syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                                                birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
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
                            String province = provinceEdit.getText().toString().trim();
                            String city = cityEdit.getText().toString().trim();
                            String barangay = barangayEdit.getText().toString().trim();
                            String streetAddress = streetAddressEdit.getText().toString().trim();
                            String birthday = birthdayEdit.getText().toString().trim();
                            String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
                            String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
                            String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
                            String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
                            String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
                            // Use the validIdType parameter from the outer method
                            syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                                    birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
                        }
                    });
        } else {
            // No bitmap available, just sync profile data
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String mobileNumber = mobileNumberEdit.getText().toString().trim();
            String province = provinceEdit.getText().toString().trim();
            String city = cityEdit.getText().toString().trim();
            String barangay = barangayEdit.getText().toString().trim();
            String streetAddress = streetAddressEdit.getText().toString().trim();
            String birthday = birthdayEdit.getText().toString().trim();
            String gender = genderSpinner.getSelectedItemPosition() > 0 ? genderSpinner.getSelectedItem().toString() : "";
            String civilStatus = civilStatusSpinner.getSelectedItemPosition() > 0 ? civilStatusSpinner.getSelectedItem().toString() : "";
            String religion = religionSpinner.getSelectedItemPosition() > 0 ? religionSpinner.getSelectedItem().toString() : "";
            String bloodType = bloodTypeSpinner.getSelectedItemPosition() > 0 ? bloodTypeSpinner.getSelectedItem().toString() : "";
            String pwdStatus = pwdStatusSpinner.getSelectedItemPosition() > 0 ? pwdStatusSpinner.getSelectedItem().toString() : "";
            // Use the validIdType parameter from the outer method
            syncProfileToFirestore(firstName, lastName, mobileNumber, province, city, barangay, streetAddress,
                    birthday, gender, civilStatus, religion, bloodType, pwdStatus, validIdType);
        }
    }

    private void loadValidId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && validIdImage != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("firebaseUid", user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String validIdUrl = doc.getString("validIdUrl");
                        String validIdType = doc.getString("validIdType");
                        
                        Log.d(TAG, "Found Valid ID URL: " + validIdUrl);
                        Log.d(TAG, "Found Valid ID Type: " + validIdType);
                        
                        // Set valid ID type in spinner if available
                        if (validIdType != null && !validIdType.isEmpty() && validIdTypeSpinner != null) {
                            setSpinnerSelection(validIdTypeSpinner, validIdType);
                        }
                        
                        if (validIdUrl != null && !validIdUrl.isEmpty()) {
                            loadValidIdFromUrl(validIdUrl);
                        } else {
                            Log.d(TAG, "No Valid ID URL found in Firestore");
                            // Try to check if Valid ID exists in Firebase Storage
                            checkValidIdInStorage(user.getUid());
                        }
                    } else {
                        Log.d(TAG, "No user document found for firebaseUid: " + user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Valid ID", e);
                });
        }
    }

    private void checkValidIdInStorage(String firebaseUid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference validIdRef = storage.getReference().child("valid_ids/" + firebaseUid + "/id.jpg");
        
        validIdRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "Found Valid ID in Storage: " + uri.toString());
            loadValidIdFromUrl(uri.toString());
            // Update Firestore with the found URL
            updateValidIdUrlInFirestore(uri.toString());
        }).addOnFailureListener(e -> {
            Log.d(TAG, "No Valid ID found in Storage for UID: " + firebaseUid);
        });
    }

    private void loadValidIdFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && validIdImage != null) {
                        validIdImage.setImageBitmap(bitmap);
                        validIdImage.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading Valid ID from URL", e);
            }
        }).start();
    }

    private void updateValidIdUrlInFirestore(String validIdUrl) {
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
                    updates.put("validIdUrl", validIdUrl);
                    
                    db.collection("users").document(docId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Valid ID URL updated in Firestore");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating Valid ID URL in Firestore", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error querying user document for Valid ID update", e);
            });
    }

    private void uploadValidId(String validIdType) {
        if (!hasNewValidId || newValidIdBitmap == null) {
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        StorageHelper.uploadValidIdImage(userId, newValidIdBitmap,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        Log.d(TAG, "Valid ID uploaded successfully: " + downloadUrl);
                        // Update Firestore with Valid ID URL and type
                        updateValidIdInFirestore(downloadUrl, validIdType);
                        Toast.makeText(EditProfileActivity.this, "Valid ID uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error uploading Valid ID", e);
                        Toast.makeText(EditProfileActivity.this, "Failed to upload Valid ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateValidIdInFirestore(String validIdUrl, String validIdType) {
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
                    updates.put("validIdUrl", validIdUrl);
                    
                    // Also update valid ID type if provided
                    if (validIdType != null && !validIdType.trim().isEmpty() && !validIdType.equals("Select Valid ID Type")) {
                        updates.put("validIdType", validIdType.trim());
                        Log.d(TAG, "Updating valid ID type: " + validIdType.trim());
                    }
                    
                    db.collection("users").document(docId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Valid ID URL and type updated in Firestore");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating Valid ID in Firestore", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error querying user document for Valid ID update", e);
                });
    }

    // Old methods removed - now using ProfileDataManager for all sync operations
}