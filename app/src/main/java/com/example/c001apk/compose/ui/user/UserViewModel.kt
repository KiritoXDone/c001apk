package com.example.c001apk.compose.ui.user

import com.example.c001apk.compose.logic.filter.ContentFilterStore
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/6/4 */
@HiltViewModel(assistedFactory = UserViewModel.ViewModelFactory::class)
class UserViewModel
@AssistedInject
constructor(
    @Assisted var uid: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
    private val localFollows: com.example.c001apk.compose.logic.follow.LocalFollowStore,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(uid: String): UserViewModel
    }

    var userState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    init {
        fetchUserProfile()
    }

    lateinit var username: String
    var isBlocked by mutableStateOf(false)

    private fun fetchUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getUserSpace(uid).collect { state ->
                userState = state
                if (state is LoadingState.Success) {
                    val response = state.response
                    uid = response.uid.orEmpty()
                    username = response.username.orEmpty()
                    isBlocked = blackListRepo.checkUid(uid)
                    if (isBlocked) loadingState = LoadingState.Error(AppLanguage.getString(R.string.user_blocked, username))
                    else fetchData()
                }
                isRefreshing = false
            }
        }
    }

    override suspend fun customFetchData() = networkRepo.getUserFeed(uid, page, lastItem)

    private fun handleBlocked() {
        viewModelScope.launch {
            isRefreshing = true
            delay(50)
            isRefreshing = false
        }
        loadingState = LoadingState.Error(AppLanguage.getString(R.string.user_blocked, username))
    }

    var isPull = false

    override fun refresh() {
        if (!isRefreshing && !isLoadMore) {
            if (userState is LoadingState.Success) {
                if (isBlocked) {
                    handleBlocked()
                } else {
                    page = 1
                    isEnd = false
                    isLoadMore = false
                    isRefreshing = true
                    firstItem = null
                    lastItem = null
                    fetchData()
                }
            } else {
                if (isPull) {
                    isPull = false
                    viewModelScope.launch {
                        isRefreshing = true
                        delay(50)
                        isRefreshing = false
                    }
                }
                userState = LoadingState.Loading
                fetchUserProfile()
            }
        }
    }

    override fun loadMore() {
        if (isBlocked) {
            handleBlocked()
        } else {
            super.loadMore()
        }
    }

    fun toggleLocalFollow() {
        val user = (userState as? LoadingState.Success)?.response ?: return
        viewModelScope.launch {
            try {
                localFollows.toggle(
                    com.example.c001apk.compose.logic.follow.LocalFollowUser(
                        uid,
                        user.username.orEmpty(),
                        user.userAvatar.orEmpty(),
                    ),
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                toastText = AppLanguage.getString(R.string.error_local_follow)
            }
        }
    }

    override fun handleFollowResponse(follow: Int): Boolean {
        val response = (userState as LoadingState.Success).response.copy(isFollow = follow)
        userState = LoadingState.Success(response)
        return true
    }

    override fun handleResponse(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        isEnd = response.lastOrNull()?.entityTemplate == "noMoreDataCard"
        return null
    }

    override fun onBlockUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isBlocked) blackListRepo.deleteUid(uid) else blackListRepo.saveUid(uid)
            isBlocked = !isBlocked
        }
    }
}
