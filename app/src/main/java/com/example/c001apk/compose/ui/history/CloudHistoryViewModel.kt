package com.example.c001apk.compose.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.logic.repository.CloudHistoryRepository
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class CloudHistoryViewModel @Inject constructor(
    private val repository: CloudHistoryRepository,
    preferences: UserPreferencesRepository,
) : ViewModel() {
    var rows by mutableStateOf<List<CloudHistoryEntry>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var hasMore by mutableStateOf(true)
        private set
    var loggedIn by mutableStateOf(false)
        private set
    var accountRevision by mutableStateOf(-1L)
        private set
    private var page = 1
    private var firstItem: String? = null
    private var lastItem: String? = null
    private var generation = 0
    private var job: Job? = null

    init {
        viewModelScope.launch {
            preferences.data.map { Triple(it.isLogin, it.uid, it.accountRevision) }
                .distinctUntilChanged().collect { (login, _, revision) ->
                    generation++
                    job?.cancel()
                    accountRevision = revision
                    loggedIn = login
                    rows = emptyList()
                    loading = false
                    refreshing = false
                    error = null
                    resetCursor()
                    if (login) loadMore()
                }
        }
    }

    private fun resetCursor() {
        page = 1
        firstItem = null
        lastItem = null
        hasMore = true
    }

    fun refresh() {
        if (!loggedIn) return
        generation++
        job?.cancel()
        loading = false
        refreshing = true
        resetCursor()
        loadMore()
    }

    fun loadMore() {
        if (!loggedIn || loading || !hasMore) return
        loading = true
        error = null
        val requestGeneration = generation
        val replace = page == 1
        job = viewModelScope.launch {
            try {
                val result = repository.page(page, firstItem, lastItem, accountRevision)
                if (requestGeneration != generation) return@launch
                val cursor = result.lastOrNull()?.id?.takeIf { it.isNotBlank() }
                check(result.isEmpty() || (cursor != null && cursor != lastItem)) { "历史分页游标未更新" }
                val entries = result.filter { !it.id.isNullOrBlank() && !it.url.isNullOrBlank() }
                rows = ((if (replace) emptyList() else rows) + entries).distinctBy { it.id }
                if (firstItem == null) firstItem = result.firstOrNull()?.id
                lastItem = cursor ?: lastItem
                hasMore = result.isNotEmpty()
                page++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration == generation) error = ApiFailures.safeMessage(e)
            } finally {
                if (requestGeneration == generation) {
                    loading = false
                    refreshing = false
                }
            }
        }
    }
}
