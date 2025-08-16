package com.example.accizardlucban;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendLinkButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        sendLinkButton = findViewById(R.id.sign_in_button);
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

        sendLinkButton.setEnabled(false);
        sendLinkButton.setText("Sending...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendLinkButton.setEnabled(true);
                        sendLinkButton.setText("Send Link");
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
                            // Navigate to PasswordRecoveryActivity with success message
                            PasswordRecoveryActivity.startWithEmail(ResetPasswordActivity.this, email);
                            finish();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                            Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                            // Navigate to PasswordRecoveryActivity with error message
                            PasswordRecoveryActivity.startWithErrorMessage(ResetPasswordActivity.this, message);
                        }
                    }
                });
    }
}
