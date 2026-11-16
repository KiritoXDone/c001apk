import argparse

DEFAULT_JOBS = 12
MAX_JOBS = 64


def parse_jobs(value: str) -> int:
    try:
        jobs = int(value)
    except ValueError as error:
        raise argparse.ArgumentTypeError("jobs must be an integer from 1 to 64") from error
    if not 1 <= jobs <= MAX_JOBS:
        raise argparse.ArgumentTypeError("jobs must be an integer from 1 to 64")
    return jobs


def add_jobs_argument(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--jobs", "--max-workers", dest="jobs", type=parse_jobs, default=DEFAULT_JOBS,
        help="Parallel build jobs, 1-64 (default: 12)",
    )


def gradle_worker_arguments(arguments: list[str], jobs: int) -> list[str]:
    # 原始 Gradle 参数不能绕过统一额度。
    if any(argument.startswith("--max-workers") or
           argument.startswith("-Dorg.gradle.workers.max") for argument in arguments):
        raise ValueError("Use --jobs before the Gradle tasks to select the worker limit")
    return [f"--max-workers={jobs}", *arguments]
