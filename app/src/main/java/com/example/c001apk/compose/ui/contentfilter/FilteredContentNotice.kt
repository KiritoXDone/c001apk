package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.filter.*
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixActionButton as Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun rememberContentFilter(): ContentFilterEngine {
    val json = LocalUserPreferences.current.contentFilterJson
    return remember(json) { ContentFilterEngine(ContentFilterPreferences.decode(json)) }
}

/** Returns before constructing the hidden content's images or interactive targets. */
@Composable
fun FilteredContentNotice(
    data: HomeFeedResponse.Data,
    scope: FilterScope? = null,
    modifier: Modifier = Modifier,
): Boolean {
    if (!rememberContentFilter().isEnabled) return false
    val input = data.filterInput(scope) ?: return false
    return FilteredContentNotice(input, modifier)
}

@Composable
fun FilteredContentNotice(input: FilterInput, modifier: Modifier = Modifier): Boolean {
    val match = rememberContentFilter().match(input)
    val model: ContentFilterViewModel? = if (match == null) null else hiltViewModel()
    return ContentFilterGate(
        input, match, modifier,
        onDisable = { model?.disableRule(it) },
        onAllow = { key -> model?.update { it.copy(allowedContent = it.allowedContent + key) } },
        error = model?.error,
    )
}

@Composable
internal fun ContentFilterGate(
    input: FilterInput,
    match: ContentFilterMatch?,
    modifier: Modifier = Modifier,
    onDisable: (String) -> Unit,
    onAllow: (String) -> Unit,
    error: String? = null,
): Boolean {
    var revealed by rememberSaveable(input.key ?: input.hashCode().toString()) { mutableStateOf(false) }
    if (match == null || revealed) return false
    FilterNotice(
        reason = match.reason,
        modifier = modifier,
        onReveal = { revealed = true },
        onDisable = { onDisable(match.ruleId) },
        onAllow = {
            input.key?.let(onAllow)
            Unit
        }.takeIf { input.key != null },
        error = error,
    )
    return true
}

@Composable
internal fun FilterNotice(
    reason: String,
    onReveal: () -> Unit,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier,
    onAllow: (() -> Unit)? = null,
    error: String? = null,
) {
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(stringResource(R.string.filter_reason, reason), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onReveal) { Text(stringResource(R.string.filter_reveal)) }
                Button(onClick = onDisable) { Text(stringResource(R.string.filter_disable_rule)) }
                onAllow?.let { Button(onClick = it) { Text(stringResource(R.string.filter_allow)) } }
            }
            error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
        }
    }
}
