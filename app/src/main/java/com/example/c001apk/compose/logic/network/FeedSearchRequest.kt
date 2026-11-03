package com.example.c001apk.compose.logic.network

// 搜索范围必须成对提供，避免意外扩大为全站搜索。
internal data class FeedSearchScope(val type: String, val parameter: String)

internal fun feedSearchScope(pageType: String?, pageParam: String?): FeedSearchScope? {
    if (pageType.isNullOrBlank() && pageParam.isNullOrBlank()) return null
    require(!pageType.isNullOrBlank() && !pageParam.isNullOrBlank()) { "搜索范围不完整，请重新进入搜索" }
    return FeedSearchScope(pageType, pageParam)
}
