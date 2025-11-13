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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.cardview.widget.CardView;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etMobileNumber, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private TextView tvSignIn, tvTermsConditions;
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
        restoreRegistrationData(); // Restore registration data if exists
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
        
        // Find Terms and Conditions TextView
        tvTermsConditions = findViewById(R.id.tvTermsConditions);
        
        // Set up spannable text with clickable Terms and Privacy Policy
        setupTermsText();
        
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
    
    private void setupTermsText() {
        String fullText = "I agree to the Terms and Conditions and Privacy Policy";
        SpannableString spannableString = new SpannableString(fullText);
        
        // Get orange color from resources
        int orangeColor = getResources().getColor(R.color.orange_primary, getTheme());
        
        // Make "Terms and Conditions" clickable
        String termsText = "Terms and Conditions";
        int termsStart = fullText.indexOf(termsText);
        int termsEnd = termsStart + termsText.length();
        
        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showTermsPopup();
            }
            
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(orangeColor);
                ds.setUnderlineText(false);
                ds.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            }
        };
        
        spannableString.setSpan(termsSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(orangeColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // Make "Privacy Policy" clickable
        String privacyText = "Privacy Policy";
        int privacyStart = fullText.indexOf(privacyText);
        int privacyEnd = privacyStart + privacyText.length();
        
        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showTermsPopup();
            }
            
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(orangeColor);
                ds.setUnderlineText(false);
                ds.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            }
        };
        
        spannableString.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(orangeColor), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        tvTermsConditions.setText(spannableString);
        tvTermsConditions.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    android.util.Log.d("RegistrationActivity", "Create Account button clicked");
                    
                    if (validateInputs()) {
                        android.util.Log.d("RegistrationActivity", "Validation passed, proceeding to PersonalInfo");
                        proceedToPersonalInfo();
                    } else {
                        android.util.Log.d("RegistrationActivity", "Validation failed - check error messages");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("RegistrationActivity", "Exception in button click", e);
                    Toast.makeText(RegistrationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        
        // Note: Terms and Privacy Policy clicks are handled by clickable spans in setupTermsText()
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
        android.util.Log.d("RegistrationActivity", "Starting validation...");
        
        // Check if views are initialized
        if (etFirstName == null || etLastName == null || etMobileNumber == null || 
            etEmail == null || etPassword == null || cbTerms == null) {
            android.util.Log.e("RegistrationActivity", "ERROR: Some views are null!");
            Toast.makeText(this, "Form initialization error. Please restart the app.", Toast.LENGTH_LONG).show();
            return false;
        }
        
        // First Name validation
        String firstName = etFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: First name is empty");
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ First name: " + firstName);

        // Last Name validation
        String lastName = etLastName.getText().toString().trim();
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Last name is empty");
            Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show();
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ Last name: " + lastName);

        // Mobile Number validation
        String mobileNumber = etMobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(mobileNumber)) {
            etMobileNumber.setError("Mobile number is required");
            etMobileNumber.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Mobile number is empty");
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Mobile number format validation
        if (!isValidPhilippineMobileNumber(mobileNumber)) {
            etMobileNumber.setError("Please enter a valid Philippine mobile number (e.g., 09123456789)");
            etMobileNumber.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Invalid mobile number format: " + mobileNumber);
            Toast.makeText(this, "Invalid mobile number format.\nExample: 09123456789 or +639123456789", Toast.LENGTH_LONG).show();
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ Mobile number: " + mobileNumber);

        // Email validation
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Email is empty");
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Invalid email format: " + email);
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ Email: " + email);

        // Password validation
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Password is empty");
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!isStrongPassword(password)) {
            etPassword.setError("Password must be at least 8 characters long");
            etPassword.requestFocus();
            android.util.Log.d("RegistrationActivity", "Validation failed: Password not strong enough: " + password);
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ Password: [hidden]");
        
        // Terms and Conditions validation
        if (cbTerms == null) {
            android.util.Log.e("RegistrationActivity", "ERROR: cbTerms checkbox is null!");
            Toast.makeText(this, "Terms checkbox error. Please restart.", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to Terms and Conditions by checking the box", Toast.LENGTH_LONG).show();
            android.util.Log.d("RegistrationActivity", "Validation failed: Terms checkbox not checked");
            cbTerms.requestFocus();
            return false;
        }
        android.util.Log.d("RegistrationActivity", "✓ Terms and Conditions: Accepted");

        android.util.Log.d("RegistrationActivity", "✅ ALL VALIDATIONS PASSED!");
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
        // Only require password to be at least 8 characters long
        return password.length() >= 8;
    }

    private void proceedToPersonalInfo() {
        try {
            android.util.Log.d("RegistrationActivity", "Proceeding to PersonalInfoActivity");
            
            // Save first and last name to SharedPreferences (existing functionality)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_FIRST_NAME, etFirstName.getText().toString().trim());
            editor.putString(KEY_LAST_NAME, etLastName.getText().toString().trim());
            editor.apply();

            // ALSO save to registration_data for retention
            saveRegistrationData();

            android.util.Log.d("RegistrationActivity", "Creating intent for PersonalInfoActivity");
            Intent intent = new Intent(RegistrationActivity.this, PersonalInfoActivity.class);
            intent.putExtra("firstName", etFirstName.getText().toString().trim());
            intent.putExtra("lastName", etLastName.getText().toString().trim());
            intent.putExtra("mobileNumber", etMobileNumber.getText().toString().trim());
            intent.putExtra("email", etEmail.getText().toString().trim());
            intent.putExtra("password", etPassword.getText().toString().trim());
            
            android.util.Log.d("RegistrationActivity", "Starting PersonalInfoActivity");
            startActivity(intent);
            
            android.util.Log.d("RegistrationActivity", "Successfully navigated to PersonalInfoActivity");
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("RegistrationActivity", "Error proceeding to personal info", e);
            Toast.makeText(this, "Error proceeding to personal info: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    /**
     * Restores registration data from SharedPreferences if exists
     * This allows users to continue registration after going back
     */
    private void restoreRegistrationData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            
            // Restore first name
            String savedFirstName = prefs.getString("saved_first_name", null);
            if (savedFirstName != null && !savedFirstName.isEmpty()) {
                etFirstName.setText(savedFirstName);
                android.util.Log.d("RegistrationActivity", "First name restored: " + savedFirstName);
            }
            
            // Restore last name
            String savedLastName = prefs.getString("saved_last_name", null);
            if (savedLastName != null && !savedLastName.isEmpty()) {
                etLastName.setText(savedLastName);
                android.util.Log.d("RegistrationActivity", "Last name restored: " + savedLastName);
            }
            
            // Restore mobile number
            String savedMobile = prefs.getString("saved_mobile_number", null);
            if (savedMobile != null && !savedMobile.isEmpty()) {
                etMobileNumber.setText(savedMobile);
                android.util.Log.d("RegistrationActivity", "Mobile number restored: " + savedMobile);
            }
            
            // Restore email
            String savedEmail = prefs.getString("saved_email", null);
            if (savedEmail != null && !savedEmail.isEmpty()) {
                etEmail.setText(savedEmail);
                android.util.Log.d("RegistrationActivity", "Email restored: " + savedEmail);
            }
            
            // Restore password
            String savedPassword = prefs.getString("saved_password", null);
            if (savedPassword != null && !savedPassword.isEmpty()) {
                etPassword.setText(savedPassword);
                android.util.Log.d("RegistrationActivity", "Password restored");
            }
            
            // Restore terms checkbox
            boolean savedTerms = prefs.getBoolean("saved_terms", false);
            cbTerms.setChecked(savedTerms);
            
            android.util.Log.d("RegistrationActivity", "✅ Registration data restored from SharedPreferences");
        } catch (Exception e) {
            android.util.Log.e("RegistrationActivity", "Error restoring registration data", e);
        }
    }

    /**
     * Saves registration data to SharedPreferences for retention
     */
    private void saveRegistrationData() {
        try {
            SharedPreferences prefs = getSharedPreferences("registration_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putString("saved_first_name", etFirstName.getText().toString().trim());
            editor.putString("saved_last_name", etLastName.getText().toString().trim());
            editor.putString("saved_mobile_number", etMobileNumber.getText().toString().trim());
            editor.putString("saved_email", etEmail.getText().toString().trim());
            editor.putString("saved_password", etPassword.getText().toString().trim());
            editor.putBoolean("saved_terms", cbTerms.isChecked());
            
            editor.apply();
            android.util.Log.d("RegistrationActivity", "✅ Registration data saved to SharedPreferences");
        } catch (Exception e) {
            android.util.Log.e("RegistrationActivity", "Error saving registration data", e);
        }
    }
}