# 独立矢量图标来源

Copyright 2019 Google LLC. All Rights Reserved.

早期为“我的”页从 TG-Twilight/c001apk Flutter 界面提取 Material Icons 字形。“我的”页现使用 MiuixIcons；这些独立矢量仍有部分用于动态、评论和独立账号管理页。

来源为 Flutter 官方 [字体归档](https://storage.googleapis.com/flutter_infra_release/flutter/fonts/3012db47f3130e62f7cc0beabff968a33cbec8d8/fonts.zip) 的 MaterialIcons-Regular.otf，归档随附 CC BY 4.0 许可；完整许可随 APK 保存于 `assets/licenses/MaterialIcons-CC-BY-4.0.txt`，也可阅读 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)。

修改：提取上述字形、将 512 单位坐标转换为 Compose 的 24 dp 矢量坐标并翻转 Y 轴。没有引入 Flutter 字体运行时或 Material 组件依赖。
