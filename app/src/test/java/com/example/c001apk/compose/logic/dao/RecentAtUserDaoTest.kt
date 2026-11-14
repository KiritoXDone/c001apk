package com.example.c001apk.compose.logic.dao

import androidx.lifecycle.LiveData
import com.example.c001apk.compose.logic.model.RecentAtUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAtUserDaoTest {
    @Test
    fun rememberingUserUpdatesRecencyWithoutLosingAvatar() = runBlocking {
        val dao = FakeRecentAtUserDao()
        dao.insert(RecentAtUser(id = 100, group = "legacy", avatar = "old", username = "Alice"))
        dao.rememberUser("Bob", "bob", 100)
        dao.rememberUser("Alice", "", 99)

        val users = dao.loadAllList()
        assertEquals(listOf("Alice", "Bob"), users.map { it.username })
        assertEquals(listOf(102L, 101L), users.map { it.id })
        assertEquals("old", users.first().avatar)
        assertEquals("legacy", users.first().group)

        dao.rememberUser("Alice", "new", 99)
        assertEquals("new", dao.loadAllList().first().avatar)
        assertEquals(2, dao.loadAllList().size)
    }
}

private class FakeRecentAtUserDao : RecentAtUserDao {
    private val users = mutableMapOf<String, RecentAtUser>()

    override suspend fun insert(data: RecentAtUser) { users[data.username] = data }
    override suspend fun insertList(list: List<RecentAtUser>) { list.forEach { insert(it) } }
    override suspend fun loadAllList(): List<RecentAtUser> = users.values.sortedByDescending { it.id }
    override fun loadAllListLive(): LiveData<List<RecentAtUser>> = error("Not used")
    override fun loadAllListFlow(): Flow<List<RecentAtUser>> = error("Not used")
    override suspend fun findUser(username: String): RecentAtUser? = users[username]
    override suspend fun latestId(): Long? = users.values.maxOfOrNull { it.id }
    override suspend fun upsert(data: RecentAtUser) { users[data.username] = data }
    override suspend fun isExist(username: String): Boolean = username in users
    override suspend fun isContain(username: String): Boolean = error("Not used")
    override suspend fun delete(username: String) { users.remove(username) }
    override suspend fun delete(data: RecentAtUser) { users.remove(data.username) }
    override suspend fun deleteAll() { users.clear() }
    override suspend fun updateUser(username: String, newId: Long) { users[username]?.id = newId }
}
