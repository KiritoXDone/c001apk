package com.example.c001apk.compose.ui.feed

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class GuestReplyGate(
    private val scope: CoroutineScope,
    private val hasIdentity: Flow<Boolean>,
    private val awaitPublishedIdentity: suspend () -> Boolean,
) {
    private var pending: Job? = null

    @Synchronized
    fun start(isGuest: Boolean, isCurrent: () -> Boolean, onWaiting: () -> Unit, onReady: () -> Unit) {
        pending?.cancel()
        pending = scope.launch {
            try {
                if (!isCurrent()) return@launch
                if (isGuest) {
                    val ready = hasIdentity.first() && awaitPublishedIdentity()
                    if (!isCurrent()) return@launch
                    if (!ready) {
                        onWaiting()
                        hasIdentity.first { it && awaitPublishedIdentity() }
                    }
                }
                if (isCurrent()) onReady()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                if (isCurrent()) onWaiting()
            }
        }
    }

    @Synchronized
    fun cancel() {
        pending?.cancel()
        pending = null
    }
}
