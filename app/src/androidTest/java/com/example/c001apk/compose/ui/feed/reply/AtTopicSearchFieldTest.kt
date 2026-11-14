package com.example.c001apk.compose.ui.feed.reply

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertTrue
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class AtTopicSearchFieldTest {
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
    fun searchIconHasSpaceInsideTheField() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                AtTopicSearchField(keyword = "", title = "搜索用户", onKeywordChange = {})
            }
        }
        val field = compose.onNodeWithTag("at_search_field", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val icon = compose.onNodeWithTag("at_search_icon", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val minimumInset = with(compose.density) { 12.dp.toPx() }
        assertTrue(icon.left - field.left >= minimumInset)
        assertTrue(icon.right < field.right)
    }
}
