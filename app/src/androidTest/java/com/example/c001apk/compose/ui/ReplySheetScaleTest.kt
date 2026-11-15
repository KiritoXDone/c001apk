package com.example.c001apk.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.component.MiuixBottomSheet
import com.example.c001apk.compose.ui.component.MiuixRootPopupHost
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

class ReplySheetScaleTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)

    @Test fun overlayPreservesPageScaleAndTracksChanges() {
        val density = mutableStateOf(Density(2f, 0.85f))
        val showing = mutableStateOf(true)
        var observed: Density? = null
        compose.setContent {
            MiuixTheme {
                CompositionLocalProvider(LocalDensity provides density.value) {
                    MiuixRootPopupHost {
                        if (showing.value) MiuixBottomSheet(onDismissRequest = { showing.value = false }) {
                            val inside = LocalDensity.current
                            SideEffect { observed = inside }
                            Box(Modifier.size(200.dp)) {
                                TextButton(text = "关闭测试抽屉", onClick = { showing.value = false })
                            }
                        }
                    }
                }
            }
        }
        compose.runOnIdle {
            assertEquals(2f, observed!!.density, 0.001f)
            assertEquals(0.85f, observed!!.fontScale, 0.001f)
            density.value = Density(2.5f, 1.15f)
        }
        compose.runOnIdle {
            assertEquals(2.5f, observed!!.density, 0.001f)
            assertEquals(1.15f, observed!!.fontScale, 0.001f)
        }
        compose.onNodeWithText("关闭测试抽屉").performClick()
        compose.runOnIdle { assertFalse(showing.value) }
    }
}
