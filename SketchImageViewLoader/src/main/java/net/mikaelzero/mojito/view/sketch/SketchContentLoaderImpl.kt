package net.mikaelzero.mojito.view.sketch

import android.content.Context
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.example.c001apk.imageviewer.ContentLoader
import com.example.c001apk.imageviewer.OnLongTapCallback
import com.example.c001apk.imageviewer.OnTapCallback
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import net.mikaelzero.mojito.view.sketch.core.zoom.ImageZoomer
import net.mikaelzero.mojito.view.sketch.BuildConfig
import kotlin.math.abs
import kotlin.math.max


/**
 * @Author: MikaelZero
 * @CreateDate: 2020/6/10 10:01 AM
 * @Description:
 */
class SketchContentLoaderImpl : ContentLoader, LifecycleObserver {


    private lateinit var sketchImageView: SketchImageView
    private lateinit var frameLayout: FrameLayout
    private var isLongHeightImage = false
    private var isLongWidthImage = false
    private var matrixListener: ImageZoomer.OnMatrixChangeListener? = null

    override fun providerRealView(): View {
        return sketchImageView
    }

    override fun providerView(): View {
        return frameLayout
    }

    override val displayRect: RectF
        get() {
            val rectF = RectF()
            sketchImageView.zoomer?.getDrawRect(rectF)
            return RectF(rectF)
        }

    override fun init(context: Context, originUrl: String, targetUrl: String?) {
        frameLayout = FrameLayout(context)
        sketchImageView = SketchImageView(context)
        sketchImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        sketchImageView.isZoomEnabled = true
        sketchImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        sketchImageView.options.isDecodeGifImage = true
        frameLayout.addView(sketchImageView)
        sketchImageView.zoomer?.blockDisplayer?.setPause(true)
    }

    override fun onDisplayRectChanged(onChange: () -> Unit) {
        matrixListener?.let { sketchImageView.zoomer?.removeOnMatrixChangeListener(it) }
        matrixListener = ImageZoomer.OnMatrixChangeListener { onChange() }.also {
            sketchImageView.zoomer?.addOnMatrixChangeListener(it)
        }
    }

    override fun dispose() {
        matrixListener?.let { sketchImageView.zoomer?.removeOnMatrixChangeListener(it) }
        matrixListener = null
    }

    override fun ownsVerticalDrag(isDown: Boolean): Boolean {
        val zoomer = sketchImageView.zoomer ?: return false
        val baseScale = if (isLongHeightImage || isLongWidthImage) {
            max(zoomer.fullZoomScale, zoomer.fillZoomScale)
        } else {
            zoomer.minZoomScale
        }
        if (zoomer.zoomScale - baseScale > 0.02f) return true
        val rect = displayRect
        return if (isDown) rect.top < -1f else rect.bottom > sketchImageView.height + 1f
    }

    override fun dragging(scale: Float) = Unit

