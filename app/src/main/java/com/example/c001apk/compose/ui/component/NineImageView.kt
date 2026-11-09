package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.c001apk.compose.constant.Constants.SUFFIX_THUMBNAIL
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.ImageShowUtil.getImageLp
import com.example.c001apk.compose.util.isGifUrl
import com.example.c001apk.compose.view.NineGridImageView
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalFeedImagePlayback = compositionLocalOf<() -> Boolean> { { true } }

/** Created by bggRGjQaUbCoE on 2024/6/5 */
@Composable
fun NineImageView(
    modifier: Modifier = Modifier,
    pic: String?,
    picArr: List<String>?,
    feedType: String?,
    isSingle: Boolean = false,
) {
    val prefs = LocalUserPreferences.current
    val canPlayImages = LocalFeedImagePlayback.current

    val primaryContainer = MiuixTheme.colorScheme.primaryContainer.toArgb()
    val onPrimaryContainer = MiuixTheme.colorScheme.onPrimaryContainer.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            NineGridImageView(it).apply {
                this.colorPrimaryContainer = primaryContainer
                this.colorOnPrimaryContainer = onPrimaryContainer
                this.isSingle = isSingle
                this.userAgent = prefs.userAgent
            }
        },
        update = { imageView ->
            imageView.isAnimationEnabled = canPlayImages()
            imageView.isSingle = isSingle
            imageView.userAgent = prefs.userAgent
            imageView.colorPrimaryContainer = primaryContainer
            imageView.colorOnPrimaryContainer = onPrimaryContainer
            if (!picArr.isNullOrEmpty()) {
                if (picArr.size == 1 || feedType in listOf("feedArticle", "trade")) {
                    val imageLp = getImageLp(pic ?: picArr[0])
                    imageView.imgWidth = imageLp.first
                    imageView.imgHeight = imageLp.second
                }
                imageView.apply {
                    val urlList: MutableList<String> = ArrayList()
                    if (feedType in listOf("feedArticle", "trade") && imgWidth > imgHeight)
                        if (!pic.isNullOrEmpty()) urlList.add(pic.toPreviewUrl())
                        else urlList.add(picArr[0].toPreviewUrl())
                    else urlList.addAll(picArr.map { it.toPreviewUrl() })
                    setUrlList(urlList)
                }
            }
        },
    )
}

private fun String.toPreviewUrl(): String =
    if (isGifUrl || contains(SUFFIX_THUMBNAIL)) this else "$this$SUFFIX_THUMBNAIL"
