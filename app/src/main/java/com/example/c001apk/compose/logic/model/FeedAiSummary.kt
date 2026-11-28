package com.example.c001apk.compose.logic.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class FeedAiHistoryResponse(
    val status: Int? = null,
    val error: Int? = null,
    val message: String? = null,
    val messageStatus: String? = null,
    val data: List<FeedAiHistoryItem>? = null,
)

data class FeedAiHistoryItem(
    val entityType: String? = null,
    @SerializedName("entityId")
    val entityId: JsonElement? = null,
    val content: String? = null,
) {
    fun normalizedEntityId(): String? =
        entityId
            ?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }
            ?.asString
            ?.trim()
            ?.takeIf(String::isNotEmpty)
}

enum class FeedAiSummaryMode {
    OfficialConcise,
    OfficialRaw,
    Coolapk1s,
}

enum class FeedAiSummaryTrigger {
    Auto,
    ClickInline,
    ClickModal,
}

data class FeedAiSummaryPlan(
    val mode: FeedAiSummaryMode,
    val trigger: FeedAiSummaryTrigger,
    val silentOnFailure: Boolean,
    val defaultModelLabel: String?,
)

sealed interface FeedAiSummaryUpdate {
    data class PreparingContext(val modelName: String?) : FeedAiSummaryUpdate

    data class Generating(val modelName: String?) : FeedAiSummaryUpdate

    data class Streaming(val text: String, val modelName: String?) : FeedAiSummaryUpdate

    data class Completed(val text: String, val modelName: String?) : FeedAiSummaryUpdate

    data object Unavailable : FeedAiSummaryUpdate
}

sealed interface FeedAiSummaryState {
    data object Hidden : FeedAiSummaryState

    data class Idle(
        val plan: FeedAiSummaryPlan,
        val modelName: String? = plan.defaultModelLabel,
    ) : FeedAiSummaryState

    data class Loading(
        val plan: FeedAiSummaryPlan,
        val text: String = "",
        val modelName: String? = plan.defaultModelLabel,
        val isPreparingContext: Boolean = false,
    ) : FeedAiSummaryState

    data class Success(
        val plan: FeedAiSummaryPlan,
        val text: String,
        val modelName: String? = plan.defaultModelLabel,
    ) : FeedAiSummaryState

    data class Error(
        val plan: FeedAiSummaryPlan,
        val message: String,
        val modelName: String? = plan.defaultModelLabel,
    ) : FeedAiSummaryState
}
