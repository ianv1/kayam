#!/usr/bin/env python3
"""
Translate wordwindow TSV file and generate Malay audio files.
- Translates problemSentence and exampleWord fields
- Replaces English names with Malay/Iban names (except Maya)
- Keeps filenames and headers unchanged
- Generates Malay audio files for all sound files
- Uses correct Malay grammar and vocabulary
"""

import os
import sys
import subprocess
import tempfile
import time
import re
from pathlib import Path
from gtts import gTTS
import csv

TSV_PATH = Path('/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/wordwindow/wordwindow_level.tsv')
BACKUP_TSV_PATH = Path('/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/wordwindow/wordwindow_level_backup.tsv')
SOUND_DIR = Path('/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/wordwindow/sound')

# Word translations dictionary
WORD_TRANSLATIONS = {
    'eggs': 'telur',
    'birds': 'burung',
    'stars': 'bintang',
    'books': 'buku',
    'yams': 'ubi',
    'apples': 'epal',
    'fish': 'ikan',
    'marbles': 'guli',
    'stamps': 'setem',
    'hats': 'topi',
    'puzzles': 'teka-teki',
    'chickens': 'ayam',
    'hen': 'ayam',
    'hens': 'ayam',
    'years old': 'tahun',
    'tables': 'meja',
    'kites': 'layang-layang',
    'cookies': 'biskut',
    'loaves': 'ketul',
    'bracelet': 'gelang',
    'bracelets': 'gelang',
    'nests': 'sarang',
    'blocks': 'blok',
    'years': 'tahun',
    'seeds': 'benih',
    'plates': 'pinggan',
    'pages': 'muka surat',
    'balloons': 'belon',
    'butterflies': 'rama-rama',
    'students': 'pelajar',
    'oranges': 'oren',
    'children': 'kanak-kanak',
    'rows': 'baris',
    'beads': 'manik',
    'balls': 'bola',
    'pencils': 'pensel',
    'hours': 'jam',
    'plums': 'plum',
    'days': 'hari',
    'mails': 'surat',
    'problems': 'masalah',
    'mangos': 'mangga',
    'sheep': 'biri-biri',
    'flowers': 'bunga',
    'airplanes': 'kapal terbang',
    'pictures': 'gambar',
    'gifts': 'hadiah',
    'daughters': 'anak perempuan',
    'green shirts': 'baju hijau',
    'girls': 'budak perempuan',
    'boys': 'budak lelaki',
    'fireflies': 'kelip-kelip',
    'inches': 'inci',
    'letters': 'surat',
    'lambs': 'anak biri-biri',
    'legs': 'kaki',
    'minutes': 'minit',
    'donuts': 'donat',
    'sweets': 'gula-gula',
    'candies': 'gula-gula',
    'candles': 'lilin',
    'brownies': 'brownies',
    'vases': 'pasu',
}

