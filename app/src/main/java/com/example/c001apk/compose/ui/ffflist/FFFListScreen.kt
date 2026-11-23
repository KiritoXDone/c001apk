package com.example.c001apk.compose.ui.ffflist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.ui.carousel.CarouselContentScreen
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixPagerTabRow
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/12 */
enum class FFFListType {
    FEED,
    FOLLOW,
    APK,
    USER_FOLLOW,
    FAN,
    RECENT,
    LIKE,
    REPLY,
    REPLYME,
    FAV,
    HISTORY,
    COLLECTION,
    COLLECTION_ITEM,
}

@Composable
fun FFFListScreen(
    onBackClick: () -> Unit,
    uid: String?,
    type: String,
    id: String?,
    title: String?,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
) {

    val isMe by lazy { uid == CookieUtil.uid }
    val followList = listOf(R.string.label_user, R.string.label_topic, R.string.label_product, R.string.label_app).map { stringResource(it) }
    val replyList = listOf(R.string.profile_replies, R.string.replies_received).map { stringResource(it) }
    val pagerState = rememberPagerState {
        if (type == FFFListType.FOLLOW.name) followList.size else replyList.size
    }
    val tabList by lazy {
        if (type == FFFListType.FOLLOW.name) followList else replyList
    }
    var refreshState by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    windowInsets =
                        WindowInsets.systemBars.only(
                            WindowInsetsSides.Start + WindowInsetsSides.Top,
                        ),
                    navigationIcon = {
                        BackButton { onBackClick() }
                    },
                    title = {
                        Text(
                            text =
                                title
                                    ?: when (type) {
                                        FFFListType.FEED.name -> stringResource(R.string.profile_posts)

                                        FFFListType.FOLLOW.name -> stringResource(R.string.profile_following)

                                        FFFListType.USER_FOLLOW.name -> {
                                            stringResource(if (isMe) R.string.follow_mine else R.string.follow_theirs)
                                        }

                                        FFFListType.FAN.name -> {
                                            stringResource(if (isMe) R.string.followers_mine else R.string.followers_theirs)
                                        }

                                        FFFListType.RECENT.name -> stringResource(R.string.profile_recent)

                                        FFFListType.LIKE.name -> stringResource(R.string.profile_likes)

                                        FFFListType.REPLY.name -> stringResource(R.string.profile_replies)

                                        FFFListType.COLLECTION.name -> stringResource(R.string.profile_collections)

                                        else -> EMPTY_STRING
                                    },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
                if (type == FFFListType.FOLLOW.name || type == FFFListType.REPLY.name) {
                    MiuixPagerTabRow(
                        tabs = tabList,
                        pagerState = pagerState,
                        onReselect = { refreshState = true },
                    )
                }
            }
        },
    ) { paddingValues ->
        when (type) {
            FFFListType.FOLLOW.name,
            FFFListType.REPLY.name -> {
                HorizontalPager(
                    state = pagerState,
                ) { index ->
                    TopBarScrollPage(pagerState.currentPage == index) {
                        if (type == FFFListType.FOLLOW.name) {
                            when (index) {
                                0,
                                3 ->
                                    FFFContentScreen(
                                        id = id,
                                        uid = uid,
                                        type =
                                            if (index == 0) FFFListType.FOLLOW.name
                                            else FFFListType.APK.name,
                                        paddingValues = paddingValues,
                                        refreshState = refreshState,
                                        resetRefreshState = { refreshState = false },
                                        onViewUser = onViewUser,
                                        onViewFeed = onViewFeed,
                                        onOpenLink = onOpenLink,
                                        onCopyText = onCopyText,
                                        onReport = onReport,
                                        onViewFFFList = onViewFFFList,
                                    )

                                1,
                                2 ->
                                    CarouselContentScreen(
                                        url =
                                            if (index == 1) "#/topic/userFollowTagList"
                                            else "#/product/followProductList",
                                        title = stringResource(if (index == 1) R.string.follow_topics else R.string.follow_products),
                                        paddingValues = paddingValues,
                                        refreshState = refreshState,
                                        resetRefreshState = { refreshState = false },
                                        onViewUser = onViewUser,
                                        onViewFeed = onViewFeed,
                                        onOpenLink = onOpenLink,
                                        onCopyText = onCopyText,
                                        onReport = onReport,
                                    )
                            }
                        } else if (type == FFFListType.REPLY.name) {
                            FFFContentScreen(
                                id = id,
                                uid = uid,
                                type =
                                    if (index == 0) FFFListType.REPLY.name
                                    else FFFListType.REPLYME.name,
                                paddingValues = paddingValues,
                                refreshState = refreshState,
                                resetRefreshState = { refreshState = false },
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onReport = onReport,
                                onViewFFFList = onViewFFFList,
                            )
                        }
                    }
                }
            }

            else -> {
                FFFContentScreen(
                    id = id,
                    uid = uid,
                    type = type,
                    paddingValues = paddingValues,
                    refreshState = null,
                    resetRefreshState = {},
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = onCopyText,
                    onReport = onReport,
                    onViewFFFList = onViewFFFList,
                )
            }
        }
    }
}
