package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class AddressInfoActivity extends AppCompatActivity {

    private AutoCompleteTextView actvProvince, actvCityTown, actvBarangay;
    private EditText etStreetAddress;
    private Button btnNext;
    private ImageButton btnBack;
    private String firstName, lastName, mobileNumber, email, password;
    private View layoutBarangay, layoutBarangayOther;

    // Use separate SharedPreferences for registration process (don't mix with user profile!)
    private static final String PREFS_NAME = "registration_data";
    private static final String KEY_BARANGAY = "barangay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_info);

        initializeViews();
        setupAutoCompleteFields();
        setupBarangaySpinner();
        getIntentData();
        setupFieldWatchers();
        updateBarangayAdapter(); // Initialize barangay adapter
        updateBarangayVisibility(); // Initialize visibility state
        restoreAddressData(); // Restore previously saved address data (after field watchers)
        setupClickListeners();
    }

    private void initializeViews() {
        actvProvince = findViewById(R.id.actvProvince);
        actvCityTown = findViewById(R.id.actvCityTown);
        actvBarangay = findViewById(R.id.spinnerBarangay);
        etStreetAddress = findViewById(R.id.etStreetAddress);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        layoutBarangay = findViewById(R.id.layoutBarangay);
        layoutBarangayOther = findViewById(R.id.layoutBarangayOther);
    }

    private void setupAutoCompleteFields() {
        actvProvince.setThreshold(1); // Show suggestions from the first letter
        actvCityTown.setThreshold(1); // Show suggestions from the first letter

        String[] provinces = getResources().getStringArray(R.array.province_list);
        InitialLetterAutoCompleteAdapter provinceAdapter = new InitialLetterAutoCompleteAdapter(this, provinces);
        actvProvince.setAdapter(provinceAdapter);
    }

    private void setupBarangaySpinner() {
        actvBarangay.setThreshold(1); // Show suggestions from the first letter
        
        // Default: no adapter (free text input for non-Lucban users)
        actvBarangay.setAdapter(null);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        mobileNumber = intent.getStringExtra("mobileNumber");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
    }

    private void setupFieldWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBarangayVisibility();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        actvProvince.addTextChangedListener(watcher);
        actvCityTown.addTextChangedListener(watcher);

        actvProvince.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("Quezon".equalsIgnoreCase(s.toString().trim())) {
                    String[] quezonCities = getResources().getStringArray(R.array.quezon_cities_municipalities);
                    InitialLetterAutoCompleteAdapter cityAdapter = new InitialLetterAutoCompleteAdapter(AddressInfoActivity.this, quezonCities);
                    actvCityTown.setAdapter(cityAdapter);
                } else {
                    actvCityTown.setAdapter(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Add watcher to city/town to update barangay adapter
        actvCityTown.addTextChangedListener(new TextWatcher() {
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

    private void updateBarangayAdapter() {
        String selectedProvince = actvProvince.getText().toString().trim();
        String cityTown = actvCityTown.getText().toString().trim();
        
        android.util.Log.d("AddressInfo", "Updating barangay adapter - Province: " + selectedProvince + ", City: " + cityTown);
        
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
            actvBarangay.setAdapter(adapter);
            android.util.Log.d("AddressInfo", "Lucban barangay adapter set");
        } else {
            // Allow free text input for other locations
            actvBarangay.setAdapter(null);
            android.util.Log.d("AddressInfo", "Free text input enabled for other locations");
        }
    }
    
    private void updateBarangayVisibility() {
        android.util.Log.d("AddressInfo", "Updating barangay visibility");
        
        // Always show barangay layout
        layoutBarangay.setVisibility(View.VISIBLE);
        layoutBarangayOther.setVisibility(View.GONE);
        
        android.util.Log.d("AddressInfo", "Barangay layout always visible");
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("AddressInfo", "Next button clicked");
                try {
                    if (validateInputs()) {
                        android.util.Log.d("AddressInfo", "Validation passed, proceeding...");
                        saveAddressData(); // Save current data before proceeding
                        proceedToProfilePicture();
                    } else {
                        android.util.Log.w("AddressInfo", "Validation failed");
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddressInfo", "Error in Next button click", e);
                    e.printStackTrace();
                    Toast.makeText(AddressInfoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("AddressInfo", "Back button clicked");
                try {
                    saveAddressData(); // Save current data before going back
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("AddressInfo", "Error in Back button click", e);
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(actvProvince.getText().toString().trim())) {
            actvProvince.setError("Province is required");
            actvProvince.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(actvCityTown.getText().toString().trim())) {
            actvCityTown.setError("City/Town is required");
            actvCityTown.requestFocus();
            return false;
        }
        
        // Validate barangay field
        if (TextUtils.isEmpty(actvBarangay.getText().toString().trim())) {
            actvBarangay.setError("Barangay is required");
            actvBarangay.requestFocus();
            return false;
        }
        
        return true;
    }

    private void proceedToProfilePicture() {
        try {
            android.util.Log.d("AddressInfo", "proceedToProfilePicture() started");
            
            String selectedProvince = actvProvince.getText().toString().trim();
            String cityTown = actvCityTown.getText().toString().trim();
            String barangay = actvBarangay.getText().toString().trim();
            String streetAddress = etStreetAddress.getText().toString().trim();
            
            android.util.Log.d("AddressInfo", "Selected - Province: " + selectedProvince + ", City: " + cityTown + ", Barangay: " + barangay + ", Street: " + streetAddress);
            
            // Format barangay for display (add "Brgy." prefix if not present)
            String formattedBarangay = barangay;
            if (!barangay.toLowerCase().startsWith("brgy") && !barangay.toLowerCase().startsWith("barangay")) {
                formattedBarangay = "Brgy. " + barangay;
            }
            
            // Build complete mailing address
            // Format: Street Address (if provided), Barangay, Town, Province
            String mailingAddress;
            if (!streetAddress.isEmpty()) {
                mailingAddress = streetAddress + ", " + formattedBarangay + ", " + cityTown + ", " + selectedProvince;
            } else {
                mailingAddress = formattedBarangay + ", " + cityTown + ", " + selectedProvince;
            }
            
            android.util.Log.d("AddressInfo", "Formatted address - Street: " + streetAddress + ", Barangay: " + formattedBarangay + ", Mailing: " + mailingAddress);

            // ⚠️ DO NOT save to user_profile_prefs during registration!
            // This is just registration data - only save to registration_data for data retention
            // The actual user profile will be created in ValidIdActivity after successful registration

            android.util.Log.d("AddressInfo", "Creating intent for ProfilePictureActivity");
            Intent intent = new Intent(AddressInfoActivity.this, ProfilePictureActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("province", selectedProvince);
            intent.putExtra("cityTown", cityTown);
            intent.putExtra("barangay", formattedBarangay);
            intent.putExtra("streetAddress", streetAddress);
            intent.putExtra("mailingAddress", mailingAddress);
            
            android.util.Log.d("AddressInfo", "Starting ProfilePictureActivity...");
            startActivity(intent);
            android.util.Log.d("AddressInfo", "✅ ProfilePictureActivity started successfully");
        } catch (Exception e) {
            android.util.Log.e("AddressInfo", "Error in proceedToProfilePicture", e);
            e.printStackTrace();
            Toast.makeText(this, "Error proceeding: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ⚠️ REMOVED: Do not update Firestore during registration process
     * The user account doesn't exist yet - this will be handled in ValidIdActivity
     * after successful account creation.
     */
    // private void updateFirestoreAddressInfo(...) { ... }

    /**
     * Restores address data from SharedPreferences
     */
    private void restoreAddressData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            
            android.util.Log.d("AddressInfo", "Attempting to restore address data...");
            
            // Restore province
            String savedProvince = prefs.getString("saved_province", null);
            if (savedProvince != null && !savedProvince.isEmpty()) {
                actvProvince.setText(savedProvince);
                android.util.Log.d("AddressInfo", "Province restored: " + savedProvince);
            }
            
            // Restore city/town (this will trigger the text watcher to update city adapter)
            String savedCityTown = prefs.getString("saved_city_town", null);
            if (savedCityTown != null && !savedCityTown.isEmpty()) {
                actvCityTown.setText(savedCityTown);
                android.util.Log.d("AddressInfo", "City/Town restored: " + savedCityTown);
            }
            
            // Restore street address
            String savedStreetAddress = prefs.getString("saved_street_address", null);
            if (savedStreetAddress != null && !savedStreetAddress.isEmpty()) {
                etStreetAddress.setText(savedStreetAddress);
                android.util.Log.d("AddressInfo", "Street address restored: " + savedStreetAddress);
            } else {
                // Explicitly clear field if no saved data
                etStreetAddress.setText("");
                android.util.Log.d("AddressInfo", "No saved street address found, field cleared");
            }
            
            // Restore barangay (delayed to ensure visibility is set correctly)
            String savedBarangay = prefs.getString("saved_barangay", null);
            if (savedBarangay != null && !savedBarangay.isEmpty()) {
                // Post to ensure text watchers have completed
                final String barangayToRestore = savedBarangay;
                actvCityTown.post(new Runnable() {
                    @Override
                    public void run() {
                        restoreBarangaySelection(barangayToRestore);
                    }
                });
                android.util.Log.d("AddressInfo", "Barangay will be restored: " + savedBarangay);
            }
            
            // Update visibility based on restored data
            updateBarangayVisibility();
            
            android.util.Log.d("AddressInfo", "✅ Address data restored from SharedPreferences");
        } catch (Exception e) {
            android.util.Log.e("AddressInfo", "Error restoring address data", e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to restore barangay selection
     */
    private void restoreBarangaySelection(String savedBarangay) {
        try {
            // Simply set the text in the AutoCompleteTextView
            actvBarangay.setText(savedBarangay);
            android.util.Log.d("AddressInfo", "Barangay restored: " + savedBarangay);
        } catch (Exception e) {
            android.util.Log.e("AddressInfo", "Error restoring barangay selection", e);
        }
    }

    /**
     * Saves address data to SharedPreferences for data retention
     */
    private void saveAddressData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Save current form data
            String province = actvProvince.getText().toString().trim();
            String cityTown = actvCityTown.getText().toString().trim();
            String barangay = actvBarangay.getText().toString().trim();
            String streetAddress = etStreetAddress.getText().toString().trim();
            
            editor.putString("saved_province", province);
            editor.putString("saved_city_town", cityTown);
            editor.putString("saved_barangay", barangay);
            editor.putString("saved_street_address", streetAddress);
            
            editor.apply();
            android.util.Log.d("AddressInfo", "✅ Address data saved to SharedPreferences");
            android.util.Log.d("AddressInfo", "Province: " + province + ", City: " + cityTown + ", Barangay: " + barangay + ", Street: " + streetAddress);
        } catch (Exception e) {
            android.util.Log.e("AddressInfo", "Error saving address data", e);
            e.printStackTrace();
        }
    }
}