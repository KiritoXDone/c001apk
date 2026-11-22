# 截图与查阅

截图在应用进程内使用 `PixelCopy` 捕获当前主窗口，编码后由脚本拉取到本机，设备端临时文件随即删除。

## 实际命令与目标

```text
python .codex/skills/app-debug/scripts/run.py screenshot.take --json
python .codex/skills/app-debug/scripts/run.py screenshot.take --high-quality --json
python .codex/skills/app-debug/scripts/run.py screenshot.sequence --count 6 --interval-ms 150 --json
```

- 目标为主 Activity 所在窗口，按窗口像素 1:1 输出；`PixelCopy` 不可用时回退到 `decorView.draw`。
- `screenshot.take` 返回 `path`（项目相对路径）、`format`、`lossless`、`quality`、`width`、`height`、`bytes`、`captured_at`。
- `screenshot.sequence` 逐帧调用同一捕获逻辑，`--count` 取值 1–30，`--interval-ms` 取值 0–5000，返回每帧的时间与路径。

## 格式与用途

- 默认：原尺寸 WebP，质量 `40`，适合 agent 阅读与记录，单张约 30–300 KB。
- `--high-quality`：原尺寸无损 PNG，供用户查看细节或保存；序列同样支持。
- 文件保存到 `build/screenshots/`，命名包含毫秒时间戳，不与历史截图冲突。

## 采样与过渡

观察动画或转场时，先按 [动画](animations.md) 降低倍率，再通过控件或导航的 `--wait frame` 触发动作，随后调用 `screenshot.take` 或 `screenshot.sequence`。`--wait settled` 之后再截图得到的是完成态。

截图与动作可用 `+` 串联，例如：

```text
python .codex/skills/app-debug/scripts/run.py animation.set --speed 0.05 + control.click --text 更新 --wait frame + screenshot.sequence --count 5 --interval-ms 120 --json + animation.set --speed 1
```

## 返回值与证据

`render_ready` 与 `transition_complete` 分别对应「已取到实际渲染帧」和「本次有限过渡结束」。连续截图逐帧记录 `captured_at`、倍率与当前页面状态，作为过渡证据引用实际文件路径。

截图只覆盖应用窗口，不含状态栏与输入法等系统窗口；需要系统层画面时使用 `adb exec-out screencap`，并注意该命令在多显示器环境需要显式 `-d` 参数。
