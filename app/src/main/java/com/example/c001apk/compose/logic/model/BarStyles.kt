package com.example.c001apk.compose.logic.model

enum class TopBarStyle(val storedId: String) {
    Classic("classic"),
    ClassicContour("classic_contour"),
    OS4("os4");

    companion object {
        fun fromStoredId(id: String): TopBarStyle = entries.firstOrNull { it.storedId == id } ?: ClassicContour
    }
}

enum class BottomBarStyle(val storedId: String) {
    Classic("classic"),
    Floating("floating"),
    OS4("os4");

    companion object {
        fun fromStoredId(id: String): BottomBarStyle? = entries.firstOrNull { it.storedId == id }
    }
}
