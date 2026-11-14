package com.example.c001apk.compose.ui.component

import android.widget.TextView
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

class LinkTextThemeTest {
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
    fun richTextUpdatesItsColorWhenThemeChanges() {
        var dark by mutableStateOf(false)
        var expectedColor = 0
        compose.setContent {
            C001apkComposeTheme(darkTheme = dark, materialYou = false) {
                val color = MiuixTheme.colorScheme.onSurface
                SideEffect { expectedColor = color.toArgb() }
                CompositionLocalProvider(LocalContentColor provides color) {
                    LinkText(text = "测试消息")
                }
            }
        }
        fun assertColor() {
            onView(withText("测试消息")).check { view, _ ->
                assertEquals(expectedColor, (view as TextView).currentTextColor)
            }
        }
        assertColor()
        compose.runOnIdle { dark = true }
        compose.waitForIdle()
        assertColor()
    }
}
