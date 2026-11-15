package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 正文独立滚动，操作按钮保持可见。 */
@Composable
fun MiuixAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    GlassDialog(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        backdrop = null,
        stroke = null,
        containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 1f),
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            icon?.invoke()
            if (title != null) {
                MiuixTheme(
                    textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.title3),
                ) {
                    title()
                }
                Spacer(Modifier.height(16.dp))
            }
            if (text != null) {
                Box(
                    Modifier.weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    text()
                }
                Spacer(Modifier.height(20.dp))
            }
            CompositionLocalProvider(LocalDialogActions provides true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (dismissButton != null) Box(Modifier.weight(1f)) { dismissButton() }
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalDialogPrimaryAction provides true) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}
