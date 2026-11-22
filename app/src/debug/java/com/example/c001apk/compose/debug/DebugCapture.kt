package com.example.c001apk.compose.debug

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONObject

/** Captures the app window into the debug screenshot directory and encodes it on the app side. */
internal fun captureScreenshot(
    app: Application,
    activity: Activity,
    request: JSONObject,
): JSONObject {
    val highQuality = request.optBoolean("highQuality", false)
    val quality = request.optInt("quality", 40).coerceIn(1, 100)
    val name =
        request.optString("name").takeIf { it.isNotEmpty() }
            ?: defaultScreenshotName(highQuality)
    val directory =
        File(app.getExternalFilesDir(null), "debug-screenshots").apply { mkdirs() }
    val file = File(directory, name)
    val decor = activity.window.decorView
    val width = decor.width
    val height = decor.height
    require(width > 0 && height > 0) { "Window has no drawable size yet" }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            copyWindow(activity, bitmap)
        } else {
            onMain { decor.draw(Canvas(bitmap)) }
        }
        file.outputStream().use { stream ->
            when {
                highQuality -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
                else ->
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream)
            }
        }
    } finally {
        bitmap.recycle()
    }
    return JSONObject()
        .put("ok", true)
        .put("path", file.absolutePath)
        .put("file", file.name)
        .put("width", width)
        .put("height", height)
        .put("format", if (highQuality) "png" else "webp")
        .put("quality", if (highQuality) JSONObject.NULL else quality)
        .put("lossless", highQuality)
}

private fun copyWindow(activity: Activity, bitmap: Bitmap) {
    val latch = CountDownLatch(1)
    var copyResult = PixelCopy.SUCCESS
    onMain {
        PixelCopy.request(
            activity.window,
            bitmap,
            { result ->
                copyResult = result
                latch.countDown()
            },
            Handler(Looper.getMainLooper()),
        )
    }
    check(latch.await(5, TimeUnit.SECONDS)) { "Window capture timed out" }
    check(copyResult == PixelCopy.SUCCESS) { "Window capture failed with code $copyResult" }
}

private fun defaultScreenshotName(highQuality: Boolean): String {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(Date())
    return "$stamp-window-agent." + if (highQuality) "png" else "webp"
}
