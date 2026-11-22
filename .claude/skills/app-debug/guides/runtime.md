# 连接与运行

统一入口为 `.codex/skills/app-debug/scripts/run.py`，从脚本自身位置定位项目，不依赖当前工作目录；Claude 镜像位于 `.claude/skills/app-debug/scripts/run.py`，实现相同。脚本把 `scripts/` 作为 `build_support` 的来源，使用前先确认项目 `scripts/build_support` 存在。

## 应用身份与前提

- 包名 `com.example.c001apk.compose`，主 Activity `.ui.main.MainActivity`。
- 调试通道由 Debug 变体注册的 `ContentProvider` 提供：`content://com.example.c001apk.compose.debug`，方法 `call`；Release 构建不包含该组件。
- 通道接受 root、shell 与本应用进程；`adb shell` 即 shell uid，可直接调用。
- 目标必须是 Debug 构建（`applicationId` 与包名相同，签名不同）。安装 Release 包会覆盖 Debug 包并移除通道。

## 输入与焦点

应用内模拟只向目标 `AndroidComposeView` 派发事件，不移动系统光标、不发送全局输入、不抢占系统焦点，用户可同时正常操作模拟器。启动、切页、退出会在当前前台进行，执行前确认模拟器前台没有用户正在进行的操作。

## 实例策略与重复启动

`client.list_devices()` 解析 `adb devices`，唯一在线设备自动选中，多设备必须传 `--device <serial>`。同一命令链的启动、交互、截图与退出绑定同一 `device`，链内混用不同设备在解析阶段报错。

单实例即可完成全部调试；需要对照两个构建时分别启动各自的模拟器，并在每次调用显式传 `--device`。

## 实际命令

```text
# 展示全部已实现指令及简短说明。
python .codex/skills/app-debug/scripts/run.py --help
```

- `app.start`：`am force-stop` 后 `am start -W` 启动主 Activity，轮询调试通道直至可响应（上限 20 s），返回状态对象。`--wait` 控制启动后的等待方式。
- `app.status`：返回 `package`、`version`、`versionCode`、`pid`、`sdk`、`device`、`activity`、`resumed`、`animationScale`、`running`。
- `app.quit`：`am force-stop` 并核对进程消失，返回 `running: false`。

## 命令串联

多条命令使用独立 `+` 参数串联，示例：

```text
python .codex/skills/app-debug/scripts/run.py page.open --destination settings --wait settled + control.click --text 关于 --contains --wait settled
```

整条链先解析全部参数，再按书写顺序执行；某段失败时保留已完成步骤与失败位置，返回非零退出码。stdout 始终只输出一个 JSON 对象：单条调用为 `{"ok":..., "command":..., "result":{...}}`，命令链为 `{"ok":..., "steps":[...]}`。进度、告警与诊断写入 stderr。

## 状态与返回值

`status` 的 `resumed` 为 `true` 表示主 Activity 处于前台；`running` 为 `false` 表示进程已退出。命令返回的 `device` 必须与调用参数一致；`app.start` 失败时先区分「设备未连接」「安装包不存在」「通道未注册」三类原因，再决定是否重装。

## 关闭范围与完成条件

`app.quit` 只结束本应用进程，不清理模拟器其他状态。使用 `animation.set` 改过倍率时，退出前按 [动画指南](animations.md) 恢复原值。截图文件保留在 `build/screenshots/`，按任务需要决定是否保留。

## 调试探索与验收记录

需要复现、验收或交接时，按项目规范写 `build/record/YYYYMMDD-简体中文标题-序号.md`，附件放同名目录，单份不超过 300 行。顶部记录目标、状态、更新时间、分支与提交、未提交改动、下一步；验收逐项标记未执行、通过或失败并引用实际证据（命令、返回值、截图路径）。

普通修改不单独建记录，用提交说明与回复交代结果即可。
