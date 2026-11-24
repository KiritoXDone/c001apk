package com.example.c001apk.compose.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.imageviewer.ImageGestureLayout
import com.example.c001apk.imageviewer.OnLongTapCallback
import net.mikaelzero.mojito.view.sketch.SketchContentLoaderImpl
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import org.junit.Assert.*
import org.junit.Test

class ImagePinchGestureTest {
    @Test
    fun twoFingersZoomInAndBackToCompleteImageWithoutDismissing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val appId = instrumentation.targetContext.packageName
        instrumentation.uiAutomation.executeShellCommand(
            "am start -W -n $appId/androidx.activity.ComponentActivity",
        ).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        val bitmap = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var host: ImageGestureLayout
            lateinit var image: SketchImageView
            var fullScale = 0f
            var longPressed = false
            scenario.onActivity { activity ->
                val loader = SketchContentLoaderImpl()
                loader.init(activity, "", null)
                host = ImageGestureLayout(activity, { !loader.ownsVerticalDrag(true) }, { _, _ -> }, {})
                host.addView(loader.providerView())
                image = loader.providerRealView() as SketchImageView
                loader.isLongImage(1600, 900)
                image.setImageDrawable(BitmapDrawable(activity.resources, bitmap))
                loader.onLongTapCallback(object : OnLongTapCallback {
                    override fun onLongTap(view: View, x: Float, y: Float) { longPressed = true }
                })
                activity.setContentView(host)
                loader.loadAnimFinish()
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val zoomer = checkNotNull(image.zoomer)
                assertTrue(zoomer.reset("pinch-regression"))
                fullScale = zoomer.fullZoomScale
                pinch(host, 200f, 700f)
                assertTrue("Two fingers must enlarge the image", zoomer.zoomScale > fullScale * 1.5f)
                assertFalse(host.isDragging)
            }
            SystemClock.sleep(400)
            // Reset finger spacing between pinches, as users do after reaching Android's
            // minimum recognisable two-finger span.
            repeat(2) {
                scenario.onActivity { pinch(host, 700f, 80f) }
                // Allow the minimum-scale spring to settle after fingers are lifted.
                SystemClock.sleep(600)
            }
            scenario.onActivity {
                assertEquals(fullScale, checkNotNull(image.zoomer).zoomScale, 0.02f)
                assertFalse(host.isDragging)
                assertFalse(longPressed)
                image.zoomer?.recycle("test-finished")
                image.setImageDrawable(null)
            }
        }
        bitmap.recycle()
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
