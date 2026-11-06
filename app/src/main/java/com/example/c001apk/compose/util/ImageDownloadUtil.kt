package com.example.c001apk.compose.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.c001apk.compose.R
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Created by bggRGjQaUbCoE on 2024/6/7 */
object ImageDownloadUtil {

    private val downloadClient =
        OkHttpClient.Builder()
            .addNetworkInterceptor(AddCookiesInterceptor)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

    suspend fun saveMotionPhoto(
        context: Context,
        source: File,
        fileName: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            if (!source.isFile || !GalleryPermission.granted(context)) return@withContext false
            if (SDK_INT >= 29) {
                saveMotionPhotoAboveQ(context, source, fileName)
            } else {
                saveMotionPhotoBelowQ(context, source, fileName)
            }
        }

    suspend fun downloadImage(
        context: Context,
        imageUrl: String,
        fileName: String,
        isEnd: Boolean,
        isShare: Boolean = false,
        userAgent: String?,
    ) {
        if (!isShare && !GalleryPermission.granted(context)) {
            withContext(Dispatchers.Main) { context.makeToast("请允许存储权限后重试") }
            return
        }
        if (isGifRequest(imageUrl, fileName)) {
            CoroutineScope(Dispatchers.IO).launch {
                val (bytes, mimeType) = downloadBytes(imageUrl, userAgent)
                val result =
                    if (bytes != null) {
                        if (isShare || SDK_INT < 29) {
                            saveBytesBelowQ(context, bytes, fileName, isShare)
                        } else {
                            saveBytesAboveQ(context, bytes, fileName, mimeType)
                        }
                    } else {
                        false
                    }
                if (!isShare && isEnd) {
                    withContext(Dispatchers.Main) {
                        context.makeToast(
                            if (result) "保存图片成功" else "保存图片失败",
                        )
                    }
                }
                if (isShare && result) {
                    ImageShowUtil.shareImage(
                        context,
                        File(context.externalCacheDir, "imageShare/$fileName"),
                        null,
                    )
                }
                if (!result) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return
        }

        val imageLoader = context.imageLoader

        val request =
            ImageRequest.Builder(context)
                .setParameter(ORIGINAL_IMAGE_EXPORT, true)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .data(imageUrl)
                .addHeader("User-Agent", userAgent ?: "")
                .target(
                    onSuccess = { drawable ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val result =
                                if (isShare || SDK_INT < 29) {
                                    saveImageBelowQ(context, drawable, fileName, isShare)
                                } else {
                                    saveImageAboveQ(context, drawable, fileName)
                                }
                            if (!isShare && isEnd) {
                                withContext(Dispatchers.Main) {
                                    context.makeToast(
                                        if (result) "保存图片成功" else "保存图片失败",
                                    )
                                }
                            }
                            if (isShare && result) {
                                ImageShowUtil.shareImage(
                                    context,
                                    File(context.externalCacheDir, "imageShare/$fileName"),
                                    null,
                                )
                            }
                        }
                    },
                    onError = {
                        Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show()
                    },
                )
                .build()

        imageLoader.enqueue(request)
    }

    @RequiresApi(29)
    private fun saveImageAboveQ(
        context: Context,
        drawable: Drawable,
        fileName: String,
    ): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.DESCRIPTION, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES +
                            "/${context.getString(R.string.app_name)}/",
                    )
                }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri
                ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                } ?: false
            } ?: false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(29)
    private fun saveBytesAboveQ(
        context: Context,
        bytes: ByteArray,
        fileName: String,
        mimeType: String?,
    ): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.DESCRIPTION, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType ?: "image/gif")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES +
                            "/${context.getString(R.string.app_name)}/",
                    )
                }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri
                ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                    outputStream.flush()
                } ?: false
            } ?: false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(29)
    private fun saveMotionPhotoAboveQ(
        context: Context,
        source: File,
        fileName: String,
    ): Boolean {
        val resolver = context.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/${context.getString(R.string.app_name)}/",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        val uri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: error("Unable to open motion photo destination")
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            resolver.delete(uri, null, null)
            false
        }
    }

    private fun saveMotionPhotoBelowQ(context: Context, source: File, fileName: String): Boolean =
        saveLegacyImage(context, fileName, false, "image/jpeg") { output ->
            source.inputStream().use { it.copyTo(output) }
        }

    private suspend fun saveImageBelowQ(
        context: Context,
        drawable: Drawable,
        fileName: String,
        isShare: Boolean,
    ): Boolean =
        withContext(Dispatchers.IO) {
            saveLegacyImage(context, fileName, isShare, "image/jpeg") { output ->
                check(drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, output))
            }
        }

    private suspend fun saveBytesBelowQ(
        context: Context,
        bytes: ByteArray,
        fileName: String,
        isShare: Boolean,
    ): Boolean =
        withContext(Dispatchers.IO) {
            saveLegacyImage(context, fileName, isShare, "image/gif") { output ->
                output.write(bytes)
            }
        }

    private fun saveLegacyImage(
        context: Context,
        fileName: String,
        isShare: Boolean,
        mimeType: String,
        write: (java.io.OutputStream) -> Unit,
    ): Boolean {
        if (!isShare && !GalleryPermission.granted(context)) return false
        val directory =
            if (isShare) File(context.externalCacheDir, "imageShare")
            else
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    context.getString(R.string.app_name),
                )
        return try {
            check(directory.isDirectory || directory.mkdirs())
            val destination = File(directory, File(fileName).name)
            FileOutputStream(destination).use(write)
            if (!isShare)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destination.absolutePath),
                    arrayOf(mimeType),
                    null,
                )
            true
        } catch (error: Exception) {
            error.printStackTrace()
            false
        }
    }

    private fun isGifRequest(imageUrl: String, fileName: String): Boolean {
        val cleanUrl = imageUrl.substringBefore('?').substringBefore('#')
        val cleanName = fileName.substringBefore('?').substringBefore('#')
        return cleanUrl.endsWith(".gif", ignoreCase = true) ||
            cleanName.endsWith(".gif", ignoreCase = true)
    }

    private suspend fun downloadBytes(
        imageUrl: String,
        userAgent: String?,
    ): Pair<ByteArray?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val client = downloadClient
                val request =
                    Request.Builder().url(imageUrl).header("User-Agent", userAgent ?: "").build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext null to null
                    }
                    val body = response.body ?: return@withContext null to null
                    require(body.contentLength() <= 64 * 1024 * 1024)
                    val bytes = body.byteStream().readAtMost(64 * 1024 * 1024 + 1)
                    require(bytes.size <= 64 * 1024 * 1024)
                    val contentType = response.header("Content-Type")
                    bytes to contentType
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null to null
            }
        }
    }
}
