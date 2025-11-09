# 🔧 FCM Token Permission Denied - Complete Fix Guide

## ❌ **Problem:**
```
Failed to save FCM token to Firestore: PERMISSION_DENIED: Missing or insufficient permissions.
```

## ✅ **Solution Applied:**

### **1. Updated Android Code** (`FCMTokenManager.java`)
The code now:
- **Queries** Firestore to find user document by `firebaseUid` field
- Falls back to using document ID if query doesn't find anything
- Uses `SetOptions.merge()` to avoid overwriting existing data
- Provides detailed logs at each step

### **2. Updated Firestore Rules**
The rules now allow users to update their own documents in **two ways**:
1. If document ID matches their Firebase Auth UID
2. If `firebaseUid` field matches their Firebase Auth UID

---

## 📋 **Step-by-Step Fix:**

### **Step 1: Apply New Firestore Rules**

1. **Open Firebase Console:** https://console.firebase.google.com
2. **Select Project:** accizard-lucban
3. **Navigate to:** Firestore Database → Rules tab
4. **Copy the rules from:** `UPDATED_FIRESTORE_RULES_FOR_FCM.txt`
5. **Paste and Publish**

**Key changes in the rules:**
```javascript
// OLD (Line 80):
allow update: if isOwner(resource.data.firebaseUid) || isAdmin();

// NEW (Lines 80-84):
allow update: if isAuthenticated() && 
                 (request.auth.uid == userId ||  // Document ID matches
                  resource.data.firebaseUid == request.auth.uid ||  // Field matches
                  isAdmin());
```

---

### **Step 2: Sync and Rebuild Android App**

```bash
# In Android Studio terminal or command prompt:

# 1. Clean build
./gradlew clean

# 2. Rebuild
./gradlew build

# Or use Android Studio:
# Build → Clean Project
# Build → Rebuild Project
```

---

### **Step 3: Test the Fix**

#### **A. Uninstall and Reinstall**
```bash
# Uninstall completely (resets all data)
adb uninstall com.example.accizardlucban

# Rebuild and install
./gradlew installDebug
```

#### **B. Login and Monitor Logs**

1. **Open Logcat** in Android Studio
2. **Filter by:** `FCMTokenManager`
3. **Login to the app**
4. **Watch for logs:**

**Expected Success Logs:**
```
D/FCMTokenManager: ✅ FCM token obtained: dXpN3F5T9K...
D/FCMTokenManager: FCM token saved to SharedPreferences
D/FCMTokenManager: Attempting to save FCM token for Firebase Auth UID: abc123...
D/FCMTokenManager: Found user document with ID: xyz789
D/FCMTokenManager: ✅ FCM token saved to Firestore for user document: xyz789
D/FCMTokenManager: Token: dXpN3F5T9K...
```

**OR if document ID matches UID:**
```
D/FCMTokenManager: No user document found with firebaseUid=abc123, trying document ID approach
D/FCMTokenManager: ✅ FCM token saved to Firestore for user: abc123
```

---

### **Step 4: Verify in Firestore**

1. **Open Firestore Console**
2. **Navigate to:** users collection
3. **Find your user document**
4. **Check for new fields:**
   ```javascript
   {
     email: "user@example.com",
     firstName: "John",
     firebaseUid: "abc123...",
     fcmToken: "dXpN3F5T9K...",  // ✅ Should now exist!
     lastUpdated: 1697456789000,  // ✅ Timestamp
     // ... other fields
   }
   ```

---

### **Step 5: Test Push Notifications**

1. **From Web App:** Create a new announcement
2. **Check Cloud Function Logs:** Should now say "Broadcasting announcement to 1 users"
3. **Check Android Device:** Notification should appear! 🎉

---

## 🔍 **Detailed Troubleshooting:**

### **Issue 1: Still Getting Permission Denied**

**Check:**
```bash
# View full Logcat
adb logcat | findstr /i "fcmtoken"

# Look for:
D/FCMTokenManager: Attempting to save FCM token for Firebase Auth UID: {uid}
D/FCMTokenManager: Found user document with ID: {docId}
```

