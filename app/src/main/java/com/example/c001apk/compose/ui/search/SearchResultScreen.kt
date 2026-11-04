package com.example.c001apk.compose.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.util.ReportType
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/9 */
enum class SearchType {
    FEED,
    APP,
    GAME,
    PRODUCT,
    USER,
    TOPIC,
}

enum class SearchFeedType {
    ALL,
    FEED,
    ARTICLE,
    COOLPIC,
    COMMENT,
    RATING,
    ANSWER,
    QUESTION,
    VOTE,
}

enum class SearchOrderType(val title: String, val sort: String, val isStrict: Boolean = false) {
    COMPREHENSIVE("综合", "none"),
    DATELINE("实时", "dateline"),
    HOT("热度", "hot"),
    REPLY("回复", "reply"),
    EXACT("精确", "none", isStrict = true),
}

private fun searchTypeTitle(type: SearchType): String {
    return when (type) {
        SearchType.FEED -> "动态"
        SearchType.APP -> "应用"
        SearchType.GAME -> "游戏"
        SearchType.PRODUCT -> "设备"
        SearchType.USER -> "用户"
        SearchType.TOPIC -> "话题"
    }
}

private fun searchFeedTitle(type: SearchFeedType): String {
    return when (type) {
        SearchFeedType.ALL -> "全部"
        SearchFeedType.FEED -> "动态"
        SearchFeedType.ARTICLE -> "文章"
        SearchFeedType.COOLPIC -> "酷图"
        SearchFeedType.COMMENT -> "评论"
        SearchFeedType.RATING -> "评分"
        SearchFeedType.ANSWER -> "回答"
        SearchFeedType.QUESTION -> "问题"
        SearchFeedType.VOTE -> "投票"
    }
}

private fun searchOrderTitle(type: SearchOrderType): String = type.title

private fun pageTypeTitle(pageType: String?): String? {
    if (pageType.isNullOrEmpty()) return pageType
    val match = SearchType.entries.firstOrNull { it.name.equals(pageType, ignoreCase = true) }
    return match?.let { searchTypeTitle(it) } ?: pageType
}

@Composable
fun SearchResultScreen(
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    keyword: String,
    title: String?,
    pageType: String?,
    pageParam: String?,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    initialPage: Int = 0,
    updateInitPage: (Int) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {
    var query by rememberSaveable(keyword) { mutableStateOf(keyword) }
    var submittedKeyword by rememberSaveable(keyword) { mutableStateOf(keyword) }
    val searchViewModel = hiltViewModel<SearchViewModel>()
    val tabList = SearchType.entries
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { if (pageType.isNullOrEmpty()) tabList.size else 1 },
        )
    var refreshState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var feedType by rememberSaveable { mutableStateOf(SearchFeedType.ALL) }
    var orderType by rememberSaveable { mutableStateOf(SearchOrderType.COMPREHENSIVE) }
    val filterMenu =
        remember(feedType, orderType) {
            DropdownEntry(
                items =
                    listOf(
                        DropdownItem(
                            text = "类型",
                            children =
                                SearchFeedType.entries.map { feed ->
                                    DropdownItem(
                                        text = searchFeedTitle(feed),
                                        selected = feedType == feed,
                                        onClick = { feedType = feed },
                                    )
                                },
                        ),
                        DropdownItem(
                            text = "排序",
                            children =
                                SearchOrderType.entries.map { order ->
                                    DropdownItem(
                                        text = searchOrderTitle(order),
                                        selected = orderType == order,
                                        onClick = { orderType = order },
                                    )
                                },
                        ),
                    ),
            )
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                SearchResultHeader(
                    query = query,
                    onQueryChange = { query = it; onQueryChange(it) },
                    onSearch = { value ->
                        query = value
                        onQueryChange(value)
                        refreshState = value == submittedKeyword
                        submittedKeyword = value
                        searchViewModel.saveHistory(value)
                    },
                    onBackClick = onBackClick,
                    subtitle =
                        if (!title.isNullOrEmpty()) "${pageTypeTitle(pageType)}: $title" else "",
                    actions = {
                        AnimatedVisibility(
                            visible = pagerState.currentPage == 0,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            WindowIconCascadingDropdownMenu(entry = filterMenu) {
                                Icon(
                                    imageVector = MiuixIcons.More,
                                    contentDescription = "搜索筛选",
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                        }
                    },
                )
                if (pageType.isNullOrEmpty()) {
                    MiuixTabRow(
                        tabs = tabList.map(::searchTypeTitle),
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
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            key(submittedKeyword) {
                SearchContentScreen(
                    searchType = tabList[index],
                    keyword = submittedKeyword,
                    pageType = pageType,
                    pageParam = pageParam,
                    refreshState = refreshState,
                    resetRefreshState = { refreshState = false },
                    feedType = feedType,
                    orderType = orderType,
                    paddingValues = paddingValues,
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = onCopyText,
                    updateInitPage = { updateInitPage(pagerState.currentPage) },
                    onReport = onReport,
                )
            }
        }
    }
}
