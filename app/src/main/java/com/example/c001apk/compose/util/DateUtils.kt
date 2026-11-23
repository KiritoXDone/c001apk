package com.example.c001apk.compose.util

import com.example.c001apk.compose.R
import com.example.c001apk.compose.c001Application
import com.example.c001apk.compose.logic.settings.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object DateUtils {
    private const val ONE_MINUTE = 60L
    private const val ONE_HOUR = 3600L
    private const val ONE_DAY = 86400L
    private const val ONE_MONTH = 2592000L

    @JvmStatic
    fun fromToday(time: Long): String {
        val context = AppLanguage.wrap(c001Application)
        val ago = (System.currentTimeMillis() / 1000 - time).coerceAtLeast(0)
        if (ago == 0L) return context.getString(R.string.time_just_now)
        val relative = when {
            ago < ONE_MINUTE -> R.plurals.time_seconds_ago to ago
            ago <= ONE_HOUR -> R.plurals.time_minutes_ago to ago / ONE_MINUTE
            ago <= ONE_DAY -> R.plurals.time_hours_ago to ago / ONE_HOUR
            ago <= ONE_MONTH -> R.plurals.time_days_ago to ago / ONE_DAY
            else -> null
        }
        if (relative != null) {
            val (resource, count) = relative
            return context.resources.getQuantityString(resource, count.toInt(), count)
        }
        val date = Date(time * 1000)
        val sameYear = Calendar.getInstance().get(Calendar.YEAR) ==
            Calendar.getInstance().apply { this.time = date }.get(Calendar.YEAR)
        val pattern = context.getString(if (sameYear) R.string.time_date_pattern else R.string.time_full_date_pattern)
        return SimpleDateFormat(pattern, context.resources.configuration.locales[0]).format(date)
    }

    fun timeStamp2Date(time: Long): String = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        AppLanguage.wrap(c001Application).resources.configuration.locales[0],
    ).format(Date(time * 1000))
}
