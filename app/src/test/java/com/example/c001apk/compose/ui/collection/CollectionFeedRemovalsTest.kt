package com.example.c001apk.compose.ui.collection

import com.example.c001apk.compose.logic.model.CollectionSaveResult
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class CollectionFeedRemovalsTest {
    private fun row(id: String, type: String = "feed") = Gson().fromJson(
        """{"id":"$id","entityType":"$type"}""", HomeFeedResponse.Data::class.java)

    @Test fun eitherConfirmedCollectionStateRemovesOnlyTheRequestedFeed() = runBlocking {
        for (collect in listOf(0, 1)) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            val removed = mutableListOf<String>()
            val notices = mutableListOf<String>()
            try {
                val actions = CollectionFeedRemovals(scope, { true }, { CollectionSaveResult(collect, null) }, removed::add, notices::add)
                actions.removeFeed("1")
                assertEquals(listOf("1"), removed)
                assertEquals(listOf("2", "1"), actions.filter(listOf(row("1"), row("2"), row("1", "card"))).map { it.id })
                assertEquals(1, notices.size)
                actions.resetForRefresh()
                assertEquals(1, actions.filter(listOf(row("1"))).size)
            } finally { scope.cancel() }
        }
    }

    @Test fun duplicatesAreIgnoredAndAccountChangesDiscardTheResult() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val result = CompletableDeferred<CollectionSaveResult>()
        var owner = true
        var requests = 0
        var callbacks = 0
        val notices = mutableListOf<String>()
        try {
            val actions = CollectionFeedRemovals(scope, { owner }, { requests++; result.await() }, { callbacks++ }, notices::add)
            actions.removeFeed("1"); actions.removeFeed("1")
            assertEquals(1, requests)
            assertEquals(setOf("1"), actions.pending)
            owner = false
            result.complete(CollectionSaveResult(0, null))
            assertEquals(0, callbacks)
            assertTrue(notices.isEmpty())
            assertTrue(actions.pending.isEmpty())
            actions.removeFeed("2")
            assertEquals(1, requests)
        } finally { scope.cancel() }
    }

    @Test fun failuresAndUnconfirmedResponsesKeepTheFeedAndAllowRetry() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var attempt = 0
        var callbacks = 0
        val notices = mutableListOf<String>()
        try {
            val actions = CollectionFeedRemovals(scope, { true }, {
                when (++attempt) {
                    1 -> throw java.io.IOException("offline")
                    2 -> CollectionSaveResult(null, null)
                    else -> CollectionSaveResult(1, null)
                }
            }, { callbacks++ }, notices::add)
            repeat(2) {
                actions.removeFeed("1")
                assertEquals(1, actions.filter(listOf(row("1"))).size)
                assertTrue(actions.pending.isEmpty())
                assertEquals(0, callbacks)
            }
            actions.removeFeed("1")
            assertEquals(1, callbacks)
            assertTrue(actions.filter(listOf(row("1"))).isEmpty())
            assertEquals(3, notices.size)
        } finally { scope.cancel() }
    }
}
