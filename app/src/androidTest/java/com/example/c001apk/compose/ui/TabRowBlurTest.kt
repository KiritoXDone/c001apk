package com.example.c001apk.compose.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixPagerTabRow
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MiuixTabRow
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text

@SdkSuppress(minSdkVersion = 33)
class TabRowBlurTest {
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

    @Test fun standardRowExposesBackdropButKeepsSelectedTabSolid() = verifyBackdrop(false)
    @Test fun contourRowExposesBackdropButKeepsSelectedTabSolid() = verifyBackdrop(true)

    @Test fun enlargedTextFitsInsideTheSegmentedTrack() {
        val selected = mutableStateOf(0)
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                C001apkComposeTheme(materialYou = false, fontScale = 2f) {
                    MiuixTabRow(
                        tabs = listOf("动态", "长标签内容"),
                        selectedTabIndex = selected.value,
                        onTabSelected = { selected.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("tabs"),
                    )
                }
            }
        }
        for (label in listOf("动态", "长标签内容")) {
            compose.onNodeWithText(label).performClick()
            val track = compose.onNodeWithTag("tabs").fetchSemanticsNode().boundsInRoot
            val text = compose.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
            assertTrue(text.top >= track.top && text.bottom <= track.bottom)
        }
    }

    @Test fun segmentedLabelsFitAtFractionalDensityWhenSelectionChanges() {
        val selected = mutableStateOf(0)
        val density = mutableStateOf(2.6f)
        val fontScale = mutableStateOf(1f)
        val labels = listOf("第一个较长的标签", "第二个较长的标签", "第三个较长的标签")
        compose.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density.value, fontScale.value),
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                C001apkComposeTheme(materialYou = false) {
                    MiuixTabRow(
                        tabs = labels,
                        selectedTabIndex = selected.value,
                        onTabSelected = { selected.value = it },
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
        }
        for ((currentDensity, currentFontScale) in listOf(2.6f to 1f, 2.625f to 1.5f)) {
            compose.runOnIdle {
                density.value = currentDensity
                fontScale.value = currentFontScale
            }
            for (index in labels.indices) {
                compose.runOnIdle { selected.value = index }
                for (label in labels) {
                    val layouts = mutableListOf<TextLayoutResult>()
                    compose.onNodeWithText(label, useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { getLayouts ->
                            assertTrue(getLayouts(layouts))
                        }
                    assertTrue(layouts.isNotEmpty())
                    assertFalse("$label must not be ellipsized", layouts.last().isLineEllipsized(0))
                    assertFalse("$label must not overflow", layouts.last().hasVisualOverflow)
                }
            }
        }
    }

    @Test fun classicStyleOverridesTheLegacyContourHint() {
        val style = mutableStateOf(TopBarStyle.Classic)
        val hint = mutableStateOf(false)
        val selected = mutableStateOf(0)
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = style.value),
            ) {
                C001apkComposeTheme(materialYou = false) {
                    MiuixTabRow(
                        tabs = listOf("One", "Two"),
                        selectedTabIndex = selected.value,
                        onTabSelected = { selected.value = it },
                        modifier = Modifier.width(240.dp).testTag("tabs"),
                        withContour = hint.value,
                    )
                }
            }
        }
        fun snapshot(): IntArray {
            compose.mainClock.advanceTimeBy(500)
            val pixels = compose.onNodeWithTag("tabs").captureToImage().toPixelMap()
            return IntArray(pixels.width * pixels.height) { index ->
                pixels[index % pixels.width, index / pixels.width].toArgb()
            }
        }
        val classic = snapshot()
        compose.runOnIdle { hint.value = true }
        assertArrayEquals(classic, snapshot())
        compose.runOnIdle { style.value = TopBarStyle.ClassicContour; hint.value = false }
        val contour = snapshot()
        compose.runOnIdle { hint.value = true }
        assertArrayEquals(contour, snapshot())
        assertFalse("The two classic styles should render differently", classic.contentEquals(contour))
        compose.onNodeWithText("Two").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, selected.value) }
    }

    @Test fun longTabsKeepPagerSelectionAcrossStyleAndDirectionChanges() {
        val style = mutableStateOf(TopBarStyle.OS4)
        val direction = mutableStateOf(LayoutDirection.Ltr)
        val labels = listOf("第一个较长的标签", "第二个较长的标签", "第三个较长的标签")
        var callbacks = 0
        var pagerInstances = 0
        lateinit var pager: PagerState
        lateinit var jumpToFirst: () -> Unit
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = style.value),
                LocalLayoutDirection provides direction.value,
            ) {
                C001apkComposeTheme(materialYou = false) {
                    val state = rememberPagerState(initialPage = 2, pageCount = { labels.size })
                    val scope = rememberCoroutineScope()
                    remember(state) { ++pagerInstances }
                    pager = state
                    jumpToFirst = { scope.launch { state.scrollToPage(0) } }
                    Column {
                        MiuixTabRow(
                            tabs = labels,
                            selectedTabIndex = state.currentPage,
                            onTabSelected = { index -> callbacks++; scope.launch { state.scrollToPage(index) } },
                            modifier = Modifier.width(240.dp).testTag("tabs"),
                        )
                        HorizontalPager(state, modifier = Modifier.width(240.dp).height(60.dp)) { page ->
                            Text("页面 $page")
                        }
                    }
                }
            }
        }
        for (current in listOf(TopBarStyle.OS4, TopBarStyle.Classic, TopBarStyle.ClassicContour, TopBarStyle.OS4)) {
            compose.runOnIdle { style.value = current }
            compose.onNodeWithText(labels[2]).assertIsDisplayed().assertIsSelected()
            compose.onNodeWithText(labels[2]).performClick()
            compose.runOnIdle { assertEquals(2, pager.currentPage); assertEquals(1, pagerInstances) }
        }
        compose.runOnIdle { direction.value = LayoutDirection.Rtl }
        compose.onNodeWithText(labels[2]).assertIsDisplayed().assertIsSelected()
        compose.runOnIdle { jumpToFirst() }
        compose.onNodeWithText(labels[0]).assertIsDisplayed().assertIsSelected().performClick()
        compose.runOnIdle {
            assertEquals(0, pager.currentPage)
            assertEquals(1, pagerInstances)
            assertEquals(5, callbacks)
        }
    }

    @Test fun overflowingOs4TabsScrollWithoutSelectingAndStillRespondToTap() {
        val labels = listOf("First long label", "Second long label", "Third long label", "Fourth long label")
        val selected = mutableStateOf(0)
        var selectionCount = 0
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                C001apkComposeTheme(materialYou = false) {
                    MiuixTabRow(
                        tabs = labels,
                        selectedTabIndex = selected.value,
                        onTabSelected = { index ->
                            selected.value = index
                            selectionCount++
                        },
                        modifier = Modifier.width(180.dp).testTag("tabs"),
                    )
                }
            }
        }
        repeat(3) { compose.onNodeWithTag("tabs").performTouchInput { swipeLeft() } }
        compose.runOnIdle {
            assertEquals(0, selected.value)
            assertEquals(0, selectionCount)
        }
        compose.onNodeWithText(labels.last()).assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithText(labels.last()).assertIsSelected()
        compose.runOnIdle { assertEquals(1, selectionCount) }
    }

    @Test fun compactOs4TabsCanDragDirectlyToAnotherPage() {
        val labels = listOf("One", "Two", "Three")
        lateinit var pager: PagerState
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                C001apkComposeTheme(materialYou = false) {
                    pager = rememberPagerState { labels.size }
                    Column {
                        MiuixPagerTabRow(labels, pager, Modifier.width(300.dp).testTag("tabs"))
                        HorizontalPager(pager, modifier = Modifier.width(300.dp).height(60.dp)) { page ->
                            Text("Page $page")
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("tabs").performTouchInput {
            swipe(Offset(width / 6f, centerY), Offset(width * 5f / 6f, centerY))
        }
        compose.onNodeWithText(labels.last()).assertIsSelected()
        compose.runOnIdle { assertEquals(2, pager.settledPage) }
    }

    @Test fun pagerTabsKeepRequestedSelectionDuringAnimationAndAcceptAnotherTap() {
        val labels = listOf("One", "Two", "Three", "Four")
        var reselectCount = 0
        lateinit var pager: PagerState
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                C001apkComposeTheme(materialYou = false) {
                    pager = rememberPagerState { labels.size }
                    Column {
                        MiuixPagerTabRow(labels, pager, Modifier.width(300.dp), onReselect = { reselectCount++ })
                        HorizontalPager(pager, modifier = Modifier.width(300.dp).height(60.dp)) { page ->
                            Text("Page $page")
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithText("Four").performClick()
            compose.mainClock.advanceTimeUntil { pager.currentPage in 1..2 }
            compose.onNodeWithText("Four").assertIsSelected()
            compose.onNodeWithText("Two").performClick()
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("Two").assertIsSelected()
        } finally {
            compose.mainClock.autoAdvance = true
        }
        compose.runOnIdle {
            assertEquals(1, pager.settledPage)
            assertEquals(0, reselectCount)
        }
        compose.onNodeWithText("Two").performClick()
        compose.runOnIdle { assertEquals(1, reselectCount) }
    }

    private fun verifyBackdrop(contour: Boolean) {
        val blur = mutableStateOf(false)
        val dark = mutableStateOf(false)
        val background = mutableStateOf(Color.Blue)
        val selected = mutableStateOf(0)
        val style = mutableStateOf(TopBarStyle.Classic)
        lateinit var view: View
        compose.setContent {
            val localView = LocalView.current
            SideEffect { view = localView }
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = style.value),
            ) {
                C001apkComposeTheme(materialYou = false, darkTheme = dark.value) {
                    MiuixScaffold(
                        blurBars = blur.value,
                        topBar = {
                            Column {
                                MiuixTopBar(title = "标签背景")
                                MiuixTabRow(
                                    tabs = listOf("One", "Two"),
                                    selectedTabIndex = selected.value,
                                    onTabSelected = { selected.value = it },
                                    withContour = contour,
                                )
                            }
                        },
                    ) {
                        // Simulate content that has scrolled underneath the header.
                        Box(Modifier.fillMaxSize().background(background.value))
                    }
                }
            }
        }

        fun sample(): Pair<Int, Int> {
            compose.waitForIdle()
            SystemClock.sleep(400)
            val offset = IntArray(2)
            compose.runOnIdle { view.getLocationOnScreen(offset) }
            val bitmap = checkNotNull(
                InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot(),
            )
            fun Bitmap.tabPixel(label: String): Int {
                val bounds = compose.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
                // Sample below the label, away from glyphs and rounded edges.
                return getPixel(
                    offset[0] + bounds.center.x.toInt(),
                    offset[1] + (bounds.bottom - 8).toInt(),
                )
            }
            return try { bitmap.tabPixel("One") to bitmap.tabPixel("Two") }
                finally { bitmap.recycle() }
        }

        for (currentStyle in listOf(TopBarStyle.Classic, TopBarStyle.ClassicContour, TopBarStyle.OS4)) {
            compose.runOnIdle {
                style.value = currentStyle
                if (currentStyle != TopBarStyle.Classic) assertEquals(1, selected.value)
                selected.value = 0
            }
            for (night in listOf(false, true)) {
                compose.runOnIdle {
                    dark.value = night
                    blur.value = false
                    background.value = Color.Blue
                }
                val opaque = sample()
                compose.runOnIdle { blur.value = true }
                val blue = sample()
                compose.runOnIdle { background.value = Color.Red }
                val red = sample()
                assertEquals("Selected tab must keep its solid default color", opaque.first, blue.first)
                assertEquals("Background changes must not tint the selected tab", blue.first, red.first)
                assertTrue("Blue content must reach the row backdrop", AndroidColor.blue(blue.second) > AndroidColor.red(blue.second) + 15)
                assertTrue("Backdrop must update with red content", AndroidColor.red(red.second) > AndroidColor.blue(red.second) + 15)
                compose.runOnIdle { blur.value = false }
                assertEquals("Disabling blur must restore the default surface", opaque, sample())
            }
            compose.onNodeWithText("Two").performClick()
            compose.runOnIdle { assertEquals(1, selected.value) }
        }
    }
}
