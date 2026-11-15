package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

val LocalDialogPrimaryAction = staticCompositionLocalOf { false }
internal val LocalDialogActions = staticCompositionLocalOf { false }

@Composable
fun MiuixDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    primary: Boolean? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (LocalDialogActions.current) {
        val role = when {
            destructive -> GlassDialogButtonRole.Destructive
            primary ?: LocalDialogPrimaryAction.current -> GlassDialogButtonRole.Primary
            else -> GlassDialogButtonRole.Normal
        }
        GlassDialogButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            role = role,
        ) {
            MiuixTheme(textStyles = MiuixTheme.textStyles.copy(main = MiuixTheme.textStyles.button)) {
                content()
            }
        }
        return
    }
    val colors =
        if (LocalDialogPrimaryAction.current) ButtonDefaults.buttonColorsPrimary()
        else ButtonDefaults.buttonColors()
    Button(onClick, modifier.fillMaxWidth(), enabled = enabled, colors = colors) {
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) { content() }
    }
}
