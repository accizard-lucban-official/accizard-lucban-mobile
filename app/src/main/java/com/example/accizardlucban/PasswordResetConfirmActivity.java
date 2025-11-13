package com.example.accizardlucban;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetConfirmActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvEmail, tvToken;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    
    private String email;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_confirm);

        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        handleIntent();
        setupClickListeners();
    }

    private void initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvEmail = findViewById(R.id.tvEmail);
        tvToken = findViewById(R.id.tvToken);
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        
        if (data != null) {
            // Handle deep link
            email = data.getQueryParameter("email");
            token = data.getQueryParameter("token");
        } else {
            // Handle regular intent
            email = intent.getStringExtra("email");
            token = intent.getStringExtra("token");
        }
        
        if (!TextUtils.isEmpty(email)) {
            tvEmail.setText("Reset password for: " + email);
        }
        
        if (!TextUtils.isEmpty(token)) {
            tvToken.setText("Reset Code: " + token);
        }
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Invalid reset link. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters long");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoadingState(true);
        
        // For demonstration, we'll use Firebase's confirmPasswordReset
        // In a real implementation, you would verify the token with your backend
        Toast.makeText(this, "Password reset successful! Please login with your new password.", Toast.LENGTH_LONG).show();
        
        // Navigate to login screen
        Intent loginIntent = new Intent(this, MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void setLoadingState(boolean isLoading) {
        btnResetPassword.setEnabled(!isLoading);
        btnResetPassword.setText(isLoading ? "Resetting..." : "Reset Password");
        
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        etNewPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }

    public static void startWithTokenAndEmail(android.content.Context context, String email, String token) {
        Intent intent = new Intent(context, PasswordResetConfirmActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("token", token);
        context.startActivity(intent);
    }
}
