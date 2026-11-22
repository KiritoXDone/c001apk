import argparse
from collections.abc import Callable
from dataclasses import dataclass


class CommandFailure(RuntimeError):
    def __init__(self, message: str, exit_code: int = 1):
        super().__init__(message)
        self.exit_code = exit_code


@dataclass(frozen=True)
class Command:
    name: str
    summary: str
    handler: str
    configure: Callable[[argparse.ArgumentParser], None] | None = None
    examples: tuple[str, ...] = ()
    target_fields: tuple[str, ...] = ()


def add_device_argument(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--device",
        help="ADB serial; defaults to the only connected device.",
    )


def add_wait_argument(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--wait",
        choices=("frame", "settled", "none"),
        default="settled",
        help="Wait for the next rendered frame, the settled UI, or nothing.",
    )


def add_selector_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--tag", help="Compose testTag of the target control.")
    parser.add_argument("--desc", help="Content description of the target control.")
    parser.add_argument("--text", help="Visible text of the target control.")
    parser.add_argument(
        "--contains",
        action="store_true",
        help="Match desc/text by substring instead of exact equality.",
    )
    parser.add_argument(
        "--index",
        type=int,
        default=0,
        help="Zero-based match index when several controls share a selector.",
    )


def configure_app_start(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    add_wait_argument(parser)


def configure_app(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)


def configure_control_list(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument("--limit", type=int, default=200, help="Maximum listed nodes.")
    add_selector_arguments(parser)


def configure_control_pointer(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    add_selector_arguments(parser)
    add_wait_argument(parser)


def configure_control_scroll(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    add_selector_arguments(parser)
    parser.add_argument("--dx", type=int, default=0, help="Horizontal gesture distance.")
    parser.add_argument("--dy", type=int, default=0, help="Vertical gesture distance.")
    add_wait_argument(parser)


def configure_control_text(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument("--value", required=True, help="Text to commit into the field.")
    add_selector_arguments(parser)
    add_wait_argument(parser)


def configure_page_open(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument(
        "--destination",
        required=True,
        choices=(
            "main", "settings", "about", "license", "accounts", "local-follows",
            "tab-editor", "navigation-order", "params", "content-filters",
            "twilight-rules", "device-import", "login",
        ),
        help="Implemented navigation destination.",
    )
    add_wait_argument(parser)


def configure_page_back(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    add_wait_argument(parser)


def configure_animation_set(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument(
        "--speed",
        type=float,
        required=True,
        help="Animation playback speed from 0.02 to 1 (0.1 means 10x longer).",
    )


def configure_update_check(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument(
        "--timeout-ms",
        type=int,
        default=25000,
        help="Maximum wait for the update check to finish.",
    )


def configure_screenshot(parser: argparse.ArgumentParser) -> None:
    add_device_argument(parser)
    parser.add_argument(
        "--high-quality",
        action="store_true",
        help="Write an original-size lossless PNG instead of WebP quality 40.",
    )


def configure_screenshot_sequence(parser: argparse.ArgumentParser) -> None:
    configure_screenshot(parser)
    parser.add_argument("--count", type=int, default=4, help="Number of frames (1-30).")
    parser.add_argument(
        "--interval-ms",
        type=int,
        default=200,
        help="Target capture interval in milliseconds.",
    )


COMMANDS: tuple[Command, ...] = (
    Command(
        "app.start", "Start the Debug app",
        "app_debug.processes:start", configure_app_start,
        examples=("run.py app.start --device emulator-5554",),
        target_fields=("device",),
    ),
    Command(
        "app.status", "Show app state",
        "app_debug.processes:status", configure_app,
        examples=("run.py app.status --json",),
        target_fields=("device",),
    ),
    Command(
        "app.quit", "Quit the Debug app",
        "app_debug.processes:quit", configure_app,
        examples=("run.py app.quit",),
        target_fields=("device",),
    ),
    Command(
        "control.list", "List visible controls",
        "app_debug.controls:listing", configure_control_list,
        examples=("run.py control.list --desc 更多操作",),
        target_fields=("device",),
    ),
    Command(
        "control.click", "Click a control",
        "app_debug.controls:click", configure_control_pointer,
        examples=("run.py control.click --text 热闻 --wait settled",),
        target_fields=("device",),
    ),
    Command(
        "control.long-click", "Long press a control",
        "app_debug.controls:long_click", configure_control_pointer,
        examples=("run.py control.long-click --desc 头像 --wait settled",),
        target_fields=("device",),
    ),
    Command(
        "control.scroll", "Scroll a control",
        "app_debug.controls:scroll", configure_control_scroll,
        examples=("run.py control.scroll --tag feed --dy -600",),
        target_fields=("device",),
    ),
    Command(
        "control.text", "Type into a control",
        "app_debug.controls:input_text", configure_control_text,
        examples=("run.py control.text --desc 搜索框 --value miuix",),
        target_fields=("device",),
    ),
    Command(
        "page.open", "Open an app page",
        "app_debug.pages:open_page", configure_page_open,
        examples=("run.py page.open --destination settings --wait settled",),
        target_fields=("device",),
    ),
    Command(
        "page.back", "Go back one page",
        "app_debug.pages:back", configure_page_back,
        examples=("run.py page.back --wait settled",),
        target_fields=("device",),
    ),
    Command(
        "animation.get", "Show animation speed",
        "app_debug.animations:get_speed", configure_app,
        examples=("run.py animation.get --json",),
        target_fields=("device",),
    ),
    Command(
        "animation.set", "Set animation speed",
        "app_debug.animations:set_speed", configure_animation_set,
        examples=("run.py animation.set --speed 0.1",),
        target_fields=("device",),
    ),
    Command(
        "update.state", "Show update check state",
        "app_debug.business:update_state", configure_app,
        examples=("run.py update.state --json",),
        target_fields=("device",),
    ),
    Command(
        "update.check", "Trigger an update check",
        "app_debug.business:update_check", configure_update_check,
        examples=("run.py update.check --json",),
        target_fields=("device",),
    ),
    Command(
        "screenshot.take", "Save screenshot",
        "app_debug.captures:take", configure_screenshot,
        examples=("run.py screenshot.take", "run.py screenshot.take --high-quality"),
        target_fields=("device",),
    ),
    Command(
        "screenshot.sequence", "Save timed screenshots",
        "app_debug.captures:sequence", configure_screenshot_sequence,
        examples=("run.py screenshot.sequence --count 6 --interval-ms 200",),
        target_fields=("device",),
    ),
)
