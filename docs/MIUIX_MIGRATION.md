# miuix 改造说明

## 基线与设计依据

- 上游：[frisk1127/c001apk-compose](https://github.com/frisk1127/c001apk-compose)，基线提交 `82ffea3c746955420c1d4f00380f5a6d7a6cad02`。
- 实现采用 Kotlin 原生 Compose。
- 设计规范参考：[Mishka UI guidelines](https://github.com/YuKongA/Mishka/blob/main/docs/ui-guidelines.md)。采用 squircle 圆角、12 dp 外边距、分段设置卡片、选中即生效的单选弹窗、窗口弹窗与毛玻璃背景采样。
- 组件：[miuix v0.9.4](https://github.com/compose-miuix-ui/miuix/tree/v0.9.4)，使用 `miuix-ui`、`miuix-preference`、`miuix-icons`、`miuix-blur`。

## 实现

- 主题完全使用 `MiuixTheme` / `ThemeController`，新安装默认使用 miuix 蓝色，保留系统动态色、自定义色、深色、纯黑、字体和内容缩放设置。原有 `materialYou` 持久化字段继续表示“系统主题色”，避免破坏数据兼容性。
- 主导航参考 TG-Twilight/c001apk，采用“主页 / 关注 / 我的”；宽屏切换为 NavigationRail。主页保留独立标题，标题与搜索按钮同高；下方为应用、动态、热门、话题、数码、酷图页签，默认动态；关注独立成页；我的保留个人资料与消息，并提供设置入口。首页板块编辑移到设置，保存完成后才退出编辑页；保留重选刷新。
- 首页顶部页签使用原生 `TabRowWithContour` 的整体底座和选中滑块，沿用组件默认尺寸、配色与动画；移除标题与页签之间额外的 8 dp 上边距，继续与现有 `HorizontalPager` 同步，支持重选刷新与“滚动时收起标签栏”。其他页面的标签样式保持原有 `TabRow`。
- “设置 → 外观与字号 → 显示”提供独立的“悬浮底栏”和“模糊效果”开关，均默认关闭，旧配置升级也保持关闭。悬浮模式使用原生 `FloatingNavigationBar`，宽屏仍使用侧边导航。启用模糊后，`MiuixScaffold` 统一接入官方 `LayerBackdrop` / `textureBlur`，主页内外层独立采样；悬浮底栏仅模糊圆角栏体，外侧留空。
- 顶栏和底栏尺寸缩小 5%，字体基准统一为原来的 95%，仍叠加用户保存的字号设置；栏内文字不重复缩放。内容大小改变后，普通导航页面保持填满宽度，分栏和宽屏判断使用当前 Compose 密度换算窗口像素，避免缩小后页面向右偏移。系统状态栏、导航手势区域继续按实际 inset 留白。
- 话题排序行最小高度由 48 dp 收紧到 36 dp，上方额外留白由 12 dp 收紧到 4 dp；动态和评论卡片的评论入口默认使用空心图标。
- 应用详情页三个排序选项改为 13 sp 小字，放在列表首项随内容滚动，最小行高 36 dp、顶部留白 4 dp、单项左右留白 6 dp，保留点击、重复点击刷新和左右滑动切换。列表回顶后，下拉余量先展开应用信息与下载区域，再交给下拉刷新。
- 非根页面支持从左侧 32 dp 边缘右滑返回，接入 AndroidX NavigationEvent 返回分发，保留预测返回进度和取消，以及页面自身的返回处理。中部横滑继续交给分页，边缘竖滑继续滚动；显示键盘时由系统返回处理输入法。拖动超过页面宽度 25%，或超过 32 dp 后快速右滑，释放时完成返回。
- “显示”中新增“滚动时收起标签栏”，默认关闭；开启后首页、话题及共享 Tab 页面上滑收起、下滑展开，关闭后恢复常驻。仅收起 Tab 栏，保留标题与底部导航；随列表滚动的小字排序选项不受此开关控制。
- WebView 页面使用相同的 miuix 顶栏，但网页内容不进入额外的背景采样绘制，避免 Chromium 硬件合成闪烁。加载进度条覆盖在固定视口上方，离开页面时停止加载并销毁 WebView。
- 信息流、详情与各类消息卡片使用 miuix Card / squircle modifier。设置按独立 lazy item 分段绘制圆角卡片。
- 动态详情将作者资料并入正文的连续阅读卡片，评论独立分区；长文章继续逐块惰性绘制，宽屏阅读内容限制为 800 dp。评论分类使用与标题同行的 13 sp 文字选项和 miuix 按压反馈，选中项以主题色和字重标识；不使用 TabRow，不提供独立“查看评论”底栏。机型紧跟 IP，与时间放在同一行；话题按钮与子评论底色直接沿用首页样式。用户主页采用封面与叠放头像、资料操作和独立统计卡片，保留关注、私信、搜索及列表入口。
- 公共图片请求使用固定通用 User-Agent，避免 CDN 拒绝缺失 User-Agent 的请求，同时在每次重定向保留凭据隔离。头像、封面和九宫格提供失败状态与点击重试，重复重组不再取消并重新发起相同的 View 图片请求。
- 输入框、按钮、图标按钮、单选/多选/开关、进度条、分隔线、通知条、菜单、底部弹层和下拉刷新使用原生 miuix。窗口弹窗限制内容高度，按钮独立于滚动区。
- 动态、评论、历史与通知卡片的三点菜单统一使用 miuix IconButton / BasicComponent / WindowListPopup。按钮使用透明底色，展开后不保持灰色按压底；菜单以实际按钮为锚点右对齐，按压与开合动画由 miuix 实现。
- 发帖编辑器保留 Android Editable、表情 span、选区和输入法处理；输入区使用 squircle 背景，勾选框和加载指示器通过 ComposeView 使用原生 miuix。View 页面上的确认、验证码和下载弹窗使用 miuix 内容。
- “我的”页采用可滚动的 miuix 个人资料卡片、两行三列快捷入口和连续消息分组，统一 12 dp 外边距、20–24 dp 圆角及原生点击反馈；私信与通知分别保留入口。对应 14 个 Flutter 图标提取为独立矢量，许可见 [图标声明](THIRD_PARTY_ICONS.md)。
- 首页快捷入口卡片的图标顶部留白为 16 dp；三点图标统一缩小 12.5%，按钮点击区域保留。
- “关注与内容 → 首页内容”分别控制首页动态的轮播横幅、频道快捷入口、推荐话题与数码入口；切换直接更新已加载列表，重新开启无需刷新。横幅和频道入口默认开启，升级保留原“头条显示广场”的选择作为推荐话题与数码入口开关。设置文案使用当前页面名称，并明确酷安表情开关控制表情代码转图片。
- 主要图标使用 MiuixIcons。点赞、表情、退格等少量社区动作保留独立 AOSP 矢量源码，附原始 Apache 2.0 许可头，不依赖 Material 图标库；Swatch 保留上游署名。机型名称使用本地绘制的现代手机轮廓，动态与评论操作菜单使用三点 More 图标。
- 删除直接 Material 3、Material Components、Material 图标库及旧 MaterialKolor Compose 配色依赖。miuix 自身内部使用的颜色算法和窗口尺寸分类工具由 miuix 管理。折叠工具栏库未使用 Material API，已排除其发布时附带的 Material 和预览工具依赖。

- 启动页采用 Android 原生 AnimatedVectorDrawable，无穷符号展开后回到原图标。参考 TG-Twilight/c001apk 的系统启动画面形式，沿用本项目现有图标路径，自行实现展开、回弹和淡入动画；系统启动画面仅等待本地账号恢复，数盟 ID 在后台获取，不阻塞首页或深链导航；主地址和回退地址均失败时，前台仅提示一次 Toast，引导前往“设置 → 协议与设备 → 数字联盟 ID”手动填写。已有 ID 或用户已手动填写时不提示，不显示额外加载页面。

## 构建

最低 Android **13 / API 33，ARM64**，使用官方 miuix-blur 的 AGSL 渲染；没有低版本模糊降级分支。目标 API 35，编译平台 `android-37.0`。

构建工具版本已固定：JDK 21（miuix-nav 内联 API 的字节码要求）、Gradle 9.7.1、AGP 9.4.1、Kotlin 2.4.20、KSP 2.3.12、Hilt 2.60.1、Activity / Activity Compose 1.13.0。Activity 版本提供 miuix 窗口菜单要求的 NavigationEventDispatcher。Android SDK 需安装 `platforms;android-37.0` 和 `build-tools;36.0.0`，子模块还使用 API 34/35。

```sh
export JAVA_HOME=/path/to/jdk21
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/path/to/Android/Sdk
bash ./gradlew :app:assembleRelease
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :media-support:testDebugUnitTest :app:lintDebug
bash ./gradlew :app:assembleDebugAndroidTest
# 已连接 Android 13+ 设备时：
bash ./gradlew :app:connectedDebugAndroidTest
```

压缩 APK：`app/build/outputs/apk/release/c001apk_<版本名>(<版本号>).apk`；调试 APK：`app/build/outputs/apk/debug/app-debug.apk`。配置发布密钥时调试包和发布包使用相同签名，未配置时使用本地调试签名；调试版和发布版统一使用独立包名 `com.example.c001apk.compose`；后续覆盖更新需保留同一签名密钥。发布包启用 R8 和资源压缩。未配置发布密钥时，本地构建使用本机调试密钥；CI Release 强制要求发布密钥。签名配置支持环境变量及 local.properties；公开分发需保管固定发布密钥。

版本名使用 `1.3.9`，支持 `-PappVersion=x.y.z`；建议从包含 Git 历史的仓库或 bundle 构建。网络环境需要代理时，Gradle/JVM 应配置对应代理属性。

## 验证范围

保留原有应用和媒体单元测试，新增设置开关只保存一次、单选弹窗即时应用，以及横竖屏发帖布局与 Editable 输入的仪器测试。测试范围见 [验证记录](VERIFICATION.md)，最终安装包的版本、签名与摘要见交付目录内的 APK 校验 JSON。未登录情况下不测试发帖、点赞、私信等会写入远程账号的操作。

## 数盟 ID

当前使用参考 People-11/shuzilm_id 注册格式重写的纯 Kotlin 实现，不再加载或打包 `libdu.so`。双地址并发查询，连接和读取超时沿用参考实现的 30 秒，应用调用层保留启动不阻塞和手动填写保护。已有 ID 继续使用，自动生成结果不会覆盖并发手动修改。

具体模块、字段范围、失败处理和显式网络验证见 [数盟注册说明](DIGITAL_UNION.md)。旧的原生栈越界补丁保留为[历史记录](DIGITAL_UNION_NATIVE_PATCH.md)。

## 分发校验

`python3 tools/verify_apk.py <APK> --sdk <Android SDK> --certificate-sha256 <预期证书摘要>` 核对独立包名、版本、Android 13 下限、ARM64 原生数盟库、压缩、v2/v3 签名和 16 KiB zipalign。证书解析兼容不同 Build Tools 输出前缀，不允许用 Source Stamp 代替实际签名者。

CI 支持手动构建；推送 `vMAJOR.MINOR.PATCH` 标签会执行测试、Lint、Release 构建和签名校验，再自动发布 APK、校验报告和 SHA256SUMS。也可从 `main` 手动运行 `Publish Release` 并指定已有标签，使用当前工作流重试该标签源码的发布。Release 要求 `KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD` 和 `SIGNING_CERT_SHA256`，缺失或签名不符即失败；Debug 使用调试密钥。Gradle 支持同名 `C001APK_` 环境变量及既有 `local.properties`，密钥不打包进源码或产物。

## 本轮上游功能移植

完整范围和 33 条 TG-Twilight 提交对应关系见 [移植跟踪](TWILIGHT_PORT.md)。默认分享使用 `www.coolapk1s.com`，官方链接仍能打开；数盟改用精简 Kotlin 注册协议。公告小助手和独立 announcements 分支按用户要求排除。

应用自更新默认读取 `KiritoXDone/c001apk` 的正式 Release，自动检查开关和手动检查入口统一位于“设置 → 关于 → 应用更新”。应用内不提供自定义更新来源，旧来源配置不再读取；开发者构建时可传入 `-PupdateRepository=owner/repo`，CI 默认使用当前 GitHub 仓库。

## 我的页面恢复

“我的”页在恢复前台时静默更新个人资料、关注/粉丝数量和消息角标，保留列表状态及已有内容，不启动下拉刷新动画。手动下拉仍显示进度；如果后台请求尚未结束，手动刷新复用该请求。资料和角标结果均检查账号版本，账号切换后的旧响应不能写回。自动首次刷新与恢复刷新合并为一个入口。
