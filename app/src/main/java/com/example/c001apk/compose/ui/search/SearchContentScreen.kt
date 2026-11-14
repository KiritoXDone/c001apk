package com.example.c001apk.compose.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.isVisible
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.CommonScreen
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast

/** Created by bggRGjQaUbCoE on 2024/6/9 */
@Composable
fun SearchContentScreen(
    searchType: SearchType,
    keyword: String,
    pageType: String?,
    pageParam: String?,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    feedType: SearchFeedType,
    orderType: SearchOrderType,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    updateInitPage: () -> Unit,
    onReport: (String, ReportType) -> Unit,
    onSelectUser: ((HomeFeedResponse.Data) -> Unit)? = null,
) {

    val view = LocalView.current
    if (view.isVisible) {
        updateInitPage()
    }

    val viewModel =
        hiltViewModel<SearchContentViewModel, SearchContentViewModel.ViewModelFactory>(
            key = keyword + searchType.name,
        ) { factory ->
            factory.create(
                type =
                    when (searchType) {
                        SearchType.FEED -> "feed"
                        SearchType.APP -> "apk"
                        SearchType.GAME -> "game"
                        SearchType.PRODUCT -> "product"
                        SearchType.USER -> "user"
                        SearchType.TOPIC -> "feedTopic"
                    },
                keyword = keyword,
                pageType = pageType,
                pageParam = pageParam,
            )
        }

    if (searchType == SearchType.FEED) {
        LaunchedEffect(feedType, orderType) {
            viewModel.updateFilters(feedType, orderType)
        }
    }

    CommonScreen(
        viewModel = viewModel,
        refreshState = refreshState,
        resetRefreshState = resetRefreshState,
        paddingValues = paddingValues,
        needTopPadding = true,
        onViewUser = { uid ->
            val selected = if (searchType == SearchType.USER && onSelectUser != null && uid.isNotBlank()) {
                (viewModel.loadingState as? LoadingState.Success)?.response?.firstOrNull {
                    it.entityType == "contacts" &&
                        (it.userInfo?.uid ?: it.fUserInfo?.uid.orEmpty()) == uid
                }
            } else null
            if (searchType == SearchType.USER && onSelectUser != null && selected != null) {
                onSelectUser(selected)
            } else {
                onViewUser(uid)
            }
        },
        onViewFeed = onViewFeed,
        onOpenLink = { url, title ->
            val selected = if (searchType == SearchType.USER && onSelectUser != null) {
                (viewModel.loadingState as? LoadingState.Success)?.response?.firstOrNull {
                    it.entityType == "user" && it.url.orEmpty() == url &&
                        (it.username ?: it.userInfo?.username ?: it.fUserInfo?.username) == title
                }
            } else null
            if (searchType == SearchType.USER && onSelectUser != null && selected != null) {
                onSelectUser(selected)
            } else {
                onOpenLink(url, title)
            }
        },
        onCopyText = onCopyText,
        onReport = onReport,
    )

    val context = LocalContext.current
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }
}
