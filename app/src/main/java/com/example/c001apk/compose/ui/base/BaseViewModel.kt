package com.example.c001apk.compose.ui.base

import com.example.c001apk.compose.logic.filter.loadFilteredPages
import com.example.c001apk.compose.logic.filter.ContentFilterStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.constant.Constants.entityTemplateList
import com.example.c001apk.compose.constant.Constants.entityTypeList
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.FeedDeletionEvents
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.util.CookieUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.example.c001apk.compose.logic.filter.ContentFilterEngine
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/6/10 */
enum class LikeType {
    FEED,
    REPLY,
}

abstract class BaseViewModel(
    val networkRepo: NetworkRepo,
    val blackListRepo: BlackListRepo,
    protected val contentFilters: ContentFilterStore,
) : ViewModel() {

    private val accountRevision = CookieUtil.accountSession.revision

    var isRefreshing by mutableStateOf(false)

    var loadingState by
        mutableStateOf<LoadingState<List<HomeFeedResponse.Data>>>(LoadingState.Loading)

    var footerState by mutableStateOf<FooterState>(FooterState.Success)

    var page = 1
    var firstLaunch = 1
    var isLoadMore = false
    var isEnd = false
    var automaticLoadPaused by mutableStateOf(false)
        protected set
    val stopAutomaticLoading get() = isEnd || automaticLoadPaused
    var firstItem: String? = null
    var lastItem: String? = null

    abstract suspend fun customFetchData(): Flow<LoadingState<List<HomeFeedResponse.Data>>>

    private var fetchJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            FeedDeletionEvents.events.collect { deletedId ->
                val state = loadingState as? LoadingState.Success ?: return@collect
                loadingState =
                    LoadingState.Success(
                        state.response.filterNot {
                            it.entityType == "feed" && it.id == deletedId
                        },
                    )
            }
        }
    }

    protected fun cancelFetch() {
        fetchJob?.cancel()
    }

    private var fetchGeneration = 0

    fun fetchData() {
        if (accountRevision != CookieUtil.accountSession.revision) return
        val generation = ++fetchGeneration
        fetchJob?.cancel()
        val append = isLoadMore && !isRefreshing
        automaticLoadPaused = false
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val filters = ContentFilterEngine(contentFilters.data.first())
                val result = loadFilteredPages(
                    fetch = { customFetchData().first { it !is LoadingState.Loading } },
                    accept = { state, attempt ->
                        if (accountRevision != CookieUtil.accountSession.revision || generation != fetchGeneration)
                            throw kotlinx.coroutines.CancellationException()
                        when (state) {
                            LoadingState.Empty -> {
                                isEnd = true
                                if (loadingState is LoadingState.Success && (append || attempt > 0))
                                    footerState = FooterState.End
                                else {
                                    loadingState = LoadingState.Empty
                                    footerState = FooterState.Success
                                }
                                true
                            }
                            is LoadingState.Error -> {
                                automaticLoadPaused = true
                                if (loadingState is LoadingState.Success) {
                                    footerState = FooterState.Error(state.errMsg)
                                    if (isRefreshing) toastText = state.errMsg
                                } else loadingState = state
                                true
                            }
                            is LoadingState.Success -> {
                                val raw = state.response
                                page++
                                if (firstItem == null) firstItem = raw.firstOrNull { !it.id.isNullOrBlank() }?.id
                                lastItem = raw.lastOrNull { !it.id.isNullOrBlank() }?.id ?: lastItem
                                val rawEnd = raw.any { it.entityTemplate == "noMoreDataCard" }
                                isEnd = rawEnd
                                val templates = if (CookieUtil.showSquare) entityTemplateList else
                                    entityTemplateList.filterNot { it in listOf("iconMiniScrollCard", "iconMiniGridCard") }
                                var response = raw.filter {
                                    (it.entityType in entityTypeList || it.entityTemplate in templates || isAdditionalItemSupported(it)) &&
                                        !blackListRepo.checkUid(it.fromuid?.takeIf(String::isNotBlank) ?: it.uid.orEmpty()) &&
                                        !blackListRepo.checkTopic(it.tags.orEmpty() + it.ttitle.orEmpty() + it.relationRows?.firstOrNull()?.title.orEmpty())
                                }
                                response = handleResponse(response) ?: response
                                isEnd = isEnd || rawEnd
                                val hasVisible = response.any { filters.match(it) == null }
                                if (append || attempt > 0) {
                                    response = (loadingState as? LoadingState.Success)?.response.orEmpty() + response
                                }
                                response = handleLoadMore(response) ?: response
                                // Preserve fetched rows so changing a content rule needs no network refresh.
                                loadingState = LoadingState.Success(response)
                                footerState = if (isEnd) FooterState.End else FooterState.Success
                                isEnd || hasVisible
                            }
                            LoadingState.Loading -> true
                        }
                    },
                )
                if (result) {
                    automaticLoadPaused = true
                    footerState = FooterState.Error(AppLanguage.getString(R.string.filter_batch_paused))
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (generation == fetchGeneration) {
                    automaticLoadPaused = true
                    val message = ApiFailures.safeMessage(failure)
                    if (loadingState is LoadingState.Success) footerState = FooterState.Error(message)
                    else loadingState = LoadingState.Error(message)
                }
            } finally {
                if (generation == fetchGeneration) {
                    isLoadMore = false
                    isRefreshing = false
                }
            }
        }
    }

    open fun handleResponse(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data>? {
        return null
    }

    open fun isAdditionalItemSupported(item: HomeFeedResponse.Data): Boolean = false

    open fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data>? {
        return null
    }

    open fun refresh() {
        if (!isRefreshing) {
            // A user refresh takes precedence over an in-flight pagination request.
            cancelFetch()
            page = 1
            isEnd = false
            isLoadMore = false
            isRefreshing = true
            firstItem = null
            lastItem = null
            fetchData()
        }
    }

    open fun loadMore() {
        if (!isRefreshing && !isLoadMore && fetchJob?.isActive != true) {
            isEnd = false
            isLoadMore = true
            fetchData()
            if (loadingState is LoadingState.Success) {
                footerState = FooterState.Loading
            } else {
                loadingState = LoadingState.Loading
            }
        }
    }

    var toastText by mutableStateOf<String?>(null)

    fun resetToastText() {
        toastText = null
    }

    fun onLike(id: String, like: Int, likeType: LikeType) {
        val isLike =
            when (likeType) {
                LikeType.FEED -> if (like == 1) "unlike" else "like"
                LikeType.REPLY -> if (like == 1) "unLikeReply" else "likeReply"
            }
        val likeUrl = "/v6/feed/$isLike"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLike(likeUrl, id).collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    if (!response.message.isNullOrEmpty()) {
                        toastText = response.message
                    } else if (response.data != null) {
                        if (handleLikeResponse(id, like, response.data.count) == null) {
                            val dataList =
                                (loadingState as LoadingState.Success).response.map {
                                    if (it.id == id) {
                                        it.copy(
                                            likenum = response.data.count,
                                            userAction =
                                                it.userAction?.copy(like = if (like == 1) 0 else 1),
                                        )
                                    } else it
                                }
                            loadingState = LoadingState.Success(dataList)
                        }
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    open fun handleLikeResponse(id: String, like: Int, count: String?): Boolean? {
        return null
    }

    fun onDelete(id: String, deleteType: LikeType) {
        val url = if (deleteType == LikeType.FEED) "/v6/feed/deleteFeed" else "/v6/feed/deleteReply"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id).collect { result ->
                val data = result.getOrNull()
                if (data != null) {
                    if (!data.message.isNullOrEmpty()) {
                        toastText = data.message
                    } else if (data.data?.count == "删除成功") {
                        var response = (loadingState as LoadingState.Success).response
                        handleDeleteResponse(id, response)?.let {
                            response = it
                            loadingState = LoadingState.Success(response)
                        }
                        toastText = data.data.count
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    open fun handleDeleteResponse(
        id: String,
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        return response.filterNot { it.id == id }
    }

    open fun onBlockUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)

            if (loadingState is LoadingState.Success) {
                var response =
                    (loadingState as LoadingState.Success).response.filterNot { it.uid == uid }
                handleBlockUser(uid, response)?.let {
                    response = it
                }
                loadingState = LoadingState.Success(response)
            }
        }
    }

    open fun handleBlockUser(
        uid: String,
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        return null
    }

    fun onFollowUser(uid: String, isFollow: Int) {
        val url = if (isFollow == 1) "/v6/user/unfollow" else "/v6/user/follow"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid).collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    if (!response.message.isNullOrEmpty()) {
                        toastText = response.message
                    } else {
                        val follow = if (isFollow == 1) 0 else 1
                        if (handleFollowResponse(follow) == null) {
                            val dataList =
                                (loadingState as LoadingState.Success).response.map {
                                    if (it.uid == uid) it.copy(isFollow = follow) else it
                                }
                            loadingState = LoadingState.Success(dataList)
                        }
                        toastText = AppLanguage.getString(if (follow == 1) R.string.user_followed else R.string.user_unfollowed)
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    open fun handleFollowResponse(follow: Int): Boolean? {
        return null
    }
}
