package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** A continuous card across lazy items, without eagerly composing the whole list. */
@Composable
fun GroupedListItem(
    first: Boolean,
    last: Boolean,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val top = if (first) CardDefaults.CornerRadius else 0.dp
    val bottom = if (last) CardDefaults.CornerRadius else 0.dp
    Column(
        modifier.fillMaxWidth().padding(horizontal = 12.dp).squircleSurface(
            color = MiuixTheme.colorScheme.surfaceContainer,
            topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom,
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides MiuixTheme.colorScheme.onSurfaceContainer) {
            content()
        }
        if (!last && showDivider) HorizontalDivider(Modifier.padding(start = 72.dp, end = 16.dp))
    }
}
