package com.example.accizardlucban;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import java.util.ArrayList;
import java.util.List;

public class SafetyTipsActivity extends AppCompatActivity {

    private static final String TAG = "SafetyTipsActivity";
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    
    private TextView titleText;
    private TextView subtitleText;
    private LinearLayout contentContainer;
    private LinearLayout posterContainer;
    private ImageView posterImageView;
    // Arrow buttons removed from layout - using swipe gestures instead
    // private ImageView leftArrowButton;
    // private ImageView rightArrowButton;
    private View dot1, dot2, dot3, dot4, dot5;
    private ScrollView scrollView;
    
    private String safetyType;
    private int currentPage = 0;
    private List<SafetyTipsContent.SafetyTipPage> pages;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_tips);
        
        try {
            // Get safety type from intent
            safetyType = getIntent().getStringExtra("safety_type");
            if (safetyType == null) {
                safetyType = "Road Safety";
            }
            
            initializeViews();
            setupGestureDetector();
            setupContent();
            setupClickListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        contentContainer = findViewById(R.id.contentContainer);
        posterContainer = findViewById(R.id.poster);
        // Arrow buttons removed from layout - using swipe gestures instead
        // leftArrowButton = findViewById(R.id.leftArrowButton);
        // rightArrowButton = findViewById(R.id.rightArrowButton);
        scrollView = findViewById(R.id.scrollView);
        
        // Initialize poster ImageView
        setupPosterImageView();
        
        // Pagination dots
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dot5 = findViewById(R.id.dot5);
    }
    
    private void setupPosterImageView() {
        try {
            // Clear the existing content in poster container
            posterContainer.removeAllViews();
            
            // Create ImageView for poster
            posterImageView = new ImageView(this);
            posterImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            posterImageView.setAdjustViewBounds(true);
            posterImageView.setPadding(0, 0, 0, 0);
            
            // Add ImageView to poster container
            posterContainer.addView(posterImageView);
            
            // Initially hide the poster
            posterContainer.setVisibility(View.GONE);
            
            Log.d(TAG, "Poster ImageView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up poster ImageView", e);
        }
    }
    
    private void setupGestureDetector() {
        try {
            // Create gesture listener
            GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
                
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    try {
                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();
                        
                        // Check if horizontal swipe is dominant
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                if (diffX > 0) {
                                    // Swipe right - go to previous page
                                    onSwipeRight();
                                } else {
                                    // Swipe left - go to next page
                                    onSwipeLeft();
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onFling: " + e.getMessage(), e);
                    }
                    return false;
                }
            };
            
            // Initialize gesture detector
            gestureDetector = new GestureDetectorCompat(this, gestureListener);
            
            // Set touch listener on scroll view
            if (scrollView != null) {
                scrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        gestureDetector.onTouchEvent(event);
                        return false; // Return false to allow ScrollView to handle scrolling
                    }
                });
                Log.d(TAG, "Gesture detector setup completed");
            } else {
                Log.w(TAG, "ScrollView not found for gesture detection");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up gesture detector: " + e.getMessage(), e);
        }
    }
    
    private void onSwipeLeft() {
        // Swipe left - go to next page or back to dashboard
        if (currentPage < pages.size() - 1) {
            currentPage++;
            displayCurrentPage();
            scrollView.smoothScrollTo(0, 0); // Scroll to top
            Log.d(TAG, "Swiped left - moved to page " + (currentPage + 1));
        } else {
            // On last page - swipe left goes back to dashboard
            Log.d(TAG, "Swiped left on last page - going back to dashboard");
            finish();
        }
    }
    
    private void onSwipeRight() {
        // Swipe right - go to previous page
        if (currentPage > 0) {
            currentPage--;
            displayCurrentPage();
            scrollView.smoothScrollTo(0, 0); // Scroll to top
            Log.d(TAG, "Swiped right - moved to page " + (currentPage + 1));
        }
    }
    
    private void setupContent() {
        try {
            // Set title based on safety type
            titleText.setText(getTitleForSafetyType(safetyType));
            subtitleText.setText(getSubtitleForSafetyType(safetyType));
            
            // Create pages for this safety type
            pages = createPagesForSafetyType(safetyType);
            
            // Display first page
            displayCurrentPage();
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up content: " + e.getMessage(), e);
        }
    }
    
    private void setupClickListeners() {
        // Back button
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        // Arrow buttons removed from layout - using swipe gestures instead
        // Left arrow button (previous page)
        // if (leftArrowButton != null) {
        //     leftArrowButton.setOnClickListener(v -> {
        //         if (currentPage > 0) {
        //             currentPage--;
        //             displayCurrentPage();
        //             if (scrollView != null) {
        //                 scrollView.smoothScrollTo(0, 0);
        //             }
        //         }
        //     });
        // }
        
        // Right arrow button (next page or go back)
        // if (rightArrowButton != null) {
        //     rightArrowButton.setOnClickListener(v -> {
        //         if (currentPage < pages.size() - 1) {
        //             // Not on last page - go to next page
        //             currentPage++;
        //             displayCurrentPage();
        //             if (scrollView != null) {
        //                 scrollView.smoothScrollTo(0, 0);
        //             }
        //         } else {
        //             // On last page - go back to dashboard
        //             finish();
        //         }
        //     });
        // }
    }
    
    private String getTitleForSafetyType(String type) {
        switch (type) {
            case "Road Safety":
                return "Overview of Road Accidents";
            case "Fire Safety":
                return "Fire Safety Guidelines";
            case "Landslide Safety":
                return "Landslide Safety Tips";
            case "Earthquake Safety":
                return "Earthquake Safety Guide";
            case "Flood Safety":
                return "Flood Safety Information";
            case "Volcanic Safety":
                return "Volcanic Safety Tips";
            case "Civil Disturbance":
                return "Civil Disturbance Safety";
            case "Armed Conflict":
                return "Armed Conflict Safety";
            case "Infectious Disease":
                return "Infectious Disease Prevention";
            default:
                return "Safety Tips";
        }
    }
    
    private String getSubtitleForSafetyType(String type) {
        switch (type) {
            case "Road Safety":
                return "in the Philippines";
            case "Fire Safety":
                return "Essential Guidelines";
            case "Landslide Safety":
                return "Prevention and Response";
            case "Earthquake Safety":
                return "Before, During, and After";
            case "Flood Safety":
                return "Prevention and Response";
            case "Volcanic Safety":
                return "Volcanic Activity Safety";
            case "Civil Disturbance":
                return "Safety Guidelines";
            case "Armed Conflict":
                return "Safety Measures";
            case "Infectious Disease":
                return "Prevention and Control";
            default:
                return "Safety Information";
        }
    }
    
    private List<SafetyTipsContent.SafetyTipPage> createPagesForSafetyType(String type) {
        List<SafetyTipsContent.SafetyTipPage> pages = new ArrayList<>();
        
        switch (type) {
            case "Road Safety":
                pages = SafetyTipsContent.createRoadSafetyPages();
                break;
            case "Fire Safety":
                pages = SafetyTipsContent.createFireSafetyPages();
                break;
            case "Landslide Safety":
                pages = SafetyTipsContent.createLandslideSafetyPages();
                break;
            case "Earthquake Safety":
                pages = SafetyTipsContent.createEarthquakeSafetyPages();
                break;
            case "Flood Safety":
                pages = SafetyTipsContent.createFloodSafetyPages();
                break;
            case "Volcanic Safety":
                pages = SafetyTipsContent.createVolcanicSafetyPages();
                break;
            case "Civil Disturbance":
                pages = SafetyTipsContent.createCivilDisturbancePages();
                break;
            case "Armed Conflict":
                pages = SafetyTipsContent.createArmedConflictPages();
                break;
            case "Infectious Disease":
                pages = SafetyTipsContent.createInfectiousDiseasePages();
                break;
            default:
                pages = SafetyTipsContent.createDefaultPages();
        }
        
        return pages;
    }
    
    private void displayCurrentPage() {
        try {
            if (pages == null || pages.isEmpty() || currentPage >= pages.size()) {
                return;
            }
            
            SafetyTipsContent.SafetyTipPage page = pages.get(currentPage);
            
            // Update title and subtitle
            titleText.setText(page.title);
            subtitleText.setText(page.subtitle);
            
            // Handle poster image
            if (page.imageResource != 0) {
                // Show poster with image
                posterImageView.setImageResource(page.imageResource);
                posterContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing poster with image resource: " + page.imageResource);
            } else {
                // Hide poster
                posterContainer.setVisibility(View.GONE);
                Log.d(TAG, "Hiding poster - no image resource");
            }
            
            // Clear previous content
            contentContainer.removeAllViews();
            
            // Add content paragraph if exists
            if (page.content != null && !page.content.isEmpty()) {
                TextView contentText = new TextView(this);
                contentText.setText(page.content);
                contentText.setTextColor(getResources().getColor(R.color.black));
                contentText.setTextSize(14);
                contentText.setPadding(0, 0, 0, 24);
                contentContainer.addView(contentText);
            }
            
            // Add bullet points
            if (page.bulletPoints != null) {
                for (String bullet : page.bulletPoints) {
                    LinearLayout bulletLayout = createBulletPoint(bullet);
                    contentContainer.addView(bulletLayout);
                }
            }
            
            // Update pagination dots
            updatePaginationDots();
            
            // Update arrow button visibility (disabled - using swipe gestures)
            // updateArrowButtons();
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying current page: " + e.getMessage(), e);
        }
    }
    
    // Arrow buttons removed - using swipe gestures instead
    /*
    private void updateArrowButtons() {
        try {
            // Show/hide left arrow button
            if (leftArrowButton != null) {
                if (currentPage == 0) {
                    // First page - hide left arrow
                    leftArrowButton.setVisibility(View.GONE);
                } else {
                    // Not first page - show left arrow
                    leftArrowButton.setVisibility(View.VISIBLE);
                }
            }
            
            // Right arrow button is always visible
            // On last page it shows check icon (completion)
            if (rightArrowButton != null) {
                rightArrowButton.setVisibility(View.VISIBLE);
                
                if (pages != null && currentPage >= pages.size() - 1) {
                    // Last page - show check icon to indicate completion
                    rightArrowButton.setImageResource(R.drawable.ic_check);
                    rightArrowButton.setContentDescription("Complete and Go Back");
                    Log.d(TAG, "Right button changed to check icon (last page)");
                } else {
                    // Not last page - show right arrow icon
                    rightArrowButton.setImageResource(R.drawable.ic_chevron_right);
                    rightArrowButton.setContentDescription("Next Page");
                }
            }
            
            Log.d(TAG, "Arrow buttons updated - Page " + (currentPage + 1) + " of " + (pages != null ? pages.size() : 0));
        } catch (Exception e) {
            Log.e(TAG, "Error updating arrow buttons: " + e.getMessage(), e);
        }
    }
    */
    
    private LinearLayout createBulletPoint(String text) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(0, 0, 0, 16);
        
        // Create orange dot bullet
        View bulletDot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
            dpToPx(8), dpToPx(8)
        );
        dotParams.setMargins(0, dpToPx(6), 0, 0);
        bulletDot.setLayoutParams(dotParams);
        bulletDot.setBackgroundResource(R.drawable.orange_dot);
        
        // Create text view
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(14);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(dpToPx(12), 0, 0, 0);
        textView.setLayoutParams(textParams);
        
        layout.addView(bulletDot);
        layout.addView(textView);
        
        return layout;
    }
    
    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    private void updatePaginationDots() {
        // Reset all dots
        dot1.setBackgroundResource(R.drawable.circle_background);
        dot2.setBackgroundResource(R.drawable.circle_background);
        dot3.setBackgroundResource(R.drawable.circle_background);
        dot4.setBackgroundResource(R.drawable.circle_background);
        dot5.setBackgroundResource(R.drawable.circle_background);
        
        // Show/hide dots based on number of pages
        if (pages != null && pages.size() == 5) {
            // Show all 5 dots for Road Safety
            dot5.setVisibility(View.VISIBLE);
        } else {
            // Hide 5th dot for other safety types (4 pages)
            dot5.setVisibility(View.GONE);
        }
        
        // Highlight current page dot
        if (currentPage == 0) {
            dot1.setBackgroundResource(R.drawable.circle_orange_bg);
        } else if (currentPage == 1) {
            dot2.setBackgroundResource(R.drawable.circle_orange_bg);
        } else if (currentPage == 2) {
            dot3.setBackgroundResource(R.drawable.circle_orange_bg);
        } else if (currentPage == 3) {
            dot4.setBackgroundResource(R.drawable.circle_orange_bg);
        } else if (currentPage == 4) {
            dot5.setBackgroundResource(R.drawable.circle_orange_bg);
        }
    }
}


