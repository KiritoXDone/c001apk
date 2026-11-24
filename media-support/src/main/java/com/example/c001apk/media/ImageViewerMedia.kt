package com.example.c001apk.media

import android.net.Uri
import android.util.Log
import android.view.View
import com.example.c001apk.imageviewer.ContentLoader
import com.example.c001apk.imageviewer.ImageViewLoadFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

enum class RichMediaHint {
    NONE,
    ULTRA_HDR,
    LIVE_PHOTO,
}

fun String.coolApkRichMediaHint(): RichMediaHint = when {
    contains("-livepic", ignoreCase = true) -> RichMediaHint.LIVE_PHOTO
    contains("-uhdr", ignoreCase = true) ||
        contains("-xhdr", ignoreCase = true) -> RichMediaHint.ULTRA_HDR
    else -> RichMediaHint.NONE
}

fun String.mayContainCoolApkRichMedia(): Boolean =
    coolApkRichMediaHint() != RichMediaHint.NONE

class MediaImageFactory(
    private val delegate: ImageViewLoadFactory,
    private val imageUrl: String,
    private val videoUrlResolver: LivePhotoVideoUrlResolver? = null,
    private val mediaHint: RichMediaHint = imageUrl.coolApkRichMediaHint(),
    private val playbackSession: MediaPlaybackSession = MediaPlaybackSession(),
    private val pagePosition: Int = 0,
) : ImageViewLoadFactory {

    private var contentLoader: MediaContentLoader? = null
    private var lastBoundFileKey: String? = null
    private var parseGeneration = 0
    private val parsedMedia = ConcurrentHashMap<String, EmbeddedMediaInfo>()

    override fun showImage(view: View, uri: Uri, isPreview: Boolean) {
        val file = uri.path?.let(::File)
        val fileKey = file?.let { "${it.absolutePath}:${it.length()}" }
        if (isPreview) {
            delegate.showImage(view, uri, isPreview)
            file?.let { contentLoader?.bindExitPreview(it) }
            return
        }

        delegate.showImage(view, uri, isPreview)
        if (file == null || fileKey == null) return

        val generation = ++parseGeneration
        val targetLoader = contentLoader
        parsedMedia[fileKey]?.let { mediaInfo ->
            bindParsedMedia(file, fileKey, mediaInfo, wasCached = true)
            return
        }
        MEDIA_PARSE_EXECUTOR.execute {
            val mediaInfo = runCatching {
                MotionPhotoParser.parse(
                    file = file,
                    expectMotionPhoto = mediaHint == RichMediaHint.LIVE_PHOTO,
                )
            }.onFailure {
                Log.w(TAG, "Unable to parse rich media file ${file.name}", it)
            }.getOrNull() ?: return@execute
            parsedMedia[fileKey] = mediaInfo
            view.post {
                if (generation == parseGeneration && contentLoader === targetLoader) {
                    bindParsedMedia(file, fileKey, mediaInfo, wasCached = false)
                }
            }
        }
    }

    override fun newContentLoader(): ContentLoader {
        contentLoader?.dispose()
        parseGeneration++
        lastBoundFileKey = null
        return MediaContentLoader(
            delegate.newContentLoader(),
            playbackSession,
            imageUrl,
            videoUrlResolver,
            mediaHint,
            pagePosition,
        ).also {
            contentLoader = it
        }
    }

    val supportsLivePhotoExport: Boolean
        get() = mediaHint == RichMediaHint.LIVE_PHOTO || contentLoader?.hasLivePhoto == true

    fun exportLivePhoto(
        destination: File,
        onComplete: (Result<File>) -> Unit,
    ): Boolean = contentLoader?.exportLivePhoto(destination, onComplete) == true

    private fun bindParsedMedia(
        file: File,
        fileKey: String,
        mediaInfo: EmbeddedMediaInfo,
        wasCached: Boolean,
    ) {
        if (BuildConfig.DEBUG && !wasCached) {
            Log.d(
                TAG,
                "parsed file=${file.name} bytes=${file.length()} hdr=${mediaInfo.hasUltraHdr} " +
                    "motion=${mediaInfo.hasMotionPhoto} declared=${mediaInfo.declaredVideoLength} " +
                    "embedded=${mediaInfo.embeddedVideoLength}",
            )
        }
        if ((mediaInfo.hasMotionPhoto || mediaInfo.hasUltraHdr) && fileKey != lastBoundFileKey) {
            lastBoundFileKey = fileKey
            contentLoader?.bind(file, mediaInfo)
        }
    }

    private companion object {
        const val TAG = "ImageViewerMedia"
        val MEDIA_PARSE_EXECUTOR = Executors.newSingleThreadExecutor()
    }
}
