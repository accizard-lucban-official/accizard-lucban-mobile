# 📧 Email Setup Guide for AcciZard Lucban Password Reset

## 🔧 Setup Instructions

### Step 1: Configure Gmail Account
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Select "Mail" and generate password
   - Copy the 16-character password

### Step 2: Update EmailService.java
Replace these lines in `EmailService.java`:
```java
private static final String EMAIL_FROM = "your-actual-email@gmail.com";
private static final String EMAIL_PASSWORD = "your-16-character-app-password";
```

### Step 3: Test Email Functionality
1. Build and run the app
2. Go to "Forgot Password" screen
3. Enter a registered email address
4. Check your mobile phone for email notification

## 📱 How It Works

### Email Features:
- **Professional HTML email** with AcciZard branding
- **Clickable "Reset My Password" button** that opens the app
- **6-digit backup code** if the button doesn't work
- **Mobile-optimized design** for phone notifications

### Deep Link Features:
- **Automatic app opening** when clicking email link
- **Direct navigation** to password reset screen
- **Token validation** and email verification
- **Secure password reset** with strong password requirements

## 🔗 Deep Link URLs

The system generates URLs like:
```
https://accizardlucban.app/reset-password?token=123456&email=user@email.com
```

When clicked on mobile:
1. **Opens your app automatically**
2. **Shows password reset screen**
3. **Pre-fills email and token**
4. **User enters new password**
5. **Password is reset successfully**

## 📧 Email Template Features

- **Mobile-responsive design**
- **Clear call-to-action button**
- **Step-by-step instructions**
- **Security warnings**
- **Professional branding**
- **Backup reset code**

## 🚀 Testing Checklist

- [ ] Gmail app password configured
- [ ] Email credentials updated in code
- [ ] App builds successfully
- [ ] Email sends to mobile phone
- [ ] Email appears in notifications
- [ ] Reset button opens app
- [ ] Password reset works
- [ ] New password login works

## 📞 Support

If you need help with setup:
- Check Gmail app password generation
- Verify email credentials in code
- Test with a real email address
- Check mobile email notifications
- Ensure app deep linking works