    override fun beginBackToMin(isResetSize: Boolean) {
        sketchImageView.zoomer?.blockDisplayer?.setPause(true)
        if (isLongHeightImage || isLongWidthImage) {

        } else {
            if (isResetSize) {
                sketchImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }
    }

    override fun backToNormal() {
    }

    override fun loadAnimFinish() {
        if (isLongHeightImage || isLongWidthImage) {

        } else {
            sketchImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        sketchImageView.zoomer?.blockDisplayer?.setPause(false)
    }

    override fun isLongImage(width: Int, height: Int): Boolean {
        val windowWidth = sketchImageView.width.takeIf { it > 0 }
            ?: sketchImageView.resources.displayMetrics.widthPixels
        isLongHeightImage = height > width * 40f / 9f
        isLongWidthImage = width > height * 8f && width > windowWidth * 2f
        val isLongImage = isLongHeightImage || isLongWidthImage
        sketchImageView.zoomer?.isReadMode = isLongImage
        if (!isLongImage) sketchImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        return isLongImage
    }

    override fun onTapCallback(onTapCallback: OnTapCallback) {
        sketchImageView.zoomer?.setOnViewTapListener { view, x, y ->
            onTapCallback.onTap(view, x, y)
        }
    }

    override fun onLongTapCallback(onLongTapCallback: OnLongTapCallback) {
        val viewConfig = ViewConfiguration.get(sketchImageView.context)
        val touchSlop = viewConfig.scaledTouchSlop.toFloat()
        val density = sketchImageView.context.resources.displayMetrics.density
        val cancelSlop = max(1f * density, touchSlop / 4f)
        var downX = 0f
        var downY = 0f
        var moved = false
        var multiTouch = false
        var isZoomed = false
        val updateParentIntercept = { disallow: Boolean ->
            sketchImageView.parent?.requestDisallowInterceptTouchEvent(disallow)
        }

        val gestureDetector = GestureDetector(
            sketchImageView.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    if (BuildConfig.DEBUG) {
                        Log.d("LongPressDebug", "onDown x=${e.x} y=${e.y}")
                    }
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (kotlin.math.abs(distanceX) > cancelSlop ||
                        kotlin.math.abs(distanceY) > cancelSlop
                    ) {
                        moved = true
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "LongPressDebug",
                            "onScroll dx=$distanceX dy=$distanceY moved=$moved zoomed=$isZoomed multi=$multiTouch"
                        )
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "LongPressDebug",
                            "onLongPress x=${e.x} y=${e.y} moved=$moved zoomed=$isZoomed multi=$multiTouch"
                        )
                    }
                    if (moved || multiTouch || isZoomed) return
                    onLongTapCallback.onLongTap(sketchImageView, e.x, e.y)
                }
            }
        )

        sketchImageView.setOnTouchListener { _, event ->
            val zoomer = sketchImageView.zoomer
            val baseScale = zoomer?.let {
                if (isLongHeightImage || isLongWidthImage) max(it.fullZoomScale, it.fillZoomScale)
                else it.minZoomScale
            } ?: 1f
            isZoomed = zoomer != null && zoomer.zoomScale - baseScale > 0.02f
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount > 1) {
                        multiTouch = true
                        moved = true
                        updateParentIntercept(true)
                        return@setOnTouchListener false
                    }
                    moved = false
                    multiTouch = false
                    downX = event.x
                    downY = event.y
                    val disallow = isZoomed || isLongHeightImage || isLongWidthImage
                    updateParentIntercept(disallow)
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "LongPressDebug",
                            "ACTION_DOWN x=$downX y=$downY zoomed=$isZoomed long=$isLongHeightImage/$isLongWidthImage"
                        )
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount > 1) {
                        multiTouch = true
                        moved = true
                        updateParentIntercept(true)
                        return@setOnTouchListener false
                    }
                    val dx = kotlin.math.abs(event.x - downX)
                    val dy = kotlin.math.abs(event.y - downY)
                    val distance = kotlin.math.hypot(dx, dy)
                    if (distance > cancelSlop) {
                        moved = true
                    }
                    val horizontalSwipe = dx > dy && dx > touchSlop
                    val allowParent = !isZoomed && !isLongHeightImage && !isLongWidthImage && horizontalSwipe
                    updateParentIntercept(!allowParent)
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "LongPressDebug",
                            "ACTION_MOVE dx=$dx dy=$dy dist=$distance moved=$moved allowParent=$allowParent zoomed=$isZoomed"
                        )
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    multiTouch = true
                    moved = true
                    updateParentIntercept(true)
                    if (BuildConfig.DEBUG) {
                        Log.d("LongPressDebug", "POINTER_DOWN moved=$moved")
                    }
                }
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    multiTouch = false
                    moved = true
                    updateParentIntercept(false)
                    if (BuildConfig.DEBUG) {
                        Log.d("LongPressDebug", "END action=${event.actionMasked} moved=$moved")
                    }
                }
            }
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    override fun pageChange(isHidden: Boolean) {

    }

}
