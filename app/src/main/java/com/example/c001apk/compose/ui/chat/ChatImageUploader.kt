package com.example.c001apk.compose.ui.chat

import android.content.Context
import android.net.Uri
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.*
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.network.ApiFailure
import com.example.c001apk.compose.logic.network.ApiFailureKind
import com.example.c001apk.compose.logic.model.OSSUploadPrepareModel
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.util.OSSUtil
import com.example.c001apk.compose.util.OSSUtil.toHex
import com.example.c001apk.compose.util.readAtMost
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ChatImageUploader
@Inject
constructor(@ApplicationContext private val context: Context, private val network: NetworkRepo) {
    suspend fun upload(uri: Uri, uid: String, active: () -> Boolean): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use {
                if (it.readAtMost(20 * 1024 * 1024 + 1).size > 20 * 1024 * 1024) {
                    throw ApiFailure(ApiFailureKind.BUSINESS, "Image exceeds 20 MiB", userMessageRes = R.string.image_too_large)
                }
            } ?: throw ApiFailure(ApiFailureKind.BUSINESS, "Image cannot be read", userMessageRes = R.string.image_unreadable)
            val (dimensions, digest) =
                OSSUtil.getImageDimensionsAndMD5(context.contentResolver, uri)
            val (width, height, mime) = dimensions ?: throw ApiFailure(ApiFailureKind.BUSINESS, "Image cannot be decoded", userMessageRes = R.string.image_undecodable)
            if (width <= 0 || height <= 0 || width.toLong() * height > 32_000_000L) {
                throw ApiFailure(ApiFailureKind.BUSINESS, "Invalid image dimensions", userMessageRes = R.string.image_invalid_dimensions)
            }
            if (mime !in listOf("image/jpeg", "image/png", "image/gif", "image/webp")) {
                throw ApiFailure(ApiFailureKind.BUSINESS, "Unsupported image format", userMessageRes = R.string.image_unsupported_format)
            }
            require(active()) { "Account changed" }
            val name = "${java.util.UUID.randomUUID()}.${mime.substringAfter('/')}"
            val response =
                network
                    .postOSSUploadPrepare(
                        hashMapOf(
                            "uploadBucket" to "message",
                            "uploadDir" to "message",
                            "is_anonymous" to "0",
                            "toUid" to uid,
                            "uploadFileList" to
                                Gson()
                                    .toJson(
                                        listOf(
                                            OSSUploadPrepareModel(
                                                name,
                                                "${width}x${height}",
                                                digest?.toHex() ?: error("Image checksum is missing"),
                                            ),
                                        ),
                                    ),
                        ),
                    )
                    .first()
                    .getOrThrow()
            val prepared = response.data ?: error("Image upload preparation failed")
            val info = prepared.uploadPrepareInfo
            val path = prepared.fileInfo.firstOrNull()?.uploadFileName ?: error("Upload configuration is incomplete")
            require(path.startsWith("message/") && active()) { "Upload configuration expired" }
            require(
                listOf(info.accessKeyId, info.accessKeySecret, info.securityToken, info.bucket)
                    .all { it.isNotBlank() },
            )
            val endpoint =
                "https://" + info.endPoint.removePrefix("https://").removePrefix("http://")
            val client =
                OSSClient(
                    context,
                    endpoint,
                    OSSStsTokenCredentialProvider(
                        info.accessKeyId,
                        info.accessKeySecret,
                        info.securityToken,
                    ),
                    ClientConfiguration().apply {
                        connectionTimeout = 15000
                        socketTimeout = 60000
                        maxErrorRetry = 0
                    },
                )
            val request =
                PutObjectRequest(info.bucket, path, uri).apply {
                    metadata = ObjectMetadata().apply { contentType = mime }
                }
            suspendCancellableCoroutine<Unit> { continuation ->
                val task =
                    client.asyncPutObject(
                        request,
                        object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                            override fun onSuccess(
                                request: PutObjectRequest?,
                                result: PutObjectResult?,
                            ) {
                                if (continuation.isActive) continuation.resume(Unit)
                            }

                            override fun onFailure(
                                request: PutObjectRequest?,
                                clientException: ClientException?,
                                serviceException: ServiceException?,
                            ) {
                                if (continuation.isActive)
                                    continuation.resumeWithException(ApiFailure(ApiFailureKind.BUSINESS, "Image upload failed", userMessageRes = R.string.image_upload_failed))
                            }
                        },
                    )
                continuation.invokeOnCancellation { task.cancel() }
            }
            require(active()) { "Account changed" }
            "/$path"
        }
}
