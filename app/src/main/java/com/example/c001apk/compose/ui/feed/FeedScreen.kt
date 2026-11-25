package com.example.c001apk.compose.ui.feed

import com.example.c001apk.compose.ui.contentfilter.rememberContentFilter
import com.example.c001apk.compose.ui.contentfilter.FilterNotice
import com.example.c001apk.compose.ui.contentfilter.ContentFilterViewModel
import com.example.c001apk.compose.ui.component.ReadingSidebarButton
import com.example.c001apk.compose.logic.filter.FilterScope
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.component.ArticleItem
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.FeedListEntry
import com.example.c001apk.compose.ui.component.FooterCard
import com.example.c001apk.compose.ui.component.ItemCard
import com.example.c001apk.compose.ui.component.LocalMarkdownRaw
import com.example.c001apk.compose.ui.component.pullToRefreshTexts
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.FeedCard
import com.example.c001apk.compose.ui.component.cards.FeedReplyCard
import com.example.c001apk.compose.ui.component.cards.FeedReplySortCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.cards.SkeletonCard
import com.example.c001apk.compose.ui.component.cards.SkeletonType
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.ui.feed.reply.VoteCommentTarget
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.launch
import kotlin.math.ceil
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.glass.Reply
import top.yukonga.miuix.kmp.nav.core.LocalNavTransitionScope
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/4 */
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
    onToggleSidebar: (() -> Unit)? = null,
    sidebarHidden: Boolean = false,
) {

    val viewModel =
        hiltViewModel<FeedViewModel, FeedViewModel.ViewModelFactory>(
            key = if (isCompat) id else "KEY",
        ) { factory ->
            factory.create(id)
        }

    var rawMarkdown by rememberSaveable(id) { mutableStateOf(false) }
    val context = LocalContext.current
    val resources = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    val showEntrySkeletons = !LocalNavTransitionScope.current.isRunning
    val lazyListState = key(id) { rememberLazyListState() }
    var initialScrollApplied by rememberSaveable(id) { mutableStateOf(false) }
    var dropdownMenuExpanded by remember(id) { mutableStateOf(false) }
    var menuHeld by remember(id) { mutableStateOf(false) }
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
            viewModel.refresh(id)
        }
    }

    val articleList = remember(key1 = viewModel.feedState) { viewModel.articleList }

    val replyLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { VoteCommentTarget.fromIntent(it) }?.let { target ->
                    viewModel.onVoteCommentPublished(target)
                    context.makeToast(resources.getString(R.string.feed_comment_published))
                    return@rememberLauncherForActivityResult
                }
                val data =
                    result.data?.getParcelableExtraCompat<HomeFeedResponse.Data>("response_data")
                data?.let {
                    viewModel.updateReply(it)
                    context.makeToast(resources.getString(R.string.feed_reply_published))
                    if (viewModel.replyType == "feed" && shouldShowSortCard) {
                        scope.launch {
                            lazyListState.scrollToItem(viewModel.itemSize)
                        }
                    }
                }
            }
        }

    fun launchEditor(intent: Intent) {
        val options =
            ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.anim_bottom_sheet_slide_up,
                R.anim.anim_bottom_sheet_slide_down,
            )
        replyLauncher.launch(intent, options)
    }

    fun launchReply() {
        launchEditor(
            Intent(context, ReplyActivity::class.java)
                .putExtra("type", viewModel.replyType)
                .putExtra("rid", viewModel.replyId)
                .putExtra("username", viewModel.replyName),
        )
    }

    val filterData = (viewModel.feedState as? LoadingState.Success)?.response
    var contentRevealed by rememberSaveable(id) { mutableStateOf(false) }
    val contentFilter = rememberContentFilter()
    val filterMatch = filterData?.let { contentFilter.match(it, FilterScope.FEED) }
    if (filterMatch != null && !contentRevealed) {
        val filters: ContentFilterViewModel = hiltViewModel()
        Scaffold(topBar = {
            com.example.c001apk.compose.ui.component.MiuixTopBar(
                title = stringResource(R.string.feed_filtered), navigationIcon = { BackButton(onBackClick) },
                actions = { ReadingSidebarButton(sidebarHidden, onToggleSidebar) },
            )
        }) { padding ->
            FilterNotice(
                reason = filterMatch.reason,
                onReveal = { contentRevealed = true },
                onDisable = { filters.disableRule(filterMatch.ruleId) },
                modifier = Modifier.padding(padding),
                error = filters.error,
            )
        }
        return
    }

    val feedReady = filterData != null && viewModel.id == id
    LaunchedEffect(id, feedReady, showEntrySkeletons) {
        if (feedReady && showEntrySkeletons && !initialScrollApplied) {
            // 评论占位先覆盖可视高度，首次定位不受短列表的滚动上限影响。
            lazyListState.requestScrollToItem(if (isViewReply) viewModel.itemSize else 0)
            initialScrollApplied = true
        }
    }

    CompositionLocalProvider(
        LocalMarkdownRaw provides rawMarkdown,
        com.example.c001apk.compose.ui.component.cards.LocalDetailComments provides true,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val detailInset = ((maxWidth - 800.dp) / 2).coerceAtLeast(12.dp)
            val replySkeletonCount = ceil(maxHeight.value / 142f).toInt()
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
                            viewModel.feedTypeName.ifBlank { stringResource(R.string.label_feed) } +
                                if (
                                    (viewModel.feedState as? LoadingState.Success)?.isPublic == true
                                )
                                    stringResource(R.string.feed_public_suffix)
                                else "",
                        actions = {
                            ReadingSidebarButton(sidebarHidden, onToggleSidebar)
                            if (viewModel.feedState is LoadingState.Success) {
                                if (isLogin && filterData?.isVoteFeed != true) {
                                    MiuixToolbarIconButton(
                                        onClick = rememberHapticClick {
                                            viewModel.replyId = viewModel.id
                                            viewModel.replyUid = viewModel.feedUid
                                            viewModel.replyName = viewModel.feedUsername
                                            viewModel.replyType = "feed"
                                            launchReply()
                                        },
                                    ) {
                                        Icon(MiuixIcons.Glass.Reply, contentDescription = stringResource(R.string.feed_reply_action), modifier = Modifier.size(24.dp))
                                    }
                                }
                                val feed = (viewModel.feedState as LoadingState.Success).response
                                val exportModel = hiltViewModel<FeedExportViewModel>(key = "export-${feed.id}")
                                var chooseExport by remember(feed.id) { mutableStateOf(false) }
                                FeedExportDialogs(feed, articleList, exportModel, chooseExport) { chooseExport = it }
                                Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                                    MiuixToolbarIconButton(
                                        onClick = {
                                            menuHeld = true
                                            dropdownMenuExpanded = true
                                        },
                                        holdDownState = menuHeld,
                                    ) {
                                        Icon(MiuixIcons.ExpandMore, contentDescription = stringResource(R.string.update_more), modifier = Modifier.size(24.dp))
                                    }
                                    DropdownMenu(
                                        expanded = dropdownMenuExpanded,
                                        onDismissRequest = { dropdownMenuExpanded = false },
                                        onDismissFinished = { menuHeld = false },
                                    ) {
                                        FeedExportMenuItem(
                                            enabled = !exportModel.busy && !exportModel.awaitingFile,
                                            onDismissMenu = { dropdownMenuExpanded = false },
                                            onChoose = { chooseExport = true },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(if (rawMarkdown) R.string.feed_format_body else R.string.feed_original_body)) },
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
                                            text = { Text(stringResource(if (viewModel.isAccountCollected) R.string.feed_uncollect else R.string.feed_collect)) },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                viewModel.collections.open(id)
                                            },
                                            onLongClick = localFavoriteClick,
                                            onLongClickLabel =
                                                stringResource(if (viewModel.isFav) R.string.feed_remove_local else R.string.feed_save_local),
                                        )
                                        if (ownFeed) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.menu_delete)) },
                                                textColor = MiuixTheme.colorScheme.error,
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
                                                            stringResource(R.string.menu_block)
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
            ) { paddingValues ->
                top.yukonga.miuix.kmp.basic.PullToRefresh(
                    refreshTexts = pullToRefreshTexts(),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = if (isViewReply && feedReady && !initialScrollApplied) 0f else 1f
                            },
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
                            LoadingState.Loading -> {
                                if (showEntrySkeletons) {
                                    // 评论入口保持评论区布局，骨架不抢占原生入场转场。
                                    if (!isViewReply) {
                                        item(key = "feedSkeleton", contentType = "skeleton") {
                                            SkeletonCard(
                                                modifier = Modifier.padding(bottom = 10.dp),
                                                type = SkeletonType.DETAIL,
                                            )
                                        }
                                    } else {
                                        item(key = "sort") {
                                            FeedReplySortCard(
                                                replyCount = viewModel.replyCount,
                                                selected = selected,
                                                enabled = false,
                                                updateSortReply = ::setReplyType,
                                            )
                                        }
                                    }
                                    repeat(replySkeletonCount) { index ->
                                        item(key = "replySkeleton:$index", contentType = "skeleton") {
                                            SkeletonCard(
                                                modifier = Modifier.padding(bottom = 10.dp),
                                                type = SkeletonType.REPLY,
                                            )
                                        }
                                    }
                                }
                            }
                            LoadingState.Empty,
                            is LoadingState.Error -> {
                                item(key = "feedState") {
                                    Box(modifier = Modifier.fillParentMaxSize()) {
                                        LoadingCard(
                                            modifier = Modifier.align(Alignment.Center),
                                            state = viewModel.feedState,
                                            onClick = viewModel::refresh,
                                            isFeed = true,
                                        )
                                    }
                                }
                            }

                            is LoadingState.Success -> {
                                val state = viewModel.feedState as LoadingState.Success
                                val response = state.response
                                val voteContent: (@Composable () -> Unit)? = if (response.isVoteFeed) {
                                    {
                                        val preferences = LocalUserPreferences.current
                                        key(preferences.accountRevision, response.id) {
                                            FeedVoteContent(
                                                vote = response.vote,
                                                title = response.vote?.messageTitle?.takeIf {
                                                    it.isNotBlank() && it != response.messageTitle && it != response.title
                                                },
                                                canVote = preferences.isLogin && !state.isPublic,
                                                submitting = viewModel.submittingVote,
                                                refreshing = viewModel.isRefreshing,
                                                error = viewModel.voteError,
                                                onSubmit = viewModel::submitVote,
                                            )
                                        }
                                    }
                                } else null
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
                                        afterMessage = voteContent,
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
                                            afterMessage = voteContent,
                                        )
                                    }
                                }

                                // 排序随评论区滚动，避免吸顶到状态栏下方并被顶栏重复采样。
                                item(key = "sort") {
                                    if (response.isVoteFeed) {
                                        val preferences = LocalUserPreferences.current
                                        val vote = response.vote
                                        val target = if (preferences.isLogin && !state.isPublic) {
                                            vote?.commentTarget(id, preferences.accountRevision)
                                        } else null
                                        VoteCommentsHeader(
                                            count = viewModel.replyCount,
                                            onWriteComment = target?.let { destination ->
                                                {
                                                    if (vote?.commentTarget(id, destination.accountRevision) == destination) {
                                                        launchEditor(destination.editorIntent(context))
                                                    }
                                                }
                                            },
                                        )
                                    } else FeedReplySortCard(
                                        replyCount = viewModel.replyCount,
                                        selected = selected,
                                        updateSortReply = { index ->
                                            setReplyType(index)
                                            if (shouldShowSortCard) {
                                                lazyListState.requestScrollToItem(viewModel.itemSize)
                                            }
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
                            }
                        }

                        if (viewModel.feedState is LoadingState.Success) {

                            ItemCard(
                                loadingState = viewModel.loadingState,
                                loadMore = viewModel::loadMore,
                                isEnd = viewModel.stopAutomaticLoading,
                                isVoteCommentList = filterData?.isVoteFeed == true,
                                showLoadingSkeletons = showEntrySkeletons,
                                loadingSkeletonCount = replySkeletonCount,
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

            if (openBottomSheet) {
                FeedReplySheet(
                    viewModel = viewModel,
                    onDismiss = { openBottomSheet = false; viewModel.resetReplyState() },
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onReport = onReport,
                    onLaunchReply = ::launchReply,
                )
            }
        }
    }
    FeedCollectionDialog(viewModel.collections)
    if (confirmDelete && ownFeed) {
        MiuixAlertDialog(
            onDismissRequest = { confirmDelete = false },
            dismissEnabled = !viewModel.deletingFeed,
            title = { Text(stringResource(R.string.feed_delete_title)) },
            text = { Text(stringResource(R.string.feed_delete_summary)) },
            dismissButton = {
                MiuixDialogButton(
                    onClick = { confirmDelete = false },
                    enabled = !viewModel.deletingFeed,
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            confirmButton = {
                MiuixDialogButton(
                    onClick = viewModel::deleteOwnFeed,
                    dismissOnClick = false,
                    enabled = !viewModel.deletingFeed,
                    destructive = true,
                ) {
                    Text(stringResource(if (viewModel.deletingFeed) R.string.status_deleting else R.string.menu_delete))
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
