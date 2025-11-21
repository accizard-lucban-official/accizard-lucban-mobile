package com.example.accizardlucban;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageHelper {
    private static final String TAG = "StorageHelper";
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Upload profile image to Firebase Storage
     */
    public static void uploadProfileImage(String userId, Bitmap imageBitmap,
                                        OnSuccessListener<String> onSuccess,
                                        OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Starting profile image upload for user: " + userId);
            
            // Validate input parameters
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Invalid userId provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid userId"));
                return;
            }
            
            if (imageBitmap == null) {
                Log.e(TAG, "Invalid image bitmap provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid image bitmap"));
                return;
            }
            
            // Create a reference to the profile image location
            // Using profile_pictures/{userId}/profile.jpg structure from Firebase rules
            StorageReference profileRef = storage.getReference()
                    .child("profile_pictures")
                    .child(userId)
                    .child("profile.jpg");

            Log.d(TAG, "Storage reference created: " + profileRef.getPath());

            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            
            Log.d(TAG, "Image compressed to " + data.length + " bytes");

            // Upload the image
            UploadTask uploadTask = profileRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Profile image upload completed successfully");
                // Get download URL
                profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Profile image uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading profile image", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadProfileImage", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload chat image to Firebase Storage
     */
    public static void uploadChatImage(String userId, Bitmap imageBitmap,
                                     OnSuccessListener<String> onSuccess,
                                     OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Starting chat image upload for user: " + userId);
            
            // Validate input parameters
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Invalid userId provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid userId"));
                return;
            }
            
            if (imageBitmap == null) {
                Log.e(TAG, "Invalid image bitmap provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid image bitmap"));
                return;
            }
            
            // Create a reference to the chat image location
            // Using chat_attachments/{chatId}/{fileName} structure from Firebase rules
            String chatId = "chat_" + userId + "_" + System.currentTimeMillis();
            String imageId = UUID.randomUUID().toString();
            StorageReference chatRef = storage.getReference()
                    .child("chat_attachments")
                    .child(chatId)
                    .child(imageId + ".jpg");

            Log.d(TAG, "Storage reference created: " + chatRef.getPath());

            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            
            Log.d(TAG, "Image compressed to " + data.length + " bytes");

            // Upload the image
            UploadTask uploadTask = chatRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image upload completed successfully");
                // Get download URL
                chatRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Chat image uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading chat image", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadChatImage", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload chat video to Firebase Storage
     */
    public static void uploadChatVideo(String userId, Uri videoUri,
                                     OnSuccessListener<String> onSuccess,
                                     OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Starting chat video upload for user: " + userId);
            
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Invalid userId provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid userId"));
                return;
            }
            
            if (videoUri == null) {
                Log.e(TAG, "Invalid video URI provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid video URI"));
                return;
            }
            
            String chatId = "chat_" + userId + "_" + System.currentTimeMillis();
            String videoId = UUID.randomUUID().toString();
            StorageReference chatRef = storage.getReference()
                    .child("chat_attachments")
                    .child(chatId)
                    .child(videoId + ".mp4");

            Log.d(TAG, "Storage reference created: " + chatRef.getPath());

            UploadTask uploadTask = chatRef.putFile(videoUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Video upload completed successfully");
                chatRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Chat video uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading chat video", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadChatVideo", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload chat audio to Firebase Storage
     */
    public static void uploadChatAudio(String userId, Uri audioUri,
                                     OnSuccessListener<String> onSuccess,
                                     OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Starting chat audio upload for user: " + userId);
            
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Invalid userId provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid userId"));
                return;
            }
            
            if (audioUri == null) {
                Log.e(TAG, "Invalid audio URI provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid audio URI"));
                return;
            }
            
            String chatId = "chat_" + userId + "_" + System.currentTimeMillis();
            String audioId = UUID.randomUUID().toString();
            StorageReference chatRef = storage.getReference()
                    .child("chat_attachments")
                    .child(chatId)
                    .child(audioId + ".m4a");

            Log.d(TAG, "Storage reference created: " + chatRef.getPath());

            UploadTask uploadTask = chatRef.putFile(audioUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Audio upload completed successfully");
                chatRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Chat audio uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading chat audio", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadChatAudio", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload report videos to Firebase Storage
     * Uses the same report_images path as images to work with existing Firebase Storage rules
     */
    public static void uploadReportVideos(String reportId, List<Uri> videoUris,
                                        OnSuccessListener<List<String>> onSuccess,
                                        OnFailureListener onFailure) {
        try {
            List<String> downloadUrls = new ArrayList<>();
            final int[] uploadCount = {0};
            final int totalVideos = videoUris.size();

            if (totalVideos == 0) {
                onSuccess.onSuccess(downloadUrls);
                return;
            }

            // Get current user ID
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";

            for (int i = 0; i < videoUris.size(); i++) {
                Uri videoUri = videoUris.get(i);
                String videoId = UUID.randomUUID().toString();
                
                // ✅ FIXED: Use report_images path (same as images) to work with existing Firebase Storage rules
                // Videos are stored with .mp4 extension to distinguish them from images
                StorageReference reportRef = storage.getReference()
                        .child("report_images")
                        .child(userId)
                        .child(reportId)
                        .child(videoId + ".mp4");

                Log.d(TAG, "Uploading report video to: " + reportRef.getPath());

                UploadTask uploadTask = reportRef.putFile(videoUri);
                final int videoIndex = i;
                
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    reportRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Report video " + (videoIndex + 1) + " uploaded successfully: " + uri.toString());
                        downloadUrls.add(uri.toString());
                        uploadCount[0]++;
                        
                        if (uploadCount[0] == totalVideos) {
                            Log.d(TAG, "All " + totalVideos + " report videos uploaded successfully");
                            onSuccess.onSuccess(downloadUrls);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL for video " + (videoIndex + 1) + ": " + e.getMessage(), e);
                        onFailure.onFailure(e);
                    });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading report video " + (videoIndex + 1) + ": " + e.getMessage(), e);
                    Log.e(TAG, "Video URI: " + videoUri.toString());
                    Log.e(TAG, "Storage path: " + reportRef.getPath());
                    onFailure.onFailure(e);
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadReportVideos: " + e.getMessage(), e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload report images to Firebase Storage
     */
    public static void uploadReportImages(String reportId, List<Uri> imageUris,
                                        OnSuccessListener<List<String>> onSuccess,
                                        OnFailureListener onFailure) {
        try {
            List<String> downloadUrls = new ArrayList<>();
            final int[] uploadCount = {0};
            final int totalImages = imageUris.size();

            if (totalImages == 0) {
                onSuccess.onSuccess(downloadUrls);
                return;
            }

            for (int i = 0; i < imageUris.size(); i++) {
                Uri imageUri = imageUris.get(i);
                String imageId = UUID.randomUUID().toString();
                
                // Using report_images/{userId}/{reportId}/{fileName} structure from Firebase rules
                // We need to get the current user ID for the path
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
                
                StorageReference reportRef = storage.getReference()
                        .child("report_images")
                        .child(userId)
                        .child(reportId)
                        .child(imageId + ".jpg");

                Log.d(TAG, "Uploading report image to: " + reportRef.getPath());

                // Upload the image
                UploadTask uploadTask = reportRef.putFile(imageUri);
                final int imageIndex = i;
                
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    reportRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Report image " + (imageIndex + 1) + " uploaded successfully: " + uri.toString());
                        downloadUrls.add(uri.toString());
                        uploadCount[0]++;
                        
                        // Check if all images are uploaded
                        if (uploadCount[0] == totalImages) {
                            Log.d(TAG, "All " + totalImages + " report images uploaded successfully");
                            onSuccess.onSuccess(downloadUrls);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL for image " + (imageIndex + 1), e);
                        onFailure.onFailure(e);
                    });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading report image " + (imageIndex + 1), e);
                    onFailure.onFailure(e);
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadReportImages", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload valid ID image to Firebase Storage
     */
    public static void uploadValidIdImage(String userId, Bitmap imageBitmap,
                                        OnSuccessListener<String> onSuccess,
                                        OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Starting valid ID image upload for user: " + userId);
            
            // Validate input parameters
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Invalid userId provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid userId"));
                return;
            }
            
            if (imageBitmap == null) {
                Log.e(TAG, "Invalid image bitmap provided");
                onFailure.onFailure(new IllegalArgumentException("Invalid image bitmap"));
                return;
            }
            
            // Create a reference to the valid ID image location
            // Using valid_ids/{userId}/id.jpg structure from Firebase rules
            StorageReference validIdRef = storage.getReference()
                    .child("valid_ids")
                    .child(userId)
                    .child("id.jpg");

            Log.d(TAG, "Storage reference created: " + validIdRef.getPath());

            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            
            Log.d(TAG, "Image compressed to " + data.length + " bytes");

            // Upload the image
            UploadTask uploadTask = validIdRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Valid ID image upload completed successfully");
                // Get download URL
                validIdRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Valid ID image uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading valid ID image", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadValidIdImage", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Upload a single image from URI to Firebase Storage
     */
    public static void uploadImageFromUri(String folderPath, String fileName, Uri imageUri,
                                        OnSuccessListener<String> onSuccess,
                                        OnFailureListener onFailure) {
        try {
            StorageReference imageRef = storage.getReference()
                    .child(folderPath)
                    .child(fileName);

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                    onSuccess.onSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
                onFailure.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in uploadImageFromUri", e);
            onFailure.onFailure(e);
        }
    }

    /**
     * Delete an image from Firebase Storage
     */
    public static void deleteImage(String imageUrl, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Image deleted successfully");
                onSuccess.onSuccess(aVoid);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting image", e);
                onFailure.onFailure(e);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteImage", e);
            onFailure.onFailure(e);
        }
    }
    
    /**
     * Check Firebase Storage connectivity
     */
    public static void checkStorageConnectivity(OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        try {
            Log.d(TAG, "Checking Firebase Storage connectivity...");
            
            // Create a test reference to a folder that should be accessible
            StorageReference testRef = storage.getReference().child("profile_pictures");
            
            // Try to list files to test connectivity (this should work even if folder is empty)
            testRef.listAll().addOnSuccessListener(listResult -> {
                Log.d(TAG, "Firebase Storage connection successful - can access storage bucket");
                onSuccess.onSuccess(true);
            }).addOnFailureListener(e -> {
                // If listAll fails, try a different approach
                Log.d(TAG, "ListAll failed, trying alternative connectivity check: " + e.getMessage());
                
                // Try to get the root reference metadata
                storage.getReference().getMetadata().addOnSuccessListener(metadata -> {
                    Log.d(TAG, "Firebase Storage connection successful via metadata check");
                    onSuccess.onSuccess(true);
                }).addOnFailureListener(e2 -> {
                    Log.e(TAG, "Firebase Storage connection failed", e2);
                    onFailure.onFailure(e2);
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking Firebase Storage connectivity", e);
            onFailure.onFailure(e);
        }
    }
    
    /**
     * Get detailed error information for Firebase Storage issues
     */
    public static String getStorageErrorDetails(Exception e) {
        if (e == null) return "Unknown error";
        
        String errorMessage = e.getMessage();
        if (errorMessage == null) return "Unknown error";
        
        Log.d(TAG, "Analyzing error: " + errorMessage);
        
        if (errorMessage.contains("permission") || errorMessage.contains("denied")) {
            return "Firebase Storage permission denied. Check your Firebase Storage rules and user authentication.";
        } else if (errorMessage.contains("network") || errorMessage.contains("timeout")) {
            return "Network error. Please check your internet connection and try again.";
        } else if (errorMessage.contains("quota") || errorMessage.contains("exceeded")) {
            return "Firebase Storage quota exceeded. Please upgrade your plan or contact support.";
        } else if (errorMessage.contains("unauthorized") || errorMessage.contains("unauthenticated")) {
            return "User not authenticated. Please sign in to upload images.";
        } else if (errorMessage.contains("not found") || errorMessage.contains("bucket")) {
            return "Storage bucket not found. Check your Firebase configuration and google-services.json file.";
        } else if (errorMessage.contains("invalid") || errorMessage.contains("malformed")) {
            return "Invalid request. Check your Firebase configuration and file format.";
        } else if (errorMessage.contains("canceled") || errorMessage.contains("cancelled")) {
            return "Upload was cancelled. Please try again.";
        } else if (errorMessage.contains("retry")) {
            return "Upload failed. Please check your connection and try again.";
        } else {
            return "Storage error: " + errorMessage;
        }
    }
    
    /**
     * Check if user is authenticated for Firebase Storage operations
     */
    public static boolean isUserAuthenticated() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser() != null;
    }
    
    /**
     * Get current user ID for Firebase Storage operations
     */
    public static String getCurrentUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }
}
