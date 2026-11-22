import re
from datetime import datetime

from app_debug import PROJECT_ROOT

from .client import adb, process_id, resolve_device
from .commands import CommandFailure

LOG_DIRECTORY = PROJECT_ROOT / "build" / "logs"


def capture(args) -> dict:
    device = resolve_device(args.device)
    pid = process_id(device)
    if pid is None:
        raise CommandFailure("App is not running; start it before capturing logs")
    completed = adb(
        device, "logcat", "-d", "-v", "threadtime", "--pid", str(pid), timeout=90,
    )
    lines = completed.stdout.splitlines()
    if args.grep:
        try:
            pattern = re.compile(args.grep)
        except re.error as error:
            raise CommandFailure("Invalid --grep pattern: " + str(error)) from error
        lines = [line for line in lines if pattern.search(line)]
    limit = max(0, args.lines)
    if limit:
        lines = lines[-limit:]
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S-%f")[:-3]
    target = LOG_DIRECTORY / (stamp + "-logcat.txt")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(
        "\n".join(lines) + ("\n" if lines else ""), encoding="utf-8",
    )
    return {
        "path": str(target.relative_to(PROJECT_ROOT)).replace("\\", "/"),
        "lines": len(lines),
        "bytes": target.stat().st_size,
        "pid": pid,
        "captured_at": datetime.now().astimezone().isoformat(timespec="milliseconds"),
    }
