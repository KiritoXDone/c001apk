package com.example.c001apk.compose.logic.network

import com.example.c001apk.compose.ui.search.SearchOrderType
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FeedSearchRequestTest {
    @Test
    fun fiveOrdersMatchCapturedModelParameters() {
        val expected = listOf("综合" to "none", "实时" to "dateline", "热度" to "hot", "回复" to "reply", "精确" to "none")
        assertEquals(expected, SearchOrderType.entries.map { it.title to it.sort })
        for (order in SearchOrderType.entries) {
            val url = ("https://api.coolapk.com" + feedSearchUrl("测试", order.sort, order.isStrict, "all", null, null)).toHttpUrl()
            assertEquals("/SearchModel/feedSearchList", url.encodedPath)
            assertEquals("60", url.queryParameter("cacheExpires"))
            assertEquals(order.sort, url.queryParameter("sort"))
            assertEquals(if (order == SearchOrderType.EXACT) "1" else null, url.queryParameter("isStrict"))
            assertNull(url.queryParameter("feedType"))
        }
    }

    @Test
    fun nestedQueryPreservesSpecialCharactersAndExistingFilters() {
        val keyword = "C++ 手机 & 平板 #100%/测试"
        val nested = feedSearchUrl(keyword, "hot", false, "feedArticle", "user", "123&456")
        val outer = "https://api.coolapk.com/v6/page/dataList".toHttpUrl().newBuilder()
            .addQueryParameter("url", nested).addQueryParameter("page", "2")
            .addQueryParameter("lastItem", "feed:123").build()
        val inner = ("https://api.coolapk.com" + outer.queryParameter("url")).toHttpUrl()
        assertEquals(keyword, inner.queryParameter("searchValue"))
        assertEquals("feedArticle", inner.queryParameter("feedType"))
        assertEquals("user", inner.queryParameter("pageType"))
        assertEquals("123&456", inner.queryParameter("pageParam"))
        assertEquals("feed:123", outer.queryParameter("lastItem"))
        assertNull(inner.fragment)
    }

    @Test
    fun ordinarySearchDoesNotRetainStrictMatching() {
        feedSearchUrl("keyword", "none", true, "all", null, null)
        val regular = feedSearchUrl("keyword", "none", false, "all", "", "")
        assertFalse(regular.contains("isStrict"))
        assertFalse(regular.contains("pageType"))
        assertFalse(regular.contains("pageParam"))
    }
}
