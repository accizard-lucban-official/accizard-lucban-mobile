# UI/UX Standards for AcciZard Lucban Mobile App

This document outlines the UI/UX standards and guidelines for developing the AcciZard Lucban mobile application. Following these standards ensures consistency, accessibility, and a high-quality user experience.

---

## Table of Contents
1. [Platform Guidelines](#platform-guidelines)
2. [Design System](#design-system)
3. [Typography](#typography)
4. [Color & Theming](#color--theming)
5. [Spacing & Layout](#spacing--layout)
6. [Touch Targets & Interactions](#touch-targets--interactions)
7. [Navigation Patterns](#navigation-patterns)
8. [Accessibility](#accessibility)
9. [Components](#components)
10. [Animations & Transitions](#animations--transitions)
11. [Images & Icons](#images--icons)
12. [Forms & Input](#forms--input)
13. [Error Handling](#error-handling)
14. [Performance Standards](#performance-standards)
15. [Responsive Design](#responsive-design)

---

## Platform Guidelines

### Android Material Design 3
Our app follows **Material Design 3** (Material You) guidelines:
- Dynamic color theming support
- Adaptive layouts for various screen sizes
- Elevation and depth using shadows
- Material motion and transitions
- Material Components library

**Reference**: [Material Design 3](https://m3.material.io/)

---

## Design System

### Grid System
- **8dp base unit**: All spacing should be multiples of 8dp
- **4dp for fine-tuning**: Use 4dp increments for smaller adjustments
- **Consistent margins**: Standard screen margins are 16dp or 24dp

### Elevation Levels
- **Level 0 (0dp)**: Surface level
- **Level 1 (1dp)**: Raised buttons, cards
- **Level 2 (3dp)**: Floating action buttons (FAB)
- **Level 3 (6dp)**: Dialogs, modals
- **Level 4 (8dp)**: Navigation drawer
- **Level 5 (12dp)**: Snackbars

---

## Typography

### Font Family
- **Primary Font**: DM Sans (Variable or static TTF files)
- **Fallback**: Poppins (already in project)

### Type Scale (Material Design 3)

| Style | Font Size | Line Height | Weight | Use Case |
|-------|-----------|-------------|--------|----------|
| Display Large | 57sp | 64sp | Regular (400) | Hero text |
| Display Medium | 45sp | 52sp | Regular (400) | Large headers |
| Display Small | 36sp | 44sp | Regular (400) | Section headers |
| Headline Large | 32sp | 40sp | Regular (400) | Page titles |
| Headline Medium | 28sp | 36sp | Regular (400) | Screen titles |
| Headline Small | 24sp | 32sp | Regular (400) | Card headers |
| Title Large | 22sp | 28sp | Medium (500) | Toolbar titles |
| Title Medium | 16sp | 24sp | Medium (500) | List item titles |
| Title Small | 14sp | 20sp | Medium (500) | Subtitles |
| Body Large | 16sp | 24sp | Regular (400) | Main content |
| Body Medium | 14sp | 20sp | Regular (400) | Secondary content |
| Body Small | 12sp | 16sp | Regular (400) | Captions |
| Label Large | 14sp | 20sp | Medium (500) | Button text |
| Label Medium | 12sp | 16sp | Medium (500) | Input labels |
| Label Small | 11sp | 16sp | Medium (500) | Chips, tags |

### Typography Rules
- **Always use `sp`** for text sizes (scales with user preferences)
- **Minimum text size**: 12sp for any readable text
- **Line height**: Minimum 1.2x font size, ideal 1.5x
- **Maximum line length**: 60-75 characters for readability
- **Text alignment**: Left-aligned for most content (RTL-aware)

---

## Color & Theming

### Color System
Define colors in `colors.xml` following Material Design color roles:

```xml
<!-- Primary Colors -->
<color name="primary">#FF6B35</color>
<color name="onPrimary">#FFFFFF</color>
<color name="primaryContainer">#FFE5DD</color>

<!-- Secondary Colors -->
<color name="secondary">#4A90E2</color>
<color name="onSecondary">#FFFFFF</color>

<!-- Tertiary Colors -->
<color name="tertiary">#7CB342</color>

<!-- Background & Surface -->
<color name="background">#FFFBFF</color>
<color name="surface">#FFFFFF</color>
<color name="surfaceVariant">#F5F5F5</color>

<!-- Error -->
<color name="error">#BA1A1A</color>
<color name="onError">#FFFFFF</color>

<!-- Outline -->
<color name="outline">#79747E</color>
```

### Color Contrast Standards (WCAG 2.1)
- **Normal text (14sp+)**: Minimum 4.5:1 contrast ratio
- **Large text (18sp+ or 14sp+ bold)**: Minimum 3:1 contrast ratio
- **UI components**: Minimum 3:1 contrast ratio
- **Non-text contrast**: 3:1 for icons, borders, focus indicators

### Dark Theme Support
- Provide dark theme variants in `values-night/themes.xml`
- Reduce white to prevent eye strain (#121212 instead of #000000)
- Use surface elevation overlays
- Reduce opacity of shadows in dark mode

### Semantic Colors
```xml
<!-- Status Colors -->
<color name="success">#4CAF50</color>
<color name="warning">#FFC107</color>
<color name="error">#F44336</color>
<color name="info">#2196F3</color>
```

---

## Spacing & Layout

### Standard Spacing Scale
Use consistent spacing throughout the app:
- **4dp**: Minimal spacing between tightly related elements
- **8dp**: Small spacing, padding within components
- **12dp**: Medium-small spacing
- **16dp**: Default spacing between elements, screen margins
- **24dp**: Large spacing between sections
- **32dp**: Extra large spacing
- **48dp**: Section dividers
- **64dp**: Major section spacing

### Layout Margins
- **Screen edge margins**: 16dp (phone), 24dp (tablet)
- **Card padding**: 16dp
- **Dialog padding**: 24dp
- **List item padding**: 16dp horizontal, 12dp vertical

### Content Width
- **Maximum content width**: 600dp (for readability on tablets)
- **Form maximum width**: 480dp
- **Use ConstraintLayout** for complex layouts
- **Use LinearLayout** for simple stacked layouts

---

## Touch Targets & Interactions

### Touch Target Sizes
- **Minimum touch target**: 48dp × 48dp (Material Design standard)
- **Recommended for important actions**: 56dp × 56dp
- **Spacing between targets**: Minimum 8dp

### Visual Touch Feedback
- **Ripple effect**: All touchable elements must have ripple feedback
- **State changes**: Visual indication for pressed, focused, disabled states
- **Elevation changes**: Buttons should respond with elevation changes

### Gesture Support
- **Tap**: Primary interaction
- **Long press**: Context actions, additional options
- **Swipe**: Navigation, dismiss actions (e.g., swipe to call)
- **Pinch-to-zoom**: Map view, image viewing
- **Pull-to-refresh**: List updates

---

## Navigation Patterns

### Bottom Navigation
- **3-5 destinations**: No more, no less
- **Height**: 56dp (phone), 64dp (tablet)
- **Icons + labels**: Always show both for clarity
- **Active state**: Clear visual indicator
- **Badge support**: For notifications/updates

### Top App Bar (Toolbar)
- **Height**: 56dp (phone), 64dp (tablet)
- **Title**: Title Medium typography (16sp)
- **Navigation icon**: 24dp icon with 16dp padding
- **Action icons**: Maximum 3 visible actions
- **Overflow menu**: For additional actions

### Navigation Hierarchy
- **Depth limit**: Maximum 3-4 levels deep
- **Back navigation**: Consistent back button behavior
- **Up navigation**: Navigate to parent screen
- **Deep linking**: Support for direct navigation

### Transitions
- **Forward navigation**: Slide from right
- **Backward navigation**: Slide to right
- **Duration**: 300ms standard, 200ms fast

---

## Accessibility

### WCAG 2.1 Level AA Compliance

#### Content Descriptions
- All images must have `contentDescription`
- Icons must have meaningful descriptions
- Decorative images: `contentDescription=""`

```xml
<!-- Example -->
<ImageView
    android:contentDescription="@string/profile_picture"
    android:src="@drawable/ic_profile" />
```

#### Focus Order
- Logical reading order (left-to-right, top-to-bottom)
- Use `android:nextFocusDown`, `android:nextFocusUp` for custom order

#### Screen Reader Support (TalkBack)
- Test all flows with TalkBack enabled
- Group related content with `android:screenReaderFocusable`
- Announce important changes with `AccessibilityEvent`

#### Touch Targets
- Minimum 48dp × 48dp for all interactive elements
- Adequate spacing between touch targets

#### Color Usage
- Never rely solely on color to convey information
- Provide text labels or icons in addition to color
- Support high contrast mode

#### Text Scaling
- Support dynamic font sizes (use `sp` units)
- Test with 200% text scaling
- Ensure layouts don't break with large text

#### Keyboard Navigation
- Support hardware keyboard navigation
- Visible focus indicators

---

## Components

### Buttons

#### Primary Button (Filled)
```xml
<Button
    android:layout_width="wrap_content"
    android:layout_height="56dp"
    android:minWidth="120dp"
    android:textSize="14sp"
    android:fontFamily="@font/dmsans_medium"
    style="@style/Widget.Material3.Button" />
```

#### Secondary Button (Outlined)
```xml
<Button
    style="@style/Widget.Material3.Button.OutlinedButton" />
```

#### Text Button
```xml
<Button
    style="@style/Widget.Material3.Button.TextButton" />
```

**Button Guidelines:**
- Height: 40dp (small), 48dp (medium), 56dp (large)
- Minimum width: 64dp
- Padding: 16dp horizontal, 8dp vertical
- Corner radius: 20dp (fully rounded)
- Text: Label Large (14sp, Medium weight)

### Cards
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardElevation="2dp"
    app:cardCornerRadius="12dp"
    android:layout_margin="8dp">
    
    <!-- Card content -->
    
</com.google.android.material.card.MaterialCardView>
```

**Card Guidelines:**
- Elevation: 1-2dp default
- Corner radius: 12dp standard, 16dp for larger cards
- Padding: 16dp internal padding
- Margin: 8dp between cards

### Text Fields (Input Fields)
```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:boxCornerRadiusTopStart="8dp"
    app:boxCornerRadiusTopEnd="8dp"
    app:boxCornerRadiusBottomStart="8dp"
    app:boxCornerRadiusBottomEnd="8dp">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:textSize="16sp" />
        
</com.google.android.material.textfield.TextInputLayout>
```

**Text Field Guidelines:**
- Height: 56dp standard
- Corner radius: 8dp
- Label: Label Medium (12sp)
- Input text: Body Large (16sp)
- Error text: Body Small (12sp)

### Floating Action Button (FAB)
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/ic_add"
    app:fabSize="normal" />
```

**FAB Guidelines:**
- Size: 56dp × 56dp (normal), 40dp × 40dp (mini)
- Position: 16dp from screen edges
- Elevation: 6dp
- Icon size: 24dp

### Dialogs
```xml
<!-- Alert Dialog -->
android:layout_width="280dp"
android:minHeight="120dp"
app:cornerRadius="28dp"
```

**Dialog Guidelines:**
- Width: 280dp (phone), 560dp (tablet)
- Corner radius: 28dp
- Padding: 24dp
- Title: Headline Small (24sp)
- Body: Body Medium (14sp)
- Buttons: Right-aligned, 8dp spacing

### Bottom Sheets
**Bottom Sheet Guidelines:**
- Corner radius: 28dp (top corners only)
- Drag handle: Optional, 32dp × 4dp
- Peek height: 56dp minimum
- Full height: 90% of screen maximum

### Snackbars
```xml
<!-- Snackbar height: 48-56dp -->
<!-- Duration: Short (4s), Long (10s), Indefinite -->
```

**Snackbar Guidelines:**
- Position: Bottom of screen, above navigation
- Padding: 16dp
- Elevation: 6dp
- Action button: Optional, right-aligned

---

## Animations & Transitions

### Animation Duration Guidelines
- **Fast**: 100ms - Minor changes (button state)
- **Standard**: 200-300ms - Most transitions
- **Slow**: 400-500ms - Large layout changes
- **Very slow**: 500ms+ - Only for special effects

### Animation Types
- **Fade**: Appearing/disappearing content
- **Slide**: Navigation transitions
- **Scale**: Buttons, FAB interactions
- **Elevation**: Card interactions

### Easing Curves
- **Standard**: Most animations (cubic-bezier)
- **Decelerate**: Entering elements
- **Accelerate**: Exiting elements

### Implementation Example
```xml
<!-- res/anim/fade_in.xml -->
<alpha
    android:fromAlpha="0.0"
    android:toAlpha="1.0"
    android:duration="300"
    android:interpolator="@android:anim/decelerate_interpolator" />
```

---

## Images & Icons

### Icon Sizes
- **System icons**: 24dp × 24dp
- **Bottom navigation icons**: 24dp × 24dp
- **Toolbar icons**: 24dp × 24dp
- **List item icons**: 24dp × 24dp (small), 40dp × 40dp (medium), 56dp × 56dp (large)
- **Avatar**: 40dp × 40dp (small), 56dp × 56dp (medium), 72dp × 72dp (large)

### Icon Types
- **Filled**: Default state, selected items
- **Outlined**: Unselected items
- **Vector drawables**: Preferred over raster images
- **Material Icons**: Use Material Symbols

### Image Guidelines
- **Format**: WebP (preferred), PNG, JPEG
- **Optimization**: Compress all images
- **Density**: Provide mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
- **Aspect ratios**: 16:9 (landscape), 1:1 (square), 4:3 (standard)

### Profile Pictures
- **Size**: 40dp (small), 56dp (medium), 72dp (large), 120dp (profile page)
- **Shape**: Circle (CircleImageView or ShapeableImageView)
- **Placeholder**: Default avatar icon
- **Border**: Optional 2dp stroke

---

## Forms & Input

### Form Layout
- **Single column**: One input per line on mobile
- **Label position**: Above input field or floating
- **Field spacing**: 16dp between fields
- **Group spacing**: 24dp between field groups

### Input Validation
- **Real-time validation**: Validate on blur or after typing stops
- **Inline errors**: Show errors below the field
- **Success indicators**: Optional green checkmark
- **Required fields**: Asterisk (*) or "Required" label

### Input Field Types
```xml
<!-- Text input -->
android:inputType="text"

<!-- Email -->
android:inputType="textEmailAddress"

<!-- Password -->
android:inputType="textPassword"

<!-- Phone -->
android:inputType="phone"

<!-- Number -->
android:inputType="number"
```

### Auto-Complete & Suggestions
- Provide auto-complete where possible
- Show recent/popular suggestions
- Allow clearing suggestions

### Form Submission
- **Loading state**: Disable form and show progress
- **Success**: Navigate to confirmation or show success message
- **Error**: Show specific error messages, keep data

---

## Error Handling

### Error Message Guidelines
- **Be specific**: "Email is already registered" not "Error"
- **Be helpful**: Provide solution or next steps
- **Be human**: Friendly, non-technical language
- **Be concise**: Keep messages short

### Error Display Methods

#### Inline Errors (Preferred)
```xml
<com.google.android.material.textfield.TextInputLayout
    app:errorEnabled="true"
    app:error="Please enter a valid email address">
```

#### Snackbar (Non-critical)
```java
Snackbar.make(view, "Unable to load data", Snackbar.LENGTH_LONG)
    .setAction("RETRY", retryListener)
    .show();
```

#### Dialog (Critical)
```java
new MaterialAlertDialogBuilder(context)
    .setTitle("Connection Error")
    .setMessage("Unable to connect to server. Please check your internet connection.")
    .setPositiveButton("Retry", retryListener)
    .setNegativeButton("Cancel", null)
    .show();
```

### Empty States
- **Illustration**: Friendly, relevant graphic
- **Title**: Clear explanation (Body Large)
- **Description**: What to do next (Body Medium)
- **Action button**: Primary action to resolve

### Loading States
- **Progress bar**: For determinate progress (0-100%)
- **Spinner**: For indeterminate progress
- **Skeleton screens**: For content loading
- **Shimmer effect**: Placeholder loading animation

---

## Performance Standards

### Loading Times
- **Initial load**: < 3 seconds
- **Navigation**: < 1 second between screens
- **API response**: < 2 seconds with loading indicator

### Frame Rate
- **Target**: 60 FPS (16ms per frame)
- **Minimum**: 30 FPS for smooth experience
- **Avoid**: Janky animations, dropped frames

### Image Loading
- **Lazy loading**: Load images as needed
- **Placeholder**: Show placeholder while loading
- **Caching**: Cache loaded images (Glide/Picasso)
- **Compression**: Optimize image sizes

### Memory Management
- **Avoid memory leaks**: Properly cleanup listeners, callbacks
- **Image recycling**: Recycle bitmaps when done
- **Background tasks**: Use proper lifecycle management

---

## Responsive Design

### Screen Size Categories
- **Phone**: 320dp - 600dp width
- **Tablet (7")**: 600dp - 840dp width
- **Tablet (10")**: 840dp+ width

### Breakpoints
Define dimensions for different screen sizes:

```xml
<!-- values/dimens.xml (default - phones) -->
<dimen name="screen_margin">16dp</dimen>

<!-- values-sw600dp/dimens.xml (tablets) -->
<dimen name="screen_margin">24dp</dimen>

<!-- values-sw720dp/dimens.xml (large tablets) -->
<dimen name="screen_margin">32dp</dimen>
```

### Layout Strategies
- **Phone portrait**: Single column, vertical scrolling
- **Phone landscape**: Optimized horizontal layout
- **Tablet portrait**: Two-column where appropriate
- **Tablet landscape**: Master-detail layouts

### Orientation Support
- **Portrait**: Primary orientation
- **Landscape**: Provide optimized layouts
- **Lock orientation**: Only when necessary (e.g., camera)

---

## Testing Checklist

### Device Testing
- [ ] Test on multiple screen sizes (small, normal, large, xlarge)
- [ ] Test on different Android versions (API 21+)
- [ ] Test in portrait and landscape orientations
- [ ] Test with different font sizes (Settings → Display → Font size)

### Accessibility Testing
- [ ] Test with TalkBack enabled
- [ ] Test with high contrast mode
- [ ] Test keyboard navigation
- [ ] Test with 200% text scaling

### Visual Testing
- [ ] Check color contrast ratios
- [ ] Verify dark theme implementation
- [ ] Check all touch targets are 48dp minimum
- [ ] Verify consistent spacing and alignment

### Interaction Testing
- [ ] Test all gestures (tap, long press, swipe)
- [ ] Verify ripple effects on all touchable elements
- [ ] Test loading states
- [ ] Test error states
- [ ] Test empty states

### Performance Testing
- [ ] Monitor frame rate during animations
- [ ] Test app performance on low-end devices
- [ ] Test with slow network connection
- [ ] Check for memory leaks

---

## Resources & References

### Official Documentation
- [Material Design 3](https://m3.material.io/)
- [Android Design Guidelines](https://developer.android.com/design)
- [Material Components Android](https://github.com/material-components/material-components-android)

### Accessibility
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)

### Tools
- **Color Contrast Checker**: [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- **Icon Resources**: [Material Symbols](https://fonts.google.com/icons)
- **Font Resources**: [Google Fonts](https://fonts.google.com/)

### Design Tools
- **Figma**: For mockups and prototypes
- **Material Theme Builder**: For color palette generation
- **Android Studio Layout Inspector**: For debugging layouts

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-10-20 | Initial documentation created |

---

## Contributing

When making UI/UX changes to the app:
1. Reference this document for standards
2. Update this document if new patterns are established
3. Document any deviations with rationale
4. Review changes for accessibility compliance

---

**Document Maintained By**: Development Team  
**Last Updated**: October 20, 2025  
**Next Review**: Quarterly













































