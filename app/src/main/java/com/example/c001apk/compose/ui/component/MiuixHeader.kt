package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    Layout(
        modifier = modifier.fillMaxWidth().background(color).windowInsetsPadding(windowInsets),
        content = {
            Row(
                modifier = Modifier.padding(start = TopAppBarDefaults.NavigationIconPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) { navigationIcon() }
            Row(
                modifier = Modifier.padding(end = TopAppBarDefaults.ActionIconPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
            Box(
                modifier = Modifier.clipToBounds().padding(horizontal = TopAppBarDefaults.TitlePadding),
                contentAlignment = if (centerTitle) Alignment.Center else Alignment.CenterStart,
            ) {
                MiuixTheme(textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.title3)) {
                    title()
                }
            }
        },
    ) { measurables, constraints ->
        val available = constraints.copy(minWidth = 0, minHeight = 0)
        val navigation = measurables[0].measure(available)
        val actionsWidth = if (available.hasBoundedWidth) available.maxWidth - navigation.width else Constraints.Infinity
        val actions = measurables[1].measure(available.copy(maxWidth = actionsWidth))
        val sideWidth = if (centerTitle) maxOf(navigation.width, actions.width) * 2
            else navigation.width + actions.width
        val titleConstraints = if (available.hasBoundedWidth) {
            val width = (available.maxWidth - sideWidth).coerceAtLeast(0)
            available.copy(minWidth = width, maxWidth = width)
        } else {
            available
        }
        val title = measurables[2].measure(titleConstraints)
        val height = constraints.constrainHeight(
            maxOf(TopAppBarDefaults.CollapsedHeight.roundToPx(), navigation.height, title.height, actions.height),
        )
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth
            else constraints.constrainWidth(sideWidth + title.width)
        layout(width, height) {
            navigation.placeRelative(0, (height - navigation.height) / 2)
            actions.placeRelative(width - actions.width, (height - actions.height) / 2)
            title.placeRelative(if (centerTitle) (width - title.width) / 2 else navigation.width, (height - title.height) / 2)
        }
    }
}
