package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.logic.dao.HomeMenuDao
import com.example.c001apk.compose.logic.model.HomeMenu
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeMenuRepo @Inject constructor(
    private val homeMenuDao: HomeMenuDao,
) {

    fun loadAllListFlow(): Flow<List<HomeMenu>> {
        return homeMenuDao.loadAllListFlow()
    }

    suspend fun loadAllList(): List<HomeMenu> {
        return homeMenuDao.loadAllList()
    }

    suspend fun insert(homeMenu: HomeMenu) {
        homeMenuDao.insert(homeMenu)
    }

    suspend fun insertList(list: List<HomeMenu>) {
        homeMenuDao.insertList(list)
    }

    suspend fun upsertList(list: List<HomeMenu>) {
        homeMenuDao.upsertList(list)
    }

    suspend fun updateList(list: List<HomeMenu>) {
        homeMenuDao.updateList(list)
    }

    suspend fun deleteAll() {
        homeMenuDao.deleteAll()
    }

}
