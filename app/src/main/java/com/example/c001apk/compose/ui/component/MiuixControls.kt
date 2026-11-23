package com.example.c001apk.compose.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.glass.GlassBottomSheet
import top.yukonga.miuix.kmp.glass.GlassDropdownPopup
import top.yukonga.miuix.kmp.glass.GlassMotion
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.PopupLayout

@Composable
fun pullToRefreshTexts(): List<String> = listOf(
    stringResource(R.string.refresh_pull),
    stringResource(R.string.refresh_release),
    stringResource(R.string.refresh_loading),
    stringResource(R.string.refresh_complete),
)

@Composable
fun MiuixMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.Start,
    onDismissFinished: (() -> Unit)? = null,
    contentScale: Float = 0.9f,
    content: @Composable () -> Unit,
) {
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    Spacer(
        Modifier.onGloballyPositioned { coordinates ->
            val anchor = coordinates.parentLayoutCoordinates ?: coordinates
            val position = anchor.positionInWindow()
            anchorBounds = Rect(
                left = position.x,
                top = position.y,
                right = position.x + anchor.size.width,
                bottom = position.y + anchor.size.height,
            )
        },
    )
    val mounted = remember { mutableStateOf(false) }
    val currentExpanded by rememberUpdatedState(expanded)
    val currentFinished by rememberUpdatedState(onDismissFinished)
    LaunchedEffect(expanded) {
        if (expanded) mounted.value = true
    }
    if (!mounted.value) return

    val density = LocalDensity.current
    PopupLayout(
        visible = mounted,
        enableWindowDim = false,
        enableBackHandler = false,
        enterTransition = EnterTransition.None,
        exitTransition = ExitTransition.None,
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * contentScale, density.fontScale),
            LocalDismissState provides onDismissRequest,
        ) {
            Box(Modifier.fillMaxSize()) {
                GlassDropdownPopup(
                    show = expanded,
                    onDismissRequest = onDismissRequest,
                    anchorBounds = anchorBounds,
                    backdrop = null,
                    modifier = modifier,
                    alignment = alignment,
                    visuals = GlassPopupDefaults.visuals(
                        stroke = null,
                        shadow = null,
                        material = null,
                        containerColor = GlassPopupDefaults.containerColor().copy(alpha = 1f),
                    ),
                    onDismissFinished = {
                        if (!currentExpanded) {
                            mounted.value = false
                            currentFinished?.invoke()
                        }
                    },
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun MiuixMoreMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.padding(4.dp)) {
            IconButton(
                onClick = { onExpandedChange(true) },
                minHeight = 35.dp,
                minWidth = 35.dp,
                backgroundColor = Color.Transparent,
            ) {
                Icon(
                    imageVector = MiuixIcons.More,
                    contentDescription = stringResource(R.string.update_more),
                    modifier = Modifier.size(17.5.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            MiuixMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                alignment = PopupPositionProvider.Align.End,
                content = content,
            )
        }
    }
}

// 菜单行参考 Miuix GlassPopupItem（Copyright 2026 compose-miuix-ui contributors，Apache-2.0）。
// 单一交互层保留自定义内容和长按。
@Composable
fun MiuixMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressColor = GlassPopupDefaults.itemPressedColor()
    val pressFill by animateColorAsState(
        targetValue = if (pressed) pressColor else Color.Transparent,
        animationSpec = if (pressed) snap() else GlassMotion.popupPressExit(),
        label = "menuItemPress",
    )
    val color = MiuixTheme.colorScheme.onSurfaceContainer
    CompositionLocalProvider(LocalContentColor provides if (enabled) color else color.copy(alpha = 0.4f)) {
        MiuixTheme(
            textStyles = MiuixTheme.textStyles.copy(
                main = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.Medium),
            ),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = GlassPopupDefaults.ItemMinHeight)
                    .padding(horizontal = GlassPopupDefaults.ItemPressInset)
                    .drawBehind {
                        drawRoundRect(
                            color = pressFill,
                            cornerRadius = CornerRadius(GlassPopupDefaults.ItemPressRadius.toPx()),
                        )
                    }
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onLongClickLabel = onLongClickLabel,
                    )
                    .padding(
                        horizontal = GlassPopupDefaults.ItemPaddingHorizontal - GlassPopupDefaults.ItemPressInset,
                        vertical = GlassPopupDefaults.ItemPaddingVertical,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.let { icon ->
                    icon()
                    Spacer(Modifier.width(GlassPopupDefaults.ItemIconGap))
                }
                Box(Modifier.weight(1f)) { text() }
                trailingIcon?.let { icon ->
                    Spacer(Modifier.width(GlassPopupDefaults.ItemIconGap))
                    icon()
                }
            }
        }
    }
}

@Composable
fun MiuixBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val contentDensity = LocalDensity.current
    GlassBottomSheet(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        backdrop = null,
        stroke = null,
        containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 1f),
        dragHandleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f),
        insideMargin = DpSize(0.dp, 0.dp),
    ) {
        CompositionLocalProvider(LocalDensity provides contentDensity) {
            Box(
                Modifier.windowInsetsPadding(
                    WindowInsets.systemBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Bottom),
                ),
            ) {
                content()
            }
        }
    }
}

object MiuixScaffoldDefaults {
    val contentWindowInsets: WindowInsets
        @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)
}
