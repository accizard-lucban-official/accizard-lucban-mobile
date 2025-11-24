# 📱➡️🌐 Mobile-to-Web Push Notifications Setup Guide

## ✅ Implementation Complete!

The mobile app now sends push notifications to web admin dashboards when:
1. **New Report Submitted** - User submits an incident report
2. **New Chat Message** - User sends a message to admin

---

## 🎯 How It Works

### **Mobile App Flow:**
1. User submits report or sends chat message
2. Mobile app writes notification trigger to Firestore: `web_notifications/{notificationId}`
3. Cloud Function detects new document
4. Cloud Function sends FCM notification to all web admin tokens
5. Web admin dashboard receives notification

### **Architecture:**
```
Mobile App → Firestore (web_notifications) → Cloud Function → FCM → Web Admin Dashboard
```

---

## 📦 Mobile App Implementation

### **Files Created/Modified:**

1. **`WebNotificationSender.java`** (NEW)
   - Utility class to send notifications to web admins
   - Methods:
     - `notifyNewReport()` - Triggers notification for new reports
     - `notifyChatMessage()` - Triggers notification for chat messages

2. **`ReportSubmissionActivity.java`** (MODIFIED)
   - Added notification trigger after successful report submission
   - Location: `submitReportToFirestore()` success callback

3. **`ChatActivity.java`** (MODIFIED)
   - Added notification trigger after successful message send
   - Location: `sendMessage()` and `uploadImageToFirebase()` success callbacks

---

## 🌐 Web App Setup Required

### **Step 1: Deploy Cloud Function**

1. Copy `MOBILE_TO_WEB_NOTIFICATIONS_CLOUD_FUNCTION.js` to your Firebase Cloud Functions
2. Install dependencies (if not already installed):
   ```bash
   cd functions
   npm install firebase-functions firebase-admin
   ```
3. Deploy the function:
   ```bash
   firebase deploy --only functions:sendMobileToWebNotification
   ```

### **Step 2: Register Web FCM Tokens**

Your web app needs to register FCM tokens for web push notifications. Add this to your web app:

```javascript
// Initialize FCM in your web app
import { getMessaging, getToken } from 'firebase/messaging';

async function registerWebFCMToken() {
  try {
    const messaging = getMessaging();
    const token = await getToken(messaging, {
      vapidKey: 'YOUR_VAPID_KEY' // Get from Firebase Console → Project Settings → Cloud Messaging
    });
    
    if (token) {
      // Save token to Firestore
      await db.collection('fcmTokens').add({
        token: token,
        platform: 'web',
        userId: 'admin', // Or your admin user ID
        createdAt: firebase.firestore.FieldValue.serverTimestamp(),
        updatedAt: firebase.firestore.FieldValue.serverTimestamp()
      });
      
      console.log('✅ Web FCM token registered:', token);
    }
  } catch (error) {
    console.error('❌ Error registering web FCM token:', error);
  }
}

// Call on admin login
registerWebFCMToken();
```

### **Step 3: Handle Incoming Notifications**

Add notification handler in your web app:

```javascript
import { getMessaging, onMessage } from 'firebase/messaging';

const messaging = getMessaging();

onMessage(messaging, (payload) => {
  console.log('📱 Notification received:', payload);
  
  // Show browser notification
  if ('Notification' in window && Notification.permission === 'granted') {
    const notification = new Notification(payload.notification.title, {
      body: payload.notification.body,
      icon: payload.notification.icon,
      badge: payload.notification.badge,
      tag: payload.data.type,
      data: payload.data
    });
    
    // Handle click
    notification.onclick = (event) => {
      event.notification.close();
      
      // Navigate based on notification type
      if (payload.data.type === 'new_report') {
        window.location.href = '/reports';
      } else if (payload.data.type === 'chat_message') {
        window.location.href = '/chat';
      }
    };
  }
});
```

### **Step 4: Request Notification Permission**

Request permission from user:

```javascript
async function requestNotificationPermission() {
  if ('Notification' in window) {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      console.log('✅ Notification permission granted');
      registerWebFCMToken();
    } else {
      console.log('❌ Notification permission denied');
    }
  }
}

// Call on admin dashboard load
requestNotificationPermission();
```

---

## 🔧 Firestore Security Rules

Add these rules to allow mobile app to write notifications:

