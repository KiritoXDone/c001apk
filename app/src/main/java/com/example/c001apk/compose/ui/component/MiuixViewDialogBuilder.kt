package com.example.c001apk.compose.ui.component

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.theme.C001apkViewTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.squircle.squircleClip

// ComponentDialog 提供 View 生命周期，布局和转场交给原生 WindowDialog。
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

    fun setItems(values: Array<out CharSequence>, listener: DialogInterface.OnClickListener) = apply {
        items = values to listener
    }

    fun create(): ComponentDialog {
        val dialog = ComponentDialog(context)
        dialog.setCancelable(cancelable)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        dialog.setOnShowListener {
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
        fun cancel() {
            negative?.second?.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
            dialog.dismiss()
        }
        val action = positive?.takeIf { it.second != null }
        dialog.setContentView(
            ComposeView(context).apply {
                setContent {
                    C001apkViewTheme {
                        MiuixAlertDialog(
                            onDismissRequest = ::cancel,
                            dismissEnabled = cancelable,
                            title = title?.let { label -> { Text(label.toString()) } },
                            titleActions = neutral?.let { (label, listener) ->
                                {
                                    var expanded by remember { mutableStateOf(false) }
                                    var isMenuHeldDown by remember { mutableStateOf(false) }
                                    var selected by remember { mutableStateOf(false) }
                                    val close = checkNotNull(LocalDialogClose.current)
                                    Box {
                                        IconButton(
                                            onClick = {
                                                isMenuHeldDown = true
                                                expanded = true
                                            },
                                            holdDownState = isMenuHeldDown,
                                            modifier = Modifier.size(40.dp),
                                        ) {
                                            Icon(MiuixIcons.More, context.getString(R.string.update_more), Modifier.size(24.dp))
                                        }
                                        MiuixMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            alignment = PopupPositionProvider.Align.End,
                                            onDismissFinished = {
                                                isMenuHeldDown = false
                                                if (selected) close {
                                                    dialog.dismiss()
                                                    listener?.onClick(dialog, DialogInterface.BUTTON_NEUTRAL)
                                                }
                                            },
                                        ) {
                                            MiuixMenuItem(
                                                text = { Text(label.toString()) },
                                                onClick = { selected = true; expanded = false },
                                            )
                                        }
                                    }
                                }
                            },
                            text = {
                                val close = checkNotNull(LocalDialogClose.current)
                                Column(Modifier.fillMaxWidth()) {
                                    message?.let { Text(it.toString()) }
                                    view?.let { embedded -> AndroidView(factory = { embedded }) }
                                    items?.let { (labels, listener) ->
                                        Card {
                                            labels.forEachIndexed { index, label ->
                                                BasicComponent(
                                                    title = label.toString(),
                                                    modifier = Modifier.squircleClip(16.dp),
                                                    onClick = { close { dialog.dismiss(); listener.onClick(dialog, index) } },
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            dismissButton = action?.let {
                                { MiuixDialogButton(onClick = ::cancel) { Text(context.getString(R.string.action_cancel)) } }
                            },
                            confirmButton = {
                                if (action == null) {
                                    MiuixDialogButton(onClick = ::cancel, primary = false) { Text(context.getString(R.string.action_cancel)) }
                                } else {
                                    MiuixDialogButton(onClick = {
                                        dialog.dismiss()
                                        action.second?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                                    }) { Text(action.first.toString()) }
                                }
                            },
                        )
                    }
                }
            },
        )
        return dialog
    }

    fun show(): ComponentDialog = create().also { it.show() }
}
