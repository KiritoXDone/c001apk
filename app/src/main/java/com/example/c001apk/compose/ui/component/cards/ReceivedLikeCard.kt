package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.ReceivedLike
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.DateUtils.fromToday
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ReceivedLikeCard(
    data: ReceivedLike,
    onViewUser: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val likerDescription = stringResource(R.string.notice_liker, data.username)
    val contentDescription = stringResource(R.string.notice_liked_content)
    Column(
        modifier.fillMaxWidth().padding(horizontal = 10.dp)
            .squircleSurface(cardBg(), 16.dp)
            .combinedClickable(
                onClick = { data.url?.let { onOpenLink(it, null) } },
                onLongClick = { onCopyText(data.copyText) },
            ).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).clickable(
                    enabled = data.userId != null,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.notice_view_liker),
                ) { data.userId?.let(onViewUser) }
                    .semantics { this.contentDescription = likerDescription },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CoilLoader(url = data.avatar, modifier = Modifier.size(30.dp).clip(CircleShape))
                Text(
                    data.username,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            data.timestamp?.let {
                Text(
                    fromToday(it),
                    modifier = Modifier.padding(start = 10.dp),
                    style = MiuixTheme.textStyles.body2.copy(fontSize = 14.sp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Text(data.action, fontSize = 15.sp)
        Row(
            Modifier.fillMaxWidth().squircleSurface(MiuixTheme.colorScheme.surface, 16.dp)
                .combinedClickable(
                    enabled = data.url != null,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.notice_view_liked_content),
                    onClick = { data.url?.let { onOpenLink(it, null) } },
                    onLongClick = { onCopyText(data.copyText) },
                ).semantics { this.contentDescription = contentDescription }.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            data.thumbnail?.let {
                CoilLoader(url = it, modifier = Modifier.size(40.dp).squircleClip(8.dp))
            }
            Column(Modifier.weight(1f)) {
                if (data.author.isNotBlank()) {
                    Text(
                        "@${data.author}",
                        style = MiuixTheme.textStyles.body1.copy(fontSize = 13.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    data.summary,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
