package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
internal fun ReadingSidebarButton(hidden: Boolean, onToggle: (() -> Unit)?) {
    if (onToggle == null) return
    MiuixToolbarIconButton(onClick = onToggle) {
        Icon(
            imageVector = MiuixIcons.Back,
            contentDescription = stringResource(if (hidden) R.string.sidebar_show else R.string.sidebar_hide),
            modifier = Modifier.rotate(if (hidden) 0f else 180f),
        )
    }
}
