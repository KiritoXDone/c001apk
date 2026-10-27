package com.example.c001apk.compose.ui.coolpic

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
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.util.ReportType
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/12 */
@Composable
fun CoolPicScreen(
    onBackClick: () -> Unit,
    title: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val tabList = listOf("精选", "热门", "最新")
    val typeList = listOf("recommend", "hot", "newest")
    val pagerState =
        rememberPagerState(
            pageCount = { tabList.size },
        )
    val scope = rememberCoroutineScope()
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
                    title = title,
                )
                MiuixTabRow(
                    tabs = tabList,
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        if (pagerState.currentPage == index) refreshState = true
                        scope.launch { pagerState.springAnimateToPage(index) }
                    },
                )
            }
        },
    ) { paddingValues ->
        HorizontalPager(state = pagerState) { index ->
            TopBarScrollPage(pagerState.currentPage == index) {
                CoolPicContentScreen(
                    title = title,
                    type = typeList[index],
                    refreshState = refreshState,
                    resetRefreshState = { refreshState = false },
                    paddingValues = paddingValues,
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
