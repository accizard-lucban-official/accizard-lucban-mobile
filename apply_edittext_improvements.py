#!/usr/bin/env python3
"""
Script to apply EditText improvements to Android layout files.
This script helps update EditText components with better padding and styling.
"""

import os
import re
import glob

def update_edittext_in_file(file_path):
    """Update EditText components in a single file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Update EditText height from 48dp to 56dp
        content = re.sub(
            r'android:layout_height="48dp"',
            'android:layout_height="56dp"',
            content
        )
        
        # Update paddingHorizontal from 16dp to 20dp
        content = re.sub(
            r'android:paddingHorizontal="16dp"',
            'android:paddingHorizontal="20dp"',
            content
        )
        
        # Add paddingVertical if not present
        content = re.sub(
            r'(android:paddingHorizontal="20dp")(?!\s+android:paddingVertical)',
            r'\1\n                            android:paddingVertical="16dp"',
            content
        )
        
        # Add gravity, scrollHorizontally, and scrollbars if not present
        edittext_pattern = r'(<EditText[^>]*android:paddingVertical="16dp"[^>]*>)'
        
        def add_attributes(match):
            edittext_tag = match.group(1)
            if 'android:gravity=' not in edittext_tag:
                edittext_tag = edittext_tag.replace('>', '\n                            android:gravity="center_vertical">')
            if 'android:scrollHorizontally=' not in edittext_tag:
                edittext_tag = edittext_tag.replace('>', '\n                            android:scrollHorizontally="false">')
            if 'android:scrollbars=' not in edittext_tag:
                edittext_tag = edittext_tag.replace('>', '\n                            android:scrollbars="none">')
            return edittext_tag
        
        content = re.sub(edittext_pattern, add_attributes, content, flags=re.MULTILINE)
        
        # Update textSize to 16sp if it's 14sp
        content = re.sub(
            r'android:textSize="14sp"',
            'android:textSize="16sp"',
            content
        )
        
        # If content changed, write it back
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ Updated: {file_path}")
            return True
        else:
            print(f"⏭️  No changes needed: {file_path}")
            return False
            
    except Exception as e:
        print(f"❌ Error processing {file_path}: {e}")
        return False

def main():
    """Main function to process all layout files."""
    # Get all layout files
    layout_files = glob.glob("app/src/main/res/layout/*.xml")
    
    print("🎯 EditText Improvements Script")
    print("=" * 50)
    
    updated_count = 0
    total_files = len(layout_files)
    
    for file_path in layout_files:
        if update_edittext_in_file(file_path):
            updated_count += 1
    
    print("=" * 50)
    print(f"📊 Summary: Updated {updated_count}/{total_files} files")
    
    if updated_count > 0:
        print("\n🎉 EditText improvements applied successfully!")
        print("📝 Please review the changes and test your app.")
    else:
        print("\n✨ All files already have the latest EditText improvements!")

if __name__ == "__main__":
    main()

