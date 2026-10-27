package com.example.c001apk.compose.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.FooterCard
import com.example.c001apk.compose.ui.component.ItemCard
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.cards.UserInfoCard
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
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search

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
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets =
                    WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title =
                    if (firstVisibleItemIndex > 0)
                        (viewModel.userState as? LoadingState.Success)?.response?.username.orEmpty()
                    else "用户主页",
                actions = {
                    if (viewModel.userState is LoadingState.Success) {
                        Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(
                                onClick = {
                                    onSearch(viewModel.username, "user", viewModel.uid)
                                },
                            ) {
                                Icon(MiuixIcons.Search, contentDescription = "搜索此用户的动态")
                            }
                            Box {
                                MoreMenuButton { dropdownMenuExpanded = true }
                                DropdownMenu(
                                    expanded = dropdownMenuExpanded,
                                    onDismissRequest = { dropdownMenuExpanded = false },
                                ) {
                                    listOf("复制", "分享", "用户信息").forEachIndexed { index, menu ->
                                        DropdownMenuItem(
                                            text = { Text(menu) },
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
        top.yukonga.miuix.kmp.basic.PullToRefresh(
            refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
            modifier =
                Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                ),
            isRefreshing = viewModel.isRefreshing,
            onRefresh = {
                viewModel.isPull = true
                viewModel.refresh()
            },
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val sideInset = ((maxWidth - 800.dp) / 2).coerceAtLeast(0.dp)
                TopBarScrollEffect { lazyListState.animateScrollToItem(0) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            top = paddingValues.calculateTopPadding() + 12.dp,
                            bottom = 24.dp + paddingValues.calculateBottomPadding(),
                            start = sideInset,
                            end = sideInset,
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                UserInfoCard(
                                    data = (viewModel.userState as LoadingState.Success).response,
                                    onFollow = viewModel::onFollowUser,
                                    onLocalFollow = viewModel::toggleLocalFollow,
                                    onPMUser = onPMUser,
                                    onViewFFFList = onViewFFFList,
                                )
                            }
                        }
                    }

                    if (viewModel.userState is LoadingState.Success) {
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
                            isEnd = viewModel.isEnd,
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
