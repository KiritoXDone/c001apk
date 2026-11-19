package com.example.c001apk.compose.logic.model

data class VoteResponse(
    val data: HomeFeedResponse.Vote?,
    val error: Int?,
    val message: String?,
)
