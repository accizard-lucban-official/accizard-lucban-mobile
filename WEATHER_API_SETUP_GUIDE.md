# Weather API Setup Guide

## 🌤️ Real Weather Data for Lucban, Quezon

Your AcciZard Lucban app now supports **real-time weather data** from OpenWeatherMap API! This provides accurate weather information specifically for Lucban, Quezon.

---

## 🔑 API Key Setup (Required)

### Step 1: Get Your Free API Key
1. **Visit**: https://openweathermap.org/api
2. **Sign up** for a free account
3. **Go to** "API keys" section in your account
4. **Copy** your API key

### Step 2: Add Your API Key
1. **Open**: `app/src/main/java/com/example/accizardlucban/WeatherManager.java`
2. **Find**: Line 21 with `API_KEY_PLACEHOLDER`
3. **Replace**: `WeatherConfig.API_KEY_PLACEHOLDER` with your actual API key

```java
// Change this line:
private static final String API_KEY = WeatherConfig.API_KEY_PLACEHOLDER;

// To this (replace YOUR_ACTUAL_API_KEY):
private static final String API_KEY = "YOUR_ACTUAL_API_KEY";
```

---

## 📊 Features

### ✅ Real Weather Data
- **Temperature**: Current temperature in Celsius
- **Humidity**: Real humidity percentage
- **Wind Speed**: Actual wind speed in km/h
- **Weather Icons**: Dynamic icons based on conditions
- **Precipitation**: Calculated precipitation chance

### ✅ Location Specific
- **Target Location**: Lucban, Quezon, Philippines
- **Coordinates**: 14.1133°N, 121.5564°E
- **Fallback**: Uses coordinates if city name fails

### ✅ Error Handling
- **API Failures**: Falls back to simulated data
- **Network Issues**: Graceful degradation
- **Invalid Data**: Safe error handling

---

## 🔄 Update Intervals

- **Weather Data**: Updates every 10 minutes
- **Time/Date**: Updates every minute
- **Automatic Refresh**: Background updates

---

## 📱 Weather Widget Display

```
┌─────────────────────────────────────┐
│ 23°  ☀️    Lucban, Quezon          │
│              Monday, 20 Oct         │
│                                     │
│ Wind    Humidity    Precip          │
│ 15 km/h   35%       10%             │
└─────────────────────────────────────┘
```

---

## 🛠️ Technical Details

### API Endpoints Used
- **Current Weather**: `/weather`
- **City Search**: `q=Lucban,Quezon,PH`
- **Coordinates**: `lat=14.1133&lon=121.5564`

### Data Format
- **Temperature**: Celsius (°C)
- **Wind Speed**: km/h
- **Humidity**: Percentage (%)
- **Pressure**: hPa

### Weather Icons Mapping
- **Clear Sky**: ☀️ (`01d`, `01n`)
- **Few Clouds**: ⛅ (`02d`, `02n`)
- **Scattered Clouds**: ☁️ (`03d`, `03n`)
- **Broken Clouds**: ☁️ (`04d`, `04n`)
- **Shower Rain**: 🌦️ (`09d`, `09n`)
- **Rain**: 🌧️ (`10d`, `10n`)
- **Thunderstorm**: ⛈️ (`11d`, `11n`)
- **Snow**: ❄️ (`13d`, `13n`)
- **Mist**: 🌫️ (`50d`, `50n`)

---

## 🚀 Free Tier Limits

- **1,000 API calls per day**
- **Current weather data**
- **5-day weather forecast**
- **Weather maps**

*Perfect for your AcciZard Lucban app!*

---

## 🔧 Troubleshooting

### API Key Issues
- **Error**: "Invalid API key"
- **Solution**: Check your API key in WeatherManager.java

### Network Issues
- **Error**: "Network error"
- **Solution**: App will show simulated data as fallback

### Location Issues
- **Error**: "City not found"
- **Solution**: App will use coordinates as fallback

---

## 📝 Code Files

### New Files Created
- `WeatherData.java` - Data model
- `WeatherApiService.java` - API interface
- `WeatherManager.java` - API manager
- `WeatherConfig.java` - Configuration

### Modified Files
- `MainDashboard.java` - Weather integration
- `build.gradle.kts` - Dependencies

---

## ✅ Ready to Use!

Once you add your API key, your weather widget will show **real-time weather data for Lucban, Quezon**! 

The app will automatically:
1. **Fetch** real weather data
2. **Update** every 10 minutes
3. **Fallback** to simulation if needed
4. **Display** accurate information

**Your AcciZard Lucban app now has professional weather functionality!** 🌤️








































