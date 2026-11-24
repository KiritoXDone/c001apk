package com.example.c001apk.compose.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.constant.Constants.SUFFIX_THUMBNAIL
import com.example.c001apk.compose.ui.component.MiuixViewDialogBuilder
import com.example.c001apk.compose.view.NineGridImageView
import com.example.c001apk.imageviewer.ImageViewer
import com.example.c001apk.imageviewer.R as ImageViewerR
import com.example.c001apk.media.LivePhotoVideoUrlResolver
import com.example.c001apk.media.MediaImageFactory
import com.example.c001apk.media.MediaPlaybackSession
import com.example.c001apk.media.RichMediaHint
import com.example.c001apk.media.coolApkRichMediaHint
import com.example.c001apk.media.mayContainCoolApkRichMedia
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory

object ImageShowUtil {

    @Volatile
    private var livePhotoVideoUrlResolver: LivePhotoVideoUrlResolver? =
        CoolApkLivePhotoVideoUrlResolver

    fun setLivePhotoVideoUrlResolver(resolver: LivePhotoVideoUrlResolver?) {
        livePhotoVideoUrlResolver = resolver
    }

    fun startBigImgView(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int,
        cookie: String? = null,
        userAgent: String? = null,
    ) {
        val originList = urlList.map { it.viewerOriginalUrl() }
        val autoLoadOriginal = shouldAutoLoadTarget()
        val thumbnailList = urlList.mapIndexed { index, url ->
            val preview =
                if (autoLoadOriginal || !originList[index].mayContainCoolApkRichMedia())
                    url.viewerUrl()
                else originList[index]
            uncroppedPreviewUrl(originList[index], preview)
        }
        openViewer(
            context = imageView.context,
            previewUrls = thumbnailList,
            originalUrls = originList,
            startingPage = position,
            autoLoadOriginal = autoLoadOriginal,
            thumbnailAt = nineGridView::getImageViewAt,
            userAgent = userAgent,
        )
    }

    fun startBigImgViewSimple(
        context: Context,
        urlList: List<String>,
        cookie: String? = null,
        userAgent: String? = null,
    ) {
        val originList = urlList.map { it.viewerOriginalUrl() }
        val autoLoadOriginal = shouldAutoLoadTarget()
        val thumbnailList = urlList.mapIndexed { index, url ->
            val preview =
                when {
                    !url.isNetworkImage() -> url
                    !autoLoadOriginal && originList[index].mayContainCoolApkRichMedia() ->
                        originList[index]
                    url.contains(SUFFIX_THUMBNAIL) -> url.viewerUrl()
                    else -> "${originList[index]}$SUFFIX_THUMBNAIL"
                }
            uncroppedPreviewUrl(originList[index], preview)
        }
        openViewer(
            context = context,
            previewUrls = thumbnailList,
            originalUrls = originList,
            autoLoadOriginal = autoLoadOriginal,
            userAgent = userAgent,
        )
    }

    fun startBigImgViewSimple(
        imageView: ImageView,
        url: String,
        cookie: String? = null,
        userAgent: String? = null,
    ) {
        val originalUrl = url.viewerOriginalUrl()
        val previewUrl = if (originalUrl.mayContainCoolApkRichMedia()) {
            originalUrl
        } else {
            uncroppedPreviewUrl(originalUrl, url)
        }
        openViewer(
            context = imageView.context,
            previewUrls = listOf(previewUrl),
            originalUrls = null,
            thumbnailAt = { imageView },
            userAgent = userAgent,
        )
    }

    private fun String.isNetworkImage(): Boolean =
        startsWith("http://") || startsWith("https://")

    private fun String.viewerUrl(): String = if (startsWith("http://")) http2https else this

    private fun String.viewerOriginalUrl(): String =
        if (isNetworkImage()) replace(SUFFIX_THUMBNAIL, EMPTY_STRING).viewerUrl() else this

