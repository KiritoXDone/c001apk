package com.example.c001apk.compose.ui.feed

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class GuestReplyGateTest {
    @Test
    fun waitsForIdentityAndPublicationBeforeLoading() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val hasIdentity = MutableStateFlow(false)
            val published = CompletableDeferred<Boolean>()
            val events = mutableListOf<String>()
            val gate = GuestReplyGate(scope, hasIdentity) { published.await() }
            gate.start(isGuest = true, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            assertEquals(listOf("waiting"), events)
            hasIdentity.value = true
            yield()
            assertEquals(listOf("waiting"), events)
            published.complete(true)
            yield()
            assertEquals(listOf("waiting", "load"), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun explicitRetryCancelsDeferredLoad() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val hasIdentity = MutableStateFlow(false)
            val events = mutableListOf<String>()
            val gate = GuestReplyGate(scope, hasIdentity) { true }
            gate.start(isGuest = true, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            gate.cancel()
            hasIdentity.value = true
            yield()
            assertEquals(listOf("waiting"), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun existingIdentityLoadsWithoutWaiting() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val events = mutableListOf<String>()
            val gate = GuestReplyGate(scope, MutableStateFlow(true)) { true }
            gate.start(isGuest = true, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            assertEquals(listOf("load"), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun signedInUserDoesNotWaitForGuestIdentity() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val events = mutableListOf<String>()
            val gate = GuestReplyGate(scope, MutableStateFlow(false)) { error("Must not inspect guest identity") }
            gate.start(isGuest = false, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            assertEquals(listOf("load"), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun explicitRetryCancelsAnInFlightInitialSynchronization() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val release = CompletableDeferred<Boolean>()
            val events = mutableListOf<String>()
            val gate = GuestReplyGate(scope, MutableStateFlow(true)) { release.await() }
            gate.start(isGuest = true, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            gate.cancel()
            release.complete(true)
            yield()
            assertEquals(emptyList<String>(), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun outdatedDestinationDoesNotLoadAfterIdentityAppears() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val hasIdentity = MutableStateFlow(false)
            val events = mutableListOf<String>()
            var current = true
            val gate = GuestReplyGate(scope, hasIdentity) { true }
            gate.start(isGuest = true, isCurrent = { current }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            current = false
            hasIdentity.value = true
            yield()
            assertEquals(listOf("waiting"), events)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun waitsAgainIfIdentityIsClearedBeforePublication() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val hasIdentity = MutableStateFlow(false)
            val events = mutableListOf<String>()
            var attempts = 0
            val gate = GuestReplyGate(scope, hasIdentity) { ++attempts > 1 }
            gate.start(isGuest = true, isCurrent = { true }, onWaiting = { events.add("waiting") }, onReady = { events.add("load") })
            yield()
            hasIdentity.value = true
            yield()
            assertEquals(listOf("waiting"), events)
            hasIdentity.value = false
            yield()
            hasIdentity.value = true
            yield()
            assertEquals(listOf("waiting", "load"), events)
        } finally {
            scope.cancel()
        }
    }
}
