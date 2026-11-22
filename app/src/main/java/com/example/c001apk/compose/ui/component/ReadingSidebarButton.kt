package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import top.yukonga.miuix.kmp.basic.Icon
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftLine

@Composable
internal fun ReadingSidebarButton(hidden: Boolean, onToggle: (() -> Unit)?) {
    if (onToggle == null) return
    MiuixToolbarIconButton(onClick = onToggle) {
        Icon(
            imageVector = MingCuteArrowLeftLine,
            contentDescription = if (hidden) "显示信息流" else "收起信息流",
            modifier = Modifier.rotate(if (hidden) 0f else 180f),
        )
    }
}
