import sys

sys.dont_write_bytecode = True

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))
sys.path.insert(0, str(ROOT / "tools"))

from prebuild import check_checkout, detect_sdk, find_portable_jdk, is_project_jdk
from release_ci import load_plan, package_files
from verify_apk import verify


def check_environment(_args):
    try:
        check_checkout()
        sdk = detect_sdk()
    except SystemExit as error:
        raise ValueError("Checkout or Android SDK is not prepared. Run scripts/prebuild.py first.") from error
    jdk = find_portable_jdk()
    if jdk is None:
        configured = os.environ.get("JAVA_HOME")
        if not configured or not is_project_jdk(Path(configured)):
            raise ValueError("JDK 21 is not prepared. Run scripts/prebuild.py first.")
    script = (ROOT / "app" / "build.gradle.kts").read_text(encoding="utf-8")
    platform = re.search(r'compileSdkVersion\("android-([^"\n]+)"\)', script)
    if platform is None:
        platform = re.search(r"compileSdk\s*=\s*([0-9]+)", script)
    if platform is None:
        raise ValueError("Cannot read the compile SDK for app.")
    if not (sdk / "platforms" / f"android-{platform[1]}" / "android.jar").is_file():
        raise ValueError(f"Android SDK platform android-{platform[1]} is missing for app.")
    tools = sdk / "build-tools" / "36.0.0"
    if not tools.is_dir():
        raise ValueError("Android build tools 36.0.0 are missing.")
    print("Prepared checkout, JDK 21 and explicit Android SDK packages verified offline.")


def package(_args):
    plan = load_plan()
    target = os.environ["TARGET_ID"]
    entry = next(item for item in plan["matrix"]["include"] if item["target"] == target)
    if (entry["platform"], entry["arch"], entry["abi"]) != ("android", "arm64", "arm64-v8a"):
        raise ValueError("Expected the existing Android ARM64 build target.")
    if (os.environ["TARGET_PLATFORM"], os.environ["TARGET_ARCH"], os.environ["TARGET_ABI"]) != (
        entry["platform"], entry["arch"], entry["abi"],
    ):
        raise ValueError("Build target environment differs from the release plan.")
    if os.environ["APP_VERSION"] != plan["version"]:
        raise ValueError("Build version differs from the release plan.")
    metadata = json.loads((ROOT / "build/gradle/app/outputs/apk/release/output-metadata.json").read_text(encoding="utf-8"))
    if len(metadata["elements"]) != 1:
        raise ValueError("Expected exactly one Release APK in Gradle output metadata.")
    item = metadata["elements"][0]
    version_code = int(subprocess.check_output(
        ["git", "rev-list", plan["sha"], "--count"], cwd=ROOT, text=True,
    ).strip())
    if item["versionName"] != plan["version"] or item["versionCode"] != version_code:
        raise ValueError("Gradle output version differs from the planned version and full Git history.")
    apk = ROOT / "build/apk/release" / f"c001apk-release-arm64-v8a-{plan['version']}.{version_code}.apk"
    certificate = os.environ["EXPECTED_CERTIFICATE"].strip()
    if not certificate:
        raise ValueError("The expected Release signing certificate is missing.")
    report = verify(apk, detect_sdk(), certificate)
    if report["versionName"] != plan["version"] or report["versionCode"] != version_code or report["abi"] != entry["abi"]:
        raise ValueError("Verified APK version or ABI differs from the release plan.")
    directory = ROOT / "build/ci/packages" / target
    directory.mkdir(parents=True, exist_ok=True)
    prefix = f"{plan['artifact_name']}-v{plan['version']}-{target}"
    public_apk = directory / f"{prefix}.apk"
    shutil.copy2(apk, public_apk)
    report["apk"] = public_apk.name
    (directory / f"{prefix}.apk.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    (directory / f"{prefix}.sha256").write_text(report["sha256"] + "  " + public_apk.name + "\n", encoding="utf-8")
    files = {}
    for path in sorted(directory.iterdir()):
        if not path.is_file() or path.is_symlink() or path.stat().st_size == 0:
            raise ValueError(f"Expected a nonempty Release package: {path.name}")
        with path.open("rb") as stream:
            digest = hashlib.file_digest(stream, "sha256").hexdigest()
        files[path.name] = {"bytes": path.stat().st_size, "sha256": digest}
    manifest = {"sha": plan["sha"], "version": plan["version"], "target": target, "files": files}
    (directory / f"{prefix}.manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    package_files(directory, target, plan)
    print(f"Verified and packaged signed Android ARM64 APK: {public_apk.name}")


def main():
    parser = argparse.ArgumentParser(description="Check prepared Android inputs and package a verified Release APK.")
    commands = parser.add_subparsers(dest="command")
    check = commands.add_parser("check-environment", help="Check prepared tools and SDK packages without downloading.")
    check.set_defaults(action=check_environment)
    packages = commands.add_parser("package", help="Verify the APK and save packages with a complete file manifest.")
    packages.set_defaults(action=package)
    args = parser.parse_args()
    if args.command is None:
        parser.print_help()
        return 0
    try:
        args.action(args)
    except (KeyError, ValueError, OSError, subprocess.CalledProcessError, StopIteration) as error:
        print(f"Android release check failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
