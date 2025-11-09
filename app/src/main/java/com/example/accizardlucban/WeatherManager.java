package com.example.accizardlucban;

import android.content.Context;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

/**
 * Weather Manager class to handle weather API calls
 */
public class WeatherManager {
    
    private static final String TAG = "WeatherManager";
    private static final String BASE_URL = WeatherConfig.API_BASE_URL;
    public static final String API_KEY = "6c98b80e4f499d9dd3de9ca079f274ea"; // OpenWeatherMap API key
    
    // Lucban, Quezon coordinates
    private static final double LUCBAN_LAT = WeatherConfig.LUCBAN_LATITUDE;
    private static final double LUCBAN_LON = WeatherConfig.LUCBAN_LONGITUDE;
    
    private WeatherApiService weatherApiService;
    private Context context;
    
    public WeatherManager(Context context) {
        this.context = context;
        setupRetrofit();
    }
    
    private void setupRetrofit() {
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create OkHttp client with timeout
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        weatherApiService = retrofit.create(WeatherApiService.class);
    }
    
    /**
     * Get current weather for Lucban, Quezon
     * @param callback Weather callback interface
     */
    public void getLucbanWeather(WeatherCallback callback) {
        // Try by city name first - specifically for Lucban, Quezon, Philippines
        Call<WeatherData> call = weatherApiService.getCurrentWeather(
                WeatherConfig.LUCBAN_FULL_NAME, // "Lucban,Quezon,PH" - precise location
                API_KEY,
                WeatherConfig.UNITS_METRIC // Celsius
        );
        
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData weatherData = response.body();
                    Log.d(TAG, "✅ Lucban, Quezon weather API success: " + weatherData.getCityName());
                    callback.onSuccess(weatherData);
                } else {
                    Log.e(TAG, "❌ Lucban weather API error: " + response.code() + " - " + response.message());
                    // Fallback to coordinates
                    getLucbanWeatherByCoords(callback);
                }
            }
            
            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e(TAG, "Weather API failure: " + t.getMessage(), t);
                // Fallback to coordinates
                getLucbanWeatherByCoords(callback);
            }
        });
    }
    
    /**
     * Get weather by coordinates as fallback
     * @param callback Weather callback interface
     */
    private void getLucbanWeatherByCoords(WeatherCallback callback) {
        Call<WeatherData> call = weatherApiService.getCurrentWeatherByCoords(
                LUCBAN_LAT,
                LUCBAN_LON,
                API_KEY,
                WeatherConfig.UNITS_METRIC
        );
        
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData weatherData = response.body();
                    Log.d(TAG, "Weather API success by coordinates: " + weatherData.getCityName());
                    callback.onSuccess(weatherData);
                } else {
                    Log.e(TAG, "Weather API error by coordinates: " + response.code());
                    callback.onError("Unable to fetch weather data");
                }
            }
            
            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e(TAG, "Weather API failure by coordinates: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Weather callback interface
     */
    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String errorMessage);
    }
    
    /**
     * Forecast callback interface
     */
    public interface ForecastCallback {
        void onSuccess(ForecastData forecastData);
        void onError(String errorMessage);
    }
    
    /**
     * Get 5-day forecast for Lucban, Quezon
     * @param callback Forecast callback interface
     */
    public void getLucbanForecast(ForecastCallback callback) {
        // Try by city name first - specifically for Lucban, Quezon, Philippines
        Call<ForecastData> call = weatherApiService.get5DayForecast(
                WeatherConfig.LUCBAN_FULL_NAME, // "Lucban,Quezon,PH" - precise location
                API_KEY,
                WeatherConfig.UNITS_METRIC
        );
        
        call.enqueue(new Callback<ForecastData>() {
            @Override
            public void onResponse(Call<ForecastData> call, Response<ForecastData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastData forecastData = response.body();
                    Log.d(TAG, "✅ Lucban, Quezon forecast API success: " + forecastData.getCity().getName());
                    callback.onSuccess(forecastData);
                } else {
                    Log.e(TAG, "❌ Lucban forecast API error: " + response.code() + " - " + response.message());
                    // Fallback to coordinates
                    getLucbanForecastByCoords(callback);
                }
            }
            
            @Override
            public void onFailure(Call<ForecastData> call, Throwable t) {
                Log.e(TAG, "Forecast API failure: " + t.getMessage(), t);
                // Fallback to coordinates
                getLucbanForecastByCoords(callback);
            }
        });
    }
    
    /**
     * Get forecast by coordinates as fallback
     * @param callback Forecast callback interface
     */
    private void getLucbanForecastByCoords(ForecastCallback callback) {
        Call<ForecastData> call = weatherApiService.get5DayForecastByCoords(
                LUCBAN_LAT,
                LUCBAN_LON,
                API_KEY,
                WeatherConfig.UNITS_METRIC
        );
        
        call.enqueue(new Callback<ForecastData>() {
            @Override
            public void onResponse(Call<ForecastData> call, Response<ForecastData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastData forecastData = response.body();
                    Log.d(TAG, "Forecast API success by coordinates: " + forecastData.getCity().getName());
                    callback.onSuccess(forecastData);
                } else {
                    Log.e(TAG, "Forecast API error by coordinates: " + response.code());
                    callback.onError("Unable to fetch forecast data");
                }
            }
            
            @Override
            public void onFailure(Call<ForecastData> call, Throwable t) {
                Log.e(TAG, "Forecast API failure by coordinates: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get official OpenWeatherMap icon URL
     * Uses the official OpenWeatherMap icon URLs directly from their CDN
     * Based on: https://openweathermap.org/weather-conditions
     * @param iconCode Icon code from API (e.g., "01d", "02n")
     * @return Official OpenWeatherMap icon URL
     */
    public static String getWeatherIconUrl(String iconCode) {
        if (iconCode == null) {
            iconCode = "01d"; // Default to clear sky day
        }
        
        // Official OpenWeatherMap icon URL pattern
        // https://openweathermap.org/img/wn/{icon_code}@2x.png
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }
    
    /**
     * Test API key validity by making a simple weather request
     * @param callback Test callback interface
     */
    public void testApiKey(TestCallback callback) {
        Log.d(TAG, "🧪 Testing API key validity...");
        
        // Make a simple current weather request to test the API key
        Call<WeatherData> call = weatherApiService.getCurrentWeather(
                WeatherConfig.LUCBAN_FULL_NAME,
                API_KEY,
                WeatherConfig.UNITS_METRIC
        );
        
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ API key is valid! Weather data received successfully");
                    callback.onSuccess("API key is working correctly");
                } else {
                    Log.e(TAG, "❌ API key test failed: " + response.code() + " - " + response.message());
                    callback.onError("API key test failed: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e(TAG, "❌ API key test failed with exception: " + t.getMessage(), t);
                callback.onError("API key test failed: " + t.getMessage());
            }
        });
    }
    
    /**
     * Test callback interface
     */
    public interface TestCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    
    /**
     * Get local drawable resource ID for weather icon
     * Maps OpenWeatherMap icon codes to local drawable resources
     * @param iconCode OpenWeatherMap icon code (e.g., "01d", "02n")
     * @return Local drawable resource ID
     */
    public static int getLocalWeatherIconResource(String iconCode) {
        if (iconCode == null) {
            return R.drawable.clear; // Default to clear sky
        }
        
        // Map OpenWeatherMap icon codes to local drawable resources
        switch (iconCode) {
            // Clear sky
            case "01d": // Clear sky day
            case "01n": // Clear sky night
                return R.drawable.clear;
                
            // Few clouds
            case "02d": // Few clouds day
            case "02n": // Few clouds night
                return R.drawable.few_clouds;
                
            // Scattered clouds
            case "03d": // Scattered clouds day
            case "03n": // Scattered clouds night
                return R.drawable.scattered_clouds;
                
            // Broken clouds
            case "04d": // Broken clouds day
            case "04n": // Broken clouds night
                return R.drawable.broken_clouds;
                
            // Shower rain
            case "09d": // Shower rain day
            case "09n": // Shower rain night
                return R.drawable.shower_rain;
                
            // Rain
            case "10d": // Rain day
            case "10n": // Rain night
                return R.drawable.rain;
                
            // Thunderstorm
            case "11d": // Thunderstorm day
            case "11n": // Thunderstorm night
                return R.drawable.thunderstorm;
                
            // Snow
            case "13d": // Snow day
            case "13n": // Snow night
                return R.drawable.snow;
                
            // Mist/Fog
            case "50d": // Mist day
            case "50n": // Mist night
                return R.drawable.mist;
                
            // Default fallback
            default:
                return R.drawable.clear;
        }
    }
    
    /**
     * Get weather icon resource ID based on OpenWeatherMap icon code
     * Uses official OpenWeatherMap-style icons for accurate weather representation
     * Based on: https://openweathermap.org/weather-conditions
     * @param iconCode Icon code from API (e.g., "01d", "02n")
     * @return Android drawable resource ID
     */
    public static int getWeatherIconResource(String iconCode) {
        if (iconCode == null) return R.drawable.owm_01d; // Default to clear sky day
        
        switch (iconCode) {
            // Group 800: Clear sky (800)
            case "01d": // clear sky day
                return R.drawable.owm_01d;
            case "01n": // clear sky night
                return R.drawable.owm_01n;
            
            // Group 80x: Clouds (801-804)
            case "02d": // few clouds day (801: 11-25%)
                return R.drawable.owm_02d;
            case "02n": // few clouds night (801: 11-25%)
                return R.drawable.owm_02n;
            
            case "03d": // scattered clouds day (802: 25-50%)
                return R.drawable.owm_03d;
            case "03n": // scattered clouds night (802: 25-50%)
                return R.drawable.owm_03n;
            
            case "04d": // broken clouds day (803: 51-84% or 804: 85-100%)
                return R.drawable.owm_04d;
            case "04n": // broken clouds night (803: 51-84% or 804: 85-100%)
                return R.drawable.owm_04n;
            
            // Group 3xx: Drizzle (300-321) - uses shower rain icons
            case "09d": // shower rain day (drizzle conditions)
                return R.drawable.owm_09d;
            case "09n": // shower rain night (drizzle conditions)
                return R.drawable.owm_09n;
            
            // Group 5xx: Rain (500-531)
            case "10d": // rain day
                return R.drawable.owm_10d;
            case "10n": // rain night
                return R.drawable.owm_10n;
            
            // Group 2xx: Thunderstorm (200-232)
            case "11d": // thunderstorm day
                return R.drawable.owm_11d;
            case "11n": // thunderstorm night
                return R.drawable.owm_11n;
            
            // Group 6xx: Snow (600-622) - rare in Philippines, use clear sky
            case "13d": // snow day
            case "13n": // snow night
                return R.drawable.owm_01d; // Use clear sky as fallback for tropical climate
            
            // Group 7xx: Atmosphere (701-781) - mist, fog, haze, etc.
            case "50d": // mist/fog day
                return R.drawable.owm_50d;
            case "50n": // mist/fog night
                return R.drawable.owm_50n;
            
            // Default fallback - clear sky for unknown conditions
            default:
                Log.w(TAG, "Unknown weather icon code: " + iconCode + ", using clear sky fallback");
                return R.drawable.owm_01d; // Default to clear sky day
        }
    }
    
    /**
     * Get weather condition description based on OpenWeatherMap weather ID
     * Based on: https://openweathermap.org/weather-conditions
     * @param weatherId Weather ID from API (e.g., 800, 500, 200)
     * @return Human-readable weather description
     */
    public static String getWeatherDescription(int weatherId) {
        switch (weatherId) {
            // Group 800: Clear
            case 800:
                return "Clear sky";
            
            // Group 80x: Clouds
            case 801:
                return "Few clouds";
            case 802:
                return "Scattered clouds";
            case 803:
                return "Broken clouds";
            case 804:
                return "Overcast clouds";
            
            // Group 2xx: Thunderstorm
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
                return "Thunderstorm";
            
            // Group 3xx: Drizzle
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                return "Drizzle";
            
            // Group 5xx: Rain
            case 500:
                return "Light rain";
            case 501:
                return "Moderate rain";
            case 502:
                return "Heavy rain";
            case 503:
                return "Very heavy rain";
            case 504:
                return "Extreme rain";
            case 511:
                return "Freezing rain";
            case 520:
            case 521:
            case 522:
            case 531:
                return "Shower rain";
            
            // Group 6xx: Snow (rare in Philippines)
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 613:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                return "Snow";
            
            // Group 7xx: Atmosphere
            case 701:
                return "Mist";
            case 711:
                return "Smoke";
            case 721:
                return "Haze";
            case 731:
            case 761:
                return "Dust";
            case 741:
                return "Fog";
            case 751:
                return "Sand";
            case 762:
                return "Volcanic ash";
            case 771:
                return "Squalls";
            case 781:
                return "Tornado";
            
            default:
                return "Unknown weather condition";
        }
    }
    
    /**
     * Format temperature for display
     * @param temp Temperature in Celsius
     * @return Formatted temperature string (e.g., "23°")
     */
    public static String formatTemperature(double temp) {
        return String.format("%.0f°", temp);
    }
    
    /**
     * Format wind speed for display
     * @param speed Wind speed in m/s
     * @return Formatted wind string (e.g., "15 km/h")
     */
    public static String formatWindSpeed(double speed) {
        // Convert m/s to km/h
        double kmh = speed * 3.6;
        return String.format("%.0f km/h", kmh);
    }
    
    /**
     * Format humidity for display
     * @param humidity Humidity percentage
     * @return Formatted humidity string (e.g., "65%")
     */
    public static String formatHumidity(int humidity) {
        return humidity + "%";
    }
    
    /**
     * Calculate precipitation chance (simplified)
     * @param humidity Humidity percentage
     * @param pressure Atmospheric pressure
     * @return Precipitation percentage
     */
    public static String calculatePrecipitation(int humidity, double pressure) {
        // Simple calculation based on humidity and pressure
        // This is a simplified approach - real precipitation data would come from API
        int precipitation = 0;
        
        if (humidity > 80) {
            precipitation = humidity - 60; // Higher humidity = higher chance
        } else if (humidity > 70) {
            precipitation = humidity - 50;
        } else if (humidity > 60) {
            precipitation = humidity - 40;
        }
        
        // Adjust based on pressure (lower pressure = higher chance)
        if (pressure < 1013) {
            precipitation += 10;
        }
        
        // Cap at 100%
        precipitation = Math.min(precipitation, 100);
        
        return precipitation + "%";
    }
    
    /**
     * Format temperature range for forecast display
     * @param maxTemp Maximum temperature
     * @param minTemp Minimum temperature
     * @return Formatted temperature string (e.g., "30° 24°")
     */
    public static String formatTemperatureRange(double maxTemp, double minTemp) {
        return String.format("%.0f° %.0f°", maxTemp, minTemp);
    }
    
    /**
     * Get day name from timestamp
     * @param timestamp Unix timestamp
     * @param dayOffset Day offset (0 = today, 1 = tomorrow, etc.)
     * @return Day name (e.g., "Today", "Wed 22")
     */
    public static String getDayName(long timestamp, int dayOffset) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, dayOffset);
        
        java.text.SimpleDateFormat dayFormat;
        if (dayOffset == 0) {
            return "Today";
        } else {
            dayFormat = new java.text.SimpleDateFormat("EEE d", java.util.Locale.getDefault());
            return dayFormat.format(calendar.getTime());
        }
    }
    
    /**
     * Process forecast data to get daily summaries
     * @param forecastData Raw forecast data from API
     * @return Array of daily forecast summaries
     */
    public static DailyForecast[] processForecastData(ForecastData forecastData) {
        if (forecastData == null || forecastData.getList() == null) {
            return new DailyForecast[0];
        }
        
        java.util.List<ForecastData.ForecastItem> items = forecastData.getList();
        java.util.Map<String, DailyForecast> dailyData = new java.util.HashMap<>();
        
        // Group forecast items by day
        for (ForecastData.ForecastItem item : items) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(item.getDt() * 1000);
            
            String dayKey = String.format("%d-%d-%d", 
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            );
            
            DailyForecast daily = dailyData.get(dayKey);
            if (daily == null) {
                daily = new DailyForecast();
                daily.timestamp = item.getDt();
                daily.maxTemp = item.getMain().getTempMax();
                daily.minTemp = item.getMain().getTempMin();
                daily.icon = item.getWeather()[0].getIcon();
                dailyData.put(dayKey, daily);
            } else {
                // Update max/min temperatures
                daily.maxTemp = Math.max(daily.maxTemp, item.getMain().getTempMax());
                daily.minTemp = Math.min(daily.minTemp, item.getMain().getTempMin());
            }
        }
        
        // Convert to array and sort by timestamp
        DailyForecast[] result = dailyData.values().toArray(new DailyForecast[0]);
        java.util.Arrays.sort(result, (a, b) -> Long.compare(a.timestamp, b.timestamp));
        
        return result;
    }
    
    /**
     * Daily forecast summary class
     */
    public static class DailyForecast {
        public long timestamp;
        public double maxTemp;
        public double minTemp;
        public String icon;
    }
}
