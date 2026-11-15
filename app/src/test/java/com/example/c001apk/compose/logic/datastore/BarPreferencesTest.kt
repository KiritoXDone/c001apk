package com.example.c001apk.compose.logic.datastore

import androidx.datastore.core.DataStoreFactory
import com.example.c001apk.compose.UserPreferences
import com.example.c001apk.compose.logic.accounts.SavedAccount
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BarPreferencesTest {
    @Test
    fun freshAndExistingPreferencesStartWithEffectsOff() {
        val fresh = UserPreferencesCompat.default()
        // An older file has no fields 54–56; proto3 must decode all as false.
        val existing = UserPreferencesCompat(UserPreferences.getDefaultInstance())
        for (preferences in listOf(fresh, existing)) {
            assertFalse(preferences.floatingNavigationBar)
            assertEquals(TopBarStyle.ClassicContour, preferences.topBarStyle)
            assertNull(preferences.bottomBarStyle)
            assertEquals(BottomBarStyle.Classic, preferences.effectiveBottomBarStyle)
            assertFalse(preferences.blurBars)
            assertFalse(preferences.collapseTabsOnScroll)
        }
    }

    @Test
    fun independentChoicesSurviveSerializationAndUnrelatedChanges() {
        for (floating in listOf(false, true)) {
            for (blur in listOf(false, true)) {
                for (collapseTabs in listOf(false, true)) {
                    val saved = UserPreferencesCompat.default().copy(
                        floatingNavigationBar = floating,
                        blurBars = blur,
                        collapseTabsOnScroll = collapseTabs,
                    )
                    val bytes = saved.copy(contentScale = 0.8f).toProto().toByteArray()
                    val restored = UserPreferencesCompat(UserPreferences.parseFrom(bytes))
                    assertEquals(floating, restored.floatingNavigationBar)
                    assertEquals(blur, restored.blurBars)
                    assertEquals(collapseTabs, restored.collapseTabsOnScroll)
                    assertEquals(0.8f, restored.contentScale)
                }
            }
        }
    }

    @Test
    fun oldFloatingPreferenceMapsToMatchingBottomStyle() {
        for (floating in listOf(false, true)) {
            val old = UserPreferences.newBuilder().setFloatingNavigationBar(floating).build()
            val restored = UserPreferencesCompat(UserPreferences.parseFrom(old.toByteArray()))
            assertEquals(TopBarStyle.ClassicContour, restored.topBarStyle)
            assertNull(restored.bottomBarStyle)
            assertEquals(
                if (floating) BottomBarStyle.Floating else BottomBarStyle.Classic,
                restored.effectiveBottomBarStyle,
            )
            assertEquals("", restored.toProto().bottomBarStyle)
            val afterTopChange = UserPreferencesCompat(
                restored.copy(topBarStyle = TopBarStyle.Classic).toProto(),
            )
            assertNull(afterTopChange.bottomBarStyle)
            assertEquals(restored.effectiveBottomBarStyle, afterTopChange.effectiveBottomBarStyle)
        }
    }

    @Test
    fun styleIdsRoundTripAndUnknownIdsFallBackSafely() {
        assertEquals(listOf("classic", "classic_contour", "os4"), TopBarStyle.entries.map { it.storedId })
        assertEquals(TopBarStyle.Classic, TopBarStyle.fromStoredId("classic"))
        assertEquals(TopBarStyle.ClassicContour, TopBarStyle.fromStoredId("classic_contour"))
        assertEquals(TopBarStyle.OS4, TopBarStyle.fromStoredId("os4"))
        assertEquals(listOf("classic", "floating", "os4"), BottomBarStyle.entries.map { it.storedId })
        for (topStyle in TopBarStyle.entries) {
            for (bottomStyle in BottomBarStyle.entries) {
                val original = UserPreferencesCompat.default().copy(
                    topBarStyle = topStyle,
                    bottomBarStyle = bottomStyle,
                )
                val proto = original.toProto()
                assertEquals(topStyle.storedId, proto.topBarStyle)
                assertEquals(bottomStyle.storedId, proto.bottomBarStyle)
                val restored = UserPreferencesCompat(UserPreferences.parseFrom(proto.toByteArray()))
                assertEquals(topStyle, restored.topBarStyle)
                assertEquals(bottomStyle, restored.bottomBarStyle)
                assertEquals(bottomStyle, restored.effectiveBottomBarStyle)
            }
        }

        for (floating in listOf(false, true)) {
            val unknown = UserPreferences.newBuilder()
                .setTopBarStyle("unknown-top")
                .setBottomBarStyle("unknown-bottom")
                .setFloatingNavigationBar(floating)
                .build()
            val restored = UserPreferencesCompat(unknown)
            assertEquals(TopBarStyle.ClassicContour, restored.topBarStyle)
            assertNull(restored.bottomBarStyle)
            assertEquals(
                if (floating) BottomBarStyle.Floating else BottomBarStyle.Classic,
                restored.effectiveBottomBarStyle,
            )
        }
    }

    @Test
    fun repositoryStyleUpdatesPersistAndKeepOtherPreferences() = runBlocking {
        val directory = Files.createTempDirectory("bar-preferences").toFile()
        val file = directory.resolve("preferences.pb")
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = DataStoreFactory.create(UserPreferencesSerializer(), scope = firstScope, produceFile = { file })
            val repository = UserPreferencesRepository(UserPreferencesDataSource(store))
            val account = SavedAccount(uid = "123", username = "user", token = "token", avatar = "avatar")
            store.updateData {
                it.copy(
                    isLogin = true,
                    uid = account.uid,
                    token = account.token,
                    savedAccounts = listOf(account),
                    accountsVersion = 1,
                    accountRevision = 7,
                    fontScale = 1.2f,
                    collapseTabsOnScroll = true,
                )
            }
            repository.setBlurBars(true)
            for (topStyle in TopBarStyle.entries) {
                repository.setTopBarStyle(topStyle)
                for (bottomStyle in BottomBarStyle.entries) {
                    repository.setBottomBarStyle(bottomStyle)
                    val saved = repository.data.first()
                    assertEquals(topStyle, saved.topBarStyle)
                    assertEquals(bottomStyle, saved.bottomBarStyle)
                    assertEquals(bottomStyle == BottomBarStyle.Floating, saved.floatingNavigationBar)
                    assertTrue(saved.blurBars)
                    assertEquals(account, saved.savedAccounts.single())
                    assertEquals(1.2f, saved.fontScale)
                    assertTrue(saved.collapseTabsOnScroll)
                    val onDisk = UserPreferencesCompat(UserPreferences.parseFrom(file.readBytes()))
                    assertEquals(topStyle, onDisk.topBarStyle)
                    assertEquals(bottomStyle, onDisk.bottomBarStyle)
                }
            }
            repository.setTopBarStyle(TopBarStyle.Classic)
            var saved = repository.data.first()
            assertEquals(BottomBarStyle.OS4, saved.bottomBarStyle)
            assertTrue(saved.blurBars)
            repository.setBlurBars(false)
            saved = repository.data.first()
            assertEquals(TopBarStyle.Classic, saved.topBarStyle)
            assertEquals(BottomBarStyle.OS4, saved.bottomBarStyle)
            repository.setFloatingNavigationBar(true)
            saved = repository.data.first()
            assertEquals(BottomBarStyle.Floating, saved.effectiveBottomBarStyle)
            assertTrue(saved.floatingNavigationBar)
            repository.setFloatingNavigationBar(false)
            saved = repository.data.first()
            assertEquals(BottomBarStyle.Classic, saved.effectiveBottomBarStyle)
            assertFalse(saved.floatingNavigationBar)
            assertEquals(TopBarStyle.Classic, saved.topBarStyle)
            assertFalse(saved.blurBars)
            repository.setBottomBarStyle(BottomBarStyle.OS4)
            firstScope.coroutineContext[Job]!!.cancelAndJoin()

            val reopened = DataStoreFactory.create(UserPreferencesSerializer(), scope = secondScope, produceFile = { file })
            val restored = reopened.data.first()
            assertEquals(TopBarStyle.Classic, restored.topBarStyle)
            assertEquals(BottomBarStyle.OS4, restored.bottomBarStyle)
            assertFalse(restored.blurBars)
            assertEquals(account, restored.savedAccounts.single())
            assertTrue(restored.isLogin)
            assertEquals(account.uid, restored.uid)
            assertEquals(account.token, restored.token)
            assertEquals(7L, restored.accountRevision)
            assertEquals(1.2f, restored.fontScale)
            assertTrue(restored.collapseTabsOnScroll)
        } finally {
            firstScope.coroutineContext[Job]!!.cancelAndJoin()
            secondScope.coroutineContext[Job]!!.cancelAndJoin()
            directory.deleteRecursively()
        }
    }
}
