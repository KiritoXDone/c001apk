package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.cards.FeedCard
import com.example.c001apk.compose.ui.feed.FeedDetailAuthor
import com.google.gson.Gson
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.theme.MiuixTheme

class FeedVisibilityNoticeTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)


    private fun post(status: Int?, name: String) = Gson().fromJson(
        """{"id":"123","entityType":"feed","publish_status":$status,"username":"$name","message":"测试动态","dateline":1793430000}""",
        HomeFeedResponse.Data::class.java,
    )

    @Test fun privateListNoticeIsAboveAuthorAndPublicPostHasNone() {
        compose.setContent {
            MiuixTheme {
                Column(Modifier.statusBarsPadding().padding(12.dp)) {
                    for ((status, name) in listOf(1 to "私密动态作者", 0 to "公开动态作者")) {
                        FeedCard(data = post(status, name), isFeedContent = false,
                            onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> },
                            onCopyText = {}, onBlockUser = {})
                    }
                }
            }
        }
        compose.onAllNodesWithText("仅自己可见", useUnmergedTree = true).assertCountEquals(1)
        val notice = compose.onNodeWithText("仅自己可见", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val author = compose.onNodeWithText("私密动态作者", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(notice.bottom <= author.top)
        screenshot("private-list.png")
    }

    @Test fun detailNoticeStaysAboveAuthorWithLargeTextAndNarrowWidth() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MiuixTheme {
                    Column(Modifier.statusBarsPadding().width(280.dp).padding(12.dp)) {
                        val data = post(1, "私密动态作者")
                        FeedDetailAuthor(data, {})
                        FeedCard(data = data, isFeedContent = true,
                            onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> },
                            onCopyText = {}, onBlockUser = {})
                    }
                }
            }
        }
        compose.onAllNodesWithText("仅自己可见", useUnmergedTree = true).assertCountEquals(1)
        compose.onNodeWithText("仅自己可见", useUnmergedTree = true).assertIsDisplayed()
        val notice = compose.onNodeWithText("仅自己可见", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val author = compose.onNodeWithText("私密动态作者", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(notice.bottom <= author.top)
        screenshot("private-detail.png")
    }


    @Test fun ownFeedDeleteIsShownInTheOverflowMenu() {
        var deleted = false
        compose.setContent {
            MiuixTheme {
                Column(Modifier.statusBarsPadding().padding(12.dp)) {
                    FeedCard(data = post(1, "我的动态").copy(uid = com.example.c001apk.compose.util.CookieUtil.uid),
                        isFeedContent = false, onViewUser = {}, onViewFeed = { _, _ -> },
                        onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = {},
                        onDelete = { _, _, _ -> deleted = true })
                }
            }
        }
        compose.onNodeWithContentDescription("更多操作").performClick()
        compose.onNodeWithText("删除").assertIsDisplayed()
        screenshot("private-delete-menu.png")
        compose.onNodeWithText("删除").performClick()
        compose.runOnIdle { assertTrue(deleted) }
    }

    private fun screenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureUi") != "true") return
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        java.io.File(instrumentation.targetContext.cacheDir, name).outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
