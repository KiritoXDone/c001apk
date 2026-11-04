package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.main.BlockParentNavigationGestures
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.rememberNavController
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Serializable
private sealed interface SheetRoute : NavKey {
    @Serializable data object Background : SheetRoute
    @Serializable data object Content : SheetRoute
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun MiuixNavigationSheet(
    onDismissRequest: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    val controller = rememberNavController<SheetRoute>(SheetRoute.Background)
    val onDismiss by rememberUpdatedState(onDismissRequest)
    val predictiveBack = LocalUserPreferences.current.predictiveBack
    val swipe = if (WindowInsets.isImeVisible) NavSwipeDirection.None else NavSwipeDirection.TopToBottom
    LaunchedEffect(controller) { if (controller.backStack.size == 1) controller.push(SheetRoute.Content) }
    NavDisplay(
        navController = controller,
        modifier = Modifier.fillMaxSize(),
        transition = NavTransitions.Modal,
        effects = NavDisplayEffects(enableCornerClip = false, backdropColor = Color.Transparent),
    ) {
        entry<SheetRoute.Background>(swipeDismiss = NavSwipeDirection.None) { Box(Modifier.fillMaxSize()) }
        entry<SheetRoute.Content>(swipeDismiss = swipe) {
            BlockParentNavigationGestures()
            // 保留内容自身处理器的优先级；关闭预测返回时只处理系统返回的完成事件。
            NavigationBackHandler(
                state = rememberNavigationEventState(NavigationEventInfo.None),
                isBackEnabled = !predictiveBack,
                onBackCompleted = { controller.pop() },
            )
            DisposableEffect(controller) {
                onDispose { if (controller.backStack.size == 1) onDismiss() }
            }
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                    onClick = { controller.pop() },
                ))
                Column(Modifier.fillMaxSize()) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Box(
                        Modifier.fillMaxSize().padding(top = 12.dp)
                            .squircleClip(24.dp, 24.dp, 0.dp, 0.dp)
                            .background(MiuixTheme.colorScheme.surface)
                            .navigationBarsPadding(),
                    ) {
                        Box(Modifier.matchParentSize().clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                            onClick = {},
                        ).clearAndSetSemantics {})
                        content { controller.pop() }
                    }
                }
            }
        }
    }
}
