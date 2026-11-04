package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf

internal class NavigationGestureGate {
    private val children = mutableStateMapOf<Any, String>()

    fun isBlocked(entryId: String) = entryId in children.values

    fun block(entryId: String): Any = Any().also { children[it] = entryId }

    fun release(token: Any) { children.remove(token) }
}

internal data class ParentNavigationGesture(val gate: NavigationGestureGate, val entryId: String)

internal val LocalParentNavigationGesture = staticCompositionLocalOf<ParentNavigationGesture?> { null }

// 原生子导航负责弹层手势，直到退出动画完成后才交还底层页面。
@Composable
internal fun BlockParentNavigationGestures() {
    val parent = LocalParentNavigationGesture.current
    DisposableEffect(parent) {
        val token = parent?.gate?.block(parent.entryId)
        onDispose { if (token != null) parent.gate.release(token) }
    }
}
