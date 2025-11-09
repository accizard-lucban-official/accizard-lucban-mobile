package com.example.accizardlucban;

/**
 * Weather API Configuration
 * 
 * ✅ OpenWeatherMap API key is now active!
 * API Key: 6c98b80e4f499d9dd3de9ca079f274ea
 * 
 * 🌤️ WEATHER FOCUSED ON LUCBAN, QUEZON, PHILIPPINES 🌤️
 * 
 * Free tier includes:
 * - 1,000 API calls per day
 * - Current weather data
 * - 5-day weather forecast
 * - Weather maps
 * 
 * Your app will now fetch real weather data specifically for Lucban, Quezon!
 * Coordinates: 14.1133°N, 121.5564°E
 */
public class WeatherConfig {
    
    // OpenWeatherMap API Configuration
    public static final String API_KEY_PLACEHOLDER = "6c98b80e4f499d9dd3de9ca079f274ea"; // Active API key
    
    // Lucban, Quezon coordinates (precise location)
    public static final double LUCBAN_LATITUDE = 14.1133;
    public static final double LUCBAN_LONGITUDE = 121.5564;
    
    // Lucban location identifiers for API calls
    public static final String LUCBAN_CITY_NAME = "Lucban";
    public static final String LUCBAN_STATE = "Quezon";
    public static final String LUCBAN_FULL_NAME = "Lucban,Quezon,PH";
    
    // API Settings
    public static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String UNITS_METRIC = "metric"; // Celsius
    public static final String COUNTRY_CODE = "PH"; // Philippines
    
    // Update intervals
    public static final long WEATHER_UPDATE_INTERVAL = 10 * 60 * 1000; // 10 minutes
    public static final long TIME_UPDATE_INTERVAL = 60 * 1000; // 1 minute
    
    // Fallback weather data (when API is unavailable)
    public static final String FALLBACK_TEMPERATURE = "25°";
    public static final String FALLBACK_HUMIDITY = "70%";
    public static final String FALLBACK_WIND = "8 km/h";
    public static final String FALLBACK_PRECIPITATION = "15%";
    public static final String FALLBACK_LOCATION = "Lucban, Quezon";
}




