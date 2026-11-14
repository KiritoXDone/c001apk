package com.example.c001apk.compose.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.cards.MessageCard
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class MessageCardTest {
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
    fun conversationRowsShowUnreadCountAndKeepTheirActions() {
        val first = Gson().fromJson(
            """{"ukey":"first","messageUid":"1","messageUsername":"小明","message":"你好"}""",
            HomeFeedResponse.Data::class.java,
        )
        val second = Gson().fromJson(
            """{"ukey":"second","messageUid":"2","messageUsername":"小雨","unreadNum":108,"message":"收到"}""",
            HomeFeedResponse.Data::class.java,
        )
        val opened = mutableListOf<String>()
        val managed = mutableListOf<String>()
        compose.setContent {
            C001apkComposeTheme(materialYou = false, fontScale = 1.3f) {
                Column {
                    listOf(first, second).forEach { message ->
                        MessageCard(
                            data = message,
                            onOpenLink = { _, _ -> },
                            onViewUser = {},
                            onViewChat = { ukey, _, _ -> opened.add(ukey) },
                            onHandleMessage = { ukey, _ -> managed.add(ukey) },
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("99+", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("null", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("小明").performClick()
        compose.onNodeWithText("小雨").performTouchInput { longClick() }
        compose.runOnIdle {
            assertEquals(listOf("first"), opened)
            assertEquals(listOf("second"), managed)
        }
    }
}
