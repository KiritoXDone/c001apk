package com.example.c001apk.compose.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.theme.paletteStyleOptions
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.HorizontalSplit
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.icon.glass.DisplayBrightness
import top.yukonga.miuix.kmp.icon.glass.SoundVibration
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val themeModes = listOf(
    SelectionItem("跟随系统", ThemeMode.FOLLOW_SYSTEM),
    SelectionItem("总是开启", ThemeMode.ALWAYS_ON),
    SelectionItem("总是关闭", ThemeMode.ALWAYS_OFF),
)

private val readingModes = listOf(
    SelectionItem("自动适配大屏", 0),
    SelectionItem("开启（窗口足够宽时）", 1),
    SelectionItem("关闭", 2),
)

internal fun LazyListScope.appearancePreferences(
    prefs: UserPreferencesCompat,
    viewModel: SettingsViewModel,
    onCustomTheme: () -> Unit,
    onNavigationOrder: () -> Unit,
    onFontSize: () -> Unit,
    onContentScale: () -> Unit,
    onHapticStrength: () -> Unit,
) {
    item(key = "Appearance_0") {
        PreferenceGroup(title = "主题") {
            SwitchListItem(
                value = prefs.materialYou,
                leadingImageVector = MiuixIcons.Theme,
                headlineText = "系统主题色",
                onValueChanged = viewModel::setMaterialYou,
            )
            if (!prefs.materialYou) {
                DropdownListItem(
                    headlineText = "主题颜色",
                    value = prefs.themeType,
                    selections = ThemeType.entries.filterNot { it == ThemeType.UNRECOGNIZED }.map { type ->
                        SelectionItem(stringResource(themeTypeLabelRes(type)), type)
                    },
                    onValueChanged = { _, value ->
                        if (value == ThemeType.Custom) onCustomTheme()
                        viewModel.setThemeType(value)
                    },
                    useWindowPopup = true,
                )
                if (prefs.themeType != ThemeType.Default && prefs.themeType != ThemeType.UNRECOGNIZED) {
                    DropdownListItem(
                        headlineText = "调色风格",
                        value = prefs.paletteStyle,
                        selections = paletteStyleOptions.mapIndexed { index, option ->
                            SelectionItem(stringResource(option.label), index)
                        },
                        onValueChanged = { _, value -> viewModel.setPaletteStyle(value) },
                        useWindowPopup = true,
                    )
                }
            }
            DropdownListItem(
                leadingImageVector = MiuixIcons.Glass.DisplayBrightness,
                headlineText = "深色主题",
                value = prefs.themeMode,
                selections = themeModes,
                onValueChanged = { _, value -> viewModel.setDarkTheme(value) },
                useWindowPopup = true,
            )
            if (prefs.themeMode != ThemeMode.ALWAYS_OFF) {
                SwitchListItem(
                    value = prefs.pureBlack,
                    headlineText = "纯黑背景",
                    onValueChanged = viewModel::setPureBlack,
                )
                SwitchListItem(
                    value = prefs.imageFilter,
                    headlineText = "压暗图片",
                    onValueChanged = viewModel::setImageFilter,
                )
            }
        }
    }
    item(key = "Appearance_1") {
        PreferenceGroup(title = "显示") {
            AppearanceDisclosure(
                title = "栏与标签",
                summary = "栏样式、排序与模糊",
                icon = MiuixIcons.HorizontalSplit,
            ) {
                BarStylePreferences(
                    topBarStyle = prefs.topBarStyle,
                    bottomBarStyle = prefs.effectiveBottomBarStyle,
                    onTopBarStyleChange = viewModel::setTopBarStyle,
                    onBottomBarStyleChange = viewModel::setBottomBarStyle,
                )
                BasicListItem(headlineText = "底栏排序", onClick = onNavigationOrder)
                SwitchListItem(
                    value = prefs.blurBars && Build.VERSION.SDK_INT >= 33,
                    enabled = Build.VERSION.SDK_INT >= 33,
                    headlineText = "栏背景模糊",
                    supportingText = if (Build.VERSION.SDK_INT < 33) "需要 Android 13 及以上" else null,
                    onValueChanged = viewModel::setBlurBars,
                )
                SwitchListItem(
                    value = prefs.collapseTabsOnScroll,
                    headlineText = "滚动时收起标签栏",
                    onValueChanged = viewModel::setCollapseTabsOnScroll,
                )
            }
            AppearanceDisclosure(
                title = "字号与缩放",
                icon = MiuixIcons.Rename,
            ) {
                BasicListItem(
                    headlineText = "字体大小",
                    supportingText = scaleLabel(prefs.fontScale),
                    onClick = onFontSize,
                )
                BasicListItem(
                    headlineText = "内容大小",
                    supportingText = scaleLabel(prefs.contentScale),
                    onClick = onContentScale,
                )
            }
            DropdownListItem(
                headlineText = "分栏阅读",
                leadingImageVector = MiuixIcons.VerticalSplit,
                value = prefs.readingMode,
                selections = readingModes,
                onValueChanged = { _, value -> viewModel.setReadingMode(value) },
                useWindowPopup = true,
            )
        }
    }
    item(key = "Appearance_2") {
        PreferenceGroup(title = "手势与触感") {
            SwitchListItem(
                value = prefs.predictiveBack,
                leadingImageVector = MiuixIcons.Back,
                headlineText = "预测返回手势",
                supportingText = "滑动返回时预览上一页",
                onValueChanged = viewModel::setPredictiveBack,
            )
            SwitchListItem(
                value = prefs.hapticFeedback,
                leadingImageVector = MiuixIcons.Glass.SoundVibration,
                headlineText = "触感反馈",
                onValueChanged = viewModel::setHapticFeedback,
            )
            if (prefs.hapticFeedback) {
                SwitchListItem(
                    value = prefs.customHaptics,
                    headlineText = "自定义触感",
                    supportingText = "关闭时跟随系统触感设置",
                    onValueChanged = viewModel::setCustomHaptics,
                )
                if (prefs.customHaptics) {
                    BasicListItem(
                        headlineText = "触感强度",
                        supportingText = prefs.hapticStrength.label,
                        onClick = onHapticStrength,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        SwitchListItem(
                            value = prefs.reverseHapticEffects,
                            headlineText = "触感兼容模式",
                            supportingText = "轻重反馈与预期相反时开启",
                            onValueChanged = viewModel::setReverseHapticEffects,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceDisclosure(
    title: String,
    icon: ImageVector,
    summary: String? = null,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    BasicComponent(
        modifier = Modifier.semantics {
            stateDescription = if (expanded) "已展开" else "已收起"
        },
        title = title,
        summary = summary,
        startAction = { Icon(icon, null, tint = MiuixTheme.colorScheme.onSurfaceVariantSummary) },
        endActions = {
            Icon(
                if (expanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
        onClick = { expanded = !expanded },
        onClickLabel = if (expanded) "收起" else "展开",
        role = Role.Button,
    )
    if (expanded) {
        Column(Modifier.padding(start = 32.dp)) { content() }
    }
}

private fun scaleLabel(scale: Float): String = "%.2fx".format(scale)

private fun themeTypeLabelRes(themeType: ThemeType): Int =
    when (themeType) {
        ThemeType.Default, ThemeType.UNRECOGNIZED -> R.string.theme_type_default
        ThemeType.Red -> R.string.theme_type_red
        ThemeType.Pink -> R.string.theme_type_pink
        ThemeType.Purple -> R.string.theme_type_purple
        ThemeType.Indigo -> R.string.theme_type_indigo
        ThemeType.Blue -> R.string.theme_type_blue
        ThemeType.LightBlue -> R.string.theme_type_light_blue
        ThemeType.Cyan -> R.string.theme_type_cyan
        ThemeType.Teal -> R.string.theme_type_teal
        ThemeType.Green -> R.string.theme_type_green
        ThemeType.LightGreen -> R.string.theme_type_light_green
        ThemeType.Lime -> R.string.theme_type_lime
        ThemeType.Yellow -> R.string.theme_type_yellow
        ThemeType.Amber -> R.string.theme_type_amber
        ThemeType.Orange -> R.string.theme_type_orange
        ThemeType.DeepOrange -> R.string.theme_type_deep_orange
        ThemeType.Brown -> R.string.theme_type_brown
        ThemeType.BlueGrey -> R.string.theme_type_blue_grey
        ThemeType.Sakura -> R.string.theme_type_sakura
        ThemeType.Custom -> R.string.theme_type_custom
    }
