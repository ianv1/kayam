#!/usr/bin/env python3
"""
Comprehensive script to translate English audio files in all games to Malay audio.
This script processes audio files from multiple games, translating English words/phrases to Malay.
"""

import os
import sys
import subprocess
import csv
from pathlib import Path
import tempfile
import re

# Base directory
BASE_DIR = Path("/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games")
TEMP_DIR = "/tmp/malay_voice_generation"

# Sound effects that should NOT be translated
SOUND_EFFECTS = {
    'quiz_correct.m4a',
    'train_slotin.m4a', 
    'ui_star_collected.m4a',
    'play_level_clear.wav',
    'sfx_balloon-blow-up.m4a',
    'sfx_balloon-pop.m4a',
    'sfx_balloon_up.m4a',
}

# Comprehensive English to Malay translations
MALAY_TRANSLATIONS = {
    # Animals
    'cat': 'kucing',
    'dog': 'anjing',
    'bug': 'serangga',
    'bugs': 'serangga',
    'hen': 'ayam betina',
    'pig': 'babi',
    'cow': 'lembu',
    'duck': 'itik',
    'chicken': 'ayam',
    'chickens': 'ayam',
    'donkey': 'keldai',
    'frog': 'katak',
    'lion': 'singa',
    'whale': 'paus',
    'bird': 'burung',
    'birds': 'burung',
    'turtle': 'penyu',
    'hyena': 'hyena',
    'bee': 'lebah',
    'bees': 'lebah',
    'hippo': 'kuda nil',
    'eagle': 'helang',
    'zebra': 'zebra',
    'octopus': 'sotong',
    'elephant': 'gajah',
    'elephants': 'gajah',
    'gazelle': 'gazel',
    'lizard': 'cicak',
    'meerkat': 'meerkat',
    'meerkats': 'meerkat',
    'squirrel': 'tupai',
    'mouse': 'tikus',
    'monkey': 'monyet',
    'monkeys': 'monyet',
    'crocodile': 'buaya',
    'rabbit': 'arnab',
    'flower': 'bunga',
    'goat': 'kambing',
    'bear': 'beruang',
    'bears': 'beruang',
    'giraffe': 'zirafah',
    'giraffes': 'zirafah',
    'swan': 'angsa',
    'swans': 'angsa',
    'gecko': 'cicak',
    'butterfly': 'rama-rama',
    'camel': 'unta',
    'camels': 'unta',
    'cactus': 'kaktus',
    'cacti': 'kaktus',
    'penguin': 'penguin',
    
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
    'ball': 'bola',
    'boat': 'bot',
    'bowl': 'mangkuk',
    'brush': 'berus',
    'card': 'kad',
    'fan': 'kipas',
    'flag': 'bendera',
    'game': 'permainan',
    'gum': 'gula-gula getah',
    'house': 'rumah',
    'jar': 'balang',
    'key': 'kunci',
    'king': 'raja',
    'lock': 'kunci',
    'mat': 'tikar',
    'nut': 'kacang',
    'pen': 'pen',
    'pin': 'pin',
    'plane': 'kapal terbang',
    'queen': 'permaisuri',
    'river': 'sungai',
    'sack': 'karung',
    'ship': 'kapal',
    'song': 'lagu',
    'spoon': 'sudu',
    'tent': 'khemah',
    'watch': 'jam tangan',
    'yard': 'halaman',
    'zero': 'sifar',
    
    # Family members
    'father': 'bapa',
    'mother': 'ibu',
    'grandpa': 'datuk',
    'grandma': 'nenek',
    'sister': 'kakak',
    'brother': 'abang',
    'dad': 'ayah',
    'mom': 'ibu',
    'uncle': 'bapa saudara',
    
    # Actions/Verbs
    'cry': 'menangis',
    'fly': 'terbang',
    'get': 'dapat',
    'give': 'beri',
    'grow': 'tumbuh',
    'hug': 'peluk',
    'jump': 'lompat',
    'make': 'buat',
    'sing': 'menyanyi',
    'stay': 'tinggal',
    'wish': 'harap',
    
    # Adjectives
    'big': 'besar',
    'cheap': 'murah',
    'dark': 'gelap',
    'dirty': 'kotor',
    'fresh': 'segar',
    'fun': 'seronok',
    'good': 'baik',
    'gray': 'kelabu',
    'loud': 'kuat',
    'only': 'sahaja',
    'quiet': 'senyap',
    'sad': 'sedih',
    'short': 'pendek',
    'slow': 'perlahan',
    'tall': 'tinggi',
    
    # Other words
    'each': 'setiap',
    'day': 'hari',
    'then': 'kemudian',
    'there': 'di sana',
    'they': 'mereka',
    'them': 'mereka',
    'up': 'atas',
    'yes': 'ya',
    'zip': 'zip',
    
    # English letters (pronounced in Malay)
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
    
    # Phonics sounds
    'a_sound': 'a',
    'f_sound': 'f',
    'k_sound': 'k',
    'm_sound': 'm',
    's_sound': 's',
    'u_sound': 'u',
    'x_sound': 'eks',
    
    # Letter combinations
    'ar': 'ar',
    'arm': 'lengan',
    'bang': 'letupan',
    'bath': 'mandi',
    'beep': 'bip',
    'bench': 'bangku',
    'bi': 'bi',
    'bl': 'bl',
    'bo': 'bo',
    'ch': 'ch',
    'check': 'semak',
    'choose': 'pilih',
    'chop': 'potong',
    'ck': 'ck',
    'cl': 'cl',
    'class': 'kelas',
    'clay': 'tanah liat',
    'co': 'co',
    'crib': 'katil bayi',
    'dash': 'sengkang',
    'dot': 'titik',
    'fern': 'paku pakis',
    'flap': 'kepak',
    'h': 'hec',
    'i': 'ai',
    'j': 'je',
    'join': 'sertai',
    'joke': 'jenaka',
    'k': 'ke',
    'l': 'el',
    'la': 'la',
    'le': 'le',
    'm': 'em',
    'ma': 'ma',
    'me': 'me',
    'mouth': 'mulut',
    'mu': 'mu',
    'n': 'en',
    'o': 'o',
    'oi': 'oi',
    'oo': 'oo',
    'paid': 'dibayar',
    'pair': 'pasangan',
    'paper': 'kertas',
    'park': 'taman',
    'pin': 'pin',
    'scar': 'parut',
    'se': 'se',
    'snip': 'potong',
    'son': 'anak lelaki',
    'span': 'jangkauan',
    'spot': 'tempat',
    'store': 'kedai',
    'ta': 'ta',
    'team': 'pasukan',
    'ten': 'sepuluh',
    'tr': 'tr',
    'ue': 'ue',
    'ur': 'ur',
    'wh': 'wh',
    'wind': 'angin',
    'x': 'eks',
    'y': 'wai',
    'ya': 'ya',
    'ye': 'ye',
    'z': 'zed',
    'zi': 'zi',
}

