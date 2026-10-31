package com.example.c001apk.compose.ui.home.feed

import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.LoadingState
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CoolPicPageLoaderTest {
    private val home = "/page?url=V11_FIND_COOLPIC"
    private val feed = "/feed/coolPictureList?dataListType=staggered_new"
    private fun row(json: String) = Gson().fromJson(json, HomeFeedResponse.Data::class.java)
    private val header = row("""{"entityId":"header","entityType":"card","entityTemplate":"imageSquareScrollCard"}""")
    private val post = row("""{"entityId":"123","id":"123","entityType":"feed","feedType":"picture"}""")
    private fun config(url: String = feed) = row("""{"entityType":"card","entityTemplate":"configCard","extraDataArr":{"url":"$url","pageTitle":"酷图"}}""")
    private fun success(vararg rows: HomeFeedResponse.Data) = LoadingState.Success(rows.toList())
    private data class Request(val url: String, val page: Int, val last: String?)

    @Test fun firstPageCombinesHeadersAndPostsThenPaginatesOnlyTheFeed() = runBlocking {
        val requests = mutableListOf<Request>()
        val loader = CoolPicPageLoader(home, "酷图") { url, _, page, last ->
            requests += Request(url, page, last)
            if (url == home) success(config(), header) else success(post)
        }
        assertEquals(listOf(header, post), (loader.load(1, null) as LoadingState.Success).response)
        assertEquals(listOf(post), (loader.load(2, "123") as LoadingState.Success).response)
        assertEquals(listOf(Request(home, 1, null), Request(feed, 1, null), Request(feed, 2, "123")), requests)
    }

    @Test fun refreshRereadsConfigAndResetsFeedCursor() = runBlocking {
        var target = feed
        val requests = mutableListOf<Request>()
        val loader = CoolPicPageLoader(home, "酷图") { url, _, page, last ->
            requests += Request(url, page, last)
            if (url == home) success(config(target), header) else success(post)
        }
        loader.load(1, null)
        target = "$feed&type=newest"
        loader.load(1, "old-cursor")
        loader.load(2, "123")
        assertEquals(listOf(Request(home, 1, null), Request(target, 1, null), Request(target, 2, "123")), requests.takeLast(3))
    }

    @Test fun legacyInlineFeedsRemainSupportedAfterRefresh() = runBlocking {
        var legacy = false
        val requests = mutableListOf<String>()
        val loader = CoolPicPageLoader(home, "酷图") { url, _, _, _ ->
            requests += url
            if (url == home && !legacy) success(config(), header) else success(header, post)
        }
        loader.load(1, null)
        legacy = true
        assertEquals(listOf(header, post), (loader.load(1, null) as LoadingState.Success).response)
        loader.load(2, "123")
        assertEquals(listOf(home, home), requests.takeLast(2))
    }

    @Test fun failedFeedDoesNotBecomeAHeadersOnlySuccessAndCanRetry() = runBlocking {
        var failure = true
        val loader = CoolPicPageLoader(home, "酷图") { url, _, _, _ ->
            if (url == home) success(config(), header)
            else if (failure) LoadingState.Error("offline") else success(post)
        }
        assertEquals(LoadingState.Error("offline"), loader.load(1, null))
        failure = false
        assertEquals(listOf(header, post), (loader.load(1, null) as LoadingState.Success).response)
    }

    @Test fun emptyFeedKeepsHeadersAndSignalsTheEndUntilRefresh() = runBlocking {
        var empty = true
        val loader = CoolPicPageLoader(home, "酷图") { url, _, _, _ ->
            if (url == home) success(config(), header)
            else if (empty) LoadingState.Empty else success(post)
        }
        assertEquals(listOf(header), (loader.load(1, null) as LoadingState.Success).response)
        assertTrue(loader.emptyFeed)
        empty = false
        loader.load(1, null)
        assertFalse(loader.emptyFeed)
    }

    @Test fun malformedConfigIsRetryableInsteadOfSilentlyHidingPosts() = runBlocking {
        val loader = CoolPicPageLoader(home, "酷图") { _, _, _, _ -> success(config(""), header) }
        assertTrue(loader.load(1, null) is LoadingState.Error)
    }

    @Test fun cancellationPropagatesWithoutChangingTheCommittedFeedTarget() = runBlocking {
        var cancel = false
        val requests = mutableListOf<String>()
        val loader = CoolPicPageLoader(home, "酷图") { url, _, _, _ ->
            requests += url
            if (url == home) success(config(if (cancel) "/other-feed" else feed), header)
            else if (url == "/other-feed") throw CancellationException()
            else success(post)
        }
        loader.load(1, null)
        cancel = true
        try { loader.load(1, null); fail("Expected cancellation") } catch (_: CancellationException) { }
        loader.load(2, "123")
        assertEquals(feed, requests.last())
    }
}
