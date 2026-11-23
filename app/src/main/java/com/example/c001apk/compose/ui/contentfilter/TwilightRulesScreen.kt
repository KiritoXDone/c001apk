package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.filter.ContentFilterPreferences
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixScaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TwilightRulesScreen(onBack: () -> Unit) {
    val model: ContentFilterViewModel = hiltViewModel()
    val json = LocalUserPreferences.current.contentFilterJson
    val prefs = remember(json) { ContentFilterPreferences.decode(json) }
    val snapshot = prefs.twilight
    MiuixScaffold(topBar = { MiuixTopBar(title = "秋风规则明细", navigationIcon = { BackButton(onBack) }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp)) {
            item {
                Text(if (snapshot == null) "尚未导入秋风规则" else "版本 ${snapshot.revision} · ${snapshot.rules.size} 条",
                    Modifier.padding(24.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            items(snapshot?.rules.orEmpty(), key = { it.key }) { rule ->
                val scopes = rule.scopes.joinToString("、") { when (it) { "feed" -> "动态"; "comment" -> "评论"; else -> "用户卡片" } }
                val matching = if (rule.kind == "text") rule.groups.joinToString(" 且 ") { it.joinToString(" / ") }
                    else rule.values.joinToString("、")
                val summary = buildString {
                    append(if (rule.level == "strict") "严格" else "标准")
                    append(" · $scopes\n$matching")
                    if (rule.kind == "text") append("\n同句内间距不超过 ${rule.maxDistance} 字符")
                    if (rule.exceptions.isNotEmpty()) append("\n例外：${rule.exceptions.joinToString("、")}")
                }
                SwitchListItem(rule.key !in prefs.disabledTwilightRules, MiuixIcons.Blocklist, rule.reason, summary, enabled = !model.saving) { enabled ->
                    model.update { it.copy(disabledTwilightRules = if (enabled) it.disabledTwilightRules - rule.key else it.disabledTwilightRules + rule.key) }
                }
            }
            item { model.error?.let { Text(it, Modifier.padding(24.dp), color = MiuixTheme.colorScheme.error) } }
        }
    }
}
