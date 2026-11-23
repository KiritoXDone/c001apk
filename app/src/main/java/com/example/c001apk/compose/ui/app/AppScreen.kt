package com.example.c001apk.compose.ui.app

import android.content.Intent
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.LocalTopBarScroll
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.cards.AppInfoCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.component.topBarTap
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.downloadApk
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ExperimentalToolbarApi
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/10 */
@OptIn(ExperimentalToolbarApi::class)
@Composable
fun AppScreen(
    onBackClick: () -> Unit,
    packageName: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onSearch: (String, String, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val viewModel =
        hiltViewModel<AppViewModel, AppViewModel.ViewModelFactory>(key = packageName) { factory ->
            factory.create(packageName)
        }

    val context = LocalContext.current

    val tabList by lazy { listOf("最近回复", "最新发布", "热度排序") }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var menuHeld by remember { mutableStateOf(false) }

    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = {
                tabList.size
            },
        )
    val scope = rememberCoroutineScope()
    var refreshState by remember { mutableStateOf(false) }

    val state = rememberCollapsingToolbarScaffoldState()
    val headerScrollConnection = rememberAppHeaderScrollConnection(state.toolbarState)

    val windowInsets = WindowInsets.systemBars

    val scrollRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    CompositionLocalProvider(LocalTopBarScroll provides scrollRequests) {
        TopBarScrollEffect {
            var previous = 0f
            animate(0f, (state.toolbarState.maxHeight - state.toolbarState.height).toFloat()) { value, _ ->
                state.toolbarState.dispatchRawDelta(value - previous)
                previous = value
            }
        }
        CollapsingToolbarScaffold(
            state = state,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface),
            toolbar = {
                TopAppBar(
                    modifier = Modifier.topBarTap(),
                    windowInsets =
                        WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                    navigationIcon = {
                        BackButton { onBackClick() }
                    },
                    title = {
                        Text(
                            modifier =
                                Modifier.graphicsLayer {
                                    alpha = if (state.toolbarState.progress == 0f) 1f else 0f
                                },
                            text = viewModel.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        if (viewModel.appState is LoadingState.Success) {
                            Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                                MiuixToolbarIconButton(
                                    onClick = {
                                        onSearch(viewModel.title, "apk", viewModel.id)
                                    },
                                ) {
                                    Icon(MiuixIcons.Basic.Search, contentDescription = "搜索此应用的动态", modifier = Modifier.size(24.dp))
                                }
                                if (isLogin) {
                                    MiuixToolbarIconButton(
                                        onClick = rememberHapticClick {
                                            val intent = Intent(context, ReplyActivity::class.java)
                                            intent.putExtra("type", "createFeed")
                                            intent.putExtra("targetType", "apk")
                                            intent.putExtra(
                                                "targetId",
                                                "${1000000000 + (viewModel.id.toIntOrNull() ?: 4599)}",
                                            )
                                            val animationBundle =
                                                ActivityOptionsCompat.makeCustomAnimation(
                                                    context,
                                                    R.anim.anim_bottom_sheet_slide_up,
                                                    R.anim.anim_bottom_sheet_slide_down,
                                                ).toBundle()
                                            ContextCompat.startActivity(context, intent, animationBundle)
                                        },
                                    ) {
                                        Icon(MiuixIcons.Edit, contentDescription = "发布动态", modifier = Modifier.size(24.dp))
                                    }
                                }
                                Box {
                                    MiuixToolbarIconButton(
                                        onClick = {
                                            menuHeld = true
                                            dropdownMenuExpanded = true
                                        },
                                        holdDownState = menuHeld,
                                    ) {
                                        Icon(MiuixIcons.ExpandMore, contentDescription = "更多操作", modifier = Modifier.size(24.dp))
                                    }
                                    DropdownMenu(
                                        expanded = dropdownMenuExpanded,
                                        onDismissRequest = { dropdownMenuExpanded = false },
                                        onDismissFinished = { menuHeld = false },
                                    ) {
                                        com.example.c001apk.compose.ui.component.ContentShareMenuItems(
                                            type = com.example.c001apk.compose.util.ShareType.APP,
                                            id = packageName,
                                            title =
                                                (viewModel.appState as LoadingState.Success)
                                                    .response
                                                    .title,
                                            dismiss = { dropdownMenuExpanded = false },
                                        )
                                        if (isLogin) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (viewModel.isFollowed) {
                                                            stringResource(id = R.string.menu_unfollow)
                                                        } else {
                                                            stringResource(id = R.string.menu_follow)
                                                        },
                                                    )
                                                },
                                                onClick = {
                                                    dropdownMenuExpanded = false
                                                    viewModel.onGetFollowApk()
                                                },
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (viewModel.isBlocked) {
                                                        stringResource(id = R.string.menu_unblock)
                                                    } else {
                                                        "拉黑"
                                                    },
                                                )
                                            },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                viewModel.blockApp()
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    color = Color.Transparent,
                )
                if (viewModel.appState !is LoadingState.Error) {
                    AppInfoCard(
                        modifier =
                            Modifier.padding(top = 58.dp)
                                .windowInsetsPadding(
                                    windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                                )
                                .parallax(0.5f)
                                .graphicsLayer {
                                    alpha = state.toolbarState.progress
                                },
                        data = (viewModel.appState as? LoadingState.Success)?.response,
                        onDownloadApk = viewModel::onGetDownloadLink,
                    )
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (viewModel.appState) {
                    LoadingState.Loading,
                    LoadingState.Empty,
                    is LoadingState.Error -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LoadingCard(
                                modifier = Modifier.align(Alignment.Center).padding(horizontal = 10.dp),
                                state = viewModel.appState,
                                onClick =
                                    if (viewModel.appState is LoadingState.Loading) null
                                    else viewModel::refresh,
                            )
                        }
                    }

                    is LoadingState.Success -> {

                        if (viewModel.commentStatus == 1) {

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.weight(1f),
                            ) { index ->
                                TopBarScrollPage(pagerState.currentPage == index) {
                                    AppContentScreen(
                                        refreshState = refreshState,
                                        resetRefreshState = {
                                            refreshState = false
                                        },
                                        id = viewModel.id,
                                        appCommentSort =
                                            when (index) {
                                                0 -> EMPTY_STRING
                                                1 -> "&sort=dateline_desc"
                                                2 -> "&sort=popular"
                                                else -> EMPTY_STRING
                                            },
                                        appCommentTitle =
                                            when (index) {
                                                0 -> "最近回复"
                                                1 -> "最新发布"
                                                2 -> "热度排序"
                                                else -> "最近回复"
                                            },
                                        onViewUser = onViewUser,
                                        onViewFeed = onViewFeed,
                                        onOpenLink = onOpenLink,
                                        onCopyText = onCopyText,
                                        onReport = onReport,
                                        listHeader = {
                                            AppSortOptions(
                                                options = tabList,
                                                selectedIndex = pagerState.currentPage,
                                                onSelect = { selectedIndex ->
                                                    if (pagerState.currentPage == selectedIndex) {
                                                        refreshState = true
                                                    }
                                                    scope.launch {
                                                        pagerState.springAnimateToPage(selectedIndex)
                                                    }
                                                },
                                            )
                                        },
                                        listModifier = Modifier.nestedScroll(headerScrollConnection),
                                    )
                                }
                            }
                        } else {
                            HorizontalDivider()
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    modifier = Modifier.padding(16.dp).align(Alignment.Center),
                                    text = viewModel.commentStatusText,
                                )
                            }
                        }
                    }
                }
            }

        }

    }

    when {
        viewModel.downloadApk -> {
            viewModel.reset()
            context.downloadApk(
                viewModel.downloadUrl,
                "${viewModel.title}-${viewModel.versionName}-${viewModel.versionCode}.apk",
            )
        }
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }
}
