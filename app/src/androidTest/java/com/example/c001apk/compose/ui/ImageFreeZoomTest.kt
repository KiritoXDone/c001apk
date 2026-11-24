package com.example.c001apk.compose.ui

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.imageviewer.ImageGestureLayout
import net.mikaelzero.mojito.view.sketch.SketchContentLoaderImpl
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import org.junit.Assert.*
import org.junit.Test

class ImageFreeZoomTest {
    @Test
    fun landscapeKeepsFreePinchAndAdaptiveDoubleTap() = verifyZoom(1600, 900)

    @Test
    fun portraitKeepsFreePinchAndAdaptiveDoubleTap() = verifyZoom(900, 1600)

    @Test
    fun longImageDoubleTapPreservesReadingScaleAndPinchCanStillFit() = verifyZoom(300, 3000, true)

    private fun verifyZoom(width: Int, height: Int, readMode: Boolean = false) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        report("Starting ${width}x${height}")
        // HyperOS may freeze a newly instrumented process before ActivityScenario resumes.
        // Establish a foreground activity from inside the instrumentation process first.
        val appId = instrumentation.targetContext.packageName
        instrumentation.uiAutomation.executeShellCommand(
            "am start -W -n $appId/androidx.activity.ComponentActivity",
        ).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        report("Foreground activity ready")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var host: ImageGestureLayout
            lateinit var image: SketchImageView
            var fullScale = 0f
            var initialScale = 0f
            var doubleTapScale = 0f
            var releasedScale = 0f
            scenario.onActivity { activity ->
                val loader = SketchContentLoaderImpl()
                loader.init(activity, "", null)
                host = ImageGestureLayout(activity, { !loader.ownsVerticalDrag(true) }, { _, _ -> }, {})
                host.addView(loader.providerView())
                host.keepScreenOn = true
                image = loader.providerRealView() as SketchImageView
                loader.isLongImage(width, height)
                image.setImageDrawable(BitmapDrawable(activity.resources, bitmap))
                activity.setContentView(host)
                loader.loadAnimFinish()
            }
            instrumentation.waitForIdleSync()
            report("Image host ready")
            scenario.onActivity {
                val zoomer = checkNotNull(image.zoomer)
                assertTrue(zoomer.reset("free-zoom-regression"))
                fullScale = zoomer.fullZoomScale
                initialScale = zoomer.zoomScale
                // These fixtures have no larger original: normal images use fill (at
                // least 1.5x fit), while the long fixture uses its width-fit reading scale.
                doubleTapScale = if (readMode) initialScale else maxOf(zoomer.fillZoomScale, fullScale * 1.5f)
                assertTrue(zoomer.maxZoomScale > doubleTapScale * 8f)
                report("Initial/fit=${initialScale / fullScale}; double-tap/fit=${doubleTapScale / fullScale}")
            }
            // Android starts scaling only after its minimum finger-span threshold;
            // allow enough separate pinches to exceed 8x on high-density phones.
            repeat(6) { step ->
                scenario.onActivity {
                    pinch(host, 200f, 700f)
                    releasedScale = checkNotNull(image.zoomer).zoomScale
                    report("Pinch ${step + 1}: ${releasedScale / fullScale}x fit")
                }
                SystemClock.sleep(400)
            }
            scenario.onActivity {
                val zoomer = checkNotNull(image.zoomer)
                assertTrue("Pinch must pass the former screen-based upper limit", zoomer.zoomScale > fullScale * 8f)
                assertEquals("Releasing must keep the chosen zoom", releasedScale, zoomer.zoomScale, releasedScale * 0.001f)
                assertFalse(host.isDragging)
                report("Released zoom retained; double-tap reset")
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                assertEquals(initialScale, checkNotNull(image.zoomer).zoomScale, initialScale * 0.001f)
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                assertEquals("Double tap must keep its former adaptive target", doubleTapScale,
                    checkNotNull(image.zoomer).zoomScale, doubleTapScale * 0.001f)
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                val zoomer = checkNotNull(image.zoomer)
                assertEquals(initialScale, zoomer.zoomScale, initialScale * 0.001f)
                report("Adaptive double-tap cycle passed; checking numerical guard")
                assertFalse(zoomer.zoom(Float.NaN, false))
                assertFalse(zoomer.zoom(Float.POSITIVE_INFINITY, false))
                assertTrue(zoomer.zoom(zoomer.maxZoomScale, false))
                pinch(host, 200f, 700f)
            }
            SystemClock.sleep(400)
            scenario.onActivity {
                val zoomer = checkNotNull(image.zoomer)
                val rect = RectF().also(zoomer::getDrawRect)
                assertTrue(rect.left.isFinite() && rect.top.isFinite() && rect.right.isFinite() && rect.bottom.isFinite())
                assertEquals(zoomer.maxZoomScale, zoomer.zoomScale, zoomer.maxZoomScale * 0.001f)
                assertTrue(zoomer.zoom(fullScale * 2f, false))
            }
            repeat(3) {
                scenario.onActivity { pinch(host, 700f, 80f) }
                SystemClock.sleep(400)
            }
            scenario.onActivity {
                assertFits(image, fullScale)
                report("Pinch returned to full image")
                image.zoomer?.recycle("test-finished")
                image.setImageDrawable(null)
            }
        }
        bitmap.recycle()
    }

    private fun report(message: String) {
        InstrumentationRegistry.getInstrumentation().sendStatus(2, Bundle().apply {
            putString("stream", message + "\n")
        })
    }

    private fun assertFits(image: SketchImageView, fullScale: Float) {
        val zoomer = checkNotNull(image.zoomer)
        assertEquals(fullScale, zoomer.zoomScale, fullScale * 0.001f)
        val rect = RectF().also(zoomer::getDrawRect)
        assertTrue(rect.width() <= image.width + 1f && rect.height() <= image.height + 1f)
    }

    private fun doubleTap(target: View) {
        val start = SystemClock.uptimeMillis()
        for (offset in listOf(0L, 100L)) {
            for ((action, delay) in listOf(MotionEvent.ACTION_DOWN to 0L, MotionEvent.ACTION_UP to 30L)) {
                MotionEvent.obtain(start + offset, start + offset + delay, action, target.width / 2f, target.height / 2f, 0).also {
                    target.dispatchTouchEvent(it)
                    it.recycle()
                }
            }
        }
    }

    private fun pinch(target: View, fromSpan: Float, toSpan: Float) {
        val start = SystemClock.uptimeMillis()
        var elapsed = 0L
        fun send(action: Int, span: Float, count: Int) {
            val properties = Array(count) { index ->
                MotionEvent.PointerProperties().apply { id = index; toolType = MotionEvent.TOOL_TYPE_FINGER }
            }
            val coordinates = Array(count) { index ->
                MotionEvent.PointerCoords().apply {
                    x = target.width / 2f + if (index == 0) -span / 2f else span / 2f
                    y = target.height / 2f
                    pressure = 1f
                    size = 1f
                }
            }
            MotionEvent.obtain(start, start + elapsed, action, count, properties, coordinates,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0).also {
                target.dispatchTouchEvent(it)
                it.recycle()
            }
            elapsed += 16
        }
        send(MotionEvent.ACTION_DOWN, fromSpan, 1)
        send(MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), fromSpan, 2)
        for (step in 1..20) send(MotionEvent.ACTION_MOVE, fromSpan + (toSpan - fromSpan) * step / 20f, 2)
        send(MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), toSpan, 2)
        send(MotionEvent.ACTION_UP, toSpan, 1)
    }
}
