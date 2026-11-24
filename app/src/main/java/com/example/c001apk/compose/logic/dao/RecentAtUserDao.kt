package com.example.c001apk.compose.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.c001apk.compose.logic.model.RecentAtUser
import kotlinx.coroutines.flow.Flow


@Dao
interface RecentAtUserDao {

    @Insert
    suspend fun insert(data: RecentAtUser)

    @Insert
    suspend fun insertList(list: List<RecentAtUser>)

    @Query("SELECT * FROM RecentAtUser ORDER BY id DESC")
    suspend fun loadAllList(): List<RecentAtUser>

    @Query("SELECT * FROM RecentAtUser ORDER BY id DESC")
    fun loadAllListFlow(): Flow<List<RecentAtUser>>

    @Query("SELECT * FROM RecentAtUser WHERE username = :username LIMIT 1")
    suspend fun findUser(username: String): RecentAtUser?

    @Query("SELECT MAX(id) FROM RecentAtUser")
    suspend fun latestId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: RecentAtUser)

    @Transaction
    suspend fun rememberUser(username: String, avatar: String, timestamp: Long) {
        val previous = findUser(username)
        val nextId = maxOf(timestamp, (latestId() ?: 0L) + 1L)
        upsert(
            RecentAtUser(
                id = nextId,
                group = previous?.group ?: "recent",
                avatar = avatar.ifBlank { previous?.avatar.orEmpty() },
                username = username,
            ),
        )
    }

    @Query("SELECT 1 FROM RecentAtUser WHERE username = :username LIMIT 1")
    suspend fun isExist(username: String): Boolean

    @Transaction
    @Query("SELECT 1 FROM RecentAtUser WHERE :username LIKE '%' || username || '%' LIMIT 1")
    suspend fun isContain(username: String): Boolean

    @Query("DELETE FROM RecentAtUser WHERE username = :username")
    suspend fun delete(username: String)

    @Delete
    suspend fun delete(data: RecentAtUser)

    @Query("DELETE FROM RecentAtUser")
    suspend fun deleteAll()

    @Query("UPDATE RecentAtUser SET id = :newId WHERE username = :username")
    suspend fun updateUser(username: String, newId: Long)

}
