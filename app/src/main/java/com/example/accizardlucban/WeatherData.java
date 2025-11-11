package com.example.accizardlucban;

import com.google.gson.annotations.SerializedName;

/**
 * Weather data model for OpenWeatherMap API response
 */
public class WeatherData {
    
    @SerializedName("main")
    private Main main;
    
    @SerializedName("weather")
    private Weather[] weather;
    
    @SerializedName("wind")
    private Wind wind;
    
    @SerializedName("name")
    private String cityName;

    @SerializedName("rain")
    private Rain rain;

    @SerializedName("snow")
    private Snow snow;
    
    // Getters and setters
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
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }
    
    /**
     * Main weather data (temperature, humidity, etc.)
     */
    public static class Main {
        @SerializedName("temp")
        private double temp;
        
        @SerializedName("humidity")
        private int humidity;
        
        @SerializedName("feels_like")
        private double feelsLike;
        
        @SerializedName("pressure")
        private double pressure;
        
        // Getters and setters
        public double getTemp() {
            return temp;
        }
        
        public void setTemp(double temp) {
            this.temp = temp;
        }
        
        public int getHumidity() {
            return humidity;
        }
        
        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
        
        public double getFeelsLike() {
            return feelsLike;
        }
        
        public void setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
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
        @SerializedName("id")
        private int id;
        
        @SerializedName("main")
        private String main;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("icon")
        private String icon;
        
        // Getters and setters
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
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
     * Rain data (precipitation volume)
     */
    public static class Rain {
        @SerializedName("1h")
        private Double oneHour;

        @SerializedName("3h")
        private Double threeHour;

        public Double getOneHour() {
            return oneHour;
        }

        public void setOneHour(Double oneHour) {
            this.oneHour = oneHour;
        }

        public Double getThreeHour() {
            return threeHour;
        }

        public void setThreeHour(Double threeHour) {
            this.threeHour = threeHour;
        }
    }

    /**
     * Snow data (precipitation volume)
     */
    public static class Snow {
        @SerializedName("1h")
        private Double oneHour;

        @SerializedName("3h")
        private Double threeHour;

        public Double getOneHour() {
            return oneHour;
        }

        public void setOneHour(Double oneHour) {
            this.oneHour = oneHour;
        }

        public Double getThreeHour() {
            return threeHour;
        }

        public void setThreeHour(Double threeHour) {
            this.threeHour = threeHour;
        }
    }
}




