---
name: app-debug
description: 调试 c001apk（com.example.c001apk.compose）的 Debug 构建，验证页面、控件与业务改动，维护调试指令与指南。用户要求调试、验证功能、检查 UI、切页、截图或启停调试应用，或者业务逻辑新增变更、需要补充业务调试指令时使用。
---

# 应用调试

先阅读根目录 `AGENTS.md` 的实际架构与配置来源，按当前任务选择指南。统一入口为 `python .codex/skills/app-debug/scripts/run.py`，Claude 镜像使用 `.claude/skills/app-debug/scripts/run.py`，实现相同，均从自身位置定位项目。

日常验证采用构建、已有测试与应用调试；新增或修改测试代码以用户明确要求编写测试为前提。

## 任务配置

- 包名 `com.example.c001apk.compose`，主 Activity `.ui.main.MainActivity`。
- 调试通道为 Debug 变体注册的 `ContentProvider`：`content://com.example.c001apk.compose.debug`，经 `adb shell content call` 调用；Release 构建不包含。
- 命令默认输出一行结果摘要与耗时；加 `--json` 输出结构化字段，日志与诊断写入 stderr。退出码非零即失败。

## 按任务阅读指南

| 需要做的事 | 指南 |
| --- | --- |
| 启动、查询、退出应用，串联命令，写调试记录 | [运行](guides/runtime.md) |
| 查看控件、点击、长按、滚动、输入文本 | [控件交互](guides/controls.md) |
| 打开指定页面、返回上一页 | [页面导航](guides/pages.md) |
| 截图、连续截图、查看细节 | [截图与查阅](guides/screenshots.md) |
| 放慢或恢复动画，观察过渡 | [动画](guides/animations.md) |
| 检查版本更新等业务状态 | [业务调用](guides/business.md) |

## 执行流程

1. 接手已有调试任务时先读最大序号的 `build/record/` 记录，核对阶段与现场。
2. 确认目标是 Debug 构建；Release 包没有调试通道，装 Release 会覆盖 Debug 包。
3. 代码改动按 `AGENTS.md` 核对分层与约束；应用源码或构建配置变化后先构建 Debug（`python scripts/build.py gradle --jobs 12 :app:assembleDebug`），其他操作复用当前产物。
4. 每次调用先 `app.status` 确认 `device` 与 `resumed`，再执行动作；同一实例的启动、交互与截图串行。
5. 控件交互用应用内模拟，经真实输入路由并核对事件、状态与渲染。
6. 业务指令调用真实业务对象；设置项传显式值。未知命令与解析失败按失败处理。
7. 分别记录控件事件链、业务状态与页面就绪结果。
8. 退出用 `app.quit`，并核对进程终态；改过动画倍率先恢复。

## 命令与返回

- 使用普通 `python` 调用；入口在导入项目模块前关闭字节码缓存。
- `run.py` 与 `run.py --help` 从同一注册表展示全部指令；`run.py <指令> --help` 展示参数与示例，帮助在环境检查前结束。
- 命令使用独立 `+` 参数串联；先解析整条链，再按顺序执行，失败时保留已完成结果并结束本组。任一段 `--json` 使整组返回有序 `steps`，含失败位置与退出码。

## 输入与焦点确认

- 应用内模拟只进入目标窗口，不移动系统光标、不发送全局输入、不抢占系统前台。
- 只读查询与模拟直接执行；可能干扰用户正在进行的操作时，先说明目标与影响并取得同意。
- 需要系统级输入或修改设备全局设置时，说明范围并取得明确授权。`animation.set` 修改的是设备级动画倍率，会影响模拟器上的其他应用，观察结束必须恢复。

## 动画与过渡观察

- 应用动画跟随系统动画时钟，`animation.set --speed` 取值 `0.02`～`1`，`0.1` 表示 10 倍时长。
- 观察过渡时串联「调倍率 → `--wait frame` 触发 → 截图 → 恢复倍率」；`animation.get` 记录原值，`restore` 字段给出恢复命令。
- 首帧与完成态、等待方式按 [动画指南](guides/animations.md) 与 [截图指南](guides/screenshots.md) 执行。

## 截图与查阅

- 应用内 `PixelCopy` 合成截图，默认原尺寸 WebP、质量 `40`，保存到 `build/screenshots/`。
- `--high-quality` 输出原尺寸无损 PNG，供用户查看细节。
- 连续截图用 `screenshot.sequence`，参数与文件命名见 [截图指南](guides/screenshots.md)。

## 平台约束

- 目标为 Android 模拟器或已授权设备，通过 `adb` 连接；多设备必须传 `--device`。
- 截图只覆盖应用窗口，不含状态栏与输入法等系统窗口。
- 语义树只包含当前进程的 Compose 界面；页面切换后上一页节点可能短暂保留，定位时优先用当前页面唯一文本。

## 完成与维护

1. 按任务核对控件事件链、业务状态、页面就绪与进程终态；失败时报告命令、错误与实际状态。
2. 恢复动画倍率与临时设置；清理证据前核对完整路径。
3. 业务逻辑新增或变更、或调试需要补充业务指令时，同步维护应用端实现、命令注册与帮助、分类指南和镜像，保持调试能力与业务行为一致。
4. Python 调试模块位于本技能 `scripts/`，依赖项目 `scripts/build_support`；构建与技能同步保持独立。

`.codex/skills` 为主源，每次修改后执行 `python scripts/build.py sync-skills` 更新 `.claude/skills`，再用 `--check` 核对两侧一致。
