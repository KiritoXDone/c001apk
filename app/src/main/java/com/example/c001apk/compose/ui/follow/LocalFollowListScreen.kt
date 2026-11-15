package com.example.c001apk.compose.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.follow.LocalFollowUser
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LocalFollowListScreen(onBack: () -> Unit, onUser: (String) -> Unit) {
    val prefs = LocalUserPreferences.current
    val model = hiltViewModel<FollowSettingsViewModel>()
    var remove by remember { mutableStateOf<LocalFollowUser?>(null) }
    MiuixScaffold(
        topBar = { MiuixTopBar(title = "本地关注", navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        val listState = rememberLazyListState()
        TopBarScrollEffect { listState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp),
        ) {
            item(key = "settings") {
                PreferenceGroup(title = "关注设置") {
                    SwitchPreference(
                        title = "悄悄关注",
                        summary = "登录后长按用户主页的关注按钮添加",
                        checked = prefs.quietFollow,
                        onCheckedChange = model::quiet,
                    )
                }
                Text("名单仅保存在本机，切换账号或关闭开关后仍保留。",
                    Modifier.padding(horizontal = 28.dp).padding(bottom = 16.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                SmallTitle("已关注 ${prefs.localFollows.size} 人")
            }
            itemsIndexed(prefs.localFollows, key = { _, user -> user.uid }) { index, user ->
                GroupedListItem(first = index == 0, last = index == prefs.localFollows.lastIndex) {
                    LocalFollowUserRow(user, onUser = { onUser(user.uid) }, onRemove = { remove = user })
                }
            }
            item(key = "hint") {
                if (prefs.localFollows.isEmpty()) {
                    PreferenceGroup {
                        BasicComponent(title = "还没有本地关注",
                            summary = if (prefs.isLogin) "开启悄悄关注后，长按用户主页的关注按钮添加。"
                                else "在用户主页点击“本地关注”，即可在这里查看。")
                    }
                } else Text("点击用户查看主页", Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }
    }
    remove?.let { user ->
        MiuixAlertDialog(
            onDismissRequest = { remove = null },
            title = { Text("取消本地关注？") },
            text = { Text("仅从本机名单移除，不会改变账号的关注关系。") },
            confirmButton = {
                MiuixDialogButton(
                    destructive = true,
                    onClick = {
                        remove = null
                        model.remove(user.uid)
                    },
                ) {
                    Text("移除")
                }
            },
            dismissButton = { MiuixDialogButton(onClick = { remove = null }) { Text("取消") } },
        )
    }
}

@Composable
internal fun LocalFollowUserRow(user: LocalFollowUser, onUser: () -> Unit, onRemove: () -> Unit) {
    var menu by remember(user.uid) { mutableStateOf(false) }
    BasicComponent(
        onClick = onUser,
        startAction = {
            if (user.avatar.isBlank()) Icon(MiuixIcons.Contacts, null, Modifier.size(40.dp))
            else CoilLoader(url = user.avatar, modifier = Modifier.size(40.dp).clip(CircleShape))
        },
        endActions = {
            MiuixMoreMenu(expanded = menu, onExpandedChange = { menu = it }) {
                MiuixMenuItem(text = { Text("移除本地关注") }, onClick = { menu = false; onRemove() })
            }
        },
    ) {
        Text(user.name.ifBlank { "UID ${user.uid}" }, style = MiuixTheme.textStyles.headline2,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (user.name.isNotBlank()) Text("UID ${user.uid}", style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}
