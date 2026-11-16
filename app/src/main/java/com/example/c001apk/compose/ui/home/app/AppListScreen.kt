package com.example.c001apk.compose.ui.home.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.AdaptiveCardColumn
import com.example.c001apk.compose.util.longVersionCodeCompat
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator

/** Created by bggRGjQaUbCoE on 2024/6/9 */
@Composable
fun AppListScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(),
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onViewApp: (String) -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onUpdateActionChange: ((() -> Unit)?) -> Unit,
) {

    val viewModel = hiltViewModel<AppListViewModel>()
    val prefs = LocalUserPreferences.current
    val context = LocalContext.current
    val view = LocalView.current
    val lazyListState = rememberLazyStaggeredGridState()
    val currentOnCheckUpdate by rememberUpdatedState(onCheckUpdate)
    val currentOnUpdateActionChange by rememberUpdatedState(onUpdateActionChange)
    val canCheckUpdate = prefs.checkUpdate && viewModel.appList.isNotEmpty()

    DisposableEffect(canCheckUpdate) {
        currentOnUpdateActionChange(
            if (canCheckUpdate) ({ currentOnCheckUpdate(viewModel.dataList) }) else null,
        )
        onDispose { currentOnUpdateActionChange(null) }
    }

    LaunchedEffect(refreshState) {
        if (refreshState) {
            resetRefreshState()
            if (view.isVisible) {
                viewModel.refresh()
                lazyListState.scrollToItem(0)
            }
        }
    }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        top.yukonga.miuix.kmp.basic.PullToRefresh(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = viewModel::refresh,
            contentPadding = paddingValues,
            refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
        ) {
            AdaptiveCardColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 24.dp,
                    ),
            ) {
                items(
                    items = viewModel.appList,
                    key = { it.packageInfo.packageName },
                ) {
                    top.yukonga.miuix.kmp.basic.Card(
                        Modifier.padding(horizontal = 12.dp),
                    ) {
                        top.yukonga.miuix.kmp.basic.BasicComponent(
                            title = it.label,
                            summary =
                                "${it.packageInfo.packageName}\n${it.packageInfo.versionName}(${it.packageInfo.longVersionCodeCompat})",
                            onClick = { onViewApp(it.packageInfo.packageName) },
                            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            startAction = {
                                AsyncImage(
                                    model =
                                        ImageRequest.Builder(context)
                                            .data(it.packageInfo)
                                            .crossfade(true)
                                            .build(),
                                    contentDescription = null,
                                    modifier = Modifier.width(44.dp).height(44.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
