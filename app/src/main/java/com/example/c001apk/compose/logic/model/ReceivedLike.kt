package com.example.c001apk.compose.logic.model

import org.jsoup.Jsoup

internal data class ReceivedLike(
    val userId: String?,
    val username: String,
    val avatar: String?,
    val timestamp: Long?,
    val action: String,
    val author: String,
    val summary: String,
    val thumbnail: String?,
    val url: String?,
    val copyText: String?,
    val isFeed: Boolean,
    val hasUsername: Boolean,
    val hasSummary: Boolean,
)

/** Like-list rows contain the original feed/reply plus a separate liking user. */
internal fun HomeFeedResponse.Data.receivedLike(): ReceivedLike? {
    if (entityType !in setOf("feed", "feed_reply")) return null
    if (likeUserInfo == null && likeUid.isNullOrBlank()) return null
    val isFeed = entityType == "feed"
    val content = if (isFeed) messageTitle.nonBlank() ?: message else message
    return ReceivedLike(
        userId = likeUserInfo?.uid.nonBlank() ?: likeUid.nonBlank(),
        username = plainText(likeUserInfo?.username.nonBlank() ?: likeUsername).ifBlank { "用户" },
        avatar = likeUserInfo?.userAvatar.nonBlank() ?: likeAvatar.nonBlank(),
        timestamp = likeTime?.takeIf { it > 0 },
        action = if (isFeed) "赞了你的帖子" else "赞了你的回复",
        author = plainText(username.nonBlank() ?: userInfo?.username),
        summary = plainText(content).ifBlank { if (isFeed) "查看帖子" else "查看回复" },
        thumbnail = pic.nonBlank() ?: picArr?.firstOrNull { it.isNotBlank() },
        url = url.nonBlank() ?: if (isFeed) id.nonBlank()?.let { "/feed/$it" } else null,
        copyText = message,
        isFeed = isFeed,
        hasUsername = plainText(likeUserInfo?.username.nonBlank() ?: likeUsername).isNotBlank(),
        hasSummary = plainText(content).isNotBlank(),
    )
}

private fun String?.nonBlank() = this?.takeIf { it.isNotBlank() }
private fun plainText(value: String?) = Jsoup.parseBodyFragment(value.orEmpty()).text()
