import time
from datetime import datetime

from app_debug import PROJECT_ROOT

from .client import adb, call, resolve_device
from .commands import CommandFailure

SCREENSHOT_DIRECTORY = PROJECT_ROOT / "build" / "screenshots"


def _timestamp() -> str:
    return datetime.now().strftime("%Y%m%d-%H%M%S-%f")[:-3]


def take(args) -> dict:
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


def sequence(args) -> dict:
    count = max(1, min(30, args.count))
    interval = max(0, min(5000, args.interval_ms))
    frames = []
    for index in range(count):
        if index:
            time.sleep(interval / 1000)
        frames.append(take(args))
    return {"count": len(frames), "interval_ms": interval, "frames": frames}
