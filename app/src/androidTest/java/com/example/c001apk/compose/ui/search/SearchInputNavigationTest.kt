package com.example.c001apk.compose.ui.search

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.component.MiuixSearchField
import com.example.c001apk.compose.ui.main.AppDestination
import com.example.c001apk.compose.ui.main.AppNavHost
import com.example.c001apk.compose.ui.main.AppNavigator
import com.example.c001apk.compose.ui.main.rememberAppNavigator
import com.example.c001apk.compose.ui.main.updateSearchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

class SearchInputNavigationTest {
    private val compose = createComposeRule()
    private lateinit var navigation: AppNavigator
    private var searchFocused = false
    private var isKeyboardVisible = false
    private var cancelInput: () -> Unit = {}

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

    @OptIn(ExperimentalLayoutApi::class)
    private fun showSearch(shouldAutoFocus: Boolean = false, shouldStartFromMain: Boolean = false) {
        compose.setContent {
            MiuixTheme {
                val isImeVisible = WindowInsets.isImeVisible
                SideEffect { isKeyboardVisible = isImeVisible }
                navigation = rememberAppNavigator(
                    if (shouldStartFromMain) AppDestination.Main else AppDestination.Search(null, null, null),
                )
                AppNavHost(navigation, Modifier.fillMaxSize()) { entry ->
                    when (val destination = entry.destination) {
                        is AppDestination.Search -> {
                            var expanded by rememberSaveable { mutableStateOf(false) }
                            val focusManager = LocalFocusManager.current
                            val keyboard = LocalSoftwareKeyboardController.current
                            cancelInput = {
                                expanded = false
                                focusManager.clearFocus()
                                keyboard?.hide()
                            }
                            MiuixSearchField(
                                query = destination.query,
                                onQueryChange = { navigation.updateSearchInput(entry.id, it) },
                                onSearch = { keyword ->
                                    cancelInput()
                                    navigation.navigate(AppDestination.SearchResult(keyword, null, null, null))
                                },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                shouldAutoFocus = shouldAutoFocus,
                                modifier = Modifier.testTag("searchInput")
                                    .onFocusChanged { searchFocused = it.isFocused },
                            )
                        }
                        else -> Text("搜索结果")
                    }
                }
            }
        }
    }

    private fun SemanticsNodeInteraction.assertQuery(value: String) =
        assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(value)))

    @Test
    fun newSearchVisitsShowKeyboardWithoutTappingTheField() {
        showSearch(shouldAutoFocus = true, shouldStartFromMain = true)
        val destinations = listOf(
            AppDestination.Search(null, null, null),
            AppDestination.Search("测试话题", "feed", "测试话题"),
        )
        destinations.forEach { destination ->
            compose.runOnIdle { navigation.navigate(destination) }
            compose.waitUntil(10_000) { searchFocused && isKeyboardVisible }
            compose.onNodeWithTag("searchInput").assertIsFocused().performTextInput("test")
            compose.runOnIdle { cancelInput() }
            compose.waitUntil(5_000) { !searchFocused && !isKeyboardVisible }
            compose.mainClock.advanceTimeBy(500)
            compose.onNodeWithTag("searchInput").assertIsNotFocused()
            compose.runOnIdle { navigation.popBackStack() }
            compose.waitForIdle()
        }
    }

    @Test
    fun returningFromResultsDoesNotAutoFocusAgain() {
        showSearch(shouldAutoFocus = true, shouldStartFromMain = true)
        compose.runOnIdle { navigation.navigate(AppDestination.Search(null, null, null)) }
        compose.waitUntil(10_000) { searchFocused && isKeyboardVisible }
        compose.onNodeWithTag("searchInput").performTextInput("query")
        compose.onNodeWithTag("searchInput").performImeAction()
        compose.waitUntil(5_000) { !isKeyboardVisible }
        compose.runOnIdle { navigation.popBackStack() }
        compose.waitForIdle()
        compose.onNodeWithTag("searchInput").assertIsNotFocused().assertQuery("query")
        compose.runOnIdle { assertFalse(isKeyboardVisible) }
    }

    @Test
    fun changingSearchQueryKeepsFocusAndTheLatestValue() {
        showSearch()
        val field = compose.onNodeWithTag("searchInput")
        field.performClick()
        field.assertIsFocused()
        field.performTextInput("a")
        field.assertIsFocused().assertQuery("a")
        field.performTextInput("中")
        field.assertIsFocused().assertQuery("a中")
        field.performTextReplacement("粘贴 C++ %20")
        field.assertIsFocused().assertQuery("粘贴 C++ %20")
        compose.runOnIdle {
            assertEquals("粘贴 C++ %20", (navigation.currentDestination as AppDestination.Search).query)
        }
    }

    @Test
    fun navigatingToAnotherVisitStillClearsCoveredInputFocus() {
        showSearch()
        compose.onNodeWithTag("searchInput").performClick().assertIsFocused()
        compose.runOnIdle {
            navigation.navigate(AppDestination.SearchResult("query", null, null, null))
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertFalse(searchFocused)
            navigation.popBackStack()
        }
        compose.waitForIdle()
        compose.runOnIdle { assertFalse(searchFocused) }
    }
}
