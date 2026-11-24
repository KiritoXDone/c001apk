package com.example.c001apk.compose.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import com.example.c001apk.imageviewer.ImageFileLoader
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class PreviewImageFileLoader(context: Context) : ImageFileLoader {

    private val client = OkHttpClient.Builder().addNetworkInterceptor(AddCookiesInterceptor).build()

    private val callMap = ConcurrentHashMap<Int, MutableSet<Call>>()
    private val cacheDir = File(context.cacheDir, "mojito").apply { mkdirs() }

    override fun loadImage(
        requestId: Int,
        uri: Uri,
        onlyRetrieveFromCache: Boolean,
        callback: ImageFileLoader.Callback,
    ) {
        val scheme = uri.scheme?.lowercase()
        val isNetwork = scheme == "http" || scheme == "https"
        val cacheFile = resolveCacheFile(uri)
        callback.onStart()

        if (!isNetwork) {
            val localFile = if (scheme == "file") File(uri.path ?: "") else cacheFile
            if (localFile.exists()) {
                callback.onSuccess(localFile)
            } else {
                callback.onFail(IOException("file not found"))
            }
            return
        }

        if (onlyRetrieveFromCache) {
            if (cacheFile.isFile && cacheFile.length() > 0) {
                callback.onSuccess(cacheFile)
            } else {
                callback.onFail(IOException("cache miss"))
            }
            return
        }

        if (cacheFile.exists()) {
            if (cacheFile.length() > 0) {
                callback.onSuccess(cacheFile)
                return
            } else {
                cacheFile.delete()
            }
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val url = uri.toString().toHttpUrlOrNull()
        if (url == null) {
            callback.onFail(IOException("invalid image URL"))
            return
        }
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        callMap.compute(requestId) { _, calls ->
            (calls ?: mutableSetOf()).apply { add(call) }
        }
        thread(name = "preview-image-$requestId") {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("http ${response.code}")
                    }
                    val body = response.body ?: throw IOException("empty body")
                    body.byteStream().use { input ->
                        writeImageCache(
                            cacheFile,
                            input,
                            body.contentLength(),
                            call::isCanceled,
                            callback::onProgress,
                        )
                    }
                    callback.onSuccess(cacheFile)
                }
            } catch (e: Exception) {
                if (e !is IOException && e.cause is IOException) {
                    callback.onFail(e.cause as IOException)
                } else {
                    callback.onFail(e)
                }
            } finally {
                callMap.computeIfPresent(requestId) { _, calls ->
                    calls.remove(call)
                    calls.takeIf { it.isNotEmpty() }
                }
            }
        }
    }

    override fun cancel(requestId: Int) {
        callMap.remove(requestId)?.forEach { it.cancel() }
    }

    private fun resolveCacheFile(uri: Uri): File {
        val url = uri.toString()
        val extension =
            uri.lastPathSegment
                ?.substringBefore('?')
                ?.substringBefore('#')
                ?.substringAfterLast('.', "")
                ?.ifBlank { "img" } ?: "img"
        val name = md5(url)
        return File(cacheDir, "$name.$extension")
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(value.toByteArray())
        val builder = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            builder.append(String.format("%02x", b))
        }
        return builder.toString()
    }
}
