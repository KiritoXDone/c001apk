package com.example.c001apk.compose.ui.feed.reply

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.RecentAtUser
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class AtUserSuggestionsTest {
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
    fun recentUsersAppearFirstAndAreNotRepeatedInFollowedUsers() {
        val recent = listOf(RecentAtUser(id = 1, avatar = "", username = "小雨"))
        val followed = listOf("小雨", "山海").map { name ->
            Gson().fromJson("""{"entityType":"user","username":"$name"}""", HomeFeedResponse.Data::class.java)
        }
        val selected = mutableListOf<String>()
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                AtUserSuggestions(
                    recentUsers = recent,
                    state = LoadingState.Success(followed),
                    footerState = FooterState.End,
                    stopAutomaticLoading = true,
                    onLoadMore = {},
                    onSelectUser = { selected.add(it.username) },
                )
            }
        }
        val recentTitle = compose.onNodeWithText("最近 @").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val recentUser = compose.onNodeWithText("小雨").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val followTitle = compose.onNodeWithText("关注用户").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val followedUser = compose.onNodeWithText("山海").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(recentTitle.top < recentUser.top && recentUser.top < followTitle.top && followTitle.top < followedUser.top)
        compose.onAllNodesWithText("小雨", useUnmergedTree = true).assertCountEquals(1)
        compose.onNodeWithText("小雨").performClick()
        compose.onNodeWithText("山海").performClick()
        compose.runOnIdle { assertEquals(listOf("小雨", "山海"), selected) }
    }
}
