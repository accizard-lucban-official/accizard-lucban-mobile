package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.Style;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.ImageView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;

public class MapViewActivity extends AppCompatActivity {

    private EditText searchLocationEditText;
    private ImageView clearSearchButton;
    private FloatingActionButton emergencyFab;
    private FloatingActionButton alertFab;
    private FloatingActionButton pinLocationFab;
    private ImageView filterButton;
    private ImageButton profile;
    private RecyclerView searchResultsRecyclerView;
    private LinearLayout searchResultsContainer;

    // Navigation tabs
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private LinearLayout reportTab;
    private LinearLayout mapTab;
    private LinearLayout alertsTab;

    // Mapbox MapView
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CameraAnimationsPlugin cameraAnimationsPlugin;

    // Search functionality
    private SimpleSearchAdapter simpleSearchAdapter;
    private List<SimpleSearchAdapter.SearchPlace> searchPlaces = new ArrayList<>();
    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    // Activity Result Launcher for Facilities
    private ActivityResultLauncher<Intent> facilitiesLauncher;

    // Filter states
    private boolean heatmapEnabled = false;
    private Map<String, Boolean> incidentFilters = new HashMap<>();
    private Map<String, Boolean> facilityFilters = new HashMap<>();
    private String selectedTimeRange = "Today";

    private FusedLocationProviderClient fusedLocationClient;
    private List<MapMarker> pinnedMarkers = new ArrayList<>();
    private FrameLayout mapContainer;
    private List<Point> pinnedLocations = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Custom marker class to hold marker data
    private static class MapMarker {
        ImageView markerView;
        Point location;
        String title;
        
        MapMarker(ImageView markerView, Point location, String title) {
            this.markerView = markerView;
            this.location = location;
            this.title = title;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize MapView and MapboxMap
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapboxMap = mapView.getMapboxMap();
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, style -> {
                // Correct plugin key for Mapbox v10+
                cameraAnimationsPlugin = mapView.getPlugin("com.mapbox.maps.plugin.animation.camera");
                
                // Set initial camera position to Lucban center
                Point lucbanCenter = Point.fromLngLat(121.5564, 14.1136);
                CameraOptions initialCamera = new CameraOptions.Builder()
                        .center(lucbanCenter)
                        .zoom(14.0)
                        .build();
                mapboxMap.setCamera(initialCamera);
                
                // Map loaded successfully - marker positions will be updated during animations
                
                // Map loaded successfully
            });
        }

