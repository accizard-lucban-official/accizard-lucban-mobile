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
import android.widget.Switch;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.Style;
import com.mapbox.maps.StyleObjectInfo;
import com.mapbox.maps.StylePropertyValue;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;

import com.example.accizardlucban.HeatmapHelper;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Color;
import android.widget.TextView;
import android.view.Window;
import android.view.ViewGroup;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.util.Log;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.mapbox.maps.ScreenCoordinate;

public class MapViewActivity extends AppCompatActivity {

    private static final String TAG = "MapViewActivity";
    private static final String HEATMAP_SOURCE_ID = "hazard-heatmap-source";
    private static final String HEATMAP_LAYER_ID = "hazard-heatmap-layer";
    // Satellite base as raster overlay (keeps custom layers available)
    private static final String SATELLITE_SOURCE_ID = "satellite-source";
    private static final String SATELLITE_LAYER_ID = "satellite-layer";
    private static final List<String> HAZARD_FILTER_KEYS = Arrays.asList(
            "Road Accident",
            "Fire",
            "Medical Emergency",
            "Flooding",
            "Volcanic Activity",
            "Landslide",
            "Earthquake",
            "Civil Disturbance",
            "Armed Conflict",
            "Infectious Disease",
            "Poor Infrastructure",
            "Obstructions",
            "Electrical Hazard",
            "Environmental Hazard",
            "Animal Concerns",
            "Others",
            "Report" // My Reports - allow heatmap for user reports
    );
    private static final String PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    
    private EditText searchLocationEditText;
    private ImageView clearSearchButton;
    private FloatingActionButton alertFab;
    private FloatingActionButton pinLocationFab;
    private FloatingActionButton helpFab;
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
    private TextView alertsBadgeMap;
    private SharedPreferences sharedPreferences;

    // Mapbox MapView
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CameraAnimationsPlugin cameraAnimationsPlugin;

    // Search functionality
    private SimpleSearchAdapter simpleSearchAdapter;
    private List<SimpleSearchAdapter.SearchPlace> searchPlaces = new ArrayList<>();
    private List<SimpleSearchAdapter.SearchPlace> currentAutocompleteResults = new ArrayList<>();
    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;


    // Filter states
    private boolean heatmapEnabled = false;
    private Map<String, Boolean> incidentFilters = new HashMap<>();
    private Map<String, Boolean> facilityFilters = new HashMap<>();
    private String selectedTimeRange = "All Time";
    
    // Filter indicators
    private TextView filterIndicator;
    private boolean hasActiveFilters = false;
    
    // Map style selector
    private Dialog mapStyleDialog;
    
    // Map layers state
    private boolean lucbanBoundaryVisible = true; // Always visible by default
    private boolean barangayBoundariesVisible = false;
    private boolean roadNetworkVisible = false;
    private boolean waterwaysVisible = false;
    private boolean satelliteMapVisible = false; // Satellite map layer toggle - INITIAL VIEW: Street View (satellite hidden)
    private boolean healthFacilitiesVisible = false;
    private boolean evacuationCentersVisible = false;
    
    // Flag to prevent double-triggering when programmatically setting checkbox
    private boolean isUpdatingSatelliteCheckbox = false;
    
    // Filter Panel Overlay
    private LinearLayout filterPanelOverlay;
    private LinearLayout filterPanel;
    private FrameLayout mainMapContainer;
    private boolean isFilterPanelVisible = false;
    
    // Filter Panel UI Elements
    private Switch heatmapSwitch;
    private CheckBox roadAccidentCheck, fireCheck, medicalEmergencyCheck, floodingCheck;
    private CheckBox volcanicActivityCheck, landslideCheck, earthquakeCheck, civilDisturbanceCheck;
    private CheckBox armedConflictCheck, infectiousDiseaseCheck;
    private CheckBox poorInfrastructureCheck, obstructionsCheck, electricalHazardCheck, environmentalHazardCheck, animalConcernsCheck;
    private CheckBox evacuationCentersCheck, healthFacilitiesCheck, policeStationsCheck;
    private CheckBox fireStationsCheck, governmentOfficesCheck;
    private CheckBox othersIncidentCheck;
    private CheckBox reportCheck;
    
    // Timeline options
    private TextView allTimeOption, todayOption, thisWeekOption, thisMonthOption, thisYearOption;
    private TextView selectedTimelineOption;
    
    // Section visibility states
    private boolean isTimelineExpanded = false;
    private boolean isIncidentTypesExpanded = false;
    private boolean isEmergencySupportExpanded = false;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private List<MapMarker> pinnedMarkers = new ArrayList<>();
    private List<MapMarker> firestorePinMarkers = new ArrayList<>(); // Add this for Firestore pins
    private List<MapMarker> userReportPinMarkers = new ArrayList<>(); // Pins from user's reports
    private FrameLayout mapContainer;
    private List<Point> pinnedLocations = new ArrayList<>();
    
    // Search marker (like Google Maps red pin)
    private ImageView searchMarker;
    private Point searchMarkerLocation;
    private Handler searchMarkerHandler;
    private Runnable searchMarkerRunnable;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FirebaseFirestore db; // Add this for Firestore operations
    private static class IncidentCheckboxEntry {
        CheckBox checkBox;
        String incidentType;

        IncidentCheckboxEntry(CheckBox checkBox, String incidentType) {
            this.checkBox = checkBox;
            this.incidentType = incidentType;
        }
    }
    private final List<IncidentCheckboxEntry> incidentCheckboxes = new ArrayList<>();
    private boolean isUpdatingIncidentCheckboxes = false;
    
    private static class FacilityCheckboxEntry {
        CheckBox checkBox;
        String facilityType;

        FacilityCheckboxEntry(CheckBox checkBox, String facilityType) {
            this.checkBox = checkBox;
            this.facilityType = facilityType;
        }
    }
    private final List<FacilityCheckboxEntry> facilityCheckboxes = new ArrayList<>();
    private boolean isUpdatingFacilityCheckboxes = false;
    private String activeFacilityFilter = null;
    
    private boolean isUpdatingHeatmapSwitch = false;
    private static final float PIN_WIDTH_DP = 56f;
    private static final float PIN_HEIGHT_DP = 76f;
    private static final float PIN_OFFSET_DP = 4f;
    private static final float CURRENT_LOCATION_PIN_OFFSET_DP = 6f;
    private int pinWidthPx;
    private int pinHeightPx;
    private int pinOffsetPx;
    private int currentLocationPinOffsetPx;
    
    // Camera tracking for Firestore pins
    private Handler firestorePinCameraHandler;
    private Runnable firestorePinCameraRunnable;
    private boolean isFirestorePinTrackingActive = false;
    
    // Guard flag to prevent infinite loops when applying filters and layers simultaneously
    private boolean isApplyingFiltersOrLayers = false;
    
    // Current location pin system (like MapPickerActivity)
    private ImageView currentLocationMarker;
    private Point currentLocationPoint;
    private Handler cameraUpdateHandler;
    private Runnable cameraUpdateRunnable;
    private boolean isCurrentLocationActive = false;
    
    // Custom marker class to hold marker data
    private static class MapMarker {
        ImageView markerView;
        Point location;
        String title;
        Pin pinData; // Add this field for Firestore pins
        String markerId; // Add this field for unique identification
        
        MapMarker(ImageView markerView, Point location, String title) {
            this.markerView = markerView;
            this.location = location;
            this.title = title;
        }
        
        // Add constructor with Pin data
        MapMarker(ImageView markerView, Point location, String title, Pin pinData, String markerId) {
            this.markerView = markerView;
            this.location = location;
            this.title = title;
            this.pinData = pinData;
            this.markerId = markerId;
        }
    }

    private static class HotspotTutorialStep {
        final int iconResId;
        final int titleResId;
        final int descriptionResId;

        HotspotTutorialStep(int iconResId, int titleResId, int descriptionResId) {
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.descriptionResId = descriptionResId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize SharedPreferences for badge
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize camera tracking handler for current location pin
        cameraUpdateHandler = new Handler(Looper.getMainLooper());
        
        // Initialize camera tracking handler for Firestore pins
        firestorePinCameraHandler = new Handler(Looper.getMainLooper());
        
        // Initialize search marker handler
        searchMarkerHandler = new Handler(Looper.getMainLooper());

        // Initialize MapView and MapboxMap
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapboxMap = mapView.getMapboxMap();
            
            // Disable compass on the map
            disableCompass();
            
            // Load default street style
            loadMapStyle();
        }

        initializeSearchPlaces();
        initializeFilters();
        initializeViews();
        initializeSearchEngine();
        setupSearchListeners();
        setupClickListeners();
        setupBottomNavigation();
        setMapTabAsSelected();
        
        // Load user profile picture
        loadUserProfilePicture();
        
        // Load pins from Firestore
        loadPinsFromFirestore();
        loadUserReportsAsPins();
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
        // Initialize incident filters - all DISABLED by default (unchecked)
        // Pins will only show when filters are toggled ON
        incidentFilters.put("Road Accident", false);
        incidentFilters.put("Fire", false);
        incidentFilters.put("Medical Emergency", false);
        incidentFilters.put("Flooding", false);
        incidentFilters.put("Volcanic Activity", false);
        incidentFilters.put("Landslide", false);
        incidentFilters.put("Earthquake", false);
        incidentFilters.put("Civil Disturbance", false);
        incidentFilters.put("Armed Conflict", false);
        incidentFilters.put("Infectious Disease", false);
        incidentFilters.put("Poor Infrastructure", false);
        incidentFilters.put("Obstructions", false);
        incidentFilters.put("Electrical Hazard", false);
        incidentFilters.put("Environmental Hazard", false);
        incidentFilters.put("Animal Concerns", false);
        incidentFilters.put("Report", false);
        incidentFilters.put("Others", false);

        // Initialize facility filters - all DISABLED by default (unchecked)
        facilityFilters.put("Evacuation Centers", false);
        facilityFilters.put("Health Facilities", false);
        facilityFilters.put("Police Stations", false);
        facilityFilters.put("Fire Stations", false);
        facilityFilters.put("Government Offices", false);
        
        // Initialize heatmap as disabled by default
        heatmapEnabled = false;
        
        // Initialize filter indicator
        updateFilterIndicator();
    }




    private void initializeViews() {
        searchLocationEditText = findViewById(R.id.searchLocationEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        alertFab = findViewById(R.id.alertFab);
        pinLocationFab = findViewById(R.id.pinLocationFab);
        helpFab = findViewById(R.id.helpFab);
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
        alertsBadgeMap = findViewById(R.id.alerts_badge_map);
        
        // Initialize filter indicator
        filterIndicator = findViewById(R.id.filterIndicator);
        
        // Initialize filter panel
        filterPanelOverlay = findViewById(R.id.filterPanelOverlay);
        filterPanel = findViewById(R.id.filterPanel);
        
        // Ensure filter panel is hidden by default
        if (filterPanelOverlay != null) {
            filterPanelOverlay.setVisibility(View.GONE);
        }
        if (filterPanel != null) {
            filterPanel.setTranslationX(-320f); // Start off-screen
        }
        isFilterPanelVisible = false;
        mainMapContainer = findViewById(R.id.mainMapContainer);
        
        // Initialize filter panel UI elements
        initializeFilterPanelViews();
    }
    
    private void initializeFilterPanelViews() {
        incidentCheckboxes.clear();
        facilityCheckboxes.clear();

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
        animalConcernsCheck = findViewById(R.id.animalConcernsCheck);
        othersIncidentCheck = findViewById(R.id.othersIncidentCheck);
        reportCheck = findViewById(R.id.reportCheck);
        evacuationCentersCheck = findViewById(R.id.evacuationCentersCheck);
        healthFacilitiesCheck = findViewById(R.id.healthFacilitiesCheck);
        policeStationsCheck = findViewById(R.id.policeStationsCheck);
        fireStationsCheck = findViewById(R.id.fireStationsCheck);
        governmentOfficesCheck = findViewById(R.id.governmentOfficesCheck);

        registerIncidentCheckbox(roadAccidentCheck, "Road Accident");
        registerIncidentCheckbox(fireCheck, "Fire");
        registerIncidentCheckbox(medicalEmergencyCheck, "Medical Emergency");
        registerIncidentCheckbox(floodingCheck, "Flooding");
        registerIncidentCheckbox(volcanicActivityCheck, "Volcanic Activity");
        registerIncidentCheckbox(landslideCheck, "Landslide");
        registerIncidentCheckbox(earthquakeCheck, "Earthquake");
        registerIncidentCheckbox(civilDisturbanceCheck, "Civil Disturbance");
        registerIncidentCheckbox(armedConflictCheck, "Armed Conflict");
        registerIncidentCheckbox(infectiousDiseaseCheck, "Infectious Disease");
        registerIncidentCheckbox(poorInfrastructureCheck, "Poor Infrastructure");
        registerIncidentCheckbox(obstructionsCheck, "Obstructions");
        registerIncidentCheckbox(electricalHazardCheck, "Electrical Hazard");
        registerIncidentCheckbox(environmentalHazardCheck, "Environmental Hazard");
        registerIncidentCheckbox(animalConcernsCheck, "Animal Concerns");
        registerIncidentCheckbox(othersIncidentCheck, "Others");
        registerIncidentCheckbox(reportCheck, "Report"); // Report is now part of single-selection group

        // Timeline options
        allTimeOption = findViewById(R.id.allTimeOption);
        todayOption = findViewById(R.id.todayOption);
        thisWeekOption = findViewById(R.id.thisWeekOption);
        thisMonthOption = findViewById(R.id.thisMonthOption);
        thisYearOption = findViewById(R.id.thisYearOption);
        
        // Set default selection to "All Time"
        if (allTimeOption != null) {
            selectedTimelineOption = allTimeOption;
        }
        
        // Setup checkbox listeners (similar to RegistrationActivity cbTerms functionality)
        setupCheckboxListeners();
    }

    private void registerIncidentCheckbox(CheckBox checkBox, String incidentType) {
        if (checkBox != null) {
            incidentCheckboxes.add(new IncidentCheckboxEntry(checkBox, incidentType));
        }
    }

    /**
     * Ensure only one incident checkbox is selected at a time
     * MUTUAL EXCLUSIVITY: When an incident is selected, clear and disable all facility checkboxes.
     * When an incident is unchecked, re-enable all facility checkboxes.
     */
    private void enforceSingleIncidentSelection(CheckBox changedCheckBox, String incidentType, boolean isChecked) {
        if (isUpdatingIncidentCheckboxes) {
            return;
        }

        isUpdatingIncidentCheckboxes = true;
        try {
            String activeIncidentType = null;

            if (changedCheckBox != null) {
                // Update the filter state immediately for the changed checkbox
                incidentFilters.put(incidentType, isChecked);
                
                if (isChecked) {
                    // Incident checkbox was checked
                    activeIncidentType = incidentType;
                    // When an incident is selected, clear all facility checkboxes
                    clearAndDisableFacilityCheckboxes();
                } else {
                    // Incident checkbox was unchecked - check if any other incident is still checked
                    Log.d(TAG, "Incident filter '" + incidentType + "' unchecked");
                    for (Map.Entry<String, Boolean> entry : incidentFilters.entrySet()) {
                        if (Boolean.TRUE.equals(entry.getValue()) && !entry.getKey().equals(incidentType)) {
                            activeIncidentType = entry.getKey();
                            break;
                        }
                    }
                    // If no incident is checked, re-enable facility checkboxes
                    if (activeIncidentType == null) {
                        setFacilityCheckboxesEnabled(true);
                        Log.d(TAG, "No incident selected - re-enabled facility checkboxes");
                    }
                }
            } else {
                // Called without a specific checkbox change - find active incident
                for (Map.Entry<String, Boolean> entry : incidentFilters.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        activeIncidentType = entry.getKey();
                        break;
                    }
                }
            }

            for (IncidentCheckboxEntry entry : incidentCheckboxes) {
                if (entry.checkBox == null) {
                    continue;
                }

                boolean shouldCheck = activeIncidentType != null && entry.incidentType.equals(activeIncidentType);

                if (entry.checkBox.isChecked() != shouldCheck) {
                    entry.checkBox.setChecked(shouldCheck);
                }

                incidentFilters.put(entry.incidentType, shouldCheck);
                updateCheckboxVisualState(entry.checkBox, shouldCheck);
            }
            
            // Enable/disable facility checkboxes based on whether any incident is selected
            if (activeIncidentType != null) {
                setFacilityCheckboxesEnabled(false);
            } else {
                setFacilityCheckboxesEnabled(true);
            }
        } finally {
            isUpdatingIncidentCheckboxes = false;
        }

        applyFiltersToMap();
        updateFilterIndicator();
        updateHeatmapToggleState();

        if (heatmapEnabled && hasActiveHazardFilter()) {
            showHeatmapView();
        } else {
            hideHeatmapView();
        }
    }

    private void enforceSingleIncidentSelection() {
        enforceSingleIncidentSelection(null, null, false);
    }

    /**
     * Apply facility selection to UI and filters, ensuring only one facility is active.
     * MUTUAL EXCLUSIVITY: When a facility is selected, clear and disable all incident checkboxes.
     * When a facility is unchecked, re-enable all incident checkboxes.
     */
    private void enforceSingleFacilitySelection(CheckBox changedCheckBox, String facilityType, boolean isChecked) {
        if (isUpdatingFacilityCheckboxes) {
            return;
        }

        if (changedCheckBox != null && isChecked && heatmapEnabled) {
            hideHeatmapView();
            heatmapEnabled = false;
            if (heatmapSwitch != null) {
                isUpdatingHeatmapSwitch = true;
                heatmapSwitch.setChecked(false);
                isUpdatingHeatmapSwitch = false;
            }
        }

        isUpdatingFacilityCheckboxes = true;
        try {
            String activeFacilityType = null;

            if (changedCheckBox != null) {
                // Update the filter state immediately for the changed checkbox
                facilityFilters.put(facilityType, isChecked);
                
                if (isChecked) {
                    // Facility checkbox was checked
                    Log.d(TAG, "Facility filter '" + facilityType + "' checked");
                    activeFacilityType = facilityType;
                    // When a facility is selected, clear all incident checkboxes
                    clearAndDisableIncidentCheckboxes();
                } else {
                    // Facility checkbox was unchecked - check if any other facility is still checked
                    Log.d(TAG, "Facility filter '" + facilityType + "' unchecked");
                    for (Map.Entry<String, Boolean> entry : facilityFilters.entrySet()) {
                        if (Boolean.TRUE.equals(entry.getValue()) && !entry.getKey().equals(facilityType)) {
                            activeFacilityType = entry.getKey();
                            break;
                        }
                    }
                    // If no facility is checked, re-enable incident checkboxes
                    if (activeFacilityType == null) {
                        setIncidentCheckboxesEnabled(true);
                        Log.d(TAG, "No facility selected - re-enabled incident checkboxes");
                    }
                }
            } else {
                // Called without a specific checkbox change - find active facility
                for (Map.Entry<String, Boolean> entry : facilityFilters.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        activeFacilityType = entry.getKey();
                        break;
                    }
                }
            }

            activeFacilityFilter = activeFacilityType;

            for (FacilityCheckboxEntry entry : facilityCheckboxes) {
                if (entry.checkBox == null) {
                    continue;
                }

                boolean shouldCheck = activeFacilityType != null && entry.facilityType.equals(activeFacilityType);

                if (entry.checkBox.isChecked() != shouldCheck) {
                    entry.checkBox.setChecked(shouldCheck);
                }

                facilityFilters.put(entry.facilityType, shouldCheck);
                updateCheckboxVisualState(entry.checkBox, shouldCheck);
                applyFacilityLayerVisibility(entry.facilityType, shouldCheck);
            }
            
            // Enable/disable incident checkboxes based on whether any facility is selected
            if (activeFacilityType != null) {
                setIncidentCheckboxesEnabled(false);
            } else {
                setIncidentCheckboxesEnabled(true);
            }
        } finally {
            isUpdatingFacilityCheckboxes = false;
        }

