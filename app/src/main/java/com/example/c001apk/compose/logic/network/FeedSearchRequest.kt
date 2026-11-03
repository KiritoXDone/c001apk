package com.example.c001apk.compose.logic.network

import okhttp3.HttpUrl.Companion.toHttpUrl

// 全站动态使用模型 URL；指定范围的搜索必须走 /v6/search。
internal data class FeedSearchScope(val type: String, val parameter: String)

internal fun feedSearchScope(pageType: String?, pageParam: String?): FeedSearchScope? {
    if (pageType.isNullOrBlank() && pageParam.isNullOrBlank()) return null
    require(!pageType.isNullOrBlank() && !pageParam.isNullOrBlank()) { "搜索范围不完整，请重新进入搜索" }
    return FeedSearchScope(pageType, pageParam)
}

internal fun feedSearchUrl(
    keyword: String,
    sort: String,
    isStrict: Boolean,
    feedType: String,
): String {
    val url = "https://api.coolapk.com/SearchModel/feedSearchList".toHttpUrl().newBuilder()
        .addQueryParameter("cacheExpires", "60")
        .addQueryParameter("searchValue", keyword)
        .addQueryParameter("sort", sort)
        .apply {
            if (isStrict) addQueryParameter("isStrict", "1")
            if (feedType != "all") addQueryParameter("feedType", feedType)
        }
        .build()
    return "${url.encodedPath}?${url.encodedQuery}"
}
