package com.example.c001apk.imageviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import kotlin.math.ceil
import kotlin.math.floor

internal class NativeImageTiles(
    private val decoder: BitmapRegionDecoder,
    private val rawToImage: Matrix,
) {
    private data class TileRequest(val region: Rect, val sample: Int)

    private val imageToRaw = Matrix().also(rawToImage::invert)
    private val rawWidth = decoder.width
    private val rawHeight = decoder.height
    private val handler = Handler(Looper.getMainLooper())
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val tileBounds = RectF()
    private val decoded = mutableMapOf<TileRequest, Bitmap>()
    private val attempted = mutableSetOf<TileRequest>()
    private var needed = emptySet<TileRequest>()
    private var decoding = false
    private var closed = false
    @Volatile private var viewportRevision = 0
    var onTilesChanged: (() -> Unit)? = null

    fun updateViewport(visible: RectF?, scale: Float) {
        if (closed) return
        val requests = if (visible == null || visible.isEmpty) emptySet() else requestsFor(visible, scale)
        if (needed == requests) return
        needed = requests
        viewportRevision++
        decoded.keys.retainAll(needed)
        attempted.retainAll(needed)
        decodeNeededTiles()
    }

    private fun requestsFor(visible: RectF, scale: Float): Set<TileRequest> {
        val visibleRaw = RectF(visible).also(imageToRaw::mapRect)
        val clipped = Rect(
            floor(visibleRaw.left).toInt().coerceIn(0, rawWidth),
            floor(visibleRaw.top).toInt().coerceIn(0, rawHeight),
            ceil(visibleRaw.right).toInt().coerceIn(0, rawWidth),
            ceil(visibleRaw.bottom).toInt().coerceIn(0, rawHeight),
        )
        if (clipped.isEmpty) return emptySet()
        var sample = 1
        while (sample < MAX_SAMPLE && sample * 2f * scale <= 1f) sample *= 2
        while (sample < MAX_SAMPLE && tileCount(clipped, sample) > MAX_TILES) sample *= 2
        if (tileCount(clipped, sample) > MAX_TILES) return emptySet()
        val edge = TILE_EDGE * sample
        return buildSet {
            var top = clipped.top / edge * edge
            while (top < clipped.bottom) {
                val bottom = top + minOf(edge, rawHeight - top)
                var left = clipped.left / edge * edge
                while (left < clipped.right) {
                    val right = left + minOf(edge, rawWidth - left)
                    add(TileRequest(Rect(left, top, right, bottom), sample))
                    left = right
                }
                top = bottom
            }
        }
    }

    private fun tileCount(visible: Rect, sample: Int): Long {
        val edge = TILE_EDGE * sample
        val columns = (visible.right - 1) / edge - visible.left / edge + 1
        val rows = (visible.bottom - 1) / edge - visible.top / edge + 1
        return columns.toLong() * rows
    }

    private fun decodeNeededTiles() {
        if (closed || decoding) return
        val requests = needed - attempted
        if (requests.isEmpty()) return
        val revision = viewportRevision
        decoding = true
        decoderExecutor.execute {
            val completed = mutableMapOf<TileRequest, Bitmap?>()
            for (request in requests) {
                if (revision != viewportRevision) break
                completed[request] = try {
                    decoder.decodeRegion(request.region, BitmapFactory.Options().apply {
                        inSampleSize = request.sample
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    })
                } catch (_: RuntimeException) {
                    null
                }
            }
            handler.post {
                decoding = false
                if (closed) {
                    decoder.recycle()
                    return@post
                }
                var changed = false
                completed.forEach { (request, bitmap) ->
                    if (request in needed) {
                        attempted.add(request)
                        if (bitmap != null) {
                            decoded[request] = bitmap
                            changed = true
                        }
                    }
                }
                if (changed) onTilesChanged?.invoke()
                decodeNeededTiles()
            }
        }
    }

    @Suppress("DEPRECATION")
    fun clipDecodedTiles(canvas: Canvas) {
        for (request in decoded.keys) {
            tileBounds.set(request.region)
            rawToImage.mapRect(tileBounds)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutRect(tileBounds)
            } else {
                canvas.clipRect(tileBounds, Region.Op.DIFFERENCE)
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (decoded.isEmpty()) return
        val saved = canvas.save()
        canvas.concat(rawToImage)
        decoded.forEach { (request, bitmap) ->
            canvas.drawBitmap(bitmap, null, request.region, paint)
        }
        canvas.restoreToCount(saved)
    }

    fun close() {
        if (closed) return
        closed = true
        viewportRevision++
        onTilesChanged = null
        decoded.clear()
        needed = emptySet()
        if (!decoding) decoder.recycle()
    }

    private companion object {
        const val TILE_EDGE = 512
        const val MAX_TILES = 24
        const val MAX_SAMPLE = 512
        val decoderExecutor = Executors.newFixedThreadPool(2)
    }
}
