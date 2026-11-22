package com.example.c001apk.compose.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixActionButton
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteEyeOffLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteRightLine

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
                Icon(MingCuteEyeOffLine, null, Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("仅保存在本机", style = MiuixTheme.textStyles.headline2)
                    Text("不会同步账号关注", style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                Text("管理", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
                Icon(MingCuteRightLine, null, Modifier.size(14.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
            }
        }
        warning?.let {
            Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp)) {
                BasicComponent(title = it, endActions = {
                    MiuixActionButton(onClick = onRetry) { Text("重试") }
                })
            }
        }
        SmallTitle("最新动态", modifier = Modifier.padding(top = 8.dp))
    }
}