**If you see:**
```
E/FCMTokenManager: Failed to query users collection: PERMISSION_DENIED
```

**Fix:** Firestore rules not published correctly
- Re-publish the rules in Firebase Console
- Wait 1-2 minutes for propagation
- Restart the app

---

### **Issue 2: User Document Not Found**

**If you see:**
```
W/FCMTokenManager: No user document found with firebaseUid=abc123
E/FCMTokenManager: Failed to save FCM token to Firestore: PERMISSION_DENIED
```

**This means:**
- Your user document doesn't have a `firebaseUid` field
- OR the `firebaseUid` value doesn't match your Firebase Auth UID

**Fix:**

**Option A - Check Current Document:**
1. Go to Firestore Console → users collection
2. Find your user document
3. Check what fields exist
4. Note the document ID

**Option B - Manually Add `firebaseUid` Field:**
1. In Firestore Console, open your user document
2. Click "+ Add field"
3. Field name: `firebaseUid`
4. Field value: Your Firebase Auth UID (from Logcat)
5. Save

**Option C - Share Info for Further Help:**
- Document ID: `_____`
- firebaseUid field value: `_____`
- Firebase Auth UID (from Logcat): `_____`

---

### **Issue 3: Document ID Doesn't Match UID**

**If your Firestore structure is:**
```
users/
  ├─ user_12345/        ← Document ID
  │   ├─ firebaseUid: "abc123xyz"  ← Different from document ID
  │   ├─ email: "user@example.com"
  │   └─ ...
```

**The updated code handles this!** It:
1. Queries by `firebaseUid` field first ✅
2. Finds document `user_12345`
3. Updates that document with `fcmToken`

---

## 🎯 **Quick Diagnostic:**

### **Run this in Logcat filter:**
```
FCMTokenManager|MainActivity
```

### **Expected Flow:**
```
1. D/MainActivity: ✅ FCM token initialization started
2. D/FCMTokenManager: Initializing FCM token for user: abc123
3. D/FCMTokenManager: ✅ FCM token obtained: dXpN3F5T...
4. D/FCMTokenManager: Attempting to save FCM token for Firebase Auth UID: abc123
5. D/FCMTokenManager: Found user document with ID: xyz789
6. D/FCMTokenManager: ✅ FCM token saved to Firestore for user document: xyz789
```

### **If it fails at step 5:**
Check Firestore Console to see if a document with `firebaseUid = abc123` exists.

### **If it fails at step 6:**
Check Firestore rules are published and correctly formatted.

---

## 📱 **Manual Testing Commands:**

### **Check Firebase Auth UID:**
In Logcat, filter by `MainActivity` and look for:
```
D/MainActivity: ✅ FCM token initialization started
```

Right before this, check:
```
D/FCMTokenManager: Initializing FCM token for user: {YOUR_FIREBASE_UID}
```

### **Check Firestore Document:**
1. Firebase Console → Firestore
2. Find user document
3. Check if `firebaseUid` field matches the UID from Logcat

### **Force Re-login:**
```bash
# Clear app data
adb shell pm clear com.example.accizardlucban

# Or uninstall
adb uninstall com.example.accizardlucban
```

---

## ✅ **Success Checklist:**

- [ ] Firestore rules updated and published
- [ ] Android app rebuilt and reinstalled
- [ ] User logged in successfully
- [ ] Logcat shows: "✅ FCM token saved to Firestore"
- [ ] Firestore shows `fcmToken` field in user document
- [ ] Cloud Function logs show: "Broadcasting announcement to X users"
- [ ] Push notification appears on device! 🎉

---

## 🆘 **Still Not Working?**

Share these details:

1. **Firestore User Document Structure:**
   ```
   Document ID: _____
   firebaseUid: _____
   (screenshot if possible)
   ```

2. **Full Logcat Output** (filter by FCMTokenManager):
   ```
   (paste logs here)
   ```

3. **Firebase Console Rules:**
   ```
   (confirm rules were published)
   ```

4. **Cloud Function Logs:**
   ```
   (from Firebase Console → Functions → Logs)
   ```

---

**Last Updated:** October 16, 2025  
**Status:** Code updated, awaiting user testing

