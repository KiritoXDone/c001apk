package com.example.c001apk.compose.ui.home

import com.example.c001apk.compose.logic.providable.LocalUserPreferences

import android.content.Intent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixScaffoldDefaults as ScaffoldDefaults
import com.example.c001apk.compose.ui.component.MiuixPagerTabRow
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
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

private fun tabTitleRes(type: TabType): Int {
    return when (type) {
        TabType.FOLLOW -> R.string.follow
        TabType.APP -> R.string.label_app
        TabType.FEED -> R.string.label_feed
        TabType.HOT -> R.string.label_hot
        TabType.TOPIC -> R.string.label_topic
        TabType.PRODUCT -> R.string.label_product
        TabType.COOLPIC -> R.string.label_coolpic
    }
}

@Composable
fun HomeTabEditorScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val storedMenus by viewModel.homeMenus.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()
    // The draft is the screen's state: Room's first emission fills it once, later emissions must
    // not overwrite edits, and it survives configuration changes without touching the database.
    var draft by rememberSaveable(stateSaver = HomeDraftSaver) {
        mutableStateOf<List<HomeMenu>?>(null)
    }
    LaunchedEffect(storedMenus) {
        if (draft == null) draft = storedMenus?.takeIf { it.isNotEmpty() }
    }
    val menus = draft ?: storedMenus.orEmpty()
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
                title = stringResource(R.string.settings_home_tabs),
                navigationIcon = {
                    MiuixToolbarIconButton(onClick = { if (!saving) onBackClick() }, enabled = !saving) {
                        Icon(MiuixIcons.Back, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        enabled = !saving && menus.isNotEmpty(),
                        onClick = { viewModel.saveMenus(menus, onSaved = onBackClick) },
                    ) {
                        Text(stringResource(if (saving) R.string.status_saving else R.string.settings_bar_order_done))
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
                    title = stringResource(R.string.home_tab_order),
                    enabled = !saving,
                ) { menu, dragging, dragHandle ->
                    val type = homeTabTypes.firstOrNull { it.name == menu.title }
                    if (type != null) {
                        CheckboxPreference(
                            title = stringResource(tabTitleRes(type)),
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
                    stringResource(R.string.home_tab_order_hint),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (saveError) {
                    Text(
                        stringResource(R.string.settings_device_save_failed),
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

    val storedMenus by viewModel.homeMenus.collectAsStateWithLifecycle()
    val menus = storedMenus
    if (menus == null) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = { HomeTopBar(onSearch) }) {}
        return
    }

    val tabList =
        remember(menus) {
            menus.filter(HomeMenu::isEnable).mapNotNull { menu ->
                homeTabTypes.firstOrNull { it.name == menu.title }
            }.ifEmpty { listOf(TabType.FEED) }
        }
    var selectedTabType by rememberSaveable { mutableStateOf(TabType.FEED) }
    val pageKey = remember(tabList) { tabList.map(TabType::name) }
    key(pageKey) {
        val initialType = selectedTabType.takeIf(tabList::contains)
            ?: TabType.FEED.takeIf(tabList::contains)
            ?: tabList.first()
        val pagerState = rememberPagerState(
            initialPage = tabList.indexOf(initialType),
            pageCount = { tabList.size },
        )

        val context = LocalContext.current
        val prefs = LocalUserPreferences.current
        var updateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
        val selectedTabIndex = pagerState.currentPage.coerceIn(tabList.indices)
        val currentTabType = tabList[selectedTabIndex]
        val canPublish = isLogin && !prefs.hideHomePublishButton && currentTabType == TabType.FEED

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
                    HomeTopBar(
                        onSearch = onSearch,
                        actions = {
                            if (canPublish) {
                                MiuixToolbarIconButton(
                                    onClick = rememberHapticClick {
                                        val intent = Intent(context, ReplyActivity::class.java)
                                        intent.putExtra("type", "createFeed")
                                        val animationBundle =
                                            ActivityOptionsCompat.makeCustomAnimation(
                                                context,
                                                R.anim.anim_bottom_sheet_slide_up,
                                                R.anim.anim_bottom_sheet_slide_down,
                                            ).toBundle()
                                        ContextCompat.startActivity(context, intent, animationBundle)
                                    },
                                ) {
                                    Icon(MiuixIcons.Edit, contentDescription = stringResource(R.string.feed_publish), modifier = Modifier.size(24.dp))
                                }
                            }
                            if (currentTabType == TabType.APP) {
                                updateAction?.let { onUpdate ->
                                    MiuixToolbarIconButton(onClick = onUpdate) {
                                        Icon(MiuixIcons.Update, contentDescription = stringResource(R.string.settings_check_app_updates), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        },
                    )
                    MiuixPagerTabRow(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                        withContour = true,
                        tabs = tabList.map { stringResource(tabTitleRes(it)) },
                        pagerState = pagerState,
                        onReselect = onRefresh,
                    )
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
                                )

                            TabType.APP ->
                                AppListScreen(
                                    paddingValues = pagePadding,
                                    refreshState = refreshState && index == pagerState.currentPage,
                                    resetRefreshState = resetRefreshState,
                                    onViewApp = onViewApp,
                                    onCheckUpdate = onCheckUpdate,
                                    onUpdateActionChange = { updateAction = it },
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
}

@Composable
private fun HomeTopBar(
    onSearch: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(title = stringResource(R.string.home), actions = {
        MiuixToolbarIconButton(onClick = onSearch) {
            Icon(MiuixIcons.Basic.Search, contentDescription = stringResource(R.string.search_hint), modifier = Modifier.size(24.dp))
        }
        actions()
    })
}
