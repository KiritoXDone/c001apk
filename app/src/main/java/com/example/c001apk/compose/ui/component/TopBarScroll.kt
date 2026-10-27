package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow

internal val LocalTopBarScroll = staticCompositionLocalOf<MutableSharedFlow<Unit>?> { null }
private val LocalTopBarScrollActive = staticCompositionLocalOf { true }

/** Pager neighbours stay composed, but only the visible page should move. */
@Composable
fun TopBarScrollPage(active: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTopBarScrollActive provides (LocalTopBarScrollActive.current && active),
        content = content,
    )
}

@Composable
fun TopBarScrollEffect(scrollToTop: suspend () -> Unit) {
    val requests = LocalTopBarScroll.current
    val active by rememberUpdatedState(LocalTopBarScrollActive.current)
    val scroll by rememberUpdatedState(scrollToTop)
    LaunchedEffect(requests) {
        requests?.collectLatest { if (active) scroll() }
    }
}

@Composable
fun rememberTopBarScrollState(): ScrollState {
    val state = rememberScrollState()
    TopBarScrollEffect { state.animateScrollTo(0) }
    return state
}

fun Modifier.topBarTap(onClickLabel: String? = null, onTap: (() -> Unit)? = null): Modifier = composed {
    val requests = LocalTopBarScroll.current
    if (onTap != null) {
        combinedClickable(
            interactionSource = null,
            indication = null,
            onClick = onTap,
            onClickLabel = onClickLabel,
            onDoubleClick = { requests?.tryEmit(Unit) },
        )
    } else {
        pointerInput(requests) {
            detectTapGestures(
                onDoubleTap = { requests?.tryEmit(Unit) },
            )
        }
    }
}
