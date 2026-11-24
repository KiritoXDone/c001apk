package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.squircle.squircleClip
import com.example.c001apk.compose.R
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.preference.RadioButtonLocation
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

data class SelectionItem<T>(val label: String, val value: T)

/** 单选入口：描述行显示当前值，对话框点选后立即保存并关闭。 */
@Composable
fun <T> SelectionListItem(
    title: String,
    value: T?,
    selections: List<SelectionItem<T>>,
    onSelected: (T) -> Unit,
    leadingImageVector: ImageVector? = null,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }
    var pendingSelection by remember { mutableStateOf<SelectionItem<T>?>(null) }
    val canSelect = enabled && selections.isNotEmpty()
    val label = selections.firstOrNull { it.value == value }?.label
    PreferenceCard {
        BasicComponent(
            modifier = if (canSelect) Modifier else Modifier.semantics(mergeDescendants = true) { disabled() },
            title = title,
            summary = label,
            enabled = canSelect,
            startAction =
                leadingImageVector?.let { icon ->
                    { PreferenceLeadingIcon(icon) }
                },
            endActions = {
                Icon(
                    MiuixIcons.Basic.ArrowUpDown,
                    null,
                    Modifier.size(10.dp, 16.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                )
            },
            onClick = { showDialog = true },
        )
    }
    WindowDialog(
        show = showDialog,
        title = title,
        onDismissRequest = { showDialog = false },
        onDismissFinished = {
            val selection = pendingSelection
            pendingSelection = null
            selection?.let { onSelected(it.value) }
        },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            Card(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                selections.forEach { item ->
                    RadioButtonPreference(
                        title = item.label,
                        selected = item.value == value,
                        onClick = {
                            pendingSelection = item
                            showDialog = false
                        },
                        modifier = Modifier.squircleClip(16.dp),
                        radioButtonLocation = RadioButtonLocation.End,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                text = stringResource(R.string.action_cancel),
                onClick = { showDialog = false },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(),
            )
        }
    }
}
