package com.example.c001apk.compose.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.glass.GlassPopup
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.WindowNavigationEventScope

internal val LocalGlassMenuExpanded = staticCompositionLocalOf<Boolean?> { null }

@Composable
internal fun MiuixGlassMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDismissFinished: (() -> Unit)?,
    modifier: Modifier,
    alignment: PopupPositionProvider.Align,
    enableWindowDim: Boolean,
    content: @Composable () -> Unit,
) {
    var anchorBoundsOnScreen by remember { mutableStateOf<Rect?>(null) }
    Spacer(
        Modifier.onGloballyPositioned { coordinates ->
            val anchor = coordinates.parentLayoutCoordinates ?: coordinates
            val windowOrigin = anchor.windowOriginOnScreen()
            if (windowOrigin.isSpecified) {
                anchorBoundsOnScreen = anchor.boundsInWindow().translate(windowOrigin)
            }
        },
    )

    var isWindowMounted by remember { mutableStateOf(false) }
    var isPanelMounted by remember { mutableStateOf(false) }
    val dimProgress = animateFloatAsState(
        targetValue = if (expanded && enableWindowDim) 1f else 0f,
        animationSpec = if (expanded) ListPopupDefaults.DimEnterAnimationSpec else ListPopupDefaults.DimExitAnimationSpec,
        label = "glassMenuDim",
    )
    val isDimGone by remember { derivedStateOf { dimProgress.value == 0f } }
    LaunchedEffect(expanded, isPanelMounted, isDimGone) {
        if (expanded) {
            isWindowMounted = true
        } else if (isWindowMounted && !isPanelMounted && isDimGone) {
            isWindowMounted = false
            onDismissFinished?.invoke()
        }
    }
    if (!isWindowMounted) return

    val dismiss = { if (expanded) onDismissRequest() }
    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            window.setDimAmount(0f)
            window.setWindowAnimations(0)
        }
        WindowNavigationEventScope {
            // 触发控件和菜单可能分属不同窗口，先通过屏幕坐标对齐。
            var windowOrigin by remember { mutableStateOf(Offset.Unspecified) }
            Box(
                Modifier.fillMaxSize().onGloballyPositioned { windowOrigin = it.windowOriginOnScreen() },
            ) {
                if (enableWindowDim) {
                    Box(
                        Modifier.matchParentSize()
                            .graphicsLayer { alpha = dimProgress.value }
                            .background(MiuixTheme.colorScheme.windowDimming),
                    )
                }
                val anchorBounds = anchorBoundsOnScreen
                GlassPopup(
                    show = expanded && windowOrigin.isSpecified && anchorBounds != null,
                    onDismissRequest = dismiss,
                    anchorBounds = if (windowOrigin.isSpecified && anchorBounds != null) {
                        anchorBounds.translate(-windowOrigin)
                    } else {
                        Rect.Zero
                    },
                    backdrop = null,
                    modifier = modifier,
                    visuals = GlassPopupDefaults.visuals(material = null),
                    horizontalAlignment = when (alignment) {
                        PopupPositionProvider.Align.Start,
                        PopupPositionProvider.Align.TopStart,
                        PopupPositionProvider.Align.BottomStart -> Alignment.Start
                        else -> Alignment.End
                    },
                ) {
                    // GlassPopup 在原生退出动画结束后卸载内容。
                    DisposableEffect(Unit) {
                        isPanelMounted = true
                        onDispose { isPanelMounted = false }
                    }
                    CompositionLocalProvider(LocalGlassMenuExpanded provides expanded, content = content)
                }
            }
        }
    }
}

private fun LayoutCoordinates.windowOriginOnScreen(): Offset =
    localToScreen(Offset.Zero) - positionInWindow()