    private fun openViewer(
        context: Context,
        previewUrls: List<String>,
        originalUrls: List<String>?,
        startingPage: Int = 0,
        autoLoadOriginal: Boolean = true,
        thumbnailAt: (Int) -> ImageView? = { null },
        userAgent: String?,
    ) {
        val mediaUrls = originalUrls ?: previewUrls
        val mediaHints = mediaUrls.map(String::coolApkRichMediaHint)
        val playback = MediaPlaybackSession()
        val defaultFactory = SketchImageLoadFactory()
        val mediaFactories = mutableMapOf<Int, MediaImageFactory>()
        ImageViewer.open(
            context,
            ImageViewer.Request(
                images = previewUrls.mapIndexed { position, url ->
                    val originalUrl = mediaUrls[position]
                    if (mediaHints[position] != RichMediaHint.NONE) {
                        ImageViewer.Image(previewUrl = originalUrl)
                    } else {
                        ImageViewer.Image(
                            previewUrl = url,
                            originalUrl = originalUrls?.get(position)?.takeUnless { it.isGifUrl },
                        )
                    }
                },
                rendererAt = { position ->
                    val hint = mediaHints[position]
                    if (hint == RichMediaHint.NONE) {
                        defaultFactory
                    } else {
                        mediaFactories.getOrPut(position) {
                            MediaImageFactory(
                                delegate = defaultFactory,
                                imageUrl = mediaUrls[position],
                                videoUrlResolver = livePhotoVideoUrlResolver,
                                mediaHint = hint,
                                playbackSession = playback,
                                pagePosition = position,
                            )
                        }
                    }
                },
                retryLabel = R.string.loading_error_retry,
                startingPage = startingPage,
                autoLoadOriginal = autoLoadOriginal,
                enableHdr = mediaHints.any { it != RichMediaHint.NONE },
                thumbnailAt = thumbnailAt,
                onPageSelected = playback::selectPage,
                onLongClick = { viewer, position, loadOriginal ->
                    showSaveImgDialog(
                        viewer,
                        mediaUrls[position],
                        originalUrls,
                        userAgent,
                        mediaFactories[position],
                        loadOriginal,
                    )
                },
            ),
        )
    }

    private fun showSaveImgDialog(
        context: Context,
        url: String,
        urlList: List<String>?,
        userAgent: String?,
        mediaFactory: MediaImageFactory?,
        loadOriginal: (() -> Unit)?,
    ) {
        val actions = saveDialogActions(mediaFactory, canLoadOriginal = loadOriginal != null)
        MiuixViewDialogBuilder(context).apply {
            setItems(actions.map { context.getString(it.titleRes) }.toTypedArray()) { _, position: Int ->
                handleSaveDialogAction(
                    context,
                    url,
                    urlList,
                    userAgent,
                    mediaFactory,
                    actions[position],
                    loadOriginal,
                )
            }
            show()
        }
    }

