package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.filter.*
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.*
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ContentFilterScreen(onBack: () -> Unit, onEdit: (String?) -> Unit, onRemoteRules: () -> Unit) {
    val model: ContentFilterViewModel = hiltViewModel()
    val json = LocalUserPreferences.current.contentFilterJson
    val prefs = remember(json) { ContentFilterPreferences.decode(json) }
    MiuixScaffold(topBar = { MiuixTopBar(title = "内容过滤", navigationIcon = { BackButton(onBack) }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp)) {
            item {
                PreferenceGroup(title = "本机过滤") {
                    SwitchListItem(prefs.enabled, MiuixIcons.Blocklist, "启用内容过滤", "只影响本机显示，可随时查看被过滤内容", enabled = !model.saving) {
                        enabled -> model.update { it.copy(enabled = enabled) }
                    }
                    FilterScope.entries.forEach { scope ->
                        SwitchListItem(scope in prefs.scopes, MiuixIcons.Blocklist, scope.title, enabled = !model.saving) { enabled ->
                            model.update { it.copy(scopes = if (enabled) it.scopes + scope else it.scopes - scope) }
                        }
                    }
                }
            }
            twilightRulesPreferences(prefs, model.saving, model.importMessage,
                onDetails = onRemoteRules,
                onImport = model::importTwilight, onRemove = model::removeTwilight, onUpdate = model::update)
            item {
                PreferenceGroup(title = "个人规则") {
                    BasicListItem(headlineText = "添加规则", supportingText = "关键词、用户 UID 或话题", onClick = { onEdit(null) })
                }
                if (prefs.rules.isEmpty()) Text("还没有个人规则，可添加或导入秋风规则后启用过滤。", Modifier.padding(24.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            FilterKind.entries.forEach { kind ->
                val rules = prefs.rules.filter { it.kind == kind }
                if (rules.isNotEmpty()) item(key = kind.name) { top.yukonga.miuix.kmp.basic.SmallTitle(kind.title) }
                items(rules, key = { it.id }) { rule ->
                    BasicListItem(
                        headlineText = rule.title,
                        supportingText = "${if (rule.enabled) "已启用" else "已停用"} · ${rule.scopes.joinToString { it.title }} · ${rule.terms.joinToString("、")}",
                        onClick = { onEdit(rule.id) },
                    )
                }
            }
            item {
                PreferenceGroup(title = "例外") {
                    BasicListItem(headlineText = "清空内容例外", supportingText = "${prefs.allowedContent.size} 条始终显示的内容，清空后重新按规则过滤", onClick = {
                        model.update { it.copy(allowedContent = emptySet()) }
                    }.takeIf { prefs.allowedContent.isNotEmpty() })
                }
            }
            item { model.error?.let { Text(it, Modifier.padding(24.dp), color = MiuixTheme.colorScheme.error) } }
        }
    }
}
