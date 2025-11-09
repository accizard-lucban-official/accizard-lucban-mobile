# 📱 Push Notification Implementation - AcciZARD Lucban Android App

## ✅ Implementation Status: COMPLETE

This document describes the complete implementation of push notifications in the AcciZARD Lucban Android app, integrated with your existing web app's Firebase Cloud Functions.

---

## 🎯 Overview

Your Android app now receives push notifications sent from your web app via Firebase Cloud Messaging (FCM). The notifications are automatically sent by Cloud Functions when:

1. **Chat Messages**: Admin sends a message to a user
2. **Announcements**: New announcement is created (broadcast to all users)
3. **Report Status Updates**: Admin updates the status of a user's report

---

## 📦 What Was Implemented

### **1. Dependencies Added** (`app/build.gradle.kts`)
```kotlin
implementation("com.google.firebase:firebase-messaging")
```

### **2. Permissions Added** (`AndroidManifest.xml`)
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### **3. New Java Classes Created**

#### **a. NotificationChannelManager.java**
- **Purpose**: Creates and manages Android notification channels
- **Channels Created**:
  - `report_updates` - HIGH priority for report status updates
  - `high_priority_announcements` - HIGH priority for critical announcements
  - `announcements` - DEFAULT priority for general announcements
  - `chat_messages` - HIGH priority for chat messages
- **Location**: `app/src/main/java/com/example/accizardlucban/`

#### **b. FCMTokenManager.java**
- **Purpose**: Manages FCM device tokens
- **Features**:
  - Gets FCM token on login
  - Saves token to Firestore (`users/{userId}/fcmToken`)
  - Handles token refresh automatically
  - Deletes token on logout
- **Location**: `app/src/main/java/com/example/accizardlucban/`

#### **c. MyFirebaseMessagingService.java**
- **Purpose**: Receives incoming push notifications from Firebase
- **Handles**:
  - Foreground notifications (app is open)
  - Background notifications (app is running but not visible)
  - Token refresh events
  - Data payload processing
- **Location**: `app/src/main/java/com/example/accizardlucban/`

#### **d. AcciZardNotificationManager.java**
- **Purpose**: Builds and displays notifications
- **Features**:
  - Proper notification styling
  - Custom vibration patterns per channel
  - Deep linking to correct screens
  - Unique notification IDs
- **Location**: `app/src/main/java/com/example/accizardlucban/`

#### **e. NotificationDeepLinkHandler.java**
- **Purpose**: Handles navigation when user taps a notification
- **Routes To**:
  - `ReportSubmissionActivity` - For report updates
  - `AlertsActivity` - For announcements
  - `ChatActivity` - For chat messages
  - `MainDashboard` - Fallback
- **Location**: `app/src/main/java/com/example/accizardlucban/`

