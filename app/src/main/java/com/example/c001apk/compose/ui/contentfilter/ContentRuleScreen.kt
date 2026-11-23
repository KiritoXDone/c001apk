package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
    MiuixScaffold(topBar = { MiuixTopBar(title = stringResource(if (id == null) R.string.filter_add else R.string.filter_edit), navigationIcon = { BackButton(onBack) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(value = title, onValueChange = { title = it.take(80) }, label = stringResource(R.string.filter_rule_name))
            PreferenceGroup(title = stringResource(R.string.filter_matching)) {
                SelectionListItem(leadingImageVector = MiuixIcons.Blocklist, title = stringResource(R.string.filter_rule_type), value = kind.ordinal,
                    selections = FilterKind.entries.map { SelectionItem(stringResource(it.titleRes), it.ordinal) },
                    onSelected = { value -> kind = FilterKind.entries[value] })
            }
            TextField(value = terms, onValueChange = { terms = it.take(6500) }, label = when (kind) {
                FilterKind.TEXT -> stringResource(R.string.filter_terms_hint)
                FilterKind.AUTHOR -> stringResource(R.string.filter_authors_hint)
                FilterKind.TOPIC -> stringResource(R.string.filter_topics_hint)
            })
            Text(stringResource(R.string.filter_matching_hint), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            if (kind == FilterKind.TEXT) {
                TextField(value = exceptions, onValueChange = { exceptions = it.take(6500) }, label = stringResource(R.string.filter_exception_hint))
                Text(stringResource(R.string.filter_exception_summary), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            PreferenceGroup(title = stringResource(R.string.filter_scopes)) {
                SwitchListItem(enabled, MiuixIcons.Blocklist, stringResource(R.string.filter_enable_rule)) { enabled = it }
                SwitchListItem(feed, MiuixIcons.Blocklist, stringResource(R.string.label_feed)) { feed = it }
                SwitchListItem(comment, MiuixIcons.Blocklist, stringResource(R.string.label_comment)) { comment = it }
                SwitchListItem(user, MiuixIcons.Blocklist, stringResource(R.string.label_user_card)) { user = it }
            }
            model.error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
            top.yukonga.miuix.kmp.basic.Button(enabled = !model.saving, onClick = {
                model.saveRule(ContentRule(ruleId, title.trim(), kind, lines(terms), if (kind == FilterKind.TEXT) lines(exceptions) else emptyList(),
                    buildSet { if (feed) add(FilterScope.FEED); if (comment) add(FilterScope.COMMENT); if (user) add(FilterScope.USER) }, enabled), onBack)
            }) { Text(stringResource(if (model.saving) R.string.status_saving else R.string.filter_save_rule)) }
            if (original != null) MiuixDialogButton(enabled = !model.saving, onClick = { deleting = true }) { Text(stringResource(R.string.filter_delete_rule)) }
        }
    }
    if (deleting) MiuixAlertDialog(onDismissRequest = { deleting = false }, title = { Text(stringResource(R.string.filter_delete_title)) },
        text = { Text(stringResource(R.string.filter_delete_summary)) },
        confirmButton = { MiuixDialogButton(onClick = { model.removeRule(ruleId, onBack) }, destructive = true) { Text(stringResource(R.string.menu_delete)) } },
        dismissButton = { MiuixDialogButton(onClick = { deleting = false }) { Text(stringResource(R.string.action_cancel)) } })
}
