package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.composeClick
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.glass.ChevronBackward

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
            imageVector = if (LocalUserPreferences.current.topBarStyle == TopBarStyle.OS4)
                MiuixIcons.Glass.ChevronBackward else MiuixIcons.Back,
            contentDescription = stringResource(R.string.action_back),
        )
    }
}
