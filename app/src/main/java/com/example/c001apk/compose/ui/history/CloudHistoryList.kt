package com.example.c001apk.compose.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.pullToRefreshTexts
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.GroupedListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun CloudHistoryList(padding: PaddingValues, onOpenLink: (String, String?) -> Unit) {
    val model = hiltViewModel<CloudHistoryViewModel>()
    val prefs = LocalUserPreferences.current
    val state = rememberLazyListState()
    val nowMillis = System.currentTimeMillis()
    val sections = remember(model.rows, nowMillis / 60000) { cloudHistorySections(model.rows, nowMillis) }
    TopBarScrollEffect { state.animateScrollToItem(0) }
    LaunchedEffect(prefs.accountRevision) { state.scrollToItem(0) }
    if (!prefs.isLogin || model.accountRevision != prefs.accountRevision) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text(stringResource(if (!prefs.isLogin) R.string.history_cloud_login else R.string.status_loading),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        return
    }
    PullToRefresh(
        isRefreshing = model.refreshing,
        onRefresh = model::refresh,
        refreshTexts = pullToRefreshTexts(),
        contentPadding = PaddingValues(top = padding.calculateTopPadding()),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp),
        ) {
            HistoryTimeSection.entries.forEach { section ->
                val entries = sections[section].orEmpty()
                if (entries.isNotEmpty()) {
                    item(key = "section:${section.name}") {
                        SmallTitle(stringResource(section.titleRes), modifier = Modifier.padding(top = 4.dp))
                    }
                    itemsIndexed(entries, key = { _, entry -> "entry:${entry.id}" }) { index, entry ->
                        GroupedListItem(first = index == 0, last = index == entries.lastIndex, showDivider = false) {
                            CloudHistoryRow(entry, onOpenLink)
                        }
                    }
                }
            }
            item(key = "footer") {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        model.loading -> CircularProgressIndicator(Modifier.size(24.dp))
                        model.error != null -> {
                            Text(model.error.orEmpty(), color = MiuixTheme.colorScheme.error)
                            Button(onClick = model::loadMore) { Text(stringResource(R.string.action_retry)) }
                        }
                        model.rows.isEmpty() -> Text(stringResource(R.string.history_cloud_empty))
                        model.hasMore -> Button(onClick = model::loadMore) { Text(stringResource(R.string.action_load_more)) }
                        else -> Text(stringResource(R.string.history_end), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
                LaunchedEffect(model.rows.lastOrNull()?.id) {
                    if (model.rows.isNotEmpty() && !model.loading && model.error == null && model.hasMore) model.loadMore()
                }
            }
        }
    }
}