def check_dependencies():
    """Check if required tools are installed"""
    try:
        subprocess.run(['ffmpeg', '-version'], capture_output=True, check=True)
        print("✓ ffmpeg is installed")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ ffmpeg is not installed")
        print("Please install ffmpeg first:")
        print("  brew install ffmpeg")
        return False
    
    try:
        subprocess.run(['say', '-v', '?'], capture_output=True, check=True)
        print("✓ macOS say command is available")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("✗ macOS say command not found")
        return False
    
    return True

def create_directories():
    """Create necessary directories"""
    os.makedirs(TEMP_DIR, exist_ok=True)
    print(f"Created directory: {TEMP_DIR}")

def is_sound_effect(filename):
    """Check if a file is a sound effect that shouldn't be translated"""
    return filename in SOUND_EFFECTS or filename.startswith('sfx_') or filename.startswith('ui_')

def get_malay_text_from_filename(filename):
    """Extract English text from filename and translate to Malay"""
    base_name = filename.replace('.m4a', '').replace('.wav', '').lower()
    
    # Check if it's a direct translation
    if base_name in MALAY_TRANSLATIONS:
        return MALAY_TRANSLATIONS[base_name]
    
    # Handle plural forms (simple: remove 's' at the end)
    if base_name.endswith('s') and len(base_name) > 1:
        singular = base_name[:-1]
        if singular in MALAY_TRANSLATIONS:
            return MALAY_TRANSLATIONS[singular]
        # Also try with 'es' ending
        if base_name.endswith('es'):
            singular = base_name[:-2]
            if singular in MALAY_TRANSLATIONS:
                return MALAY_TRANSLATIONS[singular]
    
    # Handle compound words with underscores/and
    if '_and_' in base_name or ' and ' in base_name:
        parts = re.split(r'[_ ]and[_ ]', base_name)
        translated_parts = []
        for part in parts:
            if part in MALAY_TRANSLATIONS:
                translated_parts.append(MALAY_TRANSLATIONS[part])
            else:
                # Try singular form
                if part.endswith('s') and part[:-1] in MALAY_TRANSLATIONS:
                    translated_parts.append(MALAY_TRANSLATIONS[part[:-1]])
                else:
                    translated_parts.append(part)
        if all(p in MALAY_TRANSLATIONS.values() or p.replace('s', '') in MALAY_TRANSLATIONS for p in parts):
            return ' dan '.join(translated_parts)
    
    # Handle words with underscores (try to translate each word)
    if '_' in base_name:
        parts = base_name.split('_')
        translated_parts = []
        all_translated = True
        for part in parts:
            if part in MALAY_TRANSLATIONS:
                translated_parts.append(MALAY_TRANSLATIONS[part])
            elif part.endswith('s') and part[:-1] in MALAY_TRANSLATIONS:
                translated_parts.append(MALAY_TRANSLATIONS[part[:-1]])
            else:
                translated_parts.append(part)
                all_translated = False
        if all_translated:
            return ' '.join(translated_parts)
    
    # If it's a simple word we don't know, return None to skip
    # Only process if it's a known word or simple compound
    if base_name not in MALAY_TRANSLATIONS and '_' not in base_name and ' ' not in base_name:
        # Check if it looks like it might be English (has vowels and consonants)
        if not re.match(r'^[a-z]+$', base_name):
            return None
    
    return base_name