# English to Malay/Iban name mapping (keep Maya as Maya)
NAME_MAPPING = {
    'Jack': 'Ahmad',
    'Tom': 'Ali',
    'James': 'Hassan',
    'Billy': 'Razak',
    'Henry': 'Zainal',
    'Eric': 'Farid',
    'Bobby': 'Amir',
    'Gabby': 'Bakar',
    'Deanna': 'Siti',
    'Fatou': 'Aminah',
    'Thomas': 'Ismail',
    'Sarah': 'Fatimah',
    'Katie': 'Halimah',
    'Stacie': 'Mariam',
    'Maya': 'Maya',  # Keep original
    'Shelby': 'Rahmah',
    'Ana': 'Salmah',
    'Hannah': 'Zainab',
    'Millie': 'Kartini',
    'Sabrina': 'Jamaliah',
    'Sam': 'Daud',
    'Lucy': 'Latifah',
    'Julie': 'Zuraidah',
    'Amy': 'Hasnah',
    'Jane': 'Noriza',
    'Kami': 'Kami',
    'Maria': 'Maria',
    'Stacy': 'Siti',
    'Tina': 'Tina',
    'Beth': 'Aminah',
    'Anna': 'Nor',
    'Nicole': 'Nurul',
    'Ernest': 'Ibrahim',
    'Fred': 'Hamid',
    'Paul': 'Malik',
    'Brian': 'Razali',
    'Bob': 'Azman',
    'Adam': 'Hafiz',
    'Brad': 'Kamal',
    'David': 'Yusof',
    'Dean': 'Salim',
    'George': 'Salleh',
    'Jacob': 'Yakub',
    'Jaden': 'Jamil',
    'Joe': 'Johari',
    'Josh': 'Johan',
    'Kyle': 'Khairul',
    'Michael': 'Muhammad',
    'Mike': 'Mikail',
    'Nassir': 'Nassir',
    'Nick': 'Nik',
    'Noah': 'Nuh',
    'Peter': 'Petrus',
    'Tim': 'Timothy',
    'Zach': 'Zakaria',
    'Allie': 'Alia',
    'Anita': 'Anita',
    'Audrey': 'Aida',
    'Brittany': 'Baiti',
    'Ellen': 'Elena',
    'Harper': 'Hafizah',
    'Helen': 'Helena',
    'Jackie': 'Jasmin',
    'Kelly': 'Kalsom',
    'Kendra': 'Khadijah',
    'Kerri': 'Kartini',
    'Laci': 'Laila',
    'Lena': 'Lina',
    'Magan': 'Maznah',
    'Marco': 'Marzuki',
    'Marta': 'Marta',
    'Mary': 'Maryam',
    'Megan': 'Mazlina',
    'Mia': 'Mia',
    'Miranda': 'Mira',
    'Raven': 'Rafidah',
    'Sally': 'Salma',
    'Sandy': 'Sandy',
    'Sara': 'Sara',
    'Shante': 'Shanti',
    'Sharon': 'Sharifah',
    'Shelly': 'Shahirah',
    'Sue': 'Suzana',
    'Sydney': 'Siti',
    'Tess': 'Tessa',
    'Marin': 'Marina',
    'Simpson': 'Simson',
    'Dawson': 'Daud',
    'Jones': 'Johan',
    'Bloom': 'Bahar',
}

# Translation cache
_translation_cache = {}
_translator = None

def get_translator():
    """Get or create translator instance."""
    global _translator
    if _translator is None:
        try:
            from deep_translator import GoogleTranslator
            _translator = GoogleTranslator(source='en', target='ms')
        except ImportError:
            print("⚠️  deep-translator not available. Installing...")
            subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'deep-translator'], 
                                cwd='/Users/ian/projects/kayam/kayam')
            from deep_translator import GoogleTranslator
            _translator = GoogleTranslator(source='en', target='ms')
    return _translator

def translate_to_malay(english_text):
    """Translate English text to Malay using deep-translator with caching."""
    if not english_text or not english_text.strip():
        return english_text
    
    # Check cache
    if english_text in _translation_cache:
        return _translation_cache[english_text]
    
    try:
        translator = get_translator()
        result = translator.translate(english_text)
        _translation_cache[english_text] = result
        time.sleep(0.1)  # Rate limiting
        return result
    except Exception as e:
        print(f"    ⚠️  Translation error: {e}")
        return english_text

