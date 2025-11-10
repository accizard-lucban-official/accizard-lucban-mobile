# Firestore Rules Update Guide - Report Privacy Fix

## 📋 What Changed

The **Reports collection rules** have been updated to ensure users can only see their own reports, while admins can still manage all reports.

## 🔒 New Security Rules for Reports Collection

### Before (Insecure):
```javascript
match /reports/{reportId} {
  // ❌ PROBLEM: Anyone can read ALL reports
  allow read: if true;
  
  allow create: if isAuthenticated() && 
                   request.resource.data.userId == request.auth.uid;
  
  // ❌ PROBLEM: Anyone can update/delete any report
  allow update, delete: if true;
}
```

### After (Secure):
```javascript
match /reports/{reportId} {
  // ✅ Users can only read their own reports
  // ✅ Admins can read all reports
  allow read: if isAuthenticated() && 
                 (resource.data.userId == request.auth.uid ||  // User's own report
                  isAdmin());  // Or user is admin
  
  // ✅ Users can only create reports with their own userId
  allow create: if isAuthenticated() && 
                   request.resource.data.userId == request.auth.uid;
  
  // ✅ Users can update their own reports
  // ✅ Admins can update any report
  allow update: if isAuthenticated() && 
                   (resource.data.userId == request.auth.uid ||  // User's own report
                    isAdmin());  // Or user is admin
  
  // ✅ Users can delete their own reports
  // ✅ Admins can delete any report
  allow delete: if isAuthenticated() && 
                   (resource.data.userId == request.auth.uid ||  // User's own report
                    isAdmin());  // Or user is admin
}
```

## 📝 How to Apply These Rules

### Step 1: Open Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **"accizard-lucban"**

### Step 2: Navigate to Firestore Rules
1. Click on **"Firestore Database"** in the left sidebar
2. Click on the **"Rules"** tab at the top

### Step 3: Copy the Updated Rules
1. Open the file: `UPDATED_FIRESTORE_RULES_WITH_REPORT_SECURITY.txt`
2. **Select ALL** the content (Ctrl+A or Cmd+A)
3. **Copy** it (Ctrl+C or Cmd+C)

### Step 4: Replace Existing Rules
1. In the Firebase Console, **select all existing rules** in the editor
2. **Delete** them
3. **Paste** the new rules from Step 3
4. Click **"Publish"** button at the top right

### Step 5: Verify the Rules
After publishing, you should see a success message. The rules are now active!

## 🔍 What Each Rule Does

### Read Rule
```javascript
allow read: if isAuthenticated() && 
               (resource.data.userId == request.auth.uid || isAdmin());
```
- **Users**: Can only read reports where `userId` matches their Firebase Auth UID
- **Admins**: Can read ALL reports (for management dashboard)
- **Unauthenticated**: Cannot read any reports

### Create Rule
```javascript
allow create: if isAuthenticated() && 
                 request.resource.data.userId == request.auth.uid;
```
- **Users**: Can only create reports with their own `userId`
- **Prevents**: Users from creating fake reports under someone else's name
- **Requires**: User must be authenticated

### Update Rule
```javascript
allow update: if isAuthenticated() && 
                 (resource.data.userId == request.auth.uid || isAdmin());
```
- **Users**: Can update their own reports (edit description, add images, etc.)
- **Admins**: Can update any report (change status, priority, etc.)
- **Use Case**: User fixes a mistake before admin processes it

### Delete Rule
```javascript
allow delete: if isAuthenticated() && 
                 (resource.data.userId == request.auth.uid || isAdmin());
```
- **Users**: Can delete their own reports (if submitted by mistake)
- **Admins**: Can delete any report (remove spam, duplicates, etc.)
- **Safety**: Users cannot delete other users' reports

## 🎯 Benefits of New Rules

### ✅ Privacy Protection
- Users cannot see other users' personal information
- Reports containing sensitive location data are protected
- Complies with data privacy regulations (GDPR, etc.)

### ✅ Data Integrity
- Users cannot modify or delete others' reports
- Prevents malicious users from tampering with the database
- Admins maintain full control for management purposes

### ✅ User Experience
- New users see empty Report Log (no confusion)
- Users only see their own submission history
- Clean, personalized experience

### ✅ Admin Functionality Preserved
- Admins can still view all reports
- Admins can update report status (Pending → Ongoing → Responded)
- Admins can manage all aspects of reports

## 🧪 Testing Your Rules

### Test 1: User Can Read Own Reports
```javascript
// Simulate a user trying to read their own report
// Should: ✅ ALLOW
{
  "userId": "user123_firebase_uid",
  "reportId": "report_abc"
}
// Query: reports where userId == "user123_firebase_uid"
// Result: SUCCESS
```

