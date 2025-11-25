package com.example.accizardlucban;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SplashActivity extends AppCompatActivity {
    
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 6000; // 6 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity full screen
        makeFullScreen();
        
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "SplashActivity started");
        
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
    
    /**
     * Make the activity full screen by hiding status bar and navigation bar
     */
    private void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars() | androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4+ (API 19+)
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        } else {
            // Older Android versions
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Maintain full screen when window gains focus
            makeFullScreen();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity destroyed");
    }
}

