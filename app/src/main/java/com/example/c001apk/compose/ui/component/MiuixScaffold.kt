package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.model.TopBarStyle
import kotlinx.coroutines.flow.MutableSharedFlow
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** One backdrop per page, shared by its bars. Nested main/page backdrops are independent. */
@Composable
fun MiuixScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    blurBars: Boolean = LocalUserPreferences.current.blurBars,
    floatingBottomBar: Boolean = false,
    containerColor: Color = MiuixTheme.colorScheme.surface,
    topBarColor: Color = containerColor,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    topBarScrollRequests: MutableSharedFlow<Unit>? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    // WebView's hardware compositor must draw once, outside the backdrop recording pass.
    val backdrop = rememberBarBackdrop(blurBars)
    val topBarStyle = LocalUserPreferences.current.topBarStyle
    val collapseTabs = LocalUserPreferences.current.collapseTabsOnScroll
    val tabScrollThreshold = with(LocalDensity.current) { 12.dp.toPx() }
    val tabState =
        remember(collapseTabs, tabScrollThreshold) {
            if (collapseTabs) CollapsingTabsState(tabScrollThreshold) else null
        }
    val scrollRequests = topBarScrollRequests ?: remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    CompositionLocalProvider(
        LocalCollapsingTabs provides tabState,
        LocalTopBarScroll provides scrollRequests,
    ) {
        TopBarScrollEffect { tabState?.expand() }
        Scaffold(
            modifier =
                modifier
                    .then(if (tabState != null) Modifier.nestedScroll(tabState) else Modifier)
                    .windowInsetsPadding(
                        WindowInsets.displayCutout
                            .union(WindowInsets.navigationBars)
                            .only(WindowInsetsSides.Horizontal),
                    ),
            topBar = {
                if (topBar != null) {
                    Box(Modifier.topBarTap()) {
                        val background = when {
                            topBarColor != containerColor || backdrop == null -> Modifier.background(topBarColor)
                            topBarStyle == TopBarStyle.OS4 -> Modifier.progressiveBarBlur(backdrop)
                            else -> Modifier.barBlur(backdrop)
                        }
                        Box(Modifier.matchParentSize().then(background))
                        CompositionLocalProvider(
                            LocalBarColor provides if (backdrop != null && topBarColor == containerColor) Color.Transparent else topBarColor,
                            LocalBarBackdrop provides backdrop,
                        ) {
                            CompactBar { topBar() }
                        }
                    }
                }
            },
            bottomBar = {
                if (bottomBar != null) {
                    Box {
                        val background = when {
                            floatingBottomBar -> Modifier
                            backdrop != null -> Modifier.barBlur(backdrop)
                            else -> Modifier.background(containerColor)
                        }
                        Box(Modifier.matchParentSize().then(background))
                        CompositionLocalProvider(
                            LocalBarColor provides if (backdrop != null && !floatingBottomBar)
                                Color.Transparent else containerColor,
                            LocalBarBackdrop provides backdrop,
                        ) {
                            CompactBar { bottomBar() }
                        }
                    }
                }
            },
            snackbarHost = snackbarHost,
            containerColor = containerColor,
            contentWindowInsets = contentWindowInsets,
        ) { padding ->
            Box(
                Modifier.fillMaxSize()
                    .then(
                        backdrop?.recordModifier() ?: Modifier,
                    ),
            ) {
                content(padding)
            }
        }
    }
}
