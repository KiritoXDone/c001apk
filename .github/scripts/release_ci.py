import sys

sys.dont_write_bytecode = True

import argparse
import hashlib
import json
import os
import re
import subprocess
import uuid
from pathlib import Path

os.environ["PYTHONDONTWRITEBYTECODE"] = "1"
ROOT = Path(__file__).resolve().parents[2]
CI = ROOT / "build" / "ci"
PLAN = CI / "plan"
BASE_VERSION = r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)"


def run(*args, stdin=None):
    return subprocess.run(
        args, input=stdin, text=True, encoding="utf-8", check=True,
        stdout=subprocess.PIPE, cwd=ROOT,
    ).stdout


def api(endpoint, payload=None):
    if payload is None:
        return json.loads(run("gh", "api", endpoint))
    return json.loads(run(
        "gh", "api", "--method", "POST", endpoint, "--input", "-",
        stdin=json.dumps(payload),
    ))


def pages(endpoint):
    return json.loads(run("gh", "api", "--paginate", "--slurp", endpoint))


def update_release(repository, release_id, payload):
    return json.loads(run(
        "gh", "api", "--method", "PATCH",
        f"repos/{repository}/releases/{release_id}", "--input", "-",
        stdin=json.dumps(payload),
    ))


def remote_state(repository):
    tags = {
        item["name"]: item["commit"]["sha"]
        for page in pages(f"repos/{repository}/tags?per_page=100")
        for item in page
    }
    releases = {
        item["tag_name"]: item
        for page in pages(f"repos/{repository}/releases?per_page=100")
        for item in page
    }
    return tags, releases


def output(**values):
    with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as stream:
        for key, value in values.items():
            text = str(value)
            if "\n" in text or "\r" in text:
                raise ValueError(f"Expected a single-line output: {key}")
            stream.write(f"{key}={text}\n")


def print_text(text):
    token = uuid.uuid4().hex
    print(f"::stop-commands::{token}", flush=True)
    print(text, flush=True)
    print(f"::{token}::", flush=True)


def read_source():
    source = json.loads((CI / "source.json").read_text(encoding="utf-8"))
    for key in ("app_name", "artifact_name", "version"):
        value = source[key]
        if not isinstance(value, str) or not value.strip() or any(c in value for c in "\r\n"):
            raise ValueError(f"Expected a nonempty single-line metadata value: {key}")
    if re.fullmatch(BASE_VERSION, source["version"]) is None:
        raise ValueError("Expected the application base version in major.minor.patch format.")
    if re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]*", source["artifact_name"]) is None:
        raise ValueError("Expected an artifact name using ASCII letters, digits, dots, hyphens or underscores.")
    if type(source["version_changed"]) is not bool:
        raise ValueError("Expected version_changed to be a Boolean.")
    if "skip_reason" in source and not isinstance(source["skip_reason"], str):
        raise ValueError("Expected skip_reason to be an English message.")
    return source


def read_matrix():
    matrix = json.loads(os.environ["RELEASE_MATRIX"])
    targets = [item["target"] for item in matrix["include"]]
    if not targets or len(targets) != len(set(targets)):
        raise ValueError("Expected a nonempty matrix with unique target identifiers.")
    for target in targets:
        if re.fullmatch(r"[a-z][a-z0-9-]*", target) is None:
            raise ValueError(f"Invalid matrix target: {target}")
    return matrix


def release_notes(mode, version):
    text = (ROOT / ".github" / "CHANGELOG.md").read_text(encoding="utf-8")
    if mode == "current":
        result = text.strip()
    else:
        sections = []
        current = None
        fence = None
        for line in text.splitlines():
            marker = re.match(r"^\s{0,3}(`{3,}|~{3,})", line)
            if marker:
                value = marker.group(1)
                if fence is None:
                    fence = value
                elif value[0] == fence[0] and len(value) >= len(fence):
                    fence = None
            heading = re.match(r"^##\s+(.+?)\s*$", line) if fence is None else None
            if heading:
                title = re.sub(r"\s+#+$", "", heading.group(1))
                pattern = rf"(?:\[v?{re.escape(version)}\]|v?{re.escape(version)})(?:\s+[-—]\s+.+)?"
                current = [] if re.fullmatch(pattern, title) else None
                if current is not None:
                    sections.append(current)
            elif current is not None:
                current.append(line)
        if len(sections) != 1:
            raise ValueError(f"Expected one changelog section for {version}; found {len(sections)}.")
        result = "\n".join(sections[0]).strip()
    if not result:
        raise ValueError("Expected release notes with a nonempty body.")
    return result + "\n"


def restore_plan(repository, run_id):
    name = f"release-plan-{run_id}"
    artifacts = [
        item for page in pages(f"repos/{repository}/actions/runs/{run_id}/artifacts?per_page=100")
        for item in page["artifacts"]
        if item["name"] == name and not item["expired"]
    ]
    if len(artifacts) != 1:
        raise ValueError("The saved release plan is unavailable. Start a new workflow run.")
    run("gh", "run", "download", run_id, "--repo", repository, "--name", name, "--dir", str(PLAN))
    return json.loads((PLAN / "release.json").read_text(encoding="utf-8"))


