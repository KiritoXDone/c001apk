package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse.Vote
import com.example.c001apk.compose.ui.component.MiuixActionButton
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.feed.reply.VoteCommentTarget
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun Vote.commentTarget(feedId: String, accountRevision: Long): VoteCommentTarget? {
    val voteId = id?.takeIf(String::isNotBlank) ?: return null
    val now = System.currentTimeMillis() / 1_000
    if (isUpcoming(now) || isClosed(now) || selectedIds.isEmpty() || userVoteCommentStatus == 1) return null
    // 匿名状态缺失时不推测用户在支持匿名的投票中选了哪种身份。
    if (anonymousStatus == 1 && userAnonymousStatus == null) return null
    return VoteCommentTarget(feedId, voteId, userAnonymousStatus == 1, accountRevision)
}

@Composable
internal fun VoteCommentsHeader(count: String, onWriteComment: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.vote_comments, count),
            modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.headline2,
        )
        if (onWriteComment != null) {
            MiuixActionButton(onClick = rememberHapticClick(onClick = onWriteComment)) {
                Text(stringResource(R.string.feed_write_comment))
            }
        }
    }
}
