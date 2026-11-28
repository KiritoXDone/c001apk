package com.example.c001apk.compose.logic.network

import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.FeedAiHistoryItem
import com.example.c001apk.compose.logic.model.FeedAiSummaryMode
import com.example.c001apk.compose.logic.model.FeedAiSummaryPlan
import com.example.c001apk.compose.logic.model.FeedAiSummaryTrigger
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import org.jsoup.Jsoup

object FeedAiSummaryProtocol {
    const val MODEL = "deepseek-r1"
    const val OFFICIAL_MODEL_LABEL = "DeepSeek-R1"
    const val AUTO_SUMMARY_MIN_LENGTH = 800

    private const val ENTITY_TYPE_PROMPT = "ai_prompt"
    private const val ENTITY_TYPE_ANSWER = "ai_prompt_answer"
    private const val ENTITY_TYPE_ANSWER_STREAM = "ai_prompt_answer_stream"

    const val TITLED_PROMPT =
        "请将上述文章浓缩为1到2句中文纯文本摘要，控制在60到80字，不超过100字。" +
            "标题已在上方，勿复述标题，只写核心做法、结论或关键数据。" +
            "直接以产品或主体开头，禁以本文/作者/用户开头，不罗列条目，禁用换行、引号和任何Markdown符号。"

    const val UNTITLED_PROMPT =
        "请将上述文章浓缩为1到2句中文纯文本摘要，控制在60到80字，不超过100字。" +
            "必须点明产品或事件主体，并写核心做法、结论或关键数据。" +
            "直接以主体开头，禁以本文/作者/用户开头，不罗列条目，禁用换行、引号和任何Markdown符号。"

    data class HistoryLookup(
        val cachedConcise: String?,
        val cachedRaw: String?,
        val hasPriorAnswer: Boolean,
    )

    data class Coolapk1sResult(
        val text: String,
        val modelName: String?,
    )

    fun selectPrompt(hasTitle: Boolean): String = if (hasTitle) TITLED_PROMPT else UNTITLED_PROMPT

    fun plainTextLength(
        feed: HomeFeedResponse.Data,
        articleList: List<FeedArticleContentBean>?,
    ): Int {
        if (!articleList.isNullOrEmpty()) {
            return articleList
                .asSequence()
                .filter { it.type == "text" }
                .sumOf { stripHtmlToPlainText(it.message.orEmpty()).length }
        }
        return stripHtmlToPlainText(feed.message.orEmpty()).length
    }

    fun resolvePlan(
        feed: HomeFeedResponse.Data,
        articleList: List<FeedArticleContentBean>?,
        isLogin: Boolean,
        isPublic: Boolean,
        enableAiSummary: Boolean,
        useOfficialAiAssistant: Boolean,
        showRawAiSummaryModal: Boolean,
    ): FeedAiSummaryPlan? {
        if (!enableAiSummary) return null
        val hasAssistant = isLogin && !isPublic && feed.hasAiAssistant
        val isLongPost = plainTextLength(feed, articleList) >= AUTO_SUMMARY_MIN_LENGTH

        if (isLogin && useOfficialAiAssistant && showRawAiSummaryModal && hasAssistant) {
            return FeedAiSummaryPlan(
                mode = FeedAiSummaryMode.OfficialRaw,
                trigger = FeedAiSummaryTrigger.ClickModal,
                silentOnFailure = false,
                defaultModelLabel = OFFICIAL_MODEL_LABEL,
            )
        }

        if (isLogin && useOfficialAiAssistant) {
            return when {
                hasAssistant && isLongPost ->
                    FeedAiSummaryPlan(
                        mode = FeedAiSummaryMode.OfficialConcise,
                        trigger = FeedAiSummaryTrigger.Auto,
                        silentOnFailure = true,
                        defaultModelLabel = OFFICIAL_MODEL_LABEL,
                    )
                hasAssistant ->
                    FeedAiSummaryPlan(
                        mode = FeedAiSummaryMode.OfficialConcise,
                        trigger = FeedAiSummaryTrigger.ClickInline,
                        silentOnFailure = false,
                        defaultModelLabel = OFFICIAL_MODEL_LABEL,
                    )
                isLongPost ->
                    FeedAiSummaryPlan(
                        mode = FeedAiSummaryMode.Coolapk1s,
                        trigger = FeedAiSummaryTrigger.Auto,
                        silentOnFailure = true,
                        defaultModelLabel = null,
                    )
                else -> null
            }
        }

        return if (isLongPost) {
            FeedAiSummaryPlan(
                mode = FeedAiSummaryMode.Coolapk1s,
                trigger = FeedAiSummaryTrigger.Auto,
                silentOnFailure = true,
                defaultModelLabel = null,
            )
        } else {
            null
        }
    }

    fun inspectHistory(history: List<FeedAiHistoryItem>, prompt: String): HistoryLookup {
        // 历史按时间倒序排列，同组追问与回答共用 entityId。
        val answers = LinkedHashMap<String, String>()
        val prompts = LinkedHashMap<String, String>()
        for (item in history) {
            val entityId = item.normalizedEntityId() ?: continue
            val text = item.content?.trim().orEmpty()
            when (item.entityType) {
                ENTITY_TYPE_ANSWER -> if (text.isNotEmpty()) answers.putIfAbsent(entityId, text)
                ENTITY_TYPE_PROMPT -> prompts.putIfAbsent(entityId, text)
            }
        }
        val cachedConcise =
            prompts.entries.firstNotNullOfOrNull { (entityId, promptText) ->
                if (promptText == prompt) answers[entityId] else null
            }
        // 原始自动总结通常不带配对的用户提问；否则沿用最早一条回答。
        val chronologicalAnswers = answers.entries.toList().asReversed()
        val cachedRaw =
            chronologicalAnswers.firstOrNull { (entityId, _) -> entityId !in prompts }?.value
                ?: chronologicalAnswers.firstOrNull()?.value
        return HistoryLookup(
            cachedConcise = cachedConcise,
            cachedRaw = cachedRaw,
            hasPriorAnswer = answers.isNotEmpty(),
        )
    }

