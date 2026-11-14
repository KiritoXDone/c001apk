package com.example.c001apk.compose.logic

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.database.RecentAtUserDatabase
import com.example.c001apk.compose.logic.model.RecentAtUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAtUserDatabaseTest {
    @Test
    fun repeatedSelectionsMoveToFrontAndKeepExistingAvatar() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, RecentAtUserDatabase::class.java).build()
        try {
            val dao = database.recentAtUserDao()
            dao.insert(RecentAtUser(id = 100, group = "legacy", avatar = "old-avatar", username = "Alice"))
            dao.rememberUser("Bob", "bob-avatar", 100)
            dao.rememberUser("Alice", "", 99)

            val users = dao.loadAllListFlow().first()
            assertEquals(listOf("Alice", "Bob"), users.map { it.username })
            assertEquals("old-avatar", users.first().avatar)
            assertEquals("legacy", users.first().group)
            assertEquals(listOf(102L, 101L), users.map { it.id })

            dao.rememberUser("Alice", "new-avatar", 99)
            assertEquals("new-avatar", dao.loadAllList().first().avatar)
            assertEquals(2, dao.loadAllList().size)
        } finally {
            database.close()
        }
    }
}
