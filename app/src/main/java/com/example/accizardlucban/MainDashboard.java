package com.example.accizardlucban;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.graphics.Typeface;
import android.net.Uri;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.SharedPreferences;

public class MainDashboard extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;
    private static final String TAG = "MainDashboard";
    private static final String PREFS_NAME = "AlertsActivityPrefs";
    private static final String KEY_LAST_VISIT_TIME = "last_visit_time";
    private static final String PERMISSION_PREFS = "permission_requests";
    private static final String KEY_LOCATION_PERMISSION_REQUESTED = "location_permission_requested";
    private static final String KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested";
    
    // Swipe to call variables
    private float initialX = 0f;
    private float initialTouchX = 0f;
    private boolean isSwiping = false;
    private static final float SWIPE_THRESHOLD = 0.7f; // 70% of the width
    
    // Image loading for OpenWeatherMap icons
    private ExecutorService imageExecutor;

    private TextView userNameText;
    private TextView locationText;
    private LinearLayout callButton;
    private ImageView phoneIcon;
    private CardView posterButton;
    private ImageView helpButton;
    private ImageView profileButton;
    private LinearLayout emergencyContactsLayout;
    private LineChart reportChart;
    private TextView reportFilterText;
    
    // Statistics views
    private TextView totalReportsCount;
    private TextView barangayName;
    private TextView barangayReportsCount;
    private TextView myReportsCount;
    
    // Pie chart and top barangay views
    private PieChart reportTypePieChart;
    private TextView topBarangay1Name;
    private TextView topBarangay1Count;
    private TextView topBarangay2Name;
    private TextView topBarangay2Count;
    private TextView topBarangay3Name;
    private TextView topBarangay3Count;
    
    // Weather and Time views
    private TextView temperatureText;
    private TextView dateText;
    private ImageView weatherIcon;
    private TextView weatherDescriptionText;
    private TextView humidityText;
    private TextView windText;
    private TextView precipitationText;
    private TextView weatherLocationText;
    
    // Timer for real-time updates
    private Timer timeUpdateTimer;
    private Handler mainHandler;
    
    // Weather API
    private WeatherManager weatherManager;
    
    // 5-Day Forecast Views
    private LinearLayout forecastDay0, forecastDay1, forecastDay2, forecastDay3, forecastDay4, forecastDay5;
    private TextView forecastDay0Name, forecastDay1Name, forecastDay2Name, forecastDay3Name, forecastDay4Name, forecastDay5Name;
    private ImageView forecastDay0Icon, forecastDay1Icon, forecastDay2Icon, forecastDay3Icon, forecastDay4Icon, forecastDay5Icon;
    private TextView forecastDay0Temp, forecastDay1Temp, forecastDay2Temp, forecastDay3Temp, forecastDay4Temp, forecastDay5Temp;
    private TextView forecastDay0Description, forecastDay1Description, forecastDay2Description, forecastDay3Description, forecastDay4Description, forecastDay5Description;
    private CardView roadSafetyCard;
    private CardView fireSafetyCard;
    private CardView landslideSafetyCard;
    private CardView earthquakeSafetyCard;
    private CardView floodSafetyCard;
    private CardView volcanicSafetyCard;
    private CardView civilDisturbanceCard;
    private CardView armedConflictCard;
    private CardView infectiousDiseaseCard;

    // Bottom navigation
    private LinearLayout homeTab;
    private LinearLayout chatTab;
    private LinearLayout reportTab;
    private LinearLayout mapTab;
    private LinearLayout alertsTab;
    private TextView alertsBadgeDashboard;
    private SharedPreferences sharedPreferences;

    private String currentReportFilter = "Per Barangay";
    
    // ActivityResultLauncher for handling profile updates
    private ActivityResultLauncher<Intent> profileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d(TAG, "Starting MainDashboard onCreate");
            setContentView(R.layout.activity_dashboard);
            Log.d(TAG, "✅ Layout loaded successfully");
            
            // Initialize handler for UI updates
            mainHandler = new Handler(Looper.getMainLooper());
            Log.d(TAG, "✅ Handler initialized");
            
            // Initialize image executor for loading OpenWeatherMap icons
            imageExecutor = Executors.newFixedThreadPool(3);
            Log.d(TAG, "✅ Image executor initialized");
            
            // Initialize SharedPreferences for badge
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Log.d(TAG, "✅ SharedPreferences initialized");
            
            // Initialize Weather Manager
            try {
                weatherManager = new WeatherManager(this);
                Log.d(TAG, "✅ WeatherManager initialized");
                
                // Test API key validity
                weatherManager.testApiKey(new WeatherManager.TestCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "✅ " + message);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "❌ " + errorMessage);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "⚠️ WeatherManager initialization failed (non-critical): " + e.getMessage(), e);
                // Continue without weather manager - will use fallback simulation
            }
            
            try {
                initializeViews();
                Log.d(TAG, "✅ Views initialized");
            } catch (Exception e) {
                Log.e(TAG, "❌ CRITICAL: initializeViews failed: " + e.getMessage(), e);
                throw e; // Re-throw critical error
            }
            
            try {
                setupUserInfo();
                Log.d(TAG, "✅ User info setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupUserInfo failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                setupReportFilter();
                Log.d(TAG, "✅ Report filter setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupReportFilter failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                setupChart();
                Log.d(TAG, "✅ Chart setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupChart failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                updateStatisticsCards();
                Log.d(TAG, "✅ Statistics cards setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ updateStatisticsCards failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                setupPieChart();
                Log.d(TAG, "✅ Pie chart setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupPieChart failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                updateTopBarangayStatistics();
                Log.d(TAG, "✅ Top barangay statistics setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ updateTopBarangayStatistics failed (non-critical): " + e.getMessage(), e);
            }
            
            try {
                setupClickListeners();
                Log.d(TAG, "✅ Click listeners setup");
            } catch (Exception e) {
                Log.e(TAG, "❌ CRITICAL: setupClickListeners failed: " + e.getMessage(), e);
                throw e; // Re-throw critical error
            }
            
            try {
                setupBottomNavigation();
                Log.d(TAG, "✅ Bottom navigation setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupBottomNavigation failed (non-critical): " + e.getMessage(), e);
            }
            
            // Load current user's custom emergency contacts
            try {
                loadCustomEmergencyContacts();
                Log.d(TAG, "✅ User custom emergency contacts load initiated");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ loadCustomEmergencyContacts failed (non-critical): " + e.getMessage(), e);
            }

            try {
                setupProfileLauncher();
                Log.d(TAG, "✅ Profile launcher setup");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ setupProfileLauncher failed (non-critical): " + e.getMessage(), e);
            }
            
            // Load user profile picture
            try {
                loadUserProfilePicture();
                Log.d(TAG, "✅ Profile picture loading started");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ loadUserProfilePicture failed (non-critical): " + e.getMessage(), e);
            }
            
            // Start real-time updates
            try {
                startRealTimeUpdates();
                Log.d(TAG, "✅ Real-time updates started");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ startRealTimeUpdates failed (non-critical): " + e.getMessage(), e);
            }
            
            // Update notification badge
            try {
                updateNotificationBadge();
                Log.d(TAG, "✅ Notification badge updated");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ updateNotificationBadge failed (non-critical): " + e.getMessage(), e);
            }
            
            // Initialize FCM token for push notifications (in case it wasn't done at login)
            try {
                initializeFCMToken();
                Log.d(TAG, "✅ FCM token initialization started");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ initializeFCMToken failed (non-critical): " + e.getMessage(), e);
            }
            
            // Request location and notification permissions
            try {
                requestEssentialPermissions();
                Log.d(TAG, "✅ Permission requests initiated");
            } catch (Exception e) {
                Log.e(TAG, "⚠️ requestEssentialPermissions failed (non-critical): " + e.getMessage(), e);
            }
            
            Log.d(TAG, "🎉 MainDashboard onCreate completed successfully!");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR in onCreate: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // Don't finish - let user see the error and try to recover
            // finish();
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
            phoneIcon = findViewById(R.id.phoneIcon);
            emergencyContactsLayout = findViewById(R.id.emergencyContactsLayout);
            reportChart = findViewById(R.id.reportChart);
            reportFilterText = findViewById(R.id.reportFilterText);
            
            // Statistics views
            totalReportsCount = findViewById(R.id.totalReportsCount);
            barangayName = findViewById(R.id.barangayName);
            barangayReportsCount = findViewById(R.id.barangayReportsCount);
            myReportsCount = findViewById(R.id.myReportsCount);
            
            // Pie chart and top barangay views
            reportTypePieChart = findViewById(R.id.reportTypePieChart);
            topBarangay1Name = findViewById(R.id.topBarangay1Name);
            topBarangay1Count = findViewById(R.id.topBarangay1Count);
            topBarangay2Name = findViewById(R.id.topBarangay2Name);
            topBarangay2Count = findViewById(R.id.topBarangay2Count);
            topBarangay3Name = findViewById(R.id.topBarangay3Name);
            topBarangay3Count = findViewById(R.id.topBarangay3Count);
            
            // Weather and Time views
            temperatureText = findViewById(R.id.temperatureText);
            dateText = findViewById(R.id.dateText);
            weatherIcon = findViewById(R.id.weatherIcon);
            weatherDescriptionText = findViewById(R.id.weatherDescriptionText);
            humidityText = findViewById(R.id.humidityText);
            windText = findViewById(R.id.windText);
            precipitationText = findViewById(R.id.precipitationText);
            weatherLocationText = findViewById(R.id.weatherLocationText);
        
        // Initialize 5-Day Forecast Views
        initializeForecastViews();

            // Safety cards
            roadSafetyCard = findViewById(R.id.roadSafetyCard);
            fireSafetyCard = findViewById(R.id.fireSafetyCard);
            landslideSafetyCard = findViewById(R.id.landslideSafetyCard);
            earthquakeSafetyCard = findViewById(R.id.earthquakeSafetyCard);
            floodSafetyCard = findViewById(R.id.floodSafetyCard);
            volcanicSafetyCard = findViewById(R.id.volcanicSafetyCard);
            civilDisturbanceCard = findViewById(R.id.civilDisturbanceCard);
            armedConflictCard = findViewById(R.id.armedConflictCard);
            infectiousDiseaseCard = findViewById(R.id.infectiousDiseaseCard);

            // Bottom navigation
            homeTab = findViewById(R.id.homeTab);
            chatTab = findViewById(R.id.chatTab);
            reportTab = findViewById(R.id.reportTab);
            mapTab = findViewById(R.id.mapTab);
            alertsTab = findViewById(R.id.alertsTab);
            alertsBadgeDashboard = findViewById(R.id.alerts_badge_dashboard);
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
            Log.d(TAG, "Setting up user info...");
            
            TextView welcomeText = findViewById(R.id.welcomeText);
            
            // ✅ FIXED: Load full name directly from SharedPreferences
            String fullName = getSavedFullName();
            if (welcomeText != null && fullName != null && !fullName.isEmpty()) {
                welcomeText.setText("Hello, " + fullName);
                Log.d(TAG, "Updated welcome text with: " + fullName);
            } else {
                // Fallback to ProfileDataManager if needed
                ProfileDataManager profileManager = ProfileDataManager.getInstance(this);
                String fallbackName = profileManager.getFullName();
                if (welcomeText != null && fallbackName != null && !fallbackName.isEmpty()) {
                    welcomeText.setText("Hello, " + fallbackName);
                    Log.d(TAG, "Updated welcome text with fallback name: " + fallbackName);
                } else {
                    Log.w(TAG, "No name data available for welcome text");
                }
            }
            
            // ✅ ENHANCED: Load location with better error handling and logging
            String location = getSavedBarangay();
            Log.d(TAG, "Retrieved location: '" + location + "'");
            
            if (locationText != null) {
                if (location != null && !location.isEmpty()) {
                    locationText.setText(location);
                    Log.d(TAG, "✅ Updated location text with: " + location);
                } else {
                    Log.w(TAG, "No location data available, showing placeholder");
                    locationText.setText("Location not set");
                    
                    // Try to load from Firestore as fallback
                    Log.d(TAG, "Attempting to load location from Firestore as fallback");
                    loadLocationFromFirestore();
                }
            } else {
                Log.e(TAG, "locationText view is null!");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up user info: " + e.getMessage(), e);
        }
    }
    
    private void startRealTimeUpdates() {
        try {
            // Update immediately
            updateTimeAndDate();
            updateWeather();
            updateForecast(); // Add immediate forecast update
            
            // Set up timer to update every minute
            timeUpdateTimer = new Timer();
            timeUpdateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mainHandler.post(() -> {
                        updateTimeAndDate();
                        
                        // Update weather every 10 minutes
                        if (System.currentTimeMillis() % 600000 < 60000) {
                            updateWeather();
                        }
                        
                        // Update 5-day forecast every 30 minutes for real-time data
                        if (System.currentTimeMillis() % 1800000 < 60000) {
                            updateForecast();
                        }
                    });
                }
            }, 0, 60000); // Update every minute
            
            // Additional timer for more frequent forecast updates (every 15 minutes)
            Timer forecastTimer = new Timer();
            forecastTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mainHandler.post(() -> {
                        Log.d(TAG, "Scheduled forecast update - fetching latest data");
                        updateForecast();
                    });
                }
            }, 900000, 900000); // Start after 15 minutes, then every 15 minutes
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting real-time updates: " + e.getMessage(), e);
        }
    }
    
    private void updateTimeAndDate() {
        try {
            if (dateText == null) return;
            
            Calendar calendar = Calendar.getInstance();
            
            // Update date in format "Monday, 20 Oct"
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
            String currentDate = dateFormat.format(calendar.getTime());
            dateText.setText(currentDate);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating time and date: " + e.getMessage(), e);
        }
    }
    
    private void initializeForecastViews() {
        try {
            // Initialize forecast day containers
            forecastDay0 = findViewById(R.id.forecastDay0);
            forecastDay1 = findViewById(R.id.forecastDay1);
            forecastDay2 = findViewById(R.id.forecastDay2);
            forecastDay3 = findViewById(R.id.forecastDay3);
            forecastDay4 = findViewById(R.id.forecastDay4);
            forecastDay5 = findViewById(R.id.forecastDay5);
            
            // Initialize forecast day names
            forecastDay0Name = findViewById(R.id.forecastDay0Name);
            forecastDay1Name = findViewById(R.id.forecastDay1Name);
            forecastDay2Name = findViewById(R.id.forecastDay2Name);
            forecastDay3Name = findViewById(R.id.forecastDay3Name);
            forecastDay4Name = findViewById(R.id.forecastDay4Name);
            forecastDay5Name = findViewById(R.id.forecastDay5Name);
            
            // Initialize forecast day icons
            forecastDay0Icon = findViewById(R.id.forecastDay0Icon);
            forecastDay1Icon = findViewById(R.id.forecastDay1Icon);
            forecastDay2Icon = findViewById(R.id.forecastDay2Icon);
            forecastDay3Icon = findViewById(R.id.forecastDay3Icon);
            forecastDay4Icon = findViewById(R.id.forecastDay4Icon);
            forecastDay5Icon = findViewById(R.id.forecastDay5Icon);
            
            // Initialize forecast day temperatures
            forecastDay0Temp = findViewById(R.id.forecastDay0Temp);
            forecastDay1Temp = findViewById(R.id.forecastDay1Temp);
            forecastDay2Temp = findViewById(R.id.forecastDay2Temp);
            forecastDay3Temp = findViewById(R.id.forecastDay3Temp);
            forecastDay4Temp = findViewById(R.id.forecastDay4Temp);
            forecastDay5Temp = findViewById(R.id.forecastDay5Temp);
            
            // Initialize forecast day descriptions
            forecastDay0Description = findViewById(R.id.forecastDay0Description);
            forecastDay1Description = findViewById(R.id.forecastDay1Description);
            forecastDay2Description = findViewById(R.id.forecastDay2Description);
            forecastDay3Description = findViewById(R.id.forecastDay3Description);
            forecastDay4Description = findViewById(R.id.forecastDay4Description);
            forecastDay5Description = findViewById(R.id.forecastDay5Description);
            
            Log.d(TAG, "Forecast views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing forecast views: " + e.getMessage(), e);
        }
    }
    
    private void updateWeather() {
        try {
            Log.d(TAG, "Updating weather data...");
            // Use real weather API for Lucban, Quezon
            fetchRealWeatherData();
        } catch (Exception e) {
            Log.e(TAG, "Error updating weather: " + e.getMessage(), e);
            // Fallback to simulation if API fails
            simulateWeatherUpdate();
        }
    }
    
    /**
     * Updates the 5-day forecast with real-time data
     * This method is called periodically to ensure forecast data is always current
     */
    private void updateForecast() {
        try {
            Log.d(TAG, "Updating 5-day forecast with real-time data...");
            
            // Use real weather API for Lucban, Quezon forecast
            fetchForecastData();
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating forecast: " + e.getMessage(), e);
            // Fallback to simulation if API fails
            simulateForecastUpdate();
        }
    }
    
    private void fetchRealWeatherData() {
        try {
            if (weatherManager == null) {
                Log.e(TAG, "WeatherManager is null, falling back to simulation");
                simulateWeatherUpdate();
                return;
            }
            
            Log.d(TAG, "🌤️ Fetching real weather data specifically for Lucban, Quezon, Philippines");
            
            weatherManager.getLucbanWeather(new WeatherManager.WeatherCallback() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    Log.d(TAG, "Weather data received successfully");
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        try {
                            updateWeatherUI(weatherData);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating weather UI: " + e.getMessage(), e);
                            simulateWeatherUpdate(); // Fallback to simulation
                        }
                    });
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Weather API error: " + errorMessage);
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        Log.d(TAG, "Falling back to simulated weather data");
                        simulateWeatherUpdate(); // Fallback to simulation
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching real weather data: " + e.getMessage(), e);
            simulateWeatherUpdate(); // Fallback to simulation
        }
    }
    
    private void updateWeatherUI(WeatherData weatherData) {
        try {
            if (temperatureText == null || weatherIcon == null || weatherDescriptionText == null ||
                humidityText == null || windText == null || precipitationText == null || weatherLocationText == null) {
                Log.w(TAG, "Some weather views are null, skipping UI update");
                return;
            }
            
            // Update temperature
            if (weatherData.getMain() != null) {
                double temp = weatherData.getMain().getTemp();
                temperatureText.setText(WeatherManager.formatTemperature(temp));
                
                // Update humidity
                int humidity = weatherData.getMain().getHumidity();
                humidityText.setText(WeatherManager.formatHumidity(humidity));
                
                // Calculate precipitation
                double pressure = weatherData.getMain().getPressure();
                String precipitation = WeatherManager.calculatePrecipitation(humidity, pressure);
                precipitationText.setText(precipitation);
            }
            
            // Update wind
            if (weatherData.getWind() != null) {
                double windSpeed = weatherData.getWind().getSpeed();
                windText.setText(WeatherManager.formatWindSpeed(windSpeed));
            }
            
            // Update weather icon using official OpenWeatherMap icons
            if (weatherData.getWeather() != null && weatherData.getWeather().length > 0) {
                String iconCode = weatherData.getWeather()[0].getIcon();
                int weatherId = weatherData.getWeather()[0].getId();
                String weatherDescription = WeatherManager.getWeatherDescription(weatherId);
                
                // Load official OpenWeatherMap icon from URL
                loadWeatherIconFromUrl(weatherIcon, iconCode);
                
                // Update weather description text
                if (weatherDescriptionText != null) {
                    weatherDescriptionText.setText(weatherDescription);
                }
                
                Log.d(TAG, "🌤️ Lucban Weather: " + weatherDescription + 
                          " | Icon: " + iconCode + " | ID: " + weatherId + 
                          " | Temperature: " + weatherData.getMain().getTemp() + "°C");
            }
            
            // Update location (always show Lucban, Quezon)
            weatherLocationText.setText("Lucban, Quezon");
            
            Log.d(TAG, "Weather UI updated successfully with real data");
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating weather UI: " + e.getMessage(), e);
            throw e; // Re-throw to trigger fallback
        }
    }
    
    private void simulateWeatherUpdate() {
        try {
            if (temperatureText == null || weatherIcon == null || weatherDescriptionText == null ||
                humidityText == null || windText == null || precipitationText == null || weatherLocationText == null) return;
            
            // Simulate different weather conditions with official OpenWeatherMap icon codes
            String[] conditions = {"Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Stormy"};
            String[] temperatures = {"28°", "25°", "22°", "30°", "26°"};
            String[] humidityValues = {"65%", "82%", "95%", "70%", "88%"};
            String[] windValues = {"5 km/h", "9 km/h", "15 km/h", "3 km/h", "12 km/h"};
            String[] precipitationValues = {"0%", "5%", "85%", "10%", "60%"};
            String[] iconCodes = {
                "01d",  // Clear sky day (Sunny)
                "04d",  // Broken clouds day (Cloudy)
                "10d",  // Rain day (Rainy)
                "02d",  // Few clouds day (Partly Cloudy)
                "11d"   // Thunderstorm day (Stormy)
            };
            
            // Get random weather data (use time-based seed for more realistic changes)
            int randomIndex = (int) (System.currentTimeMillis() / 60000) % conditions.length; // Change every minute
            
            String condition = conditions[randomIndex];
            String temperature = temperatures[randomIndex];
            String humidity = humidityValues[randomIndex];
            String wind = windValues[randomIndex];
            String precipitation = precipitationValues[randomIndex];
            String iconCode = iconCodes[randomIndex];
            
            // Update UI
            temperatureText.setText(temperature);
            humidityText.setText(humidity);
            windText.setText(wind);
            precipitationText.setText(precipitation);
            
            // Load official OpenWeatherMap icon from URL
            loadWeatherIconFromUrl(weatherIcon, iconCode);
            
            // Update weather description text
            if (weatherDescriptionText != null) {
                weatherDescriptionText.setText(condition);
            }
            
            // Update weather location to Lucban, Quezon
            if (weatherLocationText != null) {
                weatherLocationText.setText("Lucban, Quezon");
            }
            
            Log.d(TAG, "Weather updated (simulation): " + temperature + ", " + condition + ", Humidity: " + humidity + ", Wind: " + wind + ", Precipitation: " + precipitation);
        } catch (Exception e) {
            Log.e(TAG, "Error simulating weather update: " + e.getMessage(), e);
        }
    }
    
    private void fetchForecastData() {
        try {
            if (weatherManager == null) {
                Log.e(TAG, "WeatherManager is null, falling back to simulation");
                simulateForecastUpdate();
                return;
            }
            
            Log.d(TAG, "🔄 Fetching real-time 5-day forecast data specifically for Lucban, Quezon, Philippines");
            Log.d(TAG, "🌤️ API Key: " + WeatherManager.API_KEY.substring(0, 8) + "...");
            Log.d(TAG, "📍 Location: Lucban, Quezon, Philippines");
            
            // Add timestamp to track when forecast was last updated
            long currentTime = System.currentTimeMillis();
            Log.d(TAG, "Forecast fetch initiated at: " + new java.util.Date(currentTime));
            
            weatherManager.getLucbanForecast(new WeatherManager.ForecastCallback() {
                @Override
                public void onSuccess(ForecastData forecastData) {
                    Log.d(TAG, "✅ Real-time forecast data received successfully!");
                    Log.d(TAG, "📊 Forecast data: " + (forecastData != null ? "Valid" : "Null"));
                    if (forecastData != null && forecastData.getList() != null) {
                        Log.d(TAG, "📈 Number of forecast items: " + forecastData.getList().size());
                    }
                    
                    // Save forecast update timestamp
                    saveForecastUpdateTimestamp();
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        try {
                            updateForecastUI(forecastData);
                            Log.d(TAG, "✅ Real-time forecast UI updated successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating forecast UI: " + e.getMessage(), e);
                            simulateForecastUpdate(); // Fallback to simulation
                        }
                    });
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "❌ Forecast API error: " + errorMessage);
                    Log.e(TAG, "🔍 This might be due to:");
                    Log.e(TAG, "   - Invalid API key");
                    Log.e(TAG, "   - Network connectivity issues");
                    Log.e(TAG, "   - API rate limiting");
                    Log.e(TAG, "   - Location not found");
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        Log.d(TAG, "Falling back to simulated forecast data");
                        simulateForecastUpdate(); // Fallback to simulation
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching forecast data: " + e.getMessage(), e);
            simulateForecastUpdate(); // Fallback to simulation
        }
    }
    
    /**
     * Saves the timestamp when forecast was last updated
     * This helps track how fresh the forecast data is
     */
    private void saveForecastUpdateTimestamp() {
        try {
            SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("last_forecast_update", System.currentTimeMillis());
            editor.apply();
            Log.d(TAG, "Forecast update timestamp saved");
        } catch (Exception e) {
            Log.e(TAG, "Error saving forecast timestamp: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the time since last forecast update
     * Returns formatted string showing how fresh the data is
     */
    private String getForecastAge() {
        try {
            SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
            long lastUpdate = prefs.getLong("last_forecast_update", 0);
            
            if (lastUpdate == 0) {
                return "Data not available";
            }
            
            long timeDiff = System.currentTimeMillis() - lastUpdate;
            long minutes = timeDiff / (1000 * 60);
            
            if (minutes < 1) {
                return "Just updated";
            } else if (minutes < 60) {
                return minutes + " min ago";
            } else {
                long hours = minutes / 60;
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting forecast age: " + e.getMessage(), e);
            return "Unknown";
        }
    }
    
    private void updateForecastUI(ForecastData forecastData) {
        try {
            Log.d(TAG, "🔄 Updating forecast UI with real-time data...");
            
            WeatherManager.DailyForecast[] dailyForecasts = WeatherManager.processForecastData(forecastData);
            
            if (dailyForecasts.length == 0) {
                Log.w(TAG, "No forecast data available, using simulation");
                simulateForecastUpdate();
                return;
            }
            
            Log.d(TAG, "Processing " + dailyForecasts.length + " forecast days");
            
            // Update each forecast day (limit to 6 days: today + 5 days)
            int maxDays = Math.min(dailyForecasts.length, 6);
            TextView[] dayNames = {forecastDay0Name, forecastDay1Name, forecastDay2Name, forecastDay3Name, forecastDay4Name, forecastDay5Name};
            ImageView[] dayIcons = {forecastDay0Icon, forecastDay1Icon, forecastDay2Icon, forecastDay3Icon, forecastDay4Icon, forecastDay5Icon};
            TextView[] dayTemps = {forecastDay0Temp, forecastDay1Temp, forecastDay2Temp, forecastDay3Temp, forecastDay4Temp, forecastDay5Temp};
            TextView[] dayDescriptions = {forecastDay0Description, forecastDay1Description, forecastDay2Description, forecastDay3Description, forecastDay4Description, forecastDay5Description};
            
            // Generate real-time dates for the forecast
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            String[] realTimeDayNames = new String[6];
            
            for (int i = 0; i < 6; i++) {
                calendar.add(java.util.Calendar.DAY_OF_MONTH, i);
                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEE d", java.util.Locale.getDefault());
                realTimeDayNames[i] = i == 0 ? "Today" : dayFormat.format(calendar.getTime());
                calendar.add(java.util.Calendar.DAY_OF_MONTH, -i); // Reset calendar
            }
            
            for (int i = 0; i < maxDays; i++) {
                WeatherManager.DailyForecast daily = dailyForecasts[i];
                
                Log.d(TAG, "Updating day " + i + ": " + daily.timestamp + " - " + daily.maxTemp + "°C/" + daily.minTemp + "°C");
                
                // Update day name with real-time date
                if (dayNames[i] != null) {
                    String dayName = realTimeDayNames[i];
                    dayNames[i].setText(dayName);
                    Log.d(TAG, "Day " + i + " name set to: " + dayName);
                }
                
                // Update weather icon using official OpenWeatherMap icons
                if (dayIcons[i] != null) {
                    // Load official OpenWeatherMap icon from URL
                    loadWeatherIconFromUrl(dayIcons[i], daily.icon);
                    Log.d(TAG, "Day " + i + " loading OpenWeatherMap icon: " + daily.icon);
                }
                
                // Update temperature range
                if (dayTemps[i] != null) {
                    String tempRange = WeatherManager.formatTemperatureRange(daily.maxTemp, daily.minTemp);
                    dayTemps[i].setText(tempRange);
                    Log.d(TAG, "Day " + i + " temperature set to: " + tempRange);
                }
                
                // Update weather description
                if (dayDescriptions[i] != null) {
                    // Get weather description from icon code
                    String description = getDescriptionFromIconCode(daily.icon);
                    dayDescriptions[i].setText(description);
                    Log.d(TAG, "Day " + i + " description set to: " + description);
                }
            }
            
            // Log forecast age for debugging
            String forecastAge = getForecastAge();
            Log.d(TAG, "✅ Real-time forecast UI updated successfully. Data age: " + forecastAge);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating forecast UI: " + e.getMessage(), e);
            throw e; // Re-throw to trigger fallback
        }
    }
    
    private void simulateForecastUpdate() {
        try {
            Log.d(TAG, "🔄 Updating forecast with simulated data (fallback mode)");
            
            // Generate more realistic simulated data based on current time
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            String[] dayNames = new String[6];
            String[] tempRanges = new String[6];
            int[] weatherIcons = new int[6];
            
            // Generate day names based on current date
            for (int i = 0; i < 6; i++) {
                calendar.add(java.util.Calendar.DAY_OF_MONTH, i);
                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEE d", java.util.Locale.getDefault());
                dayNames[i] = i == 0 ? "Today" : dayFormat.format(calendar.getTime());
                calendar.add(java.util.Calendar.DAY_OF_MONTH, -i); // Reset calendar
            }
            
            // Generate realistic temperature ranges with OpenWeatherMap icon codes
            String[] baseTemps = {"30° 24°", "29° 23°", "31° 25°", "28° 22°", "32° 26°", "27° 21°"};
            String[] baseIconCodes = {
                "02d", // Few clouds day (Partly Cloudy)
                "01d", // Clear sky day (Sunny)
                "02d", // Few clouds day (Partly Cloudy)
                "10d", // Rain day (Rainy)
                "01d", // Clear sky day (Sunny)
                "10d"  // Rain day (Rainy)
            };
            
            // Add some randomness to make it more realistic
            for (int i = 0; i < 6; i++) {
                tempRanges[i] = baseTemps[i];
            }
            
            TextView[] dayNameViews = {forecastDay0Name, forecastDay1Name, forecastDay2Name, forecastDay3Name, forecastDay4Name, forecastDay5Name};
            ImageView[] dayIconViews = {forecastDay0Icon, forecastDay1Icon, forecastDay2Icon, forecastDay3Icon, forecastDay4Icon, forecastDay5Icon};
            TextView[] dayTempViews = {forecastDay0Temp, forecastDay1Temp, forecastDay2Temp, forecastDay3Temp, forecastDay4Temp, forecastDay5Temp};
            TextView[] dayDescriptionViews = {forecastDay0Description, forecastDay1Description, forecastDay2Description, forecastDay3Description, forecastDay4Description, forecastDay5Description};
            
            String[] baseDescriptions = {"Partly Cloudy", "Sunny", "Partly Cloudy", "Rainy", "Sunny", "Rainy"};
            
            for (int i = 0; i < 6; i++) {
                if (dayNameViews[i] != null) {
                    dayNameViews[i].setText(dayNames[i]);
                }
                if (dayIconViews[i] != null) {
                    // Load official OpenWeatherMap icon from URL
                    loadWeatherIconFromUrl(dayIconViews[i], baseIconCodes[i]);
                }
                if (dayTempViews[i] != null) {
                    dayTempViews[i].setText(tempRanges[i]);
                }
                if (dayDescriptionViews[i] != null && i < baseDescriptions.length) {
                    dayDescriptionViews[i].setText(baseDescriptions[i]);
                }
            }
            
            // Save simulation timestamp
            saveForecastUpdateTimestamp();
            
            Log.d(TAG, "✅ Forecast updated (simulation) - " + getForecastAge());
        } catch (Exception e) {
            Log.e(TAG, "Error simulating forecast update: " + e.getMessage(), e);
        }
    }
    
    /**
     * Manually refreshes the 5-day forecast
     * This can be called when user wants to force an update
     */
    public void refreshForecast() {
        try {
            Log.d(TAG, "🔄 Manual forecast refresh requested");
            updateForecast();
        } catch (Exception e) {
            Log.e(TAG, "Error in manual forecast refresh: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if forecast data is stale and needs updating
     * Returns true if data is older than 30 minutes
     */
    private boolean isForecastDataStale() {
        try {
            SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
            long lastUpdate = prefs.getLong("last_forecast_update", 0);
            
            if (lastUpdate == 0) {
                return true; // No data available
            }
            
            long timeDiff = System.currentTimeMillis() - lastUpdate;
            long minutes = timeDiff / (1000 * 60);
            
            return minutes > 30; // Consider stale if older than 30 minutes
        } catch (Exception e) {
            Log.e(TAG, "Error checking forecast staleness: " + e.getMessage(), e);
            return true; // Assume stale if error
        }
    }
    
    private void setupWeatherTimeBackground() {
        try {
            LinearLayout weatherTimeCard = findViewById(R.id.weatherTimeCard);
            if (weatherTimeCard != null) {
                Log.d(TAG, "Setting up weather time background...");
                
                // Try multiple approaches to ensure the gradient works
                
                // Approach 1: Programmatic gradient
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                gradientDrawable.setColors(new int[]{
                    Color.parseColor("#FF6B35"),
                    Color.parseColor("#F7931E"),
                    Color.parseColor("#FF5722")
                });
                gradientDrawable.setCornerRadius(48f); // 12dp * 4 (density)
                
                // Set the background
                weatherTimeCard.setBackground(gradientDrawable);
                
                // Force refresh
                weatherTimeCard.invalidate();
                
                Log.d(TAG, "Weather time background set programmatically with gradient");
                
                // Also try setting a solid color as fallback
                weatherTimeCard.post(() -> {
                    try {
                        weatherTimeCard.setBackgroundColor(Color.parseColor("#FF6B35"));
                        Log.d(TAG, "Fallback solid color set");
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting fallback color: " + e.getMessage(), e);
                    }
                });
                
            } else {
                Log.e(TAG, "Weather time card not found!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up weather time background: " + e.getMessage(), e);
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
            Log.d(TAG, "Getting saved barangay/location data...");
            
            // Attempt to read location saved by AddressInfo
            SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);

            // Priority 1: Check for pre-formatted location_text first
            String preFormattedLocation = prefs.getString("location_text", "");
            Log.d(TAG, "Pre-formatted location_text: '" + preFormattedLocation + "'");
            
            if (!preFormattedLocation.isEmpty() && preFormattedLocation.contains(",")) {
                // Already formatted as "City, Barangay"
                Log.d(TAG, "✅ Using pre-formatted location_text: " + preFormattedLocation);
                return preFormattedLocation;
            }
            
            // Priority 2: Get city/town and barangay to construct full location
            String cityTown = prefs.getString("city", "");
            if (cityTown.isEmpty()) {
                cityTown = prefs.getString("cityTown", "");
            }
            
            String barangay = prefs.getString("barangay", "");
            
            Log.d(TAG, "📍 Reading location data - City: '" + cityTown + "', Barangay: '" + barangay + "'");

            // ✅ ENHANCED: Construct full location display (City, Barangay)
            if (!cityTown.isEmpty() && !barangay.isEmpty()) {
                String fullLocation = cityTown + ", " + barangay;
                Log.d(TAG, "✅ Constructed full location: " + fullLocation);
                
                // Save this constructed location for future use
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("location_text", fullLocation);
                editor.apply();
                
                return fullLocation;
            } else if (!barangay.isEmpty()) {
                // Fallback to just barangay if city not available
                Log.d(TAG, "⚠️ Using barangay only: " + barangay);
                return barangay;
            } else if (!cityTown.isEmpty()) {
                // Fallback to just city if barangay not available
                Log.d(TAG, "⚠️ Using city only: " + cityTown);
                return cityTown;
            } else if (!preFormattedLocation.isEmpty()) {
                // Use location_text even if it doesn't have comma
                Log.d(TAG, "⚠️ Using location_text as fallback: " + preFormattedLocation);
                return preFormattedLocation;
            }

            // Priority 3: Alternative keys for compatibility
            String selectedBarangay = prefs.getString("selected_barangay", "");
            String otherBarangay = prefs.getString("barangay_other", "");

            String value = "";
            if (selectedBarangay != null && !selectedBarangay.isEmpty() &&
                    !"Other".equalsIgnoreCase(selectedBarangay) &&
                    !"Choose a barangay".equalsIgnoreCase(selectedBarangay)) {
                value = selectedBarangay;
            } else if (otherBarangay != null && !otherBarangay.isEmpty()) {
                value = otherBarangay;
            }

            // Optional prefix formatting if needed
            if (value != null && !value.isEmpty()) {
                if (!value.toLowerCase().startsWith("brgy") && !value.toLowerCase().startsWith("barangay")) {
                    value = "Brgy. " + value;
                }
                Log.d(TAG, "Using fallback location: " + value);
                return value;
            }
            
            Log.w(TAG, "No location data found in SharedPreferences");
            return "";
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting saved barangay: " + e.getMessage(), e);
            return "";
        }
    }
    
    private void loadLocationFromFirestore() {
        try {
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
                            
                            // ✅ NEW: Get city/town and barangay to construct full location
                            String cityTown = doc.getString("cityTown");
                            if (cityTown == null || cityTown.isEmpty()) {
                                cityTown = doc.getString("city");
                            }
                            
                            String barangay = doc.getString("barangay");
                            if (barangay == null || barangay.isEmpty()) {
                                barangay = doc.getString("location");
                            }
                            
                            String fullLocation = "";
                            if (cityTown != null && !cityTown.isEmpty() && barangay != null && !barangay.isEmpty()) {
                                fullLocation = cityTown + ", " + barangay;
                            } else if (barangay != null && !barangay.isEmpty()) {
                                fullLocation = barangay;
                            } else if (cityTown != null && !cityTown.isEmpty()) {
                                fullLocation = cityTown;
                            }
                            
                            if (!fullLocation.isEmpty()) {
                                // Update UI
                                if (locationText != null) {
                                    locationText.setText(fullLocation);
                                }
                                
                                // Save to SharedPreferences for offline access
                                SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("location_text", fullLocation);
                                if (cityTown != null) editor.putString("city", cityTown);
                                if (cityTown != null) editor.putString("cityTown", cityTown);
                                if (barangay != null) editor.putString("barangay", barangay);
                                editor.apply();
                                
                                Log.d(TAG, "Loaded full location from Firestore: " + fullLocation);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading location from Firestore", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadLocationFromFirestore: " + e.getMessage(), e);
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

    /**
     * Update statistics cards with report data
     * Shows total reports and barangay-specific reports
     */
    private void updateStatisticsCards() {
        try {
            Log.d(TAG, "📊 Updating statistics cards...");
            
            // Get user's barangay from profile
            String userBarangay = getUserBarangay();
            
            // Update barangay name (without "Brgy." prefix)
            if (barangayName != null) {
                String barangayDisplay = userBarangay != null && !userBarangay.isEmpty() 
                    ? userBarangay 
                    : "Kulapi"; // Default fallback
                barangayName.setText(barangayDisplay);
                Log.d(TAG, "Barangay name set to: " + barangayDisplay);
            }
            
            // Fetch and update statistics from Firestore
            fetchReportStatistics(userBarangay);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating statistics cards: " + e.getMessage(), e);
            // Set default values on error
            setDefaultStatistics();
        }
    }
    
    /**
     * Get user's barangay from SharedPreferences
     */
    private String getUserBarangay() {
        try {
            SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
            return prefs.getString("barangay", "");
        } catch (Exception e) {
            Log.e(TAG, "Error getting user barangay: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Fetch report statistics from Firestore
     */
    private void fetchReportStatistics(String userBarangay) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Fetch total reports count
            db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCount = queryDocumentSnapshots.size();
                    updateTotalReportsCount(totalCount);
                    
                    // Fetch barangay-specific reports if barangay is available
                    if (userBarangay != null && !userBarangay.isEmpty()) {
                        fetchBarangayReportsCount(userBarangay);
                    } else {
                        // Use default barangay
                        fetchBarangayReportsCount("Kulapi");
                    }
                    
                    // Fetch user's personal reports
                    fetchMyReportsCount();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching total reports: " + e.getMessage());
                    setDefaultStatistics();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error fetching report statistics: " + e.getMessage());
            setDefaultStatistics();
        }
    }
    
    /**
     * Fetch barangay-specific report count
     */
    private void fetchBarangayReportsCount(String barangay) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            db.collection("reports")
                .whereEqualTo("barangay", barangay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int barangayCount = queryDocumentSnapshots.size();
                    updateBarangayReportsCount(barangayCount);
                    Log.d(TAG, "Barangay " + barangay + " reports count: " + barangayCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching barangay reports: " + e.getMessage());
                    updateBarangayReportsCount(0);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error fetching barangay reports count: " + e.getMessage());
            updateBarangayReportsCount(0);
        }
    }
    
    /**
     * Update total reports count display
     */
    private void updateTotalReportsCount(int count) {
        if (totalReportsCount != null) {
            totalReportsCount.setText(String.valueOf(count));
            Log.d(TAG, "Total reports count updated to: " + count);
        }
    }
    
    /**
     * Update barangay reports count display
     */
    private void updateBarangayReportsCount(int count) {
        if (barangayReportsCount != null) {
            barangayReportsCount.setText(String.valueOf(count));
            Log.d(TAG, "Barangay reports count updated to: " + count);
        }
    }
    
    /**
     * Fetch user's personal report count
     */
    private void fetchMyReportsCount() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No current user, setting my reports count to 0");
                updateMyReportsCount(0);
                return;
            }
            
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            db.collection("reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int myCount = queryDocumentSnapshots.size();
                    updateMyReportsCount(myCount);
                    Log.d(TAG, "My reports count: " + myCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching my reports: " + e.getMessage());
                    updateMyReportsCount(0);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error fetching my reports count: " + e.getMessage());
            updateMyReportsCount(0);
        }
    }
    
    /**
     * Update my reports count display
     */
    private void updateMyReportsCount(int count) {
        if (myReportsCount != null) {
            myReportsCount.setText(String.valueOf(count));
            Log.d(TAG, "My reports count updated to: " + count);
        }
    }
    
    /**
     * Set default statistics when data fetch fails
     */
    private void setDefaultStatistics() {
        try {
            if (totalReportsCount != null) {
                totalReportsCount.setText("128");
            }
            if (barangayReportsCount != null) {
                barangayReportsCount.setText("2");
            }
            if (myReportsCount != null) {
                myReportsCount.setText("5");
            }
            Log.d(TAG, "Default statistics set");
        } catch (Exception e) {
            Log.e(TAG, "Error setting default statistics: " + e.getMessage());
        }
    }

    /**
     * Setup pie chart for report type statistics
     */
    private void setupPieChart() {
        try {
            if (reportTypePieChart == null) {
                Log.w(TAG, "Report type pie chart is null, skipping pie chart setup");
                return;
            }

            // Sample data for report types
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(35f, "Accident"));
            entries.add(new PieEntry(25f, "Fire"));
            entries.add(new PieEntry(20f, "Medical"));
            entries.add(new PieEntry(15f, "Crime"));
            entries.add(new PieEntry(5f, "Other"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{
                getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark),
                getColorSafe(R.color.red_primary, android.R.color.holo_red_dark),
                getColorSafe(R.color.rhu_green, android.R.color.holo_green_dark),
                getColorSafe(R.color.pnp_blue, android.R.color.holo_blue_dark),
                getColorSafe(R.color.gray_medium, android.R.color.darker_gray)
            });

            PieData pieData = new PieData(dataSet);
            pieData.setValueTextSize(12f);
            pieData.setValueTextColor(getColorSafe(R.color.black, android.R.color.black));

            reportTypePieChart.setData(pieData);
            reportTypePieChart.setDescription(null);
            reportTypePieChart.setDrawHoleEnabled(true);
            reportTypePieChart.setHoleColor(getColorSafe(R.color.white, android.R.color.white));
            reportTypePieChart.setTransparentCircleRadius(0f);
            reportTypePieChart.setRotationEnabled(false);
            reportTypePieChart.setHighlightPerTapEnabled(false);
            reportTypePieChart.setDrawEntryLabels(false);
            reportTypePieChart.getLegend().setEnabled(false);
            reportTypePieChart.invalidate();

            Log.d(TAG, "✅ Pie chart setup completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up pie chart: " + e.getMessage(), e);
        }
    }

    /**
     * Update top barangay statistics
     */
    private void updateTopBarangayStatistics() {
        try {
            Log.d(TAG, "📊 Updating top barangay statistics...");
            
            // Fetch top barangay data from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> barangayCounts = new HashMap<>();
                    
                    // Count reports by barangay
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String barangay = document.getString("barangay");
                        if (barangay != null && !barangay.isEmpty()) {
                            barangayCounts.put(barangay, barangayCounts.getOrDefault(barangay, 0) + 1);
                        }
                    }
                    
                    // Sort barangays by count and get top 3
                    String[] topBarangays = barangayCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(3)
                        .map(Map.Entry::getKey)
                        .toArray(String[]::new);
                    
                    // Update UI with top 3 barangays
                    updateTopBarangayUI(topBarangays, barangayCounts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching barangay statistics: " + e.getMessage());
                    setDefaultTopBarangayStatistics();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error updating top barangay statistics: " + e.getMessage(), e);
            setDefaultTopBarangayStatistics();
        }
    }

    /**
     * Update UI with top barangay data
     */
    private void updateTopBarangayUI(String[] topBarangays, Map<String, Integer> barangayCounts) {
        try {
            // Update first barangay
            if (topBarangays.length > 0 && topBarangay1Name != null && topBarangay1Count != null) {
                topBarangay1Name.setText(topBarangays[0]);
                topBarangay1Count.setText(String.valueOf(barangayCounts.get(topBarangays[0])));
            }
            
            // Update second barangay
            if (topBarangays.length > 1 && topBarangay2Name != null && topBarangay2Count != null) {
                topBarangay2Name.setText(topBarangays[1]);
                topBarangay2Count.setText(String.valueOf(barangayCounts.get(topBarangays[1])));
            }
            
            // Update third barangay
            if (topBarangays.length > 2 && topBarangay3Name != null && topBarangay3Count != null) {
                topBarangay3Name.setText(topBarangays[2]);
                topBarangay3Count.setText(String.valueOf(barangayCounts.get(topBarangays[2])));
            }
            
            Log.d(TAG, "✅ Top barangay UI updated");
        } catch (Exception e) {
            Log.e(TAG, "Error updating top barangay UI: " + e.getMessage(), e);
        }
    }

    /**
     * Set default top barangay statistics when data fetch fails
     */
    private void setDefaultTopBarangayStatistics() {
        try {
            if (topBarangay1Name != null) topBarangay1Name.setText("Kulapi");
            if (topBarangay1Count != null) topBarangay1Count.setText("15");
            if (topBarangay2Name != null) topBarangay2Name.setText("Rizal");
            if (topBarangay2Count != null) topBarangay2Count.setText("12");
            if (topBarangay3Name != null) topBarangay3Name.setText("Sampaloc");
            if (topBarangay3Count != null) topBarangay3Count.setText("8");
            
            Log.d(TAG, "✅ Default top barangay statistics set");
        } catch (Exception e) {
            Log.e(TAG, "Error setting default top barangay statistics: " + e.getMessage(), e);
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

            // Setup swipe to call functionality
            setupSwipeToCall();
            
            // Call button functionality (fallback for taps)
            if (callButton != null) {
                callButton.setOnClickListener(v -> {
                    // Show swipe instruction
                    Toast.makeText(this, "Swipe the phone icon to the right to call", Toast.LENGTH_SHORT).show();
                });
            }

            // Poster button functionality
            if (posterButton != null) {
                posterButton.setOnClickListener(v -> openPosterActivity());
            }

            // Emergency contacts click listeners
            setupEmergencyContacts();

            // Add contact button
            ImageView addContactIcon = findViewById(R.id.addContactIcon);
            if (addContactIcon != null) {
                addContactIcon.setOnClickListener(v -> showAddContactLikeLdrrmo());
            }

            // Safety tips click listeners
            setupSafetyTips();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private static final int ADD_CONTACT_IMAGE_REQUEST = 4101;
    private static final int ADD_CONTACT_SHEET_IMAGE_REQUEST = 4102;
    private Uri addContactImageUri;
    private ImageView addContactPreviewRef;
    private ImageView addContactSheetIconRef;

    private void showAddEmergencyContactBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_add_emergency_contact, null);
            dialog.setContentView(view);

            ImageView ivPreview = view.findViewById(R.id.ivContactPreview);
            Button btnPick = view.findViewById(R.id.btnPickContactImage);
            EditText etName = view.findViewById(R.id.etContactName);
            EditText etMobile = view.findViewById(R.id.etContactMobile);
            Button btnCancel = view.findViewById(R.id.btnCancel);
            Button btnSave = view.findViewById(R.id.btnSave);

            btnPick.setOnClickListener(v -> {
                addContactPreviewRef = ivPreview;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Contact Picture"), ADD_CONTACT_IMAGE_REQUEST);
            });

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String mobile = etMobile.getText().toString().trim();
                if (name.isEmpty() || mobile.isEmpty()) {
                    Toast.makeText(this, "Please enter name and mobile", Toast.LENGTH_SHORT).show();
                    return;
                }
                addCustomEmergencyContact(name, mobile, addContactImageUri);
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening add contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCustomEmergencyContact(String name, String mobile, Uri imageUri) {
        addCustomEmergencyContact(name, mobile, "", imageUri);
    }

    private void addCustomEmergencyContact(String name, String mobile, String relationship, Uri imageUri) {
        if (emergencyContactsLayout == null) return;

        // Ensure the row becomes horizontally scrollable the first time a custom contact is added
        makeEmergencyContactsScrollableIfNeeded();

        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int containerMargin = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        containerLp.setMargins(0, 0, containerMargin, 0);
        container.setLayoutParams(containerLp);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        CircleImageView icon = new CircleImageView(this);
        int iconSize = getResources().getDimensionPixelSize(R.dimen.dashboard_emergency_contact_size);
        int iconPadding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        int iconMargin = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        ivLp.bottomMargin = iconMargin;
        icon.setLayoutParams(ivLp);
        icon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        icon.setClickable(true);
        icon.setFocusable(true);
        icon.setForeground(getDrawable(android.R.drawable.list_selector_background));
        icon.setContentDescription("Custom Emergency Contact");
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (imageUri != null) {
            icon.setImageURI(imageUri);
        } else {
            icon.setImageResource(R.drawable.ic_person);
        }

        TextView nameLabel = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameLabel.setLayoutParams(tvLp);
        nameLabel.setText(toAcronym(name));
        nameLabel.setTextColor(getResources().getColor(R.color.black));
        nameLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dashboard_emergency_label_size));
        nameLabel.setTypeface(nameLabel.getTypeface(), Typeface.BOLD);
        nameLabel.setGravity(Gravity.CENTER);
        nameLabel.setIncludeFontPadding(false);

        container.addView(icon);
        container.addView(nameLabel);

        String rel = (relationship != null) ? relationship.trim() : "";
        icon.setOnClickListener(v -> showEmergencyContactDialog(name, rel, mobile, imageUri));

        // Insert right after the Add tile
        View addTile = emergencyContactsLayout.findViewById(R.id.addContactContainer);
        int addIndex = addTile != null ? emergencyContactsLayout.indexOfChild(addTile) : -1;
        int insertIndex = addIndex >= 0 ? addIndex + 1 : emergencyContactsLayout.getChildCount();
        emergencyContactsLayout.addView(container, insertIndex);
    }

    private void addCustomEmergencyContact(String name, String mobile, String relationship, String imageUrl) {
        if (emergencyContactsLayout == null) return;
        makeEmergencyContactsScrollableIfNeeded();

        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int containerMargin = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        containerLp.setMargins(0, 0, containerMargin, 0);
        container.setLayoutParams(containerLp);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        CircleImageView icon = new CircleImageView(this);
        int iconSize = getResources().getDimensionPixelSize(R.dimen.dashboard_emergency_contact_size);
        int iconPadding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        int iconMargin = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        ivLp.bottomMargin = iconMargin;
        icon.setLayoutParams(ivLp);
        icon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        icon.setClickable(true);
        icon.setFocusable(true);
        icon.setForeground(getDrawable(android.R.drawable.list_selector_background));
        icon.setContentDescription("Custom Emergency Contact");
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageIntoView(icon, imageUrl);
        } else {
            icon.setImageResource(R.drawable.ic_person);
        }

        TextView nameLabel = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameLabel.setLayoutParams(tvLp);
        nameLabel.setText(toAcronym(name));
        nameLabel.setTextColor(getResources().getColor(R.color.black));
        nameLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dashboard_emergency_label_size));
        nameLabel.setTypeface(nameLabel.getTypeface(), Typeface.BOLD);
        nameLabel.setGravity(Gravity.CENTER);
        nameLabel.setIncludeFontPadding(false);

        container.addView(icon);
        container.addView(nameLabel);

        String rel = (relationship != null) ? relationship.trim() : "";
        icon.setOnClickListener(v -> showEmergencyContactDialog(name, rel, mobile, imageUrl));

        View addTile = emergencyContactsLayout.findViewById(R.id.addContactContainer);
        int addIndex = addTile != null ? emergencyContactsLayout.indexOfChild(addTile) : -1;
        int insertIndex = addIndex >= 0 ? addIndex + 1 : emergencyContactsLayout.getChildCount();
        emergencyContactsLayout.addView(container, insertIndex);
    }

    private String toAcronym(String fullName) {
        try {
            if (fullName == null) return "";
            String trimmed = fullName.trim();
            if (trimmed.isEmpty()) return "";
            String[] parts = trimmed.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    char c = part.charAt(0);
                    if (Character.isLetterOrDigit(c)) {
                        sb.append(Character.toUpperCase(c));
                    }
                }
            }
            return sb.length() > 0 ? sb.toString() : trimmed;
        } catch (Exception e) {
            return fullName != null ? fullName : "";
        }
    }

    /**
     * Show custom contact bottom sheet with user's uploaded image and provided name.
     */
    private void showEmergencyContactDialog(String name, String relationship, String number, Uri imageUri) {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_emergency_contact, null);

            ImageView agencyIcon = bottomSheetView.findViewById(R.id.agencyIcon);
            TextView agencyName = bottomSheetView.findViewById(R.id.agencyName);
            TextView agencyFullName = bottomSheetView.findViewById(R.id.agencyFullName);
            LinearLayout facebookLinkContainer = bottomSheetView.findViewById(R.id.facebookLinkContainer);
            TextView phoneNumber = bottomSheetView.findViewById(R.id.phoneNumber);
            android.widget.FrameLayout swipeToCallContainer = bottomSheetView.findViewById(R.id.swipeToCallContainer);
            ImageView swipePhoneIcon = bottomSheetView.findViewById(R.id.swipePhoneIcon);

            // Set uploaded image if available; otherwise default person icon
            if (agencyIcon != null) {
                if (imageUri != null) {
                    agencyIcon.setImageURI(imageUri);
                } else {
                    agencyIcon.setImageResource(R.drawable.ic_person);
                }
            }

            // Name as provided by user
            if (agencyName != null) {
                agencyName.setText(name != null ? name : "Contact");
            }

            // Relationship as subtitle if provided; otherwise hide
            if (agencyFullName != null) {
                if (relationship != null && !relationship.isEmpty()) {
                    agencyFullName.setText(relationship);
                    agencyFullName.setVisibility(View.VISIBLE);
                } else {
                    agencyFullName.setText("");
                    agencyFullName.setVisibility(View.GONE);
                }
            }

            // Hide facebook link for custom contacts
            if (facebookLinkContainer != null) {
                facebookLinkContainer.setVisibility(View.GONE);
            }

            if (phoneNumber != null) {
                phoneNumber.setText(number != null ? number : "");
            }

            // Ensure swipe-to-call is visible (same as built-in agencies)
            if (swipeToCallContainer != null) {
                swipeToCallContainer.setVisibility(View.VISIBLE);
            }

            // Apply LDRRMO-like accent color to the swipe controls for custom contacts
            try {
                int accent = getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark);
                if (swipeToCallContainer != null) {
                    swipeToCallContainer.setBackgroundResource(R.drawable.call_button_background);
                    swipeToCallContainer.setBackgroundTintList(ColorStateList.valueOf(accent));
                }
                if (swipePhoneIcon != null) {
                    swipePhoneIcon.setBackgroundResource(R.drawable.circle_background);
                    swipePhoneIcon.setBackgroundTintList(ColorStateList.valueOf(accent));
                    swipePhoneIcon.setColorFilter(Color.WHITE);
                }
            } catch (Exception ignored) {}

            // Setup swipe-to-call
            setupBottomSheetSwipeToCall(swipePhoneIcon, swipeToCallContainer, name != null ? name : "Contact", number, bottomSheetDialog);

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing custom emergency contact bottom sheet: " + e.getMessage(), e);
            // Fallback to direct call
            callEmergencyContact(name != null ? name : "Contact", number);
        }
    }

    private void showEmergencyContactDialog(String name, String relationship, String number, String imageUrl) {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_emergency_contact, null);

            ImageView agencyIcon = bottomSheetView.findViewById(R.id.agencyIcon);
            TextView agencyName = bottomSheetView.findViewById(R.id.agencyName);
            TextView agencyFullName = bottomSheetView.findViewById(R.id.agencyFullName);
            LinearLayout facebookLinkContainer = bottomSheetView.findViewById(R.id.facebookLinkContainer);
            TextView phoneNumber = bottomSheetView.findViewById(R.id.phoneNumber);
            android.widget.FrameLayout swipeToCallContainer = bottomSheetView.findViewById(R.id.swipeToCallContainer);
            ImageView swipePhoneIcon = bottomSheetView.findViewById(R.id.swipePhoneIcon);

            if (agencyIcon != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    loadImageIntoView(agencyIcon, imageUrl);
                } else {
                    agencyIcon.setImageResource(R.drawable.ic_person);
                }
            }

            if (agencyName != null) {
                agencyName.setText(name != null ? name : "Contact");
            }

            if (agencyFullName != null) {
                if (relationship != null && !relationship.isEmpty()) {
                    agencyFullName.setText(relationship);
                    agencyFullName.setVisibility(View.VISIBLE);
                } else {
                    agencyFullName.setText("");
                    agencyFullName.setVisibility(View.GONE);
                }
            }

            if (facebookLinkContainer != null) {
                facebookLinkContainer.setVisibility(View.GONE);
            }

            if (phoneNumber != null) {
                phoneNumber.setText(number != null ? number : "");
            }

            // Ensure swipe-to-call is visible (same as built-in agencies)
            if (swipeToCallContainer != null) {
                swipeToCallContainer.setVisibility(View.VISIBLE);
            }

            try {
                int accent = getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark);
                if (swipeToCallContainer != null) {
                    swipeToCallContainer.setBackgroundResource(R.drawable.call_button_background);
                    swipeToCallContainer.setBackgroundTintList(ColorStateList.valueOf(accent));
                }
                if (swipePhoneIcon != null) {
                    swipePhoneIcon.setBackgroundResource(R.drawable.circle_background);
                    swipePhoneIcon.setBackgroundTintList(ColorStateList.valueOf(accent));
                    swipePhoneIcon.setColorFilter(Color.WHITE);
                }
            } catch (Exception ignored) {}

            setupBottomSheetSwipeToCall(swipePhoneIcon, swipeToCallContainer, name != null ? name : "Contact", number, bottomSheetDialog);

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing custom emergency contact (URL) bottom sheet: " + e.getMessage(), e);
            callEmergencyContact(name != null ? name : "Contact", number);
        }
    }

    private void makeEmergencyContactsScrollableIfNeeded() {
        try {
            if (emergencyContactsLayout == null) return;

            // Already inside a HorizontalScrollView
            if (emergencyContactsLayout.getParent() instanceof android.widget.HorizontalScrollView) {
                return;
            }

            ViewGroup parent = (ViewGroup) emergencyContactsLayout.getParent();
            if (parent == null) return;

            int index = parent.indexOfChild(emergencyContactsLayout);
            parent.removeView(emergencyContactsLayout);

            // Convert existing children to wrap_content with spacing to match scrollable style
            for (int i = 0; i < emergencyContactsLayout.getChildCount(); i++) {
                View child = emergencyContactsLayout.getChildAt(i);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 0, dp(16), 0);
                child.setLayoutParams(lp);
                if (child instanceof LinearLayout) {
                    ((LinearLayout) child).setGravity(Gravity.CENTER);
                }
            }

            // Create HorizontalScrollView and wrap the layout
            android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(this);
            hsv.setHorizontalScrollBarEnabled(false);
            hsv.setVerticalScrollBarEnabled(false);
            hsv.setFadingEdgeLength(0);

            LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            hsvLp.bottomMargin = dp(24);
            hsv.setLayoutParams(hsvLp);

            // Ensure the inner layout is sized for content width
            emergencyContactsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            hsv.addView(emergencyContactsLayout,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            parent.addView(hsv, index);
        } catch (Exception e) {
            Log.e(TAG, "Error making emergency contacts scrollable: " + e.getMessage(), e);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                addContactImageUri = uri;
                if (addContactPreviewRef != null) {
                    addContactPreviewRef.setVisibility(View.VISIBLE);
                    addContactPreviewRef.setImageURI(uri);
                }
            }
        }
        if (requestCode == ADD_CONTACT_SHEET_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                addContactImageUri = uri;
                if (addContactSheetIconRef != null) {
                    addContactSheetIconRef.setImageURI(uri);
                }
            }
        }
    }

    private void showAddContactLikeLdrrmo() {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_emergency_contact, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            ImageView agencyIcon = bottomSheetView.findViewById(R.id.agencyIcon);
            TextView agencyName = bottomSheetView.findViewById(R.id.agencyName);
            TextView agencyFullName = bottomSheetView.findViewById(R.id.agencyFullName);
            LinearLayout facebookLinkContainer = bottomSheetView.findViewById(R.id.facebookLinkContainer);
            TextView facebookLink = bottomSheetView.findViewById(R.id.facebookLink);
            TextView phoneNumber = bottomSheetView.findViewById(R.id.phoneNumber);
            android.widget.FrameLayout swipeToCallContainer = bottomSheetView.findViewById(R.id.swipeToCallContainer);

            if (agencyIcon != null) agencyIcon.setImageResource(R.drawable.add_contacts);
            if (agencyName != null) agencyName.setText("Add Contact");
            if (agencyFullName != null) agencyFullName.setText("Emergency Contact");
            if (facebookLinkContainer != null) facebookLinkContainer.setVisibility(View.GONE);
            if (phoneNumber != null) phoneNumber.setText("");
            if (swipeToCallContainer != null) swipeToCallContainer.setVisibility(View.GONE);

            // Tap icon to pick image and show on the icon
            if (agencyIcon != null) {
                agencyIcon.setOnClickListener(v -> {
                    addContactSheetIconRef = agencyIcon;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Contact Picture"), ADD_CONTACT_SHEET_IMAGE_REQUEST);
                });
            }

            // Insert Name and Mobile fields below the title
            if (agencyFullName != null) {
                ViewGroup root = (ViewGroup) agencyFullName.getParent();
                int insertIndex = root.indexOfChild(agencyFullName) + 1;

                EditText etName = new EditText(this);
                etName.setHint("Full Name");
                etName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.bottomMargin = dp(8);
                etName.setLayoutParams(lp);
                // Match email_edit_text styling
                etName.setBackgroundResource(R.drawable.edittext_background);
                etName.setHeight(getResources().getDimensionPixelSize(R.dimen.input_height_large));
                etName.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small),
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small)
                );
                etName.setGravity(Gravity.CENTER_VERTICAL);
                try {
                    etName.setTextColor(getResources().getColor(R.color.dark_gray));
                    etName.setHintTextColor(getResources().getColor(R.color.light_gray));
                } catch (Exception ignored) {}

                EditText etRelationship = new EditText(this);
                etRelationship.setHint("Relationship (e.g., Mother)");
                etRelationship.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                LinearLayout.LayoutParams lpRel = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lpRel.bottomMargin = dp(8);
                etRelationship.setLayoutParams(lpRel);
                etRelationship.setBackgroundResource(R.drawable.edittext_background);
                etRelationship.setHeight(getResources().getDimensionPixelSize(R.dimen.input_height_large));
                etRelationship.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small),
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small)
                );
                etRelationship.setGravity(Gravity.CENTER_VERTICAL);
                try {
                    etRelationship.setTextColor(getResources().getColor(R.color.dark_gray));
                    etRelationship.setHintTextColor(getResources().getColor(R.color.light_gray));
                } catch (Exception ignored) {}

                EditText etMobile = new EditText(this);
                etMobile.setHint("Mobile Number");
                etMobile.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp2.bottomMargin = dp(16);
                etMobile.setLayoutParams(lp2);
                // Match email_edit_text styling
                etMobile.setBackgroundResource(R.drawable.edittext_background);
                etMobile.setHeight(getResources().getDimensionPixelSize(R.dimen.input_height_large));
                etMobile.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small),
                        getResources().getDimensionPixelSize(R.dimen.padding_large),
                        getResources().getDimensionPixelSize(R.dimen.padding_small)
                );
                etMobile.setGravity(Gravity.CENTER_VERTICAL);
                try {
                    etMobile.setTextColor(getResources().getColor(R.color.dark_gray));
                    etMobile.setHintTextColor(getResources().getColor(R.color.light_gray));
                } catch (Exception ignored) {}

                Button btnSave = new Button(this);
                btnSave.setText("Save");
                LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp3.gravity = Gravity.END;
                btnSave.setLayoutParams(lp3);

                root.addView(etName, insertIndex);
                root.addView(etRelationship, insertIndex + 1);
                root.addView(etMobile, insertIndex + 2);
                root.addView(btnSave, insertIndex + 3);

                btnSave.setOnClickListener(v -> {
                    String name = etName.getText().toString().trim();
                    String relationship = etRelationship.getText().toString().trim();
                    String mobile = etMobile.getText().toString().trim();
                    if (name.isEmpty() || mobile.isEmpty()) {
                        Toast.makeText(this, "Please enter name and mobile", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Persist globally then update UI
                    saveCustomEmergencyContact(name, mobile, relationship, addContactImageUri, new Runnable() {
                        @Override
                        public void run() {
                            // UI update handled after save with URL (or no image)
                            addContactImageUri = null;
                            addContactSheetIconRef = null;
                            bottomSheetDialog.dismiss();
                        }
                    });
                });
            }

            bottomSheetDialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening contact sheet", Toast.LENGTH_SHORT).show();
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

    private void setupProfileLauncher() {
        profileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean profileUpdated = data.getBooleanExtra("profile_updated", false);
                    
                    if (profileUpdated) {
                        Log.d(TAG, "Profile was updated, refreshing user info");
                        // Refresh profile data immediately
                        setupUserInfo();
                        loadUserProfilePicture();
                        
                        // Optionally show a success message
                        String fullName = data.getStringExtra("full_name");
                        if (fullName != null && !fullName.isEmpty()) {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }
    
    private void openProfileActivity() {
        // Navigate to profile activity
        try {
            Intent intent = new Intent(this, ProfileActivity.class);
            profileLauncher.launch(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeEmergencyCall() {
        try {
            String emergencyNumber = "tel:09175204211"; // LDRRMO Lucban: 0917 520 4211

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
    
    /**
     * Setup swipe-to-call functionality for the phone icon
     * Allows users to swipe the phone icon to the right to initiate emergency call
     */
    private void setupSwipeToCall() {
        try {
            if (phoneIcon == null || callButton == null) {
                Log.w(TAG, "phoneIcon or callButton is null, cannot setup swipe to call");
                return;
            }
            
            // Store the initial position
            phoneIcon.post(() -> {
                initialX = phoneIcon.getX();
            });
            
            phoneIcon.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position
                        initialTouchX = event.getRawX();
                        isSwiping = false;
                        
                        // Visual feedback: scale down slightly
                        v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the distance moved
                        float deltaX = event.getRawX() - initialTouchX;
                        
                        // Only allow swiping to the right
                        if (deltaX > 0) {
                            isSwiping = true;
                            
                            // Calculate max swipe distance based on callButton (parent) width
                            float maxSwipeDistance = callButton.getWidth() - v.getWidth() - 40; // 40 for padding
                            
                            // Limit the movement to not go beyond the parent
                            float newX = Math.min(deltaX, maxSwipeDistance);
                            v.setTranslationX(newX);
                            
                            // Calculate swipe progress
                            float progress = newX / maxSwipeDistance;
                            
                            // Change icon alpha based on swipe progress
                            v.setAlpha(0.6f + (0.4f * progress));
                            
                            // Scale up as user swipes for emphasis
                            float scale = 1.0f + (0.2f * progress); // Scale from 1.0 to 1.2
                            v.setScaleX(scale);
                            v.setScaleY(scale);
                            
                            // Enhanced dim effect on background button (0.5 = 50% dimming)
                            callButton.setAlpha(1.0f - (0.5f * progress));
                            
                            // Add visual feedback as swipe progresses for "go" feedback
                            if (progress >= SWIPE_THRESHOLD) {
                                // Near completion - brighten the icon
                                v.setAlpha(1.0f);
                                // Dim the background more
                                callButton.setAlpha(0.4f);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isSwiping) {
                            // Calculate swipe progress
                            float maxSwipeDistance = callButton.getWidth() - v.getWidth() - 40;
                            float progress = v.getTranslationX() / maxSwipeDistance;
                            
                            if (progress >= SWIPE_THRESHOLD) {
                                // Swipe completed - make the call
                                animatePhoneIconComplete(v);
                            } else {
                                // Swipe not completed - reset position
                                animatePhoneIconReset(v);
                            }
                        } else {
                            // Just a tap - show swipe instruction
                            Toast.makeText(MainDashboard.this, 
                                "Swipe right to call LDRRMO", 
                                Toast.LENGTH_SHORT).show();
                            animatePhoneIconReset(v);
                        }
                        
                        isSwiping = false;
                        return true;

                    default:
                        return false;
                }
            });
            
            Log.d(TAG, "✅ Swipe-to-call setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up swipe to call: " + e.getMessage(), e);
        }
    }
    
    /**
     * Animate phone icon completion - slide out and make call
     */
    private void animatePhoneIconComplete(View v) {
        try {
            // Animate to completion - slide all the way to the right
            float maxDistance = callButton.getWidth() - v.getWidth();
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(v, "translationX", maxDistance);
            slideOut.setDuration(200);
            slideOut.setInterpolator(new DecelerateInterpolator());
            
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 0f);
            fadeOut.setDuration(200);
            
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f);
            scaleUpX.setDuration(200);
            
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f);
            scaleUpY.setDuration(200);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut, scaleUpX, scaleUpY);
            animatorSet.start();
            
            // Make the call after animation
            v.postDelayed(() -> {
                makeEmergencyCall();
                // Reset icon position after call is initiated
                v.postDelayed(() -> animatePhoneIconReset(v), 500);
            }, 250);
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating phone icon complete: " + e.getMessage(), e);
            // Fallback to direct call
            makeEmergencyCall();
            animatePhoneIconReset(v);
        }
    }
    
    /**
     * Animate phone icon reset - return to original position
     */
    private void animatePhoneIconReset(View v) {
        try {
            ObjectAnimator slideBack = ObjectAnimator.ofFloat(v, "translationX", 0f);
            slideBack.setDuration(300);
            slideBack.setInterpolator(new DecelerateInterpolator());
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", 1.0f);
            fadeIn.setDuration(300);
            
            ObjectAnimator scaleResetX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f);
            scaleResetX.setDuration(300);
            
            ObjectAnimator scaleResetY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f);
            scaleResetY.setDuration(300);
            
            // Reset background button alpha
            ObjectAnimator resetButtonAlpha = ObjectAnimator.ofFloat(callButton, "alpha", 1.0f);
            resetButtonAlpha.setDuration(300);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideBack, fadeIn, scaleResetX, scaleResetY, resetButtonAlpha);
            animatorSet.start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating phone icon reset: " + e.getMessage(), e);
            // Fallback to immediate reset
            v.setTranslationX(0f);
            v.setAlpha(1.0f);
            v.setScaleX(1.0f);
            v.setScaleY(1.0f);
            callButton.setAlpha(1.0f);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("tel:09175204211"); // LDRRMO Lucban: 0917 520 4211
                } else {
                    // Permission denied, show dial pad instead
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:09175204211")); // LDRRMO Lucban: 0917 520 4211
                    startActivity(dialIntent);
                    Toast.makeText(this, "Permission denied. Opening dial pad instead.",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "✅ Location permission granted");
                    Toast.makeText(this, "Location permission granted! You can now use location features.", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Update location text if needed
                    refreshAllUserData();
                } else {
                    Log.w(TAG, "❌ Location permission denied");
                    Toast.makeText(this, "Location permission denied. Some features may be limited.", 
                        Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "✅ Notification permission granted");
                    Toast.makeText(this, "Notifications enabled! You'll receive important alerts.", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Re-initialize FCM token now that permission is granted
                    initializeFCMToken();
                } else {
                    Log.w(TAG, "❌ Notification permission denied");
                    Toast.makeText(this, "Notifications disabled. You can enable them later in Settings.", 
                        Toast.LENGTH_LONG).show();
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
        // LDRRMO contact - Fixed to show emergency contact dialog
        ImageView ldrrmoIcon = findViewById(R.id.ldrrmoIcon);
        if (ldrrmoIcon != null) {
            ldrrmoIcon.setOnClickListener(v -> showEmergencyContactDialog("LDRRMO", "Local Disaster Risk Reduction and Management Office", "0917 520 4211"));
        }

        // RHU contact
        ImageView rhuIcon = findViewById(R.id.rhuIcon);
        if (rhuIcon != null) {
            rhuIcon.setOnClickListener(v -> showEmergencyContactDialog("RHU", "Rural Health Unit", "0915 685 1185"));
        }

        // PNP contact
        ImageView pnpIcon = findViewById(R.id.pnpIcon);
        if (pnpIcon != null) {
            pnpIcon.setOnClickListener(v -> showEmergencyContactDialog("PNP", "Philippine National Police", "0998 598 5759"));
        }

        // BFP contact
        ImageView bfpIcon = findViewById(R.id.bfpIcon);
        if (bfpIcon != null) {
            bfpIcon.setOnClickListener(v -> showEmergencyContactDialog("BFP", "Bureau of Fire Protection", "0932 603 1222"));
        }
    }

    /**
     * Load globally shared custom emergency contacts from Firestore and render them.
     */
    private void loadCustomEmergencyContacts() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.w(TAG, "No user logged in; skipping loadCustomEmergencyContacts");
                return;
            }
            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).collection("emergency_contacts")
                .get()
                .addOnSuccessListener(query -> {
                    if (query != null && !query.isEmpty()) {
                        for (DocumentSnapshot snap : query.getDocuments()) {
                            String name = snap.getString("name");
                            String mobile = snap.getString("mobile");
                            String relationship = snap.getString("relationship");
                            String imageUrl = snap.getString("imageUrl");
                            String contactId = snap.getId();

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                runOnUiThread(() -> addCustomEmergencyContact(
                                        name != null ? name : "Contact",
                                        mobile != null ? mobile : "",
                                        relationship != null ? relationship : "",
                                        imageUrl
                                ));
                            } else {
                                // Try to resolve image from Storage path for this user/contact
                                try {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference ref = storage.getReference().child("user_emergency_contacts/" + uid + "/" + contactId + ".jpg");
                                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String resolvedUrl = uri.toString();
                                        runOnUiThread(() -> addCustomEmergencyContact(
                                                name != null ? name : "Contact",
                                                mobile != null ? mobile : "",
                                                relationship != null ? relationship : "",
                                                resolvedUrl
                                        ));
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("imageUrl", resolvedUrl);
                                        FirebaseFirestore.getInstance()
                                                .collection("users").document(uid)
                                                .collection("emergency_contacts").document(contactId)
                                                .update(updates);
                                    }).addOnFailureListener(e -> runOnUiThread(() -> addCustomEmergencyContact(
                                            name != null ? name : "Contact",
                                            mobile != null ? mobile : "",
                                            relationship != null ? relationship : "",
                                            ""
                                    )));
                                } catch (Exception e) {
                                    runOnUiThread(() -> addCustomEmergencyContact(
                                            name != null ? name : "Contact",
                                            mobile != null ? mobile : "",
                                            relationship != null ? relationship : "",
                                            ""
                                    ));
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user emergency contacts: " + e.getMessage(), e));
        } catch (Exception e) {
            Log.e(TAG, "Error initiating loadCustomEmergencyContacts: " + e.getMessage(), e);
        }
    }

    /**
     * Save a custom emergency contact globally (visible to all users).
     * Uploads image if provided, then writes Firestore document.
     */
    private void saveCustomEmergencyContact(String name, String mobile, String relationship, Uri localImageUri, Runnable onComplete) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
                if (onComplete != null) onComplete.run();
                return;
            }
            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            com.google.firebase.firestore.DocumentReference docRef = db.collection("users").document(uid).collection("emergency_contacts").document();
            String docId = docRef.getId();

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("mobile", mobile);
            data.put("relationship", relationship);
            data.put("createdAt", System.currentTimeMillis());
            data.put("createdBy", uid);

            if (localImageUri != null) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference().child("user_emergency_contacts/" + uid + "/" + docId + ".jpg");
                ref.putFile(localImageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        data.put("imageUrl", uri.toString());
                        docRef.set(data)
                            .addOnSuccessListener(aVoid -> {
                                runOnUiThread(() -> {
                                    addCustomEmergencyContact(name, mobile, relationship, uri.toString());
                                    Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
                                    if (onComplete != null) onComplete.run();
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving contact to Firestore: " + e.getMessage(), e);
                                runOnUiThread(() -> {
                                    // Fallback: show locally so user sees it
                                    addCustomEmergencyContact(name, mobile, relationship, uri.toString());
                                    if (onComplete != null) onComplete.run();
                                });
                            });
                    }))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                        // Save without image
                        docRef.set(data)
                            .addOnSuccessListener(aVoid -> {
                                runOnUiThread(() -> {
                                    addCustomEmergencyContact(name, mobile, relationship, "");
                                    Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
                                    if (onComplete != null) onComplete.run();
                                });
                            })
                            .addOnFailureListener(err -> {
                                Log.e(TAG, "Error saving contact without image: " + err.getMessage(), err);
                                runOnUiThread(() -> {
                                    addCustomEmergencyContact(name, mobile, relationship, "");
                                    if (onComplete != null) onComplete.run();
                                });
                            });
                    });
            } else {
                // No image, save directly
                docRef.set(data)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() -> {
                            addCustomEmergencyContact(name, mobile, relationship, "");
                            Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
                            if (onComplete != null) onComplete.run();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving contact to Firestore: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            addCustomEmergencyContact(name, mobile, relationship, "");
                            if (onComplete != null) onComplete.run();
                        });
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in saveCustomEmergencyContact: " + e.getMessage(), e);
            if (onComplete != null) onComplete.run();
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
            LinearLayout facebookLinkContainer = bottomSheetView.findViewById(R.id.facebookLinkContainer);
            TextView facebookLink = bottomSheetView.findViewById(R.id.facebookLink);
            TextView phoneNumber = bottomSheetView.findViewById(R.id.phoneNumber);
            android.widget.FrameLayout swipeToCallContainer = bottomSheetView.findViewById(R.id.swipeToCallContainer);
            ImageView swipePhoneIcon = bottomSheetView.findViewById(R.id.swipePhoneIcon);
            
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
            
            // Set up Facebook link for all agencies
            if ("LDRRMO".equals(agency)) {
                if (facebookLinkContainer != null) {
                    facebookLinkContainer.setVisibility(View.VISIBLE);
                }
                if (facebookLink != null) {
                    facebookLink.setText("MDRRMO Lucban");
                    facebookLink.setOnClickListener(v -> {
                        openFacebookPage("https://www.facebook.com/mdrrmolucban");
                    });
                    
                    // No underline - external link icon indicates it's clickable
                    // facebookLink.setPaintFlags(facebookLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                }
            } else if ("RHU".equals(agency)) {
                if (facebookLinkContainer != null) {
                    facebookLinkContainer.setVisibility(View.VISIBLE);
                }
                if (facebookLink != null) {
                    facebookLink.setText("RHU Lucban");
                    facebookLink.setOnClickListener(v -> {
                        openFacebookPage("https://www.facebook.com/rhu.lucban.2025");
                    });
                    
                    // No underline - external link icon indicates it's clickable
                    // facebookLink.setPaintFlags(facebookLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                }
            } else if ("PNP".equals(agency)) {
                if (facebookLinkContainer != null) {
                    facebookLinkContainer.setVisibility(View.VISIBLE);
                }
                if (facebookLink != null) {
                    facebookLink.setText("Lucban Mps Qppo");
                    facebookLink.setOnClickListener(v -> {
                        openFacebookPage("https://www.facebook.com/lucban.mps.7");
                    });
                    
                    // No underline - external link icon indicates it's clickable
                    // facebookLink.setPaintFlags(facebookLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                }
            } else if ("BFP".equals(agency)) {
                if (facebookLinkContainer != null) {
                    facebookLinkContainer.setVisibility(View.VISIBLE);
                }
                if (facebookLink != null) {
                    facebookLink.setText("Bfp Lucban Fs Quezon");
                    facebookLink.setOnClickListener(v -> {
                        openFacebookPage("https://www.facebook.com/bfp.lucban.fs.quezon");
                    });
                    
                    // No underline - external link icon indicates it's clickable
                    // facebookLink.setPaintFlags(facebookLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                }
            } else {
                // Hide Facebook link for any other agencies (future expansion)
                if (facebookLinkContainer != null) {
                    facebookLinkContainer.setVisibility(View.GONE);
                }
            }
            
            // Apply agency-specific colors to swipe controls
            try {
                int agencyColor = getAgencyColor(agency);
                if (swipeToCallContainer != null) {
                    swipeToCallContainer.setBackgroundResource(R.drawable.call_button_background);
                    swipeToCallContainer.setBackgroundTintList(ColorStateList.valueOf(agencyColor));
                }
                if (swipePhoneIcon != null) {
                    swipePhoneIcon.setBackgroundResource(R.drawable.circle_background);
                    swipePhoneIcon.setBackgroundTintList(ColorStateList.valueOf(agencyColor));
                    swipePhoneIcon.setColorFilter(Color.WHITE);
                }
            } catch (Exception ignored) {}
            
            // Setup swipe-to-call functionality
            setupBottomSheetSwipeToCall(swipePhoneIcon, swipeToCallContainer, agency, number, bottomSheetDialog);
            
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
                return R.drawable.ic_ldrrmo; // LDRRMO specific icon
            case "RHU":
                return R.drawable.ic_rhu; // RHU specific icon
            case "PNP":
                return R.drawable.ic_pnp; // PNP specific icon
            case "BFP":
                return R.drawable.ic_bfp; // BFP specific icon
            default:
                return R.drawable.ic_emergency; // Default emergency icon
        }
    }
    
    private int getAgencyColor(String agency) {
        // All emergency contacts use orange color
        return getColorSafe(R.color.colorPrimary, android.R.color.holo_orange_dark);
    }

    /**
     * Setup swipe-to-call functionality for emergency contact bottom sheet
     */
    private void setupBottomSheetSwipeToCall(ImageView swipeIcon, android.widget.FrameLayout container, 
                                             String agency, String number, BottomSheetDialog dialog) {
        try {
            if (swipeIcon == null || container == null) {
                Log.w(TAG, "swipeIcon or container is null, cannot setup swipe to call");
                return;
            }
            
            final float[] initialTouchX = {0f};
            final boolean[] isSwiping = {false};
            
            swipeIcon.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position
                        initialTouchX[0] = event.getRawX();
                        isSwiping[0] = false;
                        
                        // Visual feedback: scale down slightly
                        v.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .start();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the distance moved
                        float deltaX = event.getRawX() - initialTouchX[0];
                        
                        // Only allow swiping to the right
                        if (deltaX > 0) {
                            isSwiping[0] = true;
                            
                            // Calculate max swipe distance based on container width
                            float maxSwipeDistance = container.getWidth() - v.getWidth() - 20;
                            
                            // Limit the movement to not go beyond the container
                            float newX = Math.min(deltaX, maxSwipeDistance);
                            v.setTranslationX(newX);
                            
                            // Calculate swipe progress
                            float progress = newX / maxSwipeDistance;
                            
                            // Change icon alpha based on swipe progress
                            v.setAlpha(0.7f + (0.3f * progress));
                            
                            // Scale up as user swipes
                            float scale = 0.9f + (0.3f * progress); // Scale from 0.9 to 1.2
                            v.setScaleX(scale);
                            v.setScaleY(scale);
                            
                            // Dim the background container
                            container.setAlpha(1.0f - (0.3f * progress));
                            
                            // Check if swipe threshold reached
                            if (progress >= 0.7f) {
                                // Near completion - brighten the icon
                                v.setAlpha(1.0f);
                                container.setAlpha(0.6f);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isSwiping[0]) {
                            // Calculate swipe progress
                            float maxSwipeDistance = container.getWidth() - v.getWidth() - 20;
                            float progress = v.getTranslationX() / maxSwipeDistance;
                            
                            if (progress >= 0.7f) {
                                // Swipe completed - make the call
                                animateSwipeComplete(v, container, () -> {
                                    dialog.dismiss();
                                    callEmergencyContact(agency, number);
                                });
                            } else {
                                // Swipe not completed - reset position
                                animateSwipeReset(v, container);
                            }
                        } else {
                            // Just a tap - show swipe instruction
                            Toast.makeText(MainDashboard.this, 
                                "Swipe right to call " + agency, 
                                Toast.LENGTH_SHORT).show();
                            animateSwipeReset(v, container);
                        }
                        
                        isSwiping[0] = false;
                        return true;

                    default:
                        return false;
                }
            });
            
            Log.d(TAG, "✅ Swipe-to-call setup completed for " + agency);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up swipe to call for bottom sheet: " + e.getMessage(), e);
        }
    }
    
    /**
     * Animate swipe completion and make call
     */
    private void animateSwipeComplete(View icon, View container, Runnable onComplete) {
        try {
            float maxDistance = container.getWidth() - icon.getWidth();
            
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(icon, "translationX", maxDistance);
            slideOut.setDuration(200);
            
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(icon, "alpha", 0f);
            fadeOut.setDuration(200);
            
            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(icon, "scaleX", 1.4f);
            scaleUp.setDuration(200);
            
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(icon, "scaleY", 1.4f);
            scaleUpY.setDuration(200);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut, scaleUp, scaleUpY);
            animatorSet.start();
            
            // Execute action after animation
            icon.postDelayed(onComplete, 250);
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating swipe complete: " + e.getMessage(), e);
            onComplete.run(); // Execute anyway
        }
    }
    
    /**
     * Animate swipe reset - return to original position
     */
    private void animateSwipeReset(View icon, View container) {
        try {
            ObjectAnimator slideBack = ObjectAnimator.ofFloat(icon, "translationX", 0f);
            slideBack.setDuration(300);
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(icon, "alpha", 1.0f);
            fadeIn.setDuration(300);
            
            ObjectAnimator scaleResetX = ObjectAnimator.ofFloat(icon, "scaleX", 1.0f);
            scaleResetX.setDuration(300);
            
            ObjectAnimator scaleResetY = ObjectAnimator.ofFloat(icon, "scaleY", 1.0f);
            scaleResetY.setDuration(300);
            
            ObjectAnimator resetContainerAlpha = ObjectAnimator.ofFloat(container, "alpha", 1.0f);
            resetContainerAlpha.setDuration(300);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideBack, fadeIn, scaleResetX, scaleResetY, resetContainerAlpha);
            animatorSet.start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating swipe reset: " + e.getMessage(), e);
            // Fallback to immediate reset
            icon.setTranslationX(0f);
            icon.setAlpha(1.0f);
            icon.setScaleX(1.0f);
            icon.setScaleY(1.0f);
            container.setAlpha(1.0f);
        }
    }
    
    private void callEmergencyContact(String agency, String number) {
        try {
            // Check if we have call permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make direct call
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                startActivity(callIntent);
                Toast.makeText(this, "Calling " + agency + "...", Toast.LENGTH_SHORT).show();
            } else {
                // Permission not granted, open dial pad
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + number));
                startActivity(dialIntent);
                Toast.makeText(this, "Opening dial pad for " + agency, Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            // If permission denied, show dial pad
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + number));
            startActivity(dialIntent);
            Toast.makeText(this, "Opening dial pad for " + agency, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error calling emergency contact: " + e.getMessage(), e);
            Toast.makeText(this, "Unable to make call. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Open Facebook page in browser or Facebook app
     * @param facebookUrl The Facebook page URL to open
     */
    private void openFacebookPage(String facebookUrl) {
        try {
            Log.d(TAG, "Opening Facebook page: " + facebookUrl);
            
            // Create intent to open URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            // Try to open in Facebook app first
            try {
                // Extract page username from URL (mdrrmolucban)
                String pageUsername = facebookUrl.substring(facebookUrl.lastIndexOf("/") + 1);
                
                // Method 1: Try fb://facewebmodal/f?href=URL (works best for most devices)
                Uri facebookUri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                intent.setData(facebookUri);
                
                // Check if Facebook app can handle this intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    Log.d(TAG, "Opened in Facebook app using facewebmodal");
                    return;
                }
                
                // Method 2: Try fb://page/<page_id> format
                intent.setData(Uri.parse("fb://page/" + pageUsername));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    Log.d(TAG, "Opened in Facebook app using page ID");
                    return;
                }
                
                // Method 3: Try fb://profile/<page_id> format
                intent.setData(Uri.parse("fb://profile/" + pageUsername));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    Log.d(TAG, "Opened in Facebook app using profile");
                    return;
                }
                
            } catch (Exception fbAppException) {
                Log.w(TAG, "Facebook app not available or error: " + fbAppException.getMessage());
            }
            
            // Fallback: Open in browser
            intent.setData(Uri.parse(facebookUrl));
            startActivity(intent);
            
            Log.d(TAG, "Opened in browser");
            // Toast message removed for cleaner UX
            // Toast.makeText(this, "Opening MDRRMO Lucban Facebook page", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening Facebook page: " + e.getMessage(), e);
            Toast.makeText(this, "Unable to open Facebook page. Please check your internet connection.", Toast.LENGTH_SHORT).show();
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

        if (floodSafetyCard != null) {
            floodSafetyCard.setOnClickListener(v -> openSafetyTips("Flood Safety"));
        }

        if (volcanicSafetyCard != null) {
            volcanicSafetyCard.setOnClickListener(v -> openSafetyTips("Volcanic Safety"));
        }

        if (civilDisturbanceCard != null) {
            civilDisturbanceCard.setOnClickListener(v -> openSafetyTips("Civil Disturbance"));
        }

        if (armedConflictCard != null) {
            armedConflictCard.setOnClickListener(v -> openSafetyTips("Armed Conflict"));
        }

        if (infectiousDiseaseCard != null) {
            infectiousDiseaseCard.setOnClickListener(v -> openSafetyTips("Infectious Disease"));
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
    
    /**
     * Refresh all user data including profile and location information
     * This ensures data is always up-to-date when returning to dashboard
     */
    private void refreshAllUserData() {
        try {
            Log.d(TAG, "Refreshing all user data...");
            
            // First try to load from Firestore to get the latest data
            loadUserDataFromFirestore();
            
            // Also refresh local data as fallback
            setupUserInfo();
            
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing all user data: " + e.getMessage(), e);
            // Fallback to local data only
            setupUserInfo();
        }
    }
    
    /**
     * Load user data from Firestore and update local SharedPreferences
     * This ensures we have the latest data from the server
     */
    private void loadUserDataFromFirestore() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.w(TAG, "No user logged in, skipping Firestore data load");
                return;
            }

            Log.d(TAG, "Loading user data from Firestore for UID: " + user.getUid());
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("firebaseUid", user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        
                        Log.d(TAG, "User document found in Firestore, updating local data");
                        
                        // Update SharedPreferences with latest data from Firestore
                        SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        // Update all user fields
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String email = doc.getString("email");
                        String phoneNumber = doc.getString("phoneNumber");
                        String province = doc.getString("province");
                        String cityTown = doc.getString("cityTown");
                        String barangay = doc.getString("barangay");
                        
                        if (firstName != null) editor.putString("first_name", firstName);
                        if (lastName != null) editor.putString("last_name", lastName);
                        if (email != null) editor.putString("email", email);
                        if (phoneNumber != null) editor.putString("mobile_number", phoneNumber);
                        if (province != null) editor.putString("province", province);
                        if (cityTown != null) {
                            editor.putString("city", cityTown);
                            editor.putString("cityTown", cityTown);
                        }
                        if (barangay != null) editor.putString("barangay", barangay);
                        
                        // Update location display format
                        if (cityTown != null && barangay != null) {
                            String fullLocation = cityTown + ", " + barangay;
                            editor.putString("location_text", fullLocation);
                            Log.d(TAG, "Updated location_text: " + fullLocation);
                        }
                        
                        editor.apply();
                        
                        // Update UI with the refreshed data
                        runOnUiThread(() -> {
                            setupUserInfo();
                            Log.d(TAG, "✅ User data refreshed from Firestore and UI updated");
                        });
                        
                    } else {
                        Log.w(TAG, "No user document found in Firestore for UID: " + user.getUid());
                        // Fallback to local data
                        runOnUiThread(() -> setupUserInfo());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data from Firestore: " + e.getMessage(), e);
                    // Fallback to local data
                    runOnUiThread(() -> setupUserInfo());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserDataFromFirestore: " + e.getMessage(), e);
            // Fallback to local data
            setupUserInfo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "MainDashboard onResume - refreshing all data");
            
            // Always refresh user info and location data when returning to dashboard
            refreshAllUserData();
            
            // Check if forecast data is stale and refresh if needed
            if (isForecastDataStale()) {
                Log.d(TAG, "Forecast data is stale, refreshing...");
                updateForecast();
            } else {
                Log.d(TAG, "Forecast data is fresh: " + getForecastAge());
            }
            
            loadUserProfilePicture(); // Refresh profile picture when returning to dashboard
            updateNotificationBadge(); // Update notification badge
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get weather description from OpenWeatherMap icon code
     * @param iconCode OpenWeatherMap icon code (e.g., "01d", "10n")
     * @return Human-readable weather description
     */
    private String getDescriptionFromIconCode(String iconCode) {
        if (iconCode == null) return "Clear sky";
        
        // Map OpenWeatherMap icon codes to descriptions
        switch (iconCode) {
            case "01d":
            case "01n":
                return "Clear sky";
            case "02d":
            case "02n":
                return "Few clouds";
            case "03d":
            case "03n":
                return "Scattered clouds";
            case "04d":
            case "04n":
                return "Broken clouds";
            case "09d":
            case "09n":
                return "Shower rain";
            case "10d":
            case "10n":
                return "Rain";
            case "11d":
            case "11n":
                return "Thunderstorm";
            case "13d":
            case "13n":
                return "Snow";
            case "50d":
            case "50n":
                return "Mist";
            default:
                return "Clear sky";
        }
    }
    
    /**
     * Load image from URL and set it to ImageView
     * Uses official OpenWeatherMap icon URLs directly from their CDN
     * Based on: https://openweathermap.org/weather-conditions
     * @param imageView ImageView to set the image
     * @param iconCode OpenWeatherMap icon code (e.g., "01d", "02n")
     */
    private void loadWeatherIconFromUrl(ImageView imageView, String iconCode) {
        if (imageView == null || iconCode == null) {
            Log.w(TAG, "ImageView or iconCode is null, using fallback icon");
            imageView.setImageResource(R.drawable.owm_01d);
            return;
        }
        
        String iconUrl = WeatherManager.getWeatherIconUrl(iconCode);
        Log.d(TAG, "🌤️ Loading OpenWeatherMap icon from URL: " + iconUrl);
        
        imageExecutor.execute(() -> {
            try {
                URL url = new URL(iconUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                
                if (bitmap != null) {
                    // Update UI on main thread
                    mainHandler.post(() -> {
                        imageView.setImageBitmap(bitmap);
                        Log.d(TAG, "✅ OpenWeatherMap icon loaded successfully: " + iconCode);
                    });
                } else {
                    Log.w(TAG, "Failed to decode bitmap from URL: " + iconUrl);
                    mainHandler.post(() -> {
                        imageView.setImageResource(R.drawable.owm_01d);
                    });
                }
                
                input.close();
                connection.disconnect();
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading OpenWeatherMap icon: " + e.getMessage());
                mainHandler.post(() -> {
                    imageView.setImageResource(R.drawable.owm_01d);
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (timeUpdateTimer != null) {
                timeUpdateTimer.cancel();
                timeUpdateTimer = null;
            }
            
            // Shutdown image executor
            if (imageExecutor != null && !imageExecutor.isShutdown()) {
                imageExecutor.shutdown();
                Log.d(TAG, "✅ Image executor shutdown");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
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
    
    private void showDeleteAccountBottomSheet() {
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete_account, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        
        // Initialize views
        EditText etPasswordConfirm = bottomSheetView.findViewById(R.id.et_password_confirm);
        Button btnDeleteAccount = bottomSheetView.findViewById(R.id.btn_delete_account);
        Button btnCancelDelete = bottomSheetView.findViewById(R.id.btn_cancel_delete);
        
        // Set click listeners
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = etPasswordConfirm.getText().toString().trim();
                if (enteredPassword.isEmpty()) {
                    etPasswordConfirm.setError("Please enter your password");
                    etPasswordConfirm.requestFocus();
                    return;
                }
                
                // Verify password and delete account
                verifyPasswordAndDeleteAccount(enteredPassword, bottomSheetDialog);
            }
        });
        
        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        
        // Show bottom sheet
        bottomSheetDialog.show();
    }
    
    private void verifyPasswordAndDeleteAccount(String enteredPassword, BottomSheetDialog bottomSheetDialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state
        Toast.makeText(this, "Verifying password...", Toast.LENGTH_SHORT).show();
        
        // Re-authenticate user with entered password
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.getEmail(), enteredPassword);
        
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password is correct, proceed with account deletion
                        deleteUserAccount(bottomSheetDialog);
                    } else {
                        // Password is incorrect
                        Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void deleteUserAccount(BottomSheetDialog bottomSheetDialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog before final deletion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Final Confirmation");
        builder.setMessage("Are you absolutely sure you want to delete your account? This action cannot be undone and all your data will be permanently lost.");
        builder.setPositiveButton("Yes, Delete Forever", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close bottom sheet first
                bottomSheetDialog.dismiss();
                
                // Show loading
                Toast.makeText(MainDashboard.this, "Deleting account...", Toast.LENGTH_SHORT).show();
                
                // Delete user data from Firestore first
                deleteUserDataFromFirestore(user.getUid(), () -> {
                    // Then delete the Firebase Auth account
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Clear local data
                                    clearUserData();
                                    
                                    // Show success message
                                    Toast.makeText(MainDashboard.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    
                                    // Navigate to login screen
                                    Intent intent = new Intent(MainDashboard.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainDashboard.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteUserDataFromFirestore(String userId, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("firebaseUid", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String docId = doc.getId();
                        
                        // Delete user document
                        db.collection("users").document(docId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User data deleted from Firestore");
                                    onComplete.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting user data from Firestore", e);
                                    // Still proceed with account deletion even if Firestore deletion fails
                                    onComplete.run();
                                });
                    } else {
                        Log.d(TAG, "No user document found in Firestore");
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying user document", e);
                    // Still proceed with account deletion even if query fails
                    onComplete.run();
                });
    }
    
    private void clearUserData() {
        // Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_profile_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Clear any other local data if needed
        Log.d(TAG, "User data cleared from local storage");
    }
    
    private void loadUserProfilePicture() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && profileButton != null) {
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
                                ProfilePictureCache.getInstance().loadProfilePicture(profileButton, profilePictureUrl);
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
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfilePicture: " + e.getMessage(), e);
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
                ProfilePictureCache.getInstance().loadProfilePicture(profileButton, uri.toString());
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

    private void loadImageFromUrl(String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    if (bitmap != null && profileButton != null) {
                        // Create circular bitmap
                        Bitmap circularBitmap = createCircularBitmap(bitmap);
                        profileButton.setImageBitmap(circularBitmap);
                        Log.d(TAG, "Profile picture loaded successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL", e);
                runOnUiThread(() -> setDefaultProfileIcon());
            }
        }).start();
    }

    private void loadImageIntoView(ImageView view, String imageUrl) {
        try {
            if (imageExecutor == null || imageExecutor.isShutdown()) {
                imageExecutor = Executors.newFixedThreadPool(3);
            }
            if (mainHandler == null) {
                mainHandler = new Handler(Looper.getMainLooper());
            }
            imageExecutor.execute(() -> {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();
                    connection.disconnect();
                    if (bitmap != null) {
                        mainHandler.post(() -> view.setImageBitmap(bitmap));
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    private Bitmap createCircularBitmap(Bitmap bitmap) {
        try {
            // Center-crop to square first to avoid distortion
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int squareSize = Math.min(width, height);
            int xOffset = (width - squareSize) / 2;
            int yOffset = (height - squareSize) / 2;

            Bitmap squareCropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

            int targetSize = 200; // Smaller size for dashboard profile button
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
        } catch (Exception e) {
            Log.e(TAG, "Error creating circular bitmap: " + e.getMessage(), e);
            return bitmap; // Return original bitmap if circular conversion fails
        }
    }

    private void setDefaultProfileIcon() {
        try {
            if (profileButton != null) {
                profileButton.setImageResource(R.drawable.ic_person);
                Log.d(TAG, "Default profile icon set");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting default profile icon: " + e.getMessage(), e);
        }
    }
    
    private void updateNotificationBadge() {
        try {
            if (alertsBadgeDashboard == null) return;
            
            // Use the same logic as AlertsActivity to count new announcements
            int newAnnouncementCount = countNewAnnouncementsFromDashboard();
            
            if (newAnnouncementCount > 0) {
                alertsBadgeDashboard.setText(String.valueOf(newAnnouncementCount));
                alertsBadgeDashboard.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing badge on dashboard with count: " + newAnnouncementCount);
            } else {
                alertsBadgeDashboard.setVisibility(View.GONE);
                Log.d(TAG, "Hiding badge on dashboard - no new announcements");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification badge on dashboard: " + e.getMessage(), e);
        }
    }
    
    private int countNewAnnouncementsFromDashboard() {
        try {
            // Use the same SharedPreferences as AlertsActivity
            long lastVisitTime = sharedPreferences.getLong(KEY_LAST_VISIT_TIME, 0);
            
            // If this is the first visit, don't show any badges
            if (lastVisitTime == 0) {
                return 0;
            }
            
            // For dashboard, we'll fetch announcements and count new ones
            fetchAndCountNewAnnouncementsFromDashboard(lastVisitTime);
            
            return 0; // Will be updated by the async fetch
        } catch (Exception e) {
            Log.e(TAG, "Error counting new announcements from dashboard: " + e.getMessage(), e);
            return 0;
        }
    }
    
    private void fetchAndCountNewAnnouncementsFromDashboard(long lastVisitTime) {
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
                            if (dateStr != null && isAnnouncementNewFromDashboard(dateStr, lastVisitTime)) {
                                newCount++;
                            }
                        }
                        
                        // Make newCount final for use in lambda
                        final int finalNewCount = newCount;
                        
                        // Update badge on UI thread
                        runOnUiThread(() -> {
                            if (alertsBadgeDashboard != null) {
                                if (finalNewCount > 0) {
                                    alertsBadgeDashboard.setText(String.valueOf(finalNewCount));
                                    alertsBadgeDashboard.setVisibility(View.VISIBLE);
                                } else {
                                    alertsBadgeDashboard.setVisibility(View.GONE);
                                }
                            }
                        });
                        
                        Log.d(TAG, "Found " + newCount + " new announcements from dashboard");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching announcements for badge from dashboard: " + e.getMessage(), e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching and counting new announcements from dashboard: " + e.getMessage(), e);
        }
    }
    
    private boolean isAnnouncementNewFromDashboard(String dateStr, long lastVisitTime) {
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
            Log.e(TAG, "Error checking if announcement is new from dashboard: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Initialize FCM token for push notifications
     * This ensures the user can receive notifications even if token wasn't initialized at login
     */
    private void initializeFCMToken() {
        try {
            // Request notification permission first (Android 13+)
            requestNotificationPermission();
            
            FCMTokenManager tokenManager = new FCMTokenManager(this);
            tokenManager.initializeFCMToken();
            Log.d(TAG, "✅ FCM token initialization started from MainDashboard");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FCM token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Request notification permission for Android 13+ (API 33+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        } else {
            Log.d(TAG, "Android version < 13, notification permission not required");
        }
    }
    
    /**
     * Request essential permissions (Location and Notification)
     * Only requests if not previously requested to avoid annoying users
     */
    private void requestEssentialPermissions() {
        try {
            Log.d(TAG, "Checking essential permissions...");
            
            SharedPreferences permPrefs = getSharedPreferences(PERMISSION_PREFS, MODE_PRIVATE);
            
            // Delay permission requests to avoid overwhelming user on first launch
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Request location permission first
                requestLocationPermissionIfNeeded(permPrefs);
                
                // Request notification permission after a delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    requestNotificationPermissionIfNeeded(permPrefs);
                }, 2000); // 2 second delay between permission requests
                
            }, 1000); // 1 second delay after dashboard loads
            
        } catch (Exception e) {
            Log.e(TAG, "Error requesting essential permissions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Request location permission if not already granted or requested
     */
    private void requestLocationPermissionIfNeeded(SharedPreferences permPrefs) {
        try {
            // Check if location permission is already granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Location permission already granted");
                return;
            }
            
            // Check if we already requested before
            boolean alreadyRequested = permPrefs.getBoolean(KEY_LOCATION_PERMISSION_REQUESTED, false);
            
            if (!alreadyRequested) {
                // Mark as requested
                SharedPreferences.Editor editor = permPrefs.edit();
                editor.putBoolean(KEY_LOCATION_PERMISSION_REQUESTED, true);
                editor.apply();
                
                // Request the permission directly (system dialog will appear)
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
                
                Log.d(TAG, "Location permission requested directly");
            } else {
                Log.d(TAG, "Location permission was already requested before, not asking again");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error requesting location permission: " + e.getMessage(), e);
        }
    }
    
    /**
     * Show dialog explaining why location permission is needed
     */
    private void showLocationPermissionDialog(SharedPreferences permPrefs) {
        try {
            new AlertDialog.Builder(this)
                .setTitle("Location Permission")
                .setMessage("AcciZard Lucban needs access to your location to:\n\n" +
                           "• Show your current location on the map\n" +
                           "• Help you report incidents at your exact location\n" +
                           "• Display nearby emergency facilities\n" +
                           "• Provide accurate weather information\n\n" +
                           "Your location data is only used within the app and never shared.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    // Mark as requested
                    SharedPreferences.Editor editor = permPrefs.edit();
                    editor.putBoolean(KEY_LOCATION_PERMISSION_REQUESTED, true);
                    editor.apply();
                    
                    // Request the permission
                    ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE);
                    
                    Log.d(TAG, "Location permission requested by user");
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    // Mark as requested so we don't ask again
                    SharedPreferences.Editor editor = permPrefs.edit();
                    editor.putBoolean(KEY_LOCATION_PERMISSION_REQUESTED, true);
                    editor.apply();
                    
                    Log.d(TAG, "Location permission denied by user");
                    Toast.makeText(this, "You can enable location later in Settings", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
                
        } catch (Exception e) {
            Log.e(TAG, "Error showing location permission dialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * Request notification permission if not already granted or requested
     */
    private void requestNotificationPermissionIfNeeded(SharedPreferences permPrefs) {
        try {
            // Only for Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Android version < 13, notification permission not required");
                return;
            }
            
            // Check if notification permission is already granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Notification permission already granted");
                return;
            }
            
            // Check if we already requested before
            boolean alreadyRequested = permPrefs.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false);
            
            if (!alreadyRequested) {
                // Mark as requested
                SharedPreferences.Editor editor = permPrefs.edit();
                editor.putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true);
                editor.apply();
                
                // Request the permission directly (system dialog will appear)
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
                
                Log.d(TAG, "Notification permission requested directly");
            } else {
                Log.d(TAG, "Notification permission was already requested before, not asking again");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error requesting notification permission: " + e.getMessage(), e);
        }
    }
    
    /**
     * Show dialog explaining why notification permission is needed
     */
    private void showNotificationPermissionDialog(SharedPreferences permPrefs) {
        try {
            new AlertDialog.Builder(this)
                .setTitle("Enable Notifications")
                .setMessage("Stay informed with AcciZard Lucban notifications:\n\n" +
                           "• Emergency alerts and warnings\n" +
                           "• Important announcements from authorities\n" +
                           "• Updates on your submitted reports\n" +
                           "• New chat messages\n" +
                           "• Severe weather alerts\n\n" +
                           "Never miss critical safety information!")
                .setPositiveButton("Enable", (dialog, which) -> {
                    // Mark as requested
                    SharedPreferences.Editor editor = permPrefs.edit();
                    editor.putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true);
                    editor.apply();
                    
                    // Request the permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_REQUEST_CODE);
                    }
                    
                    Log.d(TAG, "Notification permission requested by user");
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    // Mark as requested so we don't ask again
                    SharedPreferences.Editor editor = permPrefs.edit();
                    editor.putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true);
                    editor.apply();
                    
                    Log.d(TAG, "Notification permission denied by user");
                    Toast.makeText(this, "You can enable notifications later in Settings", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
                
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification permission dialog: " + e.getMessage(), e);
        }
    }
}