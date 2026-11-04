package com.example.c001apk.compose.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.cards.SearchHistoryCard
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/6 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    title: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    val viewModel = hiltViewModel<SearchViewModel>(key = title)
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchExpanded by rememberSaveable(title) { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissInput() {
        searchExpanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    fun cancelSearch() {
        onQueryChange("")
        dismissInput()
    }
    fun onCheckSearch(keyword: String = query) {
        if (keyword.isNotBlank()) {
            onQueryChange(keyword)
            dismissInput()
            viewModel.saveHistory(keyword)
            onSearch(keyword)
        }
    }
    BackHandler(enabled = searchExpanded || query.isNotEmpty()) { cancelSearch() }

    MiuixScaffold(
        topBar = {
            Column {
                MiuixTopBar(title = "搜索", navigationIcon = { BackButton(onBackClick) })
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = { onCheckSearch(it) },
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        interactionSource = interactionSource,
                        label = if (!title.isNullOrEmpty()) stringResource(R.string.search_hint_in, title)
                            else "搜索动态、用户、话题和应用",
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedVisibility(visible = focused || searchExpanded || query.isNotEmpty()) {
                        IconButton(onClick = ::cancelSearch, modifier = Modifier.padding(start = 4.dp)) {
                            Text("取消", color = MiuixTheme.colorScheme.primary,
                                style = MiuixTheme.textStyles.body1)
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberTopBarScrollState())
                .padding(padding).padding(horizontal = 12.dp),
        ) {
            if (searchHistory.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("搜索历史", Modifier.weight(1f).padding(start = 4.dp),
                        style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.SemiBold))
                    IconButton(onClick = { showClearDialog = true }) {
                        Text("清空", color = MiuixTheme.colorScheme.primary,
                            style = MiuixTheme.textStyles.body2)
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                ) {
                    searchHistory.forEach { item ->
                        key(item.data) {
                            SearchHistoryCard(data = item.data,
                                onSearch = { onCheckSearch(item.data) },
                                onDelete = { viewModel.delete(item.data) })
                        }
                    }
                }
            }
        }
    }

    when {
        showClearDialog -> {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                            viewModel.clearAll()
                        },
                    ) {
                        Text("清空")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                        },
                    ) {
                        Text("取消")
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.search_clear_history_confirm),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}
