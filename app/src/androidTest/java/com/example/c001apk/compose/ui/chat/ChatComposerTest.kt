package com.example.c001apk.compose.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class ChatComposerTest {
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
    fun composerKeepsEmojiAttachmentsAndSendActions() {
        val actions = mutableListOf<String>()
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                Column(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background)) {
                    Spacer(Modifier.weight(1f))
                    ChatComposerContent(
                    modifier = Modifier.fillMaxWidth().background(MiuixTheme.colorScheme.surfaceContainer)
                        .navigationBarsPadding().imePadding(),
                    recentList = emptyList(),
                    onUpdateRecentEmoji = {},
                    onPickImage = { actions.add("image") },
                    onPickOtherImage = { actions.add("file") },
                    onSendMessage = { actions.add(it) },
                    clearText = false,
                    resetClear = {},
                    clearFocus = false,
                    resetFocus = {},
                    onClearFocus = {},
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("选择表情").performClick()
        val emoji = compose.onNodeWithContentDescription("收起表情").assertIsDisplayed()
        val emojiBounds = emoji.fetchSemanticsNode().boundsInRoot
        val inputBounds = compose.onNodeWithTag("chat_composer_input", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val minGap = with(compose.density) { 4.dp.toPx() }
        val alignmentTolerance = with(compose.density) { 2.dp.toPx() }
        assertTrue(inputBounds.left - emojiBounds.right >= minGap)
        assertEquals(inputBounds.center.y, emojiBounds.center.y, alignmentTolerance)
        emoji.performClick()
        compose.onNodeWithContentDescription("选择图片").performClick()
        compose.onNodeWithContentDescription("从文件选择图片").performClick()
        var enteredText = ""
        onView(withHint("写私信...")).perform(click(), typeText("hello"))
        onView(withHint("写私信...")).check { view, _ ->
            enteredText = (view as android.widget.EditText).text.toString()
            assertTrue(enteredText.isNotBlank())
        }
        compose.onNodeWithContentDescription("发送").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf("image", "file", enteredText), actions) }
    }
}
