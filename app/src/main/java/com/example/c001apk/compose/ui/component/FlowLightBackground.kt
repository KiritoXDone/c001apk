// SPDX-License-Identifier: GPL-3.0-only
package com.example.c001apk.compose.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.cos
import kotlin.math.sin

/** 关于页背景：两组缓动光斑沿椭圆轨迹漂移，颜色取自当前主题。 */
@Composable
fun FlowLightBackground(
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true,
    isDarkTheme: Boolean = MiuixTheme.colorScheme.surface.luminance() < 0.5f,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val phase by
        rememberInfiniteTransition(label = "flowLight")
            .animateFloat(
                initialValue = 0f,
                targetValue = (2f * Math.PI).toFloat(),
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(26000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "phase",
            )
    val baseAlpha = if (isDarkTheme) 0.20f else 0.24f
    val warm = colors.primary.copy(alpha = baseAlpha)
    val cool = colors.secondaryContainer.copy(alpha = baseAlpha * 0.9f)
    val moving = if (isAnimated) phase else 0f

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(color = colors.surface)
            val warmCenter =
                Offset(
                    x = w * (0.5f + 0.28f * sin(moving)),
                    y = h * (0.30f + 0.10f * cos(moving)),
                )
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(warm, Color.Transparent),
                        center = warmCenter,
                        radius = w * 0.85f,
                    ),
                radius = w * 0.85f,
                center = warmCenter,
            )
            val coolCenter =
                Offset(
                    x = w * (0.5f + 0.34f * cos(moving + 1.6f)),
                    y = h * (0.68f + 0.08f * sin(moving + 1.6f)),
                )
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(cool, Color.Transparent),
                        center = coolCenter,
                        radius = w * 0.75f,
                    ),
                radius = w * 0.75f,
                center = coolCenter,
            )
        }
        content()
    }
}