def generate_malay_voice(text, output_file):
    """Generate Malay voice using macOS say command with Amira (female Malay voice)"""
    try:
        subprocess.run([
            'say', '-v', 'Amira', '-o', output_file, text
        ], check=True, capture_output=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"    Error generating voice: {e}")
        return False

def convert_to_m4a(input_file, output_file):
    """Convert AIFF (from say command) to m4a format with volume amplification"""
    try:
        subprocess.run([
            'ffmpeg', '-i', input_file, '-c:a', 'aac', '-b:a', '128k',
            '-af', 'volume=6dB', '-y', output_file
        ], check=True, capture_output=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"    Error converting to m4a: {e}")
        return False

def process_audio_file(audio_path):
    """Process a single audio file"""
    filename = audio_path.name
    
    # Skip sound effects
    if is_sound_effect(filename):
        return False, "sound_effect"
    
    # Skip if already backed up (already processed)
    if '_backup.m4a' in filename or '_backup.wav' in filename:
        return False, "backup_file"
    
    # Skip very long filenames (likely complex sentences/phrases)
    base_name = filename.replace('.m4a', '').replace('.wav', '').lower()
    if len(base_name) > 30 or base_name.count('_') > 3 or base_name.count(' ') > 3:
        return False, "too_complex"
    
    # Get Malay translation
    malay_text = get_malay_text_from_filename(filename)
    
    # Skip if no translation found
    if malay_text is None:
        return False, "no_translation"
    
    # Skip if translation is the same as original (likely already translated or not translatable)
    base_name = filename.replace('.m4a', '').replace('.wav', '').lower()
    if malay_text == base_name:
        # Check if it's a known word we just don't have translation for
        # Only skip if it's clearly not a simple word we should translate
        if '_' in base_name or ' ' in base_name or len(base_name) > 20:
            return False, "complex_phrase"
        # For simple words, try to translate anyway (might be a word we know)
        if base_name not in MALAY_TRANSLATIONS and not base_name.endswith('s'):
            return False, "no_translation"
    
    print(f"  Processing: {filename} -> '{malay_text}'")
    
    # Generate Malay voice
    base_name = filename.replace('.m4a', '').replace('.wav', '')
    temp_aiff = os.path.join(TEMP_DIR, f"{base_name}_malay.aiff")
    temp_m4a = os.path.join(TEMP_DIR, f"{base_name}_malay.m4a")
    
    if not generate_malay_voice(malay_text, temp_aiff):
        return False, "generation_failed"
    
    # Convert to m4a
    output_ext = '.m4a' if filename.endswith('.m4a') else '.m4a'
    final_output = audio_path.parent / filename
    
    if not convert_to_m4a(temp_aiff, temp_m4a):
        return False, "conversion_failed"
    
    # Backup original
    backup_file = audio_path.parent / f"{base_name}_backup{Path(filename).suffix}"
    if audio_path.exists():
        subprocess.run(['cp', str(audio_path), str(backup_file)], check=True)
    
    # Replace with new file
    subprocess.run(['cp', temp_m4a, str(final_output)], check=True)
    
    # Clean up temp files
    os.remove(temp_aiff)
    os.remove(temp_m4a)
    
    return True, "success"

