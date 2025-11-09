package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";
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
        
        // Check if email exists and send verification email
        checkEmailAndSendVerification(email);
    }
    
    private void checkEmailAndSendVerification(String email) {
        Log.d(TAG, "Checking if email exists: " + email);
        
        // Check if email exists in Firestore users collection
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // Email exists in our database
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                            String firebaseUid = document.getString("firebaseUid");
                            
                            Log.d(TAG, "Email found in database. Firebase UID: " + firebaseUid);
                            
                            // Check if user has a Firebase Auth account
                            checkFirebaseUserAndSendVerification(email, firebaseUid);
                        } else {
                            // Email not found in our database
                            setLoadingState(false);
                            Log.w(TAG, "Email not found in database: " + email);
                            showEmailNotFoundDialog(email);
                        }
                    } else {
                        // Error checking database
                        setLoadingState(false);
                        Log.e(TAG, "Error checking database", task.getException());
                        Toast.makeText(this, 
                            "Error checking email: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void checkFirebaseUserAndSendVerification(String email, String firebaseUid) {
        Log.d(TAG, "Checking Firebase Auth user status");
        
        // Try to sign in to check if Firebase Auth account exists
        // Note: We need the current user to send verification email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null && currentUser.getEmail() != null && currentUser.getEmail().equals(email)) {
            // User is already signed in with this email
            Log.d(TAG, "User already signed in, sending verification email");
            sendVerificationEmail(currentUser);
        } else {
            // User needs to be authenticated first to send verification
            // We'll send a password reset email instead, which serves as verification
            Log.d(TAG, "User not signed in, sending password reset as verification");
            sendPasswordResetAsVerification(email);
        }
    }
    
    private void sendVerificationEmail(FirebaseUser user) {
        Log.d(TAG, "Sending verification email to: " + user.getEmail());
        
        // Check if email is already verified
        if (user.isEmailVerified()) {
            setLoadingState(false);
            showEmailAlreadyVerifiedDialog(user.getEmail());
            return;
        }
        
        // Send verification email
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoadingState(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent successfully");
                            showVerificationSentDialog(user.getEmail());
                        } else {
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            Toast.makeText(ResetPasswordActivity.this, 
                                "Failed to send verification email: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void sendPasswordResetAsVerification(String email) {
        Log.d(TAG, "Sending password reset email as verification to: " + email);
        
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoadingState(false);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent successfully");
                            showVerificationSentDialog(email);
                        } else {
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            Toast.makeText(ResetPasswordActivity.this, 
                                "Failed to send verification email: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void showEmailNotFoundDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Not Found")
                .setMessage("The email address " + email + " is not registered in our system.\n\nWould you like to create a new account?")
                .setPositiveButton("Register", (dialog, which) -> {
                    // Navigate to registration
                    Intent intent = new Intent(ResetPasswordActivity.this, RegistrationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    private void showEmailAlreadyVerifiedDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Already Verified")
                .setMessage("The email address " + email + " is already verified.\n\nYou can sign in with your account.")
                .setPositiveButton("Sign In", (dialog, which) -> {
                    // Navigate to login
                    Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    private void showVerificationSentDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("✓ Verification Email Sent")
                .setMessage("A verification email has been sent to:\n\n" + email + 
                           "\n\nPlease check your inbox and follow the instructions to verify your email address.\n\n" +
                           "Note: Check your spam folder if you don't see the email.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNeutralButton("Resend", (dialog, which) -> {
                    // Resend verification email
                    sendResetLink();
                })
                .setCancelable(false)
                .show();
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
