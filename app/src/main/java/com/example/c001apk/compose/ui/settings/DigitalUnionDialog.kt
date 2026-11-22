package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R

@Composable
fun DigitalUnionDialog(
    id: String,
    generating: Boolean,
    error: String?,
    onGenerate: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(id) }
    var edited by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (!edited) value = id
    }
    LaunchedEffect(generating, error) {
        if (!edited && !generating && error == null) value = id
    }
    MiuixAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_szlm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.settings_szlm_desc),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                TextField(
                    value = value,
                    onValueChange = { value = it; edited = true },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.settings_szlm_title),
                    useLabelAsPlaceholder = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { edited = false; onGenerate() },
                        enabled = !generating,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    ) {
                        if (generating) {
                            InfiniteProgressIndicator(Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (generating) stringResource(R.string.settings_szlm_fetching)
                            else if (id.isBlank()) stringResource(R.string.settings_szlm_auto)
                            else stringResource(R.string.settings_szlm_refetch),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Button(
                        onClick = onImport,
                        enabled = !generating,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    ) { Text(stringResource(R.string.settings_szlm_import), textAlign = TextAlign.Center) }
                }
                error?.let { Text(it, color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            }
        },
        dismissButton = { MiuixDialogButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } },
        confirmButton = {
            MiuixDialogButton(
                onClick = {
                    onSave(value.trim())
                },
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
    )
}
