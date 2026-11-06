package com.example.c001apk.compose.ui

import coil.imageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.request.CachePolicy
import coil.size.Size
import java.io.File
import kotlinx.coroutines.runBlocking
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RenderNode
import android.graphics.RectF
import android.os.Bundle
import androidx.test.filters.SdkSuppress
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import net.mikaelzero.mojito.view.sketch.SketchContentLoaderImpl
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import net.mikaelzero.mojito.loader.OnLongTapCallback
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class ImagePreviewScaleTest {
    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun applicationImageLoaderBoundsOversizedSourcesBeforeDrawing() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for ((width, height) in listOf(8000 to 8000, 256 to 40000, 320 to 240)) {
            val file = File(context.cacheDir, "oversized-source-$width-$height.png")
            val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            try {
                file.outputStream().use { source.compress(Bitmap.CompressFormat.PNG, 100, it) }
            } finally {
                source.recycle()
            }
            try {
                for (customDecoder in listOf(false, true)) {
                    val request = ImageRequest.Builder(context).data(file).size(Size.ORIGINAL)
                        .allowHardware(false).memoryCachePolicy(CachePolicy.DISABLED)
                        .apply { if (customDecoder) decoderFactory(ImageDecoderDecoder.Factory()) }
                        .build()
                    val result = context.imageLoader.execute(request)
                    assertTrue("Decode failed: $result", result is SuccessResult)
                    val decoded = ((result as SuccessResult).drawable as BitmapDrawable).bitmap
                    try {
                        assertTrue(decoded.width.toLong() * decoded.height <= 4_194_304L)
                        assertTrue(maxOf(decoded.width, decoded.height) <= 4096)
                        if (width == 320) {
                            assertEquals(width, decoded.width)
                            assertEquals(height, decoded.height)
                        }
                        val node = RenderNode("bounded-image-draw")
                        try {
                            val canvas = node.beginRecording(256, 256)
                            try {
                                canvas.drawBitmap(decoded, null, Rect(0, 0, 256, 256), null)
                            } finally {
                                node.endRecording()
                            }
                        } finally {
                            node.discardDisplayList()
                        }
                        InstrumentationRegistry.getInstrumentation().sendStatus(2, Bundle().apply {
                            putString("stream", "\n$width x $height -> ${decoded.width} x ${decoded.height}; customDecoder=$customDecoder; draw succeeded\n")
                        })
                    } finally {
                        decoded.recycle()
                    }
                }
            } finally {
                file.delete()
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun oversizedBitmapReproducesCanvasLimitEvenInSmallBounds() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = Bitmap.createBitmap(8000, 8000, Bitmap.Config.ARGB_8888)
        val node = RenderNode("oversized-bitmap-reproduction")
        try {
            assertEquals(256_000_000, bitmap.byteCount)
            node.setPosition(0, 0, 256, 256)
            val canvas = node.beginRecording(256, 256)
            val error = try {
                assertTrue(canvas.isHardwareAccelerated)
                runCatching {
                    canvas.drawBitmap(bitmap, null, Rect(0, 0, 256, 256), null)
                }.exceptionOrNull()
            } finally {
                node.endRecording()
            }
            assertTrue("Expected the platform bitmap limit, got $error", error is RuntimeException)
            assertTrue(error!!.message.orEmpty().contains("trying to draw too large(256000000bytes) bitmap"))
            assertTrue(error.stackTrace.any {
                it.className == "android.graphics.RecordingCanvas" && it.methodName == "throwIfCannotDraw"
            })
            instrumentation.sendStatus(2, Bundle().apply {
                putString("stream", "\nReproduced: ${error.message}; target bounds: 256x256\n")
            })
            // 同一张图缩小实际像素后再绘制，区分解码尺寸与显示尺寸。
            val sampled = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
            try {
                val sampledCanvas = node.beginRecording(256, 256)
                try {
                    sampledCanvas.drawBitmap(sampled, null, Rect(0, 0, 256, 256), null)
                } finally {
                    node.endRecording()
                }
                assertEquals(4_000_000, sampled.byteCount)
                instrumentation.sendStatus(2, Bundle().apply {
                    putString("stream", "Sampled control: 1000x1000, 4000000 bytes, draw succeeded\n")
                })
            } finally {
                node.discardDisplayList()
                sampled.recycle()
            }
        } finally {
            node.discardDisplayList()
            bitmap.recycle()
        }
    }

    @Test
    fun longImageReadingScaleStillAllowsLongPress() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val called = CountDownLatch(1)
        val bitmap = Bitmap.createBitmap(900, 6000, Bitmap.Config.ARGB_8888)
        lateinit var view: SketchImageView
        val downTime = SystemClock.uptimeMillis()
        instrumentation.runOnMainSync {
            val loader = SketchContentLoaderImpl()
            loader.init(instrumentation.targetContext, "", null, null)
            view = loader.providerRealView() as SketchImageView
            loader.providerView().apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.EXACTLY),
                )
                layout(0, 0, 1080, 2400)
            }
            assertTrue(loader.isLongImage(900, 6000))
            view.setImageDrawable(BitmapDrawable(view.resources, bitmap))
            val zoomer = checkNotNull(view.zoomer)
            assertTrue(zoomer.reset("long-preview-regression"))
            assertEquals(zoomer.fillZoomScale, zoomer.zoomScale, 0.001f)
            assertEquals(zoomer.fullZoomScale, zoomer.minZoomScale, 0.001f)
            loader.onLongTapCallback(object : OnLongTapCallback {
                override fun onLongTap(view: View, x: Float, y: Float) { called.countDown() }
            })
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 540f, 1200f, 0).also {
                view.dispatchTouchEvent(it)
                it.recycle()
            }
        }
        val received = called.await(3, TimeUnit.SECONDS)
        instrumentation.runOnMainSync {
            MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 540f, 1200f, 0).also {
                view.dispatchTouchEvent(it)
                it.recycle()
            }
            view.zoomer?.recycle("test-finished")
            view.setImageDrawable(null)
            bitmap.recycle()
        }
        assertTrue("Long-image reading mode must keep its long-press menu", received)
    }

    @Test
    fun landscapeAndPortraitFitAfterLateImageLoadAndCanZoomBackOut() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for ((width, height) in listOf(1600 to 900, 900 to 1600)) {
                val loader = SketchContentLoaderImpl()
                loader.init(instrumentation.targetContext, "", null, null)
                val view = loader.providerRealView() as SketchImageView
                val host = loader.providerView()
                host.measure(
                    View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.EXACTLY),
                )
                host.layout(0, 0, 1080, 2400)
                // Original loading commonly completes after the opening animation.
                loader.loadAnimFinish()
                assertFalse(loader.isLongImage(width, height))
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                view.setImageDrawable(BitmapDrawable(view.resources, bitmap))
                val zoomer = checkNotNull(view.zoomer)
                assertTrue(zoomer.reset("preview-regression"))
                assertEquals(zoomer.fullZoomScale, zoomer.zoomScale, 0.001f)
                assertEquals(zoomer.fullZoomScale, zoomer.minZoomScale, 0.001f)
                val rect = RectF()
                zoomer.getDrawRect(rect)
                assertTrue(rect.width() <= 1080.1f && rect.height() <= 2400.1f)
                assertTrue(zoomer.zoom(zoomer.maxZoomScale, false))
                assertTrue(loader.dispatchTouchEvent(false, false, false, false))
                assertTrue(zoomer.zoom(zoomer.fullZoomScale, false))
                zoomer.getDrawRect(rect)
                assertTrue(rect.width() <= 1080.1f && rect.height() <= 2400.1f)
                zoomer.recycle("test-finished")
                view.setImageDrawable(null)
                bitmap.recycle()
            }
        }
    }
}
