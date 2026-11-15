package com.example.c001apk.compose.ui.component

import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.GlassSegmentedTabRowDefaults
import top.yukonga.miuix.kmp.glass.glass
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Loaded only after the runtime API check in rememberBarBackdrop. */
@RequiresApi(33)
@Composable
internal fun rememberApi33BarBackdrop(): BarBackdrop {
    val surface = MiuixTheme.colorScheme.surface
    val layer = rememberLayerBackdrop {
        drawRect(surface)
        drawContent()
    }
    return remember(layer) { Api33BarBackdrop(layer) }
}

@RequiresApi(33)
private class Api33BarBackdrop(private val layer: LayerBackdrop) : BarBackdrop {
    override val rawBackdrop: Backdrop get() = layer

    override fun recordModifier(): Modifier = Modifier.layerBackdrop(layer)

    @Composable
    override fun blurModifier(shape: Shape): Modifier =
        Modifier.textureBlur(
            backdrop = layer,
            shape = shape,
            blurRadius = 25f,
            colors =
                BlurDefaults.blurColors(
                    blendColors =
                        listOf(BlendColorEntry(MiuixTheme.colorScheme.surface.copy(alpha = 0.8f))),
                ),
        )

    // 渐进模糊与标签材质参考 BoxProxy bf0b1d5（GPL-3.0），来源见 THIRD_PARTY_PORTS.md。
    @Composable
    override fun progressiveBlurModifier(shape: Shape): Modifier =
        Modifier.progressiveTextureBlur(
            backdrop = layer,
            shape = shape,
            blurRadius = GlassTopAppBarDefaults.ProgressiveBlurRadius.value,
            gradient = ProgressiveBlur.Top.copy(curve = 0.5f),
            colors = BlurDefaults.blurColors(
                blendColors = listOf(BlendColorEntry(MiuixTheme.colorScheme.surface.copy(alpha = 0.3f))),
            ),
        )

    @Composable
    override fun glassTabModifier(shape: GlassShape): Modifier =
        Modifier.glass(
            backdrop = layer,
            shape = shape,
            material = GlassSegmentedTabRowDefaults.material(),
            stroke = GlassSegmentedTabRowDefaults.stroke(),
            shading = false,
        )
}
