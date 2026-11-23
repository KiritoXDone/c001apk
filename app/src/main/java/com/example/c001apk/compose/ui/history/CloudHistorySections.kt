package com.example.c001apk.compose.ui.history

import androidx.annotation.StringRes
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import java.util.Calendar

internal enum class HistoryTimeSection(@param:StringRes val titleRes: Int) {
    RECENT(R.string.history_recent),
    TODAY(R.string.history_today),
    EARLIER(R.string.history_earlier),
}

internal fun cloudHistorySections(rows: List<CloudHistoryEntry>, nowMillis: Long): Map<HistoryTimeSection, List<CloudHistoryEntry>> {
    val startOfDay = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis / 1000
    val now = nowMillis / 1000
    return rows.groupBy { row ->
        val time = row.dateline ?: 0L
        when {
            time <= 0 || time < startOfDay -> HistoryTimeSection.EARLIER
            now - time < 3600 -> HistoryTimeSection.RECENT
            else -> HistoryTimeSection.TODAY
        }
    }
}
