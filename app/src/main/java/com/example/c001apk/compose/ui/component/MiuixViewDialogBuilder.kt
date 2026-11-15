package com.example.c001apk.compose.ui.component

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.c001apk.compose.ui.theme.C001apkViewTheme
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Native miuix dialog content for View-based media and rich-text editor entry points. */
class MiuixViewDialogBuilder(private val context: Context) {
    private var title: CharSequence? = null
    private var message: CharSequence? = null
    private var view: View? = null
    private var cancelable = true
    private var positive: Pair<CharSequence, DialogInterface.OnClickListener?>? = null
    private var negative: Pair<CharSequence, DialogInterface.OnClickListener?>? = null
    private var neutral: Pair<CharSequence, DialogInterface.OnClickListener?>? = null
    private var items: Pair<Array<out CharSequence>, DialogInterface.OnClickListener>? = null

    fun setTitle(value: CharSequence?) = apply { title = value }

    fun setMessage(value: CharSequence?) = apply { message = value }

    fun setView(value: View) = apply { view = value }

    fun setCancelable(value: Boolean) = apply { cancelable = value }

    fun setPositiveButton(text: CharSequence, listener: DialogInterface.OnClickListener?) = apply {
        positive = text to listener
    }

    fun setPositiveButton(text: Int, listener: DialogInterface.OnClickListener?) =
        setPositiveButton(context.getText(text), listener)

    fun setNegativeButton(text: CharSequence, listener: DialogInterface.OnClickListener?) = apply {
        negative = text to listener
    }

    fun setNegativeButton(text: Int, listener: DialogInterface.OnClickListener?) =
        setNegativeButton(context.getText(text), listener)

    fun setNeutralButton(text: CharSequence, listener: DialogInterface.OnClickListener?) = apply {
        neutral = text to listener
    }

    fun setItems(values: Array<out CharSequence>, listener: DialogInterface.OnClickListener) =
        apply {
            items = values to listener
        }

    fun create(): ComponentDialog {
        val dialog = ComponentDialog(context)
        dialog.setCancelable(cancelable)
        dialog.window?.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        dialog.setContentView(
            ComposeView(context).apply {
                setContent {
                    C001apkViewTheme {
                        Card(cornerRadius = 28.dp, insideMargin = PaddingValues(24.dp)) {
                            Column(
                                Modifier.heightIn(max = 500.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                title?.let {
                                    Text(it.toString(), style = MiuixTheme.textStyles.title3)
                                }
                                Column(
                                    Modifier.weight(1f, fill = false)
                                        .verticalScroll(rememberScrollState()),
                                ) {
                                    message?.let { Text(it.toString()) }
                                    view?.let { embedded -> AndroidView(factory = { embedded }) }
                                    items?.let { (labels, listener) ->
                                        labels.forEachIndexed { index, label ->
                                            BasicComponent(
                                                title = label.toString(),
                                                onClick = {
                                                    dialog.dismiss()
                                                    listener.onClick(dialog, index)
                                                },
                                            )
                                        }
                                    }
                                }
                                if (neutral != null || negative != null || positive != null) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        neutral?.let { (label, listener) ->
                                            GlassDialogButton(
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    dialog.dismiss()
                                                    listener?.onClick(
                                                        dialog,
                                                        DialogInterface.BUTTON_NEUTRAL,
                                                    )
                                                },
                                            ) {
                                                Text(label.toString(), style = MiuixTheme.textStyles.button)
                                            }
                                        }
                                        negative?.let { (label, listener) ->
                                            GlassDialogButton(
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    dialog.dismiss()
                                                    listener?.onClick(
                                                        dialog,
                                                        DialogInterface.BUTTON_NEGATIVE,
                                                    )
                                                },
                                            ) {
                                                Text(label.toString(), style = MiuixTheme.textStyles.button)
                                            }
                                        }
                                        positive?.let { (label, listener) ->
                                            GlassDialogButton(
                                                modifier = Modifier.weight(1f),
                                                role = GlassDialogButtonRole.Primary,
                                                onClick = {
                                                    dialog.dismiss()
                                                    listener?.onClick(
                                                        dialog,
                                                        DialogInterface.BUTTON_POSITIVE,
                                                    )
                                                },
                                            ) {
                                                Text(label.toString(), style = MiuixTheme.textStyles.button)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
        return dialog
    }

    fun show(): ComponentDialog = create().also { it.show() }
}
