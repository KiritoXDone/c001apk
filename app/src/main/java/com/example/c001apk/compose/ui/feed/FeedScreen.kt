package com.example.c001apk.compose.ui.feed

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.component.ArticleItem
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.FeedListEntry
import com.example.c001apk.compose.ui.component.FooterCard
import com.example.c001apk.compose.ui.component.ItemCard
import com.example.c001apk.compose.ui.component.LocalMarkdownRaw
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixBottomSheet as ModalBottomSheet
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.cards.FeedCard
import com.example.c001apk.compose.ui.component.cards.FeedReplyCard
import com.example.c001apk.compose.ui.component.cards.FeedReplySortCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.getAllLinkAndText
import com.example.c001apk.compose.util.isScrollingUp
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Reply
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/4 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    isCompat: Boolean = true,
    onBackClick: () -> Unit,
    id: String,
    isViewReply: Boolean,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val viewModel =
        hiltViewModel<FeedViewModel, FeedViewModel.ViewModelFactory>(
            key = if (isCompat) id else "KEY",
        ) { factory ->
            factory.create(id, isViewReply)
        }

    var rawMarkdown by rememberSaveable(id) { mutableStateOf(false) }
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val lazyListState = rememberLazyListState()
    var dropdownMenuExpanded by remember(id) { mutableStateOf(false) }
    var confirmDelete by remember(id) { mutableStateOf(false) }
    val ownFeed =
        isLogin && (viewModel.feedState as? LoadingState.Success)?.response?.uid == CookieUtil.uid
    val localFavoriteClick = rememberHapticClick {
        dropdownMenuExpanded = false
        viewModel.toggleLocalFavorite()
    }
    LaunchedEffect(viewModel.deletedFeedId) {
        if (viewModel.deletedFeedId == id) {
            confirmDelete = false
            viewModel.consumeFeedDeletion()
            onBackClick()
        }
    }

    val scope = rememberCoroutineScope()
    var openBottomSheet by remember { mutableStateOf(false) }
    var viewReply by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val shouldShowSortCard by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > viewModel.itemSize - 1 }
    }

    fun onShowTotalReply(id: String, uid: String, frid: String?) {
        openBottomSheet = true
        viewModel.replyId = id
        viewModel.replyUid = uid
        viewModel.frid = frid
        viewModel.resetReplyState()
        viewModel.fetchTotalReply()
    }

    fun setReplyType(index: Int) {
        selected = index
        viewModel.listType =
            when (index) {
                0 -> "lastupdate_desc"
                1 -> "dateline_desc"
                2 -> "popular"
                else -> EMPTY_STRING
            }
        viewModel.fromFeedAuthor = if (index == 3) 1 else 0
    }

    LaunchedEffect(id) {
        if (id != viewModel.id) {
            viewReply = false
            setReplyType(0)
            viewModel.refresh(id, isViewReply)
        }
    }

    val articleList = remember(key1 = viewModel.feedState) { viewModel.articleList }

    val replyLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data =
                    result.data?.getParcelableExtraCompat<HomeFeedResponse.Data>("response_data")
                data?.let {
                    viewModel.updateReply(it)
                    context.makeToast("回复成功")
                    if (viewModel.replyType == "feed" && shouldShowSortCard) {
                        scope.launch {
                            lazyListState.scrollToItem(viewModel.itemSize)
                        }
                    }
                }
            }
        }

    fun launchReply() {
        val intent = Intent(context, ReplyActivity::class.java)
        intent.putExtra("type", viewModel.replyType)
        intent.putExtra("rid", viewModel.replyId)
        intent.putExtra("username", viewModel.replyName)
        val options =
            ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.anim_bottom_sheet_slide_up,
                R.anim.anim_bottom_sheet_slide_down,
            )
        replyLauncher.launch(intent, options)
    }

    CompositionLocalProvider(
        LocalMarkdownRaw provides rawMarkdown,
        com.example.c001apk.compose.ui.component.cards.LocalDetailComments provides true,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val detailInset = ((maxWidth - 800.dp) / 2).coerceAtLeast(12.dp)
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    FeedTopBar(
                        onTitleClick = {
                            scope.launch {
                                lazyListState.scrollToItem(
                                    if (shouldShowSortCard || viewReply) {
                                        viewReply = false
                                        0
                                    } else {
                                        viewReply = true
                                        viewModel.itemSize
                                    },
                                )
                            }
                        },
                        windowInsets =
                            WindowInsets.systemBars.only(
                                WindowInsetsSides.Top +
                                    if (isCompat) WindowInsetsSides.Start
                                    else WindowInsetsSides.End,
                            ),
                        navigationIcon = {
                            BackButton {
                                if (!isCompat) {
                                    viewModel.resetState()
                                }
                                onBackClick()
                            }
                        },
                        title =
                            viewModel.feedTypeName.ifBlank { "动态" } +
                                if (
                                    (viewModel.feedState as? LoadingState.Success)?.isPublic == true
                                )
                                    " · 公开内容"
                                else "",
                        actions = {
                            if (viewModel.feedState is LoadingState.Success) {
                                FeedExportAction(
                                    (viewModel.feedState as LoadingState.Success).response,
                                    articleList,
                                )
                                Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                                    MoreMenuButton { dropdownMenuExpanded = true }
                                    DropdownMenu(
                                        expanded = dropdownMenuExpanded,
                                        onDismissRequest = { dropdownMenuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(if (rawMarkdown) "格式化正文" else "查看原文") },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                rawMarkdown = !rawMarkdown
                                            },
                                        )
                                        com.example.c001apk.compose.ui.component
                                            .ContentShareMenuItems(
                                                type = ShareType.FEED,
                                                id = id,
                                                title =
                                                    (viewModel.feedState as LoadingState.Success)
                                                        .response
                                                        .messageTitle,
                                                message =
                                                    (viewModel.feedState as LoadingState.Success)
                                                        .response
                                                        .message,
                                                dismiss = { dropdownMenuExpanded = false },
                                            )
                                        DropdownMenuItem(
                                            text = { Text("收藏") },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                viewModel.collections.open(id)
                                            },
                                            onLongClick = localFavoriteClick,
                                            onLongClickLabel =
                                                if (viewModel.isFav) "取消本地收藏" else "保存到本地收藏",
                                        )
                                        if (ownFeed) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        "删除",
                                                        color = MiuixTheme.colorScheme.error,
                                                    )
                                                },
                                                enabled = !viewModel.deletingFeed,
                                                onClick = {
                                                    dropdownMenuExpanded = false
                                                    confirmDelete = true
                                                },
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (viewModel.isBlocked) {
                                                            stringResource(
                                                                id = R.string.menu_unblock,
                                                            )
                                                        } else {
                                                            "拉黑"
                                                        },
                                                    )
                                                },
                                                onClick = {
                                                    dropdownMenuExpanded = false
                                                    viewModel.blockUser()
                                                },
                                            )
                                            if (isLogin) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            stringResource(
                                                                id = R.string.menu_report,
                                                            ),
                                                        )
                                                    },
                                                    onClick = {
                                                        dropdownMenuExpanded = false
                                                        onReport(id, ReportType.FEED)
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
                floatingActionButton = {
                    if (isLogin && viewModel.feedState is LoadingState.Success) {
                        AnimatedVisibility(
                            modifier =
                                if (!isCompat) Modifier.navigationBarsPadding() else Modifier,
                            visible = lazyListState.isScrollingUp(),
                            enter = slideInVertically { it * 2 },
                            exit = slideOutVertically { it * 2 },
                        ) {
                            FloatingActionButton(
                                onClick =
                                    rememberHapticClick {
                                        viewModel.replyId = viewModel.id
                                        viewModel.replyUid = viewModel.feedUid
                                        viewModel.replyName = viewModel.feedUsername
                                        viewModel.replyType = "feed"
                                        launchReply()
                                    },
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Reply,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
            ) { paddingValues ->
                top.yukonga.miuix.kmp.basic.PullToRefresh(
                    refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
                    modifier =
                        Modifier.padding(
                            end =
                                if (isCompat) 0.dp
                                else paddingValues.calculateRightPadding(layoutDirection),
                            start =
                                if (isCompat) paddingValues.calculateLeftPadding(layoutDirection)
                                else 0.dp,
                        ),
                    contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                    isRefreshing = viewModel.isRefreshing,
                    onRefresh = {
                        viewModel.isPull = true
                        viewModel.refresh()
                    },
                ) {
                    TopBarScrollEffect {
                        viewReply = false
                        lazyListState.animateScrollToItem(0)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                start = detailInset,
                                end = detailInset,
                                top = paddingValues.calculateTopPadding() + 12.dp,
                                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                            ),
                        state = lazyListState,
                    ) {
                        when (viewModel.feedState) {
                            LoadingState.Loading,
                            LoadingState.Empty,
                            is LoadingState.Error -> {
                                item(key = "feedState") {
                                    Box(modifier = Modifier.fillParentMaxSize()) {
                                        LoadingCard(
                                            modifier = Modifier.align(Alignment.Center),
                                            state = viewModel.feedState,
                                            onClick =
                                                if (viewModel.feedState is LoadingState.Loading)
                                                    null
                                                else viewModel::refresh,
                                            isFeed = true,
                                        )
                                    }
                                }
                            }

                            is LoadingState.Success -> {
                                val response =
                                    (viewModel.feedState as LoadingState.Success).response
                                if (!articleList.isNullOrEmpty()) {
                                    ArticleItem(
                                        response = response,
                                        articleList = articleList,
                                        onOpenLink = onOpenLink,
                                        onCopyText = onCopyText,
                                        onLike = {
                                            if (isLogin) {
                                                viewModel.onLike(
                                                    response.id.orEmpty(),
                                                    response.userAction?.like ?: 0,
                                                    LikeType.FEED,
                                                )
                                            }
                                        },
                                        onViewUser = onViewUser,
                                        onComments = {
                                            scope.launch {
                                                lazyListState.animateScrollToItem(
                                                    viewModel.itemSize,
                                                )
                                            }
                                        },
                                    )
                                } else {
                                    item(key = "header") {
                                        FeedDetailAuthor(data = response, onViewUser = onViewUser)
                                    }
                                    item(key = "feed") {
                                        FeedCard(
                                            isFeedContent = true,
                                            showTradeLinkHint = !com.example.c001apk.compose.logic.providable.LocalUserPreferences.current.hideTradeLinkHint,
                                            onDismissTradeLinkHint = viewModel::dismissTradeLinkHint,
                                            data = response,
                                            onViewUser = onViewUser,
                                            onViewFeed = { targetId, replies ->
                                                if (targetId == id && replies) {
                                                    scope.launch {
                                                        lazyListState.animateScrollToItem(
                                                            viewModel.itemSize,
                                                        )
                                                    }
                                                } else onViewFeed(targetId, replies)
                                            },
                                            onOpenLink = onOpenLink,
                                            onCopyText = onCopyText,
                                            onReport = onReport,
                                            onLike = viewModel::onLike,
                                            onDelete = { id, deleteType, frid ->
                                                viewModel.frid = frid
                                                viewModel.onDelete(id, deleteType)
                                            },
                                            onBlockUser = {},
                                        )
                                    }
                                }

                                stickyHeader(key = "sort") {
                                    FeedReplySortCard(
                                        replyCount = viewModel.replyCount,
                                        selected = selected,
                                        updateSortReply = { index ->
                                            setReplyType(index)
                                            if (shouldShowSortCard) viewModel.isViewReply = true
                                            viewModel.refresh()
                                        },
                                    )
                                }

                                if (viewModel.listType == "lastupdate_desc") {
                                    if (!response.topReplyRows.isNullOrEmpty()) {
                                        response.topReplyRows?.getOrNull(0)?.let { reply ->
                                            item(key = "topReplyRows") {
                                                FeedReplyCard(
                                                    data = reply,
                                                    onViewUser = onViewUser,
                                                    onShowTotalReply = ::onShowTotalReply,
                                                    onOpenLink = onOpenLink,
                                                    onCopyText = onCopyText,
                                                    onReport = onReport,
                                                    onLike = viewModel::onLike,
                                                    onDelete = { id, deleteType, frid ->
                                                        viewModel.frid = frid
                                                        viewModel.onDelete(id, deleteType)
                                                    },
                                                    onBlockUser = { uid, frid ->
                                                        viewModel.frid = frid
                                                        viewModel.onBlockUser(uid)
                                                    },
                                                    onReply = { rid, uid, username, frid ->
                                                        viewModel.replyId = rid
                                                        viewModel.replyUid = uid
                                                        viewModel.replyName = username
                                                        viewModel.frid = frid
                                                        viewModel.replyType = "reply"
                                                        launchReply()
                                                    },
                                                )
                                            }
                                        }
                                    }

                                    if (!response.replyMeRows.isNullOrEmpty()) {
                                        response.replyMeRows?.getOrNull(0)?.let { reply ->
                                            item(key = "replyMeRows") {
                                                FeedReplyCard(
                                                    data = reply,
                                                    onViewUser = onViewUser,
                                                    onShowTotalReply = ::onShowTotalReply,
                                                    onOpenLink = onOpenLink,
                                                    onCopyText = onCopyText,
                                                    onReport = onReport,
                                                    onLike = viewModel::onLike,
                                                    onDelete = { id, deleteType, frid ->
                                                        viewModel.frid = frid
                                                        viewModel.onDelete(id, deleteType)
                                                    },
                                                    onBlockUser = { uid, frid ->
                                                        viewModel.frid = frid
                                                        viewModel.onBlockUser(uid)
                                                    },
                                                    onReply = { rid, uid, username, frid ->
                                                        viewModel.replyId = rid
                                                        viewModel.replyUid = uid
                                                        viewModel.replyName = username
                                                        viewModel.frid = frid
                                                        viewModel.replyType = "reply"
                                                        launchReply()
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                                if (viewModel.isViewReply) {
                                    viewModel.isViewReply = false
                                    scope.launch {
                                        lazyListState.scrollToItem(viewModel.itemSize)
                                    }
                                }
                            }
                        }

                        if (viewModel.feedState is LoadingState.Success) {

                            ItemCard(
                                loadingState = viewModel.loadingState,
                                loadMore = viewModel::loadMore,
                                isEnd = viewModel.isEnd,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onShowTotalReply = ::onShowTotalReply,
                                onReport = onReport,
                                onLike = viewModel::onLike,
                                onDelete = { id, deleteType, frid ->
                                    viewModel.frid = frid
                                    viewModel.onDelete(id, deleteType)
                                },
                                onBlockUser = { uid, frid ->
                                    viewModel.frid = frid
                                    viewModel.onBlockUser(uid)
                                },
                                onFollowUser = viewModel::onFollowUser,
                                onReply = { rid, uid, username, frid ->
                                    viewModel.replyId = rid
                                    viewModel.replyUid = uid
                                    viewModel.replyName = username
                                    viewModel.frid = frid
                                    viewModel.replyType = "reply"
                                    launchReply()
                                },
                            )

                            FooterCard(
                                footerState = viewModel.footerState,
                                loadMore = viewModel::loadMore,
                                isFeed = true,
                            )
                        }
                    }
                }
            }

            fun resetBottomSheet() {
                openBottomSheet = false
                viewModel.resetReplyState()
            }

            if (openBottomSheet) {
                val topInsetPx = WindowInsets.systemBars.getTop(LocalDensity.current)
                val topInset = with(LocalDensity.current) { topInsetPx.toDp() }

                ModalBottomSheet(
                    onDismissRequest = {
                        resetBottomSheet()
                    },
                    modifier = Modifier.padding(top = topInset),
                ) {
                    ReplyThreadContent(
                        state = viewModel.replyLoadingState,
                        rootId = viewModel.replyRootId,
                        threadKey = viewModel.replyId,
                        footerState = viewModel.replyFooterState,
                        isEnd = viewModel.isEndReply,
                        onLoadMore = viewModel::loadMoreReply,
                        onDismiss = ::resetBottomSheet,
                    ) { reply, index ->
                        FeedListEntry(
                            item = reply,
                            index = index,
                            onViewUser = { uid ->
                                resetBottomSheet()
                                onViewUser(uid)
                            },
                            onViewFeed = { id, isViewReply ->
                                resetBottomSheet()
                                onViewFeed(id, isViewReply)
                            },
                            onOpenLink = { url, title ->
                                resetBottomSheet()
                                onOpenLink(url, title)
                            },
                            onCopyText = {
                                context.copyText(it?.getAllLinkAndText?.richToString())
                            },
                            onReport = { id, type ->
                                resetBottomSheet()
                                onReport(id, type)
                            },
                            isTotalReply = true,
                            onLike = { id, like, likeType ->
                                viewModel.onLikeReply(id, like, likeType)
                            },
                            onDelete = { id, deleteType, _ ->
                                viewModel.onDeleteRely(id, deleteType)
                            },
                            onBlockUser = { uid, _ ->
                                viewModel.onBlockReplyUser(uid)
                            },
                            isReply2Reply = !viewModel.frid.isNullOrEmpty(),
                            onShowTotalReply = { id, uid, frid ->
                                viewModel.resetReplyState()
                                viewModel.replyId = id
                                viewModel.replyUid = uid
                                viewModel.frid = frid
                                viewModel.fetchTotalReply()
                            },
                            onReply = { rid, uid, username, _ ->
                                viewModel.isSheet = true
                                viewModel.replyId = rid
                                viewModel.replyUid = uid
                                viewModel.replyName = username
                                viewModel.frid = null
                                viewModel.replyType = "reply"
                                launchReply()
                            },
                        )
                    }
                }
            }
        }
    }
    FeedCollectionDialog(viewModel.collections)
    if (confirmDelete && ownFeed) {
        MiuixAlertDialog(
            onDismissRequest = { if (!viewModel.deletingFeed) confirmDelete = false },
            title = { Text("删除动态") },
            text = { Text("确定删除这条动态吗？删除后无法恢复。") },
            dismissButton = {
                MiuixDialogButton(
                    onClick = { confirmDelete = false },
                    enabled = !viewModel.deletingFeed,
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                MiuixDialogButton(
                    onClick = viewModel::deleteOwnFeed,
                    enabled = !viewModel.deletingFeed,
                ) {
                    Text(if (viewModel.deletingFeed) "删除中…" else "删除")
                }
            },
        )
    }
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }
}

private inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    return if (SDK_INT >= 33) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION") getParcelableExtra(key)
    }
}
