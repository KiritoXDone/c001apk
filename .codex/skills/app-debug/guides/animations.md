# 动画速度与过渡观察

应用使用 Compose 动画，时长由系统 `animator_duration_scale` 决定；调试通道读写该设置，实现慢速观察。

## 实际命令

```text
python .codex/skills/app-debug/scripts/run.py animation.get
python .codex/skills/app-debug/scripts/run.py animation.set --speed 0.1
```

- `--speed` 为播放速度倍数，范围 `0.02`～`1`：`1` 为正常速度，`0.1` 表示用 10 倍时长播放，`0.02` 表示 50 倍时长。
- 系统侧保存的是时长倍率 `durationScale = 1 / speed`（`0.1` 对应 `10`，`0.02` 对应 `50`）。
- `animation.get` 返回 `speed`、`durationScale`、`device`；动画被系统关闭时 `durationScale` 为 `0`、`speed` 为 `null`。

## 时钟与作用范围

有效时长按 `effective_duration = base_duration / speed` 计算：原本 200 ms 的转场在 `0.1` 下持续 2 s。业务时间、网络超时、协程调度与后台任务仍走真实时间，只有 UI 动画时钟被拉长。

该设置是**设备级**的，会影响模拟器上的其他应用和系统界面，观察结束后必须恢复。`animation.set` 返回 `previous.speed` 与 `restore` 字段，直接按 `restore` 恢复即可；原值为 `0`（动画关闭）时 `restore` 给出原始 `settings put` 命令。

## 观察流程

```text
python .codex/skills/app-debug/scripts/run.py animation.set --speed 0.05 + control.click --text 更新 --wait frame + screenshot.sequence --count 5 --interval-ms 120 + animation.set --speed 1
```

1. 用 `animation.get` 查询并记录原倍率。
2. 设置观察倍率。
3. 用控件或导航的 `--wait frame` 触发目标，仅等待下一个渲染帧。
4. 用 `screenshot.take` 或 `screenshot.sequence` 捕获进行中的画面。
5. 需要完成态时改用 `--wait settled`，或在观察结束后查状态。
6. 恢复原倍率。

整条链按顺序执行，中途失败时先查询当前倍率，再单独调用 `animation.set` 恢复。

## 返回值与验收

`animation.set` 返回 `speed`、`durationScale`、`previous`、`restore`。验收以「同一过渡在正常速度与观察速度下的帧进度一致、完成时长按比例变化」为准，并记录截图路径与恢复结果。

`render_ready` 表示已取到实际渲染帧，`transition_complete` 表示本次有限过渡结束；`settled` 在 5 s 内以语义坐标稳定为判定上限，超长倍率下可能先超时，此时用 `--wait frame` 或延长观察流程。
