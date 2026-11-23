package com.example.c001apk.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.util.*
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun ContentShareMenuItems(
    type: ShareType,
    id: String,
    title: String?,
    message: String? = null,
    dismiss: () -> Unit,
) {
    val context = LocalContext.current
    MiuixMenuItem(
        text = { Text(stringResource(R.string.action_copy)) },
        onClick = {
            dismiss()
            context.copyText(ShareContent.link(type, id))
        },
    )
    MiuixMenuItem(
        text = { Text(stringResource(R.string.action_share)) },
        onClick = {
            dismiss()
            context.shareText(ShareContent.text(type, id, title, message))
        },
    )
}
