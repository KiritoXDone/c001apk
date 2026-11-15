package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import com.example.c001apk.compose.logic.filter.ContentFilterPreferences
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import com.example.c001apk.compose.ui.component.settings.*
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist

internal fun LazyListScope.twilightRulesPreferences(
    preferences: ContentFilterPreferences,
    busy: Boolean,
    message: String?,
    onDetails: () -> Unit,
    onImport: () -> Unit,
    onRemove: () -> Unit,
    onUpdate: ((ContentFilterPreferences) -> ContentFilterPreferences) -> Unit,
) {
    val rules = preferences.twilight
    item(key = "twilight-controls") {
        var removing by remember { mutableStateOf(false) }
        PreferenceGroup(title = "秋风远程规则") {
            BasicListItem(
                headlineText = if (busy) "正在处理…" else if (rules == null) "导入秋风规则" else "更新秋风规则",
                supportingText = message ?: "来源：TG-Twilight/c001apk · filters\n手动下载到本机，不会自动开启内容过滤",
                enabled = !busy, onClick = onImport,
            )
            if (rules != null) {
                BasicListItem(headlineText = "规则明细",
                    supportingText = "版本 ${rules.revision} · ${rules.rules.size} 条 · 可逐条停用", onClick = onDetails)
                SwitchListItem(preferences.twilightStrict, MiuixIcons.Blocklist, "启用严格规则",
                    "仅影响秋风的严格级别关键词规则", enabled = !busy) { strict ->
                    onUpdate { it.copy(twilightStrict = strict) }
                }
                BasicListItem(headlineText = "移除秋风规则", supportingText = "保留个人规则和内容例外",
                    enabled = !busy, onClick = { removing = true })
            }
        }
        if (removing) MiuixAlertDialog(onDismissRequest = { removing = false }, title = { Text("移除秋风规则？") },
            text = { Text("将删除已导入的远程规则和相关停用选择，可随时重新导入。") },
            confirmButton = { MiuixDialogButton(enabled = !busy, destructive = true, onClick = { removing = false; onRemove() }) { Text("移除") } },
            dismissButton = { MiuixDialogButton(onClick = { removing = false }) { Text("取消") } })
    }
}
