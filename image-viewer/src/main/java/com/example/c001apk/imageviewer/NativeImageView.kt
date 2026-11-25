package com.example.c001apk.imageviewer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class NativeImageView(context: Context) : View(context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scroller = OverScroller(context)
    private val imageRect = RectF()
    private val scaleTolerance = 1.001f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private val longPressSlop = max(resources.displayMetrics.density, touchSlop / 4f)
    private var decodedImage: DecodedNativeImage? = null
    private var previewTask: Future<*>? = null
    private var originalTask: Future<*>? = null
    private var requestedImage = 0
    private var displayedImage = 0
    private var displayRectChanged: (() -> Unit)? = null
    private var zoomAnimation: ValueAnimator? = null
    private var isPageVisible = true
    private var isTilingEnabled = false
    private var hasAggregateVisibility = false
    private var disposed = false
    private var closing = false
    private var hadMultiplePointers = false
    private var movedSinceDown = false
    private var downX = 0f
    private var downY = 0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var tappedAt: Pair<Float, Float>? = null

    val drawable: Drawable?
        get() = decodedImage?.drawable

    val displayRect: RectF
        get() = RectF(imageRect)

    var currentScale = 1f
        private set
    var fitScale = 1f
        private set
    var fillScale = 1f
        private set
    var maxScale = 1f
        private set
    var isReadMode = false
        set(value) {
            if (field == value) return
            field = value
            resetViewport()
        }

    private var restingScale = 1f
    private val isZoomedBeyondRest get() = currentScale > restingScale * scaleTolerance
    private val ownsHorizontalDrag get() = isZoomedBeyondRest || (isReadMode && imageRect.width() > width + 1f)

    internal var tapCallback: OnTapCallback? = null
    internal var longTapCallback: OnLongTapCallback? = null

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                lastFocusX = detector.focusX
                lastFocusY = detector.focusY
                return !imageRect.isEmpty
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (!detector.scaleFactor.isFinite() || detector.scaleFactor <= 0f) return false
                val scale = (currentScale * detector.scaleFactor).coerceIn(fitScale, maxScale)
                val ratio = scale / currentScale
                val left = detector.focusX - (lastFocusX - imageRect.left) * ratio
                val top = detector.focusY - (lastFocusY - imageRect.top) * ratio
                lastFocusX = detector.focusX
                lastFocusY = detector.focusY
                setViewport(scale, left, top)
                return true
            }
        },
    ).apply {
        isQuickScaleEnabled = false
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (hadMultiplePointers) return false
                tappedAt = e.x to e.y
                return performClick()
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (imageRect.isEmpty) return false
                val doubleTapScale = when {
                    isReadMode -> max(fillScale, 1f)
                    fillScale * 1.2f >= 1f -> max(fillScale, fitScale * 1.5f)
                    else -> max(1f, fitScale * 1.5f)
                }
                val target = if (isZoomedBeyondRest) restingScale else doubleTapScale
                animateZoom(target.coerceIn(fitScale, maxScale), e.x, e.y)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (!hadMultiplePointers && !movedSinceDown && !isZoomedBeyondRest) {
                    longTapCallback?.onLongTap(this@NativeImageView, e.x, e.y)
                }
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                if (hadMultiplePointers || !canPan()) return false
                setViewport(currentScale, imageRect.left - distanceX, imageRect.top - distanceY)
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (hadMultiplePointers || !canPan()) return false
                val fullyFitsWidth = imageRect.width() <= width
                val fullyFitsHeight = imageRect.height() <= height
                val left = imageRect.left.roundToInt()
                val top = imageRect.top.roundToInt()
                scroller.fling(
                    left,
                    top,
                    if (fullyFitsWidth) 0 else velocityX.roundToInt(),
                    if (fullyFitsHeight) 0 else velocityY.roundToInt(),
                    if (fullyFitsWidth) left else (width - imageRect.width()).roundToInt(),
                    if (fullyFitsWidth) left else 0,
                    if (fullyFitsHeight) top else (height - imageRect.height()).roundToInt(),
                    if (fullyFitsHeight) top else 0,
                )
                postInvalidateOnAnimation()
                return true
            }
        },
    )

    init {
        isClickable = true
    }

    fun setImageDrawable(drawable: Drawable?) {
        if (disposed) return
        requestedImage++
        previewTask?.cancel(true)
        originalTask?.cancel(true)
        val decoded = drawable?.let {
            DecodedNativeImage(it, it.intrinsicWidth.coerceAtLeast(1), it.intrinsicHeight.coerceAtLeast(1))
        }
        replaceImage(decoded, preservePosition = false)
    }

    fun zoomTo(scale: Float, animate: Boolean = false): Boolean {
        if (imageRect.isEmpty || !scale.isFinite() || scale <= 0f) return false
        val target = scale.coerceIn(fitScale, maxScale)
        val centerX = width / 2f
        val centerY = height / 2f
        if (animate) {
            animateZoom(target, centerX, centerY)
        } else {
            stopMotion()
            setScale(target, centerX, centerY)
        }
        return true
    }

    internal fun load(file: File, isPreview: Boolean) {
        if (disposed || closing) return
        val request = ++requestedImage
        if (isPreview) previewTask?.cancel(true) else originalTask?.cancel(true)
        val task = imageExecutor.submit {
            val result = try {
                NativeImageDecoder.decode(resources, file)
            } catch (error: RuntimeException) {
                Log.w(TAG, "图片解码失败", error)
                null
            }
            mainHandler.post {
                if (result == null) return@post
                if (disposed || closing || (request != requestedImage && !(isPreview && request > displayedImage && drawable == null))) {
                    result.tiles?.close()
                    return@post
                }
                displayedImage = request
                replaceImage(result, preservePosition = drawable != null)
            }
        }
        if (isPreview) previewTask = task else originalTask = task
    }

    internal fun setDisplayRectListener(listener: () -> Unit) {
        displayRectChanged = listener
    }

    internal fun ownsVerticalDrag(isDown: Boolean): Boolean {
        if (imageRect.isEmpty) return false
        if (isZoomedBeyondRest) return true
        return if (isDown) imageRect.top < -1f else imageRect.bottom > height + 1f
    }

    internal fun setPageVisible(visible: Boolean) {
        isPageVisible = visible
        if (!visible) stopMotion()
        updateAnimatedImage()
        updateTiles()
    }

    internal fun setTilingEnabled(enabled: Boolean) {
        isTilingEnabled = enabled
        updateTiles()
        invalidate()
    }

    internal fun freezeForClose() {
        closing = true
        stopMotion()
        previewTask?.cancel(true)
        originalTask?.cancel(true)
    }

    internal fun dispose() {
        if (disposed) return
        disposed = true
        previewTask?.cancel(true)
        originalTask?.cancel(true)
        displayRectChanged = null
        tapCallback = null
        longTapCallback = null
        replaceImage(null, preservePosition = false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val image = decodedImage ?: return
        if (imageRect.isEmpty) return
        val saved = canvas.save()
        canvas.translate(imageRect.left, imageRect.top)
        canvas.scale(currentScale, currentScale)
        val preview = canvas.save()
        if (isTilingEnabled) image.tiles?.clipDecodedTiles(canvas)
        image.drawable.draw(canvas)
        canvas.restoreToCount(preview)
        if (isTilingEnabled) image.tiles?.draw(canvas)
        canvas.restoreToCount(saved)
    }

    override fun verifyDrawable(who: Drawable): Boolean = who === drawable || super.verifyDrawable(who)

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        hasAggregateVisibility = isVisible
        updateAnimatedImage()
        updateTiles()
    }

    override fun onDetachedFromWindow() {
        hasAggregateVisibility = false
        stopMotion()
        updateAnimatedImage()
        updateTiles()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val position = viewportPosition(oldw, oldh)
        resetViewport(position)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                stopMotion()
                hadMultiplePointers = false
                movedSinceDown = false
                downX = event.x
                downY = event.y
                parent?.requestDisallowInterceptTouchEvent(ownsHorizontalDrag)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                hadMultiplePointers = true
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val horizontal = abs(event.x - downX)
                val vertical = abs(event.y - downY)
                val distance = hypot(horizontal, vertical)
                movedSinceDown = movedSinceDown || distance > longPressSlop
                if (hadMultiplePointers || distance > touchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(
                        hadMultiplePointers || ownsHorizontalDrag || vertical > horizontal,
                    )
                }
            }
        }
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        val (x, y) = tappedAt ?: (width / 2f to height / 2f)
        tappedAt = null
        tapCallback?.onTap(this, x, y)
        return true
    }

    override fun computeScroll() {
        if (!scroller.computeScrollOffset()) return
        setViewport(currentScale, scroller.currX.toFloat(), scroller.currY.toFloat())
        postInvalidateOnAnimation()
    }

    private fun replaceImage(image: DecodedNativeImage?, preservePosition: Boolean) {
        val position = if (preservePosition) viewportPosition(width, height) else null
        stopMotion()
        (drawable as? Animatable)?.stop()
        drawable?.callback = null
        decodedImage?.tiles?.close()
        decodedImage = image
        setLayerType(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && image?.drawable is Animatable) {
                LAYER_TYPE_SOFTWARE
            } else {
                LAYER_TYPE_NONE
            },
            null,
        )
        image?.drawable?.apply {
            callback = this@NativeImageView
            setBounds(0, 0, image.width, image.height)
            if (this is BitmapDrawable) isFilterBitmap = true
        }
        image?.tiles?.onTilesChanged = { invalidate() }
        resetViewport(position)
        updateAnimatedImage()
    }

    private data class ViewportPosition(
        val xFraction: Float,
        val yFraction: Float,
        val zoomOverFit: Float,
        val atReadingScale: Boolean,
    )

    private fun viewportPosition(viewWidth: Int, viewHeight: Int): ViewportPosition? {
        if (imageRect.isEmpty) return null
        val atRest = abs(currentScale - restingScale) < restingScale / 1000f
        val readingPositionChanged = isReadMode && (imageRect.left < -1f || imageRect.top < -1f)
        if (atRest && !readingPositionChanged) return null
        return ViewportPosition(
            (viewWidth / 2f - imageRect.left) / imageRect.width(),
            (viewHeight / 2f - imageRect.top) / imageRect.height(),
            currentScale / fitScale,
            atReadingScale = atRest && isReadMode,
        )
    }

    private fun resetViewport(position: ViewportPosition? = null) {
        val image = decodedImage
        if (image == null || width == 0 || height == 0) {
            imageRect.setEmpty()
            invalidate()
            displayRectChanged?.invoke()
            return
        }
        val scaleX = width.toFloat() / image.width
        val scaleY = height.toFloat() / image.height
        fitScale = min(scaleX, scaleY)
        fillScale = max(scaleX, scaleY)
        restingScale = when {
            !isReadMode -> fitScale
            image.height > image.width -> scaleX
            else -> scaleY
        }
        maxScale = max(restingScale, MAX_RENDERED_DIMENSION / max(image.width, image.height))
        val scale = if (position == null || (position.atReadingScale && isReadMode)) {
            restingScale
        } else {
            (fitScale * position.zoomOverFit).coerceIn(fitScale, maxScale)
        }
        val left = position?.let { width / 2f - image.width * scale * it.xFraction } ?: 0f
        val top = position?.let { height / 2f - image.height * scale * it.yFraction } ?: 0f
        setViewport(scale, left, top)
    }

    private fun setViewport(scale: Float, left: Float, top: Float) {
        val image = decodedImage ?: return
        val imageWidth = image.width * scale
        val imageHeight = image.height * scale
        val x = if (imageWidth <= width) (width - imageWidth) / 2f else left.coerceIn(width - imageWidth, 0f)
        val y = if (imageHeight <= height) (height - imageHeight) / 2f else top.coerceIn(height - imageHeight, 0f)
        currentScale = scale
        imageRect.set(x, y, x + imageWidth, y + imageHeight)
        updateTiles()
        invalidate()
        displayRectChanged?.invoke()
    }

    private fun canPan(): Boolean = imageRect.width() > width + 1f || imageRect.height() > height + 1f

    private fun animateZoom(target: Float, x: Float, y: Float) {
        stopMotion()
        if (currentScale == target) return
        zoomAnimation = ValueAnimator.ofFloat(currentScale, target).apply {
            duration = 200L
            interpolator = DecelerateInterpolator()
            addUpdateListener { setScale(it.animatedValue as Float, x, y) }
            start()
        }
    }

    private fun setScale(scale: Float, x: Float, y: Float) {
        val ratio = scale / currentScale
        setViewport(
            scale,
            x - (x - imageRect.left) * ratio,
            y - (y - imageRect.top) * ratio,
        )
    }

    private fun stopMotion() {
        zoomAnimation?.cancel()
        zoomAnimation = null
        scroller.forceFinished(true)
    }

    private fun updateAnimatedImage() {
        val animation = drawable as? Animatable ?: return
        if (isPageVisible && hasAggregateVisibility && !disposed) {
            if (!animation.isRunning) animation.start()
        } else {
            animation.stop()
        }
    }

    private fun updateTiles() {
        val image = decodedImage ?: return
        val tiles = image.tiles ?: return
        val bitmap = (image.drawable as? BitmapDrawable)?.bitmap ?: return
        val decodedScale = min(bitmap.width.toFloat() / image.width, bitmap.height.toFloat() / image.height)
        if (!isTilingEnabled || !isPageVisible || !hasAggregateVisibility || imageRect.isEmpty ||
            currentScale <= decodedScale * scaleTolerance
        ) {
            tiles.updateViewport(null, currentScale)
            return
        }
        tiles.updateViewport(
            RectF(
                -imageRect.left / currentScale,
                -imageRect.top / currentScale,
                (width - imageRect.left) / currentScale,
                (height - imageRect.top) / currentScale,
            ),
            currentScale,
        )
    }

    private companion object {
        const val TAG = "NativeImageView"
        const val MAX_RENDERED_DIMENSION = 1_048_576f
        val imageExecutor = Executors.newFixedThreadPool(2)
    }
}
