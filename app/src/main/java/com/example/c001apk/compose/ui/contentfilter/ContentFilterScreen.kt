package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
    MiuixScaffold(topBar = { MiuixTopBar(title = stringResource(R.string.settings_content_filter), navigationIcon = { BackButton(onBack) }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp)) {
            item {
                PreferenceGroup(title = stringResource(R.string.filter_local)) {
                    SwitchListItem(prefs.enabled, MiuixIcons.Blocklist, stringResource(R.string.filter_enable), stringResource(R.string.filter_enable_summary), enabled = !model.saving) {
                        enabled -> model.update { it.copy(enabled = enabled) }
                    }
                    FilterScope.entries.forEach { scope ->
                        SwitchListItem(scope in prefs.scopes, MiuixIcons.Blocklist, stringResource(scope.titleRes), enabled = !model.saving) { enabled ->
                            model.update { it.copy(scopes = if (enabled) it.scopes + scope else it.scopes - scope) }
                        }
                    }
                }
            }
            twilightRulesPreferences(prefs, model.saving, model.importMessage,
                onDetails = onRemoteRules,
                onImport = model::importTwilight, onRemove = model::removeTwilight, onUpdate = model::update)
            item {
                PreferenceGroup(title = stringResource(R.string.filter_personal)) {
                    BasicListItem(headlineText = stringResource(R.string.filter_add), supportingText = stringResource(R.string.filter_add_summary), onClick = { onEdit(null) })
                }
                if (prefs.rules.isEmpty()) Text(stringResource(R.string.filter_empty), Modifier.padding(24.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            FilterKind.entries.forEach { kind ->
                val rules = prefs.rules.filter { it.kind == kind }
                if (rules.isNotEmpty()) item(key = kind.name) { top.yukonga.miuix.kmp.basic.SmallTitle(stringResource(kind.titleRes)) }
                items(rules, key = { it.id }) { rule ->
                    BasicListItem(
                        headlineText = rule.title,
                        supportingText = listOf(
                            stringResource(if (rule.enabled) R.string.filter_enabled else R.string.filter_disabled),
                            rule.scopes.map { stringResource(it.titleRes) }.joinToString(", "),
                            rule.terms.joinToString(", "),
                        ).joinToString(" · "),
                        onClick = { onEdit(rule.id) },
                    )
                }
            }
            item {
                PreferenceGroup(title = stringResource(R.string.filter_exceptions)) {
                    BasicListItem(headlineText = stringResource(R.string.filter_clear_exceptions), supportingText = stringResource(R.string.filter_exception_count, prefs.allowedContent.size), onClick = {
                        model.update { it.copy(allowedContent = emptySet()) }
                    }.takeIf { prefs.allowedContent.isNotEmpty() })
                }
            }
            item { model.error?.let { Text(it, Modifier.padding(24.dp), color = MiuixTheme.colorScheme.error) } }
        }
    }
}
