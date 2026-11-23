package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixNavigationScaffold
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.glass.ContactsBook
import top.yukonga.miuix.kmp.icon.glass.HomeOutline

class NavigationAppearanceTest {
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
    fun navigationAndBadgeSurviveIndependentAppearanceChanges() {
        val preferences = mutableStateOf(UserPreferencesCompat.default())
        val selected = mutableIntStateOf(0)
        val unread = mutableIntStateOf(3)
        var bodyInstances = 0
        var selections = 0
        compose.setContent {
            CompositionLocalProvider(LocalUserPreferences provides preferences.value) {
                C001apkComposeTheme(materialYou = false) {
                    MiuixNavigationScaffold(
                        items = listOf(
                            MiuixIcons.Glass.HomeOutline to "首页",
                            MiuixIcons.FavoritesFill to "关注",
                            MiuixIcons.Glass.ContactsBook to "我的",
                        ),
                        selectedIndex = selected.intValue,
                        wide = false,
                        unreadCount = unread.intValue,
                        onSelect = { selected.intValue = it; selections++ },
                    ) { padding ->
                        val instance = remember { ++bodyInstances }
                        Box(Modifier.fillMaxSize().padding(padding)) { Text("页面内容 $instance") }
                    }
                }
            }
        }
        val modes = listOf<Pair<BottomBarStyle?, Boolean>>(
            null to false,
            null to true,
            BottomBarStyle.OS4 to false,
            BottomBarStyle.Classic to true,
            BottomBarStyle.Floating to false,
        )
        for ((style, legacyFloating) in modes) {
            for (blur in listOf(false, true, false)) {
                compose.runOnIdle {
                    preferences.value = preferences.value.copy(
                        floatingNavigationBar = legacyFloating,
                        bottomBarStyle = style,
                        blurBars = blur,
                    )
                }
                val floating = style == BottomBarStyle.Floating || (style == null && legacyFloating)
                val glass = style == BottomBarStyle.OS4
                (if (floating || glass) compose.onNodeWithContentDescription("关注")
                    else compose.onNodeWithText("关注")).performClick()
                compose.runOnIdle { assertEquals(1, selected.intValue) }
                if (glass) {
                    val before = selections
                    compose.onNodeWithContentDescription("关注").performClick()
                    compose.runOnIdle { assertEquals(before + 1, selections) }
                    compose.onNodeWithContentDescription("我的，未读 3 条").assertIsDisplayed()
                }
                (if (floating || glass) compose.onNodeWithContentDescription("我的", substring = glass)
                    else compose.onNodeWithText("我的")).performClick()
                compose.runOnIdle { assertEquals(2, selected.intValue) }
                compose.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
                compose.onNodeWithText("页面内容 1").assertIsDisplayed()
                compose.runOnIdle { assertEquals(1, bodyInstances) }
            }
        }
        compose.runOnIdle {
            preferences.value = preferences.value.copy(bottomBarStyle = BottomBarStyle.OS4)
            unread.intValue = 100
        }
        compose.onNodeWithText("99+", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("我的，未读 99+ 条").assertIsDisplayed()
        compose.runOnIdle { unread.intValue = 0 }
        compose.onNodeWithText("99+", useUnmergedTree = true).assertDoesNotExist()
    }
}
