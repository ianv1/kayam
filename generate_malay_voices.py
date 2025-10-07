#!/usr/bin/env python3
"""
Malay Voice Generation Script for Kayam App
This script generates Malay voice files using espeak TTS
"""

import os
import subprocess
import sys
from pathlib import Path

# Configuration
SOURCE_DIR = "/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/en-us/numbervoice"
TARGET_DIR = "/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/numbervoice"
TEMP_DIR = "/tmp/malay_voice_generation"

# Malay translations mapping
MALAY_TRANSLATIONS = {
    # Basic numbers (0-9)
    "d_0": "sifar",
    "d_1": "satu", 
    "d_2": "dua",
    "d_3": "tiga",
    "d_4": "empat",
    "d_5": "lima",
    "d_6": "enam",
    "d_7": "tujuh",
    "d_8": "lapan",
    "d_9": "sembilan",
    
    # Tens (10-90)
    "d_10": "sepuluh",
    "d_20": "dua puluh",
    "d_30": "tiga puluh", 
    "d_40": "empat puluh",
    "d_50": "lima puluh",
    "d_60": "enam puluh",
    "d_70": "tujuh puluh",
    "d_80": "lapan puluh",
    "d_90": "sembilan puluh",
    
    # Hundreds
    "d_100": "seratus",
    "d_200": "dua ratus",
    "d_300": "tiga ratus",
    "d_400": "empat ratus", 
    "d_500": "lima ratus",
    "d_600": "enam ratus",
    "d_700": "tujuh ratus",
    "d_800": "lapan ratus",
    "d_900": "sembilan ratus",
    
    # Teen numbers (11-19)
    "d_11": "sebelas",
    "d_12": "dua belas",
    "d_13": "tiga belas",
    "d_14": "empat belas",
    "d_15": "lima belas",
    "d_16": "enam belas",
    "d_17": "tujuh belas",
    "d_18": "lapan belas",
    "d_19": "sembilan belas",
    
    # Compound numbers (21-99)
    "d_21": "dua puluh satu",
    "d_22": "dua puluh dua",
    "d_23": "dua puluh tiga",
    "d_24": "dua puluh empat",
    "d_25": "dua puluh lima",
    "d_26": "dua puluh enam",
    "d_27": "dua puluh tujuh",
    "d_28": "dua puluh lapan",
    "d_29": "dua puluh sembilan",
    "d_31": "tiga puluh satu",
    "d_32": "tiga puluh dua",
    "d_33": "tiga puluh tiga",
    "d_34": "tiga puluh empat",
    "d_35": "tiga puluh lima",
    "d_36": "tiga puluh enam",
    "d_37": "tiga puluh tujuh",
    "d_38": "tiga puluh lapan",
    "d_39": "tiga puluh sembilan",
    "d_41": "empat puluh satu",
    "d_42": "empat puluh dua",
    "d_43": "empat puluh tiga",
    "d_44": "empat puluh empat",
    "d_45": "empat puluh lima",
    "d_46": "empat puluh enam",
    "d_47": "empat puluh tujuh",
    "d_48": "empat puluh lapan",
    "d_49": "empat puluh sembilan",
    "d_51": "lima puluh satu",
    "d_52": "lima puluh dua",
    "d_53": "lima puluh tiga",
    "d_54": "lima puluh empat",
    "d_55": "lima puluh lima",
    "d_56": "lima puluh enam",
    "d_57": "lima puluh tujuh",
    "d_58": "lima puluh lapan",
    "d_59": "lima puluh sembilan",
    "d_61": "enam puluh satu",
    "d_62": "enam puluh dua",
    "d_63": "enam puluh tiga",
    "d_64": "enam puluh empat",
    "d_65": "enam puluh lima",
    "d_66": "enam puluh enam",
    "d_67": "enam puluh tujuh",
    "d_68": "enam puluh lapan",
    "d_69": "enam puluh sembilan",
    "d_71": "tujuh puluh satu",
    "d_72": "tujuh puluh dua",
    "d_73": "tujuh puluh tiga",
    "d_74": "tujuh puluh empat",
    "d_75": "tujuh puluh lima",
    "d_76": "tujuh puluh enam",
    "d_77": "tujuh puluh tujuh",
    "d_78": "tujuh puluh lapan",
    "d_79": "tujuh puluh sembilan",
    "d_81": "lapan puluh satu",
    "d_82": "lapan puluh dua",
    "d_83": "lapan puluh tiga",
    "d_84": "lapan puluh empat",
    "d_85": "lapan puluh lima",
    "d_86": "lapan puluh enam",
    "d_87": "lapan puluh tujuh",
    "d_88": "lapan puluh lapan",
    "d_89": "lapan puluh sembilan",
    "d_91": "sembilan puluh satu",
    "d_92": "sembilan puluh dua",
    "d_93": "sembilan puluh tiga",
    "d_94": "sembilan puluh empat",
    "d_95": "sembilan puluh lima",
    "d_96": "sembilan puluh enam",
    "d_97": "sembilan puluh tujuh",
    "d_98": "sembilan puluh lapan",
    "d_99": "sembilan puluh sembilan",
    
    # Mathematical operations
    "and": "dan",
    "equals": "sama dengan",
    "minus": "tolak", 
    "plus": "tambah",
    "times": "darab"
}

