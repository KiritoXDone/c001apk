package com.example.c001apk.compose.logic.datastore

import com.example.c001apk.compose.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeContentPreferencesTest {
    @Test
    fun defaultSearchOrderPersistsAndOldPreferencesKeepComprehensive() {
        assertEquals("COMPREHENSIVE", UserPreferencesCompat(UserPreferences.getDefaultInstance()).defaultSearchOrder)
        for (type in listOf("COMPREHENSIVE", "DATELINE", "HOT", "REPLY", "EXACT")) {
            val bytes = UserPreferencesCompat.default().copy(defaultSearchOrder = type)
                .copy(fontScale = 1.2f).toProto().toByteArray()
            assertEquals(type, UserPreferencesCompat(UserPreferences.parseFrom(bytes)).defaultSearchOrder)
        }
    }

    @Test
    fun oldPreferencesKeepBothExistingSectionsAndTheSquareChoice() {
        for (square in listOf(false, true)) {
            // These bytes contain the old field 10, without the new fields 58 and 59.
            val oldFile = UserPreferences.newBuilder().setShowSquare(square).build().toByteArray()
            val restored = UserPreferencesCompat(UserPreferences.parseFrom(oldFile))
            assertTrue(restored.showHomeCarousel)
            assertTrue(restored.showHomeChannels)
            assertEquals(square, restored.showSquare)
        }
        val fresh = UserPreferencesCompat.default()
        assertTrue(fresh.showHomeCarousel)
        assertTrue(fresh.showHomeChannels)
        assertTrue(fresh.showSquare)
    }

    @Test
    fun everyCombinationSurvivesSavingOtherSettingsAndRestart() {
        for (mask in 0..7) {
            val saved = UserPreferencesCompat.default().copy(
                showHomeCarousel = mask and 1 != 0,
                showHomeChannels = mask and 2 != 0,
                showSquare = mask and 4 != 0,
            )
            val bytes = saved.copy(fontScale = 1.2f, showEmoji = false).toProto().toByteArray()
            val restored = UserPreferencesCompat(UserPreferences.parseFrom(bytes))
            assertEquals(saved.showHomeCarousel, restored.showHomeCarousel)
            assertEquals(saved.showHomeChannels, restored.showHomeChannels)
            assertEquals(saved.showSquare, restored.showSquare)
            assertEquals(1.2f, restored.fontScale)
            assertEquals(false, restored.showEmoji)
        }
    }
}
