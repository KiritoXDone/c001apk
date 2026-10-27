package com.example.c001apk.compose.ui.feed

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Download

class FeedTopBarTest {
    private val compose = createComposeRule()
    private val calls = mutableListOf<String>()

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

    private fun header() {
        val atComments = mutableStateOf(false)
        compose.setContent {
            // Capture a snapshot so a stale gesture callback cannot pass the toggle test.
            val showingComments = atComments.value
            C001apkComposeTheme(materialYou = false) {
                MiuixScaffold(topBar = {
                    FeedTopBar(
                        title = "动态",
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        onTitleClick = {
                            calls += if (showingComments) "top" else "comments"
                            atComments.value = !showingComments
                        },
                        navigationIcon = { BackButton { calls += "back" } },
                        actions = {
                            IconButton(onClick = { calls += "save" }) {
                                Icon(MiuixIcons.Download, contentDescription = "保存内容")
                            }
                            MoreMenuButton { calls += "menu" }
                        },
                    )
                }) { TopBarScrollEffect { calls += "doubleTop" } }
            }
        }
    }

    @Test fun titleTapsToggleAndAccessibilityActionUsesCurrentCallback() {
        header()
        repeat(2) {
            compose.onNodeWithText("动态").performTouchInput { click() }
            compose.mainClock.advanceTimeBy(400)
            compose.waitForIdle()
        }
        compose.runOnIdle { assertEquals(listOf("comments", "top"), calls) }
        compose.onNode(SemanticsMatcher("Title navigation action") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label == "切换动态与评论位置"
        }).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(listOf("comments", "top", "comments"), calls) }
    }

    @Test fun buttonsDoNotAlsoTriggerTitleNavigation() {
        header()
        for (label in listOf("返回", "保存内容", "更多操作")) {
            compose.onNodeWithContentDescription(label).performTouchInput { click() }
        }
        compose.runOnIdle { assertEquals(listOf("back", "save", "menu"), calls) }
    }

    @Test fun doubleTapReturnsToTopWithoutSingleTapNavigation() {
        header()
        compose.onNodeWithText("动态").performTouchInput { doubleClick() }
        compose.runOnIdle { assertEquals(listOf("doubleTop"), calls) }
    }

    @Test fun draggingAwayAndBackDoesNotCountAsTitleTap() {
        header()
        compose.onNodeWithText("动态").performTouchInput {
            down(center)
            moveTo(center + Offset(100f, 0f), delayMillis = 100)
            moveTo(center, delayMillis = 100)
            up()
        }
        compose.runOnIdle { assertEquals(emptyList<String>(), calls) }
        compose.onNodeWithText("动态").performTouchInput { click() }
        compose.mainClock.advanceTimeBy(400)
        compose.runOnIdle { assertEquals(listOf("comments"), calls) }
    }
}
