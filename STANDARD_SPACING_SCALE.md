# Standard Spacing Scale Documentation

## Overview

This document defines the standard spacing and margin values for the AcciZard Lucban mobile application. These values follow Material Design 3 guidelines and ensure consistency across all screens and components.

---

## Base Grid System

### 8dp Base Unit
- **All spacing should be multiples of 8dp** for consistency
- **4dp increments** can be used for fine-tuning when necessary
- This creates a cohesive visual rhythm throughout the app

---

## Standard Spacing Scale

| Value | Dimension Resource | Use Case | Examples |
|-------|-------------------|----------|----------|
| **4dp** | `spacing_tiny`, `margin_xxsmall` | Minimal spacing between tightly related elements | Icon and label spacing, checkbox label spacing |
| **8dp** | `spacing_small`, `margin_tiny`, `margin_small` | Small spacing, padding within components | Button padding, list item padding, icon spacing |
| **12dp** | `spacing_medium`, `margin_medium` | Medium-small spacing | Form field groups, card internal spacing |
| **16dp** | `spacing_large`, `margin_large` | **Default spacing** between elements, screen margins | Screen edge margins, field spacing, card padding |
| **20dp** | `spacing_xlarge`, `margin_xlarge` | Large-medium spacing | Section headers, button groups |
| **24dp** | `spacing_xxlarge`, `margin_xxlarge`, `padding_xxlarge` | **Large spacing** between sections | Dialog padding, section dividers, toolbar padding |
| **32dp** | `spacing_xxxlarge`, `margin_xxxlarge` | Extra large spacing | Major section spacing, large dialogs |
| **48dp** | - | Section dividers, touch target minimum | Minimum touch target size, major breaks |
| **64dp**ăr | - | Major section spacing | Page-level breaks, footer spacing |

---

## Screen-Specific Spacing

