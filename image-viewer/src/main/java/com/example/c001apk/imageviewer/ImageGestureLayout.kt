package com.example.c001apk.imageviewer

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

class ImageGestureLayout(
    context: Context,
    private val canDismiss: () -> Boolean,
    private val onDrag: (Float, Float) -> Unit,
    private val onRelease: (Boolean) -> Unit,
) : FrameLayout(context) {
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var downX = 0f
    private var downY = 0f
    private var hadMultiplePointers = false
    private var movedHorizontally = false
    var isDragging = false
        private set

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                isDragging = false
                hadMultiplePointers = false
                movedHorizontally = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                hadMultiplePointers = true
                if (isDragging) finishDrag(dismiss = false)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (abs(dx) > touchSlop && abs(dx) >= abs(dy)) movedHorizontally = true
                if (!isDragging && !hadMultiplePointers && !movedHorizontally && dy > touchSlop &&
                    dy > abs(dx) && canDismiss()
                ) {
                    val cancel = MotionEvent.obtain(event)
                    cancel.action = MotionEvent.ACTION_CANCEL
                    super.dispatchTouchEvent(cancel)
                    cancel.recycle()
                    parent?.requestDisallowInterceptTouchEvent(true)
                    isDragging = true
                }
                if (isDragging) {
                    onDrag(dx, dy.coerceAtLeast(0f))
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    val dismiss = event.actionMasked == MotionEvent.ACTION_UP &&
                        event.rawY - downY > height * 0.18f
                    finishDrag(dismiss)
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun finishDrag(dismiss: Boolean) {
        isDragging = false
        parent?.requestDisallowInterceptTouchEvent(false)
        onRelease(dismiss)
    }

}
