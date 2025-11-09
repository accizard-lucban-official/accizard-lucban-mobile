package com.example.accizardlucban;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TermAndConditionsActivity extends AppCompatActivity {

    private ImageView backButton;
    private ScrollView scrollView;

    // Table of Contents TextViews
    private TextView tocEffectiveDate, tocDevelopedBy, tocAboutApp, tocUserResponsibilities,
            tocPrivacyPolicy, tocLocationServices, tocAccountSuspension, tocDisclaimers,
            tocChangesTerms, tocGoverningLaw, tocContactUs, tocBusinessTransfers,
            tocExternalLinksDisclaimer, tocUseOfLocationServices, tocAccountSuspensionOrTermination,
            tocDisclaimersAndLimitations, tocChangeToTheTerms, tocGoverningLaw1;

    // Content Section LinearLayouts
    private LinearLayout sectionEffectiveDate, sectionDevelopedBy, sectionAboutApp,
            sectionUserResponsibilities, sectionPrivacyPolicy, sectionLocationServices,
            sectionAccountSuspension, sectionDisclaimers, sectionChangesTerms,
            sectionGoverningLaw, sectionContactUs, sectionBusinessTransfers,
            sectionExternalLinksDisclaimer, sectionUseOfLocationServices,
            sectionAccountSuspensionOrTermination, sectionDisclaimersAndLimitations,
            sectionChangeToTheTerms, sectionGoverningLaw1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        initializeViews();
        setupClickListeners();
        setupBackButton();
    }

    private void initializeViews() {
        // Initialize back button and scroll view
        backButton = findViewById(R.id.back_button_terms);
        scrollView = findViewById(R.id.scroll_view);

        // Initialize Table of Contents TextViews
        tocEffectiveDate = findViewById(R.id.toc_effective_date);
        tocDevelopedBy = findViewById(R.id.toc_developed_by);
        tocAboutApp = findViewById(R.id.toc_about_app);
        tocUserResponsibilities = findViewById(R.id.toc_user_responsibilities);
        tocPrivacyPolicy = findViewById(R.id.toc_privacy_policy);
        tocLocationServices = findViewById(R.id.toc_location_services);
        tocAccountSuspension = findViewById(R.id.toc_account_suspension);
        tocDisclaimers = findViewById(R.id.toc_disclaimers);
        tocChangesTerms = findViewById(R.id.toc_changes_terms);
        tocGoverningLaw = findViewById(R.id.toc_governing_law);
        tocContactUs = findViewById(R.id.toc_contact_us);
        tocBusinessTransfers = findViewById(R.id.toc_business_tranfers);
        tocExternalLinksDisclaimer = findViewById(R.id.toc_external_links_disclaimer);
        tocUseOfLocationServices = findViewById(R.id.toc_use_of_location_services);
        tocAccountSuspensionOrTermination = findViewById(R.id.toc_account_suspension_or_termination);
        tocDisclaimersAndLimitations = findViewById(R.id.toc_discalaimer_and_limitations);
        tocChangeToTheTerms = findViewById(R.id.toc_change_to_the_terms);
        tocGoverningLaw1 = findViewById(R.id.toc_governing_law1);

        // Initialize Content Section LinearLayouts
        sectionEffectiveDate = findViewById(R.id.section_effective_date);
        sectionDevelopedBy = findViewById(R.id.section_developed_by);
        sectionAboutApp = findViewById(R.id.section_about_app);
        sectionUserResponsibilities = findViewById(R.id.section_user_responsibilities);
        sectionPrivacyPolicy = findViewById(R.id.section_privacy_policy);
        sectionLocationServices = findViewById(R.id.section_location_services);
        sectionAccountSuspension = findViewById(R.id.section_account_suspension);
        sectionDisclaimers = findViewById(R.id.section_disclaimers);
        sectionChangesTerms = findViewById(R.id.section_changes_terms);
        sectionGoverningLaw = findViewById(R.id.section_governing_law);
        sectionContactUs = findViewById(R.id.section_contact_us);
        sectionBusinessTransfers = findViewById(R.id.section_business_transfers);
        sectionExternalLinksDisclaimer = findViewById(R.id.section_external_links_disclaimer);
        sectionUseOfLocationServices = findViewById(R.id.section_use_of_location_services);
        sectionAccountSuspensionOrTermination = findViewById(R.id.section_account_suspension_or_termination);
        sectionDisclaimersAndLimitations = findViewById(R.id.section_disclaimers_and_limitations);
        sectionChangeToTheTerms = findViewById(R.id.section_change_to_the_terms);
        sectionGoverningLaw1 = findViewById(R.id.section_governing_law1);
    }

    private void setupClickListeners() {
        // Set click listeners for table of contents items
        if (tocEffectiveDate != null) {
            tocEffectiveDate.setOnClickListener(v -> scrollToSection(sectionEffectiveDate, "Effective Date"));
        }
        if (tocDevelopedBy != null) {
            tocDevelopedBy.setOnClickListener(v -> scrollToSection(sectionDevelopedBy, "Developed by"));
        }
        if (tocAboutApp != null) {
            tocAboutApp.setOnClickListener(v -> scrollToSection(sectionAboutApp, "About the Application"));
        }
        if (tocUserResponsibilities != null) {
            tocUserResponsibilities.setOnClickListener(v -> scrollToSection(sectionUserResponsibilities, "User Responsibilities"));
        }
        if (tocPrivacyPolicy != null) {
            tocPrivacyPolicy.setOnClickListener(v -> scrollToSection(sectionPrivacyPolicy, "Intellectual Property and Limited License"));
        }
        if (tocLocationServices != null) {
            tocLocationServices.setOnClickListener(v -> scrollToSection(sectionLocationServices, "Prohibited Uses"));
        }
        if (tocAccountSuspension != null) {
            tocAccountSuspension.setOnClickListener(v -> scrollToSection(sectionAccountSuspension, "Termination of License"));
        }
        if (tocDisclaimers != null) {
            tocDisclaimers.setOnClickListener(v -> scrollToSection(sectionDisclaimers, "Disclaimer of Warranties"));
        }
        if (tocChangesTerms != null) {
            tocChangesTerms.setOnClickListener(v -> scrollToSection(sectionChangesTerms, "Cookies and Device Information"));
        }
        if (tocGoverningLaw != null) {
            tocGoverningLaw.setOnClickListener(v -> scrollToSection(sectionGoverningLaw, "Data Collection and Privacy Policy"));
        }
        if (tocBusinessTransfers != null) {
            tocBusinessTransfers.setOnClickListener(v -> scrollToSection(sectionBusinessTransfers, "Business Transfers"));
        }
        if (tocExternalLinksDisclaimer != null) {
            tocExternalLinksDisclaimer.setOnClickListener(v -> scrollToSection(sectionExternalLinksDisclaimer, "External Links Disclaimer"));
        }
        if (tocUseOfLocationServices != null) {
            tocUseOfLocationServices.setOnClickListener(v -> scrollToSection(sectionUseOfLocationServices, "Use of Location Services"));
        }
        if (tocAccountSuspensionOrTermination != null) {
            tocAccountSuspensionOrTermination.setOnClickListener(v -> scrollToSection(sectionAccountSuspensionOrTermination, "Account Suspension or Termination"));
        }
        if (tocDisclaimersAndLimitations != null) {
            tocDisclaimersAndLimitations.setOnClickListener(v -> scrollToSection(sectionDisclaimersAndLimitations, "Disclaimers and Limitations"));
        }
        if (tocChangeToTheTerms != null) {
            tocChangeToTheTerms.setOnClickListener(v -> scrollToSection(sectionChangeToTheTerms, "Change to the Terms"));
        }
        if (tocGoverningLaw1 != null) {
            tocGoverningLaw1.setOnClickListener(v -> scrollToSection(sectionGoverningLaw1, "Governing Law"));
        }
        if (tocContactUs != null) {
            tocContactUs.setOnClickListener(v -> scrollToSection(sectionContactUs, "Contact Us"));
        }

        // Add click feedback animations
        addClickFeedback(tocEffectiveDate);
        addClickFeedback(tocDevelopedBy);
        addClickFeedback(tocAboutApp);
        addClickFeedback(tocUserResponsibilities);
        addClickFeedback(tocPrivacyPolicy);
        addClickFeedback(tocLocationServices);
        addClickFeedback(tocAccountSuspension);
        addClickFeedback(tocDisclaimers);
        addClickFeedback(tocChangesTerms);
        addClickFeedback(tocGoverningLaw);
        addClickFeedback(tocBusinessTransfers);
        addClickFeedback(tocExternalLinksDisclaimer);
        addClickFeedback(tocUseOfLocationServices);
        addClickFeedback(tocAccountSuspensionOrTermination);
        addClickFeedback(tocDisclaimersAndLimitations);
        addClickFeedback(tocChangeToTheTerms);
        addClickFeedback(tocGoverningLaw1);
        addClickFeedback(tocContactUs);
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Add click animation
                animateButtonClick(backButton);

                // Close activity after animation
                backButton.postDelayed(() -> {
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }, 150);
            });
        }
    }

    private void scrollToSection(LinearLayout targetSection, String sectionName) {
        if (targetSection != null && scrollView != null) {
            // Post to ensure layout is complete
            scrollView.post(() -> {
                // Calculate the position of the target section
                int[] location = new int[2];
                targetSection.getLocationOnScreen(location);

                // Get scroll view location
                int[] scrollLocation = new int[2];
                scrollView.getLocationOnScreen(scrollLocation);

                // Calculate the scroll position (adjust for header offset)
                int scrollY = Math.max(0, location[1] - scrollLocation[1] - 100); // 100px offset from top

                // Smooth scroll to the section
                ObjectAnimator scrollAnimator = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getScrollY(), scrollY);
                scrollAnimator.setDuration(800);
                scrollAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                scrollAnimator.start();

                // Show toast feedback
                showToast("Navigating to " + sectionName);

                // Highlight the section briefly
                highlightSection(targetSection);
            });
        }
    }

    private void highlightSection(LinearLayout section) {
        if (section != null) {
            // Create a brief highlight animation
            int originalColor = ContextCompat.getColor(this, android.R.color.transparent);
            int highlightColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);

            // Set highlight color
            section.setBackgroundColor(highlightColor);

            // Fade out the highlight after a short delay
            section.postDelayed(() -> {
                ObjectAnimator fadeOut = ObjectAnimator.ofArgb(section, "backgroundColor", highlightColor, originalColor);
                fadeOut.setDuration(1000);
                fadeOut.start();
            }, 500);
        }
    }

    private void addClickFeedback(TextView textView) {
        if (textView != null) {
            textView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Scale down slightly when pressed
                        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.95f);
                        scaleDown.setDuration(100);
                        scaleDown.start();

                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.95f);
                        scaleDownY.setDuration(100);
                        scaleDownY.start();
                        break;

                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        // Scale back to normal
                        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(v, "scaleX", 0.95f, 1.0f);
                        scaleUp.setDuration(100);
                        scaleUp.start();

                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 0.95f, 1.0f);
                        scaleUpY.setDuration(100);
                        scaleUpY.start();
                        break;
                }
                return false; // Let the click event continue
            });
        }
    }

    private void animateButtonClick(View button) {
        if (button != null) {
            // Create a scale animation for button press feedback
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.9f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.9f, 1.0f);

            scaleX.setDuration(150);
            scaleY.setDuration(150);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            scaleX.start();
            scaleY.start();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Add custom back button behavior with animation
        if (backButton != null) {
            animateButtonClick(backButton);

            backButton.postDelayed(() -> {
                super.onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }, 150);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Scroll to top when activity resumes
        if (scrollView != null) {
            scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
        }
    }
}