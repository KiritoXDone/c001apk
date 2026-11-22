package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.search.SearchOrderType

@Composable
internal fun SearchPreferences(value: String, onValueChanged: (String) -> Unit) {
    PreferenceGroup(title = stringResource(R.string.settings_group_search)) {
        SelectionListItem(
            value = SearchOrderType.entries.firstOrNull { it.name == value }?.name ?: SearchOrderType.COMPREHENSIVE.name,
            title = stringResource(R.string.settings_default_search_order),
            selections = SearchOrderType.entries.map { SelectionItem(searchOrderLabel(it), it.name) },
            onSelected = onValueChanged,
        )
    }
}

@Composable
private fun searchOrderLabel(type: SearchOrderType): String =
    stringResource(
        when (type) {
            SearchOrderType.COMPREHENSIVE -> R.string.search_order_comprehensive
            SearchOrderType.DATELINE -> R.string.search_order_dateline
            SearchOrderType.HOT -> R.string.search_order_hot
            SearchOrderType.REPLY -> R.string.search_order_reply
            SearchOrderType.EXACT -> R.string.search_order_exact
        },
    )
