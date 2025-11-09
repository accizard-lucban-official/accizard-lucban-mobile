package com.example.accizardlucban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to cache profile pictures for instant loading across all activities
 */
public class ProfilePictureCache {
    
    private static final String TAG = "ProfilePictureCache";
    private static ProfilePictureCache instance;
    private Map<String, Bitmap> cache = new HashMap<>();
    
    private ProfilePictureCache() {
        // Private constructor for singleton
    }
    
    public static synchronized ProfilePictureCache getInstance() {
        if (instance == null) {
            instance = new ProfilePictureCache();
        }
        return instance;
    }
    
    /**
     * Load profile picture with instant caching
     * @param imageView The ImageView to display the profile picture
     * @param imageUrl The URL of the profile picture
     * @param useCircular Whether to make the image circular
     */
    public void loadProfilePicture(ImageView imageView, String imageUrl, boolean useCircular) {
        if (imageView == null) {
            Log.w(TAG, "ImageView is null, cannot load profile picture");
            return;
        }
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.d(TAG, "No profile picture URL, using default");
            imageView.setImageResource(R.drawable.ic_person);
            return;
        }
        
        // Check if we have a cached version for instant display
        if (cache.containsKey(imageUrl)) {
            Bitmap cachedBitmap = cache.get(imageUrl);
            if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                imageView.setImageBitmap(cachedBitmap);
                Log.d(TAG, "Using cached image - INSTANT display for: " + imageUrl);
                return;
            } else {
                // Remove invalid cached bitmap
                cache.remove(imageUrl);
            }
        }
        
        // Load from URL and cache for instant future access
        loadFromUrlAndCache(imageView, imageUrl, useCircular);
    }
    
    /**
     * Load chat image with instant caching (optimized for larger images)
     * @param imageView The ImageView to display the chat image
     * @param imageUrl The URL of the chat image
     */
    public void loadChatImage(ImageView imageView, String imageUrl) {
        if (imageView == null) {
            Log.w(TAG, "ImageView is null, cannot load chat image");
            return;
        }
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.d(TAG, "No chat image URL");
            return;
        }
        
        // Check if we have a cached version for instant display
        if (cache.containsKey(imageUrl)) {
            Bitmap cachedBitmap = cache.get(imageUrl);
            if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                imageView.setImageBitmap(cachedBitmap);
                Log.d(TAG, "Using cached chat image - INSTANT display");
                return;
            } else {
                cache.remove(imageUrl);
            }
        }
        
        // Show placeholder while loading
        imageView.setImageResource(R.drawable.ic_camera_placeholder);
        
        // Load from URL and cache for instant future access
        loadChatImageFromUrlAndCache(imageView, imageUrl);
    }
    
    /**
     * Load profile picture with instant caching (default circular)
     */
    public void loadProfilePicture(ImageView imageView, String imageUrl) {
        loadProfilePicture(imageView, imageUrl, true);
    }
    
    private void loadFromUrlAndCache(ImageView imageView, String imageUrl, boolean useCircular) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                
                if (bitmap != null) {
                    // Create circular or keep original based on parameter
                    Bitmap finalBitmap = useCircular ? createCircularBitmap(bitmap) : bitmap;
                    
                    // Cache the bitmap for instant loading next time
                    cache.put(imageUrl, finalBitmap);
                    Log.d(TAG, "Profile picture loaded and cached: " + imageUrl);
                    
                    // Update UI on main thread
                    imageView.post(() -> {
                        if (!finalBitmap.isRecycled()) {
                            imageView.setImageBitmap(finalBitmap);
                        }
                    });
                } else {
                    imageView.post(() -> imageView.setImageResource(R.drawable.ic_person));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile picture from URL: " + imageUrl, e);
                imageView.post(() -> imageView.setImageResource(R.drawable.ic_person));
            }
        }).start();
    }
    
    private void loadChatImageFromUrlAndCache(ImageView imageView, String imageUrl) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                
                if (bitmap != null) {
                    // Scale down if too large to prevent memory issues
                    Bitmap finalBitmap = scaleChatImage(bitmap);
                    
                    // Cache the bitmap for instant loading next time
                    cache.put(imageUrl, finalBitmap);
                    Log.d(TAG, "Chat image loaded and cached: " + imageUrl);
                    
                    // Update UI on main thread
                    imageView.post(() -> {
                        if (!finalBitmap.isRecycled()) {
                            imageView.setImageBitmap(finalBitmap);
                        }
                    });
                } else {
                    Log.w(TAG, "Failed to load chat image bitmap");
                    imageView.post(() -> imageView.setImageResource(R.drawable.ic_camera_placeholder));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading chat image from URL: " + imageUrl, e);
                imageView.post(() -> imageView.setImageResource(R.drawable.ic_camera_placeholder));
            }
        }).start();
    }
    
    /**
     * Scale chat image to reasonable size (keeps aspect ratio)
     */
    private Bitmap scaleChatImage(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            // Max dimension for chat images (larger than profile pictures)
            int maxDimension = 800;
            
            // If already small enough, return original
            if (width <= maxDimension && height <= maxDimension) {
                return bitmap;
            }
            
            // Calculate scale to fit within max dimension while keeping aspect ratio
            float scale;
            if (width > height) {
                scale = (float) maxDimension / width;
            } else {
                scale = (float) maxDimension / height;
            }
            
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            
            // Recycle original if it's a different bitmap
            if (scaledBitmap != bitmap) {
                bitmap.recycle();
            }
            
            return scaledBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error scaling chat image", e);
            return bitmap;
        }
    }
    
    private Bitmap createCircularBitmap(Bitmap bitmap) {
        try {
            // Center-crop to square first to avoid distortion
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int squareSize = Math.min(width, height);
            int xOffset = (width - squareSize) / 2;
            int yOffset = (height - squareSize) / 2;

            Bitmap squareCropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize);

            int targetSize = 200; // Good size for profile pictures
            Bitmap scaledSquare = squareSize == targetSize
                    ? squareCropped
                    : Bitmap.createScaledBitmap(squareCropped, targetSize, targetSize, true);

            Bitmap circularBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(circularBitmap);

            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            android.graphics.Path path = new android.graphics.Path();
            path.addCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, android.graphics.Path.Direction.CW);
            canvas.clipPath(path);
            canvas.drawBitmap(scaledSquare, 0, 0, paint);

            if (scaledSquare != squareCropped) {
                scaledSquare.recycle();
            }
            if (squareCropped != bitmap) {
                squareCropped.recycle();
            }

            return circularBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error creating circular bitmap", e);
            return bitmap;
        }
    }
    
    /**
     * Clear the cache (use when user signs out)
     */
    public void clearCache() {
        for (Bitmap bitmap : cache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        cache.clear();
        Log.d(TAG, "Profile picture cache cleared");
    }
    
    /**
     * Pre-cache a profile picture for instant loading
     */
    public void precacheProfilePicture(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        if (cache.containsKey(imageUrl)) return; // Already cached
        
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                
                if (bitmap != null) {
                    Bitmap circularBitmap = createCircularBitmap(bitmap);
                    cache.put(imageUrl, circularBitmap);
                    Log.d(TAG, "Profile picture pre-cached: " + imageUrl);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error pre-caching profile picture", e);
            }
        }).start();
    }
}




