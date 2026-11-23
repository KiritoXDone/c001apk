package com.example.c001apk.compose.ui.contentfilter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
        PreferenceGroup(title = stringResource(R.string.twilight_title)) {
            BasicListItem(
                headlineText = stringResource(if (busy) R.string.status_processing else if (rules == null) R.string.twilight_import else R.string.twilight_update),
                supportingText = message ?: stringResource(R.string.twilight_source),
                enabled = !busy, onClick = onImport,
            )
            if (rules != null) {
                BasicListItem(headlineText = stringResource(R.string.twilight_details),
                    supportingText = stringResource(R.string.twilight_details_summary, rules.revision, rules.rules.size), onClick = onDetails)
                SwitchListItem(preferences.twilightStrict, MiuixIcons.Blocklist, stringResource(R.string.twilight_strict),
                    stringResource(R.string.twilight_strict_summary), enabled = !busy) { strict ->
                    onUpdate { it.copy(twilightStrict = strict) }
                }
                BasicListItem(headlineText = stringResource(R.string.twilight_remove), supportingText = stringResource(R.string.twilight_remove_hint),
                    enabled = !busy, onClick = { removing = true })
            }
        }
        if (removing) MiuixAlertDialog(onDismissRequest = { removing = false }, title = { Text(stringResource(R.string.twilight_remove_title)) },
            text = { Text(stringResource(R.string.twilight_remove_summary)) },
            confirmButton = { MiuixDialogButton(enabled = !busy, destructive = true, onClick = { removing = false; onRemove() }) { Text(stringResource(R.string.action_remove)) } },
            dismissButton = { MiuixDialogButton(onClick = { removing = false }) { Text(stringResource(R.string.action_cancel)) } })
    }
}
