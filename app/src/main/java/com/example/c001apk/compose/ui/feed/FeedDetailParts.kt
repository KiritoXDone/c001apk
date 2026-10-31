package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.cards.FeedVisibilityNotice
import com.example.c001apk.compose.ui.component.icons.Smartphone
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.Utils.richToString
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SinkFeedback

@Composable
fun FeedDetailAuthor(data: HomeFeedResponse.Data, onViewUser: (String) -> Unit) {
    ReadingSurface(first = true) {
        FeedVisibilityNotice(data.publishStatus)
        Column(
            Modifier.fillMaxWidth()
                .clickable(interactionSource = null, indication = SinkFeedback()) {
                    onViewUser(data.uid.orEmpty())
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(48.dp)
                        .squircleBackground(MiuixTheme.colorScheme.secondaryContainer, 16.dp),
                ) {
                    Text(
                        data.username.orEmpty().take(1),
                        modifier = Modifier.align(Alignment.Center),
                        style = MiuixTheme.textStyles.title3,
                    )
                    CoilLoader(
                        url = data.userAvatar,
                        modifier = Modifier.fillMaxSize().squircleClip(16.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        data.username.orEmpty(),
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            buildAnnotatedString {
                                append(
                                    listOfNotNull(
                                            fromToday(data.dateline ?: 0),
                                            data.ipLocation?.takeIf(String::isNotBlank),
                                        )
                                        .joinToString(" · "),
                                )
                                data.deviceTitle?.richToString()?.takeIf(String::isNotBlank)?.let {
                                    device ->
                                    append(" · ")
                                    appendInlineContent("device", "机型")
                                    append(" $device")
                                }
                            },
                        inlineContent =
                            mapOf(
                                "device" to
                                    InlineTextContent(
                                        Placeholder(
                                            12.sp,
                                            12.sp,
                                            PlaceholderVerticalAlign.TextCenter,
                                        ),
                                    ) {
                                        Icon(
                                            MiuixIcons.Smartphone,
                                            null,
                                            Modifier.fillMaxSize(),
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    },
                            ),
                        style = MiuixTheme.textStyles.footnote1,
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    MiuixIcons.ChevronForward,
                    contentDescription = "查看用户主页",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Article blocks stay lazy while the outer edges form one continuous reading card. */
@Composable
fun ReadingSurface(first: Boolean = false, last: Boolean = false, content: @Composable () -> Unit) {
    val top = if (first) 20.dp else 0.dp
    val bottom = if (last) 20.dp else 0.dp
    Column(
        Modifier.fillMaxWidth()
            .squircleSurface(MiuixTheme.colorScheme.surfaceContainer, top, top, bottom, bottom)
            .padding(top = if (first) 6.dp else 0.dp, bottom = if (last) 8.dp else 0.dp),
    ) {
        content()
    }
}
