import time

from .client import (
    LAUNCH_ACTIVITY,
    PACKAGE,
    adb,
    call,
    process_id,
    resolve_device,
)
from .commands import CommandFailure

READY_TIMEOUT_SECONDS = 20.0


def _status(device: str) -> dict:
    pid = process_id(device)
    if pid is None:
        return {"running": False, "pid": None, "device": device}
    response = call(device, {"command": "status"})
    response.pop("ok", None)
    response["running"] = True
    response["device"] = device
    return response


def status(args) -> dict:
    return _status(resolve_device(args.device))


def start(args) -> dict:
    device = resolve_device(args.device)
    adb(device, "shell", "am", "start", "-W", "-n", LAUNCH_ACTIVITY, timeout=60)
    deadline = time.monotonic() + READY_TIMEOUT_SECONDS
    last_error = None
    while time.monotonic() < deadline:
        try:
            response = _status(device)
            if response.get("running") and response.get("resumed"):
                return response
        except Exception as error:  # 启动期间 Provider 可能尚未就绪。
            last_error = error
        time.sleep(0.4)
    message = "App did not become ready within " + str(int(READY_TIMEOUT_SECONDS)) + "s"
    if last_error is not None:
        message += "; last error: " + str(last_error)
    raise CommandFailure(message)


def quit(args) -> dict:
    device = resolve_device(args.device)
    pid = process_id(device)
    if pid is None:
        return {"running": False, "pid": None, "device": device}
    try:
        call(device, {"command": "quit"}, timeout=30)
    except Exception:
        # 退出阶段允许接口先断开，最终以进程终态判定。
        pass
    adb(device, "shell", "am", "force-stop", PACKAGE, check=False)
    deadline = time.monotonic() + 10.0
    while time.monotonic() < deadline:
        if process_id(device) is None:
            return {"running": False, "pid": pid, "device": device}
        time.sleep(0.2)
    raise CommandFailure("App process " + str(pid) + " is still running after force-stop")
