package com.example.c001apk.compose.ui.main

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.constrainWidth
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun AppNavHost(
    navigator: AppNavigator,
    modifier: Modifier = Modifier,
    pane: Boolean = false,
    transparentRoot: Boolean = false,
    active: Boolean = true,
    gesturesEnabled: Boolean = true,
    onBack: () -> Unit = { navigator.popBackStack() },
    content: @Composable (AppNavEntry) -> Unit,
) {
    val predictiveBack = LocalUserPreferences.current.predictiveBack
    val childGestures = remember(navigator) { NavigationGestureGate() }
    val currentOnBack by rememberUpdatedState(onBack)
    // WebView owns its internal history; its BackHandler still receives system back first.
    val swipeDismiss = if (
        active && gesturesEnabled && predictiveBack && !WindowInsets.isImeVisible &&
        !childGestures.isBlocked(navigator.currentEntry.id) &&
        navigator.currentDestination !is AppDestination.WebView
    ) NavSwipeDirection.LeftToRight else NavSwipeDirection.None
    val screenCornerRadius = rememberNavSystemCornerRadius()
    val windowWidth = LocalWindowInfo.current.containerSize.width.coerceAtLeast(1)
    val hiddenPane = if (active) Modifier else Modifier
        .clearAndSetSemantics {}
        .layout { measurable, constraints ->
            // A zero-width retained feed can overflow its height while measuring padded content.
            val measureConstraints = if (constraints.maxWidth == 0) {
                constraints.copy(maxWidth = windowWidth)
            } else constraints
            val placeable = measurable.measure(measureConstraints)
            // Measuring retains the native host; not placing it removes even expanded touch bounds.
            layout(constraints.constrainWidth(placeable.width), placeable.height) {}
        }
    NavigationPaneScope(active = active) {
        NavDisplay(
            navController = navigator.controller,
            modifier = modifier.clipToBounds().then(hiddenPane),
            onBack = onBack,
            transition = NavTransitions.MiuixDefault,
            effects = NavDisplayEffects(
                cornerClipRadius = if (pane) 0.dp else screenCornerRadius,
                backdropColor = if (transparentRoot) Color.Unspecified else MiuixTheme.colorScheme.background,
            ),
        ) {
            entry<AppNavEntry>(contentKey = { it.id }, swipeDismiss = swipeDismiss) { entry ->
                // Register once, before the page's own handlers. The native entry dispatcher gates
                // covered/removing entries; inner WebView/dialog handlers retain higher priority.
                // Completion-only handling leaves the page still until a system gesture commits.
                NavigationBackHandler(
                    state = rememberNavigationEventState(NavigationEventInfo.None),
                    isBackEnabled = !LocalUserPreferences.current.predictiveBack && navigator.canGoBack,
                    onBackCompleted = { currentOnBack() },
                )
                CompositionLocalProvider(
                    LocalParentNavigationGesture provides remember(childGestures, entry.id) {
                        ParentNavigationGesture(childGestures, entry.id)
                    },
                ) {
                    content(entry)
                }
            }
        }
    }
}
