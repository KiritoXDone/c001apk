package com.example.c001apk.compose.ui.settings

import android.os.Build
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.theme.paletteStyleOptions
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.RotateLeft
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.icon.glass.CollapseList
import top.yukonga.miuix.kmp.icon.glass.Custom
import top.yukonga.miuix.kmp.icon.glass.DisplayBrightness
import top.yukonga.miuix.kmp.icon.glass.EyeProtection
import top.yukonga.miuix.kmp.icon.glass.HapticIntensity
import top.yukonga.miuix.kmp.icon.glass.HighlighterPen
import top.yukonga.miuix.kmp.icon.glass.Layers
import top.yukonga.miuix.kmp.icon.glass.ScreenSize
import top.yukonga.miuix.kmp.icon.glass.SoundVibration

@Composable
private fun themeModeItems() =
    listOf(
        SelectionItem(stringResource(R.string.settings_theme_follow_system), ThemeMode.FOLLOW_SYSTEM),
        SelectionItem(stringResource(R.string.settings_theme_always_on), ThemeMode.ALWAYS_ON),
        SelectionItem(stringResource(R.string.settings_theme_always_off), ThemeMode.ALWAYS_OFF),
    )

@Composable
private fun readingModeItems() =
    listOf(
        SelectionItem(stringResource(R.string.settings_reading_auto), 0),
        SelectionItem(stringResource(R.string.settings_reading_on), 1),
        SelectionItem(stringResource(R.string.settings_reading_off), 2),
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
                leadingImageVector = MiuixIcons.Theme,
                headlineText = stringResource(R.string.settings_material_you),
                onValueChanged = viewModel::setMaterialYou,
            )
            if (!prefs.materialYou) {
                SelectionListItem(
                    leadingImageVector = MiuixIcons.Glass.HighlighterPen,
                    title = stringResource(R.string.settings_theme_color),
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
                        leadingImageVector = MiuixIcons.Glass.Custom,
                        title = stringResource(R.string.settings_palette_style),
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
                leadingImageVector = MiuixIcons.Glass.DisplayBrightness,
                title = stringResource(R.string.settings_dark_theme),
                value = prefs.themeMode,
                selections = themeModeItems(),
                onSelected = viewModel::setDarkTheme,
            )
            if (prefs.themeMode != ThemeMode.ALWAYS_OFF) {
                SwitchListItem(
                    value = prefs.pureBlack,
                    leadingImageVector = MiuixIcons.Background,
                    headlineText = stringResource(R.string.settings_pure_black),
                    onValueChanged = viewModel::setPureBlack,
                )
                SwitchListItem(
                    value = prefs.imageFilter,
                    leadingImageVector = MiuixIcons.Glass.EyeProtection,
                    headlineText = stringResource(R.string.settings_dim_images),
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
            BasicListItem(headlineText = stringResource(R.string.settings_bar_order), leadingImageVector = MiuixIcons.Sort, onClick = onNavigationOrder)
            SwitchListItem(
                value = prefs.blurBars && Build.VERSION.SDK_INT >= 33,
                enabled = Build.VERSION.SDK_INT >= 33,
                leadingImageVector = MiuixIcons.Glass.Layers,
                headlineText = stringResource(R.string.settings_blur_bars),
                supportingText =
                    if (Build.VERSION.SDK_INT < 33) stringResource(R.string.settings_blur_bars_needs_android13)
                    else null,
                onValueChanged = viewModel::setBlurBars,
            )
            SwitchListItem(
                value = prefs.collapseTabsOnScroll,
                leadingImageVector = MiuixIcons.Glass.CollapseList,
                headlineText = stringResource(R.string.settings_collapse_tabs),
                onValueChanged = viewModel::setCollapseTabsOnScroll,
            )
        }
    }
    item(key = "Appearance_scale") {
        PreferenceGroup {
            BasicListItem(headlineText = stringResource(R.string.settings_font_size), leadingImageVector = MiuixIcons.Rename, onClick = onFontSize)
            BasicListItem(headlineText = stringResource(R.string.settings_content_size), leadingImageVector = MiuixIcons.Glass.ScreenSize, onClick = onContentScale)
            SelectionListItem(
                leadingImageVector = MiuixIcons.VerticalSplit,
                title = stringResource(R.string.settings_split_reading),
                value = prefs.readingMode,
                selections = readingModeItems(),
                onSelected = viewModel::setReadingMode,
            )
        }
    }
    item(key = "Appearance_gesture") {
        PreferenceGroup {
            SwitchListItem(
                value = prefs.predictiveBack,
                leadingImageVector = MiuixIcons.Back,
                headlineText = stringResource(R.string.settings_predictive_back),
                onValueChanged = viewModel::setPredictiveBack,
            )
            SwitchListItem(
                value = prefs.hapticFeedback,
                leadingImageVector = MiuixIcons.Glass.SoundVibration,
                headlineText = stringResource(R.string.settings_haptic_feedback),
                onValueChanged = viewModel::setHapticFeedback,
            )
            if (prefs.hapticFeedback) {
                SwitchListItem(
                    value = prefs.customHaptics,
                    leadingImageVector = MiuixIcons.Tune,
                    headlineText = stringResource(R.string.settings_custom_haptics),
                    onValueChanged = viewModel::setCustomHaptics,
                )
                if (prefs.customHaptics) {
                    BasicListItem(
                        leadingImageVector = MiuixIcons.Glass.HapticIntensity,
                        headlineText = stringResource(R.string.settings_haptic_strength),
                        supportingText = hapticStrengthLabel(prefs.hapticStrength),
                        onClick = onHapticStrength,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        SwitchListItem(
                            value = prefs.reverseHapticEffects,
                            leadingImageVector = MiuixIcons.RotateLeft,
                            headlineText = stringResource(R.string.settings_haptic_compat),
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

@Composable
internal fun hapticStrengthLabel(strength: HapticStrength): String =
    stringResource(
        when (strength) {
            HapticStrength.Light -> R.string.haptic_light
            HapticStrength.Medium -> R.string.haptic_standard
            HapticStrength.Strong -> R.string.haptic_strong
        },
    )
