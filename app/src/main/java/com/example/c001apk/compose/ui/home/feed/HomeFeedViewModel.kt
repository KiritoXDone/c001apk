package com.example.c001apk.compose.ui.home.feed

import com.example.c001apk.compose.logic.filter.ContentFilterStore
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.base.BaseViewModel
import com.example.c001apk.compose.ui.home.TabType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import com.example.c001apk.compose.logic.state.LoadingState

/** Created by bggRGjQaUbCoE on 2024/6/3 */
@HiltViewModel(assistedFactory = HomeFeedViewModel.ViewModelFactory::class)
class HomeFeedViewModel
@AssistedInject
constructor(
    @Assisted val type: TabType = TabType.FEED,
    @Assisted("dataListUrl") var dataListUrl: String,
    @Assisted("dataListTitle") var dataListTitle: String,
    @Assisted("installTime") var installTime: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    private val coolPicPages = CoolPicPageLoader(dataListUrl, dataListTitle) { url, title, page, last ->
        networkRepo.getDataList(url, title, null, last, page).first { it !is LoadingState.Loading }
    }

    @AssistedFactory
    interface ViewModelFactory {
        fun create(
            type: TabType,
            @Assisted("dataListUrl") dataListUrl: String,
            @Assisted("dataListTitle") dataListTitle: String,
            @Assisted("installTime") installTime: String,
        ): HomeFeedViewModel
    }

    init {
        if (installTime.isEmpty()) {
            installTime = System.currentTimeMillis().toString()
            setInstallTime()
        }
        fetchData()
    }

    override suspend fun customFetchData() =
        when (type) {
            TabType.FOLLOW,
            TabType.HOT ->
                networkRepo.getDataList(dataListUrl, dataListTitle, null, lastItem, page)

            TabType.COOLPIC -> flow { emit(coolPicPages.load(page, lastItem)) }

            TabType.FEED ->
                networkRepo.getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem)

            else -> throw IllegalArgumentException("invalid type: ${type.name}")
        }

    override fun refresh() {
        firstLaunch = 0
        super.refresh()
    }

    // Home visibility is applied to the displayed list, not to the fetched data.
    override fun isAdditionalItemSupported(item: HomeFeedResponse.Data): Boolean =
        type == TabType.FEED && item.homeFeedHeader() != null

    override fun handleResponse(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        firstLaunch = 0
        if (type == TabType.COOLPIC && coolPicPages.emptyFeed) isEnd = true
        return null
    }

    private fun setInstallTime() {
        viewModelScope.launch {
            userPreferencesRepository.setInstallTime(installTime)
        }
    }

    override fun handleLoadMore(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }
}
