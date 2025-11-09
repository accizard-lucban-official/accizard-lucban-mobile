package com.example.accizardlucban;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private EditText etBirthday;
    private ImageView infoIcon;
    private Spinner spinnerGender;
    private Spinner spinnerCivilStatus;
    private Spinner spinnerReligion;
    private Spinner spinnerBloodType;
    private CheckBox cbPwd;
    private ImageButton btnBack;
    private Button btnNext;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
    private String password;

    private static final String PREFS_NAME = "user_profile_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        initializeViews();
        getIntentData();
        setupSpinners();
        restorePersonalInfoData(); // Restore previously saved personal info
        setupClickListeners();
    }

    private void initializeViews() {
        etBirthday = findViewById(R.id.etBirthday);
        infoIcon = findViewById(R.id.infoIcon);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCivilStatus = findViewById(R.id.spinnerCivilStatus);
        spinnerReligion = findViewById(R.id.spinnerReligion);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        cbPwd = findViewById(R.id.cbPwd);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        
        // Make birthday field non-editable but clickable to show date picker
        etBirthday.setFocusable(false);
        etBirthday.setClickable(true);
        
        // Add calendar icon on the left side of the birthday field
        etBirthday.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_menu_my_calendar, // Left icon
            0,  // Top
            0,  // Right
            0   // Bottom
        );
        etBirthday.setCompoundDrawablePadding(16); // Add padding between icon and text
        
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        
        // Set up tooltip on info icon and ensure no color tint is applied
        infoIcon.setColorFilter(null); // Clear any color filters
        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PersonalInfoActivity.this, 
                    "You are not eligible to register to the app. You must be 7+ years old. If you are a minor, make sure to inform your parents\n.",
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        mobileNumber = intent.getStringExtra("mobileNumber");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
    }

    private void setupSpinners() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Prefer not to say", "Male", "Female"});
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Prefer not to say", "Single", "Married", "Separated", "Widowed"});
        spinnerCivilStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> religionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Others", "Roman Catholic", "Christian", "Iglesia ni Cristo", "Islam", "Buddhism", "Hinduism"});
        spinnerReligion.setAdapter(religionAdapter);

        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Not Available", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        spinnerBloodType.setAdapter(bloodAdapter);
    }

    /**
     * Restores personal info data from SharedPreferences
     */
    private void restorePersonalInfoData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            
            // Restore birthday
            String savedBirthday = prefs.getString("saved_birthday", null);
            if (savedBirthday != null && !savedBirthday.isEmpty()) {
                etBirthday.setText(savedBirthday);
                android.util.Log.d("PersonalInfo", "Birthday restored: " + savedBirthday);
            }
            
            // Restore gender
            String savedGender = prefs.getString("saved_gender", null);
            if (savedGender != null && !savedGender.isEmpty()) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGender.getAdapter();
                int position = adapter.getPosition(savedGender);
                if (position >= 0) {
                    spinnerGender.setSelection(position);
                    android.util.Log.d("PersonalInfo", "Gender restored: " + savedGender);
                }
            }
            
            // Restore civil status
            String savedCivilStatus = prefs.getString("saved_civil_status", null);
            if (savedCivilStatus != null && !savedCivilStatus.isEmpty()) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCivilStatus.getAdapter();
                int position = adapter.getPosition(savedCivilStatus);
                if (position >= 0) {
                    spinnerCivilStatus.setSelection(position);
                    android.util.Log.d("PersonalInfo", "Civil status restored: " + savedCivilStatus);
                }
            }
            
            // Restore religion
            String savedReligion = prefs.getString("saved_religion", null);
            if (savedReligion != null && !savedReligion.isEmpty()) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerReligion.getAdapter();
                int position = adapter.getPosition(savedReligion);
                if (position >= 0) {
                    spinnerReligion.setSelection(position);
                    android.util.Log.d("PersonalInfo", "Religion restored: " + savedReligion);
                }
            }
            
            // Restore blood type
            String savedBloodType = prefs.getString("saved_blood_type", null);
            if (savedBloodType != null && !savedBloodType.isEmpty()) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerBloodType.getAdapter();
                int position = adapter.getPosition(savedBloodType);
                if (position >= 0) {
                    spinnerBloodType.setSelection(position);
                    android.util.Log.d("PersonalInfo", "Blood type restored: " + savedBloodType);
                }
            }
            
            // Restore PWD checkbox
            boolean savedPwd = prefs.getBoolean("saved_pwd", false);
            cbPwd.setChecked(savedPwd);
            android.util.Log.d("PersonalInfo", "PWD status restored: " + savedPwd);
            
            android.util.Log.d("PersonalInfo", "✅ Personal info data restored from SharedPreferences");
        } catch (Exception e) {
            android.util.Log.e("PersonalInfo", "Error restoring personal info data", e);
            e.printStackTrace();
        }
    }

    private void showDatePickerDialog() {
        // Get current real-time date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        // If there's already a date in the field, parse it and use it as the initial date
        String currentDate = etBirthday.getText().toString().trim();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");
                if (parts.length == 3) {
                    month = Integer.parseInt(parts[0]) - 1; // Month is 0-based
                    day = Integer.parseInt(parts[1]);
                    year = Integer.parseInt(parts[2]);
                }
            } catch (Exception e) {
                // Use current real-time date if parsing fails
                Calendar current = Calendar.getInstance();
                year = current.get(Calendar.YEAR);
                month = current.get(Calendar.MONTH);
                day = current.get(Calendar.DAY_OF_MONTH);
            }
        }
        // If field is empty, it will show today's date by default (already set above)

        // Create and show DatePickerDialog with real-time date
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Create Calendar for selected date
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        
                        // Create Calendar for 7 years ago from today
                        Calendar sevenYearsAgo = Calendar.getInstance();
                        sevenYearsAgo.add(Calendar.YEAR, -7);
                        
                        // Validate that user is at least 7 years old
                        if (selectedDate.after(sevenYearsAgo)) {
                            // User is less than 7 years old
                            Toast.makeText(PersonalInfoActivity.this, 
                                "You are not eligible to register to the app. You must be 7+ years old. If you are a minor, make sure to inform your parents\n.",
                                Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // Format the date as MM/dd/yyyy
                        String formattedDate = String.format(Locale.US, "%02d/%02d/%04d", 
                                selectedMonth + 1, selectedDay, selectedYear);
                        etBirthday.setText(formattedDate);
                        etBirthday.setError(null); // Clear any error
                    }
                },
                year,
                month,
                day
        );
        
        // Don't set max date - allow showing all years up to current year
        // Validation will still check if user is at least 18 years old when date is selected
        
        // Set minimum date to 150 years ago to show more history
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -150);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save current data before going back (even if incomplete)
                saveCurrentDataForRetention();
                finish();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (savePersonalInfo()) {
                    navigateToAddressInfo();
                } else {
                    Toast.makeText(PersonalInfoActivity.this, "Please complete required fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Saves current form data for retention (even if incomplete)
     * Called when user clicks back button
     */
    private void saveCurrentDataForRetention() {
        try {
            String birthday = etBirthday.getText().toString().trim();
            String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
            String civilStatus = spinnerCivilStatus.getSelectedItem() != null ? spinnerCivilStatus.getSelectedItem().toString() : "";
            String religion = spinnerReligion.getSelectedItem() != null ? spinnerReligion.getSelectedItem().toString() : "";
            String bloodType = spinnerBloodType.getSelectedItem() != null ? spinnerBloodType.getSelectedItem().toString() : "";
            boolean isPwd = cbPwd.isChecked();
            
            savePersonalInfoForRetention(birthday, gender, civilStatus, religion, bloodType, isPwd);
            android.util.Log.d("PersonalInfo", "Current data saved before going back");
        } catch (Exception e) {
            android.util.Log.e("PersonalInfo", "Error saving current data", e);
        }
    }

    private boolean savePersonalInfo() {
        // Optional validation: only birthday required for now
        String birthday = etBirthday.getText().toString().trim();
        if (birthday.isEmpty()) {
            etBirthday.setError("Birthday is required");
            etBirthday.requestFocus();
            return false;
        }

        // Read spinner values
        String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "";
        String civilStatus = spinnerCivilStatus.getSelectedItem() != null ? spinnerCivilStatus.getSelectedItem().toString() : "";
        String religion = spinnerReligion.getSelectedItem() != null ? spinnerReligion.getSelectedItem().toString() : "";
        String bloodType = spinnerBloodType.getSelectedItem() != null ? spinnerBloodType.getSelectedItem().toString() : "";
        boolean isPwd = cbPwd.isChecked();

        // Save to user_profile_prefs (existing functionality)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("birthday", birthday);
        editor.putString("gender", gender);
        editor.putString("civil_status", civilStatus);
        editor.putString("religion", religion);
        editor.putString("blood_type", bloodType);
        editor.putBoolean("pwd", isPwd);
        // Save email from registration
        editor.putString("email_address", email);
        editor.apply();

        // ALSO save to registration_data for data retention
        savePersonalInfoForRetention(birthday, gender, civilStatus, religion, bloodType, isPwd);

        // Also persist to Firestore under the current user's document
        updateFirestorePersonalInfo(birthday, gender, civilStatus, religion, bloodType, isPwd);
        return true;
    }

    /**
     * Saves personal info data to SharedPreferences for data retention
     */
    private void savePersonalInfoForRetention(String birthday, String gender, String civilStatus,
                                               String religion, String bloodType, boolean isPwd) {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putString("saved_birthday", birthday);
            editor.putString("saved_gender", gender);
            editor.putString("saved_civil_status", civilStatus);
            editor.putString("saved_religion", religion);
            editor.putString("saved_blood_type", bloodType);
            editor.putBoolean("saved_pwd", isPwd);
            
            editor.apply();
            android.util.Log.d("PersonalInfo", "✅ Personal info data saved to SharedPreferences for retention");
        } catch (Exception e) {
            android.util.Log.e("PersonalInfo", "Error saving personal info data for retention", e);
            e.printStackTrace();
        }
    }

    private void updateFirestorePersonalInfo(String birthday, String gender, String civilStatus,
                                             String religion, String bloodType, boolean isPwd) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
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
                        String docId = doc.getId();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("birthday", birthday);
                        updates.put("gender", gender);
                        updates.put("civil_status", civilStatus);
                        updates.put("religion", religion);
                        updates.put("blood_type", bloodType);
                        updates.put("pwd", isPwd);

                        db.collection("users").document(docId).update(updates);
                    }
                });
    }

    private void navigateToAddressInfo() {
        Intent intent = new Intent(PersonalInfoActivity.this, AddressInfoActivity.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Save current data before going back
        saveCurrentDataForRetention();
        super.onBackPressed();
    }
}






