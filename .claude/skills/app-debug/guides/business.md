# 业务调用

业务指令通过 Hilt 入口直接调用应用的业务对象，绕过界面但保留真实状态流，用于验证业务结果与界面展示是否一致。

## 实际命令

```text
python .codex/skills/app-debug/scripts/run.py update.state
python .codex/skills/app-debug/scripts/run.py update.check
```

- `update.state`：读取 `AppUpdates.state`，返回 `checking`、`manual`、`hasUpdate`、`version`、`tag`、`page`、`message`。
- `update.check`：触发一次手动检查（`manual = true`），轮询直至 `checking` 变为 `false`，`--timeout-ms` 默认 25000、上限 60000；返回同上字段并附加 `completed`。

## 偏好设置

```text
python .codex/skills/app-debug/scripts/run.py prefs.get
python .codex/skills/app-debug/scripts/run.py prefs.set --key themeMode --value dark
```

`prefs.get` 返回当前界面偏好；`prefs.set` 写入一个键并返回写入后的完整状态。可写键：

| 键 | 取值 |
| --- | --- |
| `themeMode` | `system`、`dark`、`light` |
| `materialYou`、`pureBlack`、`floatingNavigationBar`、`blurBars`、`predictiveBack` | `true`、`false` |
| `fontScale`、`contentScale` | 数字，限制在界面允许的 `0.8`～`1.3` |
| `readingMode` | 整数 |
| `topBarStyle` | `classic`、`classic_contour`、`os4` |
| `bottomBarStyle` | `classic`、`floating`、`os4` |
| `showHomeCarousel`、`showHomeChannels`、`showSquare`、`recordHistory`、`checkUpdate`、`collapseTabsOnScroll` | `true`、`false` |
| `imageQuality` | `1`～`100` |

写入通过 `UserPreferencesRepository` 的现有 setter 完成，走真实的 DataStore 与界面刷新；未知键或非法值返回错误，不做静默兜底。改过主题、字号等可见项后，用 `screenshot.take` 或 `control.list` 核对界面已按新值渲染，再按需要恢复原值。

## 实现与边界

调试入口 `AppDebugEntryPoint` 是 Debug 变体独有的 Hilt `@EntryPoint`，通过 `EntryPointAccessors.fromApplication` 获取 `AppUpdates` 单例。Release 构建不包含该入口与对应 Provider。

业务指令只调用已有公开方法（`check`、`state`），不改写状态、不伪造返回值。检查结果来自真实的 GitHub Release 接口，网络失败时 `message` 为应用给出的实际文案，`hasUpdate` 为 `false`。

## 返回值与状态

- `hasUpdate: true` 表示发现比当前版本更新的正式版，`version` 与 `page` 对应发布信息。
- `message` 非空表示手动检查得到的提示（已是最新、网络失败、格式不支持），与对话框文案一致。
- `completed: false` 表示超时仍未结束，此时 `checking` 为 `true`，需要继续查询或延长超时。

验收以「业务状态」和「界面表现」对照：先 `update.state` 记录状态，再 `page.open --destination settings` 或观察更新对话框，核对 `version` 与界面文案一致。跳过某个版本后重启应用，`coldStart` 不应再弹出该版本对话框。
