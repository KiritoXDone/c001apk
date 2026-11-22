import re
import subprocess
import time
from datetime import datetime

from app_debug import PROJECT_ROOT

from .client import adb, call, resolve_device
from .commands import CommandFailure

SCREENSHOT_DIRECTORY = PROJECT_ROOT / "build" / "screenshots"
PNG_SIGNATURE = bytes((0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))


def _timestamp() -> str:
    return datetime.now().strftime("%Y%m%d-%H%M%S-%f")[:-3]


def capture(args) -> dict:
    device = resolve_device(args.device)
    high_quality = args.high_quality
    name = _timestamp() + "-window-" + ("hq.png" if high_quality else "agent.webp")
    response = call(device, {
        "command": "screenshot",
        "name": name,
        "highQuality": high_quality,
        "quality": 40,
    })
    target = SCREENSHOT_DIRECTORY / name
    target.parent.mkdir(parents=True, exist_ok=True)
    adb(device, "pull", response["path"], str(target))
    adb(device, "shell", "rm", "-f", response["path"], check=False)
    if not target.is_file() or target.stat().st_size == 0:
        raise CommandFailure("Screenshot pull produced no file at " + str(target))
    return {
        "path": str(target.relative_to(PROJECT_ROOT)).replace("\\", "/"),
        "format": response["format"],
        "lossless": response["lossless"],
        "quality": response["quality"],
        "width": response["width"],
        "height": response["height"],
        "bytes": target.stat().st_size,
        "captured_at": datetime.now().astimezone().isoformat(timespec="milliseconds"),
    }


def capture_sequence(args) -> dict:
    count = max(1, min(30, args.count))
    interval = max(0, min(5000, args.interval_ms))
    frames = []
    for index in range(count):
        if index:
            time.sleep(interval / 1000)
        frames.append(capture(args))
    return {"count": len(frames), "interval_ms": interval, "frames": frames}


def display_capture(args) -> dict:
    device = resolve_device(args.device)
    displays = adb(device, "shell", "dumpsys", "SurfaceFlinger", "--display-id")
    match = re.search(r"^Display (\d+)", displays.stdout, re.M)
    if not match:
        raise CommandFailure("SurfaceFlinger reported no display id")
    display_id = match.group(1)
    name = _timestamp() + "-display.png"
    target = SCREENSHOT_DIRECTORY / name
    target.parent.mkdir(parents=True, exist_ok=True)
    with target.open("wb") as stream:
        completed = subprocess.run(
            ["adb", "-s", device, "exec-out", "screencap", "-p", "-d", display_id],
            stdout=stream, stderr=subprocess.PIPE, timeout=60,
        )
    if completed.returncode != 0:
        target.unlink(missing_ok=True)
        detail = completed.stderr.decode("utf-8", "replace").strip()
        raise CommandFailure("Display capture failed: " + (detail or "unknown error"))
    header = target.read_bytes()[:8]
    if header != PNG_SIGNATURE:
        target.unlink(missing_ok=True)
        raise CommandFailure("Display capture did not produce a PNG file")
    return {
        "path": str(target.relative_to(PROJECT_ROOT)).replace("\\", "/"),
        "format": "png",
        "display_id": display_id,
        "bytes": target.stat().st_size,
        "captured_at": datetime.now().astimezone().isoformat(timespec="milliseconds"),
    }
