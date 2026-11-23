package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
    MiuixScaffold(topBar = { MiuixTopBar(title = stringResource(R.string.twilight_rules_title), navigationIcon = { BackButton(onBack) }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp)) {
            item {
                Text(if (snapshot == null) stringResource(R.string.twilight_empty) else stringResource(R.string.twilight_version, snapshot.revision, snapshot.rules.size),
                    Modifier.padding(24.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            items(snapshot?.rules.orEmpty(), key = { it.key }) { rule ->
                val scopes = rule.scopes.map {
                    stringResource(when (it) {
                        "feed" -> R.string.label_feed
                        "comment" -> R.string.label_comment
                        else -> R.string.label_user_card
                    })
                }.joinToString(", ")
                val matching = if (rule.kind == "text") rule.groups.joinToString(stringResource(R.string.filter_and)) { it.joinToString(" / ") }
                    else rule.values.joinToString(", ")
                val level = stringResource(if (rule.level == "strict") R.string.filter_strict else R.string.filter_standard)
                val distance = if (rule.kind == "text") stringResource(R.string.filter_distance, rule.maxDistance) else null
                val exceptions = if (rule.exceptions.isNotEmpty()) stringResource(R.string.filter_exception_terms, rule.exceptions.joinToString(", ")) else null
                val summary = listOfNotNull("$level · $scopes", matching, distance, exceptions).joinToString("\n")
                SwitchListItem(rule.key !in prefs.disabledTwilightRules, MiuixIcons.Blocklist, rule.reason, summary, enabled = !model.saving) { enabled ->
                    model.update { it.copy(disabledTwilightRules = if (enabled) it.disabledTwilightRules - rule.key else it.disabledTwilightRules + rule.key) }
                }
            }
            item { model.error?.let { Text(it, Modifier.padding(24.dp), color = MiuixTheme.colorScheme.error) } }
        }
    }
}
