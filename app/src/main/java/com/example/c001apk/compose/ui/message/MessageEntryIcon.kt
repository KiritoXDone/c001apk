package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteHeartLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteMailLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteNotificationLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUser3Line
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MessageEntryIcon(entry: MessageEntry) {
    val vector = when (entry) {
        MessageEntry.NOTICES -> MingCuteNotificationLine
        MessageEntry.LIKES -> MingCuteHeartLine
        MessageEntry.FOLLOWS -> MingCuteUser3Line
        MessageEntry.MESSAGES -> MingCuteMailLine
    }
    Icon(vector, null, Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurface)
}
