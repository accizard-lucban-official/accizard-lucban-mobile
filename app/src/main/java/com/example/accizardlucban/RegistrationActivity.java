package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.cardview.widget.CardView;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etMobileNumber, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private TextView tvSignIn, tvTermsConditions, tvPrivacyPolicy;
    private ImageView ivTogglePassword;
    private FrameLayout popupContainer;
    private View popupView;
    private boolean isPopupVisible = false;

    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);

        initializeViews();
        setupClickListeners();
        setupPasswordToggle();
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvSignIn = findViewById(R.id.tvSignIn);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        
        // Find Terms and Conditions TextViews
        tvTermsConditions = findViewById(R.id.tvTermsConditions);
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);
        
        // Create popup container
        popupContainer = new FrameLayout(this);
        popupContainer.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        popupContainer.setVisibility(View.GONE);
        
        // Add popup container to root layout
        ViewGroup rootLayout = findViewById(android.R.id.content);
        rootLayout.addView(popupContainer);
    }

    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    proceedToPersonalInfo();
                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity (sign in screen)
                try {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close current activity
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RegistrationActivity.this, "Error navigating to sign in", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Set click listeners for Terms and Conditions
        if (tvTermsConditions != null) {
            tvTermsConditions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTermsPopup();
                }
            });
        }
        
        if (tvPrivacyPolicy != null) {
            tvPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTermsPopup();
                }
            });
        }
    }

    private void setupPasswordToggle() {
        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            private boolean isPasswordVisible = false;
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivTogglePassword.setAlpha(0.5f);
                    ivTogglePassword.setImageResource(R.drawable.ic_eye_off); // Closed eye
                } else {
                    etPassword.setTransformationMethod(null);
                    ivTogglePassword.setAlpha(1.0f);
                    ivTogglePassword.setImageResource(R.drawable.baseline_remove_red_eye_24); // Open eye
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length());
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etMobileNumber.getText().toString().trim())) {
            etMobileNumber.setError("Mobile number is required");
            etMobileNumber.requestFocus();
            return false;
        }

        // Mobile number validation (Philippine format)
        String mobileNumber = etMobileNumber.getText().toString().trim();
        if (!isValidPhilippineMobileNumber(mobileNumber)) {
            etMobileNumber.setError("Please enter a valid Philippine mobile number");
            etMobileNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        String password = etPassword.getText().toString().trim();
        if (!isStrongPassword(password)) {
            etPassword.setError("Password must be at least 8 characters, include uppercase, lowercase, number, and symbol");
            etPassword.requestFocus();
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to Terms and Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidPhilippineMobileNumber(String mobileNumber) {
        // Remove spaces and dashes
        mobileNumber = mobileNumber.replaceAll("[\\s-]", "");

        // Check if it starts with +63 or 63 or 09
        if (mobileNumber.startsWith("+63")) {
            mobileNumber = mobileNumber.substring(3);
        } else if (mobileNumber.startsWith("63")) {
            mobileNumber = mobileNumber.substring(2);
        } else if (mobileNumber.startsWith("09")) {
            mobileNumber = mobileNumber.substring(1);
        }

        // Should be 10 digits starting with 9
        return mobileNumber.length() == 10 && mobileNumber.startsWith("9") && mobileNumber.matches("\\d+");
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*()-_=+[{]}|;:'\",<.>/?`~".indexOf(c) >= 0) hasSymbol = true;
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    private void proceedToPersonalInfo() {
        try {
            // Save first and last name to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_FIRST_NAME, etFirstName.getText().toString().trim());
            editor.putString(KEY_LAST_NAME, etLastName.getText().toString().trim());
            editor.apply();

            Intent intent = new Intent(RegistrationActivity.this, PersonalInfoActivity.class);
            intent.putExtra("firstName", etFirstName.getText().toString().trim());
            intent.putExtra("lastName", etLastName.getText().toString().trim());
            intent.putExtra("mobileNumber", etMobileNumber.getText().toString().trim());
            intent.putExtra("email", etEmail.getText().toString().trim());
            intent.putExtra("password", etPassword.getText().toString().trim());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error proceeding to personal info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showTermsPopup() {
        if (isPopupVisible) return;
        
        try {
            // Inflate the popup layout
            LayoutInflater inflater = LayoutInflater.from(this);
            popupView = inflater.inflate(R.layout.popup_terms_conditions, popupContainer, false);
            
            // Clear any existing views and add the popup
            popupContainer.removeAllViews();
            popupContainer.addView(popupView);
            
            // Set up popup controls
            setupPopupControls();
            
            // Show popup with animation
            popupContainer.setVisibility(View.VISIBLE);
            isPopupVisible = true;
            
            // Animate popup appearance
            animatePopupIn();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing terms popup", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupPopupControls() {
        if (popupView == null) return;
        
        // Close button
        ImageView btnClose = popupView.findViewById(R.id.btnClosePopup);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> hideTermsPopup());
        }
        
        // Accept button
        Button btnAccept = popupView.findViewById(R.id.btnAcceptTerms);
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                cbTerms.setChecked(true);
                hideTermsPopup();
                Toast.makeText(RegistrationActivity.this, "Terms and Conditions accepted", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Decline button
        Button btnDecline = popupView.findViewById(R.id.btnDeclineTerms);
        if (btnDecline != null) {
            btnDecline.setOnClickListener(v -> {
                cbTerms.setChecked(false);
                hideTermsPopup();
                Toast.makeText(RegistrationActivity.this, "Terms and Conditions declined", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Background click to close
        popupContainer.setOnClickListener(v -> {
            if (v == popupContainer) {
                hideTermsPopup();
            }
        });
        
        // Prevent CardView clicks from closing popup
        CardView cardView = popupView.findViewById(R.id.cardViewPopup);
        if (cardView != null) {
            cardView.setOnClickListener(v -> {
                // Do nothing - prevent background click
            });
        }
    }
    
    private void hideTermsPopup() {
        if (!isPopupVisible) return;
        
        animatePopupOut(() -> {
            popupContainer.setVisibility(View.GONE);
            popupContainer.removeAllViews();
            isPopupVisible = false;
        });
    }
    
    private void animatePopupIn() {
        if (popupView == null) return;
        
        CardView cardView = popupView.findViewById(R.id.cardViewPopup);
        if (cardView != null) {
            // Start from scaled down
            cardView.setScaleX(0.7f);
            cardView.setScaleY(0.7f);
            cardView.setAlpha(0f);
            
            // Animate to full size
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.7f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.7f, 1.0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1.0f);
            
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            alpha.setDuration(300);
            
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            alpha.setInterpolator(new AccelerateDecelerateInterpolator());
            
            scaleX.start();
            scaleY.start();
            alpha.start();
        }
        
        // Animate background fade in
        popupContainer.setAlpha(0f);
        ObjectAnimator backgroundAlpha = ObjectAnimator.ofFloat(popupContainer, "alpha", 0f, 1.0f);
        backgroundAlpha.setDuration(200);
        backgroundAlpha.start();
    }
    
    private void animatePopupOut(Runnable onComplete) {
        if (popupView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        CardView cardView = popupView.findViewById(R.id.cardViewPopup);
        if (cardView != null) {
            // Animate to scaled down
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1.0f, 0.7f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1.0f, 0.7f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 1.0f, 0f);
            
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            alpha.setDuration(200);
            
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            alpha.setInterpolator(new AccelerateDecelerateInterpolator());
            
            alpha.addUpdateListener(animation -> {
                if (animation.getAnimatedFraction() == 1.0f && onComplete != null) {
                    onComplete.run();
                }
            });
            
            scaleX.start();
            scaleY.start();
            alpha.start();
        } else {
            if (onComplete != null) onComplete.run();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (isPopupVisible) {
            hideTermsPopup();
        } else {
            super.onBackPressed();
        }
    }
}