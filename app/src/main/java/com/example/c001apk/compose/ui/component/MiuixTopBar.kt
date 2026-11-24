package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    color: Color = LocalBarColor.current,
    windowInsets: WindowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    MiuixHeader(
        centerTitle = true,
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MiuixTheme.textStyles.subtitle, maxLines = 1)
                }
            }
        },
        modifier = modifier,
        color = color,
        windowInsets = windowInsets,
        navigationIcon = navigationIcon,
        actions = actions,
    )
}
