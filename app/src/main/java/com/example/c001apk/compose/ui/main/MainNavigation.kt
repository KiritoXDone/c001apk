package com.example.c001apk.compose.ui.main

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.model.MainNavigationOrder
import com.example.c001apk.compose.ui.component.MiuixNavigationRail
import com.example.c001apk.compose.ui.component.ReadingLayout
import com.example.c001apk.compose.ui.component.WindowWidthSizeClass
import com.example.c001apk.compose.ui.component.currentWindowSize
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.feed.FeedScreen
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.getReportUrl

@Composable
fun MainNavigation(
    navigator: AppNavigator,
    badge: Int,
    resetBadge: () -> Unit,
    widthSizeClass: WindowWidthSizeClass,
) {
    val context = LocalContext.current
    val mainPageState = rememberMainPageState()
    val performHapticClick = rememberHapticClick {}
    val preferences = LocalUserPreferences.current
    val order = preferences.mainNavigationOrder
    val accountRevision = preferences.accountRevision
    var displayedAccountRevision by rememberSaveable { mutableStateOf(accountRevision) }
    val detailNavigator = rememberAppNavigator(AppDestination.DetailPlaceholder)
    var detailParent by rememberSaveable { mutableStateOf<String?>(null) }
    var detailExiting by remember { mutableStateOf(false) }
    var sidebarHidden by rememberSaveable(accountRevision) { mutableStateOf(false) }
    val onMainPageSelected: (Int) -> Unit = { index ->
        performHapticClick()
        if (index != mainPageState.selectedIndex) {
            detailParent = null
            detailExiting = false
        }
        mainPageState.select(index, detailNavigator)
        if (index == 2 && badge != 0) resetBadge()
    }

    LaunchedEffect(accountRevision) {
        if (displayedAccountRevision != accountRevision) {
            displayedAccountRevision = accountRevision
            mainPageState.selectedIndex = 2
            mainPageState.homeRefreshing = false
            mainPageState.followRefreshing = false
            detailParent = null
            detailExiting = false
            detailNavigator.reset(AppDestination.DetailPlaceholder)
            navigator.reset()
        }
    }
    val windowSize = currentWindowSize()
    val density = LocalDensity.current
    val canSplit = ReadingLayout.split(
        windowSize.width.value,
        minOf(windowSize.width.value, windowSize.height.value),
        density.fontScale,
        preferences.readingMode,
    )
    val paneAttached = detailParent == navigator.currentEntry.id
    val paneVisible = paneAttached && detailNavigator.canGoBack
    MainPageBackHandler(navigator, mainPageState, paneVisible, onMainPageSelected)
    val split = paneVisible && canSplit && !sidebarHidden
    val detailWidth = if (canSplit && !sidebarHidden) (windowSize.width * .52f).coerceIn(390.dp, 760.dp) else windowSize.width

    fun onReport(id: String, type: ReportType) {
        navigator.navigateToWebView(getReportUrl(id, type))
    }

    fun onViewFeed(id: String, isViewReply: Boolean) {
        if (canSplit || paneVisible) {
            if (detailParent != navigator.currentEntry.id) {
                detailNavigator.reset(AppDestination.DetailPlaceholder)
            }
            detailParent = navigator.currentEntry.id
            detailExiting = false
            val destination = AppDestination.Feed(id, isViewReply)
            detailNavigator.navigateReading(destination)
        } else {
            navigator.navigateToFeed(id, isViewReply)
        }
    }

    fun onOpenLink(url: String, title: String?) {
        navigator.onOpenLink(context, url, title, onViewFeed = ::onViewFeed)
    }

    fun closeDetail() {
        if (detailNavigator.canGoBack) {
            detailExiting = detailNavigator.controller.backStack.size == 2
            detailNavigator.popBackStack()
        }
    }

    val showRail = widthSizeClass != WindowWidthSizeClass.Compact &&
        navigator.currentDestination == AppDestination.Main && (!paneVisible || split)
    MainNavigationLayout(
        split = split,
        detailWidth = if (paneAttached && (paneVisible || detailExiting)) detailWidth else 0.dp,
        rail = {
            if (showRail) {
                MiuixNavigationRail(
                    items = mainNavigationItems(mainPageState.selectedIndex, order),
                    selectedIndex = order.indexOf(mainPageState.selectedIndex),
                    unreadCount = badge,
                    onSelect = { position -> onMainPageSelected(order[position]) },
                    badgeIndex = order.indexOf(MainNavigationOrder.PROFILE),
                )
            }
        },
        main = { paneModifier ->
            AppNavHost(
                navigator = navigator,
                pane = split || showRail,
                active = !paneVisible || split,
                gesturesEnabled = !paneVisible,
                modifier = paneModifier.then(
                    if (paneVisible && !split) Modifier.alpha(0f).clearAndSetSemantics {}
                    else Modifier,
                ),
            ) { entry ->
                AppDestinationContent(
                    entryId = entry.id,
                    route = entry.destination,
                    navigator = navigator,
                    mainPageState = mainPageState,
                    onMainPageSelected = onMainPageSelected,
                    badge = badge,
                    widthSizeClass = widthSizeClass,
                    onViewFeed = ::onViewFeed,
                    onOpenLink = ::onOpenLink,
                    onReport = ::onReport,
                )
            }
        },
        detail = { paneModifier ->
            // Keep the native host alive across parent navigation and posture changes. Hidden panes
            // retain their state, but have no hit area and cannot consume back or resume their pages.
            AppNavHost(
                navigator = detailNavigator,
                pane = canSplit,
                transparentRoot = true,
                active = paneAttached && (paneVisible || detailExiting),
                modifier = paneModifier,
                onBack = ::closeDetail,
            ) { entry ->
                val feed = entry.destination as? AppDestination.Feed
                if (feed != null) {
                    DisposableEffect(entry.id) {
                        onDispose {
                            if (!detailNavigator.canGoBack) {
                                detailExiting = false
                                detailParent = null
                            }
                        }
                    }
                    FeedScreen(
                        isCompat = false,
                        onBackClick = ::closeDetail,
                        id = feed.id,
                        isViewReply = feed.isViewReply,
                        onViewUser = navigator::navigateToUser,
                        onViewFeed = ::onViewFeed,
                        onOpenLink = ::onOpenLink,
                        onCopyText = navigator::navigateToCopyText,
                        onReport = ::onReport,
                        onToggleSidebar = if (canSplit) ({ sidebarHidden = !sidebarHidden }) else null,
                        sidebarHidden = sidebarHidden,
                    )
                }
            }
        },
    )
}

/** The rail belongs to the whole reading surface, outside both navigation hosts. */
@Composable
internal fun MainNavigationLayout(
    split: Boolean,
    detailWidth: Dp,
    rail: @Composable () -> Unit,
    main: @Composable (Modifier) -> Unit,
    detail: @Composable (Modifier) -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        rail()
        BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
            val paneWidth = detailWidth.coerceIn(0.dp, maxWidth)
            main(
                (if (split) Modifier.width(maxWidth - paneWidth) else Modifier.fillMaxWidth())
                    .fillMaxHeight().align(Alignment.CenterStart),
            )
            detail(Modifier.width(paneWidth).fillMaxHeight().align(Alignment.CenterEnd))
        }
    }
}
