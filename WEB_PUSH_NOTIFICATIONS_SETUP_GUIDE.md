# Web Push Notifications Setup Guide

## Overview
This guide explains how to add web push notifications to your web application using Firebase Cloud Messaging (FCM).

## Prerequisites
- Firebase project with web app configured
- HTTPS domain (required for service workers)
- Firebase Admin SDK access (for Cloud Functions)

## Step 1: Create Service Worker

Create `firebase-messaging-sw.js` in your web app's root directory:

```javascript
// firebase-messaging-sw.js
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

// Initialize Firebase
firebase.initializeApp({
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_AUTH_DOMAIN",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_STORAGE_BUCKET",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID"
});

const messaging = firebase.messaging();

// Handle background messages
messaging.onBackgroundMessage((payload) => {
  console.log('Received background message:', payload);
  
  const notificationTitle = payload.notification?.title || 'New Notification';
  const notificationOptions = {
    body: payload.notification?.body || '',
    icon: payload.notification?.icon || '/icon-192x192.png',
    badge: '/badge-72x72.png',
    tag: payload.data?.type || 'default',
    requireInteraction: false,
    data: payload.data
  };

  return self.registration.showNotification(notificationTitle, notificationOptions);
});

// Handle notification clicks
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  
  const data = event.notification.data;
  let url = '/';
  
  // Handle different notification types
  if (data?.type === 'chat') {
    url = '/chat';
  } else if (data?.type === 'report') {
    url = '/reports';
  } else if (data?.type === 'announcement') {
    url = '/alerts';
  }
  
  event.waitUntil(
    clients.openWindow(url)
  );
});
```

## Step 2: Initialize FCM in Web App

Add to your main JavaScript file (e.g., `app.js` or `main.js`):

```javascript
// Initialize Firebase
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_AUTH_DOMAIN",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_STORAGE_BUCKET",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID"
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

// Request notification permission and get token
async function requestNotificationPermission() {
  try {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      const token = await getToken(messaging, {
        vapidKey: 'YOUR_VAPID_KEY'
      });
      
      if (token) {
        console.log('FCM Token:', token);
        // Send token to your backend/Firestore
        await saveTokenToFirestore(token);
      } else {
        console.log('No registration token available.');
      }
    } else {
      console.log('Notification permission denied.');
    }
  } catch (error) {
    console.error('Error getting token:', error);
  }
}

// Save token to Firestore (same collection as mobile)
async function saveTokenToFirestore(token) {
  const user = firebase.auth().currentUser;
  if (!user) return;
  
  const db = firebase.firestore();
  const tokenData = {
    userId: user.uid,
    token: token,
    platform: 'web',
    createdAt: firebase.firestore.FieldValue.serverTimestamp(),
    updatedAt: firebase.firestore.FieldValue.serverTimestamp()
  };
  
  // Check if token already exists
  const existingToken = await db.collection('fcmTokens')
    .where('userId', '==', user.uid)
    .where('platform', '==', 'web')
    .limit(1)
    .get();
  
  if (!existingToken.empty) {
    // Update existing token
    await existingToken.docs[0].ref.update({
      token: token,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    });
  } else {
    // Create new token
    await db.collection('fcmTokens').add(tokenData);
  }
}

// Handle foreground messages
onMessage(messaging, (payload) => {
  console.log('Message received:', payload);
  
  // Show notification in foreground
  if (Notification.permission === 'granted') {
    new Notification(payload.notification?.title || 'New Notification', {
      body: payload.notification?.body || '',
      icon: payload.notification?.icon || '/icon-192x192.png',
      badge: '/badge-72x72.png',
      tag: payload.data?.type || 'default',
      data: payload.data
    });
  }
});

// Request permission on page load (if user is logged in)
if (firebase.auth().currentUser) {
  requestNotificationPermission();
}

// Also request when user logs in
firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    requestNotificationPermission();
  }
});
```

## Step 3: Update Cloud Functions

Update your existing Cloud Functions to send to both mobile and web tokens:

```javascript
// In your Cloud Function (e.g., sendChatNotification)
const admin = require('firebase-admin');

async function sendNotification(userId, title, body, data) {
  const db = admin.firestore();
  
  // Get all FCM tokens for the user (both mobile and web)
  const tokensSnapshot = await db.collection('fcmTokens')
    .where('userId', '==', userId)
    .get();
  
  if (tokensSnapshot.empty) {
    console.log('No FCM tokens found for user:', userId);
    return;
  }
  
  const messages = [];
  
  tokensSnapshot.forEach((doc) => {
    const tokenData = doc.data();
    const token = tokenData.token;
    const platform = tokenData.platform || 'android';
    
    // Create message based on platform
    if (platform === 'web') {
      messages.push({
        token: token,
        notification: {
          title: title,
          body: body
        },
        webpush: {
          notification: {
            title: title,
            body: body,
            icon: 'https://your-domain.com/icon-192x192.png',
            badge: 'https://your-domain.com/badge-72x72.png'
          },
          fcmOptions: {
            link: data?.url || 'https://your-domain.com'
          }
        },
        data: data
      });
    } else {
      // Android/iOS message
      messages.push({
        token: token,
        notification: {
          title: title,
          body: body
        },
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            channelId: 'default'
          }
        },
        apns: {
          payload: {
            aps: {
              sound: 'default'
            }
          }
        },
        data: data
      });
    }
  });
  
  // Send all messages
  if (messages.length > 0) {
    const response = await admin.messaging().sendAll(messages);
    console.log('Successfully sent messages:', response.successCount);
    console.log('Failed messages:', response.failureCount);
  }
}
```

## Step 4: Update Firestore Security Rules

Add rules for web FCM tokens:

```javascript
match /fcmTokens/{tokenId} {
  allow read, write: if request.auth != null && 
    request.auth.uid == resource.data.userId;
  allow create: if request.auth != null && 
    request.auth.uid == request.resource.data.userId;
}
```

## Step 5: Get VAPID Key

1. Go to Firebase Console
2. Project Settings > Cloud Messaging
3. Under "Web configuration", generate or copy your VAPID key
4. Use this key in your web app's `getToken()` call

## Step 6: Test Web Push Notifications

1. Deploy your web app with HTTPS
2. Register a user and grant notification permission
3. Send a test notification from Cloud Functions
4. Verify notification appears in browser

## Notes

- Service worker must be served from root directory
- HTTPS is required for service workers
- VAPID key is required for web push
- Tokens are stored in same `fcmTokens` collection as mobile
- Cloud Functions handle both mobile and web notifications

## Troubleshooting

- **No token received**: Check browser console, ensure HTTPS, verify VAPID key
- **Notifications not showing**: Check notification permissions, verify service worker registration
- **Background messages not working**: Ensure service worker is properly registered and active






