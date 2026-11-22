import sys

sys.dont_write_bytecode = True

import os

os.environ["PYTHONDONTWRITEBYTECODE"] = "1"

import argparse
import contextlib
import importlib
from importlib.machinery import ModuleSpec
import json
from pathlib import Path
import subprocess
from types import ModuleType

SCRIPT_DIRECTORY = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIRECTORY.parents[3]

# 主源与镜像保持相同技能层级，scripts 作为独立命名空间包加载。
debug_package = ModuleType("app_debug")
debug_package.__path__ = [str(SCRIPT_DIRECTORY)]
debug_package.__package__ = "app_debug"
debug_package.__spec__ = ModuleSpec("app_debug", loader=None, is_package=True)
debug_package.PROJECT_ROOT = PROJECT_ROOT
debug_package.SCRIPT_DIRECTORY = SCRIPT_DIRECTORY
sys.modules["app_debug"] = debug_package

from app_debug.commands import COMMANDS, CommandFailure

COMMAND_SEPARATOR = "+"


def create_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="run.py",
        allow_abbrev=False,
        description="Run this project's Android Debug application commands.",
        epilog=(
            "Chain: COMMAND [ARGS] + COMMAND [ARGS] ...\n"
            "All segments are parsed first; execution stops on failure.\n"
            "Results are printed as one JSON object on stdout."
        ),
        formatter_class=argparse.RawTextHelpFormatter,
    )
    commands = parser.add_subparsers(
        title="Commands", dest="command", metavar="COMMAND", required=True,
    )
    for command in COMMANDS:
        examples = None
        if command.examples:
            examples = "Examples:\n" + "\n".join(
                "  " + example for example in command.examples
            )
        child = commands.add_parser(
            command.name,
            allow_abbrev=False,
            help=command.summary,
            description=command.summary,
            epilog=examples,
            formatter_class=argparse.RawTextHelpFormatter,
        )
        if command.configure is not None:
            command.configure(child)
        child.set_defaults(_command_spec=command)
    return parser


def load_handler(reference: str):
    module, function = reference.split(":", 1)
    return getattr(importlib.import_module(module), function)


def split_command_segments(argv: list[str]) -> list[list[str]]:
    segments: list[list[str]] = [[]]
    for token in argv:
        if token == COMMAND_SEPARATOR:
            segments.append([])
        else:
            segments[-1].append(token)
    if any(not segment for segment in segments):
        raise ValueError(
            "Empty command segment; separate commands with a single '+'."
        )
    return segments


def validate_chain_targets(parsed: list[argparse.Namespace]) -> None:
    targets: dict[str, object] = {}
    for args in parsed:
        for field in args._command_spec.target_fields:
            value = getattr(args, field, None)
            if value is None:
                continue
            if field in targets and targets[field] != value:
                raise ValueError(
                    "Commands in one chain must use the same '"
                    + field + "' target."
                )
            targets[field] = value
    for args in parsed:
        for field in args._command_spec.target_fields:
            if getattr(args, field, None) is None and field in targets:
                setattr(args, field, targets[field])


def failure(
    args: argparse.Namespace,
    message: str,
    exit_code: int,
    steps: list[dict],
    *,
    format_error=str,
) -> int:
    print(format_error("error: " + message), file=sys.stderr)
    index = len(steps) + 1
    steps.append({
        "index": index, "command": args.command, "ok": False,
        "error": message, "exit_code": exit_code,
    })
    print(json.dumps({
        "ok": False, "command": args.command, "error": message,
        "exit_code": exit_code, "steps": steps,
    }, ensure_ascii=False))
    return exit_code


def emit_tool_output(output: str | bytes | None) -> None:
    if output is None:
        return
    text = (
        output.decode("utf-8", errors="replace")
        if isinstance(output, bytes) else output
    )
    print(text, end="" if text.endswith("\n") else "\n", file=sys.stderr)


def describe_os_error(error: OSError) -> str:
    message = "File or process operation failed [" + type(error).__name__ + "]"
    for field in ("errno", "winerror", "filename", "filename2"):
        value = getattr(error, field, None)
        if value is not None:
            message += " [" + field + ": " + str(value) + "]"
    return message


def execute_command(args: argparse.Namespace, timed_step):
    with contextlib.redirect_stdout(sys.stderr):
        with timed_step("Execute " + args.command):
            handler = load_handler(args._command_spec.handler)
            return handler(args)


def main(argv: list[str] | None = None) -> int:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    parser = create_parser()
    arguments = sys.argv[1:] if argv is None else argv
    if not arguments:
        parser.print_help()
        return 0
    try:
        segments = split_command_segments(arguments)
    except ValueError as error:
        parser.error(str(error))
    # 全部参数解析成功后才加载执行模块，避免后段拼错时前段已经生效。
    parsed = [parser.parse_args(segment) for segment in segments]
    try:
        validate_chain_targets(parsed)
    except ValueError as error:
        parser.error(str(error))
    steps: list[dict] = []
    # 参数与帮助完成后才导入共享执行能力，帮助不依赖构建环境。
    sys.path.insert(0, str(PROJECT_ROOT / "scripts"))
    try:
        from build_support.console import fail
        from build_support.timer import timed_step
    except ImportError as error:
        return failure(
            parsed[0], "Cannot load shared script support: " + str(error), 1, steps,
        )
    for args in parsed:
        try:
            result = execute_command(args, timed_step)
        except CommandFailure as error:
            return failure(args, str(error), error.exit_code, steps, format_error=fail)
        except subprocess.CalledProcessError as error:
            emit_tool_output(error.stdout)
            emit_tool_output(error.stderr)
            message = "Task command returned exit code " + str(error.returncode)
            return failure(args, message, error.returncode, steps, format_error=fail)
        except OSError as error:
            return failure(args, describe_os_error(error), 1, steps, format_error=fail)
        except (RuntimeError, ValueError, ImportError, AttributeError) as error:
            return failure(args, str(error), 1, steps, format_error=fail)
        steps.append({
            "index": len(steps) + 1, "command": args.command,
            "ok": True, "result": result,
        })
    if len(parsed) == 1:
        print(json.dumps({
            "ok": True, "command": parsed[0].command,
            "result": steps[0]["result"],
        }, ensure_ascii=False))
    else:
        print(json.dumps({"ok": True, "steps": steps}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
