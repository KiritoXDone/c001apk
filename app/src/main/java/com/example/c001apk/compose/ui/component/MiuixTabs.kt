package com.example.c001apk.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.glass.GlassSegmentedTabRow
import top.yukonga.miuix.kmp.glass.GlassSegmentedTabRowDefaults
import top.yukonga.miuix.kmp.glass.glassShadow
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import kotlin.math.roundToInt

/** Inset the list's initial content, allowing scrolled items to pass beneath the tab bar. */
internal val LocalTabContentPadding = staticCompositionLocalOf { PaddingValues() }

private class TabPageRequest(val index: Int)

@Composable
internal fun MiuixPagerTabRow(
    tabs: List<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    withContour: Boolean = false,
    onReselect: () -> Unit = {},
) {
    var requestedPage by remember(pagerState) { mutableStateOf<TabPageRequest?>(null) }
    val destination = requestedPage
    LaunchedEffect(pagerState, destination) {
        if (destination != null) {
            try {
                pagerState.springAnimateToPage(destination.index)
            } finally {
                if (requestedPage == destination) requestedPage = null
            }
        }
    }
    MiuixTabRow(
        tabs = tabs,
        selectedTabIndex = destination?.index ?: pagerState.settledPage,
        onTabSelected = { index ->
            if (requestedPage == null && !pagerState.isScrollInProgress && index == pagerState.settledPage) {
                onReselect()
            } else if (requestedPage?.index != index) {
                requestedPage = TabPageRequest(index)
            }
        },
        modifier = modifier,
        withContour = withContour,
    )
}

/** Shared collapse behavior for native tab styles hosted in the page header. */
@Composable
fun MiuixTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    withContour: Boolean = false,
) {
    if (tabs.isEmpty()) return
    val selected = selectedTabIndex.coerceIn(tabs.indices)
    val topBarStyle = LocalUserPreferences.current.topBarStyle
    AnimatedVisibility(
        visible = LocalCollapsingTabs.current?.visible ?: true,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        when (topBarStyle) {
            TopBarStyle.Classic -> ClassicTabRow(tabs, selected, onTabSelected, modifier, withContour = false)
            TopBarStyle.ClassicContour -> ClassicTabRow(tabs, selected, onTabSelected, modifier, withContour = true)
            TopBarStyle.OS4 -> Os4TabRow(tabs, selected, onTabSelected, modifier, withContour)
        }
    }
}

@Composable
private fun ClassicTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    withContour: Boolean,
) {
    val colors = TabRowDefaults.tabRowColors(
        backgroundColor = if (LocalBarBackdrop.current != null) Color.Transparent
            else MiuixTheme.colorScheme.surface,
    )
    if (withContour) {
        TabRowWithContour(tabs, selected, onSelect, modifier, colors, indication = LocalIndication.current)
    } else {
        TabRow(tabs, selected, onSelect, modifier, colors, indication = LocalIndication.current)
    }
}

@Composable
private fun Os4TabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    withContour: Boolean,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val measurer = rememberTextMeasurer()
    val selectedStyle = MiuixTheme.textStyles.subtitle
    val normalStyle = MiuixTheme.textStyles.body2
    val labelSize = remember(tabs, measurer, selectedStyle, normalStyle) {
        tabs.flatMap { label ->
            listOf(
                measurer.measure(AnnotatedString(label), selectedStyle, maxLines = 1, softWrap = false).size,
                measurer.measure(AnnotatedString(label), normalStyle, maxLines = 1, softWrap = false).size,
            )
        }.let { sizes -> sizes.maxOf { it.width } to sizes.maxOf { it.height } }
    }
    // 轨道尺寸与居中改编自 BoxProxy bf0b1d5（GPL-3.0），来源见 THIRD_PARTY_PORTS.md。
    // 与原生 padding 和间距逐项取整一致，避免标签少分配像素。
    val trackInset = with(density) { GlassSegmentedTabRowDefaults.TrackPadding.roundToPx() }
    val gap = with(density) { GlassSegmentedTabRowDefaults.TabGap.roundToPx() }
    val labelPadding = with(density) { GlassSegmentedTabRowDefaults.TabPaddingHorizontal.roundToPx() }
    val naturalWidth = (labelSize.first + labelPadding * 2) * tabs.size + gap * (tabs.size - 1) + trackInset * 2
    val height = with(density) {
        maxOf(GlassSegmentedTabRowDefaults.Height.roundToPx(), labelSize.second + trackInset * 2).toDp()
    }
    val shape = GlassSegmentedTabRowDefaults.shape(height)
    val trackBase = if (withContour) MiuixTheme.colorScheme.surfaceContainer else MiuixTheme.colorScheme.surface
    val indicator = GlassSegmentedTabRowDefaults.indicatorColor().compositeOver(trackBase.copy(alpha = 1f))
    val backdrop = LocalBarBackdrop.current
    val surface = if (backdrop != null) backdrop.glassTabModifier(shape) else Modifier.background(trackBase, shape)
    BoxWithConstraints(modifier.fillMaxWidth().height(height)) {
        val viewport = if (constraints.hasBoundedWidth) constraints.maxWidth else naturalWidth
        val contentWidth = maxOf(viewport, naturalWidth)
        val canScroll = contentWidth > viewport
        val slotWidth = (contentWidth - trackInset * 2 - gap * (tabs.size - 1)).toFloat() / tabs.size
        val center = trackInset + selected * (slotWidth + gap) + slotWidth / 2
        val targetScroll = (center - viewport / 2f).roundToInt().coerceIn(0, contentWidth - viewport)
        val scroll = rememberScrollState(initial = targetScroll)
        LaunchedEffect(selected, targetScroll, tabs, layoutDirection) {
            if (scroll.value != targetScroll) scroll.animateScrollTo(targetScroll)
        }
        Box(Modifier.fillMaxWidth().height(height).glassShadow(shape, GlassShadows.Float).clip(shape).then(surface)) {
            Box(Modifier.horizontalScroll(scroll, enabled = canScroll, overscrollEffect = null)) {
                GlassSegmentedTabRow(
                    tabs = tabs,
                    selectedIndex = selected,
                    onSelect = onSelect,
                    backdrop = null,
                    modifier = Modifier.width(with(density) { contentWidth.toDp() }),
                    height = height,
                    surfaceAlpha = 0f,
                    trackColor = Color.Transparent,
                    stroke = null,
                    shadow = null,
                    indicatorColor = indicator,
                    selectedContentColor = MiuixTheme.colorScheme.onSurface,
                    contentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    dragToSelect = !canScroll,
                )
            }
        }
    }
}

/** For tabs below a page header, record only the pager as the source of their native miuix blur. */
@Composable
fun MiuixTabScaffold(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    visible: Boolean = true,
    belowTabs: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (!visible) {
        content()
        return
    }
    MiuixScaffold(
        topBarScrollRequests = LocalTopBarScroll.current,
        topBar = {
            Column {
                MiuixTabRow(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = onTabSelected,
                )
                belowTabs()
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        CompositionLocalProvider(LocalTabContentPadding provides padding) {
            content()
        }
    }
}
