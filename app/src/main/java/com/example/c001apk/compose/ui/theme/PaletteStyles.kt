package com.example.c001apk.compose.ui.theme

import androidx.annotation.StringRes
import com.example.c001apk.compose.R
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

internal data class PaletteStyleOption(
    val style: ThemePaletteStyle,
    @StringRes val label: Int,
)

// 顺序对应已持久化索引，上游新增枚举不会自动成为应用选项。
internal val paletteStyleOptions = listOf(
    PaletteStyleOption(ThemePaletteStyle.TonalSpot, R.string.palette_style_tonal_spot),
    PaletteStyleOption(ThemePaletteStyle.Neutral, R.string.palette_style_neutral),
    PaletteStyleOption(ThemePaletteStyle.Vibrant, R.string.palette_style_vibrant),
    PaletteStyleOption(ThemePaletteStyle.Expressive, R.string.palette_style_expressive),
    PaletteStyleOption(ThemePaletteStyle.Rainbow, R.string.palette_style_rainbow),
    PaletteStyleOption(ThemePaletteStyle.FruitSalad, R.string.palette_style_fruit_salad),
    PaletteStyleOption(ThemePaletteStyle.Monochrome, R.string.palette_style_monochrome),
    PaletteStyleOption(ThemePaletteStyle.Fidelity, R.string.palette_style_fidelity),
    PaletteStyleOption(ThemePaletteStyle.Content, R.string.palette_style_content),
)
