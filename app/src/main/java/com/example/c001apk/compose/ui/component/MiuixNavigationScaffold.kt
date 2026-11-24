package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.FloatingToolbarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationBarDefaults
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixNavigationScaffold(
    items: List<Pair<ImageVector, String>>,
    selectedIndex: Int,
    wide: Boolean,
    unreadCount: Int,
    onSelect: (Int) -> Unit,
    showRail: Boolean = true,
    badgeIndex: Int = 2,
    content: @Composable (PaddingValues) -> Unit,
) {
    val prefs = LocalUserPreferences.current
    val bottomStyle = prefs.effectiveBottomBarStyle
    val floating = !wide && bottomStyle != BottomBarStyle.Classic
    val badge: @Composable () -> Unit = {
        if (unreadCount > 0) Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
    }
    Row(Modifier.fillMaxSize()) {
        if (wide && showRail)
            MiuixNavigationRail(items, selectedIndex, unreadCount, onSelect, badgeIndex)
        Scaffold(
            modifier = Modifier.weight(1f),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingBottomBar = floating,
            bottomBar = {
                if (!wide && bottomStyle == BottomBarStyle.Floating) {
                    val backdrop = LocalBarBackdrop.current
                    FloatingNavigationBar(
                        color =
                            if (backdrop != null) Color.Transparent
                            else MiuixTheme.colorScheme.surfaceContainer,
                        modifier =
                            if (backdrop != null)
                                Modifier.barBlur(
                                    backdrop,
                                    RoundedCornerShape(FloatingToolbarDefaults.CornerRadius),
                                )
                            else Modifier,
                    ) {
                        items.forEachIndexed { index, item ->
                            FloatingNavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { onSelect(index) },
                                icon = item.first,
                                label = item.second,
                                badge = if (index == badgeIndex) badge else null,
                            )
                        }
                    }
                } else if (!wide && bottomStyle == BottomBarStyle.OS4) {
                    val backdrop = LocalBarBackdrop.current?.rawBackdrop
                    val bottomInset = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        .asPaddingValues().calculateBottomPadding()
                    val bottomPadding = if (bottomInset == 0.dp) 32.dp else bottomInset + 16.dp
                    val container = if (prefs.pureBlack && MiuixTheme.colorScheme.background.luminance() < 0.5f)
                        MiuixTheme.colorScheme.surface else MiuixTheme.colorScheme.surfaceContainer
                    MiuixTheme(colors = MiuixTheme.colorScheme.copy(surfaceContainer = container.copy(alpha = 1f))) {
                        GlassNavigationBar(
                            items = items.mapIndexed { index, (icon, label) ->
                                val description = if (index == badgeIndex && unreadCount > 0) {
                                    stringResource(
                                        R.string.unread_description,
                                        label,
                                        if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    )
                                } else label
                                GlassNavigationItem(
                                    icon = icon,
                                    label = label,
                                    contentDescription = description,
                                    badge = badge.takeIf { index == badgeIndex && unreadCount > 0 },
                                )
                            },
                            selectedIndex = selectedIndex,
                            onSelect = onSelect,
                            backdrop = backdrop,
                            modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = bottomPadding),
                            defaultWindowInsetsPadding = false,
                            stroke = if (backdrop != null) GlassNavigationBarDefaults.stroke() else null,
                            shadow = if (backdrop != null) GlassShadows.Float else null,
                        )
                    }
                } else if (!wide)
                    NavigationBar(color = LocalBarColor.current) {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { onSelect(index) },
                                icon = item.first,
                                label = item.second,
                                badge = if (index == badgeIndex) badge else null,
                            )
                        }
                    }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().consumeWindowInsets(padding)) { content(padding) }
        }
    }
}

@Composable
fun MiuixNavigationRail(
    items: List<Pair<ImageVector, String>>,
    selectedIndex: Int,
    unreadCount: Int,
    onSelect: (Int) -> Unit,
    badgeIndex: Int = 2,
) {
    NavigationRail {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                icon = item.first,
                label = item.second,
                badge = if (index == badgeIndex && unreadCount > 0) {
                    { Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) } }
                } else null,
            )
        }
    }
}
