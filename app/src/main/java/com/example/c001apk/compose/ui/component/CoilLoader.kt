package com.example.c001apk.compose.ui.component

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.http2https
import com.example.c001apk.compose.util.isGifUrl
import com.example.c001apk.compose.util.logImageFailure
import jp.wasabeef.transformers.coil.ColorFilterTransformation
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/11 */
@Composable
fun CoilLoader(
    modifier: Modifier = Modifier,
    url: String?,
    colorFilter: Long? = null,
    contentScale: ContentScale = ContentScale.Crop,
    allowHardware: Boolean = true,
    onSuccess: ((Drawable) -> Unit)? = null,
) {
    val prefs = LocalUserPreferences.current
    url?.let {
        val context = LocalContext.current
        val imageUrl = it.http2https
        val isGif = imageUrl.isGifUrl
        var failed by remember(imageUrl) { mutableStateOf(false) }
        var attempt by remember(imageUrl) { mutableIntStateOf(0) }
        Box(modifier = modifier, propagateMinConstraints = true) {
            key(imageUrl, attempt) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(context)
                            .allowHardware(allowHardware)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCacheKey(imageUrl)
                            .diskCacheKey(imageUrl)
                            .data(imageUrl)
                            .addHeader("User-Agent", prefs.userAgent)
                            .apply {
                                if (isGif) {
                                    decoderFactory(
                                        if (Build.VERSION.SDK_INT >= 28) {
                                            ImageDecoderDecoder.Factory()
                                        } else {
                                            GifDecoder.Factory()
                                        },
                                    )
                                }
                                colorFilter
                                    ?.takeUnless { isGif }
                                    ?.let {
                                        transformations(
                                            ColorFilterTransformation(Color(colorFilter).toArgb()),
                                        )
                                    }
                                if (
                                    !isGif &&
                                        colorFilter == null &&
                                        prefs.isDarkMode() &&
                                        CookieUtil.imageFilter
                                ) {
                                    transformations(
                                        ColorFilterTransformation(Color(0x2D000000).toArgb()),
                                    )
                                }
                            }
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxWidth(),
                    onSuccess = {
                        failed = false
                        onSuccess?.invoke(it.result.drawable)
                    },
                    onError = {
                        failed = true
                        logImageFailure(it.result.throwable)
                    },
                )
            }
            if (failed) {
                Box(
                    Modifier.matchParentSize().background(MiuixTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = {
                            failed = false
                            attempt++
                        },
                    ) {
                        Icon(
                            MiuixIcons.Refresh,
                            contentDescription = "图片加载失败，重试",
                            modifier = Modifier.size(20.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}
