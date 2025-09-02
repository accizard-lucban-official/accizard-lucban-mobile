package com.example.accizardlucban;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class MainDashboard extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainDashboard";

    private TextView userNameText;
    private TextView locationText;
    private LinearLayout callButton;
    private CardView posterButton;
    private ImageView helpButton;
    private ImageView profileButton;
    private LinearLayout emergencyContactsLayout;
    private LineChart reportChart;
    private TextView reportFilterText;
    private CardView roadSafetyCard;
    private CardView fireSafetyCard;
    private CardView landslideSafetyCard;
    private CardView earthquakeSafetyCard;

    // Bottom navigation
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private LinearLayout reportTab;
    private LinearLayout mapTab;
    private LinearLayout alertsTab;

    private String currentReportFilter = "Per Barangay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_dashboard);
            
            initializeViews();
            setupUserInfo();
            setupReportFilter();
            setupChart();
            setupClickListeners();
            setupBottomNavigation();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            // Header views
            locationText = findViewById(R.id.locationText);
            helpButton = findViewById(R.id.helpButton);
            profileButton = findViewById(R.id.profileButton);

            // Main content views
            callButton = findViewById(R.id.callButton);
            posterButton = findViewById(R.id.posterButton);
            emergencyContactsLayout = findViewById(R.id.emergencyContactsLayout);
            reportChart = findViewById(R.id.reportChart);
            reportFilterText = findViewById(R.id.reportFilterText);

            // Safety cards
            roadSafetyCard = findViewById(R.id.roadSafetyCard);
            fireSafetyCard = findViewById(R.id.fireSafetyCard);
            landslideSafetyCard = findViewById(R.id.landslideSafetyCard);
            earthquakeSafetyCard = findViewById(R.id.earthquakeSafetyCard);

            // Bottom navigation
            homeTab = findViewById(R.id.homeTab);
            chatTab = findViewById(R.id.chatTab);
            reportTab = findViewById(R.id.reportTab);
            mapTab = findViewById(R.id.mapTab);
            alertsTab = findViewById(R.id.alertsTab);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void setupReportFilter() {
        try {
            if (reportFilterText != null) {
                // Initialize label
                reportFilterText.setText(currentReportFilter + " ▼");
                reportFilterText.setOnClickListener(v -> showReportFilterMenu(v));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up report filter: " + e.getMessage(), e);
        }
    }

    private void showReportFilterMenu(View anchor) {
        try {
            PopupMenu popupMenu = new PopupMenu(this, anchor);
            popupMenu.getMenu().add("Per Barangay");
            popupMenu.getMenu().add("Per Type");

            popupMenu.setOnMenuItemClickListener(item -> {
                String selection = item.getTitle().toString();
                if (!selection.equals(currentReportFilter)) {
                    currentReportFilter = selection;
                    if (reportFilterText != null) {
                        reportFilterText.setText(currentReportFilter + " ▼");
                    }
                    updateChartData();
                }
                return true;
            });

            popupMenu.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing report filter menu: " + e.getMessage(), e);
        }
    }

    private void setupUserInfo() {
        try {
            // Note: userNameText is now welcomeText in the updated layout
            TextView welcomeText = findViewById(R.id.welcomeText);
            // Load persisted profile data from registration/address
            String fullName = getSavedFullName();
            String barangay = getSavedBarangay();

            if (welcomeText != null && fullName != null && !fullName.isEmpty()) {
                welcomeText.setText("Hello, " + fullName);
            }
            if (locationText != null && barangay != null && !barangay.isEmpty()) {
                locationText.setText(barangay);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up user info: " + e.getMessage(), e);
        }
    }

    private String getSavedFullName() {
        try {
            // Read from the same prefs used by RegistrationActivity
            final String prefsName = "user_profile_prefs";
            final String keyFirstName = "first_name";
            final String keyLastName = "last_name";

            SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
            String first = prefs.getString(keyFirstName, "");
            String last = prefs.getString(keyLastName, "");

            StringBuilder sb = new StringBuilder();
            if (first != null && !first.trim().isEmpty()) sb.append(first.trim());
            if (last != null && !last.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(last.trim());
            }
            return sb.toString();
        } catch (Exception e) {
            Log.w(TAG, "Unable to read saved full name: " + e.getMessage());
            return "";
        }
    }

    private String getSavedBarangay() {
        try {
            // Attempt to read barangay selection saved by AddressInfo (or elsewhere)
            // We support multiple potential keys to be resilient.
            SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);

            // Primary key saved by AddressInfoActivity
            String barangayFromAddressInfo = prefs.getString("barangay", "");

            String selectedBarangay = prefs.getString("selected_barangay", "");
            String otherBarangay = prefs.getString("barangay_other", "");

            // Some implementations may save under different keys
            if ((selectedBarangay == null || selectedBarangay.isEmpty())) {
                selectedBarangay = prefs.getString("spinnerBarangay", "");
            }
            if ((otherBarangay == null || otherBarangay.isEmpty())) {
                otherBarangay = prefs.getString("etBarangayOther", "");
            }

            String value;
            if (barangayFromAddressInfo != null && !barangayFromAddressInfo.isEmpty()) {
                value = barangayFromAddressInfo;
            } else
            if (selectedBarangay != null && !selectedBarangay.isEmpty() &&
                    !"Other".equalsIgnoreCase(selectedBarangay)) {
                value = selectedBarangay;
            } else if (otherBarangay != null && !otherBarangay.isEmpty()) {
                value = otherBarangay;
            } else {
                value = "";
            }

            // Optional prefix formatting if needed
            if (value != null && !value.isEmpty()) {
                if (!value.toLowerCase().startsWith("brgy.")) {
                    value = "Brgy. " + value;
                }
            }
            return value;
        } catch (Exception e) {
            Log.w(TAG, "Unable to read saved barangay: " + e.getMessage());
            return "";
        }
    }

    private void setupChart() {
        try {
            if (reportChart == null) {
                Log.w(TAG, "Report chart is null, skipping chart setup");
                return;
            }

            // Build initial data for current filter
            ArrayList<Entry> entries = buildEntriesForCurrentFilter();

            LineDataSet dataSet = new LineDataSet(entries, "Reports");

            // Check if colors exist, use fallback colors if not
            int primaryColor = getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark);
            int lightColor = getColorSafe(R.color.orange_light, android.R.color.holo_orange_light);

            dataSet.setColor(primaryColor);
            dataSet.setCircleColor(primaryColor);
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(lightColor);
            dataSet.setDrawCircleHole(false);

            LineData lineData = new LineData(dataSet);
            reportChart.setData(lineData);

            // Customize chart appearance
            Description description = new Description();
            description.setText("");
            reportChart.setDescription(description);
            reportChart.getLegend().setEnabled(false);

            // Configure axes
            XAxis xAxis = reportChart.getXAxis();
            xAxis.setEnabled(false);

            YAxis leftAxis = reportChart.getAxisLeft();
            leftAxis.setEnabled(false);

            YAxis rightAxis = reportChart.getAxisRight();
            rightAxis.setEnabled(false);

            reportChart.setTouchEnabled(false);
            reportChart.setDragEnabled(false);
            reportChart.setScaleEnabled(false);
            reportChart.setPinchZoom(false);
            reportChart.setDrawGridBackground(false);
            reportChart.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up chart: " + e.getMessage(), e);
        }
    }

    private void updateChartData() {
        try {
            if (reportChart == null) return;

            ArrayList<Entry> entries = buildEntriesForCurrentFilter();
            LineDataSet dataSet = new LineDataSet(entries, "Reports");

            int primaryColor = getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark);
            int lightColor = getColorSafe(R.color.orange_light, android.R.color.holo_orange_light);

            dataSet.setColor(primaryColor);
            dataSet.setCircleColor(primaryColor);
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(lightColor);
            dataSet.setDrawCircleHole(false);

            reportChart.setData(new LineData(dataSet));
            reportChart.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error updating chart data: " + e.getMessage(), e);
        }
    }

    private ArrayList<Entry> buildEntriesForCurrentFilter() {
        ArrayList<Entry> entries = new ArrayList<>();
        try {
            if ("Per Type".equals(currentReportFilter)) {
                // Example: 5 types
                entries.add(new Entry(0, 4));
                entries.add(new Entry(1, 7));
                entries.add(new Entry(2, 3));
                entries.add(new Entry(3, 6));
                entries.add(new Entry(4, 2));
            } else {
                // Default: Per Barangay (12 months sample as before)
                entries.add(new Entry(0, 2));
                entries.add(new Entry(1, 3));
                entries.add(new Entry(2, 1));
                entries.add(new Entry(3, 4));
                entries.add(new Entry(4, 2));
                entries.add(new Entry(5, 5));
                entries.add(new Entry(6, 3));
                entries.add(new Entry(7, 6));
                entries.add(new Entry(8, 4));
                entries.add(new Entry(9, 7));
                entries.add(new Entry(10, 5));
                entries.add(new Entry(11, 8));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error building entries: " + e.getMessage(), e);
        }
        return entries;
    }

    private int getColorSafe(int colorRes, int fallbackColorRes) {
        try {
            return ContextCompat.getColor(this, colorRes);
        } catch (Exception e) {
            return ContextCompat.getColor(this, fallbackColorRes);
        }
    }

    private void setupClickListeners() {
        try {
            // Header buttons
            if (helpButton != null) {
                helpButton.setOnClickListener(v -> showHelpDialog());
            }
            if (profileButton != null) {
                profileButton.setOnClickListener(v -> openProfileActivity());
            }

            // Call button functionality
            if (callButton != null) {
                callButton.setOnClickListener(v -> makeEmergencyCall());
            }

            // Poster button functionality
            if (posterButton != null) {
                posterButton.setOnClickListener(v -> openPosterActivity());
            }

            // Emergency contacts click listeners
            setupEmergencyContacts();

            // Safety tips click listeners
            setupSafetyTips();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void showHelpDialog() {
        try {
            // Navigate to OnBoardingActivity for help/tutorial
            Intent intent = new Intent(this, OnBoardingActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to OnBoardingActivity: " + e.getMessage(), e);
            // Fallback to dialog if OnBoardingActivity is not available
            try {
                new AlertDialog.Builder(this)
                        .setTitle("Help")
                        .setMessage("This app helps you report emergencies and access safety information.\n\n" +
                                "Features:\n" +
                                "• Emergency calling\n" +
                                "• Safety tips and guidelines\n" +
                                "• Report incidents\n" +
                                "• Emergency contacts")
                        .setPositiveButton("OK", null)
                        .show();
            } catch (Exception dialogError) {
                Log.e(TAG, "Error showing fallback dialog: " + dialogError.getMessage(), dialogError);
                Toast.makeText(this, "Help: This app helps you report emergencies and access safety information",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openProfileActivity() {
        // Navigate to profile activity
        try {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeEmergencyCall() {
        try {
            String emergencyNumber = "tel:911"; // You can change this to local emergency number

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, make the call
                makeCall(emergencyNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error making emergency call: " + e.getMessage(), e);
            Toast.makeText(this, "Error making emergency call", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            // If permission denied or other security issue, show dial pad
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse(phoneNumber));
            startActivity(dialIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("tel:911");
                } else {
                    // Permission denied, show dial pad instead
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:911"));
                    startActivity(dialIntent);
                    Toast.makeText(this, "Permission denied. Opening dial pad instead.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage(), e);
            Toast.makeText(this, "Error handling permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPosterActivity() {
        try {
            Intent intent = new Intent(this, PosterActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Poster feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEmergencyContacts() {
        // LDRRMO contact
        ImageView ldrrmoIcon = findViewById(R.id.ldrrmoIcon);
        if (ldrrmoIcon != null) {
            ldrrmoIcon.setOnClickListener(v -> showEmergencyContactDialog("LDRRMO", "Local Disaster Risk Reduction Management", "555-555"));
        }

        // RHU contact
        ImageView rhuIcon = findViewById(R.id.rhuIcon);
        if (rhuIcon != null) {
            rhuIcon.setOnClickListener(v -> showEmergencyContactDialog("RHU", "Rural Health Unit", "555-556"));
        }

        // PNP contact
        ImageView pnpIcon = findViewById(R.id.pnpIcon);
        if (pnpIcon != null) {
            pnpIcon.setOnClickListener(v -> showEmergencyContactDialog("PNP", "Philippine National Police", "555-557"));
        }

        // BFP contact
        ImageView bfpIcon = findViewById(R.id.bfpIcon);
        if (bfpIcon != null) {
            bfpIcon.setOnClickListener(v -> showEmergencyContactDialog("BFP", "Bureau of Fire Protection", "555-558"));
        }
    }

    private void showEmergencyContactDialog(String agency, String fullName, String number) {
        try {
            // Create bottom sheet dialog
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_emergency_contact, null);
            
            // Find views in the bottom sheet
            ImageView agencyIcon = bottomSheetView.findViewById(R.id.agencyIcon);
            TextView agencyName = bottomSheetView.findViewById(R.id.agencyName);
            TextView agencyFullName = bottomSheetView.findViewById(R.id.agencyFullName);
            TextView phoneNumber = bottomSheetView.findViewById(R.id.phoneNumber);
            android.widget.Button callButton = bottomSheetView.findViewById(R.id.callButton);
            
            // Set agency-specific icon
            int iconResource = getAgencyIcon(agency);
            if (agencyIcon != null) {
                agencyIcon.setImageResource(iconResource);
            }
            
            // Set text content
            if (agencyName != null) {
                agencyName.setText("Lucban " + agency);
            }
            if (agencyFullName != null) {
                agencyFullName.setText(fullName);
            }
            if (phoneNumber != null) {
                phoneNumber.setText(number);
            }
            
            // Set call button click listener
            if (callButton != null) {
                callButton.setOnClickListener(v -> {
                    bottomSheetDialog.dismiss();
                    callEmergencyContact(agency, number);
                });
            }
            
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing emergency contact bottom sheet: " + e.getMessage(), e);
            // Fallback to direct call
            callEmergencyContact(agency, number);
        }
    }
    
    private int getAgencyIcon(String agency) {
        switch (agency) {
            case "LDRRMO":
                return R.drawable.ic_emergency; // You can create specific icons for each agency
            case "RHU":
                return R.drawable.ic_health; // Health icon
            case "PNP":
                return R.drawable.ic_police; // Police icon
            case "BFP":
                return R.drawable.ic_fire; // Fire icon
            default:
                return R.drawable.ic_emergency; // Default emergency icon
        }
    }

    private void callEmergencyContact(String agency, String number) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + number));

        try {
            startActivity(dialIntent);
            Toast.makeText(this, "Calling " + agency + "...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to make call. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSafetyTips() {
        if (roadSafetyCard != null) {
            roadSafetyCard.setOnClickListener(v -> openSafetyTips("Road Safety"));
        }

        if (fireSafetyCard != null) {
            fireSafetyCard.setOnClickListener(v -> openSafetyTips("Fire Safety"));
        }

        if (landslideSafetyCard != null) {
            landslideSafetyCard.setOnClickListener(v -> openSafetyTips("Landslide Safety"));
        }

        if (earthquakeSafetyCard != null) {
            earthquakeSafetyCard.setOnClickListener(v -> openSafetyTips("Earthquake Safety"));
        }
    }

    private void openSafetyTips(String safetyType) {
        try {
            Intent intent = new Intent(this, SafetyTipsActivity.class);
            intent.putExtra("safety_type", safetyType);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, safetyType + " tips coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        // Home tab (current)
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                // Already on home, do nothing or refresh
                Toast.makeText(this, "You're on Home", Toast.LENGTH_SHORT).show();
            });
        }

        // Chat tab
        if (chatTab != null) {
            chatTab.setOnClickListener(v -> navigateToTab("Chat"));
        }

        // Report tab
        if (reportTab != null) {
            reportTab.setOnClickListener(v -> navigateToTab("Report"));
        }

        // Map tab
        if (mapTab != null) {
            mapTab.setOnClickListener(v -> navigateToTab("Map"));
        }

        // Alerts tab
        if (alertsTab != null) {
            alertsTab.setOnClickListener(v -> navigateToTab("Alerts"));
        }
    }

    private void navigateToTab(String tabName) {
        try {
            Intent intent = null;
            switch (tabName) {
                case "Chat":
                    intent = new Intent(this, ChatActivity.class);
                    break;
                case "Report":
                    intent = new Intent(this, ReportSubmissionActivity.class);
                    break;
                case "Map":
                    intent = new Intent(this, MapViewActivity.class);
                    break;
                case "Alerts":
                    intent = new Intent(this, AlertsActivity.class);
                    break;
            }

            if (intent != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, tabName + " feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        // Refresh user data when returning to dashboard
        // You can implement actual data loading here
        setupUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            // Show exit confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        super.onBackPressed();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error handling back press: " + e.getMessage(), e);
            super.onBackPressed();
        }
    }
}