package com.example.c001apk.compose.logic.follow

import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.state.LoadingState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** TG-Twilight's per-user merge semantics, implemented for native coroutines (AGPL-3.0). */
class LocalFollowPager(
    uids: List<String>,
    private val fetch: suspend (String, Int, String?) -> LoadingState<List<HomeFeedResponse.Data>>,
) {
    private class Cursor(val uid: String) {
        var page = 1
        var last: String? = null
        var ended = false
        val buffer = mutableListOf<HomeFeedResponse.Data>()
        val seen = mutableSetOf<String>()
    }

    private val cursors = uids.distinct().map(::Cursor)
    private val seen = mutableSetOf<String>()
    var warning: String? = null
        private set

    var failedUsers = 0
        private set

    val isEnd
        get() = cursors.all { it.ended && it.buffer.isEmpty() }

    suspend fun next(limit: Int = 20): List<HomeFeedResponse.Data> = coroutineScope {
        val output = mutableListOf<HomeFeedResponse.Data>()
        val skipped = mutableSetOf<Cursor>()
        var failures = 0
        var deferred = 0
        warning = null
        while (output.size < limit) {
            val pending = cursors.filter { !it.ended && it.buffer.isEmpty() && it !in skipped }
            for (batch in pending.chunked(4)) {
                val results =
                    batch
                        .map { cursor ->
                            async {
                                repeat(3) {
                                    when (val state = fetch(cursor.uid, cursor.page, cursor.last)) {
                                        is LoadingState.Error -> return@async cursor to 1
                                        LoadingState.Empty -> {
                                            cursor.ended = true
                                            return@async cursor to 0
                                        }
                                        is LoadingState.Success -> {
                                            val items = state.response
                                            cursor.ended = items.any {
                                                it.entityTemplate == "noMoreDataCard"
                                            }
                                            val feeds = items.filter {
                                                it.entityType == "feed" &&
                                                    !it.id.isNullOrBlank() &&
                                                    it.uid == cursor.uid
                                            }
                                            cursor.last = items.lastOrNull { !it.id.isNullOrBlank() }?.id ?: cursor.last
                                            cursor.page++
                                            cursor.buffer.addAll(
                                                feeds.filter { cursor.seen.add(it.id!!) },
                                            )
                                            cursor.buffer.sortByDescending { it.dateline ?: 0L }
                                            if (cursor.buffer.isNotEmpty() || cursor.ended)
                                                return@async cursor to 0
                                        }
                                        else -> return@async cursor to 1
                                    }
                                }
                                cursor to 2
                            }
                        }
                        .awaitAll()
                for ((cursor, result) in results) when (result) {
                    1 -> {
                        failures++
                        skipped.add(cursor)
                    }
                    2 -> {
                        deferred++
                        skipped.add(cursor)
                    }
                }
            }
            val cursor =
                cursors
                    .filter { it.buffer.isNotEmpty() }
                    .maxByOrNull { it.buffer.first().dateline ?: 0L } ?: break
            val item = cursor.buffer.removeAt(0)
            if (seen.add(item.id!!)) output.add(item)
        }
        failedUsers = failures
        warning =
            when {
                failures > 0 -> "$failures user feeds could not be loaded"
                deferred > 0 -> "Some content was filtered"
                else -> null
            }
        output
    }
}
