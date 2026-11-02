#!/usr/bin/env python3
# 准备便携 JDK、Gradle 启动器检查与 Android SDK 定位。
import os
import sys

sys.dont_write_bytecode = True
os.environ["PYTHONDONTWRITEBYTECODE"] = "1"

import argparse
import hashlib
import json
import platform
import re
import shutil
import subprocess
import tarfile
import tempfile
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JDK_MAJOR = 21
WRAPPER_FILES = ("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar",
                 "gradle/wrapper/gradle-wrapper.properties")


def fail(message: str) -> None:
    raise SystemExit(f"错误：{message}")


def java_executable(home: Path) -> Path:
    return home / "bin" / ("java.exe" if os.name == "nt" else "java")


def is_project_jdk(home: Path) -> bool:
    release = home / "release"
    compiler = home / "bin" / ("javac.exe" if os.name == "nt" else "javac")
    if not release.is_file() or not java_executable(home).is_file() or not compiler.is_file():
        return False
    return re.search(rf'^JAVA_VERSION="{JDK_MAJOR}(?:\.|\+|")',
                     release.read_text(encoding="utf-8"), re.M) is not None


def find_portable_jdk() -> Path | None:
    folder = ROOT / "build/jdk"
    for home in (folder, *sorted(folder.glob("*/")), *sorted(folder.glob("*/Contents/Home"))):
        if is_project_jdk(home):
            return home
    return None


def download(url: str, target: Path) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": "c001apk-prebuild"})
    with urllib.request.urlopen(request, timeout=120) as response, target.open("wb") as output:
        shutil.copyfileobj(response, output)


def install_portable_jdk() -> Path:
    system = {"win32": "windows", "darwin": "mac", "linux": "linux"}.get(sys.platform)
    architecture = {"amd64": "x64", "x86_64": "x64", "arm64": "aarch64",
                    "aarch64": "aarch64"}.get(platform.machine().lower())
    if not system or not architecture:
        fail("当前系统或处理器没有对应的便携 JDK 下载配置")
    query = urllib.parse.urlencode({"os": system, "architecture": architecture,
                                    "image_type": "jdk", "jvm_impl": "hotspot", "vendor": "eclipse"})
    url = f"https://api.adoptium.net/v3/assets/latest/{JDK_MAJOR}/hotspot?{query}"
    request = urllib.request.Request(url, headers={"User-Agent": "c001apk-prebuild"})
    with urllib.request.urlopen(request, timeout=60) as response:
        package = json.load(response)[0]["binary"]["package"]
    print(f"准备下载 Temurin {JDK_MAJOR}（{system}/{architecture}）", flush=True)
    temporary_root = ROOT / "build/tmp"
    temporary_root.mkdir(parents=True, exist_ok=True)
    # 下载和解压失败时保留现场，不删除或覆盖已有工具链。
    stage = Path(tempfile.mkdtemp(prefix="jdk-", dir=temporary_root))
    archive = stage / package["name"]
    download(package["link"], archive)
    with archive.open("rb") as source:
        checksum = hashlib.file_digest(source, "sha256").hexdigest()
    if checksum != package["checksum"]:
        fail(f"JDK 校验和不匹配，下载文件保存在 {archive}")
    extracted = stage / "extracted"
    if zipfile.is_zipfile(archive):
        with zipfile.ZipFile(archive) as source:
            source.extractall(extracted)
    else:
        with tarfile.open(archive) as source:
            source.extractall(extracted, filter="data")
    candidates = [home for home in (*extracted.glob("*/"), *extracted.glob("*/Contents/Home"))
                  if is_project_jdk(home)]
    if len(candidates) != 1:
        fail(f"下载内容中未找到唯一的 JDK {JDK_MAJOR}：{extracted}")
    destination = ROOT / "build/jdk" / stage.name
    destination.parent.mkdir(parents=True, exist_ok=True)
    candidates[0].rename(destination)
    return destination


def resolve_jdk(*, portable: bool = False) -> Path:
    current = find_portable_jdk()
    if current:
        return current
    # CI 的 setup-java 已提供同版本 JDK，无需重复下载。
    configured = os.environ.get("JAVA_HOME")
    if not portable and configured and is_project_jdk(Path(configured)):
        return Path(configured).resolve()
    return install_portable_jdk()


def detect_sdk() -> Path:
    local = ROOT / "local.properties"
    if local.is_file():
        match = re.search(r"^sdk\.dir\s*=\s*(.+)$", local.read_text(encoding="utf-8"), re.M)
        if match:
            configured = match[1].strip().replace("\\:", ":").replace("\\\\", "\\")
            sdk = Path(configured).expanduser()
            if not sdk.is_absolute():
                sdk = ROOT / sdk
            if not (sdk / "platforms").is_dir():
                fail("local.properties 中的 sdk.dir 无效，请修正该配置")
            return sdk.resolve()
    for name in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        if value := os.environ.get(name):
            sdk = Path(value).expanduser()
            if not (sdk / "platforms").is_dir():
                fail(f"{name} 没有指向包含 platforms 的 Android SDK")
            return sdk.resolve()
    if os.name == "nt":
        sdk = Path(os.environ.get("LOCALAPPDATA", Path.home() / "AppData/Local")) / "Android/Sdk"
    elif sys.platform == "darwin":
        sdk = Path.home() / "Library/Android/sdk"
    else:
        sdk = Path.home() / "Android/Sdk"
    if (sdk / "platforms").is_dir():
        return sdk.resolve()
    fail("未找到 Android SDK，请设置 ANDROID_HOME 或 local.properties 中的 sdk.dir")


def check_checkout() -> None:
    missing = [name for name in WRAPPER_FILES if not (ROOT / name).is_file()]
    if missing:
        fail("缺少受版本控制的构建文件，请恢复：" + "、".join(missing))
    result = subprocess.run(["git", "rev-parse", "--is-shallow-repository"], cwd=ROOT,
                            text=True, capture_output=True, check=True)
    if result.stdout.strip() == "true":
        fail("当前为浅克隆，请补齐 Git 历史；versionCode 依赖完整提交数量")


def prepare(*, portable: bool = False) -> tuple[Path, Path]:
    check_checkout()
    sdk = detect_sdk()
    jdk = resolve_jdk(portable=portable)
    print(f"构建 JDK：{jdk}\nAndroid SDK：{sdk}", flush=True)
    return jdk, sdk


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="检查构建输入并准备 JDK 21 与 Android SDK")
    parser.add_argument("--portable", action="store_true", help="即使 JAVA_HOME 可用也准备项目便携 JDK")
    args = parser.parse_args()
    prepare(portable=args.portable)
    print("预构建检查完成")


if __name__ == "__main__":
    main()
