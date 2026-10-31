package com.example.c001apk.compose.ui

import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.smid.withDigitalUnionId
import org.junit.Assert.*
import org.junit.Test

class DigitalUnionIdentityTest {
    @Test
    fun applyingIdMigratesProtocolDefaultsAndPreservesThemeAndAccount() {
        val original =
            UserPreferencesCompat.default()
                .copy(
                    versionName = "13.4.1",
                    versionCode = "2312121",
                    apiVersion = "13",
                    uid = "test-user",
                    token = "test-token",
                    pureBlack = true,
                )
        val updated = original.withDigitalUnionId("test-device-id-123456")
        assertEquals("test-device-id-123456", updated.szlmId)
        assertEquals("16.1.3", updated.versionName)
        assertEquals("2604022", updated.versionCode)
        assertTrue(updated.xAppDevice.isNotBlank())
        assertTrue(updated.userAgent.contains("CoolMarket/16.1.3-2604022"))
        assertEquals(original.uid, updated.uid)
        assertEquals(original.token, updated.token)
        assertTrue(updated.pureBlack)
    }
}
