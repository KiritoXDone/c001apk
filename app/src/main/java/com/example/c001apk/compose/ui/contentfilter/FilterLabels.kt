package com.example.c001apk.compose.ui.contentfilter

import androidx.annotation.StringRes
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.filter.FilterKind
import com.example.c001apk.compose.logic.filter.FilterScope

@get:StringRes
internal val FilterKind.titleRes: Int
    get() = when (this) {
        FilterKind.TEXT -> R.string.filter_keywords
        FilterKind.AUTHOR -> R.string.label_user
        FilterKind.TOPIC -> R.string.label_topic
    }

@get:StringRes
internal val FilterScope.titleRes: Int
    get() = when (this) {
        FilterScope.FEED -> R.string.label_feed
        FilterScope.COMMENT -> R.string.label_comment
        FilterScope.USER -> R.string.label_user_card
    }
