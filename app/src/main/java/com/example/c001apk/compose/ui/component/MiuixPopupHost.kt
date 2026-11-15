package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Scaffold

@Composable
fun MiuixRootPopupHost(content: @Composable () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
    ) {
        content()
    }
}
