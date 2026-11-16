package com.example.c001apk.compose.ui.topic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CommonScreen
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast

/** Created by bggRGjQaUbCoE on 2024/6/9 */
@Composable
fun TopicContentScreen(
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    entityType: String,
    id: String?,
    url: String,
    title: String,
    productTitle: String,
    productLogo: String?,
    productConfigRows: List<HomeFeedResponse.ProductConfig>,
    productSheetTopPadding: Dp,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val viewModel =
        hiltViewModel<TopicContentViewModel, TopicContentViewModel.ViewModelFactory>(
            key = "$entityType:${id.orEmpty()}:$url:$title",
        ) { factory ->
            factory.create(url, title)
        }

    val comparisonSelections by viewModel.productComparisonSelections.collectAsStateWithLifecycle()
    val selectedComparisonIds = comparisonSelections.mapNotNullTo(linkedSetOf()) { it.config.id }

    val sortOptions = remember(url) { topicSortOptions(url) }
    var sortType by rememberSaveable(entityType, id, url) {
        mutableStateOf(sortOptions.firstOrNull()?.value ?: ProductSortType.REPLY)
    }
    val sortable = sortOptions.isNotEmpty()
    if (sortable) {
        LaunchedEffect(sortType, url) {
            viewModel.changeSort(sortType, sortedTopicUrl(url, sortType), title)
        }
    }

    key(if (sortable) sortType else null) {
        CommonScreen(
            viewModel = viewModel,
            refreshState = refreshState,
            resetRefreshState = resetRefreshState,
            paddingValues = paddingValues,
            needTopPadding = true,
            listTopSpacing = if (sortable) 4.dp else 12.dp,
            listHeader =
                if (sortable) {
                    { TopicSortOptions(sortOptions, sortType) { sortType = it } }
                } else null,
            onViewUser = onViewUser,
            onViewFeed = onViewFeed,
            onOpenLink = onOpenLink,
            onCopyText = onCopyText,
            onReport = onReport,
            productConfigRows = productConfigRows,
            selectedProductConfigIds = selectedComparisonIds,
            onViewProductConfig = viewModel::showProductConfig,
            onToggleProductConfigComparison = {
                viewModel.toggleGlobalProductComparison(it, productTitle, productLogo)
            },
            onShowProductConfigComparison = {
                viewModel.showCurrentProductConfigComparison(
                    productConfigRows,
                    productTitle,
                    productLogo,
                )
            },
            onShowGlobalProductComparison = viewModel::showGlobalProductComparison,
            onToggleProductEntityComparison = { viewModel.toggleGlobalProductComparison(it) },
        )
    }

    ProductConfigSheet(
        state = viewModel.productConfigSheetState,
        topPadding = productSheetTopPadding,
        onDismiss = viewModel::dismissProductConfigSheet,
        onRemoveComparison = viewModel::removeGlobalProductComparison,
    )

    val context = LocalContext.current
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }
}