def check_dependencies():
    """Check if required tools are installed"""
    # Check ffmpeg
    try:
        subprocess.run(['ffmpeg', '-version'], capture_output=True, check=True)
        print("✓ ffmpeg is installed")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ ffmpeg is not installed")
        print("Please install ffmpeg first:")
        print("  brew install ffmpeg")
        return False
    
    # Check espeak
    try:
        subprocess.run(['espeak', '--version'], capture_output=True, check=True)
        print("✓ espeak is installed")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ espeak is not installed")
        print("Please install espeak first:")
        print("  brew install espeak")
        return False
    
    return True

def create_directories():
    """Create necessary directories"""
    os.makedirs(TEMP_DIR, exist_ok=True)
    os.makedirs(TARGET_DIR, exist_ok=True)
    print(f"Created directories: {TEMP_DIR}, {TARGET_DIR}")

def generate_malay_voice(text, output_file):
    """Generate Malay voice using macOS say command with Amira (female Malay voice)"""
    try:
        # Use macOS say command with Amira voice (female Malay voice)
        subprocess.run([
            'say', '-v', 'Amira', '-o', output_file, text
        ], check=True)
        print(f"    Generated with Amira (female Malay voice)")
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"Error generating voice for '{text}': {e}")
        return False

def convert_to_m4a(input_file, output_file):
    """Convert AIFF (from say command) to m4a format with volume amplification"""
    try:
        # Increase volume by 6dB (doubles the perceived loudness)
        subprocess.run([
            'ffmpeg', '-i', input_file, '-c:a', 'aac', '-b:a', '128k',
            '-af', 'volume=6dB', '-y', output_file
        ], check=True, capture_output=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"Error converting to m4a: {e}")
        return False

def process_voice_file(filename):
    """Process a single voice file"""
    base_name = filename.replace('.m4a', '')
    
    if base_name not in MALAY_TRANSLATIONS:
        print(f"⚠️  No translation found for {filename}")
        return False
    
    malay_text = MALAY_TRANSLATIONS[base_name]
    print(f"Processing {filename} -> '{malay_text}'")
    
    # Generate Malay voice directly
    temp_aiff = os.path.join(TEMP_DIR, f"{base_name}_malay.aiff")
    temp_m4a = os.path.join(TEMP_DIR, f"{base_name}_malay.m4a")
    final_m4a = os.path.join(TARGET_DIR, filename)
    
    # Generate Malay voice
    if not generate_malay_voice(malay_text, temp_aiff):
        return False
    
    # Convert to m4a
    if not convert_to_m4a(temp_aiff, temp_m4a):
        return False
    
    # Copy to target directory
    subprocess.run(['cp', temp_m4a, final_m4a], check=True)
    
    # Clean up temp files
    os.remove(temp_aiff)
    os.remove(temp_m4a)
    
    print(f"✓ Generated {final_m4a}")
    return True

def copy_durations_file():
    """Copy durations.tsv file if it exists"""
    durations_src = os.path.join(SOURCE_DIR, 'durations.tsv')
    durations_dst = os.path.join(TARGET_DIR, 'durations.tsv')
    
    if os.path.exists(durations_src):
        subprocess.run(['cp', durations_src, durations_dst], check=True)
        print("✓ Copied durations.tsv")
    else:
        print("⚠️  durations.tsv not found in source directory")

def main():
    """Main function"""
    print("🎤 Malay Voice Generation Script for Kayam App")
    print("=" * 50)
    
    # Check dependencies
    if not check_dependencies():
        print("\n❌ Please install missing dependencies and try again.")
        sys.exit(1)
    
    # Create directories
    create_directories()
    
    # Get list of m4a files to process
    if not os.path.exists(SOURCE_DIR):
        print(f"❌ Source directory not found: {SOURCE_DIR}")
        print("Please make sure the English voice files exist.")
        sys.exit(1)
    
    source_files = [f for f in os.listdir(SOURCE_DIR) if f.endswith('.m4a')]
    
    if not source_files:
        print(f"❌ No m4a files found in {SOURCE_DIR}")
        sys.exit(1)
    
    print(f"\n📁 Found {len(source_files)} voice files to process")
    
    # Process each file
    success_count = 0
    for filename in sorted(source_files):
        if process_voice_file(filename):
            success_count += 1
    
    # Copy durations file
    copy_durations_file()
    
    print(f"\n🎉 Successfully processed {success_count}/{len(source_files)} files")
    print(f"📂 Malay voice files saved to: {TARGET_DIR}")
    
    # Clean up temp directory
    subprocess.run(['rm', '-rf', TEMP_DIR], check=True)
    print("🧹 Cleaned up temporary files")

if __name__ == "__main__":
    main()