        initializeSearchPlaces();
        initializeFilters();
        initializeViews();
        initializeSearchEngine();
        setupSearchListeners();
        setupClickListeners();
        setupBottomNavigation();
        setMapTabAsSelected();
        setupFacilitiesLauncher();
    }

    private void initializeSearchPlaces() {
        searchPlaces.clear();

        // Lucban Town Center and Government Buildings
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Municipal Hall", "Lucban, Quezon", 14.1136, 121.5564));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Plaza", "Lucban, Quezon", 14.1135, 121.5562));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Public Market", "Lucban, Quezon", 14.1139, 121.5569));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Church (San Luis Obispo)", "Lucban, Quezon", 14.1133, 121.5561));

        // Health Facilities - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban District Hospital", "Lucban, Quezon", 14.1145, 121.5572));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Medical Center", "Lucban, Quezon", 14.1138, 121.5567));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Health Center", "Lucban, Quezon", 14.1140, 121.5568));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Health Station", "Lucban, Quezon", 14.1142, 121.5570));

        // Emergency Services - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Police Station", "Lucban, Quezon", 14.1142, 121.5565));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Fire Station", "Lucban, Quezon", 14.1140, 121.5563));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Municipal Disaster Risk Reduction Office", "Lucban, Quezon", 14.1137, 121.5566));

        // Educational Institutions - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Elementary School", "Lucban, Quezon", 14.1148, 121.5575));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban National High School", "Lucban, Quezon", 14.1149, 121.5576));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Integrated School", "Lucban, Quezon", 14.1150, 121.5577));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Technical-Vocational School", "Lucban, Quezon", 14.1151, 121.5578));

        // Banks and Financial Services - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Rural Bank", "Lucban, Quezon", 14.1137, 121.5565));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("BPI Lucban Branch", "Lucban, Quezon", 14.1134, 121.5563));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Landbank Lucban", "Lucban, Quezon", 14.1138, 121.5566));

        // Tourist Spots and Landmarks - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Kamay ni Hesus", "Lucban, Quezon", 14.1200, 121.5600));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Museum", "Lucban, Quezon", 14.1132, 121.5560));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Pahiyas Festival Site", "Lucban, Quezon", 14.1134, 121.5562));

        // Barangays - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay 1 (Poblacion)", "Lucban, Quezon", 14.1136, 121.5564));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay 2 (Poblacion)", "Lucban, Quezon", 14.1140, 121.5568));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay 3 (Poblacion)", "Lucban, Quezon", 14.1132, 121.5560));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Ayuti", "Lucban, Quezon", 14.1200, 121.5650));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Atisan", "Lucban, Quezon", 14.1250, 121.5700));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Bagumbayan", "Lucban, Quezon", 14.1100, 121.5500));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Malaking Ambling", "Lucban, Quezon", 14.1300, 121.5800));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Mungkay", "Lucban, Quezon", 14.1050, 121.5450));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Nagcamalite", "Lucban, Quezon", 14.1180, 121.5620));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Barangay Tinamnan", "Lucban, Quezon", 14.1080, 121.5480));

        // Commercial Areas - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Business District", "Lucban, Quezon", 14.1135, 121.5565));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Shopping Center", "Lucban, Quezon", 14.1141, 121.5571));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Terminal", "Lucban, Quezon", 14.1143, 121.5573));

        // Utilities and Services - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Post Office", "Lucban, Quezon", 14.1134, 121.5563));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Water District", "Lucban, Quezon", 14.1139, 121.5567));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Electric Cooperative", "Lucban, Quezon", 14.1144, 121.5574));

        // Parks and Recreation - Lucban
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Town Park", "Lucban, Quezon", 14.1135, 121.5562));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Sports Complex", "Lucban, Quezon", 14.1160, 121.5590));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucban Basketball Court", "Lucban, Quezon", 14.1147, 121.5575));

        // MAJOR CITIES AND TOWNS IN QUEZON PROVINCE
        
        // Tayabas City
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas City Hall", "Tayabas City, Quezon", 14.0283, 121.5854));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas Provincial Hospital", "Tayabas City, Quezon", 14.0285, 121.5856));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas Police Station", "Tayabas City, Quezon", 14.0287, 121.5858));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas Fire Station", "Tayabas City, Quezon", 14.0289, 121.5860));
        
        // Lucena City (Provincial Capital)
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Provincial Capitol", "Lucena City, Quezon", 13.9318, 121.6157));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena City Hall", "Lucena City, Quezon", 13.9319, 121.6158));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Medical Center", "Lucena City, Quezon", 13.9320, 121.6159));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena City Police Station", "Lucena City, Quezon", 13.9321, 121.6160));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena City Fire Station", "Lucena City, Quezon", 13.9322, 121.6161));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena City Public Market", "Lucena City, Quezon", 13.9323, 121.6162));
        
        // Sariaya
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Municipal Hall", "Sariaya, Quezon", 13.9624, 121.5268));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya District Hospital", "Sariaya, Quezon", 13.9625, 121.5269));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Police Station", "Sariaya, Quezon", 13.9626, 121.5270));
        
        // Candelaria
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Municipal Hall", "Candelaria, Quezon", 13.9311, 121.4234));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria District Hospital", "Candelaria, Quezon", 13.9312, 121.4235));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Police Station", "Candelaria, Quezon", 13.9313, 121.4236));
        
        // San Pablo City (Laguna, but nearby)
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("San Pablo City Hall", "San Pablo City, Laguna", 14.0697, 121.3255));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("San Pablo City Medical Center", "San Pablo City, Laguna", 14.0698, 121.3256));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("San Pablo City Police Station", "San Pablo City, Laguna", 14.0699, 121.3257));
        
        // Calauan (Laguna, nearby)
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Calauan Municipal Hall", "Calauan, Laguna", 14.1497, 121.3155));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Calauan District Hospital", "Calauan, Laguna", 14.1498, 121.3156));
        
        // MAJOR HOSPITALS AND MEDICAL FACILITIES
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Memorial Medical Center", "Lucena City, Quezon", 13.9320, 121.6159));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas Provincial Hospital", "Tayabas City, Quezon", 14.0285, 121.5856));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya District Hospital", "Sariaya, Quezon", 13.9625, 121.5269));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria District Hospital", "Candelaria, Quezon", 13.9312, 121.4235));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("San Pablo City Medical Center", "San Pablo City, Laguna", 14.0698, 121.3256));
        
        // MAJOR POLICE STATIONS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Provincial Police Office", "Lucena City, Quezon", 13.9321, 121.6160));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas City Police Station", "Tayabas City, Quezon", 14.0287, 121.5858));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Police Station", "Sariaya, Quezon", 13.9626, 121.5270));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Police Station", "Candelaria, Quezon", 13.9313, 121.4236));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("San Pablo City Police Station", "San Pablo City, Laguna", 14.0699, 121.3257));
        
        // MAJOR FIRE STATIONS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Provincial Fire Office", "Lucena City, Quezon", 13.9322, 121.6161));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas City Fire Station", "Tayabas City, Quezon", 14.0289, 121.5860));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Fire Station", "Sariaya, Quezon", 13.9627, 121.5271));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Fire Station", "Candelaria, Quezon", 13.9314, 121.4237));
        
        // MAJOR SHOPPING CENTERS AND MARKETS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Pacific Mall Lucena", "Lucena City, Quezon", 13.9324, 121.6163));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("SM City Lucena", "Lucena City, Quezon", 13.9325, 121.6164));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena City Public Market", "Lucena City, Quezon", 13.9323, 121.6162));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Public Market", "Sariaya, Quezon", 13.9628, 121.5272));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Public Market", "Candelaria, Quezon", 13.9315, 121.4238));
        
        // MAJOR SCHOOLS AND UNIVERSITIES
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Southern Luzon State University", "Lucban, Quezon", 14.1152, 121.5579));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon National Agricultural School", "Tiaong, Quezon", 13.9556, 121.3167));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Manuel S. Enverga University", "Lucena City, Quezon", 13.9326, 121.6165));
        
        // MAJOR BANKS AND FINANCIAL INSTITUTIONS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Landbank Lucena Branch", "Lucena City, Quezon", 13.9327, 121.6166));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("BPI Lucena Branch", "Lucena City, Quezon", 13.9328, 121.6167));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("BDO Lucena Branch", "Lucena City, Quezon", 13.9329, 121.6168));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Metrobank Lucena Branch", "Lucena City, Quezon", 13.9330, 121.6169));
        
        // MAJOR TRANSPORTATION HUBS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Lucena Grand Terminal", "Lucena City, Quezon", 13.9331, 121.6170));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Sariaya Bus Terminal", "Sariaya, Quezon", 13.9629, 121.5273));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Candelaria Bus Terminal", "Candelaria, Quezon", 13.9316, 121.4239));
        
        // MAJOR TOURIST DESTINATIONS
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Kamay ni Hesus Healing Center", "Lucban, Quezon", 14.1200, 121.5600));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Villa Escudero Plantation", "Tiaong, Quezon", 13.9557, 121.3168));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Mount Banahaw", "Dolores, Quezon", 14.0733, 121.4800));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Tayabas Basilica", "Tayabas City, Quezon", 14.0290, 121.5861));
        
        // MAJOR GOVERNMENT OFFICES
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Department of Health Quezon", "Lucena City, Quezon", 13.9332, 121.6171));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Department of Education Quezon", "Lucena City, Quezon", 13.9333, 121.6172));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Department of Social Welfare Quezon", "Lucena City, Quezon", 13.9334, 121.6173));
        
        // MAJOR UTILITIES
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Electric Cooperative", "Lucena City, Quezon", 13.9335, 121.6174));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Water District", "Lucena City, Quezon", 13.9336, 121.6175));
        searchPlaces.add(new SimpleSearchAdapter.SearchPlace("Quezon Telephone Company", "Lucena City, Quezon", 13.9337, 121.6176));
    }

    private void initializeFilters() {
        // Initialize incident filters - all enabled by default
        incidentFilters.put("Road Accident", true);
        incidentFilters.put("Fire", true);
        incidentFilters.put("Medical Emergency", true);
        incidentFilters.put("Flooding", true);
        incidentFilters.put("Volcanic Activity", true);
        incidentFilters.put("Landslide", true);
        incidentFilters.put("Earthquake", true);
        incidentFilters.put("Civil Disturbance", true);
        incidentFilters.put("Armed Conflict", true);
        incidentFilters.put("Infectious Disease", true);

        // Initialize facility filters - all enabled by default
        facilityFilters.put("Evacuation Centers", true);
        facilityFilters.put("Health Facilities", true);
        facilityFilters.put("Police Stations", true);
        facilityFilters.put("Fire Stations", true);
        facilityFilters.put("Government Offices", true);
    }

    private void setupFacilitiesLauncher() {
        facilitiesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Get filter settings from Facilities activity
                        heatmapEnabled = data.getBooleanExtra("heatmapEnabled", false);
                        selectedTimeRange = data.getStringExtra("selectedTimeRange");
                        if (selectedTimeRange == null) selectedTimeRange = "Today";

                        // Update incident filters
                        incidentFilters.put("Road Accident", data.getBooleanExtra("roadAccident", true));
                        incidentFilters.put("Fire", data.getBooleanExtra("fire", true));
                        incidentFilters.put("Medical Emergency", data.getBooleanExtra("medicalEmergency", true));
                        incidentFilters.put("Flooding", data.getBooleanExtra("flooding", true));
                        incidentFilters.put("Volcanic Activity", data.getBooleanExtra("volcanicActivity", true));
                        incidentFilters.put("Landslide", data.getBooleanExtra("landslide", true));
                        incidentFilters.put("Earthquake", data.getBooleanExtra("earthquake", true));
                        incidentFilters.put("Civil Disturbance", data.getBooleanExtra("civilDisturbance", true));
                        incidentFilters.put("Armed Conflict", data.getBooleanExtra("armedConflict", true));
                        incidentFilters.put("Infectious Disease", data.getBooleanExtra("infectiousDisease", true));

                        // Update facility filters
                        facilityFilters.put("Evacuation Centers", data.getBooleanExtra("evacuationCenters", true));
                        facilityFilters.put("Health Facilities", data.getBooleanExtra("healthFacilities", true));
                        facilityFilters.put("Police Stations", data.getBooleanExtra("policeStations", true));
                        facilityFilters.put("Fire Stations", data.getBooleanExtra("fireStations", true));
                        facilityFilters.put("Government Offices", data.getBooleanExtra("governmentOffices", true));

                        // Filters applied successfully
                    }
                }
        );
    }



    private void initializeViews() {
        searchLocationEditText = findViewById(R.id.searchLocationEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        emergencyFab = findViewById(R.id.emergencyFab);
        alertFab = findViewById(R.id.alertFab);
        pinLocationFab = findViewById(R.id.pinLocationFab);
        filterButton = findViewById(R.id.filterButton);
        profile = findViewById(R.id.profile);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsContainer = findViewById(R.id.searchResultsContainer);
        mapContainer = findViewById(R.id.mapContainer);

        // Initialize navigation tabs
        homeTab = findViewById(R.id.homeTab);
        chatTab = findViewById(R.id.chatTab);
        reportTab = findViewById(R.id.reportTab);
        mapTab = findViewById(R.id.mapTab);
        alertsTab = findViewById(R.id.alertsTab);
    }

    private void initializeSearchEngine() {
        try {
            simpleSearchAdapter = new SimpleSearchAdapter(new ArrayList<>(), this::onSearchResultSelected);
            searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            searchResultsRecyclerView.setAdapter(simpleSearchAdapter);
        } catch (Exception e) {
            // Error initializing search
        }
    }

    private void setupSearchListeners() {
        // Text change listener for search
        searchLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    performSearch(query);
                    clearSearchButton.setVisibility(View.VISIBLE);
                } else {
                    hideSearchResults();
                    clearSearchButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        clearSearchButton.setOnClickListener(v -> {
            searchLocationEditText.setText("");
            hideSearchResults();
            clearSearchButton.setVisibility(View.GONE);
        });

        // Search action listener
        searchLocationEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchLocationEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchLocationEditText.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Focus change listener
        searchLocationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String query = searchLocationEditText.getText().toString().trim();
                if (query.length() >= 2) {
                    showSearchResults();
                }
            } else {
                // Hide search results when search field loses focus
                searchHandler.postDelayed(this::hideSearchResults, 200);
            }
        });
    }

    private void performSearch(String query) {
        // Cancel previous debounced search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Create new debounced search
        searchRunnable = () -> {
            List<SimpleSearchAdapter.SearchPlace> filteredPlaces = new ArrayList<>();
            String queryLower = query.toLowerCase();

            // Search through all places
            for (SimpleSearchAdapter.SearchPlace place : searchPlaces) {
                if (place.getName().toLowerCase().contains(queryLower) ||
                        place.getAddress().toLowerCase().contains(queryLower)) {
                    filteredPlaces.add(place);
                }
            }

            // Sort results by relevance (exact matches first, then partial matches)
            filteredPlaces.sort((a, b) -> {
                boolean aExact = a.getName().toLowerCase().startsWith(queryLower);
                boolean bExact = b.getName().toLowerCase().startsWith(queryLower);
                if (aExact && !bExact) return -1;
                if (!aExact && bExact) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            // Limit results to prevent overwhelming the user
            final List<SimpleSearchAdapter.SearchPlace> finalFilteredPlaces;
            if (filteredPlaces.size() > 10) {
                finalFilteredPlaces = filteredPlaces.subList(0, 10);
            } else {
                finalFilteredPlaces = filteredPlaces;
            }

            runOnUiThread(() -> {
                simpleSearchAdapter.updatePlaces(finalFilteredPlaces);
                showSearchResults();
            });
        };

        // Delay search by 300ms to avoid excessive searching
        searchHandler.postDelayed(searchRunnable, 300);
    }

    private void onSearchResultSelected(SimpleSearchAdapter.SearchPlace place) {
        // Set the selected place name in the search field
        searchLocationEditText.setText(place.getName());

        // Hide search results
        hideSearchResults();

        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchLocationEditText.getWindowToken(), 0);
        }

        // Create point for the selected location
        Point selectedPoint = Point.fromLngLat(place.getLongitude(), place.getLatitude());

        // Navigate to selected location

        // Navigate to the location with smooth animation
        // Use different zoom levels based on place type
        double zoomLevel = getZoomLevelForPlace(place);

        // Animate to location
            animateToLocation(selectedPoint, zoomLevel);
    }

    private double getZoomLevelForPlace(SimpleSearchAdapter.SearchPlace place) {
        String name = place.getName().toLowerCase();

        // Higher zoom for specific buildings and landmarks
        if (name.contains("hospital") || name.contains("medical") ||
            name.contains("police") || name.contains("fire") ||
            name.contains("school") || name.contains("bank") ||
            name.contains("church") || name.contains("hall")) {
            return 18.0; // Very close view for buildings
        }

        // Medium zoom for general areas
        if (name.contains("plaza") || name.contains("park") ||
            name.contains("market") || name.contains("terminal")) {
            return 16.0; // Medium view for public spaces
        }

        // Lower zoom for barangays and general areas
        if (name.contains("barangay") || name.contains("district")) {
            return 15.0; // Wider view for areas
        }

        // Default zoom for other places
        return 17.0;
    }

    private void showSearchResults() {
        searchResultsContainer.setVisibility(View.VISIBLE);
    }

    private void hideSearchResults() {
        searchResultsContainer.setVisibility(View.GONE);
    }

        private void animateToLocation(Point point, double zoom) {
        if (mapboxMap == null) {
            return;
        }
        if (cameraAnimationsPlugin == null) {
            // fallback to instant move
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(point)
                    .zoom(zoom)
                    .build();
            mapboxMap.setCamera(cameraOptions);
            return;
        }
        try {
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(point)
                    .zoom(zoom)
                    .build();
            MapAnimationOptions animationOptions = new MapAnimationOptions.Builder()
                    .duration(2000)
                    .build();
            cameraAnimationsPlugin.flyTo(cameraOptions, animationOptions, null);
        } catch (Exception e) {
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(point)
                    .zoom(zoom)
                    .build();
            mapboxMap.setCamera(cameraOptions);
        }
    }

    private void setupClickListeners() {
        emergencyFab.setOnClickListener(v -> {
            // Emergency services contacted
        });

        alertFab.setOnClickListener(v -> {
            // Alert sent
        });

        pinLocationFab.setOnClickListener(v -> {
            pinCurrentLocation();
        });

        // Long press on pin FAB to clear all pins
        pinLocationFab.setOnLongClickListener(v -> {
            clearAllPins();
            return true;
        });

        // Long press on emergency FAB to navigate to nearest hospital
        emergencyFab.setOnLongClickListener(v -> {
            SimpleSearchAdapter.SearchPlace nearestHospital = null;
            for (SimpleSearchAdapter.SearchPlace place : searchPlaces) {
                if (place.getName().toLowerCase().contains("hospital") ||
                        place.getName().toLowerCase().contains("medical")) {
                    nearestHospital = place;
                    break;
                }
            }
            if (nearestHospital != null) {
                onSearchResultSelected(nearestHospital);
            }
            return true;
        });

        // Long press on alert FAB to navigate to Lucban center
        alertFab.setOnLongClickListener(v -> {
            Point lucbanCenter = Point.fromLngLat(121.5564, 14.1136);
            animateToLocation(lucbanCenter, 15.0);
            return true;
        });

        filterButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MapViewActivity.this, Facilities.class);
                intent.putExtra("heatmapEnabled", heatmapEnabled);
                intent.putExtra("selectedTimeRange", selectedTimeRange);
                intent.putExtra("roadAccident", incidentFilters.containsKey("Road Accident") ? incidentFilters.get("Road Accident") : true);
                intent.putExtra("fire", incidentFilters.containsKey("Fire") ? incidentFilters.get("Fire") : true);
                intent.putExtra("medicalEmergency", incidentFilters.containsKey("Medical Emergency") ? incidentFilters.get("Medical Emergency") : true);
                intent.putExtra("flooding", incidentFilters.containsKey("Flooding") ? incidentFilters.get("Flooding") : true);
                intent.putExtra("volcanicActivity", incidentFilters.containsKey("Volcanic Activity") ? incidentFilters.get("Volcanic Activity") : true);
                intent.putExtra("landslide", incidentFilters.containsKey("Landslide") ? incidentFilters.get("Landslide") : true);
                intent.putExtra("earthquake", incidentFilters.containsKey("Earthquake") ? incidentFilters.get("Earthquake") : true);
                intent.putExtra("civilDisturbance", incidentFilters.containsKey("Civil Disturbance") ? incidentFilters.get("Civil Disturbance") : true);
                intent.putExtra("armedConflict", incidentFilters.containsKey("Armed Conflict") ? incidentFilters.get("Armed Conflict") : true);
                intent.putExtra("infectiousDisease", incidentFilters.containsKey("Infectious Disease") ? incidentFilters.get("Infectious Disease") : true);
                intent.putExtra("evacuationCenters", facilityFilters.containsKey("Evacuation Centers") ? facilityFilters.get("Evacuation Centers") : true);
                intent.putExtra("healthFacilities", facilityFilters.containsKey("Health Facilities") ? facilityFilters.get("Health Facilities") : true);
                intent.putExtra("policeStations", facilityFilters.containsKey("Police Stations") ? facilityFilters.get("Police Stations") : true);
                intent.putExtra("fireStations", facilityFilters.containsKey("Fire Stations") ? facilityFilters.get("Fire Stations") : true);
                intent.putExtra("governmentOffices", facilityFilters.containsKey("Government Offices") ? facilityFilters.get("Government Offices") : true);
                facilitiesLauncher.launch(intent);
            } catch (Exception e) {
                // Error opening filters
            }
        });

        profile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MapViewActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                // Profile error
            }
        });
    }

    private void setupBottomNavigation() {
        homeTab.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, MainDashboard.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            } catch (Exception e) {
                // Error navigating to Home
            }
        });

        chatTab.setOnClickListener(v -> {
            try{
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            } catch (Exception e) {
                // Chat feature coming soon
            }
        });

        reportTab.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ReportSubmissionActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            } catch (Exception e) {
                // Error navigating to Report
            }
        });

        mapTab.setOnClickListener(v -> {
            // Already on the Map
        });

        alertsTab.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, AlertsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            } catch (Exception e) {
                // Error navigating to Alerts
            }
        });
    }

    private void setMapTabAsSelected() {
        resetAllTabs();

        ImageView mapIcon = mapTab.findViewById(R.id.mapIcon);
        if (mapIcon != null) {
            // You can change the icon to selected state here if you have different icons
        }

        android.widget.TextView mapText = mapTab.findViewById(R.id.mapText);
        if (mapText != null) {
            mapText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

    private void resetAllTabs() {
        int defaultColor = getResources().getColor(android.R.color.darker_gray);
        // You can add logic here to reset all tab text colors if needed
    }

    @Override
    public void onStart() {
        super.onStart();
        setMapTabAsSelected();
    }

    @Override
    public void onResume() {
        super.onResume();
        setMapTabAsSelected();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove any pending search callbacks
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    private void pinCurrentLocation() {
        if (checkLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                            addPinToMap(currentPoint, "Current Location");
                            
                            // Animate to current location
                            animateToLocation(currentPoint, 16.0);
                        } else {
                            // Unable to get current location
                        }
                    }
                });
        } else {
            requestLocationPermission();
        }
    }

    private void addPinToMap(Point point, String title) {
        // Create a custom marker view
        ImageView markerView = new ImageView(this);
        markerView.setImageResource(R.drawable.ic_location);
        markerView.setColorFilter(Color.RED);
        markerView.setScaleX(1.5f);
        markerView.setScaleY(1.5f);
        
        // Position the marker at the specific location on the map
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        // Position marker in center of map container initially
        params.gravity = Gravity.CENTER;
        markerView.setLayoutParams(params);
        
        // Add marker to the map container
        if (mapContainer != null) {
            mapContainer.addView(markerView);
            
            // Create MapMarker object and add to list
            MapMarker mapMarker = new MapMarker(markerView, point, title);
            pinnedMarkers.add(mapMarker);
            pinnedLocations.add(point);
        }
    }

    private void clearAllPins() {
        if (mapContainer != null) {
            for (MapMarker mapMarker : pinnedMarkers) {
                mapContainer.removeView(mapMarker.markerView);
            }
            pinnedMarkers.clear();
            pinnedLocations.clear();
        }
    }

    private void updateMarkerPosition(MapMarker mapMarker) {
        // Simplified marker positioning - markers will stay in center
        // This avoids complex coordinate conversion issues
        if (mapMarker != null && mapMarker.markerView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mapMarker.markerView.getLayoutParams();
            if (params != null) {
                params.gravity = Gravity.CENTER;
                mapMarker.markerView.setLayoutParams(params);
            }
        }
    }
    
    private void updateAllMarkerPositions() {
        for (MapMarker mapMarker : pinnedMarkers) {
            updateMarkerPosition(mapMarker);
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
            LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
            } else {
                // Location permission denied
            }
        }
    }
}