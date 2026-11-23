package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

internal val LocalDialogClose = staticCompositionLocalOf<((() -> Unit) -> Unit)?> { null }

// 退出完成后执行关闭回调，保留条件组合中的原生退出动画。
@Composable
fun MiuixAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    titleActions: (@Composable () -> Unit)? = null,
    dismissEnabled: Boolean = true,
) {
    var visible by remember { mutableStateOf(true) }
    var afterDismiss by remember { mutableStateOf<(() -> Unit)?>(null) }
    val close: (() -> Unit) -> Unit = { action ->
        if (visible) {
            afterDismiss = action
            visible = false
        }
    }
    WindowDialog(
        show = visible,
        modifier = modifier,
        onDismissRequest = { if (dismissEnabled) close(onDismissRequest) },
        onDismissFinished = {
            val action = afterDismiss
            afterDismiss = null
            action?.invoke()
        },
    ) {
        CompositionLocalProvider(LocalDialogClose provides close) {
            Column(Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                icon?.invoke()
                if (title != null) {
                    MiuixDialogTitle(title, titleActions)
                    Spacer(Modifier.height(12.dp))
                }
                if (text != null) {
                    Box(
                        Modifier.weight(1f, fill = false).fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        text()
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (dismissButton != null) Box(Modifier.weight(1f)) { dismissButton() }
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalDialogPrimaryAction provides (dismissButton != null)) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MiuixDialogTitle(
    title: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null,
) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = if (actions != null) 40.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = if (actions != null) 48.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            MiuixTheme(textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.title3.copy(textAlign = TextAlign.Center))) {
                title()
            }
        }
        Box(Modifier.align(Alignment.CenterEnd)) { actions?.invoke() }
    }
}
