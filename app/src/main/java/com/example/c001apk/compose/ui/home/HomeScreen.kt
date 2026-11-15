package com.example.c001apk.compose.ui.home

import com.example.c001apk.compose.logic.providable.LocalUserPreferences

import android.content.Intent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixScaffoldDefaults as ScaffoldDefaults
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.component.settings.ReorderablePreferenceGroup
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.ui.home.app.AppListScreen
import com.example.c001apk.compose.ui.home.feed.HomeFeedScreen
import com.example.c001apk.compose.ui.home.topic.HomeTopicScreen
import com.example.c001apk.compose.util.CookieUtil.isLogin
import com.example.c001apk.compose.util.ReportType
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Created by bggRGjQaUbCoE on 2024/6/5 */
enum class TabType {
    FOLLOW,
    APP,
    FEED,
    HOT,
    TOPIC,
    PRODUCT,
    COOLPIC,
}

val homeTabTypes = TabType.entries.filter { it != TabType.FOLLOW }

private fun tabTitle(type: TabType): String {
    return when (type) {
        TabType.FOLLOW -> "关注"
        TabType.APP -> "应用"
        TabType.FEED -> "动态"
        TabType.HOT -> "热门"
        TabType.TOPIC -> "话题"
        TabType.PRODUCT -> "数码"
        TabType.COOLPIC -> "酷图"
    }
}

