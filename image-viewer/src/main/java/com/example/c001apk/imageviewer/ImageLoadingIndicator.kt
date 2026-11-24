package com.example.c001apk.imageviewer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import kotlin.math.min

internal class ImageLoadingIndicator(context: Context) : View(context) {
    private val strokeWidth = 4f * resources.displayMetrics.density
    private val arcBounds = RectF()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(64, 0, 0, 0)
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = this@ImageLoadingIndicator.strokeWidth
    }
    private var progress = 0
    private var fraction = 0f
    private var rotationDegrees = 0f
    private var animation: ValueAnimator? = null

    fun setProgress(percent: Int) {
        val value = percent.coerceIn(0, 100)
        if (progress == value) return
        progress = value
        if (isShown && isAttachedToWindow) {
            updateAnimation()
        } else {
            fraction = progress / 100f
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) updateAnimation() else stopAnimation()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) - strokeWidth
        if (radius <= 0f) return
        canvas.drawCircle(centerX, centerY, radius + strokeWidth / 2, backgroundPaint)
        arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        val startAngle = if (progress == 0) -90f + rotationDegrees else -90f
        val sweepAngle = if (progress == 0) 72f else (fraction * 360f).coerceAtLeast(5f)
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, arcPaint)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = ProgressBar::class.java.name
        if (progress > 0) {
            info.rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(
                AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_PERCENT,
                0f,
                1f,
                progress / 100f,
            )
        }
    }

    private fun updateAnimation() {
        stopAnimation()
        animation = if (progress == 0) {
            fraction = 0f
            ValueAnimator.ofFloat(0f, 360f).apply {
                duration = 900L
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    rotationDegrees = it.animatedValue as Float
                    invalidate()
                }
            }
        } else {
            ValueAnimator.ofFloat(fraction, progress / 100f).apply {
                duration = 300L
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    fraction = it.animatedValue as Float
                    invalidate()
                }
            }
        }
        animation?.start()
    }

    private fun stopAnimation() {
        animation?.cancel()
        animation = null
    }
}
