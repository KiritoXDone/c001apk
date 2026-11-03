package com.example.c001apk.compose.logic.network

import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.search.SearchOrderType
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FeedSearchRequestTest {
    private val requests = CopyOnWriteArrayList<Request>()
    private var failRequest = false
    private val api = Retrofit.Builder()
        .baseUrl("https://api.coolapk.com/")
        .client(OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            requests.add(request)
            if (failRequest) throw IOException("offline")
            val type = request.url.queryParameter("feedType").orEmpty()
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"data":[{"id":"123","entityId":"123","entityType":"feed","feedType":"$type"}]}"""
                    .toResponseBody("application/json".toMediaType()))
                .build()
        }.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
    private val repository = NetworkRepo(api, api, api, api)

    @Test
    fun globalFeedTypesAndOrdersUseTheOfficialSearchEndpoint() = runBlocking {
        val expected = listOf("综合" to "none", "实时" to "dateline", "热度" to "hot", "回复" to "reply", "精确" to "none")
        assertEquals(expected, SearchOrderType.entries.map { it.title to it.sort })
        val types = listOf("all", "feed", "feedArticle", "picture", "comment", "rating", "answer", "question", "vote")
        for (type in types) for (order in SearchOrderType.entries) {
            val state = repository.getFeedSearch("测试", type, order.sort, order.isStrict, null, null, 1, null).first()
            assertTrue(state is LoadingState.Success)
            assertEquals(type, (state as LoadingState.Success).response.single().feedType)
            val url = requests.last().url
            assertEquals("/v6/search", url.encodedPath)
            assertEquals("feed", url.queryParameter("type"))
            assertEquals(type, url.queryParameter("feedType"))
            assertEquals("测试", url.queryParameter("searchValue"))
            assertEquals(order.sort, url.queryParameter("sort"))
            assertEquals(if (order.isStrict) "1" else "0", url.queryParameter("isStrict"))
            assertEquals("1", url.queryParameter("page"))
            assertEquals("-1", url.queryParameter("showAnonymous"))
            assertNull(url.queryParameter("pageType"))
            assertNull(url.queryParameter("pageParam"))
            assertNull(url.queryParameter("lastItem"))
        }
    }

    @Test
    fun scopedSearchPreservesSpecialCharactersAndPagination() = runBlocking {
        val keyword = "C++ 手机 & 平板 #100%/测试"
        val scopes = listOf("tag" to "薅羊毛+分队", "user" to "123&456", "product_phone" to "100")
        for ((type, parameter) in scopes) {
            val state = repository.getFeedSearch(keyword, "picture", "hot", true, type, parameter, 2, "feed:123").first()
            assertTrue(state is LoadingState.Success)
            val url = requests.last().url
            assertEquals("/v6/search", url.encodedPath)
            assertEquals(keyword, url.queryParameter("searchValue"))
            assertEquals("picture", url.queryParameter("feedType"))
            assertEquals("hot", url.queryParameter("sort"))
            assertEquals("1", url.queryParameter("isStrict"))
            assertEquals(type, url.queryParameter("pageType"))
            assertEquals(parameter, url.queryParameter("pageParam"))
            assertEquals("2", url.queryParameter("page"))
            assertEquals("feed:123", url.queryParameter("lastItem"))
            assertNull(url.fragment)
        }
    }

    @Test
    fun incompleteScopeFailsWithoutSendingAGlobalRequest() = runBlocking {
        assertNull(feedSearchScope(null, null))
        assertNull(feedSearchScope("", ""))
        assertThrows(IllegalArgumentException::class.java) { feedSearchScope("tag", null) }
        assertThrows(IllegalArgumentException::class.java) { feedSearchScope(null, "THANOX") }
        val state = repository.getFeedSearch("测试", "all", "none", false, "tag", null, 1, null).first()
        assertTrue(state is LoadingState.Error)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun searchFailureStaysAnErrorAndOtherSearchTypesKeepTheirContract() = runBlocking {
        failRequest = true
        val state = repository.getFeedSearch("测试", "picture", "none", false, null, null, 1, null).first()
        assertTrue(state is LoadingState.Error)
        val apk = api.getSearch("apk", "all", "default", "测试", null, null, 1, null).request().url
        assertEquals("/v6/search", apk.encodedPath)
        assertEquals("apk", apk.queryParameter("type"))
        assertNull(apk.queryParameter("isStrict"))
    }
}
