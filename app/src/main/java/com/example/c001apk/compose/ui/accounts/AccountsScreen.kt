package com.example.c001apk.compose.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.accounts.AccountStore
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.icons.FlutterProfileIcons
import com.example.c001apk.compose.util.decodeCookieValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.*

@HiltViewModel
class AccountsViewModel @Inject constructor(private val accounts: AccountStore) : ViewModel() {
    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private fun act(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        viewModelScope.launch {
            try {
                block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                error = "账号操作未完成，请重试"
            } finally {
                busy = false
            }
        }
    }

    fun switch(uid: String) = act { accounts.switchTo(uid) }

    fun guest() = act { accounts.useGuest() }

    fun remove(uid: String) = act { accounts.remove(uid) }
}

@Composable
fun AccountsScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    val prefs = LocalUserPreferences.current
    val model = hiltViewModel<AccountsViewModel>()
    var deleting by remember { mutableStateOf<SavedAccount?>(null) }
    MiuixScaffold(
        topBar = { MiuixTopBar(title = "账号管理", navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        val topBarListState = rememberLazyListState()
        TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = topBarListState,
            contentPadding = padding,
        ) {
            item {
                BasicComponent(
                    title = "添加账号",
                    summary = "登录新账号，保留已有账号",
                    enabled = !model.busy,
                    onClick = onLogin,
                    startAction = { Icon(FlutterProfileIcons.PersonAdd, null) },
                )
                BasicComponent(
                    title = "游客模式",
                    summary = if (!prefs.isLogin) "当前使用游客模式" else "保留账号，下次可以直接切换",
                    enabled = !model.busy && prefs.isLogin,
                    onClick = model::guest,
                )
                HorizontalDivider()
            }
            items(prefs.savedAccounts, key = { it.uid }) { account ->
                BasicComponent(
                    title = decodeCookieValue(account.username).ifBlank { "酷安用户" },
                    summary =
                        when {
                            account.needsLogin -> "登录已失效，点击重新登录"
                            prefs.isLogin && prefs.uid == account.uid -> "当前账号"
                            else -> "UID ${account.uid}"
                        },
                    enabled = !model.busy,
                    onClick = { if (account.needsLogin) onLogin() else model.switch(account.uid) },
                    startAction = {
                        CoilLoader(
                            url = account.avatar,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                        )
                    },
                    endActions = {
                        MiuixActionButton(onClick = { deleting = account }, enabled = !model.busy) {
                            Text("删除")
                        }
                    },
                )
            }
            item {
                if (prefs.savedAccounts.isEmpty()) Text("暂无保存的账号", Modifier.padding(24.dp))
                if (model.busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp))
                model.error?.let { Text(it, Modifier.padding(24.dp)) }
            }
        }
    }
    deleting?.let { account ->
        MiuixAlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除已保存的账号？") },
            text = { Text("删除后需要重新登录；本地收藏、历史和关注会保留。") },
            confirmButton = {
                MiuixDialogButton(
                    onClick = {
                        deleting = null
                        model.remove(account.uid)
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = { MiuixDialogButton(onClick = { deleting = null }) { Text("取消") } },
        )
    }
}
