package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.FeedListEntry
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.theme.MiuixTheme

class CollectionRemoveMenuTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)


    private val post = Gson().fromJson(
        """{"id":"123","entityId":"123","uid":"author","username":"收藏的动态","entityType":"feed","message":"收藏菜单测试"}""",
        HomeFeedResponse.Data::class.java,
    )

    @Test fun collectionActionTargetsThePostWithoutOpeningOrDeletingIt() {
        var removed = ""
        var opened = false
        var deleted = false
        compose.setContent {
            MiuixTheme {
                Column(Modifier.statusBarsPadding()) {
                    FeedListEntry(item = post, index = 0, onViewUser = {},
                        onViewFeed = { _, _ -> opened = true }, onOpenLink = { _, _ -> }, onCopyText = {},
                        onBlockUser = { _, _ -> }, onDelete = { _, _, _ -> deleted = true },
                        onRemoveFromCollection = { removed = it })
                }
            }
        }
        compose.onNodeWithContentDescription("更多操作").performClick()
        compose.onNodeWithText("取消收藏").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals("123", removed)
            assertEquals(false, opened)
            assertEquals(false, deleted)
        }
    }

    @Test fun actionIsAbsentOutsideOwnedCollectionsAndDisabledWhileRemoving() {
        val owned = mutableStateOf(false)
        compose.setContent {
            MiuixTheme {
                Column(Modifier.statusBarsPadding()) {
                    FeedListEntry(item = post, index = 0, onViewUser = {},
                        onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> }, onCopyText = {},
                        onBlockUser = { _, _ -> },
                        onRemoveFromCollection = if (owned.value) ({ _: String -> }) else null,
                        removingFromCollection = setOf("123"))
                }
            }
        }
        compose.onNodeWithContentDescription("更多操作").performClick()
        compose.onNodeWithText("取消收藏").assertDoesNotExist()
        compose.runOnIdle { owned.value = true }
        compose.onNodeWithText("取消收藏中…").assertIsDisplayed().assertIsNotEnabled()
    }
}
