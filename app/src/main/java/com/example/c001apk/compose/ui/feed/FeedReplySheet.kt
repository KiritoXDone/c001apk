package com.example.c001apk.compose.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.c001apk.compose.ui.component.FeedListEntry
import com.example.c001apk.compose.ui.component.MiuixNavigationSheet
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getAllLinkAndText

@Composable
internal fun FeedReplySheet(
    viewModel: FeedViewModel,
    onDismiss: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onLaunchReply: () -> Unit,
) {
    val context = LocalContext.current
    MiuixNavigationSheet(onDismissRequest = onDismiss) { dismiss ->
        ReplyThreadContent(
            state = viewModel.replyLoadingState,
            rootId = viewModel.replyRootId,
            threadKey = viewModel.replyId,
            footerState = viewModel.replyFooterState,
            isEnd = viewModel.stopReplyAutomaticLoading,
            onLoadMore = viewModel::loadMoreReply,
            onDismiss = dismiss,
        ) { reply, index ->
            FeedListEntry(
                item = reply,
                index = index,
                onViewUser = { uid ->
                    onDismiss()
                    onViewUser(uid)
                },
                onViewFeed = { id, isViewReply ->
                    onDismiss()
                    onViewFeed(id, isViewReply)
                },
                onOpenLink = { url, title ->
                    onDismiss()
                    onOpenLink(url, title)
                },
                onCopyText = {
                    context.copyText(it?.getAllLinkAndText?.richToString())
                },
                onReport = { id, type ->
                    onDismiss()
                    onReport(id, type)
                },
                isTotalReply = true,
                onLike = { id, like, likeType ->
                    viewModel.onLikeReply(id, like, likeType)
                },
                onDelete = { id, deleteType, _ ->
                    viewModel.onDeleteRely(id, deleteType)
                },
                onBlockUser = { uid, _ ->
                    viewModel.onBlockReplyUser(uid)
                },
                isReply2Reply = !viewModel.frid.isNullOrEmpty(),
                onShowTotalReply = { id, uid, frid ->
                    viewModel.resetReplyState()
                    viewModel.replyId = id
                    viewModel.replyUid = uid
                    viewModel.frid = frid
                    viewModel.fetchTotalReply()
                },
                onReply = { rid, uid, username, _ ->
                    viewModel.isSheet = true
                    viewModel.replyId = rid
                    viewModel.replyUid = uid
                    viewModel.replyName = username
                    viewModel.frid = null
                    viewModel.replyType = "reply"
                    onLaunchReply()
                },
            )
        }
    }
}
