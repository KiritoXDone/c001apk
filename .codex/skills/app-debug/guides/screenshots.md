# 截图与查阅

截图在应用进程内使用 `PixelCopy` 捕获当前 Activity 的窗口，编码后由脚本拉取到本机，设备端临时文件随即删除。

## 实际命令与目标

```text
python .codex/skills/app-debug/scripts/run.py screenshot.take
python .codex/skills/app-debug/scripts/run.py screenshot.take --high-quality
python .codex/skills/app-debug/scripts/run.py screenshot.sequence --count 6 --interval-ms 150
python .codex/skills/app-debug/scripts/run.py screenshot.display
```

- 目标为当前 Activity 所在窗口，包含独立图片预览，按窗口像素 1:1 输出；`PixelCopy` 不可用时回退到 `decorView.draw`。
- `screenshot.take` 返回 `path`（项目相对路径）、`format`、`lossless`、`quality`、`width`、`height`、`bytes`、`captured_at`。
- `screenshot.sequence` 逐帧调用同一捕获逻辑，`--count` 取值 1–30，`--interval-ms` 取值 0–5000，返回每帧的时间与路径。
- `screenshot.display` 截取整个显示，覆盖对话框、菜单、输入法与其他窗口；脚本读取 `SurfaceFlinger` 的 display id 后直接 `exec-out screencap -p -d <id>` 写入 `build/screenshots/`，校验 PNG 头后返回 `path`、`display_id`、`bytes`。

## 格式与用途

- 默认：原尺寸 WebP，编码质量 `40`，供 agent 查阅；agent 默认使用普通截图命令。
- 高质量：只有用户明确要求高质量截图时才添加 `--high-quality`，输出原尺寸无损 PNG，供用户查看和保存；PNG 压缩级别只影响体积。序列同样支持。
- 文件保存到 `build/screenshots/`，命名包含毫秒时间戳，不与历史截图冲突。

## 采样与过渡

观察动画或转场时，先按 [动画](animations.md) 降低倍率，再通过控件或导航的 `--wait frame` 触发动作，随后调用 `screenshot.take` 或 `screenshot.sequence`。`--wait settled` 之后再截图得到的是完成态。

截图与动作可用 `+` 串联，例如：

```text
python .codex/skills/app-debug/scripts/run.py animation.set --speed 0.05 + control.click --text 更新 --wait frame + screenshot.sequence --count 5 --interval-ms 120 + animation.set --speed 1
```

## 返回值与证据

`render_ready` 与 `transition_complete` 分别对应「已取到实际渲染帧」和「本次有限过渡结束」。连续截图逐帧记录 `captured_at`、倍率与当前页面状态，作为过渡证据引用实际文件路径。

`screenshot.take` 只覆盖应用窗口，不含对话框、菜单、输入法等独立窗口；需要这些画面时用 `screenshot.display`，不要手工拼 `screencap` 与 `pull`。多显示器环境下不带 `-d` 时 `screencap` 的告警会混进输出流并破坏 PNG，脚本已固定带 display id。
