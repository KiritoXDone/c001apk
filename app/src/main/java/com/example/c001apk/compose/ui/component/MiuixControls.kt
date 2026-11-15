package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowListPopup

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
    WindowListPopup(
        show = expanded,
        onDismissRequest = onDismissRequest,
        popupModifier = modifier,
        alignment = alignment,
        onDismissFinished = onDismissFinished,
        minWidth = ListPopupDefaults.MinWidth * contentScale,
    ) {
        // Scale inside the platform window, leaving anchor and window coordinates unchanged.
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * contentScale, density.fontScale),
        ) {
            ListPopupColumn(content)
        }
    }
}

/** Transparent overflow button with miuix press feedback and an anchored native popup. */
@Composable
fun MiuixMoreMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        // The popup reads this parent's bounds, so its animation starts at the actual button.
        Box(Modifier.padding(4.dp)) {
            IconButton(
                onClick = { onExpandedChange(true) },
                minHeight = 35.dp,
                minWidth = 35.dp,
                backgroundColor = Color.Transparent,
            ) {
                Icon(
                    imageVector = MiuixIcons.More,
                    contentDescription = "更多操作",
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
    CompositionLocalProvider(
        LocalContentColor provides
            if (enabled) MiuixTheme.colorScheme.onSurface
            else MiuixTheme.colorScheme.disabledOnSurface,
    ) {
        BasicComponent(
            modifier =
                modifier.then(
                    if (!enabled) Modifier.semantics(mergeDescendants = true) { disabled() }
                    else Modifier,
                ).then(
                    if (onLongClick != null)
                        Modifier.combinedClickable(
                            enabled = enabled,
                            role = Role.Button,
                            onClick = onClick,
                            onLongClick = onLongClick,
                            onLongClickLabel = onLongClickLabel,
                        )
                    else Modifier,
                ),
            onClick = if (onLongClick == null) onClick else null,
            enabled = enabled,
            role = Role.Button,
            insideMargin = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // ListPopupColumn queries intrinsic widths with an unbounded constraint.
            // BasicComponent's action-slot layout in miuix 0.9.4 overflows in that pass.
            // Keep its native interaction surface, with intrinsic-safe menu content.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.let { icon ->
                    icon()
                    Spacer(Modifier.width(8.dp))
                }
                Box(Modifier.weight(1f)) { text() }
                trailingIcon?.let { icon ->
                    Spacer(Modifier.width(8.dp))
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
    WindowBottomSheet(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        backgroundColor = MiuixTheme.colorScheme.surface,
        dragHandleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f),
        insideMargin = DpSize(0.dp, 0.dp),
    ) {
        // The platform Dialog supplies a new density; preserve the page's component/font scale.
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
