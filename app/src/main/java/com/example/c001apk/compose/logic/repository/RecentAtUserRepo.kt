package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.logic.dao.RecentAtUserDao
import com.example.c001apk.compose.logic.model.RecentAtUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RecentAtUserRepo @Inject constructor(
    private val recentAtUserDao: RecentAtUserDao,
) {

    fun loadAllListFlow(): Flow<List<RecentAtUser>> = recentAtUserDao.loadAllListFlow()

    suspend fun rememberUser(username: String, avatar: String) {
        recentAtUserDao.rememberUser(username, avatar, System.currentTimeMillis())
    }

    suspend fun insertUser(user: RecentAtUser) {
        recentAtUserDao.insert(user)
    }

    suspend fun insertList(list: List<RecentAtUser>) {
        recentAtUserDao.insertList(list)
    }

    suspend fun deleteUser(user: String) {
        recentAtUserDao.delete(user)
    }

    suspend fun deleteUser(user: RecentAtUser) {
        recentAtUserDao.delete(user)
    }

    suspend fun deleteAll() {
        recentAtUserDao.deleteAll()
    }

    suspend fun checkUser(username: String): Boolean {
        return recentAtUserDao.isExist(username)
    }

    suspend fun updateUser(username: String, newId: Long) {
        recentAtUserDao.updateUser(username, newId)
    }

}
