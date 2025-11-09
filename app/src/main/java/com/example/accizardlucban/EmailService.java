package com.example.accizardlucban;

import android.util.Log;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    
    private static final String TAG = "EmailService";
    
    // Email configuration - Replace with your actual email service credentials
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "accizardlucban.test@gmail.com"; // Replace with your actual Gmail
    private static final String EMAIL_PASSWORD = "test_app_password_123"; // Replace with your Gmail App Password
    
    // Test mode - set to true for testing without real email
    private static final boolean TEST_MODE = true;
    
    // Deep link configuration
    private static final String RESET_BASE_URL = "https://accizardlucban.app/reset-password";
    
    public interface EmailCallback {
        void onSuccess();
        void onError(String error);
    }
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static void sendPasswordResetEmail(String recipientEmail, EmailCallback callback) {
        executor.execute(new SendEmailTask(recipientEmail, callback));
    }
    
    private static class SendEmailTask implements Runnable {
        private final String recipientEmail;
        private final EmailCallback callback;
        private final String resetToken;
        
        public SendEmailTask(String recipientEmail, EmailCallback callback) {
            this.recipientEmail = recipientEmail;
            this.callback = callback;
            this.resetToken = generateResetToken();
        }
        
        @Override
        public void run() {
            try {
                Log.d(TAG, "Starting email send process for: " + recipientEmail);
                
                if (TEST_MODE) {
                    // Simulate email sending for testing
                    Log.d(TAG, "TEST MODE: Simulating email send to " + recipientEmail);
                    Thread.sleep(2000); // Simulate network delay
                    
                    // Post success to main thread
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        Log.d(TAG, "TEST MODE: Email simulation completed successfully");
                        callback.onSuccess();
                    });
                    return;
                }
                
                // Configure email properties
                Properties props = new Properties();
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.connectiontimeout", "10000");
                props.put("mail.smtp.timeout", "10000");
                props.put("mail.smtp.writetimeout", "10000");
                
                Log.d(TAG, "SMTP properties configured");
                
                // Create session with authentication
                Log.d(TAG, "Creating email session with authentication");
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        Log.d(TAG, "Authenticating with email: " + EMAIL_FROM);
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                });
                
                // Enable debug mode for JavaMail
                session.setDebug(true);
                
                // Create message
                Log.d(TAG, "Creating email message");
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM, "AcciZard Lucban"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                message.setSubject("Password Reset Request - AcciZard Lucban");
                
                // Create HTML email content
                Log.d(TAG, "Generating email template with token: " + resetToken);
                String htmlContent = createEmailTemplate(resetToken);
                message.setContent(htmlContent, "text/html; charset=utf-8");
                
                // Send email
                Log.d(TAG, "Attempting to send email via SMTP");
                Transport.send(message);
                Log.d(TAG, "Email sent successfully via SMTP");
                
                Log.d(TAG, "Password reset email sent successfully to: " + recipientEmail);
                
                // Post result to main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess());
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email: " + e.getMessage(), e);
                e.printStackTrace();
                
                // Post error to main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    String errorMsg = "Email send failed: " + e.getMessage();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                });
            }
        }
        
        private String generateResetToken() {
            // Generate a random 6-digit token for demonstration
            Random random = new Random();
            return String.format("%06d", random.nextInt(1000000));
        }
        
        private String createEmailTemplate(String resetToken) {
            String resetLink = RESET_BASE_URL + "?token=" + resetToken + "&email=" + recipientEmail;
            return "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Password Reset - AcciZard Lucban</title>" +
                    "    <style>" +
                    "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                    "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }" +
                    "        .header { background: linear-gradient(135deg, #FF6B35, #FF8E53); color: white; padding: 30px; text-align: center; }" +
                    "        .logo { font-size: 28px; font-weight: bold; margin-bottom: 10px; }" +
                    "        .tagline { font-size: 14px; opacity: 0.9; }" +
                    "        .content { padding: 40px 30px; }" +
                    "        .greeting { font-size: 18px; margin-bottom: 20px; }" +
                    "        .message { font-size: 16px; margin-bottom: 30px; line-height: 1.8; }" +
                    "        .token-container { background-color: #f8f9fa; border: 2px dashed #FF6B35; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }" +
                    "        .token { font-size: 32px; font-weight: bold; color: #FF6B35; letter-spacing: 3px; margin: 10px 0; }" +
                    "        .instructions { background-color: #e8f4fd; border-left: 4px solid #2196F3; padding: 20px; margin: 30px 0; }" +
                    "        .instructions h3 { margin-top: 0; color: #1976D2; }" +
                    "        .step { margin: 15px 0; padding-left: 20px; position: relative; }" +
                    "        .step::before { content: '→'; position: absolute; left: 0; color: #FF6B35; font-weight: bold; }" +
                    "        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 14px; color: #666; }" +
                    "        .contact-info { margin-top: 20px; }" +
                    "        .social-links { margin-top: 15px; }" +
                    "        .btn { display: inline-block; background-color: #FF6B35; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 10px 0; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <div class='logo'>🚨 AcciZard Lucban</div>" +
                    "            <div class='tagline'>REPORT • PROTECT • PREVENT</div>" +
                    "        </div>" +
                    "        " +
                    "        <div class='content'>" +
                    "            <div class='greeting'>Hello,</div>" +
                    "            " +
                    "            <div class='message'>" +
                    "                We received a request to reset your password for your AcciZard Lucban account. " +
                    "                If you didn't make this request, please ignore this email and your password will remain unchanged." +
                    "            </div>" +
                    "            " +
                    "            <div class='reset-button-container' style='text-align: center; margin: 30px 0;'>" +
                    "                <a href='" + resetLink + "' class='reset-button' style='display: inline-block; background-color: #FF6B35; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-size: 18px; font-weight: bold; margin: 10px 0;'>🔐 Reset My Password</a>" +
                    "                <br><small style='color: #666; margin-top: 10px; display: block;'>This link expires in 15 minutes</small>" +
                    "            </div>" +
                    "            " +
                    "            <div class='token-container'>" +
                    "                <strong>Alternative: Use Reset Code</strong>" +
                    "                <div class='token'>" + resetToken + "</div>" +
                    "                <small style='color: #666;'>If the button doesn't work, use this code in the app</small>" +
                    "            </div>" +
                    "            " +
                    "            <div class='instructions'>" +
                    "                <h3>📱 Two Ways to Reset Your Password:</h3>" +
                    "                <div style='background: #e8f5e8; padding: 15px; border-radius: 8px; margin: 15px 0;'>" +
                    "                    <strong>🎯 Method 1 (Recommended): Click the Reset Button</strong>" +
                    "                    <div class='step'>Click the 'Reset My Password' button above</div>" +
                    "                    <div class='step'>You'll be taken directly to the password reset page</div>" +
                    "                    <div class='step'>Enter your new password and confirm it</div>" +
                    "                </div>" +
                    "                <div style='background: #fff3cd; padding: 15px; border-radius: 8px; margin: 15px 0;'>" +
                    "                    <strong>🔢 Method 2: Use the Reset Code</strong>" +
                    "                    <div class='step'>Open the AcciZard Lucban app on your mobile device</div>" +
                    "                    <div class='step'>Go to the Login screen and tap 'Forgot Password?'</div>" +
                    "                    <div class='step'>Enter your email address (" + recipientEmail + ")</div>" +
                    "                    <div class='step'>When prompted, enter the reset code: <strong>" + resetToken + "</strong></div>" +
                    "                    <div class='step'>Create your new password and confirm it</div>" +
                    "                </div>" +
                    "            </div>" +
                    "            " +
                    "            <div class='warning'>" +
                    "                <strong>⚠️ Security Notice:</strong><br>" +
                    "                • This reset code will expire in 15 minutes<br>" +
                    "                • Never share this code with anyone<br>" +
                    "                • If you didn't request this reset, please contact our support team immediately" +
                    "            </div>" +
                    "            " +
                    "            <div class='message'>" +
                    "                If you're having trouble with the reset process, please don't hesitate to contact our support team. " +
                    "                We're here to help ensure your account remains secure." +
                    "            </div>" +
                    "        </div>" +
                    "        " +
                    "        <div class='footer'>" +
                    "            <strong>AcciZard Lucban Support Team</strong>" +
                    "            <div class='contact-info'>" +
                    "                📧 Email: support@accizardlucban.com<br>" +
                    "                📞 Phone: +63 123 456 7890<br>" +
                    "                📍 Address: Lucban, Quezon, Philippines" +
                    "            </div>" +
                    "            " +
                    "            <div style='margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                    "                <small>" +
                    "                    This email was sent to " + recipientEmail + " because a password reset was requested for your AcciZard Lucban account. " +
                    "                    If you didn't request this, please ignore this email." +
                    "                </small>" +
                    "            </div>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
        }
    }
}
