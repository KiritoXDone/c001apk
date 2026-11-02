package com.example.c001apk.compose.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.MiuixMoreMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem
import com.example.c001apk.compose.ui.component.AdaptiveCardColumn
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.cards.HistoryCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/17 */
enum class HistoryType {
    FAV,
    HISTORY,
}

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    type: String,
    onViewUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onCopyText: (String?) -> Unit,
) {

    val viewModel =
        hiltViewModel<HistoryViewModel, HistoryViewModel.ViewModelFactory>(key = type) { factory ->
            factory.create(HistoryType.valueOf(type))
        }
    val dataList by viewModel.dataList.collectAsStateWithLifecycle(initialValue = emptyList())

    var cloudSelected by rememberSaveable(type) { mutableStateOf(false) }
    var sourceMenu by remember { mutableStateOf(false) }
    val showCloud = type == HistoryType.HISTORY.name && cloudSelected
    val layoutDirection = LocalLayoutDirection.current
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = { BackButton { onBackClick() } },
                title = when (type) {
                    HistoryType.FAV.name -> "本地收藏"
                    HistoryType.HISTORY.name -> "浏览历史"
                    else -> type
                },
                actions = {
                    AnimatedVisibility(
                        visible = !showCloud && (type == HistoryType.HISTORY.name || dataList.isNotEmpty()),
                        enter = fadeIn(), exit = fadeOut(),
                    ) {
                        IconButton(onClick = { showClearDialog = true }, enabled = dataList.isNotEmpty()) {
                            Icon(MiuixIcons.Delete, contentDescription = "清除本地记录")
                        }
                    }
                    if (type == HistoryType.HISTORY.name) {
                        MiuixMoreMenu(expanded = sourceMenu, onExpandedChange = { sourceMenu = it }) {
                            listOf(false to "本地", true to "云端").forEach { (cloud, label) ->
                                val selected = cloudSelected == cloud
                                MiuixMenuItem(
                                    text = {
                                        Text(if (selected) "$label（当前）" else label,
                                            color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface)
                                    },
                                    onClick = {
                                        cloudSelected = cloud
                                        sourceMenu = false
                                        showClearDialog = false
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (showCloud) {
            CloudHistoryList(paddingValues, onOpenLink)
        } else if (type == HistoryType.HISTORY.name) {
            LocalHistoryList(
                records = dataList, padding = paddingValues,
                onViewFeed = onViewFeed, onViewUser = onViewUser, onCopyText = onCopyText,
                onDelete = viewModel::delete, onBlockUser = viewModel::blockUser, onReport = onReport,
            )
        } else {
            AdaptiveCardColumn(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateLeftPadding(layoutDirection),
                        ),
                contentPadding =
                    PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 10.dp,
                        bottom = 10.dp + paddingValues.calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(dataList, key = { it.id }) {
                    HistoryCard(
                        data = it,
                        onViewUser = onViewUser,
                        onReport = onReport,
                        onDelete = { id ->
                            viewModel.delete(id)
                        },
                        onBlockUser = { uid ->
                            viewModel.blockUser(uid)
                        },
                        onOpenLink = onOpenLink,
                        onViewFeed = onViewFeed,
                        onCopyText = onCopyText,
                    )
                }
            }

            if (dataList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingCard(state = LoadingState.Empty)
                }
            }
        }
    }

    when {
        !showCloud && showClearDialog -> {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                            viewModel.clearAll()
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                title = { Text(if (type == HistoryType.HISTORY.name) "清除全部本地浏览历史？" else "确定清除全部？") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
