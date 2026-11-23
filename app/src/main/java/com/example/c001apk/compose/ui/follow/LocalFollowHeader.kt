package com.example.c001apk.compose.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixActionButton
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.glass.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun LocalFollowHeader(warning: String?, onManage: () -> Unit, onRetry: () -> Unit) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp),
            onClick = onManage,
            showIndication = true,
            insideMargin = PaddingValues(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(MiuixIcons.Hide, null, Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.follow_local_only), style = MiuixTheme.textStyles.headline2)
                    Text(stringResource(R.string.follow_no_sync), style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                Text(stringResource(R.string.action_manage), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
                Icon(MiuixIcons.Glass.ChevronForward, null, Modifier.size(14.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
            }
        }
        warning?.let {
            Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp)) {
                BasicComponent(title = it, endActions = {
                    MiuixActionButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                })
            }
        }
        SmallTitle(stringResource(R.string.follow_latest_posts), modifier = Modifier.padding(top = 8.dp))
    }
}
