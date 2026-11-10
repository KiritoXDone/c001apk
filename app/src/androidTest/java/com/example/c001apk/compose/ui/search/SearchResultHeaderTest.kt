package com.example.c001apk.compose.ui.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class SearchResultHeaderTest {
    private val compose = createComposeRule()

    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val appId = instrumentation.targetContext.packageName
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n $appId/androidx.activity.ComponentActivity",
            ).use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        }
    }).around(compose)

    private fun header(onSearch: (String) -> Unit) {
        compose.setContent {
            var query by rememberSaveable { mutableStateOf("Pixel 9") }
            C001apkComposeTheme(materialYou = false) {
                MiuixScaffold(topBar = {
                    SearchResultHeader(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = onSearch,
                        onBackClick = { error("Editing must not navigate back") },
                        subtitle = "话题: 手机摄影",
                    )
                }) { }
            }
        }
    }

    private fun SemanticsNodeInteraction.assertQuery(value: String) =
        assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(value)))

    @Test
    fun pausedSearchDoesNotRestoreAnInputConnection() {
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this).apply {
                currentState = Lifecycle.State.RESUMED
            }
        }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                C001apkComposeTheme(materialYou = false) {
                    SearchResultHeader("Pixel 9", {}, {}, {}, "")
                }
            }
        }
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.onNodeWithContentDescription("搜索关键词，点击编辑").performClick()
        compose.onNode(hasSetTextAction()).assertIsFocused()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.STARTED }
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.onNodeWithContentDescription("搜索关键词，点击编辑").performClick()
        compose.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun editsInPlaceAndImeSearchPreservesQueryForNextEdit() {
        val submitted = mutableListOf<String>()
        header(submitted::add)
        compose.onNodeWithContentDescription("搜索关键词，点击编辑").performClick()
        val field = compose.onNode(hasSetTextAction())
        field.assertIsFocused()
        field.performTextInputSelection(TextRange(3))
        field.performTextInput("+")
        field.assertQuery("Pix+el 9")
        compose.runOnIdle { assertEquals(emptyList<String>(), submitted) }
        field.performImeAction()
        compose.waitForIdle()
        compose.onNodeWithText("Pix+el 9").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("Pix+el 9"), submitted) }
        compose.onNodeWithContentDescription("搜索关键词，点击编辑").performClick()
        field.performTextReplacement("C++ %20 中文")
        field.performImeAction()
        compose.waitForIdle()
        compose.onNodeWithText("C++ %20 中文").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("Pix+el 9", "C++ %20 中文"), submitted) }
    }

    @Test
    fun titleHasNoSearchButtonAndEmptyQueryDoesNotSubmit() {
        val submitted = mutableListOf<String>()
        header(submitted::add)
        compose.onNodeWithText("话题: 手机摄影").assertIsDisplayed()
        compose.onNodeWithContentDescription("搜索").assertDoesNotExist()
        compose.onNodeWithContentDescription("搜索关键词，点击编辑").performClick()
        val field = compose.onNode(hasSetTextAction())
        field.performClick()
        field.performTextClearance()
        field.assertQuery("")
        field.performImeAction()
        compose.runOnIdle { assertEquals(emptyList<String>(), submitted) }
        field.performTextReplacement("user_tag:%s%d + %20")
        field.performImeAction()
        compose.waitForIdle()
        compose.onNodeWithText("user_tag:%s%d + %20").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf("user_tag:%s%d + %20"), submitted) }
    }
}
