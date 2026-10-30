<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="c001apk" width="88">

# c001apk

基于 Kotlin 与 Jetpack Compose 的第三方酷安客户端，采用 miuix 界面。

[![Release](https://img.shields.io/github/v/release/KiritoXDone/c001apk?style=flat-square)](https://github.com/KiritoXDone/c001apk/releases/latest)
[![Build](https://github.com/KiritoXDone/c001apk/actions/workflows/release.yml/badge.svg)](https://github.com/KiritoXDone/c001apk/actions/workflows/release.yml)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](#下载)
[![License](https://img.shields.io/badge/License-AGPL--3.0-607D8B?style=flat-square)](LICENSE)

[下载](https://github.com/KiritoXDone/c001apk/releases/latest) · [更新记录](CHANGELOG.md) · [问题反馈](https://github.com/KiritoXDone/c001apk/issues) · [构建指南](docs/BUILDING.md)

</div>

本项目在 [c001apk-compose](https://github.com/frisk1127/c001apk-compose) 的基础上继续开发，移植了 [c001apk 秋风版](https://github.com/TG-Twilight/c001apk) 的部分功能。与酷安官方无隶属关系。

## 下载

从 [Releases](https://github.com/KiritoXDone/c001apk/releases/latest) 下载最新 APK。支持 **Android 7.0 及以上的 ARM64 设备**；模糊效果需要 Android 13 及以上，并在设置中手动开启。

- 应用内更新入口：**设置 → 关于 → 应用更新**。
- 包名：`com.example.c001apk.compose`。同包名、同签名的版本可覆盖升级并保留数据，其他客户端的数据不会自动迁移。
- 每个正式版本附带 `SHA256SUMS` 和 APK 校验报告。

## 界面预览

<table>
  <tr>
    <th>主页</th>
    <th>我的</th>
    <th>外观设置</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/home.png" alt="主页动态列表" width="280"></td>
    <td><img src="docs/screenshots/my.png" alt="我的页面" width="280"></td>
    <td><img src="docs/screenshots/settings.png" alt="外观与字号设置" width="280"></td>
  </tr>
</table>

## 功能

- **浏览与搜索**：动态、文章、应用、用户、话题和数码产品；数码页支持品牌、子分类筛选与排序。
- **互动与消息**：发帖、评论、点赞、账号收藏、私信；通知支持滑动切换分类，回复和私信支持牛牛表情。
- **账号与整理**：多账号、游客模式、本地关注、关注分类、本地收藏和浏览历史。
- **内容过滤**：关键词、用户及话题规则，支持例外和临时查看；可手动导入秋风远程规则，过滤默认关闭。
- **阅读与图片**：Markdown 正文、代码块和表格，Markdown / 离线 HTML 导出，图片缩放、原图查看与保存。
- **界面设置**：明暗主题、主题色、字体与组件大小，可选模糊效果和悬浮底栏，支持宽屏分栏阅读。

## 构建

需要 **JDK 21** 和 Android SDK。克隆完整 Git 历史，配置 `JAVA_HOME`，并通过 `ANDROID_HOME` 或 `local.properties` 的 `sdk.dir` 指定 SDK 路径。

```sh
git clone https://github.com/KiritoXDone/c001apk.git
cd c001apk
bash ./gradlew :app:assembleDebug
```

Debug APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。Windows 使用 `gradlew.bat` 替代 `bash ./gradlew`。

SDK 组件、Release 签名、测试和发布流程见[构建指南](docs/BUILDING.md)。

## 参与贡献

欢迎提交 [Issue](https://github.com/KiritoXDone/c001apk/issues) 或 Pull Request。报告问题前请先搜索已有 Issue，并提供应用版本、设备型号、Android 版本和复现步骤；附图或日志中的账号凭据、Cookie 和设备标识请先移除。

每个 PR 尽量只处理一个问题，说明改动目的和测试方式。移植上游代码时请注明来源与提交，保留原有版权和许可声明。

## 文档

- [构建与发布](docs/BUILDING.md)
- [miuix 界面与导航](docs/MIUIX_NAVIGATION.md)
- [功能移植记录](docs/TWILIGHT_PORT.md)
- [账号收藏](docs/ACCOUNT_COLLECTIONS.md)
- [第三方代码与素材](docs/THIRD_PARTY_PORTS.md) · [图标来源](docs/THIRD_PARTY_ICONS.md)

## 致谢

- [frisk1127/c001apk-compose](https://github.com/frisk1127/c001apk-compose)：Compose 客户端基础。
- [TG-Twilight/c001apk](https://github.com/TG-Twilight/c001apk)：功能移植与协议实现参考。
- [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)：界面组件、导航、图标与模糊效果。
- [YuKongA/Mishka](https://github.com/YuKongA/Mishka)：界面设计参考。
- [daimiaopeng/coolapk-desktop](https://github.com/daimiaopeng/coolapk-desktop)：协议兼容实现参考。

感谢 [LINUX DO](https://linux.do/) 社区的交流与分享。

## 许可

项目采用 [GNU AGPL v3](LICENSE)。第三方代码、SDK、图标和素材遵循各自许可，详见[来源说明](docs/THIRD_PARTY_PORTS.md)与[随包许可证](app/src/main/assets/licenses)。酷安名称、商标及服务内容归相应权利人所有。
