package com.example.c001apk.compose.util

import android.graphics.BitmapFactory
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.Options
import coil.size.Dimension
import coil.size.Scale
import coil.size.Size
import kotlin.math.min
import kotlin.math.sqrt

// 显示保护只限制解码后的像素，不改下载文件及磁盘缓存里的原始内容。
const val ORIGINAL_IMAGE_EXPORT = "original_image_export"

class BoundedImageInterceptor : Interceptor {
    private val defaultFactory = BitmapFactoryDecoder.Factory()

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        if (chain.request.parameters.value<Boolean>(ORIGINAL_IMAGE_EXPORT) == true) {
            return chain.proceed(chain.request)
        }
        val request = chain.request.newBuilder()
            .decoderFactory(BoundedImageDecoder(chain.request.decoderFactory ?: defaultFactory, defaultFactory))
            .build()
        return chain.proceed(request)
    }
}

private class BoundedImageDecoder(
    private val delegate: Decoder.Factory,
    private val fallback: Decoder.Factory,
) : Decoder.Factory {
    override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        result.source.source().peek().inputStream().use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width <= 0 || height <= 0) return delegate.create(result, options, imageLoader)
        val factor = min(1.0, min(
            sqrt(4_194_304.0 / (width.toDouble() * height)),
            4096.0 / maxOf(width, height),
        ))
        if (factor == 1.0) return delegate.create(result, options, imageLoader)
        val maxWidth = (width * factor).toInt().coerceAtLeast(1)
        val maxHeight = (height * factor).toInt().coerceAtLeast(1)
        val size = Size(
            min((options.size.width as? Dimension.Pixels)?.px ?: maxWidth, maxWidth),
            min((options.size.height as? Dimension.Pixels)?.px ?: maxHeight, maxHeight),
        )
        // FIT 同时约束两条边，避免长图按短边裁切解码后仍生成巨型位图。
        val boundedOptions = options.copy(
            size = size,
            scale = Scale.FIT,
            allowInexactSize = false,
        )
        return delegate.create(result, boundedOptions, imageLoader)
            ?: fallback.create(result, boundedOptions, imageLoader)
    }
}
