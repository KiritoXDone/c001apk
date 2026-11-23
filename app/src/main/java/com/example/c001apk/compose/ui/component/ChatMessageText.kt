package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.chat.ChatFeedLink
import com.example.c001apk.compose.util.ShareContent
import com.example.c001apk.compose.util.ShareType
import org.jsoup.Jsoup
import top.yukonga.miuix.kmp.basic.BasicComponent

@Composable
fun ChatMessageText(
    text: String?,
    modifier: Modifier = Modifier,
    onOpenLink: ((String, String?) -> Unit)?,
) {
    val feedId =
        remember(text) {
            val doc = Jsoup.parseBodyFragment(text.orEmpty())
            val plain = doc.text().trim()
            val candidate =
                if (
                    plain.startsWith("http://") ||
                        plain.startsWith("https://") ||
                        plain.startsWith("coolmarket://")
                )
                    plain
                else
                    doc.select("a[href]")
                        .singleOrNull()
                        ?.takeIf { it.text() == plain }
                        ?.attr("href")
            candidate?.let(ChatFeedLink::parse)
        }
    if (feedId == null) LinkText(text = text, modifier = modifier, onOpenLink = onOpenLink)
    else
        BasicComponent(
            title = stringResource(R.string.feed_coolapk),
            summary = stringResource(R.string.feed_with_id, feedId),
            modifier = modifier,
            onClick = { onOpenLink?.invoke(ShareContent.link(ShareType.FEED, feedId), null) },
        )
}
