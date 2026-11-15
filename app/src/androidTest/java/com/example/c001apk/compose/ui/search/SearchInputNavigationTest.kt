package com.example.c001apk.compose.ui.search

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

    private fun showSearch() {
        compose.setContent {
            MiuixTheme {
                navigation = rememberAppNavigator(AppDestination.Search(null, null, null))
                AppNavHost(navigation, Modifier.fillMaxSize()) { entry ->
                    when (val destination = entry.destination) {
                        is AppDestination.Search -> {
                            var expanded by rememberSaveable { mutableStateOf(false) }
                            MiuixSearchField(
                                query = destination.query,
                                onQueryChange = { navigation.updateSearchInput(entry.id, it) },
                                onSearch = {},
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
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
