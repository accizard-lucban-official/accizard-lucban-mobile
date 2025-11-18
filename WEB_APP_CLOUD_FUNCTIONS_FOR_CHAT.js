// ==========================================
// WEB APP CLOUD FUNCTIONS FOR CHAT
// Copy these to your Firebase Cloud Functions
// ==========================================

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  admin.initializeApp();
}

// ==========================================
// 1. Send Push Notification for New Chat Messages
// ==========================================
exports.sendChatNotification = functions.firestore
  .document('chat_messages/{messageId}')
  .onCreate(async (snap, context) => {
    try {
      const messageData = snap.data();
      const messageId = context.params.messageId;
      
      console.log('📨 New chat message created:', messageId);
      console.log('📄 Message data:', JSON.stringify(messageData, null, 2));
      
      // Only send notification if message is from admin to user
      // (i.e., senderId !== userId means admin is sending to user)
      if (messageData.senderId !== messageData.userId) {
        const userId = messageData.userId;
        const senderName = messageData.senderName || "AcciZard Support";
        
        // Get message content from either "content" or "message" field
        let messageContent = messageData.content || messageData.message || "New message";
        
        console.log('👤 Sending notification to user:', userId);
        console.log('✉️ Message content:', messageContent);
        
        // Get user's FCM token from users collection
        const userDoc = await admin.firestore()
          .collection('users')
          .doc(userId)
          .get();
        
        if (!userDoc.exists) {
          console.log('⚠️ User document not found for:', userId);
          return null;
        }
        
        const userData = userDoc.data();
        const fcmToken = userData.fcmToken;
        
        if (!fcmToken) {
          console.log('⚠️ No FCM token found for user:', userId);
          return null;
        }
        
        console.log('📱 FCM Token found:', fcmToken.substring(0, 20) + '...');
        
        // Prepare notification body
        let notificationBody = messageContent;
        
        // Handle image messages
        if (messageData.imageUrl && !messageContent) {
          notificationBody = "📷 Sent an image";
        } else if (messageData.imageUrl && messageContent === "Sent an image") {
          notificationBody = "📷 Sent an image";
        } else if (messageData.imageUrl) {
          notificationBody = `📷 ${messageContent}`;
        }
        
        // Handle file attachments
        if (messageData.fileUrl && messageData.fileName) {
          notificationBody = `📎 ${messageData.fileName}`;
        }
        
        // Truncate long messages for notification (max 100 chars)
        if (notificationBody.length > 100) {
          notificationBody = notificationBody.substring(0, 97) + "...";
        }
        
        // Create notification payload
        const notification = {
          notification: {
            title: senderName,
            body: notificationBody,
            sound: "default"
          },
          data: {
            type: "chat_message",
            userId: userId,
            messageId: messageId,
            senderId: messageData.senderId,
            senderName: senderName,
            content: messageContent,
            click_action: "OPEN_CHAT"
          },
          token: fcmToken,
          // Android-specific config
          android: {
            priority: 'high',
            notification: {
              channelId: 'chat_messages',
              sound: 'default',
              priority: 'high',
              defaultSound: true,
              defaultVibrateTimings: true
            }
          },
          // iOS-specific config
          apns: {
            payload: {
              aps: {
                sound: 'default',
                badge: 1
              }
            }
          }
        };
        
        // Send the notification
        try {
          const response = await admin.messaging().send(notification);
          console.log('✅ Chat notification sent successfully:', response);
          console.log('📱 Notification body:', notificationBody);
          return response;
        } catch (error) {
          console.error('❌ Error sending chat notification:', error);
          return null;
        }
      } else {
        console.log('ℹ️ Message is from user, skipping notification');
        return null;
      }
    } catch (error) {
      console.error('❌ Error in sendChatNotification:', error);
      return null;
    }
  });

