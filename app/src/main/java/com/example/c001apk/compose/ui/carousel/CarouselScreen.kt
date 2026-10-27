package com.example.c001apk.compose.ui.carousel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.TopicBean
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.CommonScreen
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/11 */
@Composable
fun CarouselScreen(
    onBackClick: () -> Unit,
    url: String,
    title: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val viewModel =
        hiltViewModel<CarouselViewModel, CarouselViewModel.ViewModelFactory>(key = url + title) {
            factory ->
            factory.create(isInit = true, url = url, title = title)
        }

    val scope = rememberCoroutineScope()
    var refreshState by remember { mutableStateOf(false) }

    val tabList =
        (viewModel.loadingState as? LoadingState.Success<List<HomeFeedResponse.Data>>)
            ?.response
            ?.find { it.entityTemplate == "iconTabLinkGridCard" }
            ?.entities
            ?.map { TopicBean(it.url.orEmpty(), it.title.orEmpty()) }
            .orEmpty()
    val pagerState = if (tabList.isNotEmpty()) rememberPagerState { tabList.size } else null

    val context = LocalContext.current
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }

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
                            text = viewModel.pageTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
                if (pagerState != null) {
                    MiuixTabRow(
                        tabs = tabList.map { it.title },
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
        when (viewModel.loadingState) {
            LoadingState.Loading,
            LoadingState.Empty,
            is LoadingState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues)) {
                    LoadingCard(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 10.dp),
                        state = viewModel.loadingState,
                        onClick =
                            if (viewModel.loadingState is LoadingState.Loading) null
                            else viewModel::loadMore,
                    )
                }
            }
            is LoadingState.Success -> {
                if (pagerState == null) {
                    CommonScreen(
                        viewModel = viewModel,
                        refreshState = null,
                        resetRefreshState = {},
                        paddingValues = paddingValues,
                        needTopPadding = true,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText,
                        onReport = onReport,
                    )
                } else {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index
                        ->
                        TopBarScrollPage(pagerState.currentPage == index) {
                            CarouselContentScreen(
                                url = tabList[index].url,
                                title = tabList[index].title,
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
                    }
                }
            }
        }
    }
}
