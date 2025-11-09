package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.ScreenCoordinate;

import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

// Location services imports
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapPickerActivity extends AppCompatActivity implements OnMapClickListener {
    
    private static final String TAG = "MapPickerActivity";
    
    // Location permission request code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    // Custom AutoComplete Adapter for location suggestions
    private class LocationAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> allSuggestions;
        private List<String> filteredSuggestions;
        private Geocoder geocoder;

        public LocationAutoCompleteAdapter(Context context, int resource) {
            super(context, resource);
            this.allSuggestions = new ArrayList<>();
            this.filteredSuggestions = new ArrayList<>();
            this.geocoder = new Geocoder(context, Locale.getDefault());
            
            // Add common Lucban locations
            addCommonLocations();
        }

        private void addCommonLocations() {
            // Lucban Locations
            allSuggestions.add("Lucban Municipal Hall");
            allSuggestions.add("Saint Louis Bishop Parish Church");
            allSuggestions.add("Lucban Elementary School");
            allSuggestions.add("Lucban National High School");
            allSuggestions.add("Quezon National Agricultural School");
            allSuggestions.add("Lucban Public Market");
            allSuggestions.add("Lucban Town Plaza");
            allSuggestions.add("Kamay ni Hesus Shrine");
            allSuggestions.add("Lucban Police Station");
            allSuggestions.add("Lucban Fire Station");
            allSuggestions.add("Lucban Rural Health Unit");
            allSuggestions.add("Lucban Post Office");
            allSuggestions.add("Lucban Municipal Library");
            allSuggestions.add("Lucban Sports Complex");
            allSuggestions.add("Lucban Cemetery");
            
            // Lucban Barangays
            allSuggestions.add("Brgy. Tinamnan");
            allSuggestions.add("Brgy. Ayuti");
            allSuggestions.add("Brgy. Piis");
            allSuggestions.add("Brgy. Kulapi");
            allSuggestions.add("Brgy. Mahabang Parang");
            allSuggestions.add("Brgy. Malupak");
            allSuggestions.add("Brgy. Manasa");
            allSuggestions.add("Brgy. May-it");
            allSuggestions.add("Brgy. Rizal");
            allSuggestions.add("Brgy. San Isidro");
            allSuggestions.add("Brgy. Santa Cruz");
            allSuggestions.add("Brgy. Silangang Mayao");
            allSuggestions.add("Brgy. Kanlurang Mayao");
            
            // Major Cities and Municipalities in Quezon Province
            allSuggestions.add("Lucena City");
            allSuggestions.add("Tayabas City");
            allSuggestions.add("Sariaya");
            allSuggestions.add("Candelaria");
            allSuggestions.add("Tiaong");
            allSuggestions.add("San Pablo City");
            allSuggestions.add("Alaminos");
            allSuggestions.add("Bay");
            allSuggestions.add("Calauag");
            allSuggestions.add("Calamba City");
            allSuggestions.add("Cavinti");
            allSuggestions.add("Dolores");
            allSuggestions.add("Gumaca");
            allSuggestions.add("Infanta");
            allSuggestions.add("Jomalig");
            allSuggestions.add("Lopez");
            allSuggestions.add("Mauban");
            allSuggestions.add("Mulanay");
            allSuggestions.add("Padre Burgos");
            allSuggestions.add("Pagbilao");
            allSuggestions.add("Panukulan");
            allSuggestions.add("Patnanungan");
            allSuggestions.add("Perez");
            allSuggestions.add("Pitogo");
            allSuggestions.add("Plaridel");
            allSuggestions.add("Polillo");
            allSuggestions.add("Quezon");
            allSuggestions.add("Real");
            allSuggestions.add("Sampaloc");
            allSuggestions.add("San Andres");
            allSuggestions.add("San Antonio");
            allSuggestions.add("San Francisco");
            allSuggestions.add("San Narciso");
            allSuggestions.add("Tagkawayan");
            allSuggestions.add("Unisan");
            
            // Major Landmarks and Tourist Spots
            allSuggestions.add("Mount Banahaw");
            allSuggestions.add("Mount Cristobal");
            allSuggestions.add("Quezon Memorial Circle");
            allSuggestions.add("Quezon Provincial Capitol");
            allSuggestions.add("Quezon Medical Center");
            allSuggestions.add("Quezon National High School");
            allSuggestions.add("Quezon State University");
            allSuggestions.add("Southern Luzon State University");
            allSuggestions.add("Manuel S. Enverga University Foundation");
            allSuggestions.add("Quezon Convention Center");
            allSuggestions.add("Quezon Sports Complex");
            allSuggestions.add("Quezon Provincial Hospital");
            allSuggestions.add("Quezon Provincial Jail");
            allSuggestions.add("Quezon Provincial Police Office");
            allSuggestions.add("Quezon Provincial Fire Station");
            
            // Beaches and Resorts
            allSuggestions.add("Borawan Island");
            allSuggestions.add("Dampalitan Island");
            allSuggestions.add("Pulong Bato Beach");
            allSuggestions.add("Cagbalete Island");
            allSuggestions.add("Alibijaban Island");
            allSuggestions.add("Jomalig Island");
            allSuggestions.add("Polillo Island");
            allSuggestions.add("Patnanungan Island");
            
            // Churches and Religious Sites
            allSuggestions.add("Minor Basilica of Saint Michael the Archangel");
            allSuggestions.add("Saint Ferdinand Cathedral");
            allSuggestions.add("Our Lady of the Most Holy Rosary Cathedral");
            allSuggestions.add("Saint Joseph Cathedral");
            allSuggestions.add("Saint Francis of Assisi Church");
            allSuggestions.add("Saint Anthony of Padua Church");
            allSuggestions.add("Saint Isidore Church");
            allSuggestions.add("Saint John the Baptist Church");
            
            // Markets and Commercial Areas
            allSuggestions.add("Lucena City Public Market");
            allSuggestions.add("Tayabas City Public Market");
            allSuggestions.add("Sariaya Public Market");
            allSuggestions.add("Candelaria Public Market");
            allSuggestions.add("Tiaong Public Market");
            allSuggestions.add("SM City Lucena");
            allSuggestions.add("Quezon Premier Mall");
            allSuggestions.add("Pacific Mall Lucena");
            
            // Transportation Hubs
            allSuggestions.add("Lucena Grand Central Terminal");
            allSuggestions.add("Tayabas Bus Terminal");
            allSuggestions.add("Sariaya Bus Terminal");
            allSuggestions.add("Candelaria Bus Terminal");
            allSuggestions.add("Lucena Port");
            allSuggestions.add("Real Port");
            allSuggestions.add("Infanta Port");
            allSuggestions.add("Polillo Port");
            
            // Government Offices
            allSuggestions.add("Quezon Provincial Capitol");
            allSuggestions.add("Lucena City Hall");
            allSuggestions.add("Tayabas City Hall");
            allSuggestions.add("Sariaya Municipal Hall");
            allSuggestions.add("Candelaria Municipal Hall");
            allSuggestions.add("Tiaong Municipal Hall");
            allSuggestions.add("Gumaca Municipal Hall");
            allSuggestions.add("Mauban Municipal Hall");
            allSuggestions.add("Calauag Municipal Hall");
            allSuggestions.add("Lopez Municipal Hall");
            allSuggestions.add("Unisan Municipal Hall");
            allSuggestions.add("Padre Burgos Municipal Hall");
            allSuggestions.add("Pagbilao Municipal Hall");
            allSuggestions.add("Real Municipal Hall");
            allSuggestions.add("Infanta Municipal Hall");
        }

        @Override
        public int getCount() {
            return filteredSuggestions.size();
        }

        @Override
        public String getItem(int position) {
            return filteredSuggestions.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    filteredSuggestions.clear();

                    if (constraint != null && constraint.length() > 0) {
                        String searchText = constraint.toString().toLowerCase();
                        
                        // Filter common locations
                        for (String suggestion : allSuggestions) {
                            if (suggestion.toLowerCase().contains(searchText)) {
                                filteredSuggestions.add(suggestion);
                            }
                        }
                        
                        // Add geocoding suggestions if we have less than 5 results
                        if (filteredSuggestions.size() < 5) {
                            addGeocodingSuggestions(searchText);
                        }
                    }

                    results.values = filteredSuggestions;
                    results.count = filteredSuggestions.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        private void addGeocodingSuggestions(String searchText) {
            if (searchText.length() < 3) return; // Only search for 3+ characters
            
            try {
                // Search in Quezon Province instead of just Lucban
                List<Address> addresses = geocoder.getFromLocationName(searchText + ", Quezon Province, Philippines", 5);
                if (addresses != null) {
                    for (Address address : addresses) {
                        String suggestion = buildLocationSuggestion(address);
                        if (!filteredSuggestions.contains(suggestion)) {
                            filteredSuggestions.add(suggestion);
                        }
                    }
                }
            } catch (IOException e) {
                // Ignore geocoding errors
            }
        }

        private String buildLocationSuggestion(Address address) {
            StringBuilder suggestion = new StringBuilder();
            
            if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
                suggestion.append(address.getFeatureName());
            }
            
            if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty() && 
                !address.getThoroughfare().equals(address.getFeatureName())) {
                if (suggestion.length() > 0) suggestion.append(", ");
                suggestion.append(address.getThoroughfare());
            }
            
            if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                if (suggestion.length() > 0) suggestion.append(", ");
                suggestion.append(address.getLocality());
            }
            
            return suggestion.toString();
        }
    }

    private MapView mapView;
    private MapboxMap mapboxMap;
    private CameraAnimationsPlugin cameraAnimationsPlugin;
    private GesturesPlugin gesturesPlugin;
    private Point selectedPoint;
    private Style currentStyle;
    
    // Simple marker system - just like Google Maps
    private FrameLayout mapContainer;
    private ImageView currentMarker;
    private Point pinnedLocation;
    private static final float MARKER_WIDTH_DP = 44f;
    private static final float MARKER_HEIGHT_DP = 60f;
    private static final float MARKER_PIN_OFFSET_DP = 3f;
    private int markerWidthPx;
    private int markerHeightPx;
    private int markerPinOffsetPx;
    
    // Camera tracking for marker positioning
    private Handler cameraUpdateHandler;
    private Runnable cameraUpdateRunnable;
    
    // Threading for reverse geocoding
    private ExecutorService executorService;
    
    // UI Components
    private ImageButton btnBack;
    private AutoCompleteTextView etSearchLocation;
    private LocationAutoCompleteAdapter autocompleteAdapter;
    
    // Popup Components
    private LinearLayout locationPopup;
    private TextView tvPopupLocationName;
    private TextView tvPopupCoordinates;
    private Button btnCancelLocation;
    private Button btnSelectLocationPopup;
    
    // Current location info
    private String currentLocationName = "";
    
    // View-only mode flag
    private boolean isViewOnlyMode = false;
    
    // Location services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);
        
        // Check if opened in view-only mode
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            isViewOnlyMode = receivedIntent.getBooleanExtra("viewOnlyMode", false);
            Log.d(TAG, "MapPicker mode: " + (isViewOnlyMode ? "VIEW ONLY" : "SELECTION"));
        }

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize camera tracking handler
        cameraUpdateHandler = new Handler(Looper.getMainLooper());
        
        // Initialize location services
        initializeLocationServices();

        // Initialize UI components
        mapView = findViewById(R.id.mapView);
        mapContainer = findViewById(R.id.mapContainer);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnBack = findViewById(R.id.btnBack);
        
        // If in view-only mode, hide search functionality
        if (isViewOnlyMode) {
            if (etSearchLocation != null) etSearchLocation.setVisibility(View.GONE);
            Log.d(TAG, "✅ View-only mode: Search UI hidden");
        } else {
            // Setup autocomplete functionality only if not in view-only mode
            setupAutocompleteSearch();
        }
        
        // Initialize popup components
        locationPopup = findViewById(R.id.locationPopup);
        tvPopupLocationName = findViewById(R.id.tvPopupLocationName);
        tvPopupCoordinates = findViewById(R.id.tvPopupCoordinates);
        btnCancelLocation = findViewById(R.id.btnCancelLocation);
        btnSelectLocationPopup = findViewById(R.id.btnSelectLocationPopup);
        
        // Setup back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Setup popup button listeners
        if (btnCancelLocation != null) {
            btnCancelLocation.setOnClickListener(v -> hideLocationPopup());
        }
        
        if (btnSelectLocationPopup != null) {
            btnSelectLocationPopup.setOnClickListener(v -> selectLocationFromPopup());
        }

        mapboxMap = mapView.getMapboxMap();
        
        // Initialize plugins before loading style
        cameraAnimationsPlugin = CameraAnimationsUtils.getCamera(mapView);
        gesturesPlugin = GesturesUtils.getGestures(mapView);
        
        mapboxMap.loadStyleUri("mapbox://styles/accizard-lucban-official/cmhox8ita005o01sr1psmbgp6", style -> {
            currentStyle = style;

            // Ensure Lucban boundary is visible on initial load
            ensureLucbanBoundaryVisible(style);
            
            // Retry with delays to ensure the layer is accessible
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ensureLucbanBoundaryVisible(mapboxMap.getStyle());
            }, 300);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ensureLucbanBoundaryVisible(mapboxMap.getStyle());
            }, 800);

            // Add map click listener - must be added after style is loaded
            setupMapClickListener();
            
            // Check if we have a previously selected location
            handlePreviouslySelectedLocation();
            
            Log.d("MapPicker", "Map style loaded, click listener registered");
        });
    }

    /**
     * Ensure Lucban Boundary layer is always visible on initial load
     * This layer shows the main Lucban boundary outline
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
     * Handle previously selected location when MapPickerActivity is reopened
     */
    private void handlePreviouslySelectedLocation() {
        Intent intent = getIntent();
        if (intent != null) {
            double longitude = intent.getDoubleExtra("selectedLongitude", 0.0);
            double latitude = intent.getDoubleExtra("selectedLatitude", 0.0);
            String locationName = intent.getStringExtra("selectedLocationName");
            
            if (longitude != 0.0 && latitude != 0.0) {
                // We have a previously selected location
                Point previousPoint = Point.fromLngLat(longitude, latitude);

                // Set as selected point
                selectedPoint = previousPoint;
                
                // Add marker at the previous location
                addMarkerAtLocation(previousPoint);
                
                // Move camera to the previous location
                moveCameraToLocation(previousPoint);
                
                // Show popup with previous location details
                showLocationPopup(previousPoint);
                
                // Set the location name if available
                if (locationName != null && !locationName.isEmpty()) {
                    currentLocationName = locationName;
                    if (tvPopupLocationName != null) {
                        tvPopupLocationName.setText("📍 " + currentLocationName);
                    }
                } else {
                    // Get location details for the previous location
                    getEnhancedLocationDetails(previousPoint);
                }
                
                // Set search text to show the previous location (only if not in view-only mode)
                if (!isViewOnlyMode && etSearchLocation != null && locationName != null) {
                    etSearchLocation.setText(locationName);
                }
                
                // In view-only mode, show a helpful message
                if (isViewOnlyMode) {
                    Toast.makeText(this, "📍 Viewing your current location. Tap outside to close.", Toast.LENGTH_LONG).show();
                }
            } else {
                // No previous location, set default camera position to Lucban center
                // This ensures the boundaries are visible on initial load
                Point lucbanCenter = Point.fromLngLat(121.5564, 14.1136);
                CameraOptions initialCamera = new CameraOptions.Builder()
                        .center(lucbanCenter)
                        .zoom(14.0)
                        .build();
                mapboxMap.setCamera(initialCamera);
                Log.d(TAG, "Initial camera set to Lucban center with boundaries visible");
            }
        }
    }

    /**
     * Setup autocomplete functionality for search
     */
    private void setupAutocompleteSearch() {
        // Create autocomplete adapter with custom layout
        autocompleteAdapter = new LocationAutoCompleteAdapter(this, R.layout.item_autocomplete_suggestion);
        
        // Set adapter to AutoCompleteTextView
        etSearchLocation.setAdapter(autocompleteAdapter);
        etSearchLocation.setThreshold(1); // Show suggestions after 1 character
        
        // Handle item selection
        etSearchLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = autocompleteAdapter.getItem(position);
            etSearchLocation.setText(selectedLocation);
            etSearchLocation.dismissDropDown();
            
            // Automatically search for the selected location
            searchLocation(selectedLocation);
        });
        
        // Handle text changes for real-time suggestions
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show dropdown when typing
                if (s.length() > 0) {
                    etSearchLocation.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void searchLocation(String locationName) {
        // Show loading message
        Toast.makeText(this, "Searching for: " + locationName, Toast.LENGTH_SHORT).show();
        
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Enhanced search query for better accuracy
            String enhancedQuery = locationName;
            if (!locationName.toLowerCase().contains("philippines") && 
                !locationName.toLowerCase().contains("quezon")) {
                enhancedQuery = locationName + ", Quezon Province, Philippines";
            }
            
            List<Address> addresses = geocoder.getFromLocationName(enhancedQuery, 10); // Increased to 10 for better results
            if (addresses != null && !addresses.isEmpty()) {
                // Find the most accurate address (prefer addresses with more details)
                Address bestAddress = findMostAccurateAddress(addresses);
                
                // Use high precision coordinates
                double longitude = bestAddress.getLongitude();
                double latitude = bestAddress.getLatitude();
                
                // Validate coordinates are within reasonable bounds for Philippines
                if (isValidPhilippinesCoordinates(longitude, latitude)) {
                    Point point = Point.fromLngLat(longitude, latitude);

                    // Clear existing marker
                    clearMarker();

                    // Set as selected point with high precision
                    selectedPoint = point;

                    // Add marker with precise positioning
                    addMarkerAtLocation(point);

                    // Move camera to location with higher zoom for accuracy
                    moveCameraToLocation(point);

                    // Show popup with precise coordinates
                    showLocationPopup(point);

                    // Get detailed location information
                    getEnhancedLocationDetails(point);
                    
                    // Update the search field to show the found location
                    etSearchLocation.setText(locationName);
                    
                    // Log coordinates for debugging
                    Log.d("MapPicker", "Search result coordinates: " + latitude + ", " + longitude);
                    Log.d("MapPicker", "Search result address: " + bestAddress.getAddressLine(0));
                    
                    Toast.makeText(this, "Location found and pinned!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid coordinates for Philippines", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location '" + locationName + "' not found. Try a different search term.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("MapPicker", "Error searching location: " + locationName, e);
            Toast.makeText(this, "Error searching location. Please check your internet connection.", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Find the most accurate address from the list
     */
    private Address findMostAccurateAddress(List<Address> addresses) {
        Address bestAddress = addresses.get(0);
        int maxScore = calculateAddressAccuracyScore(bestAddress);
        
        for (Address address : addresses) {
            int score = calculateAddressAccuracyScore(address);
            if (score > maxScore) {
                maxScore = score;
                bestAddress = address;
            }
        }
        
        return bestAddress;
    }
    
    /**
     * Calculate accuracy score for an address (higher is better)
     */
    private int calculateAddressAccuracyScore(Address address) {
        int score = 0;
        
        // Prefer addresses with more detailed information
        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) score += 10;
        if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) score += 8;
        if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().isEmpty()) score += 6;
        if (address.getLocality() != null && !address.getLocality().isEmpty()) score += 5;
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) score += 4;
        if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) score += 3;
        if (address.getSubAdminArea() != null && !address.getSubAdminArea().isEmpty()) score += 2;
        if (address.getCountryName() != null && !address.getCountryName().isEmpty()) score += 1;
        
        // Prefer addresses with postal codes
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) score += 5;
        
        // Strongly prefer locations in Lucban, Quezon Province
        String locality = address.getLocality() != null ? address.getLocality().toLowerCase() : "";
        String adminArea = address.getAdminArea() != null ? address.getAdminArea().toLowerCase() : "";
        
        if (locality.contains("lucban")) {
            score += 20; // Highest priority for Lucban
        }
        if (adminArea.contains("quezon")) {
            score += 15; // High priority for Quezon Province
        }
        
        // Prefer Philippines locations
        if ("Philippines".equalsIgnoreCase(address.getCountryName())) {
            score += 10;
        }
        
        return score;
    }
    
    /**
     * Validate if coordinates are within Philippines bounds
     */
    private boolean isValidPhilippinesCoordinates(double longitude, double latitude) {
        // Philippines approximate bounds
        return longitude >= 116.0 && longitude <= 127.0 && 
               latitude >= 4.0 && latitude <= 22.0;
    }

    @Override
    public boolean onMapClick(@NonNull Point point) {
        Log.d("MapPicker", "===== MAP CLICKED =====");
        Log.d("MapPicker", String.format("Map tapped at: %.6f, %.6f", 
            point.longitude(), point.latitude()));
        
        try {
            // Clear any existing marker
            clearMarker();
            Log.d("MapPicker", "Previous marker cleared");
            
            // Store the selected point
            selectedPoint = point;
            Log.d("MapPicker", "Selected point stored");

            // Add marker immediately (like Google Maps red pin)
            addMarkerAtLocation(point);
            Log.d("MapPicker", "Marker added to map");

            // Show popup immediately with location details (like Google Maps)
            showLocationPopup(point);
            Log.d("MapPicker", "Popup shown");

            // Get detailed location information in background with enhanced geocoding
            getEnhancedLocationDetails(point);
            Log.d("MapPicker", "Getting location details...");

            // Clear search field to indicate a new manual pin
            if (etSearchLocation != null) {
                etSearchLocation.setText("");
            }
            
            // Show feedback to user
            Toast.makeText(this, "Location pinned!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e("MapPicker", "Error handling map click", e);
            Toast.makeText(this, "Error pinning location", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    /**
     * Setup map click listener
     */
    private void setupMapClickListener() {
        try {
            // Don't add click listener if in view-only mode
            if (isViewOnlyMode) {
                Log.d("MapPicker", "✅ View-only mode: Map click listener NOT added (map is view-only)");
                return;
            }
            
            if (gesturesPlugin != null) {
                gesturesPlugin.addOnMapClickListener(this);
                Log.d("MapPicker", "Map click listener registered successfully");
            } else {
                Log.e("MapPicker", "GesturesPlugin is null - click listener NOT registered");
                Toast.makeText(this, "Warning: Map gestures not initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MapPicker", "Error setting up map click listener", e);
            Toast.makeText(this, "Error setting up map interaction", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Add marker at the tapped location using accizard_pin drawable
     * GEOGRAPHIC LOCKING: Marker stays at fixed coordinates during zoom/pan
     */
    private void addMarkerAtLocation(Point point) {
        Log.d(TAG, "addMarkerAtLocation called");
        
        if (mapContainer != null) {
            try {
                Log.d(TAG, "Map container found, creating marker...");
                
                // Create marker using accizard_pin drawable
                ensureMarkerDimensions();

                ImageView markerView = new ImageView(this);
                markerView.setImageResource(R.drawable.accizard_pin);
                FrameLayout.LayoutParams initialParams = new FrameLayout.LayoutParams(markerWidthPx, markerHeightPx);
                markerView.setLayoutParams(initialParams);
                Log.d(TAG, "Marker created using accizard_pin drawable");
                
                // CRITICAL: Store the FIXED geographic location (lat/lon)
                // This Point object will NEVER change - it represents the exact location
                pinnedLocation = point;
                Log.d(TAG, String.format("📍 Pinned location stored at FIXED coordinates: %.6f, %.6f", 
                    point.latitude(), point.longitude()));
                
                // Add to container
                mapContainer.addView(markerView);
                currentMarker = markerView;
                Log.d(TAG, "Marker added to container");
                
                // Position marker at actual coordinates immediately
                positionMarkerAtCoordinates(point);
                Log.d(TAG, "Marker positioned");
                
                // Add Google Maps-style drop animation
                animateMarkerDrop(markerView);
                Log.d(TAG, "Marker animation started");
                
                // Start camera tracking to keep marker positioned correctly during zoom/pan
                // This will continuously update the screen position to match the geographic coordinates
                startCameraTracking();
                Log.d(TAG, "🔒 Geographic locking enabled - pin will stay at coordinates during zoom/pan");
                
                Log.d(TAG, "✅ Marker successfully added and locked at: " + point.longitude() + ", " + point.latitude());
                
            } catch (Exception e) {
                Log.e(TAG, "Error adding marker", e);
                Toast.makeText(this, "Error creating marker", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "mapContainer is NULL - cannot add marker!");
            Toast.makeText(this, "Error: Map container not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Animate marker drop like Google Maps
     */
    private void animateMarkerDrop(ImageView markerView) {
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
     * Position marker at specific coordinates with high precision (using accizard_pin drawable)
     * GEOGRAPHIC LOCKING: Converts FIXED geographic coordinates to current screen position
     */
    private void positionMarkerAtCoordinates(Point point) {
        if (currentMarker != null && mapboxMap != null && mapContainer != null) {
        try {
                // CRITICAL: The Point object contains FIXED geographic coordinates (lat/lon)
                // These coordinates NEVER change - only the screen position changes
                // Convert FIXED geographic coordinates to CURRENT screen coordinates
            ScreenCoordinate screenCoord = mapboxMap.pixelForCoordinate(point);
            
            // Get map container dimensions
            int containerWidth = mapContainer.getWidth();
            int containerHeight = mapContainer.getHeight();
            
                if (containerWidth <= 0 || containerHeight <= 0) {
                    // Container not ready yet, try again later
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        positionMarkerAtCoordinates(point);
                    }, 100);
                    return;
                }
                
                // Calculate marker position relative to container with high precision
            double x = screenCoord.getX();
            double y = screenCoord.getY();
            
                // Check if coordinates are within visible bounds (with some margin)
                int margin = 80; // Increased margin for accizard_pin drawable size
                if (x >= -margin && x <= containerWidth + margin && 
                    y >= -margin && y <= containerHeight + margin) {
                    
                // Create layout parameters with absolute positioning
                ensureMarkerDimensions();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    markerWidthPx,
                    markerHeightPx
                );
                
                    // Precise marker positioning for accizard_pin drawable
                    // accizard_pin dimensions: width=90dp, height=116dp (adjust if different)
                    // Pin point is at the bottom tip of the marker
                    // Center the pin point exactly on the geographic coordinates
                    params.leftMargin = (int) Math.round(x - (markerWidthPx / 2.0));
                    params.topMargin = (int) Math.round(y - markerHeightPx + markerPinOffsetPx);
                    
                    currentMarker.setLayoutParams(params);
                    currentMarker.setVisibility(View.VISIBLE);
                    
                    // Periodic logging every 60 frames (~1 second at 60fps)
                    // Log.d(TAG, String.format("📍 Pin LOCKED at geo: %.6f, %.6f -> screen: %.0f, %.0f", 
                    //     point.latitude(), point.longitude(), x, y));
                    
            } else {
                    // Marker is outside visible area, hide it
                    currentMarker.setVisibility(View.GONE);
                    // Log.d(TAG, "Pin hidden - outside visible bounds");
            }
            
        } catch (Exception e) {
                Log.e(TAG, "Error positioning marker", e);
                // Keep marker hidden if positioning fails
                if (currentMarker != null) {
                    currentMarker.setVisibility(View.GONE);
                }
            }
        }
    }

    private void ensureMarkerDimensions() {
        if (markerWidthPx == 0 || markerHeightPx == 0 || markerPinOffsetPx == 0) {
            markerWidthPx = dpToPx(MARKER_WIDTH_DP);
            markerHeightPx = dpToPx(MARKER_HEIGHT_DP);
            markerPinOffsetPx = dpToPx(MARKER_PIN_OFFSET_DP);
        }
    }

    private int dpToPx(float dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /**
     * REMOVED: No longer needed - using accizard_pin drawable instead
     * 
     * This method has been replaced with direct drawable usage in addMarkerAtLocation()
     * The marker now uses R.drawable.accizard_pin for consistent branding
     */
    // private Bitmap createMarkerBitmap() { ... }
    
    /**
     * Start camera tracking to keep marker positioned correctly
     * GEOGRAPHIC LOCKING: Pin stays at exact LAT/LON coordinates during zoom/pan
     */
    private void startCameraTracking() {
        // Stop any existing tracking first
        stopCameraTracking();
        
        if (cameraUpdateRunnable == null) {
            cameraUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    // Update marker position if it exists
                    if (currentMarker != null && pinnedLocation != null) {
                        // CRITICAL: Always use the FIXED pinnedLocation Point
                        // This ensures the pin stays at the same geographic coordinates
                        positionMarkerAtCoordinates(pinnedLocation);
                    }
                    
                    // Schedule next update for smooth tracking during camera movements
                    if (cameraUpdateHandler != null && currentMarker != null) {
                        cameraUpdateHandler.postDelayed(this, 16); // Update every 16ms (~60fps) for smooth tracking
                    }
                }
            };
        }
        
        // Start the tracking
        if (cameraUpdateHandler != null) {
            cameraUpdateHandler.post(cameraUpdateRunnable);
            Log.d(TAG, "🚀 Camera tracking started - Pin will stay at geographic coordinates during zoom/pan");
        }
    }

    /**
     * Stop camera tracking
     */
    private void stopCameraTracking() {
        if (cameraUpdateHandler != null && cameraUpdateRunnable != null) {
            cameraUpdateHandler.removeCallbacks(cameraUpdateRunnable);
            Log.d(TAG, "Camera tracking stopped");
        }
    }

    /**
     * Clear current marker
     */
    private void clearMarker() {
        if (currentMarker != null && mapContainer != null) {
            mapContainer.removeView(currentMarker);
            currentMarker = null;
            pinnedLocation = null;
            
            // Stop camera tracking when no markers
            stopCameraTracking();
        }
    }

    /**
     * Show enhanced Google Maps-style location popup with high precision coordinates
     */
    private void showLocationPopup(Point point) {
        if (locationPopup != null) {
            // Update coordinates with high precision (6 decimal places = ~0.1m accuracy)
            if (tvPopupCoordinates != null) {
                tvPopupCoordinates.setText("📍 " + String.format("%.6f, %.6f", 
                    point.longitude(), point.latitude()));
            }
            
            // Set loading text with better formatting (like Google Maps)
            if (tvPopupLocationName != null) {
                tvPopupLocationName.setText("🔍 Getting location details...");
            }
            
            // Modify buttons for view-only mode
            if (isViewOnlyMode) {
                // In view-only mode: Hide Select button, change Cancel to Close
                if (btnSelectLocationPopup != null) {
                    btnSelectLocationPopup.setVisibility(View.GONE);
                }
                if (btnCancelLocation != null) {
                    btnCancelLocation.setText("Close");
                    btnCancelLocation.setOnClickListener(v -> finish()); // Close activity instead of just hiding popup
                }
                Log.d("MapPicker", "✅ View-only mode: Popup buttons configured (Close only, no Select)");
            } else {
                // Normal mode: Show both buttons
                if (btnSelectLocationPopup != null) {
                    btnSelectLocationPopup.setVisibility(View.VISIBLE);
                }
                if (btnCancelLocation != null) {
                    btnCancelLocation.setText("Cancel");
                    btnCancelLocation.setOnClickListener(v -> hideLocationPopup());
                }
            }
            
            // Show popup with enhanced Google Maps-style animation
            locationPopup.setVisibility(View.VISIBLE);
            locationPopup.setAlpha(0f);
            locationPopup.setTranslationY(120f);
            locationPopup.setScaleX(0.8f);
            locationPopup.setScaleY(0.8f);
            
            // Enhanced animate in (Google Maps style)
            locationPopup.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.2f))
                .start();
                
            // Log popup display for debugging
            Log.d("MapPicker", String.format("Enhanced popup shown for coordinates: %.6f, %.6f", 
                point.longitude(), point.latitude()));
        }
    }

    /**
     * Hide location popup
     */
    private void hideLocationPopup() {
        if (locationPopup != null) {
            locationPopup.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(200)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(() -> locationPopup.setVisibility(View.GONE))
                .start();
            
            // Clear marker and reset
            clearMarker();
            selectedPoint = null;
            currentLocationName = "";
        }
    }

    /**
     * Select location from popup with high precision coordinates
     */
    private void selectLocationFromPopup() {
        if (selectedPoint != null) {
            // Return location data with high precision coordinates
            Intent resultIntent = new Intent();
            
            // Format coordinates with high precision (6 decimal places = ~0.1m accuracy)
            String coordinatesString = String.format("%.6f, %.6f", 
                selectedPoint.longitude(), selectedPoint.latitude());
            
            resultIntent.putExtra("pickedLocation", coordinatesString);
            resultIntent.putExtra("longitude", selectedPoint.longitude());
            resultIntent.putExtra("latitude", selectedPoint.latitude());
            resultIntent.putExtra("locationName", currentLocationName);
            
            // Log the final coordinates being returned
            Log.d("MapPicker", String.format("Returning coordinates: %.6f, %.6f", 
                selectedPoint.longitude(), selectedPoint.latitude()));
            Log.d("MapPicker", "Location name: " + currentLocationName);
            
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    /**
     * Get enhanced location details using reverse geocoding (Google Maps style)
     */
    private void getEnhancedLocationDetails(Point point) {
        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                // Get multiple addresses for better accuracy
                List<Address> addresses = geocoder.getFromLocation(
                    point.latitude(), 
                    point.longitude(), 
                    5 // Get up to 5 addresses for better selection
                );
                
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        // Find the best address from the results
                        Address bestAddress = findBestAddress(addresses);
                        
                        // Build location name (like Google Maps)
                        currentLocationName = buildGoogleMapsStyleLocationName(bestAddress);
                        
                        // Update popup with location details (Google Maps style)
                        if (tvPopupLocationName != null) {
                            if (currentLocationName.isEmpty() || currentLocationName.equals("Dropped Pin")) {
                                tvPopupLocationName.setText("📍 Dropped Pin");
                            } else {
                                tvPopupLocationName.setText("📍 " + currentLocationName);
                            }
                        }
                        
                        // Log the enhanced location details
                        Log.d("MapPicker", "Enhanced location: " + currentLocationName);
                        
                        // Update search field to show the current location name
                        if (etSearchLocation != null && !currentLocationName.isEmpty() && !currentLocationName.equals("Dropped Pin")) {
                            runOnUiThread(() -> {
                                etSearchLocation.setText(currentLocationName);
                            });
                        }
                        
                    } else {
                        currentLocationName = "Dropped Pin";
                        if (tvPopupLocationName != null) {
                            tvPopupLocationName.setText("📍 " + currentLocationName);
                        }
                    }
                });
                
            } catch (IOException e) {
                Log.e("MapPicker", "Error getting location details", e);
                runOnUiThread(() -> {
                    currentLocationName = "Dropped Pin";
                    if (tvPopupLocationName != null) {
                        tvPopupLocationName.setText("📍 " + currentLocationName);
                    }
                });
            }
        });
    }

    /**
     * Find the best address from multiple geocoding results
     */
    private Address findBestAddress(List<Address> addresses) {
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
     * Calculate score for address quality (higher is better)
     */
    private int calculateAddressScore(Address address) {
        int score = 0;
        
        // Feature name gets highest priority
        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
            score += 20;
            // Prefer named locations over generic ones
            String featureName = address.getFeatureName().toLowerCase();
            if (featureName.contains("church") || featureName.contains("school") || 
                featureName.contains("hospital") || featureName.contains("market") ||
                featureName.contains("hall") || featureName.contains("station")) {
                score += 10;
            }
        }
        
        // Thoroughfare (street name) is important
        if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
            score += 15;
        }
        
        // Sub-thoroughfare (house number) adds precision
        if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().isEmpty()) {
            score += 10;
        }
        
        // Locality (city/town) is important for context
        if (address.getLocality() != null && !address.getLocality().isEmpty()) {
            score += 12;
            
            // Strongly prefer locations in Lucban
            String locality = address.getLocality().toLowerCase();
            if (locality.contains("lucban")) {
                score += 25; // Highest priority for Lucban
            }
        }
        
        // Sub-locality (neighborhood) adds detail
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
            score += 8;
        }
        
        // Admin area (province) provides regional context
        if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
            score += 6;
            
            // Strongly prefer Quezon Province
            String adminArea = address.getAdminArea().toLowerCase();
            if (adminArea.contains("quezon")) {
                score += 20; // High priority for Quezon Province
            }
        }
        
        // Postal code indicates accuracy
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            score += 5;
        }
        
        // Country name should be Philippines
        if ("Philippines".equalsIgnoreCase(address.getCountryName())) {
            score += 10; // Increased from 3 to 10 for Philippines
        }
        
        return score;
    }

    /**
     * Build Google Maps style location name
     */
    private String buildGoogleMapsStyleLocationName(Address address) {
        if (address == null) return "Dropped Pin";
        
        StringBuilder locationName = new StringBuilder();
        
        // Primary location name (feature name)
        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
            locationName.append(address.getFeatureName());
        }
        
        // Add street name if different from feature name
        if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty() && 
            !address.getThoroughfare().equals(address.getFeatureName())) {
            if (locationName.length() > 0) locationName.append(", ");
            locationName.append(address.getThoroughfare());
        }
        
        // Add house number if available
        if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().isEmpty()) {
            if (locationName.length() > 0) locationName.append(" ");
            locationName.append(address.getSubThoroughfare());
        }
        
        // Add neighborhood/sub-locality if different from feature name
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty() && 
            !address.getSubLocality().equals(address.getFeatureName())) {
            if (locationName.length() > 0) locationName.append(", ");
            locationName.append(address.getSubLocality());
        }
        
        // Add city/town if different
        if (address.getLocality() != null && !address.getLocality().isEmpty() && 
            !address.getLocality().equals(address.getFeatureName()) &&
            !address.getLocality().equals(address.getSubLocality())) {
            if (locationName.length() > 0) locationName.append(", ");
            locationName.append(address.getLocality());
        }
        
        // Add province for context (but not if it's already mentioned)
        if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
            String adminArea = address.getAdminArea();
            if (!locationName.toString().contains(adminArea)) {
                if (locationName.length() > 0) locationName.append(", ");
                locationName.append(adminArea);
            }
        }
        
        String result = locationName.toString().trim();
        return result.isEmpty() ? "Dropped Pin" : result;
    }

    /**
     * Move camera to location with high precision zoom
     */
    private void moveCameraToLocation(Point point) {
        if (mapboxMap != null) {
            // Use higher zoom level for better accuracy (18.0 for street-level precision)
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .center(point)
                    .zoom(18.0) // Increased from 17.0 for better accuracy
                        .build();

                mapboxMap.setCamera(cameraOptions);

                if (cameraAnimationsPlugin != null) {
                        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder()
                        .duration(1500)
                                .build();
                        cameraAnimationsPlugin.flyTo(cameraOptions, animationOptions, null);
            }
            
            // Log camera movement for debugging
            Log.d("MapPicker", String.format("Camera moved to: %.6f, %.6f at zoom %.1f", 
                point.longitude(), point.latitude(), 18.0));
        }
    }

    /**
     * Initialize location services
     */
    private void initializeLocationServices() {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    handleCurrentLocationUpdate(location);
                    
                    // Stop location updates after getting the location
                    stopLocationUpdates();
                }
            }
        };
    }
    
    /**
     * Get current location using GPS
     */
    private void getCurrentLocation() {
        // Check if location permissions are granted
        if (!checkLocationPermissions()) {
            requestLocationPermissions();
            return;
        }
        
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Show loading message
        Toast.makeText(this, "Getting your current location...", Toast.LENGTH_SHORT).show();
        
        // Get current location
        getCurrentLocationWithCallback();
    }
    
    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    
    private void getCurrentLocationWithCallback() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Create location request
                LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(2000)
                        .setMaxUpdateDelayMillis(10000)
                        .build();
                
                // Request location updates
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                
                // Also try to get last known location as fallback
                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            // Check if location is recent (within 5 minutes)
                            if (System.currentTimeMillis() - location.getTime() < 5 * 60 * 1000) {
                                handleCurrentLocationUpdate(location);
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("MapPicker", "Security exception while getting location", e);
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleCurrentLocationUpdate(Location location) {
        if (location != null) {
            // Create point from current location
            Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            
            // Clear existing marker
            clearMarker();
            
            // Set as selected point
            selectedPoint = currentPoint;
            
            // Add marker at current location
            addMarkerAtLocation(currentPoint);
            
            // Move camera to current location
            moveCameraToLocation(currentPoint);
            
            // Show popup with current location
            showLocationPopup(currentPoint);
            
            // Get location details
            getEnhancedLocationDetails(currentPoint);
            
            // Update search field to show current location
            if (etSearchLocation != null) {
                etSearchLocation.setText("My Current Location");
            }
            
            // Show success message
            Toast.makeText(this, "Current location obtained!", Toast.LENGTH_SHORT).show();
            
            Log.d("MapPicker", "Current location: " + location.getLatitude() + ", " + location.getLongitude());
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // MapView lifecycle methods
    @Override 
    protected void onStart() { 
        super.onStart(); 
        mapView.onStart();
    }
    
    @Override 
    protected void onStop() { 
        super.onStop(); 
        mapView.onStop();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, try to get location again
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to get your current location", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override 
    protected void onDestroy() { 
        super.onDestroy(); 
        mapView.onDestroy();
        
        // Cleanup executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // Cleanup camera tracking
        stopCameraTracking();
        
        // Stop location updates to prevent memory leaks
        stopLocationUpdates();
    }
    
    @Override 
    public void onLowMemory() { 
        super.onLowMemory(); 
        mapView.onLowMemory();
    }
}