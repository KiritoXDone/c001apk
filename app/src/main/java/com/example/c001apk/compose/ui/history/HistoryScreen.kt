package com.example.c001apk.compose.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixMoreMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem
import com.example.c001apk.compose.ui.component.AdaptiveCardColumn
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.cards.HistoryCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete

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
                    HistoryType.FAV.name -> stringResource(R.string.profile_local_favorites)
                    HistoryType.HISTORY.name -> stringResource(R.string.profile_history)
                    else -> type
                },
                actions = {
                    AnimatedVisibility(
                        visible = !showCloud && (type == HistoryType.HISTORY.name || dataList.isNotEmpty()),
                        enter = fadeIn(), exit = fadeOut(),
                    ) {
                        MiuixToolbarIconButton(onClick = { showClearDialog = true }, enabled = dataList.isNotEmpty()) {
                            Icon(MiuixIcons.Delete, contentDescription = stringResource(R.string.history_clear))
                        }
                    }
                    if (type == HistoryType.HISTORY.name) {
                        MiuixMoreMenu(expanded = sourceMenu, onExpandedChange = { sourceMenu = it }) {
                            listOf(false to stringResource(R.string.history_local), true to stringResource(R.string.history_cloud)).forEach { (cloud, label) ->
                                val selected = cloudSelected == cloud
                                MiuixMenuItem(
                                    text = { Text(label) },
                                    isSelected = selected,
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
                    MiuixDialogButton(
                        destructive = true,
                        onClick = {
                            showClearDialog = false
                            viewModel.clearAll()
                        },
                    ) {
                        Text(stringResource(R.string.action_clear))
                    }
                },
                dismissButton = {
                    MiuixDialogButton(
                        onClick = {
                            showClearDialog = false
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                title = { Text(stringResource(if (type == HistoryType.HISTORY.name) R.string.history_clear_title else R.string.favorites_clear_title)) },
                text = {
                    Text(stringResource(if (type == HistoryType.HISTORY.name) R.string.history_clear_summary else R.string.favorites_clear_summary))
                },
            )
        }
    }
}