def process_game_directory(game_dir):
    """Process all audio files in a game directory"""
    game_name = game_dir.name
    print(f"\n🎮 Processing game: {game_name}")
    
    # Find all audio files
    audio_files = list(game_dir.rglob("*.m4a")) + list(game_dir.rglob("*.wav"))
    
    # Filter out backup files and sound effects
    audio_files = [f for f in audio_files 
                   if not is_sound_effect(f.name) 
                   and '_backup' not in f.name
                   and f.parent.name != 'backup_original']
    
    if not audio_files:
        print(f"  ⏭️  No audio files to process")
        return 0, 0, 0
    
    print(f"  📁 Found {len(audio_files)} audio files")
    
    success_count = 0
    skipped_count = 0
    error_count = 0
    
    for i, audio_file in enumerate(audio_files, 1):
        if i % 10 == 0:
            print(f"    Progress: {i}/{len(audio_files)} files processed...")
        try:
            success, reason = process_audio_file(audio_file)
            if success:
                success_count += 1
            else:
                skipped_count += 1
                # Only print skip reasons for non-common cases
                if reason not in ["no_translation", "too_complex", "complex_phrase"]:
                    print(f"    ⏭️  Skipped {audio_file.name}: {reason}")
        except Exception as e:
            error_count += 1
            print(f"    ❌ Error processing {audio_file.name}: {e}")
    
    return success_count, skipped_count, error_count

def main():
    """Main function"""
    print("🎤 Comprehensive Game Audio Translation Script")
    print("=" * 60)
    
    # Check dependencies
    if not check_dependencies():
        print("\n❌ Please install missing dependencies and try again.")
        sys.exit(1)
    
    # Create directories
    create_directories()
    
    if not BASE_DIR.exists():
        print(f"❌ Base directory not found: {BASE_DIR}")
        sys.exit(1)
    
    # Process each game directory
    total_success = 0
    total_skipped = 0
    total_errors = 0
    games_processed = 0
    
    game_dirs = [d for d in BASE_DIR.iterdir() if d.is_dir()]
    
    print(f"\n📂 Found {len(game_dirs)} game directories")
    print("Starting translation process...\n")
    
    for game_dir in sorted(game_dirs):
        success, skipped, errors = process_game_directory(game_dir)
        total_success += success
        total_skipped += skipped
        total_errors += errors
        if success > 0:
            games_processed += 1
    
    print(f"\n" + "=" * 60)
    print(f"🎉 Translation Summary")
    print(f"  ✅ Successfully processed: {total_success} files")
    print(f"  ⏭️  Skipped: {total_skipped} files")
    print(f"  ❌ Errors: {total_errors} files")
    print(f"  🎮 Games processed: {games_processed}/{len(game_dirs)}")
    print(f"  📂 Files saved in: {BASE_DIR}")
    
    # Clean up temp directory
    subprocess.run(['rm', '-rf', TEMP_DIR], check=True)
    print("  🧹 Cleaned up temporary files")
    
    if total_success > 0:
        print("\n💡 Note: Original files are backed up with '_backup' suffix")
        print("   You can restore them if needed")

if __name__ == "__main__":
    main()
