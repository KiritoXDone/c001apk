import base64
import json
import re
import subprocess

PACKAGE = "com.example.c001apk.compose"
AUTHORITY = PACKAGE + ".debug"
LAUNCH_ACTIVITY = PACKAGE + "/.ui.main.MainActivity"
PROVIDER_URI = "content://" + AUTHORITY


class DebugError(RuntimeError):
    pass


def adb(device: str, *arguments: str, timeout: float = 60.0, check: bool = True):
    command = ["adb"]
    if device:
        command += ["-s", device]
    command += list(arguments)
    completed = subprocess.run(
        command, capture_output=True, text=True, encoding="utf-8",
        errors="replace", timeout=timeout,
    )
    if check and completed.returncode != 0:
        detail = (completed.stderr or completed.stdout).strip()
        raise DebugError("adb " + " ".join(arguments) + " failed: " + detail)
    return completed


def list_devices() -> list[str]:
    completed = adb("", "devices", check=True)
    serials = []
    for line in completed.stdout.splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            serials.append(parts[0])
    return serials


def resolve_device(value: str | None) -> str:
    if value:
        return value
    serials = list_devices()
    if len(serials) == 1:
        return serials[0]
    if not serials:
        raise DebugError("No connected device; connect one or pass --device")
    raise DebugError(
        "Several devices are connected; pass --device with one of: "
        + ", ".join(serials)
    )


def call(device: str, payload: dict, timeout: float = 90.0) -> dict:
    argument = base64.b64encode(
        json.dumps(payload, ensure_ascii=False).encode("utf-8")
    ).decode("ascii")
    completed = adb(
        device, "shell", "content", "call", "--uri", PROVIDER_URI,
        "--method", "call", "--arg", argument, timeout=timeout,
    )
    match = re.search(r"result=([A-Za-z0-9+/=]+)", completed.stdout)
    if not match:
        detail = (completed.stdout or completed.stderr).strip()
        raise DebugError(
            "Debug provider returned no result: " + (detail[:200] or "empty output")
        )
    response = json.loads(base64.b64decode(match.group(1)))
    if not response.get("ok"):
        raise DebugError(str(response.get("error", "Debug command failed")))
    return response


def process_id(device: str) -> int | None:
    completed = adb(device, "shell", "pidof", PACKAGE, check=False)
    text = completed.stdout.strip()
    return int(text.split()[0]) if text else None
