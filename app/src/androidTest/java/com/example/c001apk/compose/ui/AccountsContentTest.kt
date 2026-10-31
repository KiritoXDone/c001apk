package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.ui.accounts.AccountsContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.theme.MiuixTheme

class AccountsContentTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)


    @Test fun removingAnAccountFromItsMenuDoesNotSwitchAccounts() {
        val accounts = listOf(
            SavedAccount("100001", "山间来信", "test-token"),
            SavedAccount("100002", "林间晚风", "test-token"),
            SavedAccount("100003", "小岛日记", ""),
        )
        var selected = ""
        var removed = ""
        compose.setContent {
            MiuixTheme {
                AccountsContent(accounts, "100001", false, null, PaddingValues(),
                    rememberLazyListState(), {}, {}, { selected = it.uid }, { removed = it.uid })
            }
        }
        compose.onNodeWithText("当前账号").assertIsDisplayed()
        compose.onNodeWithText("登录已失效，点击重新登录").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("更多操作")[1].performClick()
        compose.onNodeWithText("删除账号").performClick()
        compose.runOnIdle {
            assertEquals("100002", removed)
            assertEquals("", selected)
        }
        compose.onNodeWithText("林间晚风").performClick()
        compose.runOnIdle { assertEquals("100002", selected) }
    }

    @Test fun guestWithoutSavedAccountsCanStillAddAnAccount() {
        var logins = 0
        compose.setContent {
            MiuixTheme {
                AccountsContent(emptyList(), null, false, null, PaddingValues(),
                    rememberLazyListState(), { logins++ }, {}, {}, {})
            }
        }
        compose.onNodeWithText("暂无保存的账号").assertIsDisplayed()
        compose.onNodeWithText("游客模式").assertHasNoClickAction()
        compose.onNodeWithText("添加账号").performClick()
        compose.runOnIdle { assertEquals(1, logins) }
    }
}
