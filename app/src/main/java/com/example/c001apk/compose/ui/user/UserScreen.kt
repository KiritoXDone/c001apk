package com.example.c001apk.compose.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.FooterCard
import com.example.c001apk.compose.ui.component.ItemCard
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.cards.UserProfileHeader
import com.example.c001apk.compose.ui.component.cards.UserProfileDefaults
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.DateUtils.timeStamp2Date
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getShareText
import com.example.c001apk.compose.util.makeToast
import com.example.c001apk.compose.util.shareText
import top.yukonga.miuix.kmp.basic.Icon
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton as IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.RefreshState
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.basic.TopAppBarTitleAlignment
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteCopyLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDownLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteInformationLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteReportLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteSearchLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteShare2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUserForbidLine

/** Created by bggRGjQaUbCoE on 2024/6/4 */
@Composable
fun UserScreen(
    uid: String,
    onBackClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onSearch: (String, String, String) -> Unit,
    onViewFFFList: (String, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onPMUser: (String, String) -> Unit,
) {

    val layoutDirection = LocalLayoutDirection.current

    val context = LocalContext.current
    val viewModel =
        hiltViewModel<UserViewModel, UserViewModel.ViewModelFactory>(key = uid) { factory ->
            factory.create(uid)
        }
    var showUserInfoDialog by remember { mutableStateOf(false) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var menuHeldDown by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val feedListState = rememberLazyListState()
    val feedRefreshState = rememberPullToRefreshState()
    val profileScrollConnection = remember(lazyListState, feedListState, feedRefreshState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 刷新与回弹交给原生组件，资料滚动只消费空闲状态。
                if (feedRefreshState.refreshState != RefreshState.Idle || viewModel.isRefreshing) return Offset.Zero
                val feedAtTop = feedListState.firstVisibleItemIndex == 0 && feedListState.firstVisibleItemScrollOffset == 0
                return if (available.y < 0f || feedAtTop) {
                    Offset(0f, -lazyListState.dispatchRawDelta(-available.y))
                } else Offset.Zero
            }
        }
    }
    val profile = (viewModel.userState as? LoadingState.Success)?.response
    val colors = MiuixTheme.colorScheme
    val topInset = WindowInsets.statusBars.union(WindowInsets.displayCutout).asPaddingValues().calculateTopPadding()
    val coverHeight = maxOf(UserProfileDefaults.CoverHeight, topInset + 144.dp)
    val collapseDistance = with(LocalDensity.current) {
        (coverHeight - UserProfileDefaults.AvatarOverlap - topInset - 52.dp).coerceAtLeast(1.dp).toPx()
    }
    val topBarProgress by remember(profile != null, collapseDistance) {
        derivedStateOf {
            when {
                profile == null || lazyListState.firstVisibleItemIndex > 0 -> 1f
                else -> (lazyListState.firstVisibleItemScrollOffset / collapseDistance).coerceIn(0f, 1f)
            }
        }
    }
    val foreground = if (topBarProgress < 0.5f) Color.White else colors.onSurface
    val buttonBackground = if (topBarProgress < 0.5f) Color.Black.copy(alpha = 0.24f) else Color.Unspecified
    val view = LocalView.current
    val darkTheme = com.example.c001apk.compose.logic.providable.LocalUserPreferences.current.isDarkMode()
    androidx.lifecycle.compose.LifecycleResumeEffect(topBarProgress < 0.5f, darkTheme) {
        val window = (context as? android.app.Activity)?.window
        val controller = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = topBarProgress >= 0.5f && !darkTheme
        onPauseOrDispose { controller?.isAppearanceLightStatusBars = !darkTheme }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBarColor = colors.surface.copy(alpha = topBarProgress),
        topBar = {
            SmallTopAppBar(
                color = Color.Transparent,
                titleColor = foreground,
                titleAlignment = TopAppBarTitleAlignment.Center,
                consumeBarTaps = false,
                windowInsets =
                    WindowInsets.statusBars.union(WindowInsets.displayCutout),
                navigationIcon = {
                    IconButton(
                        onClick = com.example.c001apk.compose.util.composeClick { onBackClick() },
                        modifier = Modifier.testTag("user_back"),
                        backgroundColor = buttonBackground,
                    ) {
                        Icon(MingCuteArrowLeftLine, "返回", Modifier.size(24.dp), tint = foreground)
                    }
                },
                title = if (topBarProgress >= 0.8f) profile?.username ?: "用户主页" else "",
                actions = {
                    if (viewModel.userState is LoadingState.Success) {
                        Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(
                                onClick = {
                                    onSearch(viewModel.username, "user", viewModel.uid)
                                },
                                modifier = Modifier.testTag("user_search"),
                                backgroundColor = buttonBackground,
                            ) {
                                Icon(MingCuteSearchLine, "搜索此用户的动态", Modifier.size(24.dp), tint = foreground)
                            }
                            Box {
                                IconButton(
                                    onClick = { dropdownMenuExpanded = true; menuHeldDown = true },
                                    holdDownState = menuHeldDown,
                                    modifier = Modifier.testTag("user_more"),
                                    backgroundColor = buttonBackground,
                                ) {
                                    Icon(MingCuteDownLine, "更多操作", Modifier.size(24.dp), tint = foreground)
                                }
                                DropdownMenu(
                                    expanded = dropdownMenuExpanded,
                                    onDismissRequest = { dropdownMenuExpanded = false },
                                    onDismissFinished = { menuHeldDown = false },
                                    alignment = PopupPositionProvider.Align.End,
                                    contentScale = 1f,
                                ) {
                                    listOf("复制", "分享", "用户信息").forEachIndexed { index, menu ->
                                        DropdownMenuItem(
                                            text = { Text(menu) },
                                            leadingIcon = {
                                                Icon(
                                                    listOf(MingCuteCopyLine, MingCuteShare2Line, MingCuteInformationLine)[index],
                                                    null,
                                                    Modifier.size(26.dp),
                                                )
                                            },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                when (index) {
                                                    0 ->
                                                        context.copyText(
                                                            getShareText(ShareType.USER, uid),
                                                        )

                                                    1 ->
                                                        context.shareText(
                                                            com.example.c001apk.compose.util
                                                                .ShareContent
                                                                .text(
                                                                    ShareType.USER,
                                                                    uid,
                                                                    title = viewModel.username,
                                                                ),
                                                        )

                                                    2 -> showUserInfoDialog = true
                                                }
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(MingCuteUserForbidLine, null, Modifier.size(26.dp)) },
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
                                            viewModel.onBlockUser(viewModel.uid)
                                        },
                                    )
                                    if (isLogin) {
                                        DropdownMenuItem(
                                            leadingIcon = { Icon(MingCuteReportLine, null, Modifier.size(26.dp)) },
                                            text = {
                                                Text(stringResource(id = R.string.menu_report))
                                            },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                onReport(uid, ReportType.USER)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            Modifier.padding(
                start = paddingValues.calculateLeftPadding(layoutDirection),
                end = paddingValues.calculateRightPadding(layoutDirection),
            ),
        ) {
            val sideInset = ((maxWidth - 800.dp) / 2).coerceAtLeast(0.dp)
            // 外层需能将整段资料移出视口，避免关注按钮停留在顶栏采样区。
            val feedViewportHeight = maxHeight
            TopBarScrollEffect {
                feedListState.requestScrollToItem(0)
                lazyListState.animateScrollToItem(0)
            }
            val navigationInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomSpacing = if (navigationInset > 0.dp) 12.dp else 20.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = if (profile != null) 0.dp else paddingValues.calculateTopPadding() + 10.dp,
                        bottom = if (profile != null) 0.dp else maxOf(navigationInset, paddingValues.calculateBottomPadding()) + bottomSpacing,
                        start = sideInset,
                        end = sideInset,
                    ),
                state = lazyListState,
            ) {
                when (viewModel.userState) {
                    LoadingState.Loading,
                    LoadingState.Empty,
                    is LoadingState.Error -> {
                        item(key = "userState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                LoadingCard(
                                    modifier =
                                        Modifier.align(Alignment.Center)
                                            .padding(horizontal = 10.dp),
                                    state = viewModel.userState,
                                    onClick =
                                        if (viewModel.userState is LoadingState.Loading) null
                                        else viewModel::refresh,
                                )
                            }
                        }
                    }

                    is LoadingState.Success -> {
                        item(key = "userInfo") {
                            UserProfileHeader(
                                data = (viewModel.userState as LoadingState.Success).response,
                                coverHeight = coverHeight,
                                onFollow = viewModel::onFollowUser,
                                onLocalFollow = viewModel::toggleLocalFollow,
                                onPMUser = onPMUser,
                                onViewFFFList = onViewFFFList,
                            )
                        }
                    }
                }

                if (viewModel.userState is LoadingState.Success) {
                    item(key = "userFeed") {
                        top.yukonga.miuix.kmp.basic.PullToRefresh(
                            modifier = Modifier.fillMaxWidth().height(feedViewportHeight)
                                .nestedScroll(profileScrollConnection)
                                .testTag("user_feed_refresh"),
                            refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
                            pullToRefreshState = feedRefreshState,
                            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                            isRefreshing = viewModel.isRefreshing,
                            onRefresh = {
                                viewModel.isPull = true
                                viewModel.refresh()
                            },
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = feedListState,
                                contentPadding = PaddingValues(
                                    top = UserProfileDefaults.SectionSpacing,
                                    bottom = maxOf(navigationInset, paddingValues.calculateBottomPadding()) + bottomSpacing,
                                ),
                                verticalArrangement = Arrangement.spacedBy(UserProfileDefaults.SectionSpacing),
                            ) {
                                item(key = "feedSection") {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "动态",
                                            style =
                                                top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.subtitle,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        )
                                        Text(
                                            "${(viewModel.userState as LoadingState.Success).response.feed?.id ?: 0}",
                                            modifier = Modifier.padding(start = 8.dp),
                                            style =
                                                top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1,
                                            color =
                                                top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
                                                    .onSurfaceVariantSummary,
                                        )
                                    }
                                }
                                ItemCard(
                                    loadingState = viewModel.loadingState,
                                    loadMore = viewModel::loadMore,
                                    isEnd = viewModel.stopAutomaticLoading,
                                    onViewUser = { uid ->
                                        if (uid != viewModel.uid) {
                                            onViewUser(uid)
                                        }
                                    },
                                    onViewFeed = onViewFeed,
                                    onOpenLink = onOpenLink,
                                    onCopyText = onCopyText,
                                    onReport = onReport,
                                    onLike = viewModel::onLike,
                                    onDelete = { id, deleteType, _ ->
                                        viewModel.onDelete(id, deleteType)
                                    },
                                    onBlockUser = { uid, _ ->
                                        viewModel.onBlockUser(uid)
                                    },
                                )

                                FooterCard(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    footerState = viewModel.footerState,
                                    loadMore = viewModel::loadMore,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    when {
        showUserInfoDialog -> {
            val data = (viewModel.userState as LoadingState.Success).response
            AlertDialog(
                onDismissRequest = { showUserInfoDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = { showUserInfoDialog = false },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = data.username.orEmpty(),
                    )
                },
                text = {
                    Text(
                        text =
                            """
                                UID: ${data.uid}
                                
                                等级: Lv.${data.level}
                                
                                性别: ${if (data.gender == 0) "女" else if (data.gender == 1) "男" else "未知"}
                                
                                注册时长: ${((System.currentTimeMillis() / 1000 - (data.regdate ?: 0)) / 24 / 3600)} 天
                                
                                注册时间: ${timeStamp2Date(data.regdate ?: 0)}
                            """
                                .trimIndent(),
                    )
                },
            )
        }
    }

    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }
}
