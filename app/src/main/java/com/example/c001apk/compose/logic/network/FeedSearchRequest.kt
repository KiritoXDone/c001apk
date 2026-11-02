package com.example.c001apk.compose.logic.network

import okhttp3.HttpUrl.Companion.toHttpUrl

/** The page API receives an encoded model URL as its `url` query parameter. */
internal fun feedSearchUrl(
    keyword: String,
    sort: String,
    isStrict: Boolean,
    feedType: String,
    pageType: String?,
    pageParam: String?,
): String {
    val url = "https://api.coolapk.com/SearchModel/feedSearchList".toHttpUrl().newBuilder()
        .addQueryParameter("cacheExpires", "60")
        .addQueryParameter("searchValue", keyword)
        .addQueryParameter("sort", sort)
        .apply {
            if (isStrict) addQueryParameter("isStrict", "1")
            if (feedType != "all") addQueryParameter("feedType", feedType)
            pageType?.takeIf(String::isNotBlank)?.let { addQueryParameter("pageType", it) }
            pageParam?.takeIf(String::isNotBlank)?.let { addQueryParameter("pageParam", it) }
        }
        .build()
    return "${url.encodedPath}?${url.encodedQuery}"
}