@Composable
fun HomeTabEditorScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val storedMenus by viewModel.homeMenus.collectAsStateWithLifecycle(initialValue = emptyList())
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()
    // The draft is the screen's state: Room's first emission fills it once, later emissions must
    // not overwrite edits, and it survives configuration changes without touching the database.
    var draft by rememberSaveable(stateSaver = HomeDraftSaver) {
        mutableStateOf<List<HomeMenu>?>(null)
    }
    LaunchedEffect(storedMenus) {
        if (draft == null && storedMenus.isNotEmpty()) draft = storedMenus
    }
    val menus = draft ?: storedMenus
    val enabledCount = menus.count(HomeMenu::isEnable)
    // An accepted save owns its write until completion; don't pop and cancel the entry's scope.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = saving,
        onBackCompleted = {},
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = "首页板块",
                navigationIcon = {
                    MiuixToolbarIconButton(onClick = { if (!saving) onBackClick() }, enabled = !saving) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        enabled = !saving && menus.isNotEmpty(),
                        onClick = { viewModel.saveMenus(menus, onSaved = onBackClick) },
                    ) {
                        Text(if (saving) "保存中" else "完成")
                    }
                },
            )
        },
    ) { paddingValues ->
        val topBarListState = rememberLazyListState()
        TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = topBarListState,
            contentPadding = paddingValues,
        ) {
            item {
                ReorderablePreferenceGroup(
                    items = menus,
                    key = HomeMenu::title,
                    onReorder = { draft = it },
                    title = "板块顺序",
                    enabled = !saving,
                ) { menu, dragging, dragHandle ->
                    val type = homeTabTypes.firstOrNull { it.name == menu.title }
                    if (type != null) {
                        CheckboxPreference(
                            title = tabTitle(type),
                            checked = menu.isEnable,
                            // The last enabled tab stays checkable-looking but inert; disabling the
                            // whole row would also take its drag handle and checkbox away.
                            onCheckedChange =
                                if (menu.isEnable && enabledCount <= 1) {
                                    null
                                } else {
                                    { checked ->
                                        draft =
                                            menus.map { item ->
                                                if (item.title == menu.title) {
                                                    item.copy(isEnable = checked)
                                                } else {
                                                    item
                                                }
                                            }
                                    }
                                },
                            endActions = { dragHandle() },
                            holdDownState = dragging,
                            enabled = !saving,
                        )
                    }
                }
                Text(
                    "拖动右侧手柄调整板块顺序，勾选决定是否显示。至少保留一个板块，点击完成后保存。",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (saveError) {
                    Text(
                        "保存失败，请重试",
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/** Saves the editor draft as plain title/enabled pairs so it survives process recreation. */
private val HomeDraftSaver: Saver<List<HomeMenu>?, Any> =
    listSaver(
        save = { draft ->
            draft.orEmpty().map { menu -> "${menu.title}\u0001${if (menu.isEnable) 1 else 0}" }
        },
        restore = { saved ->
            saved
                .mapNotNull { entry ->
                    val separator = entry.indexOf('\u0001')
                    if (separator <= 0) return@mapNotNull null
                    val title = entry.substring(0, separator)
                    val type = homeTabTypes.firstOrNull { it.name == title } ?: return@mapNotNull null
                    HomeMenu(
                        position = 0,
                        title = type.name,
                        isEnable = entry.substring(separator + 1) != "0",
                    )
                }
                .ifEmpty { null }
                ?.let { normalizeHomeMenus(it) }
        },
    )

@Composable
fun HomeScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    viewModel: HomeViewModel = hiltViewModel(),
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {

    val scope = rememberCoroutineScope()

    val storedMenus by viewModel.homeMenus.collectAsStateWithLifecycle(initialValue = emptyList())
    val tabList =
        remember(storedMenus) {
            if (storedMenus.isEmpty()) {
                homeTabTypes
            } else {
                storedMenus
                    .asSequence()
                    .filter(HomeMenu::isEnable)
                    .sortedBy(HomeMenu::position)
                    .mapNotNull { menu ->
                        homeTabTypes.firstOrNull { it.name == menu.title }
                    }
                    .toList()
                    .ifEmpty { listOf(TabType.FEED) }
            }
        }
    var selectedTabType by rememberSaveable { mutableStateOf(TabType.FEED) }
    val initialPage = tabList.indexOf(selectedTabType).coerceAtLeast(0)
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = {
                tabList.size
            },
        )
    val context = LocalContext.current
    var isScrollingUp by remember { mutableStateOf(false) }
    val selectedTabIndex = pagerState.currentPage.coerceIn(tabList.indices)

    LaunchedEffect(tabList) {
        val targetType =
            selectedTabType.takeIf(tabList::contains)
                ?: TabType.FEED.takeIf(tabList::contains)
                ?: tabList.first()
        selectedTabType = targetType
        val targetIndex = tabList.indexOf(targetType)
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState, tabList) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                tabList.getOrNull(page)?.let { selectedTabType = it }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            androidx.compose.foundation.layout.Column {
                TopAppBar(
                    title = "主页",
                    actions = {
                        MiuixToolbarIconButton(onClick = onSearch) {
                            Icon(MiuixIcons.Search, contentDescription = "搜索")
                        }
                    },
                )
                MiuixTabRow(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                    withContour = true,
                    tabs = tabList.map(::tabTitle),
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        if (pagerState.currentPage == index) onRefresh()
                        selectedTabType = tabList[index]
                        scope.launch { pagerState.springAnimateToPage(index) }
                    },
                )
            }
        },
        floatingActionButton = {
            if (isLogin && !LocalUserPreferences.current.hideHomePublishButton && tabList.getOrNull(selectedTabIndex) == TabType.FEED) {
                AnimatedVisibility(
                    modifier = Modifier.padding(bottom = bottomPadding),
                    visible = isScrollingUp,
                    enter = slideInVertically { it * 2 },
                    exit = slideOutVertically { it * 2 },
                ) {
                    FloatingActionButton(
                        onClick =
                            rememberHapticClick {
                                val intent = Intent(context, ReplyActivity::class.java)
                                intent.putExtra("type", "createFeed")
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
                            imageVector = MiuixIcons.Add,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { paddingValues ->
        val pagePadding =
            androidx.compose.foundation.layout.PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = bottomPadding + paddingValues.calculateBottomPadding(),
            )
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                key = { index -> tabList[index].name },
            ) { index ->
                TopBarScrollPage(pagerState.currentPage == index) {
                    when (val type = tabList[index]) {
                        TabType.FOLLOW,
                        TabType.FEED,
                        TabType.HOT,
                        TabType.COOLPIC ->
                            HomeFeedScreen(
                                paddingValues = pagePadding,
                                refreshState = refreshState && index == pagerState.currentPage,
                                resetRefreshState = resetRefreshState,
                                type = type,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onReport = onReport,
                                isScrollingUp = {
                                    isScrollingUp = it
                                },
                            )

                        TabType.APP ->
                            AppListScreen(
                                paddingValues = pagePadding,
                                refreshState = refreshState && index == pagerState.currentPage,
                                resetRefreshState = resetRefreshState,
                                onViewApp = onViewApp,
                                onCheckUpdate = onCheckUpdate,
                            )

                        TabType.TOPIC,
                        TabType.PRODUCT ->
                            HomeTopicScreen(
                                paddingValues = pagePadding,
                                type = type,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                            )
                    }
                }
            }
        }
    }
}
