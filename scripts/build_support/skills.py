import hashlib
import json
import shutil
import uuid
from pathlib import Path


def inventory(folder: Path) -> dict[str, str]:
    if not folder.is_dir():
        return {}
    return {
        path.relative_to(folder).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
        for path in sorted(folder.rglob("*")) if path.is_file()
    }


def sync_skills(root: Path, *, check: bool = False) -> None:
    root = root.resolve()
    source = root / ".codex/skills"
    mirror = root / ".claude/skills"
    baseline_path = root / "build/cache/skill-sync.json"
    if not source.is_dir():
        raise ValueError("Missing skill source: .codex/skills")
    source_files, mirror_files = inventory(source), inventory(mirror)
    if check:
        if source_files != mirror_files:
            differences = sorted(name for name in source_files.keys() | mirror_files.keys()
                                 if source_files.get(name) != mirror_files.get(name))
            raise ValueError("Skill mirrors differ: " + ", ".join(differences))
        print(f"  Skills         {len(source_files)} matching files")
        return
    if baseline_path.is_file():
        baseline = json.loads(baseline_path.read_text(encoding="utf-8"))
        drift = sorted(name for name in baseline.keys() | mirror_files.keys()
                       if baseline.get(name) != mirror_files.get(name))
    else:
        drift = sorted(name for name in mirror_files
                       if source_files.get(name) != mirror_files[name])
    if drift:
        raise ValueError("Independent Claude changes detected: " + ", ".join(drift))
    backup = root / "build/skill-sync/backups" / uuid.uuid4().hex
    for name in sorted(source_files.keys() | mirror_files.keys()):
        if source_files.get(name) == mirror_files.get(name):
            continue
        destination = (mirror / name).resolve()
        if not destination.is_relative_to(mirror.resolve()):
            raise ValueError("Skill destination escapes .claude/skills")
        if destination.exists():
            saved = backup / name
            saved.parent.mkdir(parents=True, exist_ok=True)
            destination.rename(saved)
        if name in source_files:
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source / name, destination)
    if inventory(mirror) != source_files:
        raise ValueError("Skill synchronization verification failed")
    for directory in mirror.iterdir():
        if directory.is_dir() and not (source / directory.name).exists() and not inventory(directory):
            saved = backup / "empty-directories" / directory.name
            saved.parent.mkdir(parents=True, exist_ok=True)
            directory.rename(saved)
    baseline_path.parent.mkdir(parents=True, exist_ok=True)
    baseline_path.write_text(json.dumps(source_files, indent=2) + "\n", encoding="utf-8")
    print(f"  Skills         {len(source_files)} synchronized files")
