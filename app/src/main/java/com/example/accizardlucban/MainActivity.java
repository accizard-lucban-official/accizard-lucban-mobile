package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;
import android.content.SharedPreferences;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private TextView forgotPasswordText, signUpText, emergencyText;
    private LinearLayout callLucbanLayout; // Changed from TextView to LinearLayout
    private ImageView ivTogglePassword;
    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // FirebaseAuth instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Remove or comment out the anonymous sign-in example
        // mAuth.signInAnonymously() ...

        try {
            setContentView(R.layout.activity_main);
            initializeViews();
            loadSavedCredentials();
            setupClickListeners(mAuth);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading main activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            emailEditText = findViewById(R.id.email_edit_text);
            passwordEditText = findViewById(R.id.password_edit_text);
            signInButton = findViewById(R.id.sign_in_button);
            forgotPasswordText = findViewById(R.id.forgot_password_text);
            signUpText = findViewById(R.id.sign_up_text);
            emergencyText = findViewById(R.id.emergency_text);
            callLucbanLayout = findViewById(R.id.call_lucban_text); // Changed to LinearLayout
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
    }

    // Update setupClickListeners to accept FirebaseAuth
    private void setupClickListeners(FirebaseAuth mAuth) {
        if (signInButton != null) {
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
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

                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                    // Fetch user profile from Firestore and save to SharedPreferences
                                    fetchAndSaveUserProfile(email);
                                } else {
                                    Toast.makeText(MainActivity.this, "Firebase Auth failed: " + (task.getException() != null ? task.getException().getMessage() : "Username or Password did not match"), Toast.LENGTH_SHORT).show();
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

        // Changed to use LinearLayout instead of TextView
        if (callLucbanLayout != null) {
            callLucbanLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Calling Lucban LDRRMO...", Toast.LENGTH_SHORT).show();
                    // You can add actual calling functionality here
                    // For example:
                    // Intent callIntent = new Intent(Intent.ACTION_CALL);
                    // callIntent.setData(Uri.parse("tel:+1234567890"));
                    // startActivity(callIntent);
                }
            });
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

            // Check if account needs verification
            if (email.equals("unverified@example.com")) {
                Intent intent = new Intent(MainActivity.this, AccountVerificationActivity.class);
                startActivity(intent);
            } else {
                // Successful login - navigate to MainDashboard
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MainDashboard.class);
                startActivity(intent);
                // Optional: finish current activity so user can't go back to login with back button
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during sign in: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchAndSaveUserProfile(String email) {
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
                // Navigate to MainDashboard after saving user data
                Intent intent = new Intent(MainActivity.this, MainDashboard.class);
                startActivity(intent);
                finish();
            });
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
}