package com.example.c001apk.compose.ui.accounts

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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

    var errorRes by mutableStateOf<Int?>(null)
        private set

    private fun act(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        errorRes = null
        viewModelScope.launch {
            try {
                block()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                errorRes = R.string.accounts_action_failed
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
        topBar = { MiuixTopBar(title = stringResource(R.string.settings_accounts), navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        val topBarListState = rememberLazyListState()
        TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
        AccountsContent(
            accounts = prefs.savedAccounts,
            activeUid = prefs.uid.takeIf { prefs.isLogin },
            busy = model.busy,
            error = model.errorRes?.let { stringResource(it) },
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
            title = { Text(stringResource(R.string.accounts_delete_title)) },
            text = { Text(stringResource(R.string.accounts_delete_summary)) },
            confirmButton = {
                MiuixDialogButton(
                    destructive = true,
                    onClick = {
                        deleting = null
                        model.remove(account.uid)
                    },
                ) {
                    Text(stringResource(R.string.menu_delete))
                }
            },
            dismissButton = { MiuixDialogButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}
