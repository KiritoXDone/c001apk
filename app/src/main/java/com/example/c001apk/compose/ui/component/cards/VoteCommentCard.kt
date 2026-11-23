package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.FavoriteFill
import top.yukonga.miuix.kmp.icon.glass.FavoriteOutline
import top.yukonga.miuix.kmp.icon.glass.Reply
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun VoteCommentCard(
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)?,
    onLike: ((String, Int, LikeType) -> Unit)?,
    onDelete: ((String, LikeType, String?) -> Unit)?,
    onBlockUser: (String) -> Unit,
) {
    val feedId = data.id.orEmpty()
    val authorId = data.uid.orEmpty()
    val name = data.username?.takeIf(String::isNotBlank) ?: stringResource(R.string.label_anonymous_user)
    val like = data.userAction?.like ?: 0
    val liked = like == 1
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 4.dp),
        showIndication = true,
        onClick = if (feedId.isNotBlank()) ({ onViewFeed(feedId, false) }) else null,
        onLongPress = { onCopyText(data.message) },
    ) {
        FeedVisibilityNotice(data.publishStatus)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).heightIn(min = 44.dp).squircleClip(10.dp)
                    .clickable(
                        enabled = authorId.isNotBlank(),
                        onClickLabel = stringResource(R.string.user_view_profile),
                    ) { onViewUser(authorId) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(name.take(1), style = MiuixTheme.textStyles.body2)
                    CoilLoader(Modifier.fillMaxSize(), data.userAvatar?.takeIf(String::isNotBlank))
                }
                Text(
                    text = name,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                data.dateline?.takeIf { it > 0 }?.let { timestamp ->
                    Text(
                        fromToday(timestamp),
                        color = colors.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.footnote1,
                        maxLines = 1,
                    )
                }
            }
            FeedOverflowMenu(
                data = data,
                modifier = Modifier.padding(start = 4.dp),
                onReport = onReport,
                onDelete = onDelete,
                onBlockUser = onBlockUser,
            )
        }
        FeedMessage(
            modifier = Modifier.fillMaxWidth(),
            data = data,
            onOpenLink = onOpenLink,
            isFeedContent = false,
            onViewFeed = onViewFeed,
            onCopyText = onCopyText,
        )
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VoteCommentAction(
                icon = if (liked) MiuixIcons.Glass.FavoriteFill else MiuixIcons.Glass.FavoriteOutline,
                label = stringResource(if (liked) R.string.action_unlike else R.string.action_like),
                text = data.likenum?.takeIf(String::isNotBlank) ?: "0",
                enabled = LocalUserPreferences.current.isLogin && onLike != null && feedId.isNotBlank(),
                highlighted = liked,
                onClick = { onLike?.invoke(feedId, like, LikeType.FEED) },
            )
            Box(Modifier.width(1.dp).height(12.dp).background(colors.dividerLine))
            VoteCommentAction(
                icon = MiuixIcons.Glass.Reply,
                label = stringResource(R.string.action_view_replies),
                text = stringResource(R.string.feed_reply),
                enabled = feedId.isNotBlank(),
                onClick = { onViewFeed(feedId, true) },
            )
        }
        FeedRows(
            isFeedContent = false,
            relationRows = data.relationRows,
            targetRow = data.targetRow,
            onOpenLink = onOpenLink,
        )
    }
}

@Composable
private fun VoteCommentAction(
    icon: ImageVector,
    label: String,
    text: String,
    enabled: Boolean,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val tint = if (highlighted) colors.primary else colors.onSurfaceVariantSummary
    Row(
        modifier = Modifier.widthIn(min = 48.dp).heightIn(min = 44.dp).squircleClip(10.dp)
            .clickable(
                enabled = enabled,
                onClickLabel = label,
                role = Role.Button,
                onClick = rememberHapticClick(onClick = onClick),
            )
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = tint)
        Text(text, color = tint, style = MiuixTheme.textStyles.body2)
    }
}