### Screen Edge Margins
- **Phones**: `16dp` (`margin_large`)
- **Tablets (7")**: `24dp` (`margin_xxlarge`)
- **Large Tablets (10")**: `32dp` (`margin_xxxlarge`)

### Content Padding
- **Screen content**: `16dp` - `24dp` horizontal padding
- **Form containers**: `24dp` padding (`padding_xxlarge`)
- **Card content**: `16dp` padding (`padding_large`)

---

## Component-Specific Spacing

### Buttons
- **Button height**: `48dp` (medium), `56dp` (large)
- **Button padding**: `16dp` horizontal, `8dp` vertical
- **Spacing between buttons**: Minimum `8dp`, recommended `16dp`
- **Button margin**: `16dp` from adjacent elements

### Input Fields
- **Field height**: `48dp` (medium), `56dp` (large)
- **Field padding**: `16dp` horizontal (`padding_large`)
- **Spacing between fields**: `16dp` (`margin_large`)
- **Spacing between field groups**: `24dp` (`margin_xxlarge`)
- **Label-to-field spacing**: `8dp` (`margin_tiny`)

### Cards
- **Card padding**: `16dp` (`padding_large`)
- **Card margin**: `8dp` between cards (`margin_small`)
- **Card corner radius**: `12dp` (standard)
- **Card elevation**: `2dp` - `4dp`

### Lists
- **List item padding**: `16dp` horizontal, `12dp` vertical
- **List item height**: `48dp` (small), `56dp` (medium), `64dp` (large)
- **Spacing between items**: `8dp` - `12dp`
- **List margin**: `16dp` from screen edges

### Dialogs
- **Dialog padding**: `24dp` (`padding_xxlarge`)
- **Dialog width**: `280dp` (phone), `560dp` (tablet)
- **Dialog corner radius**: `28dp`
- **Button spacing in dialogs**: `8dp`

### Navigation
- **Toolbar height**: `56dp` (phone), `64dp` (tablet)
- **Toolbar padding**: `16dp` horizontal
- **Bottom navigation height**: `56dp` (phone), `64dp` (tablet)
- **Navigation icon padding**: `16dp` around icons

---

## Touch Target Spacing

### Minimum Requirements
- **Minimum touch target**: `48dp × 48dp` (Material Design standard)
- **Recommended touch target**: `56dp × 56dp` for important actions
- **Spacing between touch targets**: Minimum `8dp`, recommended `16dp`

### Visual Touch Feedback
- All touchable elements must have adequate spacing
- Avoid placing touch targets closer than `8dp` apart
- Use `16dp` spacing for better usability

---

## Typography Spacing

### Text Element Spacing
- **Title spacing**: `24dp` bottom margin (`margin_xxlarge`)
- **Subtitle spacing**: `16dp` bottom margin (`margin_large`)
- **Body text spacing**: `12dp` - `16dp` between paragraphs
- **Label spacing**: `8dp` above input field (`margin_tiny`)

### Line Height Guidelines
- **Minimum line height**: 1.2x font size
- **Ideal line height**: 1.5x font size
- **Maximum line length**: 60-75 characters for readability

---

## Layout Spacing Patterns

### Vertical Spacing Pattern
```
Screen Edge (16dp)
  └─ Section Header
      └─ Spacing (24dp)
          └─ Content
              └─ Spacing (16dp)
                  └─ Next Element
                      └─ Spacing (16dp)
                          └─ Next Element
                              └─ Spacing (24dp)
                                  └─ Next Section
```

### Form Spacing Pattern
```
Section Title (24dp margin bottom)
  ├─ Field Label (8dp margin bottom)
  ├─ Input Field (16dp margin bottom)
  ├─ Field Label (8dp margin bottom)
  ├─ Input Field (24dp margin bottom) [end of group]
  └─ Next Field Group
```

---

## Responsive Spacing

### Breakpoints
- **Small screens** (320dp - 600dp): Use standard phone spacing (`16dp` margins)
- **Tablets** (600dp - 840dp): Increase to `24dp` margins
- **Large tablets** (840dp+): Use `32dp` margins

### Orientation Changes
- **Portrait**: Use standard spacing values
- **Landscape**: Maintain same spacing, adjust layout structure
- **Dialogs**: Scale proportionally but maintain minimum padding

---

## Usage Guidelines

### When to Use Each Spacing Value

#### 4dp - Minimal Spacing
- Icon and text pairing
- Checkbox/radio button labels
- Small badge spacing
- Border widths

#### 8dp - Small Spacing
- Button internal padding
- List item padding (vertical)
- Icon spacing
- Touch target minimum spacing

#### 12dp - Medium Spacing
- Related form fields
- Card internal elements
- List items grouping

#### 16dp - Default Spacing ⭐
- Screen edge margins (phones)
- Between major UI elements
- Input field spacing
- Card padding
- Button margins

#### 24dp - Large Spacing ⭐
- Section breaks
- Dialog padding
- Screen edge margins (tablets)
- Form field group spacing
- Toolbar padding

#### 32dp - Extra Large Spacing
- Major section spacing
- Large dialog spacing
- Screen edge margins (large tablets)
- Footer spacing

---

## Best Practices

### ✅ DO's

1. **Always use dimension resources** from `dimens.xml` instead of hardcoded values
   ```xml
   <!-- Good -->
   android:layout_marginBottom="@dimen/margin_large"
   
   <!-- Bad -->
   android:layout_marginBottom="16dp"
   ```

2. **Maintain consistency** - Use the same spacing value for similar elements
3. **Use multiples of 8dp** - Stick to the 8dp grid system
4. **Increase spacing for tablets** - Use larger margins on bigger screens
5. **Group related elements** - Use smaller spacing (8dp-12dp) for related items
6. **Separate sections** - Use larger spacing (24dp-32dp) between major sections

### ❌ DON'Ts

1. **Don't use indifference spacing** - Avoid arbitrary values like `13dp`, `19dp`, etc.
2. **Don't mix spacing patterns** - Be consistent within the same screen
3. **Don't ignore touch targets** - Maintain minimum 48dp × 48dp
4. **Don't overcrowd** - Maintain breathing room between elements
5. **Don't hardcode values** - Always use dimension resources

---

## Dimension Resources Reference

### Spacing Resources
```xml
<dimen name="spacing_tiny">4dp</dimen>
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">12dp</dimen>
<dimen name="spacing_large">16dp</dimen>
<dimen name="spacing_xlarge">20dp</dimen>
<dimen name="spacing_xxlarge">24dp</dimen>
<dimen name="spacing_xxxlarge">32dp</dimen>
```

### Margin Resources
```xml
<dimen name="margin_xxsmall">4dp</dimen>
<dimen name="margin_tiny">8dp</dimen>
<dimen name="margin_small">8dp</dimen>
<dimen name="margin_medium">12dp</dimen>
<dimen name="margin_large">16dp</dimen>
<dimen name="margin_xlarge">20dp</dimen>
<dimen name="margin_xxlarge">24dp</dimen>
<dimen name="margin_xxxlarge">32dp</dimen>
```

### Padding Resources
```xml
<dimen name="padding_tiny">4dp</dimen>
<dimen name="padding_small">8dp</dimen>
<dimen name="padding_medium">12dp</dimen>
<dimen name="padding_large">16dp</dimen>
<dimen name="padding_xlarge">20dp</dimen>
<dimen name="padding_xxlarge">24dp</dimen>
<dimen name="padding_xxxlarge">32dp</dimen>
```

---

## Quick Reference Cheat Sheet

```
Screen Edge Margin:       16dp (phone) / 24dp (tablet)
Button Height:            48dp (medium) / 56dp (large)
Input Field Height:       48dp (medium) / 56dp (large)
Card Padding:             16dp
Dialog Padding:           24dp
Touch Target Minimum:     48dp × 48dp
Touch Target Spacing:     8dp minimum

Field Spacing:            16dp
Field Group Spacing:      24dp
Section Spacing:          24dp - 32dp
Label to Field:           8dp

List Item Padding:        16dp horizontal, 12dp vertical
Icon Spacing:             8dp - 16dp
```

---

## Examples

### Example 1: Login Form
```xml
<!-- Section title -->
<TextView
    android:layout_marginBottom="@dimen/margin_xxlarge" /> <!-- 24dp -->

<!-- Email label -->
<TextView
    android:layout_marginBottom="@dimen/margin_tiny" /> <!-- 8dp -->

<!-- Email input -->
<EditText
    android:layout_marginBottom="@dimen/margin_large" /> <!-- 16dp -->

<!-- Password label -->
<TextView
    android:layout_marginBottom="@dimen/margin_tiny" /> <!-- 8dp -->

<!-- Password input -->
<EditText
    android:layout_marginBottom="@dimen/margin_large" /> <!-- 16dp -->

<!-- Submit button -->
<Button
    android:layout_marginTop="@dimen/margin_xxlarge" /> <!-- 24dp -->
```

### Example 2: Card Layout
```xml
<CardView
    android:layout_margin="@dimen/margin_small"> <!-- 8dp -->
    
    <LinearLayout
        android:padding="@dimen/padding_large"> <!-- 16dp -->
        
        <!-- Card content -->
        
    </LinearLayout>
</CardView>
```

### Example 3: Dialog
```xml
<LinearLayout
    android:padding="@dimen/padding_xxlarge"> <!-- 24dp -->
    
    <!-- Dialog title -->
    <TextView
        android:layout_marginBottom="@dimen/margin_large" /> <!-- 16dp -->
    
    <!-- Dialog content -->
    <TextView
        android:layout_marginBottom="@dimen/margin_xxlarge" /> <!-- 24dp -->
    
    <!-- Dialog buttons -->
    <LinearLayout>
        <!-- Button spacing: 8dp -->
    </LinearLayout>
</LinearLayout>
```

---

## Implementation Checklist

When implementing a new screen or component:

- [ ] Use dimension resources from `dimens.xml`
- [ ] Follow the 8dp grid system
- [ ] Apply screen edge margins (16dp for phones)
- [ ] Ensure touch targets are at least 48dp
- [ ] Maintain consistent spacing for similar elements
- [ ] Separate major sections with 24dp-32dp spacing
- [ ] Use Γ smaller spacing (8dp-12dp) for related elements
- [ ] Test on different screen sizes
- [ ] Verify spacing looks balanced

---

## Maintenance

This document should be reviewed and updated when:
- Material Design guidelines change
- App design system evolves
- New patterns are established
- Feedback indicates spacing issues

**Last Updated**: January 2025  
**Maintained By**: Development Team  
**Review Frequency**: Quarterly

---

*For more details, refer to `UI_UX_STANDARDS.md` and `values/dimens.xml`*


















