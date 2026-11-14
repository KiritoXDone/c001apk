package com.example.c001apk.compose.logic.smid

import androidx.datastore.core.DataStore
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.datastore.UserPreferencesDataSource
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.util.CookieUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIdentityPublicationTest {
    @Test
    fun synchronizationWaitsForIdentityWriteAndPublication() = runBlocking {
        val previous = listOf(
            CookieUtil.szlmId, CookieUtil.xAppDevice, CookieUtil.userAgent,
            CookieUtil.versionName, CookieUtil.versionCode, CookieUtil.apiVersion, CookieUtil.sdkInt,
        )
        val store = DelayedIdentityStore(
            UserPreferencesCompat.default().copy(
                manufacturer = "maker", brand = "brand", model = "model", buildNumber = "build",
                androidVersion = "16", sdkInt = "36", userAgent = "test-agent",
            ),
        )
        val preferences = UserPreferencesRepository(UserPreferencesDataSource(store))
        val writing = async(Dispatchers.IO) { preferences.setSZLMId("synthetic-id") }
        var synchronizing: Deferred<Boolean>? = null
        try {
            store.updated.await()
            val syncing = async(Dispatchers.IO) { preferences.synchronizeDeviceIdentity() }
            synchronizing = syncing
            assertNull(withTimeoutOrNull(100) { syncing.await() })
            store.release.complete(Unit)
            writing.await()
            assertTrue(syncing.await())
            assertEquals("synthetic-id", CookieUtil.szlmId)
            assertTrue(hasRequestDeviceIdentity(CookieUtil.xAppDevice))
        } finally {
            store.release.complete(Unit)
            writing.cancelAndJoin()
            synchronizing?.cancelAndJoin()
            CookieUtil.szlmId = previous[0]
            CookieUtil.xAppDevice = previous[1]
            CookieUtil.userAgent = previous[2]
            CookieUtil.versionName = previous[3]
            CookieUtil.versionCode = previous[4]
            CookieUtil.apiVersion = previous[5]
            CookieUtil.sdkInt = previous[6]
        }
    }
}

private class DelayedIdentityStore(initial: UserPreferencesCompat) : DataStore<UserPreferencesCompat> {
    private val state = MutableStateFlow(initial)
    val updated = CompletableDeferred<Unit>()
    val release = CompletableDeferred<Unit>()
    override val data: Flow<UserPreferencesCompat> = state

    override suspend fun updateData(transform: suspend (t: UserPreferencesCompat) -> UserPreferencesCompat): UserPreferencesCompat {
        val value = transform(state.value)
        state.value = value
        updated.complete(Unit)
        release.await()
        return value
    }
}
