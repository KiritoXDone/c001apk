# Miuix 源码

固定来源：[compose-miuix-ui/miuix v0.9.5-beta02](https://github.com/compose-miuix-ui/miuix/releases/tag/v0.9.5-beta02)，提交
`66248bacc7be29ae6bdc0957de9458b42ac67732`，Apache-2.0 许可证见 [LICENSE](LICENSE)。

保留应用使用的 core、material、squircle、ui、preference、icons、nav、blur、glass 和 glass-icons 模块的
`commonMain`、`androidMain` 原始源码与基线配置；其他平台和示例不参与应用构建。每个模块的
`build.gradle.kts` 是本项目的 Android 构建适配，并将上游性能基线目录映射给 Android 主源码集。
`squircle` 的 SDF 生成任务沿用上游算法，
生成结果位于项目 `build/gradle/`，不纳入版本控制。更新时应一起核对上游模块依赖和生成任务。

模块依赖：`ui` 依赖 `core` 和 `squircle`，`preference` 依赖 `ui`，`icons` 依赖 `core`，
`nav` 依赖 `squircle`，`squircle` 和 `blur` 依赖 `material`。应用直接使用 `ui`、
`preference`、`icons`、`nav`、`blur`、`glass` 和 `glass-icons`。`glass` 依赖 `blur`、
`material` 和 `ui`，`glass-icons` 依赖 `core`。

本地兼容补丁：`SmallTopAppBar` 和 `GlassTopAppBar` 支持可组合标题、
标题对齐、自定义 inset 和点击消费控制；`SmallTopAppBar` 可选按大栏策略仅裁剪标题，
允许控件阴影越过栏边（这是本项目对小栏的适配）；`GlassIconButton` 支持禁用及保持状态，关闭继承时
不回退父 backdrop；`GlassNavigationBar` 支持自定义回退底色；`GlassSegmentedTabRow` 允许
空 backdrop，并允许让溢出轨道的外层滚动容器接管横拖；`GlassDropdownPopup` 支持关闭完成回调及未指定 valueAnchor 时的水平对齐。
`NavDisplay` 根据内容键判断顶层页面变化，避免同一页面的参数更新导致输入框失焦。
`FloatingActionButton` 按实际容器底色选择深浅内容色，以白图标 3:1 的对比度为边界，半透明底色先与页面背景合成。
`InputField` 在外部改写查询文本时把光标置于末尾，清空时同步重置选区。
Android 系统动态配色在调色参数和系统配置不变时复用计算结果。
这些是本项目扩展，更新上游时需逐项复核。