        applyFiltersToMap();
        updateFilterIndicator();
    }

    /**
     * Clear all facility checkboxes and disable them
     * Called when an incident checkbox is selected
     */
    private void clearAndDisableFacilityCheckboxes() {
        isUpdatingFacilityCheckboxes = true;
        try {
            // Clear all facility filters
            for (String facilityType : facilityFilters.keySet()) {
                facilityFilters.put(facilityType, false);
            }
            activeFacilityFilter = null;
            
            // Uncheck and disable all facility checkboxes
            for (FacilityCheckboxEntry entry : facilityCheckboxes) {
                if (entry.checkBox != null) {
                    entry.checkBox.setChecked(false);
                    entry.checkBox.setEnabled(false);
                    entry.checkBox.setAlpha(0.5f); // Visual indication that it's disabled
                    updateCheckboxVisualState(entry.checkBox, false);
                    applyFacilityLayerVisibility(entry.facilityType, false);
                }
            }
            
            Log.d(TAG, "Cleared and disabled all facility checkboxes");
        } finally {
            isUpdatingFacilityCheckboxes = false;
        }
    }

    /**
     * Clear all incident checkboxes and disable them
     * Called when a facility checkbox is selected
     */
    private void clearAndDisableIncidentCheckboxes() {
        isUpdatingIncidentCheckboxes = true;
        try {
            // Clear all incident filters
            for (String incidentType : incidentFilters.keySet()) {
                incidentFilters.put(incidentType, false);
            }
            
            // Uncheck and disable all incident checkboxes
            for (IncidentCheckboxEntry entry : incidentCheckboxes) {
                if (entry.checkBox != null) {
                    entry.checkBox.setChecked(false);
                    entry.checkBox.setEnabled(false);
                    entry.checkBox.setAlpha(0.5f); // Visual indication that it's disabled
                    updateCheckboxVisualState(entry.checkBox, false);
                }
            }
            
            // Disable heatmap switch when incidents are disabled
            if (heatmapSwitch != null) {
                heatmapSwitch.setEnabled(false);
                heatmapSwitch.setAlpha(0.5f);
            }
            
            // Hide heatmap if it was visible
            if (heatmapEnabled) {
                heatmapEnabled = false;
                hideHeatmapView();
            }
            
            Log.d(TAG, "Cleared and disabled all incident checkboxes");
        } finally {
            isUpdatingIncidentCheckboxes = false;
        }
    }

    /**
     * Enable or disable all facility checkboxes
     * @param enabled true to enable, false to disable
     */
    private void setFacilityCheckboxesEnabled(boolean enabled) {
        for (FacilityCheckboxEntry entry : facilityCheckboxes) {
            if (entry.checkBox != null) {
                entry.checkBox.setEnabled(enabled);
                entry.checkBox.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        Log.d(TAG, "Facility checkboxes " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Enable or disable all incident checkboxes
     * @param enabled true to enable, false to disable
     */
    private void setIncidentCheckboxesEnabled(boolean enabled) {
        for (IncidentCheckboxEntry entry : incidentCheckboxes) {
            if (entry.checkBox != null) {
                entry.checkBox.setEnabled(enabled);
                entry.checkBox.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
        
        // Also enable/disable heatmap switch based on incident checkbox state
        if (heatmapSwitch != null) {
            // Heatmap switch should be enabled only if incidents are enabled AND a hazard filter is active
            boolean heatmapShouldBeEnabled = enabled && hasActiveHazardFilter();
            heatmapSwitch.setEnabled(heatmapShouldBeEnabled);
            heatmapSwitch.setAlpha(heatmapShouldBeEnabled ? 1.0f : 0.5f);
        }
        
        Log.d(TAG, "Incident checkboxes " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Setup checkbox listeners similar to RegistrationActivity cbTerms functionality
     * This method handles checkbox state changes and updates the filter states accordingly
     */
    private void setupCheckboxListeners() {
        try {
            // Heatmap Switch Listener
            if (heatmapSwitch != null) {
                heatmapSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isUpdatingHeatmapSwitch) {
                        return;
                    }

                    if (isChecked && !hasActiveHazardFilter()) {
                        isUpdatingHeatmapSwitch = true;
                        buttonView.setChecked(false);
                        isUpdatingHeatmapSwitch = false;
                        heatmapEnabled = false;
                        hideHeatmapView();
                        Toast.makeText(MapViewActivity.this,
                                "Turn on an accident or hazard filter to use the heatmap.",
                                Toast.LENGTH_SHORT).show();
                        updateFilterIndicator();
                        return;
                    }

                    heatmapEnabled = isChecked;
                    Log.d(TAG, "Heatmap toggle changed: " + isChecked);

                    if (heatmapEnabled) {
                        showHeatmapView();
                    } else {
                        hideHeatmapView();
                    }

                    // Update filter indicator when heatmap changes
                    updateFilterIndicator();
                });
            }
            
            // Incident Type Checkbox Listeners (all part of single-selection group)
            setupIncidentCheckboxListener(roadAccidentCheck, "Road Accident");
            setupIncidentCheckboxListener(fireCheck, "Fire");
            setupIncidentCheckboxListener(medicalEmergencyCheck, "Medical Emergency");
            setupIncidentCheckboxListener(floodingCheck, "Flooding");
            setupIncidentCheckboxListener(volcanicActivityCheck, "Volcanic Activity");
            setupIncidentCheckboxListener(landslideCheck, "Landslide");
            setupIncidentCheckboxListener(earthquakeCheck, "Earthquake");
            setupIncidentCheckboxListener(civilDisturbanceCheck, "Civil Disturbance");
            setupIncidentCheckboxListener(armedConflictCheck, "Armed Conflict");
            setupIncidentCheckboxListener(infectiousDiseaseCheck, "Infectious Disease");
            setupIncidentCheckboxListener(poorInfrastructureCheck, "Poor Infrastructure");
            setupIncidentCheckboxListener(obstructionsCheck, "Obstructions");
            setupIncidentCheckboxListener(electricalHazardCheck, "Electrical Hazard");
            setupIncidentCheckboxListener(environmentalHazardCheck, "Environmental Hazard");
            setupIncidentCheckboxListener(animalConcernsCheck, "Animal Concerns");
            setupIncidentCheckboxListener(othersIncidentCheck, "Others");
            // Report checkbox is now part of the single-selection group
            setupIncidentCheckboxListener(reportCheck, "Report");
            
            // Facility Checkbox Listeners (register first, then setup listeners)
            registerFacilityCheckbox(evacuationCentersCheck, "Evacuation Centers");
            registerFacilityCheckbox(healthFacilitiesCheck, "Health Facilities");
            registerFacilityCheckbox(policeStationsCheck, "Police Stations");
            registerFacilityCheckbox(fireStationsCheck, "Fire Stations");
            registerFacilityCheckbox(governmentOfficesCheck, "Government Offices");
            
            setupFacilityCheckboxListener(evacuationCentersCheck, "Evacuation Centers");
            setupFacilityCheckboxListener(healthFacilitiesCheck, "Health Facilities");
            setupFacilityCheckboxListener(policeStationsCheck, "Police Stations");
            setupFacilityCheckboxListener(fireStationsCheck, "Fire Stations");
            setupFacilityCheckboxListener(governmentOfficesCheck, "Government Offices");
            
            Log.d(TAG, "All checkbox listeners setup successfully");
            applyFacilitySelectionFromState();
            updateHeatmapToggleState();
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up checkbox listeners: " + e.getMessage(), e);
        }
    }
    
    /**
     * Setup individual incident type checkbox listener
     */
    private void setupIncidentCheckboxListener(CheckBox checkBox, String incidentType) {
        if (checkBox != null) {
            // Ensure checkbox is properly initialized
            checkBox.setChecked(incidentFilters.getOrDefault(incidentType, false));
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isUpdatingIncidentCheckboxes) {
                    return;
                }
                
                // Prevent interaction if checkbox is disabled
                if (!checkBox.isEnabled() && isChecked) {
                    checkBox.setChecked(false);
                    return;
                }

                try {
                    incidentFilters.put(incidentType, isChecked);
                    Log.d(TAG, "Incident filter '" + incidentType + "' changed: " + isChecked);
                    
                    // If "My Reports" is checked, reload user reports to ensure latest data
                    if ("Report".equals(incidentType) && isChecked) {
                        Log.d(TAG, "My Reports checked - reloading user reports");
                        // Reload reports - the loadUserReportsAsPins method will apply filters after loading
                        // Don't call applyFiltersToFirestorePins here because reports might not be loaded yet
                        loadUserReportsAsPins();
                    } else if ("Report".equals(incidentType) && !isChecked) {
                        // When "My Reports" is unchecked, hide all user report pins
                        Log.d(TAG, "My Reports unchecked - hiding user report pins");
                        runOnUiThread(() -> {
                            applyFiltersToFirestorePins(false);
                        });
                    } else {
                        // For other incident types, apply filters immediately
                        runOnUiThread(() -> {
                            applyFiltersToFirestorePins(true);
                        });
                    }
                    
                    enforceSingleIncidentSelection(checkBox, incidentType, isChecked);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling incident checkbox change for " + incidentType, e);
                }
            });
        }
    }
    
    
    /**
     * Register a facility checkbox for single-selection enforcement
     */
    private void registerFacilityCheckbox(CheckBox checkBox, String facilityType) {
        if (checkBox != null) {
            facilityCheckboxes.add(new FacilityCheckboxEntry(checkBox, facilityType));
        }
    }
    /**
     * Setup individual facility checkbox listener
     */
    private void setupFacilityCheckboxListener(CheckBox checkBox, String facilityType) {
        if (checkBox != null) {
            // Set flag to prevent listener from firing during initialization
            isUpdatingFacilityCheckboxes = true;
            try {
                // Initialize checkbox state based on current filters without triggering listener
                boolean initialChecked = facilityFilters.getOrDefault(facilityType, false);
                checkBox.setChecked(initialChecked);
            } finally {
                isUpdatingFacilityCheckboxes = false;
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isUpdatingFacilityCheckboxes) {
                    return;
                }
                
                // Prevent interaction if checkbox is disabled
                if (!checkBox.isEnabled() && isChecked) {
                    checkBox.setChecked(false);
                    return;
                }

                Log.d(TAG, "Facility filter '" + facilityType + "' changed to: " + isChecked);
                handleFacilitySelectionChange(checkBox, facilityType, isChecked);
            });
        }
    }

    /**
     * Handle facility checkbox interactions while enforcing single selection
     */
    private void handleFacilitySelectionChange(CheckBox changedCheckBox, String facilityType, boolean isChecked) {
        try {
            enforceSingleFacilitySelection(changedCheckBox, facilityType, isChecked);
        } catch (Exception e) {
            Log.e(TAG, "Error handling facility selection change for " + facilityType, e);
        }
    }

    /**
     * Apply the current facility selection state to the UI and map layers
     */
    private void applyFacilitySelectionFromState() {
        enforceSingleFacilitySelection(null, null, false);
    }

    /**
     * Toggle map layers associated with facility selections
     */
    private void applyFacilityLayerVisibility(String facilityType, boolean visible) {
        if (facilityType == null) {
            return;
        }

        switch (facilityType) {
            case "Health Facilities":
                toggleHealthFacilities(visible);
                break;
            case "Evacuation Centers":
                toggleEvacuationCenters(visible);
                break;
            case "Police Stations":
            case "Fire Stations":
            case "Government Offices":
                // Currently handled via Firestore pins; layers can be added here when available
                break;
            default:
                // Unknown facility type - no layer to toggle
                break;
        }
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
        // Text change listener for autocomplete search
        searchLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 1) { // Show suggestions from first character
                    performAutocompleteSearch(query);
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
            currentAutocompleteResults.clear();
            clearSearchButton.setVisibility(View.GONE);
            
            // Clear search marker when clearing search
            clearSearchMarker();
            
            searchLocationEditText.requestFocus();
            
            // Show keyboard
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchLocationEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Search action listener (when user presses enter/search button on keyboard)
        searchLocationEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchLocationEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchLocationEditText.getWindowToken(), 0);
                }
                
                // Hide search results
                hideSearchResults();
                
                // If there are search results, select the first one (will use geocoding)
                if (!currentAutocompleteResults.isEmpty()) {
                    SimpleSearchAdapter.SearchPlace firstResult = currentAutocompleteResults.get(0);
                    onSearchResultSelected(firstResult);
                } else {
                    // No autocomplete results, perform direct geocoding search
                    performGeocodingSearch(query);
                }
                return true;
            }
            return false;
        });

        // Focus change listener
        searchLocationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String query = searchLocationEditText.getText().toString().trim();
                if (query.length() >= 1) {
                    showSearchResults();
                    performAutocompleteSearch(query); // Refresh results
                }
            } else {
                // Hide search results when search field loses focus (with delay to allow clicks)
                searchHandler.postDelayed(this::hideSearchResults, 300);
            }
        });
    }

    private void performAutocompleteSearch(String query) {
        // Cancel previous debounced search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Create new debounced autocomplete search
        searchRunnable = () -> {
            List<SimpleSearchAdapter.SearchPlace> filteredPlaces = new ArrayList<>();
            String queryLower = query.toLowerCase();

            // Search through all places with smart matching
            for (SimpleSearchAdapter.SearchPlace place : searchPlaces) {
                String nameLower = place.getName().toLowerCase();
                String addressLower = place.getAddress().toLowerCase();
                
                // Match if query appears in name or address
                if (nameLower.contains(queryLower) || addressLower.contains(queryLower)) {
                    filteredPlaces.add(place);
                }
            }

            // Sort results by relevance for better autocomplete experience
            filteredPlaces.sort((a, b) -> {
                String aName = a.getName().toLowerCase();
                String bName = b.getName().toLowerCase();
                
                // Priority 1: Exact name match
                if (aName.equals(queryLower) && !bName.equals(queryLower)) return -1;
                if (!aName.equals(queryLower) && bName.equals(queryLower)) return 1;
                
                // Priority 2: Starts with query
                boolean aStarts = aName.startsWith(queryLower);
                boolean bStarts = bName.startsWith(queryLower);
                if (aStarts && !bStarts) return -1;
                if (!aStarts && bStarts) return 1;
                
                // Priority 3: Word starts with query (after space)
                boolean aWordStarts = aName.contains(" " + queryLower);
                boolean bWordStarts = bName.contains(" " + queryLower);
                if (aWordStarts && !bWordStarts) return -1;
                if (!aWordStarts && bWordStarts) return 1;
                
                // Priority 4: Alphabetical order
                return a.getName().compareToIgnoreCase(b.getName());
            });

            // Limit results for better autocomplete UX (show top 8 suggestions)
            final List<SimpleSearchAdapter.SearchPlace> finalFilteredPlaces;
            if (filteredPlaces.size() > 8) {
                finalFilteredPlaces = filteredPlaces.subList(0, 8);
            } else {
                finalFilteredPlaces = filteredPlaces;
            }

            runOnUiThread(() -> {
                if (simpleSearchAdapter != null) {
                    simpleSearchAdapter.updatePlaces(finalFilteredPlaces);
                    currentAutocompleteResults = new ArrayList<>(finalFilteredPlaces);
                    
                    // Show results only if there are matches
                    if (!finalFilteredPlaces.isEmpty()) {
                        showSearchResults();
                        Log.d(TAG, "Autocomplete found " + finalFilteredPlaces.size() + " suggestions for: " + query);
                    } else {
                        showNoResultsMessage();
                    }
                }
            });
        };

        // Shorter delay for instant autocomplete feel (150ms)
        searchHandler.postDelayed(searchRunnable, 150);
    }
    
    private void performSearch(String query) {
        // This method can be kept for backward compatibility or removed
        performAutocompleteSearch(query);
    }

    private void onSearchResultSelected(SimpleSearchAdapter.SearchPlace place) {
        try {
            Log.d(TAG, "Autocomplete selection: " + place.getName());
            
            // Set the selected place name in the search field
            searchLocationEditText.setText(place.getName());
            
            // Move cursor to end of text
            searchLocationEditText.setSelection(searchLocationEditText.getText().length());

            // Hide search results
            hideSearchResults();

            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchLocationEditText.getWindowToken(), 0);
            }
            
            // Clear focus from search field
            searchLocationEditText.clearFocus();

            // Use geocoding to get accurate coordinates for the searched location
            performGeocodingSearch(place.getName());
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling search result selection", e);
            Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Performs geocoding search to get accurate coordinates and adds a marker
     */
    private void performGeocodingSearch(String locationName) {
        // Show loading message
        Toast.makeText(this, "Searching for: " + locationName, Toast.LENGTH_SHORT).show();
        
        // Run geocoding in background thread
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                
                // Enhanced search query for better accuracy
                String enhancedQuery = locationName;
                if (!locationName.toLowerCase().contains("philippines") && 
                    !locationName.toLowerCase().contains("quezon")) {
                    enhancedQuery = locationName + ", Quezon Province, Philippines";
                }
                
                List<Address> addresses = geocoder.getFromLocationName(enhancedQuery, 10);
                
                if (addresses != null && !addresses.isEmpty()) {
                    // Find the most accurate address
                    Address bestAddress = findMostAccurateAddress(addresses);
                    
                    // Get high precision coordinates
                    double latitude = bestAddress.getLatitude();
                    double longitude = bestAddress.getLongitude();
                    
                    // Validate coordinates are within Philippines bounds
                    if (isValidPhilippinesCoordinates(longitude, latitude)) {
                        Point point = Point.fromLngLat(longitude, latitude);
                        
                        // Run UI updates on main thread
                        runOnUiThread(() -> {
                            // Clear existing search marker
                            clearSearchMarker();
                            
                            // Add marker at the searched location
                            addSearchMarkerAtLocation(point, locationName);
                            
                            // Navigate to the location with appropriate zoom
                            double zoomLevel = getZoomLevelForLocationName(locationName);
                            animateToLocation(point, zoomLevel);
                            
                            Log.d(TAG, "Search result coordinates: " + latitude + ", " + longitude);
                            Log.d(TAG, "Search result address: " + bestAddress.getAddressLine(0));
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Location not found in Philippines", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Location not found. Please try a different search term.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error searching location. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in geocoding search", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * Finds the most accurate address from geocoding results
     */
    private Address findMostAccurateAddress(List<Address> addresses) {
        if (addresses.isEmpty()) return null;
        
        Address bestAddress = addresses.get(0);
        int bestScore = calculateAddressScore(bestAddress);
        
        for (Address address : addresses) {
            int score = calculateAddressScore(address);
            if (score > bestScore) {
                bestScore = score;
                bestAddress = address;
            }
        }
        
        return bestAddress;
    }
    
    /**
     * Calculates a score for address accuracy (higher is better)
     */
    private int calculateAddressScore(Address address) {
        int score = 0;
        
        // Prefer addresses with more details
        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
            score += 10;
        }
        if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
            score += 5;
        }
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
            score += 3;
        }
        if (address.getLocality() != null && !address.getLocality().isEmpty()) {
            score += 2;
        }
        if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
            score += 1;
        }
        
        return score;
    }
    
    /**
     * Validates if coordinates are within Philippines bounds
     */
    private boolean isValidPhilippinesCoordinates(double longitude, double latitude) {
        // Philippines approximate bounds
        return longitude >= 116.0 && longitude <= 127.0 && 
               latitude >= 4.0 && latitude <= 22.0;
    }
    
    /**
     * Gets appropriate zoom level based on location name
     */
    private double getZoomLevelForLocationName(String locationName) {
        String name = locationName.toLowerCase();
        
        // Higher zoom for specific buildings and landmarks
        if (name.contains("hospital") || name.contains("medical") ||
            name.contains("police") || name.contains("fire") ||
            name.contains("school") || name.contains("university") ||
            name.contains("college") || name.contains("bank") ||
            name.contains("church") || name.contains("hall") ||
            name.contains("building")) {
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
    
    /**
     * Adds a search marker at the specified location (like Google Maps red pin)
     */
    private void addSearchMarkerAtLocation(Point point, String title) {
        if (mapContainer == null || mapboxMap == null) {
            Log.e(TAG, "Map container or mapboxMap is null");
            return;
        }
        
        try {
            // Clear any existing search marker first
            clearSearchMarker();
            
            // Ensure pin dimensions are set
            ensurePinDimensions();
            
            // Create marker using accizard_pin drawable (like Google Maps)
            searchMarker = new ImageView(this);
            searchMarker.setImageResource(R.drawable.accizard_pin);
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                pinWidthPx,
                pinHeightPx
            );
            searchMarker.setLayoutParams(params);
            
            // Store the location
            searchMarkerLocation = point;
            
            // Add to container
            mapContainer.addView(searchMarker);
            
            // Position marker at coordinates
            positionSearchMarkerAtCoordinates(point);
            
            // Add drop animation
            animateSearchMarkerDrop(searchMarker);
            
            // Start camera tracking to keep marker positioned correctly
            startSearchMarkerCameraTracking();
            
            Log.d(TAG, "Search marker added at: " + point.longitude() + ", " + point.latitude());
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding search marker", e);
        }
    }
    
    /**
     * Positions search marker at specific coordinates
     */
    private void positionSearchMarkerAtCoordinates(Point point) {
        if (searchMarker == null || mapboxMap == null || mapContainer == null) {
            return;
        }
        
        try {
            // Convert geographic coordinates to screen coordinates
            ScreenCoordinate screenCoord = mapboxMap.pixelForCoordinate(point);
            
            // Get map container dimensions
            int containerWidth = mapContainer.getWidth();
            int containerHeight = mapContainer.getHeight();
            
            if (containerWidth <= 0 || containerHeight <= 0) {
                // Container not ready yet, try again later
                searchMarkerHandler.postDelayed(() -> {
                    positionSearchMarkerAtCoordinates(point);
                }, 100);
                return;
            }
            
            // Calculate marker position
            double x = screenCoord.getX();
            double y = screenCoord.getY();
            
            // Check if coordinates are within visible bounds
            int margin = 80;
            if (x >= -margin && x <= containerWidth + margin && 
                y >= -margin && y <= containerHeight + margin) {
                
                ensurePinDimensions();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    pinWidthPx,
                    pinHeightPx
                );
                
                // Center the pin point exactly on the geographic coordinates
                params.leftMargin = (int) Math.round(x - (pinWidthPx / 2.0));
                params.topMargin = (int) Math.round(y - pinHeightPx + pinOffsetPx);
                
                searchMarker.setLayoutParams(params);
                searchMarker.setVisibility(View.VISIBLE);
                
            } else {
                // Marker is outside visible area, hide it
                searchMarker.setVisibility(View.GONE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error positioning search marker", e);
            if (searchMarker != null) {
                searchMarker.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Animates search marker drop like Google Maps
     */
    private void animateSearchMarkerDrop(ImageView markerView) {
        markerView.setScaleX(0.1f);
        markerView.setScaleY(0.1f);
        markerView.setTranslationY(-50f);
        markerView.setAlpha(0.8f);
        
        markerView.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .translationY(0f)
            .alpha(1.0f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
            .start();
    }
    
    /**
     * Starts camera tracking for search marker
     */
    private void startSearchMarkerCameraTracking() {
        // Stop any existing tracking first
        stopSearchMarkerCameraTracking();
        
        if (searchMarkerRunnable == null) {
            searchMarkerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (searchMarker != null && searchMarkerLocation != null) {
                        positionSearchMarkerAtCoordinates(searchMarkerLocation);
                    }
                    
                    if (searchMarkerHandler != null && searchMarker != null) {
                        searchMarkerHandler.postDelayed(this, 16); // Update every 16ms (~60fps)
                    }
                }
            };
        }
        
        if (searchMarkerHandler != null) {
            searchMarkerHandler.post(searchMarkerRunnable);
        }
    }
    
    /**
     * Stops camera tracking for search marker
     */
    private void stopSearchMarkerCameraTracking() {
        if (searchMarkerHandler != null && searchMarkerRunnable != null) {
            searchMarkerHandler.removeCallbacks(searchMarkerRunnable);
        }
    }
    
    /**
     * Clears the search marker
     */
    private void clearSearchMarker() {
        stopSearchMarkerCameraTracking();
        
        if (searchMarker != null && mapContainer != null) {
            mapContainer.removeView(searchMarker);
            searchMarker = null;
            searchMarkerLocation = null;
            Log.d(TAG, "Search marker cleared");
        }
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
        try {
            if (searchResultsContainer != null) {
                searchResultsContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Autocomplete dropdown shown");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing search results", e);
        }
    }

    private void hideSearchResults() {
        try {
            if (searchResultsContainer != null) {
                searchResultsContainer.setVisibility(View.GONE);
                Log.d(TAG, "Autocomplete dropdown hidden");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding search results", e);
        }
    }
    
    private void showNoResultsMessage() {
        try {
            if (simpleSearchAdapter != null) {
                // Show empty results with "No results found" message
                List<SimpleSearchAdapter.SearchPlace> emptyList = new ArrayList<>();
                simpleSearchAdapter.updatePlaces(emptyList);
                currentAutocompleteResults = new ArrayList<>();
                
                // Could optionally show a "No results" placeholder
                hideSearchResults();
                Log.d(TAG, "No autocomplete results found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing no results message", e);
        }
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
        alertFab.setOnClickListener(v -> {
            showMapStyleSelector();
        });

        pinLocationFab.setOnClickListener(v -> {
            if (isCurrentLocationActive) {
                // If current location is already active, clear it
                clearCurrentLocationPin();
            } else {
                // Get current location and pin it
                pinCurrentLocation();
            }
        });

        // Long press on pin FAB to clear all pins
        pinLocationFab.setOnLongClickListener(v -> {
            clearAllPins();
            clearCurrentLocationPin();
            return true;
        });

        if (helpFab != null) {
            helpFab.setOnClickListener(v -> showHelpDialog());
        }

        // Long press on alert FAB to show map options
        alertFab.setOnLongClickListener(v -> {
            // Show options: navigate to Lucban OR refresh pins
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Map Options");
            builder.setItems(new CharSequence[]{"Center on Lucban", "Refresh Pins", "Create Road Accident Pin"}, (dialog, which) -> {
                if (which == 0) {
                    Point lucbanCenter = Point.fromLngLat(121.5564, 14.1136);
                    animateToLocation(lucbanCenter, 15.0);
                } else if (which == 1) {
                    refreshPinsFromFirestore();
                } else if (which == 2) {
                    createRoadAccidentPinAtCameraCenter();
                }
            });
            builder.show();
            return true;
        });

        filterButton.setOnClickListener(v -> {
            toggleFilterPanel();
        });

        profile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MapViewActivity.this, ProfileActivity.class);
                intent.putExtra("from_activity", "MapViewActivity");
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                // Don't call finish() - keep MapViewActivity in the back stack
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to Profile: " + e.getMessage(), e);
                // Error opening profile (toast removed)
            }
        });

        // Map container click listener to close filter panel
        if (mainMapContainer != null) {
            mainMapContainer.setOnClickListener(v -> {
                if (isFilterPanelVisible) {
                    hideFilterPanel();
                }
            });
        }

        // Filter panel overlay click listener to close panel when clicking outside
        if (filterPanelOverlay != null) {
            filterPanelOverlay.setOnClickListener(v -> {
                if (isFilterPanelVisible) {
                    hideFilterPanel();
                }
            });
        }

    }

    private void setupBottomNavigation() {
        homeTab.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, MainDashboard.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } catch (Exception e) {
                // Error navigating to Home
            }
        });

        chatTab.setOnClickListener(v -> {
            try{
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } catch (Exception e) {
                // Chat feature coming soon
            }
        });

        reportTab.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ReportSubmissionActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        
        // Cleanup current location camera tracking
        stopCurrentLocationCameraTracking();
        
        // Cleanup camera update handler
        if (cameraUpdateHandler != null) {
            cameraUpdateHandler.removeCallbacksAndMessages(null);
        }
        
        // Cleanup Firestore pin camera tracking
        stopFirestorePinCameraTracking();
        
        // Cleanup Firestore pin camera handler
        if (firestorePinCameraHandler != null) {
            firestorePinCameraHandler.removeCallbacksAndMessages(null);
        }
    }

    private void pinCurrentLocation() {
        if (checkLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            // Show loading message
            // Getting current location (toast removed)
            
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Check if location is recent (within 5 minutes)
                            long locationAge = System.currentTimeMillis() - location.getTime();
                            if (locationAge < 5 * 60 * 1000) {
                                handleCurrentLocationUpdate(location);
                            } else {
                                // Location is too old, try to get fresh location
                                requestFreshLocation();
                            }
                        } else {
                            // No last location, try to get fresh location
                            requestFreshLocation();
                        }
                    }
                });
        } else {
            requestLocationPermission();
        }
    }
    
    /**
     * Request fresh location updates
     */
    private void requestFreshLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            // Use location callback for fresh location
            com.google.android.gms.location.LocationCallback locationCallback = new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {
                        Location location = locationResult.getLastLocation();
                        handleCurrentLocationUpdate(location);
                        
                        // Stop location updates after getting the location
                        stopLocationUpdates();
                    }
                }
            };
            
            // Create location request
            com.google.android.gms.location.LocationRequest locationRequest = new com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(10000)
                .build();
            
            // Request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while getting location", e);
            // Location permission denied (toast removed)
        }
    }
    
    /**
     * Stop location updates
     */
    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            // Remove all location callbacks
            fusedLocationClient.removeLocationUpdates(new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                    // Empty implementation
                }
            });
        }
    }
    
    /**
     * Handle current location update (like MapPickerActivity)
     */
    private void handleCurrentLocationUpdate(Location location) {
        if (location != null) {
            // Create point from current location
            Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            
            // Clear existing current location pin
            clearCurrentLocationPin();
            
            // Set as current location point
            currentLocationPoint = currentPoint;
            isCurrentLocationActive = true;
            
            // Add current location marker (fixed positioning like MapPickerActivity)
            addCurrentLocationMarker(currentPoint);
            
            // Animate to current location with higher zoom
            animateToLocation(currentPoint, 17.0);
            
            // Show success message
            // Current location pinned (toast removed)
            
            Log.d(TAG, "Current location: " + location.getLatitude() + ", " + location.getLongitude());
        } else {
            // Unable to get current location (toast removed)
        }
    }

    /**
     * Add current location marker with fixed positioning (like MapPickerActivity)
     */
    private void addCurrentLocationMarker(Point point) {
        Log.d(TAG, "addCurrentLocationMarker called");
        
        if (mapContainer != null) {
            try {
                Log.d(TAG, "Map container found, creating current location marker...");
                ensurePinDimensions();
                
                // Create marker
                ImageView markerView = new ImageView(this);
                Bitmap markerBitmap = createCurrentLocationMarkerBitmap();
                markerView.setImageBitmap(markerBitmap);
                Log.d(TAG, "Current location marker bitmap created: " + markerBitmap.getWidth() + "x" + markerBitmap.getHeight());
                FrameLayout.LayoutParams initialParams = new FrameLayout.LayoutParams(pinWidthPx, pinHeightPx);
                markerView.setLayoutParams(initialParams);
                
                // Add to container
                mapContainer.addView(markerView);
                currentLocationMarker = markerView;
                Log.d(TAG, "Current location marker added to container");
                
                // Position marker at actual coordinates immediately
                positionCurrentLocationMarkerAtCoordinates(point);
                Log.d(TAG, "Current location marker positioned");
                
                // Add Google Maps-style drop animation
                animateCurrentLocationMarkerDrop(markerView);
                Log.d(TAG, "Current location marker animation started");
                
                // Start camera tracking to keep marker positioned correctly
                startCurrentLocationCameraTracking();
                Log.d(TAG, "Current location camera tracking started");
                
                Log.d(TAG, "Current location marker successfully added at: " + point.longitude() + ", " + point.latitude());
                
            } catch (Exception e) {
                Log.e(TAG, "Error adding current location marker", e);
                // Error creating current location marker (toast removed)
            }
        } else {
            Log.e(TAG, "mapContainer is NULL - cannot add current location marker!");
            // Error: Map container not found (toast removed)
        }
    }
    
    /**
     * Create current location marker bitmap (blue pin like Google Maps)
     */
    private Bitmap createCurrentLocationMarkerBitmap() {
        ensurePinDimensions();
        Drawable pinDrawable = ContextCompat.getDrawable(this, R.drawable.accizard_pin);
        if (pinDrawable == null) {
            return Bitmap.createBitmap(pinWidthPx, pinHeightPx, Bitmap.Config.ARGB_8888);
        }

        Bitmap bitmap = Bitmap.createBitmap(pinWidthPx, pinHeightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        pinDrawable.setBounds(0, 0, pinWidthPx, pinHeightPx);
        pinDrawable.draw(canvas);

        return bitmap;
    }
    
    /**
     * Position current location marker at specific coordinates with high precision (like MapPickerActivity)
     */
    private void positionCurrentLocationMarkerAtCoordinates(Point point) {
        if (currentLocationMarker != null && mapboxMap != null && mapContainer != null) {
            if (heatmapEnabled && hasActiveHazardFilter()) {
                currentLocationMarker.setVisibility(View.GONE);
                return;
            }
            try {
                ensurePinDimensions();
                // Convert geographic coordinates to screen coordinates with high precision
                ScreenCoordinate screenCoord = mapboxMap.pixelForCoordinate(point);
                
                // Get map container dimensions
                int containerWidth = mapContainer.getWidth();
                int containerHeight = mapContainer.getHeight();
                
                if (containerWidth <= 0 || containerHeight <= 0) {
                    // Container not ready yet, try again later
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        positionCurrentLocationMarkerAtCoordinates(point);
                    }, 100);
                    return;
                }
                
                // Calculate marker position relative to container with high precision
                double x = screenCoord.getX();
                double y = screenCoord.getY();
                
                // Check if coordinates are within visible bounds (with some margin)
                int margin = dpToPx(40f); // Allow marker to be slightly outside bounds
                if (x >= -margin && x <= containerWidth + margin && 
                    y >= -margin && y <= containerHeight + margin) {
                    
                    // Create layout parameters with absolute positioning
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        pinWidthPx,
                        pinHeightPx
                    );
                    
                    // Center the pin point exactly on the geographic coordinates
                    params.leftMargin = (int) Math.round(x - (pinWidthPx / 2.0));
                    params.topMargin = (int) Math.round(y - pinHeightPx + currentLocationPinOffsetPx);
                    
                    currentLocationMarker.setLayoutParams(params);
                    currentLocationMarker.setVisibility(View.VISIBLE);
                    
                    // Log positioning for debugging
                    Log.d(TAG, String.format("Current location marker positioned at screen coords: %.2f, %.2f -> margin: %d, %d", 
                        x, y, params.leftMargin, params.topMargin));
                    
                } else {
                    // Marker is outside visible area, hide it
                    currentLocationMarker.setVisibility(View.GONE);
                    Log.d(TAG, "Current location marker hidden - outside visible bounds");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error positioning current location marker", e);
                // Keep marker hidden if positioning fails
                if (currentLocationMarker != null) {
                    currentLocationMarker.setVisibility(View.GONE);
                }
            }
        }
    }
    
    /**
     * Animate current location marker drop like Google Maps
     */
    private void animateCurrentLocationMarkerDrop(ImageView markerView) {
        // Start from above and scale
        markerView.setScaleX(0.1f);
        markerView.setScaleY(0.1f);
        markerView.setTranslationY(-50f);
        markerView.setAlpha(0.8f);
        
        // Animate to final position
        markerView.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .translationY(0f)
            .alpha(1.0f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
            .start();
    }
    
    /**
     * Start camera tracking for current location marker (like MapPickerActivity)
     */
    private void startCurrentLocationCameraTracking() {
        if (cameraUpdateRunnable == null) {
            cameraUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    // Update current location marker position if it exists
                    if (currentLocationMarker != null && currentLocationPoint != null) {
                        positionCurrentLocationMarkerAtCoordinates(currentLocationPoint);
                    }
                    
                    // Schedule next update
                    if (cameraUpdateHandler != null) {
                        cameraUpdateHandler.postDelayed(this, 50); // Update every 50ms for smooth tracking
                    }
                }
            };
        }
        
        // Start the tracking
        if (cameraUpdateHandler != null) {
            cameraUpdateHandler.post(cameraUpdateRunnable);
        }
    }
    
    /**
     * Stop current location camera tracking
     */
    private void stopCurrentLocationCameraTracking() {
        if (cameraUpdateHandler != null && cameraUpdateRunnable != null) {
            cameraUpdateHandler.removeCallbacks(cameraUpdateRunnable);
        }
    }
    
    /**
     * Clear current location pin
     */
    private void clearCurrentLocationPin() {
        if (currentLocationMarker != null && mapContainer != null) {
            mapContainer.removeView(currentLocationMarker);
            currentLocationMarker = null;
            currentLocationPoint = null;
            isCurrentLocationActive = false;
            
            // Stop camera tracking when no current location marker
            stopCurrentLocationCameraTracking();
            
            Log.d(TAG, "Current location pin cleared");
        }
    }

    private void addPinToMap(Point point, String title) {
        // Create a custom marker view
        ensurePinDimensions();
        ImageView markerView = new ImageView(this);
        markerView.setImageResource(R.drawable.ic_location);
        markerView.setColorFilter(Color.RED);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            pinWidthPx,
            pinHeightPx
        );
        params.gravity = Gravity.CENTER;
        markerView.setLayoutParams(params);
        
        // Add marker to the map container
        if (mapContainer != null) {
            mapContainer.addView(markerView);
            
            // Create MapMarker object and add to list
            MapMarker mapMarker = new MapMarker(markerView, point, title);
            pinnedMarkers.add(mapMarker);
            pinnedLocations.add(point);

            if (heatmapEnabled && hasActiveHazardFilter()) {
                markerView.setVisibility(View.GONE);
            }
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

    private void loadUserProfilePicture() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && profile != null) {
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
                                ProfilePictureCache.getInstance().loadProfilePicture(profile, profilePictureUrl);
                            } else {
                                Log.d(TAG, "No profile picture URL found in Firestore");
                                // Try to check if profile picture exists in Firebase Storage
                                checkProfilePictureInStorage(user.getUid());
                            }
                        } else {
                            Log.d(TAG, "No user document found for firebaseUid: " + user.getUid());
                            setDefaultProfileIcon();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading profile picture", e);
                        setDefaultProfileIcon();
                    });
            } else {
                setDefaultProfileIcon();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfilePicture: " + e.getMessage(), e);
            setDefaultProfileIcon();
        }
    }

    private void checkProfilePictureInStorage(String firebaseUid) {
        try {
            // Try to construct the profile picture path and check if it exists
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference profileRef = storage.getReference().child("profile_pictures/" + firebaseUid + "/profile.jpg");
            
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Found profile picture in Storage: " + uri.toString());
                // Use cached loading for instant display
                ProfilePictureCache.getInstance().loadProfilePicture(profile, uri.toString());
                // Update Firestore with the found URL
                updateProfilePictureUrlInFirestore(uri.toString());
            }).addOnFailureListener(e -> {
                Log.d(TAG, "No profile picture found in Storage for UID: " + firebaseUid);
                // Set default profile icon if no picture found
                setDefaultProfileIcon();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking profile picture in storage: " + e.getMessage(), e);
            setDefaultProfileIcon();
        }
    }

    private void updateProfilePictureUrlInFirestore(String profilePictureUrl) {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
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
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
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
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile picture URL in Firestore: " + e.getMessage(), e);
        }
    }

    private void loadProfileImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profile != null) {
                        // Create circular bitmap
                        android.graphics.Bitmap circularBitmap = createCircularProfileBitmap(bitmap);
                        profile.setImageBitmap(circularBitmap);
                        Log.d(TAG, "Profile picture loaded successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL", e);
                runOnUiThread(() -> setDefaultProfileIcon());
            }
        }).start();
    }

    private android.graphics.Bitmap createCircularProfileBitmap(android.graphics.Bitmap bitmap) {
        try {
            // Center-crop to square first to avoid distortion
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int squareSize = Math.min(width, height);
            int xOffset = (width - squareSize) / 2;
            int yOffset = (height - squareSize) / 2;

            android.graphics.Bitmap squareCropped = android.graphics.Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

            int targetSize = 150; // Size for profile button
            android.graphics.Bitmap scaledSquare = squareSize == targetSize
                    ? squareCropped
                    : android.graphics.Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

            android.graphics.Bitmap circularBitmap = android.graphics.Bitmap.createBitmap(targetSize, targetSize, android.graphics.Bitmap.Config.ARGB_8888);
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
        } catch (Exception e) {
            Log.e(TAG, "Error creating circular bitmap: " + e.getMessage(), e);
            return bitmap; // Return original bitmap if circular conversion fails
        }
    }

    private void setDefaultProfileIcon() {
        try {
            if (profile != null) {
                profile.setImageResource(R.drawable.ic_person);
                Log.d(TAG, "Default profile icon set");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting default profile icon: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Set map tab as selected
            setMapTabAsSelected();
            
            // Refresh profile picture when returning to this activity
            loadUserProfilePicture();
            
            // Update notification badge
            updateNotificationBadge();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadgeMap == null) return;
            
            // Use the same logic as AlertsActivity to count new announcements
            int newAnnouncementCount = countNewAnnouncementsFromMap();
            
            if (newAnnouncementCount > 0) {
                alertsBadgeMap.setText(String.valueOf(newAnnouncementCount));
                alertsBadgeMap.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing badge on map with count: " + newAnnouncementCount);
            } else {
                alertsBadgeMap.setVisibility(View.GONE);
                Log.d(TAG, "Hiding badge on map - no new announcements");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge on map: " + e.getMessage(), e);
        }
    }
    
    private int countNewAnnouncementsFromMap() {
        try {
            // Use the same SharedPreferences as AlertsActivity
            long lastVisitTime = sharedPreferences.getLong(KEY_LAST_VISIT_TIME, 0);
            
            // If this is the first visit, don't show any badges
            if (lastVisitTime == 0) {
                return 0;
            }
            
            // For map, we'll fetch announcements and count new ones
            fetchAndCountNewAnnouncementsFromMap(lastVisitTime);
            
            return 0; // Will be updated by the async fetch
        } catch (Exception e) {
            Log.e(TAG, "Error counting new announcements from map: " + e.getMessage(), e);
            return 0;
        }
    }
    
    private void fetchAndCountNewAnnouncementsFromMap(long lastVisitTime) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("announcements")
                .orderBy("createdTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int newCount = 0;
                        Date currentDate = new Date();
                        
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String dateStr = doc.getString("date");
                            if (dateStr != null && isAnnouncementNewFromMap(dateStr, lastVisitTime)) {
                                newCount++;
                            }
                        }
                        
                        // Make newCount final for use in lambda
                        final int finalNewCount = newCount;
                        
                        // Update badge on UI thread
                        runOnUiThread(() -> {
                            if (alertsBadgeMap != null) {
                                if (finalNewCount > 0) {
                                    alertsBadgeMap.setText(String.valueOf(finalNewCount));
                                    alertsBadgeMap.setVisibility(View.VISIBLE);
                                } else {
                                    alertsBadgeMap.setVisibility(View.GONE);
                                }
                            }
                        });
                        
                        Log.d(TAG, "Found " + newCount + " new announcements from map");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching announcements for badge from map: " + e.getMessage(), e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching and counting new announcements from map: " + e.getMessage(), e);
        }
    }
    
    private boolean isAnnouncementNewFromMap(String dateStr, long lastVisitTime) {
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
            Log.e(TAG, "Error checking if announcement is new from map: " + e.getMessage(), e);
            return false;
        }
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
    
    /**
     * Update the filter indicator to show active filters
     */
    private void updateFilterIndicator() {
        try {
            if (filterIndicator == null) return;
            
            // Count disabled filters
            int disabledIncidents = 0;
            int disabledFacilities = 0;
            
            for (Boolean enabled : incidentFilters.values()) {
                if (!enabled) disabledIncidents++;
            }
            
            for (Boolean enabled : facilityFilters.values()) {
                if (!enabled) disabledFacilities++;
            }
            
            // Check if heatmap is enabled or any filters are disabled
            hasActiveFilters = heatmapEnabled || disabledIncidents > 0 || disabledFacilities > 0;
            
            // Filter indicator text removed - no summary displayed
            // Hide indicator regardless of filter state
            if (filterIndicator != null) {
                filterIndicator.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating filter indicator: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply filters to map markers and search results
     */
    private void applyFiltersToMap() {
        try {
            Log.d(TAG, "Applying filters to map...");

            List<SimpleSearchAdapter.SearchPlace> filteredPlaces = new ArrayList<>();
            for (SimpleSearchAdapter.SearchPlace place : searchPlaces) {
                if (shouldShowPlace(place)) {
                    filteredPlaces.add(place);
                }
            }

            Log.d(TAG, "Filtered places: " + filteredPlaces.size() + " out of " + searchPlaces.size());

            if (simpleSearchAdapter != null) {
                simpleSearchAdapter.updatePlaces(filteredPlaces);
            }

            // Always apply filters to pins, regardless of layer state
            // Layers (Barangay Boundaries, Road Network, Waterways, Satellite Map) should NOT hide pins
            applyFiltersToFirestorePins();
            updateHeatmapToggleState();

            if (heatmapEnabled && hasActiveHazardFilter()) {
                showHeatmapView();
                setAllPinsVisibility(false);
            } else {
                hideHeatmapView();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error applying filters to map: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply current filter settings to existing Firestore pins
     * UPDATED: Added showToast parameter to control feedback
     */
    private void applyFiltersToFirestorePins() {
        applyFiltersToFirestorePins(true);
    }
    
    /**
     * Apply current filter settings to existing Firestore pins
     * @param showToast Whether to show toast message with results
     */
    private void applyFiltersToFirestorePins(boolean showToast) {
        // Prevent infinite loops when filters and layers are applied simultaneously
        if (isApplyingFiltersOrLayers) {
            Log.d(TAG, "Skipping filter application - already in progress");
            return;
        }
        
        try {
            isApplyingFiltersOrLayers = true;
            Log.d(TAG, "Applying filters to Firestore pins...");
            
            int visiblePins = 0;
            int hiddenPins = 0;
            int totalPins = firestorePinMarkers.size() + userReportPinMarkers.size();
            // Note: layersActive is no longer used to hide pins - layers should NOT hide pins
            boolean isMyReportsFilterActive = incidentFilters.getOrDefault("Report", false);
            
            // Process regular Firestore pins
            for (MapMarker mapMarker : firestorePinMarkers) {
                if (mapMarker.pinData != null) {
                    boolean previouslyVisible = mapMarker.markerView.getVisibility() == View.VISIBLE;
                    
                    // Check heatmap first - if heatmap is active, hide pins
                    if (heatmapEnabled && hasActiveHazardFilter()) {
                        if (previouslyVisible) {
                            mapMarker.markerView.clearAnimation();
                            mapMarker.markerView.setAlpha(1f);
                        }
                        mapMarker.markerView.setVisibility(View.GONE);
                        hiddenPins++;
                        continue;
                    }
                    
                    // Always check filter visibility - layers do NOT hide pins
                    boolean shouldShow = shouldShowPinBasedOnFilters(mapMarker.pinData);

                    // Only update visibility if it changed to prevent glitching
                    if (shouldShow) {
                        if (!previouslyVisible) {
                            mapMarker.markerView.clearAnimation();
                            mapMarker.markerView.setAlpha(1f);
                            mapMarker.markerView.setVisibility(View.VISIBLE);
                            mapMarker.markerView.bringToFront();
                            visiblePins++;
                        } else {
                            // Already visible, no need to update
                            visiblePins++;
                        }
                    } else {
                        if (previouslyVisible) {
                            mapMarker.markerView.clearAnimation();
                            mapMarker.markerView.setAlpha(1f);
                            mapMarker.markerView.setVisibility(View.GONE);
                            hiddenPins++;
                        } else {
                            // Already hidden, no need to update
                            hiddenPins++;
                        }
                    }
                }
            }
            
            // Process user report pins
            // When "My Reports" filter is active, show user report pins with their custom icons
            int userReportPinsCount = userReportPinMarkers.size();
            int visibleUserReportPins = 0;
            
            Log.d(TAG, "Processing " + userReportPinsCount + " user report pins. My Reports filter active: " + isMyReportsFilterActive);
            
            for (MapMarker mapMarker : userReportPinMarkers) {
                if (mapMarker.pinData != null && mapMarker.markerView != null) {
                    boolean previouslyVisible = mapMarker.markerView.getVisibility() == View.VISIBLE;
                    
                    // When "My Reports" is active, show ALL user report pins (shouldShowPinBasedOnFilters already handles this)
                    // But we need to check if "My Reports" is actually active
                    boolean shouldShow = false;
                    if (isMyReportsFilterActive) {
                        // When "My Reports" is active, shouldShowPinBasedOnFilters returns true for user report pins
                        shouldShow = shouldShowPinBasedOnFilters(mapMarker.pinData);
                        Log.d(TAG, "User report pin check - My Reports active: " + isMyReportsFilterActive + 
                            ", shouldShow: " + shouldShow + ", Pin ID: " + mapMarker.pinData.getId() + 
                            ", Type: " + mapMarker.pinData.getType());
                    } else {
                        // When "My Reports" is not active, hide user report pins
                        shouldShow = false;
                    }
                    
                    if (shouldShow) {
                        if (!previouslyVisible) {
                            mapMarker.markerView.clearAnimation();
                            mapMarker.markerView.setAlpha(1f);
                            mapMarker.markerView.setVisibility(View.VISIBLE);
                            mapMarker.markerView.bringToFront();
                            visiblePins++;
                            visibleUserReportPins++;
                            Log.d(TAG, "✅ Showing user report pin: " + mapMarker.pinData.getType() + " at " + 
                                mapMarker.pinData.getLatitude() + ", " + mapMarker.pinData.getLongitude());
                        } else {
                            visiblePins++;
                            visibleUserReportPins++;
                        }
                    } else {
                        if (previouslyVisible) {
                            mapMarker.markerView.clearAnimation();
                            mapMarker.markerView.setAlpha(1f);
                            mapMarker.markerView.setVisibility(View.GONE);
                            hiddenPins++;
                            Log.d(TAG, "⚠️ Hiding user report pin: " + mapMarker.pinData.getType() + 
                                " (My Reports: " + isMyReportsFilterActive + ")");
                        } else {
                            hiddenPins++;
                        }
                    }
                } else {
                    Log.w(TAG, "⚠️ User report pin marker has null data or view");
                }
            }
            
            Log.d(TAG, "═══════════════════════════════════════════════════════");
            Log.d(TAG, "📊 FILTERING SUMMARY:");
            Log.d(TAG, "  - Total pins: " + totalPins);
            Log.d(TAG, "    • Firestore pins: " + firestorePinMarkers.size());
            Log.d(TAG, "    • User report pins: " + userReportPinsCount);
            Log.d(TAG, "  - Visible pins: " + visiblePins);
            Log.d(TAG, "    • User report pins visible: " + visibleUserReportPins);
            Log.d(TAG, "  - Hidden pins: " + hiddenPins);
            Log.d(TAG, "  - My Reports filter active: " + isMyReportsFilterActive);
            Log.d(TAG, "═══════════════════════════════════════════════════════");
            
            // Log details of each user report pin for debugging
            if (isMyReportsFilterActive && userReportPinsCount > 0) {
                Log.d(TAG, "🔍 DETAILED USER REPORT PINS STATUS:");
                for (int i = 0; i < userReportPinMarkers.size(); i++) {
                    MapMarker marker = userReportPinMarkers.get(i);
                    if (marker != null && marker.pinData != null) {
                        boolean isVisible = marker.markerView != null && marker.markerView.getVisibility() == View.VISIBLE;
                        boolean shouldShow = shouldShowPinBasedOnFilters(marker.pinData);
                        Log.d(TAG, "  Pin #" + (i + 1) + ":");
                        Log.d(TAG, "    - ID: " + marker.pinData.getId());
                        Log.d(TAG, "    - Type: " + marker.pinData.getType());
                        Log.d(TAG, "    - Coordinates: " + marker.pinData.getLatitude() + ", " + marker.pinData.getLongitude());
                        Log.d(TAG, "    - Should show (filter): " + shouldShow);
                        Log.d(TAG, "    - Currently visible: " + isVisible);
                        Log.d(TAG, "    - Marker view: " + (marker.markerView != null ? "EXISTS" : "NULL"));
                        if (marker.markerView != null) {
                            Log.d(TAG, "    - View visibility: " + (marker.markerView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/HIDDEN"));
                        }
                    }
                }
                Log.d(TAG, "═══════════════════════════════════════════════════════");
            }
            
            // Show toast if no pins match the active filter
            // "My Reports" filter: No toast messages - just show/hide pins silently
            if (showToast && !isMyReportsFilterActive && visiblePins == 0 && totalPins > 0 && !(heatmapEnabled && hasActiveHazardFilter())) {
                // For other filters (not "My Reports"), show appropriate toast messages
                // Get the active filter name(s)
                String activeFilterName = getActiveFilterName();
                if (activeFilterName != null && !activeFilterName.isEmpty()) {
                    Toast.makeText(this, "No pins found for " + activeFilterName, Toast.LENGTH_SHORT).show();
                } else {
                    // Check if any filters are actually active
                    boolean hasActiveFilters = false;
                    for (Boolean enabled : incidentFilters.values()) {
                        if (enabled) {
                            hasActiveFilters = true;
                            break;
                        }
                    }
                    if (!hasActiveFilters) {
                        for (Boolean enabled : facilityFilters.values()) {
                            if (enabled) {
                                hasActiveFilters = true;
                                break;
                            }
                        }
                    }
                    if (hasActiveFilters) {
                        Toast.makeText(this, "No pins found for the selected filter", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            // Removed success toast - only show toast when no pins are found
            
            // Update heatmap if enabled and has active hazard filters
            // Layers do NOT affect heatmap visibility - heatmap works independently
            if (heatmapEnabled && hasActiveHazardFilter()) {
                updateHeatmapLayerData();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying filters to Firestore pins: " + e.getMessage(), e);
        } finally {
            isApplyingFiltersOrLayers = false;
        }
    }
    
    /**
     * Get the name of the currently active filter
     */
    private String getActiveFilterName() {
        // Check incident filters
        for (String filterName : incidentFilters.keySet()) {
            if (incidentFilters.getOrDefault(filterName, false)) {
                return filterName;
            }
        }
        
        // Check facility filters
        for (String filterName : facilityFilters.keySet()) {
            if (facilityFilters.getOrDefault(filterName, false)) {
                return filterName;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a place should be shown based on current filters
     */
    private boolean shouldShowPlace(SimpleSearchAdapter.SearchPlace place) {
        try {
            String name = place.getName().toLowerCase();
            String address = place.getAddress().toLowerCase();
            
            // Check facility filters
            if (name.contains("hospital") || name.contains("medical") || name.contains("health")) {
                return facilityFilters.getOrDefault("Health Facilities", false);
            }
            
            if (name.contains("police") || name.contains("station")) {
                return facilityFilters.getOrDefault("Police Stations", false);
            }
            
            if (name.contains("fire")) {
                return facilityFilters.getOrDefault("Fire Stations", false);
            }
            
            if (name.contains("hall") || name.contains("government") || name.contains("municipal") || 
                name.contains("capitol") || name.contains("office")) {
                return facilityFilters.getOrDefault("Government Offices", false);
            }
            
            if (name.contains("evacuation") || name.contains("center")) {
                return facilityFilters.getOrDefault("Evacuation Centers", false);
            }
            
            // If no specific category matches, show by default
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking if place should be shown: " + e.getMessage(), e);
            return true;
        }
    }
    
    private void showHeatmapView() {
        if (!heatmapEnabled || !hasActiveHazardFilter() || mapboxMap == null) {
            return;
        }
        try {
            Style style = mapboxMap.getStyle();
            if (style == null) {
                return;
            }
            ensureHeatmapLayer(style);
            updateHeatmapLayerData(style);
            setHeatmapVisibility(style, true);
            setAllPinsVisibility(false);
            Log.d(TAG, "Heatmap view enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error showing heatmap view: " + e.getMessage(), e);
        }
    }
    
    private void hideHeatmapView() {
        try {
            if (mapboxMap == null) return;
            Style style = mapboxMap.getStyle();
            if (style == null) return;
            setHeatmapVisibility(style, false);
            setAllPinsVisibility(true);
            applyFiltersToFirestorePins(false);
            Log.d(TAG, "Heatmap view disabled");
        } catch (Exception e) {
            Log.e(TAG, "Error hiding heatmap view: " + e.getMessage(), e);
        }
    }

    private void ensureHeatmapLayer(Style style) {
        if (style == null) return;
        HeatmapHelper.INSTANCE.ensureComponents(style, HEATMAP_SOURCE_ID, HEATMAP_LAYER_ID);
    }

    private void updateHeatmapLayerData() {
        if (!heatmapEnabled || mapboxMap == null) {
            return;
        }

        Style style = mapboxMap.getStyle();
        if (style == null) {
            return;
        }

        updateHeatmapLayerData(style);
    }

    private void updateHeatmapLayerData(Style style) {
        if (style == null || !heatmapEnabled) return;
        try {
            ensureHeatmapLayer(style);
        } catch (Exception e) {
            Log.w(TAG, "Unable to ensure heatmap layer: " + e.getMessage());
            return;
        }

        List<Feature> features = new ArrayList<>();
        
        // Add Firestore pins to heatmap
        for (MapMarker mapMarker : firestorePinMarkers) {
            if (mapMarker != null && mapMarker.location != null && mapMarker.pinData != null) {
                if (isHazardCategory(mapMarker.pinData.getCategory()) &&
                        shouldShowPinBasedOnFilters(mapMarker.pinData)) {
                    features.add(Feature.fromGeometry(Point.fromLngLat(
                            mapMarker.location.longitude(),
                            mapMarker.location.latitude()
                    )));
                }
            }
        }
        
        // Add user report pins to heatmap when "My Reports" is active
        boolean isMyReportsActive = incidentFilters.getOrDefault("Report", false);
        if (isMyReportsActive) {
            for (MapMarker mapMarker : userReportPinMarkers) {
                if (mapMarker != null && mapMarker.location != null && mapMarker.pinData != null) {
                    // Include user report pins in heatmap - they can be any hazard type
                    if (shouldShowPinBasedOnFilters(mapMarker.pinData)) {
                        features.add(Feature.fromGeometry(Point.fromLngLat(
                                mapMarker.location.longitude(),
                                mapMarker.location.latitude()
                        )));
                    }
                }
            }
        }

        FeatureCollection collection = FeatureCollection.fromFeatures(features);
        HeatmapHelper.INSTANCE.updateFeatures(style, HEATMAP_SOURCE_ID, collection);
        setHeatmapVisibility(style, !features.isEmpty());
    }

    private void setHeatmapVisibility(Style style, boolean visible) {
        if (style == null) return;
        try {
            HeatmapHelper.INSTANCE.setVisibility(style, HEATMAP_LAYER_ID, visible);
        } catch (Exception e) {
            Log.w(TAG, "Unable to change heatmap visibility: " + e.getMessage());
        }
    }

    private boolean hasActiveHazardFilter() {
        for (String key : HAZARD_FILTER_KEYS) {
            if (incidentFilters.getOrDefault(key, false)) {
                return true;
            }
        }
        return false;
    }

    private void updateHeatmapToggleState() {
        boolean hasHazard = hasActiveHazardFilter();
        if (heatmapSwitch == null) {
            return;
        }

        heatmapSwitch.setEnabled(hasHazard);
        heatmapSwitch.setAlpha(hasHazard ? 1f : 0.5f);

        if (!hasHazard && heatmapEnabled) {
            isUpdatingHeatmapSwitch = true;
            heatmapEnabled = false;
            heatmapSwitch.setChecked(false);
            isUpdatingHeatmapSwitch = false;
            hideHeatmapView();
        }
    }
    
    /**
     * Get current filter summary for debugging
     */
    private String getFilterSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Heatmap: ").append(heatmapEnabled ? "ON" : "OFF").append("\n");
        summary.append("Time Range: ").append(selectedTimeRange).append("\n");
        summary.append("Incident Filters:\n");
        
        for (Map.Entry<String, Boolean> entry : incidentFilters.entrySet()) {
            summary.append("  ").append(entry.getKey()).append(": ").append(entry.getValue() ? "ON" : "OFF").append("\n");
        }
        
        summary.append("Facility Filters:\n");
        for (Map.Entry<String, Boolean> entry : facilityFilters.entrySet()) {
            summary.append("  ").append(entry.getKey()).append(": ").append(entry.getValue() ? "ON" : "OFF").append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Show the map style selector dialog
     */
    private void showMapStyleSelector() {
        try {
            // Create dialog
            mapStyleDialog = new Dialog(this);
            mapStyleDialog.setContentView(R.layout.dialog_map_style_selector);
            mapStyleDialog.setCancelable(true);
            
            // Set dialog size
            mapStyleDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            
            // Initialize dialog views
            setupMapStyleDialog();
            
            // Show dialog
            mapStyleDialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing map style selector: " + e.getMessage(), e);
            // Error opening map styles (toast removed)
        }
    }
    
    /**
     * Setup the map style selector dialog
     */
    private void setupMapStyleDialog() {
        try {
            // Close button
            ImageView closeButton = mapStyleDialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(v -> mapStyleDialog.dismiss());
            
            // Map layers checkboxes
            CheckBox barangayCheck = mapStyleDialog.findViewById(R.id.barangayBoundariesCheck);
            CheckBox roadNetworkCheck = mapStyleDialog.findViewById(R.id.roadNetworkCheck);
            CheckBox waterwaysCheck = mapStyleDialog.findViewById(R.id.waterwaysCheck);
            CheckBox satelliteCheck = mapStyleDialog.findViewById(R.id.satelliteMapCheck);
            
            // Set initial states
            if (barangayCheck != null) {
                barangayCheck.setChecked(barangayBoundariesVisible);
            }
            if (roadNetworkCheck != null) {
                roadNetworkCheck.setChecked(roadNetworkVisible);
            }
            if (waterwaysCheck != null) {
                waterwaysCheck.setChecked(waterwaysVisible);
            }
            if (satelliteCheck != null) {
                satelliteCheck.setChecked(satelliteMapVisible);
            }
            
            // Layer click listeners (clicking the row toggles the checkbox)
            LinearLayout barangayLayer = mapStyleDialog.findViewById(R.id.barangayBoundariesLayer);
            LinearLayout roadNetworkLayer = mapStyleDialog.findViewById(R.id.roadNetworkLayer);
            LinearLayout waterwaysLayer = mapStyleDialog.findViewById(R.id.waterwaysLayer);
            LinearLayout satelliteLayer = mapStyleDialog.findViewById(R.id.satelliteMapLayer);
            
            if (barangayLayer != null && barangayCheck != null) {
                barangayLayer.setOnClickListener(v -> {
                    boolean newState = !barangayCheck.isChecked();
                    barangayCheck.setChecked(newState);
                    toggleBarangayBoundaries(newState);
                });
            }
            
            if (roadNetworkLayer != null && roadNetworkCheck != null) {
                roadNetworkLayer.setOnClickListener(v -> {
                    boolean newState = !roadNetworkCheck.isChecked();
                    roadNetworkCheck.setChecked(newState);
                    toggleRoadNetwork(newState);
                });
            }
            
            if (waterwaysLayer != null && waterwaysCheck != null) {
                waterwaysLayer.setOnClickListener(v -> {
                    boolean newState = !waterwaysCheck.isChecked();
                    waterwaysCheck.setChecked(newState);
                    toggleWaterways(newState);
                });
            }
            
            if (satelliteLayer != null && satelliteCheck != null) {
                satelliteLayer.setOnClickListener(v -> {
                    boolean newState = !satelliteCheck.isChecked();
                    // Prevent checkbox listener from firing when we programmatically set it
                    isUpdatingSatelliteCheckbox = true;
                    satelliteCheck.setChecked(newState);
                    isUpdatingSatelliteCheckbox = false;
                    // FIXED: Invert the parameter to match the corrected checkbox listener logic
                    toggleSatelliteMap(!newState);
                });
            }
            
            // Direct checkbox listeners
            if (barangayCheck != null) {
                barangayCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toggleBarangayBoundaries(isChecked);
                });
            }
            
            if (roadNetworkCheck != null) {
                roadNetworkCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toggleRoadNetwork(isChecked);
                });
            }
            
            if (waterwaysCheck != null) {
                waterwaysCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toggleWaterways(isChecked);
                });
            }
            
            if (satelliteCheck != null) {
                satelliteCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Skip if we're programmatically updating the checkbox (to prevent double-trigger)
                    if (isUpdatingSatelliteCheckbox) {
                        return;
                    }
                    // FIXED: The logic was inverted - when checkbox is checked, show satellite
                    // When checkbox is unchecked, show street view
                    // The behavior was backwards, so we need to invert the parameter
                    toggleSatelliteMap(!isChecked);
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map style dialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load the base custom style and prepare optional satellite raster base layer
     * INITIAL VIEW: Street View Map (satellite is hidden by default)
     */
    private void loadMapStyle() {
        try {
            if (mapboxMap == null) return;
            
            // Ensure satellite is disabled for initial street view
            satelliteMapVisible = false;
            
            // Always load the custom street style as the base map
            // This is the default initial view - Street View Map
            String styleUri = "mapbox://styles/accizard-lucban-official/cmhox8ita005o01sr1psmbgp6";
            
            mapboxMap.loadStyleUri(styleUri, loadedStyle -> {
                Log.d(TAG, "Map style loaded - INITIAL VIEW: Street View Map (same as MapPickerActivity)");
                
                // Disable compass after style is loaded (compass view is created when style loads)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    disableCompass();
                }, 100);
                
                // Initialize camera animations plugin
                if (cameraAnimationsPlugin == null) {
                    cameraAnimationsPlugin = mapView.getPlugin("com.mapbox.maps.plugin.animation.camera");
                }
                
                // Set initial camera position to Lucban center (same as MapPickerActivity)
                Point lucbanCenter = Point.fromLngLat(121.5564, 14.1136);
                CameraOptions initialCamera = new CameraOptions.Builder()
                        .center(lucbanCenter)
                        .zoom(14.0)
                        .build();
                mapboxMap.setCamera(initialCamera);
                
                // Ensure Lucban boundary is visible on initial load (same as MapPickerActivity)
                ensureLucbanBoundaryVisible(loadedStyle);
                showOnlyLucbanBoundary(loadedStyle);
                
                // Retry with delays to ensure the layer is accessible (same as MapPickerActivity)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Style reloadedStyle = mapboxMap.getStyle();
                    if (reloadedStyle != null) {
                        ensureLucbanBoundaryVisible(reloadedStyle);
                        showOnlyLucbanBoundary(reloadedStyle);
                    }
                }, 300);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Style reloadedStyle = mapboxMap.getStyle();
                    if (reloadedStyle != null) {
                        ensureLucbanBoundaryVisible(reloadedStyle);
                        showOnlyLucbanBoundary(reloadedStyle);
                    }
                }, 800);
                
                // Update marker positions if any pins exist
                updateAllMarkerPositions();
                
                // Initialize map layers
                // This ensures: lucban-boundary is visible, all other layers are hidden
                initializeMapLayers(loadedStyle);

                // Ensure satellite raster base exists but keep it HIDDEN for initial street view
                ensureSatelliteLayer(loadedStyle);
                setSatelliteVisibility(loadedStyle, false); // Explicitly hide satellite for street view
                Log.d(TAG, "Initial map view set to: Street View (satellite layer hidden) - Same as MapPickerActivity");
                
                // Re-apply current layer states after style loads
                // Use multiple delays to ensure the style is fully loaded and layers are accessible
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    reapplyLayerStates();
                }, 1500); // Final retry after 1.5 seconds
                
                // Reload Firestore pins after style change
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    refreshPinsFromFirestore();
                }, 500); // Small delay to ensure map is ready
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading map style: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize map layers (Lucban Boundary, Barangay Boundaries, Road Network, Waterways)
     * Initial state: Only lucban-boundary is visible, all other layers are hidden
     */
    private void initializeMapLayers(Style style) {
        try {
            if (style == null || mapboxMap == null) return;
            
            // Initial map state:
            // - lucban-boundary: ALWAYS VISIBLE (main Lucban boundary outline)
            // - All other layers: HIDDEN by default
            
            // Ensure lucban-boundary is always visible
            lucbanBoundaryVisible = true;
            
            // Ensure other layers are hidden by default
            barangayBoundariesVisible = false;
            roadNetworkVisible = false;
            waterwaysVisible = false;
            healthFacilitiesVisible = false;
            evacuationCentersVisible = false;
            
            // Apply initial visibility states to existing layers in the style
            setLayerVisibility(style, "lucban-brgys", false);
            setLayerVisibility(style, "lucban-fill", false);
            setLayerVisibility(style, "lucban-brgy-names", false);
            setLayerVisibility(style, "road", false);
            setLayerVisibility(style, "waterway", false);
            setLayerVisibility(style, "health-facilities", false);
            setLayerVisibility(style, "evacuation-centers", false);
            
            // Hide pins by default when layers are initialized
            // Pins will only show when NO layers are active
            updatePinsVisibilityBasedOnLayers();
            
            Log.d(TAG, "Map layers initialized - lucban-boundary: visible, others: hidden, pins: hidden");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing map layers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if any map layers are currently active/visible
     */
    private boolean hasAnyLayerActive() {
        return barangayBoundariesVisible || 
               roadNetworkVisible || 
               waterwaysVisible || 
               healthFacilitiesVisible || 
               evacuationCentersVisible;
    }
    
    /**
     * Update pins visibility based on whether any layers are active
     * Pins are ALWAYS shown based on filters, regardless of layer state
     * Layers (Barangay Boundaries, Road Network, Waterways, Satellite Map) do NOT hide pins
     * Only heatmap with active hazard filters will hide pins
     */
    private void updatePinsVisibilityBasedOnLayers() {
        // Prevent infinite loops when filters and layers are applied simultaneously
        if (isApplyingFiltersOrLayers) {
            Log.d(TAG, "Skipping pins visibility update - already in progress");
            return;
        }
        
        try {
            isApplyingFiltersOrLayers = true;
            
            // Only hide pins when heatmap is enabled with active hazard filters
            if (heatmapEnabled && hasActiveHazardFilter()) {
                setAllPinsVisibility(false);
                Log.d(TAG, "Pins visibility updated - Heatmap active, hiding all pins");
                return; // Flag will be reset in finally block
            }
            
            // Always apply filter-based visibility, regardless of layer state
            // Layers (Barangay Boundaries, Road Network, Waterways, Satellite Map) should NOT hide pins
            applyFiltersToFirestorePins(false);
            boolean anyLayerActive = hasAnyLayerActive();
            Log.d(TAG, "Pins visibility updated - Applying filters (layers active: " + anyLayerActive + ")");
        } finally {
            isApplyingFiltersOrLayers = false;
        }
    }
    
    /**
     * Re-apply current layer states to the map style
     * This is called after style loads to ensure layers are properly configured
     * Gets the current style from mapboxMap to ensure we're working with the active style
     */
    private void reapplyLayerStates() {
        try {
            if (mapboxMap == null) {
                Log.w(TAG, "mapboxMap is null, cannot reapply layer states");
                return;
            }
            
            // Get the current style from mapboxMap (not from parameter)
            Style style = mapboxMap.getStyle();
            if (style == null) {
                Log.w(TAG, "Style not ready yet for style: " + (satelliteMapVisible ? "satellite" : "street") + ", will retry");
                return;
            }
            
            Log.d(TAG, "Re-applying layer states to current style: " + (satelliteMapVisible ? "satellite" : "street"));
            
            // Re-apply all layer visibility states based on current flags
            // Use dedicated apply methods for better reliability
            applyBarangayBoundariesVisibility();
            applyRoadNetworkVisibility();
            applyWaterwaysVisibility();
            setLayerVisibility(style, "health-facilities", healthFacilitiesVisible);
            setLayerVisibility(style, "evacuation-centers", evacuationCentersVisible);
            
            // Ensure lucban-boundary is always visible
            setLayerVisibility(style, "lucban-boundary", true);
            
            // Update pins visibility
            updatePinsVisibilityBasedOnLayers();
            
            Log.d(TAG, "Layer states re-applied successfully for style: " + (satelliteMapVisible ? "satellite" : "street"));
        } catch (Exception e) {
            Log.e(TAG, "Error re-applying layer states for style " + (satelliteMapVisible ? "satellite" : "street") + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to set layer visibility using Mapbox Style API
     */
    private void setLayerVisibility(Style style, String layerId, boolean visible) {
        try {
            if (style == null || layerId == null) return;
            
            // Set visibility property using Value object
            String visibilityValue = visible ? "visible" : "none";
            com.mapbox.bindgen.Value value = com.mapbox.bindgen.Value.valueOf(visibilityValue);
            style.setStyleLayerProperty(layerId, "visibility", value);
            Log.d(TAG, "Layer '" + layerId + "' visibility set to: " + visibilityValue + " (Style: " + (satelliteMapVisible ? "satellite" : "street") + ")");
        } catch (Exception e) {
            // Layer might not exist in the style, log for debugging
            Log.w(TAG, "Layer '" + layerId + "' visibility not set in style '" + (satelliteMapVisible ? "satellite" : "street") + "' (may not exist): " + e.getMessage());
        }
    }

    /**
     * Ensure a raster satellite base layer and source exist in the current style.
     * Satellite layer is positioned at the bottom (right above background) to act as base map.
     * When visible: Shows satellite imagery as base map
     * When hidden: Street view (default style background) shows through
     */
    private void ensureSatelliteLayer(Style style) {
        if (style == null) return;
        try {
            // Add raster source if missing
            try {
                style.getStyleSourceProperty(SATELLITE_SOURCE_ID, "type");
                Log.d(TAG, "✅ Satellite source already exists");
            } catch (Exception missingSource) {
                java.util.HashMap<String, com.mapbox.bindgen.Value> source = new java.util.HashMap<>();
                source.put("type", com.mapbox.bindgen.Value.valueOf("raster"));
                source.put("url", com.mapbox.bindgen.Value.valueOf("mapbox://mapbox.satellite"));
                // Use long for integers in Value
                source.put("tileSize", com.mapbox.bindgen.Value.valueOf(256L));
                style.addStyleSource(SATELLITE_SOURCE_ID, com.mapbox.bindgen.Value.valueOf(source));
                Log.d(TAG, "✅ Satellite raster source added successfully");
            }

            // Add raster layer if missing
            // Position it at the very bottom (right above background) so it acts as base map
            if (!layerExists(style, SATELLITE_LAYER_ID)) {
                java.util.HashMap<String, com.mapbox.bindgen.Value> layer = new java.util.HashMap<>();
                layer.put("id", com.mapbox.bindgen.Value.valueOf(SATELLITE_LAYER_ID));
                layer.put("type", com.mapbox.bindgen.Value.valueOf("raster"));
                layer.put("source", com.mapbox.bindgen.Value.valueOf(SATELLITE_SOURCE_ID));
                
                // Set opacity to 1.0 (fully opaque) to ensure satellite is visible
                layer.put("raster-opacity", com.mapbox.bindgen.Value.valueOf(1.0));
                
                // Try multiple positioning strategies to ensure satellite layer is visible
                // Strategy 1: Position right below lucban-boundary (so it's above most base map layers)
                boolean layerAdded = false;
                try {
                    com.mapbox.maps.LayerPosition position1 = new com.mapbox.maps.LayerPosition(
                            "lucban-boundary", /* above - place satellite below boundary */
                            null, /* below - no specific layer below */
                            null  /* index */
                    );
                    style.addStyleLayer(com.mapbox.bindgen.Value.valueOf(layer), position1);
                    Log.d(TAG, "✅ Satellite raster layer added (positioned below lucban-boundary)");
                    layerAdded = true;
                } catch (Exception e1) {
                    Log.w(TAG, "Strategy 1 failed, trying strategy 2: " + e1.getMessage());
                    
                    // Strategy 2: Position right above background
                    try {
                        com.mapbox.maps.LayerPosition position2 = new com.mapbox.maps.LayerPosition(
                                null, /* above */
                                "background", /* below - place satellite right above background */
                                null  /* index */
                        );
                        style.addStyleLayer(com.mapbox.bindgen.Value.valueOf(layer), position2);
                        Log.d(TAG, "✅ Satellite raster layer added (positioned above background)");
                        layerAdded = true;
                    } catch (Exception e2) {
                        Log.w(TAG, "Strategy 2 failed, trying strategy 3: " + e2.getMessage());
                        
                        // Strategy 3: Default position (will be added at end of layer stack)
                        try {
                            com.mapbox.maps.LayerPosition position3 = new com.mapbox.maps.LayerPosition(
                                    null, /* above */
                                    null, /* below */
                                    null  /* index */
                            );
                            style.addStyleLayer(com.mapbox.bindgen.Value.valueOf(layer), position3);
                            Log.d(TAG, "✅ Satellite raster layer added (default position)");
                            layerAdded = true;
                        } catch (Exception e3) {
                            Log.e(TAG, "❌ All positioning strategies failed: " + e3.getMessage(), e3);
                            throw e3;
                        }
                    }
                }
                
                if (layerAdded) {
                    // After adding, try to move it to a better position if needed
                    // This ensures satellite is visible above base map layers
                    try {
                        // Verify the layer was added and log its position
                        if (layerExists(style, SATELLITE_LAYER_ID)) {
                            Log.d(TAG, "✅ Satellite layer confirmed to exist in style");
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Could not verify satellite layer after adding: " + e.getMessage());
                    }
                }
            } else {
                Log.d(TAG, "✅ Satellite layer already exists");
                // Ensure opacity is set to 1.0
                try {
                    style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-opacity", com.mapbox.bindgen.Value.valueOf(1.0));
                } catch (Exception e) {
                    Log.w(TAG, "Could not set satellite layer opacity: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to ensure satellite layer: " + e.getMessage(), e);
        }
    }

    /**
     * Set satellite raster base visibility.
     * When visible: Satellite imagery shows as base map (fully opaque)
     * When hidden: Street view (default style) shows through
     */
    private void setSatelliteVisibility(Style style, boolean visible) {
        if (style == null) {
            Log.w(TAG, "Style is null, cannot set satellite visibility");
            return;
        }
        try {
            // First, ensure the layer exists
            if (!layerExists(style, SATELLITE_LAYER_ID)) {
                Log.d(TAG, "Satellite layer doesn't exist, creating it first");
                ensureSatelliteLayer(style);
            }
            
            // Set visibility
            String visibilityValue = visible ? "visible" : "none";
            com.mapbox.bindgen.Value value = com.mapbox.bindgen.Value.valueOf(visibilityValue);
            style.setStyleLayerProperty(SATELLITE_LAYER_ID, "visibility", value);
            Log.d(TAG, "✅ Satellite layer visibility set to: " + visibilityValue);
            
            // When visible, ensure opacity is 1.0 (fully opaque) to show satellite clearly
            if (visible) {
                try {
                    style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-opacity", com.mapbox.bindgen.Value.valueOf(1.0));
                    Log.d(TAG, "✅ Satellite layer opacity set to 1.0 (fully opaque)");
                    
                    // Also ensure brightness and contrast are at default for clear satellite view
                    try {
                        style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-brightness-min", com.mapbox.bindgen.Value.valueOf(0.0));
                        style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-brightness-max", com.mapbox.bindgen.Value.valueOf(1.0));
                        style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-contrast", com.mapbox.bindgen.Value.valueOf(0.0));
                    } catch (Exception e) {
                        // These properties might not be supported, that's okay
                    }
                } catch (Exception opacityException) {
                    Log.w(TAG, "Could not set satellite opacity: " + opacityException.getMessage());
                }
            }
            
            Log.d(TAG, "🛰️ Map view switched to: " + (visible ? "SATELLITE" : "STREET"));
            
            // Force a style refresh to ensure changes are applied
            try {
                // Trigger a style update by getting the style again
                Style refreshedStyle = mapboxMap != null ? mapboxMap.getStyle() : null;
                if (refreshedStyle != null && refreshedStyle != style) {
                    Log.d(TAG, "Style refreshed to ensure satellite changes are applied");
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not refresh style: " + e.getMessage());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting satellite visibility: " + e.getMessage(), e);
            // Retry if layer doesn't exist yet
            if (e.getMessage() != null && (e.getMessage().contains("not found") || e.getMessage().contains("does not exist"))) {
                Log.d(TAG, "Satellite layer not found, ensuring it exists first");
                ensureSatelliteLayer(style);
                // Try again after ensuring layer exists
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        String visibilityValue = visible ? "visible" : "none";
                        com.mapbox.bindgen.Value value = com.mapbox.bindgen.Value.valueOf(visibilityValue);
                        style.setStyleLayerProperty(SATELLITE_LAYER_ID, "visibility", value);
                        if (visible) {
                            style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-opacity", com.mapbox.bindgen.Value.valueOf(1.0));
                        }
                        Log.d(TAG, "✅ Satellite layer visibility set after retry: " + visibilityValue);
                    } catch (Exception retryException) {
                        Log.e(TAG, "❌ Error setting satellite visibility after retry: " + retryException.getMessage(), retryException);
                    }
                }, 200);
            }
        }
    }
    
    /**
     * Verify satellite layer state and fix if needed
     */
    private void verifySatelliteLayerState(Style style, boolean shouldBeVisible) {
        if (style == null) return;
        try {
            if (layerExists(style, SATELLITE_LAYER_ID)) {
                // Check current visibility
                try {
                    // getStyleLayerProperty returns StylePropertyValue, we need to extract the value
                    StylePropertyValue propertyValue = style.getStyleLayerProperty(SATELLITE_LAYER_ID, "visibility");
                    // Get the value from StylePropertyValue
                    com.mapbox.bindgen.Value currentVisibilityValue = propertyValue.getValue();
                    String visibilityStr = currentVisibilityValue.toString();
                    boolean isCurrentlyVisible = visibilityStr.contains("visible");
                    
                    if (shouldBeVisible && !isCurrentlyVisible) {
                        Log.w(TAG, "⚠️ Satellite layer should be visible but isn't - fixing...");
                        setSatelliteVisibility(style, true);
                    } else if (!shouldBeVisible && isCurrentlyVisible) {
                        Log.w(TAG, "⚠️ Satellite layer should be hidden but isn't - fixing...");
                        setSatelliteVisibility(style, false);
                    } else {
                        Log.d(TAG, "✅ Satellite layer state verified: " + (shouldBeVisible ? "VISIBLE" : "HIDDEN"));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not verify satellite layer visibility: " + e.getMessage());
                    // If verification fails, just ensure the state is set correctly
                    setSatelliteVisibility(style, shouldBeVisible);
                }
            } else {
                Log.w(TAG, "⚠️ Satellite layer doesn't exist - creating it...");
                ensureSatelliteLayer(style);
                setSatelliteVisibility(style, shouldBeVisible);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying satellite layer state: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a layer exists in the current style
     */
    private boolean layerExists(Style style, String layerId) {
        try {
            if (style == null || layerId == null) return false;
            // Try to get the layer - if it doesn't exist, an exception will be thrown
            style.getStyleLayerProperty(layerId, "visibility");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Hide or show all map pins (Firestore pins, user pins, and current location pin)
     */
    private void setAllPinsVisibility(boolean visible) {
        try {
            if (visible && heatmapEnabled && hasActiveHazardFilter()) {
                visible = false;
            }
            int visibility = visible ? View.VISIBLE : View.GONE;
            
            // Hide/show Firestore pins
            for (MapMarker mapMarker : firestorePinMarkers) {
                if (mapMarker.markerView != null) {
                    mapMarker.markerView.setVisibility(visibility);
                }
            }
            
            // Hide/show user-pinned markers
            for (MapMarker mapMarker : pinnedMarkers) {
                if (mapMarker.markerView != null) {
                    mapMarker.markerView.setVisibility(visibility);
                }
            }
            
            // Hide/show current location pin
            if (currentLocationMarker != null) {
                currentLocationMarker.setVisibility(visibility);
            }
            
            Log.d(TAG, "All pins visibility set to: " + (visible ? "VISIBLE" : "HIDDEN"));
        } catch (Exception e) {
            Log.e(TAG, "Error setting pins visibility: " + e.getMessage(), e);
        }
    }
    
    /**
     * Toggle Barangay Boundaries layer visibility
     * Controls: lucban-brgys + lucban-fill + lucban-brgy-names
     */
    private void toggleBarangayBoundaries(boolean visible) {
        try {
            barangayBoundariesVisible = visible;
            applyBarangayBoundariesVisibility();
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling barangay boundaries: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply barangay boundaries visibility to current style
     */
    private void applyBarangayBoundariesVisibility() {
        Style style = mapboxMap != null ? mapboxMap.getStyle() : null;
        if (style != null) {
            // Toggle all three barangay boundary layers
            setLayerVisibility(style, "lucban-brgys", barangayBoundariesVisible);
            setLayerVisibility(style, "lucban-fill", barangayBoundariesVisible);
            setLayerVisibility(style, "lucban-brgy-names", barangayBoundariesVisible);
            Log.d(TAG, "Barangay boundaries layers applied: " + barangayBoundariesVisible + " (Style: " + (satelliteMapVisible ? "satellite" : "street") + ")");
        } else {
            // Style not ready yet, retry after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                applyBarangayBoundariesVisibility();
            }, 300);
        }
    }
    
    /**
     * Toggle Road Network layer visibility
     * Controls: road
     */
    private void toggleRoadNetwork(boolean visible) {
        try {
            roadNetworkVisible = visible;
            applyRoadNetworkVisibility();
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling road network: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply road network visibility to current style
     */
    private void applyRoadNetworkVisibility() {
        Style style = mapboxMap != null ? mapboxMap.getStyle() : null;
        if (style != null) {
            setLayerVisibility(style, "road", roadNetworkVisible);
            Log.d(TAG, "Road network layer applied: " + roadNetworkVisible + " (Style: " + (satelliteMapVisible ? "satellite" : "street") + ")");
        } else {
            // Style not ready yet, retry after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                applyRoadNetworkVisibility();
            }, 300);
        }
    }
    
    /**
     * Toggle Waterways layer visibility
     * Controls: waterway
     */
    private void toggleWaterways(boolean visible) {
        try {
            waterwaysVisible = visible;
            applyWaterwaysVisibility();
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling waterways: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply waterways visibility to current style
     */
    private void applyWaterwaysVisibility() {
        Style style = mapboxMap != null ? mapboxMap.getStyle() : null;
        if (style != null) {
            setLayerVisibility(style, "waterway", waterwaysVisible);
            Log.d(TAG, "Waterways layer applied: " + waterwaysVisible + " (Style: " + (satelliteMapVisible ? "satellite" : "street") + ")");
        } else {
            // Style not ready yet, retry after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                applyWaterwaysVisibility();
            }, 300);
        }
    }
    
    /**
     * Toggle Satellite Map layer visibility
     * When enabled: Shows satellite imagery as base map (hides street view background)
     * When disabled: Returns to street view (shows street view background, hides satellite)
     */
    private void toggleSatelliteMap(boolean visible) {
        try {
            satelliteMapVisible = visible;
            Log.d(TAG, "🛰️ Satellite map toggle: " + (visible ? "ENABLED (switching to satellite view)" : "DISABLED (switching to street view)"));
            
            // Toggle satellite raster base visibility without reloading the whole style
            Style style = mapboxMap != null ? mapboxMap.getStyle() : null;
            if (style != null) {
                // Ensure satellite layer exists first
                ensureSatelliteLayer(style);
                
                // IMPORTANT: When enabling satellite, hide background layers FIRST, then show satellite
                // When disabling satellite, hide satellite FIRST, then show background layers
                // FIXED: The logic was inverted - now correctly shows satellite when visible=true
                if (visible) {
                    // Enable satellite: Hide background layers first, then show satellite
                    toggleBackgroundLayerForSatellite(style, true);
                    setSatelliteVisibility(style, true);
                } else {
                    // Disable satellite: Hide satellite first, then show background layers
                    setSatelliteVisibility(style, false);
                    toggleBackgroundLayerForSatellite(style, false);
                }
                
                // Force map to refresh/redraw immediately
                forceMapRefresh();
                
                // Verify and fix any issues after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Style currentStyle = mapboxMap != null ? mapboxMap.getStyle() : null;
                    if (currentStyle != null) {
                        verifySatelliteLayerState(currentStyle, visible);
                        
                        // Double-check satellite layer properties if enabled
                        if (visible) {
                            verifySatelliteLayerProperties(currentStyle);
                        }
                    }
                }, 300);
                
                Log.d(TAG, "✅ Satellite toggle complete - Map view: " + (visible ? "SATELLITE" : "STREET"));
            } else {
                // Style not ready yet, retry after a short delay
                Log.d(TAG, "Style not ready, will retry satellite toggle");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    toggleSatelliteMap(visible);
                }, 300);
                return;
            }
            
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
            
        } catch (Exception e) {
            Log.e(TAG, "Error toggling satellite map: " + e.getMessage(), e);
            // Retry on error
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Style style = mapboxMap != null ? mapboxMap.getStyle() : null;
                    if (style != null) {
                        ensureSatelliteLayer(style);
                        if (visible) {
                            toggleBackgroundLayerForSatellite(style, true);
                            setSatelliteVisibility(style, true);
                        } else {
                            setSatelliteVisibility(style, false);
                            toggleBackgroundLayerForSatellite(style, false);
                        }
                        forceMapRefresh();
                    }
                } catch (Exception retryException) {
                    Log.e(TAG, "Error retrying satellite toggle: " + retryException.getMessage(), retryException);
                }
            }, 500);
        }
    }
    
    /**
     * Log all layers in the style for debugging
     */
    private void logAllLayers(Style style, String context) {
        if (style == null) return;
        try {
            List<StyleObjectInfo> allLayers = style.getStyleLayers();
            Log.d(TAG, "=== " + context + " - Total layers: " + allLayers.size() + " ===");
            for (StyleObjectInfo layerInfo : allLayers) {
                String layerId = layerInfo.getId();
                try {
                    StylePropertyValue visibilityProp = style.getStyleLayerProperty(layerId, "visibility");
                    String visibility = visibilityProp.getValue().toString();
                    Log.d(TAG, "  Layer: " + layerId + " | Visibility: " + visibility);
                } catch (Exception e) {
                    Log.d(TAG, "  Layer: " + layerId + " | Visibility: (unknown)");
                }
            }
            Log.d(TAG, "=== End of layer list ===");
        } catch (Exception e) {
            Log.e(TAG, "Error logging layers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Force map to refresh/redraw to ensure satellite changes are visible
     */
    private void forceMapRefresh() {
        try {
            if (mapboxMap != null && mapView != null) {
                // Trigger a camera update to force redraw
                CameraState currentCamera = mapboxMap.getCameraState();
                if (currentCamera != null) {
                    // Slightly adjust zoom to force refresh (then restore)
                    double currentZoom = currentCamera.getZoom();
                    Point currentCenter = currentCamera.getCenter();
                    
                    CameraOptions refreshCamera = new CameraOptions.Builder()
                            .center(currentCenter)
                            .zoom(currentZoom + 0.0001) // Tiny zoom change
                            .build();
                    mapboxMap.setCamera(refreshCamera);
                    
                    // Immediately restore original camera
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        CameraOptions restoreCamera = new CameraOptions.Builder()
                                .center(currentCenter)
                                .zoom(currentZoom)
                                .build();
                        mapboxMap.setCamera(restoreCamera);
                    }, 50);
                    
                    Log.d(TAG, "🔄 Map refresh triggered");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not force map refresh: " + e.getMessage());
        }
    }
    
    /**
     * Verify satellite layer properties are correct
     */
    private void verifySatelliteLayerProperties(Style style) {
        if (style == null) return;
        try {
            if (!layerExists(style, SATELLITE_LAYER_ID)) {
                Log.w(TAG, "⚠️ Satellite layer does not exist!");
                return;
            }
            
            // Check visibility
            try {
                StylePropertyValue visibilityProp = style.getStyleLayerProperty(SATELLITE_LAYER_ID, "visibility");
                String visibility = visibilityProp.getValue().toString();
                Log.d(TAG, "🔍 Satellite layer visibility: " + visibility);
                if (!visibility.contains("visible")) {
                    Log.w(TAG, "⚠️ Satellite layer is not visible! Setting to visible...");
                    setSatelliteVisibility(style, true);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not check satellite visibility: " + e.getMessage());
            }
            
            // Check opacity
            try {
                StylePropertyValue opacityProp = style.getStyleLayerProperty(SATELLITE_LAYER_ID, "raster-opacity");
                com.mapbox.bindgen.Value opacityValue = opacityProp.getValue();
                String opacityStr = opacityValue.toString();
                Log.d(TAG, "🔍 Satellite layer opacity: " + opacityStr);
                
                // Try to parse the opacity value
                try {
                    // Remove any quotes or brackets from the string representation
                    String cleanOpacity = opacityStr.replaceAll("[^0-9.]", "");
                    if (!cleanOpacity.isEmpty()) {
                        double opacity = Double.parseDouble(cleanOpacity);
                        if (opacity < 1.0) {
                            Log.w(TAG, "⚠️ Satellite layer opacity is " + opacity + ", setting to 1.0...");
                            style.setStyleLayerProperty(SATELLITE_LAYER_ID, "raster-opacity", com.mapbox.bindgen.Value.valueOf(1.0));
                        }
                    }
                } catch (NumberFormatException e) {
                    // If we can't parse it, just log the string value
                    Log.d(TAG, "Could not parse opacity as number, value is: " + opacityStr);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not check satellite opacity: " + e.getMessage());
            }
            
            // Check source
            try {
                StylePropertyValue sourceProp = style.getStyleLayerProperty(SATELLITE_LAYER_ID, "source");
                String source = sourceProp.getValue().toString();
                Log.d(TAG, "🔍 Satellite layer source: " + source);
            } catch (Exception e) {
                Log.w(TAG, "Could not check satellite source: " + e.getMessage());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying satellite layer properties: " + e.getMessage(), e);
        }
    }
    
    /**
     * Toggle all base map layers visibility based on satellite state
     * When satellite is enabled: Hide all base map layers (except custom layers) to show satellite imagery
     * When satellite is disabled: Show all base map layers to show street view
     */
    private void toggleBackgroundLayerForSatellite(Style style, boolean satelliteEnabled) {
        if (style == null) return;
        try {
            String visibilityValue = satelliteEnabled ? "none" : "visible";
            com.mapbox.bindgen.Value value = com.mapbox.bindgen.Value.valueOf(visibilityValue);
            
            // List of our custom layers that should ALWAYS remain visible (regardless of satellite state)
            // Note: road and waterway will be handled by their individual toggle states
            Set<String> customLayers = new HashSet<>(Arrays.asList(
                "lucban-boundary",      // Lucban boundary (always visible)
                "lucban-brgys",         // Barangay boundaries (if enabled)
                "lucban-fill",          // Barangay fill (if enabled)
                "lucban-brgy-names",    // Barangay names (if enabled)
                "health-facilities",    // Health facilities (if enabled)
                "evacuation-centers",   // Evacuation centers (if enabled)
                SATELLITE_LAYER_ID      // Satellite layer itself
            ));
            
            // Also preserve road and waterway if they are currently enabled
            // (They will be toggled by their own methods, but we don't want to hide them if user enabled them)
            if (roadNetworkVisible) {
                customLayers.add("road");
            }
            if (waterwaysVisible) {
                customLayers.add("waterway");
            }
            
            // Explicitly handle background layer - must be hidden when satellite is enabled
            try {
                if (layerExists(style, "background")) {
                    style.setStyleLayerProperty("background", "visibility", value);
                    Log.d(TAG, "Background layer toggled to: " + visibilityValue);
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not toggle background layer: " + e.getMessage());
            }
            
            // Get all layers in the style
            List<StyleObjectInfo> allLayers = style.getStyleLayers();
            int hiddenCount = 0;
            int shownCount = 0;
            List<String> layersToggled = new ArrayList<>();
            List<String> layersSkipped = new ArrayList<>();
            
            Log.d(TAG, "Starting to toggle base map layers. Custom layers to preserve: " + customLayers.toString());
            
            for (StyleObjectInfo layerInfo : allLayers) {
                String layerId = layerInfo.getId();
                
                // Skip our custom layers - they should remain visible
                if (customLayers.contains(layerId)) {
                    layersSkipped.add(layerId + " (custom)");
                    continue;
                }
                
                // Skip the satellite layer itself (handled separately)
                if (layerId.equals(SATELLITE_LAYER_ID)) {
                    layersSkipped.add(layerId + " (satellite)");
                    continue;
                }
                
                // Skip background (already handled above)
                if (layerId.equals("background")) {
                    layersSkipped.add(layerId + " (background)");
                    continue;
                }
                
                // Toggle visibility for all other layers (base map layers)
                try {
                    if (layerExists(style, layerId)) {
                        style.setStyleLayerProperty(layerId, "visibility", value);
                        if (satelliteEnabled) {
                            hiddenCount++;
                        } else {
                            shownCount++;
                        }
                        layersToggled.add(layerId);
                        Log.d(TAG, "✅ Toggled base map layer '" + layerId + "' to: " + visibilityValue);
                    }
                } catch (Exception e) {
                    // Layer might not support visibility property or can't be modified
                    Log.w(TAG, "⚠️ Could not toggle layer '" + layerId + "': " + e.getMessage());
                }
            }
            
            Log.d(TAG, "📊 Base map layers summary:");
            Log.d(TAG, "  - Toggled: " + hiddenCount + " hidden, " + shownCount + " shown (satellite: " + satelliteEnabled + ")");
            Log.d(TAG, "  - Layers toggled: " + layersToggled.toString());
            Log.d(TAG, "  - Layers skipped: " + layersSkipped.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error toggling base map layers for satellite: " + e.getMessage(), e);
        }
    }
    
    /**
     * Toggle Health Facilities layer visibility
     * Controls: health-facilities
     */
    private void toggleHealthFacilities(boolean visible) {
        try {
            healthFacilitiesVisible = visible;
            Style style = mapboxMap.getStyle();
            if (style != null) {
                setLayerVisibility(style, "health-facilities", visible);
                Log.d(TAG, "Health facilities layer toggled: " + visible);
            } else {
                // Style not ready yet, will be applied when style loads
                Log.d(TAG, "Style not ready, layer state saved for later: " + visible);
            }
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling health facilities: " + e.getMessage(), e);
        }
    }
    
    /**
     * Toggle Evacuation Centers layer visibility
     * Controls: evacuation-centers
     */
    private void toggleEvacuationCenters(boolean visible) {
        try {
            evacuationCentersVisible = visible;
            Style style = mapboxMap.getStyle();
            if (style != null) {
                setLayerVisibility(style, "evacuation-centers", visible);
                Log.d(TAG, "Evacuation centers layer toggled: " + visible);
            } else {
                // Style not ready yet, will be applied when style loads
                Log.d(TAG, "Style not ready, layer state saved for later: " + visible);
            }
            // Update pins visibility based on all layers
            updatePinsVisibilityBasedOnLayers();
        } catch (Exception e) {
            Log.e(TAG, "Error toggling evacuation centers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ensure Lucban Boundary layer is always visible on initial load
     * This layer shows the main Lucban boundary outline
     * Same implementation as MapPickerActivity
     */
    private void ensureLucbanBoundaryVisible(Style style) {
        try {
            if (style == null) return;
            
            // Set visibility property using Value object
            String visibilityValue = "visible";
            com.mapbox.bindgen.Value value = com.mapbox.bindgen.Value.valueOf(visibilityValue);
            style.setStyleLayerProperty("lucban-boundary", "visibility", value);
            Log.d(TAG, "Lucban boundary layer visibility set to: visible");
        } catch (Exception e) {
            // Layer might not exist in the style yet, will retry
            Log.w(TAG, "Lucban boundary layer not accessible yet (will retry): " + e.getMessage());
        }
    }
    
    /**
     * Ensure Lucban Boundary layer is always visible (overload without Style parameter)
     * This layer should never be hidden - it's the main boundary outline
     */
    private void ensureLucbanBoundaryVisible() {
        try {
            // Force lucban-boundary to always be visible
            lucbanBoundaryVisible = true;
            Style style = mapboxMap.getStyle();
            if (style != null) {
                setLayerVisibility(style, "lucban-boundary", true);
                Log.d(TAG, "Lucban boundary layer ensured to be visible");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring lucban boundary visibility: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hide all layers except the Lucban boundary and background to ensure only the boundary is visible.
     * Same implementation as MapPickerActivity - shows only background and lucban-boundary on initial load
     */
    private void showOnlyLucbanBoundary(Style style) {
        if (style == null) return;

        final Set<String> allowedLayers = new HashSet<>(Arrays.asList(
                "background",
                "lucban-boundary"
        ));

        try {
            List<StyleObjectInfo> layerInfos = style.getStyleLayers();
            for (StyleObjectInfo layerInfo : layerInfos) {
                String layerId = layerInfo.getId();
                com.mapbox.bindgen.Value visibilityValue = allowedLayers.contains(layerId)
                        ? com.mapbox.bindgen.Value.valueOf("visible")
                        : com.mapbox.bindgen.Value.valueOf("none");
                style.setStyleLayerProperty(layerId, "visibility", visibilityValue);
            }
            Log.d(TAG, "Map layers updated to show only Lucban boundary (same as MapPickerActivity).");
        } catch (Exception e) {
            Log.w(TAG, "Unable to update map layers for boundary-only view: " + e.getMessage());
        }
    }
    
    /**
     * Toggle filter panel visibility with animation
     */
    private void toggleFilterPanel() {
        if (isFilterPanelVisible) {
            hideFilterPanel();
        } else {
            showFilterPanel();
        }
    }
    
    /**
     * Show filter panel with slide-in animation from LEFT
     */
    private void showFilterPanel() {
        if (filterPanelOverlay == null || filterPanel == null) return;
        
        try {
            // Load current filter states into UI
            loadFilterStatesIntoUI();
            
            // Hide search bar and filter button
            LinearLayout searchBarLayout = findViewById(R.id.searchBarLayout);
            if (searchBarLayout != null) {
                searchBarLayout.setVisibility(View.GONE);
            }
            
            // Show overlay
            filterPanelOverlay.setVisibility(View.VISIBLE);
            
            // Animate panel slide-in from LEFT (from -320dp to 0dp)
            filterPanel.animate()
                .translationX(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
            
            // Prevent the filter panel from closing when clicked (stop event bubbling)
            filterPanel.setOnClickListener(v -> {
                // Do nothing - prevent the click from bubbling up to the overlay
            });
            
            isFilterPanelVisible = true;
            Log.d(TAG, "Filter panel shown from left with search bar hidden");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter panel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hide filter panel with slide-out animation to LEFT
     */
    private void hideFilterPanel() {
        if (filterPanelOverlay == null || filterPanel == null) return;
        
        try {
            // Animate panel slide-out to LEFT (from 0dp to -320dp)
            filterPanel.animate()
                .translationX(-320f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(() -> {
                    filterPanelOverlay.setVisibility(View.GONE);
                    
                    // Show search bar and filter button again
                    LinearLayout searchBarLayout = findViewById(R.id.searchBarLayout);
                    if (searchBarLayout != null) {
                        searchBarLayout.setVisibility(View.VISIBLE);
                    }
                })
                .start();
            
            isFilterPanelVisible = false;
            Log.d(TAG, "Filter panel hidden to left with search bar shown");
            
        } catch (Exception e) {
            Log.e(TAG, "Error hiding filter panel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load current filter states into UI elements
     */
    private void loadFilterStatesIntoUI() {
        try {
            // Load heatmap state
            if (heatmapSwitch != null) {
                heatmapSwitch.setChecked(heatmapEnabled);
            }
            
            // Load facility filter states and enforce single selection
            applyFacilitySelectionFromState();

            // Load timeline selection
            updateTimelineSelection();

            // Load incident filter states
            for (IncidentCheckboxEntry entry : incidentCheckboxes) {
                if (entry.checkBox != null) {
                    incidentFilters.put(entry.incidentType, entry.checkBox.isChecked());
                }
            }
            enforceSingleIncidentSelection();

            // Facility filter states are already loaded above with single-selection enforcement
            
            // Collapse all sections by default - user must expand them manually
            LinearLayout incidentTypesContent = findViewById(R.id.incidentTypesContent);
            ImageView incidentTypesArrow = findViewById(R.id.incidentTypesArrow);
            if (incidentTypesContent != null && incidentTypesArrow != null) {
                incidentTypesContent.setVisibility(View.GONE);
                isIncidentTypesExpanded = false;
                incidentTypesArrow.setRotation(0f);
            }
            
            // Collapse Timeline section
            LinearLayout timelineContent = findViewById(R.id.timelineContent);
            ImageView timelineArrow = findViewById(R.id.timelineArrow);
            if (timelineContent != null && timelineArrow != null) {
                timelineContent.setVisibility(View.GONE);
                isTimelineExpanded = false;
                timelineArrow.setRotation(0f);
            }
            
            // Collapse Emergency Support section
            LinearLayout emergencySupportContent = findViewById(R.id.emergencySupportContent);
            ImageView emergencySupportArrow = findViewById(R.id.emergencySupportArrow);
            if (emergencySupportContent != null && emergencySupportArrow != null) {
                emergencySupportContent.setVisibility(View.GONE);
                isEmergencySupportExpanded = false;
                emergencySupportArrow.setRotation(0f);
            }
            
            // Setup filter panel click listeners
            setupFilterPanelClickListeners();
            
            updateHeatmapToggleState();
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading filter states into UI: " + e.getMessage(), e);
        }
    }
    
    /**
     * Setup click listeners for filter panel elements
     */
    private void setupFilterPanelClickListeners() {
        try {
            // Close button
            ImageView closeButton = findViewById(R.id.closeFilterPanel);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> hideFilterPanel());
            }
            
            // Timeline section
            LinearLayout timelineHeader = findViewById(R.id.timelineHeader);
            if (timelineHeader != null) {
                timelineHeader.setOnClickListener(v -> toggleSection("timeline"));
            }
            
            // Incident Types section
            LinearLayout incidentTypesHeader = findViewById(R.id.incidentTypesHeader);
            if (incidentTypesHeader != null) {
                incidentTypesHeader.setOnClickListener(v -> toggleSection("incidents"));
            }
            
            // Emergency Support section
            LinearLayout emergencySupportHeader = findViewById(R.id.emergencySupportHeader);
            if (emergencySupportHeader != null) {
                emergencySupportHeader.setOnClickListener(v -> toggleSection("facilities"));
            }
            
            // Timeline options
            if (allTimeOption != null) {
                allTimeOption.setOnClickListener(v -> selectTimelineOption("All Time", allTimeOption));
            }
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
            
            // Apply and Clear buttons
            Button applyFiltersButton = findViewById(R.id.applyFiltersButton);
            if (applyFiltersButton != null) {
                applyFiltersButton.setOnClickListener(v -> applyFiltersFromPanel());
            }
            
            Button clearFiltersButton = findViewById(R.id.clearFiltersButton);
            if (clearFiltersButton != null) {
                clearFiltersButton.setOnClickListener(v -> clearAllFilters());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up filter panel click listeners: " + e.getMessage(), e);
        }
    }
    
    /**
     * Toggle section visibility
     */
    private void toggleSection(String section) {
        try {
            switch (section) {
                case "timeline":
                    isTimelineExpanded = !isTimelineExpanded;
                    animateSection(findViewById(R.id.timelineContent), findViewById(R.id.timelineArrow), isTimelineExpanded);
                    break;
                case "incidents":
                    isIncidentTypesExpanded = !isIncidentTypesExpanded;
                    animateSection(findViewById(R.id.incidentTypesContent), findViewById(R.id.incidentTypesArrow), isIncidentTypesExpanded);
                    break;
                case "facilities":
                    isEmergencySupportExpanded = !isEmergencySupportExpanded;
                    animateSection(findViewById(R.id.emergencySupportContent), findViewById(R.id.emergencySupportArrow), isEmergencySupportExpanded);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling section: " + e.getMessage(), e);
        }
    }
    
    /**
     * Animate section expansion/collapse
     */
    private void animateSection(LinearLayout content, ImageView arrow, boolean expand) {
        if (content == null || arrow == null) return;

        try {
            // Animate content visibility
            if (expand) {
                content.setVisibility(View.VISIBLE);
            } else {
                content.setVisibility(View.GONE);
            }

            // Animate arrow rotation
            float fromDegrees = expand ? 0f : 180f;
            float toDegrees = expand ? 180f : 0f;

            android.view.animation.RotateAnimation rotateAnimation = new android.view.animation.RotateAnimation(
                    fromDegrees, toDegrees,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            arrow.startAnimation(rotateAnimation);
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating section: " + e.getMessage(), e);
        }
    }
    
    /**
     * Select timeline option
     */
    private void selectTimelineOption(String timeRange, TextView selectedOption) {
        try {
            // Reset all timeline options to default color
            resetTimelineOptions();

            // Set selected option
            selectedTimeRange = timeRange;
            selectedTimelineOption = selectedOption;

            // Highlight selected option
            if (selectedOption != null) {
                selectedOption.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                selectedOption.setBackground(getResources().getDrawable(android.R.drawable.btn_default));
            }

            // Apply timeline filter to pins
            Log.d(TAG, "Timeline filter changed to: " + timeRange);
            applyFiltersToMap();
            
        } catch (Exception e) {
            Log.e(TAG, "Error selecting timeline option: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reset timeline options to default appearance
     */
    private void resetTimelineOptions() {
        try {
            int defaultColor = getResources().getColor(android.R.color.black);

            if (allTimeOption != null) {
                allTimeOption.setTextColor(defaultColor);
                allTimeOption.setBackground(null);
            }
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
        } catch (Exception e) {
            Log.e(TAG, "Error resetting timeline options: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update timeline selection UI
     */
    private void updateTimelineSelection() {
        try {
            // Update UI based on selected time range
            TextView optionToSelect = null;

            switch (selectedTimeRange) {
                case "All Time":
                    optionToSelect = allTimeOption;
                    break;
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
        } catch (Exception e) {
            Log.e(TAG, "Error updating timeline selection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply filters from panel
     */
    private void applyFiltersFromPanel() {
        try {
            // Update filter states from UI
            updateFilterStatesFromUI();
            
            // Apply filters to map
            applyFiltersToMap();
            
            // Update filter indicator
            updateFilterIndicator();
            
            // Hide panel
            hideFilterPanel();
            
            // Filters applied successfully (no toast message)
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying filters from panel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update filter states from UI elements
     */
    private void updateFilterStatesFromUI() {
        try {
            // Update heatmap state
            if (heatmapSwitch != null) {
                heatmapEnabled = heatmapSwitch.isChecked();
            }

            // Update incident filters
            for (IncidentCheckboxEntry entry : incidentCheckboxes) {
                if (entry.checkBox != null) {
                    incidentFilters.put(entry.incidentType, entry.checkBox.isChecked());
                }
            }

            // Update facility filters from checkboxes (single selection enforced afterwards)
            boolean facilityCaptured = false;
            for (FacilityCheckboxEntry entry : facilityCheckboxes) {
                if (entry.checkBox != null) {
                    boolean checked = entry.checkBox.isChecked();
                    if (checked && !facilityCaptured) {
                        facilityFilters.put(entry.facilityType, true);
                        facilityCaptured = true;
                    } else {
                        facilityFilters.put(entry.facilityType, false);
                    }
                }
            }
            applyFacilitySelectionFromState();
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating filter states from UI: " + e.getMessage(), e);
        }

        // Enforce single selection after updating
        enforceSingleIncidentSelection();
    }
    
    /**
     * Clear all filters
     */
    private void clearAllFilters() {
        try {
            // Reset all incident filters to false (unchecked)
            incidentFilters.put("Road Accident", false);
            incidentFilters.put("Fire", false);
            incidentFilters.put("Medical Emergency", false);
            incidentFilters.put("Flooding", false);
            incidentFilters.put("Volcanic Activity", false);
            incidentFilters.put("Landslide", false);
            incidentFilters.put("Earthquake", false);
            incidentFilters.put("Civil Disturbance", false);
            incidentFilters.put("Armed Conflict", false);
            incidentFilters.put("Infectious Disease", false);
            incidentFilters.put("Poor Infrastructure", false);
            incidentFilters.put("Obstructions", false);
            incidentFilters.put("Electrical Hazard", false);
            incidentFilters.put("Environmental Hazard", false);
            incidentFilters.put("Animal Concerns", false);
            incidentFilters.put("Report", false);
            incidentFilters.put("Others", false);

            // Reset all facility filters to false (unchecked)
            facilityFilters.put("Evacuation Centers", false);
            facilityFilters.put("Health Facilities", false);
            facilityFilters.put("Police Stations", false);
            facilityFilters.put("Fire Stations", false);
            facilityFilters.put("Government Offices", false);
            activeFacilityFilter = null;
            
            // Reset heatmap
            heatmapEnabled = false;
            
            // Reset timeline
            selectedTimeRange = "All Time";
            
            // Re-enable all checkboxes when filters are cleared
            setIncidentCheckboxesEnabled(true);
            setFacilityCheckboxesEnabled(true);
            
            // Re-enable heatmap switch
            if (heatmapSwitch != null) {
                heatmapSwitch.setEnabled(false); // Will be enabled when a hazard filter is active
                heatmapSwitch.setAlpha(0.5f);
            }
            
            // Reload UI with cleared states
            loadFilterStatesIntoUI();
            
            // Apply cleared filters
            applyFiltersToMap();
            updateFilterIndicator();
            
            // All filters cleared (no toast message)
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing filters: " + e.getMessage(), e);
        }
    }
    
    // ========================================
    // FIRESTORE PINS IMPLEMENTATION
    // ========================================
    
    /**
     * Fetch all pins from Firestore and display them on the map
     */
    private void loadPinsFromFirestore() {
        if (db == null) {
            Log.e(TAG, "Firestore not initialized");
            return;
        }
        
        try {
            Log.d(TAG, "Loading pins from Firestore...");
            
            // Query all pins ordered by creation date (newest first)
            db.collection("pins")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        // Clear existing Firestore pin markers
                        clearFirestorePins();
                        
                        int pinCount = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Parse pin data from Firestore
                                Pin pin = parsePinFromDocument(document);
                                
                                if (pin != null && pin.getLatitude() != 0 && pin.getLongitude() != 0) {
                                    // Create point from pin coordinates
                                    Point pinPoint = Point.fromLngLat(pin.getLongitude(), pin.getLatitude());
                                    
                                    // Add pin to map
                                    addFirestorePinToMap(pin, pinPoint);
                                    pinCount++;
                                    
                                    Log.d(TAG, "Added pin: " + pin.getDisplayTitle() + " (" + pin.getCategory() + ") at " + 
                                        pin.getLatitude() + ", " + pin.getLongitude());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing pin document: " + document.getId(), e);
                            }
                        }
                        
                        Log.d(TAG, "Successfully loaded " + pinCount + " pins from Firestore");
                        
                        // Make pinCount final for use in lambda
                        final int finalPinCount = pinCount;
                        
                        // Apply current filters to the newly loaded pins (without toast during initial load)
                        runOnUiThread(() -> {
                            applyFiltersToFirestorePins(false); // Don't show toast during initial load
                            updateHeatmapToggleState();
                            if (heatmapEnabled && hasActiveHazardFilter()) {
                                showHeatmapView();
                            } else {
                                hideHeatmapView();
                            }
                            Log.d(TAG, "Map pins loaded and filtered: " + finalPinCount + " total pins");
                        });
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing Firestore pins", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pins from Firestore", e);
                    // Failed to load map pins (toast removed)
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPinsFromFirestore: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load user's reports from Firestore "reports" collection and display them as pins
     */
    private void loadUserReportsAsPins() {
        try {
            FirebaseUser currentUser = mAuth != null ? mAuth.getCurrentUser() : null;
            if (currentUser == null) {
                Log.d(TAG, "No user logged in, skipping user reports load");
                return;
            }

            if (db == null) {
                Log.e(TAG, "Firestore not initialized");
                return;
            }

            Log.d(TAG, "Loading user reports from Firestore for user: " + currentUser.getUid());

            // Clear existing user report pins
            clearUserReportPins();

            // Query user's reports from "reports" collection
            String userId = currentUser.getUid();
            Log.d(TAG, "═══════════════════════════════════════════════════════");
            Log.d(TAG, "🔍 QUERYING USER REPORTS FROM FIRESTORE");
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "Collection: reports");
            Log.d(TAG, "Filter: userId == " + userId);
            Log.d(TAG, "═══════════════════════════════════════════════════════");
            
            db.collection("reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        Log.d(TAG, "✅ FIRESTORE QUERY SUCCESSFUL");
                        Log.d(TAG, "Total documents found: " + queryDocumentSnapshots.size());
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        
                        // Use final array to allow modification inside lambda
                        final int[] reportCount = {0};
                        final int[] skippedCount = {0};
                        
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.w(TAG, "═══════════════════════════════════════════════════════");
                            Log.w(TAG, "⚠️ NO REPORTS FOUND IN FIRESTORE");
                            Log.w(TAG, "User ID: " + userId);
                            Log.w(TAG, "Collection: reports");
                            Log.w(TAG, "This could mean:");
                            Log.w(TAG, "  1. User hasn't submitted any reports yet");
                            Log.w(TAG, "  2. Reports are stored with different userId field");
                            Log.w(TAG, "  3. Reports are in a different collection");
                            Log.w(TAG, "═══════════════════════════════════════════════════════");
                            // No toast for "My Reports" - just apply filters silently
                            runOnUiThread(() -> {
                                // Apply filters without showing toast
                                applyFiltersToFirestorePins(false);
                            });
                            return;
                        }
                        
                        // Log all document IDs and key fields for debugging
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        Log.d(TAG, "📋 DOCUMENTS FOUND - ANALYZING EACH REPORT:");
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d(TAG, "Document ID: " + doc.getId());
                            Log.d(TAG, "  - userId: " + doc.getString("userId"));
                            Log.d(TAG, "  - reportType: " + doc.getString("reportType"));
                            Log.d(TAG, "  - latitude: " + doc.getDouble("latitude"));
                            Log.d(TAG, "  - longitude: " + doc.getDouble("longitude"));
                            Log.d(TAG, "  - locationName: " + doc.getString("locationName"));
                            Log.d(TAG, "  - coordinates: " + doc.getString("coordinates"));
                            Log.d(TAG, "  - location: " + doc.getString("location"));
                            Log.d(TAG, "  - Full data: " + doc.getData().toString());
                            Log.d(TAG, "  ───────────────────────────────────────────────");
                        }
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        Log.d(TAG, "🔄 STARTING TO PROCESS " + queryDocumentSnapshots.size() + " REPORTS");
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Get report data
                                String reportId = document.getId();
                                Log.d(TAG, "═══════════════════════════════════════════════════════");
                                Log.d(TAG, "📄 Processing report: " + reportId);
                                Log.d(TAG, "Full document data: " + document.getData().toString());
                                
                                String reportType = document.getString("reportType");
                                if (reportType == null || reportType.isEmpty()) {
                                    // Fallback to category if reportType is not available
                                    reportType = document.getString("category");
                                }
                                
                                // If still empty, try "type" field
                                if ((reportType == null || reportType.isEmpty())) {
                                    reportType = document.getString("type");
                                }
                                
                                // Try multiple ways to get coordinates
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");
                                
                                // Fallback: Try alternative field names
                                if (latitude == null) {
                                    latitude = document.getDouble("lat");
                                }
                                if (longitude == null) {
                                    longitude = document.getDouble("lng");
                                }
                                if (longitude == null) {
                                    longitude = document.getDouble("lon");
                                }
                                
                                // Fallback: Try parsing from coordinates string field
                                if ((latitude == null || longitude == null) && document.contains("coordinates")) {
                                    String coordinatesStr = document.getString("coordinates");
                                    if (coordinatesStr != null && !coordinatesStr.isEmpty()) {
                                        try {
                                            // Parse format: "14.123456, 121.567890" or "14.123456,121.567890"
                                            String[] coords = coordinatesStr.split(",");
                                            if (coords.length == 2) {
                                                if (latitude == null) {
                                                    latitude = Double.parseDouble(coords[0].trim());
                                                }
                                                if (longitude == null) {
                                                    longitude = Double.parseDouble(coords[1].trim());
                                                }
                                                Log.d(TAG, "Parsed coordinates from string: " + latitude + ", " + longitude);
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "Could not parse coordinates string: " + coordinatesStr, e);
                                        }
                                    }
                                }
                                
                                // Fallback: Try parsing from location field if it contains coordinates
                                if ((latitude == null || longitude == null) && document.contains("location")) {
                                    String locationStr = document.getString("location");
                                    if (locationStr != null && locationStr.contains("(") && locationStr.contains(")")) {
                                        try {
                                            // Parse format: "Location Name (14.123456, 121.567890)"
                                            int start = locationStr.indexOf("(");
                                            int end = locationStr.indexOf(")");
                                            if (start > 0 && end > start) {
                                                String coordsStr = locationStr.substring(start + 1, end);
                                                String[] coords = coordsStr.split(",");
                                                if (coords.length == 2) {
                                                    if (latitude == null) {
                                                        latitude = Double.parseDouble(coords[0].trim());
                                                    }
                                                    if (longitude == null) {
                                                        longitude = Double.parseDouble(coords[1].trim());
                                                    }
                                                    Log.d(TAG, "Parsed coordinates from location field: " + latitude + ", " + longitude);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "Could not parse coordinates from location field: " + locationStr, e);
                                        }
                                    }
                                }
                                
                                String locationName = document.getString("locationName");
                                if (locationName == null || locationName.isEmpty()) {
                                    locationName = document.getString("location");
                                    // Remove coordinates from location name if present
                                    if (locationName != null && locationName.contains("(")) {
                                        locationName = locationName.substring(0, locationName.indexOf("(")).trim();
                                    }
                                }

                                Log.d(TAG, "📊 Report Summary:");
                                Log.d(TAG, "  - Report ID: " + reportId);
                                Log.d(TAG, "  - Type: " + reportType);
                                Log.d(TAG, "  - Latitude: " + latitude);
                                Log.d(TAG, "  - Longitude: " + longitude);
                                Log.d(TAG, "  - Location Name: " + locationName);
                                Log.d(TAG, "  - Coordinates Valid: " + (latitude != null && longitude != null && 
                                    latitude != 0.0 && longitude != 0.0 &&
                                    latitude >= -90 && latitude <= 90 &&
                                    longitude >= -180 && longitude <= 180));

                                // Only create pin if coordinates are available and valid
                                boolean hasValidCoordinates = latitude != null && longitude != null && 
                                    latitude != 0.0 && longitude != 0.0 &&
                                    latitude >= -90 && latitude <= 90 &&
                                    longitude >= -180 && longitude <= 180;
                                
                                if (hasValidCoordinates) {
                                    Log.d(TAG, "✅ VALID COORDINATES - Creating pin...");
                                    
                                    // Create Pin object from report
                                    Pin pin = new Pin();
                                    pin.setId("report_" + reportId); // Prefix to distinguish from regular pins
                                    pin.setType(reportType != null ? reportType : "Others"); // Use reportType for icon selection
                                    pin.setCategory(reportType != null ? reportType : "Others"); // Also set category as fallback
                                    pin.setLocationName(locationName != null ? locationName : "Location not specified");
                                    pin.setLatitude(latitude);
                                    pin.setLongitude(longitude);
                                    pin.setCreatedBy(currentUser.getUid());
                                    pin.setReportId(reportId);
                                    
                                    // Get timestamp - reports store timestamp as long (milliseconds), not Firestore Timestamp
                                    // Try to get as long first to avoid RuntimeException
                                    java.util.Date pinDate = null;
                                    
                                    try {
                                        // First try to get as long (milliseconds) - this is how reports store it
                                        Long timestampLong = document.getLong("timestamp");
                                        if (timestampLong != null && timestampLong > 0) {
                                            pinDate = new java.util.Date(timestampLong);
                                            Log.d(TAG, "  - Timestamp (long): " + timestampLong + " = " + pinDate);
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "  - Could not get timestamp as long: " + e.getMessage());
                                    }
                                    
                                    // If long didn't work, try Firestore Timestamp
                                    if (pinDate == null) {
                                        try {
                                            com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
                                            if (timestamp != null) {
                                                pinDate = timestamp.toDate();
                                                Log.d(TAG, "  - Timestamp (Firestore): " + pinDate);
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "  - Could not get timestamp as Firestore Timestamp: " + e.getMessage());
                                        }
                                    }
                                    
                                    // Fallback to createdAt field
                                    if (pinDate == null) {
                                        try {
                                            Long createdAtLong = document.getLong("createdAt");
                                            if (createdAtLong != null && createdAtLong > 0) {
                                                pinDate = new java.util.Date(createdAtLong);
                                                Log.d(TAG, "  - Using createdAt (long): " + pinDate);
                                            }
                                        } catch (Exception e) {
                                            // Try as Firestore Timestamp
                                            try {
                                                com.google.firebase.Timestamp createdAt = document.getTimestamp("createdAt");
                                                if (createdAt != null) {
                                                    pinDate = createdAt.toDate();
                                                    Log.d(TAG, "  - Using createdAt (Firestore): " + pinDate);
                                                }
                                            } catch (Exception e2) {
                                                Log.w(TAG, "  - Could not get createdAt: " + e2.getMessage());
                                            }
                                        }
                                    }
                                    
                                    // Final fallback - use current date
                                    if (pinDate == null) {
                                        pinDate = new java.util.Date();
                                        Log.w(TAG, "  - Using current date as fallback: " + pinDate);
                                    }
                                    
                                    pin.setCreatedAt(pinDate);
                                    
                                    // Create point from coordinates
                                    Point pinPoint = Point.fromLngLat(longitude, latitude);
                                    
                                    // Add pin to map with custom icon based on report type
                                    // The icon will be selected automatically by getDrawableForPin() based on reportType
                                    Log.d(TAG, "Creating pin for report:");
                                    Log.d(TAG, "  - Report ID: " + reportId);
                                    Log.d(TAG, "  - Type: " + reportType);
                                    Log.d(TAG, "  - Location: " + locationName);
                                    Log.d(TAG, "  - Coordinates: " + latitude + ", " + longitude);
                                    Log.d(TAG, "  - Icon Resource: " + getDrawableForPin(pin));
                                    
                                    // Verify pin was created correctly before adding
                                    Log.d(TAG, "About to add pin to map:");
                                    Log.d(TAG, "  - Pin ID: " + pin.getId());
                                    Log.d(TAG, "  - Pin Type: " + pin.getType());
                                    Log.d(TAG, "  - Pin Coordinates: " + pin.getLatitude() + ", " + pin.getLongitude());
                                    Log.d(TAG, "  - Point Coordinates: " + pinPoint.latitude() + ", " + pinPoint.longitude());
                                    
                                    addUserReportPinToMap(pin, pinPoint);
                                    reportCount[0]++;
                                    
                                    // Verify pin was added to the list
                                    Log.d(TAG, "✅ Successfully added user report pin: Type=" + reportType + ", Icon=" + getDrawableForPin(pin) + " at " + latitude + ", " + longitude);
                                    Log.d(TAG, "  - Total pins in userReportPinMarkers list: " + userReportPinMarkers.size());
                                    Log.d(TAG, "═══════════════════════════════════════");
                                } else {
                                    skippedCount[0]++;
                                    Log.w(TAG, "❌ INVALID COORDINATES - Skipping report:");
                                    Log.w(TAG, "  - Report ID: " + reportId);
                                    Log.w(TAG, "  - Latitude: " + latitude + " (valid: -90 to 90, not null, not 0.0)");
                                    Log.w(TAG, "  - Longitude: " + longitude + " (valid: -180 to 180, not null, not 0.0)");
                                    Log.w(TAG, "  - Location Name: " + locationName);
                                    Log.w(TAG, "  - Reason: " + 
                                        (latitude == null ? "Latitude is NULL" : 
                                         longitude == null ? "Longitude is NULL" :
                                         latitude == 0.0 ? "Latitude is 0.0" :
                                         longitude == 0.0 ? "Longitude is 0.0" :
                                         latitude < -90 || latitude > 90 ? "Latitude out of range" :
                                         longitude < -180 || longitude > 180 ? "Longitude out of range" : "Unknown"));
                                    Log.d(TAG, "═══════════════════════════════════════════════════════");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error processing report document: " + document.getId(), e);
                                e.printStackTrace();
                            }
                        }
                        
                        final int finalReportCount = reportCount[0];
                        final int finalSkippedCount = skippedCount[0];
                        final int totalReportsFound = queryDocumentSnapshots.size();
                        
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        Log.d(TAG, "📊 LOADING SUMMARY:");
                        Log.d(TAG, "  - Total reports found in Firestore: " + totalReportsFound);
                        Log.d(TAG, "  - Pins successfully created: " + finalReportCount);
                        Log.d(TAG, "  - Reports skipped (invalid coordinates): " + finalSkippedCount);
                        Log.d(TAG, "  - User report pins in list: " + userReportPinMarkers.size());
                        Log.d(TAG, "═══════════════════════════════════════════════════════");
                        
                        // Apply filters to update visibility and show pins if "My Reports" is active
                        runOnUiThread(() -> {
                            // Check if "My Reports" filter is active
                            boolean isMyReportsActive = incidentFilters.getOrDefault("Report", false);
                            Log.d(TAG, "My Reports filter active: " + isMyReportsActive);
                            
                            if (isMyReportsActive) {
                                // Apply filters to show user report pins with their custom icons
                                // This will show appropriate toast messages based on results
                                applyFiltersToFirestorePins(true); // Show toast to inform user
                                Log.d(TAG, "✅ My Reports filter active - showing " + finalReportCount + " user report pins with custom icons");
                                
                                // Additional helpful message if reports were found but skipped
                                if (finalSkippedCount > 0 && finalReportCount == 0) {
                                    Toast.makeText(MapViewActivity.this, 
                                        "Found " + totalReportsFound + " report(s) but none have valid coordinates. Please ensure reports include location data.", 
                                        Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // Just apply filters without toast (pins will be hidden)
                                applyFiltersToFirestorePins(false);
                                Log.d(TAG, "My Reports filter not active - pins will be hidden");
                            }
                        });
                        
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error processing user reports", e);
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(MapViewActivity.this, 
                                "Error loading reports: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading user reports from Firestore", e);
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(MapViewActivity.this, 
                            "Error loading reports: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserReportsAsPins: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add user report pin to map
     */
    private void addUserReportPinToMap(Pin pin, Point point) {
        if (mapContainer == null) {
            Log.e(TAG, "❌ Cannot add pin - mapContainer is null");
            return;
        }
        
        if (pin == null) {
            Log.e(TAG, "❌ Cannot add pin - pin data is null");
            return;
        }
        
        if (point == null) {
            Log.e(TAG, "❌ Cannot add pin - point is null for pin: " + pin.getType());
            return;
        }
        
        try {
            Log.d(TAG, "Adding pin to map container:");
            Log.d(TAG, "  - Pin Type: " + pin.getType());
            Log.d(TAG, "  - Coordinates: " + pin.getLatitude() + ", " + pin.getLongitude());
            Log.d(TAG, "  - Point: " + point.longitude() + ", " + point.latitude());
            Log.d(TAG, "  - Map Container: " + (mapContainer != null ? "Ready" : "NULL"));
            
            ensurePinDimensions();
            
            ImageView markerView = new ImageView(this);
            int drawableResource = getDrawableForPin(pin); // Use reportType for icon
            markerView.setImageResource(drawableResource);
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pinWidthPx, pinHeightPx);
            markerView.setLayoutParams(params);
            
            // Add click listener to show pin details modal (same as regular pins)
            markerView.setOnClickListener(v -> showPinDetails(pin));
            Log.d(TAG, "  - Click listener added to pin");
            
            // Add to container
            mapContainer.addView(markerView);
            Log.d(TAG, "  - Pin view added to container");
            
            MapMarker mapMarker = new MapMarker(markerView, point, pin.getDisplayTitle(), pin, "report_" + pin.getReportId());
            userReportPinMarkers.add(mapMarker);
            Log.d(TAG, "  - Pin marker added to list (total: " + userReportPinMarkers.size() + ")");
            
            // Position the pin using the correct method
            positionFirestorePinAtCoordinates(mapMarker, point);
            Log.d(TAG, "  - Pin positioned at coordinates");
            
            // Check if "My Reports" filter is active - if so, show the pin immediately
            boolean isMyReportsActive = incidentFilters.getOrDefault("Report", false);
            Log.d(TAG, "  - My Reports filter active: " + isMyReportsActive);
            
            if (isMyReportsActive) {
                // When "My Reports" is active, ALWAYS show user report pins
                // shouldShowPinBasedOnFilters should return true for user report pins when "My Reports" is active
                boolean shouldShow = shouldShowPinBasedOnFilters(pin);
                if (!shouldShow) {
                    // Force show if it's a user report pin (shouldn't happen, but just in case)
                    if (pin.getId() != null && pin.getId().startsWith("report_")) {
                        shouldShow = true;
                        Log.w(TAG, "  ⚠️ Force showing user report pin (filter returned false but should be true)");
                    }
                }
                markerView.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                if (shouldShow) {
                    markerView.bringToFront();
                    Log.d(TAG, "  ✅ Pin immediately shown: " + pin.getType() + " at " + pin.getLatitude() + ", " + pin.getLongitude());
                } else {
                    Log.w(TAG, "  ⚠️ Pin hidden by filter: " + pin.getType() + " (ID: " + pin.getId() + ")");
                }
            } else {
                // Initially hide the pin - it will be shown when "My Reports" filter is active
                markerView.setVisibility(View.GONE);
                Log.d(TAG, "  ⚠️ Pin hidden (My Reports not active): " + pin.getType());
            }
            
            // Start camera tracking if not already active (tracking is global for all pins)
            if (!isFirestorePinTrackingActive) {
                startFirestorePinCameraTracking();
                Log.d(TAG, "  ✅ Started camera tracking for user report pins");
            }
            
            Log.d(TAG, "✅ Successfully added user report pin to map");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding user report pin to map", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all user report pins from map
     */
    private void clearUserReportPins() {
        try {
            for (MapMarker marker : userReportPinMarkers) {
                if (marker.markerView != null && marker.markerView.getParent() != null) {
                    ((android.view.ViewGroup) marker.markerView.getParent()).removeView(marker.markerView);
                }
            }
            userReportPinMarkers.clear();
            Log.d(TAG, "Cleared all user report pins");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user report pins", e);
        }
    }
    
    /**
     * Parse Pin object from Firestore document - Updated for your structure
     */
    private Pin parsePinFromDocument(QueryDocumentSnapshot document) {
        try {
            Pin pin = new Pin();
            pin.setId(document.getId());
            
            // Get fields from your actual Firestore structure
            pin.setCategory(document.getString("category"));
            pin.setType(document.getString("type")); // Read type field from Firestore
            pin.setCreatedBy(document.getString("createdBy"));
            pin.setCreatedByName(document.getString("createdByName"));
            pin.setLocationName(document.getString("locationName"));
            pin.setReportId(document.getString("reportId"));
            
            // Get coordinates
            Double lat = document.getDouble("latitude");
            Double lng = document.getDouble("longitude");
            
            if (lat != null && lng != null) {
                pin.setLatitude(lat);
                pin.setLongitude(lng);
            } else {
                Log.w(TAG, "Pin " + document.getId() + " has invalid coordinates");
                return null;
            }
            
            // Get timestamp
            com.google.firebase.Timestamp createdTimestamp = document.getTimestamp("createdAt");
            if (createdTimestamp != null) {
                pin.setCreatedAt(createdTimestamp.toDate());
            }
            
            // Get search terms array
            @SuppressWarnings("unchecked")
            List<String> searchTerms = (List<String>) document.get("searchTerms");
            pin.setSearchTerms(searchTerms);
            
            return pin;
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing pin from document: " + document.getId(), e);
            return null;
        }
    }
    
    /**
     * Add a Firestore pin to the map with custom SVG icons based on category
     * GEOGRAPHIC LOCKING: Pins stay at exact LAT/LON coordinates, never drift!
     */
    private void addFirestorePinToMap(Pin pin, Point point) {
        try {
            if (mapContainer == null) {
                Log.e(TAG, "mapContainer is null - cannot add pin");
                return;
            }
            
            if (point.latitude() == 0.0 && point.longitude() == 0.0) {
                Log.e(TAG, "Invalid coordinates (0,0) for pin: " + pin.getDisplayTitle());
                return;
            }
            
            Log.d(TAG, "================================================");
            Log.d(TAG, "Adding pin: " + pin.getDisplayTitle());
            Log.d(TAG, "Category: " + pin.getCategory());
            Log.d(TAG, "FIXED Geographic Coordinates: " + point.latitude() + ", " + point.longitude());
            Log.d(TAG, "These coordinates will NEVER change!");
            Log.d(TAG, "================================================");
            
            ensurePinDimensions();
            ImageView markerView = new ImageView(this);
            int drawableResource = getDrawableForPin(pin); // Use new method that checks both type and category
            markerView.setImageResource(drawableResource);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pinWidthPx, pinHeightPx);
            markerView.setLayoutParams(layoutParams);
            markerView.setOnClickListener(v -> showPinDetails(pin));
            
            mapContainer.addView(markerView);
            
            MapMarker mapMarker = new MapMarker(markerView, point, pin.getDisplayTitle(), pin, pin.getId());
            firestorePinMarkers.add(mapMarker);
            
            positionFirestorePinAtCoordinates(mapMarker, point);
            
            boolean anyLayerActive = hasAnyLayerActive();
            if (heatmapEnabled && hasActiveHazardFilter()) {
                markerView.setVisibility(View.GONE);
            } else if (anyLayerActive) {
                markerView.setVisibility(View.GONE);
            } else {
                boolean shouldShow = shouldShowPinBasedOnFilters(pin);
                markerView.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
            
            if (!isFirestorePinTrackingActive) {
                startFirestorePinCameraTracking();
                Log.d(TAG, "Started camera tracking for geographic locking");
            }
            
            Log.d(TAG, "✅ Pin added successfully with FIXED coordinates: " + point.latitude() + ", " + point.longitude());
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding Firestore pin to map", e);
        }
    }
    
    /**
     * Position Firestore pin at specific geographic coordinates
     * GEOGRAPHIC LOCKING: Pins stay at exact LAT/LON, screen position updates to match
     */
    private void positionFirestorePinAtCoordinates(MapMarker mapMarker, Point point) {
        if (mapMarker == null || mapMarker.markerView == null || mapboxMap == null || mapContainer == null) {
            return;
        }
        
        if (heatmapEnabled && hasActiveHazardFilter()) {
            mapMarker.markerView.setVisibility(View.GONE);
            return;
        }
        
        try {
            ensurePinDimensions();
            // CRITICAL: Verify the Point coordinates haven't changed
            if (point.latitude() == 0.0 && point.longitude() == 0.0) {
                Log.e(TAG, "ERROR: Pin has invalid coordinates (0,0)!");
                mapMarker.markerView.setVisibility(View.GONE);
                return;
            }
            
            // Convert FIXED geographic coordinates to CURRENT screen coordinates
            ScreenCoordinate screenCoord = mapboxMap.pixelForCoordinate(point);
            
            // Get map container dimensions
            int containerWidth = mapContainer.getWidth();
            int containerHeight = mapContainer.getHeight();
            
            if (containerWidth <= 0 || containerHeight <= 0) {
                // Container not ready yet, try again later
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    positionFirestorePinAtCoordinates(mapMarker, point);
                }, 100);
                return;
            }
            
            // Calculate marker position relative to container with high precision
            double x = screenCoord.getX();
            double y = screenCoord.getY();
            
            // Create layout parameters with absolute positioning
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                pinWidthPx,
                pinHeightPx
            );
            
            // Center the pin point exactly on the geographic coordinates
            params.leftMargin = (int) Math.round(x - (pinWidthPx / 2.0));
            params.topMargin = (int) Math.round(y - pinHeightPx + pinOffsetPx);
            
            mapMarker.markerView.setLayoutParams(params);
            
            // Always check filter visibility - layers do NOT hide pins
            // Layers (Barangay Boundaries, Road Network, Waterways, Satellite Map) should NOT hide pins
            if (mapMarker.pinData != null) {
                // Check heatmap first - if heatmap is active, hide pins
                if (heatmapEnabled && hasActiveHazardFilter()) {
                    mapMarker.markerView.setVisibility(View.GONE);
                } else {
                    // Show pin based on filters - layers do NOT affect pin visibility
                    boolean shouldShow = shouldShowPinBasedOnFilters(mapMarker.pinData);
                    mapMarker.markerView.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                }
            } else {
                mapMarker.markerView.setVisibility(View.VISIBLE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error positioning Firestore pin at coordinates", e);
            Log.e(TAG, "Pin: " + (mapMarker.pinData != null ? mapMarker.pinData.getDisplayTitle() : "unknown"));
            Log.e(TAG, "Coordinates: " + point.latitude() + ", " + point.longitude());
        }
    }
    
    /**
     * Get custom drawable resource for pin based on type (priority) or category (fallback)
     * Uses the "type" field from Firestore if available, otherwise falls back to "category"
     */
    private int getDrawableForPin(Pin pin) {
        if (pin == null) {
            return R.drawable.ic_location;
        }
        
        // PRIORITY: Check type field from Firestore
        String pinType = pin.getType();
        if (pinType != null && !pinType.trim().isEmpty()) {
            int typeDrawable = getDrawableForPinType(pinType);
            if (typeDrawable != R.drawable.ic_location) {
                return typeDrawable; // Found match in type field
            }
        }
        
        // FALLBACK: Use category field
        String category = pin.getCategory();
        if (category != null && !category.trim().isEmpty()) {
            return getDrawableForPinCategory(category);
        }
        
        return R.drawable.ic_location; // Default pin
    }
    
    /**
     * Get drawable resource for pin type from Firestore
     */
    private int getDrawableForPinType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return R.drawable.ic_location;
        }
        
        String typeLower = type.toLowerCase().trim();
        
        // Map type values to drawable resources
        // "Road Crash" from Firestore uses road_crash drawable
        if (typeLower.contains("road crash") || typeLower.contains("road accident") || 
            typeLower.contains("accident") || typeLower.contains("traffic")) {
            return R.drawable.road_crash;
        }
        if (typeLower.contains("fire") && !typeLower.contains("station")) {
            return R.drawable.fire;
        }
        if (typeLower.contains("medical emergency") || (typeLower.contains("medical") && typeLower.contains("emergency"))) {
            return R.drawable.medical_emergency__1_;
        }
        if (typeLower.contains("flooding") || typeLower.contains("flood")) {
            return R.drawable.flooding;
        }
        if (typeLower.contains("volcanic activity") || typeLower.contains("volcanic") || typeLower.contains("volcano")) {
            return R.drawable.volcano;
        }
        if (typeLower.contains("landslide")) {
            return R.drawable.landslide;
        }
        if (typeLower.contains("earthquake")) {
            return R.drawable.earthquake;
        }
        if (typeLower.contains("civil disturbance") || typeLower.contains("civil")) {
            return R.drawable.civil_disturbance__1_;
        }
        if (typeLower.contains("armed conflict") || typeLower.contains("armed")) {
            return R.drawable.armed_conflict__1_;
        }
        if (typeLower.contains("infectious disease") || typeLower.contains("infectious")) {
            return R.drawable.infectious_disease;
        }
        if (typeLower.contains("poor infrastructure") || typeLower.contains("infrastructure")) {
            return R.drawable.poor_infrastructure;
        }
        if (typeLower.contains("obstruction") || typeLower.contains("obstacle")) {
            return R.drawable.obstruction;
        }
        if (typeLower.contains("electrical hazard") || (typeLower.contains("electrical") && typeLower.contains("hazard"))) {
            return R.drawable.electrical_hazard;
        }
        if (typeLower.contains("environmental hazard") || (typeLower.contains("environmental") && typeLower.contains("hazard"))) {
            return R.drawable.environmental_hazard;
        }
        if (typeLower.contains("animal concerns") || typeLower.contains("animal concern") || 
            (typeLower.contains("animal") && typeLower.contains("concern"))) {
            return R.drawable.animal_concern;
        }
        if (typeLower.contains("others") || typeLower.contains("other")) {
            return R.drawable.accizard_pin; // Use same pin as current location marker
        }
        
        // Facility types
        if (typeLower.contains("government office") || typeLower.contains("government offices") || 
            typeLower.contains("gobyerno") || (typeLower.contains("government") && typeLower.contains("office"))) {
            return R.drawable.government_office;
        }
        if (typeLower.contains("evacuation center") || typeLower.contains("evacuation")) {
            return R.drawable.evacuation_center;
        }
        if (typeLower.contains("health facility") || typeLower.contains("health") || 
            typeLower.contains("hospital") || typeLower.contains("clinic")) {
            return R.drawable.health_facility__1_;
        }
        if (typeLower.contains("police station") || typeLower.contains("police") || 
            typeLower.contains("pulisya")) {
            return R.drawable.police_station__1_;
        }
        if (typeLower.contains("fire station") || typeLower.contains("bumbero") || 
            (typeLower.contains("fire") && typeLower.contains("station"))) {
            return R.drawable.fire_station;
        }
        
        return R.drawable.ic_location; // Default if no match
    }
    
    /**
     * Get custom drawable resource for pin category based on your SVG files
     */
    private int getDrawableForPinCategory(String category) {
        if (category == null) {
            return R.drawable.ic_location; // Default pin (using existing icon)
        }
        
        // Map categories to your custom SVG drawables (matching your file names exactly)
        switch (category.toLowerCase()) {
            case "accident":
            case "road accident":
            case "traffic":
                return R.drawable.road_crash;
            case "fire":
            case "sunog":
                return R.drawable.fire;
            case "flooding":
            case "baha":
            case "water":
                return R.drawable.flooding;
            case "medical emergency":
            case "medical":
            case "hospital":
                return R.drawable.medical_emergency__1_;
            case "earthquake":
            case "lindol":
                return R.drawable.earthquake;
            case "landslide":
            case "guho":
                return R.drawable.landslide;
            case "volcanic activity":
            case "bulkan":
                return R.drawable.volcano;
            case "civil disturbance":
            case "kaguluhan":
                return R.drawable.civil_disturbance__1_;
            case "armed conflict":
            case "barilan":
                return R.drawable.armed_conflict__1_;
            case "infectious disease":
            case "sakit":
                return R.drawable.infectious_disease;
            case "poor infrastructure":
            case "infrastructure":
                return R.drawable.poor_infrastructure;
            case "obstruction":
            case "obstructions":
            case "obstacle":
                return R.drawable.obstruction;
            case "electrical hazard":
            case "electrical":
                return R.drawable.electrical_hazard;
            case "environmental hazard":
            case "environmental":
                return R.drawable.environmental_hazard;
            case "animal concerns":
            case "animal concern":
                return R.drawable.animal_concern;
            case "police station":
            case "pulisya":
                return R.drawable.police_station__1_;
            case "fire station":
            case "bumbero":
                return R.drawable.fire_station;
            case "evacuation center":
            case "evacuation":
                return R.drawable.evacuation_center;
            case "government office":
            case "government offices":
            case "gobyerno":
                return R.drawable.government_office;
            case "health facility":
            case "health":
                return R.drawable.health_facility__1_;
            case "emergency":
            case "emergency response":
                return R.drawable.medical_emergency__1_;
            case "report":
            case "reports":
                return R.drawable.accizard_pin; // Report pins use accizard_pin drawable
            case "others":
            case "other":
                return R.drawable.accizard_pin; // Others use same pin as current location marker
            default:
                return R.drawable.ic_location; // Default pin (using existing icon)
        }
    }
    
    /**
     * Check if a pin matches the selected timeline filter
     * @param pin The pin to check
     * @return true if pin matches timeline filter, false otherwise
     */
    private boolean matchesTimelineFilter(Pin pin) {
        if (pin == null || selectedTimeRange == null) {
            return true; // If no timeline selected or pin is null, show all pins
        }
        
        // If timeline is "All Time" (default), show all pins
        if (selectedTimeRange.equals("All Time")) {
            return true; // Show all pins regardless of date
        } else if (selectedTimeRange.equals("Today")) {
            return isPinCreatedToday(pin);
        } else if (selectedTimeRange.equals("This Week")) {
            return isPinCreatedThisWeek(pin);
        } else if (selectedTimeRange.equals("This Month")) {
            return isPinCreatedThisMonth(pin);
        } else if (selectedTimeRange.equals("This Year")) {
            return isPinCreatedThisYear(pin);
        }
        
        // Default: show all pins if timeline filter is not recognized
        return true;
    }
    
    /**
     * Check if a pin was created today
     */
    private boolean isPinCreatedToday(Pin pin) {
        if (pin == null || pin.getCreatedAt() == null) {
            return false;
        }
        
        Date pinDate = pin.getCreatedAt();
        Date today = new Date();
        
        // Get start of today (00:00:00)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date startOfToday = cal.getTime();
        
        // Get end of today (23:59:59.999)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        Date endOfToday = cal.getTime();
        
        return pinDate.compareTo(startOfToday) >= 0 && pinDate.compareTo(endOfToday) <= 0;
    }
    
    /**
     * Check if a pin was created this week
     */
    private boolean isPinCreatedThisWeek(Pin pin) {
        if (pin == null || pin.getCreatedAt() == null) {
            return false;
        }
        
        Date pinDate = pin.getCreatedAt();
        Date today = new Date();
        
        // Get start of this week (Monday 00:00:00)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date startOfWeek = cal.getTime();
        
        return pinDate.compareTo(startOfWeek) >= 0;
    }
    
    /**
     * Check if a pin was created this month
     */
    private boolean isPinCreatedThisMonth(Pin pin) {
        if (pin == null || pin.getCreatedAt() == null) {
            return false;
        }
        
        Date pinDate = pin.getCreatedAt();
        Date today = new Date();
        
        // Get start of this month (1st day, 00:00:00)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();
        
        return pinDate.compareTo(startOfMonth) >= 0;
    }
    
    /**
     * Check if a pin was created this year
     */
    private boolean isPinCreatedThisYear(Pin pin) {
        if (pin == null || pin.getCreatedAt() == null) {
            return false;
        }
        
        Date pinDate = pin.getCreatedAt();
        Date today = new Date();
        
        // Get start of this year (January 1st, 00:00:00)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date startOfYear = cal.getTime();
        
        return pinDate.compareTo(startOfYear) >= 0;
    }
    
    /**
     * Check if a pin should be visible based on current filter settings
     * 
     * Behavior:
     * - Timeline filter: Shows only pins created within the selected time range (Today, This Week, This Month, This Year)
     * - "My Reports" filter: When active, shows only pins created by the current user
     *   (pins display with their appropriate icons based on their type: Fire, Medical Emergency, etc.)
     * - Pins are visible when their corresponding filter is ENABLED (checked)
     * - If NO filters are checked, ALL pins are hidden
     * - When BOTH an Incident Type AND an Emergency Support Facility are checked, 
     *   pins for BOTH types will be shown simultaneously
     * - Facility pins are checked against facility filters only
     * - Incident pins are checked against incident filters only
     */
    private boolean shouldShowPinBasedOnFilters(Pin pin) {
        if (pin == null) {
            return false; // Hide pins with no data
        }
        
        // Check if "My Reports" filter is active FIRST
        boolean isMyReportsFilterActive = incidentFilters.getOrDefault("Report", false);
        if (isMyReportsFilterActive) {
            // When "My Reports" is active, only show pins from user reports
            // User report pins have IDs starting with "report_"
            if (pin.getId() != null && pin.getId().startsWith("report_")) {
                // This is a user report pin - show ALL user reports when "My Reports" is active
                // DO NOT apply timeline filter to user reports - show all of them
                // This ensures users can see all their submitted reports regardless of when they were created
                Log.d(TAG, "✅ My Reports active - showing user report pin: " + pin.getId() + " (Type: " + pin.getType() + ")");
                return true;
            } else {
                // This is a regular pin - hide it when "My Reports" is active
                Log.d(TAG, "⚠️ My Reports active - hiding regular pin: " + pin.getId());
                return false;
            }
        } else {
            // When "My Reports" is NOT active, hide user report pins
            if (pin.getId() != null && pin.getId().startsWith("report_")) {
                Log.d(TAG, "⚠️ My Reports NOT active - hiding user report pin: " + pin.getId());
                return false;
            }
        }
        
        // For regular pins (not user reports), check timeline filter
        if (!matchesTimelineFilter(pin)) {
            return false;
        }
        
        // Check if ANY filters are active
        boolean hasActiveIncidentFilters = false;
        for (Boolean enabled : incidentFilters.values()) {
            if (enabled) {
                hasActiveIncidentFilters = true;
                break;
            }
        }
        
        boolean hasActiveFacilityFilters = false;
        for (Boolean enabled : facilityFilters.values()) {
            if (enabled) {
                hasActiveFacilityFilters = true;
                break;
            }
        }
        
        // If NO filters are checked, hide ALL pins (show only Lucban boundary)
        if (!hasActiveIncidentFilters && !hasActiveFacilityFilters) {
            return false; // Hide all pins when no filters are active - show only boundary
        }
        
        // PRIORITY: Use "type" field from Firestore for filtering
        // The "type" field from web app should match filter names exactly
        String pinType = pin.getType();
        if (pinType != null && !pinType.trim().isEmpty()) {
            String typeTrimmed = pinType.trim();
            String typeLower = typeTrimmed.toLowerCase();
            
            // First, check if this is clearly a facility type (before checking incidents)
            // This prevents facility types from being misidentified as incidents
            // Must be specific to avoid false matches
            boolean isFacilityType = false;
            if (typeLower.contains("government office") || typeLower.contains("government offices") || 
                typeLower.contains("gobyerno") || (typeLower.contains("government") && typeLower.contains("office")) ||
                typeLower.contains("evacuation center") || typeLower.contains("evacuation centers") ||
                (typeLower.contains("health facility") || typeLower.contains("health facilities")) ||
                typeLower.contains("police station") || typeLower.contains("police stations") ||
                typeLower.contains("fire station") || typeLower.contains("fire stations") ||
                typeLower.contains("bumbero") || typeLower.contains("pulisya") ||
                typeLower.contains("hospital") || typeLower.contains("clinic")) {
                // Make sure it's not an incident type that mentions these words
                if (!typeLower.contains("medical emergency") && !typeLower.contains("health emergency") &&
                    !typeLower.contains("evacuation order") && !typeLower.contains("evacuation warning")) {
                    isFacilityType = true;
                }
            }
            
            // Check if this type matches any Incident Type filter
            // Only check if it's NOT a facility type
            if (hasActiveIncidentFilters && !isFacilityType) {
                // Map type to Incident Type filter names
                // "Road Crash" from Firestore matches "Road Accident" filter
                if (typeLower.contains("road crash") || typeLower.contains("road accident") || 
                    typeLower.contains("accident") || typeLower.contains("traffic")) {
                    return incidentFilters.getOrDefault("Road Accident", false);
                }
                if (typeLower.contains("fire") && !typeLower.contains("station")) {
                    return incidentFilters.getOrDefault("Fire", false);
                }
                if (typeLower.contains("medical emergency") || (typeLower.contains("medical") && typeLower.contains("emergency"))) {
                    return incidentFilters.getOrDefault("Medical Emergency", false);
                }
                if (typeLower.contains("flooding") || typeLower.contains("flood")) {
                    return incidentFilters.getOrDefault("Flooding", false);
                }
                if (typeLower.contains("volcanic activity") || typeLower.contains("volcanic") || typeLower.contains("volcano")) {
                    return incidentFilters.getOrDefault("Volcanic Activity", false);
                }
                if (typeLower.contains("landslide")) {
                    return incidentFilters.getOrDefault("Landslide", false);
                }
                if (typeLower.contains("earthquake")) {
                    return incidentFilters.getOrDefault("Earthquake", false);
                }
                if (typeLower.contains("civil disturbance") || typeLower.contains("civil")) {
                    return incidentFilters.getOrDefault("Civil Disturbance", false);
                }
                if (typeLower.contains("armed conflict") || typeLower.contains("armed")) {
                    return incidentFilters.getOrDefault("Armed Conflict", false);
                }
                if (typeLower.contains("infectious disease") || typeLower.contains("infectious")) {
                    return incidentFilters.getOrDefault("Infectious Disease", false);
                }
                if (typeLower.contains("poor infrastructure") || typeLower.contains("infrastructure")) {
                    return incidentFilters.getOrDefault("Poor Infrastructure", false);
                }
                if (typeLower.contains("obstruction") || typeLower.contains("obstacle")) {
                    return incidentFilters.getOrDefault("Obstructions", false);
                }
                if (typeLower.contains("electrical hazard") || (typeLower.contains("electrical") && typeLower.contains("hazard"))) {
                    return incidentFilters.getOrDefault("Electrical Hazard", false);
                }
                if (typeLower.contains("environmental hazard") || (typeLower.contains("environmental") && typeLower.contains("hazard"))) {
                    return incidentFilters.getOrDefault("Environmental Hazard", false);
                }
                if (typeLower.contains("animal concerns") || typeLower.contains("animal concern") || 
                    (typeLower.contains("animal") && typeLower.contains("concern"))) {
                    return incidentFilters.getOrDefault("Animal Concerns", false);
                }
                
                // Try exact match with filter names (case-insensitive)
                for (String filterName : incidentFilters.keySet()) {
                    if (typeTrimmed.equalsIgnoreCase(filterName)) {
                        return incidentFilters.getOrDefault(filterName, false);
                    }
                }
                
                // If type doesn't match any incident filter and incident filters are active, hide it
                return false;
            }
            
            // Check if type matches any Facility Type filter
            // Only check if it's identified as a facility type
            if (hasActiveFacilityFilters && isFacilityType) {
                // Check Government Offices
                if (typeLower.contains("government office") || typeLower.contains("government offices") || 
                    typeLower.contains("gobyerno") || (typeLower.contains("government") && typeLower.contains("office"))) {
                    return facilityFilters.getOrDefault("Government Offices", false);
                }
                
                // Check Evacuation Centers (must be specific)
                if (typeLower.contains("evacuation center") || typeLower.contains("evacuation centers")) {
                    return facilityFilters.getOrDefault("Evacuation Centers", false);
                }
                
                // Check Health Facilities (must be specific - "health facility" not just "health")
                if (typeLower.contains("health facility") || typeLower.contains("health facilities") ||
                    typeLower.contains("hospital") || typeLower.contains("clinic")) {
                    return facilityFilters.getOrDefault("Health Facilities", false);
                }
                
                // Check Police Stations (must be specific)
                if (typeLower.contains("police station") || typeLower.contains("police stations") || 
                    typeLower.contains("pulisya")) {
                    return facilityFilters.getOrDefault("Police Stations", false);
                }
                
                // Check Fire Stations (must be specific)
                if (typeLower.contains("fire station") || typeLower.contains("fire stations") || 
                    typeLower.contains("bumbero") || (typeLower.contains("fire") && typeLower.contains("station"))) {
                    return facilityFilters.getOrDefault("Fire Stations", false);
                }
                
                // Try exact match with facility filter names (case-insensitive)
                for (String filterName : facilityFilters.keySet()) {
                    if (typeTrimmed.equalsIgnoreCase(filterName)) {
                        return facilityFilters.getOrDefault(filterName, false);
                    }
                }
            }
            
            // If type is a facility type but didn't match any facility filter, hide it
            if (isFacilityType && hasActiveFacilityFilters) {
                // Facility type but no matching filter - hide it
                return false;
            }
            
            // If type is not a facility type and didn't match any incident filter, hide it
            if (!isFacilityType && hasActiveIncidentFilters) {
                // Not a facility type and no matching incident filter - hide it
                return false;
            }
        }
        
        // FALLBACK: Use category field if type is not available
        String category = pin.getCategory();
        if (category == null || category.trim().isEmpty()) {
            return false; // Hide pins with no category or type
        }
        
        String categoryLower = category.toLowerCase().trim();
        
        // First, determine if this is a facility pin or an incident pin
        boolean isFacility = isFacilityCategory(category);
        
        if (isFacility) {
            // This is a facility pin - check facility filters
            // Only filter if facility filters are active
            if (!hasActiveFacilityFilters) {
                return false; // Hide all facility pins when no facility filters are active
            }
            
            // Check Evacuation Centers (must be exact match or specific phrase)
            if (categoryLower.equals("evacuation center") || categoryLower.equals("evacuation centers") ||
                categoryLower.equals("evacuation")) {
                boolean filterEnabled = facilityFilters.getOrDefault("Evacuation Centers", false);
                Log.d(TAG, "Evacuation Centers pin - Category: " + category + ", Filter enabled: " + filterEnabled);
                return filterEnabled;
            }
            
            // Check Health Facilities (must be exact match or specific phrase)
            if (categoryLower.equals("health facility") || categoryLower.equals("health facilities") ||
                categoryLower.equals("health") || categoryLower.contains("hospital") || categoryLower.contains("clinic")) {
                // Make sure it's not an incident type
                if (!categoryLower.contains("emergency") && !categoryLower.contains("medical emergency") &&
                    !categoryLower.contains("disease") && !categoryLower.contains("infectious")) {
                    boolean filterEnabled = facilityFilters.getOrDefault("Health Facilities", false);
                    Log.d(TAG, "Health Facilities pin - Category: " + category + ", Filter enabled: " + filterEnabled);
                    return filterEnabled;
                }
            }
            
            // Check Police Stations
            if ((categoryLower.contains("police") && categoryLower.contains("station")) || categoryLower.contains("pulisya")) {
                return facilityFilters.getOrDefault("Police Stations", false);
            }
            
            // Check Fire Stations
            if ((categoryLower.contains("fire") && categoryLower.contains("station")) || categoryLower.contains("bumbero")) {
                return facilityFilters.getOrDefault("Fire Stations", false);
            }
            
            // Check Government Offices
            if ((categoryLower.contains("government") && categoryLower.contains("office")) || categoryLower.contains("gobyerno")) {
                return facilityFilters.getOrDefault("Government Offices", false);
            }
            
            // Facility pin but doesn't match any known facility type - hide it
            Log.d(TAG, "Facility pin with unknown category: " + category + " - hiding");
            return false;
        } else {
            // This is an incident pin - check incident filters
            // Only filter if incident filters are active
            if (!hasActiveIncidentFilters) {
                return false; // Hide all incident pins when no incident filters are active
            }
            
            if (categoryLower.contains("accident") || categoryLower.contains("traffic")) {
                return incidentFilters.getOrDefault("Road Accident", false);
            }
            if (categoryLower.contains("fire") || categoryLower.contains("sunog")) {
                // Make sure it's not a fire station (facility)
                if (!categoryLower.contains("station") && !categoryLower.contains("bumbero")) {
                    return incidentFilters.getOrDefault("Fire", false);
                }
            }
            if ((categoryLower.contains("medical") || categoryLower.contains("emergency")) && 
                !categoryLower.contains("fire") && !categoryLower.contains("health facility")) {
                return incidentFilters.getOrDefault("Medical Emergency", false);
            }
            if (categoryLower.contains("flooding") || categoryLower.contains("baha")) {
                return incidentFilters.getOrDefault("Flooding", false);
            }
            if (categoryLower.contains("volcanic") || categoryLower.contains("bulkan")) {
                return incidentFilters.getOrDefault("Volcanic Activity", false);
            }
            if (categoryLower.contains("landslide") || categoryLower.contains("guho")) {
                return incidentFilters.getOrDefault("Landslide", false);
            }
            if (categoryLower.contains("earthquake") || categoryLower.contains("lindol")) {
                return incidentFilters.getOrDefault("Earthquake", false);
            }
            if (categoryLower.contains("civil") || categoryLower.contains("kaguluhan")) {
                return incidentFilters.getOrDefault("Civil Disturbance", false);
            }
            if (categoryLower.contains("armed") || categoryLower.contains("barilan")) {
                return incidentFilters.getOrDefault("Armed Conflict", false);
            }
            if (categoryLower.contains("infectious") || categoryLower.contains("sakit")) {
                return incidentFilters.getOrDefault("Infectious Disease", false);
            }
            if (categoryLower.contains("poor infrastructure") || categoryLower.contains("infrastructure")) {
                return incidentFilters.getOrDefault("Poor Infrastructure", false);
            }
            if (categoryLower.contains("obstruction") || categoryLower.contains("obstacle")) {
                return incidentFilters.getOrDefault("Obstructions", false);
            }
            if (categoryLower.contains("electrical") && categoryLower.contains("hazard")) {
                return incidentFilters.getOrDefault("Electrical Hazard", false);
            }
            if (categoryLower.contains("environmental") && categoryLower.contains("hazard")) {
                return incidentFilters.getOrDefault("Environmental Hazard", false);
            }
            if (categoryLower.contains("animal concerns") || categoryLower.contains("animal concern") || 
                (categoryLower.contains("animal") && categoryLower.contains("concern"))) {
                return incidentFilters.getOrDefault("Animal Concerns", false);
            }
            if (categoryLower.contains("report")) {
                return incidentFilters.getOrDefault("Report", false);
            }
            if (categoryLower.contains("other")) {
                return incidentFilters.getOrDefault("Others", false);
            }
            
            // Incident pin but doesn't match any known incident type - hide it
            return false;
        }
    }
    
    /**
     * Show pin details in a custom dialog - Beautiful modal design
     */
    private void showPinDetails(Pin pin) {
        try {
            // Create custom dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_pin_details);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            
            // Set dialog size and position
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
                window.setGravity(android.view.Gravity.CENTER);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            // Get views from custom layout
            ImageView pinIcon = dialog.findViewById(R.id.pinIcon);
            TextView pinTitle = dialog.findViewById(R.id.pinTitle);
            TextView pinDescription = dialog.findViewById(R.id.pinDescription);
            
            // Set pin icon based on type or category
            if (pinIcon != null) {
                int iconResource = getDrawableForPin(pin); // Use new method that checks both type and category
                pinIcon.setImageResource(iconResource);
                // Remove color filter since we're using custom SVG icons
                pinIcon.setColorFilter(null);
            }
            
            // Set title
            if (pinTitle != null) {
                pinTitle.setText(pin.getDisplayTitle());
            }
            
            // Build description message following required format
            StringBuilder description = new StringBuilder();

            boolean isFacilityPin = isFacilityCategory(pin.getCategory());
            String fullAddress = pin.getFullAddress();
            if (fullAddress == null || fullAddress.isEmpty()) {
                fullAddress = "Not available";
            }

            if (isFacilityPin) {
                description.append("Emergency Facility:\n");
                String title = pin.getDisplayTitle();
                if (title == null || title.isEmpty()) {
                    title = "Not available";
                }
                description.append("- Title: ").append(title).append("\n");
                description.append("- Location: ").append(fullAddress);
            } else {
                description.append("Report Details:\n");
                // PRIORITY: Use "type" field from Firestore, fallback to "category"
                String type = null;
                if (pin.getType() != null && !pin.getType().trim().isEmpty()) {
                    type = pin.getType().trim();
                } else if (pin.getCategory() != null && !pin.getCategory().trim().isEmpty()) {
                    type = formatCategoryLabel(pin.getCategory());
                } else {
                    type = "Not available";
                }
                description.append("- Type: ").append(type).append("\n");
                description.append("- Location: ").append(fullAddress).append("\n");

                String dateText = "Not available";
                String timeText = "Not available";
                if (pin.getCreatedAt() != null) {
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                    dateText = dateFormat.format(pin.getCreatedAt());
                    timeText = timeFormat.format(pin.getCreatedAt());
                }
                description.append("- Date: ").append(dateText).append("\n");
                description.append("- Time: ").append(timeText);
            }

            if (pinDescription != null) {
                pinDescription.setText(description.toString());
            }
            
            // Navigate button removed
            
            // Show dialog with animation
            dialog.show();
            
            // Add fade-in animation
            if (window != null) {
                window.getDecorView().setAlpha(0f);
                window.getDecorView().animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing pin details", e);
            // Error displaying pin details (toast removed)
        }
    }
    
    /**
     * Start camera tracking for Firestore pins
     * GEOGRAPHIC LOCKING: Updates screen position every 50ms to keep pins at exact coordinates
     */
    private void startFirestorePinCameraTracking() {
        if (firestorePinCameraRunnable == null) {
            firestorePinCameraRunnable = new Runnable() {
                private int updateCount = 0;
                
                @Override
                public void run() {
                    // Update all Firestore pin positions if they exist
                    if (!firestorePinMarkers.isEmpty()) {
                        for (MapMarker marker : firestorePinMarkers) {
                            if (marker.markerView != null && marker.location != null) {
                                // CRITICAL: marker.location Point object is IMMUTABLE and NEVER changes!
                                // We're converting the SAME fixed coordinates to current screen position
                                positionFirestorePinAtCoordinates(marker, marker.location);
                                
                                // Log every 100 updates (every 5 seconds) to verify coordinates stay fixed
                                if (updateCount % 100 == 0 && marker.pinData != null) {
                                    Log.d(TAG, "📍 Pin tracking: " + marker.pinData.getDisplayTitle() + 
                                        " ALWAYS at coordinates: " + marker.location.latitude() + ", " + marker.location.longitude());
                                }
                            }
                        }
                    }
                    
                    // Update all user report pin positions if they exist
                    if (!userReportPinMarkers.isEmpty()) {
                        for (MapMarker marker : userReportPinMarkers) {
                            if (marker.markerView != null && marker.location != null) {
                                // CRITICAL: marker.location Point object is IMMUTABLE and NEVER changes!
                                // We're converting the SAME fixed coordinates to current screen position
                                positionFirestorePinAtCoordinates(marker, marker.location);
                            }
                        }
                    }
                    
                    updateCount++;
                    
                    // Schedule next update
                    if (firestorePinCameraHandler != null) {
                        firestorePinCameraHandler.postDelayed(this, 50); // Update every 50ms = 20fps
                    }
                }
            };
        }
        
        // Start the tracking
        if (firestorePinCameraHandler != null && !isFirestorePinTrackingActive) {
            firestorePinCameraHandler.post(firestorePinCameraRunnable);
            isFirestorePinTrackingActive = true;
            Log.d(TAG, "🚀 Started geographic locking camera tracking (20 updates/second)");
            Log.d(TAG, "📍 Pins will ALWAYS stay at their exact LAT/LON coordinates!");
        }
    }
    
    /**
     * Stop Firestore pin camera tracking
     */
    private void stopFirestorePinCameraTracking() {
        if (firestorePinCameraHandler != null && firestorePinCameraRunnable != null) {
            firestorePinCameraHandler.removeCallbacks(firestorePinCameraRunnable);
            isFirestorePinTrackingActive = false;
            Log.d(TAG, "Stopped Firestore pin camera tracking");
        }
    }
    
    /**
     * Clear all Firestore pins from the map
     */
    private void clearFirestorePins() {
        try {
            if (mapContainer != null && !firestorePinMarkers.isEmpty()) {
                for (MapMarker marker : firestorePinMarkers) {
                    if (marker.markerView != null) {
                        mapContainer.removeView(marker.markerView);
                    }
                }
                firestorePinMarkers.clear();
                Log.d(TAG, "Cleared all Firestore pins from map");
            }
            
            // Stop camera tracking when no pins
            if (firestorePinMarkers.isEmpty()) {
                stopFirestorePinCameraTracking();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing Firestore pins", e);
        }
    }
    
    /**
     * Refresh pins from Firestore
     */
    private void refreshPinsFromFirestore() {
        clearFirestorePins();
        loadPinsFromFirestore();
    }

    /**
     * Create a new road accident pin at the current camera center and save it to Firestore.
     */
    private void createRoadAccidentPinAtCameraCenter() {
        if (db == null) {
            Log.e(TAG, "Firestore not initialized. Cannot create road accident pin.");
            return;
        }

        Point targetPoint;
        if (mapboxMap != null) {
            CameraState cameraState = mapboxMap.getCameraState();
            if (cameraState != null && cameraState.getCenter() != null) {
                targetPoint = cameraState.getCenter();
            } else {
                targetPoint = Point.fromLngLat(121.5564, 14.1136); // Lucban center fallback
            }
        } else {
            targetPoint = Point.fromLngLat(121.5564, 14.1136);
        }

        double latitude = targetPoint.latitude();
        double longitude = targetPoint.longitude();
        String locationName = String.format(Locale.getDefault(),
                "Road Accident near %.5f, %.5f", latitude, longitude);

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("category", "Road Accident");
        pinData.put("latitude", latitude);
        pinData.put("longitude", longitude);
        pinData.put("locationName", locationName);
        pinData.put("createdAt", new Date());

        FirebaseUser currentUser = mAuth != null ? mAuth.getCurrentUser() : null;
        if (currentUser != null) {
            pinData.put("createdBy", currentUser.getUid());
            if (currentUser.getDisplayName() != null) {
                pinData.put("createdByName", currentUser.getDisplayName());
            }
        }

        List<String> searchTerms = Arrays.asList(
                "road accident",
                "accident",
                "road",
                String.format(Locale.getDefault(), "%.5f", latitude),
                String.format(Locale.getDefault(), "%.5f", longitude),
                locationName.toLowerCase(Locale.getDefault())
        );
        pinData.put("searchTerms", searchTerms);

        db.collection("pins")
                .add(pinData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Road accident pin created with ID: " + documentReference.getId());
                    Toast.makeText(MapViewActivity.this,
                            "Road accident pin saved successfully", Toast.LENGTH_SHORT).show();
                    refreshPinsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create road accident pin", e);
                    Toast.makeText(MapViewActivity.this,
                            "Unable to save road accident pin", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isFacilityCategory(String category) {
        if (category == null) {
            return false;
        }

        String normalized = category.trim().toLowerCase(java.util.Locale.getDefault());
        
        // Check exact matches first
        switch (normalized) {
            case "evacuation centers":
            case "evacuation center":
            case "health facilities":
            case "health facility":
            case "police stations":
            case "police station":
            case "fire stations":
            case "fire station":
            case "government offices":
            case "government office":
            case "others":
                 return true;
        }
        
        // Check if category contains facility keywords (must be specific to avoid false matches)
        // Only match "evacuation center" or "evacuation centers", not just "evacuation"
        if (normalized.contains("evacuation center") || normalized.contains("evacuation centers")) {
            return true;
        }
        // Only match "health facility" or "health facilities", not just "health"
        if ((normalized.contains("health facility") || normalized.contains("health facilities")) &&
            !normalized.contains("emergency") && !normalized.contains("medical emergency") &&
            !normalized.contains("disease") && !normalized.contains("infectious")) {
            return true;
        }
        // Also match hospital or clinic
        if (normalized.contains("hospital") || normalized.contains("clinic")) {
            if (!normalized.contains("emergency") && !normalized.contains("medical emergency")) {
                return true;
            }
        }
        if (normalized.contains("police") && normalized.contains("station")) {
            return true;
        }
        if (normalized.contains("fire") && normalized.contains("station")) {
            return true;
        }
        if (normalized.contains("government") && normalized.contains("office")) {
            return true;
        }
        if (normalized.contains("pulisya")) {
            return true;
        }
        if (normalized.contains("bumbero")) {
            return true;
        }
        if (normalized.contains("gobyerno")) {
            return true;
        }

        // Check if category matches any facility filter key
        if (facilityFilters != null) {
            for (String key : facilityFilters.keySet()) {
                if (key != null && key.trim().equalsIgnoreCase(category.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isHazardCategory(String category) {
        if (category == null) return false;
        String normalized = category.trim().toLowerCase(Locale.getDefault());

        if (isRoadAccidentCategory(category)) {
            return true;
        }
        if (normalized.contains("fire") && !normalized.contains("station") && !normalized.contains("bumbero")) {
            return true;
        }
        if (normalized.contains("medical") || normalized.contains("injur") || normalized.contains("emergency")) {
            return true;
        }
        if (normalized.contains("flood") || normalized.contains("baha")) {
            return true;
        }
        if (normalized.contains("volcan") || normalized.contains("bulkan")) {
            return true;
        }
        if (normalized.contains("landslide") || normalized.contains("guho")) {
            return true;
        }
        if (normalized.contains("earthquake") || normalized.contains("lindol")) {
            return true;
        }
        if (normalized.contains("civil") || normalized.contains("disturbance") || normalized.contains("riot")) {
            return true;
        }
        if (normalized.contains("armed") || normalized.contains("conflict") || normalized.contains("gunfire")
                || normalized.contains("barilan")) {
            return true;
        }
        if (normalized.contains("infectious") || normalized.contains("disease") || normalized.contains("outbreak")
                || normalized.contains("sakit")) {
            return true;
        }
        if (normalized.equals("others")) {
            return true;
        }
        return false;
    }

    private boolean isRoadAccidentCategory(String category) {
        if (category == null) return false;
        String normalized = category.trim().toLowerCase(Locale.getDefault());
        return normalized.contains("road accident")
                || normalized.contains("road_accident")
                || normalized.contains("road-crash")
                || (normalized.contains("road") && (normalized.contains("crash") || normalized.contains("accident")))
                || normalized.contains("collision")
                || normalized.equals("accident");
    }

    private String formatCategoryLabel(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "Not available";
        }

        String[] parts = category.trim().toLowerCase(java.util.Locale.getDefault()).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.length() > 0 ? builder.toString() : "Not available";
    }

    private void ensurePinDimensions() {
        if (pinWidthPx == 0 || pinHeightPx == 0 || pinOffsetPx == 0 || currentLocationPinOffsetPx == 0) {
            pinWidthPx = dpToPx(PIN_WIDTH_DP);
            pinHeightPx = dpToPx(PIN_HEIGHT_DP);
            pinOffsetPx = dpToPx(PIN_OFFSET_DP);
            currentLocationPinOffsetPx = dpToPx(CURRENT_LOCATION_PIN_OFFSET_DP);
        }
    }

    private int dpToPx(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateCheckboxVisualState(android.widget.CompoundButton checkbox, boolean isChecked) {
        try {
            if (checkbox == null) {
                return;
            }

            checkbox.animate()
                .scaleX(isChecked ? 1.05f : 1.0f)
                .scaleY(isChecked ? 1.05f : 1.0f)
                .setDuration(120)
                .withEndAction(() -> checkbox.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .start())
                .start();

            if (checkbox.getButtonDrawable() != null) {
                int tintColor = isChecked ? android.R.color.holo_orange_light : android.R.color.darker_gray;
                checkbox.setButtonTintList(ContextCompat.getColorStateList(this, tintColor));
            }
        } catch (Exception ignored) {
        }
    }

    private List<HotspotTutorialStep> buildHotspotTutorialSteps() {
        List<HotspotTutorialStep> steps = new ArrayList<>();
        steps.add(new HotspotTutorialStep(
                R.drawable.ic_hotspot_flame,
                R.string.hotspot_tutorial_welcome_title,
                R.string.hotspot_tutorial_welcome_description));
        steps.add(new HotspotTutorialStep(
                R.drawable.ic_hotspot_flame,
                R.string.view_incident_hotspots,
                R.string.view_incident_hotspots_description));
        steps.add(new HotspotTutorialStep(
                R.drawable.ic_hotspot_flame,
                R.string.hotspot_tutorial_emergency_title,
                R.string.hotspot_tutorial_emergency_description));
        steps.add(new HotspotTutorialStep(
                R.drawable.ic_hotspot_flame,
                R.string.hotspot_tutorial_pin_title,
                R.string.hotspot_tutorial_pin_description));
        return steps;
    }

    private void showHelpDialog() {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_welcome_hotspot);
            dialog.setCanceledOnTouchOutside(true);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            ImageButton closeButton = dialog.findViewById(R.id.button_close);
            ImageView iconView = dialog.findViewById(R.id.image_incident_flame);
            TextView titleView = dialog.findViewById(R.id.text_title);
            TextView descriptionView = dialog.findViewById(R.id.text_description);
            LinearLayout indicatorContainer = dialog.findViewById(R.id.indicator_container);
            Button previousButton = dialog.findViewById(R.id.button_previous);
            Button nextButton = dialog.findViewById(R.id.button_next);

            List<HotspotTutorialStep> steps = buildHotspotTutorialSteps();
            if (steps.isEmpty() || iconView == null || titleView == null || descriptionView == null
                    || indicatorContainer == null || previousButton == null || nextButton == null) {
                dialog.dismiss();
                return;
            }

            final int[] currentIndex = {0};

            Runnable refreshUi = new Runnable() {
                @Override
                public void run() {
                    HotspotTutorialStep step = steps.get(currentIndex[0]);
                    iconView.setImageResource(step.iconResId);
                    titleView.setText(step.titleResId);
                    descriptionView.setText(step.descriptionResId);

                    indicatorContainer.removeAllViews();
                    for (int i = 0; i < steps.size(); i++) {
                        View dot = new View(MapViewActivity.this);
                        int width = (i == currentIndex[0]) ? dpToPx(24f) : dpToPx(8f);
                        int height = dpToPx(8f);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                        if (i > 0) {
                            params.setMarginStart(dpToPx(8f));
                        }
                        dot.setLayoutParams(params);
                        dot.setBackgroundResource(i == currentIndex[0] ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive);
                        indicatorContainer.addView(dot);
                    }

                    boolean hasPrevious = currentIndex[0] > 0;
                    previousButton.setEnabled(hasPrevious);
                    previousButton.setAlpha(hasPrevious ? 1f : 0.4f);

                    boolean isLast = currentIndex[0] == steps.size() - 1;
                    nextButton.setText(isLast ? getString(R.string.get_started) : getString(R.string.next));
                }
            };

            refreshUi.run();

            previousButton.setOnClickListener(v -> {
                if (currentIndex[0] > 0) {
                    currentIndex[0]--;
                    refreshUi.run();
                }
            });

            nextButton.setOnClickListener(v -> {
                if (currentIndex[0] < steps.size() - 1) {
                    currentIndex[0]++;
                    refreshUi.run();
                } else {
                    dialog.dismiss();
                }
            });

            if (closeButton != null) {
                closeButton.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing help dialog", e);
        }
    }

    /**
     * Disable compass on the map view
     */
    private void disableCompass() {
        if (mapView == null) return;
        
        try {
            // Method 1: Try to find compass view by traversing the view hierarchy
            View compassView = findCompassView(mapView);
            if (compassView != null) {
                compassView.setVisibility(View.GONE);
                Log.d(TAG, "✅ Compass view hidden successfully");
                return;
            }
            
            // Method 2: Try to find by resource identifier
            try {
                int compassId = mapView.getContext().getResources().getIdentifier("compassView", "id", "com.mapbox.maps");
                if (compassId != 0) {
                    compassView = mapView.findViewById(compassId);
                    if (compassView != null) {
                        compassView.setVisibility(View.GONE);
                        Log.d(TAG, "✅ Compass view hidden via resource ID");
                        return;
                    }
                }
            } catch (Exception ignored) {}
            
            // Method 3: Hide all ImageViews that might be the compass (last resort)
            hideCompassViews(mapView);
            
        } catch (Exception e) {
            Log.w(TAG, "Could not disable compass: " + e.getMessage());
        }
    }
    
    /**
     * Recursively find compass view in the view hierarchy
     */
    private View findCompassView(ViewGroup parent) {
        if (parent == null) return null;
        
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            
            // Check if this view's class name or content description suggests it's a compass
            String className = child.getClass().getSimpleName().toLowerCase();
            if (className.contains("compass")) {
                return child;
            }
            
            // Check content description
            if (child.getContentDescription() != null) {
                String desc = child.getContentDescription().toString().toLowerCase();
                if (desc.contains("compass")) {
                    return child;
                }
            }
            
            // Recursively search in child ViewGroups
            if (child instanceof ViewGroup) {
                View found = findCompassView((ViewGroup) child);
                if (found != null) return found;
            }
        }
        
        return null;
    }
    
    /**
     * Hide compass views by searching for ImageViews that look like compass icons
     * This is a fallback method - only used if the compass view isn't found by other methods
     */
    private void hideCompassViews(ViewGroup parent) {
        if (parent == null) return;
        
        try {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                
                // Check if it's an ImageView that might be a compass
                if (child instanceof ImageView) {
                    ImageView imageView = (ImageView) child;
                    // Compass is usually a small square view in the top-right corner
                    // Be more specific: look for small square views (compass is typically 48-56dp)
                    if (child.getLayoutParams() != null) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                        int width = params.width;
                        int height = params.height;
                        
                        // Compass is typically 48-56dp (roughly 144-168px on mdpi)
                        // Check if it's a small square-ish view that could be compass
                        if (width > 0 && width < 200 && height > 0 && height < 200) {
                            // Additional check: compass is usually positioned in top-right
                            // We'll be conservative and only hide if it's clearly a compass-like view
                            String viewTag = child.getTag() != null ? child.getTag().toString().toLowerCase() : "";
                            if (viewTag.contains("compass") || 
                                (child.getContentDescription() != null && 
                                 child.getContentDescription().toString().toLowerCase().contains("compass"))) {
                                child.setVisibility(View.GONE);
                                Log.d(TAG, "✅ Hidden compass view via fallback method");
                            }
                        }
                    }
                }
                
                // Recursively search in child ViewGroups
                if (child instanceof ViewGroup) {
                    hideCompassViews((ViewGroup) child);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error hiding compass views: " + e.getMessage());
        }
    }

}