package com.example.c001apk.compose.ui.accounts

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.accounts.AccountStore
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
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
        AccountsContent(
            accounts = prefs.savedAccounts,
            activeUid = prefs.uid.takeIf { prefs.isLogin },
            busy = model.busy,
            error = model.error,
            padding = padding,
            listState = topBarListState,
            onLogin = onLogin,
            onGuest = model::guest,
            onSelect = { account -> if (account.needsLogin) onLogin() else model.switch(account.uid) },
            onRemove = { deleting = it },
        )
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
