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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.ui.carousel.CarouselContentScreen
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.ReportType
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.springAnimateToPage

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
    val followList by lazy { listOf("用户", "话题", "数码", "应用") }
    val replyList by lazy { listOf("我的回复", "我收到的回复") }
    val pagerState = rememberPagerState {
        if (type == FFFListType.FOLLOW.name) followList.size else replyList.size
    }
    val tabList by lazy {
        if (type == FFFListType.FOLLOW.name) followList else replyList
    }
    var refreshState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                                        FFFListType.FEED.name -> "我的动态"

                                        FFFListType.FOLLOW.name -> "我的关注"

                                        FFFListType.USER_FOLLOW.name -> {
                                            if (isMe) "我关注的人" else "TA关注的人"
                                        }

                                        FFFListType.FAN.name -> {
                                            if (isMe) "关注我的人" else "TA的粉丝"
                                        }

                                        FFFListType.RECENT.name -> "我的常去"

                                        FFFListType.LIKE.name -> "我的赞"

                                        FFFListType.REPLY.name -> "我的回复"

                                        FFFListType.COLLECTION.name -> "我的收藏"

                                        else -> EMPTY_STRING
                                    },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
                if (type == FFFListType.FOLLOW.name || type == FFFListType.REPLY.name) {
                    MiuixTabRow(
                        tabs = tabList,
                        selectedTabIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            if (pagerState.currentPage == index) refreshState = true
                            scope.launch { pagerState.springAnimateToPage(index) }
                        },
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
                                        title = if (index == 1) "我关注的话题" else "我关注的数码吧",
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
