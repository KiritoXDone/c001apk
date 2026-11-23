package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonColors
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

val LocalDialogPrimaryAction = staticCompositionLocalOf { false }

@Composable
fun MiuixDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    primary: Boolean? = null,
    dismissOnClick: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val close = LocalDialogClose.current
    val colors = if (primary ?: (LocalDialogPrimaryAction.current || destructive)) {
        ButtonDefaults.textButtonColorsPrimary()
    } else {
        ButtonDefaults.textButtonColors()
    }
    // 保留内容槽，沿用原生 TextButton 的尺寸、颜色与交互。
    Button(
        onClick = { if (dismissOnClick && close != null) close(onClick) else onClick() },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonColors(colors.color, colors.disabledColor, colors.textColor, colors.disabledTextColor),
    ) {
        MiuixTheme(textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.button)) {
            content()
        }
    }
}
