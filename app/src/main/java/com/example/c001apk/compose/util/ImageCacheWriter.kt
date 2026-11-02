package com.example.c001apk.compose.util

import java.io.File
import java.io.IOException
import java.io.InputStream
import okio.FileSystem
import okio.Path.Companion.toOkioPath

// 仅发布完整下载，避免暴露其他请求尚未写完的文件。
internal fun writeImageCache(
    destination: File,
    input: InputStream,
    expectedLength: Long,
    isCanceled: () -> Boolean = { false },
    onProgress: (Int) -> Unit = {},
): File {
    val directory = checkNotNull(destination.parentFile)
    directory.mkdirs()
    // 并发下载各自持有临时文件，关闭后才发布。
    val temporary = File.createTempFile("image-", ".tmp", directory)
    try {
        var received = 0L
        temporary.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                if (isCanceled()) throw IOException("image request canceled")
                val count = input.read(buffer)
                if (count == -1) break
                output.write(buffer, 0, count)
                received += count
                if (expectedLength > 0) {
                    onProgress(((received * 100) / expectedLength).coerceIn(0, 100).toInt())
                }
            }
        }
        if (received == 0L || (expectedLength >= 0 && received != expectedLength)) {
            throw IOException("incomplete image download")
        }
        if (isCanceled()) throw IOException("image request canceled")
        // 同目录原子替换，读取方始终只能看到替换前或替换后的完整图片。
        FileSystem.SYSTEM.atomicMove(temporary.toOkioPath(), destination.toOkioPath())
        return destination
    } finally {
        temporary.delete()
    }
}
