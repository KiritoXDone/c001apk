package com.example.c001apk.compose.ui.component

import android.graphics.Typeface
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.ImageShowUtil
import com.example.c001apk.compose.view.LinkTextView
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun LinkText(
    modifier: Modifier = Modifier,
    text: String?,
    textSize: Float = 15f,
    isBold: Boolean = false,
    lineSpacingMultiplier: Float = 1.0f,
    maxLines: Int? = null,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    color: Int? = null,
    onOpenLink: ((String, String?) -> Unit)? = null,
    isReply: Boolean = false,
    onShowTotalReply: (() -> Unit)? = null,
    imgList: List<String>? = null,
) {
    val contentColor = LocalContentColor.current
    val userPreference = LocalUserPreferences.current

    val primary = MiuixTheme.colorScheme.primary.toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LinkTextView(context).apply {
                if (isBold) setTypeface(typeface, Typeface.BOLD)
                setLineSpacing(0.0f, lineSpacingMultiplier)
                maxLines?.let {
                    this.maxLines = it
                    this.ellipsize = ellipsize
                }
                setTextColor(contentColor.toArgb())
                color?.let {
                    setTextColor(it)
                }
            }
        },
        update = { textView ->
            textView.setTextColor(color ?: contentColor.toArgb())
            textView.setParams(
                isReply = isReply,
                size = textSize,
                fontScale = userPreference.fontScale,
            )
            textView.setSpText(
                text = text.orEmpty(),
                color = primary,
                onOpenLink = { url, title ->
                    onOpenLink?.let { it(url, title) }
                },
                onShowTotalReply = onShowTotalReply,
                onShowImages = { url ->
                    ImageShowUtil.startBigImgViewSimple(
                        textView.context,
                        imgList ?: listOf(url),
                        userAgent = userPreference.userAgent,
                    )
                },
            )
        },
    )
}
