package com.example.c001apk.compose.ui

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.CollapsingTabsState
import com.example.c001apk.compose.ui.component.LocalCollapsingTabs
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MiuixTabScaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.component.topBarTap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

class TopBarScrollTest {
    private val compose = createComposeRule()

    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor ->
                ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        }
    }).around(compose)

    @Test fun opaqueBarBlocksTapsAndKeepsActions() = checkBar(blur = false)

    @Test fun blurredBarBlocksTapsAndKeepsActions() = checkBar(blur = true)

    @Test fun titleStaysCenteredWithUnequalOrChangingButtons() {
        val widths = mutableStateOf(0.dp to 120.dp)
        var actions = 0
        compose.setContent {
            MiuixTheme {
                MiuixTopBar(
                    title = "居中标题",
                    modifier = Modifier.fillMaxWidth().testTag("bar"),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = { Box(Modifier.width(widths.value.first).height(48.dp)) },
                    actions = {
                        Box(Modifier.width(widths.value.second).height(48.dp)
                            .testTag("actions").clickable { actions++ })
                    },
                )
            }
        }
        for (sides in listOf(0.dp to 120.dp, 120.dp to 0.dp, 48.dp to 120.dp)) {
            compose.runOnIdle { widths.value = sides }
            val bar = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot
            val title = compose.onNodeWithText("居中标题").fetchSemanticsNode().boundsInRoot
            assertEquals(bar.center.x, title.center.x, 1f)
            if (sides.second > 0.dp) {
                val buttons = compose.onNodeWithTag("actions").fetchSemanticsNode().boundsInRoot
                assertTrue(title.right <= buttons.left)
            }
        }
        compose.onNodeWithTag("actions").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, actions) }
    }

    private fun checkBar(blur: Boolean) {
        var underneath = 0
        var action = 0
        var scrolls = 0
        compose.setContent {
            CompositionLocalProvider(LocalUserPreferences provides UserPreferencesCompat.default()) {
                MiuixTheme {
                    MiuixScaffold(
                        blurBars = blur,
                        topBar = {
                            MiuixTopBar(
                                title = "顶栏",
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                actions = { Text("操作", Modifier.clickable { action++ }) },
                            )
                        },
                    ) {
                        TopBarScrollEffect { scrolls++ }
                        // Deliberately put a clickable item underneath the entire bar.
                        Box(Modifier.fillMaxSize().clickable { underneath++ })
                    }
                }
            }
        }
        compose.onNodeWithText("顶栏").performTouchInput { click() }
        compose.mainClock.advanceTimeBy(400)
        compose.onNodeWithText("顶栏").performTouchInput { doubleClick() }
        compose.onNodeWithText("操作").performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(0, underneath)
            assertEquals(1, scrolls)
            assertEquals(1, action)
        }
    }

    @Test fun doubleTapScrollsOnlyActivePageAndPreservesSingleTap() {
        val firstActive = mutableStateOf(true)
        val lists = arrayOfNulls<LazyListState>(2)
        lateinit var tabs: CollapsingTabsState
        var singles = 0
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides
                    UserPreferencesCompat.default().copy(collapseTabsOnScroll = true),
            ) {
                MiuixTheme {
                    MiuixScaffold(
                        blurBars = false,
                        topBar = {
                            MiuixTopBar(
                                title = "动态",
                                modifier = Modifier.topBarTap { singles++ },
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            )
                        },
                    ) { padding ->
                        MiuixTabScaffold(listOf("列表"), 0, {}) {
                            tabs = LocalCollapsingTabs.current!!
                            Row {
                                repeat(2) { index ->
                                    TopBarScrollPage(firstActive.value == (index == 0)) {
                                        val state = rememberLazyListState(initialFirstVisibleItemIndex = 15)
                                        lists[index] = state
                                        TopBarScrollEffect { state.animateScrollToItem(0) }
                                        LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            state = state,
                                            contentPadding = padding,
                                        ) {
                                            items(40) { Text("条目 $index/$it", Modifier.height(80.dp)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("动态").performTouchInput { click() }
        compose.mainClock.advanceTimeBy(400)
        compose.runOnIdle {
            assertEquals(1, singles)
            tabs.onPostScroll(Offset(0f, -100f), Offset.Zero, NestedScrollSource.UserInput)
            assertFalse(tabs.visible)
        }
        compose.onNodeWithText("动态").performTouchInput { doubleClick() }
        compose.runOnIdle {
            assertEquals(0, lists[0]!!.firstVisibleItemIndex)
            assertEquals(15, lists[1]!!.firstVisibleItemIndex)
            assertEquals(1, singles)
            assertTrue(tabs.visible)
            firstActive.value = false
        }
        compose.runOnIdle { assertEquals(15, lists[1]!!.firstVisibleItemIndex) }
        compose.onNodeWithText("动态").performTouchInput { doubleClick() }
        compose.runOnIdle { assertEquals(0, lists[1]!!.firstVisibleItemIndex) }
    }
}
