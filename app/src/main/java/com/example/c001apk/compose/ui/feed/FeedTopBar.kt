package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.component.topBarTap

@Composable
internal fun FeedTopBar(
    title: String,
    windowInsets: WindowInsets,
    onTitleClick: () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    MiuixTopBar(
        title = title,
        windowInsets = windowInsets,
        modifier = Modifier
            // A drag must cancel title navigation even if the pointer returns inside the bar.
            .pointerInput(Unit) { detectDragGestures { change, _ -> change.consume() } }
            .topBarTap(
                onClickLabel = "切换动态与评论位置",
                onTap = onTitleClick,
            ),
        navigationIcon = navigationIcon,
        actions = actions,
    )
}
