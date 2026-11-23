package com.example.c001apk.compose.ui.follow

import com.example.c001apk.compose.logic.filter.loadFilteredPages
import com.example.c001apk.compose.logic.filter.ContentFilterStore
import com.example.c001apk.compose.logic.filter.ContentFilterEngine
import androidx.compose.runtime.*
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.follow.*
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.*
import com.example.c001apk.compose.logic.state.*
import com.example.c001apk.compose.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class LocalFollowViewModel
@Inject
constructor(
    private val store: LocalFollowStore,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {
    private var pager: LocalFollowPager? = null
    private var work: Job? = null
    var warning by mutableStateOf<String?>(null)
        private set

    private var users = emptyList<LocalFollowUser>()

    init {
        viewModelScope.launch {
            combine(
                    store.data.map { it.localFollows }.distinctUntilChanged(),
                    blackListRepo.loadAllUserListFlow(),
                    blackListRepo.loadAllTopicListFlow(),
                ) { list, _, _ ->
                    list
                }
                .collect {
                    users = it
                    refresh()
                }
        }
    }

    override suspend fun customFetchData(): Flow<LoadingState<List<HomeFeedResponse.Data>>> =
        flowOf(LoadingState.Empty)

    override fun refresh() {
        work?.cancel()
        work = viewModelScope.launch {
            isRefreshing = true
            loadingState = LoadingState.Loading
            val visible = users.filterNot { blackListRepo.checkUid(it.uid) }
            val current =
                LocalFollowPager(visible.map { it.uid }) { uid, page, last ->
                    networkRepo.getUserFeed(uid, page, last, includeTop = false).first()
                }
            pager = current
            try {
                append(current, reset = true)
            } finally {
                isRefreshing = false
            }
        }
    }

    override fun loadMore() {
        if (work?.isActive == true) return
        val current = pager ?: return
        if (current.isEnd) return
        work = viewModelScope.launch {
            isLoadMore = true
            footerState = FooterState.Loading
            try {
                append(current, reset = false)
            } finally {
                isLoadMore = false
            }
        }
    }

    private suspend fun append(current: LocalFollowPager, reset: Boolean) {
        automaticLoadPaused = false
        val filters = ContentFilterEngine(contentFilters.data.first())
        var combined = if (reset) emptyList() else (loadingState as? LoadingState.Success)?.response.orEmpty()
        val paused = loadFilteredPages(
            fetch = { current.next() },
            accept = { items, _ ->
                val added = items.filterNot { blackListRepo.checkTopic(it.tags.orEmpty() + it.ttitle.orEmpty()) }
                combined = (combined + added).distinctBy { it.id }
                warning = when {
                    current.failedUsers > 0 -> AppLanguage.getString(R.string.follow_failed_users, current.failedUsers)
                    current.warning != null -> AppLanguage.getString(R.string.filter_batch_paused)
                    else -> null
                }
                current.isEnd || warning != null || added.any { filters.match(it) == null }
            },
        )
        automaticLoadPaused = paused || warning != null
        isEnd = current.isEnd
        if (paused) warning = AppLanguage.getString(R.string.filter_batch_paused)
        loadingState =
            if (combined.isNotEmpty() || paused) LoadingState.Success(combined)
            else if (warning != null) LoadingState.Error(warning!!) else LoadingState.Empty
        footerState =
            if (warning != null) FooterState.Error(warning!!)
            else if (current.isEnd) FooterState.End else FooterState.Success
    }
}

@HiltViewModel
class FollowSettingsViewModel @Inject constructor(private val store: LocalFollowStore) :
    androidx.lifecycle.ViewModel() {
    fun select(value: Int) {
        viewModelScope.launch { store.select(value) }
    }

    fun quiet(enabled: Boolean) {
        viewModelScope.launch { store.setQuiet(enabled) }
    }

    fun remove(uid: String) {
        viewModelScope.launch { store.remove(uid) }
    }
}
