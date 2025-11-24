package com.example.accizardlucban;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {
    
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 6000; // 6 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "SplashActivity started");
        
        // Get LottieAnimationView
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        
        // Play animation
        if (lottieAnimationView != null) {
            lottieAnimationView.setAnimation("accizard-final-splashscreen.json");
            lottieAnimationView.playAnimation();
            Log.d(TAG, "Lottie animation started");
        } else {
            Log.w(TAG, "LottieAnimationView not found");
        }
        
        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Log.d(TAG, "Navigating to MainActivity");
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to MainActivity", e);
            }
        }, SPLASH_DURATION);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity destroyed");
    }
}

