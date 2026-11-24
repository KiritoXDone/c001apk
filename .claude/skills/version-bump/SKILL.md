---
name: version-bump
description: 更新 c001apk 应用版本、整理发布日志并创建版本提交。用户要求更新版本号、提升版本、整理发布日志或准备发布版本时使用。
---

# 更新版本

先阅读根目录 `AGENTS.md` 和同级 [commit skill](../commit/SKILL.md)，按实际版本配置和发布范围执行；需要推送提交或标签时，阅读同级 [git-push skill](../git-push/SKILL.md)。

必要验证使用实际构建、已有测试和 APK 校验。新增或修改测试代码以用户明确要求为前提。

## 项目版本与发布配置

- 默认版本名来源为 `app/build.gradle.kts` 中的 `providers.gradleProperty("appVersion").orElse(...)`；`--app-version` 只覆盖对应构建，不代替仓库默认版本更新。
- `versionCode` 由完整 Git 历史中的 `git rev-list HEAD --count` 派生；版本提交完成后重新构建最终 APK。
- 版本使用三个非负整数，以点分隔；发布标签采用 `vMAJOR.MINOR.PATCH`，各段不带前导零。用户指定版本时沿用；未指定时递增补丁号，主版本提升需要明确授权。
- 工作流直接读取源码默认版本，不另存版本默认值；有效版本通过 `--app-version` 传入构建与 APK 校验。
- 更新日志保存在 `.github/CHANGELOG.md`，使用简体中文，在顶部追加 `## <version> — <date>` 小节并保留历史；发布预检查按基础版本精确提取唯一且非空的章节。
- 正式发布统一使用 `.github/workflows/build-release.yml`：`main` push 中实际应用版本变化时自动构建，手动运行 `Build Release` 使用所选 `main` 提交的当前版本；无需预先创建或推送标签。
- 发布计划固定本次 SHA、版本和日志，发布阶段创建 `vMAJOR.MINOR.PATCH` 标签并完整上传草稿后公开。已公开同 SHA 的 Release 保持不变；同名标签指向其他 SHA 时失败，不移动标签。
- 同一运行重跑恢复原发布计划与草稿；计划丢失时发起新运行。新版本从正常递增的完整 `main` 历史构建，保证 Git 派生 `versionCode` 递增。
- 依赖版本沿用现有配置；版本整理不自动升级依赖，也不运行项目不存在的依赖更新或格式化任务。
- 发布基线从实际发布版本、标签及其提交确认，并核对它位于当前历史中；首次发布或多个候选无法确定时，先确认比较范围。

## 执行流程

1. 检查当前分支、HEAD、完整历史、工作区和暂存区，确定已授权的发布范围与目标版本。
2. 从 HEAD 的版本配置阅读当前仓库版本，另行核对工作区目标版本；查询实际发布记录、标签和上一发布提交，确定唯一比较基线。
3. 按 AGENTS 的原子化原则先提交已完成的应用改动；版本源、必要派生元数据和本次日志归入独立版本提交。
4. 比较发布基线到当前最终代码，按功能归纳仍然生效的用户变化；同一功能反复修正合并为一条，回退后无净变化的内容省略。
5. 修改 `app/build.gradle.kts` 中的唯一默认版本，在 `.github/CHANGELOG.md` 顶部追加目标版本日志，核对标签格式、版本来源和日志标题一致。
6. 按涉及的模块核对依赖方向、导航与状态边界、文件上限、资源和许可证，运行相关已有检查与构建；验证失败时保留证据并修复对应问题。
7. 按 commit skill 核对暂存范围、白名单、隐私和实际验证结果，创建本地版本提交，标题使用 `chore(release): prepare v<version>`。
8. 因该提交会改变 `versionCode`，从最终提交重新构建发布 APK；自行分发时使用已配置的固定发布签名并启用 `--require-release-signing`，再通过 `tools/verify_apk.py` 校验包、版本、ABI 和签名。
9. 核对最新提交、工作区、APK 元数据和对应日志；按当前用户授权推送 `main` 版本提交，自动工作流负责标签与发布。实际推送由 git-push skill 核对范围和剩余次数。

## 构建与验证

```text
python scripts/build.py build-apk --build-type release
python scripts/build.py build-apk --build-type release --jobs <N>
python scripts/build.py build-apk --build-type release --require-release-signing --jobs <N>
python scripts/build.py gradle --jobs <N> :app:testDebugUnitTest :media-support:testDebugUnitTest :image-viewer:testDebugUnitTest :app:lintDebug
python -B -m unittest discover -s tools -p test_*.py
```

- `--jobs` 默认 12，接受 1～64 的整数；用户指定后，本次会话后续构建显式沿用，用户更改时更新，新会话恢复默认。
- 按实际改动选择必要检查，不把全部示例机械执行一遍。APK 校验参数使用实际产物路径、SDK 和预期签名摘要。
- 验证结果对应真实执行状态；提交前的 APK 用于验证，最终发布 APK 对应版本提交之后的源码与派生版本号。

## 日志与 issue

- 发布日志说明普通用户能感知的功能、行为、界面、修复和性能变化；工程规范与脚本维护不写成应用功能。
- 查询实际目标仓库的 open issues，按功能筛选并阅读候选原文；全部要求满足且证据充分时使用关闭关键词，其余情况采用引用或省略。
- 查询失败时明确说明未完成的关联核对，不猜 issue 编号或完成状态。
- 公开日志和附件保留仓库相对路径及必要实现证据，清理账号、凭据、设备标识和本机独特路径。

## 完成与维护

确认唯一版本来源、发布基线、目标版本、日志、派生字段、提交范围和最终 APK 一致，报告实际版本提交与验证结果。

`.codex/skills` 为主源，修改后立即执行 `python scripts/build.py sync-skills`，再通过 `python scripts/build.py sync-skills --check` 核对主源与 Claude 镜像的文件集合和字节一致；主源及镜像的关联修改一起提交。
