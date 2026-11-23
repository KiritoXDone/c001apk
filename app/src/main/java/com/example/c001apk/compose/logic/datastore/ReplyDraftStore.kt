package com.example.c001apk.compose.logic.datastore

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.c001apk.compose.logic.model.OSSUploadPrepareModel
import com.example.c001apk.compose.util.OSSUtil.getImageDimensionsAndMD5
import com.example.c001apk.compose.util.OSSUtil.toHex
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class ReplyEditorDraft(
    val text: String,
    val checked: Boolean,
    val images: List<ReplyDraftImage>,
)

@Serializable
internal data class ReplyDraftImage(
    val fileName: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val checksum: String,
) {
    fun checksumBytes(): ByteArray = Base64.decode(checksum, Base64.NO_WRAP)

    fun toUploadModel() =
        OSSUploadPrepareModel(
            name = "${fileName.replace("-", "")}.${mimeType.removePrefix("image/")}",
            resolution = "${width}x$height",
            md5 = checksumBytes().toHex(),
        )
}

internal class ReplyDraftStore(context: Context) {
    private val app = context.applicationContext
    private val preferences = app.getSharedPreferences("reply_drafts", Context.MODE_PRIVATE)
    private val imageDirectory = File(app.filesDir, "reply_draft_images")

    fun key(accountId: String, target: List<String>): String =
        MessageDigest.getInstance("SHA-256")
            .digest(Json.encodeToString(listOf(accountId) + target).toByteArray())
            .toHex()

    fun load(key: String): ReplyEditorDraft? =
        read(key)?.let { draft ->
            draft.copy(images = draft.images.filter { File(imageDirectory, it.fileName).isFile })
        }

    fun save(key: String, draft: ReplyEditorDraft?) {
        val previous = read(key)
        if (draft == previous) return
        with(preferences.edit()) {
            if (draft == null) remove(key) else putString(key, Json.encodeToString(draft))
            apply()
        }
        val retainedNames = draft?.images.orEmpty().mapTo(hashSetOf()) { it.fileName }
        deleteImages(previous?.images.orEmpty().filter { it.fileName !in retainedNames })
    }

    fun imageUri(image: ReplyDraftImage): Uri = imageUri(File(imageDirectory, image.fileName))

    private fun imageUri(file: File): Uri =
        FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)

    suspend fun importImage(source: Uri): ReplyDraftImage {
        val file = File(imageDirectory, UUID.randomUUID().toString())
        var imported = false
        try {
            val image = withContext(Dispatchers.IO) {
                imageDirectory.mkdirs()
                val resolver = app.contentResolver
                val input = resolver.openInputStream(source) ?: throw IOException("Cannot open image")
                input.use { stream -> file.outputStream().use { stream.copyTo(it) } }
                val (dimensions, checksum) = getImageDimensionsAndMD5(resolver, imageUri(file))
                if (dimensions == null || checksum == null || dimensions.first <= 0 ||
                    dimensions.second <= 0 || !dimensions.third.startsWith("image/")
                ) {
                    throw IOException("Cannot read image")
                }
                ReplyDraftImage(
                    fileName = file.name,
                    width = dimensions.first,
                    height = dimensions.second,
                    mimeType = dimensions.third,
                    checksum = Base64.encodeToString(checksum, Base64.NO_WRAP),
                )
            }
            imported = true
            return image
        } finally {
            if (!imported) withContext(NonCancellable + Dispatchers.IO) { file.delete() }
        }
    }

    fun deleteImages(images: List<ReplyDraftImage>) {
        if (images.isEmpty()) return
        val files = images.map { File(imageDirectory, it.fileName) }
        cleanupScope.launch { files.forEach { it.delete() } }
    }

    private fun read(key: String): ReplyEditorDraft? {
        val value = preferences.getString(key, null) ?: return null
        return try {
            Json.decodeFromString<ReplyEditorDraft>(value)
        } catch (_: SerializationException) {
            null
        }
    }

    private companion object {
        val cleanupScope = CoroutineScope(Dispatchers.IO)
    }
}
