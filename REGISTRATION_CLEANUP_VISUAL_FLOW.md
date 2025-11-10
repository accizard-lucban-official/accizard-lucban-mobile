# Registration Data Cleanup - Visual Flow 📊

## 🎯 **Smart Data Management**

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DATA RETENTION STRATEGY                           │
└─────────────────────────────────────────────────────────────────────┘

KEEP DATA (Within Registration):
├─> PersonalInfo → Address → Back → PersonalInfo ✅ Data retained
├─> Address → Profile → Back → Address ✅ Data retained
└─> Profile → ValidID → Back → Profile ✅ Data retained

CLEAR DATA (Exit/Complete):
├─> Start new registration ✅ Clear old data
├─> Exit from PersonalInfo ✅ Clear all data
└─> Complete registration ✅ Clear all data


┌─────────────────────────────────────────────────────────────────────┐
│                    SCENARIO 1: NORMAL REGISTRATION                   │
└─────────────────────────────────────────────────────────────────────┘

User: Start Registration
    ↓
┌───────────────────────────────┐
│ RegistrationActivity.onCreate │
│ Clear old data ✅             │
│ Log: "Fresh start"            │
└───────────────────────────────┘
    ↓
PersonalInfo (Empty fields) ✅
    ↓ [Fill birthday, gender, etc.]
    ↓ [Click Next]
    ↓ [Save data]
    ↓
AddressInfo (Empty fields)
    ↓ [Fill province, city, etc.]
    ↓ [Click Next]
    ↓ [Save data]
    ↓
ProfilePicture (No image)
    ↓ [Upload & crop image]
    ↓ [Click Next]
    ↓ [Save data]
    ↓
ValidID (No images)
    ↓ [Upload IDs]
    ↓ [Click Next]
    ↓ [Create account]
    ↓ [Clear all data] ✅
    ↓
SuccessActivity ✅


┌─────────────────────────────────────────────────────────────────────┐
│                    SCENARIO 2: EXIT REGISTRATION                     │
└─────────────────────────────────────────────────────────────────────┘

User: Start Registration
    ↓
RegistrationActivity
    ↓ [Clear old data] ✅
    ↓
PersonalInfo
    ↓ [Fill some fields]
    ↓ [Birthday: "01/15/1990"]
    ↓ [Gender: "Male"]
    ↓
User decides to exit
    ↓ [Clicks Back button]
    ↓
┌───────────────────────────────┐
│ PersonalInfo.onBackPressed    │
│ Clear ALL data ✅             │
│ Toast: "Registration canceled"│
└───────────────────────────────┘
    ↓
RegistrationActivity (returned)
    ↓
User tries again later
    ↓
RegistrationActivity → PersonalInfo
    ↓
PersonalInfo opens with:
    ├─> Birthday: EMPTY ✅
    ├─> Gender: "Prefer not to say" (default) ✅
    └─> All fields: CLEAN ✅


┌─────────────────────────────────────────────────────────────────────┐
│                    SCENARIO 3: NAVIGATE WITHIN REGISTRATION          │
└─────────────────────────────────────────────────────────────────────┘

PersonalInfo
    ↓ [Fill: Birthday "01/15/1990", Gender "Male"]
    ↓ [Click Next]
    ↓ [Data SAVED ✅]
    ↓
AddressInfo
    ↓ [Fill: Province "Quezon", City "Lucban"]
    ↓ [Click Next]
    ↓ [Data SAVED ✅]
    ↓
ProfilePicture
    ↓ [Upload image]
    ↓ [User realizes they need to change address]
    ↓ [Click Back]
    ↓ [Image SAVED ✅]
    ↓
AddressInfo (Data RESTORED ✅)
    ├─> Province: "Quezon" ✅
    ├─> City: "Lucban" ✅
    └─> Toast: "Address information restored"
    ↓ [User updates province to "Metro Manila"]
    ↓ [Click Back]
    ↓ [Data SAVED ✅]
    ↓
PersonalInfo (Data RESTORED ✅)
    ├─> Birthday: "01/15/1990" ✅
    ├─> Gender: "Male" ✅
    └─> Toast: "Personal information restored"
    ↓ [Click Next → Next → Next]
    ↓
ProfilePicture (Image RESTORED ✅)
    └─> Image displayed ✅
    ↓ [Continue registration...]


┌─────────────────────────────────────────────────────────────────────┐
│                    DATA STATE DIAGRAM                                │
└─────────────────────────────────────────────────────────────────────┘

