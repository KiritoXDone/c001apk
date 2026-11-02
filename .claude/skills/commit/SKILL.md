---
name: commit
description: Create or amend Git commits for c001apk. Use when preparing commit messages, committing task changes, or amending an existing commit.
---

# Commit

Follow the root `AGENTS.md`. Each commit must contain one cohesive logical change. A task or conversation is not a commit boundary.

## Atomic Commit Boundaries

- Never combine independently reviewable changes just because they share a task, file, subsystem, or push. Separate independent features, bug fixes, refactors, formatting, build changes, and workflow or skill changes.
- Keep a change with its required model, caller, resource, test, and directly relevant documentation updates. Do not split mechanically by file or layer when that leaves an incomplete implementation.
- Before staging, partition the diff into logical changes and state the proposed commit subjects. Order dependent commits so each preserves a coherent source tree.
- When one file contains multiple logical changes, stage only the relevant hunks or prepare an equivalent scoped index version. Preserve the remaining working-tree changes.
- Every staged hunk must serve the commit's single stated purpose. If a hunk can be removed without affecting that purpose, move it to a separate commit. Never hide unrelated work behind a broad subject.
- A push may contain multiple atomic commits; permission to push everything does not permit a mixed commit.

## Message Format

- Write the subject and body in English. Use `type(scope): summary`; omit the scope when changes span multiple modules. Choose `feat`, `fix`, `refactor`, `perf`, `docs`, `build`, `ci`, `test`, or `chore` according to the actual change.
- Use the lowercase name of an actual module or subsystem as the scope, such as `feed`, `media`, or `build`. Describe the final change in the summary; avoid vague wording such as "update code".
- Include a body when needed to explain the concrete problem, resulting behavior, actual validation, and relevant limitations. Do not narrate the work process or add tool or model attribution.
- Do not report a check as passed if it was not run, failed, or had no executable test cases.
- Read an issue before referencing it. Use `close #<number>` only when its requirements are fully satisfied; use `Refs #<number>` for partial relevance.

## Workflow

1. Check `git status --short --branch`, staged and unstaged diffs, and untracked files to confirm the task scope and current branch; partition the changes using the atomic commit rules above.
2. Run the existing project checks required by the changes. Documentation-only and skill-only changes do not require a build unless the user explicitly requests one.
3. Check new paths against the allowlist; never bypass it with `git add -f`. Do not stage machine-specific paths, logs, signing material, screenshots, or build outputs.
4. Stage explicit paths or scoped hunks for one logical change, then review the complete `git diff --cached` against its proposed subject and run `git diff --cached --check`. Do not stage all task changes in one batch unless they form one atomic change.
5. Create the local commit, then verify it with `git log -1 --format=%B` and `git status --short --branch`.

Amend existing commits only within the user's authorization; do not rewrite published history without permission. When changing only a commit message, verify that the file tree is unchanged.

## Pushing

Creating a local commit does not authorize a push. Explain the remote, target branch, and every outgoing commit, then wait for explicit confirmation. Do not ask again if existing confirmation covers this push. Do not automatically push tags, force-push, or merge pull requests.

Keep this skill identical in `.codex/skills/commit/` and `.claude/skills/commit/`.
