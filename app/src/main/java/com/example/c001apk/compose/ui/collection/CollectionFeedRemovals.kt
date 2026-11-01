package com.example.c001apk.compose.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.c001apk.compose.logic.model.CollectionSaveResult
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.network.ApiFailures
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class CollectionFeedRemovals(
    private val scope: CoroutineScope,
    private val isCurrentOwner: () -> Boolean,
    private val remove: suspend (String) -> CollectionSaveResult,
    private val onRemoved: (String) -> Unit,
    private val notify: (String) -> Unit,
) {
    var pending by mutableStateOf<Set<String>>(emptySet())
        private set
    private var removed by mutableStateOf<Set<String>>(emptySet())

    fun removeFeed(id: String) {
        if (id.isBlank() || id in pending || id in removed || !isCurrentOwner()) return
        pending = pending + id
        scope.launch {
            try {
                val result = remove(id)
                // collect=1 is valid when the post remains in another collection.
                check(result.collect == 0 || result.collect == 1) { "未收到收藏状态，请重试" }
                if (!isCurrentOwner()) return@launch
                removed = removed + id
                onRemoved(id)
                notify("已从当前收藏夹取消收藏")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isCurrentOwner()) notify(ApiFailures.safeMessage(e))
            } finally {
                pending = pending - id
            }
        }
    }

    fun filter(rows: List<HomeFeedResponse.Data>) =
        rows.filterNot { it.entityType == "feed" && it.id in removed }

    fun resetForRefresh() {
        if (pending.isEmpty()) removed = emptySet()
    }
}
