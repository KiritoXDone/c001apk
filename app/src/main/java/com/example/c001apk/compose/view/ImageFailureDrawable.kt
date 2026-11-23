package com.example.c001apk.compose.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import com.example.c001apk.compose.R
import android.graphics.drawable.Drawable

/** A bounded placeholder, including a retry hint when there is room for text. */
class ImageFailureDrawable(context: Context) : Drawable() {
    private val retryLabel = context.getString(R.string.image_tap_retry)
    private val density = context.resources.displayMetrics.density
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val attributes =
                context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
            color = attributes.getColor(0, Color.GRAY)
            attributes.recycle()
            strokeWidth = 2 * density
            strokeCap = Paint.Cap.ROUND
            textAlign = Paint.Align.CENTER
            textSize = 12 * density
        }

    override fun draw(canvas: Canvas) {
        val showText = bounds.width() >= 80 * density && bounds.height() >= 64 * density
        val x = bounds.exactCenterX()
        val y = bounds.exactCenterY() - if (showText) 9 * density else 0f
        val radius = minOf(10 * density, bounds.width() / 5f, bounds.height() / 5f)
        paint.style = Paint.Style.STROKE
        canvas.drawArc(x - radius, y - radius, x + radius, y + radius, 40f, 280f, false, paint)
        canvas.drawPath(
            Path().apply {
                moveTo(x + radius, y - radius)
                lineTo(x + radius, y - radius / 3)
                lineTo(x + radius / 3, y - radius / 3)
            },
            paint,
        )
        if (showText) {
            paint.style = Paint.Style.FILL
            canvas.drawText(retryLabel, x, y + 28 * density, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android") override fun getOpacity() = PixelFormat.TRANSLUCENT
}
