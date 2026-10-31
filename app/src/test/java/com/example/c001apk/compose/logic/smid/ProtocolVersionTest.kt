package com.example.c001apk.compose.logic.smid

import androidx.datastore.core.DataStoreFactory
import com.example.c001apk.compose.logic.datastore.*
import java.nio.file.Files
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class ProtocolVersionTest {
    @Test fun bundledDefaultsUpgradeWithoutReplacingExistingIdOrCustomVersions() = runBlocking {
        val old = UserPreferencesCompat.default().copy(versionName = "16.1.3", versionCode = "2604022",
            apiVersion = "16", szlmId = "existing-device-id", uid = "account", token = "secret",
            userAgent = "test +CoolMarket/16.1.3-2604022-universal", blurBars = true)
        val migrated = UserPreferencesSerializer().readFrom(old.toProto().toByteArray().inputStream())
        assertEquals(CoolapkProtocolVersion("16.7.0", "2610291", "16"), migrated.protocolVersion())
        assertEquals(old.szlmId, migrated.szlmId)
        assertEquals(old.uid, migrated.uid); assertEquals(old.token, migrated.token)
        assertTrue(migrated.blurBars); assertTrue(migrated.digitalUnionRefreshPending)
        assertEquals("test +CoolMarket/16.7.0-2610291-universal", migrated.userAgent)
        assertEquals(migrated, migrated.upgradeDefaultProtocol())
        val custom = old.copy(versionName = "16.6.4", versionCode = "2609291")
        assertEquals(custom, custom.upgradeDefaultProtocol())
    }

    @Test fun anExplicitlySelectedLegacyVersionIsNotMigratedBackOnRestart() = runBlocking {
        for (version in listOf(CoolapkProtocolVersion("16.1.3", "2604022", "16"),
            CoolapkProtocolVersion("13.4.1", "2312121", "13"))) {
            val selected = UserPreferencesCompat.default().withProtocolVersion(version)
            assertTrue(selected.protocolVersionCustomized)
            val restored = UserPreferencesSerializer().readFrom(selected.toProto().toByteArray().inputStream())
            assertEquals(selected, restored)
            assertEquals(version, selected.withDigitalUnionId("synthetic-device-id").protocolVersion())
        }
    }

    @Test fun protocolChangesPersistRefreshIntentAndRejectStaleGeneration() = runBlocking {
        val directory = Files.createTempDirectory("protocol-identity").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = DataStoreFactory.create(UserPreferencesSerializer(), scope = scope,
                produceFile = { directory.resolve("prefs.pb") })
            val source = UserPreferencesDataSource(store)
            source.setSZLMId("existing-device-id")
            val before = store.data.first()
            source.setVersionName("16.7.1")
            val changed = store.data.first()
            assertTrue(changed.digitalUnionRefreshPending)
            assertEquals(before.deviceIdentityRevision + 1, changed.deviceIdentityRevision)
            assertEquals(before.szlmId, changed.szlmId)
            assertNull(source.saveGeneratedIdentity("stale-device-id-12345", before))
            assertTrue(UserPreferencesCompat(changed.toProto()).digitalUnionRefreshPending)
            source.setVersionCode("2610301")
            assertEquals("2610301", store.data.first().versionCode)
            source.setApiVersion("17")
            val apiChanged = store.data.first()
            assertEquals("17", apiChanged.apiVersion)
            assertNull(source.saveGeneratedIdentity("stale-device-id-12345", changed))
            assertNotNull(source.saveGeneratedIdentity("new-generated-device-id", apiChanged))
            assertFalse(store.data.first().digitalUnionRefreshPending)
            source.setApiVersion("16")
            val pending = store.data.first()
            source.setSZLMId("user-imported-device-id")
            assertFalse(store.data.first().digitalUnionRefreshPending)
            assertNull(source.saveGeneratedIdentity("late-generated-device-id", pending))
            assertEquals("user-imported-device-id", store.data.first().szlmId)
        } finally { scope.cancel(); directory.deleteRecursively() }
    }

    @Test fun observerIgnoresUnrelatedChangesAndCancelsForManualImport() = runBlocking {
        val state = MutableStateFlow(UserPreferencesCompat.default())
        var calls = 0
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val watch = launch(start = CoroutineStart.UNDISPATCHED) {
            observeProtocolIdentityRefresh(state, refresh = {
                calls++; started.complete(Unit)
                try { awaitCancellation() } finally { cancelled.complete(Unit) }
            }, onFailure = { fail("Cancelled request must not show a failure") }, settleMillis = 0)
        }
        yield()
        state.value = state.value.copy(blurBars = true)
        yield(); assertEquals(0, calls)
        state.value = state.value.withProtocolVersion(state.value.protocolVersion().copy(name = "16.7.1"))
        withTimeout(2_000) { started.await() }
        state.value = state.value.copy(szlmId = "manual-device-id", digitalUnionRefreshPending = false,
            deviceIdentityRevision = state.value.deviceIdentityRevision + 1)
        withTimeout(2_000) { cancelled.await() }
        assertEquals(1, calls)
        watch.cancelAndJoin()
    }

}
