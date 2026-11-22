# 业务调用

业务指令通过 Hilt 入口直接调用应用的业务对象，绕过界面但保留真实状态流，用于验证业务结果与界面展示是否一致。

## 实际命令

```text
python .codex/skills/app-debug/scripts/run.py update.state --json
python .codex/skills/app-debug/scripts/run.py update.check --json
```

- `update.state`：读取 `AppUpdates.state`，返回 `checking`、`manual`、`hasUpdate`、`version`、`tag`、`page`、`message`。
- `update.check`：触发一次手动检查（`manual = true`），轮询直至 `checking` 变为 `false`，`--timeout-ms` 默认 25000、上限 60000；返回同上字段并附加 `completed`。

## 实现与边界

调试入口 `AppDebugEntryPoint` 是 Debug 变体独有的 Hilt `@EntryPoint`，通过 `EntryPointAccessors.fromApplication` 获取 `AppUpdates` 单例。Release 构建不包含该入口与对应 Provider。

业务指令只调用已有公开方法（`check`、`state`），不改写状态、不伪造返回值。检查结果来自真实的 GitHub Release 接口，网络失败时 `message` 为应用给出的实际文案，`hasUpdate` 为 `false`。

## 返回值与状态

- `hasUpdate: true` 表示发现比当前版本更新的正式版，`version` 与 `page` 对应发布信息。
- `message` 非空表示手动检查得到的提示（已是最新、网络失败、格式不支持），与对话框文案一致。
- `completed: false` 表示超时仍未结束，此时 `checking` 为 `true`，需要继续查询或延长超时。

验收以「业务状态」和「界面表现」对照：先 `update.state` 记录状态，再 `page.open --destination settings` 或观察更新对话框，核对 `version` 与界面文案一致。跳过某个版本后重启应用，`coldStart` 不应再弹出该版本对话框。
