package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlin.math.abs
import kotlin.math.sign

internal val LocalCollapsingTabs = staticCompositionLocalOf<CollapsingTabsState?> { null }

/** Observe actual vertical scrolling without taking distance away from the list. */
internal class CollapsingTabsState(private val thresholdPx: Float) : NestedScrollConnection {
    var visible by mutableStateOf(true)
        private set
    private var direction = 0f
    private var distance = 0f

    fun expand() {
        visible = true
        direction = 0f
        distance = 0f
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (available.y > 0f && consumed.y >= 0f) {
            visible = true
            distance = 0f
        } else if (consumed.y != 0f) {
            val nextDirection = consumed.y.sign
            if (direction != nextDirection) distance = 0f
            direction = nextDirection
            distance += abs(consumed.y)
            if (distance >= thresholdPx) {
                visible = consumed.y > 0f
                distance = 0f
            }
        }
        return Offset.Zero
    }
}
