package com.example.c001apk.compose.util

private val imageDimensions = Regex("(?:@|%40)([1-9][0-9]*)x([1-9][0-9]*)\\.[a-zA-Z0-9]+$")

/** Coolapk's .s.jpg thumbnails can crop long images instead of scaling the whole image. */
internal fun uncroppedPreviewUrl(originalUrl: String, thumbnailUrl: String): String {
    val path = originalUrl.substringBefore('?').substringBefore('#')
    val dimensions = imageDimensions.find(path) ?: return thumbnailUrl
    val width = dimensions.groupValues[1].toIntOrNull() ?: return thumbnailUrl
    val height = dimensions.groupValues[2].toIntOrNull() ?: return thumbnailUrl
    val isLongImage =
        height.toLong() * 9 > width.toLong() * 22 || width.toLong() > height.toLong() * 8
    return if (isLongImage) originalUrl else thumbnailUrl
}