def check_tag(tags, plan):
    existing = tags.get(plan["tag"])
    if existing is not None and existing != plan["sha"]:
        raise ValueError(f"Tag {plan['tag']} points to {existing}; expected {plan['sha']}.")


def prepare(args):
    source = read_source()
    if os.environ["GITHUB_EVENT_NAME"] == "push":
        reason = source.get("skip_reason", "")
        if not reason and args.channel == "release" and not source["version_changed"]:
            reason = "Application version is unchanged in this push; automatic release build skipped."
        elif not reason and args.channel == "beta" and source["version_changed"]:
            reason = "Application version changed in this push; automatic beta build skipped."
        if reason:
            name, branch = ("Release", "main") if args.channel == "release" else ("Beta", "beta")
            print_text(reason + f"\nRun Build {name} manually on {branch} to publish this commit.")
            output(should_build="false")
            return

    repository = os.environ["GITHUB_REPOSITORY"]
    run_id = os.environ["GITHUB_RUN_ID"]
    matrix = read_matrix()
    notes = release_notes(args.changelog_mode, source["version"])
    identity = {
        "repository": repository, "run_id": run_id, "sha": os.environ["GITHUB_SHA"],
        "channel": args.channel, "app_name": source["app_name"],
        "artifact_name": source["artifact_name"], "base_version": source["version"],
        "matrix": matrix, "publish_release": not args.build_only,
    }
    tags, releases = ({}, {}) if args.build_only else remote_state(repository)
    if int(os.environ["GITHUB_RUN_ATTEMPT"]) > 1:
        plan = restore_plan(repository, run_id)
        if any(plan.get(key) != value for key, value in identity.items()):
            raise ValueError("The saved release plan differs from the current build inputs.")
        if (PLAN / "notes.md").read_text(encoding="utf-8") != notes:
            raise ValueError("The saved release notes differ from the current changelog.")
    else:
        version = source["version"]
        if args.channel == "beta":
            pattern = re.compile(rf"v{re.escape(version)}-beta([1-9][0-9]*)")
            numbers = [
                int(match.group(1)) for tag in tags.keys() | releases.keys()
                if (match := pattern.fullmatch(tag))
            ]
            version += f"-beta{max(numbers, default=0) + 1}"
        plan = dict(identity, version=version, tag=f"v{version}")
        PLAN.mkdir(parents=True, exist_ok=True)
        (PLAN / "release.json").write_text(json.dumps(plan, indent=2) + "\n", encoding="utf-8")
        (PLAN / "notes.md").write_text(notes, encoding="utf-8")

    if plan["publish_release"]:
        check_tag(tags, plan)
    summary = (
        f"App: {plan['app_name']}\nMode: {'release' if plan['publish_release'] else 'build only'}\n"
        f"Base version: {plan['base_version']}\n"
        f"Version: {plan['version']}\nPlanned tag: {plan['tag']}\nCommit: {plan['sha']}\n\n{notes}"
    )
    print_text(summary)
    with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as stream:
        stream.write(summary)
    existing = releases.get(plan["tag"])
    if existing and not existing["draft"]:
        if tags.get(plan["tag"]) != plan["sha"]:
            raise ValueError("Expected the published tag to resolve to the planned commit.")
        print(f"Release already published: {existing['html_url']}")
        output(should_build="false")
        return
    output(
        should_build="true", publish_release=str(plan["publish_release"]).lower(),
        app_name=plan["app_name"], version=plan["version"],
        tag=plan["tag"], matrix=json.dumps(matrix, separators=(",", ":")),
    )


def load_plan():
    plan = json.loads((PLAN / "release.json").read_text(encoding="utf-8"))
    for key, env in (("repository", "GITHUB_REPOSITORY"), ("sha", "GITHUB_SHA"), ("run_id", "GITHUB_RUN_ID")):
        if plan[key] != os.environ[env]:
            raise ValueError(f"Expected the release plan to match {env}.")
    return plan


def package_files(directory, target, plan):
    if target not in {item["target"] for item in plan["matrix"]["include"]}:
        raise ValueError(f"Expected a planned build target: {target}")
    prefix = f"{plan['artifact_name']}-v{plan['version']}-{target}"
    expected = {prefix + suffix for suffix in (".apk", ".apk.json", ".sha256", ".manifest.json")}
    files = sorted(directory.iterdir())
    if {path.name for path in files} != expected:
        raise ValueError(f"Release package list differs from the expected files for {target}.")
    for path in files:
        if path.is_symlink() or not path.is_file() or path.stat().st_size == 0:
            raise ValueError(f"Expected a nonempty packaged file: {path.name}")
    manifest = json.loads((directory / f"{prefix}.manifest.json").read_text(encoding="utf-8"))
    if (manifest["sha"], manifest["version"], manifest["target"]) != (plan["sha"], plan["version"], target):
        raise ValueError("Package manifest differs from the release plan.")
    if set(manifest["files"]) != expected - {f"{prefix}.manifest.json"}:
        raise ValueError("Package manifest does not identify the complete release file list.")
    for name, metadata in manifest["files"].items():
        path = directory / name
        with path.open("rb") as stream:
            digest = hashlib.file_digest(stream, "sha256").hexdigest()
        if path.stat().st_size != metadata["bytes"] or digest != metadata["sha256"]:
            raise ValueError(f"Package size or digest differs from its manifest: {name}")
    report = json.loads((directory / f"{prefix}.apk.json").read_text(encoding="utf-8"))
    if report["apk"] != f"{prefix}.apk" or report["versionName"] != plan["version"]:
        raise ValueError("APK report differs from the release package and planned version.")
    if report["sha256"] != manifest["files"][f"{prefix}.apk"]["sha256"]:
        raise ValueError("APK report digest differs from its package manifest.")
    return files


