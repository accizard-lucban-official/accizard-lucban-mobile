package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordRecoveryActivity extends AppCompatActivity {

    private Button goBackButton;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Check if there's a custom message to display
        handleIntent();
    }

    private void initializeViews() {
        goBackButton = findViewById(R.id.go_back_button);
        errorMessageTextView = findViewById(R.id.error_message_text_view);
    }

    private void setupClickListeners() {
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToLogin();
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Check if there's a custom error message
            String customMessage = intent.getStringExtra("error_message");
            if (customMessage != null && !customMessage.isEmpty()) {
                errorMessageTextView.setText(customMessage);
            }

            // Check if there's a custom email to display
            String email = intent.getStringExtra("email");
            if (email != null && !email.isEmpty()) {
                String message = "Password reset link has been\n" +
                        "sent to " + email + "\n\n" +
                        "Please check your email and\n" +
                        "follow the instructions to\n" +
                        "reset your password.";
                errorMessageTextView.setText(message);
            }
        }
    }

    private void goBackToLogin() {
        // Create intent to go back to login activity
        Intent intent = new Intent(PasswordRecoveryActivity.this, MainActivity.class);

        // Clear the activity stack so user can't go back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Handle back button press - go back to login
        goBackToLogin();
    }

    // Static method to start this activity with custom message
    public static void startWithErrorMessage(android.content.Context context, String errorMessage) {
        Intent intent = new Intent(context, PasswordRecoveryActivity.class);
        intent.putExtra("error_message", errorMessage);
        context.startActivity(intent);
    }

    // Static method to start this activity with email
    public static void startWithEmail(android.content.Context context, String email) {
        Intent intent = new Intent(context, PasswordRecoveryActivity.class);
        intent.putExtra("email", email);
        context.startActivity(intent);
    }
}