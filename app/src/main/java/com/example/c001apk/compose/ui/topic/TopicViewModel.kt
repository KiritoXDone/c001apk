package com.example.c001apk.compose.ui.topic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.LoadingState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@HiltViewModel(assistedFactory = TopicViewModel.ViewModelFactory::class)
class TopicViewModel @AssistedInject constructor(
    @Assisted("url") val url: String,
    @Assisted("tag") val tag: String?,
    @Assisted("id") var id: String?,
    private val networkRepo: NetworkRepo,
    private val blackListRepo: BlackListRepo,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("tag") tag: String?,
            @Assisted("id") id: String?,
        ): TopicViewModel
    }

    var topicState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    init {
        fetchTopicLayout()
    }

    lateinit var entityType: String
    lateinit var title: String
    var selectedTab: String? = null
    var tabList: List<HomeFeedResponse.TabList>? = null
    var productConfigRows: List<HomeFeedResponse.ProductConfig> = emptyList()
    var productLogo: String? = null

    var isFollowed by mutableStateOf(false)
    var isBlocked by mutableStateOf(false)
        private set

    private fun fetchTopicLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getTopicLayout(url, tag, id)
                .collect { state ->
                    if (state is LoadingState.Success) {
                        val response = state.response
                        id = response.id
                        entityType = response.entityType.orEmpty()
                        title = response.title.orEmpty()
                        tabList = response.tabList
                        productConfigRows = response.configRows.orEmpty()
                        productLogo = response.logo
                        selectedTab = response.selectedTab
                        isFollowed = response.userAction?.follow == 1
                        checkIsBlocked(title)
                    }
                    topicState = state
                }
        }
    }

    fun refresh() {
        topicState = LoadingState.Loading
        fetchTopicLayout()
    }

    var toastText by mutableStateOf<String?>(null)
        private set

    fun resetToastText() {
        toastText = null
    }

    private var followJob: kotlinx.coroutines.Job? = null

    fun onGetFollow() {
        if (!::title.isInitialized) return
        changeFollow { desired ->
            networkRepo.getFollow(if (desired) "/v6/feed/followTag" else "/v6/feed/unFollowTag", title, null)
        }
    }

    fun onPostFollow() {
        if (id.isNullOrBlank()) return
        changeFollow { desired ->
            networkRepo.postFollow(hashMapOf("id" to id.orEmpty(), "status" to if (desired) "1" else "0"))
        }
    }

    private fun changeFollow(request: suspend (Boolean) -> kotlinx.coroutines.flow.Flow<Result<com.example.c001apk.compose.logic.model.LikeResponse>>) {
        if (followJob?.isActive == true) return
        val desired = !isFollowed
        followJob = viewModelScope.launch(Dispatchers.IO) {
            request(desired).collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    val accepted = response.topicFollowAcknowledged()
                    if (accepted) isFollowed = desired
                    toastText = if (accepted) {
                        AppLanguage.getString(if (desired) R.string.user_followed else R.string.user_unfollowed)
                    } else response.message ?: AppLanguage.getString(R.string.error_follow)
                } else toastText = ApiFailures.safeMessage(result.exceptionOrNull())
            }
        }
    }

    fun blockTopic() {
        viewModelScope.launch(Dispatchers.IO) {
            if (isBlocked)
                blackListRepo.deleteTopic(title)
            else
                blackListRepo.saveTopic(title)
            isBlocked = !isBlocked
        }
    }

    private fun checkIsBlocked(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isBlocked = blackListRepo.checkTopic(title)
        }
    }

}
