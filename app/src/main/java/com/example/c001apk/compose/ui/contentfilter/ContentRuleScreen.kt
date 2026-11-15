package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.filter.*
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.*
import java.util.UUID
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ContentRuleScreen(id: String?, onBack: () -> Unit) {
    val model: ContentFilterViewModel = hiltViewModel()
    val prefs = ContentFilterPreferences.decode(LocalUserPreferences.current.contentFilterJson)
    val original = remember(id) { prefs.rules.find { it.id == id } }
    val ruleId = rememberSaveable { id ?: UUID.randomUUID().toString() }
    var title by rememberSaveable { mutableStateOf(original?.title.orEmpty()) }
    var terms by rememberSaveable { mutableStateOf(original?.terms?.joinToString("\n").orEmpty()) }
    var exceptions by rememberSaveable { mutableStateOf(original?.exceptions?.joinToString("\n").orEmpty()) }
    var kind by rememberSaveable { mutableStateOf(original?.kind ?: FilterKind.TEXT) }
    var enabled by rememberSaveable { mutableStateOf(original?.enabled ?: true) }
    var feed by rememberSaveable { mutableStateOf(original?.scopes?.contains(FilterScope.FEED) ?: true) }
    var comment by rememberSaveable { mutableStateOf(original?.scopes?.contains(FilterScope.COMMENT) ?: true) }
    var user by rememberSaveable { mutableStateOf(original?.scopes?.contains(FilterScope.USER) ?: true) }
    var deleting by remember { mutableStateOf(false) }
    fun lines(value: String) = value.lineSequence().map(String::trim).filter(String::isNotBlank).distinct().toList()
    MiuixScaffold(topBar = { MiuixTopBar(title = if (id == null) "添加规则" else "编辑规则", navigationIcon = { BackButton(onBack) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(value = title, onValueChange = { title = it.take(80) }, label = "规则名称")
            PreferenceGroup(title = "匹配方式") {
                DropdownListItem(leadingImageVector = MiuixIcons.Blocklist, headlineText = "规则类型", value = kind.ordinal,
                    selections = FilterKind.entries.map { SelectionItem(it.title, it.ordinal) },
                    onValueChanged = { index, _ -> kind = FilterKind.entries[index] })
            }
            TextField(value = terms, onValueChange = { terms = it.take(6500) }, label = when (kind) {
                FilterKind.TEXT -> "关键词，每行一项"
                FilterKind.AUTHOR -> "数字 UID，每行一个"
                FilterKind.TOPIC -> "完整话题名称，每行一个"
            })
            Text("命中任意一项即过滤；关键词不区分大小写和全角半角。", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            if (kind == FilterKind.TEXT) {
                TextField(value = exceptions, onValueChange = { exceptions = it.take(6500) }, label = "例外词，每行一项（可选）")
                Text("同一句包含例外词时放行，不影响其他句子的匹配。", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            PreferenceGroup(title = "生效范围") {
                SwitchListItem(enabled, MiuixIcons.Blocklist, "启用此规则") { enabled = it }
                SwitchListItem(feed, MiuixIcons.Blocklist, "动态") { feed = it }
                SwitchListItem(comment, MiuixIcons.Blocklist, "评论") { comment = it }
                SwitchListItem(user, MiuixIcons.Blocklist, "用户卡片") { user = it }
            }
            model.error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
            top.yukonga.miuix.kmp.basic.Button(enabled = !model.saving, onClick = {
                model.saveRule(ContentRule(ruleId, title.trim(), kind, lines(terms), if (kind == FilterKind.TEXT) lines(exceptions) else emptyList(),
                    buildSet { if (feed) add(FilterScope.FEED); if (comment) add(FilterScope.COMMENT); if (user) add(FilterScope.USER) }, enabled), onBack)
            }) { Text(if (model.saving) "正在保存" else "保存规则") }
            if (original != null) MiuixDialogButton(enabled = !model.saving, onClick = { deleting = true }) { Text("删除规则") }
        }
    }
    if (deleting) MiuixAlertDialog(onDismissRequest = { deleting = false }, title = { Text("删除规则？") },
        text = { Text("删除后，此规则过滤的内容将恢复显示。") },
        confirmButton = { MiuixDialogButton(onClick = { model.removeRule(ruleId, onBack) }, destructive = true) { Text("删除") } },
        dismissButton = { MiuixDialogButton(onClick = { deleting = false }) { Text("取消") } })
}
