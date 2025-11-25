# 📱➡️🌐 Mobile-to-Web Push Notifications Setup Guide

## ✅ Implementation Complete!

The mobile app is now configured to work with the web app's existing Cloud Functions for mobile-to-web push notifications.

---

## 🎯 How It Works

### **Architecture:**
```
Mobile App → Firestore (reports/chat_messages) → Web App Cloud Functions → FCM → Web Admin Dashboard
```

### **Flow:**
1. **Mobile User Action**: User submits report or sends chat message
2. **Mobile App**: Creates document in Firestore (`reports` or `chat_messages` collection)
3. **Web App Cloud Functions**: Automatically detect new documents
4. **Cloud Functions**: 
   - Detect mobile users (by checking for `fcmToken` without `webFcmToken`)
   - Send FCM notifications to all web users (from `users` and `superAdmin` collections)
5. **Web Admin Dashboard**: Receives notification

---

## 📦 Mobile App Implementation

### **What the Mobile App Does:**

✅ **1. Store FCM Token** (Already Implemented)
- FCM token is stored in `users/{userId}/fcmToken` field
- Handled by `FCMTokenManager.java`
- Token is saved on login and refreshed automatically

✅ **2. Create Reports Normally** (Already Implemented)
- Reports are created in `reports` collection
- Handled by `ReportSubmissionActivity.java`
- No additional notification code needed

✅ **3. Send Chat Messages Normally** (Already Implemented)
- Messages are created in `chat_messages` collection
- Handled by `ChatActivity.java`
- No additional notification code needed

### **No Additional Code Required!**

The mobile app **does NOT need** to:
- ❌ Create separate notification documents
- ❌ Write to `web_notifications` collection
- ❌ Manually trigger notifications
- ❌ Query web admin tokens

**Everything is handled automatically by the web app's Cloud Functions!**

---

## 🌐 Web App Cloud Functions

The web app's Cloud Functions automatically:

### **1. Detect New Reports**
- Monitor `reports` collection for new documents
- Check if reporter has `fcmToken` (mobile user)
- Send notifications to all web admins

### **2. Detect New Chat Messages**
- Monitor `chat_messages` collection for new documents
- Check if sender has `fcmToken` without `webFcmToken` (mobile user)
- Send notifications to all web admins

### **3. Identify Mobile Users**
- Mobile users: Have `fcmToken` field in `users/{userId}`
- Web users: Have `webFcmToken` field in `users/{userId}` or `superAdmin/{adminId}`

### **4. Send to Web Admins**
- Query `users` collection for web admins (have `webFcmToken`)
- Query `superAdmin` collection for super admins
- Send FCM notifications to all web admin tokens

---

## ✅ Mobile App Requirements (Already Complete)

### **1. FCM Token Storage**
✅ **Status**: Already implemented in `FCMTokenManager.java`

The mobile app stores FCM tokens in:
```
users/{userId}/fcmToken: "mobile-fcm-token-here"
```

This is done automatically on login via:
```java
FCMTokenManager tokenManager = new FCMTokenManager(context);
tokenManager.initializeFCMToken();
```

### **2. Report Submission**
✅ **Status**: Already implemented in `ReportSubmissionActivity.java`

Reports are created normally in Firestore:
```java
FirestoreHelper.createReportWithAutoId(reportData, successListener, failureListener);
```

The web app's Cloud Function will automatically detect this and send notifications.

### **3. Chat Message Sending**
✅ **Status**: Already implemented in `ChatActivity.java`

Messages are created normally in Firestore:
```java
db.collection("chat_messages").add(messageData);
```

The web app's Cloud Function will automatically detect this and send notifications.

---

## 🔧 Web App Setup (Required)

The web app needs to have Cloud Functions that:

### **1. Monitor Reports Collection**
```javascript
exports.onNewReport = functions.firestore
  .document('reports/{reportId}')
  .onCreate(async (snap, context) => {
    const reportData = snap.data();
    const userId = reportData.userId;
    
    // Check if this is from a mobile user (has fcmToken, no webFcmToken)
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    const userData = userDoc.data();
    
    if (userData && userData.fcmToken && !userData.webFcmToken) {
      // This is a mobile user - send notification to web admins
      await sendNotificationToWebAdmins({
        type: 'new_report',
        reportId: context.params.reportId,
        reportData: reportData
      });
    }
  });
```

### **2. Monitor Chat Messages Collection**
```javascript
exports.onNewChatMessage = functions.firestore
  .document('chat_messages/{messageId}')
  .onCreate(async (snap, context) => {
    const messageData = snap.data();
    const userId = messageData.userId;
    
    // Check if this is from a mobile user (has fcmToken, no webFcmToken)
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    const userData = userDoc.data();
    
    if (userData && userData.fcmToken && !userData.webFcmToken) {
      // This is a mobile user - send notification to web admins
      await sendNotificationToWebAdmins({
        type: 'chat_message',
        messageId: context.params.messageId,
        messageData: messageData
      });
    }
  });
```

