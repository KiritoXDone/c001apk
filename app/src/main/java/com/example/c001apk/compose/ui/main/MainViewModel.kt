package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.logic.smid.DigitalUnionRepository
import com.example.c001apk.compose.logic.smid.observeProtocolIdentityRefresh
import com.example.c001apk.compose.ui.base.PrefsViewModel
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.encodeCookieValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/6/3 */
@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val networkRepo: NetworkRepo,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val digitalUnion: DigitalUnionRepository,
    private val accounts: com.example.c001apk.compose.logic.accounts.AccountStore,
) : PrefsViewModel(userPreferencesRepository) {

    internal val startup = MainStartupState()

    init {
        viewModelScope.launch {
            startup.prepare(
                initializeAccount = { accounts.initialize() },
                ensureId = { digitalUnion.ensureId() },
            )
            getCheckLoginInfo()
        }
        viewModelScope.launch {
            observeProtocolIdentityRefresh(userPreferencesRepository.data,
                refresh = { digitalUnion.ensureId() }, onFailure = startup::reportIdentityFailure)
        }
    }

    var badge by mutableIntStateOf(0)
        private set

    private fun getCheckLoginInfo() {
        viewModelScope.launch {
            accounts.data
                .distinctUntilChangedBy { it.accountRevision }
                .collectLatest { prefs ->
                    badge = 0
                    val revision = prefs.accountRevision
                    networkRepo.checkLoginInfo().collect { result ->
                        if (CookieUtil.accountSession.revision != revision) return@collect
                        val failure =
                            result.exceptionOrNull()
                                as? com.example.c001apk.compose.logic.network.ApiFailure
                        if (prefs.isLogin && failure?.expiredSession == true)
                            accounts.expire(revision)
                        val response = result.getOrNull() ?: return@collect
                        val login = response.body()?.data
                        // Anonymous or partial responses never create/replace a saved account.
                        if (prefs.isLogin && login?.uid == prefs.uid) {
                            badge = login.notifyCount.badge
                            CookieUtil.atme = login.notifyCount.atme
                            CookieUtil.atcommentme = login.notifyCount.atcommentme
                            CookieUtil.feedlike = login.notifyCount.feedlike
                            CookieUtil.contacts_follow = login.notifyCount.contactsFollow
                            CookieUtil.message = login.notifyCount.message
                            CookieUtil.notification = login.notifyCount.notification
                            accounts.refreshProfile(
                                revision,
                                username = encodeCookieValue(login.username),
                                avatar = login.userAvatar,
                            )
                        }
                        if (prefs.isLogin && response.body()?.message == "登录信息有误")
                            accounts.expire(revision)
                    }
                }
        }
    }

    suspend fun refreshNotificationCounts() {
        val session = CookieUtil.accountSession
        if (!session.isLogin) return
        networkRepo.checkCount().collect { result ->
            if (session.revision != CookieUtil.accountSession.revision) return@collect
            result.getOrNull()?.data?.let {
                badge = it.badge
                CookieUtil.atme = it.atme
                CookieUtil.atcommentme = it.atcommentme
                CookieUtil.feedlike = it.feedlike
                CookieUtil.contacts_follow = it.contactsFollow
                CookieUtil.message = it.message
                CookieUtil.notification = it.notification
            }
        }
    }

    fun resetBadge() {
        badge = 0
    }
}
