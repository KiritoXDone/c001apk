package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixActionButton
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.settings.PreferenceLeadingIcon
import com.example.c001apk.compose.ui.component.settings.ReorderablePreferenceGroup
import com.example.c001apk.compose.ui.main.Router
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.R

@Composable
fun NavigationBarOrderScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences = LocalUserPreferences.current
    var order by rememberSaveable(
        stateSaver = listSaver<List<Int>, Int>(save = { it }, restore = { it }),
    ) { mutableStateOf(preferences.mainNavigationOrder) }
    val pages = listOf(Router.HOME, Router.FOLLOW, Router.PROFILE)
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = viewModel.savingNavigationOrder,
        onBackCompleted = {},
    )
    MiuixScaffold(
        topBar = {
            MiuixTopBar(
                title = stringResource(R.string.settings_bar_order),
                navigationIcon = { BackButton { if (!viewModel.savingNavigationOrder) onBackClick() } },
                actions = {
                    MiuixActionButton(
                        enabled = !viewModel.savingNavigationOrder,
                        onClick = { viewModel.setMainNavigationOrder(order, onBackClick) },
                    ) { Text(if (viewModel.savingNavigationOrder) stringResource(R.string.settings_bar_order_saving) else stringResource(R.string.settings_bar_order_done)) }
                },
            )
        },
    ) { padding ->
        val listState = rememberLazyListState()
        TopBarScrollEffect { listState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item {
                ReorderablePreferenceGroup(
                    items = order,
                    key = { it.toString() },
                    onReorder = { order = it },
                    title = stringResource(R.string.settings_bar_order_display),
                    enabled = !viewModel.savingNavigationOrder,
                ) { pageId, dragging, dragHandle ->
                    val page = pages[pageId]
                    BasicComponent(
                        title = stringResource(page.stringId!!),
                        startAction = { PreferenceLeadingIcon(page.unselectedIcon!!) },
                        endActions = { dragHandle() },
                        holdDownState = dragging,
                        enabled = !viewModel.savingNavigationOrder,
                    )
                }
                Text(
                    stringResource(R.string.settings_bar_order_hint),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                viewModel.navigationOrderError?.let {
                    Text(it, Modifier.padding(horizontal = 28.dp, vertical = 8.dp), color = MiuixTheme.colorScheme.error)
                }
            }
        }
    }
}
