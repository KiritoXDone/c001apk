import sys

sys.dont_write_bytecode = True

import argparse
import json
import os
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

os.environ["PYTHONDONTWRITEBYTECODE"] = "1"
ROOT = Path(__file__).resolve().parents[2]


def git(*args):
    return subprocess.run(
        ["git", *args], cwd=ROOT, check=True, text=True, encoding="utf-8",
        stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    ).stdout


def read_text(ref, path):
    return git("show", f"{ref}:{path}")


def read_application_metadata(ref):
    script = read_text(ref, "app/build.gradle.kts")
    versions = re.findall(
        r'val\s+appVersion\s*=\s*providers\s*\.gradleProperty\("appVersion"\)'
        r'\s*\.orElse\("([^"\n]+)"\)\s*\.get\(\)', script,
    )
    if len(versions) != 1:
        raise ValueError("Expected one default appVersion provider in app/build.gradle.kts.")
    if re.fullmatch(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)", versions[0]) is None:
        raise ValueError("Expected the application version in major.minor.patch format.")
    try:
        resources = ET.fromstring(read_text(ref, "app/src/main/res/values/strings.xml"))
    except ET.ParseError as error:
        raise ValueError("Cannot parse the default application string resources.") from error
    names = [item.text for item in resources.findall("string") if item.get("name") == "app_name"]
    if len(names) != 1 or not names[0]:
        raise ValueError("Expected one nonempty app_name string resource.")
    return {"app_name": names[0], "artifact_name": names[0], "version": versions[0]}


def error_detail(error):
    if isinstance(error, subprocess.CalledProcessError):
        return (error.stderr or "").strip() or f"Command exited with status {error.returncode}."
    if isinstance(error, OSError):
        return f"{type(error).__name__}: errno={error.errno}; filename={error.filename}"
    return str(error)


def check_version_history(source, after):
    event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text(encoding="utf-8"))
    before = event.get("before")
    if not before:
        source["skip_reason"] = "The push event does not identify the previous commit."
        return
    if set(before) == {"0"}:
        source["skip_reason"] = "A newly created branch has no previous push commit to compare."
        return
    before_commit = subprocess.run(
        ["git", "cat-file", "-e", f"{before}^{{commit}}"], cwd=ROOT,
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
    )
    if before_commit.returncode != 0:
        source["skip_reason"] = f"The previous push commit is unavailable in the checkout: {before}."
        return
    versions = {after: source["version"]}

    def version_at(ref):
        if ref not in versions:
            try:
                versions[ref] = read_application_metadata(ref)["version"]
            except (KeyError, ValueError, OSError, subprocess.CalledProcessError) as error:
                raise ValueError(f"Cannot read the application version at commit {ref}: {error_detail(error)}") from error
        return versions[ref]

    try:
        if version_at(before) != version_at(after):
            source["version_changed"] = True
            return
        commits = git("rev-list", "--parents", "--reverse", after, f"^{before}")
        for line in commits.splitlines():
            commit, *parents = line.split()
            if not parents:
                source["skip_reason"] = f"Commit {commit} has no parent for the version comparison."
                return
            if version_at(commit) != version_at(parents[0]):
                source["version_changed"] = True
                return
    except ValueError as error:
        source["skip_reason"] = str(error)


def read_github_event():
    after = os.environ["GITHUB_SHA"]
    source = read_application_metadata(after)
    source["version_changed"] = False
    if os.environ["GITHUB_EVENT_NAME"] == "push" and os.environ["GITHUB_REF"] in ("refs/heads/main", "refs/heads/beta"):
        check_version_history(source, after)
    destination = ROOT / "build" / "ci" / "source.json"
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(source, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("Saved application metadata and version history: build/ci/source.json")


def main():
    parser = argparse.ArgumentParser(description="Read application metadata and check pushed version changes.")
    parser.add_argument("--github-event", action="store_true", help="Read the current GitHub event and save release metadata.")
    args = parser.parse_args()
    if not args.github_event:
        parser.print_help()
        return 0
    try:
        read_github_event()
    except (KeyError, ValueError, OSError, subprocess.CalledProcessError) as error:
        print("Application metadata check failed.", file=sys.stderr)
        print(error_detail(error), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