    private fun handleSaveDialogAction(
        context: Context,
        url: String,
        urlList: List<String>?,
        userAgent: String?,
        mediaFactory: MediaImageFactory?,
        action: SaveImageAction,
        loadOriginal: (() -> Unit)?,
    ) {
        if (
            action in
                listOf(
                    SaveImageAction.SAVE_IMAGE,
                    SaveImageAction.SAVE_ALL,
                    SaveImageAction.SAVE_LIVE_PHOTO,
                ) && !GalleryPermission.granted(context)
        ) {
            GalleryPermission.request(context) {
                handleSaveDialogAction(context, url, urlList, userAgent, mediaFactory, action, loadOriginal)
            }
            return
        }
        when (action) {
            SaveImageAction.SAVE_IMAGE ->
                CoroutineScope(Dispatchers.IO).launch {
                    checkImageExist(context, url, true, userAgent)
                }

            SaveImageAction.SAVE_LIVE_PHOTO -> saveLivePhoto(context, url, mediaFactory)

            SaveImageAction.SAVE_ALL ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (urlList.isNullOrEmpty()) {
                        checkImageExist(context, url, true, userAgent)
                    } else {
                        urlList.forEachIndexed { index, url ->
                            checkImageExist(context, url, index == urlList.lastIndex, userAgent)
                        }
                    }
                }

            SaveImageAction.SHARE ->
                CoroutineScope(Dispatchers.IO).launch {
                    val index = url.lastIndexOf('/')
                    val filename = url.substring(index + 1)
                    if (checkShareImageExist(context, filename)) {
                        shareImage(
                            context,
                            File(context.externalCacheDir, "imageShare/$filename"),
                            null,
                        )
                    } else {
                        ImageDownloadUtil.downloadImage(
                            context,
                            url,
                            filename,
                            isEnd = true,
                            isShare = true,
                            userAgent = userAgent,
                        )
                    }
                }

            SaveImageAction.COPY_URL -> context.copyText(url)
            SaveImageAction.LOAD_ORIGINAL -> loadOriginal?.invoke()
        }
    }

    private fun saveDialogActions(
        mediaFactory: MediaImageFactory?,
        canLoadOriginal: Boolean,
    ): List<SaveImageAction> =
        buildList {
            if (canLoadOriginal) add(SaveImageAction.LOAD_ORIGINAL)
            add(SaveImageAction.SAVE_IMAGE)
            if (mediaFactory?.supportsLivePhotoExport == true) {
                add(SaveImageAction.SAVE_LIVE_PHOTO)
            }
            add(SaveImageAction.SAVE_ALL)
            add(SaveImageAction.SHARE)
            add(SaveImageAction.COPY_URL)
        }

    private fun saveLivePhoto(
        context: Context,
        url: String,
        mediaFactory: MediaImageFactory?,
    ) {
        if (mediaFactory == null) {
            context.makeToast(context.getString(R.string.image_live_not_ready))
            return
        }
        val exportDirectory = File(context.cacheDir, "live-photo-export").apply { mkdirs() }
        val fileName = "MVIMG_${System.currentTimeMillis()}.jpg"
        val exportFile = File(exportDirectory, fileName)
        val started =
            mediaFactory.exportLivePhoto(exportFile) { result ->
                result.fold(
                    onSuccess = { source ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val saved = ImageDownloadUtil.saveMotionPhoto(context, source, fileName)
                            source.delete()
                            withContext(Dispatchers.Main) {
                                context.makeToast(
                                    context.getString(if (saved) R.string.image_live_saved else R.string.image_live_save_failed),
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.w("MotionPhotoExport", "Unable to export $url", error)
                        exportFile.delete()
                        context.makeToast(context.getString(R.string.image_live_save_failed))
                    },
                )
            }
        if (!started) {
            context.makeToast(context.getString(R.string.image_live_not_ready))
        }
    }

    private fun checkShareImageExist(context: Context, filename: String): Boolean {
        val imageCheckDir = File(context.externalCacheDir, "imageShare/$filename")
        return imageCheckDir.exists()
    }

    private suspend fun checkImageExist(
        context: Context,
        url: String,
        isEnd: Boolean,
        userAgent: String?,
    ) {
        val filename = url.substring(url.lastIndexOf('/') + 1)
        val path = "${context.getString(R.string.app_name)}/$filename"
        val imageFile =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                path,
            )
        if (imageFile.exists()) {
            if (isEnd)
                withContext(Dispatchers.Main) {
                    context.makeToast(context.getString(R.string.image_file_exists))
                }
        } else {
            ImageDownloadUtil.downloadImage(context, url, filename, isEnd, userAgent = userAgent)
        }
    }

    private fun getFileProvider(context: Context, file: File): Uri {
        val authority = context.packageName + ".fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun shareImage(context: Context, file: File, title: String?) {
        try {
            val contentUri = getFileProvider(context, file)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            ContextCompat.startActivity(context, Intent.createChooser(intent, title), null)
        } catch (e: ActivityNotFoundException) {
            context.makeToast("failed to share image")
            e.printStackTrace()
        }
    }

    fun getImageLp(url: String): Pair<Int, Int> {
        var imgWidth = 1
        var imgHeight = 1
        val at = url.lastIndexOf("@")
        val x = url.lastIndexOf("x")
        val dot = url.lastIndexOf(".")
        if (at != -1 && x != -1 && dot != -1) {
            imgWidth = url.substring(at + 1, x).toInt()
            imgHeight = url.substring(x + 1, dot).toInt()
        }
        return Pair(imgWidth, imgHeight)
    }

    private fun shouldAutoLoadTarget(): Boolean =
        when (CookieUtil.imageQuality) {
            0 -> NetWorkUtil.isWifiConnected()
            1 -> true
            else -> false
        }

    private enum class SaveImageAction {
        LOAD_ORIGINAL,
        SAVE_IMAGE,
        SAVE_LIVE_PHOTO,
        SAVE_ALL,
        SHARE,
        COPY_URL;

        @get:StringRes
        val titleRes: Int
            get() = when (this) {
                LOAD_ORIGINAL -> ImageViewerR.string.image_viewer_original
                SAVE_IMAGE -> R.string.image_save
                SAVE_LIVE_PHOTO -> R.string.image_save_live
                SAVE_ALL -> R.string.image_save_all
                SHARE -> R.string.image_share
                COPY_URL -> R.string.image_copy_url
            }
    }
}
