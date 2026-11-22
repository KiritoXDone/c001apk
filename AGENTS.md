# c001apk 项目约定

第三方酷安客户端，基于 Kotlin、Jetpack Compose、miuix、Hilt 与 Room。

## 项目结构与架构

| 位置 | 职责 |
| --- | --- |
| `app/` | 应用入口、导航、页面、业务逻辑、账号与网络协议 |
| `mojito/` | 图片浏览、缩放与转场基础组件 |
| `SketchImageViewLoader/` | Sketch 图片加载实现与图片浏览适配 |
| `media-support/` | Media3 播放与媒体浏览适配 |
| `miuix/` | 固定上游版本的 Miuix Android 及公共源码模块 |
| `tools/` | APK 身份、资源、签名与对齐校验 |
| `scripts/` | 预构建、工具链检查、编译与产物收集 |

依赖方向为 `app` → 三个业务库模块和 Miuix 源码模块，`media-support` → `SketchImageViewLoader` 和 `mojito`，`SketchImageViewLoader` → `mojito`。Miuix 模块关系与源码来源见 `miuix/README.md`。禁止循环依赖，库模块不得引用应用业务。四个业务模块不套用其他项目的 `core/*`、`feature/*` 分层。

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
- 页面和可导航弹层的入栈、出栈、转场、预测返回及拖拽关闭优先统一使用 miuix-nav 的原生 API；有现成能力时不得自行实现手势识别、进度驱动或动画曲线。
- 父子导航只保留必要的手势优先级协调和业务状态清理。组件内部的滚动、输入及交互沿用相应 Miuix 组件；确有原生 API 无法覆盖的需求时，先说明限制，再做最小适配。

## 配置来源

- `app/build.gradle.kts` 管理应用身份、唯一默认版本、Git 派生版本号和签名；`gradle/libs.versions.toml` 管理插件与依赖；`gradle/wrapper/gradle-wrapper.properties` 管理 Gradle 版本。
- 多语言资源位于 `app/src/main/res/`；主题位于 `app/src/main/java/com/example/c001apk/compose/ui/theme/`。
- `scripts/build_support/` 维护构建并发额度与技能同步；技能同步独立于 SDK、JDK 和应用状态。
- `CLAUDE.md` 只保留一行 `@AGENTS.md`。`.codex/skills/` 为主源，修改后立即通过同步入口更新 `.claude/skills/` 并检查文件集合与字节一致；独立镜像改动先归入主源。

## 代码约束

### 命名与风格

| 类别 | 命名 |
| --- | --- |
| 类、接口、对象与可组合函数 | 大驼峰 |
| 普通函数、变量与属性 | 小驼峰 |
| 常量 | 大写下划线 |
| 包名 | 全小写，以点分隔 |
| 回调 | `on` 加事件，如 `onClick` |
| 布尔属性与函数 | 判断句，按语义使用 `is`、`has`、`can`、`should` |

- 函数名表示动作或返回值，变量名表示内容；布尔命名优先正向语义，避免双重否定、`flag` 与同义不同名；多种业务状态使用枚举或密封类型。
- 单位体现在名称或类型中，如 `timeoutMs`、`sizeBytes`；缩写大小写按语言约定，Kotlin 使用 `HttpClient` 这类词形。
- 简短逻辑使用单表达式函数；复杂逻辑使用块体。密封类型的 `when` 覆盖真实分支，不写吞掉新状态的兜底。
- 用卫语句提前处理实际存在的非法输入，减少嵌套；不要为固定层数拆出没有语义的函数。
- `let` 用于可空链式操作，`apply` 用于初始化，`use` 用于资源管理。
- 新增导入不用通配符，顺序沿用文件约定；多行参数列表使用尾随逗号。
- 缩进 4 个空格，逻辑段之间空一行；只调整与任务有关的格式，不批量重排无关文件。
- 界面优先复用 miuix 和 `ui/component/`，文案遵循当前功能的资源组织方式；严禁使用液态玻璃效果。
- 新增交互控件应具备可访问名称；需要稳定自动化定位时添加 `testTag`。图标按钮提供有意义的 `contentDescription`。
- 媒体访问沿用 Android URI 与现有权限处理，不照搬播放器项目专有路径类型，也不统一转小写处理所有文件路径。

### 错误、注释与隐私

- 不过度防御，不为尚不存在的失败增加备用实现；禁止空异常处理、把错误变成默认成功、重复校验同一不变量。
- 只在能恢复或补充有效上下文的边界捕获异常；保留协程取消，错误交给现有状态与日志通道处理。
- 注释、开发规范和用户说明使用简体中文，中英文之间空一格；Git 提交信息使用英文。代码注释使用语言原生的单行语法，每段不超过 2 行，只写意图、约束与边界。
- 单个自有代码文件最多 2400 行，含注释与空行；超限按原模块职责拆分。
- 及时清理本次任务涉及的废弃代码与重复资源，保留第三方版权和许可证。
- 共享文件只写仓库相对路径、环境变量或通用占位符；不得写入本机绝对路径、用户名、设备标识、账号凭据、Cookie、签名密码或抓包内容。
- 本机路径通过环境变量或被忽略的 `local.properties` 配置。日志、截图、构建产物不得进入版本控制。

## 修改与提交

