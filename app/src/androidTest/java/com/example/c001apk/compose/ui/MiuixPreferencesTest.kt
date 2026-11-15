package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.MiuixRootPopupHost
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Theme

class MiuixPreferencesTest {
    private val compose = createComposeRule()
    // HyperOS 可能拦截后台启动，先由 instrumentation shell 启动测试 Activity。
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

    @Test
    fun clickingSwitchRowSavesExactlyOnce() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                var enabled by remember { mutableStateOf(false) }
                var saves by remember { mutableIntStateOf(0) }
                Column {
                    SwitchListItem(enabled, MiuixIcons.Theme, "系统主题色") {
                        enabled = it
                        saves++
                    }
                    Text("$enabled / $saves")
                }
            }
        }
        compose.onNodeWithText("系统主题色").performClick()
        compose.onNodeWithText("true / 1").assertIsDisplayed()
        compose.onNodeWithText("系统主题色").performClick()
        compose.onNodeWithText("false / 2").assertIsDisplayed()
    }

    @Test
    fun selectingThemeAppliesWithoutConfirmButton() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                MiuixRootPopupHost {
                    var mode by remember { mutableStateOf("跟随系统") }
                    DropdownListItem(
                        value = mode,
                        headlineText = "主题模式",
                        selections = listOf("跟随系统", "浅色", "深色").map { SelectionItem(it, it) },
                        onValueChanged = { _, value -> mode = value },
                    )
                }
            }
        }
        compose.onNodeWithText("主题模式").performClick()
        compose.onNodeWithText("深色").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("深色").assertIsDisplayed()
        compose.onNodeWithText("取消").assertDoesNotExist()
    }

    @Test
    fun emptyAndDisabledDropdownsDoNotOpenAndSelectionWritesMappedValueOnce() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                MiuixRootPopupHost {
                    var value by remember { mutableIntStateOf(1) }
                    var saves by remember { mutableIntStateOf(0) }
                    Column {
                        DropdownListItem(
                            value = value,
                            headlineText = "可选项",
                            selections = listOf(SelectionItem("第一项", 1), SelectionItem("第二项", 7)),
                            onValueChanged = { _, selected -> value = selected; saves++ },
                        )
                        DropdownListItem<Int>(
                            value = null,
                            headlineText = "空选项",
                            selections = emptyList(),
                            onValueChanged = { _, _ -> saves++ },
                        )
                        DropdownListItem(
                            value = 1,
                            headlineText = "禁用项",
                            selections = listOf(SelectionItem("不可选", 1)),
                            enabled = false,
                            onValueChanged = { _, _ -> saves++ },
                        )
                        Text("$value / $saves")
                    }
                }
            }
        }
        compose.onNodeWithText("空选项").assertIsNotEnabled()
        compose.onNodeWithText("禁用项").assertIsNotEnabled()
        compose.onNodeWithText("可选项").performClick()
        compose.onNodeWithText("第二项").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("7 / 1").assertIsDisplayed()
        compose.onNodeWithText("第一项").assertDoesNotExist()
    }
}