// ==========================================
// 2. Set isRead = false for Admin Messages
// ==========================================
exports.setAdminMessageAsUnread = functions.firestore
  .document('chat_messages/{messageId}')
  .onCreate(async (snap, context) => {
    try {
      const messageData = snap.data();
      const messageId = context.params.messageId;
      
      console.log('📨 Checking message read status:', messageId);
      
      // If message is from admin (senderId !== userId) and isRead is not set
      if (messageData.senderId !== messageData.userId && messageData.isRead === undefined) {
        console.log('📝 Setting isRead = false for admin message:', messageId);
        
        try {
          await snap.ref.update({ isRead: false });
          console.log('✅ Set isRead = false for message:', messageId);
          return true;
        } catch (error) {
          console.error('❌ Error setting isRead:', error);
          return null;
        }
      } else if (messageData.senderId === messageData.userId) {
        // Message is from user - optionally set isRead = true
        console.log('ℹ️ Message is from user, setting isRead = true:', messageId);
        
        if (messageData.isRead === undefined) {
          try {
            await snap.ref.update({ isRead: true });
            console.log('✅ Set isRead = true for user message:', messageId);
            return true;
          } catch (error) {
            console.error('❌ Error setting isRead for user message:', error);
            return null;
          }
        }
      } else {
        console.log('ℹ️ isRead already set for message:', messageId);
        return null;
      }
    } catch (error) {
      console.error('❌ Error in setAdminMessageAsUnread:', error);
      return null;
    }
  });

// ==========================================
// 3. OPTIONAL: Track Unread Message Count per User
// ==========================================
exports.updateUnreadCount = functions.firestore
  .document('chat_messages/{messageId}')
  .onWrite(async (change, context) => {
    try {
      const newData = change.after.exists ? change.after.data() : null;
      const oldData = change.before.exists ? change.before.data() : null;
      
      if (!newData) {
        console.log('ℹ️ Message deleted, skipping unread count update');
        return null;
      }
      
      const userId = newData.userId;
      
      // Only update if message is from admin and read status changed
      if (newData.senderId !== userId) {
        const wasRead = oldData ? oldData.isRead : false;
        const isRead = newData.isRead;
        
        if (wasRead !== isRead) {
          console.log('📊 Updating unread count for user:', userId);
          
          // Count total unread messages for this user
          const unreadSnapshot = await admin.firestore()
            .collection('chat_messages')
            .where('userId', '==', userId)
            .where('isUser', '==', false)
            .where('isRead', '==', false)
            .get();
          
          const unreadCount = unreadSnapshot.size;
          
          console.log('📊 Unread count for user ' + userId + ':', unreadCount);
          
          // Update user document with unread count
          await admin.firestore()
            .collection('users')
            .doc(userId)
            .update({
              unreadChatCount: unreadCount,
              lastUpdated: admin.firestore.FieldValue.serverTimestamp()
            });
          
          console.log('✅ Updated unread count for user ' + userId + ':', unreadCount);
          return true;
        }
      }
      
      return null;
    } catch (error) {
      console.error('❌ Error in updateUnreadCount:', error);
      return null;
    }
  });

// ==========================================
// 4. OPTIONAL: Send Welcome Message to New Users
// ==========================================
exports.sendWelcomeMessage = functions.firestore
  .document('users/{userId}')
  .onCreate(async (snap, context) => {
    try {
      const userId = context.params.userId;
      const userData = snap.data();
      
      console.log('👋 New user registered:', userId);
      
      // Create welcome message
      const welcomeMessage = {
        userId: userId,
        userPhoneNumber: userData.phoneNumber || "",
        content: "Welcome to AcciZard Lucban! How can we help you today?",
        message: "Welcome to AcciZard Lucban! How can we help you today?",
        senderId: "system",
        senderName: "AcciZard Support",
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isUser: false,
        imageUrl: null,
        isRead: false
      };
      
      await admin.firestore()
        .collection('chat_messages')
        .add(welcomeMessage);
      
      console.log('✅ Welcome message sent to user:', userId);
      return true;
    } catch (error) {
      console.error('❌ Error sending welcome message:', error);
      return null;
    }
  });

// ==========================================
// DEPLOYMENT INSTRUCTIONS
// ==========================================
/*
1. Install Firebase CLI:
   npm install -g firebase-tools

2. Login to Firebase:
   firebase login

3. Initialize Cloud Functions (if not already):
   firebase init functions

4. Copy these functions to your index.js file in the functions folder

5. Install required dependencies:
   cd functions
   npm install firebase-admin firebase-functions

6. Deploy the functions:
   firebase deploy --only functions

7. Verify deployment:
   firebase functions:log

8. Test by sending a message from your web app admin panel
*/











































