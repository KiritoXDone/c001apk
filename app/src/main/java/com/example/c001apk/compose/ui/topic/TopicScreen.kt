package com.example.c001apk.compose.ui.topic

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.ContentShareMenuItems
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.ShareType
import com.example.c001apk.compose.util.makeToast
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/9 */
@Composable
fun TopicScreen(
    onBackClick: () -> Unit,
    tag: String?,
    id: String?,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onSearch: (String, String, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val viewModel =
        hiltViewModel<TopicViewModel, TopicViewModel.ViewModelFactory>(key = tag ?: id) { factory ->
            factory.create(
                url =
                    when {
                        !tag.isNullOrEmpty() -> "/v6/topic/newTagDetail"
                        !id.isNullOrEmpty() -> "/v6/product/detail"
                        else -> throw IllegalArgumentException("empty param")
                    },
                tag = tag,
                id = id,
            )
        }

    val context = LocalContext.current
    val density = LocalDensity.current
    var titleBarHeight by remember { mutableIntStateOf(0) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val tabList = viewModel.tabList.orEmpty()
    val pagerState =
        if (viewModel.topicState is LoadingState.Success && tabList.isNotEmpty()) {
            rememberPagerState(
                initialPage =
                    tabList.indexOfFirst { it.pageName == viewModel.selectedTab }.coerceAtLeast(0),
                pageCount = { tabList.size },
            )
        } else null
    val scope = rememberCoroutineScope()
    var refreshState by remember { mutableStateOf(false) }
    var isScrollingUp by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    modifier = Modifier.onSizeChanged { titleBarHeight = it.height },
                    windowInsets =
                        WindowInsets.systemBars.only(
                            WindowInsetsSides.Start + WindowInsetsSides.Top,
                        ),
                    navigationIcon = {
                        BackButton { onBackClick() }
                    },
                    title = {
                        Text(
                            text =
                                (viewModel.topicState as? LoadingState.Success)?.response?.title
                                    ?: EMPTY_STRING,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        if (viewModel.topicState is LoadingState.Success) {
                            Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                                IconButton(
                                    onClick = {
                                        onSearch(
                                            viewModel.title,
                                            if (viewModel.entityType == "topic") "tag"
                                            else "product_phone",
                                            if (viewModel.entityType == "topic") viewModel.title
                                            else viewModel.id.orEmpty(),
                                        )
                                    },
                                ) {
                                    Icon(MiuixIcons.Search, contentDescription = null)
                                }
                                Box {
                                    MoreMenuButton { dropdownMenuExpanded = true }
                                    DropdownMenu(
                                        expanded = dropdownMenuExpanded,
                                        onDismissRequest = { dropdownMenuExpanded = false },
                                    ) {
                                        ContentShareMenuItems(
                                            type =
                                                if (viewModel.entityType == "product")
                                                    ShareType.PRODUCT
                                                else ShareType.TOPIC,
                                            id =
                                                if (viewModel.entityType == "product")
                                                    viewModel.id.orEmpty()
                                                else viewModel.title,
                                            title = viewModel.title,
                                            dismiss = { dropdownMenuExpanded = false },
                                        )
                                        if (isLogin) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (viewModel.isFollowed) {
                                                            stringResource(
                                                                id = R.string.menu_unfollow,
                                                            )
                                                        } else {
                                                            stringResource(
                                                                id = R.string.menu_follow,
                                                            )
                                                        },
                                                    )
                                                },
                                                onClick = {
                                                    dropdownMenuExpanded = false
                                                    if (viewModel.entityType == "topic")
                                                        viewModel.onGetFollow()
                                                    else viewModel.onPostFollow()
                                                },
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (viewModel.isBlocked) {
                                                        stringResource(id = R.string.menu_unblock)
                                                    } else {
                                                        stringResource(id = R.string.menu_block)
                                                    },
                                                )
                                            },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                viewModel.blockTopic()
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    color = Color.Transparent,
                )
                if (pagerState != null && tabList.size > 1) {
                    MiuixTabRow(
                        tabs = tabList.map { it.title.orEmpty() },
                        selectedTabIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            if (pagerState.currentPage == index) refreshState = true
                            scope.launch { pagerState.springAnimateToPage(index) }
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            if (isLogin) {
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = slideInVertically { it * 2 },
                    exit = slideOutVertically { it * 2 },
                ) {
                    FloatingActionButton(
                        onClick =
                            rememberHapticClick {
                                val intent = Intent(context, ReplyActivity::class.java)
                                intent.putExtra("type", "createFeed")
                                intent.putExtra(
                                    "targetType",
                                    if (viewModel.entityType == "topic") "tag" else "product_phone",
                                )
                                intent.putExtra("targetId", viewModel.id)
                                if (viewModel.entityType == "topic")
                                    intent.putExtra("title", viewModel.title)
                                val animationBundle =
                                    ActivityOptionsCompat.makeCustomAnimation(
                                            context,
                                            R.anim.anim_bottom_sheet_slide_up,
                                            R.anim.anim_bottom_sheet_slide_down,
                                        )
                                        .toBundle()
                                ContextCompat.startActivity(context, intent, animationBundle)
                            },
                    ) {
                        Icon(
                            painter =
                                rememberDrawablePainter(
                                    ResourcesCompat.getDrawable(
                                        context.resources,
                                        R.drawable.outline_note_alt_24,
                                        context.theme,
                                    ),
                                ),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            when (viewModel.topicState) {
                LoadingState.Loading,
                LoadingState.Empty,
                is LoadingState.Error -> {
                    Box(Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                        LoadingCard(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 10.dp),
                            state = viewModel.topicState,
                            onClick =
                                if (viewModel.topicState is LoadingState.Loading) null
                                else viewModel::refresh,
                        )
                    }
                }

                is LoadingState.Success -> {
                    if (pagerState != null) {
                        HorizontalPager(state = pagerState) { index ->
                            TopBarScrollPage(pagerState.currentPage == index) {
                                TopicContentScreen(
                                    refreshState = refreshState,
                                    resetRefreshState = { refreshState = false },
                                    paddingValues = paddingValues,
                                    entityType = viewModel.entityType,
                                    id = viewModel.id,
                                    url = tabList[index].url.orEmpty(),
                                    title = tabList[index].title.orEmpty(),
                                    productTitle = viewModel.title,
                                    productLogo = viewModel.productLogo,
                                    productConfigRows = viewModel.productConfigRows,
                                    productSheetTopPadding = with(density) { titleBarHeight.toDp() },
                                    onViewUser = onViewUser,
                                    onViewFeed = onViewFeed,
                                    onOpenLink = onOpenLink,
                                    onCopyText = onCopyText,
                                    onReport = onReport,
                                    isScrollingUp = { isScrollingUp = it },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }
}