### **3. Send Notifications to Web Admins**
```javascript
async function sendNotificationToWebAdmins(notificationData) {
  // Get all web admin FCM tokens
  const webAdmins = [];
  
  // Get from users collection (web admins)
  const usersSnapshot = await admin.firestore()
    .collection('users')
    .where('webFcmToken', '!=', null)
    .get();
  
  usersSnapshot.forEach(doc => {
    const webFcmToken = doc.data().webFcmToken;
    if (webFcmToken) {
      webAdmins.push(webFcmToken);
    }
  });
  
  // Get from superAdmin collection
  const superAdminsSnapshot = await admin.firestore()
    .collection('superAdmin')
    .get();
  
  superAdminsSnapshot.forEach(doc => {
    const adminData = doc.data();
    if (adminData.webFcmToken) {
      webAdmins.push(adminData.webFcmToken);
    }
  });
  
  // Send notifications to all web admins
  if (webAdmins.length > 0) {
    const message = {
      notification: {
        title: notificationData.type === 'new_report' 
          ? '🚨 New Report Submitted' 
          : '💬 New Chat Message',
        body: buildNotificationBody(notificationData)
      },
      data: notificationData,
      tokens: webAdmins
    };
    
    await admin.messaging().sendEachForMulticast(message);
  }
}
```

---

## 🧪 Testing

### **Test 1: New Report Notification**
1. Login to mobile app as user
2. Submit a new report
3. **Check Firestore**: `reports` collection should have new document
4. **Check Cloud Function logs**: Should show notification sent to web admins
5. **Expected**: Web admin dashboard receives notification

### **Test 2: Chat Message Notification**
1. Login to mobile app as user
2. Send a chat message
3. **Check Firestore**: `chat_messages` collection should have new document
4. **Check Cloud Function logs**: Should show notification sent to web admins
5. **Expected**: Web admin dashboard receives notification

### **Test 3: Verify FCM Token Storage**
1. Check Firestore: `users/{userId}/fcmToken` should exist
2. Verify token is a valid FCM token (long string)
3. Verify user does NOT have `webFcmToken` (identifies as mobile user)

---

## 🔍 Debugging

### **Check Mobile App Logs:**
```
D/FCMTokenManager: ✅ FCM token saved to Firestore using document ID: {userId}
D/ReportSubmissionActivity: ✅ Report submitted successfully with ID: {reportId}
D/ChatActivity: Message sent successfully with ID: {messageId}
```

### **Check Firestore:**
1. **Reports**: `reports` collection should have new documents when reports are submitted
2. **Chat Messages**: `chat_messages` collection should have new documents when messages are sent
3. **FCM Tokens**: `users/{userId}/fcmToken` should exist for mobile users

### **Check Web App Cloud Function Logs:**
```bash
firebase functions:log
```

Look for:
- `📱 New report detected from mobile user`
- `📱 New chat message detected from mobile user`
- `✅ Notification sent to X web admins`

---

## 📊 Data Structure

### **Mobile User (in Firestore):**
```json
{
  "users/{userId}": {
    "fcmToken": "mobile-fcm-token-here",
    // No webFcmToken field = mobile user
  }
}
```

### **Web Admin (in Firestore):**
```json
{
  "users/{adminId}": {
    "webFcmToken": "web-fcm-token-here",
    // Has webFcmToken = web admin
  }
}
```

### **Super Admin (in Firestore):**
```json
{
  "superAdmin/{adminId}": {
    "webFcmToken": "web-fcm-token-here"
  }
}
```

---

## ⚠️ Important Notes

1. **FCM Token Storage**: Mobile app must store FCM token in `users/{userId}/fcmToken`
2. **User Identification**: Web app identifies mobile users by checking for `fcmToken` without `webFcmToken`
3. **No Manual Triggers**: Mobile app does NOT need to manually trigger notifications
4. **Cloud Functions**: Web app's Cloud Functions handle all notification logic
5. **Web Admin Tokens**: Web app must store web FCM tokens in `users/{adminId}/webFcmToken` or `superAdmin/{adminId}/webFcmToken`

---

## 🎉 Status

✅ **Mobile App**: Complete - stores FCM tokens, creates reports/messages normally  
⏳ **Web App**: Requires Cloud Functions to detect and send notifications  

---

## 📝 Summary

**Mobile App Responsibilities:**
- ✅ Store FCM token in `users/{userId}/fcmToken`
- ✅ Create reports in `reports` collection
- ✅ Send chat messages in `chat_messages` collection

**Web App Responsibilities:**
- ⏳ Monitor `reports` and `chat_messages` collections
- ⏳ Detect mobile users (has `fcmToken`, no `webFcmToken`)
- ⏳ Send FCM notifications to web admins (has `webFcmToken`)

**No Additional Mobile App Code Required!** 🎉
