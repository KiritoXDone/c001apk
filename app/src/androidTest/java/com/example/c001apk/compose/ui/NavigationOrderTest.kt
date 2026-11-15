package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixNavigationRail
import com.example.c001apk.compose.ui.component.MiuixNavigationScaffold
import com.example.c001apk.compose.ui.main.MainPageState
import com.example.c001apk.compose.ui.main.mainNavigationItems
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

class NavigationOrderTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        }
    }).around(compose)

    @Test
    fun allOrdersKeepProfileBadgeAndStableSelectionAcrossBarStylesAndRail() {
        val order = mutableStateOf(listOf(0, 1, 2))
        val floating = mutableStateOf(false)
        val bottomStyle = mutableStateOf<BottomBarStyle?>(null)
        val rail = mutableStateOf(false)
        val selected = mutableIntStateOf(0)
        val unread = mutableIntStateOf(3)
        val state = MainPageState()
        compose.setContent {
            CompositionLocalProvider(LocalUserPreferences provides UserPreferencesCompat.default().copy(
                mainNavigationOrder = order.value,
                floatingNavigationBar = floating.value,
                bottomBarStyle = bottomStyle.value,
            )) {
                MiuixTheme {
                    val items = mainNavigationItems(selected.intValue, order.value)
                    val onSelect: (Int) -> Unit = { position ->
                        val pageId = order.value[position]
                        selected.intValue = pageId
                        state.select(pageId)
                        if (pageId == 2) unread.intValue = 0
                    }
                    if (rail.value) {
                        Box(Modifier.testTag("rail")) {
                            MiuixNavigationRail(
                                items = items,
                                selectedIndex = order.value.indexOf(selected.intValue),
                                unreadCount = unread.intValue,
                                onSelect = onSelect,
                                badgeIndex = order.value.indexOf(2),
                            )
                        }
                    } else MiuixNavigationScaffold(
                        items = items,
                        selectedIndex = order.value.indexOf(selected.intValue),
                        wide = false,
                        unreadCount = unread.intValue,
                        onSelect = onSelect,
                        badgeIndex = order.value.indexOf(2),
                    ) { padding -> Box(Modifier.fillMaxSize().padding(padding)) { Text("selected ${selected.intValue}") } }
                }
            }
        }
        val orders = listOf(
            listOf(0, 1, 2), listOf(0, 2, 1), listOf(1, 0, 2),
            listOf(1, 2, 0), listOf(2, 0, 1), listOf(2, 1, 0),
        )
        for (newOrder in orders) {
            for (style in 0..3) {
                compose.runOnIdle {
                    order.value = newOrder
                    floating.value = style == 1
                    rail.value = style == 2
                    bottomStyle.value = if (style == 3) BottomBarStyle.OS4 else null
                    unread.intValue = 3
                    state.homeRefreshing = false
                    state.followRefreshing = false
                }
                compose.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
                val node: (String) -> androidx.compose.ui.test.SemanticsNodeInteraction = { label ->
                    if (style == 1 || style == 3) compose.onNodeWithContentDescription(label, substring = style == 3)
                    else compose.onNodeWithText(label)
                }
                val home = node("主页").fetchSemanticsNode().boundsInRoot
                val follow = node("关注").fetchSemanticsNode().boundsInRoot
                val profile = node("我的").fetchSemanticsNode().boundsInRoot
                val positions = listOf(home, follow, profile)
                for (i in 0..1) {
                    val first = positions[newOrder[i]]
                    val next = positions[newOrder[i + 1]]
                    if (style == 2) assertTrue(first.top < next.top)
                    else assertTrue(first.left < next.left)
                }
                // Badge remains spatially attached to Profile even when Profile moves to the first slot.
                val badge = compose.onNodeWithText("3", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                if (style != 2) assertTrue(kotlin.math.abs(badge.center.x - profile.center.x) < 130f)
                node("关注").performClick()
                node("关注").performClick()
                compose.runOnIdle {
                    assertEquals(1, selected.intValue)
                    assertTrue(state.followRefreshing)
                    assertFalse(state.homeRefreshing)
                }
                node("我的").performClick()
                compose.runOnIdle { assertEquals(2, selected.intValue); assertEquals(0, unread.intValue) }
                node("主页").performClick()
                node("主页").performClick()
                compose.runOnIdle { assertEquals(0, selected.intValue); assertTrue(state.homeRefreshing) }
            }
        }
    }
}
