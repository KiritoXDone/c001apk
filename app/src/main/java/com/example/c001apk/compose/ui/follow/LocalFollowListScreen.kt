package com.example.c001apk.compose.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.follow.LocalFollowUser
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LocalFollowListScreen(onBack: () -> Unit, onUser: (String) -> Unit) {
    val prefs = LocalUserPreferences.current
    val model = hiltViewModel<FollowSettingsViewModel>()
    var remove by remember { mutableStateOf<LocalFollowUser?>(null) }
    MiuixScaffold(
        topBar = { MiuixTopBar(title = stringResource(R.string.follow_local_title), navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        val listState = rememberLazyListState()
        TopBarScrollEffect { listState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp),
        ) {
            item(key = "settings") {
                PreferenceGroup(title = stringResource(R.string.follow_settings)) {
                    SwitchPreference(
                        title = stringResource(R.string.user_quiet_follow),
                        summary = stringResource(R.string.follow_quiet_add),
                        checked = prefs.quietFollow,
                        onCheckedChange = model::quiet,
                    )
                }
                Text(stringResource(R.string.follow_local_notice),
                    Modifier.padding(horizontal = 28.dp).padding(bottom = 16.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                SmallTitle(stringResource(R.string.follow_local_count, prefs.localFollows.size))
            }
            itemsIndexed(prefs.localFollows, key = { _, user -> user.uid }) { index, user ->
                GroupedListItem(first = index == 0, last = index == prefs.localFollows.lastIndex) {
                    LocalFollowUserRow(user, onUser = { onUser(user.uid) }, onRemove = { remove = user })
                }
            }
            item(key = "hint") {
                if (prefs.localFollows.isEmpty()) {
                    PreferenceGroup {
                        BasicComponent(title = stringResource(R.string.follow_local_empty),
                            summary = stringResource(if (prefs.isLogin) R.string.follow_quiet_hint else R.string.follow_local_hint))
                    }
                } else Text(stringResource(R.string.follow_view_profile), Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }
    }
    remove?.let { user ->
        MiuixAlertDialog(
            onDismissRequest = { remove = null },
            title = { Text(stringResource(R.string.follow_remove_title)) },
            text = { Text(stringResource(R.string.follow_remove_summary)) },
            confirmButton = {
                MiuixDialogButton(
                    destructive = true,
                    onClick = {
                        remove = null
                        model.remove(user.uid)
                    },
                ) {
                    Text(stringResource(R.string.action_remove))
                }
            },
            dismissButton = { MiuixDialogButton(onClick = { remove = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
internal fun LocalFollowUserRow(user: LocalFollowUser, onUser: () -> Unit, onRemove: () -> Unit) {
    var menu by remember(user.uid) { mutableStateOf(false) }
    BasicComponent(
        onClick = onUser,
        startAction = {
            if (user.avatar.isBlank()) Icon(MiuixIcons.Glass.ContactsCircle, null, Modifier.size(40.dp))
            else CoilLoader(url = user.avatar, modifier = Modifier.size(40.dp).clip(CircleShape))
        },
        endActions = {
            MiuixMoreMenu(expanded = menu, onExpandedChange = { menu = it }) {
                MiuixMenuItem(text = { Text(stringResource(R.string.follow_remove)) }, onClick = { menu = false; onRemove() })
            }
        },
    ) {
        Text(user.name.ifBlank { "UID ${user.uid}" }, style = MiuixTheme.textStyles.headline2,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (user.name.isNotBlank()) Text("UID ${user.uid}", style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}
