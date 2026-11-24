package com.example.c001apk.compose.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.R
import com.example.c001apk.imageviewer.ImageViewer
import com.example.c001apk.imageviewer.ImageViewerActivity
import com.example.c001apk.imageviewer.NativeImageLoadFactory
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class ImagePreviewWindowTest {
    @Test fun backReturnsWithoutWindowDim() = verifyReturn("back")
    @Test fun tapReturnsWithoutWindowDim() = verifyReturn("tap")
    @Test fun dragReturnsWithoutWindowDim() = verifyReturn("drag")

    private fun verifyReturn(close: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // Keep HyperOS from freezing the process before ActivityScenario resumes.
        instrumentation.uiAutomation.executeShellCommand(
            "am start -W -n ${context.packageName}/androidx.activity.ComponentActivity",
        ).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        val bitmap = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
        }
        val file = File.createTempFile("preview-window-", ".png", context.cacheDir)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        var preview: ImageViewerActivity? = null
        try {
            ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
                val laidOut = CountDownLatch(1)
                val shown = CountDownLatch(1)
                val closed = CountDownLatch(1)
                val resumed = CountDownLatch(1)
                lateinit var thumbnail: ImageView
                scenario.onActivity { activity ->
                    val root = FrameLayout(activity).apply {
                        setBackgroundColor(Color.WHITE)
                        keepScreenOn = true
                    }
                    thumbnail = ImageView(activity).apply { setImageBitmap(bitmap) }
                    root.addView(thumbnail, FrameLayout.LayoutParams(360, 200).apply {
                        leftMargin = 100
                        topMargin = 500
                    })
                    activity.setContentView(root)
                    root.postOnAnimation { root.postOnAnimation { laidOut.countDown() } }
                }
                assertTrue("Host must be laid out", laidOut.await(3, TimeUnit.SECONDS))
                // ActivityScenario starts a new task; allow the system task transition to settle.
                SystemClock.sleep(700)
                assertHostIsWhite()
                scenario.onActivity { activity ->
                    ImageViewer.open(
                        activity,
                        ImageViewer.Request(
                            images = listOf(ImageViewer.Image(Uri.fromFile(file).toString())),
                            rendererAt = { NativeImageLoadFactory() },
                            retryLabel = R.string.loading_error_retry,
                            thumbnailAt = { thumbnail },
                            onOpened = { shown.countDown() },
                            onClosed = { closed.countDown() },
                        ),
                    )
                    activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener {
                        if (it) resumed.countDown()
                    }
                }
                assertTrue("Preview must open", shown.await(5, TimeUnit.SECONDS))
                SystemClock.sleep(500)
                instrumentation.runOnMainSync {
                    preview = checkNotNull(ImageViewer.currentActivity?.get())
                    assertEquals("Only the viewer's animated backdrop may darken the host", 0,
                        preview!!.window.attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    // Isolate the actual window compositor from the viewer's animated black backdrop.
                    // A system dim layer would keep the white host gray even at zero view alpha.
                    preview!!.findViewById<View>(android.R.id.content).alpha = 0f
                }
                awaitDraw(preview!!.window.decorView)
                SystemClock.sleep(200)
                assertHostIsWhite()
                instrumentation.runOnMainSync { preview!!.findViewById<View>(android.R.id.content).alpha = 1f }
                awaitDraw(preview!!.window.decorView)
                when (close) {
                    "back" -> instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    "tap" -> gesture(preview!!.window.decorView, false)
                    "drag" -> gesture(preview!!.window.decorView, true)
                }
                assertTrue("Preview must close via $close", closed.await(5, TimeUnit.SECONDS))
                assertTrue("Host must regain focus", resumed.await(5, TimeUnit.SECONDS))
                scenario.onActivity { assertFalse(it.isFinishing) }
                assertHostIsWhite()
            }
        } finally {
            instrumentation.runOnMainSync { preview?.takeUnless { it.isDestroyed }?.finish() }
            file.delete()
            bitmap.recycle()
        }
    }

    private fun awaitDraw(view: View) {
        val drawn = CountDownLatch(1)
        view.postOnAnimation { view.postOnAnimation { drawn.countDown() } }
        assertTrue("Window must draw", drawn.await(3, TimeUnit.SECONDS))
    }

    private fun assertHostIsWhite() {
        val screenshot = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        try {
            val color = screenshot.getPixel(screenshot.width * 9 / 10, screenshot.height * 3 / 4)
            assertTrue("Host was dimmed: ${Integer.toHexString(color)}",
                Color.red(color) >= 245 && Color.green(color) >= 245 && Color.blue(color) >= 245)
        } finally {
            screenshot.recycle()
        }
    }

    private fun gesture(view: View, drag: Boolean) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val start = SystemClock.uptimeMillis()
        val x = view.width / 2f
        val from = view.height * 0.4f
        fun send(action: Int, y: Float) {
            MotionEvent.obtain(start, SystemClock.uptimeMillis(), action, x, y, 0).also {
                instrumentation.sendPointerSync(it)
                it.recycle()
            }
        }
        send(MotionEvent.ACTION_DOWN, from)
        if (drag) for (step in 1..20) {
            SystemClock.sleep(16)
            send(MotionEvent.ACTION_MOVE, from + view.height * 0.4f * step / 20)
        } else SystemClock.sleep(40)
        send(MotionEvent.ACTION_UP, if (drag) view.height * 0.8f else from)
    }
}
