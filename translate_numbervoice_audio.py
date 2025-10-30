#!/usr/bin/env python3
"""
Script to translate English audio files in numbervoice to Malay audio.
This script translates numbers and mathematical operations to Malay.
"""

import os
import sys
import subprocess
import tempfile
from pathlib import Path
import argparse

# English to Malay translations for mathematical operations
MATH_OPERATIONS = {
    'and': 'dan',
    'equals': 'sama dengan',
    'minus': 'tolak',
    'plus': 'tambah',
    'times': 'darab'
}

def check_dependencies():
    """Check if required dependencies are available."""
    try:
        import gtts
        print("✓ gTTS (Google Text-to-Speech) is available")
    except ImportError:
        print("✗ gTTS not found. Please install dependencies first:")
        print("  python3 -m venv venv")
        print("  source venv/bin/activate")
        print("  pip install gtts")
        sys.exit(1)
    
    # Check if ffmpeg is available
    try:
        subprocess.run(['ffmpeg', '-version'], check=True, capture_output=True)
        print("✓ ffmpeg is available")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ ffmpeg not found. Please install ffmpeg:")
        print("  brew install ffmpeg")
        sys.exit(1)

def number_to_malay(number):
    """Convert a number to Malay text."""
    # Malay number words
    malay_numbers = {
        0: 'sifar', 1: 'satu', 2: 'dua', 3: 'tiga', 4: 'empat',
        5: 'lima', 6: 'enam', 7: 'tujuh', 8: 'lapan', 9: 'sembilan',
        10: 'sepuluh', 11: 'sebelas', 12: 'dua belas', 13: 'tiga belas',
        14: 'empat belas', 15: 'lima belas', 16: 'enam belas',
        17: 'tujuh belas', 18: 'lapan belas', 19: 'sembilan belas',
        20: 'dua puluh', 30: 'tiga puluh', 40: 'empat puluh',
        50: 'lima puluh', 60: 'enam puluh', 70: 'tujuh puluh',
        80: 'lapan puluh', 90: 'sembilan puluh',
        100: 'seratus', 200: 'dua ratus', 300: 'tiga ratus',
        400: 'empat ratus', 500: 'lima ratus', 600: 'enam ratus',
        700: 'tujuh ratus', 800: 'lapan ratus', 900: 'sembilan ratus'
    }
    
    if number in malay_numbers:
        return malay_numbers[number]
    
    # Handle compound numbers
    if number < 100:
        tens = (number // 10) * 10
        ones = number % 10
        if tens == 0:
            return malay_numbers[ones]
        else:
            return f"{malay_numbers[tens]} {malay_numbers[ones]}"
    
    elif number < 1000:
        hundreds = (number // 100) * 100
        remainder = number % 100
        if remainder == 0:
            return malay_numbers[hundreds]
        else:
            return f"{malay_numbers[hundreds]} {number_to_malay(remainder)}"
    
    else:
        # For numbers 1000+, use a simpler approach
        return str(number)

def get_malay_text(filename):
    """Get Malay translation for a filename."""
    name_without_ext = filename.stem
    
    # Handle mathematical operations
    if name_without_ext in MATH_OPERATIONS:
        return MATH_OPERATIONS[name_without_ext]
    
    # Handle number files (d_0.m4a, d_1.m4a, etc.)
    if name_without_ext.startswith('d_'):
        try:
            number = int(name_without_ext[2:])  # Extract number after 'd_'
            return number_to_malay(number)
        except ValueError:
            return name_without_ext
    
    return name_without_ext

def generate_malay_audio(text, output_path, language='ms'):
    """Generate Malay audio using gTTS and save as m4a."""
    from gtts import gTTS
    import tempfile
    import subprocess
    
    print(f"  Generating Malay audio for: '{text}'")
    
    # Create gTTS object
    tts = gTTS(text=text, lang=language, slow=False)
    
    # Generate audio to temporary file
    with tempfile.NamedTemporaryFile(suffix='.mp3', delete=False) as temp_file:
        tts.save(temp_file.name)
        
        # Convert MP3 to M4A using ffmpeg
        try:
            subprocess.run([
                'ffmpeg', '-i', temp_file.name, 
                '-c:a', 'aac', 
                '-b:a', '128k',
                '-y',  # Overwrite output file
                output_path
            ], check=True, capture_output=True)
        except subprocess.CalledProcessError as e:
            print(f"  ❌ Error converting audio: {e}")
            raise
        except FileNotFoundError:
            print("  ❌ ffmpeg not found. Please install ffmpeg:")
            print("     brew install ffmpeg")
            raise
        
        # Clean up temp file
        os.unlink(temp_file.name)

def process_audio_files(sound_dir):
    """Process all audio files in the sound directory."""
    sound_path = Path(sound_dir)
    
    if not sound_path.exists():
        print(f"Error: Directory {sound_dir} does not exist")
        return False
    
    print(f"Processing audio files in: {sound_path}")
    print(f"Found {len(list(sound_path.glob('*.m4a')))} audio files")
    
    # Create backup directory
    backup_dir = sound_path / 'backup_original'
    backup_dir.mkdir(exist_ok=True)
    print(f"Backup directory created: {backup_dir}")
    
    processed_count = 0
    skipped_count = 0
    
    for audio_file in sound_path.glob('*.m4a'):
        filename = audio_file.name
        
        print(f"🔄 Processing: {filename}")
        
        # Create backup of original file
        backup_file = backup_dir / filename
        audio_file.rename(backup_file)
        print(f"  📁 Backed up to: {backup_file}")
        
        # Get Malay translation
        malay_text = get_malay_text(audio_file)
        print(f"  📝 English: '{audio_file.stem}' → Malay: '{malay_text}'")
        
        # Generate Malay audio
        try:
            generate_malay_audio(malay_text, str(audio_file))
            print(f"  ✅ Generated: {filename}")
            processed_count += 1
        except Exception as e:
            print(f"  ❌ Error generating audio for {filename}: {e}")
            # Restore original file if generation failed
            backup_file.rename(audio_file)
            print(f"  🔄 Restored original file")
            skipped_count += 1
    
    print(f"\n📊 Summary:")
    print(f"  ✅ Processed: {processed_count} files")
    print(f"  ⏭️  Skipped: {skipped_count} files")
    print(f"  📁 Backups saved in: {backup_dir}")
    
    return True

def main():
    parser = argparse.ArgumentParser(description='Translate English audio files to Malay for numbervoice')
    parser.add_argument('sound_dir', nargs='?',
                       help='Path to the sound directory containing audio files',
                       default='/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/numbervoice/')
    parser.add_argument('--dry-run', action='store_true',
                       help='Show what would be processed without making changes')
    
    args = parser.parse_args()
    
    print("🔢 Number Voice Audio Translation Tool")
    print("=" * 50)
    
    if args.dry_run:
        print("🔍 DRY RUN MODE - No files will be modified")
        sound_path = Path(args.sound_dir)
        if sound_path.exists():
            for audio_file in sound_path.glob('*.m4a'):
                filename = audio_file.name
                malay_text = get_malay_text(audio_file)
                print(f"🔄 Would process: {filename} ('{audio_file.stem}' → '{malay_text}')")
        return
    
    # Check dependencies
    print("🔧 Checking dependencies...")
    check_dependencies()
    
    # Process audio files
    print("\n🎯 Starting translation process...")
    success = process_audio_files(args.sound_dir)
    
    if success:
        print("\n🎉 Translation completed successfully!")
        print("\n💡 Tips:")
        print("  - Original files are backed up in the 'backup_original' folder")
        print("  - You can restore originals by moving files back from backup folder")
        print("  - Test the audio files in the game to ensure quality is acceptable")
    else:
        print("\n❌ Translation failed. Please check the error messages above.")
        sys.exit(1)

if __name__ == '__main__':
    main()
