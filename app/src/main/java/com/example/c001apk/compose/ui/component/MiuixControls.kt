package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun MiuixActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 35.dp,
        minWidth = 35.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors =
            if (LocalDialogPrimaryAction.current) ButtonDefaults.buttonColorsPrimary()
            else ButtonDefaults.buttonColors(),
        content = content,
    )
}

/** Header slot for screens whose title contains a search input or an animated profile. */
@Composable
fun MiuixHeader(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
    color: Color = LocalBarColor.current,
    centerTitle: Boolean = false,
) = CompactBar {
    top.yukonga.miuix.kmp.basic.Surface(color = color) {
        Layout(
            modifier = modifier
                .windowInsetsPadding(windowInsets)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            content = {
                Row { navigationIcon() }
                Box(Modifier.padding(horizontal = 8.dp)) {
                    MiuixTheme(
                        textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.title3),
                    ) {
                        title()
                    }
                }
                Row(content = actions)
            },
        ) { measurables, constraints ->
            val loose = constraints.copy(minWidth = 0, minHeight = 0)
            val navigation = measurables[0].measure(loose)
            val action = measurables[2].measure(loose.copy(maxWidth = loose.maxWidth - navigation.width))
            val titleWidth =
                (constraints.maxWidth -
                    if (centerTitle) 2 * maxOf(navigation.width, action.width)
                    else navigation.width + action.width).coerceAtLeast(0)
            val title = measurables[1].measure(loose.copy(minWidth = titleWidth, maxWidth = titleWidth))
            val height = maxOf(constraints.minHeight, navigation.height, title.height, action.height)
            layout(constraints.maxWidth, height) {
                navigation.placeRelative(0, (height - navigation.height) / 2)
                title.placeRelative(
                    if (centerTitle) (constraints.maxWidth - title.width) / 2 else navigation.width,
                    (height - title.height) / 2,
                )
                action.placeRelative(constraints.maxWidth - action.width, (height - action.height) / 2)
            }
        }
    }
}

@Composable
fun MiuixMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.Start,
    onDismissFinished: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    WindowListPopup(
        show = expanded,
        onDismissRequest = onDismissRequest,
        popupModifier = modifier,
        alignment = alignment,
        onDismissFinished = onDismissFinished,
    ) {
        ListPopupColumn(content)
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
    WindowBottomSheet(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        insideMargin = DpSize(0.dp, 12.dp),
    ) {
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

object MiuixScaffoldDefaults {
    val contentWindowInsets: WindowInsets
        @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)
}
