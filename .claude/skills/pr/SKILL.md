---
name: pr
description: Prepare, create, or review pull requests for c001apk. Use when the user requests a pull request, a pull request description, or a review of branch changes.
---

# Pull Requests

Follow the root `AGENTS.md` and read the [commit skill](../commit/SKILL.md) before creating or amending commits. Keep each pull request focused on one independently reviewable topic.

## Branches and Scope

- Verify the actual repository, remote, current branch, and working tree. Default to `main` as the base branch; do not copy a reference project's `dev` branch or beta release channel.
- Review all incoming changes with `git log <base>..<head>` and `git diff <base>...<head>`, not just the latest commit.
- Create a task branch when needed. Do not rewrite history, merge branches, or discard user changes merely to prepare a description.
- Check dependency direction across the four modules, navigation and state boundaries, required resources, and third-party licenses. The allowlist must include necessary build inputs and exclude local configuration and artifacts.

## Title and Body

- Use the `type(scope): summary` structure from the commit skill. Write the pull request summary and body in Simplified Chinese.
- Lead with the concrete problem and resulting behavior, then explain relevant implementation tradeoffs, validation results, and unverified areas. One or two paragraphs are enough for a small change.
- Support every passing check with evidence from an actual run. Use the project's existing unit tests, Android lint, APK builds, and device checks as needed; do not invent formatting tasks.
- Use issue-closing references only after reading the issue and fully satisfying its requirements.
- Save multiline bodies under `build/tmp/`, then submit with `gh pr create --body-file <relative-path>` to preserve newlines without shell-escaping errors.

## Creating and Merging

1. Finish task-related checks and local commits. Prepare the title, body, head branch, and base branch for review.
2. Explain outgoing changes and obtain explicit confirmation before pushing. Do not ask again if existing confirmation covers this push.
3. When the user's request includes creating a pull request, create it with an explicit repository, head branch, and base branch, then report its link.
4. Do not create a pull request when the user only requests a description or review. After creation, do not automatically merge it, delete branches, or publish a release.

Releases are triggered by version tags; merging into `main` does not publish a release. Use the [version-bump skill](../version-bump/SKILL.md) for release preparation.

Keep this skill identical in `.codex/skills/pr/` and `.claude/skills/pr/`.
