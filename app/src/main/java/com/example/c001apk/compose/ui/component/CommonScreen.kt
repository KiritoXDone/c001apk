package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.base.BaseViewModel
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.isScrollingUp
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** Created by bggRGjQaUbCoE on 2024/6/10 */
@Composable
fun CommonScreen(
    viewModel: BaseViewModel,
    refreshState: Boolean?,
    resetRefreshState: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    needTopPadding: Boolean = false,
    listHeader: (@Composable () -> Unit)? = null,
    listTopSpacing: Dp = 12.dp,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    isHomeFeed: Boolean = false,
    onReport: ((String, ReportType) -> Unit)? = null,
    onViewFFFList: ((String?, String, String?, String?) -> Unit)? = null,
    onHandleRecent: ((String, String, String, Int) -> Unit)? = null,
    onHandleMessage: ((String, Int) -> Unit)? = null,
    onViewChat: ((String, String, String) -> Unit)? = null,
    onDeleteNotice: ((String) -> Unit)? = null,
    isScrollingUp: ((Boolean) -> Unit)? = null,
    productConfigRows: List<HomeFeedResponse.ProductConfig> = emptyList(),
    selectedProductConfigIds: Set<String> = emptySet(),
    onViewProductConfig: ((HomeFeedResponse.ProductConfig) -> Unit)? = null,
    onToggleProductConfigComparison: ((HomeFeedResponse.ProductConfig) -> Unit)? = null,
    onShowProductConfigComparison: (() -> Unit)? = null,
    onShowGlobalProductComparison: (() -> Unit)? = null,
    onToggleProductEntityComparison: ((HomeFeedResponse.Entities) -> Unit)? = null,
    listModifier: Modifier = Modifier,
) {

    val view = LocalView.current
    val layoutDirection = LocalLayoutDirection.current
    val contentTopPadding =
        listTopSpacing +
            LocalTabContentPadding.current.calculateTopPadding() +
            if (needTopPadding) paddingValues.calculateTopPadding() else 0.dp
    val lazyListState = rememberLazyStaggeredGridState()
    TopBarScrollEffect { lazyListState.animateScrollToItem(0) }

    LaunchedEffect(refreshState) {
        if (refreshState == true) {
            resetRefreshState()
            if (view.isVisible) {
                viewModel.refresh()
                lazyListState.scrollToItem(0)
            }
        }
    }

    isScrollingUp?.let {
        it(lazyListState.isScrollingUp())
    }

    PullToRefresh(
        modifier =
            Modifier.padding(
                start = paddingValues.calculateLeftPadding(layoutDirection),
                end = paddingValues.calculateRightPadding(layoutDirection),
            ),
        isRefreshing = viewModel.isRefreshing,
        onRefresh = viewModel::refresh,
        refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
        contentPadding =
            PaddingValues(
                top = contentTopPadding,
            ),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val scale = LocalDensity.current.fontScale.coerceIn(1f, 2f)
            val columns = ReadingLayout.columns(maxWidth.value, scale)
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                modifier = listModifier.fillMaxSize().scrollEndHaptic(),
                contentPadding =
                    PaddingValues(
                        top = contentTopPadding,
                        bottom = 24.dp + paddingValues.calculateBottomPadding(),
                    ),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                state = lazyListState,
            ) {
                if (listHeader != null) {
                    item(key = "listHeader", span = StaggeredGridItemSpan.FullLine) { listHeader() }
                }
                GridItemCard(
                    loadingState = viewModel.loadingState,
                    loadMore = viewModel::loadMore,
                    isEnd = viewModel.isEnd,
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = onCopyText,
                    isHomeFeed = isHomeFeed,
                    onReport = onReport,
                    onViewFFFList = onViewFFFList,
                    onLike = viewModel::onLike,
                    onDelete = { id, deleteType, _ ->
                        viewModel.onDelete(id, deleteType)
                    },
                    onBlockUser = { uid, _ ->
                        viewModel.onBlockUser(uid)
                    },
                    onFollowUser = viewModel::onFollowUser,
                    onHandleRecent = onHandleRecent,
                    onHandleMessage = onHandleMessage,
                    onViewChat = onViewChat,
                    onDeleteNotice = onDeleteNotice,
                    productConfigRows = productConfigRows,
                    selectedProductConfigIds = selectedProductConfigIds,
                    onViewProductConfig = onViewProductConfig,
                    onToggleProductConfigComparison = onToggleProductConfigComparison,
                    onShowProductConfigComparison = onShowProductConfigComparison,
                    onShowGlobalProductComparison = onShowGlobalProductComparison,
                    onToggleProductEntityComparison = onToggleProductEntityComparison,
                )

                item(key = "footer", span = StaggeredGridItemSpan.FullLine) {
                    com.example.c001apk.compose.ui.component.cards.LoadingCard(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        state = viewModel.footerState,
                        onClick =
                            if (
                                viewModel.footerState
                                    is com.example.c001apk.compose.logic.state.FooterState.Error
                            )
                                viewModel::loadMore
                            else null,
                        isFeed = false,
                    )
                }
            }
        }
    }
}
