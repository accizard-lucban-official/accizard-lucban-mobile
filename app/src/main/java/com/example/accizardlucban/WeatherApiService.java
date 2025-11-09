package com.example.accizardlucban;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Weather API service interface for OpenWeatherMap
 */
public interface WeatherApiService {
    
    /**
     * Get current weather data for a specific city
     * @param city City name (e.g., "Lucban,Quezon")
     * @param appId OpenWeatherMap API key
     * @param units Temperature units (metric for Celsius)
     * @return WeatherData object
     */
    @GET("weather")
    Call<WeatherData> getCurrentWeather(
            @Query("q") String city,
            @Query("appid") String appId,
            @Query("units") String units
    );
    
    /**
     * Get current weather data by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param appId OpenWeatherMap API key
     * @param units Temperature units (metric for Celsius)
     * @return WeatherData object
     */
    @GET("weather")
    Call<WeatherData> getCurrentWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String appId,
            @Query("units") String units
    );
    
    /**
     * Get 5-day weather forecast by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param appId OpenWeatherMap API key
     * @param units Temperature units (metric for Celsius)
     * @return ForecastData object
     */
    @GET("forecast")
    Call<ForecastData> get5DayForecastByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String appId,
            @Query("units") String units
    );
    
    /**
     * Get 5-day weather forecast for a specific city
     * @param city City name (e.g., "Lucban,Quezon")
     * @param appId OpenWeatherMap API key
     * @param units Temperature units (metric for Celsius)
     * @return ForecastData object
     */
    @GET("forecast")
    Call<ForecastData> get5DayForecast(
            @Query("q") String city,
            @Query("appid") String appId,
            @Query("units") String units
    );
}
