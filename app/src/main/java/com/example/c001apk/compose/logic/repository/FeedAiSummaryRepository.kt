package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.R
import com.example.c001apk.compose.di.Api1StreamService
import com.example.c001apk.compose.di.PublicStreamClient
import com.example.c001apk.compose.logic.model.FeedAiSummaryMode
import com.example.c001apk.compose.logic.model.FeedAiSummaryUpdate
import com.example.c001apk.compose.logic.network.ApiFailure
import com.example.c001apk.compose.logic.network.ApiFailureKind
import com.example.c001apk.compose.logic.network.ApiService
import com.example.c001apk.compose.logic.network.FeedAiSummaryProtocol
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.CoolapkRequestPolicy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class FeedAiSummaryRepository
@Inject
constructor(
    @param:Api1StreamService private val api: ApiService,
    @param:PublicStreamClient private val publicClient: OkHttpClient,
) {
    fun streamSummary(
        feedId: String,
        aiAccessKey: String?,
        hasTitle: Boolean,
        mode: FeedAiSummaryMode,
        expectedRevision: Long,
    ): Flow<FeedAiSummaryUpdate> = flow {
        when (mode) {
            FeedAiSummaryMode.OfficialConcise ->
                streamOfficialSummary(
                    feedId = feedId,
                    aiAccessKey = aiAccessKey.orEmpty(),
                    hasTitle = hasTitle,
                    isRaw = false,
                    expectedRevision = expectedRevision,
                    emitUpdate = { emit(it) },
                )

            FeedAiSummaryMode.OfficialRaw ->
                streamOfficialSummary(
                    feedId = feedId,
                    aiAccessKey = aiAccessKey.orEmpty(),
                    hasTitle = hasTitle,
                    isRaw = true,
                    expectedRevision = expectedRevision,
                    emitUpdate = { emit(it) },
                )

            FeedAiSummaryMode.Coolapk1s ->
                streamCoolapk1sSummary(
                    feedId = feedId,
                    expectedRevision = expectedRevision,
                    emitUpdate = { emit(it) },
                )
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun streamOfficialSummary(
        feedId: String,
        aiAccessKey: String,
        hasTitle: Boolean,
        isRaw: Boolean,
        expectedRevision: Long,
        emitUpdate: suspend (FeedAiSummaryUpdate) -> Unit,
    ) {
        ensureActiveAccount(expectedRevision, requireLogin = true)
        if (aiAccessKey.isBlank()) {
            emitUpdate(FeedAiSummaryUpdate.Unavailable)
            return
        }
        val modelLabel = FeedAiSummaryProtocol.OFFICIAL_MODEL_LABEL
        val prompt = FeedAiSummaryProtocol.selectPrompt(hasTitle)

        val historyResponse = api.getFeedAiHistory(feedId)
        ensureActiveAccount(expectedRevision, requireLogin = true)
        if (
            (historyResponse.status != null && historyResponse.status != 0) ||
                !historyResponse.message.isNullOrBlank()
        ) {
            val message = historyResponse.message?.trim().orEmpty()
            throw ApiFailure(
                kind = ApiFailureKind.BUSINESS,
                message = message.ifBlank { "feedAskHistoryList failed (status=${historyResponse.status})" },
                userMessageRes = if (message.isBlank()) R.string.api_business else null,
            )
        }
        val history =
            historyResponse.data
                ?: throw ApiFailure(
                    kind = ApiFailureKind.INVALID_RESPONSE,
                    message = "feedAskHistoryList did not return a history list",
                    userMessageRes = R.string.api_invalid_response,
                )

        val lookup = FeedAiSummaryProtocol.inspectHistory(history, prompt)
        val cached = if (isRaw) lookup.cachedRaw else lookup.cachedConcise
        if (cached != null) {
            emitUpdate(FeedAiSummaryUpdate.Completed(cached, modelLabel))
            return
        }

        if (!isRaw && !lookup.hasPriorAnswer) {
            // 无首轮历史时先以空 prompt 非流式建立上下文，再发起二次追问生成短摘要。
            emitUpdate(FeedAiSummaryUpdate.PreparingContext(modelLabel))
            executeOfficialAsk(
                feedId = feedId,
                aiAccessKey = aiAccessKey,
                prompt = "",
                streamOutput = false,
                expectedRevision = expectedRevision,
                onDelta = {},
            )
        }

        emitUpdate(FeedAiSummaryUpdate.Generating(modelLabel))
        val summary =
            executeOfficialAsk(
                feedId = feedId,
                aiAccessKey = aiAccessKey,
                prompt = if (isRaw) "" else prompt,
                streamOutput = true,
                expectedRevision = expectedRevision,
                onDelta = { emitUpdate(FeedAiSummaryUpdate.Streaming(it, modelLabel)) },
            )
        emitUpdate(FeedAiSummaryUpdate.Completed(summary, modelLabel))
    }

    private suspend fun executeOfficialAsk(
        feedId: String,
        aiAccessKey: String,
        prompt: String,
        streamOutput: Boolean,
        expectedRevision: Long,
        onDelta: suspend (String) -> Unit,
    ): String {
        ensureActiveAccount(expectedRevision, requireLogin = true)
        val call =
            api.postFeedAiAsk(
                feedId = feedId,
                model = FeedAiSummaryProtocol.MODEL,
                aiAccessKey = aiAccessKey,
                incrementalOutput = 1,
                streamOutput = if (streamOutput) 1 else 0,
                isRetry = 0,
                prompt = prompt,
            )
        val cancelHandle =
            currentCoroutineContext().job.invokeOnCompletion { cause ->
                if (cause != null) call.cancel()
            }
        try {
            val response = call.execute()
            ensureActiveAccount(expectedRevision, requireLogin = true)
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                throw ApiFailure(
                    kind = ApiFailureKind.HTTP,
                    message = "HTTP ${response.code()}",
                    userMessageRes = R.string.api_http,
                    messageArgs = listOf(response.code()),
                )
            }
            val body =
                response.body()
                    ?: throw ApiFailure(
                        kind = ApiFailureKind.INVALID_RESPONSE,
                        message = "Empty feedAsk response body",
                        userMessageRes = R.string.api_invalid_response,
                    )
            return body.use { responseBody ->
                FeedAiSummaryProtocol.parseAskResponse(
                    responseBody = responseBody,
                    emitStream = streamOutput,
                    checkActive = {
                        currentCoroutineContext().ensureActive()
                        ensureActiveAccount(expectedRevision, requireLogin = true)
                    },
                    onDelta = onDelta,
                )
            }
        } catch (e: IOException) {
            currentCoroutineContext().ensureActive()
            ensureActiveAccount(expectedRevision, requireLogin = true)
            throw e
        } finally {
            cancelHandle.dispose()
        }
    }

    private suspend fun streamCoolapk1sSummary(
        feedId: String,
        expectedRevision: Long,
        emitUpdate: suspend (FeedAiSummaryUpdate) -> Unit,
    ) {
        ensureActiveAccount(expectedRevision, requireLogin = false)
        if (!feedId.matches(Regex("^[0-9]+$"))) {
            emitUpdate(FeedAiSummaryUpdate.Unavailable)
            return
        }
        emitUpdate(FeedAiSummaryUpdate.Generating(modelName = null))
        val request =
            Request.Builder()
                .url("https://www.coolapk1s.com/summary/$feedId")
                .header("Referer", "https://www.coolapk1s.com/feed/$feedId")
                .header("User-Agent", CoolapkRequestPolicy.PUBLIC_USER_AGENT)
                .header("Accept", "text/event-stream, application/json")
                .get()
                .build()
        val call = publicClient.newCall(request)
        val cancelHandle =
            currentCoroutineContext().job.invokeOnCompletion { cause ->
                if (cause != null) call.cancel()
            }
        try {
            call.execute().use { response ->
                ensureActiveAccount(expectedRevision, requireLogin = false)
                if (response.code == 404) {
                    emitUpdate(FeedAiSummaryUpdate.Unavailable)
                    return
                }
                if (!response.isSuccessful) {
                    throw ApiFailure(
                        kind = ApiFailureKind.HTTP,
                        message = "HTTP ${response.code}",
                        userMessageRes = R.string.api_http,
                        messageArgs = listOf(response.code),
                    )
                }
                val body = response.body
                if (body == null) {
                    emitUpdate(FeedAiSummaryUpdate.Unavailable)
                    return
                }
                val result =
                    FeedAiSummaryProtocol.parseCoolapk1sResponse(
                        responseBody = body,
                        checkActive = {
                            currentCoroutineContext().ensureActive()
                            ensureActiveAccount(expectedRevision, requireLogin = false)
                        },
                        onDelta = { text, model ->
                            emitUpdate(FeedAiSummaryUpdate.Streaming(text, model))
                        },
                    )
                if (result == null) {
                    emitUpdate(FeedAiSummaryUpdate.Unavailable)
                } else {
                    emitUpdate(FeedAiSummaryUpdate.Completed(result.text, result.modelName))
                }
            }
        } catch (e: IOException) {
            currentCoroutineContext().ensureActive()
            ensureActiveAccount(expectedRevision, requireLogin = false)
            throw e
        } finally {
            cancelHandle.dispose()
        }
    }

    private fun ensureActiveAccount(expectedRevision: Long, requireLogin: Boolean) {
        val account = CookieUtil.accountSession
        if (account.revision != expectedRevision) {
            throw CancellationException("Account changed")
        }
        if (requireLogin && !account.isLogin) {
            throw ApiFailure(
                kind = ApiFailureKind.AUTHENTICATION,
                message = "Authentication required",
                userMessageRes = R.string.api_login,
            )
        }
    }
}
