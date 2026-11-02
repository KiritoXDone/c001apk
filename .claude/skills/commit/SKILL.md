---
name: commit
description: Create or amend Git commits for c001apk. Use when preparing commit messages, committing task changes, or amending an existing commit.
---

# Commit

Follow the root `AGENTS.md`. Default to one local commit per task and include only changes relevant to that task.

## Message Format

- Write the subject and body in English. Use `type(scope): summary`; omit the scope when changes span multiple modules. Choose `feat`, `fix`, `refactor`, `perf`, `docs`, `build`, `ci`, `test`, or `chore` according to the actual change.
- Use the lowercase name of an actual module or subsystem as the scope, such as `feed`, `media`, or `build`. Describe the final change in the summary; avoid vague wording such as "update code".
- Include a body when needed to explain the concrete problem, resulting behavior, actual validation, and relevant limitations. Do not narrate the work process or add tool or model attribution.
- Do not report a check as passed if it was not run, failed, or had no executable test cases.
- Read an issue before referencing it. Use `close #<number>` only when its requirements are fully satisfied; use `Refs #<number>` for partial relevance.

## Workflow

1. Check `git status --short --branch`, staged and unstaged diffs, and untracked files to confirm the task scope and current branch.
2. Run the existing project checks required by the changes. Documentation-only and skill-only changes do not require a build unless the user explicitly requests one.
3. Check new paths against the allowlist; never bypass it with `git add -f`. Do not stage machine-specific paths, logs, signing material, screenshots, or build outputs.
4. Stage explicit paths with `git add -- <paths>`, then review the complete `git diff --cached` and run `git diff --cached --check`.
5. Create the local commit, then verify it with `git log -1 --format=%B` and `git status --short --branch`.

Amend existing commits only within the user's authorization; do not rewrite published history without permission. When changing only a commit message, verify that the file tree is unchanged.

## Pushing

Creating a local commit does not authorize a push. Explain the remote, target branch, and every outgoing commit, then wait for explicit confirmation. Do not ask again if existing confirmation covers this push. Do not automatically push tags, force-push, or merge pull requests.

Keep this skill identical in `.codex/skills/commit/` and `.claude/skills/commit/`.
