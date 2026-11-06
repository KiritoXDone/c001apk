package com.example.c001apk.compose.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.createMiuixLoadingDialog
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.view.MiuixCheckboxView
import com.example.c001apk.compose.view.MiuixInputLayout
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MiuixEditorTest {
    @Test
    fun sendingDialogUsesContentSizeWithoutClipping() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(instrumentation.targetContext, ComponentActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ActivityScenario.launch<ComponentActivity>(intent).use { scenario ->
            lateinit var dialog: ComponentDialog
            scenario.onActivity { activity ->
                dialog = createMiuixLoadingDialog(activity)
                dialog.show()
            }
            try {
                instrumentation.waitForIdleSync()
                scenario.onActivity { activity ->
                    val content = dialog.findViewById<View>(android.R.id.content)!!
                    val density = activity.resources.displayMetrics.density * CookieUtil.contentScale
                    assertTrue(content.width >= 164 * density - 2)
                    assertTrue(content.height >= 60 * density - 2)
                    assertTrue(content.width > content.height * 2)
                    assertEquals(0.16f, dialog.window!!.attributes.dimAmount, 0.001f)
                }
                val screenshot = instrumentation.uiAutomation.takeScreenshot()
                val file = File(instrumentation.targetContext.getExternalFilesDir(null), "reply-loading.png")
                file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
                screenshot.recycle()
            } finally {
                scenario.onActivity { dialog.dismiss() }
            }
        }
    }

    @Test
    fun bothEditorLayoutsPreserveEditableTextWithoutMaterialTheme() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (orientation in
                listOf(Configuration.ORIENTATION_PORTRAIT, Configuration.ORIENTATION_LANDSCAPE)) {
                val base = instrumentation.targetContext
                val configuration =
                    Configuration(base.resources.configuration).apply {
                        this.orientation = orientation
                    }
                val context =
                    ContextThemeWrapper(
                        base.createConfigurationContext(configuration),
                        R.style.Theme_C001apkCompose,
                    )
                val root =
                    LayoutInflater.from(context).inflate(R.layout.activity_reply, null, false)
                val editor = root.findViewById<EditText>(R.id.editText)
                editor.setText("miuix 草稿")
                editor.setSelection(editor.length())
                editor.editableText.append(" #话题#")
                assertEquals("miuix 草稿 #话题#", editor.text.toString())
                assertTrue(root.findViewById<View>(R.id.textInputLayout) is MiuixInputLayout)
                assertTrue(root.findViewById<View>(R.id.checkBox) is MiuixCheckboxView)
            }
        }
    }
}
