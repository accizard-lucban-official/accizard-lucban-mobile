# 📍 Facility Pin Type Field Guide

## 🎯 Overview

When creating facility pins in Firestore, the **`type` field** (not category) **must match exactly** the Emergency Support Facilities options that users can select in the mobile app. The app reads from the `type` field to determine the facility category.

## ✅ Valid Type Field Values

When creating a facility pin, set the **`type` field** to **exactly** one of these values (this is what the user clicks in Emergency Support Facilities):

1. **"Evacuation Centers"** (plural, with capital E and C)
2. **"Health Facilities"** (plural, with capital H and F)
3. **"Police Stations"** (plural, with capital P and S)
4. **"Fire Stations"** (plural, with capital F and S)
5. **"Government Offices"** (plural, with capital G and O)

## 📝 How to Create Facility Pins

### Option 1: Using FirestoreHelper (Recommended)

```java
// Create facility pin data with correct category
Map<String, Object> pinData = FirestoreHelper.createFacilityPinData(
    "Evacuation Centers",  // Category - must match Emergency Support Facilities
    "Abang - Brgy. Operations Center, Tayabas Rd, Lucban, 4328 Quezon", // Location name
    14.1136,  // Latitude
    121.5564, // Longitude
    "Capacity: 100" // Optional description
);

// Save to Firestore
FirestoreHelper.createFacilityPin(
    pinData,
    documentReference -> {
        // Success - pin created
        Log.d("Pin", "Facility pin created: " + documentReference.getId());
    },
    error -> {
        // Error
        Log.e("Pin", "Error creating facility pin", error);
    }
);
```

### Option 2: Direct Firestore (Web Admin Panel)

When creating pins directly in Firestore or from a web admin panel:

```javascript
// Example: Creating an Evacuation Center pin
{
  "type": "Evacuation Centers",  // ✅ PRIORITY: Set type field (matches Emergency Support Facilities)
  "category": "Evacuation Centers",  // ✅ Also set category for consistency
  "locationName": "Abang - Brgy. Operations Center, Tayabas Rd, Lucban, 4328 Quezon",
  "latitude": 14.1136,
  "longitude": 121.5564,
  "description": "Capacity: 100",
  "createdAt": Timestamp.now()
}
```

**Important:** The app reads from the `type` field for facilities, so make sure to set it!

### Option 3: Normalize Existing Type

If you have a type that might not match exactly, use the helper method:

```java
String userSelectedType = "evacuation center"; // User selected this in Emergency Support Facilities
String correctType = FirestorePinUpdater.getCorrectCategoryName(userSelectedType);
// Returns: "Evacuation Centers" ✅

// Now use correctType when creating the pin (set type field)
pinData.put("type", correctType);
pinData.put("category", correctType); // Also set category for consistency
```

## ⚠️ Important Notes

1. **Use `type` Field**: The app reads from the `type` field for facilities, not `category`:
   - ✅ Set `type: "Evacuation Centers"` (correct)
   - ❌ Only setting `category` without `type` (wrong - app won't recognize it)

2. **Case Sensitive**: Type values are case-sensitive. Use exact capitalization:
   - ✅ "Evacuation Centers" (correct)
   - ❌ "evacuation centers" (wrong)
   - ❌ "Evacuation Center" (wrong - missing 's')

3. **Plural Form**: All types use plural form:
   - ✅ "Evacuation Centers" (plural)
   - ❌ "Evacuation Center" (singular - wrong)

4. **Spacing**: Use exact spacing:
   - ✅ "Health Facilities" (correct)
   - ❌ "HealthFacilities" (wrong - no space)

## 🔄 Automatic Type Normalization

The `FirestorePinUpdater.getCorrectCategoryName()` method automatically converts common variations to the correct type value:

| User Input | Normalized To |
|------------|---------------|
| "evacuation center" | "Evacuation Centers" |
| "Evacuation Center" | "Evacuation Centers" |
| "health facility" | "Health Facilities" |
| "police station" | "Police Stations" |
| "fire station" | "Fire Stations" |
| "government office" | "Government Offices" |

## 📋 Complete Example

```java
// When user selects "Evacuation Centers" from Emergency Support Facilities
String selectedFacilityType = "Evacuation Centers"; // This is what user clicked

// Create pin data - type field will be set to match user selection
Map<String, Object> pinData = FirestoreHelper.createFacilityPinData(
    selectedFacilityType,  // Will be normalized if needed, saved to "type" field
    "Abang - Brgy. Operations Center, Tayabas Rd, Lucban, 4328 Quezon",
    14.1136,
    121.5564,
    "Capacity: 100"
);

// The pinData now has:
// - type: "Evacuation Centers" ✅ (matches what user clicked)
// - category: "Evacuation Centers" (for consistency)

// Add additional fields if needed
pinData.put("createdBy", userId);
pinData.put("createdByName", userName);

// Save to Firestore
FirestoreHelper.createFacilityPin(
    pinData,
    documentReference -> {
        // Success - pin created with correct type field
    },
    error -> {
        // Error handling
    }
);
```

## 🛠️ Updating Existing Pins

To update existing facility pins to use correct type field values, call:

```java
// In MapViewActivity
updateFacilityPinCategories();
```

This will automatically update all facility pins in Firestore to:
- Set the `type` field to match Emergency Support Facilities options
- Also update the `category` field for consistency

**Note:** The app reads from the `type` field for facilities, so this ensures all facility pins are properly recognized.

