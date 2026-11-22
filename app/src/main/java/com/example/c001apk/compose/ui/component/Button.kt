package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import com.example.c001apk.compose.util.composeClick
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftLine
import top.yukonga.miuix.kmp.basic.Icon

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun BackButton(
    onBackClick: () -> Unit,
) {
    MiuixToolbarIconButton(
        onClick =
            composeClick {
                onBackClick()
            },
    ) {
        Icon(
            imageVector = MingCuteArrowLeftLine,
            contentDescription = "返回",
        )
    }
}
