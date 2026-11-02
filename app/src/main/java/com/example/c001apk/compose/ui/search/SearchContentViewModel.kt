package com.example.c001apk.compose.ui.search

import com.example.c001apk.compose.logic.filter.ContentFilterStore
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.ui.base.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@HiltViewModel(assistedFactory = SearchContentViewModel.ViewModelFactory::class)
class SearchContentViewModel @AssistedInject constructor(
    @Assisted("type") val type: String,
    @Assisted("keyword") val keyword: String,
    @Assisted("pageType") val pageType: String?,
    @Assisted("pageParam") var pageParam: String?,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(
            @Assisted("type") type: String,
            @Assisted("keyword") keyword: String,
            @Assisted("pageType") pageType: String?,
            @Assisted("pageParam") pageParam: String?,
        ): SearchContentViewModel
    }

    private var feedType = "all"
    private var searchFeedType = SearchFeedType.ALL
    private var sortType = SearchOrderType.COMPREHENSIVE

    init {
        fetchData()
    }

    fun updateFilters(feed: SearchFeedType, order: SearchOrderType) {
        if (feed == searchFeedType && order == sortType) return
        searchFeedType = feed
        sortType = order
        feedType = when (feed) {
            SearchFeedType.ALL -> "all"
            SearchFeedType.FEED -> "feed"
            SearchFeedType.ARTICLE -> "feedArticle"
            SearchFeedType.COOLPIC -> "picture"
            SearchFeedType.COMMENT -> "comment"
            SearchFeedType.RATING -> "rating"
            SearchFeedType.ANSWER -> "answer"
            SearchFeedType.QUESTION -> "question"
            SearchFeedType.VOTE -> "vote"
        }
        // A filter change must also replace an in-flight refresh, not only pagination.
        cancelFetch()
        isRefreshing = false
        refresh()
    }

    override suspend fun customFetchData() =
        if (type == "feed") {
            networkRepo.getFeedSearch(
                keyword, feedType, sortType.sort, sortType.isStrict, sortType.title,
                pageType, pageParam, page, lastItem,
            )
        } else {
            networkRepo.getSearch(type, feedType, "default", keyword, pageType, pageParam, page, lastItem)
        }

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}
