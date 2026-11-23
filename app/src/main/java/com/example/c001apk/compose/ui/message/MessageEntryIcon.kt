package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Contacts
import top.yukonga.miuix.kmp.icon.glass.Email
import top.yukonga.miuix.kmp.icon.glass.FavoriteOutline
import top.yukonga.miuix.kmp.icon.glass.NotificationOutline
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MessageEntryIcon(entry: MessageEntry) {
    val vector = when (entry) {
        MessageEntry.NOTICES -> MiuixIcons.Glass.NotificationOutline
        MessageEntry.LIKES -> MiuixIcons.Glass.FavoriteOutline
        MessageEntry.FOLLOWS -> MiuixIcons.Glass.Contacts
        MessageEntry.MESSAGES -> MiuixIcons.Glass.Email
    }
    Icon(vector, null, Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurface)
}
