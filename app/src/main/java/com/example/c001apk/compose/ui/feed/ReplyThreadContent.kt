package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.FooterCard
import com.example.c001apk.compose.ui.component.cards.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Original comment and replies remain lazy, with one continuous surface for the reply group. */
@Composable
fun ReplyThreadContent(
    state: LoadingState<List<HomeFeedResponse.Data>>,
    rootId: String?,
    threadKey: String,
    footerState: FooterState,
    isEnd: Boolean,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    replyContent: @Composable (HomeFeedResponse.Data, Int) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(threadKey) { listState.scrollToItem(0) }
    val all = (state as? LoadingState.Success)?.response.orEmpty()
    val original = all.firstOrNull { rootId != null && it.id == rootId }
    val replies = if (original == null) all else all.filterNot { it.id == rootId }

    Column(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                stringResource(R.string.feed_all_replies),
                modifier = Modifier.align(Alignment.Center),
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(MiuixIcons.Basic.Close, stringResource(R.string.feed_close_replies), Modifier.size(20.dp))
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val inset = ((maxWidth - 800.dp) / 2).coerceAtLeast(16.dp)
            CompositionLocalProvider(LocalDetailComments provides true) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding =
                        PaddingValues(
                            start = inset,
                            end = inset,
                            top = 4.dp,
                            // 导航栏高度只作为滚动内容的底边距：滑到底时最后一条能完整
                            // 露出手势条上方，平时滚动内容直接贴到底边，不留固定空白条。
                            bottom = 24.dp +
                                WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding(),
                        ),
                ) {
                    if (state is LoadingState.Success) {
                        if (original != null) {
                            item(key = "originalLabel") { ThreadSectionLabel(stringResource(R.string.feed_original_comment)) }
                            item(key = "original") {
                                CompositionLocalProvider(
                                    LocalReplyThreadCorners provides ReplyThreadCorners(true, true),
                                ) {
                                    replyContent(original, 0)
                                }
                            }
                        }
                        if (replies.isNotEmpty()) {
                            item(key = "replyLabel") { ThreadSectionLabel(stringResource(R.string.feed_reply), original != null) }
                            itemsIndexed(
                                replies,
                                key = { index, item -> "reply:${item.id}:$index" },
                            ) { index, reply ->
                                Column {
                                    CompositionLocalProvider(
                                        LocalReplyThreadCorners provides
                                            ReplyThreadCorners(
                                                index == 0,
                                                index == replies.lastIndex,
                                            ),
                                    ) {
                                        replyContent(reply, index + 1)
                                    }
                                    if (index != replies.lastIndex) {
                                        Box(
                                            Modifier.fillMaxWidth()
                                                .background(
                                                    MiuixTheme.colorScheme.surfaceContainer,
                                                ),
                                        ) {
                                            HorizontalDivider(
                                                Modifier.padding(start = 56.dp, end = 16.dp),
                                            )
                                        }
                                    }
                                }
                                if (index == replies.lastIndex && !isEnd) {
                                    LaunchedEffect(replies.size) { onLoadMore() }
                                }
                            }
                        } else if (!isEnd) {
                            item(key = "loadReplies") { LaunchedEffect(all.size) { onLoadMore() } }
                        }
                    } else {
                        item(key = "state") {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                LoadingCard(
                                    state = state,
                                    onClick =
                                        if (state is LoadingState.Loading) null else onLoadMore,
                                )
                            }
                        }
                    }
                    FooterCard(footerState = footerState, loadMore = onLoadMore, isFeed = true)
                }
            }
        }
    }
}

@Composable
private fun ThreadSectionLabel(title: String, afterOriginal: Boolean = false) {
    Text(
        title,
        modifier =
            Modifier.padding(
                start = 4.dp,
                top = if (afterOriginal) 12.dp else 4.dp,
                bottom = 6.dp,
            ),
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
