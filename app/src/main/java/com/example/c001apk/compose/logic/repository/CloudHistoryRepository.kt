package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.di.Api1Service
import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import com.example.c001apk.compose.logic.network.ApiFailure
import com.example.c001apk.compose.logic.network.ApiFailureKind
import com.example.c001apk.compose.logic.network.ApiService
import com.example.c001apk.compose.util.CookieUtil
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class CloudHistoryRepository @Inject constructor(@param:Api1Service private val api: ApiService) {
    suspend fun page(page: Int, firstItem: String?, lastItem: String?, expectedRevision: Long): List<CloudHistoryEntry> {
        val account = CookieUtil.accountSession
        if (account.revision != expectedRevision) throw CancellationException("账号已切换")
        if (!account.isLogin) throw ApiFailure(ApiFailureKind.AUTHENTICATION, "登录后可查看云端浏览历史")
        val response = api.getCloudHistory(page, firstItem, lastItem)
        if (account.revision != CookieUtil.accountSession.revision) throw CancellationException("账号已切换")
        if (response.error?.let { it != 0 } == true || response.status?.let { it !in listOf(0, 1) } == true) {
            throw ApiFailure(ApiFailureKind.BUSINESS, response.message ?: "云端浏览历史加载失败")
        }
        return response.data ?: throw ApiFailure(ApiFailureKind.INVALID_RESPONSE, response.message ?: "未收到云端浏览历史")
    }
}
