# c001apk 项目规范

第三方酷安客户端，基于 Kotlin、Jetpack Compose、miuix、Hilt 与 Room。

## 架构与模块依赖

| 位置 | 职责 |
| --- | --- |
| `app/` | 应用入口、导航、页面、业务逻辑、账号与网络协议 |
| `mojito/` | 图片浏览、缩放与转场基础组件 |
| `SketchImageViewLoader/` | Sketch 图片加载实现与图片浏览适配 |
| `media-support/` | Media3 播放与媒体浏览适配 |
| `tools/` | APK 身份、资源、签名与对齐校验 |
| `scripts/` | 预构建、工具链检查、编译与产物收集 |

依赖方向为 `app` → 三个库模块，`media-support` → `SketchImageViewLoader` 和 `mojito`，`SketchImageViewLoader` → `mojito`。禁止循环依赖，库模块不得引用应用业务。现有四模块结构不套用其他项目的 `core/*`、`feature/*` 分层。

### 应用内分层

以下路径相对于 `app/src/main/java/com/example/c001apk/compose/`：

- `logic/model/` 保存数据模型；模型只负责数据、序列化与轻量派生逻辑。
- `logic/network/` 与 `logic/network/session/` 处理接口、协议与会话；页面不直接拼装请求或签名。
- `logic/repository/` 组织数据访问；`logic/database/`、`logic/dao/`、`logic/datastore/` 负责持久化。
- `logic/accounts/`、`logic/filter/`、`logic/update/` 等按业务划分；不要为形式统一新增用例层或公共基类。
- `ui/` 按页面划分，`ui/component/` 保存共享组件，`ui/theme/` 保存主题，`di/` 负责依赖组装。
- `util/` 与 `view/` 保留现有公共工具和传统视图，不把新业务塞进通用工具类。

### 状态、协程与注入

- 保持单向数据流：页面发送操作，状态持有者处理业务，再向页面提供状态。
- 新 ViewModel 沿用所在功能的 Hilt 注入与状态模型；可变状态留在内部，对外提供只读状态。
- 单次异步操作使用 `suspend`，订阅使用 `Flow` 或 `StateFlow`；ViewModel 的协程使用 `viewModelScope`。
- 文件、数据库和阻塞网络操作离开主线程；沿用当前模块的调度方式，不引入仓库里不存在的调度器注解。
- `@Binds` 用于接口绑定，`@Provides` 用于实例提供；安装范围与实例生命周期一致。
- 保留现有导航、宽屏分栏和返回行为，不引入第二套导航、主题或状态总线。导航改动先阅读 `docs/MIUIX_NAVIGATION.md`。

## Kotlin 与界面规范

| 类别 | 命名 |
| --- | --- |
| 类、接口、对象与可组合函数 | 大驼峰 |
| 普通函数、变量与属性 | 小驼峰 |
| 常量 | 大写下划线 |
| 包名 | 全小写，以点分隔 |
| 回调 | `on` 加事件，如 `onClick` |
| 布尔属性与函数 | 判断句，按语义使用 `is`、`has`、`can`、`should` |

- 布尔命名优先正向语义，避免双重否定、`flag` 与同义不同名；多种业务状态使用枚举或密封类型。
- 简短逻辑使用单表达式函数；复杂逻辑使用块体。密封类型的 `when` 覆盖真实分支，不写吞掉新状态的兜底。
- 用卫语句提前处理实际存在的非法输入，减少嵌套；不要为固定层数拆出没有语义的函数。
- `let` 用于可空链式操作，`apply` 用于初始化，`use` 用于资源管理。
- 新增导入不用通配符，顺序沿用文件约定；多行参数列表使用尾随逗号。
- 缩进 4 个空格，逻辑段之间空一行；只调整与任务有关的格式，不批量重排无关文件。
- 界面优先复用 miuix 和 `ui/component/`，文案遵循当前功能的资源组织方式。
- 新增交互控件应具备可访问名称；需要稳定自动化定位时添加 `testTag`。图标按钮提供有意义的 `contentDescription`。
- 媒体访问沿用 Android URI 与现有权限处理，不照搬播放器项目专有路径类型，也不统一转小写处理所有文件路径。

## 错误、注释与隐私

- 不过度防御，不为尚不存在的失败增加备用实现；禁止空异常处理、把错误变成默认成功、重复校验同一不变量。
- 只在能恢复或补充有效上下文的边界捕获异常；保留协程取消，错误交给现有状态与日志通道处理。
- 注释、文档和用户说明使用简体中文，中英文之间空一格；技能文件和 Git 提交信息使用英文。代码注释使用语言原生的单行语法，每段不超过 2 行，只写意图、约束与边界。
- 及时清理本次任务涉及的废弃代码与重复资源，保留第三方版权和许可证。
- 共享文件只写仓库相对路径、环境变量或通用占位符；不得写入本机绝对路径、用户名、设备标识、账号凭据、Cookie、签名密码或抓包内容。
- 本机路径通过环境变量或被忽略的 `local.properties` 配置。日志、截图、构建产物不得进入版本控制。

