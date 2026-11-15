package com.example.c001apk.compose.ui

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.CollapsingTabsState
import com.example.c001apk.compose.ui.component.LocalCollapsingTabs
import com.example.c001apk.compose.ui.component.LocalBarBackdrop
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MiuixHeader
import com.example.c001apk.compose.ui.component.MiuixTabScaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.rememberBarBackdrop
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.component.topBarTap
import com.example.c001apk.compose.ui.component.icons.FlutterProfileIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.glassShadow
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.material.GlassShadows
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

    @Test fun switchingStyleAndBlurKeepsTitleInputFocusedAndDraft() {
        val preferences = mutableStateOf(UserPreferencesCompat.default())
        compose.setContent {
            CompositionLocalProvider(LocalUserPreferences provides preferences.value) {
                MiuixTheme {
                    MiuixScaffold(
                        topBar = {
                            var draft by remember { mutableStateOf("") }
                            MiuixHeader(
                                title = {
                                    BasicTextField(
                                        value = draft,
                                        onValueChange = { draft = it },
                                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("titleInput"),
                                        singleLine = true,
                                    )
                                },
                                centerTitle = true,
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            )
                        },
                    ) { Text("页面内容") }
                }
            }
        }
        compose.onNodeWithTag("titleInput").performClick().performTextInput("draft")
        for (style in listOf(TopBarStyle.OS4, TopBarStyle.Classic, TopBarStyle.ClassicContour, TopBarStyle.OS4)) {
            for (blur in listOf(false, true)) {
                compose.runOnIdle { preferences.value = preferences.value.copy(topBarStyle = style, blurBars = blur) }
                compose.onNodeWithTag("titleInput").assertIsFocused().assertTextEquals("draft")
                compose.onNodeWithText("页面内容").assertExists()
            }
        }
    }

    @SdkSuppress(minSdkVersion = 33)
    @Test fun os4ButtonShadowMatchesBoxWithBlurOnAndOff() {
        val blur = mutableStateOf(false)
        compose.setContent {
            MiuixTheme {
                val backdrop = rememberBarBackdrop(blur.value)
                val shape = GlassShape(GlassTopAppBarDefaults.ButtonSize / 2)
                val fill = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 1f)
                val stroke = if (backdrop != null) GlassTopAppBarDefaults.buttonStroke() else null
                CompositionLocalProvider(
                    LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
                    LocalBarBackdrop provides backdrop,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.matchParentSize()
                                .then(backdrop?.recordModifier() ?: Modifier)
                                .background(Color.White),
                        )
                        Row(Modifier.align(Alignment.Center)) {
                            @Composable fun ShadowSample(tag: String, content: @Composable () -> Unit) {
                                Box(
                                    Modifier.size(90.dp).background(Color.White).testTag(tag),
                                    contentAlignment = Alignment.Center,
                                ) { content() }
                            }
                            ShadowSample("actual") { MiuixToolbarIconButton(onClick = {}) {} }
                            ShadowSample("boxReference") {
                                GlassIconButton(
                                    onClick = {},
                                    modifier = Modifier.glassShadow(shape, GlassShadows.Regular),
                                    inheritTopBarMaterial = false,
                                    backdrop = backdrop?.rawBackdrop,
                                    shape = shape,
                                    fill = fill,
                                    stroke = stroke,
                                    shadow = null,
                                ) {}
                            }
                            ShadowSample("noShadow") {
                                GlassIconButton(
                                    onClick = {},
                                    inheritTopBarMaterial = false,
                                    backdrop = backdrop?.rawBackdrop,
                                    shape = shape,
                                    fill = fill,
                                    stroke = stroke,
                                    shadow = null,
                                ) {}
                            }
                        }
                    }
                }
            }
        }
        fun edge(tag: String): Color {
            val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
            val offset = with(compose.density) { 24.dp.roundToPx() }
            return pixels[pixels.width / 2 + offset, pixels.height / 2]
        }
        for (blurred in listOf(false, true)) {
            compose.runOnIdle { blur.value = blurred }
            compose.mainClock.advanceTimeBy(400)
            val actual = edge("actual")
            val reference = edge("boxReference")
            val noShadow = edge("noShadow")
            assertTrue("Shadow must remain visible with blur=$blurred", noShadow.red - actual.red > 0.01f)
            assertEquals("Only the Box reference shadow should be drawn with blur=$blurred", reference.red, actual.red, 0.004f)
        }
    }

    @SdkSuppress(minSdkVersion = 33)
    @Test fun os4ButtonShadowExtendsBelowTheTopBar() {
        lateinit var actionDensity: Density
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = TopBarStyle.OS4),
            ) {
                MiuixTheme {
                    MiuixScaffold(
                        modifier = Modifier.fillMaxSize().testTag("scene"),
                        containerColor = Color.White,
                        blurBars = false,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            MiuixTopBar(
                                title = "我的",
                                modifier = Modifier.testTag("bar"),
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                actions = {
                                    val density = LocalDensity.current
                                    SideEffect { actionDensity = density }
                                    MiuixToolbarIconButton(onClick = {}) {
                                        Icon(FlutterProfileIcons.Settings, contentDescription = "设置")
                                    }
                                },
                            )
                        },
                    ) { Box(Modifier.fillMaxSize().background(Color.White)) }
                }
            }
        }
        compose.mainClock.advanceTimeBy(400)
        val scene = compose.onNodeWithTag("scene").fetchSemanticsNode().boundsInRoot
        val bar = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot
        val button = compose.onNode(hasContentDescription("设置") and hasClickAction()).fetchSemanticsNode().boundsInRoot
        val pixels = compose.onNodeWithTag("scene").captureToImage().toPixelMap()
        val below = (bar.bottom - scene.top).toInt() + with(actionDensity) { 2.dp.roundToPx() }
        val shadowX = (button.center.x - scene.left).toInt()
        val clearX = (bar.center.x - scene.left).toInt()
        val shadow = pixels[shadowX, below]
        val background = pixels[clearX, below]
        assertEquals("Bar height must stay unchanged", with(actionDensity) { 52.dp.roundToPx().toFloat() }, bar.height, 1f)
        assertTrue("Shadow must extend below the bar: shadow=$shadow background=$background", background.red - shadow.red > 0.01f)
    }

    @Test fun multipleActionsUseBoxSpacingAndKeepBothActionsClickable() {
        val style = mutableStateOf(TopBarStyle.OS4)
        val calls = mutableListOf<String>()
        lateinit var actionDensity: Density
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(topBarStyle = style.value),
            ) {
                MiuixTheme {
                    MiuixTopBar(
                        title = "我的",
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        actions = {
                            val density = LocalDensity.current
                            SideEffect { actionDensity = density }
                            MiuixToolbarIconButton(onClick = { calls += "账号" }) {
                                Icon(FlutterProfileIcons.Account, contentDescription = "账号")
                            }
                            MiuixToolbarIconButton(onClick = { calls += "设置" }) {
                                Icon(FlutterProfileIcons.Settings, contentDescription = "设置")
                            }
                        },
                    )
                }
            }
        }
        for (current in listOf(TopBarStyle.Classic, TopBarStyle.ClassicContour, TopBarStyle.OS4)) {
            compose.runOnIdle { style.value = current }
            val account = compose.onNode(hasContentDescription("账号") and hasClickAction()).fetchSemanticsNode().boundsInRoot
            val settings = compose.onNode(hasContentDescription("设置") and hasClickAction()).fetchSemanticsNode().boundsInRoot
            val expected = if (current == TopBarStyle.OS4) with(actionDensity) { 8.dp.roundToPx().toFloat() } else 0f
            assertEquals("Action gap for $current: account=$account settings=$settings", expected, settings.left - account.right, 1f)
            compose.onNodeWithContentDescription("账号").performClick()
            compose.onNodeWithContentDescription("设置").performClick()
        }
        compose.runOnIdle { assertEquals(listOf("账号", "设置", "账号", "设置", "账号", "设置"), calls) }
    }

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