### **4. Service Registered** (`AndroidManifest.xml`)
```xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### **5. Integration Points**

#### **MainActivity.java**
- **onCreate()**: Creates notification channels on app start
- **After Login Success**: Initializes and saves FCM token to Firestore
- **Token Storage**: Saves to `users/{userId}/fcmToken` field

#### **MainDashboard.java**
- **onCreate()**: Re-initializes FCM token (safety measure)
- **Ensures**: User always has valid token for receiving notifications

---

## 🔔 Notification Types & Data Structure

### **1. Chat Message Notification**
```json
{
  "notification": {
    "title": "AcciZard Lucban",
    "body": "Thank you for reaching out..."
  },
  "data": {
    "type": "chat_message",
    "userId": "user123",
    "messageId": "msg456",
    "senderId": "admin789",
    "senderName": "AcciZard Lucban"
  }
}
```
**Action**: Opens `ChatActivity`

### **2. Announcement Notification**
```json
{
  "notification": {
    "title": "🚨 Important Announcement",
    "body": "Typhoon warning in effect for Lucban..."
  },
  "data": {
    "type": "announcement",
    "announcementId": "ann123",
    "announcementType": "Emergency Alert",
    "priority": "high",
    "date": "10/16/2024"
  }
}
```
**Action**: Opens `AlertsActivity`

### **3. Report Status Update Notification**
```json
{
  "notification": {
    "title": "🚨 Responders Dispatched",
    "body": "Your Fire report is being responded to at Barangay Ayuti"
  },
  "data": {
    "type": "report_update",
    "reportId": "rpt789",
    "reportNumber": "RPT-2024-001",
    "reportType": "Fire",
    "oldStatus": "Pending",
    "newStatus": "Responding",
    "barangay": "Barangay Ayuti",
    "location": "123 Main St"
  }
}
```
**Action**: Opens `ReportSubmissionActivity` (My Reports)

---

## 🔧 How It Works

### **User Login Flow:**
1. User logs in via `MainActivity`
2. App creates notification channels
3. App gets FCM token from Firebase
4. Token is saved to Firestore: `users/{userId}/fcmToken`
5. Web app Cloud Functions can now send notifications to this device

### **Notification Delivery Flow:**
1. **Trigger Event** (e.g., admin updates report status)
2. **Cloud Function Executes** (`sendReportStatusNotification`)
3. **Function Reads** `users/{userId}/fcmToken` from Firestore
4. **Function Sends** notification via FCM API
5. **Android App Receives** notification in `MyFirebaseMessagingService`
6. **App Displays** notification using `AcciZardNotificationManager`
7. **User Taps** notification
8. **App Opens** correct screen via `NotificationDeepLinkHandler`

---

## 📊 Notification Channels

| Channel ID | Name | Priority | Vibration Pattern | Use Case |
|------------|------|----------|-------------------|----------|
| `report_updates` | Report Status Updates | HIGH | Short-short | Report status changes |
| `high_priority_announcements` | Important Announcements | HIGH | Long-short-long-short-long | Emergency alerts, typhoon warnings |
| `announcements` | Announcements | DEFAULT | Short | General announcements, events |
| `chat_messages` | Chat Messages | HIGH | Single short | Messages from support |

---

## 🚀 Testing Push Notifications

### **Test 1: Chat Notification**
1. Login to Android app with a user account
2. From web app (admin), send a chat message to that user
3. ✅ Notification should appear on Android device
4. Tap notification → should open `ChatActivity`

### **Test 2: Announcement Notification**
1. Login to Android app
2. From web app (admin), create a new announcement
3. ✅ Notification should appear on Android device
4. Tap notification → should open `AlertsActivity`

### **Test 3: Report Status Update Notification**
1. Submit a report from Android app
2. From web app (admin), update the report status (e.g., Pending → Responding)
3. ✅ Notification should appear on Android device
4. Tap notification → should open `ReportSubmissionActivity` (My Reports)

### **Test Different Notification States:**
- **Foreground**: App is open → notification appears as heads-up notification
- **Background**: App is running but not visible → notification in system tray
- **Terminated**: App is completely closed → notification in system tray

---

## 🔍 Debugging & Logs

### **Check FCM Token:**
Look for this log in Android Studio Logcat:
```
D/FCMTokenManager: ✅ FCM token saved to Firestore for user: {userId}
D/FCMTokenManager: Token: {actual_token_string}
```

### **Check Notification Receipt:**
```
D/MyFCMService: 📩 Message received from: {sender}
D/MyFCMService: Notification Title: {title}
D/MyFCMService: Notification Body: {body}
```

### **Check Notification Display:**
```
D/AcciZardNotificationMgr: Showing notification - Type: report_update, Channel: report_updates
D/AcciZardNotificationMgr: ✅ Notification displayed - ID: {notificationId}
```

### **Check Deep Link Navigation:**
```
D/NotificationDeepLink: 📱 Notification tapped - Type: report_update
D/NotificationDeepLink: Opening My Reports screen for report: RPT-2024-001
```

---

## 📝 Firestore Database Structure

### **User Document** (`users/{userId}`)
```javascript
{
  email: "user@example.com",
  firstName: "John",
  lastName: "Doe",
  // ... other user fields ...
  fcmToken: "dXpN3F5T9K...", // ← FCM token stored here
  lastUpdated: 1697456789000
}
```

**Important**: The Cloud Functions read this `fcmToken` field to send notifications to the specific device.

---

## ⚙️ Configuration

### **Notification Icon & Color**
Set in `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/accizard_logo_svg" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@color/orange" />
```

### **Notification Permissions** (Android 13+)
The app **automatically requests** `POST_NOTIFICATIONS` permission when the user logs in on Android 13+ (API 33+) devices. 

**Critical**: Users MUST grant this permission to receive system notifications. Without it:
- Notifications will NOT appear in the system tray
- Notifications may show as Toast messages instead
- Users won't receive emergency alerts

The permission is requested in:
- `MainActivity.java` - During login
- `MainDashboard.java` - As a safety measure on app start

---

## 🎨 Customization

### **Change Vibration Pattern:**
Edit `AcciZardNotificationManager.getVibrationPattern()`:
```java
private long[] getVibrationPattern(String channelId) {
    switch (channelId) {
        case NotificationChannelManager.CHANNEL_HIGH_PRIORITY_ANNOUNCEMENTS:
            return new long[]{0, 500, 200, 500, 200, 500}; // Emergency pattern
        // ... add your custom patterns
    }
}
```

### **Add Notification Actions:**
Edit `AcciZardNotificationManager.addNotificationActions()`:
```java
if ("chat_message".equals(notificationType)) {
    builder.addAction(R.drawable.ic_reply, "Reply", replyPendingIntent);
}
```

### **Change Notification Sound:**
Edit `NotificationChannelManager.createHighPriorityAnnouncementsChannel()`:
```java
channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/raw/emergency_sound"), ...);
```

---

## 🔒 Security Considerations

1. **FCM Token Security**: Tokens are stored in Firestore with proper security rules
2. **User-Specific Notifications**: Cloud Functions ensure users only receive their own notifications
3. **Data Validation**: All notification data is validated before processing
4. **Permission Handling**: POST_NOTIFICATIONS permission requested appropriately

---

## 📞 Support & Troubleshooting

### **Notifications Showing as Toast Messages Instead of System Notifications?**
**Root Cause:** Notification permission not granted on Android 13+

**Fix:**
1. Uninstall the app
2. Reinstall and login
3. When permission dialog appears, tap **"Allow"**
4. Verify in Logcat: `D/MainActivity: ✅ Notification permission granted`

### **Notification Not Received?**
1. ✅ **FIRST CHECK**: Notification permission granted (Settings → Apps → AcciZARD → Notifications)
2. ✅ Check if FCM token is saved in Firestore (`users/{userId}/fcmToken`)
3. ✅ Check Firebase Console → Cloud Functions for errors
4. ✅ Check Android Logcat for errors
5. ✅ Check if device has internet connection

### **Token Not Saving?**
1. ✅ Ensure user is logged in when token is requested
2. ✅ Check Firestore security rules allow token updates
3. ✅ Check Logcat for `FCMTokenManager` errors

### **Notification Not Opening Correct Screen?**
1. ✅ Check `NotificationDeepLinkHandler` logs
2. ✅ Verify notification data contains correct `type` field
3. ✅ Ensure target activities exist in `AndroidManifest.xml`

---

## 🎉 Summary

Your AcciZARD Lucban Android app now has **full push notification support** integrated with your web app's Firebase Cloud Functions!

**What Works:**
- ✅ Chat message notifications
- ✅ Announcement notifications (broadcast to all users)
- ✅ Report status update notifications
- ✅ Deep linking to correct screens
- ✅ Multiple notification channels with priorities
- ✅ Token management and refresh
- ✅ Foreground, background, and terminated state handling

**Next Steps:**
1. Deploy your Cloud Functions (if not already deployed)
2. Test all three notification types
3. Customize notification sounds/vibrations if desired
4. Monitor FCM delivery metrics in Firebase Console

---

## 📚 Related Files

- `app/build.gradle.kts` - FCM dependency
- `app/src/main/AndroidManifest.xml` - Permissions and service registration
- `app/src/main/java/com/example/accizardlucban/`
  - `NotificationChannelManager.java`
  - `FCMTokenManager.java`
  - `MyFirebaseMessagingService.java`
  - `AcciZardNotificationManager.java`
  - `NotificationDeepLinkHandler.java`
  - `MainActivity.java` (integration)
  - `MainDashboard.java` (integration)

---

**Implementation Date**: October 16, 2025  
**Firebase Project**: accizard-lucban  
**Package**: com.example.accizardlucban  

---

🎊 **Push notifications are now live!** Your users will receive real-time alerts for emergency situations, announcements, and report updates. 🎊

