package com.example.c001apk.compose.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import com.example.c001apk.compose.logic.model.FeedEntity
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.GroupedListItem
import com.example.c001apk.compose.ui.component.MiuixMoreMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getShareText
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun LocalHistoryList(
    records: List<FeedEntity>,
    padding: PaddingValues,
    onViewFeed: (String, Boolean) -> Unit,
    onViewUser: (String) -> Unit,
    onCopyText: (String?) -> Unit,
    onDelete: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {
    val entries = remember(records) { records.map { record ->
        CloudHistoryEntry(record.id, "${record.uname}的动态", record.message, record.avatar,
            "/feed/${record.id}", "feed", "动态", record.time.toLongOrNull()?.div(1000))
    } }
    val byId = remember(records) { records.associateBy { it.id } }
    val now = System.currentTimeMillis()
    val sections = remember(entries, now / 60000) { cloudHistorySections(entries, now) }
    val state = rememberLazyListState()
    TopBarScrollEffect { state.animateScrollToItem(0) }
    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            LoadingCard(state = LoadingState.Empty)
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(), state = state,
        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp),
    ) {
        HistoryTimeSection.entries.forEach { section ->
            val rows = sections[section].orEmpty()
            if (rows.isNotEmpty()) {
                item(key = "section:${section.name}") { SmallTitle(section.title, Modifier.padding(top = 4.dp)) }
                itemsIndexed(rows, key = { _, entry -> entry.id!! }) { index, entry ->
                    val record = byId.getValue(entry.id!!)
                    GroupedListItem(first = index == 0, last = index == rows.lastIndex, showDivider = false) {
                        CloudHistoryRow(entry,
                            onOpenLink = { _, _ -> onViewFeed(record.id, false) },
                            onLongClick = { onCopyText(record.message) },
                            onAvatarClick = { onViewUser(record.uid) },
                            endActions = { LocalHistoryMenu(record, onDelete, onBlockUser, onReport) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalHistoryMenu(
    record: FeedEntity,
    onDelete: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {
    var expanded by remember(record.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val loggedIn = LocalUserPreferences.current.isLogin
    MiuixMoreMenu(expanded, { expanded = it }) {
        MiuixMenuItem(text = { Text("复制") }, onClick = {
            expanded = false
            context.copyText(getShareText(ShareType.FEED, record.id))
        })
        MiuixMenuItem(text = { Text("拉黑") }, onClick = { expanded = false; onBlockUser(record.uid) })
        if (loggedIn) MiuixMenuItem(text = { Text("举报") }, onClick = {
            expanded = false; onReport(record.id, ReportType.FEED)
        })
        MiuixMenuItem(text = { Text("删除记录", color = MiuixTheme.colorScheme.error) }, onClick = {
            expanded = false; onDelete(record.id)
        })
    }
}
