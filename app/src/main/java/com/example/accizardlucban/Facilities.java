package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.RadioButton;
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
    private LinearLayout emergencySupportSection;
    private ImageView emergencySupportArrow;

    // Switches and radio buttons
    private Switch heatmapSwitch;
    private RadioButton roadAccidentCheck, fireCheck, medicalEmergencyCheck, floodingCheck;
    private RadioButton volcanicActivityCheck, landslideCheck, earthquakeCheck, civilDisturbanceCheck;
    private RadioButton armedConflictCheck, infectiousDiseaseCheck;
    private RadioButton poorInfrastructureCheck, obstructionsCheck, electricalHazardCheck, environmentalHazardCheck;
    private RadioButton animalConcernsCheck;
    private final List<RadioButton> incidentCheckboxes = new ArrayList<>();
    private boolean isUpdatingIncidentChecks = false;
    private boolean isUpdatingFacilityChecks = false;
    private RadioButton evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck;
    private RadioButton fireStationsCheck, governmentOfficesCheck;

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
    
    // ScrollView for scrolling content
    private android.widget.ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("FacilitiesClickDebug", "=== onCreate: START ===");
        setContentView(R.layout.activity_facilities);

        initializeViews();
        loadFilterStates();
        setupClickListeners();
        setupButtons();
        setupTimelineOptions();
        setupSelectAllButtons();
        updateFilterSummary(); // Initialize filter summary display
        
        // Log final state of all checkboxes
        logAllCheckboxStates("onCreate: FINAL STATE");
        android.util.Log.d("FacilitiesClickDebug", "=== onCreate: END ===");
    }
    
    /**
     * Log the state of all checkboxes for debugging
     */
    private void logAllCheckboxStates(String context) {
        android.util.Log.d("FacilitiesClickDebug", "=== logAllCheckboxStates: " + context + " ===");
        android.util.Log.d("FacilitiesClickDebug", "isUpdatingIncidentChecks=" + isUpdatingIncidentChecks + ", isUpdatingFacilityChecks=" + isUpdatingFacilityChecks);
        
        android.util.Log.d("FacilitiesClickDebug", "--- INCIDENT CHECKBOXES ---");
        for (RadioButton checkBox : incidentCheckboxes) {
            if (checkBox != null) {
                android.util.Log.d("FacilitiesClickDebug", "Incident: " + checkBox.getText() + 
                    " | enabled=" + checkBox.isEnabled() + 
                    " | clickable=" + checkBox.isClickable() + 
                    " | checked=" + checkBox.isChecked() +
                    " | focusable=" + checkBox.isFocusable() +
                    " | focusableInTouchMode=" + checkBox.isFocusableInTouchMode());
            }
        }
        
        android.util.Log.d("FacilitiesClickDebug", "--- FACILITY CHECKBOXES ---");
        RadioButton[] facilityCheckboxes = {
            evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
            fireStationsCheck, governmentOfficesCheck
        };
        for (RadioButton checkBox : facilityCheckboxes) {
            if (checkBox != null) {
                android.util.Log.d("FacilitiesClickDebug", "Facility: " + checkBox.getText() + 
                    " | enabled=" + checkBox.isEnabled() + 
                    " | clickable=" + checkBox.isClickable() + 
                    " | checked=" + checkBox.isChecked() +
                    " | focusable=" + checkBox.isFocusable() +
                    " | focusableInTouchMode=" + checkBox.isFocusableInTouchMode());
            }
        }
        android.util.Log.d("FacilitiesClickDebug", "=== logAllCheckboxStates: END ===");
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
        emergencySupportSection = findViewById(R.id.emergencySupportSection);
        emergencySupportArrow = findViewById(R.id.emergencySupportArrow);

        // Switches and radio buttons
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
        animalConcernsCheck = findViewById(R.id.animalConcernsCheck);

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
        if (animalConcernsCheck == null) {
            android.util.Log.e("Facilities", "animalConcernsCheck is NULL!");
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
        registerIncidentCheckbox(animalConcernsCheck);
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
        
        // ScrollView
        scrollView = findViewById(R.id.mainScrollView);

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
        
        // Initialize RadioButton styling
        initializeRadioButtonStyling();
        
        // Ensure all checkboxes are enabled and clickable from the start
        ensureAllCheckboxesEnabled();
        
        // Log layout measurements for debugging
        logLayoutMeasurements();
        
        // Ensure all content is visible after layout is complete
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                // Force layout pass to ensure proper measurements
                scrollView.requestLayout();
                View scrollContent = scrollView.getChildAt(0);
                if (scrollContent != null) {
                    scrollContent.requestLayout();
                }
                
                logLayoutMeasurements();
                // If Emergency Support section is already expanded on create, scroll to it
                if (emergencySupportContent != null && 
                    emergencySupportContent.getVisibility() == View.VISIBLE) {
                    scrollToEmergencySupportSection();
                }
            }
        });
    }
    
    /**
     * Log layout measurements for debugging scroll issues
     * Search for "FacilitiesScrollDebug" in Logcat
     */
    private void logLayoutMeasurements() {
        if (scrollView != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        View child = scrollView.getChildAt(0);
                        if (child != null) {
                            int scrollViewHeight = scrollView.getHeight();
                            int scrollViewMeasuredHeight = scrollView.getMeasuredHeight();
                            int contentHeight = child.getHeight();
                            int contentMeasuredHeight = child.getMeasuredHeight();
                            int scrollY = scrollView.getScrollY();
                            int maxScrollY = Math.max(0, contentHeight - scrollViewHeight);
                            
                            android.util.Log.d("FacilitiesScrollDebug", "=== SCROLLVIEW MEASUREMENTS ===");
                            android.util.Log.d("FacilitiesScrollDebug", "ScrollView height: " + scrollViewHeight);
                            android.util.Log.d("FacilitiesScrollDebug", "ScrollView measuredHeight: " + scrollViewMeasuredHeight);
                            android.util.Log.d("FacilitiesScrollDebug", "Content height: " + contentHeight);
                            android.util.Log.d("FacilitiesScrollDebug", "Content measuredHeight: " + contentMeasuredHeight);
                            android.util.Log.d("FacilitiesScrollDebug", "Current scrollY: " + scrollY);
                            android.util.Log.d("FacilitiesScrollDebug", "Max scrollY: " + maxScrollY);
                            android.util.Log.d("FacilitiesScrollDebug", "Can scroll: " + (maxScrollY > 0));
                            
                            if (emergencySupportSection != null) {
                                int sectionTop = emergencySupportSection.getTop();
                                int sectionBottom = emergencySupportSection.getBottom();
                                int sectionHeight = emergencySupportSection.getHeight();
                                android.util.Log.d("FacilitiesScrollDebug", "Emergency Support Section:");
                                android.util.Log.d("FacilitiesScrollDebug", "  Top: " + sectionTop);
                                android.util.Log.d("FacilitiesScrollDebug", "  Bottom: " + sectionBottom);
                                android.util.Log.d("FacilitiesScrollDebug", "  Height: " + sectionHeight);
                                android.util.Log.d("FacilitiesScrollDebug", "  Visible: " + (emergencySupportContent != null ? 
                                    (emergencySupportContent.getVisibility() == View.VISIBLE) : "null"));
                            }
                            
                            if (governmentOfficesCheck != null) {
                                int govTop = governmentOfficesCheck.getTop();
                                int govBottom = governmentOfficesCheck.getBottom();
                                android.util.Log.d("FacilitiesScrollDebug", "Government Offices:");
                                android.util.Log.d("FacilitiesScrollDebug", "  Top: " + govTop);
                                android.util.Log.d("FacilitiesScrollDebug", "  Bottom: " + govBottom);
                                android.util.Log.d("FacilitiesScrollDebug", "  Visible in viewport: " + 
                                    (govBottom <= scrollViewHeight + scrollY));
                            }
                            
                            android.util.Log.d("FacilitiesScrollDebug", "==============================");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FacilitiesScrollDebug", "Error logging measurements: " + e.getMessage(), e);
                    }
                }
            });
        }
    }
    
    /**
     * Initialize all RadioButtons with proper RadioButton styling
     */
    private void initializeRadioButtonStyling() {
        // Apply RadioButton color state list to all incident RadioButtons
        for (RadioButton radioButton : incidentCheckboxes) {
            if (radioButton != null) {
                radioButton.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
            }
        }
        
        // Apply RadioButton color state list to all facility RadioButtons
        if (evacuationCentersCheck != null) {
            evacuationCentersCheck.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
        }
        if (healthFacilitiesCheck != null) {
            healthFacilitiesCheck.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
        }
        if (policeStationsCheck != null) {
            policeStationsCheck.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
        }
        if (fireStationsCheck != null) {
            fireStationsCheck.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
        }
        if (governmentOfficesCheck != null) {
            governmentOfficesCheck.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
        }
    }

    private void registerIncidentCheckbox(RadioButton checkBox) {
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
        if (animalConcernsCheck != null) {
            animalConcernsCheck.setChecked(intent.getBooleanExtra("animalConcerns", true));
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

        // Enforce mutual exclusivity: if an incident is selected, deselect all facilities
        // If a facility is selected, deselect all incidents
        boolean hasSelectedIncident = false;
        for (RadioButton checkBox : incidentCheckboxes) {
            if (checkBox != null && checkBox.isChecked()) {
                hasSelectedIncident = true;
                break;
            }
        }

        boolean hasSelectedFacility = false;
        if (evacuationCentersCheck != null && evacuationCentersCheck.isChecked()) hasSelectedFacility = true;
        if (healthFacilitiesCheck != null && healthFacilitiesCheck.isChecked()) hasSelectedFacility = true;
        if (policeStationsCheck != null && policeStationsCheck.isChecked()) hasSelectedFacility = true;
        if (fireStationsCheck != null && fireStationsCheck.isChecked()) hasSelectedFacility = true;
        if (governmentOfficesCheck != null && governmentOfficesCheck.isChecked()) hasSelectedFacility = true;

        // If both have selections, prioritize incidents and deselect facilities
        if (hasSelectedIncident && hasSelectedFacility) {
            deselectAllFacilities();
        }

        // Update timeline selection UI
        updateTimelineSelection();
        
        // Ensure all checkboxes are enabled and clickable after loading states
        ensureAllCheckboxesEnabled();
    }

    private void setupClickListeners() {
        // Timeline section click listener
        if (timelineHeader != null) {
            timelineHeader.setOnClickListener(v -> {
                ensureAllCheckboxesEnabled();
                toggleSection("timeline");
            });
        }

        // Incident Types section click listener
        if (incidentTypesHeader != null) {
            incidentTypesHeader.setOnClickListener(v -> {
                ensureAllCheckboxesEnabled();
                toggleSection("incidents");
            });
        }

        // Emergency Support section click listener
        if (emergencySupportHeader != null) {
            emergencySupportHeader.setOnClickListener(v -> {
                ensureAllCheckboxesEnabled();
                toggleSection("facilities");
            });
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
        setupExclusiveIncidentCheckbox(animalConcernsCheck);
        
        // Facility checkboxes
        RadioButton[] facilityCheckboxes = {
            evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
            fireStationsCheck, governmentOfficesCheck
        };
        
        for (RadioButton checkBox : facilityCheckboxes) {
            if (checkBox != null) {
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                checkBox.setFocusable(true);
                checkBox.setFocusableInTouchMode(true);
                android.util.Log.d("Facilities", "setupCheckboxListeners: " + checkBox.getText() + " enabled=" + checkBox.isEnabled() + ", clickable=" + checkBox.isClickable());
                
                // Add onClick listener to ensure all checkboxes are enabled BEFORE the click is processed
                checkBox.setOnClickListener(v -> {
                    android.util.Log.d("FacilitiesClickDebug", "*** onClick FIRED: Facility=" + checkBox.getText() + 
                        " | enabled=" + checkBox.isEnabled() + 
                        " | clickable=" + checkBox.isClickable() + 
                        " | checked=" + checkBox.isChecked() +
                        " | isUpdatingFacilityChecks=" + isUpdatingFacilityChecks);
                    // CRITICAL: Immediately enable ALL checkboxes in BOTH sections before processing click
                    // This ensures users can click checkboxes in the other section without manually deselecting
                    ensureAllCheckboxesEnabled();
                    // Force enable incident checkboxes specifically
                    for (RadioButton incidentCheck : incidentCheckboxes) {
                        if (incidentCheck != null) {
                            boolean wasEnabled = incidentCheck.isEnabled();
                            boolean wasClickable = incidentCheck.isClickable();
                            incidentCheck.setEnabled(true);
                            incidentCheck.setClickable(true);
                            incidentCheck.setFocusable(true);
                            incidentCheck.setFocusableInTouchMode(true);
                            android.util.Log.d("FacilitiesClickDebug", "onClick: Force enabled incident=" + incidentCheck.getText() + 
                                " | wasEnabled=" + wasEnabled + " -> enabled=" + incidentCheck.isEnabled() +
                                " | wasClickable=" + wasClickable + " -> clickable=" + incidentCheck.isClickable());
                        }
                    }
                });
                
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    android.util.Log.d("FacilitiesClickDebug", "*** onCheckedChange FIRED: Facility=" + checkBox.getText() + 
                        ", isChecked=" + isChecked + 
                        " | enabled=" + checkBox.isEnabled() + 
                        " | clickable=" + checkBox.isClickable() +
                        " | isUpdatingFacilityChecks=" + isUpdatingFacilityChecks);
                    handleFacilityCheckboxChange(checkBox, isChecked);
                });
            }
        }
        
        // Heatmap switch
        if (heatmapSwitch != null) {
            heatmapSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateFilterSummary();
            });
        }
    }

    private void setupExclusiveIncidentCheckbox(RadioButton checkBox) {
        if (checkBox == null) {
            return;
        }

        // Ensure checkbox is enabled, clickable, and focusable
        checkBox.setEnabled(true);
        checkBox.setClickable(true);
        checkBox.setFocusable(true);
        checkBox.setFocusableInTouchMode(true);
        android.util.Log.d("Facilities", "setupExclusiveIncidentCheckbox: " + checkBox.getText() + " enabled=" + checkBox.isEnabled() + ", clickable=" + checkBox.isClickable());

        // Add onClick listener to ensure all checkboxes are enabled BEFORE the click is processed
        checkBox.setOnClickListener(v -> {
            android.util.Log.d("FacilitiesClickDebug", "*** onClick FIRED: Incident=" + checkBox.getText() + 
                " | enabled=" + checkBox.isEnabled() + 
                " | clickable=" + checkBox.isClickable() + 
                " | checked=" + checkBox.isChecked() +
                " | isUpdatingIncidentChecks=" + isUpdatingIncidentChecks);
            // CRITICAL: Immediately enable ALL checkboxes in BOTH sections before processing click
            // This ensures users can click checkboxes in the other section without manually deselecting
            ensureAllCheckboxesEnabled();
            // Force enable facility checkboxes specifically
            RadioButton[] facilityCheckboxes = {
                evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
                fireStationsCheck, governmentOfficesCheck
            };
            for (RadioButton facilityCheck : facilityCheckboxes) {
                if (facilityCheck != null) {
                    boolean wasEnabled = facilityCheck.isEnabled();
                    boolean wasClickable = facilityCheck.isClickable();
                    facilityCheck.setEnabled(true);
                    facilityCheck.setClickable(true);
                    facilityCheck.setFocusable(true);
                    facilityCheck.setFocusableInTouchMode(true);
                    android.util.Log.d("FacilitiesClickDebug", "onClick: Force enabled facility=" + facilityCheck.getText() + 
                        " | wasEnabled=" + wasEnabled + " -> enabled=" + facilityCheck.isEnabled() +
                        " | wasClickable=" + wasClickable + " -> clickable=" + facilityCheck.isClickable());
                }
            }
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.util.Log.d("FacilitiesClickDebug", "*** onCheckedChange FIRED: Incident=" + checkBox.getText() + 
                ", isChecked=" + isChecked + 
                " | enabled=" + checkBox.isEnabled() + 
                " | clickable=" + checkBox.isClickable() +
                " | isUpdatingIncidentChecks=" + isUpdatingIncidentChecks);
            handleIncidentCheckboxChange(checkBox, isChecked);
        });
    }

    private void handleIncidentCheckboxChange(RadioButton changedCheckBox, boolean isChecked) {
        android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: START | checkbox=" + changedCheckBox.getText() + 
            " | isChecked=" + isChecked + 
            " | isUpdatingIncidentChecks=" + isUpdatingIncidentChecks +
            " | checkbox.enabled=" + changedCheckBox.isEnabled() +
            " | checkbox.clickable=" + changedCheckBox.isClickable());
        
        // CRITICAL: Ensure ALL checkboxes are enabled and clickable BEFORE processing the change
        // This allows users to click checkboxes in the other section without manually deselecting first
        ensureAllCheckboxesEnabled();
        
        // Prevent recursive calls, but don't block the click - just skip if already processing
        if (isUpdatingIncidentChecks) {
            android.util.Log.w("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Already updating, but ensuring checkboxes are enabled");
            ensureAllCheckboxesEnabled();
            android.util.Log.d("FacilitiesClickDebug", "<<< handleIncidentCheckboxChange: EARLY RETURN (already updating)");
            return;
        }

        isUpdatingIncidentChecks = true;
        android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Set isUpdatingIncidentChecks=true");
        try {
            if (isChecked) {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Incident checked, deselecting all facilities");
                // IMMEDIATELY deselect ALL facility checkboxes when ANY incident type is selected
                // This ensures mutual exclusivity - facilities must be empty when incidents are selected
                deselectAllFacilities();
                
                // Then deselect all other incident types (only one incident can be selected at a time)
                for (RadioButton other : incidentCheckboxes) {
                    if (other != null && other != changedCheckBox && other.isChecked()) {
                        android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Deselecting other incident=" + other.getText());
                        other.setChecked(false);
                        updateCheckboxVisualState(other, false);
                    }
                }
            } else {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Incident unchecked");
                // When an incident is unchecked, we don't need to do anything special
                // Facilities can remain as they are
            }
            
            // ALWAYS ensure all checkboxes remain enabled and clickable after any change
            android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: Ensuring all checkboxes enabled before finish");
            ensureAllCheckboxesEnabled();

            updateCheckboxVisualState(changedCheckBox, isChecked);
            updateFilterSummary();
        } finally {
            isUpdatingIncidentChecks = false;
            android.util.Log.d("FacilitiesClickDebug", "<<< handleIncidentCheckboxChange: Set isUpdatingIncidentChecks=false");
            // Use post to ensure checkboxes are enabled after UI updates
            changedCheckBox.post(() -> {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleIncidentCheckboxChange: POST handler - ensuring all enabled");
                ensureAllCheckboxesEnabled();
                // Force enable again to ensure they're clickable
                for (RadioButton checkBox : incidentCheckboxes) {
                    if (checkBox != null) {
                        boolean wasEnabled = checkBox.isEnabled();
                        boolean wasClickable = checkBox.isClickable();
                        checkBox.setEnabled(true);
                        checkBox.setClickable(true);
                        android.util.Log.d("FacilitiesClickDebug", "POST: Force enabled incident=" + checkBox.getText() + 
                            " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() +
                            " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable());
                    }
                }
                RadioButton[] facilityCheckboxes = {
                    evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
                    fireStationsCheck, governmentOfficesCheck
                };
                for (RadioButton checkBox : facilityCheckboxes) {
                    if (checkBox != null) {
                        boolean wasEnabled = checkBox.isEnabled();
                        boolean wasClickable = checkBox.isClickable();
                        checkBox.setEnabled(true);
                        checkBox.setClickable(true);
                        android.util.Log.d("FacilitiesClickDebug", "POST: Force enabled facility=" + checkBox.getText() + 
                            " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() +
                            " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable());
                    }
                }
                android.util.Log.d("FacilitiesClickDebug", "<<< handleIncidentCheckboxChange: POST handler complete");
            });
            android.util.Log.d("FacilitiesClickDebug", "<<< handleIncidentCheckboxChange: END");
        }
    }

    private void handleFacilityCheckboxChange(RadioButton changedCheckBox, boolean isChecked) {
        android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: START | checkbox=" + changedCheckBox.getText() + 
            " | isChecked=" + isChecked + 
            " | isUpdatingFacilityChecks=" + isUpdatingFacilityChecks +
            " | checkbox.enabled=" + changedCheckBox.isEnabled() +
            " | checkbox.clickable=" + changedCheckBox.isClickable());
        
        // CRITICAL: Ensure ALL checkboxes are enabled and clickable BEFORE processing the change
        // This allows users to click checkboxes in the other section without manually deselecting first
        ensureAllCheckboxesEnabled();
        
        // Prevent recursive calls, but don't block the click - just skip if already processing
        if (isUpdatingFacilityChecks) {
            android.util.Log.w("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: Already updating, but ensuring checkboxes are enabled");
            ensureAllCheckboxesEnabled();
            android.util.Log.d("FacilitiesClickDebug", "<<< handleFacilityCheckboxChange: EARLY RETURN (already updating)");
            return;
        }

        isUpdatingFacilityChecks = true;
        android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: Set isUpdatingFacilityChecks=true");
        try {
            if (isChecked) {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: Facility checked, deselecting all incidents");
                // IMMEDIATELY deselect ALL incident type checkboxes when ANY facility is selected
                // This ensures mutual exclusivity - incidents must be empty when facilities are selected
                deselectAllIncidents();
            } else {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: Facility unchecked");
            }
            
            // ALWAYS ensure all checkboxes remain enabled and clickable after any change
            android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: Ensuring all checkboxes enabled before finish");
            ensureAllCheckboxesEnabled();

            updateCheckboxVisualState(changedCheckBox, isChecked);
            updateFilterSummary();
        } finally {
            isUpdatingFacilityChecks = false;
            android.util.Log.d("FacilitiesClickDebug", "<<< handleFacilityCheckboxChange: Set isUpdatingFacilityChecks=false");
            // Use post to ensure checkboxes are enabled after UI updates
            changedCheckBox.post(() -> {
                android.util.Log.d("FacilitiesClickDebug", ">>> handleFacilityCheckboxChange: POST handler - ensuring all enabled");
                ensureAllCheckboxesEnabled();
                // Force enable again to ensure they're clickable
                for (RadioButton checkBox : incidentCheckboxes) {
                    if (checkBox != null) {
                        boolean wasEnabled = checkBox.isEnabled();
                        boolean wasClickable = checkBox.isClickable();
                        checkBox.setEnabled(true);
                        checkBox.setClickable(true);
                        android.util.Log.d("FacilitiesClickDebug", "POST: Force enabled incident=" + checkBox.getText() + 
                            " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() +
                            " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable());
                    }
                }
                RadioButton[] facilityCheckboxes = {
                    evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
                    fireStationsCheck, governmentOfficesCheck
                };
                for (RadioButton checkBox : facilityCheckboxes) {
                    if (checkBox != null) {
                        boolean wasEnabled = checkBox.isEnabled();
                        boolean wasClickable = checkBox.isClickable();
                        checkBox.setEnabled(true);
                        checkBox.setClickable(true);
                        android.util.Log.d("FacilitiesClickDebug", "POST: Force enabled facility=" + checkBox.getText() + 
                            " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() +
                            " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable());
                    }
                }
                android.util.Log.d("FacilitiesClickDebug", "<<< handleFacilityCheckboxChange: POST handler complete");
            });
            android.util.Log.d("FacilitiesClickDebug", "<<< handleFacilityCheckboxChange: END");
        }
    }

    private void enforceSingleIncidentSelection() {
        if (incidentCheckboxes.isEmpty()) {
            return;
        }

        boolean foundChecked = false;
        isUpdatingIncidentChecks = true;
        try {
            for (RadioButton checkBox : incidentCheckboxes) {
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

    /**
     * Deselect all facility checkboxes
     */
    private void deselectAllFacilities() {
        android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: START | isUpdatingFacilityChecks=" + isUpdatingFacilityChecks);
        // Set flag first to prevent listener from processing
        boolean wasUpdating = isUpdatingFacilityChecks;
        if (!wasUpdating) {
            isUpdatingFacilityChecks = true;
            android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: Set isUpdatingFacilityChecks=true");
        }
        try {
            // Ensure all facilities are enabled and clickable BEFORE deselecting
            ensureFacilitiesEnabled();
            
            // Deselect all facilities - call setChecked(false) on all regardless of current state
            // This ensures they are all unchecked when an incident is selected
            RadioButton[] facilityCheckboxes = {
                evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
                fireStationsCheck, governmentOfficesCheck
            };
            
            for (RadioButton checkBox : facilityCheckboxes) {
                if (checkBox != null) {
                    boolean beforeEnabled = checkBox.isEnabled();
                    boolean beforeClickable = checkBox.isClickable();
                    boolean beforeChecked = checkBox.isChecked();
                    android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: BEFORE | " + checkBox.getText() + 
                        " | enabled=" + beforeEnabled + 
                        " | clickable=" + beforeClickable + 
                        " | checked=" + beforeChecked);
                    checkBox.setChecked(false);
                    // Ensure it remains enabled and clickable after deselection
                    checkBox.setEnabled(true);
                    checkBox.setClickable(true);
                    checkBox.setFocusable(true);
                    checkBox.setFocusableInTouchMode(true);
                    android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: AFTER | " + checkBox.getText() + 
                        " | enabled=" + checkBox.isEnabled() + 
                        " | clickable=" + checkBox.isClickable() + 
                        " | checked=" + checkBox.isChecked() +
                        " | focusable=" + checkBox.isFocusable());
                }
            }
            android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: Finished deselection");
        } finally {
            // Only reset flag if we weren't already updating
            if (!wasUpdating) {
                isUpdatingFacilityChecks = false;
                android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllFacilities: Set isUpdatingFacilityChecks=false");
            }
            // Ensure all facilities remain enabled and clickable after deselection
            ensureFacilitiesEnabled();
            android.util.Log.d("FacilitiesClickDebug", "<<< deselectAllFacilities: END");
        }
    }

    /**
     * Deselect all incident type checkboxes
     */
    private void deselectAllIncidents() {
        android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: START | isUpdatingIncidentChecks=" + isUpdatingIncidentChecks);
        // Set flag first to prevent listener from processing
        boolean wasUpdating = isUpdatingIncidentChecks;
        if (!wasUpdating) {
            isUpdatingIncidentChecks = true;
            android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: Set isUpdatingIncidentChecks=true");
        }
        try {
            // Ensure all incidents are enabled and clickable BEFORE deselecting
            ensureIncidentsEnabled();
            
            // Deselect all incidents - call setChecked(false) on all regardless of current state
            // This ensures they are all unchecked when a facility is selected
            for (RadioButton checkBox : incidentCheckboxes) {
                if (checkBox != null) {
                    boolean beforeEnabled = checkBox.isEnabled();
                    boolean beforeClickable = checkBox.isClickable();
                    boolean beforeChecked = checkBox.isChecked();
                    android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: BEFORE | " + checkBox.getText() + 
                        " | enabled=" + beforeEnabled + 
                        " | clickable=" + beforeClickable + 
                        " | checked=" + beforeChecked);
                    checkBox.setChecked(false);
                    // Ensure it remains enabled and clickable after deselection
                    checkBox.setEnabled(true);
                    checkBox.setClickable(true);
                    checkBox.setFocusable(true);
                    checkBox.setFocusableInTouchMode(true);
                    android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: AFTER | " + checkBox.getText() + 
                        " | enabled=" + checkBox.isEnabled() + 
                        " | clickable=" + checkBox.isClickable() + 
                        " | checked=" + checkBox.isChecked() +
                        " | focusable=" + checkBox.isFocusable());
                }
            }
            android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: Finished deselection");
        } finally {
            // Only reset flag if we weren't already updating
            if (!wasUpdating) {
                isUpdatingIncidentChecks = false;
                android.util.Log.d("FacilitiesClickDebug", ">>> deselectAllIncidents: Set isUpdatingIncidentChecks=false");
            }
            // Ensure all incidents remain enabled and clickable after deselection
            ensureIncidentsEnabled();
            android.util.Log.d("FacilitiesClickDebug", "<<< deselectAllIncidents: END");
        }
    }

    /**
     * Ensure all facility checkboxes are enabled and clickable
     */
    private void ensureFacilitiesEnabled() {
        android.util.Log.d("FacilitiesClickDebug", "=== ensureFacilitiesEnabled: START ===");
        RadioButton[] facilityCheckboxes = {
            evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck,
            fireStationsCheck, governmentOfficesCheck
        };
        
        for (RadioButton checkBox : facilityCheckboxes) {
            if (checkBox != null) {
                boolean wasEnabled = checkBox.isEnabled();
                boolean wasClickable = checkBox.isClickable();
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                checkBox.setFocusable(true);
                checkBox.setFocusableInTouchMode(true);
                android.util.Log.d("FacilitiesClickDebug", "ensureFacilitiesEnabled: " + checkBox.getText() + 
                    " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() + 
                    " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable() +
                    " | checked=" + checkBox.isChecked() +
                    " | focusable=" + checkBox.isFocusable() +
                    " | focusableInTouchMode=" + checkBox.isFocusableInTouchMode());
            } else {
                android.util.Log.w("FacilitiesClickDebug", "ensureFacilitiesEnabled: Found NULL checkbox!");
            }
        }
        android.util.Log.d("FacilitiesClickDebug", "=== ensureFacilitiesEnabled: END ===");
    }

    /**
     * Ensure all incident checkboxes are enabled and clickable
     */
    private void ensureIncidentsEnabled() {
        android.util.Log.d("FacilitiesClickDebug", "=== ensureIncidentsEnabled: START ===");
        for (RadioButton checkBox : incidentCheckboxes) {
            if (checkBox != null) {
                boolean wasEnabled = checkBox.isEnabled();
                boolean wasClickable = checkBox.isClickable();
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                checkBox.setFocusable(true);
                checkBox.setFocusableInTouchMode(true);
                android.util.Log.d("FacilitiesClickDebug", "ensureIncidentsEnabled: " + checkBox.getText() + 
                    " | wasEnabled=" + wasEnabled + " -> enabled=" + checkBox.isEnabled() + 
                    " | wasClickable=" + wasClickable + " -> clickable=" + checkBox.isClickable() +
                    " | checked=" + checkBox.isChecked() +
                    " | focusable=" + checkBox.isFocusable() +
                    " | focusableInTouchMode=" + checkBox.isFocusableInTouchMode());
            } else {
                android.util.Log.w("FacilitiesClickDebug", "ensureIncidentsEnabled: Found NULL checkbox!");
            }
        }
        android.util.Log.d("FacilitiesClickDebug", "=== ensureIncidentsEnabled: END ===");
    }
    
    /**
     * Ensure all checkboxes (both incidents and facilities) are enabled and clickable
     */
    private void ensureAllCheckboxesEnabled() {
        android.util.Log.d("FacilitiesClickDebug", "=== ensureAllCheckboxesEnabled: START ===");
        android.util.Log.d("FacilitiesClickDebug", "ensureAllCheckboxesEnabled: isUpdatingIncidentChecks=" + isUpdatingIncidentChecks + ", isUpdatingFacilityChecks=" + isUpdatingFacilityChecks);
        ensureIncidentsEnabled();
        ensureFacilitiesEnabled();
        android.util.Log.d("FacilitiesClickDebug", "=== ensureAllCheckboxesEnabled: END ===");
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
        
        // If Emergency Support section is expanded, scroll to show it fully
        if (expand && content == emergencySupportContent && scrollView != null && emergencySupportSection != null) {
            android.util.Log.d("FacilitiesScrollDebug", "Emergency Support section expanded - scheduling scroll");
            
            // Wait for animation to complete and layout to be measured
            // Use ViewTreeObserver to ensure layout is complete
            final ViewTreeObserver observer = emergencySupportSection.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove listener to avoid multiple calls
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        emergencySupportSection.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        emergencySupportSection.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    
                    // Scroll to show the Emergency Support section fully
                    scrollToEmergencySupportSection();
                }
            });
            
            // Fallback: also try after animation duration
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToEmergencySupportSection();
                }
            }, 350); // Slightly longer than animation duration
        }
    }
    
    /**
     * Scroll to show the Emergency Support section fully when expanded
     * This method calculates the position of the section and scrolls to ensure all content is visible
     */
    private void scrollToEmergencySupportSection() {
        if (scrollView == null || emergencySupportSection == null || emergencySupportContent == null) {
            return;
        }
        
        if (emergencySupportContent.getVisibility() != View.VISIBLE) {
            return; // Section is not expanded
        }
        
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // Use ViewTreeObserver to ensure layout is complete
                    ViewTreeObserver observer = emergencySupportSection.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // Remove listener to avoid multiple calls
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                emergencySupportSection.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                emergencySupportSection.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            
                            performScrollToSection();
                        }
                    });
                    
                    // Also perform scroll immediately in case layout is already complete
                    performScrollToSection();
                } catch (Exception e) {
                    android.util.Log.e("FacilitiesScrollDebug", "Error in scrollToEmergencySupportSection: " + e.getMessage(), e);
                    // Fallback: just scroll to bottom
                    scrollView.fullScroll(android.view.View.FOCUS_DOWN);
                }
            }
        });
    }
    
    /**
     * Helper method to actually perform the scroll calculation and execution
     */
    private void performScrollToSection() {
        try {
            // Get the ScrollView's child container
            View scrollContent = scrollView.getChildAt(0);
            if (scrollContent == null || emergencySupportSection == null) {
                return;
            }
            
            // Get screen positions to calculate relative position
            int[] sectionLocation = new int[2];
            int[] scrollViewLocation = new int[2];
            int[] contentLocation = new int[2];
            
            emergencySupportSection.getLocationOnScreen(sectionLocation);
            scrollView.getLocationOnScreen(scrollViewLocation);
            scrollContent.getLocationOnScreen(contentLocation);
            
            // Calculate section position relative to scrollContent
            int sectionTopRelative = sectionLocation[1] - contentLocation[1];
            int sectionBottomRelative = sectionTopRelative + emergencySupportSection.getHeight();
            
            // Get ScrollView dimensions and current scroll position
            int scrollViewHeight = scrollView.getHeight();
            int currentScrollY = scrollView.getScrollY();
            int maxScrollY = Math.max(0, scrollContent.getHeight() - scrollViewHeight);
            
            // Add padding to ensure full visibility (accounts for bottom buttons and spacing)
            int bottomPadding = (int)(80 * getResources().getDisplayMetrics().density); // 80dp for buttons + margin
            int targetScrollY = Math.max(0, Math.min(sectionBottomRelative - scrollViewHeight + bottomPadding, maxScrollY));
            
            android.util.Log.d("FacilitiesScrollDebug", "Scrolling to Emergency Support: " +
                "sectionTop=" + sectionTopRelative + 
                ", sectionBottom=" + sectionBottomRelative +
                ", scrollViewHeight=" + scrollViewHeight +
                ", currentScrollY=" + currentScrollY +
                ", targetScrollY=" + targetScrollY +
                ", maxScrollY=" + maxScrollY);
            
            // Scroll if the section is not fully visible
            // Store values that need to be accessed in the delayed runnable
            final int finalScrollViewHeight = scrollViewHeight;
            final int finalBottomPadding = bottomPadding;
            final int finalMaxScrollY = maxScrollY;
            
            if (targetScrollY > currentScrollY || sectionBottomRelative > (currentScrollY + scrollViewHeight)) {
                // Use smooth scroll for better UX
                scrollView.smoothScrollTo(0, targetScrollY);
                
                // Double-check after animation completes (smoothScrollTo takes ~250ms)
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int finalScrollY = scrollView.getScrollY();
                            int finalVisibleBottom = finalScrollY + finalScrollViewHeight;
                            
                            // Recalculate section bottom with current scroll
                            int[] finalSectionLocation = new int[2];
                            int[] finalContentLocation = new int[2];
                            emergencySupportSection.getLocationOnScreen(finalSectionLocation);
                            scrollContent.getLocationOnScreen(finalContentLocation);
                            int finalSectionTopRelative = finalSectionLocation[1] - finalContentLocation[1];
                            int finalSectionBottomRelative = finalSectionTopRelative + emergencySupportSection.getHeight();
                            
                            if (finalSectionBottomRelative > finalVisibleBottom - (int)(20 * getResources().getDisplayMetrics().density)) {
                                // Need to scroll more - use instant scroll for final adjustment
                                int finalTargetScrollY = Math.max(0, Math.min(
                                    finalSectionBottomRelative - finalScrollViewHeight + finalBottomPadding,
                                    finalMaxScrollY));
                                scrollView.scrollTo(0, finalTargetScrollY);
                                android.util.Log.d("FacilitiesScrollDebug", "Final adjustment scroll to: " + finalTargetScrollY);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("FacilitiesScrollDebug", "Error in delayed scroll check: " + e.getMessage(), e);
                        }
                    }
                }, 400); // Wait for smoothScroll animation to complete
            } else {
                android.util.Log.d("FacilitiesScrollDebug", "Emergency Support section already fully visible");
            }
        } catch (Exception e) {
            android.util.Log.e("FacilitiesScrollDebug", "Error in performScrollToSection: " + e.getMessage(), e);
            // Fallback: just scroll to bottom
            scrollView.fullScroll(android.view.View.FOCUS_DOWN);
        }
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
        try {
            if (select) {
                // Deselect all facilities when selecting an incident
                deselectAllFacilities();
                
                boolean selectionMade = false;
                for (RadioButton checkBox : incidentCheckboxes) {
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
                for (RadioButton checkBox : incidentCheckboxes) {
                    if (checkBox == null) continue;
                    checkBox.setChecked(false);
                    updateCheckboxVisualState(checkBox, false);
                }
                Toast.makeText(this, "All incidents deselected", Toast.LENGTH_SHORT).show();
            }
            updateFilterSummary();
        } finally {
            isUpdatingIncidentChecks = false;
        }
    }

    private void selectAllFacilities(boolean select) {
        isUpdatingFacilityChecks = true;
        try {
            if (select) {
                // Deselect all incidents when selecting facilities
                deselectAllIncidents();
            }
            
            if (evacuationCentersCheck != null) evacuationCentersCheck.setChecked(select);
            if (healthFacilitiesCheck != null) healthFacilitiesCheck.setChecked(select);
            if (policeStationsCheck != null) policeStationsCheck.setChecked(select);
            if (fireStationsCheck != null) fireStationsCheck.setChecked(select);
            if (governmentOfficesCheck != null) governmentOfficesCheck.setChecked(select);

            Toast.makeText(this, select ? "All facilities selected" : "All facilities deselected", Toast.LENGTH_SHORT).show();
            updateFilterSummary();
        } finally {
            isUpdatingFacilityChecks = false;
        }
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
        resultIntent.putExtra("animalConcerns", animalConcernsCheck != null ? animalConcernsCheck.isChecked() : true);

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
     * Update RadioButton visual state with animation and feedback
     */
    private void updateCheckboxVisualState(android.widget.CompoundButton radioButton, boolean isChecked) {
        try {
            // Add visual feedback animation
            radioButton.animate()
                .scaleX(isChecked ? 1.1f : 1.0f)
                .scaleY(isChecked ? 1.1f : 1.0f)
                .setDuration(150)
                .withEndAction(() -> {
                    radioButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
                })
                .start();
                
            // Update RadioButton color using proper color state list
            if (radioButton instanceof RadioButton) {
                radioButton.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_color_selector));
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

            for (RadioButton checkBox : incidentCheckboxes) {
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
        for (RadioButton checkBox : incidentCheckboxes) {
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