### Test 2: User Cannot Read Others' Reports
```javascript
// Simulate user123 trying to read user456's report
// Should: ❌ DENY
{
  "currentUser": "user123_firebase_uid",
  "reportUserId": "user456_firebase_uid"
}
// Query: reports where userId == "user456_firebase_uid"
// Result: PERMISSION DENIED
```

### Test 3: Admin Can Read All Reports
```javascript
// Simulate admin trying to read any report
// Should: ✅ ALLOW
{
  "currentUser": "admin_firebase_uid",
  "isAdmin": true
}
// Query: ALL reports
// Result: SUCCESS (can see all reports)
```

### Test 4: User Can Only Create with Own userId
```javascript
// Simulate user trying to create report with their own userId
// Should: ✅ ALLOW
{
  "currentUser": "user123_firebase_uid",
  "reportData": {
    "userId": "user123_firebase_uid",
    "description": "Road accident"
  }
}
// Result: SUCCESS

// Simulate user trying to create report with different userId
// Should: ❌ DENY
{
  "currentUser": "user123_firebase_uid",
  "reportData": {
    "userId": "user456_firebase_uid",  // ❌ Not their own userId
    "description": "Road accident"
  }
}
// Result: PERMISSION DENIED
```

## 🔧 Testing in Firebase Console

You can test these rules directly in Firebase Console:

1. Go to **Firestore Database** → **Rules** tab
2. Click **"Test Rules"** button
3. Select **"Read"** operation
4. Enter collection path: `reports/test_report_id`
5. Set authentication: Add `request.auth.uid` value
6. Click **"Run"** to see if access is allowed or denied

## 🚨 Important Notes

### Note 1: Existing Data
- These rules apply immediately to ALL existing reports
- No data migration needed
- Existing reports are automatically protected

### Note 2: Admin Access
- Make sure admin users are properly registered in either:
  - `/superAdmin/{adminUid}` collection (Super Admins)
  - `/admins/{adminUid}` collection (LDRRMO Admins)
- The `isAdmin()` helper function checks both collections

### Note 3: Mobile App Compatibility
- The Android app code (ReportSubmissionActivity.java) now matches these rules
- App queries: `.whereEqualTo("userId", currentUser.getUid())`
- Rules enforce: `resource.data.userId == request.auth.uid`
- Perfect alignment between app and security rules ✅

### Note 4: Web Dashboard
- Admin web dashboard users may use localStorage authentication
- If your admins DON'T have Firebase Authentication, you may need to adjust
- Consider adding Firebase Auth for admin users for better security

## 📞 Troubleshooting

### Problem: "Permission Denied" Error for Users
**Cause**: User's Firebase Auth UID doesn't match the `userId` field in the report

**Solution**:
1. Check that reports are created with correct `userId`
2. Verify: `reportData.put("userId", currentUser.getUid())`
3. Ensure user is properly authenticated when submitting

### Problem: Admin Cannot See Reports
**Cause**: Admin user is not registered in `superAdmin` or `admins` collection

**Solution**:
1. Go to Firestore Database
2. Check if admin's UID exists in `/superAdmin` or `/admins` collection
3. Add admin UID if missing:
   ```
   Collection: admins
   Document ID: {admin_firebase_uid}
   Fields: (any fields needed for your admin data)
   ```

### Problem: New Users See Other Users' Reports
**Cause**: Old Firestore rules still active (haven't published new rules)

**Solution**:
1. Make sure you clicked **"Publish"** button in Firebase Console
2. Wait 1-2 minutes for rules to propagate
3. Clear app data and re-login
4. Check if issue persists

## ✅ Verification Checklist

After applying the rules, verify:

- [ ] New user accounts have empty Report Log
- [ ] Users can submit reports successfully
- [ ] Users can see their own reports
- [ ] Users CANNOT see other users' reports
- [ ] Admins can see ALL reports in admin dashboard
- [ ] Admins can update report status
- [ ] No "Permission Denied" errors for normal operations

## 📚 Related Files

- `REPORT_LOG_USER_FILTER_FIX.md` - Android app code changes
- `UPDATED_FIRESTORE_RULES_WITH_REPORT_SECURITY.txt` - Complete rules file
- `app/src/main/java/com/example/accizardlucban/ReportSubmissionActivity.java` - Updated app code

---

**Last Updated**: October 19, 2025  
**Status**: Ready to Deploy  
**Priority**: High (Privacy & Security Fix)




























