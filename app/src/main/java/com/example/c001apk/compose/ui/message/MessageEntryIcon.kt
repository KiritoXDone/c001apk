package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.icons.FlutterProfileIcons
import com.example.c001apk.compose.ui.component.icons.NotificationIcon
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 根据图形实际边界统一视觉尺寸。
@Composable
internal fun MessageEntryIcon(entry: MessageEntry) {
    val vector = when (entry) {
        MessageEntry.NOTICES -> NotificationIcon
        MessageEntry.LIKES -> FlutterProfileIcons.ThumbUp
        MessageEntry.FOLLOWS -> FlutterProfileIcons.PersonAdd
        MessageEntry.MESSAGES -> FlutterProfileIcons.Mail
    }
    // 保留比例，图形最长边统一为 20dp。
    val extent = when (entry) {
        MessageEntry.NOTICES -> 19.5f
        MessageEntry.LIKES, MessageEntry.FOLLOWS -> 22.03124f
        MessageEntry.MESSAGES -> 19.96876f
    }
    val centerY = when (entry) {
        MessageEntry.NOTICES -> 12.25f
        MessageEntry.LIKES -> 10.99219f
        else -> 12f
    }
    val scale = 20f / extent
    val offset = with(LocalDensity.current) { ((12f - centerY) * scale).dp.toPx() }
    Icon(vector, null, Modifier.size(24.dp).graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = offset
    }, tint = MiuixTheme.colorScheme.onSurface)
}
