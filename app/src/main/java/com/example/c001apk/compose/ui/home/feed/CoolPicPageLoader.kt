package com.example.c001apk.compose.ui.home.feed

import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.LoadingState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** The channel supplies headers; newer protocol versions put its feed URL in a config card. */
internal class CoolPicPageLoader(
    private val homeUrl: String,
    private val homeTitle: String,
    private val fetch: suspend (String, String, Int, String?) -> LoadingState<List<HomeFeedResponse.Data>>,
) {
    private var feedUrl: String? = null
    private var feedTitle = homeTitle
    var emptyFeed = false
        private set

    suspend fun load(page: Int, lastItem: String?): LoadingState<List<HomeFeedResponse.Data>> {
        if (page > 1) return fetch(feedUrl ?: homeUrl, feedTitle, page, lastItem)

        val home = fetch(homeUrl, homeTitle, 1, null)
        if (home !is LoadingState.Success) return home
        val config = home.response.firstOrNull {
            it.entityType == "card" && it.entityTemplate == "configCard"
        }
        if (config == null) {
            currentCoroutineContext().ensureActive()
            feedUrl = null
            feedTitle = homeTitle
            emptyFeed = false
            return home
        }
        val url = config.extraDataArr?.url?.takeIf { it.isNotBlank() }
            ?: return LoadingState.Error("酷图动态配置无效，请刷新重试")
        val title = config.extraDataArr.pageTitle?.takeIf { it.isNotBlank() } ?: homeTitle
        val feed = fetch(url, title, 1, null)
        val posts = when (feed) {
            is LoadingState.Success -> feed.response
            LoadingState.Empty -> emptyList()
            else -> return feed
        }
        currentCoroutineContext().ensureActive()
        feedUrl = url
        feedTitle = title
        emptyFeed = posts.isEmpty()
        val headers = home.response.filterNot { it.entityTemplate == "configCard" }
        return LoadingState.Success(headers + posts, home.isPublic)
    }
}
