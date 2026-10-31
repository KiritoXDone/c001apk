package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
        title = { Text("数字联盟 ID") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "用于连接酷安服务，可自动获取，也可粘贴已有 ID。",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                TextField(
                    value = value,
                    onValueChange = { value = it; edited = true },
                    modifier = Modifier.fillMaxWidth(),
                    label = "数字联盟 ID",
                    useLabelAsPlaceholder = true,
                )
                Button(
                    onClick = { edited = false; onGenerate() },
                    enabled = !generating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (generating) {
                        InfiniteProgressIndicator(Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (generating) "正在获取…" else if (id.isBlank()) "自动获取" else "重新获取")
                }
                Button(onClick = onImport, enabled = !generating) { Text("从官方酷安导入") }
                error?.let { Text(it, color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            }
        },
        dismissButton = { MiuixDialogButton(onClick = onDismiss) { Text("关闭") } },
        confirmButton = {
            MiuixDialogButton(
                onClick = {
                    onSave(value.trim())
                },
            ) {
                Text("保存")
            }
        },
    )
}
