package com.example.c001apk.compose.ui.history

import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import java.util.Calendar

internal enum class HistoryTimeSection(val title: String) {
    RECENT("刚刚"),
    TODAY("今天早些时候"),
    EARLIER("昨天或更早"),
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