def fix_malay_verbs(text):
    """Fix common verb translation errors in Malay."""
    # Fix "lay eggs" -> should be "bertelur" not "meletakkan telur"
    text = re.sub(r'meletakkan\s+(\d+)\s+telur', r'bertelur \1 biji telur', text, flags=re.IGNORECASE)
    text = re.sub(r'meletakkan\s+telur', 'bertelur', text, flags=re.IGNORECASE)
    
    # Fix "laid eggs" -> "bertelur"
    text = re.sub(r'\bmeletakkan\b.*?\btelur\b', 'bertelur', text, flags=re.IGNORECASE)
    
    # Fix "served" (food) -> "menghidangkan" not "berkhidmat"
    text = re.sub(r'\bberkhidmat\b', 'menghidangkan', text, flags=re.IGNORECASE)
    
    # Ensure consistent use of "biji telur" for counted eggs
    text = re.sub(r'(\d+)\s+telur\b(?!\s+(adalah|ialah|merupakan))', r'\1 biji telur', text)
    
    # Fix "How many eggs did the hen lay?" -> "Berapa biji telur yang telah dihasilkan oleh ayam"
    text = re.sub(r'Berapa\s+banyak\s+telur\s+yang\s+(dilakukan|diletakkan)\s+(oleh\s+)?ayam', 
                  'Berapa biji telur yang telah dihasilkan oleh ayam', text, flags=re.IGNORECASE)
    text = re.sub(r'yang\s+dilakukan\s+(oleh\s+)?ayam', 'yang telah dihasilkan oleh ayam', text, flags=re.IGNORECASE)
    text = re.sub(r'yang\s+diletakkan\s+(oleh\s+)?ayam', 'yang telah dihasilkan oleh ayam', text, flags=re.IGNORECASE)
    
    return text

def fix_malay_possessives(text, name_mapping):
    """Fix possessive constructions in Malay: 'Name's noun' -> 'noun Name'"""
    # First, ensure "hen" is translated to "ayam" if it appears
    text = re.sub(r'\b[Hh]en\b', 'ayam', text)
    text = re.sub(r'\b[Hh]ens\b', 'ayam', text)
    
    # Fix verb errors first
    text = fix_malay_verbs(text)
    
    # Pattern to match "Name's noun" where Name is in our mapping
    for en_name, ms_name in name_mapping.items():
        # Match "Name's word" and convert to "word Name"
        pattern = r'\b' + re.escape(ms_name) + r"'s\s+(\w+)"
        def replace_func(match):
            noun = match.group(1)
            # In Malay, possessives are "noun Name" not "Name's noun"
            return f"{noun} {ms_name}"
        text = re.sub(pattern, replace_func, text)
        
        # Remove standalone "Name's" 
        text = re.sub(r'\b' + re.escape(ms_name) + r"'s\b", ms_name, text)
        
        # Fix "Name's" followed by Malay words (already translated)
        pattern2 = r'\b' + re.escape(ms_name) + r"'s\s+(ayam|telur|burung|buku|epal|ikan|guli|setem|topi|teka-teki|meja|layang-layang|biskut|gelang|sarang|blok|tahun|benih|pinggan|muka surat|belon|rama-rama|pelajar|oren|kanak-kanak|baris|manik|bola|pensel|jam|plum|hari|surat|masalah|mangga|biri-biri|bunga|kapal terbang|gambar|hadiah|anak perempuan|baju hijau|budak perempuan|budak lelaki|kelip-kelip|inci|anak biri-biri|kaki|minit|donat|gula-gula|lilin|brownies|pasu)\b"
        def replace_func2(match):
            noun = match.group(1)
            return f"{noun} {ms_name}"
        text = re.sub(pattern2, replace_func2, text, flags=re.IGNORECASE)
    
    return text

def replace_names_in_text(text, name_mapping):
    """Replace English names with Malay/Iban names in text."""
    # Common words that should never be replaced
    protected_words = {'and', 'or', 'the', 'a', 'an', 'in', 'on', 'at', 'to', 'for', 'of', 'with', 
                      'by', 'from', 'as', 'is', 'are', 'was', 'were', 'be', 'been', 'being', 'have',
                      'has', 'had', 'do', 'does', 'did', 'will', 'would', 'could', 'should', 'may',
                      'might', 'can', 'must', 'shall', 'if', 'then', 'else', 'when', 'where', 'what',
                      'who', 'which', 'how', 'why', 'all', 'some', 'any', 'many', 'much', 'more',
                      'most', 'few', 'little', 'each', 'every', 'both', 'either', 'neither', 'one',
                      'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine', 'ten'}
    
    # Sort by length (longest first) to avoid partial replacements
    sorted_names = sorted(name_mapping.items(), key=lambda x: len(x[0]), reverse=True)
    
    for en_name, ms_name in sorted_names:
        # Skip if it's a protected word
        if en_name.lower() in protected_words:
            continue
        
        # Handle possessive: "Name's" -> "ms_name's" (we'll fix to Malay format after translation)
        text = re.sub(r'\b' + re.escape(en_name) + r"'s\b", ms_name + "'s", text)
        
        # Replace standalone name
        text = re.sub(r'\b' + re.escape(en_name) + r'\b', ms_name, text)
    
    return text

