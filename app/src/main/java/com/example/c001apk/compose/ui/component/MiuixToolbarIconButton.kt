package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.IconButtonDefaults
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.glassShadow
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixToolbarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDownState: Boolean = false,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    if (LocalUserPreferences.current.topBarStyle != TopBarStyle.OS4) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            holdDownState = holdDownState,
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            minHeight = minHeight,
            minWidth = minWidth,
            content = content,
        )
        return
    }
    val backdrop = LocalBarBackdrop.current?.rawBackdrop
    val size = maxOf(GlassTopAppBarDefaults.ButtonSize, minHeight, minWidth)
    val shape = GlassShape(cornerRadius.coerceAtMost(size / 2))
    // 按 BoxProxy bf0b1d5 在按钮外绘制阴影，阴影不依赖模糊开关。
    GlassIconButton(
        onClick = onClick,
        modifier = modifier.glassShadow(shape, GlassShadows.Regular, if (enabled) 1f else 0.38f),
        enabled = enabled,
        holdDownState = holdDownState,
        inheritTopBarMaterial = false,
        backdrop = backdrop,
        size = size,
        shape = shape,
        fill = if (backgroundColor.isSpecified && backgroundColor != Color.Transparent) backgroundColor
            else MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 1f),
        stroke = if (backdrop != null) GlassTopAppBarDefaults.buttonStroke() else null,
        shadow = null,
        content = content,
    )
}
