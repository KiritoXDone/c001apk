package com.example.c001apk.compose.ui.component.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.c001apk.compose.ui.component.rememberConfiguredHapticFeedback
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Moves one entry; the remaining entries keep their relative order. */
internal fun <T> List<T>.moveItem(from: Int, to: Int): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

/**
 * A short, hand-dragged list of preference rows inside one native [PreferenceGroup] card.
 *
 * Rows are a plain [androidx.compose.foundation.layout.Column] (no nested lazy list), so every
 * row keeps its natural height: larger fonts never clip. Dragging starts from the handle and
 * consumes the down event, which keeps the checkbox/row click and the parent scroll away from a
 * drag. Order changes are published while the finger moves ([onReorder]); the owner keeps the
 * draft. A cancelled drag restores the order captured when the drag began.
 *
 * @param items current order, owned by the caller.
 * @param key stable identity of one row; never the display position.
 * @param onReorder called with the new order on every move, and with the restored order on cancel.
 * @param title optional group title rendered by [PreferenceGroup].
 * @param enabled when false the handles ignore gestures and accessibility moves are unavailable.
 * @param content one row; [dragHandle] must be placed in an end action slot.
 */
@Composable
internal fun <T> ReorderablePreferenceGroup(
    items: List<T>,
    key: (T) -> String,
    onReorder: (List<T>) -> Unit,
    title: String? = null,
    enabled: Boolean = true,
    content: @Composable (item: T, dragging: Boolean, dragHandle: @Composable () -> Unit) -> Unit,
) {
    var localOrder by remember { mutableStateOf<List<T>?>(null) }
    var lastPublished by remember { mutableStateOf<List<T>?>(null) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var settlingKey by remember { mutableStateOf<String?>(null) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var dragStartOrder by remember { mutableStateOf(items) }
    var settleFrom by remember { mutableFloatStateOf(0f) }
    var settleStarted by remember { mutableStateOf(false) }
    val rowHeights = remember { mutableStateMapOf<String, Int>() }
    val settleOffset = remember { Animatable(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val haptics = rememberConfiguredHapticFeedback()
    val scope = rememberCoroutineScope()

    val working = localOrder ?: items
    val dragShape = RoundedCornerShape(16.dp)

    // External edits (for example a second finger changing a checkbox) are newer than our
    // gesture snapshot. Yield without publishing a stale order over that edit.
    LaunchedEffect(items, draggingKey, enabled) {
        if ((!enabled && draggingKey != null) ||
            (localOrder != null && items != localOrder && items != lastPublished)
        ) {
            draggingKey = null
            dragDistance = 0f
            settlingKey = null
            settleJob?.cancel()
            localOrder = null
            lastPublished = null
        } else if (draggingKey == null && localOrder != null && items == localOrder) {
            localOrder = null
            lastPublished = null
        }
    }

    fun startDrag(itemKey: String): Boolean {
        if (!enabled || draggingKey != null) return false
        val index = working.indexOfFirst { key(it) == itemKey }
        if (index < 0 || working.size < 2) return false
        settleJob?.cancel()
        settlingKey = null
        settleStarted = false
        localOrder = working
        lastPublished = items
        dragStartOrder = working
        dragDistance = 0f
        draggingKey = itemKey
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        return true
    }

    fun dragBy(deltaY: Float) {
        val itemKey = draggingKey ?: return
        val order = localOrder ?: items
        val from = order.indexOfFirst { key(it) == itemKey }
        val heights = order.map { rowHeights[key(it)] ?: 0 }
        val (target, offset) = dragReorderTarget(from, heights, dragDistance, deltaY)
        dragDistance = offset
        if (target != from) {
            val moved = order.moveItem(from, target)
            localOrder = moved
            lastPublished = moved
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onReorder(moved)
        }
    }

    fun endDrag() {
        val itemKey = draggingKey ?: return
        val residual = dragDistance
        draggingKey = null
        dragDistance = 0f
        if (abs(residual) < 1f) return
        settlingKey = itemKey
        settleFrom = residual
        settleStarted = false
        settleJob?.cancel()
        settleJob =
            scope.launch {
                settleOffset.snapTo(residual)
                settleStarted = true
                settleOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                )
                if (settlingKey == itemKey) settlingKey = null
            }
    }

    fun cancelDrag() {
        val itemKey = draggingKey
        draggingKey = null
        dragDistance = 0f
        settlingKey = null
        settleStarted = false
        settleJob?.cancel()
        if (itemKey == null) return
        // A cancelled gesture must not leave a partially reordered draft behind.
        val restored = dragStartOrder
        localOrder = restored
        lastPublished = restored
        onReorder(restored)
    }

    fun moveByKey(itemKey: String, direction: Int): Boolean {
        if (!enabled || draggingKey != null) return false
        val order = localOrder ?: items
        val from = order.indexOfFirst { key(it) == itemKey }
        if (from < 0) return false
        val to = from + direction
        if (to !in order.indices) return false
        val movedList = order.moveItem(from, to)
        localOrder = movedList
        lastPublished = movedList
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onReorder(movedList)
        return true
    }

    PreferenceGroup(title = title) {
        working.forEachIndexed { index, item ->
            val itemKey = key(item)
            val isDragging = draggingKey == itemKey
            val isSettling = settlingKey == itemKey
            key(itemKey) {
                // Only the dragged row leaves its slot; the rest reflow as the order changes.
                val offsetY =
                    when {
                        isDragging -> dragDistance
                        isSettling -> if (settleStarted) settleOffset.value else settleFrom
                        else -> 0f
                    }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging || isSettling) 1f else 0f)
                            .offset { IntOffset(0, offsetY.roundToInt()) }
                            .onSizeChanged { rowHeights[itemKey] = it.height }
                            .then(
                                if (isDragging || isSettling) {
                                    Modifier
                                        .shadow(6.dp, dragShape)
                                        .background(MiuixTheme.colorScheme.surfaceContainerHigh, dragShape)
                                } else {
                                    Modifier
                                },
                            ),
                ) {
                    content(item, isDragging) {
                        ReorderHandle(
                            enabled = enabled,
                            canMoveUp = index > 0,
                            canMoveDown = index < working.lastIndex,
                            onStart = { startDrag(itemKey) },
                            onDrag = { if (draggingKey == itemKey) dragBy(it) },
                            onEnd = { if (draggingKey == itemKey) endDrag() },
                            onCancel = { if (draggingKey == itemKey) cancelDrag() },
                            onMoveUp = { moveByKey(itemKey, -1) },
                            onMoveDown = { moveByKey(itemKey, 1) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Drag handle for [ReorderablePreferenceGroup].
 *
 * The down event is consumed before the parent scrollable or the row click can start, and the
 * accessibility actions stay available for users who cannot drag.
 */
@Composable
private fun ReorderHandle(
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onStart: () -> Boolean,
    onDrag: (Float) -> Unit,
    onEnd: () -> Unit,
    onCancel: () -> Unit,
    onMoveUp: () -> Boolean,
    onMoveDown: () -> Boolean,
) {
    val dragDescription = stringResource(R.string.reorder_drag)
    val moveUpDescription = stringResource(R.string.reorder_up)
    val moveDownDescription = stringResource(R.string.reorder_down)
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnEnd by rememberUpdatedState(onEnd)
    val currentOnCancel by rememberUpdatedState(onCancel)
    val currentOnMoveUp by rememberUpdatedState(onMoveUp)
    val currentOnMoveDown by rememberUpdatedState(onMoveDown)

    val color =
        if (enabled) {
            MiuixTheme.colorScheme.onSurfaceVariantActions
        } else {
            MiuixTheme.colorScheme.disabledOnSurface
        }

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .semantics {
                    contentDescription = dragDescription
                    val actions =
                        buildList {
                            if (enabled && canMoveUp) {
                                add(CustomAccessibilityAction(moveUpDescription) { currentOnMoveUp() })
                            }
                            if (enabled && canMoveDown) {
                                add(CustomAccessibilityAction(moveDownDescription) { currentOnMoveDown() })
                            }
                        }
                    if (actions.isNotEmpty()) customActions = actions
                }
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                // The handle owns this gesture: no row click, no parent scroll.
                                down.consume()
                                var initialDelta = 0f
                                val slopChange =
                                    awaitTouchSlopOrCancellation(down.id) { change, overSlop ->
                                        initialDelta = overSlop.y
                                        change.consume()
                                    }
                                    ?: return@awaitEachGesture
                                slopChange.consume()
                                if (!currentOnStart()) return@awaitEachGesture
                                currentOnDrag(initialDelta)
                                var completed = false
                                try {
                                    completed =
                                        drag(down.id) { change ->
                                            currentOnDrag(change.positionChange().y)
                                            change.consume()
                                        }
                                } finally {
                                    if (completed) currentOnEnd() else currentOnCancel()
                                }
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(16.dp)) {
            val strokeWidth = 1.6.dp.toPx()
            listOf(0.2f, 0.5f, 0.8f).forEach { fraction ->
                val y = size.height * fraction
                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
