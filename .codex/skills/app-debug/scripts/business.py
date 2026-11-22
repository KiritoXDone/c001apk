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


def prefs_get(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {"command": "business", "action": "prefsGet"})


def prefs_set(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "business", "action": "prefsSet",
        "key": args.key, "value": args.value,
    })