## 构建与验证

环境为 Python 3.12 及以上、JDK 21 与 Android SDK。首次构建运行预构建脚本；构建脚本缺少 JDK 时也会自动准备。优先使用 `build/jdk/` 中的便携 JDK；CI 可使用 `JAVA_HOME` 指定的 JDK 21。无需切换本机默认 Java，也不把它的路径写入共享配置。

```text
python scripts/prebuild.py
python scripts/prebuild.py --portable
python scripts/build.py doctor
python scripts/build.py build-apk --build-type debug
python scripts/build.py build-apk --build-type release
python scripts/build.py gradle :app:testDebugUnitTest :media-support:testDebugUnitTest :mojito:testDebugUnitTest :app:lintDebug
python -B -m unittest discover -s tools -p test_*.py
```

- 构建 APK 与执行 Gradle 任务统一使用 `scripts/build.py`。参数、签名配置和 SDK 组件见 `docs/BUILDING.md`。
- 便携工具链位于 `build/jdk/`；模块输出位于 `build/gradle/<模块>/`；收集的安装包位于 `build/apk/<构建类型>/`；项目缓存位于 `build/cache/`。
- Gradle 的 `clean` 只清理模块输出，不清理工具链、安装包和本地记录。禁止为重新构建先删除整个 `build/`。
- 保留应用包名 `com.example.c001apk.compose`、最低 API 24、目标 API 35、ARM64 ABI 与当前签名策略。
- 编译 SDK 分别为应用的 37.0、媒体模块的 35、两个图片模块的 34；构建工具为 36.0.0。不要因为本机缺组件就改项目 SDK 版本。
- 应用 Java/Kotlin 目标为 21，库模块为 17；当前显式关闭 AGP 内置 Kotlin 和新 DSL，继续使用已有插件，不照搬参考项目的插件配置。
- 本项目没有 `ktlintFormat`、`ktlintCheck`、`versionCatalogUpdate` 任务，不写不存在的检查命令。
- 纯文档或技能调整无需编译，除非用户要求。构建、源码、资源或清单改动运行相关已有检查与编译；没有实际执行或没有测试用例，不能报告通过。
- 不默认新增测试文件；优先使用现有测试。模拟器验收区分编译成功、安装成功、启动成功与功能验证。

## Git 白名单

根目录 `.gitignore` 默认忽略，只放行明确目录内的必要扩展名及少数固定文件。新增文件先用 `git check-ignore --no-index -v <路径>` 核对最终匹配规则；需要新的构建输入时精确补充白名单，不使用 `git add -f` 绕过规则。

已有受控文件不会因新增忽略规则自动退出索引。调整规则时核对所有受控路径，保留 Gradle Wrapper、源码、资源、测试样本、许可证、维护文档与工作流。模块内不要另建重复的忽略规则。

## 本地方案与过程记录

- 普通任务用提交说明和最终回复交代结果，不固定生成方案或过程记录。
- 确需保留方案时放在 `build/docs/<任务标识>.md`，验收与交接记录放在 `build/record/<任务标识>-<阶段号>.md`；临时脚本和附件放在同名子目录。
- 任务标识为 `YYYYMMDD-主题`；同一目标的重试与修正更新原记录，不重复生成总结文件。
- 记录保留目标、状态、分支与提交、验收结果、证据路径和下一步；未执行、失败与通过明确区分，每份最多 200 行。
- worktree 记录放在 `build/worktrees/<名称>.md`；接手时先阅读有效记录，再核对当前差异。
- `docs/` 保留可长期维护的项目文档，本地过程材料不得混入。

## 助手规范与技能

`CLAUDE.md` 只保留一行 `@AGENTS.md`。规范正文只维护本文件；`.codex/skills/` 与 `.claude/skills/` 的同名技能保持内容一致，路径均使用仓库相对写法。

- 创建或修改提交前阅读 [提交技能](.codex/skills/commit/SKILL.md)。严格按独立逻辑改动拆分提交，禁止杂糅；同一任务、文件或推送不代表应合为一笔。相关实现及必要依赖、测试和文档一同提交，同文件的独立改动按块暂存。
- 更新应用版本时阅读 [版本技能](.codex/skills/version-bump/SKILL.md)。普通工程规范和脚本调整不自行更新版本。
- 创建、说明或审查合并请求前阅读 [合并请求技能](.codex/skills/pr/SKILL.md)。当前默认目标分支为 `main`，没有参考项目的双通道发布流程。
- 推送前说明改动、远端、目标分支及所有待推送提交，等待用户明确确认；已有覆盖本次推送的确认时不重复询问。
- 创建标签、推送标签、发布版本或合并请求，需要用户指令覆盖相应操作；不得因新增技能而自动执行这些操作。