def generate_malay_audio(text, output_path):
    """Generate Malay audio using gTTS."""
    tts = gTTS(text=text, lang='ms', slow=False)
    
    with tempfile.NamedTemporaryFile(suffix='.mp3', delete=False) as tmp:
        tts.save(tmp.name)
        tmp_mp3 = tmp.name
    
    try:
        subprocess.run([
            'ffmpeg', '-y', '-i', tmp_mp3,
            '-c:a', 'aac', '-b:a', '128k',
            str(output_path)
        ], check=True, capture_output=True)
    finally:
        try:
            os.unlink(tmp_mp3)
        except:
            pass

def get_duration(file_path):
    """Get audio file duration in HH:MM:SS.CC format."""
    out = subprocess.check_output([
        'ffprobe', '-v', 'error', '-show_entries', 'format=duration',
        '-of', 'default=noprint_wrappers=1:nokey=1', str(file_path)
    ]).decode().strip()
    
    try:
        duration = float(out)
        cs = int(round(duration * 100))
        s = cs // 100; cs = cs % 100
        m = s // 60; s = s % 60
        h = m // 60; m = m % 60
        return f"{h:02d}:{m:02d}:{s:02d}.{cs:02d}"
    except:
        return "00:00:00.00"

def main():
    print("📝 WordWindow Translation Tool (Final Version)")
    print("=" * 60)
    
    # Check dependencies
    try:
        import gtts
        print("✓ gTTS available")
    except ImportError:
        print("✗ gTTS not found")
        sys.exit(1)
    
    # Ensure backup exists
    if not BACKUP_TSV_PATH.exists():
        if TSV_PATH.exists():
            import shutil
            shutil.copy2(TSV_PATH, BACKUP_TSV_PATH)
            print(f"📁 Created backup: {BACKUP_TSV_PATH}")
        else:
            print(f"❌ TSV file not found: {TSV_PATH}")
            sys.exit(1)
    
    # Restore from backup to start fresh
    if TSV_PATH.exists():
        import shutil
        shutil.copy2(BACKUP_TSV_PATH, TSV_PATH)
        print(f"📁 Restored TSV from backup")
    
    # Read TSV file
    print(f"\n📖 Reading TSV file: {TSV_PATH}")
    rows = []
    header = None
    
    with open(TSV_PATH, 'r', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter='\t')
        header = next(reader)
        rows = list(reader)
    
    print(f"Found {len(rows)} data rows")
    
    # Find column indices
    prob_sent_idx = header.index('#problemSentence')
    example_word1_idx = header.index('#exampleWord1')
    example_word2_idx = header.index('#exampleWord2')
    example_word3_idx = header.index('#exampleWord3')
    example_word4_idx = header.index('#exampleWord4')
    voice_source_idx = header.index('#voiceSource')
    
    # Create sound backup directory
    sound_backup_dir = SOUND_DIR / 'backup_original'
    sound_backup_dir.mkdir(exist_ok=True)
    
    # Map voiceSource to problemSentence for audio generation
    voice_to_sentence = {}
    for row in rows:
        if len(row) > voice_source_idx and len(row) > prob_sent_idx:
            voice_file = row[voice_source_idx].strip()
            sentence = row[prob_sent_idx].strip()
            if voice_file and sentence:
                voice_to_sentence[voice_file] = sentence
    
    print(f"\n🔊 Found {len(voice_to_sentence)} unique audio files to generate")
    
    # Translate and update TSV
    print("\n🔄 Translating TSV content...")
    translated_count = 0
    
    for i, row in enumerate(rows):
        if len(row) <= prob_sent_idx:
            continue
        
        # Translate problem sentence
        if row[prob_sent_idx]:
            english_sent = row[prob_sent_idx]
            # First replace names in English
            english_with_names = replace_names_in_text(english_sent, NAME_MAPPING)
            # Then translate to Malay
            malay_sent = translate_to_malay(english_with_names)
            # Fix possessive constructions in Malay
            malay_sent = fix_malay_possessives(malay_sent, NAME_MAPPING)
            row[prob_sent_idx] = malay_sent
            translated_count += 1
            if (i + 1) % 10 == 0:
                print(f"  Translated {i + 1}/{len(rows)} rows...")
        
        # Translate example words
        for word_idx in [example_word1_idx, example_word2_idx, example_word3_idx, example_word4_idx]:
            if len(row) > word_idx and row[word_idx]:
                word = row[word_idx].strip()
                if word in WORD_TRANSLATIONS:
                    row[word_idx] = WORD_TRANSLATIONS[word]
    
    # Write translated TSV
    print(f"\n💾 Writing translated TSV...")
    with open(TSV_PATH, 'w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter='\t', lineterminator='\n')
        writer.writerow(header)
        writer.writerows(rows)
    print(f"✅ TSV updated with {translated_count} translated sentences")
    
    # Generate audio files
    print(f"\n🎵 Generating Malay audio files...")
    durations = {}
    processed = 0
    
    # Load existing durations if any
    dur_file = SOUND_DIR / 'durations.tsv'
    if dur_file.exists():
        for line in dur_file.read_text(encoding='utf-8').splitlines():
            if line.strip():
                parts = line.split('\t')
                if len(parts) == 2:
                    durations[parts[0]] = parts[1]
    
    for voice_file, english_sent in voice_to_sentence.items():
        audio_path = SOUND_DIR / voice_file
        if not audio_path.exists():
            print(f"  ⚠️  Audio file not found: {voice_file}")
            continue
        
        # Translate sentence (same process as TSV)
        english_with_names = replace_names_in_text(english_sent, NAME_MAPPING)
        malay_sent = translate_to_malay(english_with_names)
        malay_sent = fix_malay_possessives(malay_sent, NAME_MAPPING)
        
        print(f"  🔄 {voice_file}: '{english_sent[:50]}...' → '{malay_sent[:50]}...'")
        
        # Backup original
        backup_audio = sound_backup_dir / voice_file
        if not backup_audio.exists():
            audio_path.rename(backup_audio)
        
        # Generate Malay audio
        try:
            generate_malay_audio(malay_sent, audio_path)
            duration = get_duration(audio_path)
            durations[voice_file] = duration
            processed += 1
            print(f"    ✅ Generated, duration: {duration}")
        except Exception as e:
            print(f"    ❌ Error: {e}")
            # Restore backup if exists
            if backup_audio.exists():
                backup_audio.replace(audio_path)
    
    # Update durations.tsv
    if processed > 0:
        out_lines = [f"{k}\t{v}" for k, v in sorted(durations.items())]
        dur_file.write_text('\n'.join(out_lines) + '\n', encoding='utf-8')
        print(f"\n📝 Updated durations.tsv with {processed} files")
    
    print(f"\n🎉 Translation complete!")
    print(f"  ✅ Translated {translated_count} sentences in TSV")
    print(f"  ✅ Replaced English names with Malay/Iban names (kept Maya)")
    print(f"  ✅ Generated {processed} audio files")
    print(f"  📁 Backups saved in: {BACKUP_TSV_PATH} and {sound_backup_dir}")

if __name__ == '__main__':
    main()
