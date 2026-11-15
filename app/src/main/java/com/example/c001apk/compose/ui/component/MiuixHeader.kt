package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarTitleAlignment
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
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
    SmallTopAppBar(
        title = "",
        modifier = modifier,
        color = color,
        windowInsets = windowInsets,
        consumeBarTaps = false,
        titlePadding = 8.dp,
        navigationIconPadding = GlassTopAppBarDefaults.HorizontalPadding,
        actionIconPadding = GlassTopAppBarDefaults.HorizontalPadding,
        titleAlignment = if (centerTitle) TopAppBarTitleAlignment.Center else TopAppBarTitleAlignment.Start,
        titleContent = {
            MiuixTheme(textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.title3)) {
                title()
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
    )
}