```javascript
match /web_notifications/{notificationId} {
  // Allow authenticated users to create notifications
  allow create: if request.auth != null;
  
  // Allow Cloud Functions to read/update
  allow read, update: if request.auth != null || 
                       resource.data.status == 'pending';
  
  // Only allow Cloud Function to delete (via admin SDK)
  allow delete: if false;
}

match /fcmTokens/{tokenId} {
  // Allow authenticated users to create/update their own tokens
  allow create, update: if request.auth != null && 
                         request.resource.data.userId == request.auth.uid;
  
  // Allow Cloud Functions to read (via admin SDK)
  allow read: if request.auth != null;
  
  // Allow users to delete their own tokens
  allow delete: if request.auth != null && 
                 resource.data.userId == request.auth.uid;
}
```

---

## 🧪 Testing

### **Test 1: New Report Notification**
1. Login to mobile app as user
2. Submit a new report
3. **Expected**: Web admin dashboard receives notification
4. **Check Firestore**: `web_notifications` collection should have new document
5. **Check Cloud Function logs**: Should show notification sent

### **Test 2: Chat Message Notification**
1. Login to mobile app as user
2. Send a chat message
3. **Expected**: Web admin dashboard receives notification
4. **Check Firestore**: `web_notifications` collection should have new document

### **Test 3: Verify FCM Tokens**
1. Check Firestore: `fcmTokens` collection
2. Verify web tokens exist with `platform: "web"`
3. Verify tokens are valid (not expired)

---

## 🔍 Debugging

### **Check Mobile App Logs:**
```
D/WebNotificationSender: Sending new report notification to web admins
D/WebNotificationSender: ✅ Web notification trigger created: {notificationId}
```

### **Check Cloud Function Logs:**
```bash
firebase functions:log --only sendMobileToWebNotification
```

Look for:
- `📱 Mobile-to-web notification received`
- `📱 Found X web admin FCM tokens`
- `✅ Successfully sent X notifications`

### **Check Firestore:**
1. Open Firebase Console → Firestore
2. Check `web_notifications` collection:
   - New documents should appear when mobile sends notification
   - Status should change from `pending` to `processed`
3. Check `fcmTokens` collection:
   - Should have documents with `platform: "web"`

---

## 📊 Notification Types

### **1. New Report Notification**
```json
{
  "notification": {
    "title": "🚨 New Report Submitted",
    "body": "Fire from John Doe"
  },
  "data": {
    "type": "new_report",
    "reportId": "rpt123",
    "reportType": "Fire",
    "reporterName": "John Doe",
    "location": "123 Main St",
    "userId": "user123",
    "click_action": "OPEN_REPORTS"
  }
}
```

### **2. Chat Message Notification**
```json
{
  "notification": {
    "title": "💬 New Chat Message",
    "body": "John Doe: Hello, I need help..."
  },
  "data": {
    "type": "chat_message",
    "messageId": "msg456",
    "messageContent": "Hello, I need help...",
    "senderName": "John Doe",
    "userId": "user123",
    "click_action": "OPEN_CHAT"
  }
}
```

---

## ⚠️ Important Notes

1. **Web FCM Tokens**: Web app must register FCM tokens in `fcmTokens` collection with `platform: "web"`
2. **Cloud Function**: Must be deployed for notifications to work
3. **Notification Permission**: Web app must request and receive notification permission
4. **VAPID Key**: Required for web push notifications (get from Firebase Console)
5. **Token Refresh**: Web tokens may expire - implement token refresh logic

---

## 🎉 Status

✅ **Mobile App**: Complete - sends notifications to Firestore  
⏳ **Web App**: Requires setup (Cloud Function + FCM token registration)  
✅ **Cloud Function**: Code provided - needs deployment  

---

## 📝 Next Steps

1. ✅ Deploy Cloud Function
2. ✅ Register web FCM tokens in web app
3. ✅ Handle incoming notifications in web app
4. ✅ Request notification permission
5. ✅ Test end-to-end flow

---

## 🔗 Related Files

- `WebNotificationSender.java` - Mobile app utility class
- `MOBILE_TO_WEB_NOTIFICATIONS_CLOUD_FUNCTION.js` - Cloud Function code
- `ReportSubmissionActivity.java` - Report submission integration
- `ChatActivity.java` - Chat message integration

