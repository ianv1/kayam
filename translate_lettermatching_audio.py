#!/usr/bin/env python3
"""
Script to translate English audio files in lettermatching to Malay audio.
This script reads the lettermatching_levels.tsv file, extracts all MatchSound audio files,
and generates Malay audio for each one using macOS say command with Amira voice.
"""

import os
import sys
import subprocess
import csv
from pathlib import Path
import tempfile

# Configuration
TSV_FILE = "/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/lettermatching/lettermatching_levels.tsv"
SOURCE_DIR = "/Users/ian/projects/kayam/kayam/mainapp/Resources/games/numbermatching/sound"
TARGET_DIR = "/Users/ian/projects/kayam/kayam/mainapp/Resources/games/numbermatching/sound"
TEMP_DIR = "/tmp/malay_voice_generation"

# English to Malay translations
MALAY_TRANSLATIONS = {
    # Animals
    'cat': 'kucing',
    'dog': 'anjing',
    'bug': 'serangga',
    'hen': 'ayam betina',
    'pig': 'babi',
    'cow': 'lembu',
    'duck': 'itik',
    'chicken': 'ayam',
    'donkey': 'keldai',
    'frog': 'katak',
    
    # Objects
    'bed': 'katil',
    'sun': 'matahari',
    'toy': 'mainan',
    'hat': 'topi',
    'box': 'kotak',
    'tree': 'pokok',
    'drum': 'gendang',
    'face': 'muka',
    'fire': 'api',
    'cake': 'kek',
    'book': 'buku',
    'leaf': 'daun',
    'star': 'bintang',
    'baby': 'bayi',
    'milk': 'susu',
    'kid': 'kanak-kanak',
    
    # Family members
    'father': 'bapa',
    'mother': 'ibu',
    'grandpa': 'datuk',
    'grandma': 'nenek',
    'sister': 'kakak',
    'brother': 'abang',
    'dad': 'ayah',
    'mom': 'ibu',
    
    # English letters (pronounced in Malay - using standard English letter names)
    'eng_a': 'e',
    'eng_b': 'bi',
    'eng_c': 'si',
    'eng_d': 'di',
    'eng_e': 'i',
    'eng_f': 'ef',
    'eng_g': 'ji',
    'eng_h': 'hec',
    'eng_i': 'ai',
    'eng_j': 'je',
    'eng_k': 'ke',
    'eng_l': 'el',
    'eng_m': 'em',
    'eng_n': 'en',
    'eng_o': 'o',
    'eng_p': 'pi',
    'eng_q': 'kiu',
    'eng_r': 'ar',
    'eng_s': 'es',
    'eng_t': 'ti',
    'eng_u': 'yu',
    'eng_v': 'vi',
    'eng_w': 'dabelyu',
    'eng_x': 'eks',
    'eng_y': 'wai',
    'eng_z': 'zed',
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
    
    # Check say command (macOS built-in)
    try:
        # say command doesn't have --version, so we test with a simple command
        subprocess.run(['say', '-v', '?'], capture_output=True, check=True)
        print("✓ macOS say command is available")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ macOS say command not found")
        return False
    
    return True

def create_directories():
    """Create necessary directories"""
    os.makedirs(TEMP_DIR, exist_ok=True)
    os.makedirs(TARGET_DIR, exist_ok=True)
    print(f"Created directories: {TEMP_DIR}, {TARGET_DIR}")

def extract_audio_files_from_tsv():
    """Extract unique audio file names from the TSV file"""
    audio_files = set()
    
    if not os.path.exists(TSV_FILE):
        print(f"❌ TSV file not found: {TSV_FILE}")
        return audio_files
    
    with open(TSV_FILE, 'r', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter='\t')
        for row in reader:
            # Skip comment lines
            if row and row[0].startswith('#'):
                continue
            # MatchSound is the 7th column (index 6) - after empty column
            if len(row) > 6 and row[6]:
                audio_file = row[6].strip()
                if audio_file and audio_file.endswith('.m4a'):
                    audio_files.add(audio_file)
    
    return sorted(audio_files)

def get_malay_text(filename):
    """Get Malay translation for English word"""
    base_name = filename.replace('.m4a', '')
    return MALAY_TRANSLATIONS.get(base_name, base_name)

def generate_malay_voice(text, output_file):
    """Generate Malay voice using macOS say command with Amira (female Malay voice)"""
    try:
        # Use macOS say command with Amira voice (female Malay voice)
        subprocess.run([
            'say', '-v', 'Amira', '-o', output_file, text
        ], check=True, capture_output=True)
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

def process_audio_file(filename):
    """Process a single audio file"""
    base_name = filename.replace('.m4a', '')
    
    if base_name not in MALAY_TRANSLATIONS:
        print(f"⚠️  No translation found for {filename}")
        return False
    
    malay_text = MALAY_TRANSLATIONS[base_name]
    print(f"Processing {filename} -> '{malay_text}'")
    
    # Check if source file exists
    source_file = os.path.join(SOURCE_DIR, filename)
    if not os.path.exists(source_file):
        print(f"⚠️  Source file not found: {source_file}")
        return False
    
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
    
    # Backup original file
    backup_file = os.path.join(TARGET_DIR, f"{base_name}_backup.m4a")
    if os.path.exists(final_m4a):
        subprocess.run(['cp', final_m4a, backup_file], check=True)
        print(f"  📁 Backed up original to: {backup_file}")
    
    # Copy to target directory
    subprocess.run(['cp', temp_m4a, final_m4a], check=True)
    
    # Clean up temp files
    os.remove(temp_aiff)
    os.remove(temp_m4a)
    
    print(f"✓ Generated {final_m4a}")
    return True

def main():
    """Main function"""
    print("🎤 Letter Matching Audio Translation Script")
    print("=" * 50)
    
    # Check dependencies
    if not check_dependencies():
        print("\n❌ Please install missing dependencies and try again.")
        sys.exit(1)
    
    # Create directories
    create_directories()
    
    # Extract audio files from TSV
    print(f"\n📖 Reading TSV file: {TSV_FILE}")
    audio_files = extract_audio_files_from_tsv()
    
    if not audio_files:
        print("❌ No audio files found in TSV file")
        sys.exit(1)
    
    print(f"📁 Found {len(audio_files)} unique audio files to process")
    print(f"Files: {', '.join(audio_files[:10])}{'...' if len(audio_files) > 10 else ''}")
    
    # Process each file
    success_count = 0
    skipped_count = 0
    
    for filename in audio_files:
        if process_audio_file(filename):
            success_count += 1
        else:
            skipped_count += 1
    
    print(f"\n🎉 Successfully processed {success_count}/{len(audio_files)} files")
    print(f"⏭️  Skipped: {skipped_count} files")
    print(f"📂 Malay voice files saved to: {TARGET_DIR}")
    
    # Clean up temp directory
    subprocess.run(['rm', '-rf', TEMP_DIR], check=True)
    print("🧹 Cleaned up temporary files")
    
    if success_count > 0:
        print("\n💡 Note: Original files are backed up with '_backup.m4a' suffix")
        print("   You can restore them if needed")

if __name__ == "__main__":
    main()
