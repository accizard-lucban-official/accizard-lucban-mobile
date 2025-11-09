package com.example.accizardlucban;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestEmailActivity extends AppCompatActivity {

    private static final String TAG = "TestEmailActivity";
    
    private EditText etTestEmail;
    private Button btnSendTestEmail;
    private TextView tvStatus, tvLogs;
    private ProgressBar progressBar;
    private StringBuilder logBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_email);

        initializeViews();
        setupClickListeners();
        logBuilder = new StringBuilder();
    }

    private void initializeViews() {
        etTestEmail = findViewById(R.id.etTestEmail);
        btnSendTestEmail = findViewById(R.id.btnSendTestEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvLogs = findViewById(R.id.tvLogs);
        progressBar = findViewById(R.id.progressBar);
        
        progressBar.setVisibility(View.GONE);
        tvStatus.setText("Ready to test email");
    }

    private void setupClickListeners() {
        btnSendTestEmail.setOnClickListener(v -> sendTestEmail());
    }

    private void sendTestEmail() {
        String email = etTestEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            etTestEmail.setError("Email is required");
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etTestEmail.setError("Invalid email format");
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear previous logs
        logBuilder.setLength(0);
        tvLogs.setText("");
        
        setLoadingState(true);
        addLog("Starting email test for: " + email);
        tvStatus.setText("Sending test email...");

        EmailService.sendPasswordResetEmail(email, new EmailService.EmailCallback() {
            @Override
            public void onSuccess() {
                setLoadingState(false);
                addLog("✅ SUCCESS: Email sent successfully!");
                tvStatus.setText("✅ Email sent successfully!");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                
                Toast.makeText(TestEmailActivity.this, 
                    "Test email sent successfully!\n\nCheck your email app for the password reset message.", 
                    Toast.LENGTH_LONG).show();
                
                // Show instructions
                addLog("\n📱 NEXT STEPS:");
                addLog("1. Check your email app for notifications");
                addLog("2. Look for 'Password Reset Request - AcciZard Lucban'");
                addLog("3. Click the 'Reset My Password' button in the email");
                addLog("4. The app should open automatically");
            }

            @Override
            public void onError(String error) {
                setLoadingState(false);
                addLog("❌ ERROR: " + error);
                tvStatus.setText("❌ Email sending failed");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                
                Toast.makeText(TestEmailActivity.this, 
                    "Email sending failed: " + error, 
                    Toast.LENGTH_LONG).show();
                
                // Show troubleshooting
                addLog("\n🔧 TROUBLESHOOTING:");
                addLog("1. Check internet connection");
                addLog("2. Verify email credentials in EmailService.java");
                addLog("3. Enable 'Less secure app access' in Gmail");
                addLog("4. Use Gmail App Password instead of regular password");
            }
        });
    }

    private void addLog(String message) {
        Log.d(TAG, message);
        logBuilder.append(message).append("\n");
        runOnUiThread(() -> tvLogs.setText(logBuilder.toString()));
    }

    private void setLoadingState(boolean isLoading) {
        btnSendTestEmail.setEnabled(!isLoading);
        btnSendTestEmail.setText(isLoading ? "Sending..." : "Send Test Email");
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        etTestEmail.setEnabled(!isLoading);
        
        if (isLoading) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
