// ==========================================
// WEB APP: User Message Sending & Admin Badge System
// Copy this code to your web application
// ==========================================

// Initialize Firebase (adjust based on your setup)
// const db = firebase.firestore();
// or
// import { getFirestore, collection, addDoc, query, where, onSnapshot, updateDoc, doc } from 'firebase/firestore';

// ==========================================
// 1. SEND USER MESSAGE FROM WEB
// ==========================================
/**
 * Send a message from user on the web
 * ✅ IMPORTANT: Do NOT set isRead or set it to false
 * The cloud function will handle setting isRead: false for admin badge
 */
function sendUserMessageFromWeb(userId, messageContent, senderName = null) {
  const db = firebase.firestore();
  
  // Get current user info (adjust based on your auth setup)
  const currentUser = firebase.auth().currentUser;
  if (!currentUser) {
    console.error('User not authenticated');
    return Promise.reject('User not authenticated');
  }
  
  const messageData = {
    userId: userId,
    content: messageContent,
    message: messageContent, // Some apps use 'message' field
    senderId: currentUser.uid,
    senderName: senderName || currentUser.displayName || 'User',
    timestamp: firebase.firestore.FieldValue.serverTimestamp(),
    isUser: true,
    imageUrl: null,
    // ✅ CRITICAL: Do NOT set isRead, or set it to false
    // The cloud function will set isRead: false so admin can see badge
    isRead: false
  };
  
  return db.collection('chat_messages')
    .add(messageData)
    .then((docRef) => {
      console.log('✅ User message sent from web:', docRef.id);
      return docRef.id;
    })
    .catch((error) => {
      console.error('❌ Error sending user message:', error);
      throw error;
    });
}

// ==========================================
// 2. GET UNREAD MESSAGE COUNT FOR ADMIN
// ==========================================
/**
 * Get count of unread user messages for admin badge
 * This counts messages where:
 * - isUser: true (from user)
 * - isRead: false (not read by admin yet)
 */
function getUnreadUserMessageCount() {
  const db = firebase.firestore();
  
  return db.collection('chat_messages')
    .where('isUser', '==', true)
    .where('isRead', '==', false)
    .get()
    .then((snapshot) => {
      const count = snapshot.size;
      console.log('📊 Unread user messages count:', count);
      return count;
    })
    .catch((error) => {
      console.error('❌ Error getting unread count:', error);
      return 0;
    });
}

// ==========================================
// 3. REAL-TIME UNREAD COUNT LISTENER
// ==========================================
/**
 * Set up real-time listener for unread message count
 * Updates badge automatically when messages are read/unread
 */
let unreadCountListener = null;

function setupUnreadCountListener(callback) {
  const db = firebase.firestore();
  
  // Remove existing listener if any
  if (unreadCountListener) {
    unreadCountListener();
  }
  
  // Set up real-time listener
  unreadCountListener = db.collection('chat_messages')
    .where('isUser', '==', true)
    .where('isRead', '==', false)
    .onSnapshot((snapshot) => {
      const count = snapshot.size;
      console.log('📊 Unread count updated:', count);
      
      // Update badge UI
      updateBadgeUI(count);
      
      // Call callback if provided
      if (callback) {
        callback(count);
      }
    }, (error) => {
      console.error('❌ Error in unread count listener:', error);
    });
  
  return unreadCountListener;
}

// ==========================================
// 4. UPDATE BADGE UI
// ==========================================
/**
 * Update the badge UI element with unread count
 * Adjust selector based on your HTML structure
 */
function updateBadgeUI(count) {
  // Find badge element (adjust selector as needed)
  const badgeElement = document.getElementById('unread-badge');
  // or: const badgeElement = document.querySelector('.unread-badge');
  // or: const badgeElement = document.querySelector('[data-badge="unread"]');
  
  if (!badgeElement) {
    console.warn('Badge element not found');
    return;
  }
  
  if (count > 0) {
    badgeElement.textContent = count > 99 ? '99+' : count.toString();
    badgeElement.style.display = 'block'; // or 'inline-block', 'flex', etc.
    badgeElement.classList.add('has-unread');
  } else {
    badgeElement.textContent = '';
    badgeElement.style.display = 'none';
    badgeElement.classList.remove('has-unread');
  }
}

// ==========================================
// 5. MARK MESSAGE AS READ (When Admin Clicks)
// ==========================================
/**
 * Mark a user message as read when admin clicks/views it
 * This should only be called when admin actually clicks on the message
 */
function markUserMessageAsRead(messageId) {
  const db = firebase.firestore();
  
  return db.collection('chat_messages')
    .doc(messageId)
    .update({
      isRead: true
    })
    .then(() => {
      console.log('✅ Message marked as read:', messageId);
      // Badge will update automatically via real-time listener
      return true;
    })
    .catch((error) => {
      console.error('❌ Error marking message as read:', error);
      throw error;
    });
}

