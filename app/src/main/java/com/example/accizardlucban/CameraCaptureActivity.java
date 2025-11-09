package com.example.accizardlucban;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraCaptureActivity extends AppCompatActivity {
    
    private static final String TAG = "CameraCaptureActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 101;
    
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isPreviewRunning = false;
    private Button btnCapture;
    private Button btnCancel;
    private File photoFile;
    private View overlayView;
    private ImageView previewImageView;
    private FrameLayout previewContainer;
    private boolean hasCapturedPhoto = false;
    private Button btnConfirm;
    private TextView instructionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set portrait orientation
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Keep screen on while camera is active
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.d(TAG, "=== CameraCaptureActivity started ===");
        
        // Create camera layout
        createCameraLayout();
        
        // Check permission
        if (checkCameraPermission()) {
            // Camera will start when surface is created
        } else {
            requestCameraPermission();
        }
    }
    
    private void createCameraLayout() {
        // Main container
        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.setBackgroundColor(0xFF000000);
        
        // Camera preview surface
        surfaceView = new SurfaceView(this);
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "Surface created");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "Surface changed: " + width + "x" + height);
                if (isPreviewRunning) {
                    stopPreview();
                }
                startPreview();
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "Surface destroyed");
                stopPreview();
            }
        });
        
        // Overlay with L-shaped corners
        overlayView = new View(this) {
            @Override
            protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw darker overlay for areas outside the guide frame
                // Top area
                android.graphics.Paint overlayPaint = new android.graphics.Paint();
                overlayPaint.setColor(0xBB000000); // Dark semi-transparent
                
                // Guide frame dimensions - larger area for ID capture
                int guideWidth = (int) (width * 0.9); // Increased from 0.8 to 0.9
                int guideHeight = (int) (height * 0.5); // Increased from 0.4 to 0.5
                int left = (width - guideWidth) / 2;
                int top = (height - guideHeight) / 2;
                int right = left + guideWidth;
                int bottom = top + guideHeight;
                
                // Draw dark overlay in 4 rectangles around the guide frame
                canvas.drawRect(0, 0, width, top, overlayPaint); // Top
                canvas.drawRect(0, bottom, width, height, overlayPaint); // Bottom
                canvas.drawRect(0, top, left, bottom, overlayPaint); // Left
                canvas.drawRect(right, top, width, bottom, overlayPaint); // Right
                
                // L-shaped corner guides
                android.graphics.Paint guidePaint = new android.graphics.Paint();
                guidePaint.setColor(0xFFFFFFFF);
                guidePaint.setStrokeWidth(6);
                guidePaint.setStyle(android.graphics.Paint.Style.STROKE);
                
                int cornerLength = 50;
                
                // Top-left L-shape
                canvas.drawLine(left, top, left + cornerLength, top, guidePaint);
                canvas.drawLine(left, top, left, top + cornerLength, guidePaint);
                
                // Top-right L-shape
                canvas.drawLine(right - cornerLength, top, right, top, guidePaint);
                canvas.drawLine(right, top, right, top + cornerLength, guidePaint);
                
                // Bottom-left L-shape
                canvas.drawLine(left, bottom, left + cornerLength, bottom, guidePaint);
                canvas.drawLine(left, bottom - cornerLength, left, bottom, guidePaint);
                
                // Bottom-right L-shape
                canvas.drawLine(right - cornerLength, bottom, right, bottom, guidePaint);
                canvas.drawLine(right, bottom - cornerLength, right, bottom, guidePaint);
            }
        };
        overlayView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        // Instructions text
        instructionText = new android.widget.TextView(this);
        instructionText.setText("Position your ID inside the frame");
        instructionText.setTextColor(0xFFFFFFFF);
        instructionText.setTextSize(18);
        instructionText.setGravity(android.view.Gravity.CENTER);
        instructionText.setBackgroundColor(0x66000000);
        instructionText.setPadding(20, 30, 20, 30);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
        textParams.topMargin = 50;
        instructionText.setLayoutParams(textParams);
        
        // Capture button
        btnCapture = new Button(this);
        btnCapture.setText("Capture");
        btnCapture.setTextColor(0xFFFFFFFF);
        btnCapture.setBackgroundColor(0xFF6200EE);
        btnCapture.setTextSize(16);
        FrameLayout.LayoutParams captureParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                120,
                android.view.Gravity.BOTTOM);
        captureParams.leftMargin = 40;
        captureParams.rightMargin = 40;
        captureParams.bottomMargin = 100;
        btnCapture.setLayoutParams(captureParams);
        btnCapture.setOnClickListener(v -> capturePhoto());
        
        // Cancel button
        btnCancel = new Button(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextColor(0xFFFFFFFF);
        btnCancel.setBackgroundColor(0x66000000);
        btnCancel.setTextSize(14);
        FrameLayout.LayoutParams cancelParams = new FrameLayout.LayoutParams(
                200,
                80,
                android.view.Gravity.BOTTOM | android.view.Gravity.START);
        cancelParams.leftMargin = 20;
        cancelParams.bottomMargin = 20;
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setOnClickListener(v -> finish());
        
        // Get screen dimensions for preview container
        android.view.Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        
        // Preview container - positioned inside the L-shaped frame
        previewContainer = new FrameLayout(this);
        int previewWidth = (int) (screenWidth * 0.9);
        int previewHeight = (int) (screenHeight * 0.5);
        FrameLayout.LayoutParams previewContainerParams = new FrameLayout.LayoutParams(
                previewWidth,
                previewHeight,
                android.view.Gravity.CENTER);
        previewContainer.setLayoutParams(previewContainerParams);
        previewContainer.setVisibility(View.GONE); // Hidden initially
        previewContainer.setBackgroundColor(0xFFFFFFFF);
        
        // Preview image view
        previewImageView = new ImageView(this);
        previewImageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewContainer.addView(previewImageView);
        
        // Confirm button (initially hidden)
        btnConfirm = new Button(this);
        btnConfirm.setText("Confirm Photo");
        btnConfirm.setTextColor(0xFFFFFFFF);
        btnConfirm.setBackgroundColor(0xFF00C853);
        btnConfirm.setTextSize(16);
        FrameLayout.LayoutParams confirmParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                120,
                android.view.Gravity.BOTTOM);
        confirmParams.leftMargin = 40;
        confirmParams.rightMargin = 40;
        confirmParams.bottomMargin = 100;
        btnConfirm.setLayoutParams(confirmParams);
        btnConfirm.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(v -> confirmAndFinish());
        
        // Add all views
        mainLayout.addView(surfaceView);
        mainLayout.addView(overlayView);
        mainLayout.addView(previewContainer);
        mainLayout.addView(instructionText);
        mainLayout.addView(btnCapture);
        mainLayout.addView(btnConfirm);
        mainLayout.addView(btnCancel);
        
        setContentView(mainLayout);
    }
    
    @SuppressLint("deprecation")
    private void startPreview() {
        if (!isPreviewRunning && checkCameraPermission()) {
            try {
                camera = Camera.open();
                if (camera != null && surfaceHolder.getSurface().isValid()) {
                    // Set orientation to portrait (90 degrees)
                    camera.setDisplayOrientation(90);
                    
                    // Get camera parameters
                    Camera.Parameters params = camera.getParameters();
                    
                    // Set rotation for captured image to portrait
                    params.setRotation(90);
                    
                    // Configure preview size to be portrait-friendly
                    try {
                        Camera.Size bestSize = getBestPreviewSize(params, surfaceView.getWidth(), surfaceView.getHeight());
                        if (bestSize != null) {
                            params.setPreviewSize(bestSize.width, bestSize.height);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting preview size", e);
                    }
                    
                    // Set zoom level to wide angle (if supported)
                    try {
                        if (params.isZoomSupported()) {
                            int maxZoom = params.getMaxZoom();
                            if (maxZoom > 0) {
                                // Set to minimum zoom for wide angle
                                params.setZoom(0);
                                Log.d(TAG, "Zoom set to 0 (wide angle)");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting zoom", e);
                    }
                    
                    // Apply parameters
                    try {
                        camera.setParameters(params);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting camera parameters", e);
                    }
                    
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    isPreviewRunning = true;
                    Log.d(TAG, "Camera preview started in portrait mode");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error starting camera preview", e);
                releaseCamera();
            } catch (Exception e) {
                Log.e(TAG, "Error opening camera", e);
                releaseCamera();
            }
        }
    }
    
    @SuppressLint("deprecation")
    private Camera.Size getBestPreviewSize(Camera.Parameters parameters, int width, int height) {
        Camera.Size bestSize = null;
        int minDiff = Integer.MAX_VALUE;
        
        // For portrait, we're using rotated dimensions
        int displayHeight = Math.max(width, height);
        int displayWidth = Math.min(width, height);
        
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            int diff = Math.abs(size.width - displayWidth) + Math.abs(size.height - displayHeight);
            if (diff < minDiff) {
                minDiff = diff;
                bestSize = size;
            }
        }
        
        return bestSize;
    }
    
    @SuppressLint("deprecation")
    private void stopPreview() {
        if (isPreviewRunning && camera != null) {
            try {
                camera.stopPreview();
                isPreviewRunning = false;
                Log.d(TAG, "Camera preview stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping preview", e);
            }
            releaseCamera();
        }
    }
    
    @SuppressLint("deprecation")
    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
    
    @SuppressLint("deprecation")
    private void capturePhoto() {
        if (camera == null || !isPreviewRunning) {
            return;
        }
        
        try {
            btnCapture.setEnabled(false);
            btnCapture.setText("Capturing...");
            
            // Set rotation parameter before taking picture
            Camera.Parameters params = camera.getParameters();
            params.setRotation(90); // Portrait orientation
            camera.setParameters(params);
            
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        // Save photo to file
                        photoFile = new File(getCacheDir(), "captured_id_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        fos.write(data);
                        fos.close();
                        
                        Log.d(TAG, "Photo saved in portrait: " + photoFile.getAbsolutePath());
                        
                        // Display the captured image in the preview container
                        runOnUiThread(() -> {
                            try {
                                Bitmap capturedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                                if (capturedBitmap != null) {
                                    previewImageView.setImageBitmap(capturedBitmap);
                                    previewContainer.setVisibility(View.VISIBLE);
                                    surfaceView.setVisibility(View.GONE);
                                    overlayView.setVisibility(View.GONE);
                                    instructionText.setVisibility(View.GONE);
                                    btnCapture.setVisibility(View.GONE);
                                    btnConfirm.setVisibility(View.VISIBLE);
                                    hasCapturedPhoto = true;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error displaying preview", e);
                                btnCapture.setEnabled(true);
                                btnCapture.setText("Capture");
                            }
                        });
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving photo", e);
                        runOnUiThread(() -> {
                            btnCapture.setEnabled(true);
                            btnCapture.setText("Capture");
                        });
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error capturing photo", e);
            btnCapture.setEnabled(true);
            btnCapture.setText("Capture");
        }
    }
    
    private void confirmAndFinish() {
        if (photoFile != null && photoFile.exists()) {
            // Return the photo path
            Intent resultIntent = new Intent();
            resultIntent.putExtra("image_path", photoFile.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    
    private boolean checkCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
            } else {
                finish();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (surfaceHolder.getSurface().isValid()) {
            startPreview();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreview();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
