#!/usr/bin/env python3
"""Verify a locally built APK without accepting a different package, ABI or signer."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile


def certificate_digest(report):
    values = re.findall(r"(?m)^\s*(?:Signer #\d+|Signer) certificate SHA-256 digest:\s*([0-9a-fA-F:]+)", report)
    normalized = {value.replace(":", "").lower() for value in values}
    if len(normalized) != 1 or not re.fullmatch(r"[0-9a-f]{64}", next(iter(normalized), "")):
        raise ValueError("Expected exactly one signing certificate SHA-256 digest")
    return normalized.pop()


def verify(apk, sdk, expected_certificate=None):
    build_tools = sdk / "build-tools" / "36.0.0"
    environment = os.environ.copy()
    if environment.get("JAVA_HOME"):
        environment["PATH"] = str(Path(environment["JAVA_HOME"]) / "bin") + os.pathsep + environment.get("PATH", "")
    def run(name, *args):
        return subprocess.check_output([str(build_tools / name), *map(str, args)], text=True, stderr=subprocess.STDOUT, env=environment)
    signature = run("apksigner", "verify", "--verbose", "--print-certs", apk)
    digest = certificate_digest(signature)
    if expected_certificate and digest != expected_certificate.replace(":", "").strip().lower():
        raise ValueError("APK signing certificate does not match the expected certificate")
    verified_v2 = bool(re.search(r"Verified using v2 scheme.*:\s*true", signature))
    verified_v3 = bool(re.search(r"Verified using v3(?:\.1)? scheme.*:\s*true", signature))
    if not verified_v2:
        raise ValueError("APK has no verified v2 signature required for Android 7")
    run("zipalign", "-c", "-P", "16", "-v", "4", apk)
    badging = run("aapt2", "dump", "badging", apk)
    package = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", badging)
    if not package or package[1] != "com.example.c001apk.compose":
        raise ValueError("Unexpected application ID")
    if not re.search(r"(?m)^(?:minSdkVersion|sdkVersion):'24'$", badging):
        raise ValueError("Expected Android 7 minimum SDK")
    with zipfile.ZipFile(apk) as archive:
        for required in ("okhttp3/internal/publicsuffix/publicsuffixes.gz", "org/commonmark/internal/util/entities.txt"):
            if required not in archive.namelist():
                raise ValueError(f"Required runtime resource is missing: {required}")
        native = [item for item in archive.infolist() if item.filename.startswith("lib/") and item.filename.endswith(".so")]
        if not native or {item.filename.split('/')[1] for item in native} != {"arm64-v8a"}:
            raise ValueError("Expected only ARM64 native libraries")
        if any(item.filename.endswith("/libdu.so") for item in native):
            raise ValueError("Legacy native Digital Union SDK must not be packaged")
        if any(item.compress_type != zipfile.ZIP_DEFLATED for item in native):
            raise ValueError("Expected compressed native libraries")
    return {"apk": str(apk), "package": package[1], "versionCode": int(package[2]), "versionName": package[3],
            "minSdk": 24, "abi": "arm64-v8a", "certificateSha256": digest,
            "sha256": hashlib.sha256(apk.read_bytes()).hexdigest(), "bytes": apk.stat().st_size,
            "verifiedV2": verified_v2, "verifiedV3": verified_v3, "zipalign16KiB": True, "compressedNativeLibraries": True}


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path)
    parser.add_argument("--sdk", type=Path, default=Path(os.environ.get("ANDROID_HOME", os.environ.get("ANDROID_SDK_ROOT", ""))))
    parser.add_argument("--certificate-sha256")
    args = parser.parse_args()
    print(json.dumps(verify(args.apk, args.sdk, args.certificate_sha256), ensure_ascii=False, indent=2))
