package com.example.c001apk.compose.ui.settings

import android.os.Build
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftCircleLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteCellphoneVibrationLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteClockwiseLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteColorPickerLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteColumns2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteContrastLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDashboardLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDropletsLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteFontSizeLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteMoonLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCutePaletteLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteBrightnessLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteListCollapseLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteSettings2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteLayersLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteSortAscendingLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteText2Line
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.theme.paletteStyleOptions

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
    item(key = "Appearance_theme") {
        PreferenceGroup {
            SwitchListItem(
                value = prefs.materialYou,
                leadingImageVector = MingCutePaletteLine,
                headlineText = "系统主题色",
                onValueChanged = viewModel::setMaterialYou,
            )
            if (!prefs.materialYou) {
                SelectionListItem(
                    leadingImageVector = MingCuteColorPickerLine,
                    title = "主题颜色",
                    value = prefs.themeType,
                    selections =
                        ThemeType.entries.filterNot { it == ThemeType.UNRECOGNIZED }.map { type ->
                            SelectionItem(stringResource(themeTypeLabelRes(type)), type)
                        },
                    onSelected = { type ->
                        if (type == ThemeType.Custom) onCustomTheme()
                        viewModel.setThemeType(type)
                    },
                )
                if (prefs.themeType != ThemeType.Default && prefs.themeType != ThemeType.UNRECOGNIZED) {
                    SelectionListItem(
                        leadingImageVector = MingCuteDropletsLine,
                        title = "调色风格",
                        value = prefs.paletteStyle,
                        selections =
                            paletteStyleOptions.mapIndexed { index, option ->
                                SelectionItem(stringResource(option.label), index)
                            },
                        onSelected = viewModel::setPaletteStyle,
                    )
                }
            }
            SelectionListItem(
                leadingImageVector = MingCuteMoonLine,
                title = "深色主题",
                value = prefs.themeMode,
                selections = themeModes,
                onSelected = viewModel::setDarkTheme,
            )
            if (prefs.themeMode != ThemeMode.ALWAYS_OFF) {
                SwitchListItem(
                    value = prefs.pureBlack,
                    leadingImageVector = MingCuteContrastLine,
                    headlineText = "纯黑背景",
                    onValueChanged = viewModel::setPureBlack,
                )
                SwitchListItem(
                    value = prefs.imageFilter,
                    leadingImageVector = MingCuteBrightnessLine,
                    headlineText = "压暗图片",
                    onValueChanged = viewModel::setImageFilter,
                )
            }
        }
    }
    item(key = "Appearance_bars") {
        PreferenceGroup {
            BarStylePreferences(
                topBarStyle = prefs.topBarStyle,
                bottomBarStyle = prefs.effectiveBottomBarStyle,
                onTopBarStyleChange = viewModel::setTopBarStyle,
                onBottomBarStyleChange = viewModel::setBottomBarStyle,
            )
            BasicListItem(headlineText = "底栏排序", leadingImageVector = MingCuteSortAscendingLine, onClick = onNavigationOrder)
            SwitchListItem(
                value = prefs.blurBars && Build.VERSION.SDK_INT >= 33,
                enabled = Build.VERSION.SDK_INT >= 33,
                leadingImageVector = MingCuteLayersLine,
                headlineText = "栏背景模糊",
                supportingText = if (Build.VERSION.SDK_INT < 33) "需要 Android 13 及以上" else null,
                onValueChanged = viewModel::setBlurBars,
            )
            SwitchListItem(
                value = prefs.collapseTabsOnScroll,
                leadingImageVector = MingCuteListCollapseLine,
                headlineText = "滚动时收起标签栏",
                onValueChanged = viewModel::setCollapseTabsOnScroll,
            )
        }
    }
    item(key = "Appearance_scale") {
        PreferenceGroup {
            BasicListItem(headlineText = "字体大小", leadingImageVector = MingCuteFontSizeLine, onClick = onFontSize)
            BasicListItem(headlineText = "内容大小", leadingImageVector = MingCuteText2Line, onClick = onContentScale)
            SelectionListItem(
                leadingImageVector = MingCuteColumns2Line,
                title = "分栏阅读",
                value = prefs.readingMode,
                selections = readingModes,
                onSelected = viewModel::setReadingMode,
            )
        }
    }
    item(key = "Appearance_gesture") {
        PreferenceGroup {
            SwitchListItem(
                value = prefs.predictiveBack,
                leadingImageVector = MingCuteArrowLeftCircleLine,
                headlineText = "预测返回手势",
                onValueChanged = viewModel::setPredictiveBack,
            )
            SwitchListItem(
                value = prefs.hapticFeedback,
                leadingImageVector = MingCuteCellphoneVibrationLine,
                headlineText = "触感反馈",
                onValueChanged = viewModel::setHapticFeedback,
            )
            if (prefs.hapticFeedback) {
                SwitchListItem(
                    value = prefs.customHaptics,
                    leadingImageVector = MingCuteSettings2Line,
                    headlineText = "自定义触感",
                    onValueChanged = viewModel::setCustomHaptics,
                )
                if (prefs.customHaptics) {
                    BasicListItem(
                        leadingImageVector = MingCuteDashboardLine,
                        headlineText = "触感强度",
                        supportingText = prefs.hapticStrength.label,
                        onClick = onHapticStrength,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        SwitchListItem(
                            value = prefs.reverseHapticEffects,
                            leadingImageVector = MingCuteClockwiseLine,
                            headlineText = "触感兼容模式",
                            onValueChanged = viewModel::setReverseHapticEffects,
                        )
                    }
                }
            }
        }
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
