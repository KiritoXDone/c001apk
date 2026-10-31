package com.example.c001apk.compose.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.icons.FlutterProfileIcons
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.util.decodeCookieValue
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
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
        item(key = "heading") { SmallTitle("已保存的账号") }
        if (accounts.isEmpty()) {
            item(key = "empty") {
                PreferenceGroup {
                    BasicComponent(title = "暂无保存的账号", summary = "添加账号后，可在这里快速切换")
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
                    title = "添加账号",
                    summary = "登录新账号，保留已有账号",
                    enabled = !busy,
                    onClick = onLogin,
                    startAction = { Icon(FlutterProfileIcons.PersonAdd, null, Modifier.size(24.dp)) },
                )
                ArrowPreference(
                    title = "游客模式",
                    summary = if (activeUid == null) "当前使用游客模式" else "保留账号，下次可以直接切换",
                    enabled = !busy && activeUid != null,
                    onClick = onGuest,
                    startAction = { Icon(MiuixIcons.Contacts, null, Modifier.size(24.dp)) },
                )
            }
            Text(
                "账号仅保存在本机，切换不会清除本地内容。",
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
            if (account.avatar.isBlank()) Icon(MiuixIcons.Contacts, null, Modifier.size(40.dp))
            else CoilLoader(url = account.avatar, modifier = Modifier.size(40.dp).clip(CircleShape))
        },
        endActions = {
            MiuixMoreMenu(expanded = menu && !busy, onExpandedChange = { if (!busy) menu = it }) {
                MiuixMenuItem(
                    text = { Text("删除账号") },
                    onClick = { menu = false; if (!busy) onRemove() },
                )
            }
        },
    ) {
        Text(decodeCookieValue(account.username).ifBlank { "酷安用户" },
            style = MiuixTheme.textStyles.headline2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            when {
                account.needsLogin -> "登录已失效，点击重新登录"
                active -> "当前账号"
                else -> "UID ${account.uid}"
            },
            style = MiuixTheme.textStyles.footnote1,
            color = if (active && !account.needsLogin) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
