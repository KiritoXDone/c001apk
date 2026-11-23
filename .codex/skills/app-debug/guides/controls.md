# 控件交互

控件定位来自应用自身的 Compose 语义树（`SemanticsOwner.rootSemanticsNode`），交互通过 `AndroidComposeView.dispatchTouchEvent` 派发真实 `MotionEvent`，经过命中测试、遮挡与手势识别，最终由控件自身的 `clickable`/`pointerInput` 处理。

## 模拟规则

- 点击在目标节点 `boundsInRoot` 的中心派发 `ACTION_DOWN`，60 ms 后派发 `ACTION_UP`；长按保持按下 700 ms 后抬起。
- 滚动在目标节点或最大可滚动节点上派发按下、12 段移动、抬起序列（每段 16 ms），`--dx/--dy` 为手势位移，负数向上/向左，必须至少一个非零。
- 文本先点击目标输入框使其获得焦点，再通过 `onCreateInputConnection` 提交文本并结束组合输入。
- 事件只进入应用内部输入链，不移动系统光标、不产生全局按键。
- 语义树只包含当前进程的 Compose 界面；Android 10 及以上优先定位获得焦点的窗口，支持独立 Dialog 和菜单窗口，旧系统使用主 Activity 窗口。系统窗口（状态栏、输入法）不在此列。

## 控件与命令

```text
python .codex/skills/app-debug/scripts/run.py control.list --limit 300
python .codex/skills/app-debug/scripts/run.py control.click --text 关于 --contains --wait settled
python .codex/skills/app-debug/scripts/run.py control.long-click --desc 头像 --wait settled
python .codex/skills/app-debug/scripts/run.py control.scroll --dy -600 --wait settled
python .codex/skills/app-debug/scripts/run.py control.text --desc 搜索框 --value miuix
python .codex/skills/app-debug/scripts/run.py control.text --desc 搜索框 --clear --value miuix --submit
python .codex/skills/app-debug/scripts/run.py control.wait --text 搜索结果 --timeout-ms 5000
```

- `control.list`：列出可交互节点，字段为 `id`、`tag`、`desc`、`text`、`role`、`clickable`、`enabled`、`scrollable`、`bounds`（屏幕像素）。过滤参数 `--tag/--desc/--text` 与 `--contains`、`--index` 作用于列表本身。
- `control.click` / `control.long-click`：按 `--tag/--desc/--text` 定位，`--contains` 改为包含匹配，`--index` 选择第 n 个匹配。
- `control.scroll`：省略选择器时作用于面积最大的可滚动节点。
- `control.text`：`--value` 与 `--clear` 至少给一个；省略选择器时直接投递给当前焦点输入框。`--clear` 先清空现有文本并等待两帧，再通过新的输入连接写入文本；`--submit` 等待文本状态更新后发送 IME 动作，`--ime-action` 选择 `search`（默认）、`done`、`go`、`send`、`next`。
- `control.wait`：轮询语义树直到目标出现，`--absent` 改为等待消失，`--timeout-ms` 默认 10000、上限 120000；超时返回非零退出码与匹配数量。

## 定位注意

- 语义文本是合并结果：设置项文本为 `关于 1.6.2`（标题与副标题拼接），用 `--text 关于` 精确匹配会失败，应加 `--contains`。
- 页面切换后，上一页在过渡期间仍保留在语义树中，可能出现同文本节点。优先选择当前页面唯一的文本，必要时用 `--index` 或 `--tag`；`bounds` 可辅助区分。
- `--limit` 默认 200，节点较多的页面先提高上限再筛选，避免误以为控件不存在。

## 等待与动画观察

`--wait frame` 等待动作后的下一个渲染帧，用于观察进行中的过渡；`--wait settled` 每 120 ms 采样一次语义签名（节点 id 与 bounds），连续两次相同即返回，上限 5 s；`--wait none` 立即返回。观察慢速过渡时先按 [动画](animations.md) 设置倍率，再配合 [截图](screenshots.md) 取帧。

## 返回值与验收

点击与长按返回 `ok`、`action`、实际坐标 `x/y`、命中节点 `target`；滚动返回同样的起止坐标与 `target`；文本返回 `ok`、`text`、`cleared`、`submitted`；等待返回 `matched`、`absent`、`timedOut`、`elapsedMs`。

验收以「事件已派发 → 控件状态变化 → 界面对应渲染」三步都成立为准：仅返回 `ok` 或仅看到最终截图都不够。对话框、菜单等临时界面要核对遮罩拦截与关闭后的状态恢复。
