package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixBottomSheet
import com.example.c001apk.compose.ui.main.AppDestination
import com.example.c001apk.compose.ui.main.AppNavHost
import com.example.c001apk.compose.ui.main.AppNavigator
import com.example.c001apk.compose.ui.main.MainPageBackHandler
import com.example.c001apk.compose.ui.main.rememberAppNavigator
import com.example.c001apk.compose.ui.main.rememberMainPageState
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

class PredictiveBackPreferenceTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrument = InstrumentationRegistry.getInstrumentation()
            val pkg = instrument.targetContext.packageName
            instrument.uiAutomation.executeShellCommand(
                "am start -W -n $pkg/androidx.activity.ComponentActivity",
            ).use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        }
    }).around(compose)

    private val enabled = mutableStateOf(true)
    private val inner = mutableStateOf(false)
    private val active = mutableStateOf(true)
    private val back = DirectNavigationEventInput()
    private lateinit var navigator: AppNavigator
    private var backCount = 0
    private var parentBackCount = 0

    private fun event(progress: Float) = NavigationEvent(
        swipeEdge = NavigationEvent.EDGE_LEFT,
        progress = progress,
        touchX = progress * 600f,
        touchY = 500f,
    )

    private fun showDetail(predictive: Boolean) {
        enabled.value = predictive
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(predictiveBack = enabled.value),
            ) {
                MiuixTheme {
                    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
                    DisposableEffect(dispatcher) {
                        dispatcher.addInput(back)
                        onDispose { dispatcher.removeInput(back) }
                    }
                    navigator = rememberAppNavigator()
                    NavigationBackHandler(
                        state = rememberNavigationEventState(NavigationEventInfo.None),
                        onBackCompleted = { parentBackCount++ },
                    )
                    AppNavHost(
                        navigator = navigator,
                        modifier = Modifier.fillMaxSize().testTag("host"),
                        active = active.value,
                        onBack = { backCount++; navigator.popBackStack() },
                    ) { entry ->
                        if (entry.destination != AppDestination.Main) {
                            Box(Modifier.fillMaxSize().testTag("detail-content")) { Text("详情") }
                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                isBackEnabled = inner.value,
                                onBackCompleted = { inner.value = false },
                            )
                        } else Text("主页")
                    }
                }
            }
        }
        compose.runOnIdle { navigator.navigate(AppDestination.Feed("test")) }
        compose.waitForIdle()
    }

    private fun showBottomSheet(predictive: Boolean) {
        enabled.value = predictive
        val showing = mutableStateOf(true)
        compose.setContent {
            CompositionLocalProvider(
                LocalUserPreferences provides UserPreferencesCompat.default().copy(predictiveBack = enabled.value),
            ) {
                MiuixTheme {
                    if (showing.value) {
                        MiuixBottomSheet(onDismissRequest = { backCount++; showing.value = false }) {
                            val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
                            DisposableEffect(dispatcher) {
                                dispatcher.addInput(back)
                                onDispose { dispatcher.removeInput(back) }
                            }
                            Box(Modifier.fillMaxWidth().height(300.dp).testTag("sheet-content")) {
                                Text("查看更多回复")
                            }
                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                isBackEnabled = inner.value,
                                onBackCompleted = { inner.value = false },
                            )
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun disabledBottomSheetKeepsStillDuringBackProgress() {
        showBottomSheet(false)
        val before = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val during = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        assertEquals("Disabled sheet must not translate horizontally", before.left, during.left, 1f)
        assertEquals("Disabled sheet must not translate vertically", before.top, during.top, 1f)
        assertEquals("Disabled sheet must not scale horizontally", before.width, during.width, 1f)
        assertEquals("Disabled sheet must not scale vertically", before.height, during.height, 1f)
        compose.runOnIdle { back.backCancelled() }
        compose.runOnIdle { assertEquals(0, backCount) }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)); back.backCompleted() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, backCount) }
        compose.onNodeWithTag("sheet-content").assertDoesNotExist()
    }

    @Test
    fun enabledBottomSheetStillPreviewsAndCancels() {
        showBottomSheet(true)
        val before = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val during = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        assertTrue("Enabled sheet should follow back progress", during.top > before.top + 10f)
        compose.runOnIdle { back.backCancelled() }
        compose.waitForIdle()
        val cancelled = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        assertEquals(before.top, cancelled.top, 1f)
        compose.runOnIdle { assertEquals(0, backCount) }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)); back.backCompleted() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, backCount) }
        compose.onNodeWithTag("sheet-content").assertDoesNotExist()
    }

    @Test
    fun changingBottomSheetPreferenceKeepsInnerHandlerPriority() {
        showBottomSheet(true)
        val before = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { enabled.value = false }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val disabled = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        assertEquals(before.top, disabled.top, 1f)
        compose.runOnIdle { back.backCancelled() }
        compose.runOnIdle { enabled.value = true }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val enabledPreview = compose.onNodeWithTag("sheet-content").fetchSemanticsNode().boundsInRoot
        assertTrue(enabledPreview.top > before.top + 10f)
        compose.runOnIdle { back.backCancelled() }
        compose.waitForIdle()
        compose.runOnIdle { inner.value = true; enabled.value = false }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.4f)); back.backCompleted() }
        compose.runOnIdle { assertFalse(inner.value); assertEquals(0, backCount) }
        compose.onNodeWithTag("sheet-content").assertExists()
        compose.runOnIdle { back.backStarted(event(0f)); back.backCompleted() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, backCount) }
        compose.onNodeWithTag("sheet-content").assertDoesNotExist()
    }

    @Test
    fun disabledGestureKeepsPageStillCancelsSafelyAndCommitsOnlyOnce() {
        showDetail(false)
        val before = compose.onNodeWithTag("detail-content").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val during = compose.onNodeWithTag("detail-content").fetchSemanticsNode().boundsInRoot
        assertEquals(before.left, during.left, 1f)
        compose.runOnIdle { back.backCancelled() }
        compose.runOnIdle { assertTrue(navigator.canGoBack); assertEquals(0, backCount) }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)); back.backCompleted() }
        compose.runOnIdle { assertEquals(AppDestination.Main, navigator.currentDestination); assertEquals(1, backCount) }
    }

    @Test
    fun nativePreviewStillMovesAndCancelsWhenEnabled() {
        showDetail(true)
        val before = compose.onNodeWithTag("detail-content").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.6f)) }
        compose.waitForIdle()
        val during = compose.onNodeWithTag("detail-content").fetchSemanticsNode().boundsInRoot
        assertTrue("Native preview should translate the current page", during.left > before.left + 10f)
        compose.runOnIdle { back.backCancelled() }
        compose.runOnIdle { assertTrue(navigator.canGoBack); assertEquals(0, backCount) }
    }

    @Test
    fun changingPreferenceDoesNotStealBackFromExistingInnerHandler() {
        showDetail(true)
        compose.runOnIdle { inner.value = true; enabled.value = false }
        compose.runOnIdle { back.backStarted(event(0f)); back.backProgressed(event(.4f)); back.backCompleted() }
        compose.runOnIdle { assertFalse(inner.value); assertTrue(navigator.canGoBack); assertEquals(0, backCount) }
        compose.runOnIdle { enabled.value = true }
        compose.runOnIdle { enabled.value = false }
        compose.runOnIdle { back.backCompleted() }
        compose.runOnIdle { assertEquals(AppDestination.Main, navigator.currentDestination); assertEquals(1, backCount) }
    }

    @Test
    fun inactivePaneDoesNotConsumeBackEvenWhenPreviewIsDisabled() {
        showDetail(false)
        compose.runOnIdle { active.value = false }
        compose.runOnIdle { back.backCompleted() }
        compose.runOnIdle {
            assertEquals(1, parentBackCount)
            assertTrue(navigator.canGoBack)
            assertEquals(0, backCount)
        }
        compose.runOnIdle { active.value = true }
        compose.runOnIdle { back.backCompleted() }
        compose.runOnIdle { assertEquals(AppDestination.Main, navigator.currentDestination); assertEquals(1, backCount) }
    }

    @Test
    fun disablingPreferenceAlsoDisablesInContentDismissal() {
        showDetail(false)
        compose.onNodeWithTag("host").performTouchInput {
            swipe(Offset(width * .15f, centerY), Offset(width * .85f, centerY), durationMillis = 800)
        }
        compose.runOnIdle { assertTrue(navigator.canGoBack); assertEquals(0, backCount) }
    }

    @Test
    fun disabledPredictiveHandlerAtRootDoesNotStealReturnToHome() {
        enabled.value = false
        lateinit var mainPage: com.example.c001apk.compose.ui.main.MainPageState
        compose.setContent {
            CompositionLocalProvider(LocalUserPreferences provides UserPreferencesCompat.default().copy(predictiveBack = false)) {
                MiuixTheme {
                    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
                    DisposableEffect(dispatcher) {
                        dispatcher.addInput(back)
                        onDispose { dispatcher.removeInput(back) }
                    }
                    navigator = rememberAppNavigator()
                    val page = rememberMainPageState()
                    mainPage = page
                    MainPageBackHandler(navigator, page, false) { page.select(it) }
                    AppNavHost(navigator) { Text("主页") }

                    androidx.compose.runtime.LaunchedEffect(Unit) { page.select(2) }
                }
            }
        }
        compose.runOnIdle { assertEquals(2, mainPage.selectedIndex); back.backCompleted() }
        compose.runOnIdle { assertEquals(0, mainPage.selectedIndex); assertFalse(navigator.canGoBack) }
    }
}
