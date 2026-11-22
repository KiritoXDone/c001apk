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
                leadingImageVector = MingCutePaletteLine,
                headlineText = stringResource(R.string.settings_material_you),
                onValueChanged = viewModel::setMaterialYou,
            )
            if (!prefs.materialYou) {
                SelectionListItem(
                    leadingImageVector = MingCuteColorPickerLine,
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
                        leadingImageVector = MingCuteDropletsLine,
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
                leadingImageVector = MingCuteMoonLine,
                title = stringResource(R.string.settings_dark_theme),
                value = prefs.themeMode,
                selections = themeModeItems(),
                onSelected = viewModel::setDarkTheme,
            )
            if (prefs.themeMode != ThemeMode.ALWAYS_OFF) {
                SwitchListItem(
                    value = prefs.pureBlack,
                    leadingImageVector = MingCuteContrastLine,
                    headlineText = stringResource(R.string.settings_pure_black),
                    onValueChanged = viewModel::setPureBlack,
                )
                SwitchListItem(
                    value = prefs.imageFilter,
                    leadingImageVector = MingCuteBrightnessLine,
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
            BasicListItem(headlineText = stringResource(R.string.settings_bar_order), leadingImageVector = MingCuteSortAscendingLine, onClick = onNavigationOrder)
            SwitchListItem(
                value = prefs.blurBars && Build.VERSION.SDK_INT >= 33,
                enabled = Build.VERSION.SDK_INT >= 33,
                leadingImageVector = MingCuteLayersLine,
                headlineText = stringResource(R.string.settings_blur_bars),
                supportingText =
                    if (Build.VERSION.SDK_INT < 33) stringResource(R.string.settings_blur_bars_needs_android13)
                    else null,
                onValueChanged = viewModel::setBlurBars,
            )
            SwitchListItem(
                value = prefs.collapseTabsOnScroll,
                leadingImageVector = MingCuteListCollapseLine,
                headlineText = stringResource(R.string.settings_collapse_tabs),
                onValueChanged = viewModel::setCollapseTabsOnScroll,
            )
        }
    }
    item(key = "Appearance_scale") {
        PreferenceGroup {
            BasicListItem(headlineText = stringResource(R.string.settings_font_size), leadingImageVector = MingCuteFontSizeLine, onClick = onFontSize)
            BasicListItem(headlineText = stringResource(R.string.settings_content_size), leadingImageVector = MingCuteText2Line, onClick = onContentScale)
            SelectionListItem(
                leadingImageVector = MingCuteColumns2Line,
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
                leadingImageVector = MingCuteArrowLeftCircleLine,
                headlineText = stringResource(R.string.settings_predictive_back),
                onValueChanged = viewModel::setPredictiveBack,
            )
            SwitchListItem(
                value = prefs.hapticFeedback,
                leadingImageVector = MingCuteCellphoneVibrationLine,
                headlineText = stringResource(R.string.settings_haptic_feedback),
                onValueChanged = viewModel::setHapticFeedback,
            )
            if (prefs.hapticFeedback) {
                SwitchListItem(
                    value = prefs.customHaptics,
                    leadingImageVector = MingCuteSettings2Line,
                    headlineText = stringResource(R.string.settings_custom_haptics),
                    onValueChanged = viewModel::setCustomHaptics,
                )
                if (prefs.customHaptics) {
                    BasicListItem(
                        leadingImageVector = MingCuteDashboardLine,
                        headlineText = stringResource(R.string.settings_haptic_strength),
                        supportingText = hapticStrengthLabel(prefs.hapticStrength),
                        onClick = onHapticStrength,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        SwitchListItem(
                            value = prefs.reverseHapticEffects,
                            leadingImageVector = MingCuteClockwiseLine,
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
