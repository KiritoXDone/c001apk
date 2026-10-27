package com.example.c001apk.compose.ui.follow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.follow.LocalFollowUser
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun LocalFollowListScreen(onBack: () -> Unit, onUser: (String) -> Unit) {
    val prefs = LocalUserPreferences.current
    val model = hiltViewModel<FollowSettingsViewModel>()
    var remove by remember { mutableStateOf<LocalFollowUser?>(null) }
    MiuixScaffold(
        topBar = { MiuixTopBar(title = "本地关注", navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        val topBarListState = rememberLazyListState()
        TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = topBarListState,
            contentPadding = padding,
        ) {
            item {
                SwitchPreference(
                    title = "悄悄关注",
                    summary = "登录后长按用户主页的关注按钮，保存到本机名单",
                    checked = prefs.quietFollow,
                    onCheckedChange = model::quiet,
                )
                Text("名单在切换账号、退出登录或关闭此开关后保留。", Modifier.padding(20.dp))
            }
            items(prefs.localFollows, key = { it.uid }) { user ->
                BasicComponent(
                    title = user.name.ifBlank { "UID ${user.uid}" },
                    summary = "UID ${user.uid}",
                    onClick = { onUser(user.uid) },
                    startAction = {
                        CoilLoader(url = user.avatar, modifier = Modifier.size(40.dp))
                    },
                    endActions = {
                        MiuixActionButton(onClick = { remove = user }) { Text("取消关注") }
                    },
                )
            }
            if (prefs.localFollows.isEmpty())
                item { Text("还没有本地关注。在用户主页添加后即可查看动态。", Modifier.padding(24.dp)) }
        }
    }
    remove?.let { user ->
        MiuixAlertDialog(
            onDismissRequest = { remove = null },
            title = { Text("取消本地关注？") },
            text = { Text("仅从本机名单移除，不会改变账号的关注关系。") },
            confirmButton = {
                MiuixDialogButton(
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
