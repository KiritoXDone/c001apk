package com.example.c001apk.compose.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.util.decodeCookieValue
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.AddCircle
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AccountsContent(
    accounts: List<SavedAccount>,
    activeUid: String?,
    busy: Boolean,
    error: String?,
    padding: PaddingValues,
    listState: LazyListState,
    onLogin: () -> Unit,
    onGuest: () -> Unit,
    onSelect: (SavedAccount) -> Unit,
    onRemove: (SavedAccount) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
    ) {
        item(key = "heading") { SmallTitle(stringResource(R.string.accounts_saved)) }
        if (accounts.isEmpty()) {
            item(key = "empty") {
                PreferenceGroup {
                    BasicComponent(title = stringResource(R.string.accounts_empty), summary = stringResource(R.string.accounts_empty_summary))
                }
            }
        }
        itemsIndexed(accounts, key = { _, account -> account.uid }) { index, account ->
            GroupedListItem(first = index == 0, last = index == accounts.lastIndex) {
                AccountRow(account, activeUid == account.uid, busy,
                    onSelect = { onSelect(account) }, onRemove = { onRemove(account) })
            }
        }
        item(key = "actions") {
            PreferenceGroup(modifier = Modifier.padding(top = 16.dp)) {
                ArrowPreference(
                    title = stringResource(R.string.accounts_add),
                    summary = stringResource(R.string.accounts_add_summary),
                    enabled = !busy,
                    onClick = onLogin,
                    startAction = { Icon(MiuixIcons.Glass.AddCircle, null, Modifier.size(24.dp)) },
                )
                ArrowPreference(
                    title = stringResource(R.string.accounts_guest),
                    summary = stringResource(if (activeUid == null) R.string.accounts_guest_active else R.string.accounts_guest_summary),
                    enabled = !busy && activeUid != null,
                    onClick = onGuest,
                    startAction = { Icon(MiuixIcons.Glass.ContactsCircle, null, Modifier.size(24.dp)) },
                )
            }
            Text(
                stringResource(R.string.accounts_local_notice),
                Modifier.padding(horizontal = 28.dp),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp))
            error?.let {
                Text(it, Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    color = MiuixTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AccountRow(
    account: SavedAccount,
    active: Boolean,
    busy: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember(account.uid) { mutableStateOf(false) }
    LaunchedEffect(busy) { if (busy) menu = false }
    BasicComponent(
        enabled = !busy,
        onClick = onSelect,
        startAction = {
            if (account.avatar.isBlank()) Icon(MiuixIcons.Glass.ContactsCircle, null, Modifier.size(40.dp))
            else CoilLoader(url = account.avatar, modifier = Modifier.size(40.dp).clip(CircleShape))
        },
        endActions = {
            MiuixMoreMenu(expanded = menu && !busy, onExpandedChange = { if (!busy) menu = it }) {
                MiuixMenuItem(
                    text = { Text(stringResource(R.string.accounts_delete)) },
                    onClick = { menu = false; if (!busy) onRemove() },
                )
            }
        },
    ) {
        Text(decodeCookieValue(account.username).ifBlank { stringResource(R.string.accounts_default_name) },
            style = MiuixTheme.textStyles.headline2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            when {
                account.needsLogin -> stringResource(R.string.accounts_expired)
                active -> stringResource(R.string.accounts_current)
                else -> "UID ${account.uid}"
            },
            style = MiuixTheme.textStyles.footnote1,
            color = if (active && !account.needsLogin) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
