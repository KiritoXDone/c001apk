from .client import call, resolve_device


def open_page(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "page", "action": "open",
        "destination": args.destination, "wait": args.wait,
    })


def back(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "page", "action": "back", "wait": args.wait,
    })
