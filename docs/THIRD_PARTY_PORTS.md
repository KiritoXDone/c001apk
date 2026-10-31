# 功能移植与协议来源

本工程保留根目录 AGPL-3.0 LICENSE 和原作者声明。Kotlin 功能移植参考 TG-Twilight/c001apk，冻结提交与逐项对应关系见 [TWILIGHT_PORT.md](TWILIGHT_PORT.md)。账号存储、本地关注、设备标识、会话和验证等移植文件沿用 AGPL-3.0。

2026-10-28 增量参考 TG-Twilight `0b6a58b`（1.5.0）的身份写入保护、过滤与分页、发布按钮开关、阅读侧栏和话题关注响应语义。以原生 DataStore、Compose 和 Miuix 导航重新实现，不引入 Flutter 界面、液态玻璃或上游内置用户名单。

可选远程过滤导入使用 [TG-Twilight 的 filters 分支](https://github.com/TG-Twilight/c001apk/blob/filters/rules.json)。`TwilightRuleSet` 和 `TwilightFilterEngine` 根据上游 `content_filter_rule.dart`、`content_filter_engine.dart` 的 schema v1 校验及字面量匹配语义移植，沿用 AGPL-3.0。版本 2 样例仅放在 JVM 测试资源中，不作为应用内置名单；实际名单由用户主动下载后在本机生效。

Token V3 算法、协议查找表、公开分享页 JSON 与网易验证码配置，参考 TG-Twilight 的对应实现，原始协议兼容来源为 [daimiaopeng/coolapk-desktop](https://github.com/daimiaopeng/coolapk-desktop/tree/3ef2ad4da6d171e118d2b29b0f18d7ffa6da9086) 的 `auth.rs`、`auth_blob.b64`、`client.rs`、`neteaseCaptcha.ts`。MIT 完整版权和许可随应用收录在 [coolapk-desktop-MIT.txt](../app/src/main/assets/licenses/coolapk-desktop-MIT.txt)。查找表用于协议兼容，其第三方权利不由该 MIT 许可担保；酷安名称、商标及服务内容归相应权利人。

签名测试向量沿用参考工程的合成设备与固定时间数据，不包含真实账号或设备标识。人机验证由用户在网易官方 Web SDK 中完成，不分发验证码 SDK 源码。

32 张 `niuniu_emotion_*.png` 沿用 TG-Twilight 所收录的酷安官方 16.6.4 表情素材，名称映射用于显示兼容。2026-10-28 按 [issue #19](https://github.com/KiritoXDone/c001apk/issues/19) 核对最新上游，32 张 Git blob 哈希均一致；回复及私信面板提供独立“牛牛”分类，分页由共享目录生成。版权归酷安及相应权利人，根 AGPL-3.0 不改变这些素材的权利。

## 数盟注册协议

纯 Kotlin 注册实现参考 [People-11/shuzilm_id](https://github.com/People-11/shuzilm_id/tree/03118bc4dd2fbf4807f971c839391742c181da9b) 公开说明的协议行为及精简字段集，有效执行路径与其 `sample/DuClient.kt` 保持一致，仅对接工程配置，外围负责后台调用与身份写入保护；省略未调用的分析辅助函数。未引入该仓库的原生样本。当前模块、采集范围和验证方式见 [数盟注册说明](DIGITAL_UNION.md)。

## Markdown

CommonMark 0.30.0 与 GFM 表格、删除线、任务列表扩展遵循 BSD 3-Clause；完整许可证打包于 `app/src/main/assets/licenses/CommonMark-BSD-3-Clause.txt`。解析行为参考 TG-Twilight 98526cc，使用原生 Compose 渲染，不使用 WebView 执行正文。

## 本版本更新渠道

应用版本采用语义版本；可用 Gradle `-PappVersion=x.y.z` 设置版本，`-PupdateRepository=KiritoXDone/仓库名` 配置构建产物的发布源。应用内不提供自定义来源，也不读取旧来源配置。默认更新源为 `KiritoXDone/c001apk`，不请求 TG-Twilight 或 Compose 基线的 APK。自动检查默认开启，每进程冷启动仅检查一次；自动检查开关和手动检查入口统一位于“设置 → 关于 → 应用更新”。

仅使用 GitHub Latest 正式 Release、语义版本比较和设备 ABI 选择；更新说明通过同一安全 Markdown 渲染。浏览器负责下载，不自动安装、不向 GitHub 携带酷安认证信息。接口依据 [GitHub Releases REST API](https://docs.github.com/en/rest/releases/releases#get-the-latest-release)。
