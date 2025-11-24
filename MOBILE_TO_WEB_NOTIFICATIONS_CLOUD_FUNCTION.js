// ==========================================
// MOBILE-TO-WEB PUSH NOTIFICATIONS
// Cloud Function for Web App
// ==========================================
// 
// This Cloud Function listens to the web_notifications collection
// and sends FCM push notifications to web admin dashboards when
// mobile users submit reports or send chat messages.
//
// DEPLOYMENT:
// 1. Copy this code to your Firebase Cloud Functions
// 2. Deploy: firebase deploy --only functions:sendMobileToWebNotification
// ==========================================

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  admin.initializeApp();
}

// ==========================================
// Send Mobile-to-Web Notifications
// ==========================================
// 
// Triggered when mobile app writes to web_notifications collection
// Sends FCM notification to all web admin FCM tokens
//
exports.sendMobileToWebNotification = functions.firestore
  .document('web_notifications/{notificationId}')
  .onCreate(async (snap, context) => {
    try {
      const notificationData = snap.data();
      const notificationId = context.params.notificationId;
      
      console.log('📱 Mobile-to-web notification received:', notificationId);
      console.log('📄 Notification data:', JSON.stringify(notificationData, null, 2));
      
      // Skip if already processed
      if (notificationData.status === 'processed') {
        console.log('ℹ️ Notification already processed, skipping');
        return null;
      }
      
      // Get all web admin FCM tokens
      const fcmTokensSnapshot = await admin.firestore()
        .collection('fcmTokens')
        .where('platform', '==', 'web')
        .get();
      
      if (fcmTokensSnapshot.empty) {
        console.log('⚠️ No web admin FCM tokens found');
        // Mark as processed even if no tokens
        await snap.ref.update({ status: 'processed', processedAt: admin.firestore.FieldValue.serverTimestamp() });
        return null;
      }
      
      const webTokens = [];
      fcmTokensSnapshot.forEach(doc => {
        const tokenData = doc.data();
        if (tokenData.token) {
          webTokens.push(tokenData.token);
        }
      });
      
      console.log(`📱 Found ${webTokens.length} web admin FCM tokens`);
      
      if (webTokens.length === 0) {
        console.log('⚠️ No valid web FCM tokens found');
        await snap.ref.update({ status: 'processed', processedAt: admin.firestore.FieldValue.serverTimestamp() });
        return null;
      }
      
      // Build notification payload based on type
      let notificationTitle = '';
      let notificationBody = '';
      let notificationData_payload = {};
      
      if (notificationData.type === 'new_report') {
        notificationTitle = '🚨 New Report Submitted';
        notificationBody = `${notificationData.reportType || 'Report'} from ${notificationData.reporterName || 'User'}`;
        notificationData_payload = {
          type: 'new_report',
          reportId: notificationData.reportId || '',
          reportType: notificationData.reportType || '',
          reporterName: notificationData.reporterName || '',
          location: notificationData.location || '',
          userId: notificationData.userId || '',
          click_action: 'OPEN_REPORTS'
        };
      } else if (notificationData.type === 'chat_message') {
        notificationTitle = '💬 New Chat Message';
        const messagePreview = notificationData.messageContent 
          ? (notificationData.messageContent.length > 50 
              ? notificationData.messageContent.substring(0, 47) + '...' 
              : notificationData.messageContent)
          : 'New message';
        notificationBody = `${notificationData.senderName || 'User'}: ${messagePreview}`;
        notificationData_payload = {
          type: 'chat_message',
          messageId: notificationData.messageId || '',
          messageContent: notificationData.messageContent || '',
          senderName: notificationData.senderName || '',
          userId: notificationData.userId || '',
          click_action: 'OPEN_CHAT'
        };
      } else {
        console.log('⚠️ Unknown notification type:', notificationData.type);
        await snap.ref.update({ status: 'processed', processedAt: admin.firestore.FieldValue.serverTimestamp() });
        return null;
      }
      
      // Prepare FCM message for web
      const message = {
        notification: {
          title: notificationTitle,
          body: notificationBody,
          icon: '/icon-192x192.png', // Your web app icon
          badge: '/badge-72x72.png' // Your web app badge
        },
        data: notificationData_payload,
        webpush: {
          fcmOptions: {
            link: notificationData_payload.click_action === 'OPEN_REPORTS' 
              ? '/reports' 
              : '/chat'
          },
          notification: {
            icon: '/icon-192x192.png',
            badge: '/badge-72x72.png',
            requireInteraction: notificationData.type === 'new_report' // Require interaction for reports
          }
        },
        tokens: webTokens
      };
      
      // Send to all web tokens
      try {
        const response = await admin.messaging().sendEachForMulticast(message);
        console.log(`✅ Successfully sent ${response.successCount} notifications`);
        console.log(`⚠️ Failed to send ${response.failureCount} notifications`);
        
        // Log failures
        if (response.failureCount > 0) {
          response.responses.forEach((resp, idx) => {
            if (!resp.success) {
              console.error(`❌ Failed to send to token ${idx}:`, resp.error);
            }
          });
        }
        
        // Mark notification as processed
        await snap.ref.update({ 
          status: 'processed', 
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          sentCount: response.successCount,
          failedCount: response.failureCount
        });
        
        return response;
      } catch (error) {
        console.error('❌ Error sending mobile-to-web notification:', error);
        await snap.ref.update({ 
          status: 'error', 
          error: error.message,
          processedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return null;
      }
    } catch (error) {
      console.error('❌ Error in sendMobileToWebNotification:', error);
      return null;
    }
  });

// ==========================================
// Cleanup Old Notifications (Optional)
// ==========================================
// 
// This function can be scheduled to clean up old processed notifications
// Run daily to keep the collection size manageable
//
exports.cleanupOldWebNotifications = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    try {
      const cutoffTime = Date.now() - (7 * 24 * 60 * 60 * 1000); // 7 days ago
      
      const oldNotifications = await admin.firestore()
        .collection('web_notifications')
        .where('status', 'in', ['processed', 'error'])
        .where('timestamp', '<', cutoffTime)
        .limit(500)
        .get();
      
      const batch = admin.firestore().batch();
      let deleteCount = 0;
      
      oldNotifications.forEach(doc => {
        batch.delete(doc.ref);
        deleteCount++;
      });
      
      if (deleteCount > 0) {
        await batch.commit();
        console.log(`✅ Cleaned up ${deleteCount} old web notifications`);
      }
      
      return null;
    } catch (error) {
      console.error('❌ Error cleaning up old notifications:', error);
      return null;
    }
  });

