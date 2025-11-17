package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Facilities extends AppCompatActivity {

    // Timeline section views
    private LinearLayout timelineHeader;
    private LinearLayout timelineContent;
    private ImageView timelineArrow;

    // Incident Types section views
    private LinearLayout incidentTypesHeader;
    private LinearLayout incidentTypesContent;
    private ImageView incidentTypesArrow;

    // Emergency Support section views
    private LinearLayout emergencySupportHeader;
    private LinearLayout emergencySupportContent;
    private ImageView emergencySupportArrow;

    // Switches and checkboxes
    private Switch heatmapSwitch;
    private CheckBox roadAccidentCheck, fireCheck, medicalEmergencyCheck, floodingCheck;
    private CheckBox volcanicActivityCheck, landslideCheck, earthquakeCheck, civilDisturbanceCheck;
    private CheckBox armedConflictCheck, infectiousDiseaseCheck;
    private CheckBox poorInfrastructureCheck, obstructionsCheck, electricalHazardCheck, environmentalHazardCheck;
    private final List<CheckBox> incidentCheckboxes = new ArrayList<>();
    private boolean isUpdatingIncidentChecks = false;
    private CheckBox evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck;
    private CheckBox fireStationsCheck, governmentOfficesCheck;

    // Timeline options
    private TextView todayOption, thisWeekOption, thisMonthOption, thisYearOption;
    private TextView selectedTimelineOption;
    private String selectedTimeRange = "Today";

    // Apply and Cancel buttons
    private Button applyFiltersButton;
    private Button cancelButton;

    // Section visibility states
    private boolean isTimelineExpanded = false;
    private boolean isIncidentTypesExpanded = false;
    private boolean isEmergencySupportExpanded = false;

    // Select All / Deselect All buttons
    private Button selectAllIncidentsButton;
    private Button deselectAllIncidentsButton;
    private Button selectAllFacilitiesButton;
    private Button deselectAllFacilitiesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facilities);

        initializeViews();
        loadFilterStates();
        setupClickListeners();
        setupButtons();
        setupTimelineOptions();
        setupSelectAllButtons();
        updateFilterSummary(); // Initialize filter summary display
    }

    private void initializeViews() {
        // Timeline section
        timelineHeader = findViewById(R.id.timelineHeader);
        timelineContent = findViewById(R.id.timelineContent);
        timelineArrow = findViewById(R.id.timelineArrow);

        // Incident Types section
        incidentTypesHeader = findViewById(R.id.incidentTypesHeader);
        incidentTypesContent = findViewById(R.id.incidentTypesContent);
        incidentTypesArrow = findViewById(R.id.incidentTypesArrow);

        // Emergency Support section
        emergencySupportHeader = findViewById(R.id.emergencySupportHeader);
        emergencySupportContent = findViewById(R.id.emergencySupportContent);
        emergencySupportArrow = findViewById(R.id.emergencySupportArrow);

        // Switches and checkboxes
        heatmapSwitch = findViewById(R.id.heatmapSwitch);
        roadAccidentCheck = findViewById(R.id.roadAccidentCheck);
        fireCheck = findViewById(R.id.fireCheck);
        medicalEmergencyCheck = findViewById(R.id.medicalEmergencyCheck);
        floodingCheck = findViewById(R.id.floodingCheck);
        volcanicActivityCheck = findViewById(R.id.volcanicActivityCheck);
        landslideCheck = findViewById(R.id.landslideCheck);
        earthquakeCheck = findViewById(R.id.earthquakeCheck);
        civilDisturbanceCheck = findViewById(R.id.civilDisturbanceCheck);
        armedConflictCheck = findViewById(R.id.armedConflictCheck);
        infectiousDiseaseCheck = findViewById(R.id.infectiousDiseaseCheck);
        poorInfrastructureCheck = findViewById(R.id.poorInfrastructureCheck);
        obstructionsCheck = findViewById(R.id.obstructionsCheck);
        electricalHazardCheck = findViewById(R.id.electricalHazardCheck);
        environmentalHazardCheck = findViewById(R.id.environmentalHazardCheck);

        // Verify new checkboxes are found
        if (poorInfrastructureCheck == null) {
            android.util.Log.e("Facilities", "poorInfrastructureCheck is NULL!");
        }
        if (obstructionsCheck == null) {
            android.util.Log.e("Facilities", "obstructionsCheck is NULL!");
        }
        if (electricalHazardCheck == null) {
            android.util.Log.e("Facilities", "electricalHazardCheck is NULL!");
        }
        if (environmentalHazardCheck == null) {
            android.util.Log.e("Facilities", "environmentalHazardCheck is NULL!");
        }

        incidentCheckboxes.clear();
        registerIncidentCheckbox(roadAccidentCheck);
        registerIncidentCheckbox(fireCheck);
        registerIncidentCheckbox(medicalEmergencyCheck);
        registerIncidentCheckbox(floodingCheck);
        registerIncidentCheckbox(volcanicActivityCheck);
        registerIncidentCheckbox(landslideCheck);
        registerIncidentCheckbox(earthquakeCheck);
        registerIncidentCheckbox(civilDisturbanceCheck);
        registerIncidentCheckbox(armedConflictCheck);
        registerIncidentCheckbox(infectiousDiseaseCheck);
        registerIncidentCheckbox(poorInfrastructureCheck);
        registerIncidentCheckbox(obstructionsCheck);
        registerIncidentCheckbox(electricalHazardCheck);
        registerIncidentCheckbox(environmentalHazardCheck);
        evacuationCentersCheck = findViewById(R.id.evacuationCentersCheck);
        healthFacilitiesCheck = findViewById(R.id.healthFacilitiesCheck);
        policeStationsCheck = findViewById(R.id.policeStationsCheck);
        fireStationsCheck = findViewById(R.id.fireStationsCheck);
        governmentOfficesCheck = findViewById(R.id.governmentOfficesCheck);

        // Timeline options
        findTimelineOptions();

        // Buttons
        applyFiltersButton = findViewById(R.id.applyFiltersButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Select All / Deselect All buttons

        // Set initial visibility for collapsible sections
        if (timelineContent != null) {
            timelineContent.setVisibility(View.GONE);
        }
        // Expand Incident Types section by default so users can see all options including new ones
        if (incidentTypesContent != null) {
            incidentTypesContent.setVisibility(View.VISIBLE);
            isIncidentTypesExpanded = true;
            // Rotate arrow to indicate expanded state
            if (incidentTypesArrow != null) {
                incidentTypesArrow.setRotation(180f);
            }
        }
        if (emergencySupportContent != null) {
            emergencySupportContent.setVisibility(View.GONE);
        }
    }

    private void registerIncidentCheckbox(CheckBox checkBox) {
        if (checkBox != null && !incidentCheckboxes.contains(checkBox)) {
            incidentCheckboxes.add(checkBox);
        }
    }

    private void findTimelineOptions() {
        // Find timeline option TextViews
        todayOption = findViewById(R.id.todayOption);
        thisWeekOption = findViewById(R.id.thisWeekOption);
        thisMonthOption = findViewById(R.id.thisMonthOption);
        thisYearOption = findViewById(R.id.thisYearOption);

        // Set default selection
        if (todayOption != null) {
            selectedTimelineOption = todayOption;
        }
    }

    private void loadFilterStates() {
        // Load filter states from Intent extras (passed from MapViewActivity)
        Intent intent = getIntent();

        // Load heatmap state
        if (heatmapSwitch != null) {
            heatmapSwitch.setChecked(intent.getBooleanExtra("heatmapEnabled", false));
        }

        // Load timeline selection
        selectedTimeRange = intent.getStringExtra("selectedTimeRange");
        if (selectedTimeRange == null) selectedTimeRange = "Today";

        // Load incident filter states
        if (roadAccidentCheck != null) {
            roadAccidentCheck.setChecked(intent.getBooleanExtra("roadAccident", true));
        }
        if (fireCheck != null) {
            fireCheck.setChecked(intent.getBooleanExtra("fire", true));
        }
        if (medicalEmergencyCheck != null) {
            medicalEmergencyCheck.setChecked(intent.getBooleanExtra("medicalEmergency", true));
        }
        if (floodingCheck != null) {
            floodingCheck.setChecked(intent.getBooleanExtra("flooding", true));
        }
        if (volcanicActivityCheck != null) {
            volcanicActivityCheck.setChecked(intent.getBooleanExtra("volcanicActivity", true));
        }
        if (landslideCheck != null) {
            landslideCheck.setChecked(intent.getBooleanExtra("landslide", true));
        }
        if (earthquakeCheck != null) {
            earthquakeCheck.setChecked(intent.getBooleanExtra("earthquake", true));
        }
        if (civilDisturbanceCheck != null) {
            civilDisturbanceCheck.setChecked(intent.getBooleanExtra("civilDisturbance", true));
        }
        if (armedConflictCheck != null) {
            armedConflictCheck.setChecked(intent.getBooleanExtra("armedConflict", true));
        }
        if (infectiousDiseaseCheck != null) {
            infectiousDiseaseCheck.setChecked(intent.getBooleanExtra("infectiousDisease", true));
        }
        if (poorInfrastructureCheck != null) {
            poorInfrastructureCheck.setChecked(intent.getBooleanExtra("poorInfrastructure", true));
        }
        if (obstructionsCheck != null) {
            obstructionsCheck.setChecked(intent.getBooleanExtra("obstructions", true));
        }
        if (electricalHazardCheck != null) {
            electricalHazardCheck.setChecked(intent.getBooleanExtra("electricalHazard", true));
        }
        if (environmentalHazardCheck != null) {
            environmentalHazardCheck.setChecked(intent.getBooleanExtra("environmentalHazard", true));
        }

        enforceSingleIncidentSelection();

        // Load facility filter states
        if (evacuationCentersCheck != null) {
            evacuationCentersCheck.setChecked(intent.getBooleanExtra("evacuationCenters", true));
        }
        if (healthFacilitiesCheck != null) {
            healthFacilitiesCheck.setChecked(intent.getBooleanExtra("healthFacilities", true));
        }
        if (policeStationsCheck != null) {
            policeStationsCheck.setChecked(intent.getBooleanExtra("policeStations", true));
        }
        if (fireStationsCheck != null) {
            fireStationsCheck.setChecked(intent.getBooleanExtra("fireStations", true));
        }
        if (governmentOfficesCheck != null) {
            governmentOfficesCheck.setChecked(intent.getBooleanExtra("governmentOffices", true));
        }

        // Update timeline selection UI
        updateTimelineSelection();
    }

    private void setupClickListeners() {
        // Timeline section click listener
        if (timelineHeader != null) {
            timelineHeader.setOnClickListener(v -> toggleSection("timeline"));
        }

        // Incident Types section click listener
        if (incidentTypesHeader != null) {
            incidentTypesHeader.setOnClickListener(v -> toggleSection("incidents"));
        }

        // Emergency Support section click listener
        if (emergencySupportHeader != null) {
            emergencySupportHeader.setOnClickListener(v -> toggleSection("facilities"));
        }
        
        // Setup checkbox change listeners for visual feedback
        setupCheckboxListeners();
    }
    
    private void setupCheckboxListeners() {
        // Incident type checkboxes
        setupExclusiveIncidentCheckbox(roadAccidentCheck);
        setupExclusiveIncidentCheckbox(fireCheck);
        setupExclusiveIncidentCheckbox(medicalEmergencyCheck);
        setupExclusiveIncidentCheckbox(floodingCheck);
        setupExclusiveIncidentCheckbox(volcanicActivityCheck);
        setupExclusiveIncidentCheckbox(landslideCheck);
        setupExclusiveIncidentCheckbox(earthquakeCheck);
        setupExclusiveIncidentCheckbox(civilDisturbanceCheck);
        setupExclusiveIncidentCheckbox(armedConflictCheck);
        setupExclusiveIncidentCheckbox(infectiousDiseaseCheck);
        setupExclusiveIncidentCheckbox(poorInfrastructureCheck);
        setupExclusiveIncidentCheckbox(obstructionsCheck);
        setupExclusiveIncidentCheckbox(electricalHazardCheck);
        setupExclusiveIncidentCheckbox(environmentalHazardCheck);
        
        // Facility checkboxes
        if (evacuationCentersCheck != null) {
            evacuationCentersCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateCheckboxVisualState(buttonView, isChecked);
                updateFilterSummary();
            });
        }
        
        if (healthFacilitiesCheck != null) {
            healthFacilitiesCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateCheckboxVisualState(buttonView, isChecked);
                updateFilterSummary();
            });
        }
        
        if (policeStationsCheck != null) {
            policeStationsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateCheckboxVisualState(buttonView, isChecked);
                updateFilterSummary();
            });
        }
        
        if (fireStationsCheck != null) {
            fireStationsCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateCheckboxVisualState(buttonView, isChecked);
                updateFilterSummary();
            });
        }
        
        if (governmentOfficesCheck != null) {
            governmentOfficesCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateCheckboxVisualState(buttonView, isChecked);
                updateFilterSummary();
            });
        }
        
        // Heatmap switch
        if (heatmapSwitch != null) {
            heatmapSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateFilterSummary();
            });
        }
    }

    private void setupExclusiveIncidentCheckbox(CheckBox checkBox) {
        if (checkBox == null) {
            return;
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> handleIncidentCheckboxChange(checkBox, isChecked));
    }

    private void handleIncidentCheckboxChange(CheckBox changedCheckBox, boolean isChecked) {
        if (isUpdatingIncidentChecks) {
            return;
        }

        isUpdatingIncidentChecks = true;
        try {
            if (isChecked) {
                for (CheckBox other : incidentCheckboxes) {
                    if (other != null && other != changedCheckBox && other.isChecked()) {
                        other.setChecked(false);
                        updateCheckboxVisualState(other, false);
                    }
                }
            }

            updateCheckboxVisualState(changedCheckBox, isChecked);
            updateFilterSummary();
        } finally {
            isUpdatingIncidentChecks = false;
        }
    }

    private void enforceSingleIncidentSelection() {
        if (incidentCheckboxes.isEmpty()) {
            return;
        }

        boolean foundChecked = false;
        isUpdatingIncidentChecks = true;
        try {
            for (CheckBox checkBox : incidentCheckboxes) {
                if (checkBox == null) {
                    continue;
                }

                if (checkBox.isChecked()) {
                    if (!foundChecked) {
                        foundChecked = true;
                    } else {
                        checkBox.setChecked(false);
                        updateCheckboxVisualState(checkBox, false);
                    }
                }
            }
        } finally {
            isUpdatingIncidentChecks = false;
        }

        updateFilterSummary();
    }

    private void setupButtons() {
        // Apply Filters button
        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> applyFilters());
        }

        // Cancel button
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }

    private void setupTimelineOptions() {
        if (todayOption != null) {
            todayOption.setOnClickListener(v -> selectTimelineOption("Today", todayOption));
        }
        if (thisWeekOption != null) {
            thisWeekOption.setOnClickListener(v -> selectTimelineOption("This Week", thisWeekOption));
        }
        if (thisMonthOption != null) {
            thisMonthOption.setOnClickListener(v -> selectTimelineOption("This Month", thisMonthOption));
        }
        if (thisYearOption != null) {
            thisYearOption.setOnClickListener(v -> selectTimelineOption("This Year", thisYearOption));
        }
    }

    private void setupSelectAllButtons() {
        // Select All Incidents button
        if (selectAllIncidentsButton != null) {
            selectAllIncidentsButton.setOnClickListener(v -> selectAllIncidents(true));
        }

        // Deselect All Incidents button
        if (deselectAllIncidentsButton != null) {
            deselectAllIncidentsButton.setOnClickListener(v -> selectAllIncidents(false));
        }

        // Select All Facilities button
        if (selectAllFacilitiesButton != null) {
            selectAllFacilitiesButton.setOnClickListener(v -> selectAllFacilities(true));
        }

        // Deselect All Facilities button
        if (deselectAllFacilitiesButton != null) {
            deselectAllFacilitiesButton.setOnClickListener(v -> selectAllFacilities(false));
        }
    }

    private void toggleSection(String section) {
        switch (section) {
            case "timeline":
                isTimelineExpanded = !isTimelineExpanded;
                animateSection(timelineContent, timelineArrow, isTimelineExpanded);
                break;
            case "incidents":
                isIncidentTypesExpanded = !isIncidentTypesExpanded;
                animateSection(incidentTypesContent, incidentTypesArrow, isIncidentTypesExpanded);
                break;
            case "facilities":
                isEmergencySupportExpanded = !isEmergencySupportExpanded;
                animateSection(emergencySupportContent, emergencySupportArrow, isEmergencySupportExpanded);
                break;
        }
    }

    private void animateSection(LinearLayout content, ImageView arrow, boolean expand) {
        if (content == null || arrow == null) return;

        // Animate content visibility
        if (expand) {
            content.setVisibility(View.VISIBLE);
        } else {
            content.setVisibility(View.GONE);
        }

        // Animate arrow rotation
        float fromDegrees = expand ? 0f : 180f;
        float toDegrees = expand ? 180f : 0f;

        RotateAnimation rotateAnimation = new RotateAnimation(
                fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(300);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);
    }

    private void selectTimelineOption(String timeRange, TextView selectedOption) {
        // Reset all timeline options to default color
        resetTimelineOptions();

        // Set selected option
        selectedTimeRange = timeRange;
        selectedTimelineOption = selectedOption;

        // Highlight selected option
        if (selectedOption != null) {
            selectedOption.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            selectedOption.setBackground(ContextCompat.getDrawable(this, android.R.drawable.btn_default));
        }

        Toast.makeText(this, "Selected: " + timeRange, Toast.LENGTH_SHORT).show();
    }

    private void resetTimelineOptions() {
        int defaultColor = ContextCompat.getColor(this, android.R.color.black);

        if (todayOption != null) {
            todayOption.setTextColor(defaultColor);
            todayOption.setBackground(null);
        }
        if (thisWeekOption != null) {
            thisWeekOption.setTextColor(defaultColor);
            thisWeekOption.setBackground(null);
        }
        if (thisMonthOption != null) {
            thisMonthOption.setTextColor(defaultColor);
            thisMonthOption.setBackground(null);
        }
        if (thisYearOption != null) {
            thisYearOption.setTextColor(defaultColor);
            thisYearOption.setBackground(null);
        }
    }

    private void updateTimelineSelection() {
        // Update UI based on selected time range
        TextView optionToSelect = null;

        switch (selectedTimeRange) {
            case "Today":
                optionToSelect = todayOption;
                break;
            case "This Week":
                optionToSelect = thisWeekOption;
                break;
            case "This Month":
                optionToSelect = thisMonthOption;
                break;
            case "This Year":
                optionToSelect = thisYearOption;
                break;
        }

        if (optionToSelect != null) {
            selectTimelineOption(selectedTimeRange, optionToSelect);
        }
    }

    private void selectAllIncidents(boolean select) {
        if (incidentCheckboxes.isEmpty()) {
            updateFilterSummary();
            return;
        }

        isUpdatingIncidentChecks = true;
        if (select) {
            boolean selectionMade = false;
            for (CheckBox checkBox : incidentCheckboxes) {
                if (checkBox == null) continue;
                if (!selectionMade) {
                    checkBox.setChecked(true);
                    updateCheckboxVisualState(checkBox, true);
                    selectionMade = true;
                } else {
                    checkBox.setChecked(false);
                    updateCheckboxVisualState(checkBox, false);
                }
            }
            Toast.makeText(this, "Only one incident type can be selected at a time.", Toast.LENGTH_SHORT).show();
        } else {
            for (CheckBox checkBox : incidentCheckboxes) {
                if (checkBox == null) continue;
                checkBox.setChecked(false);
                updateCheckboxVisualState(checkBox, false);
            }
            Toast.makeText(this, "All incidents deselected", Toast.LENGTH_SHORT).show();
        }
        isUpdatingIncidentChecks = false;
        updateFilterSummary();
    }

    private void selectAllFacilities(boolean select) {
        if (evacuationCentersCheck != null) evacuationCentersCheck.setChecked(select);
        if (healthFacilitiesCheck != null) healthFacilitiesCheck.setChecked(select);
        if (policeStationsCheck != null) policeStationsCheck.setChecked(select);
        if (fireStationsCheck != null) fireStationsCheck.setChecked(select);
        if (governmentOfficesCheck != null) governmentOfficesCheck.setChecked(select);

        Toast.makeText(this, select ? "All facilities selected" : "All facilities deselected", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters() {
        // Create result intent with all filter states
        Intent resultIntent = new Intent();

        // Add heatmap state
        resultIntent.putExtra("heatmapEnabled", heatmapSwitch != null ? heatmapSwitch.isChecked() : false);

        // Add timeline selection
        resultIntent.putExtra("selectedTimeRange", selectedTimeRange);

        // Add incident filter states
        resultIntent.putExtra("roadAccident", roadAccidentCheck != null ? roadAccidentCheck.isChecked() : true);
        resultIntent.putExtra("fire", fireCheck != null ? fireCheck.isChecked() : true);
        resultIntent.putExtra("medicalEmergency", medicalEmergencyCheck != null ? medicalEmergencyCheck.isChecked() : true);
        resultIntent.putExtra("flooding", floodingCheck != null ? floodingCheck.isChecked() : true);
        resultIntent.putExtra("volcanicActivity", volcanicActivityCheck != null ? volcanicActivityCheck.isChecked() : true);
        resultIntent.putExtra("landslide", landslideCheck != null ? landslideCheck.isChecked() : true);
        resultIntent.putExtra("earthquake", earthquakeCheck != null ? earthquakeCheck.isChecked() : true);
        resultIntent.putExtra("civilDisturbance", civilDisturbanceCheck != null ? civilDisturbanceCheck.isChecked() : true);
        resultIntent.putExtra("armedConflict", armedConflictCheck != null ? armedConflictCheck.isChecked() : true);
        resultIntent.putExtra("infectiousDisease", infectiousDiseaseCheck != null ? infectiousDiseaseCheck.isChecked() : true);
        resultIntent.putExtra("poorInfrastructure", poorInfrastructureCheck != null ? poorInfrastructureCheck.isChecked() : true);
        resultIntent.putExtra("obstructions", obstructionsCheck != null ? obstructionsCheck.isChecked() : true);
        resultIntent.putExtra("electricalHazard", electricalHazardCheck != null ? electricalHazardCheck.isChecked() : true);
        resultIntent.putExtra("environmentalHazard", environmentalHazardCheck != null ? environmentalHazardCheck.isChecked() : true);

        // Add facility filter states
        resultIntent.putExtra("evacuationCenters", evacuationCentersCheck != null ? evacuationCentersCheck.isChecked() : true);
        resultIntent.putExtra("healthFacilities", healthFacilitiesCheck != null ? healthFacilitiesCheck.isChecked() : true);
        resultIntent.putExtra("policeStations", policeStationsCheck != null ? policeStationsCheck.isChecked() : true);
        resultIntent.putExtra("fireStations", fireStationsCheck != null ? fireStationsCheck.isChecked() : true);
        resultIntent.putExtra("governmentOffices", governmentOfficesCheck != null ? governmentOfficesCheck.isChecked() : true);

        // Set result and finish
        setResult(RESULT_OK, resultIntent);

        // Show confirmation message with filter summary
        String filterSummary = getFilterSummary();
        Toast.makeText(this, "Filters applied successfully!\n" + filterSummary, Toast.LENGTH_LONG).show();

        finish();
    }
    
    /**
     * Update checkbox visual state with animation and feedback
     */
    private void updateCheckboxVisualState(android.widget.CompoundButton checkbox, boolean isChecked) {
        try {
            // Add visual feedback animation
            checkbox.animate()
                .scaleX(isChecked ? 1.1f : 1.0f)
                .scaleY(isChecked ? 1.1f : 1.0f)
                .setDuration(150)
                .withEndAction(() -> {
                    checkbox.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
                })
                .start();
                
            // Update checkbox color based on state
            if (isChecked) {
                checkbox.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.holo_orange_light));
            } else {
                checkbox.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
            }
            
        } catch (Exception e) {
            // Error updating visual state
        }
    }
    
    /**
     * Update filter summary and show current selection count
     */
    private void updateFilterSummary() {
        try {
            // Count selected incident types
            int selectedIncidents = 0;
            int totalIncidents = incidentCheckboxes.size();

            for (CheckBox checkBox : incidentCheckboxes) {
                if (checkBox != null && checkBox.isChecked()) {
                    selectedIncidents++;
                }
            }
            
            // Count selected facilities
            int selectedFacilities = 0;
            int totalFacilities = 5; // Total number of facility types
            
            if (evacuationCentersCheck != null && evacuationCentersCheck.isChecked()) selectedFacilities++;
            if (healthFacilitiesCheck != null && healthFacilitiesCheck.isChecked()) selectedFacilities++;
            if (policeStationsCheck != null && policeStationsCheck.isChecked()) selectedFacilities++;
            if (fireStationsCheck != null && fireStationsCheck.isChecked()) selectedFacilities++;
            if (governmentOfficesCheck != null && governmentOfficesCheck.isChecked()) selectedFacilities++;
            
            // Update apply button text with selection count
            if (applyFiltersButton != null) {
                String buttonText = "Apply Filters";
                if ((totalIncidents > 0 && selectedIncidents < totalIncidents) || selectedFacilities < totalFacilities || 
                    (heatmapSwitch != null && heatmapSwitch.isChecked())) {
                    buttonText += " (" + selectedIncidents + "/" + (totalIncidents == 0 ? "-" : totalIncidents) + " incidents, " + 
                                 selectedFacilities + "/" + totalFacilities + " facilities)";
                }
                applyFiltersButton.setText(buttonText);
            }
            
        } catch (Exception e) {
            // Error updating filter summary
        }
    }
    
    /**
     * Get filter summary for confirmation message
     */
    private String getFilterSummary() {
        try {
            StringBuilder summary = new StringBuilder();
            
            // Add heatmap status
            if (heatmapSwitch != null && heatmapSwitch.isChecked()) {
                summary.append("🔥 Heatmap: ON\n");
            }
            
            // Add timeline
            summary.append("📅 Timeline: ").append(selectedTimeRange).append("\n");
            
            // Count disabled incident types
        int disabledIncidents = 0;
        for (CheckBox checkBox : incidentCheckboxes) {
            if (checkBox != null && !checkBox.isChecked()) {
                disabledIncidents++;
            }
        }
            
            if (disabledIncidents > 0) {
                summary.append("🚫 ").append(disabledIncidents).append(" incident types hidden\n");
            }
            
            // Count disabled facilities
            int disabledFacilities = 0;
            if (evacuationCentersCheck != null && !evacuationCentersCheck.isChecked()) disabledFacilities++;
            if (healthFacilitiesCheck != null && !healthFacilitiesCheck.isChecked()) disabledFacilities++;
            if (policeStationsCheck != null && !policeStationsCheck.isChecked()) disabledFacilities++;
            if (fireStationsCheck != null && !fireStationsCheck.isChecked()) disabledFacilities++;
            if (governmentOfficesCheck != null && !governmentOfficesCheck.isChecked()) disabledFacilities++;
            
            if (disabledFacilities > 0) {
                summary.append("🏢 ").append(disabledFacilities).append(" facility types hidden\n");
            }
            
            // If no filters applied, show default message
            if (disabledIncidents == 0 && disabledFacilities == 0 && 
                (heatmapSwitch == null || !heatmapSwitch.isChecked())) {
                summary.append("All filters enabled");
            }
            
            return summary.toString();
            
        } catch (Exception e) {
            return "Filters applied";
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}