Event                           | Data Action       | User Sees
─────────────────────────────────────────────────────────────────────
Start RegistrationActivity      | CLEAR ALL ✅      | Empty fields
PersonalInfo → Next             | SAVE ✅           | -
Address → Back                  | SAVE ✅           | Data restored
PersonalInfo (returned)         | RESTORE ✅        | Previous data
PersonalInfo → Back (exit)      | CLEAR ALL ✅      | Toast: "Canceled"
RegistrationActivity (returned) | -                 | -
Start registration again        | CLEAR ALL ✅      | Empty fields
Complete registration           | CLEAR ALL ✅      | -


┌─────────────────────────────────────────────────────────────────────┐
│                    DECISION TREE                                     │
└─────────────────────────────────────────────────────────────────────┘

User clicks Back button in any registration screen:
    ↓
Is current activity PersonalInfoActivity?
    │
    ├─> YES (first step)
    │   └─> User is EXITING registration
    │       └─> Clear ALL data ✅
    │           └─> Toast: "Registration canceled"
    │               └─> Return to RegistrationActivity
    │
    └─> NO (other steps)
        └─> User is NAVIGATING within registration
            └─> Save current data ✅
                └─> Toast: "Data restored" (on return)
                    └─> Return to previous screen


┌─────────────────────────────────────────────────────────────────────┐
│                    BEFORE vs AFTER                                   │
└─────────────────────────────────────────────────────────────────────┘

BEFORE THIS FIX:
────────────────
Attempt 1:
  Start → Fill "John", "01/15/1990" → Exit

Attempt 2:
  Start → See "John", "01/15/1990" ❌
  User confused, has to clear manually
  Bad UX

AFTER THIS FIX:
───────────────
Attempt 1:
  Start → Fill "John", "01/15/1990" → Exit
  ↓ [Data cleared] ✅

Attempt 2:
  Start → See empty fields ✅
  User fills fresh data
  Good UX


┌─────────────────────────────────────────────────────────────────────┐
│                    CLEANUP POINTS                                    │
└─────────────────────────────────────────────────────────────────────┘

Point 1: RegistrationActivity.onCreate()
    └─> Clears: ALL old registration data
        └─> Reason: Fresh start for new registration

Point 2: PersonalInfoActivity.onBackPressed()
    └─> Clears: ALL current registration data
        └─> Reason: User exiting registration

Point 3: ValidIdActivity (on success)
    └─> Clears: ALL registration data
        └─> Reason: Registration complete

Timeline:
═════════
T=0     : Start registration → CLEAR ✅
T=1min  : Fill PersonalInfo → Save
T=2min  : Fill Address → Save
T=3min  : Exit registration → CLEAR ✅
T=5min  : Start again → CLEAR ✅
T=6min  : Complete registration → CLEAR ✅


┌─────────────────────────────────────────────────────────────────────┐
│                    KEY METHODS                                       │
└─────────────────────────────────────────────────────────────────────┘

RegistrationActivity:
    └─> clearPreviousRegistrationData()
        ├─> Called in: onCreate()
        └─> Action: editor.clear()

PersonalInfoActivity:
    └─> clearAllRegistrationData()
        ├─> Called in: onBackPressed(), btnBack.onClick()
        └─> Action: editor.clear() + Toast

ValidIdActivity:
    └─> clearRegistrationData()
        ├─> Called in: On successful account creation
        └─> Action: Remove specific keys


┌─────────────────────────────────────────────────────────────────────┐
│                    USER FEEDBACK                                     │
└─────────────────────────────────────────────────────────────────────┘

When user EXITS registration:
    ┌─────────────────────────────────┐
    │  Toast Message:                 │
    │  "Registration canceled"        │
    │                                 │
    │  Logcat:                        │
    │  "✅ All registration data      │
    │   cleared - user exited"        │
    └─────────────────────────────────┘

When user STARTS new registration:
    ┌─────────────────────────────────┐
    │  Logcat:                        │
    │  "✅ Previous registration      │
    │   data cleared - fresh start"   │
    │                                 │
    │  UI:                            │
    │  All fields empty ✅            │
    └─────────────────────────────────┘

When NAVIGATING within registration:
    ┌─────────────────────────────────┐
    │  Toast Message:                 │
    │  "Personal information restored"│
    │  "Address information restored" │
    │  "Profile picture restored"     │
    │  "X ID image(s) restored"       │
    └─────────────────────────────────┘
```

---

## ✅ **Summary**

**Problem:** Old registration data persisted when user exited and tried again

**Solution:**
- ✅ Clear data when starting new registration
- ✅ Clear data when exiting registration
- ✅ Keep data when navigating within registration
- ✅ Clear data when registration completes

**Result:** Perfect UX - clean start, convenient navigation!

---

*Visual flow for registration data cleanup.*

**Clean, professional registration experience!** ✨


























