from .client import call, resolve_device
from .commands import CommandFailure


def selector(args) -> dict:
    result = {}
    if getattr(args, "tag", None):
        result["tag"] = args.tag
    if getattr(args, "desc", None):
        result["desc"] = args.desc
    if getattr(args, "text", None):
        result["text"] = args.text
    if not result:
        raise CommandFailure("Provide --tag, --desc or --text to identify the control")
    if getattr(args, "contains", False):
        result["contains"] = True
    if getattr(args, "index", 0):
        result["index"] = args.index
    return result


def _matches(value: str, expected: str, contains: bool) -> bool:
    return expected in value if contains else value == expected


def list_controls(args) -> dict:
    device = resolve_device(args.device)
    response = call(device, {"command": "controls", "limit": max(1, args.limit)})
    items = response.get("items", [])
    tag = getattr(args, "tag", None)
    desc = getattr(args, "desc", None)
    text = getattr(args, "text", None)
    contains = getattr(args, "contains", False)
    filtered = [
        item for item in items
        if (not tag or item.get("tag") == tag)
        and (not desc or _matches(item.get("desc", ""), desc, contains))
        and (not text or _matches(item.get("text", ""), text, contains))
    ]
    return {"count": len(filtered), "items": filtered}


def click(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "pointer", "action": "click",
        "target": selector(args), "wait": args.wait,
    })


def long_click(args) -> dict:
    device = resolve_device(args.device)
    return call(device, {
        "command": "pointer", "action": "longClick",
        "target": selector(args), "wait": args.wait,
    })


def scroll(args) -> dict:
    if not args.dx and not args.dy:
        raise CommandFailure("Provide --dx or --dy for the scroll gesture")
    device = resolve_device(args.device)
    payload = {
        "command": "pointer", "action": "scroll",
        "dx": args.dx, "dy": args.dy, "wait": args.wait,
    }
    if args.tag or args.desc or args.text:
        payload["target"] = selector(args)
    return call(device, payload)


def input_text(args) -> dict:
    device = resolve_device(args.device)
    payload = {"command": "text", "text": args.value, "wait": args.wait}
    if args.tag or args.desc or args.text:
        payload["target"] = selector(args)
    return call(device, payload)
