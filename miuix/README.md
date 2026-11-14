# Miuix 源码

固定来源：[compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)，提交
`2855a36ba516cbf2b5ff46d722da38c94a59c07b`，Apache-2.0 许可证见 [LICENSE](LICENSE)。

保留应用使用的 core、material、squircle、ui、preference、icons、nav 和 blur 模块的
`commonMain`、`androidMain` 原始源码与基线配置；其他平台和示例不参与应用构建。每个模块的
`build.gradle.kts` 是本项目的 Android 构建适配，并将上游性能基线目录映射给 Android 主源码集。
`squircle` 的 SDF 生成任务沿用上游算法，
生成结果位于项目 `build/gradle/`，不纳入版本控制。更新时应一起核对上游模块依赖和生成任务。

模块依赖：`ui` 依赖 `core` 和 `squircle`，`preference` 依赖 `ui`，`icons` 依赖 `core`，
`nav` 依赖 `squircle`，`squircle` 和 `blur` 依赖 `material`。应用直接使用 `ui`、
`preference`、`icons`、`nav` 和 `blur`。
