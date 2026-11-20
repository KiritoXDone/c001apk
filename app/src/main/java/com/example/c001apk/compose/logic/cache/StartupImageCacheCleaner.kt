package com.example.c001apk.compose.logic.cache

import android.content.Context
import android.util.Log
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import net.mikaelzero.mojito.view.sketch.core.cache.DiskCache
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils

@Singleton
class StartupImageCacheCleaner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: UserPreferencesRepository,
) {
    // 每个进程只在首次进入主界面时清理，任务不随 Activity 重建或销毁而取消。
    private val cleanup = CoroutineScope(SupervisorJob() + Dispatchers.IO).async(
        start = CoroutineStart.LAZY,
    ) {
        try {
            if (preferences.data.first().autoClearImageCache) clearImageCaches()
        } catch (e: IOException) {
            Log.w("StartupImageCache", "自动清理图片缓存失败", e)
        }
    }

    suspend fun awaitCleanup() = cleanup.await()

    @OptIn(ExperimentalCoilApi::class)
    private fun clearImageCaches() {
        val loader = Coil.imageLoader(context)
        loader.memoryCache?.clear()
        loader.diskCache?.clear()

        val directories = buildList {
            listOf("mojito", "motion-photo", "live-photo-video", "live-photo-export").forEach { name ->
                add(File(context.cacheDir, name))
            }
            context.externalCacheDirs.filterNotNull().forEach { root ->
                add(File(root, "imageShare"))
            }
            val sketchName = SketchUtils.appendProcessName(context, DiskCache.DISK_CACHE_DIR_NAME)
            // Sketch 可在内外部缓存目录中使用带数字后缀的目录。
            (listOf(context.cacheDir) + context.externalCacheDirs.filterNotNull()).forEach { root ->
                for (index in 0..10) {
                    add(File(root, sketchName + if (index == 0) "" else index.toString()))
                }
            }
        }
        var failed = false
        directories.forEach { directory ->
            if (!directory.deleteRecursively()) failed = true
        }
        if (failed) throw IOException("部分图片缓存未能清理")
    }
}