def collect_packages(plan):
    targets = [item["target"] for item in plan["matrix"]["include"]]
    directory = CI / "artifacts"
    expected = {f"packages-{target}" for target in targets}
    actual = {path.name for path in directory.iterdir()}
    # download-artifact 只匹配到一个产物时直接解包到目标目录。
    if len(targets) == 1 and expected.isdisjoint(actual):
        return package_files(directory, targets[0], plan)
    if actual != expected:
        raise ValueError(f"Expected matrix artifacts {sorted(expected)}; found {sorted(actual)}.")
    files = [path for target in targets for path in package_files(directory / f"packages-{target}", target, plan)]
    if len({path.name for path in files}) != len(files):
        raise ValueError("Expected unique release asset filenames across all targets.")
    return files


def publish(_args):
    plan = load_plan()
    if not plan["publish_release"]:
        raise ValueError("A build-only plan cannot create tags or publish a Release.")
    files = collect_packages(plan)
    repository = plan["repository"]
    tags, releases = remote_state(repository)
    check_tag(tags, plan)
    release = releases.get(plan["tag"])
    if release and not release["draft"]:
        if tags.get(plan["tag"]) != plan["sha"]:
            raise ValueError("Expected the published tag to resolve to the planned commit.")
        print(f"Release already published: {release['html_url']}")
        return

    if plan["tag"] not in tags:
        api(f"repos/{repository}/git/refs", {"ref": f"refs/tags/{plan['tag']}", "sha": plan["sha"]})
    metadata = {
        "tag_name": plan["tag"], "target_commitish": plan["sha"],
        "name": f"{plan['app_name']} v{plan['version']}",
        "body": (PLAN / "notes.md").read_text(encoding="utf-8"),
        "draft": True, "prerelease": plan["channel"] == "beta",
    }
    if release:
        release = update_release(repository, release["id"], metadata)
    else:
        release = api(f"repos/{repository}/releases", metadata)
    run("gh", "release", "upload", plan["tag"], *map(str, files), "--repo", repository, "--clobber")
    assets = [
        item for page in pages(f"repos/{repository}/releases/{release['id']}/assets?per_page=100")
        for item in page
    ]
    expected = {path.name: path.stat().st_size for path in files}
    actual = {item["name"]: item["size"] for item in assets if item["state"] == "uploaded"}
    if actual != expected or len(assets) != len(files):
        raise ValueError("Draft assets differ from the complete matrix package list.")
    paths = {path.name: path for path in files}
    for asset in assets:
        digest = asset.get("digest")
        if digest:
            with paths[asset["name"]].open("rb") as stream:
                expected_digest = "sha256:" + hashlib.file_digest(stream, "sha256").hexdigest()
            if digest != expected_digest:
                raise ValueError(f"Asset digest mismatch: {asset['name']}")
    release = update_release(repository, release["id"], {
        "draft": False, "prerelease": plan["channel"] == "beta",
        "make_latest": "true" if plan["channel"] == "release" else "false",
    })
    print(f"Published release: {release['html_url']}")


def main():
    parser = argparse.ArgumentParser(description="Prepare, verify and publish matrix releases.")
    commands = parser.add_subparsers(dest="command")
    prepare_parser = commands.add_parser("prepare", help="Check metadata and save the release plan.")
    prepare_parser.add_argument("--channel", choices=("release", "beta"), required=True)
    prepare_parser.add_argument("--changelog-mode", choices=("current", "versioned"), required=True)
    prepare_parser.add_argument("--build-only", action="store_true", help="Build the current version without checking tags or publishing.")
    prepare_parser.set_defaults(action=prepare)
    publish_parser = commands.add_parser("publish", help="Publish the complete release through a draft.")
    publish_parser.set_defaults(action=publish)
    args = parser.parse_args()
    if args.command is None:
        parser.print_help()
        return 0
    try:
        args.action(args)
    except (KeyError, ValueError, OSError, subprocess.CalledProcessError) as error:
        print(f"Release operation failed: {type(error).__name__}", file=sys.stderr)
        if isinstance(error, (KeyError, ValueError)):
            print(str(error), file=sys.stderr)
        elif isinstance(error, OSError):
            print(f"errno={error.errno}; filename={error.filename}", file=sys.stderr)
        else:
            print(f"Command exited with status {error.returncode}.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
