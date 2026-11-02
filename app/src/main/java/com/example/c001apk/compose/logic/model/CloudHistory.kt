package com.example.c001apk.compose.logic.model

// id 是不可拆分的分页游标；dateline 为服务端记录的浏览时间。
data class CloudHistoryEntry(
    val id: String?,
    val title: String?,
    val description: String?,
    val logo: String?,
    val url: String?,
    val historyType: String?,
    val typeName: String?,
    val dateline: Long?,
)

data class CloudHistoryResponse(
    val data: List<CloudHistoryEntry>?,
    val status: Int?,
    val error: Int?,
    val message: String?,
)
