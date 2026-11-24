package com.example.c001apk.compose.ui

import android.graphics.Bitmap
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
import com.example.c001apk.imageviewer.NativeImageContentLoader
import com.example.c001apk.imageviewer.NativeImageView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            lateinit var image: NativeImageView
            val loader = NativeImageContentLoader()
            var fullScale = 0f
            var initialScale = 0f
            var doubleTapScale = 0f
            var releasedScale = 0f
            scenario.onActivity { activity ->
                loader.init(activity, "", null)
                host = ImageGestureLayout(activity, { !loader.ownsVerticalDrag(true) }, { _, _ -> }, {})
                host.addView(loader.providerView())
                host.keepScreenOn = true
                image = loader.providerRealView()
                assertEquals(readMode, loader.isLongImage(width, height))
                image.setImageDrawable(BitmapDrawable(activity.resources, bitmap))
                activity.setContentView(host)
                loader.loadAnimFinish()
            }
            instrumentation.waitForIdleSync()
            report("Image host ready")
            scenario.onActivity {
                fullScale = image.fitScale
                initialScale = image.currentScale
                assertTrue(fullScale > 0f)
                assertEquals(if (readMode) image.fillScale else fullScale, initialScale, 0.001f)
                doubleTapScale = if (readMode) {
                    maxOf(image.fillScale, 1f)
                } else {
                    val adaptiveScale = if (image.fillScale * 1.2f >= 1f) image.fillScale else 1f
                    maxOf(adaptiveScale, fullScale * 1.5f)
                }
                assertTrue(image.maxScale > doubleTapScale * 8f)
                report("Initial/fit=${initialScale / fullScale}; double-tap/fit=${doubleTapScale / fullScale}")
            }
            // Android starts scaling only after its minimum finger-span threshold;
            // allow enough separate pinches to exceed 8x on high-density phones.
            repeat(6) { step ->
                scenario.onActivity {
                    pinch(host, 200f, 700f)
                    releasedScale = image.currentScale
                    report("Pinch ${step + 1}: ${releasedScale / fullScale}x fit")
                }
                SystemClock.sleep(400)
            }
            scenario.onActivity {
                assertTrue("Pinch must pass the former screen-based upper limit", image.currentScale > fullScale * 8f)
                assertEquals("Releasing must keep the chosen zoom", releasedScale, image.currentScale, releasedScale * 0.001f)
                assertFalse(host.isDragging)
                report("Released zoom retained; double-tap reset")
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                assertEquals(initialScale, image.currentScale, initialScale * 0.001f)
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                assertEquals("Double tap must keep its former adaptive target", doubleTapScale,
                    image.currentScale, doubleTapScale * 0.001f)
                doubleTap(host)
            }
            SystemClock.sleep(600)
            scenario.onActivity {
                assertEquals(initialScale, image.currentScale, initialScale * 0.001f)
                report("Adaptive double-tap cycle passed; checking numerical guard")
                assertFalse(image.zoomTo(Float.NaN))
                assertFalse(image.zoomTo(Float.POSITIVE_INFINITY))
                assertTrue(image.zoomTo(image.maxScale))
                pinch(host, 200f, 700f)
            }
            SystemClock.sleep(400)
            scenario.onActivity {
                val rect = image.displayRect
                assertTrue(rect.left.isFinite() && rect.top.isFinite() && rect.right.isFinite() && rect.bottom.isFinite())
                assertEquals(image.maxScale, image.currentScale, image.maxScale * 0.001f)
                assertTrue(image.zoomTo(fullScale * 2f))
            }
            repeat(3) {
                scenario.onActivity { pinch(host, 700f, 80f) }
                SystemClock.sleep(400)
            }
            scenario.onActivity {
                assertFits(image, fullScale)
                report("Pinch returned to full image")
                loader.dispose()
            }
        }
        bitmap.recycle()
    }

    private fun report(message: String) {
        InstrumentationRegistry.getInstrumentation().sendStatus(2, Bundle().apply {
            putString("stream", message + "\n")
        })
    }

    private fun assertFits(image: NativeImageView, fullScale: Float) {
        assertEquals(fullScale, image.currentScale, fullScale * 0.001f)
        val rect = image.displayRect
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
