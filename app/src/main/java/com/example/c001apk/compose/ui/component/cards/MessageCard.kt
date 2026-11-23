package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.LinkText
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.DateUtils.fromToday
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    onViewUser: (String) -> Unit,
    onHandleMessage: ((String, Int) -> Unit)? = null,
    onViewChat: ((String, String, String) -> Unit)? = null,
) {
    val name = data.messageUsername?.takeIf(String::isNotBlank) ?: "用户"
    val unreadCount = (data.unreadNum ?: 0).coerceAtLeast(0)
    val preview = data.message?.takeIf(String::isNotBlank)
        ?: if (!data.messagePic.isNullOrBlank()) "[图片]" else ""

    Row(
        modifier = modifier.fillMaxWidth()
            .squircleSurface(cardBg(), 20.dp)
            .combinedClickable(
                role = Role.Button,
                onClick = {
                    onViewChat?.invoke(data.ukey.orEmpty(), data.messageUid.orEmpty(), data.messageUsername.orEmpty())
                },
                onLongClickLabel = "会话操作",
                onLongClick = onHandleMessage?.let { handle ->
                    { handle(data.ukey.orEmpty(), data.isTop ?: 0) }
                },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                .clickable { onViewUser(data.messageUid.orEmpty()) }
                .semantics { contentDescription = "查看${name}的主页" },
            contentAlignment = Alignment.Center,
        ) {
            if (!data.messageUserAvatar.isNullOrBlank()) {
                CoilLoader(modifier = Modifier.fillMaxSize(), url = data.messageUserAvatar)
            } else {
                Icon(
                    MiuixIcons.Glass.ContactsCircle,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                data.dateline?.let { timestamp ->
                    Text(
                        text = fromToday(timestamp),
                        modifier = Modifier.padding(start = 8.dp),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinkText(
                    text = preview,
                    onOpenLink = onOpenLink,
                    maxLines = 1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.toArgb(),
                    modifier = Modifier.weight(1f),
                )
                if (data.isTop == 1) {
                    Text(
                        text = "置顶",
                        color = MiuixTheme.colorScheme.primary,
                        style = MiuixTheme.textStyles.body2,
                    )
                }
                if (unreadCount > 0) {
                    Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                }
            }
        }
    }
}
