package com.example.c001apk.compose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.constant.Constants.seedColors
import com.example.c001apk.compose.ui.component.rememberConfiguredHapticFeedback
import top.yukonga.miuix.kmp.theme.*

@Composable
fun C001apkComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeType: ThemeType = ThemeType.Default,
    seedColor: String? = null,
    materialYou: Boolean = true,
    pureBlack: Boolean = false,
    paletteStyle: Int = 0,
    fontScale: Float = 1f,
    contentScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val nativeDefault = !materialYou && themeType == ThemeType.Default
    val seed =
        if (materialYou || nativeDefault) null
        else
            Color(
                seedColors.getOrNull(ThemeType.entries.indexOf(themeType))
                    ?: "FF$seedColor".toLongOrNull(16)
                    ?: seedColors[0],
            )
    val mode =
        when {
            nativeDefault && darkTheme -> ColorSchemeMode.Dark
            nativeDefault -> ColorSchemeMode.Light
            darkTheme -> ColorSchemeMode.MonetDark
            else -> ColorSchemeMode.MonetLight
        }
    val controller =
        remember(mode, seed, paletteStyle) {
            ThemeController(
                colorSchemeMode = mode,
                keyColor = seed,
                paletteStyle = paletteStyleOptions.getOrNull(paletteStyle)?.style ?: ThemePaletteStyle.TonalSpot,
            )
        }
    val base = controller.currentColors()
    val colors =
        if (darkTheme)
            base.copy(
                background = if (pureBlack) Color.Black else Color(0xFF101010),
                surface = if (pureBlack) Color.Black else Color(0xFF101010),
            )
        else base
    val view = LocalView.current
    if (!view.isInEditMode)
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor =
                if (Build.VERSION.SDK_INT < 26 && !darkTheme) Color(0xFF202020).toArgb()
                else Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= 29) {
                @Suppress("DEPRECATION")
                window.isStatusBarContrastEnforced = false
                @Suppress("DEPRECATION")
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    val haptics = rememberConfiguredHapticFeedback()
    val density = LocalDensity.current
    MiuixTheme(colors = colors) {
        CompositionLocalProvider(
            LocalHapticFeedback provides haptics,
            LocalContentColor provides colors.onSurface,
            LocalDensity provides
                Density(
                    density.density * contentScale,
                    density.fontScale * fontScale * DefaultFontScale,
                ),
        ) {
            content()
        }
    }
}
