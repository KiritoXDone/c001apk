from .client import call, resolve_device


def update_state(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {"command": "business", "action": "updateState"})


def update_check(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "business", "action": "updateCheck",
        "timeoutMs": args.timeout_ms,
    })
