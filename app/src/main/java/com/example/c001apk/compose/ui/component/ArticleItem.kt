package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.cards.FeedArticleCard
import com.example.c001apk.compose.ui.component.cards.FeedBottomInfo
import com.example.c001apk.compose.ui.component.cards.FeedRows
import com.example.c001apk.compose.ui.feed.FeedDetailAuthor
import com.example.c001apk.compose.ui.feed.ReadingSurface
import top.yukonga.miuix.kmp.basic.Text

fun LazyListScope.ArticleItem(
    response: HomeFeedResponse.Data,
    articleList: List<FeedArticleContentBean>,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onLike: () -> Unit,
    onViewUser: (String) -> Unit,
    onComments: () -> Unit,
    afterMessage: (@Composable () -> Unit)? = null,
) {
    val hasCover = !response.messageCover.isNullOrEmpty()
    val hasTitle = !response.title.isNullOrEmpty()
    val hasRows = response.targetRow != null || !response.relationRows.isNullOrEmpty()
    item(key = "header") { FeedDetailAuthor(data = response, onViewUser = onViewUser) }
    if (hasCover)
        item(key = "cover") {
            ReadingSurface {
                NineImageView(
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp),
                    pic = null,
                    picArr = listOf(response.messageCover.orEmpty()),
                    feedType = null,
                    isSingle = true,
                )
            }
        }
    if (hasTitle)
        item(key = "title") {
            ReadingSurface {
                Text(
                    response.title.orEmpty(),
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                )
            }
        }
    itemsIndexed(articleList, key = { _, item -> item.key }) { _, item ->
        ReadingSurface {
            FeedArticleCard(item = item, onOpenLink = onOpenLink, onCopyText = onCopyText)
        }
    }
    item(key = "bottom") {
        ReadingSurface(last = !hasRows) {
            if (afterMessage != null) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { afterMessage() }
            }
            FeedBottomInfo(
                modifier =
                    Modifier.padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = if (hasRows) 4.dp else 12.dp),
                isFeedContent = true,
                ip = response.ipLocation.orEmpty(),
                dateline = response.dateline ?: 0,
                replyNum = response.vote?.totalCommentNum?.toString() ?: response.replynum.orEmpty(),
                likeNum = response.likenum.orEmpty(),
                onViewFeed = onComments,
                onLike = onLike,
                like = response.userAction?.like,
            )
        }
    }
    if (hasRows)
        item(key = "rows") {
            ReadingSurface(last = true) {
                FeedRows(
                    isFeedContent = true,
                    relationRows = response.relationRows,
                    targetRow = response.targetRow,
                    onOpenLink = onOpenLink,
                )
            }
        }
}
