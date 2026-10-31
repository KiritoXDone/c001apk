package com.example.c001apk.compose.logic.smid

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/** App coroutine boundary around the reference's blocking implementation. */
class DigitalUnionClient @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun query(): String = runInterruptible(Dispatchers.IO) {
        DuClient.query(context)
    } ?: throw IOException("暂时无法获取数盟 ID，请检查网络后重试，或手动导入")
}
