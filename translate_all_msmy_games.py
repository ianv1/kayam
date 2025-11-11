#!/usr/bin/env python3
"""
Translate all Malay-localized game audio under ms-my/games to Malay using gTTS.
- Skips folders already handled (shapematching/sound, numbertrain/sounds, numbervoice)
- Skips common SFX by pattern
- Derives text from filenames (underscores -> spaces) with per-known mappings
- Backs up originals to backup_original and replaces in place
- Updates durations.tsv with new durations using ffprobe

Usage:
  python3 translate_all_msmy_games.py [--dry-run] [--include <subpath> ...]
"""

import os
import sys
import argparse
import subprocess
from pathlib import Path
import tempfile
from typing import Dict, List, Tuple

ROOT_GAMES_DIR = Path("/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games")

# Folders we've already processed in this session
EXCLUDE_SUBPATHS = {
    # Relative to ROOT_GAMES_DIR
    Path("shapematching/sound"),
    Path("numbertrain/sounds"),
}
# Also exclude numbervoice at the locale root (handled already by another script)
EXCLUDE_ABSOLUTE = {
    Path("/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/numbervoice"),
    Path("/Users/ian/projects/kayam/kayam/mainapp/Resources/localized/ms-my/games/readingbird"),
}

# Heuristic SFX filename fragments (lowercase match)
SFX_KEYWORDS = {
    "ui_", "sfx_", "click", "tap", "tick", "whoosh", "star", "success", "fail", "error",
    "correct", "wrong", "win", "lose", "slot", "slotin", "bell", "ding", "pop", "beep",
    "background", "bgm", "music", "ambient", "shuffle", "drag", "drop", "sparkle", "coin",
    "timer", "countdown", "levelup", "next", "button", "swipe", "bounce", "boom", "explosion"
}

# Known per-filename English->Malay mappings used earlier
SHAPE_TRANSLATIONS = {
    'circle': 'bulat','cone': 'kon','cube': 'kubus','cylinder': 'silinder','diamond': 'berlian',
    'hexagon': 'heksagon','large': 'besar','medium': 'sederhana','octagon': 'oktagon','oval': 'oval',
    'parallelogram': 'segi empat selari','pentagon': 'pentagon','pyramid': 'piramid','rectangle': 'segiempat tepat',
    'rectangular_prism': 'prisma segi empat tepat','rhombus': 'rombus','small': 'kecil','sphere': 'sfera',
    'square': 'segi empat sama','star': 'bintang','trapezoid': 'trapezoid','triangle': 'segi tiga',
    'triangular_prism': 'prisma segi tiga'
}

NUMBERTRAIN_TRANSLATIONS = {
    'arrange_from_smallest_to_largest': 'susun nombor dari terkecil hingga terbesar',
    'largest_number': 'nombor terbesar'
}

MATH_OPERATIONS = {
    'and': 'dan','equals': 'sama dengan','minus': 'tolak','plus': 'tambah','times': 'darab'
}

EXTENSIONS = {".m4a", ".wav"}


def check_dependencies() -> None:
    try:
        import gtts  # noqa: F401
        print("✓ gTTS is available")
    except ImportError:
        print("✗ gTTS not found. Please run inside your venv and install: pip install gtts")
        sys.exit(1)
    try:
        subprocess.run(['ffmpeg', '-version'], check=True, capture_output=True)
        subprocess.run(['ffprobe', '-version'], check=True, capture_output=True)
        print("✓ ffmpeg/ffprobe are available")
    except Exception:
        print("✗ ffmpeg/ffprobe not found. Install with: brew install ffmpeg")
        sys.exit(1)


def is_sfx(filename: str) -> bool:
    name = filename.lower()
    return any(k in name for k in SFX_KEYWORDS)


