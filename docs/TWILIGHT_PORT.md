# TG-Twilight 功能移植跟踪

## 2026-10-28 增量移植

参考上游 [1.5.0 / 0b6a58b](https://github.com/TG-Twilight/c001apk/commit/0b6a58be3f209290b1b75a8d97108ebb43fc1082)，将适用行为移植到原生 Compose/Miuix：

| 功能 | 原生实现 |
| --- | --- |
| 手动导入数盟 ID 优先于迟到的生成结果 | `IdentityWriteGuard` 在 DataStore 原子更新中核对身份版本、ID 和设备参数；同值导入也使旧请求失效，保留原生进程隔离、回退地址及非阻塞启动 |
| 过滤后的有限分页 | 普通列表、评论、楼中楼、本地关注以原始响应推进游标；每批最多自动补取 3 页，随后显示手动继续入口，暂停不等于服务端结束 |
| 本地个人内容过滤 | 关键词、UID、完整话题名称；动态/评论/用户卡片范围；同句例外词；命中原因、临时查看、停用规则、单条内容例外；保留已取数据以立即恢复显示 |
| 秋风远程规则导入 | 设置 → 关注与内容 → 内容过滤 → 导入秋风规则；手动下载/更新、独立规则明细页、标准/严格级别、逐条停用及移除；保留个人规则和内容例外 |
| 首页发布按钮开关 | 设置 → 关注与内容 → 隐藏首页发布按钮，默认保持显示 |
| 大屏阅读侧栏及阅读返回栈 | 收起/展开信息流，保持两侧位置；沿用 Miuix 原生导航，最多保留 8 条阅读记录及各自状态 |
| 话题关注结果 | 接受无 `data` 的成功响应，错误/验证码不修改状态；合并连续点击并保存请求的目标状态 |

内容过滤默认关闭，不预置针对特定用户的名单。用户可手动导入 [TG-Twilight 远程规则](https://github.com/TG-Twilight/c001apk/blob/filters/rules.json)，导入不自动启用过滤，不在启动时请求或后台自动更新。规则下载使用独立无账号凭据的客户端，限制 1 MiB、20 秒总超时，拒绝重定向、无效格式及旧版本/同版本变更；通过校验后原子替换远程快照，保留个人规则、例外及停用选择。匹配保留 schema v1 的 UID、完整话题名/ID、多组关键词距离、句内例外及严格级别语义，不执行远程正则。

液态玻璃不移植。HDR 与实况照片已由 `media-support` 与图片浏览模块实现，本次保留现有链路。

本批回归入口：`IdentityWriteGuardTest`、`ContentFilterTest`、`ContentFilterStoreTest`、`TwilightRulesTest`、`TwilightRulesClientTest`、`FilteredPaginationTest`、`LocalFollowPagerTest`、`TopicFollowResultTest`、`AppNavigatorTest`，以及实机 `ContentFilterInteractionTest`、`FilteredFeedPaginationTest`、`MainRailLayoutTest`、`MiuixNavigationTest`。

## 前一批移植

用户范围：只移植 TG-Twilight 在其提交中新增、补全和修复的 Android 相关功能。当前原生 Compose + miuix 实现、独立包名、数盟 SDK 和用户指定的 UI 调整继续保留。

用户追加要求：默认分享使用 `www.coolapk1s.com`，不附加官方域名的第二条链接；继续识别 `coolapk1s.com`、`www.coolapk1s.com` 与酷安官方域名。

用户明确排除：独立 announcements 分支及「c001apk 小助手」公告功能，不创建公告分支或接入公告请求。应用更新检查和酷安小秘书仍在范围内。

上游冻结点：`fb3e06cac5c65e9f86c902f1b4304aeee055f3e2`（main，1.3.5）。审阅完整历史 81 条；TG-Twilight GitHub 身份有 33 条，其中 2 条使用 Simon Twilight 显示名且仅修改文档。不会因上游继续提交而悄悄扩大本轮范围。

## 提交约定

- 使用 Conventional Commits：`feat(scope): summary`、`fix(scope): summary`、`test(scope): summary`、`docs(scope): summary`、`build(scope): summary`。
- 一个功能或独立修复对应一个提交；测试与所验证功能一起提交，避免无法独立评审的大杂烩。
- 提交正文写明行为变化、关键验证，并以 `Upstream-commit: <完整 SHA>` 记录对应来源。
- Author / Committer 均为 `KiritoXDone <58523182+KiritoXDone@users.noreply.github.com>`，每次提交前后核验；不重写已有历史。
- 不直接 cherry-pick Dart/Flutter、桌面宿主或 CI Secret 配置；Android 功能用 Kotlin 实现，相关行为用原生测试验收。

## 功能清单

| 范围 | 当前状态 | 验收要点 |
| --- | --- | --- |
| 我的页面与对应图标 | 已完成 | `0051208`；Flutter 对应矢量、三栏快捷入口、账号/设置入口；真机查看游客页面 |
| 多账号 / 游客模式 | 已完成 | `21d6c3d`、`c0fdff9`；账号状态及真实 DataStore 关闭重开测试通过，拒绝旧会话异步登录 |
| 本地 / 悄悄关注与分类 | 已完成 | `2eb7c52`、`c0fdff9`；游客入口、名单/分类持久化；分页顺序、去重、错误重试、并发和取消测试通过 |
| 酷安 API / 登录 / 验证码 | 已完成 | `09db7ee`、`62eb4c1`；V3、错误分类、原生验证、单次重试、公开详情回退；Android 16 只读接口测试通过 |
| 设备标识与持久会话 | 已完成 | `ffcacab`、`09db7ee`、`62eb4c1`；保留 native 数盟默认生成，官方 ID 导入为可选；会话与身份绑定、过期及持久化测试通过 |
| c001apk 自身更新 | 已完成；发布仓库未配置 | `6e6e8a8`；独立客户端、语义版本、正式 Release、ABI、冷启动/手动/跳过；发布解析单测通过 |
| 私信 / 小秘书 / 事件链接 | 已完成 | `c3a6a7c`、`43ac343`、`e58d3fb`；图片/动态消息、重试/取消、消息计数及事件导航；链接测试通过，未发送真实私信 |
| 自适应 / 链接 / 返回 / 图片 | 已完成 | `471d3f5`、`43ac343`、`62eb4c1`、`f5bdeba`；列数/分栏策略与 URL 边界测试通过；宽屏设备尚未实测 |
| Markdown | 已完成 | `8908230`、`f5bdeba`；原生 AST 渲染、原文切换、安全解析、限行预览；解析测试及 APK 资源测试通过 |
| 正文导出 | 已完成 | `f0a0d90`、`f5bdeba`；Markdown/离线 HTML、热门评论、图片限制；导出单测及真机 Markdown SAF 保存通过 |
| 公告小助手 | 用户明确不需要，不移植 | 独立公告分支、公告客户端与入口全部排除 |
| 构建与分发 | 已完成本地构建及校验；云端 CI 未运行 | `8db4df6`；Android 13+ ARM64、R8、资源保留、原生库压缩、签名及对齐校验通过 |

## 上游提交索引

仅文档、桌面和 Flutter 构建机制变动记录为无需功能移植；不能将功能提交笼统标成完成。本地提交与已执行验证见下表及 [验证记录](VERIFICATION.md)；实现完成不代表所有远程写入场景均已实测。

| 上游提交 | 内容 | 处理 |
| --- | --- | --- |
| [`e9b9262`](https://github.com/TG-Twilight/c001apk/commit/e9b9262d6e4ef3d7e442507749799e7e24b8a3f0) | 网络协议、签名、错误分类、游客详情回退、登录与验证码、缓存清理；Revive c001apk and add reproducible multi-platform Actions builds | `77e6771`、`09db7ee`、`62eb4c1`；协议/会话/验证码单测、真机只读接口通过；原生缓存清理保留；补齐点赞 `detail=0` 与 `trace` 表单合同测试，用户已确认真机点赞成功 |
| [`b0f87eb`](https://github.com/TG-Twilight/c001apk/commit/b0f87eb32442a65b6b53206a9df416baacdba66f) | Android SDK CI 安装；Install Android SDK command-line tools explicitly in CI | `8db4df6`；CI 显式安装 command-line tools 和各模块所需平台 |
| [`934a736`](https://github.com/TG-Twilight/c001apk/commit/934a73684600871820fc83172dd38f10ddb7760d) | Android SDK CI 安装；Avoid requesting the discontinued Android tools package | `8db4df6`；不请求废弃 tools 包 |
| [`735ca56`](https://github.com/TG-Twilight/c001apk/commit/735ca56126a3c02c06a3075b38efc880586ada07) | iOS 构建；Pass the iOS binary before lipo architecture verification flags | 不适用于 Android 原生工程 |
| [`001038f`](https://github.com/TG-Twilight/c001apk/commit/001038f1aaf1c31a896017382aee175e9859e89a) | 文档 / 发布记录 / 仓库维护；Document verified Android split version codes and add build badge | 无新增应用功能，保留来源记录 |
| [`0c68f9d`](https://github.com/TG-Twilight/c001apk/commit/0c68f9d2e205736ccb56eb98ce654d6770d374fc) | 文档 / 发布记录 / 仓库维护；Revise browsing content and features in README | 无新增应用功能，保留来源记录 |
| [`65c698b`](https://github.com/TG-Twilight/c001apk/commit/65c698b706943a0b6c172da18c37d52fea26cb71) | 文档 / 发布记录 / 仓库维护；Record successful cloud builds and artifact verification | 无新增应用功能，保留来源记录 |
| [`38db0a9`](https://github.com/TG-Twilight/c001apk/commit/38db0a95e0cf75d8b7597ed9907bf9e0af0f8077) | 文档 / 发布记录 / 仓库维护；Merge remote README edits with cloud build verification docs | 无新增应用功能，保留来源记录 |
| [`4bf240a`](https://github.com/TG-Twilight/c001apk/commit/4bf240a714b1a7af4b8f3a5e3de0a0b56595dbaf) | 文档 / 发布记录 / 仓库维护；Update basic account repair progress and center README feature table | 无新增应用功能，保留来源记录 |
| [`1fe9eb6`](https://github.com/TG-Twilight/c001apk/commit/1fe9eb6991c1fd8b4c2c781be4689fc67373be29) | 文档 / 发布记录 / 仓库维护；Link the published 1.1.0 release from download and build guides | 无新增应用功能，保留来源记录 |
| [`5a0663f`](https://github.com/TG-Twilight/c001apk/commit/5a0663f1fddecb4cf14b71fddb7dc311013ce5e4) | 多账号与游客切换、本地/悄悄关注、自身更新、导航与分类设置；Release 1.2.0 features: local follows, accounts and update checks | `21d6c3d`、`2eb7c52`、`6e6e8a8`、`471d3f5`；账号、关注、更新、导航/设置已对应移植 |
| [`53bcec0`](https://github.com/TG-Twilight/c001apk/commit/53bcec0bfc805edf2b04fe215e25849018e538c5) | 更新提示异步回归；Wait for update UI state across real and fake async clocks | `6e6e8a8`；更新检查合并请求、冷启动一次、界面恢复后再显示结果；保留原生异步生命周期 |
| [`7a15623`](https://github.com/TG-Twilight/c001apk/commit/7a15623ad8ce7fdce1956d66784ba5c054094e10) | iOS 构建；Align iOS native lockfile with upgraded WebView dependency | 不适用于 Android 原生工程 |
| [`c2bb245`](https://github.com/TG-Twilight/c001apk/commit/c2bb24587c761ac1659d467f86afaab270c58b06) | iOS 构建；Package unsigned iOS arm64 IPA with release checksums | 不适用于 Android 原生工程 |
| [`1b093c8`](https://github.com/TG-Twilight/c001apk/commit/1b093c8014e8d30f17be73d9b93c984cdf3ca845) | 账号切换真实 IO 回归；Wait for account actions to finish real IO in widget tests | `c0fdff9`；AccountStoreIoTest 使用真实文件和两个 DataStore 生命周期验证切换 |
| [`14621eb`](https://github.com/TG-Twilight/c001apk/commit/14621eb98aa1e8c9dc257e1658bd59a96696ab69) | iOS 构建；Document 1.2.0 release and unsigned iOS download | 不适用于 Android 原生工程 |
| [`a2f5fbb`](https://github.com/TG-Twilight/c001apk/commit/a2f5fbbdfd05e82388a87587bff5670b6e67b2ba) | 关注分类、图片/动态私信、酷安小秘书、事件链接、表情处理；feat: release 1.2.5 with follow categories and richer messaging | `2eb7c52`、`c3a6a7c`、`43ac343`、`e58d3fb`；分类、图片/动态消息、小秘书、事件、表情已实现 |
| [`148effb`](https://github.com/TG-Twilight/c001apk/commit/148effb6e83260af365801082f577e08634779fc) | 文档 / 发布记录 / 仓库维护；docs: use compact SVG banner in README | 无新增应用功能，保留来源记录 |
| [`aed2333`](https://github.com/TG-Twilight/c001apk/commit/aed23337b2c4dbbf5a4a4f72daa68579dfdf6213) | 文档 / 发布记录 / 仓库维护；chore: keep temporary release notes out of tracked files | 无新增应用功能，保留来源记录 |
| [`50d6f28`](https://github.com/TG-Twilight/c001apk/commit/50d6f28d1a93d7d9b53561c82b48cc645ca5635f) | 文档 / 发布记录 / 仓库维护；docs: version screenshot paths to avoid stale image caches | 无新增应用功能，保留来源记录 |
| [`94e9a6e`](https://github.com/TG-Twilight/c001apk/commit/94e9a6e0a3fcd9cb2468110054ee5255044297ab) | 文档 / 发布记录 / 仓库维护；docs: record verified 1.2.5 cloud distribution | 无新增应用功能，保留来源记录 |
| [`4084b81`](https://github.com/TG-Twilight/c001apk/commit/4084b814d4d56fd5f0b412a72ecc314881ce4fa5) | 空验证码字段 validate Cookie、验证后重试、每次冷启动更新检查；fix: restore verified comments and startup updates for 1.2.6 | `09db7ee`、`6e6e8a8`；空字段验证 Cookie/单次重试和每进程冷启动检查已实现 |
| [`1f23265`](https://github.com/TG-Twilight/c001apk/commit/1f2326567ff6314573b5a5a43fc2f865c173bde0) | 文档 / 发布记录 / 仓库维护；docs: record verified 1.2.6 cloud distribution | 无新增应用功能，保留来源记录 |
| [`875c542`](https://github.com/TG-Twilight/c001apk/commit/875c542209162b548ad8283b3a550abbeaeb9945) | 官方设备标识导入、设备参数一致性、API 会话与验证码状态持久化；feat: import official device identity and persist API sessions for 1.2.8 | `ffcacab`、`09db7ee`、`62eb4c1`；保留更适合本工程的 native 数盟默认方案，导入作为可选能力 |
| [`f309b43`](https://github.com/TG-Twilight/c001apk/commit/f309b4382b5ef12675437d584487d54e5b890eee) | 文档 / 发布记录 / 仓库维护；Update DEVICE_ID_IMPORT.md | 无新增应用功能，保留来源记录 |
| [`99ceff3`](https://github.com/TG-Twilight/c001apk/commit/99ceff3fd211b5789b631dda201d9d011b0543df) | Android 签名配置与维护环境；chore: resume Flutter maintenance and unify Android signing | `8db4df6`；配置外部签名、校验摘要；不复用上游 Flutter 包名或签名 |
| [`9a601e9`](https://github.com/TG-Twilight/c001apk/commit/9a601e928d06205c9035868407b7ebd1ef583ed1) | 自适应浏览与阅读面板、消息布局、返回动效、Android 链接、分享与下架提示；feat: prepare 1.3.2 with adaptive layouts and expressive UI | `43ac343`、`471d3f5`、`e58d3fb`、`62eb4c1`；原生自适应、阅读分栏、返回、分享、下架提示和图库能力 |
| [`d8a095d`](https://github.com/TG-Twilight/c001apk/commit/d8a095db0396b5b3a6aa1a7f3761b8dcc9f31891) | Android API 37 发布包名；fix: install Android API 37 by its published SDK package name [skip ci] | `8db4df6`；使用已发布的 platforms;android-37.0 |
| [`e077e15`](https://github.com/TG-Twilight/c001apk/commit/e077e1506dbcd52f2c141a63fd5944a2ea85486d) | Flutter Android 插件注册；fix: regenerate Android plugin registration for release builds [skip ci] | 不适用于 Kotlin/Compose 原生工程，无 Flutter 插件注册过程 |
| [`f1fa1b4`](https://github.com/TG-Twilight/c001apk/commit/f1fa1b427425cda0a2e9ecb73652b6f8e7d3e3cf) | APK 证书核验工具兼容；fix: parse Build Tools 37 certificate reports without weakening verification [skip ci] | `8db4df6`；两个证书解析测试及实际 Release APK 校验通过 |
| [`b31ae23`](https://github.com/TG-Twilight/c001apk/commit/b31ae23ea1fa022016fc21299571aa28b835bc3e) | APK 内原生库压缩；fix: compress Android native libraries in local and CI APKs [skip ci] | `8db4df6`；jniLibs.useLegacyPackaging=true，逐个检查 ZIP 内 .so 压缩方式 |
| [`98526cc`](https://github.com/TG-Twilight/c001apk/commit/98526cc30cc6ed5d96fc4bed32ee03dfc92b7024) | Markdown、正文导出、公告小助手；feat: add Markdown reading, content export and announcement assistant [skip ci] | `8908230`、`f0a0d90`、`f5bdeba`；Markdown/导出已实现；公告小助手按用户要求排除 |
| [`fb3e06c`](https://github.com/TG-Twilight/c001apk/commit/fb3e06cac5c65e9f86c902f1b4304aeee055f3e2) | 将测试功能纳入稳定应用身份与存储；release: integrate test features under stable identity for 1.3.5 [skip ci] | 功能整合到既有 com.example.c001apk.compose 身份与 DataStore；覆盖安装保留 native ID，不迁入 Flutter 存储 |
