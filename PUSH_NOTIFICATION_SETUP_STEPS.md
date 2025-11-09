# 🚀 Push Notification Setup - Quick Start Guide

## ✅ Implementation Complete!

All code has been added to your Android app. Follow these steps to activate push notifications.

---

## 📋 Step-by-Step Setup

### **Step 1: Sync Gradle** ⚙️
1. Open Android Studio
2. Click **"Sync Project with Gradle Files"** button (elephant icon with arrow)
3. Wait for sync to complete
4. Verify no build errors

### **Step 2: Rebuild the App** 🔨
```bash
# Clean and rebuild
./gradlew clean build
```
Or in Android Studio: **Build → Rebuild Project**

### **Step 3: Deploy Cloud Functions** ☁️ (If Not Already Done)
Your web app already has the Cloud Functions, but make sure they're deployed:

```bash
cd functions
firebase deploy --only functions
```

This deploys:
- `sendChatNotification`
- `sendAnnouncementNotification`
- `sendReportStatusNotification`

### **Step 4: Install App on Device** 📱
1. Connect your Android device via USB or use an emulator
2. Click **Run** button in Android Studio
3. Select your device
4. Wait for installation to complete

### **Step 5: Grant Notification Permission** 🔔
On Android 13+ (API 33+) devices, the app will **automatically request notification permission** when you login. 

**Important**: You MUST grant this permission to receive system notifications. Without it, notifications will only show as Toast messages.

- When prompted, tap **"Allow"** to enable notifications
- You can also grant permission later in: Settings → Apps → AcciZARD → Notifications

### **Step 6: Login to the App** 🔐
1. Open the app on your device
2. Login with your user credentials
3. Check Android Studio Logcat for this log:
   ```
   D/FCMTokenManager: ✅ FCM token saved to Firestore for user: {userId}
   ```

### **Step 7: Verify Token in Firestore** ✅
1. Open Firebase Console → Firestore Database
2. Navigate to `users/{your-user-id}`
3. Verify the `fcmToken` field exists with a long token string
4. If the token is there, you're ready to receive notifications! 🎉

---

## 🧪 Test Notifications

### **Test 1: Chat Message** 💬
1. Login to your web app as admin
2. Send a chat message to the user logged in on mobile
3. **Expected**: Notification appears on mobile device
4. **Tap notification**: Opens ChatActivity

### **Test 2: Announcement** 📢
1. From web app (admin), create a new announcement
2. **Expected**: Notification appears on all mobile devices
3. **Tap notification**: Opens AlertsActivity

### **Test 3: Report Status Update** 📋
1. Submit a report from mobile app
2. From web app (admin), update the report status
3. **Expected**: Notification appears on mobile device
4. **Tap notification**: Opens ReportSubmissionActivity

---

## 🔍 Troubleshooting

### **Problem: No FCM token in Firestore**
**Solution:**
1. **Check notification permission is granted** (Android 13+)
   - Go to: Settings → Apps → AcciZARD → Notifications
   - Ensure "All AcciZARD notifications" is enabled
2. Check Logcat for errors in `FCMTokenManager`
3. Ensure internet connection is active
4. Verify Firebase is initialized (check for Firebase init logs)
5. Re-login to the app

### **Problem: Token saved but no notifications received**
**Solution:**
1. **MOST COMMON**: Notification permission not granted on Android 13+
   - Check: Settings → Apps → AcciZARD → Notifications
   - Grant permission if disabled
   - Uninstall and reinstall app to see permission prompt again
2. Verify Cloud Functions are deployed (`firebase deploy --only functions`)
3. Check Firebase Console → Functions for execution logs
4. Test in different app states (foreground, background, terminated)

### **Problem: Notifications showing as Toast messages instead of system notifications** ⚠️
**Solution:**
This happens when notification permission is NOT granted on Android 13+.

1. **Check permission status:**
   - Go to: Settings → Apps → AcciZARD → Notifications
   - Ensure notifications are enabled
   
2. **If permission was denied:**
   - Uninstall the app completely
   - Reinstall the app
   - Login again
   - When permission dialog appears, tap **"Allow"**
   
3. **Verify in logs:**
   ```
   D/MainActivity: ✅ Notification permission granted
   ```
   
**Why this happens:**
- Android 13+ requires runtime permission for POST_NOTIFICATIONS
- Without permission, system notifications are blocked
- Firebase may fall back to showing Toast messages

### **Problem: Notification received but tap does nothing**
**Solution:**
1. Check Logcat for `NotificationDeepLink` errors
2. Verify target activities exist in AndroidManifest.xml
3. Ensure notification data contains `type` field

### **Problem: Build errors after Gradle sync**
**Solution:**
1. Run `./gradlew clean`
2. Invalidate caches: **File → Invalidate Caches → Invalidate and Restart**
3. Ensure Firebase BOM version is compatible (currently using 32.7.0)

---

## 📊 Monitoring & Logs

### **Important Logs to Watch:**

**FCM Token Initialization:**
```
D/MainActivity: ✅ Notification channels initialized
D/FCMTokenManager: ✅ FCM token obtained: {token}
D/FCMTokenManager: ✅ FCM token saved to Firestore for user: {userId}
```

**Notification Receipt:**
```
D/MyFCMService: 📩 Message received from: {sender}
D/MyFCMService: Notification Title: {title}
D/AcciZardNotificationMgr: ✅ Notification displayed - ID: {id}
```

**Notification Tap:**
```
D/NotificationDeepLink: 📱 Notification tapped - Type: {type}
D/NotificationDeepLink: Opening {screen} screen
```

---

## ✨ Features Implemented

- ✅ **4 Notification Channels**: Report updates, announcements, high-priority announcements, chat messages
- ✅ **Automatic Token Management**: Gets token on login, refreshes automatically
- ✅ **Deep Linking**: Opens correct screen when notification is tapped
- ✅ **Custom Vibration Patterns**: Different patterns for different notification types
- ✅ **Foreground/Background Handling**: Works in all app states
- ✅ **Web App Integration**: Uses same Firebase project and Cloud Functions

---

## 📞 Need Help?

Check the comprehensive documentation: `PUSH_NOTIFICATION_IMPLEMENTATION.md`

**Common Files to Review:**
- `app/src/main/java/com/example/accizardlucban/MyFirebaseMessagingService.java` - Receives notifications
- `app/src/main/java/com/example/accizardlucban/FCMTokenManager.java` - Manages tokens
- `app/src/main/java/com/example/accizardlucban/NotificationDeepLinkHandler.java` - Handles navigation

---

## 🎊 Ready to Go!

Once you complete Step 1-7 above, your app will be receiving push notifications from your web app's Cloud Functions!

**Next Actions:**
1. ✅ Sync Gradle
2. ✅ Rebuild app
3. ✅ Install on device
4. ✅ Login and verify token in Firestore
5. ✅ Test all three notification types

**Happy coding!** 🚀

