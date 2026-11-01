#!/usr/bin/env python3
# 统一执行 Gradle，并按实际输出元数据收集 APK。
import os
import sys

sys.dont_write_bytecode = True
os.environ["PYTHONDONTWRITEBYTECODE"] = "1"

import argparse
import json
import shutil
import subprocess
from pathlib import Path

from prebuild import ROOT, fail, java_executable, prepare


def gradle_environment(jdk: Path, sdk: Path) -> dict[str, str]:
    environment = {**os.environ, "JAVA_HOME": str(jdk), "ANDROID_HOME": str(sdk),
                   "ANDROID_SDK_ROOT": str(sdk), "PYTHONUTF8": "1"}
    environment["PATH"] = str(jdk / "bin") + os.pathsep + environment.get("PATH", "")
    environment.pop("JDK_HOME", None)
    return environment


def run_gradle(arguments: list[str], jdk: Path, sdk: Path) -> int:
    # 直接启动仓库的 Wrapper，跨平台保持参数原样传递，避免批处理二次解释。
    command = [str(java_executable(jdk)), "-Xmx64m", "-Xms64m",
               "-Dorg.gradle.appname=gradlew", "-classpath",
               str(ROOT / "gradle/wrapper/gradle-wrapper.jar"),
               "org.gradle.wrapper.GradleWrapperMain", f"-Dorg.gradle.java.home={jdk}",
               "--project-cache-dir", str(ROOT / "build/cache"), *arguments]
    return subprocess.run(command, cwd=ROOT, env=gradle_environment(jdk, sdk)).returncode


def collect_apk(build_type: str) -> Path:
    variant = "debug" if build_type == "debug" else "release"
    folder = ROOT / "build/gradle/app/outputs/apk" / variant
    metadata = json.loads((folder / "output-metadata.json").read_text(encoding="utf-8"))
    elements = metadata["elements"]
    if len(elements) != 1:
        fail("当前项目应只生成一个 ARM64 安装包，请核对 ABI 与产物配置")
    item = elements[0]
    source = (folder / item["outputFile"]).resolve()
    if not source.is_relative_to(folder.resolve()) or not source.is_file():
        fail("APK 元数据指向了输出目录外或不存在的文件")
    destination = ROOT / "build/apk" / build_type
    destination.mkdir(parents=True, exist_ok=True)
    target = destination / f"c001apk-{build_type}-arm64-v8a-{item['versionName']}.{item['versionCode']}.apk"
    shutil.copy2(source, target)
    print(f"安装包：{target}\n大小：{target.stat().st_size / 1024 / 1024:.2f} MB", flush=True)
    return target


def main() -> int:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="统一构建、测试与安装包输出入口")
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("doctor", help="检查工具链，缺少 JDK 时自动准备")
    apk = commands.add_parser("build-apk", help="编译并收集安装包到 build/apk")
    apk.add_argument("--build-type", choices=("debug", "release", "release-no-r8"), default="debug")
    apk.add_argument("--app-version", help="覆盖本次构建版本名")
    apk.add_argument("--update-repository", help="指定应用更新仓库")
    apk.add_argument("--require-release-signing", action="store_true", help="要求使用发布签名")
    apk.add_argument("--max-workers", type=int, help="限制 Gradle 并行工作线程数")
    gradle = commands.add_parser("gradle", help="透传 Gradle 任务与参数")
    gradle.add_argument("arguments", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    if args.command == "gradle":
        if args.arguments and args.arguments[0] == "--":
            args.arguments = args.arguments[1:]
        if not args.arguments:
            parser.error("请提供至少一个 Gradle 任务或参数")
    if args.command == "build-apk" and args.max_workers is not None and args.max_workers < 1:
        parser.error("并行工作线程数必须大于零")
    jdk, sdk = prepare()
    if args.command == "doctor":
        return 0
    if args.command == "gradle":
        return run_gradle(args.arguments, jdk, sdk)
    arguments = [":app:assembleDebug" if args.build_type == "debug" else ":app:assembleRelease"]
    if args.build_type == "release-no-r8":
        arguments.append("-PdisableR8=true")
    if args.app_version:
        arguments.append(f"-PappVersion={args.app_version}")
    if args.update_repository:
        arguments.append(f"-PupdateRepository={args.update_repository}")
    if args.require_release_signing:
        arguments.append("-PrequireReleaseSigning=true")
    if args.max_workers:
        arguments.append(f"--max-workers={args.max_workers}")
    result = run_gradle(arguments, jdk, sdk)
    if result != 0:
        return result
    collect_apk(args.build_type)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
