package com.example.c001apk.compose.logic.smid

import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DigitalUnionRepository
@Inject
constructor(
    private val client: DigitalUnionClient,
    private val preferences: UserPreferencesRepository,
) {
    private val queryMutex = Mutex()

    suspend fun ensureId(force: Boolean = false): Result<String> = queryMutex.withLock {
        val current = preferences.data.first()
        if (!force && current.szlmId.isNotBlank() && !current.digitalUnionRefreshPending) {
            if (current.xAppDevice.isBlank() || (!current.protocolVersionCustomized && current.versionName == "13.4.1")) {
                if (!preferences.saveGeneratedIdentity(current.szlmId, current)) return@withLock latestIdentity()
            }
            return@withLock Result.success(current.szlmId)
        }
        try {
            val id = client.query()
            if (preferences.saveGeneratedIdentity(id, current)) Result.success(id)
            else latestIdentity()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            Result.failure(failure)
        } catch (failure: Exception) {
            Result.failure(IOException("获取数盟 ID 失败，请稍后重试", failure))
        } catch (failure: LinkageError) {
            Result.failure(IOException("数盟组件加载失败", failure))
        }
    }

    private suspend fun latestIdentity(): Result<String> {
        val id = preferences.data.first().szlmId
        return if (id.isNotBlank()) Result.success(id)
        else Result.failure(IOException("设备标识已更新，已保留你刚保存的值"))
    }
}
