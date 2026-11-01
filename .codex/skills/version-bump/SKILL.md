---
name: version-bump
description: Update the c001apk app version, maintain the changelog, and create a release commit. Use when the user requests a version bump or preparation for a new release.
---

# Version Bump

Read the root `AGENTS.md` and the adjacent [commit skill](../commit/SKILL.md) first. Use this skill for version changes, not ordinary documentation, project-rule, or script updates.

## Version Sources

- The default version name is defined by `providers.gradleProperty("appVersion").orElse(...)` in `app/build.gradle.kts`.
- `versionCode` comes from `git rev-list HEAD --count`. Do not replace it with a hardcoded integer, date encoding, or another counter.
- Keep the default versions for manual inputs, reusable workflow inputs, and environment variables in `.github/workflows/android.yml` synchronized.
- Preserve history in the root `CHANGELOG.md`. The release workflow extracts sections by `## <version>`; do not replace the entire file or use a heading-free list.
- Version tags trigger `.github/workflows/release.yml`; pushing only the main branch does not publish a release.

## Workflow

1. Read the current version, full Git history, existing tags, and previous release commit to determine the change range.
2. Review uncommitted changes and include only authorized, completed application work. Commit application changes separately first when needed; keep unrelated files out of the release commit.
3. Use the user's requested version when specified; otherwise increment the patch version by one. Use three nonnegative integers separated by dots, and do not raise the major version without authorization.
4. Summarize the final effects by feature within the change range. Consolidate repeated fixes to the same feature, omit changes with no net effect, and do not present build or project-rule maintenance as user-facing features.
5. Query open issues in the actual target repository, filter by feature, and read candidate issues. Add closing references only when their requirements are fully satisfied. If access fails, report that the issues remain unverified instead of guessing.
6. Synchronize all default versions in the app and workflows. Add a `## <version> — <date>` section at the top of `CHANGELOG.md`, list user-visible changes in Simplified Chinese, and preserve older sections.
7. Verify that release tag rules, version defaults, and the new section agree. Preserve dependency versions and do not run dependency-update or formatting tasks that this project does not have.
8. Build the corresponding version through `scripts/build.py` and run existing checks appropriate to the affected areas. Follow the commit skill and use `chore(release): prepare v<version>` as the release commit subject.
9. The release commit increases the commit count, so rebuild the final release APK from the source after that commit. Do not treat an APK built before the commit as the final release artifact.

Report the release commit and validation results when finished. Do not automatically create or push tags or publish a release. A user request to push still follows the project's push-confirmation rules.

Keep this skill identical in `.codex/skills/version-bump/` and `.claude/skills/version-bump/`.