def derive_text_from_filename(stem: str) -> str:
    # Known mappings first
    if stem in SHAPE_TRANSLATIONS:
        return SHAPE_TRANSLATIONS[stem]
    if stem in NUMBERTRAIN_TRANSLATIONS:
        return NUMBERTRAIN_TRANSLATIONS[stem]
    if stem in MATH_OPERATIONS:
        return MATH_OPERATIONS[stem]

    # SAFE RULES ONLY beyond this point. Return empty string to skip when unsafe.
    # 1) numbertrace style: num_01, num_10 ...
    if stem.startswith('num_'):
        digits = stem[4:]
        try:
            n = int(digits)
            # Accept 1..10 (seen in resources); extend as needed
            return number_to_malay(n)
        except Exception:
            return ""

    # 2) which_is_a_* or which_is_an_* (shapes only)
    if stem.startswith('which_is_a_'):
        key = stem[len('which_is_a_'):]
        if key in SHAPE_TRANSLATIONS:
            return f"yang manakah {SHAPE_TRANSLATIONS[key]}"
        return ""
    if stem.startswith('which_is_an_'):
        key = stem[len('which_is_an_'):]
        if key in SHAPE_TRANSLATIONS:
            return f"yang manakah {SHAPE_TRANSLATIONS[key]}"
        return ""

    # 3) simple math prompts we know
    PROMPT_MAP = {
        'which_is_the_biggest': 'yang manakah paling besar',
        'which_is_the_smallest': 'yang manakah paling kecil',
        'order_the_numbers_from_smallest_to_largest': 'susun nombor dari terkecil hingga terbesar',
        'order_the_numbers_from_the_smallest_to_the_largest': 'susun nombor dari terkecil hingga terbesar',
        'which_group_is_smaller': 'kumpulan mana lebih kecil',
    }
    if stem in PROMPT_MAP:
        return PROMPT_MAP[stem]

    # Otherwise, unsafe to auto-translate
    return ""


def speak_to_file(text: str, out_path: Path) -> None:
    from gtts import gTTS
    with tempfile.NamedTemporaryFile(suffix='.mp3', delete=False) as tmp:
        tts = gTTS(text=text, lang='ms', slow=False)
        tts.save(tmp.name)
        tmp_mp3 = tmp.name
    try:
        # Encode to same container/codec family
        if out_path.suffix == '.m4a':
            cmd = ['ffmpeg', '-y', '-i', tmp_mp3, '-c:a', 'aac', '-b:a', '128k', str(out_path)]
        elif out_path.suffix == '.wav':
            cmd = ['ffmpeg', '-y', '-i', tmp_mp3, '-c:a', 'pcm_s16le', str(out_path)]
        else:
            # Default to m4a
            cmd = ['ffmpeg', '-y', '-i', tmp_mp3, '-c:a', 'aac', '-b:a', '128k', str(out_path.with_suffix('.m4a'))]
        subprocess.run(cmd, check=True, capture_output=True)
    finally:
        try:
            os.unlink(tmp_mp3)
        except Exception:
            pass


def ffprobe_duration_seconds(file_path: Path) -> float:
    # Returns duration in seconds (float)
    cmd = [
        'ffprobe', '-v', 'error', '-show_entries', 'format=duration',
        '-of', 'default=noprint_wrappers=1:nokey=1', str(file_path)
    ]
    out = subprocess.check_output(cmd).decode('utf-8').strip()
    try:
        return float(out)
    except Exception:
        return 0.0


def format_hhmmss_ss(seconds: float) -> str:
    if seconds < 0:
        seconds = 0.0
    total_centiseconds = int(round(seconds * 100))
    cs = total_centiseconds % 100
    total_seconds = total_centiseconds // 100
    s = total_seconds % 60
    total_minutes = total_seconds // 60
    m = total_minutes % 60
    h = total_minutes // 60
    return f"{h:02d}:{m:02d}:{s:02d}.{cs:02d}"


def load_durations(durations_path: Path) -> Dict[str, str]:
    mapping: Dict[str, str] = {}
    if not durations_path.exists():
        return mapping
    for line in durations_path.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        parts = line.split('\t')
        if len(parts) == 2:
            mapping[parts[0]] = parts[1]
    return mapping


def save_durations(durations_path: Path, mapping: Dict[str, str]) -> None:
    lines = [f"{fname}\t{dur}" for fname, dur in sorted(mapping.items())]
    durations_path.write_text("\n".join(lines) + "\n", encoding='utf-8')


def should_skip_dir(dir_path: Path) -> bool:
    # Exclude absolute numbervoice dir
    if dir_path in EXCLUDE_ABSOLUTE:
        return True
    # Exclude specific subpaths under games
    try:
        rel = dir_path.relative_to(ROOT_GAMES_DIR)
        for ex in EXCLUDE_SUBPATHS:
            if rel.as_posix().lower() == ex.as_posix().lower():
                return True
    except ValueError:
        pass
    return False


