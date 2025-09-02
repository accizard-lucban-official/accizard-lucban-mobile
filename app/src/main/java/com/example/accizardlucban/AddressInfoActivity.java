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
import android.widget.Spinner;
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

    private AutoCompleteTextView actvProvince, actvCityTown;
    private Spinner spinnerBarangay;
    private EditText etBarangayOther;
    private Button btnNext, btnBack;
    private String firstName, lastName, mobileNumber, email, password;
    private View layoutBarangay;

    private static final String PREFS_NAME = "user_profile_prefs";
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
        setupClickListeners();
    }

    private void initializeViews() {
        actvProvince = findViewById(R.id.actvProvince);
        actvCityTown = findViewById(R.id.actvCityTown);
        etBarangayOther = findViewById(R.id.etBarangayOther);
        spinnerBarangay = findViewById(R.id.spinnerBarangay);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        layoutBarangay = findViewById(R.id.layoutBarangay);
    }

    private void setupAutoCompleteFields() {
        actvProvince.setThreshold(1); // Show suggestions from the first letter
        actvCityTown.setThreshold(1); // Show suggestions from the first letter

        String[] provinces = getResources().getStringArray(R.array.province_list);
        InitialLetterAutoCompleteAdapter provinceAdapter = new InitialLetterAutoCompleteAdapter(this, provinces);
        actvProvince.setAdapter(provinceAdapter);
    }

    private void setupBarangaySpinner() {
        String[] lucbanBarangays = {
                "Choose a barangay", "Abang", "Aliliw", "Atulinao", "Ayuti (Poblacion)", "Barangay 1 (Poblacion)", "Barangay 2 (Poblacion)", "Barangay 3 (Poblacion)", "Barangay 4 (Poblacion)", "Barangay 5 (Poblacion)", "Barangay 6 (Poblacion)", "Barangay 7 (Poblacion)", "Barangay 8 (Poblacion)", "Barangay 9 (Poblacion)", "Barangay 10 (Poblacion)", "Igang", "Kabatete", "Kakawit", "Kalangay", "Kalyaat", "Kilib", "Kulapi", "Mahabang Parang", "Malupak", "Manasa", "May-It", "Nagsinamo", "Nalunao", "Palola", "Piis", "Samil", "Tiawe", "Tinamnan"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lucbanBarangays);
        spinnerBarangay.setAdapter(adapter);
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
    }

    private void updateBarangayVisibility() {
        String selectedProvince = actvProvince.getText().toString().trim();
        String cityTown = actvCityTown.getText().toString().trim();
        if (cityTown.isEmpty()) {
            layoutBarangay.setVisibility(View.GONE);
            spinnerBarangay.setVisibility(View.GONE);
            etBarangayOther.setVisibility(View.GONE);
        } else if ("Quezon".equalsIgnoreCase(selectedProvince) && "Lucban".equalsIgnoreCase(cityTown)) {
            layoutBarangay.setVisibility(View.VISIBLE);
            spinnerBarangay.setVisibility(View.VISIBLE);
            etBarangayOther.setVisibility(View.GONE);
        } else {
            layoutBarangay.setVisibility(View.GONE);
            spinnerBarangay.setVisibility(View.GONE);
            etBarangayOther.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    proceedToProfilePicture();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        String selectedProvince = actvProvince.getText().toString().trim();
        String cityTown = actvCityTown.getText().toString().trim();
        if ("Quezon".equalsIgnoreCase(selectedProvince) && "Lucban".equalsIgnoreCase(cityTown)) {
            int barangayPos = spinnerBarangay.getSelectedItemPosition();
            if (barangayPos == 0) {
                Toast.makeText(this, "Please select a barangay", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (etBarangayOther.getText().toString().trim().isEmpty()) {
                etBarangayOther.setError("Barangay is required");
                etBarangayOther.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void proceedToProfilePicture() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String selectedProvince = actvProvince.getText().toString().trim();
        String cityTown = actvCityTown.getText().toString().trim();
        String barangay = "";
        if ("Quezon".equalsIgnoreCase(selectedProvince) && "Lucban".equalsIgnoreCase(cityTown)) {
            barangay = spinnerBarangay.getSelectedItem().toString();
            editor.putString(KEY_BARANGAY, barangay);
        } else {
            barangay = etBarangayOther.getText().toString().trim();
            editor.putString(KEY_BARANGAY, barangay);
        }
        editor.apply();

        // Persist to Firestore under current user's document as well
        updateFirestoreAddressInfo(selectedProvince, cityTown, barangay);

        Intent intent = new Intent(AddressInfoActivity.this, ProfilePictureActivity.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("province", selectedProvince);
        intent.putExtra("cityTown", cityTown);
        intent.putExtra("barangay", barangay);
        startActivity(intent);
    }

    private void updateFirestoreAddressInfo(String province, String cityTown, String barangay) {
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
                        updates.put("province", province);
                        updates.put("cityTown", cityTown);
                        updates.put("barangay", barangay);
                        updates.put("address", province + ", " + cityTown + ", " + barangay);

                        db.collection("users").document(docId).update(updates);
                    }
                });
    }
}