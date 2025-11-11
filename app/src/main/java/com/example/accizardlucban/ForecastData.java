package com.example.accizardlucban;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 5-Day Weather Forecast data model for OpenWeatherMap API response
 */
public class ForecastData {
    
    @SerializedName("list")
    private List<ForecastItem> list;
    
    @SerializedName("city")
    private City city;
    
    // Getters and setters
    public List<ForecastItem> getList() {
        return list;
    }
    
    public void setList(List<ForecastItem> list) {
        this.list = list;
    }
    
    public City getCity() {
        return city;
    }
    
    public void setCity(City city) {
        this.city = city;
    }
    
    /**
     * Individual forecast item (3-hour interval)
     */
    public static class ForecastItem {
        @SerializedName("dt")
        private long dt;
        
        @SerializedName("main")
        private Main main;
        
        @SerializedName("weather")
        private Weather[] weather;
        
        @SerializedName("wind")
        private Wind wind;
        
        @SerializedName("dt_txt")
        private String dtTxt;
        
        // Getters and setters
        public long getDt() {
            return dt;
        }
        
        public void setDt(long dt) {
            this.dt = dt;
        }
        
        public Main getMain() {
            return main;
        }
        
        public void setMain(Main main) {
            this.main = main;
        }
        
        public Weather[] getWeather() {
            return weather;
        }
        
        public void setWeather(Weather[] weather) {
            this.weather = weather;
        }
        
        public Wind getWind() {
            return wind;
        }
        
        public void setWind(Wind wind) {
            this.wind = wind;
        }
        
        public String getDtTxt() {
            return dtTxt;
        }
        
        public void setDtTxt(String dtTxt) {
            this.dtTxt = dtTxt;
        }
    }
    
    /**
     * Main weather data (temperature, humidity, etc.)
     */
    public static class Main {
        @SerializedName("temp")
        private double temp;
        
        @SerializedName("temp_min")
        private double tempMin;
        
        @SerializedName("temp_max")
        private double tempMax;
        
        @SerializedName("humidity")
        private int humidity;
        
        @SerializedName("pressure")
        private double pressure;
        
        // Getters and setters
        public double getTemp() {
            return temp;
        }
        
        public void setTemp(double temp) {
            this.temp = temp;
        }
        
        public double getTempMin() {
            return tempMin;
        }
        
        public void setTempMin(double tempMin) {
            this.tempMin = tempMin;
        }
        
        public double getTempMax() {
            return tempMax;
        }
        
        public void setTempMax(double tempMax) {
            this.tempMax = tempMax;
        }
        
        public int getHumidity() {
            return humidity;
        }
        
        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
        
        public double getPressure() {
            return pressure;
        }
        
        public void setPressure(double pressure) {
            this.pressure = pressure;
        }
    }
    
    /**
     * Weather condition data
     */
    public static class Weather {
        @SerializedName("main")
        private String main;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("icon")
        private String icon;
        
        // Getters and setters
        public String getMain() {
            return main;
        }
        
        public void setMain(String main) {
            this.main = main;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
    
    /**
     * Wind data
     */
    public static class Wind {
        @SerializedName("speed")
        private double speed;
        
        @SerializedName("deg")
        private int deg;
        
        // Getters and setters
        public double getSpeed() {
            return speed;
        }
        
        public void setSpeed(double speed) {
            this.speed = speed;
        }
        
        public int getDeg() {
            return deg;
        }
        
        public void setDeg(int deg) {
            this.deg = deg;
        }
    }
    
    /**
     * City information
     */
    public static class City {
        @SerializedName("name")
        private String name;
        
        @SerializedName("country")
        private String country;
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
    }
}




























