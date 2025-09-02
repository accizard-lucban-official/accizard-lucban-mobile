package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendLinkButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean useCustomEmail = true; // Set to true to use custom email service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        sendLinkButton = findViewById(R.id.sign_in_button);
        // Optional progress bar: only use if present in layout
        // Avoid referencing a missing ID to keep compilation safe
        progressBar = null;
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        // Add test email button for debugging
        Button btnTestEmail = new Button(this);
        btnTestEmail.setText("🔧 Test Email System");
        btnTestEmail.setOnClickListener(v -> {
            Intent testIntent = new Intent(ResetPasswordActivity.this, TestEmailActivity.class);
            startActivity(testIntent);
        });
    }

    private void setupClickListeners() {
        sendLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetLink();
            }
        });
    }

    private void sendResetLink() {
        String email = emailEditText != null && emailEditText.getText() != null
                ? emailEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        setLoadingState(true);
        
        // Send Firebase password reset email directly
        sendFirebaseResetEmail(email);
    }
    
    private void checkEmailExists(String email) {
        // Check if email exists in Firestore users collection
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // Email exists in our database, proceed with reset
                            if (useCustomEmail) {
                                sendCustomResetEmail(email);
                            } else {
                                sendFirebaseResetEmail(email);
                            }
                        } else {
                            // Email not found in our database
                            setLoadingState(false);
                            Toast.makeText(this, "Email not found. Please check your email address or register for an account.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Error checking database, fallback to Firebase reset
                        if (useCustomEmail) {
                            sendCustomResetEmail(email);
                        } else {
                            sendFirebaseResetEmail(email);
                        }
                    }
                });
    }
    
    private void sendCustomResetEmail(String email) {
        EmailService.sendPasswordResetEmail(email, new EmailService.EmailCallback() {
            @Override
            public void onSuccess() {
                setLoadingState(false);
                Toast.makeText(ResetPasswordActivity.this, 
                    "Password reset instructions sent to " + email + "\n\nPlease check your email and follow the instructions.", 
                    Toast.LENGTH_LONG).show();
                
                // Navigate to PasswordRecoveryActivity with success message
                PasswordRecoveryActivity.startWithEmail(ResetPasswordActivity.this, email);
                finish();
            }
            
            @Override
            public void onError(String error) {
                setLoadingState(false);
                
                // Fallback to Firebase if custom email fails
                Toast.makeText(ResetPasswordActivity.this, 
                    "Sending via alternative method...", Toast.LENGTH_SHORT).show();
                sendFirebaseResetEmail(email);
            }
        });
    }
    
    private void sendFirebaseResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoadingState(false);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, 
                                "Password reset link sent to " + email + "\n\nPlease check your email.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Navigate to PasswordRecoveryActivity with success message
                            PasswordRecoveryActivity.startWithEmail(ResetPasswordActivity.this, email);
                            finish();
                        } else {
                            String message = task.getException() != null ? 
                                task.getException().getMessage() : "Failed to send reset email";
                            Toast.makeText(ResetPasswordActivity.this, 
                                "Error: " + message, Toast.LENGTH_LONG).show();
                            
                            // Navigate to PasswordRecoveryActivity with error message
                            PasswordRecoveryActivity.startWithErrorMessage(ResetPasswordActivity.this, message);
                        }
                    }
                });
    }
    
    private void setLoadingState(boolean isLoading) {
        sendLinkButton.setEnabled(!isLoading);
        sendLinkButton.setText(isLoading ? "Sending..." : "Send Link");
        
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        emailEditText.setEnabled(!isLoading);
    }
}
