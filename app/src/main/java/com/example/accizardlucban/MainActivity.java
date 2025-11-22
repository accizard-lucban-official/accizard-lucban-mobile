package com.example.accizardlucban;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.DecelerateInterpolator;
import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int CALL_PERMISSION_REQUEST_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;
    
    // Swipe to call variables
    private float initialX = 0f;
    private float initialTouchX = 0f;
    private boolean isSwiping = false;
    private static final float SWIPE_THRESHOLD = 0.7f; // 70% of the width

    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private TextView forgotPasswordText, signUpText, emergencyText;
    private FrameLayout callLucbanLayout; // Changed from LinearLayout to FrameLayout
    private ImageView phoneIconMain;
    private ImageView ivTogglePassword;
    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_LOGGED_OUT = "user_logged_out";
    
    // Rate limiting constants for email verification
    private static final String PREFS_EMAIL_VERIFICATION = "email_verification_rate_limit";
    private static final String KEY_LAST_VERIFICATION_TIME = "last_verification_time";
    private static final String KEY_VERIFICATION_ATTEMPTS = "verification_attempts";
    private static final String KEY_BLOCKED_UNTIL = "blocked_until";
    private static final long MIN_TIME_BETWEEN_EMAILS = 60000; // 1 minute between emails (in milliseconds)
    private static final int MAX_ATTEMPTS_PER_HOUR = 3; // Maximum 3 attempts per hour
    private static final long HOUR_IN_MILLIS = 3600000; // 1 hour in milliseconds
    private static final long BLOCK_DURATION = 3600000; // Block for 1 hour if rate limit exceeded
    
    // Retry logic constants
    private static final int MAX_RETRY_ATTEMPTS = 3; // Maximum retry attempts
    private static final long INITIAL_RETRY_DELAY = 2000; // Initial delay: 2 seconds
    private static final long MAX_RETRY_DELAY = 30000; // Maximum delay: 30 seconds
    private static final double BACKOFF_MULTIPLIER = 2.0; // Exponential backoff multiplier
    
    private FirebaseAuth mAuth;
    private AuthStateListener authStateListener;
    private boolean hasCheckedAuth = false;
    private Handler authCheckHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ FORCE LIGHT MODE - Disable dark mode globally for the entire app
        // This ensures the app maintains its original color scheme regardless of system dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        
        // ✅ CRITICAL FIX: Reset ChatActivityTracker to ensure clean state on app start
        ChatActivityTracker.reset();
        Log.d(TAG, "✅ ChatActivityTracker reset in MainActivity onCreate");

        // FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user explicitly logged out
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean userLoggedOut = prefs.getBoolean(KEY_USER_LOGGED_OUT, false);
        
        if (userLoggedOut) {
            // User explicitly logged out, show login screen
            Log.d(TAG, "User explicitly logged out. Showing login screen.");
            prefs.edit().putBoolean(KEY_USER_LOGGED_OUT, false).apply(); // Reset flag
            setupLoginScreen();
            return;
        }

        // Check if user is already authenticated immediately
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            try {
                currentUser.reload();
            } catch (Exception ignored) {}

            if (currentUser.isEmailVerified()) {
                Log.d(TAG, "User already authenticated. Skipping MainActivity login screen.");
                String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                checkUserSuspensionStatus(email, () -> {
                    // User not suspended, proceed with login
                    initializeNotificationChannels();
                    initializeFCMToken();
                    navigateAfterLoginFast(email);
                });
                return;
            } else {
                Log.w(TAG, "Authenticated user found but email not verified. Showing login screen.");
            }
        }

        // Set up AuthStateListener to wait for Firebase Auth to restore session
        setupAuthStateListener();
        
        // Also set up login screen in case auth doesn't restore
        setupLoginScreen();
        
        // Wait a bit for Firebase Auth to restore session, then check for saved credentials
        authCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!hasCheckedAuth) {
                    checkAndAutoLogin();
                }
            }
        }, 500); // Wait 500ms for Firebase Auth to restore
    }
    
    /**
     * Sets up AuthStateListener to detect when Firebase Auth restores the session
     */
    private void setupAuthStateListener() {
        authStateListener = new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (!hasCheckedAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        hasCheckedAuth = true;
                        Log.d(TAG, "Firebase Auth session restored. User authenticated.");
                        // Remove listener to prevent multiple calls
                        mAuth.removeAuthStateListener(authStateListener);
                        String email = user.getEmail() != null ? user.getEmail() : "";
                        checkUserSuspensionStatus(email, () -> {
                            // User not suspended, proceed with login
                            initializeNotificationChannels();
                            initializeFCMToken();
                            navigateAfterLoginFast(email);
                        });
                    }
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }
    
    /**
     * Sets up the login screen UI
     */
    private void setupLoginScreen() {
        try {
            setContentView(R.layout.activity_main);
            
            // Clear all registration data when returning to MainActivity
            clearAllRegistrationData();
            
            initializeViews();
            loadSavedCredentials();
            setupClickListeners(mAuth);
            
            // Initialize report counter for unique report IDs
            initializeReportCounter();
            
            // Initialize push notification channels
            initializeNotificationChannels();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading main activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Checks for saved credentials and attempts auto-login if Firebase Auth session wasn't restored
     */
    private void checkAndAutoLogin() {
        if (hasCheckedAuth) {
            return; // Already handled
        }
        
        hasCheckedAuth = true;
        
        // Remove auth state listener since we're handling it manually now
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        
        // Check if Firebase Auth has a user now
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "Firebase Auth session found during delayed check.");
            String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
            checkUserSuspensionStatus(email, () -> {
                // User not suspended, proceed with login
                initializeNotificationChannels();
                initializeFCMToken();
                navigateAfterLoginFast(email);
            });
            return;
        }
        
        // If no Firebase Auth session, try auto-login with saved credentials
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);
        
        if (savedEmail != null && savedPassword != null && !savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            Log.d(TAG, "Attempting auto-login with saved credentials.");
            autoLogin(savedEmail, savedPassword);
        } else {
            Log.d(TAG, "No saved credentials found. User must login manually.");
        }
    }
    
    /**
     * Attempts to auto-login using saved credentials
     */
    private void autoLogin(String email, String password) {
        // Normalize email to lowercase for consistency
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty()) {
            Log.w(TAG, "Auto-login skipped: invalid credentials");
            return;
        }
        
        Log.d(TAG, "Attempting auto-login for email: " + normalizedEmail);
        mAuth.signInWithEmailAndPassword(normalizedEmail, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Log.d(TAG, "✅ Auto-login successful - email verified");
                            checkUserSuspensionStatus(normalizedEmail, () -> {
                                // User not suspended, proceed with login
                                initializeNotificationChannels();
                                initializeFCMToken();
                                navigateAfterLoginFast(normalizedEmail);
                            });
                        } else {
                            Log.w(TAG, "Auto-login failed - email not verified");
                            // Show login screen - user needs to verify email
                        }
                    } else {
                        Log.w(TAG, "Auto-login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        // Clear invalid credentials
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().remove(KEY_EMAIL).remove(KEY_PASSWORD).apply();
                    }
                }
            });
    }

    private void initializeViews() {
        try {
            emailEditText = findViewById(R.id.email_edit_text);
            passwordEditText = findViewById(R.id.password_edit_text);
            signInButton = findViewById(R.id.sign_in_button);
            forgotPasswordText = findViewById(R.id.forgot_password_text);
            signUpText = findViewById(R.id.sign_up_text);
            emergencyText = findViewById(R.id.emergency_text);
            callLucbanLayout = findViewById(R.id.call_lucban_text); // Changed to FrameLayout
            phoneIconMain = findViewById(R.id.phoneIconMain);
            ivTogglePassword = findViewById(R.id.ivTogglePassword);
            
            // Setup password visibility toggle
            setupPasswordToggle();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        // Don't auto-fill password for security reasons
        if (emailEditText != null && !savedEmail.isEmpty()) {
            emailEditText.setText(savedEmail);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove auth state listener when activity is destroyed
        if (authStateListener != null && mAuth != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
        // Cancel any pending handlers
        if (authCheckHandler != null) {
            authCheckHandler.removeCallbacksAndMessages(null);
        }
    }
    
    private void initializeReportCounter() {
        FirestoreHelper.initializeReportCounter(
            new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    android.util.Log.d("MainActivity", "Report counter initialized successfully");
                }
            },
            new com.google.android.gms.tasks.OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    android.util.Log.w("MainActivity", "Failed to initialize report counter", e);
                    // Don't show error to user as this is background initialization
                }
            }
        );
    }

    // Update setupClickListeners to accept FirebaseAuth
    private void setupClickListeners(FirebaseAuth mAuth) {
        if (signInButton != null) {
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim().toLowerCase() : "";
                    String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

                    if (email.isEmpty()) {
                        emailEditText.setError("Email is required");
                        Toast.makeText(MainActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.isEmpty()) {
                        passwordEditText.setError("Password is required");
                        Toast.makeText(MainActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Log attempt for debugging (without sensitive data)
                    Log.d(TAG, "Attempting login for email: " + email);

                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Check if email is verified
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    if (auth.getCurrentUser() != null) {
                                        if (auth.getCurrentUser().isEmailVerified()) {
                                            // Email is verified, check suspension status before proceeding
                                            Log.d(TAG, "✅ Login successful - email verified");
                                            checkUserSuspensionStatus(email, () -> {
                                                // User not suspended, proceed with login
                                                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                                saveCredentials(email, password);
                                                
                                                // Initialize FCM token for push notifications
                                                initializeFCMToken();
                                                
                                                // Navigate immediately to avoid white screen
                                                // Data will be loaded in the background in the target activity
                                                navigateAfterLoginFast(email);
                                            });
                                        } else {
                                            // Email not verified, show verification dialog
                                            Log.w(TAG, "Email not verified");
                                            showEmailVerificationDialog(email, password);
                                        }
                                    }
                                } else {
                                    // Sign in failed - provide specific error messages
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    String errorMessage = "Authentication failed. Please check your credentials.";
                                    
                                    if (task.getException() != null) {
                                        Exception exception = task.getException();
                                        
                                        // Check if it's a FirebaseAuthException to get error code
                                        if (exception instanceof FirebaseAuthException) {
                                            FirebaseAuthException authException = (FirebaseAuthException) exception;
                                            String errorCode = authException.getErrorCode();
                                            Log.d(TAG, "Firebase Auth error code: " + errorCode);
                                            
                                            switch (errorCode) {
                                                case "ERROR_USER_NOT_FOUND":
                                                    errorMessage = "No account found with this email address. Please sign up first.";
                                                    break;
                                                case "ERROR_WRONG_PASSWORD":
                                                    errorMessage = "Incorrect password. Please try again or use 'Forgot Password' to reset it.";
                                                    break;
                                                case "ERROR_INVALID_EMAIL":
                                                    errorMessage = "Invalid email address. Please check your email format.";
                                                    break;
                                                case "ERROR_USER_DISABLED":
                                                    errorMessage = "This account has been disabled. Please contact support.";
                                                    break;
                                                case "ERROR_TOO_MANY_REQUESTS":
                                                    errorMessage = "Too many failed login attempts. Please try again later.";
                                                    break;
                                                case "ERROR_INVALID_CREDENTIAL":
                                                    errorMessage = "Invalid email or password. Please check your credentials and try again.";
                                                    break;
                                                case "ERROR_OPERATION_NOT_ALLOWED":
                                                    errorMessage = "Email/password sign-in is not enabled. Please contact support.";
                                                    break;
                                                case "ERROR_NETWORK_REQUEST_FAILED":
                                                    errorMessage = "Network error. Please check your internet connection and try again.";
                                                    break;
                                                default:
                                                    // Check error message for additional context
                                                    String exceptionMessage = exception.getMessage();
                                                    if (exceptionMessage != null) {
                                                        if (exceptionMessage.contains("user-not-found")) {
                                                            errorMessage = "No account found with this email address. Please sign up first.";
                                                        } else if (exceptionMessage.contains("wrong-password")) {
                                                            errorMessage = "Incorrect password. Please try again or use 'Forgot Password' to reset it.";
                                                        } else if (exceptionMessage.contains("invalid-email")) {
                                                            errorMessage = "Invalid email address. Please check your email format.";
                                                        } else if (exceptionMessage.contains("user-disabled")) {
                                                            errorMessage = "This account has been disabled. Please contact support.";
                                                        } else if (exceptionMessage.contains("too-many-requests")) {
                                                            errorMessage = "Too many failed login attempts. Please try again later.";
                                                        } else if (exceptionMessage.contains("invalid-credential") || exceptionMessage.contains("malformed") || exceptionMessage.contains("expired")) {
                                                            errorMessage = "Invalid email or password. Please check your credentials and try again.";
                                                        } else if (exceptionMessage.contains("network")) {
                                                            errorMessage = "Network error. Please check your internet connection and try again.";
                                                        } else {
                                                            errorMessage = "Login failed: " + exceptionMessage;
                                                        }
                                                    }
                                                    break;
                                            }
                                        } else {
                                            // Not a FirebaseAuthException, check message
                                            String exceptionMessage = exception.getMessage();
                                            if (exceptionMessage != null) {
                                                Log.d(TAG, "Non-FirebaseAuthException error: " + exceptionMessage);
                                                if (exceptionMessage.contains("user-not-found")) {
                                                    errorMessage = "No account found with this email address. Please sign up first.";
                                                } else if (exceptionMessage.contains("wrong-password")) {
                                                    errorMessage = "Incorrect password. Please try again or use 'Forgot Password' to reset it.";
                                                } else if (exceptionMessage.contains("invalid-email")) {
                                                    errorMessage = "Invalid email address. Please check your email format.";
                                                } else if (exceptionMessage.contains("network")) {
                                                    errorMessage = "Network error. Please check your internet connection and try again.";
                                                } else {
                                                    errorMessage = "Login failed: " + exceptionMessage;
                                                }
                                            }
                                        }
                                    }
                                    
                                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                }
            });
        }

        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error navigating to reset password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (signUpText != null) {
            signUpText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error navigating to registration", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (emergencyText != null) {
            emergencyText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Emergency feature", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Phone icon swipe functionality
        if (phoneIconMain != null && callLucbanLayout != null) {
            setupSwipeToCall();
        }
    }

    private void handleSignIn() {
        try {
            String email = "";
            String password = "";

            if (emailEditText != null) {
                email = emailEditText.getText().toString().trim();
            }

            if (passwordEditText != null) {
                password = passwordEditText.getText().toString().trim();
            }

            if (email.isEmpty()) {
                if (emailEditText != null) {
                    emailEditText.setError("Email is required");
                }
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                if (passwordEditText != null) {
                    passwordEditText.setError("Password is required");
                }
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Make variables final for inner class access
            final String finalEmail = email;
            final String finalPassword = password;

            // Show loading state
            if (signInButton != null) {
                signInButton.setEnabled(false);
                signInButton.setText("Signing in...");
            }

            // Authenticate with Firebase
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(finalEmail, finalPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Reset button state
                        if (signInButton != null) {
                            signInButton.setEnabled(true);
                            signInButton.setText("Sign In");
                        }

                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            if (auth.getCurrentUser() != null) {
                                // Check if email is verified
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    // Email is verified, check suspension status before proceeding
                                    Log.d(TAG, "✅ Login successful - email verified");
                                    checkUserSuspensionStatus(finalEmail, () -> {
                                        // User not suspended, proceed with login
                                        Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        saveCredentials(finalEmail, finalPassword);
                                        
                                        // Initialize FCM token for push notifications
                                        initializeFCMToken();
                                        
                                        // Navigate immediately to avoid white screen
                                        // Data will be loaded in the background in the target activity
                                        navigateAfterLoginFast(finalEmail);
                                    });
                                } else {
                                    // Email not verified, show verification dialog
                                    Log.w(TAG, "Email not verified");
                                    showEmailVerificationDialog(finalEmail, finalPassword);
                                }
                            }
                        } else {
                            // Sign in failed
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = "Authentication failed.";
                            if (task.getException() != null) {
                                String errorCode = task.getException().getMessage();
                                if (errorCode != null) {
                                    if (errorCode.contains("user-not-found")) {
                                        errorMessage = "No account found with this email address.";
                                    } else if (errorCode.contains("wrong-password")) {
                                        errorMessage = "Incorrect password.";
                                    } else if (errorCode.contains("invalid-email")) {
                                        errorMessage = "Invalid email address.";
                                    } else if (errorCode.contains("too-many-requests")) {
                                        errorMessage = "Too many failed attempts. Please try again later.";
                                    } else {
                                        errorMessage = errorCode;
                                    }
                                }
                            }
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during sign in: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Reset button state
            if (signInButton != null) {
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");
            }
        }
    }

    private void showEmailVerificationDialog(String email, String password) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address before signing in. Check your email for a verification link or click 'Resend Email' to send a new verification email.")
            .setPositiveButton("Resend Email", (dialog, which) -> {
                resendVerificationEmail(email, password);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Sign out the user since email is not verified
                FirebaseAuth.getInstance().signOut();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Checks if we can send a verification email based on rate limiting
     * @return null if allowed, error message if blocked
     */
    private String canSendVerificationEmail() {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();
        
        // Check if currently blocked
        long blockedUntil = prefs.getLong(KEY_BLOCKED_UNTIL, 0);
        if (blockedUntil > currentTime) {
            long minutesRemaining = (blockedUntil - currentTime) / 60000;
            return "Too many verification email attempts. Please wait " + minutesRemaining + " minute(s) before trying again.";
        }
        
        // Check time since last email
        long lastVerificationTime = prefs.getLong(KEY_LAST_VERIFICATION_TIME, 0);
        if (lastVerificationTime > 0) {
            long timeSinceLastEmail = currentTime - lastVerificationTime;
            if (timeSinceLastEmail < MIN_TIME_BETWEEN_EMAILS) {
                long secondsRemaining = (MIN_TIME_BETWEEN_EMAILS - timeSinceLastEmail) / 1000;
                return "Please wait " + secondsRemaining + " second(s) before requesting another verification email.";
            }
        }
        
        // Check attempts in the last hour
        long firstAttemptTime = prefs.getLong("first_attempt_time", 0);
        int attempts = prefs.getInt(KEY_VERIFICATION_ATTEMPTS, 0);
        
        if (firstAttemptTime > 0 && (currentTime - firstAttemptTime) < HOUR_IN_MILLIS) {
            // Still within the hour window
            if (attempts >= MAX_ATTEMPTS_PER_HOUR) {
                long timeUntilReset = HOUR_IN_MILLIS - (currentTime - firstAttemptTime);
                long minutesRemaining = timeUntilReset / 60000;
                return "Maximum verification email attempts reached. Please wait " + minutesRemaining + " minute(s) before trying again.";
            }
        } else {
            // Hour window expired, reset attempts
            attempts = 0;
            firstAttemptTime = currentTime;
        }
        
        return null; // Allowed to send
    }
    
    /**
     * Records a verification email attempt
     */
    private void recordVerificationAttempt(boolean success) {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long currentTime = System.currentTimeMillis();
        
        if (success) {
            // Successful send - record time and increment attempts
            editor.putLong(KEY_LAST_VERIFICATION_TIME, currentTime);
            
            long firstAttemptTime = prefs.getLong("first_attempt_time", 0);
            int attempts = prefs.getInt(KEY_VERIFICATION_ATTEMPTS, 0);
            
            if (firstAttemptTime == 0 || (currentTime - firstAttemptTime) >= HOUR_IN_MILLIS) {
                // New hour window
                editor.putLong("first_attempt_time", currentTime);
                editor.putInt(KEY_VERIFICATION_ATTEMPTS, 1);
            } else {
                // Same hour window
                editor.putInt(KEY_VERIFICATION_ATTEMPTS, attempts + 1);
            }
        }
        
        editor.apply();
    }
    
    /**
     * Handles rate limit error from Firebase and blocks future attempts
     */
    private void handleRateLimitError() {
        SharedPreferences prefs = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long currentTime = System.currentTimeMillis();
        
        // Block for the specified duration
        editor.putLong(KEY_BLOCKED_UNTIL, currentTime + BLOCK_DURATION);
        editor.putLong("first_attempt_time", currentTime);
        editor.putInt(KEY_VERIFICATION_ATTEMPTS, MAX_ATTEMPTS_PER_HOUR);
        editor.apply();
        
        Log.w(TAG, "Rate limit detected. Blocked until: " + new java.util.Date(currentTime + BLOCK_DURATION));
    }
    
    /**
     * Gets user-friendly error message for Firebase errors
     */
    private String getUserFriendlyErrorMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return "Failed to send verification email. Please try again later.";
        }
        
        String errorMessage = exception.getMessage();
        
        // Check for rate limiting errors
        if (errorMessage.contains("blocked") || 
            errorMessage.contains("unusual activity") || 
            errorMessage.contains("too many requests") ||
            errorMessage.contains("quota exceeded")) {
            
            handleRateLimitError();
            long blockedUntil = getSharedPreferences(PREFS_EMAIL_VERIFICATION, MODE_PRIVATE)
                .getLong(KEY_BLOCKED_UNTIL, 0);
            
            if (blockedUntil > System.currentTimeMillis()) {
                long minutesRemaining = (blockedUntil - System.currentTimeMillis()) / 60000;
                return "Too many verification email requests. Please wait " + minutesRemaining + 
                       " minute(s) before trying again. This helps prevent spam.";
            }
            
            return "Too many verification email requests. Please wait 1 hour before trying again. This helps prevent spam.";
        }
        
        // Check for network errors
        if (errorMessage.contains("network") || errorMessage.contains("timeout")) {
            return "Network error. Please check your internet connection and try again.";
        }
        
        // Check for invalid email
        if (errorMessage.contains("invalid-email")) {
            return "Invalid email address. Please check your email and try again.";
        }
        
        // Generic error
        return "Failed to send verification email. Please try again later.";
    }
    
    /**
     * Checks if an error is retryable (transient errors that might succeed on retry)
     */
    private boolean isRetryableError(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return false;
        }
        
        String errorMessage = exception.getMessage().toLowerCase();
        
        // Retry for transient errors
        if (errorMessage.contains("network") || 
            errorMessage.contains("timeout") ||
            errorMessage.contains("connection") ||
            errorMessage.contains("unavailable") ||
            errorMessage.contains("internal error") ||
            errorMessage.contains("service unavailable")) {
            return true;
        }
        
        // Don't retry for permanent errors
        if (errorMessage.contains("invalid-email") ||
            errorMessage.contains("user-disabled") ||
            errorMessage.contains("user-not-found") ||
            errorMessage.contains("email-already-in-use") ||
            errorMessage.contains("operation-not-allowed")) {
            return false;
        }
        
        // Don't retry for rate limit errors (handled separately)
        if (errorMessage.contains("blocked") || 
            errorMessage.contains("unusual activity") || 
            errorMessage.contains("too many requests")) {
            return false;
        }
        
        // Default: don't retry for unknown errors
        return false;
    }
    
    /**
     * Resends verification email with retry logic for transient errors
     */
    private void resendVerificationEmailWithRetry(String email, String password, int retryCount) {
        // Check rate limits before sending
        String rateLimitError = canSendVerificationEmail();
        if (rateLimitError != null) {
            Toast.makeText(MainActivity.this, rateLimitError, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Rate limit check failed: " + rateLimitError);
            FirebaseAuth.getInstance().signOut();
            return;
        }
        
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            if (retryCount > 0) {
                Log.d(TAG, "Retry attempt " + retryCount + " of " + MAX_RETRY_ATTEMPTS + " for resending verification email");
            }
            
            mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            recordVerificationAttempt(true);
                            Toast.makeText(MainActivity.this, 
                                "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                            // Sign out the user since email is not verified
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "Failed to send verification email (attempt " + (retryCount + 1) + ")", exception);
                            
                            // Check if it's a rate limit error
                            if (exception != null && exception.getMessage() != null) {
                                String errorMsg = exception.getMessage();
                                if (errorMsg.contains("blocked") || 
                                    errorMsg.contains("unusual activity") || 
                                    errorMsg.contains("too many requests")) {
                                    recordVerificationAttempt(false);
                                    handleRateLimitError();
                                    String userFriendlyMessage = getUserFriendlyErrorMessage(exception);
                                    Toast.makeText(MainActivity.this, userFriendlyMessage, Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                    return;
                                }
                            }
                            
                            // Check if error is retryable and we haven't exceeded max retries
                            if (isRetryableError(exception) && retryCount < MAX_RETRY_ATTEMPTS) {
                                // Calculate exponential backoff delay
                                long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(BACKOFF_MULTIPLIER, retryCount));
                                delay = Math.min(delay, MAX_RETRY_DELAY); // Cap at max delay
                                
                                Log.d(TAG, "Retryable error detected. Retrying in " + (delay / 1000) + " seconds...");
                                
                                // Schedule retry with exponential backoff
                                authCheckHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        resendVerificationEmailWithRetry(email, password, retryCount + 1);
                                    }
                                }, delay);
                            } else {
                                // Not retryable or max retries exceeded
                                String userFriendlyMessage = getUserFriendlyErrorMessage(exception);
                                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                                    userFriendlyMessage = "Failed to send verification email after " + (MAX_RETRY_ATTEMPTS + 1) + " attempts. " + userFriendlyMessage;
                                    Log.w(TAG, "Max retry attempts reached");
                                }
                                
                                Toast.makeText(MainActivity.this, userFriendlyMessage, Toast.LENGTH_LONG).show();
                                // Sign out the user since email is not verified
                                FirebaseAuth.getInstance().signOut();
                            }
                        }
                    }
                });
        }
    }
    
    private void resendVerificationEmail(String email, String password) {
        resendVerificationEmailWithRetry(email, password, 0);
    }

    /**
     * Fetches user profile and determines whether to show onboarding or go directly to dashboard
     * DEPRECATED: Use navigateAfterLoginFast() instead to avoid white screen delay
     */
    private void fetchAndSaveUserProfileWithOnboarding(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("first_name", doc.getString("firstName"));
                        editor.putString("last_name", doc.getString("lastName"));
                        editor.putString("mobile_number", doc.getString("phoneNumber"));
                        editor.putString("email", doc.getString("email"));
                        editor.putString("province", doc.getString("province"));
                        editor.putString("city", doc.getString("cityTown"));
                        editor.putString("barangay", doc.getString("barangay"));
                        editor.apply();
                        break;
                    }
                }
                
                // Check if this is the first login
                navigateAfterLogin();
            });
    }
    
    /**
     * Fast navigation after login - navigates immediately without waiting for Firestore
     * This prevents white screen delay and provides instant feedback to user
     * User data will be loaded in background by the target activity
     */
    private void navigateAfterLoginFast(String email) {
        try {
            Log.d(TAG, "Fast navigation initiated for email: " + email);
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false);
            
            // Start data fetch in background (non-blocking)
            fetchUserDataInBackground(email);
            
            if (!hasSeenOnboarding) {
                // First time login - show onboarding
                Log.d(TAG, "First time login detected - showing onboarding immediately");
                Intent intent = new Intent(MainActivity.this, OnBoardingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Add smooth transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } else {
                // Returning user - go directly to dashboard
                Log.d(TAG, "Returning user - going to dashboard immediately");
                Intent intent = new Intent(MainActivity.this, MainDashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Add smooth transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in fast navigation", e);
            // Fallback to dashboard
            Intent intent = new Intent(MainActivity.this, MainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
    
    /**
     * Fetches user data in background without blocking navigation
     * Data is saved to SharedPreferences for use by other activities
     */
    private void fetchUserDataInBackground(String email) {
        try {
            Log.d(TAG, "Starting background data fetch for: " + email);
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            
                            // Save all user data
                            String firstName = doc.getString("firstName");
                            String lastName = doc.getString("lastName");
                            String phoneNumber = doc.getString("phoneNumber");
                            String emailAddr = doc.getString("email");
                            String province = doc.getString("province");
                            String cityTown = doc.getString("cityTown");
                            String barangay = doc.getString("barangay");
                            
                            if (firstName != null) editor.putString("first_name", firstName);
                            if (lastName != null) editor.putString("last_name", lastName);
                            if (phoneNumber != null) editor.putString("mobile_number", phoneNumber);
                            if (emailAddr != null) editor.putString("email", emailAddr);
                            if (province != null) editor.putString("province", province);
                            if (cityTown != null) {
                                editor.putString("city", cityTown);
                                editor.putString("cityTown", cityTown);
                            }
                            if (barangay != null) editor.putString("barangay", barangay);
                            
                            // Construct and save location display
                            if (cityTown != null && barangay != null) {
                                String fullLocation = cityTown + ", " + barangay;
                                editor.putString("location_text", fullLocation);
                                Log.d(TAG, "Saved location: " + fullLocation);
                            }
                            
                            editor.apply();
                            Log.d(TAG, "✅ User data saved in background successfully");
                            break;
                        }
                    } else {
                        Log.w(TAG, "No user document found for email: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data in background: " + e.getMessage(), e);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchUserDataInBackground: " + e.getMessage(), e);
        }
    }

    /**
     * Determines navigation after login - OnBoarding for first-time users, MainDashboard for returning users
     */
    private void navigateAfterLogin() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false);
            
            if (!hasSeenOnboarding) {
                // First time login - show onboarding
                Log.d(TAG, "First time login detected - showing onboarding");
                Intent intent = new Intent(MainActivity.this, OnBoardingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Returning user - go directly to dashboard
                Log.d(TAG, "Returning user - going to dashboard");
                Intent intent = new Intent(MainActivity.this, MainDashboard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating after login", e);
            // Fallback to dashboard
            Intent intent = new Intent(MainActivity.this, MainDashboard.class);
            startActivity(intent);
            finish();
        }
    }

    private void saveCredentials(String email, String password) {
        try {
            // Normalize email to lowercase for consistency
            String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
            if (normalizedEmail.isEmpty()) {
                Log.w(TAG, "Cannot save credentials: email is empty");
                return;
            }
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_EMAIL, normalizedEmail);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_USER_LOGGED_OUT, false); // Ensure logout flag is false
            editor.apply();
            Log.d(TAG, "✅ Credentials saved for persistent login");
        } catch (Exception e) {
            Log.e(TAG, "Error saving credentials: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clears saved credentials - called when user explicitly logs out
     */
    public static void clearSavedCredentials(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
            editor.putBoolean(KEY_USER_LOGGED_OUT, true); // Mark as logged out
            editor.apply();
            Log.d(TAG, "✅ Saved credentials cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing credentials: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if user account is suspended in Firestore
     * @param email User's email address
     * @param onNotSuspended Callback to execute if user is not suspended
     */
    private void checkUserSuspensionStatus(String email, Runnable onNotSuspended) {
        try {
            if (email == null || email.isEmpty()) {
                Log.w(TAG, "Email is empty, cannot check suspension status");
                if (onNotSuspended != null) {
                    onNotSuspended.run();
                }
                return;
            }
            
            Log.d(TAG, "Checking suspension status for email: " + email);
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Check if user is suspended
                            Boolean isSuspended = doc.getBoolean("suspended");
                            
                            if (isSuspended != null && isSuspended) {
                                // User is suspended
                                String suspensionReason = doc.getString("suspensionReason");
                                if (suspensionReason == null || suspensionReason.isEmpty()) {
                                    suspensionReason = "Your account has been suspended by the administrators.";
                                }
                                
                                Log.w(TAG, "User account is suspended. Reason: " + suspensionReason);
                                
                                // Sign out the user
                                mAuth.signOut();
                                
                                // Show suspension dialog
                                showSuspensionDialog(suspensionReason);
                                return;
                            } else {
                                // User is not suspended, proceed with login
                                Log.d(TAG, "User account is not suspended, proceeding with login");
                                if (onNotSuspended != null) {
                                    onNotSuspended.run();
                                }
                                return;
                            }
                        }
                    } else {
                        // User document not found or error - allow login (might be new user or Firestore issue)
                        Log.w(TAG, "Could not find user document or error checking suspension: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        if (onNotSuspended != null) {
                            onNotSuspended.run();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Error checking suspension - allow login (don't block user due to Firestore issues)
                    Log.e(TAG, "Error checking suspension status: " + e.getMessage(), e);
                    if (onNotSuspended != null) {
                        onNotSuspended.run();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception checking suspension status: " + e.getMessage(), e);
            // On error, allow login (don't block user due to exceptions)
            if (onNotSuspended != null) {
                onNotSuspended.run();
            }
        }
    }
    
    /**
     * Shows a modal dialog informing user their account is suspended
     * @param suspensionReason Reason for suspension
     */
    private void showSuspensionDialog(String suspensionReason) {
        try {
            String message = suspensionReason + "\n\n" +
                    "To regain access to your account, please contact the administrators:\n\n" +
                    "Email: lucbanmdrrm@gmail.com\n" +
                    "Phone: 0917 520 4211";
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Account Suspended")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        // User stays on login screen
                    })
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing suspension dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Your account has been suspended. Please contact administrators.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupPasswordToggle() {
        if (ivTogglePassword != null) {
            ivTogglePassword.setOnClickListener(new View.OnClickListener() {
                private boolean isPasswordVisible = false;
                @Override
                public void onClick(View v) {
                    if (isPasswordVisible) {
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        ivTogglePassword.setAlpha(0.5f);
                        ivTogglePassword.setImageResource(R.drawable.ic_eye_off); // Closed eye
                    } else {
                        passwordEditText.setTransformationMethod(null);
                        ivTogglePassword.setAlpha(1.0f);
                        ivTogglePassword.setImageResource(R.drawable.baseline_remove_red_eye_24); // Open eye
                    }
                    isPasswordVisible = !isPasswordVisible;
                    passwordEditText.setSelection(passwordEditText.getText().length());
                }
            });
        }
    }
    
    private void setupSwipeToCall() {
        try {
            if (phoneIconMain == null || callLucbanLayout == null) return;
            
            // Store the initial position
            phoneIconMain.post(() -> {
                initialX = phoneIconMain.getX();
            });
            
            phoneIconMain.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position
                        initialTouchX = event.getRawX();
                        isSwiping = false;
                        
                        // Visual feedback: scale down slightly
                        v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the distance moved
                        float deltaX = event.getRawX() - initialTouchX;
                        
                        // Only allow swiping to the right
                        if (deltaX > 0) {
                            isSwiping = true;
                            
                            // Calculate max swipe distance based on callLucbanLayout (parent) width
                            float maxSwipeDistance = callLucbanLayout.getWidth() - v.getWidth() - 40; // 40 for padding
                            
                            // Limit the movement to not go beyond the parent
                            float newX = Math.min(deltaX, maxSwipeDistance);
                            v.setTranslationX(newX);
                            
                            // Calculate swipe progress
                            float progress = newX / maxSwipeDistance;
                            
                            // Change icon alpha based on swipe progress
                            v.setAlpha(0.6f + (0.4f * progress));
                            
                            // Scale up as user swipes for emphasis
                            float scale = 1.0f + (0.2f * progress); // Scale from 1.0 to 1.2
                            v.setScaleX(scale);
                            v.setScaleY(scale);
                            
                            // Enhanced dim effect on background button (0.5 = 50% dimming)
                            callLucbanLayout.setAlpha(1.0f - (0.5f * progress));
                            
                            // Add visual feedback as swipe progresses for "go" feedback
                            if (progress >= SWIPE_THRESHOLD) {
                                // Near completion - brighten the icon
                                v.setAlpha(1.0f);
                                // Dim the background more
                                callLucbanLayout.setAlpha(0.4f);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isSwiping) {
                            // Calculate swipe progress
                            float maxSwipeDistance = callLucbanLayout.getWidth() - v.getWidth() - 40;
                            float progress = v.getTranslationX() / maxSwipeDistance;
                            
                            if (progress >= SWIPE_THRESHOLD) {
                                // Swipe completed - make the call
                                animatePhoneIconComplete(v);
                            } else {
                                // Swipe not completed - reset position
                                animatePhoneIconReset(v);
                            }
                        } else {
                            // Just a tap - show swipe instruction
                            Toast.makeText(MainActivity.this, 
                                "Swipe right to call LDRRMO", 
                                Toast.LENGTH_SHORT).show();
                            animatePhoneIconReset(v);
                        }
                        
                        isSwiping = false;
                        return true;

                    default:
                        return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up swipe to call: " + e.getMessage(), e);
        }
    }
    
    private void animatePhoneIconComplete(View v) {
        try {
            // Animate to completion - slide all the way to the right
            float maxDistance = callLucbanLayout.getWidth() - v.getWidth();
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(v, "translationX", maxDistance);
            slideOut.setDuration(200);
            slideOut.setInterpolator(new DecelerateInterpolator());
            
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 0f);
            fadeOut.setDuration(200);
            
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f);
            scaleUpX.setDuration(200);
            
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f);
            scaleUpY.setDuration(200);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideOut, fadeOut, scaleUpX, scaleUpY);
            animatorSet.start();
            
            // Make the call after animation
            v.postDelayed(() -> {
                makeEmergencyCall();
                // Reset icon position after call is initiated
                v.postDelayed(() -> animatePhoneIconReset(v), 500);
            }, 250);
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating phone icon complete: " + e.getMessage(), e);
            makeEmergencyCall();
            animatePhoneIconReset(v);
        }
    }
    
    private void animatePhoneIconReset(View v) {
        try {
            // Animate back to original position
            ObjectAnimator slideBack = ObjectAnimator.ofFloat(v, "translationX", 0f);
            slideBack.setDuration(300);
            slideBack.setInterpolator(new DecelerateInterpolator());
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", 1.0f);
            fadeIn.setDuration(300);
            
            ObjectAnimator scaleResetX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f);
            scaleResetX.setDuration(300);
            
            ObjectAnimator scaleResetY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f);
            scaleResetY.setDuration(300);
            
            // Reset callLucbanLayout alpha too
            ObjectAnimator resetButtonAlpha = ObjectAnimator.ofFloat(callLucbanLayout, "alpha", 1.0f);
            resetButtonAlpha.setDuration(300);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(slideBack, fadeIn, scaleResetX, scaleResetY, resetButtonAlpha);
            animatorSet.start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error animating phone icon reset: " + e.getMessage(), e);
            v.setTranslationX(0f);
            v.setAlpha(1.0f);
            v.setScaleX(1.0f);
            v.setScaleY(1.0f);
            if (callLucbanLayout != null) {
                callLucbanLayout.setAlpha(1.0f);
            }
        }
    }
    
    private void makeEmergencyCall() {
        try {
            String emergencyNumber = "tel:09175204211"; // LDRRMO Lucban: 0917 520 4211

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, make the call
                makeCall(emergencyNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error making emergency call: " + e.getMessage(), e);
            Toast.makeText(this, "Error making emergency call", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            // If permission denied or other security issue, show dial pad
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse(phoneNumber));
            startActivity(dialIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("tel:09175204211"); // LDRRMO Lucban: 0917 520 4211
                } else {
                    // Permission denied, show dial pad instead
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:09175204211")); // LDRRMO Lucban: 0917 520 4211
                    startActivity(dialIntent);
                    Toast.makeText(this, "Permission denied. Opening dial pad instead.",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "✅ Notification permission granted");
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "❌ Notification permission denied");
                    Toast.makeText(this, "Notifications disabled. You won't receive emergency alerts.", 
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage(), e);
            Toast.makeText(this, "Error handling permission", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initialize notification channels for push notifications
     * Creates all required channels: report_updates, announcements, high_priority_announcements, chat_messages
     */
    private void initializeNotificationChannels() {
        try {
            NotificationChannelManager channelManager = new NotificationChannelManager(this);
            channelManager.createAllChannels();
            Log.d(TAG, "✅ Notification channels initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing notification channels: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize FCM token and save to Firestore
     * This enables the user to receive push notifications
     */
    private void initializeFCMToken() {
        try {
            // Request notification permission first (Android 13+)
            requestNotificationPermission();
            
            FCMTokenManager tokenManager = new FCMTokenManager(this);
            tokenManager.initializeFCMToken();
            Log.d(TAG, "✅ FCM token initialization started");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FCM token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Request notification permission for Android 13+ (API 33+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        } else {
            Log.d(TAG, "Android version < 13, notification permission not required");
        }
    }
    
    /**
     * Clears all registration data from SharedPreferences when returning to MainActivity
     * This ensures a clean slate for new registration attempts
     */
    private void clearAllRegistrationData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Clear registration activity data
            editor.remove("saved_first_name");
            editor.remove("saved_last_name");
            editor.remove("saved_mobile_number");
            editor.remove("saved_email");
            editor.remove("saved_password");
            editor.remove("saved_terms");
            
            // Clear personal info data
            editor.remove("saved_birthday");
            editor.remove("saved_gender");
            editor.remove("saved_civil_status");
            editor.remove("saved_religion");
            editor.remove("saved_blood_type");
            editor.remove("saved_pwd");
            
            // Clear address data
            editor.remove("saved_province");
            editor.remove("saved_city_town");
            editor.remove("saved_barangay");
            editor.remove("saved_street_address");
            
            // Clear profile picture data
            editor.remove("has_profile_picture");
            editor.remove("profile_picture_base64");
            
            // Clear valid ID data
            editor.remove("has_valid_id");
            editor.remove("valid_id_count");
            
            // Clear all valid ID images
            for (int i = 0; i < 10; i++) { // Clear up to 10 images
                editor.remove("valid_id_image_" + i);
            }
            
            editor.apply();
            Log.d(TAG, "✅ All registration data cleared from SharedPreferences");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing registration data", e);
        }
    }
}