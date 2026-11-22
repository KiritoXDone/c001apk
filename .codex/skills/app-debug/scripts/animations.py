from .client import adb, resolve_device
from .commands import CommandFailure

SETTING = "animator_duration_scale"
MIN_SPEED = 0.02
MAX_SPEED = 1.0


def _read_scale(device: str) -> float:
    completed = adb(device, "shell", "settings", "get", "global", SETTING)
    value = completed.stdout.strip()
    # 未修改过的设备返回 null，系统按默认 1 处理。
    if value in ("", "null"):
        return 1.0
    try:
        return float(value or "1")
    except ValueError as error:
        raise CommandFailure("Unexpected animator scale value: " + value) from error


def _describe(device: str, scale: float) -> dict:
    # 系统设置是时长倍率，播放速度取其倒数；0 表示动画被关闭。
    speed = None if scale <= 0 else round(1 / scale, 6)
    return {
        "device": device,
        "speed": speed,
        "durationScale": scale,
        "scope": "device animator scale",
    }


def get_speed(args) -> dict:
    device = resolve_device(args.device)
    return _describe(device, _read_scale(device))


def set_speed(args) -> dict:
    speed = args.speed
    if not MIN_SPEED <= speed <= MAX_SPEED:
        raise CommandFailure("--speed must be within 0.02 and 1")
    device = resolve_device(args.device)
    previous = _read_scale(device)
    adb(device, "shell", "settings", "put", "global", SETTING, f"{1 / speed:g}")
    result = _describe(device, _read_scale(device))
    result["previous"] = _describe(device, previous)
    result["restore"] = (
        "--speed " + f"{1 / previous:g}" if previous > 0
        else "adb shell settings put global " + SETTING + " 0"
    )
    return result