- 每次提交对应一个完整逻辑目标，关联实现、配置和文档一起提交，独立目标分开提交。同一任务、文件或推送不代表应合为一笔，同文件的独立改动按块暂存。
- 独立修改完成且必要验证通过后及时创建本地提交，依赖尚未完成的关联改动保留到同一目标完成。
- 根目录 `.gitignore` 默认忽略，只放行明确目录内的必要扩展名及少数固定文件。新增文件先用 `git check-ignore --no-index -v <路径>` 核对最终匹配规则；需要新的构建输入时精确补充白名单，不使用 `git add -f` 绕过规则。
- 已有受控文件不会因新增忽略规则自动退出索引。调整规则时核对所有受控路径，保留 Gradle Wrapper、源码、资源、测试样本、许可证、维护文档与工作流。模块内不要另建重复的忽略规则。

## 技能使用

- 新增、重命名或审查函数、变量、类型、常量等代码标识符时，阅读 code-style skill。
- 新增或修改界面文案、翻译键或多语言资源时，阅读 translation skill。
- 设计或调整 UI、绘制控件、修改界面样式与交互动画时，阅读 ui-design skill。
- 创建或修改本地提交时，阅读 commit skill。
- 推送提交、分支或标签时，阅读 git-push skill。
- 更新版本或整理发布日志时，阅读 version-bump skill。
- 回复 Git issue 时，阅读 issue-reply skill。
- 准备、创建、更新、描述或审查 PR 时，阅读 pull-request skill。
- 审查隐私性或本机独特路径时，阅读 privacy-review skill。
- 调试应用、验证功能、操作页面或控件、启停调试应用、截图，以及业务逻辑新增或变更、补充业务调试指令时，阅读 app-debug skill。

## 工程入口

环境为 Python 3.12 及以上、JDK 21 与 Android SDK。首次构建运行预构建脚本；构建脚本缺少 JDK 时也会自动准备。优先使用 `build/jdk/` 中的便携 JDK；CI 可使用 `JAVA_HOME` 指定的 JDK 21。无需切换本机默认 Java，也不把它的路径写入共享配置。

| 操作 | 命令 |
| --- | --- |
| 检查工程与构建条件 | `python scripts/prebuild.py` 或 `python scripts/build.py doctor` |
| 构建 Debug | `python scripts/build.py build-apk --build-type debug` |
| 构建 Release | `python scripts/build.py build-apk --build-type release` |
| 同步技能镜像 | `python scripts/build.py sync-skills` |
| 核对技能镜像 | `python scripts/build.py sync-skills --check` |

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
- 应用和 Miuix 源码模块 Java/Kotlin 目标为 21，三个业务库模块为 17；当前显式关闭 AGP 内置 Kotlin 和新 DSL，继续使用已有插件，不照搬参考项目的插件配置。
- 本项目没有 `ktlintFormat`、`ktlintCheck`、`versionCatalogUpdate` 任务，不写不存在的检查命令。
- 纯文档或技能调整无需编译，除非用户要求。构建、源码、资源或清单改动运行相关已有检查与编译；没有实际执行或没有测试用例，不能报告通过。
- 不默认新增测试文件；优先使用现有测试。模拟器验收区分编译成功、安装成功、启动成功与功能验证。

### 构建并发

- `scripts/build.py build-apk` 和 `scripts/build.py gradle` 的 `--jobs N` 默认 12，接受 1～64 的整数，错误参数在环境检查前返回 2。
- 用户指定并发数后，本次会话后续构建显式沿用该值，直到用户更改；跨上下文压缩保持，新会话恢复默认 12。
- 共用 `scripts/build_support/concurrency.py`，将额度透传为 Gradle `--max-workers=N`；原有跨模块并行由 `gradle.properties` 管理。
- 一次构建使用一个总额度，独立构建并行时先分配额度，合计不超过当前会话额度；共享 Gradle 输出按顺序执行。

## 本地方案与过程记录

- 普通任务用提交说明和最终回复交代结果，不固定生成方案或过程记录。
- 方案保存为 `build/docs/YYYYMMDD-简体中文标题.md`，同一任务维护一份；记录保存为 `build/record/YYYYMMDD-简体中文标题-序号.md`，附件放在同名子目录。
- 日期采用任务开始日，跨天和跨会话保持日期与标题；序号从 1 连续递增，每个序号对应一个独立探索或实施目标。同一目标的重试、修正、补编和恢复持续更新原记录。
- 每份记录最多 300 行；完整工具输出保存为附件并引用，日志与截图引用实际位置。
- 顶部写任务与阶段目标、状态、更新时间、分支与提交、未提交改动和下一步；缺项写“无”。状态使用进行中、受阻、已完成、已终止，完成以对应验收通过为依据。
- 验收标记未执行、通过或失败并引用证据；阶段切换时更新唯一覆盖清单、前置门槛、有效决定、剩余项、运行作业句柄与恢复要求。
- 接手先定位对应日期与标题，再阅读最大数值序号的记录，核对证据、分支、提交、工作区和作业状态。
- worktree 记录放在 `build/worktrees/<名称>.md`；接手时先阅读有效记录，再核对当前差异。
- `docs/` 保留可长期维护的项目文档，本地过程材料不得混入。

## 并行编写

- 主 agent 维护唯一覆盖清单、前置门槛与跨文件接口。工程基线和独立技能同步通过后，按独立模块或能力并行编写。
- 派发前明确目标、编辑范围、依赖、接口、返回结果和验收条件；共享文件由单一负责人编辑，同一文件的改动按顺序安排。
- 元数据来源、技能镜像同步、目标产物目录、应用实例和设备操作由单一负责人管理；子 agent 修改技能源后交给同步负责人立即同步。
- 子 agent 返回结果后，主 agent 阅读实际差异并核对验证证据；任务派发或代理报告不能代替验收。提交与推送由主 agent 汇总操作。