def find_audio_files(root: Path) -> List[Path]:
    return [p for p in root.rglob('*') if p.is_file() and p.suffix.lower() in EXTENSIONS]


def process_dir(target_dir: Path, dry_run: bool) -> Tuple[int, int, int]:
    if should_skip_dir(target_dir):
        return (0, 0, 0)

    audio_files = find_audio_files(target_dir)
    if not audio_files:
        return (0, 0, 0)

    print(f"\n📂 {target_dir}")
    print(f"  Found {len(audio_files)} audio files")

    # Prepare backup dir
    backup_dir = target_dir / 'backup_original'
    if not dry_run:
        backup_dir.mkdir(exist_ok=True)

    # durations.tsv next to files if present in this directory
    durations_path = target_dir / 'durations.tsv'
    durations = load_durations(durations_path)

    processed = 0
    skipped = 0
    sfx_skipped = 0

    for af in audio_files:
        if af.parent == backup_dir:
            continue
        fname = af.name
        stem = af.stem
        if is_sfx(fname):
            print(f"  ⏭️  SFX skip: {fname}")
            sfx_skipped += 1
            continue

        text = derive_text_from_filename(stem)
        if not text:
            print(f"  ⏭️  No text derived: {fname}")
            skipped += 1
            continue

        print(f"  🔄 {fname}: '{stem}' → '{text}'")

        if dry_run:
            processed += 1
            continue

        # Backup
        backup_path = backup_dir / fname
        if not backup_path.exists():
            af.replace(backup_path)
        else:
            # If already backed up, assume previously processed; refresh output only
            pass

        # Generate new audio at original location
        out_path = af  # will write to original name
        try:
            speak_to_file(text, out_path)
            duration = ffprobe_duration_seconds(out_path)
            durations[fname] = format_hhmmss_ss(duration)
            print(f"    ✅ wrote, duration {durations[fname]}")
            processed += 1
        except Exception as e:
            print(f"    ❌ error: {e}")
            # Attempt restore
            if backup_path.exists():
                backup_path.replace(out_path)
            skipped += 1

    # Save durations.tsv if any processed
    if not dry_run and processed > 0:
        save_durations(durations_path, durations)
        print(f"  📝 durations.tsv updated ({durations_path})")

    return (processed, skipped, sfx_skipped)


def main():
    parser = argparse.ArgumentParser(description='Translate all ms-my games audio to Malay')
    parser.add_argument('--dry-run', action='store_true', help='do not modify files, just show actions')
    parser.add_argument('--include', nargs='*', default=None, help='limit to these relative subpaths under games/')
    args = parser.parse_args()

    print("🎮 ms-my Games Audio Translator")
    print("=" * 50)

    check_dependencies()

    if not ROOT_GAMES_DIR.exists():
        print(f"Root not found: {ROOT_GAMES_DIR}")
        sys.exit(1)

    targets: List[Path] = []
    if args.include:
        for sub in args.include:
            p = ROOT_GAMES_DIR / sub
            if p.exists():
                targets.append(p)
    else:
        # Walk one level deep: process any directory containing audio files
        # We'll process both leaf 'sound(s)' dirs and any dirs with audio
        for dirpath, dirnames, filenames in os.walk(ROOT_GAMES_DIR):
            dpath = Path(dirpath)
            # Consider each directory as a target if it contains audio files
            if any(Path(dirpath, f).suffix.lower() in EXTENSIONS for f in filenames):
                targets.append(dpath)

    # Deduplicate while preserving order
    seen = set()
    unique_targets = []
    for t in targets:
        if t in seen:
            continue
        seen.add(t)
        unique_targets.append(t)

    total_processed = total_skipped = total_sfx_skipped = 0

    for t in unique_targets:
        p, s, sfxs = process_dir(t, args.dry_run)
        total_processed += p
        total_skipped += s
        total_sfx_skipped += sfxs

    print("\n📊 Summary")
    print(f"  ✅ Processed: {total_processed}")
    print(f"  ⏭️  Skipped: {total_skipped}")
    print(f"  🎚️  SFX skipped: {total_sfx_skipped}")

if __name__ == '__main__':
    main()
