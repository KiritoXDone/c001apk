package com.example.c001apk.compose.ui.chat

object ChatFeedLink {
    fun parse(input: String): String? {
        val value = input.trim()
        if (Regex("^[0-9]+$").matches(value)) return value
        val parts =
            com.example.c001apk.compose.util.CoolapkLink.parse(value)?.segments ?: return null
        return parts.getOrNull(1)?.takeIf {
            parts.size == 2 && parts[0] == "feed" && Regex("[0-9]+").matches(it)
        }
    }
}
