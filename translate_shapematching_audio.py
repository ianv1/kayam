#!/usr/bin/env python3
"""
Script to translate English audio files in shapematching to Malay audio.
This script identifies shape names (not sound effects) and translates them to Malay using TTS.
"""

import os
import sys
import subprocess
import tempfile
from pathlib import Path
import argparse

# Sound effects that should NOT be translated
SOUND_EFFECTS = {
    'quiz_correct.m4a',
    'train_slotin.m4a', 
    'ui_star_collected.m4a'
}

# English to Malay translations for shape names
SHAPE_TRANSLATIONS = {
    'circle': 'bulat',
    'cone': 'kon',
    'cube': 'kubus',
    'cylinder': 'silinder',
    'diamond': 'berlian',
    'hexagon': 'heksagon',
    'large': 'besar',
    'medium': 'sederhana',
    'octagon': 'oktagon',
    'oval': 'oval',
    'parallelogram': 'segiempat selari',
    'pentagon': 'pentagon',
    'pyramid': 'piramid',
    'rectangle': 'segiempat tepat',
    'rectangular_prism': 'prisma segiempat tepat',
    'rhombus': 'rombus',
    'small': 'kecil',
    'sphere': 'sfera',
    'square': 'segiempat sama',
    'star': 'bintang',
    'trapezoid': 'trapezoid',
    'triangle': 'segi tiga',
    'triangular_prism': 'prisma segi tiga'
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

def is_sound_effect(filename):
    """Check if a file is a sound effect that shouldn't be translated."""
    return filename in SOUND_EFFECTS

def get_malay_text(english_text):
    """Get Malay translation for English shape name."""
    return SHAPE_TRANSLATIONS.get(english_text, english_text)

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
        
        if is_sound_effect(filename):
            print(f"⏭️  Skipping sound effect: {filename}")
            skipped_count += 1
            continue
        
        # Extract shape name from filename
        shape_name = audio_file.stem  # filename without extension
        
        if shape_name not in SHAPE_TRANSLATIONS:
            print(f"⚠️  No translation found for: {shape_name}")
            skipped_count += 1
            continue
        
        print(f"🔄 Processing: {filename}")
        
        # Create backup of original file
        backup_file = backup_dir / filename
        audio_file.rename(backup_file)
        print(f"  📁 Backed up to: {backup_file}")
        
        # Get Malay translation
        malay_text = get_malay_text(shape_name)
        print(f"  📝 English: '{shape_name}' → Malay: '{malay_text}'")
        
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
    parser = argparse.ArgumentParser(description='Translate English audio files to Malay for shapematching game')
    parser.add_argument('sound_dir', nargs='?',
                       help='Path to the sound directory containing audio files',
                       default='/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/shapematching/sound/')
    parser.add_argument('--dry-run', action='store_true',
                       help='Show what would be processed without making changes')
    
    args = parser.parse_args()
    
    print("🎵 Shape Matching Audio Translation Tool")
    print("=" * 50)
    
    if args.dry_run:
        print("🔍 DRY RUN MODE - No files will be modified")
        sound_path = Path(args.sound_dir)
        if sound_path.exists():
            for audio_file in sound_path.glob('*.m4a'):
                filename = audio_file.name
                if is_sound_effect(filename):
                    print(f"⏭️  Would skip sound effect: {filename}")
                else:
                    shape_name = audio_file.stem
                    if shape_name in SHAPE_TRANSLATIONS:
                        malay_text = get_malay_text(shape_name)
                        print(f"🔄 Would process: {filename} ('{shape_name}' → '{malay_text}')")
                    else:
                        print(f"⚠️  No translation for: {filename}")
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
