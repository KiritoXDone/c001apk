package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.click
import org.junit.Assert.assertEquals
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Rule
import org.junit.Test
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Search

class MiuixMenuTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)


    @Test
    fun overflowWithSubmenuArrowsOpensAndSelectsBothEntries() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                var expanded by remember { mutableStateOf(false) }
                var selected by remember { mutableStateOf("") }
                Column {
                    Box {
                        MoreMenuButton { expanded = true }
                        MiuixMenu(expanded, onDismissRequest = { expanded = false }) {
                            listOf("类型", "排序").forEach { label ->
                                MiuixMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selected = label
                                        expanded = false
                                    },
                                    trailingIcon = { Icon(MiuixIcons.Forward, null) },
                                )
                            }
                        }
                    }
                    Text("已选择：$selected")
                }
            }
        }
        listOf("类型", "排序").forEach { label ->
            compose.onNodeWithContentDescription("更多操作").performClick()
            compose.onNodeWithText(label).assertIsDisplayed().performClick()
            compose.onNodeWithText("已选择：$label").assertIsDisplayed()
            compose.waitForIdle()
        }
    }

    @Test
    fun iconsSupportLargeTextAndPreserveLongClickAndDisabledState() {
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale = 2f),
                ) {
                    var action by remember { mutableStateOf("") }
                    Column(Modifier.width(240.dp)) {
                        ListPopupColumn {
                            MiuixMenuItem(
                                text = { Text("带图标的长菜单选项") },
                                onClick = { action = "点击" },
                                onLongClick = { action = "长按" },
                                leadingIcon = { Icon(MiuixIcons.Search, null) },
                                trailingIcon = { Icon(MiuixIcons.Forward, null) },
                            )
                            MiuixMenuItem(
                                text = { Text("禁用选项") },
                                onClick = { action = "错误" },
                                enabled = false,
                                trailingIcon = { Icon(MiuixIcons.Forward, null) },
                            )
                        }
                        Text("操作：$action")
                    }
                }
            }
        }
        compose.onNodeWithText("带图标的长菜单选项").assertIsDisplayed().performTouchInput { longClick() }
        compose.onNodeWithText("操作：长按").assertIsDisplayed()
        compose.onNodeWithText("禁用选项").assertIsNotEnabled()
        compose.onNodeWithText("带图标的长菜单选项").performClick()
        compose.onNodeWithText("操作：点击").assertIsDisplayed()
    }

    @Test
    fun popupShrinksByTenPercentWithoutShrinkingItsAnchorOrBreakingTouch() {
        val scale = mutableStateOf(1f)
        var clicks = 0
        compose.setContent {
            C001apkComposeTheme(materialYou = false) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    MoreMenuButton { expanded = true }
                    MiuixMenu(expanded, onDismissRequest = { expanded = false }, contentScale = scale.value) {
                        MiuixMenuItem(text = { Text("复制") },
                            modifier = Modifier.testTag("scaled-item"),
                            onClick = { clicks++; expanded = false })
                        MiuixMenuItem(text = { Text("其他操作") }, onClick = {})
                    }
                }
            }
        }
        val anchor = compose.onNodeWithContentDescription("更多操作").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithContentDescription("更多操作").performClick()
        compose.waitForIdle()
        val before = compose.onNodeWithTag("scaled-item").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag("scaled-item").performTouchInput { click(center) }
        compose.runOnIdle { scale.value = 0.9f }
        compose.onNodeWithContentDescription("更多操作").performClick()
        compose.waitForIdle()
        val after = compose.onNodeWithTag("scaled-item").fetchSemanticsNode().boundsInRoot
        assertEquals(before.width * 0.9f, after.width, 2f)
        assertEquals(before.height * 0.9f, after.height, 2f)
        compose.onNodeWithTag("scaled-item").performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(2, clicks) }
        assertEquals(anchor.width, compose.onNodeWithContentDescription("更多操作").fetchSemanticsNode().boundsInRoot.width, 0f)
    }
}
