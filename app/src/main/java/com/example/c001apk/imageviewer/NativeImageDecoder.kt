package com.example.c001apk.imageviewer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Movie
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class DecodedNativeImage(
    val drawable: Drawable,
    val width: Int,
    val height: Int,
    val tiles: NativeImageTiles? = null,
)

internal object NativeImageDecoder {
    private const val MAX_PREVIEW_SIDE = 4096
    private const val MAX_PREVIEW_PIXELS = 4_194_304

    fun decode(resources: Resources, file: File): DecodedNativeImage? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, options)
        val orientation by lazy(LazyThreadSafetyMode.NONE) { readOrientation(file) }
        val image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                decodeDrawable(file)
            } catch (_: IOException) {
                null
            }
        } else {
            null
        } ?: decodeLegacy(resources, file, options, orientation) ?: return null

        if (image.drawable !is BitmapDrawable ||
            image.drawable.bitmap.hasUltraHdrGainmap() ||
            (image.drawable.bitmap.width >= image.width && image.drawable.bitmap.height >= image.height) ||
            options.outWidth <= 0 || options.outHeight <= 0
        ) return image

        val transform = orientationMatrix(orientation, options.outWidth, options.outHeight)
        val originalBounds = RectF(0f, 0f, options.outWidth.toFloat(), options.outHeight.toFloat())
        transform.mapRect(originalBounds)
        if (originalBounds.width().toInt() != image.width ||
            originalBounds.height().toInt() != image.height
        ) return image
        val decoder = openRegionDecoder(file) ?: return image
        if (decoder.width != options.outWidth || decoder.height != options.outHeight) {
            decoder.recycle()
            return image
        }
        return DecodedNativeImage(image.drawable, image.width, image.height, NativeImageTiles(decoder, transform))
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeDrawable(file: File): DecodedNativeImage {
        var width = 0
        var height = 0
        val drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(file)) { decoder, info, _ ->
            width = info.size.width
            height = info.size.height
            val scale = previewScale(width, height)
            if (scale < 1.0) {
                decoder.setTargetSize(max(1, (width * scale).toInt()), max(1, (height * scale).toInt()))
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        return DecodedNativeImage(drawable, width, height)
    }

    @Suppress("DEPRECATION")
    private fun decodeLegacy(
        resources: Resources,
        file: File,
        bounds: BitmapFactory.Options,
        orientation: Int,
    ): DecodedNativeImage? {
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width <= 0 || height <= 0) return null
        val scale = previewScale(width, height)
        if (bounds.outMimeType == "image/gif" && scale == 1.0) {
            Movie.decodeFile(file.path)?.takeIf { it.width() > 0 && it.height() > 0 }?.let { movie ->
                return DecodedNativeImage(LegacyGifDrawable(movie), movie.width(), movie.height())
            }
        }
        var sample = 1
        while (ceil(width.toDouble() / sample) > MAX_PREVIEW_SIDE ||
            ceil(height.toDouble() / sample) > MAX_PREVIEW_SIDE ||
            ceil(width.toDouble() / sample) * ceil(height.toDouble() / sample) > MAX_PREVIEW_PIXELS
        ) sample *= 2
        val bitmap = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }) ?: return null
        val rotate = orientationMatrix(orientation, bitmap.width, bitmap.height)
        val oriented = if (rotate.isIdentity) bitmap else {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotate, true)
                .also { if (it !== bitmap) bitmap.recycle() }
        }
        val swapsAxes = orientation in ExifInterface.ORIENTATION_TRANSPOSE..ExifInterface.ORIENTATION_ROTATE_270
        return DecodedNativeImage(
            BitmapDrawable(resources, oriented),
            if (swapsAxes) height else width,
            if (swapsAxes) width else height,
        )
    }

    private fun previewScale(width: Int, height: Int): Double =
        minOf(
            1.0,
            MAX_PREVIEW_SIDE.toDouble() / max(width, height),
            sqrt(MAX_PREVIEW_PIXELS.toDouble() / (width.toLong() * height)),
        )

    private fun readOrientation(file: File): Int = try {
        ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (_: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }

    @Suppress("DEPRECATION")
    private fun openRegionDecoder(file: File): BitmapRegionDecoder? = try {
        BitmapRegionDecoder.newInstance(file.path, false)
    } catch (_: IOException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun Bitmap.hasUltraHdrGainmap(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hasGainmap()

    private fun orientationMatrix(orientation: Int, width: Int, height: Int): Matrix {
        val w = width.toFloat()
        val h = height.toFloat()
        val values = when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> floatArrayOf(-1f, 0f, w, 0f, 1f, 0f)
            ExifInterface.ORIENTATION_ROTATE_180 -> floatArrayOf(-1f, 0f, w, 0f, -1f, h)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> floatArrayOf(1f, 0f, 0f, 0f, -1f, h)
            ExifInterface.ORIENTATION_TRANSPOSE -> floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f)
            ExifInterface.ORIENTATION_ROTATE_90 -> floatArrayOf(0f, -1f, h, 1f, 0f, 0f)
            ExifInterface.ORIENTATION_TRANSVERSE -> floatArrayOf(0f, -1f, h, -1f, 0f, w)
            ExifInterface.ORIENTATION_ROTATE_270 -> floatArrayOf(0f, 1f, 0f, -1f, 0f, w)
            else -> return Matrix()
        }
        return Matrix().apply { setValues(values + floatArrayOf(0f, 0f, 1f)) }
    }
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
private class LegacyGifDrawable(private val movie: Movie) : Drawable(), Animatable {
    private var startedAtMs = 0L
    private var running = false
    private val nextFrame = Runnable { invalidateSelf() }

    override fun draw(canvas: Canvas) {
        val duration = movie.duration().takeIf { it > 0 } ?: 1000
        if (running) movie.setTime(((SystemClock.uptimeMillis() - startedAtMs) % duration).toInt())
        val saved = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width().toFloat() / movie.width(), bounds.height().toFloat() / movie.height())
        movie.draw(canvas, 0f, 0f)
        canvas.restoreToCount(saved)
        if (running) scheduleSelf(nextFrame, SystemClock.uptimeMillis() + FRAME_INTERVAL_MS)
    }

    override fun start() {
        if (running) return
        startedAtMs = SystemClock.uptimeMillis()
        running = true
        invalidateSelf()
    }

    override fun stop() {
        running = false
        unscheduleSelf(nextFrame)
    }

    override fun isRunning() = running

    override fun getIntrinsicWidth() = movie.width()

    override fun getIntrinsicHeight() = movie.height()

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    private companion object {
        const val FRAME_INTERVAL_MS = 16L
    }
}
