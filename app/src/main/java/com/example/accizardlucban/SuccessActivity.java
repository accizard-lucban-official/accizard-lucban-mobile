package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SuccessActivity extends AppCompatActivity {

    private static final String TAG = "SuccessActivity";
    private LinearLayout btnGoBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();
        
        // Email verification is already sent from ValidIdActivity
        // Just log the user email for confirmation
        logEmailVerificationStatus();
    }

    private void initializeViews() {
        btnGoBackToLogin = findViewById(R.id.btnGoBackToLogin);
    }

    private void setupClickListeners() {
        btnGoBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user before going to login
                if (mAuth != null) {
                    mAuth.signOut();
                }
                
                // Navigate back to login screen
                Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Logs the email verification status
     * Email verification is sent from ValidIdActivity during registration
     */
    private void logEmailVerificationStatus() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "User: " + user.getEmail());
                Log.d(TAG, "Email verified: " + user.isEmailVerified());
                
                if (!user.isEmailVerified()) {
                    Log.d(TAG, "✅ Verification email has been sent to: " + user.getEmail());
                } else {
                    Log.d(TAG, "Email already verified");
                }
            } else {
                Log.e(TAG, "No user found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking email verification status: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button to prevent going back to registration flow
        // Show message to user
        Toast.makeText(this, "Please click 'Go to Login' button", Toast.LENGTH_SHORT).show();
    }
}