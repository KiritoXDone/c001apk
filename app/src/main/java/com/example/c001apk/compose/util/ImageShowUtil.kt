package com.example.c001apk.compose.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentDialog
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.constant.Constants.SUFFIX_THUMBNAIL
import com.example.c001apk.compose.ui.component.MiuixViewDialogBuilder
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.view.CircleIndexIndicator
import com.example.c001apk.compose.view.NineGridImageView
import com.example.c001apk.media.HdrMojitoActivityCoverLoader
import com.example.c001apk.media.LivePhotoVideoUrlResolver
import com.example.c001apk.media.MojitoMediaImageFactory
import com.example.c001apk.media.MojitoMediaPlaybackSession
import com.example.c001apk.media.RichMediaHint
import com.example.c001apk.media.coolApkRichMediaHint
import com.example.c001apk.media.mayContainCoolApkRichMedia
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.MojitoBuilder
import net.mikaelzero.mojito.ext.mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.DefaultTargetFragmentCover
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback
import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory
import net.mikaelzero.mojito.loader.ContentLoader
import top.yukonga.miuix.kmp.basic.Card as Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
        val originList = urlList.map {
            if (it.contains(SUFFIX_THUMBNAIL)) it.replace(SUFFIX_THUMBNAIL, EMPTY_STRING).http2https
            else it.http2https
        }
        val autoLoadOriginal = shouldAutoLoadTarget()
        val thumbnailList = urlList.mapIndexed { index, url ->
            val preview =
                if (autoLoadOriginal || !originList[index].mayContainCoolApkRichMedia())
                    url.http2https
                else originList[index]
            uncroppedPreviewUrl(originList[index], preview)
        }
        Mojito.start(imageView.context) {
            urls(thumbnailList, originList)
            val mediaFactories =
                enableRichMedia(
                    urls = originList,
                    previewUrls = thumbnailList,
                )
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
            if (urlList.size != 1) setIndicator(CircleIndexIndicator())
            views(nineGridView.getImageViews().toTypedArray())
            autoLoadTarget(autoLoadOriginal)
            fragmentCoverLoader {
                DefaultTargetFragmentCover()
            }
            setOnMojitoListener(
                object : SimpleMojitoViewCallback() {
                    override fun onStartAnim(position: Int) {
                        nineGridView.getImageViewAt(position)?.apply {
                            postDelayed(
                                {
                                    this.isVisible = false
                                },
                                200,
                            )
                        }
                    }

                    override fun onMojitoViewFinish(pagePosition: Int) {
                        nineGridView.getImageViews().forEach {
                            it.isVisible = true
                        }
                    }

                    override fun onViewPageSelected(position: Int) {
                        nineGridView.getImageViews().forEachIndexed { index, imageView ->
                            imageView.isVisible = position != index
                        }
                    }

                    override fun onLongClick(
                        fragmentActivity: FragmentActivity?,
                        view: View,
                        x: Float,
                        y: Float,
                        position: Int,
                    ) {
                        if (BuildConfig.DEBUG) {
                            Log.d(
                                "MojitoLongPress",
                                "ImageShowUtil onLongClick pos=$position x=$x y=$y activity=${fragmentActivity != null}",
                            )
                        }
                        if (fragmentActivity != null) {
                            showSaveImgDialog(
                                fragmentActivity,
                                originList[position],
                                originList,
                                userAgent,
                                mediaFactories[position],
                            )
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.i("Mojito", "fragmentActivity is null, skip save image")
                            }
                        }
                    }
                },
            )
        }
    }

    fun startBigImgViewSimple(
        context: Context,
        urlList: List<String>,
        cookie: String? = null,
        userAgent: String? = null,
    ) {
        val originList = urlList.map {
            if (it.contains(SUFFIX_THUMBNAIL)) it.replace(SUFFIX_THUMBNAIL, EMPTY_STRING).http2https
            else it.http2https
        }
        val autoLoadOriginal = shouldAutoLoadTarget()
        val thumbnailList = urlList.mapIndexed { index, url ->
            val preview =
                when {
                    !autoLoadOriginal && originList[index].mayContainCoolApkRichMedia() ->
                        originList[index]
                    url.contains(SUFFIX_THUMBNAIL) -> url.http2https
                    else -> "${originList[index]}$SUFFIX_THUMBNAIL"
                }
            uncroppedPreviewUrl(originList[index], preview)
        }
        Mojito.start(context) {
            urls(thumbnailList, originList)
            val mediaFactories =
                enableRichMedia(
                    urls = originList,
                    previewUrls = thumbnailList,
                )
            autoLoadTarget(autoLoadOriginal)
            fragmentCoverLoader {
                DefaultTargetFragmentCover()
            }
            progressLoader {
                DefaultPercentProgress()
            }
            if (urlList.size > 1) setIndicator(CircleIndexIndicator())
            setOnMojitoListener(
                object : SimpleMojitoViewCallback() {
                    override fun onLongClick(
                        fragmentActivity: FragmentActivity?,
                        view: View,
                        x: Float,
                        y: Float,
                        position: Int,
                    ) {
                        if (BuildConfig.DEBUG) {
                            Log.d(
                                "MojitoLongPress",
                                "ImageShowUtilSimple onLongClick pos=$position x=$x y=$y activity=${fragmentActivity != null}",
                            )
                        }
                        if (fragmentActivity != null) {
                            showSaveImgDialog(
                                fragmentActivity,
                                originList[position],
                                originList,
                                userAgent,
                                mediaFactories[position],
                            )
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.i("Mojito", "fragmentActivity is null, skip save image")
                            }
                        }
                    }
                },
            )
        }
    }

    fun startBigImgViewSimple(
        imageView: ImageView,
        url: String,
        cookie: String? = null,
        userAgent: String? = null,
    ) {
        val previewUrl = uncroppedPreviewUrl(url.replace(SUFFIX_THUMBNAIL, EMPTY_STRING), url)
        imageView.mojito(
            url = previewUrl,
            builder = {
                val mediaFactories =
                    enableRichMedia(
                        urls = listOf(previewUrl),
                    )
                progressLoader {
                    DefaultPercentProgress()
                }
                setOnMojitoListener(
                    object : SimpleMojitoViewCallback() {
                        override fun onLongClick(
                            fragmentActivity: FragmentActivity?,
                            view: View,
                            x: Float,
                            y: Float,
                            position: Int,
                        ) {
                            if (BuildConfig.DEBUG) {
                                Log.d(
                                    "MojitoLongPress",
                                    "ImageShowUtilSingle onLongClick pos=$position x=$x y=$y activity=${fragmentActivity != null}",
                                )
                            }
                            if (fragmentActivity != null) {
                                showSaveImgDialog(
                                    fragmentActivity,
                                    previewUrl,
                                    null,
                                    userAgent,
                                    mediaFactories[position],
                                )
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.i("Mojito", "fragmentActivity is null, skip save image")
                                }
                            }
                        }
                    },
                )
            },
        )
    }

    private fun showSaveImgDialog(
        context: Context,
        url: String,
        urlList: List<String>?,
        userAgent: String?,
        mediaFactory: MojitoMediaImageFactory?,
    ) {
        val actions = saveDialogActions(mediaFactory)
        if (context is FragmentActivity) {
            showSaveImgDialogCompose(context, url, urlList, userAgent, mediaFactory, actions)
            return
        }
        MiuixViewDialogBuilder(context).apply {
            setItems(actions.map { context.getString(it.titleRes) }.toTypedArray()) { _, position: Int ->
                handleSaveDialogAction(
                    context,
                    url,
                    urlList,
                    userAgent,
                    mediaFactory,
                    actions[position],
                )
            }
            show()
        }
    }

    private fun showSaveImgDialogCompose(
        activity: FragmentActivity,
        url: String,
        urlList: List<String>?,
        userAgent: String?,
        mediaFactory: MojitoMediaImageFactory?,
        actions: List<SaveImageAction>,
    ) {
        val dialog = ComponentDialog(activity)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val composeView =
            ComposeView(activity).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                setContent {
                    C001apkComposeTheme(
                        darkTheme = CookieUtil.isDarkMode,
                        themeType = CookieUtil.themeType,
                        seedColor = CookieUtil.seedColor,
                        materialYou = CookieUtil.materialYou,
                        pureBlack = CookieUtil.pureBlack,
                        paletteStyle = CookieUtil.paletteStyle,
                        fontScale = CookieUtil.fontScale,
                        contentScale = CookieUtil.contentScale,
                    ) {
                        SaveImageDialogContent(
                            items = actions.map { activity.getString(it.titleRes) },
                            onClick = { index ->
                                handleSaveDialogAction(
                                    activity,
                                    url,
                                    urlList,
                                    userAgent,
                                    mediaFactory,
                                    actions[index],
                                )
                                dialog.dismiss()
                            },
                        )
                    }
                }
            }
        dialog.setContentView(composeView)
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    @Composable
    private fun SaveImageDialogContent(
        items: List<String>,
        onClick: (Int) -> Unit,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp).padding(horizontal = 24.dp),
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier.padding(PaddingValues(vertical = 10.dp)),
            ) {
                items.forEachIndexed { index, item ->
                    Text(
                        text = item,
                        style = MiuixTheme.textStyles.body1,
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable { onClick(index) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    private fun handleSaveDialogAction(
        context: Context,
        url: String,
        urlList: List<String>?,
        userAgent: String?,
        mediaFactory: MojitoMediaImageFactory?,
        action: SaveImageAction,
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
                handleSaveDialogAction(context, url, urlList, userAgent, mediaFactory, action)
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
        }
    }

    private fun saveDialogActions(mediaFactory: MojitoMediaImageFactory?): List<SaveImageAction> =
        buildList {
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
        mediaFactory: MojitoMediaImageFactory?,
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

    private fun MojitoBuilder.enableRichMedia(
        urls: List<String>,
        previewUrls: List<String>? = null,
    ): MutableMap<Int, MojitoMediaImageFactory> {
        val mediaFactories = mutableMapOf<Int, MojitoMediaImageFactory>()
        val mediaHints = urls.map(String::coolApkRichMediaHint)
        val richMediaPositions =
            urls.indices.filterTo(mutableSetOf()) { mediaHints[it] != RichMediaHint.NONE }
        val gifPositions = urls.indices.filterTo(mutableSetOf()) { urls[it].isGifUrl }
        if (richMediaPositions.isEmpty() && gifPositions.isEmpty()) return mediaFactories

        val defaultFactory = Mojito.imageViewFactory() ?: return mediaFactories
        val gifFactories = mutableMapOf<Int, ImageViewLoadFactory>()
        val playbackSession = MojitoMediaPlaybackSession()
        multiContentLoader(
            providerLoader = { position ->
                when (position) {
                    in richMediaPositions ->
                        mediaFactories.getOrPut(position) {
                            MojitoMediaImageFactory(
                                delegate = defaultFactory,
                                imageUrl = urls[position],
                                videoUrlResolver = livePhotoVideoUrlResolver,
                                mediaHint = mediaHints[position],
                                playbackSession = playbackSession,
                                pagePosition = position,
                                deferMediaBindingUntilTarget =
                                    previewUrls?.getOrNull(position)?.let {
                                        it != urls[position]
                                    } == true,
                            )
                        }
                    in gifPositions ->
                        gifFactories.getOrPut(position) {
                            SingleBindGifImageFactory(defaultFactory)
                        }
                    else -> defaultFactory
                }
            },
            providerEnableTargetLoad = { position -> position !in gifPositions },
        )
        if (richMediaPositions.isNotEmpty()) {
            setActivityCoverLoader(HdrMojitoActivityCoverLoader(playbackSession))
        }
        return mediaFactories
    }

    private class SingleBindGifImageFactory(
        private val delegate: ImageViewLoadFactory,
    ) : ImageViewLoadFactory {
        private var boundFileKey: String? = null

        override fun loadSillContent(view: View, uri: Uri) {
            val file = uri.path?.let(::File)
            val fileKey = file?.let { "${it.absolutePath}:${it.length()}" }
            if (fileKey != null && fileKey == boundFileKey) return

            boundFileKey = fileKey
            delegate.loadSillContent(view, uri)
        }

        override fun loadContentFail(view: View, @DrawableRes drawableResId: Int) {
            delegate.loadContentFail(view, drawableResId)
        }

        override fun newContentLoader(): ContentLoader {
            boundFileKey = null
            return delegate.newContentLoader()
        }
    }

    private fun shouldAutoLoadTarget(): Boolean =
        when (CookieUtil.imageQuality) {
            0 -> NetWorkUtil.isWifiConnected()
            1 -> true
            else -> false
        }

    private enum class SaveImageAction(@param:StringRes val titleRes: Int) {
        SAVE_IMAGE(R.string.image_save),
        SAVE_LIVE_PHOTO(R.string.image_save_live),
        SAVE_ALL(R.string.image_save_all),
        SHARE(R.string.image_share),
        COPY_URL(R.string.image_copy_url),
    }
}
