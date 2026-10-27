package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

object ReadingLayout {
    fun columns(width: Float, fontScale: Float) =
        ((width + 10) / (360 * fontScale.coerceIn(1f, 2f) + 10)).toInt().coerceIn(1, 4)

    fun split(width: Float, shortestSide: Float, fontScale: Float, mode: Int) =
        mode != 2 &&
            (mode == 1 || shortestSide >= 600) &&
            width >= 840 * fontScale.coerceIn(1f, 1.5f)
}

/** One native lazy layout at every width keeps the same item anchor when columns change. */
@Composable
fun AdaptiveCardColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    content: LazyStaggeredGridScope.() -> Unit,
) {
    TopBarScrollEffect { state.animateScrollToItem(0) }
    BoxWithConstraints(modifier) {
        val columns = ReadingLayout.columns(maxWidth.value, LocalDensity.current.fontScale)
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columns),
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalItemSpacing = verticalArrangement.spacing,
            content = content,
        )
    }
}