// ==========================================
// 6. MARK ALL MESSAGES AS READ (When Admin Opens Chat)
// ==========================================
/**
 * Mark all unread user messages as read when admin opens a user's chat
 * This is useful when admin opens the chat view
 */
function markAllUserMessagesAsRead(userId) {
  const db = firebase.firestore();
  
  // Get all unread messages for this user
  return db.collection('chat_messages')
    .where('userId', '==', userId)
    .where('isUser', '==', true)
    .where('isRead', '==', false)
    .get()
    .then((snapshot) => {
      const batch = db.batch();
      let count = 0;
      
      snapshot.forEach((doc) => {
        batch.update(doc.ref, { isRead: true });
        count++;
      });
      
      if (count > 0) {
        return batch.commit().then(() => {
          console.log(`✅ Marked ${count} messages as read for user:`, userId);
          return count;
        });
      } else {
        console.log('ℹ️ No unread messages to mark');
        return 0;
      }
    })
    .catch((error) => {
      console.error('❌ Error marking messages as read:', error);
      throw error;
    });
}

// ==========================================
// 7. GET UNREAD MESSAGES FOR SPECIFIC USER
// ==========================================
/**
 * Get unread messages for a specific user
 * Useful for showing which users have unread messages
 */
function getUnreadMessagesForUser(userId) {
  const db = firebase.firestore();
  
  return db.collection('chat_messages')
    .where('userId', '==', userId)
    .where('isUser', '==', true)
    .where('isRead', '==', false)
    .orderBy('timestamp', 'desc')
    .get()
    .then((snapshot) => {
      const messages = [];
      snapshot.forEach((doc) => {
        messages.push({
          id: doc.id,
          ...doc.data()
        });
      });
      return messages;
    })
    .catch((error) => {
      console.error('❌ Error getting unread messages:', error);
      throw error;
    });
}

// ==========================================
// 8. GET USERS WITH UNREAD MESSAGES
// ==========================================
/**
 * Get list of users who have unread messages
 * Useful for showing a list of users with pending messages
 */
function getUsersWithUnreadMessages() {
  const db = firebase.firestore();
  
  return db.collection('chat_messages')
    .where('isUser', '==', true)
    .where('isRead', '==', false)
    .get()
    .then((snapshot) => {
      const userIds = new Set();
      snapshot.forEach((doc) => {
        const data = doc.data();
        if (data.userId) {
          userIds.add(data.userId);
        }
      });
      return Array.from(userIds);
    })
    .catch((error) => {
      console.error('❌ Error getting users with unread messages:', error);
      throw error;
    });
}

// ==========================================
// 9. INITIALIZE BADGE SYSTEM
// ==========================================
/**
 * Initialize the badge system when page loads
 * Call this when your admin panel loads
 */
function initializeBadgeSystem() {
  // Get initial count
  getUnreadUserMessageCount().then((count) => {
    updateBadgeUI(count);
  });
  
  // Set up real-time listener
  setupUnreadCountListener((count) => {
    console.log('Badge updated to:', count);
  });
  
  console.log('✅ Badge system initialized');
}

// ==========================================
// 10. CLEANUP (When Leaving Page)
// ==========================================
/**
 * Clean up listeners when leaving the page
 * Call this when admin logs out or navigates away
 */
function cleanupBadgeSystem() {
  if (unreadCountListener) {
    unreadCountListener();
    unreadCountListener = null;
    console.log('✅ Badge system cleaned up');
  }
}

// ==========================================
// USAGE EXAMPLES
// ==========================================

/*
// Example 1: Send message from user
sendUserMessageFromWeb('user123', 'Hello, I need help')
  .then((messageId) => {
    console.log('Message sent:', messageId);
  })
  .catch((error) => {
    console.error('Failed to send:', error);
  });

// Example 2: Initialize badge on page load
document.addEventListener('DOMContentLoaded', () => {
  initializeBadgeSystem();
});

// Example 3: Mark message as read when clicked
function onMessageClick(messageId) {
  markUserMessageAsRead(messageId)
    .then(() => {
      console.log('Message marked as read');
    });
}

// Example 4: Mark all as read when opening chat
function openUserChat(userId) {
  markAllUserMessagesAsRead(userId)
    .then((count) => {
      console.log(`Marked ${count} messages as read`);
      // Then load and display messages
      loadUserMessages(userId);
    });
}

// Example 5: Cleanup on page unload
window.addEventListener('beforeunload', () => {
  cleanupBadgeSystem();
});
*/

