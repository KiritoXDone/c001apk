package com.example.c001apk.compose.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.ui.settings.DigitalUnionDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import top.yukonga.miuix.kmp.theme.MiuixTheme

class DigitalUnionDialogTest {
    private val compose = createComposeRule()
    @get:Rule val rule: RuleChain = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n ${instrumentation.targetContext.packageName}/androidx.activity.ComponentActivity",
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() } }
        }
    }).around(compose)

    @Test fun backgroundRefreshDoesNotOverwriteAnUnsavedManualDraft() {
        val id = mutableStateOf("old-synthetic-id")
        var saved = ""
        compose.setContent {
            MiuixTheme {
                DigitalUnionDialog(id.value, generating = false, error = null,
                    onGenerate = {}, onImport = {}, onDismiss = {}, onSave = { saved = it })
            }
        }
        compose.onNode(hasSetTextAction()).performTextReplacement("manual-synthetic-id")
        compose.runOnIdle { id.value = "new-background-id" }
        compose.onNodeWithText("保存").performClick()
        compose.runOnIdle { assertEquals("manual-synthetic-id", saved) }
    }
}
