package com.example.c001apk.compose.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixPagerTabRow
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu

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

private fun searchTypeTitleRes(type: SearchType): Int {
    return when (type) {
        SearchType.FEED -> R.string.label_feed
        SearchType.APP -> R.string.label_app
        SearchType.GAME -> R.string.label_game
        SearchType.PRODUCT -> R.string.label_device
        SearchType.USER -> R.string.label_user
        SearchType.TOPIC -> R.string.label_topic
    }
}

private fun searchFeedTitleRes(type: SearchFeedType): Int {
    return when (type) {
        SearchFeedType.ALL -> R.string.label_all
        SearchFeedType.FEED -> R.string.label_feed
        SearchFeedType.ARTICLE -> R.string.label_article
        SearchFeedType.COOLPIC -> R.string.label_coolpic
        SearchFeedType.COMMENT -> R.string.label_comment
        SearchFeedType.RATING -> R.string.label_rating
        SearchFeedType.ANSWER -> R.string.label_answer
        SearchFeedType.QUESTION -> R.string.label_question
        SearchFeedType.VOTE -> R.string.label_vote
    }
}

private fun searchOrderTitleRes(type: SearchOrderType): Int = when (type) {
    SearchOrderType.COMPREHENSIVE -> R.string.search_order_comprehensive
    SearchOrderType.DATELINE -> R.string.search_order_dateline
    SearchOrderType.HOT -> R.string.search_order_hot
    SearchOrderType.REPLY -> R.string.search_order_reply
    SearchOrderType.EXACT -> R.string.search_order_exact
}

@Composable
private fun pageTypeTitle(pageType: String?): String? {
    if (pageType.isNullOrEmpty()) return pageType
    val match = SearchType.entries.firstOrNull { it.name.equals(pageType, ignoreCase = true) }
    return match?.let { stringResource(searchTypeTitleRes(it)) } ?: pageType
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
    var feedType by rememberSaveable { mutableStateOf(SearchFeedType.ALL) }
    val defaultOrder = LocalUserPreferences.current.defaultSearchOrder
    var orderType by rememberSaveable {
        mutableStateOf(SearchOrderType.entries.firstOrNull { it.name == defaultOrder }
            ?: SearchOrderType.COMPREHENSIVE)
    }
    val filterMenu = DropdownEntry(
        items = listOf(
            DropdownItem(
                text = stringResource(R.string.search_type),
                children = SearchFeedType.entries.map { feed ->
                    DropdownItem(
                        text = stringResource(searchFeedTitleRes(feed)),
                        selected = feedType == feed,
                        onClick = { feedType = feed },
                    )
                },
            ),
            DropdownItem(
                text = stringResource(R.string.menu_sort),
                children = SearchOrderType.entries.map { order ->
                    DropdownItem(
                        text = stringResource(searchOrderTitleRes(order)),
                        selected = orderType == order,
                        onClick = { orderType = order },
                    )
                },
            ),
        ),
    )

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
                                    contentDescription = stringResource(R.string.search_filter),
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                        }
                    },
                )
                if (pageType.isNullOrEmpty()) {
                    MiuixPagerTabRow(
                        tabs = tabList.map { stringResource(searchTypeTitleRes(it)) },
                        pagerState = pagerState,
                        onReselect = { refreshState = true },
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
