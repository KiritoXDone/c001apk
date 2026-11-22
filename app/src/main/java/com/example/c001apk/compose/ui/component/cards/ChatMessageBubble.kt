package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.constant.Constants.PREFIX_HTTP
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.ChatMessageText
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.ImageView
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.ImageShowUtil
import com.example.c001apk.compose.util.longClick
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUser3Line

@Composable
internal fun ChatMessageBubble(
    data: HomeFeedResponse.Data,
    outgoing: Boolean,
    modifier: Modifier = Modifier,
    onGetImageUrl: (String) -> Unit,
    onLongClick: (String, String, String) -> Unit,
    onViewUser: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onClearFocus: () -> Unit,
) {
    val picture = data.messagePic.orEmpty()
    LaunchedEffect(data.id, picture) {
        if (picture.isNotBlank() && !picture.startsWith(PREFIX_HTTP)) {
            onGetImageUrl(data.id.orEmpty())
        }
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxBubbleWidth = (maxWidth - 92.dp).coerceAtLeast(0.dp).coerceAtMost(420.dp)
        Row(
            modifier = Modifier.fillMaxWidth()
                .longClick { onLongClick(data.id.orEmpty(), data.message.orEmpty(), picture) }
                .padding(horizontal = 14.dp, vertical = 5.dp),
            horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            if (!outgoing) ChatAvatar(data.fromUserAvatar) { onViewUser(data.messageUid.orEmpty()) }
            Column(
                modifier = Modifier.widthIn(max = maxBubbleWidth)
                    .padding(start = if (outgoing) 0.dp else 10.dp, end = if (outgoing) 10.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start,
            ) {
                if (!data.message.isNullOrEmpty()) {
                    CompositionLocalProvider(
                        LocalContentColor provides if (outgoing) MiuixTheme.colorScheme.onPrimaryContainer
                        else MiuixTheme.colorScheme.onSurface,
                    ) {
                        ChatMessageText(
                            text = data.message,
                            onOpenLink = onOpenLink,
                            modifier = Modifier.squircleSurface(
                                color = if (outgoing) MiuixTheme.colorScheme.primaryContainer
                                else MiuixTheme.colorScheme.surfaceContainerHigh,
                                topStart = if (outgoing) 20.dp else 8.dp,
                                topEnd = if (outgoing) 8.dp else 20.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp,
                            ).padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
                if (picture.isNotEmpty()) {
                    val aspect = remember(picture) {
                        val dimensions = runCatching {
                            ImageShowUtil.getImageLp(picture.substringBefore('?'))
                        }.getOrNull()
                        if (dimensions != null && dimensions.first > 0 && dimensions.second > 0) {
                            (dimensions.second.toFloat() / dimensions.first).coerceIn(0.65f, 1.8f)
                        } else 1f
                    }
                    val imageWidth = maxBubbleWidth.coerceAtMost(200.dp)
                    ImageView(
                        url = picture,
                        isChat = true,
                        modifier = Modifier.width(imageWidth).height(imageWidth * aspect)
                            .squircleClip(18.dp).background(cardBg()),
                        onClearFocus = onClearFocus,
                    )
                }
            }
            if (outgoing) ChatAvatar(data.fromUserAvatar) { onViewUser(data.fromuid.orEmpty()) }
        }
    }
}

@Composable
private fun ChatAvatar(avatar: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "查看用户主页" },
        contentAlignment = Alignment.Center,
    ) {
        if (!avatar.isNullOrBlank()) {
            CoilLoader(modifier = Modifier.fillMaxSize(), url = avatar)
        } else {
            Icon(
                MingCuteUser3Line,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