    fun validatePayload(payload: JsonObject) {
        ApiFailures.fromBody(payload)?.let { throw it }
        val status = payload.get("status")?.takeUnless { it.isJsonNull }?.asString?.trim()
        val message = payload.get("message")?.takeUnless { it.isJsonNull }?.asString?.trim().orEmpty()
        if ((status != null && status != "0") || message.isNotEmpty()) {
            throw ApiFailure(
                kind = ApiFailureKind.BUSINESS,
                message = message.ifBlank { "AI request failed (status=$status)" },
                userMessageRes = if (message.isBlank()) R.string.api_business else null,
            )
        }
    }

    suspend fun parseAskResponse(
        responseBody: ResponseBody,
        emitStream: Boolean,
        checkActive: () -> Unit,
        onDelta: suspend (String) -> Unit,
    ): String {
        val subtype = responseBody.contentType()?.subtype.orEmpty()
        if (subtype.equals("json", ignoreCase = true)) {
            val raw = responseBody.string()
            val payload = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull()
            if (payload != null) {
                validatePayload(payload)
            }
            throw ApiFailure(
                kind = ApiFailureKind.INVALID_RESPONSE,
                message = "feedAsk did not return an event stream",
                userMessageRes = R.string.api_invalid_response,
            )
        }

        val source = responseBody.source()
        val parts = StringBuilder()
        var finalText: String? = null
        while (true) {
            checkActive()
            val rawLine = source.readUtf8Line() ?: break
            val line = rawLine.trim().removePrefix("data:").trimStart()
            if (!line.startsWith("{")) continue
            val payload =
                runCatching { JsonParser.parseString(line).asJsonObject }
                    .getOrElse {
                        throw ApiFailure(
                            kind = ApiFailureKind.INVALID_RESPONSE,
                            message = "Malformed AI event payload",
                            userMessageRes = R.string.api_invalid_response,
                        )
                    }
            validatePayload(payload)
            val data = payload.get("data")?.takeIf { it.isJsonObject }?.asJsonObject ?: continue
            val entityType = data.get("entityType")?.takeUnless { it.isJsonNull }?.asString
            val text = data.get("text")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            when {
                entityType == ENTITY_TYPE_ANSWER_STREAM && text.isNotEmpty() -> {
                    parts.append(text)
                    if (emitStream) onDelta(parts.toString())
                }
                entityType == ENTITY_TYPE_ANSWER -> {
                    // 终态事件携带完整答案，不再重复追加到流式增量后。
                    finalText = text
                }
            }
        }

        val answer = (finalText ?: parts.toString()).trim()
        if (answer.isEmpty()) {
            throw ApiFailure(
                kind = ApiFailureKind.INVALID_RESPONSE,
                message = "AI event stream returned no summary",
                userMessageRes = R.string.feed_ai_summary_empty,
            )
        }
        return answer
    }

    suspend fun parseCoolapk1sResponse(
        responseBody: ResponseBody,
        checkActive: () -> Unit,
        onDelta: suspend (String, String?) -> Unit,
    ): Coolapk1sResult? {
        val subtype = responseBody.contentType()?.subtype.orEmpty()
        if (subtype.equals("json", ignoreCase = true)) {
            val raw = responseBody.string()
            val payload = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull()
                ?: return null
            val text = payload.get("text")?.takeUnless { it.isJsonNull }?.asString?.trim().orEmpty()
            val model = payload.get("model")?.takeUnless { it.isJsonNull }?.asString?.trim()
                ?.takeIf(String::isNotEmpty)
            return if (text.isEmpty()) null else Coolapk1sResult(text = text, modelName = model)
        }

        val source = responseBody.source()
        val parts = StringBuilder()
        var modelName: String? = null
        while (true) {
            checkActive()
            val rawLine = source.readUtf8Line() ?: break
            val trimmed = rawLine.trim()
            if (!trimmed.startsWith("data:")) continue
            val payloadText = trimmed.removePrefix("data:").trim()
            if (payloadText.isEmpty() || payloadText == "[DONE]") continue
            val payload = runCatching { JsonParser.parseString(payloadText).asJsonObject }.getOrNull()
                ?: continue
            payload.get("model")?.takeUnless { it.isJsonNull }?.asString?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { modelName = it }
            val error = payload.get("error")?.takeUnless { it.isJsonNull }?.asString?.trim()
            if (!error.isNullOrEmpty()) {
                throw ApiFailure(kind = ApiFailureKind.BUSINESS, message = error)
            }
            val delta = payload.get("delta")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            if (delta.isNotEmpty()) {
                parts.append(delta)
                onDelta(parts.toString(), modelName)
            }
            if (payload.get("done")?.takeUnless { it.isJsonNull }?.asBoolean == true) {
                break
            }
        }
        val text = parts.toString().trim()
        return if (text.isEmpty()) null else Coolapk1sResult(text = text, modelName = modelName)
    }

    private fun stripHtmlToPlainText(raw: String): String {
        if (raw.isBlank()) return ""
        return Jsoup.parseBodyFragment(raw).text().trim()
    }
}
