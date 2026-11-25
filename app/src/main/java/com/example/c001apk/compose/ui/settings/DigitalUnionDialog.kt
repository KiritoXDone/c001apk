package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.LocalDialogClose
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import com.example.c001apk.compose.ui.component.MiuixMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.PopupPositionProvider

@Composable
fun DigitalUnionDialog(
    id: String,
    generating: Boolean,
    error: String?,
    onGenerate: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, (Boolean) -> Unit) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(id) }
    var edited by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var isMenuHeldDown by remember { mutableStateOf(false) }
    var menuAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var saving by remember { mutableStateOf(false) }
    val canSave = value.isNotBlank() && !generating && !saving
    fun submit(close: (() -> Unit) -> Unit) {
        if (!canSave) return
        saving = true
        onSave(value.trim()) { success ->
            saving = false
            if (success) close(onDismiss)
        }
    }
    LaunchedEffect(id) { if (!edited) value = id }
    LaunchedEffect(generating, error) {
        if (!edited && !generating && error == null) value = id
    }
    MiuixAlertDialog(
        onDismissRequest = onDismiss,
        dismissEnabled = !saving,
        title = { Text(stringResource(R.string.settings_szlm_title)) },
        titleActions = {
            val close = checkNotNull(LocalDialogClose.current)
            Box {
                IconButton(
                    onClick = {
                        isMenuHeldDown = true
                        menu = true
                    },
                    holdDownState = isMenuHeldDown,
                    enabled = !generating && !saving,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(MiuixIcons.More, stringResource(R.string.update_more), Modifier.size(24.dp))
                }
                MiuixMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false },
                    alignment = PopupPositionProvider.Align.End,
                    onDismissFinished = {
                        isMenuHeldDown = false
                        val action = menuAction
                        menuAction = null
                        action?.invoke()
                    },
                ) {
                    MiuixMenuItem(
                        text = { Text(stringResource(if (id.isBlank()) R.string.settings_szlm_auto else R.string.settings_szlm_refetch)) },
                        onClick = { menuAction = { edited = false; onGenerate() }; menu = false },
                    )
                    MiuixMenuItem(
                        text = { Text(stringResource(R.string.settings_szlm_import)) },
                        onClick = { menuAction = { close(onImport) }; menu = false },
                    )
                }
            }
        },
        text = {
            val close = checkNotNull(LocalDialogClose.current)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_szlm_desc), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                TextField(
                    value = value,
                    onValueChange = { value = it; edited = true },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.settings_szlm_title),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit(close) }),
                )
                if (generating) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfiniteProgressIndicator(Modifier.size(20.dp))
                    Text(stringResource(R.string.settings_szlm_fetching))
                }
                error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
            }
        },
        dismissButton = {
            MiuixDialogButton(onClick = onDismiss, enabled = !saving) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            val close = checkNotNull(LocalDialogClose.current)
            MiuixDialogButton(onClick = { submit(close) }, enabled = canSave, dismissOnClick = false) {
                Text(stringResource(if (saving) R.string.status_saving else R.string.action_save))
            }
        },
    )
}
